package com.nahid.customer.util.contant;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionMessageConstant {

    public static final String ENTITY_NOT_FOUND_BY_ID = "%s not found with id: %s";
    public static final String ERROR_MESSAGE_CONSTANT = "%s error occurred due to %s";
    public static final String UNIQUE_MESSAGE_TEMPLATE = "%s must be unique";
    public static final String ENTITY_ALREADY_EXISTS_TEMPLATE = "%s already exists with %s: %s.";
    public static final String ILLEGAL_OBJECT = "Illegal object: %s";
}
