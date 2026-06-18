-- Migration: Add Currency and Country Details to Restaurant Table

-- Add currency_code column
ALTER TABLE restaurant ADD COLUMN IF NOT EXISTS currency_code VARCHAR(10);

-- Add country_code column
ALTER TABLE restaurant ADD COLUMN IF NOT EXISTS country_code VARCHAR(10);

-- Add country_name column
ALTER TABLE restaurant ADD COLUMN IF NOT EXISTS country_name VARCHAR(100);

-- Add indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_restaurant_currency_code ON restaurant(currency_code);
CREATE INDEX IF NOT EXISTS idx_restaurant_country_code ON restaurant(country_code);

