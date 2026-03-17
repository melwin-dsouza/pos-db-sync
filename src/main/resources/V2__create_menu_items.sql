CREATE TABLE IF NOT EXISTS menu_items (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id UUID NOT NULL REFERENCES restaurant(id),
    menu_item_id INTEGER NOT NULL,
    menu_item_text VARCHAR(200) NOT NULL,
    menu_category_id INTEGER,
    menu_group_id INTEGER,
    display_index INTEGER,
    default_unit_price NUMERIC(100,2),
    menu_item_description VARCHAR(500),
    menu_item_notification VARCHAR(10),
    menu_item_in_active BOOLEAN,
    menu_item_in_stock BOOLEAN,
    menu_item_discountable BOOLEAN,
    menu_item_type VARCHAR(10),
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    UNIQUE(restaurant_id, menu_item_id)
);


CREATE INDEX IF NOT EXISTS idx_menu_items_restaurant_id ON menu_items(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_menu_items_item_id ON menu_items(menu_item_id);