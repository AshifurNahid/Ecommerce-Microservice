package com.nahid.customer.util.contant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constants for API response messages used across the application.
 * Contains both general templates and entity-specific messages.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponseConstant {

    // General templates that work for any entity
    public static final String FETCH_SUCCESSFUL = "%s fetched successfully";
    public static final String CREATE_SUCCESSFUL = "%s created successfully";
    public static final String UPDATE_SUCCESSFUL = "%s updated successfully";
    public static final String DELETE_SUCCESSFUL = "%s deleted successfully";
    public static final String FETCH_ALL_SUCCESSFUL = "All %s retrieved successfully";
    public static final String STATUS_UPDATE_SUCCESSFUL = "%s status updated to %s successfully";

    // Search and filter constants
    public static final String SEARCH_RESULTS_FETCHED = "Search results fetched successfully";
    public static final String STATUS_FILTERED_RESULTS = "Customers with status %s fetched successfully";

    // Customer-specific constants (only for unique cases)
    public static final String CUSTOMER_VERIFICATION_SUCCESS = "Customer verified successfully";
    public static final String CUSTOMER_PROFILE_COMPLETE = "Customer profile is complete";
    public static final String CUSTOMER_ADDRESS_OPERATION = "Address %s successfully"; // Accepts: added, updated, deleted
}
