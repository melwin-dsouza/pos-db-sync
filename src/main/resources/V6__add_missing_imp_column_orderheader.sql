ALTER TABLE order_headers ADD COLUMN discount_basis VARCHAR(10);
ALTER TABLE order_headers ADD COLUMN discount_amount_used DECIMAL(12, 2);
ALTER TABLE order_headers ADD COLUMN cash_gratuity DECIMAL(12, 2);


----

UPDATE order_headers SET discount_basis='1' where discount_amount is not null
--
UPDATE order_headers
SET discount_amount_used = ROUND((amount_due * discount_amount) / (100.0 - discount_amount), 2)
WHERE  discount_amount is not null;
--
UPDATE order_headers h SET cash_gratuity=(select sum(employee_comp) from order_payments p where h.order_id=p.order_id)

UPDATE order_headers h SET cash_gratuity=(select sum(employee_comp) from on_account_charges p where h.order_id=p.order_id)
where h.cash_gratuity is null or h.cash_gratuity <=0.00

