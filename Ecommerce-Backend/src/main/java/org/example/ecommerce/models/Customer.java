package org.example.ecommerce.models;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.ecommerce.system.validations.ValidAddress;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

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

//    @ValidAddress
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private Address address;

    private String phone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @OneToMany(mappedBy = "customer")
    @Fetch(FetchMode.SUBSELECT)
    private Set<CartItem> cart = new HashSet<>();

    @OneToMany(mappedBy = "customer")
    @Fetch(FetchMode.SUBSELECT)
    private Set<Order> orders = new HashSet<>();

    private OAuthProvider provider;

    boolean isActive;

    @OneToMany(mappedBy = "customer")
    private Set<CreditCard> creditCard;
}
