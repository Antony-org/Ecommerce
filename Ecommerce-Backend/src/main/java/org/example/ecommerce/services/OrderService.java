package org.example.ecommerce.services;

import org.example.ecommerce.dtos.OrderResponseDTO;
import org.example.ecommerce.dtos.OrderRequestDTO;
import org.example.ecommerce.dtos.payment.PaymentDTO;
import org.example.ecommerce.exceptionHandling.exception.BadRequestException;
import org.example.ecommerce.mappers.OrderMapper;
import org.example.ecommerce.models.*;
import org.example.ecommerce.repositories.OrderItemRepository;
import org.example.ecommerce.repositories.OrderRepository;
import org.example.ecommerce.specifications.OrderSpecs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CartItemService cartItemService;
    private final OrderItemRepository orderItemRepository;
    private final PaymentService paymentService;
    private final CustomerService customerService;
    public OrderService(OrderRepository orderRepository, OrderMapper orderMapper, CartItemService cartItemService, OrderItemRepository orderItemRepository, PaymentService paymentService, CustomerService customerService) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.cartItemService = cartItemService;
        this.orderItemRepository = orderItemRepository;
        this.paymentService = paymentService;
        this.customerService = customerService;
    }

    public Page<OrderResponseDTO> findAll(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findAll(pageable).map(orderMapper::toDTO);
    }

    public Page<OrderResponseDTO> findAllBySpecs(Integer page, Integer size,
                                                 Map<String, Object> filter) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Order> specification = Specification.where(null);
        if (filter.get("startDate") != null &&
                filter.get("startDate") instanceof LocalDateTime) {
            specification = specification.and(OrderSpecs.hasDateAfter(
                    (LocalDateTime) filter.get("startDate")));
        }

        if (filter.get("endDate") != null &&
                filter.get("endDate") instanceof LocalDateTime) {
            specification = specification.and(OrderSpecs.hasDateBefore(
                    (LocalDateTime) filter.get("endDate")));
        }

        if (filter.get("minPrice") != null &&
                filter.get("minPrice") instanceof Integer) {
            specification = specification.and(OrderSpecs.hasPriceGreaterThan(
                    (Integer) filter.get("minPrice")));
        }

        if (filter.get("maxPrice") != null &&
                filter.get("maxPrice") instanceof Integer) {
            specification = specification.and(OrderSpecs.hasPriceLessThan(
                    (Integer) filter.get("maxPrice")));
        }

        if (filter.get("state") != null &&
                filter.get("state") instanceof OrderState) {
            specification = specification.and(OrderSpecs.hasState(
                    (OrderState) filter.get("state")));
        }

        if (filter.get("customerId") != null &&
                filter.get("customerId") instanceof Long) {
            specification = specification.and(OrderSpecs.hasCustomerId(
                    (Long) filter.get("customerId")));
        }

        return orderRepository.findAll(specification, pageable).map(orderMapper::toDTO);

    }

    public Page<OrderResponseDTO> findByCustomerId(Integer page, Integer size,
                                                   Long customerId) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findByCustomerId(customerId, pageable).map(orderMapper::toDTO);
    }

    public void updateOrderState(Long orderId, OrderState orderState) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isPresent()) {
            order.get().setState(orderState);
            orderRepository.save(order.get());
        }
    }

    @Transactional
    public Optional<OrderResponseDTO> initOrder(OrderRequestDTO orderRequestDTO) {



        //Retrieve current customer
        Customer customer =
                customerService.findUserByEmail(
                        (String) ((Jwt)SecurityContextHolder.getContext()
                                    .getAuthentication().getPrincipal()).getClaims().get(
                                        "sub"));

        List<CartItem> cartItems =
                cartItemService.findCartItemsByCustomerId(customer.getId());

        //Create Order with PLACED state
        Order order = Order.builder()
                .state(OrderState.PLACED)
                .paymentMethod(orderRequestDTO.getPaymentMethod())
                .customer(customer)
                .date(LocalDateTime.now())
                .build();
        orderRepository.save(order);

        //Saving OrderItems
        List<OrderItem> orderItemList =
                cartItems.stream().collect(ArrayList::new,(set,cartItem)->{
                    if(cartItem.getProduct().getStock()<cartItem.getQuantity()) {
                        throw new BadRequestException("Product with id ("
                                + cartItem.getProduct().getId() + ")"
                                + "does not have enough stock");
                    }
                    cartItem.getProduct().setStock(cartItem.getProduct().getStock() - cartItem.getQuantity());
                    OrderItem orderItem = new OrderItem();
                    orderItem.setProduct(cartItem.getProduct());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setCurrentPrice(cartItem.getProduct().getPrice());
                    orderItem.setOrder(order);
                    set.add(orderItem);
                }, AbstractCollection::addAll);

        orderItemList = orderItemRepository.saveAll(orderItemList);

        order.setTotalPrice(orderItemList.stream().
                map((orderItem)->
                        orderItem.getCurrentPrice()
                                * orderItem.getQuantity())
                .reduce(Integer::sum)
                .orElse(0));
        Order saved = orderRepository.save(order);
        OrderResponseDTO orderResponseDTO = orderMapper.toDTO(saved);

        //Emptying Cart
        cartItemService.emptyCart(customer.getId());

        if (order.getPaymentMethod().equals(PaymentMethod.CREDIT_CARD)){
            PaymentDTO paymentDTO = new PaymentDTO(customer,order);
            String paymentLink = paymentService.generateLink(paymentDTO);
            orderResponseDTO.setPaymentLink(paymentLink);
        }

        return Optional.ofNullable(orderResponseDTO);
    }
}
