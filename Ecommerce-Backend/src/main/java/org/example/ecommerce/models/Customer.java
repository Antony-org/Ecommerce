package org.example.ecommerce.models;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "customer")
@Getter
@Setter
@Valid
@NoArgsConstructor
public class Customer extends User{
    @OneToOne(cascade = CascadeType.ALL)
    private Address address;

    private String phone;

    @NotNull
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @OneToMany(mappedBy = "customer")
    private Set<CartItem> cart = new HashSet<>();

    @OneToMany(mappedBy = "customer")
    private Set<Order> orders = new HashSet<>();

    boolean isActive;

    @OneToMany(mappedBy = "customer")
    private Set<CreditCard> creditCard;
}
