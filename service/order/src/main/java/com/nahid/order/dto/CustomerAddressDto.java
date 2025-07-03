package com.nahid.order.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerAddressDto {
    private String id;
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private boolean isDefault;
}
