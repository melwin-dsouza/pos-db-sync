-- Migration: Add Miscellaneous Tables (OrderVoidLogs, OnAccountCharges, CustomerFiles)

-- Order Void Logs table
CREATE TABLE IF NOT EXISTS order_void_logs (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id UUID NOT NULL REFERENCES restaurant(id),
    order_id INTEGER NOT NULL,
    order_transaction_id INTEGER,
    employee_id INTEGER,
    void_reason VARCHAR(300),
    void_date_time TIMESTAMPTZ NOT NULL,
    void_for_item_reduction BOOLEAN,
    void_amount NUMERIC(12, 2),
    auto_id INTEGER,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    UNIQUE(restaurant_id, order_id)
);

-- On Account Charges table
CREATE TABLE IF NOT EXISTS on_account_charges (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id UUID NOT NULL REFERENCES restaurant(id),
    order_charge_id INTEGER NOT NULL,
    charge_date_time TIMESTAMPTZ NOT NULL,
    cashier_id INTEGER,
    non_cashier_employee_id INTEGER,
    customer_id INTEGER,
    order_id INTEGER,
    amount_charged NUMERIC(12, 2),
    order_charge_payment_id INTEGER,
    employee_comp NUMERIC(12, 2),
    charge_due_date TIMESTAMPTZ,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    UNIQUE(restaurant_id, order_charge_id)
);

-- Customer Files table
CREATE TABLE IF NOT EXISTS customer_files (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id UUID NOT NULL REFERENCES restaurant(id),
    customer_id INTEGER NOT NULL,
    customer_name VARCHAR(30),
    customer_notes VARCHAR(300),
    delivery_address VARCHAR(300),
    delivery_remarks VARCHAR(300),
    delivery_charge NUMERIC(12, 2),
    delivery_comp NUMERIC(12, 2),
    phone_number VARCHAR(15),
    company_name VARCHAR(50),
    email_address VARCHAR(100),
    customer_since_date TIMESTAMPTZ,
    total_spent NUMERIC(14, 7),
    total_count INTEGER,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    UNIQUE(restaurant_id, customer_id)
);

-- Create indexes for common queries
CREATE INDEX IF NOT EXISTS idx_order_void_logs_restaurant_id ON order_void_logs(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_order_void_logs_order_id ON order_void_logs(order_id);
CREATE INDEX IF NOT EXISTS idx_on_account_charges_restaurant_id ON on_account_charges(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_on_account_charges_order_id ON on_account_charges(order_id);
CREATE INDEX IF NOT EXISTS idx_on_account_charges_customer_id ON on_account_charges(customer_id);
CREATE INDEX IF NOT EXISTS idx_customer_files_restaurant_id ON customer_files(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_customer_files_customer_id ON customer_files(customer_id);


------------


ALTER TABLE order_void_logs
DROP CONSTRAINT order_void_logs_restaurant_id_order_id_key;

------------