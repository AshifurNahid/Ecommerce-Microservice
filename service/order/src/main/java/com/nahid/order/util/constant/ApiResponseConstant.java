package com.nahid.order.util.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponseConstant {

    // Generic CRUD Messages
    public static final String FETCH_SUCCESSFUL = "%s fetched successfully";
    public static final String CREATE_SUCCESSFUL = "%s created successfully";
    public static final String UPDATE_SUCCESSFUL = "%s updated successfully";
    public static final String DELETE_SUCCESSFUL = "%s deleted successfully";
    public static final String FETCH_ALL_SUCCESSFUL = "All %s retrieved successfully";
    public static final String STATUS_UPDATE_SUCCESSFUL = "%s status updated to %s successfully";

    // Order-specific Messages
    public static final String ORDER_CREATED_SUCCESSFULLY = "Order created successfully";
    public static final String ORDER_FETCHED_SUCCESSFULLY = "Order fetched successfully";
    public static final String ORDER_UPDATED_SUCCESSFULLY = "Order updated successfully";
    public static final String ORDER_CANCELLED_SUCCESSFULLY = "Order cancelled successfully";
    public static final String ORDER_STATUS_UPDATED = "Order status updated successfully";
    public static final String ORDERS_FETCHED_SUCCESSFULLY = "Orders fetched successfully";
    public static final String ORDERS_BY_USER_FETCHED = "Orders for user fetched successfully";
    public static final String ORDERS_BY_STATUS_FETCHED = "Orders with status %s fetched successfully";
    public static final String ORDER_COUNT_FETCHED = "Order count fetched successfully";

    // Order Processing Messages
    public static final String ORDER_PROCESSING_INITIATED = "Order processing initiated";
    public static final String ORDER_VALIDATION_SUCCESSFUL = "Order validation completed successfully";
    public static final String ORDER_PAYMENT_PROCESSED = "Order payment processed successfully";
    public static final String ORDER_SHIPPED = "Order shipped successfully";
    public static final String ORDER_DELIVERED = "Order delivered successfully";

    // Search and Filter Messages
    public static final String SEARCH_RESULTS_FETCHED = "Search results fetched successfully";
    public static final String FILTERED_RESULTS_FETCHED = "Filtered results fetched successfully";
    public static final String ORDER_HISTORY_FETCHED = "Order history fetched successfully";
}
