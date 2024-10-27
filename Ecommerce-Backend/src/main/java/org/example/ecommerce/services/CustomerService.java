package org.example.ecommerce.services;

import org.example.ecommerce.models.Customer;
import org.example.ecommerce.models.Role;
import org.example.ecommerce.repositories.CustomerRepository;
import org.example.ecommerce.system.exceptions.ObjectNotFoundException;
import org.example.ecommerce.system.exceptions.ValidationException;
import org.example.ecommerce.system.validations.UserValidator;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService implements UserDetailsService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator userValidator;

    public CustomerService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder, UserValidator userValidator) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.userValidator = userValidator;
    }

    //implement crud operations
    public Customer save(Customer customer) {
        System.out.println("inside save: " + customer);

        List<String> errors = userValidator.validateCustomer(customer);
        if (!errors.isEmpty()) {
            System.out.println("Errors: " + errors);
            throw new ValidationException(errors);
        }

        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        customer.setRole(Role.ROLE_USER);

        return this.customerRepository.save(customer);
    }

    public void delete(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Customer", id));
        customerRepository.delete(customer);
    }

    public Customer findById(Long id) {
        //if not found throw ObjectNotFoundException
        return customerRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Customer", id));
    }

    public Customer findUserByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new ObjectNotFoundException(email));
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Customer update(Long id, Customer newCustomer) {
        Customer foundCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Customer", id));
        foundCustomer.setFirstName(newCustomer.getFirstName());
        foundCustomer.setMiddleName(newCustomer.getMiddleName());
        foundCustomer.setLastName(newCustomer.getLastName());
        foundCustomer.setEmail(newCustomer.getEmail());
        foundCustomer.setPhone(newCustomer.getPhone());
        foundCustomer.setAddress(newCustomer.getAddress());
        foundCustomer.setDateOfBirth(newCustomer.getDateOfBirth());
        foundCustomer.setActive(newCustomer.isActive());

        return customerRepository.save(foundCustomer);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void changePassword(Customer user, String oldPassword, String newPassword) {
        List<String> validationErrors = userValidator.validateChangePassword(user, oldPassword, newPassword);
        if (!validationErrors.isEmpty()) {
            throw new ValidationException(validationErrors);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        customerRepository.save(user);
    }

//    @Override
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        return this.customerRepository.findByEmail(email)
//                .map(customer -> new MyUserPrincipal(customer))
//                .orElseThrow(() -> new UsernameNotFoundException("Customer not found with email: " + email));
//    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found with email: " + email));

        if (customer.getProvider() != null) {
            throw new UsernameNotFoundException("Customer registered with OAuth provider: " + customer.getProvider());
        }

        return new MyUserPrincipal(customer);
    }

    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }
}
