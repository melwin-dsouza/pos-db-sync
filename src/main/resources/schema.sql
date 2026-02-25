-- Multi-tenant POS Database Schema
-- Single database with restaurant_id isolation

-- Restaurant table
CREATE TABLE IF NOT EXISTS restaurant (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    api_key VARCHAR(32) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

-- Users table (restaurant owners/managers)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id UUID NOT NULL REFERENCES restaurant(id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MANAGER',
    must_change_password BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(restaurant_id, email)
);

-- Order Headers table
CREATE TABLE IF NOT EXISTS order_headers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id UUID NOT NULL REFERENCES restaurant(id) ON DELETE CASCADE,
    order_id INTEGER NOT NULL,
    order_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
    employee_id INTEGER,
    station_id INTEGER,
    order_type VARCHAR(50),
    dine_in_table_id INTEGER,
    driver_employee_id INTEGER,
    discount_id INTEGER,
    discount_amount NUMERIC(12, 2),
    amount_due NUMERIC(12, 2),
    cash_discount_amount NUMERIC(12, 2),
    cash_discount_approval_emp_id INTEGER,
    sub_total NUMERIC(12, 2),
    guest_number INTEGER,
    edit_timestamp TIMESTAMP WITH TIME ZONE,
    row_guid VARCHAR(36),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(restaurant_id, order_id)
);

-- Order Payments table
CREATE TABLE IF NOT EXISTS order_payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id UUID NOT NULL REFERENCES restaurant(id) ON DELETE CASCADE,
    order_payment_id INTEGER NOT NULL,
    payment_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
    cashier_id INTEGER,
    non_cashier_employee_id INTEGER,
    order_id INTEGER NOT NULL,
    payment_method VARCHAR(50),
    amount_tendered NUMERIC(12, 2),
    amount_paid NUMERIC(12, 2),
    employee_comp NUMERIC(12, 2),
    row_guid VARCHAR(36),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(restaurant_id, order_payment_id)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_restaurant_id ON restaurant(id);
CREATE INDEX IF NOT EXISTS idx_users_restaurant_id ON users(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_order_headers_restaurant_id ON order_headers(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_order_headers_order_id ON order_headers(order_id);
CREATE INDEX IF NOT EXISTS idx_order_headers_date ON order_headers(order_date_time);
CREATE INDEX IF NOT EXISTS idx_order_payments_restaurant_id ON order_payments(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_order_payments_order_id ON order_payments(order_id);
CREATE INDEX IF NOT EXISTS idx_order_payments_date ON order_payments(payment_date_time);

