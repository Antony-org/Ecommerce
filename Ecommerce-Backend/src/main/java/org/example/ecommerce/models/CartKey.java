package org.example.ecommerce.models;

import jakarta.persistence.Column;
import lombok.*;

import java.io.Serializable;


@Setter
@Getter
@NoArgsConstructor
public class CartKey implements Serializable {

    private Customer customer;
    private Product product;
}