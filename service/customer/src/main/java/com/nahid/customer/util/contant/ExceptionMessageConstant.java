package com.nahid.customer.util.contant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constants for exception messages used across the application.
 * Contains both general templates and entity-specific messages.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionMessageConstant {

    // General templates that work for any entity
    public static final String ENTITY_NOT_FOUND_BY_ID = "%s not found with id: %s";
    public static final String ENTITY_NOT_FOUND_BY_FIELD = "%s not found with %s: %s";
    public static final String ENTITY_ALREADY_EXISTS = "%s already exists with %s: %s";
    public static final String UNIQUE_FIELD_VIOLATION = "%s must be unique";
    public static final String ERROR_OCCURRED = "%s error occurred due to %s";
    public static final String ILLEGAL_OBJECT = "Illegal object: %s";
    public static final String INVALID_REQUEST = "Invalid request: %s";
    public static final String INVALID_REQUEST_TEMPLATE = "Invalid request: %s. %s";
    public static final String STATUS_CHANGE_ERROR = "Cannot change %s status from %s to %s";

    // Customer-specific constants (only for cases that don't fit general templates)
    public static final String CUSTOMER_VERIFICATION_FAILED = "Customer verification failed: %s";
    public static final String CUSTOMER_ACCOUNT_LOCKED = "Customer account is locked. Please contact support.";
    public static final String CUSTOMER_ACCOUNT_DISABLED = "Customer account is disabled.";
}
