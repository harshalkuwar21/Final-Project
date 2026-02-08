-- Sample Data for Testing Bookings Management System
-- Run these INSERT statements in your database to populate test data

-- Sample Customers
INSERT INTO customer (id, name, mobile, email, address, lead_source, notes, next_follow_up_date) VALUES
(1, 'Rajesh Kumar', '+91-9876543210', 'rajesh.kumar@email.com', '123 MG Road, Bangalore, Karnataka', 'Walk-in', 'Interested in SUV models', '2026-02-15'),
(2, 'Priya Sharma', '+91-9876543211', 'priya.sharma@email.com', '456 Brigade Road, Bangalore, Karnataka', 'Website', 'Looking for sedan under 15 lakhs', '2026-02-10'),
(3, 'Amit Patel', '+91-9876543212', 'amit.patel@email.com', '789 Residency Road, Bangalore, Karnataka', 'Referral', 'Needs car for daily commute', '2026-02-20'),
(4, 'Sneha Reddy', '+91-9876543213', 'sneha.reddy@email.com', '321 Jayanagar, Bangalore, Karnataka', 'Walk-in', 'First-time car buyer', '2026-02-12'),
(5, 'Vikram Singh', '+91-9876543214', 'vikram.singh@email.com', '654 Koramangala, Bangalore, Karnataka', 'Website', 'Interested in electric vehicles', '2026-02-18');

-- Sample Showroom (required for cars)
INSERT INTO showroom (id, name, city, image_url) VALUES
(1, 'DK3 Cars Bangalore Central', 'Bangalore', '/images/showroom1.jpg');

-- Sample Cars
INSERT INTO car (id, brand, model, variant, fuel_type, transmission, color, price, status, vin, engine_no, purchase_date, supplier_info, sold, stock_quantity, showroom_id) VALUES
(1, 'Toyota', 'Fortuner', '2.8 Diesel 4x2 AT', 'Diesel', 'Automatic', 'White', 3500000.00, 'Available', 'VIN001TOY2026', 'ENG001TOY2026', '2026-01-01', 'Toyota India Pvt Ltd', false, 1, 1),
(2, 'Honda', 'City', '1.5L i-VTEC ZX CVT', 'Petrol', 'CVT', 'Silver', 1500000.00, 'Available', 'VIN002HON2026', 'ENG002HON2026', '2026-01-02', 'Honda Cars India Ltd', false, 1, 1),
(3, 'Mahindra', 'Scorpio', '2.2 mHawk 4WD', 'Diesel', 'Manual', 'Black', 1800000.00, 'Available', 'VIN003MAH2026', 'ENG003MAH2026', '2026-01-03', 'Mahindra & Mahindra Ltd', false, 1, 1),
(4, 'Hyundai', 'Creta', '1.5 Diesel SX', 'Diesel', 'Manual', 'Red', 1600000.00, 'Available', 'VIN004HYU2026', 'ENG004HYU2026', '2026-01-04', 'Hyundai Motor India Ltd', false, 1, 1),
(5, 'Tata', 'Nexon', '1.5 Petrol XM', 'Petrol', 'Manual', 'Blue', 900000.00, 'Available', 'VIN005TAT2026', 'ENG005TAT2026', '2026-01-05', 'Tata Motors Ltd', false, 1, 1),
(6, 'Kia', 'Seltos', '1.5 Petrol HTX IVT', 'Petrol', 'IVT', 'Grey', 1700000.00, 'Available', 'VIN006KIA2026', 'ENG006KIA2026', '2026-01-06', 'Kia India Pvt Ltd', false, 1, 1),
(7, 'Maruti Suzuki', 'Vitara Brezza', '1.5L VXI', 'Petrol', 'Manual', 'Orange', 950000.00, 'Available', 'VIN007MS2026', 'ENG007MS2026', '2026-01-07', 'Maruti Suzuki India Ltd', false, 1, 1),
(8, 'Ford', 'EcoSport', '1.5L Titanium Plus', 'Petrol', 'Manual', 'Green', 1100000.00, 'Available', 'VIN008FOR2026', 'ENG008FOR2026', '2026-01-08', 'Ford India Pvt Ltd', false, 1, 1);

-- Sample Sales Executives (Users with ROLE_SALES_EXECUTIVE)
INSERT INTO user (userid, first, last, email, contact, password, enabled, role, employee_id, join_date, salary, sales_target, department, active) VALUES
(1, 'Rahul', 'Verma', 'rahul.verma@dk3cars.com', '+91-9876543215', '$2a$10$encryptedpassword1', true, 'ROLE_SALES_EXECUTIVE', 'EMP001', '2025-01-15', 45000.00, 5000000.00, 'Sales', true),
(2, 'Anjali', 'Mehta', 'anjali.mehta@dk3cars.com', '+91-9876543216', '$2a$10$encryptedpassword2', true, 'ROLE_SALES_EXECUTIVE', 'EMP002', '2025-02-01', 42000.00, 4500000.00, 'Sales', true),
(3, 'Suresh', 'Nair', 'suresh.nair@dk3cars.com', '+91-9876543217', '$2a$10$encryptedpassword3', true, 'ROLE_SALES_EXECUTIVE', 'EMP003', '2025-03-10', 48000.00, 5500000.00, 'Sales', true),
(4, 'Kavita', 'Sharma', 'kavita.sharma@dk3cars.com', '+91-9876543218', '$2a$10$encryptedpassword4', true, 'ROLE_SALES_EXECUTIVE', 'EMP004', '2025-04-05', 40000.00, 4000000.00, 'Sales', true);

-- Sample Bookings
INSERT INTO booking (id, customer_id, car_id, sales_executive_id, status, booking_amount, payment_mode, expected_delivery_date, booking_date) VALUES
(1, 1, 1, 1, 'Confirmed', 3500000.00, 'Bank Transfer', '2026-02-28', '2026-01-15'),
(2, 2, 2, 2, 'Pending', 1500000.00, 'Credit Card', '2026-02-15', '2026-01-14'),
(3, 3, 3, 3, 'Confirmed', 1800000.00, 'Cash', '2026-03-01', '2026-01-13'),
(4, 4, 4, 4, 'Pending', 1600000.00, 'Cheque', '2026-02-20', '2026-01-12'),
(5, 5, 5, 1, 'Confirmed', 900000.00, 'Bank Transfer', '2026-02-25', '2026-01-11'),
(6, 1, 6, 2, 'Cancelled', 1700000.00, 'Credit Card', NULL, '2026-01-10'),
(7, 2, 7, 3, 'Pending', 950000.00, 'Cash', '2026-02-18', '2026-01-09'),
(8, 3, 8, 4, 'Confirmed', 1100000.00, 'Bank Transfer', '2026-02-22', '2026-01-08');

-- Additional sample data for testing filters and search
INSERT INTO customer (id, name, mobile, email, address, lead_source, notes, next_follow_up_date) VALUES
(6, 'Deepak Jain', '+91-9876543219', 'deepak.jain@email.com', '987 Indiranagar, Bangalore, Karnataka', 'Referral', 'Corporate client', '2026-02-25'),
(7, 'Meera Iyer', '+91-9876543220', 'meera.iyer@email.com', '147 Malleswaram, Bangalore, Karnataka', 'Walk-in', 'Needs test drive', '2026-02-14'),
(8, 'Ravi Kumar', '+91-9876543221', 'ravi.kumar@email.com', '258 Rajajinagar, Bangalore, Karnataka', 'Website', 'Budget constraint', '2026-02-16');

INSERT INTO car (id, brand, model, variant, fuel_type, transmission, color, price, status, vin, engine_no, purchase_date, supplier_info, sold, stock_quantity, showroom_id) VALUES
(9, 'Volkswagen', 'Taigun', '1.0 TSI Highline', 'Petrol', 'Automatic', 'White', 1400000.00, 'Available', 'VIN009VW2026', 'ENG009VW2026', '2026-01-09', 'Volkswagen India Pvt Ltd', false, 1, 1),
(10, 'Renault', 'Kwid', '1.0 RXT', 'Petrol', 'Manual', 'Yellow', 500000.00, 'Available', 'VIN010REN2026', 'ENG010REN2026', '2026-01-10', 'Renault India Pvt Ltd', false, 1, 1);

INSERT INTO booking (id, customer_id, car_id, sales_executive_id, status, booking_amount, payment_mode, expected_delivery_date, booking_date) VALUES
(9, 6, 9, 1, 'Pending', 1400000.00, 'Credit Card', '2026-03-05', '2026-01-16'),
(10, 7, 10, 2, 'Confirmed', 500000.00, 'Cash', '2026-02-12', '2026-01-16'),
(11, 8, 1, 3, 'Pending', 3500000.00, 'Bank Transfer', '2026-03-10', '2026-01-17');