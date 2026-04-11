-- Sample script to set up subscription data for testing
-- This is for development/testing purposes only

-- Update existing user_restaurants records with subscription data (example)
-- Replace user_ids and restaurant_ids with actual values from your database

/*
-- Example: Set up a monthly subscription that expires in 30 days
UPDATE user_restaurants
SET
    subscription_type = 'MONTHLY',
    subscription_start_date = CURRENT_TIMESTAMP,
    subscription_expiry_date = CURRENT_TIMESTAMP + INTERVAL '30 days',
    is_subscription_active = true,
    created_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE user_id = 'user-uuid-here' AND restaurant_id = 'restaurant-uuid-here';

-- Example: Set up a yearly subscription that expires in 365 days
UPDATE user_restaurants
SET
    subscription_type = 'YEARLY',
    subscription_start_date = CURRENT_TIMESTAMP,
    subscription_expiry_date = CURRENT_TIMESTAMP + INTERVAL '365 days',
    is_subscription_active = true,
    created_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE user_id = 'another-user-uuid-here' AND restaurant_id = 'another-restaurant-uuid-here';

-- Example: Set up an expired subscription for testing
UPDATE user_restaurants
SET
    subscription_type = 'MONTHLY',
    subscription_start_date = CURRENT_TIMESTAMP - INTERVAL '60 days',
    subscription_expiry_date = CURRENT_TIMESTAMP - INTERVAL '30 days',
    is_subscription_active = false,
    created_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE user_id = 'expired-user-uuid-here' AND restaurant_id = 'expired-restaurant-uuid-here';
*/
