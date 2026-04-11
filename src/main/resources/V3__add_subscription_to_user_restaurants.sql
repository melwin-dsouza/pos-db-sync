-- V3__add_subscription_to_user_restaurants.sql
-- Add subscription columns to existing user_restaurants table

ALTER TABLE user_restaurants DROP CONSTRAINT user_restaurants_pkey;

ALTER TABLE user_restaurants ADD COLUMN IF NOT EXISTS id BIGSERIAL PRIMARY KEY;


ALTER TABLE user_restaurants ADD COLUMN IF NOT EXISTS subscription_type VARCHAR(30);
ALTER TABLE user_restaurants ADD COLUMN IF NOT EXISTS subscription_start_date TIMESTAMPTZ;
ALTER TABLE user_restaurants ADD COLUMN IF NOT EXISTS subscription_expiry_date TIMESTAMPTZ;
ALTER TABLE user_restaurants ADD COLUMN IF NOT EXISTS is_subscription_active BOOLEAN DEFAULT false;
ALTER TABLE user_restaurants ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ ;
ALTER TABLE user_restaurants ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ ;

-- Add indexes for better performance
CREATE INDEX IF NOT EXISTS idx_user_restaurants_subscription_active ON user_restaurants(is_subscription_active);
CREATE INDEX IF NOT EXISTS idx_user_restaurants_expiry ON user_restaurants(subscription_expiry_date);

-- Create unique constraint if not exists
ALTER TABLE user_restaurants ADD CONSTRAINT uk_user_restaurant UNIQUE (user_id, restaurant_id);
