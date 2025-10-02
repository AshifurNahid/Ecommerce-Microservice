-- ==================================================================
-- User Service Data
-- ==================================================================

-- Insert Users
-- Note: Passwords should be hashed in a real application. This is just for testing.
INSERT INTO users (id, email, password, first_name, last_name, phone_number, role, status, enabled, account_non_expired, account_non_locked, credentials_non_expired, created_at, updated_at) VALUES
(2, 'john.doe@example.com', 'password123', 'John', 'Doe', '+11234567890', 'USER', 'ACTIVE', true, true, true, true, NOW(), NOW()),
(3, 'jane.admin@example.com', 'adminpass', 'Jane', 'Admin', '+10987654321', 'ADMIN', 'ACTIVE', true, true, true, true, NOW(), NOW());

-- Insert User Addresses
-- Note: user_id links to the users table
INSERT INTO user_addresses (id, user_id, street, city, state, postal_code, country, is_default) VALUES
(1, 2, '123 Main St', 'Anytown', 'Anystate', '12345', 'USA', true),
(2, 3, '456 Admin Ave', 'Adminville', 'Adminstate', '67890', 'USA', true);

-- ==================================================================
-- Product Service Data
-- ==================================================================

-- Insert Categories
INSERT INTO categories (id, name, description, is_active, created_at, updated_at) VALUES
(1, 'Electronics', 'Devices and gadgets', true, NOW(), NOW()),
(2, 'Books', 'Printed and digital books', true, NOW(), NOW()),
(3, 'Clothing', 'Apparel and accessories', true, NOW(), NOW()),
(4, 'Home Goods', 'Items for your home', true, NOW(), NOW());

-- Insert Products
-- Note: category_id links to the categories table
INSERT INTO products (id, name, description, sku, price, cost_price, stock_quantity, min_stock_level, brand, weight, is_active, is_featured, images_url, created_at, updated_at, category_id) VALUES
(1, 'Laptop Pro', 'A powerful laptop for professionals', 'LP-PRO-001', 1200.00, 900.00, 50, 10, 'TechBrand', 2.5, true, true, 'http://example.com/laptop.jpg', NOW(), NOW(), 1),
(2, 'Smartphone X', 'The latest smartphone with amazing features', 'SP-X-002', 800.00, 600.00, 150, 20, 'TechBrand', 0.2, true, false, 'http://example.com/phone.jpg', NOW(), NOW(), 1),
(3, 'The Galaxy Within', 'A science fiction novel', 'BK-GW-003', 15.99, 8.50, 200, 30, 'BookPublishers', 0.5, true, false, 'http://example.com/book.jpg', NOW(), NOW(), 2),
(4, 'Classic T-Shirt', 'A comfortable cotton t-shirt', 'TS-CLASSIC-004', 25.00, 10.00, 300, 50, 'FashionCo', 0.3, true, false, 'http://example.com/tshirt.jpg', NOW(), NOW(), 3),
(5, 'Denim Jeans', 'Stylish and durable denim jeans', 'JN-DENIM-005', 75.00, 30.00, 100, 20, 'FashionCo', 0.8, true, false, 'http://example.com/jeans.jpg', NOW(), NOW(), 3),
(6, 'Coffee Maker', 'Brews the perfect cup of coffee', 'CM-BREW-006', 50.00, 25.00, 80, 15, 'HomeBrand', 1.5, true, true, 'http://example.com/coffeemaker.jpg', NOW(), NOW(), 4),
(7, 'Desk Lamp', 'A modern LED desk lamp', 'DL-LED-007', 35.00, 15.00, 120, 25, 'HomeBrand', 1.0, true, false, 'http://example.com/desklamp.jpg', NOW(), NOW(), 4);

-- ==================================================================
-- Order Service Data
-- ==================================================================

-- Insert Orders
-- Note: order_id is a UUID, generate one for your database if needed.
INSERT INTO orders (order_id, order_number, user_id, status, total_amount, currency, shipping_first_name, shipping_last_name, shipping_street_address, shipping_city, shipping_state, shipping_postal_code, shipping_country, shipping_phone, created_at, updated_at, version) VALUES
('a1b2c3d4-e5f6-7890-1234-567890abcdef', 'ORD-2023-0001', 1, 'PENDING', 1215.99, 'USD', 'John', 'Doe', '123 Main St', 'Anytown', 'Anystate', '12345', 'USA', '555-1234', NOW(), NOW(), 1),
('d1e2f3g4-h5i6-7890-1234-567890abcdef', 'ORD-2023-0002', 2, 'COMPLETED', 850.00, 'USD', 'Jane', 'Admin', '456 Admin Ave', 'Adminville', 'Adminstate', '67890', 'USA', '555-5678', NOW(), NOW(), 1);

-- Insert Order Items
-- Note: order_item_id is a UUID, generate one for your database if needed.
INSERT INTO order_items (order_item_id, order_id, product_id, product_name, product_sku, quantity, unit_price, total_price, created_at) VALUES
-- Order 1 Items
('b1c2d3e4-f5g6-7890-1234-567890abcdef', 'a1b2c3d4-e5f6-7890-1234-567890abcdef', 1, 'Laptop Pro', 'LP-PRO-001', 1, 1200.00, 1200.00, NOW()),
('c1d2e3f4-g5h6-7890-1234-567890abcdef', 'a1b2c3d4-e5f6-7890-1234-567890abcdef', 3, 'The Galaxy Within', 'BK-GW-003', 1, 15.99, 15.99, NOW()),
-- Order 2 Items
('e1f2g3h4-i5j6-7890-1234-567890abcdef', 'd1e2f3g4-h5i6-7890-1234-567890abcdef', 2, 'Smartphone X', 'SP-X-002', 1, 800.00, 800.00, NOW()),
('f1g2h3i4-j5k6-7890-1234-567890abcdef', 'd1e2f3g4-h5i6-7890-1234-567890abcdef', 6, 'Coffee Maker', 'CM-BREW-006', 1, 50.00, 50.00, NOW());
