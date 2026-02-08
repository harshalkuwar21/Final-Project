-- Test Queries to Verify Sample Data

-- Count records in each table
SELECT 'Customers' as table_name, COUNT(*) as count FROM customer
UNION ALL
SELECT 'Cars', COUNT(*) FROM car
UNION ALL
SELECT 'Users', COUNT(*) FROM user
UNION ALL
SELECT 'Bookings', COUNT(*) FROM booking;

-- Check bookings with customer and car details
SELECT
    b.id,
    b.booking_date,
    b.status,
    c.name as customer_name,
    CONCAT(car.brand, ' ', car.model) as car_model,
    b.booking_amount,
    b.payment_mode,
    b.expected_delivery_date
FROM booking b
LEFT JOIN customer c ON b.customer_id = c.id
LEFT JOIN car car ON b.car_id = car.id
ORDER BY b.booking_date DESC;

-- Check bookings by status
SELECT status, COUNT(*) as count
FROM booking
GROUP BY status;

-- Check available cars
SELECT brand, model, price, status
FROM car
WHERE status = 'Available'
ORDER BY price;

-- Check sales executives
SELECT CONCAT(first, ' ', last) as name, email, sales_target
FROM user
WHERE role = 'ROLE_STAFF'
ORDER BY sales_target DESC;