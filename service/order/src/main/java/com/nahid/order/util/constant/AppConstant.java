package com.nahid.order.util.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AppConstant {

    // Entity Names
    public static final String ORDER = "Order";
    public static final String ORDERS = "Orders";
    public static final String ORDER_COUNT = "Order count";
    public static final String ORDER_ITEM = "OrderItem";
    public static final String USER = "User";
    public static final String PRODUCT = "Product";

    // Order Status Values
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_SHIPPED = "SHIPPED";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    // Event Types
    public static final String ORDER_CREATED_EVENT = "ORDER_CREATED";
    public static final String ORDER_UPDATED_EVENT = "ORDER_UPDATED";
    public static final String ORDER_CANCELLED_EVENT = "ORDER_CANCELLED";

    // Default Values
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final String DEFAULT_SORT_FIELD = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "desc";

    // Validation Messages
    public static final String VALIDATION_USER_ID_REQUIRED = "User ID is required";
    public static final String VALIDATION_ORDER_ITEMS_REQUIRED = "Order items cannot be empty";
    public static final String VALIDATION_SHIPPING_ADDRESS_REQUIRED = "Shipping address is required";
}
