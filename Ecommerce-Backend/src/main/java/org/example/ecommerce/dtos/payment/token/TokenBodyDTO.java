package org.example.ecommerce.dtos.payment.token;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenBodyDTO {
    private String token;
    @JsonProperty("masked_pan")
    private String maskedPan;
    private String email;
}
