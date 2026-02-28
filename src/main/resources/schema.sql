-- Multi-tenant POS Database Schema
-- Single database with restaurant_id isolation

-- Restaurant table
CREATE TABLE IF NOT EXISTS restaurant (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    keyword VARCHAR(255),
    description TEXT,
    address VARCHAR,
    phone_number VARCHAR(50),
    api_key VARCHAR(32) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    last_sync_time TIMESTAMPTZ ,
    opening_time TIME,
    closing_time TIME,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ
);

-- Users table (restaurant owners/managers)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    restaurant_id UUID REFERENCES restaurant(id) ON DELETE SET NULL,
    email VARCHAR(255) NOT NULL,
    mobile_number VARCHAR(50),
    full_name VARCHAR(255),
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'OWNER',
    must_change_password BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ
);

-- User-Restaurant many-to-many join table
CREATE TABLE IF NOT EXISTS user_restaurants (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    restaurant_id UUID NOT NULL REFERENCES restaurant(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, restaurant_id)
);

-- Order Headers table
CREATE TABLE IF NOT EXISTS order_headers (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id UUID NOT NULL REFERENCES restaurant(id),
    order_id INTEGER NOT NULL,
    order_date_time TIMESTAMPTZ,
    employee_id INTEGER,
    station_id INTEGER,
    order_type_id VARCHAR(50),
    order_type VARCHAR(50),
    dine_in_table_id INTEGER,
    customer_id INTEGER,
    delivery_charge NUMERIC(12, 2),
    driver_employee_id INTEGER,
    discount_id INTEGER,
    discount_amount NUMERIC(12, 2),
    amount_due NUMERIC(12, 2),
    cash_discount_amount NUMERIC(12, 2),
    cash_discount_approval_emp_id INTEGER,
    sub_total NUMERIC(12, 2),
    vat_rate DOUBLE PRECISION,
    vat_amount NUMERIC(12, 2),
    guest_number INTEGER,
    edit_timestamp TIMESTAMPTZ,
    row_guid VARCHAR(50),
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    UNIQUE(restaurant_id, order_id)
);

-- Order Payments table
CREATE TABLE IF NOT EXISTS order_payments (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id UUID NOT NULL REFERENCES restaurant(id) ,
    order_payment_id INTEGER NOT NULL,
    order_id INTEGER NOT NULL,
    payment_date_time TIMESTAMPTZ,
    cashier_id INTEGER,
    non_cashier_employee_id INTEGER,
    payment_method VARCHAR(50),
    amount_tendered NUMERIC(12, 2),
    amount_paid NUMERIC(12, 2),
    employee_comp NUMERIC(12, 2),
    row_guid VARCHAR(50),
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    UNIQUE(restaurant_id, order_payment_id)
);

-- Order Transactions table
CREATE TABLE IF NOT EXISTS order_transactions (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id UUID NOT NULL REFERENCES restaurant(id),
    order_transaction_id INTEGER NOT NULL,
    order_id INTEGER NOT NULL,
    menu_item_id INTEGER,
    menu_item_unit_price NUMERIC(100, 2),
    quantity NUMERIC(100, 2),
    extended_price NUMERIC(100, 2),
    discount_id INTEGER,
    discount_amount NUMERIC(100, 2),
    discount_basis VARCHAR(255),
    discount_amount_used NUMERIC(100, 2),
    row_guid VARCHAR(50),
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    UNIQUE(restaurant_id, order_transaction_id)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_restaurant_id ON restaurant(id);

CREATE INDEX IF NOT EXISTS idx_users_restaurant_id ON users(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

CREATE INDEX IF NOT EXISTS idx_user_restaurants_user_id ON user_restaurants(user_id);
CREATE INDEX IF NOT EXISTS idx_user_restaurants_restaurant_id ON user_restaurants(restaurant_id);

CREATE INDEX IF NOT EXISTS idx_order_headers_restaurant_id ON order_headers(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_order_headers_order_id ON order_headers(order_id);
CREATE INDEX IF NOT EXISTS idx_order_headers_date ON order_headers(order_date_time);

CREATE INDEX IF NOT EXISTS idx_order_payments_restaurant_id ON order_payments(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_order_payments_order_id ON order_payments(order_id);
CREATE INDEX IF NOT EXISTS idx_order_payments_date ON order_payments(payment_date_time);

CREATE INDEX IF NOT EXISTS idx_order_transactions_restaurant_id ON order_transactions(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_order_transactions_order_id ON order_transactions(order_id);
