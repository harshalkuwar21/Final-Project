-- Quick verification queries for sample_data.sql

SELECT 'showroom' AS table_name, COUNT(*) AS row_count FROM showroom
UNION ALL
SELECT 'user', COUNT(*) FROM user
UNION ALL
SELECT 'customer', COUNT(*) FROM customer
UNION ALL
SELECT 'car', COUNT(*) FROM car
UNION ALL
SELECT 'booking', COUNT(*) FROM booking
UNION ALL
SELECT 'sale', COUNT(*) FROM sale
UNION ALL
SELECT 'payment', COUNT(*) FROM payment;

SELECT
    b.id,
    b.status,
    b.workflow_status,
    c.name AS customer_name,
    CONCAT(car.brand, ' ', car.model) AS car_name,
    CONCAT(u.first, ' ', u.last) AS sales_executive,
    b.booking_amount,
    b.total_amount,
    b.paid_amount,
    b.remaining_amount,
    b.booking_date,
    b.expected_delivery_date
FROM booking b
LEFT JOIN customer c ON c.id = b.customer_id
LEFT JOIN car car ON car.id = b.car_id
LEFT JOIN user u ON u.userid = b.sales_executive_id
ORDER BY b.id;

SELECT
    p.id,
    p.amount,
    p.payment_method,
    p.status,
    p.payment_date,
    p.transaction_id,
    p.booking_id,
    p.sale_id
FROM payment p
ORDER BY p.id;

SELECT
    CONCAT(first, ' ', last) AS staff_name,
    role,
    department,
    showroom_id,
    active
FROM user
WHERE role <> 'ROLE_USER'
ORDER BY userid;

SELECT
    brand,
    model,
    status,
    sold,
    stock_quantity,
    showroom_id
FROM car
ORDER BY id;

SELECT
    status,
    workflow_status,
    COUNT(*) AS total
FROM booking
GROUP BY status, workflow_status
ORDER BY status, workflow_status;
