-- Sample data for manual testing in a fresh MySQL database.
-- Load this after Hibernate has created the schema.
-- If you already have data, either clear the tables first or remove explicit IDs.

-- -----------------------------------------------------
-- SHOWROOMS
-- -----------------------------------------------------
INSERT INTO showroom (id, name, city, address, contact_number, map_url, working_hours, manager_name, image_url) VALUES
(1, 'DK3 Cars Nashik', 'Nashik', 'Mumbai Naka, Nashik', '+91-9876500001', 'https://maps.example.com/nashik', '10:00 AM - 8:00 PM', 'Amit Deshpande', '/images/background.jpg'),
(2, 'DK3 Cars Pune', 'Pune', 'Baner Road, Pune', '+91-9876500002', 'https://maps.example.com/pune', '9:30 AM - 8:30 PM', 'Neha Kulkarni', '/images/background.jpg'),
(3, 'DK3 Cars Bengaluru', 'Bengaluru', 'MG Road, Bengaluru', '+91-9876500003', 'https://maps.example.com/bengaluru', '10:00 AM - 9:00 PM', 'Rohit Sharma', '/images/background.jpg');

-- -----------------------------------------------------
-- USERS
-- Password hash below is the common BCrypt hash for `password`.
-- Includes both ROLE_STAFF and ROLE_SALES_EXECUTIVE because the codebase uses both.
-- -----------------------------------------------------
INSERT INTO user (userid, first, last, email, contact, password, profile_photo_url, reset_otp, reset_otp_expiry, enabled, role, employee_id, join_date, salary, sales_target, department, active, showroom_id) VALUES
(1, 'Admin', 'User', 'admin@dk3cars.com', '+91-9000000001', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi8eJkA6Digw1k3DybZ0SqnX5nGoK2e', NULL, NULL, NULL, 1, 'ROLE_ADMIN', 'ADM001', '2025-01-01', 90000.00, 0.00, 'Administration', 1, NULL),
(2, 'Rahul', 'Verma', 'rahul.verma@dk3cars.com', '+91-9000000002', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi8eJkA6Digw1k3DybZ0SqnX5nGoK2e', NULL, NULL, NULL, 1, 'ROLE_SALES_EXECUTIVE', 'SAL001', '2025-02-10', 45000.00, 5000000.00, 'Sales', 1, 1),
(3, 'Anjali', 'Mehta', 'anjali.mehta@dk3cars.com', '+91-9000000003', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi8eJkA6Digw1k3DybZ0SqnX5nGoK2e', NULL, NULL, NULL, 1, 'ROLE_SALES_EXECUTIVE', 'SAL002', '2025-03-12', 47000.00, 5500000.00, 'Sales', 1, 2),
(4, 'Karan', 'Patil', 'karan.patil@dk3cars.com', '+91-9000000004', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi8eJkA6Digw1k3DybZ0SqnX5nGoK2e', NULL, NULL, NULL, 1, 'ROLE_STAFF', 'STF001', '2025-04-05', 38000.00, 0.00, 'Operations', 1, 1),
(5, 'Sneha', 'Iyer', 'sneha.iyer@dk3cars.com', '+91-9000000005', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi8eJkA6Digw1k3DybZ0SqnX5nGoK2e', NULL, NULL, NULL, 1, 'ROLE_ACCOUNTANT', 'ACC001', '2025-05-20', 52000.00, 0.00, 'Finance', 1, 2),
(6, 'Demo', 'Customer', 'demo.user@dk3cars.com', '+91-9000000006', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi8eJkA6Digw1k3DybZ0SqnX5nGoK2e', NULL, NULL, NULL, 1, 'ROLE_USER', 'USR001', '2025-06-01', 0.00, 0.00, 'Customer', 1, NULL);

-- -----------------------------------------------------
-- CUSTOMERS
-- -----------------------------------------------------
INSERT INTO customer (id, name, mobile, email, address, lead_source, notes, next_follow_up_date) VALUES
(1, 'Rajesh Kumar', '+91-9876543210', 'rajesh.kumar@email.com', '123 MG Road, Bengaluru, Karnataka', 'Walk-in', 'Interested in SUVs and automatic variants.', '2026-03-20'),
(2, 'Priya Sharma', '+91-9876543211', 'priya.sharma@email.com', '45 Baner Road, Pune, Maharashtra', 'Website', 'Looking for a sedan under 18 lakh.', '2026-03-18'),
(3, 'Amit Patel', '+91-9876543212', 'amit.patel@email.com', '22 College Road, Nashik, Maharashtra', 'Referral', 'Needs a family car with strong mileage.', '2026-03-22'),
(4, 'Sneha Reddy', '+91-9876543213', 'sneha.reddy@email.com', '9 Jayanagar, Bengaluru, Karnataka', 'Walk-in', 'Requested finance options and home delivery.', '2026-03-19'),
(5, 'Vikram Singh', '+91-9876543214', 'vikram.singh@email.com', '78 Koregaon Park, Pune, Maharashtra', 'Social Media', 'Interested in EV stock availability.', '2026-03-25'),
(6, 'Meera Iyer', '+91-9876543215', 'meera.iyer@email.com', '15 Indiranagar, Bengaluru, Karnataka', 'Website', 'Booked test drive for weekend.', '2026-03-17');

-- -----------------------------------------------------
-- CARS
-- -----------------------------------------------------
INSERT INTO car (
    id, brand, model, variant, fuel_type, transmission, mileage, color, price, status,
    engine_cc, safety_rating, seating_capacity, fuel_options, transmission_options,
    mileage_details, variant_details, color_options,
    review_score, review_exterior, review_performance, review_value, review_fuel_economy, review_comfort,
    faq_details, vin, engine_no, purchase_date, supplier_info, sold, stock_quantity, showroom_id
) VALUES
(1, 'Toyota', 'Fortuner', '2.8 Diesel AT', 'Diesel', 'Automatic', '14 kmpl', 'White', 3500000.00, 'Available',
 '2755 cc', '5 Star', '7 Seater', 'Diesel', 'Automatic',
 'City:11 kmpl|Highway:14 kmpl', 'Fortuner 4x2 AT~Premium SUV automatic~3500000|Fortuner Legender~Sport styling and premium interior~4200000',
 'White~#f5f5f5~/images/car.jpg|Black~#111111~/images/car.jpg',
 4.6, 4.7, 4.6, 4.4, 4.0, 4.5,
 'Does it support 4x4?~Higher variants support 4x4.|Is this a good highway SUV?~Yes, it is suited for long distance touring.',
 'VIN001TOY2026', 'ENG001TOY2026', '2026-01-10', 'Toyota India Pvt Ltd', 0, 2, 1),
(2, 'Honda', 'City', 'ZX CVT', 'Petrol', 'Automatic', '18 kmpl', 'Silver', 1550000.00, 'Available',
 '1498 cc', '5 Star', '5 Seater', 'Petrol', 'Manual, Automatic',
 'City:15 kmpl|Highway:18 kmpl', 'City V~Manual city sedan~1320000|City ZX CVT~Top automatic trim~1550000',
 'Silver~#b8bcc2~/images/car.jpg|Red~#9d1025~/images/car.jpg',
 4.4, 4.5, 4.3, 4.4, 4.6, 4.5,
 'Is Honda City good for daily commute?~Yes, it is comfortable and efficient for urban use.',
 'VIN002HON2026', 'ENG002HON2026', '2026-01-11', 'Honda Cars India Ltd', 0, 3, 2),
(3, 'Mahindra', 'Scorpio N', 'Z8 Diesel MT', 'Diesel', 'Manual', '16 kmpl', 'Black', 2100000.00, 'Reserved',
 '2198 cc', '5 Star', '7 Seater', 'Diesel', 'Manual, Automatic',
 'City:13 kmpl|Highway:16 kmpl', 'Scorpio N Z6~Base diesel SUV~1850000|Scorpio N Z8~Feature-rich diesel trim~2100000',
 'Black~#121212~/images/car.jpg|Green~#2f4f2f~/images/car.jpg',
 4.5, 4.4, 4.7, 4.3, 4.1, 4.4,
 'Is Scorpio N good for rough roads?~Yes, it handles broken roads well.',
 'VIN003MAH2026', 'ENG003MAH2026', '2026-01-12', 'Mahindra & Mahindra Ltd', 0, 1, 1),
(4, 'Hyundai', 'Creta', 'SX(O) Diesel', 'Diesel', 'Manual', '19 kmpl', 'Red', 1850000.00, 'Available',
 '1493 cc', '5 Star', '5 Seater', 'Petrol, Diesel', 'Manual, CVT, DCT',
 'City:15 kmpl|Highway:19 kmpl', 'Creta S~Popular mid variant~1550000|Creta SX(O)~Top feature variant~1850000',
 'Red~#a30d16~/images/car.jpg|Grey~#666666~/images/car.jpg',
 4.5, 4.5, 4.4, 4.3, 4.5, 4.6,
 'Does Creta have ventilated seats?~Available in higher variants.',
 'VIN004HYU2026', 'ENG004HYU2026', '2026-01-13', 'Hyundai Motor India Ltd', 0, 4, 3),
(5, 'Tata', 'Nexon EV', 'Empowered Plus', 'EV', 'Automatic', '425 km', 'Blue', 1920000.00, 'Available',
 'Electric', '5 Star', '5 Seater', 'EV', 'Automatic',
 'Claimed Range:425 km|Fast Charge:56 min', 'Creative Plus~Mid EV trim~1750000|Empowered Plus~Top EV trim~1920000',
 'Blue~#0e4b8b~/images/car.jpg|White~#fafafa~/images/car.jpg',
 4.3, 4.2, 4.3, 4.2, 4.8, 4.3,
 'What is the charging time?~Fast charging can reach 10 to 80 percent in under one hour.',
 'VIN005TAT2026', 'ENG005TAT2026', '2026-01-14', 'Tata Motors Ltd', 0, 2, 2),
(6, 'Kia', 'Carens Clavis', 'HTE Petrol 1.5L Manual 7 STR', 'Petrol', 'Manual', '16 kmpl', 'Intense Red', 1311000.00, 'Available',
 '1482 cc / 1493 cc / 1497 cc', '4 Star', '6 & 7 Seater', 'Petrol & Diesel', 'Manual, IMT, Automatic',
 'Petrol Manual:16 kmpl|Diesel Manual:19.54 kmpl', 'HTE~Entry 7 seater~1311000|HTX(O) DCT~Top automatic trim~2277000',
 'Intense Red~#fc0505~/images/car.jpg|Imperial Blue~#202151~/images/car.jpg',
 4.7, 4.8, 4.6, 4.7, 4.2, 4.9,
 'What is the on-road price?~It varies by city and taxes.|How many airbags?~Top variants offer up to 6 airbags.',
 'VIN006KIA2026', 'ENG006KIA2026', '2026-02-01', 'Kia India Pvt Ltd', 0, 5, 1),
(7, 'Maruti Suzuki', 'Brezza', 'ZXI', 'Petrol', 'Manual', '20 kmpl', 'Orange', 1250000.00, 'Sold',
 '1462 cc', '4 Star', '5 Seater', 'Petrol, CNG', 'Manual, Automatic',
 'City:16 kmpl|Highway:20 kmpl', 'LXI~Base trim~980000|ZXI~Well-equipped trim~1250000',
 'Orange~#c35a16~/images/car.jpg|Silver~#a8adb3~/images/car.jpg',
 4.2, 4.1, 4.0, 4.4, 4.7, 4.2,
 'Is Brezza available in CNG?~Yes, select variants support CNG.',
 'VIN007MS2026', 'ENG007MS2026', '2026-01-15', 'Maruti Suzuki India Ltd', 1, 0, 3);

-- -----------------------------------------------------
-- BOOKINGS
-- -----------------------------------------------------
INSERT INTO booking (
    id, customer_id, car_id, sales_executive_id, status, workflow_status, booking_amount, payment_mode, transaction_id,
    payment_gateway, payment_outcome, payment_option, down_payment_amount, down_payment_verified, down_payment_method,
    down_payment_reference, gst_amount, rto_charges, road_tax_amount, insurance_amount, fastag_charges, handling_charges,
    accessories_amount, extended_warranty_amount, tcs_amount, total_amount, paid_amount, remaining_amount, escrow_status,
    expected_delivery_date, delivery_time_slot, delivery_type, rejection_reason, booking_date, status_updated_at,
    full_name, dob, gender, aadhaar_number, pan_number, address, city, state, pin_code,
    aadhaar_photo_url, pan_photo_url, signature_photo_url, passport_photo_url,
    pre_verification_status, customer_name_matched, pre_verification_remarks, pre_verified_by, pre_verified_at,
    insurance_company_name, insurance_policy_number, insurance_document_url, insurance_generated_at,
    form20_submitted, form21_submitted, form22_submitted, invoice_submitted_to_rto, insurance_submitted_to_rto,
    rto_authority, rto_application_status, temporary_registration_number, temporary_registration_url, rto_applied_at,
    original_documents_verified, physical_verification_done, delivery_note_signed, delivery_completed_at,
    booking_receipt_url, proforma_invoice_url, allotment_letter_url, delivery_confirmation_letter_url,
    final_invoice_url, warranty_document_url, loan_document_url
) VALUES
(1, 1, 1, 2, 'Confirmed', 'Approved', 250000.00, 'Bank Transfer', 'TXN-BKG-0001',
 'Razorpay', 'Completed', 'Full Payment', 250000.00, 1, 'Bank Transfer',
 'DP-0001', 630000.00, 15000.00, 95000.00, 65000.00, 600.00, 4500.00,
 25000.00, 18000.00, 35000.00, 4383100.00, 250000.00, 4133100.00, 'Secured',
 '2026-03-25', '11:00 AM', 'Showroom Pickup', NULL, '2026-03-10', '2026-03-11 10:30:00',
 'Rajesh Kumar', '1990-04-10', 'Male', '123412341234', 'ABCDE1234F', '123 MG Road, Bengaluru', 'Bengaluru', 'Karnataka', '560001',
 '/uploads/aadhaar/rajesh.pdf', '/uploads/pan/rajesh.pdf', '/uploads/sign/rajesh.png', '/uploads/photo/rajesh.jpg',
 'Pre-Verified', 1, 'Documents matched with booking form.', 'Karan Patil', '2026-03-11 11:15:00',
 'HDFC ERGO', 'POLICY-0001', '/uploads/insurance/policy-0001.pdf', '2026-03-12 14:00:00',
 1, 1, 1, 1, 1,
 'Ministry of Road Transport and Highways', 'Applied', 'MH15TR1001', '/uploads/rto/tr-1001.pdf', '2026-03-13 15:00:00',
 0, 0, 0, NULL,
 '/uploads/docs/booking-receipt-1.pdf', '/uploads/docs/proforma-1.pdf', '/uploads/docs/allotment-1.pdf', NULL,
 NULL, NULL, NULL),
(2, 2, 2, 3, 'Pending', 'Payment Verified', 100000.00, 'Card', 'TXN-BKG-0002',
 'PayU', 'Completed', 'Loan Required', 100000.00, 1, 'Card',
 'DP-0002', 279000.00, 10000.00, 42000.00, 35000.00, 600.00, 3000.00,
 12000.00, 10000.00, 15500.00, 2057100.00, 100000.00, 1957100.00, 'Secured',
 '2026-03-28', '4:00 PM', 'Showroom Pickup', NULL, '2026-03-09', '2026-03-10 16:45:00',
 'Priya Sharma', '1994-09-22', 'Female', '567856785678', 'PQRSX6789L', '45 Baner Road, Pune', 'Pune', 'Maharashtra', '411045',
 '/uploads/aadhaar/priya.pdf', '/uploads/pan/priya.pdf', '/uploads/sign/priya.png', '/uploads/photo/priya.jpg',
 'Pre-Verified', 1, 'Customer verified, awaiting final approval.', 'Sneha Iyer', '2026-03-10 17:00:00',
 NULL, NULL, NULL, NULL,
 0, 0, 0, 0, 0,
 'Ministry of Road Transport and Highways', 'Not Applied', NULL, NULL, NULL,
 0, 0, 0, NULL,
 '/uploads/docs/booking-receipt-2.pdf', '/uploads/docs/proforma-2.pdf', NULL, NULL,
 NULL, NULL, '/uploads/docs/loan-2.pdf'),
(3, 4, 5, 2, 'Pending', 'Pending', 50000.00, 'UPI', 'TXN-BKG-0003',
 'PhonePe', 'Pending', 'Loan Required', 50000.00, 0, 'UPI',
 'DP-0003', 345600.00, 8000.00, 25000.00, 42000.00, 600.00, 2500.00,
 0.00, 8000.00, 19200.00, 2360900.00, 50000.00, 2310900.00, 'Secured',
 '2026-04-02', '1:00 PM', 'Home Delivery', NULL, '2026-03-12', '2026-03-12 13:10:00',
 'Sneha Reddy', '1996-01-18', 'Female', '111122223333', 'LMNOP4567Q', '9 Jayanagar, Bengaluru', 'Bengaluru', 'Karnataka', '560041',
 '/uploads/aadhaar/sneha.pdf', '/uploads/pan/sneha.pdf', '/uploads/sign/sneha.png', '/uploads/photo/sneha.jpg',
 'Pending', 0, 'Pending document verification.', NULL, NULL,
 NULL, NULL, NULL, NULL,
 0, 0, 0, 0, 0,
 'Ministry of Road Transport and Highways', 'Not Applied', NULL, NULL, NULL,
 0, 0, 0, NULL,
 NULL, NULL, NULL, NULL,
 NULL, NULL, '/uploads/docs/loan-3.pdf'),
(4, 6, 6, 2, 'Cancelled', 'Rejected', 25000.00, 'Cash', 'TXN-BKG-0004',
 'Cash', 'Refunded', 'Full Payment', 25000.00, 1, 'Cash',
 'DP-0004', 235980.00, 9000.00, 30000.00, 30000.00, 600.00, 2500.00,
 0.00, 0.00, 13110.00, 1648190.00, 25000.00, 1623190.00, 'Released',
 NULL, NULL, 'Showroom Pickup', 'Customer postponed purchase.', '2026-03-08', '2026-03-09 12:00:00',
 'Meera Iyer', '1998-05-11', 'Female', '444455556666', 'TUVWX1234Z', '15 Indiranagar, Bengaluru', 'Bengaluru', 'Karnataka', '560038',
 '/uploads/aadhaar/meera.pdf', '/uploads/pan/meera.pdf', '/uploads/sign/meera.png', '/uploads/photo/meera.jpg',
 'Rejected', 0, 'Customer requested cancellation.', 'Karan Patil', '2026-03-09 11:40:00',
 NULL, NULL, NULL, NULL,
 0, 0, 0, 0, 0,
 'Ministry of Road Transport and Highways', 'Not Applied', NULL, NULL, NULL,
 0, 0, 0, NULL,
 '/uploads/docs/booking-receipt-4.pdf', NULL, NULL, NULL,
 NULL, NULL, NULL);

-- -----------------------------------------------------
-- SALES
-- -----------------------------------------------------
INSERT INTO sale (id, customer_id, car_id, sales_executive_id, buyer_name, sold_date, selling_price, discount, gst_amount, total_amount, payment_mode, status) VALUES
(1, 3, 7, 3, 'Amit Patel', '2026-03-05', 1250000.00, 25000.00, 220500.00, 1445500.00, 'Bank Transfer', 'Paid');

-- -----------------------------------------------------
-- PAYMENTS
-- -----------------------------------------------------
INSERT INTO payment (id, sale_id, booking_id, amount, payment_method, payment_date, status, transaction_id) VALUES
(1, NULL, 1, 250000.00, 'Bank Transfer', '2026-03-10', 'Completed', 'TXN-BKG-0001'),
(2, NULL, 2, 100000.00, 'Card', '2026-03-09', 'Completed', 'TXN-BKG-0002'),
(3, NULL, 3, 50000.00, 'UPI', '2026-03-12', 'Pending', 'TXN-BKG-0003'),
(4, 1, NULL, 1445500.00, 'Bank Transfer', '2026-03-05', 'Completed', 'TXN-SALE-0001');

-- -----------------------------------------------------
-- TEST DRIVES
-- -----------------------------------------------------
INSERT INTO test_drive (id, customer_id, car_id, sales_executive_id, status, scheduled_date_time, completed_date_time, feedback, converted_to_sale) VALUES
(1, 2, 2, 3, 'Completed', '2026-03-07 11:00:00', '2026-03-07 11:40:00', 'Customer liked the comfort and CVT drive quality.', 0),
(2, 5, 5, 3, 'Scheduled', '2026-03-18 15:30:00', NULL, NULL, 0),
(3, 6, 6, 2, 'Cancelled', '2026-03-08 10:00:00', NULL, 'Customer requested a later slot.', 0);

-- -----------------------------------------------------
-- DOCUMENTS
-- -----------------------------------------------------
INSERT INTO document (id, document_type, file_name, file_path, file_url, car_id, customer_id, expiry_date, upload_date) VALUES
(1, 'Invoice', 'invoice-1.pdf', 'uploads/docs/invoice-1.pdf', '/uploads/docs/invoice-1.pdf', 7, 3, NULL, '2026-03-05'),
(2, 'Insurance', 'insurance-1.pdf', 'uploads/docs/insurance-1.pdf', '/uploads/docs/insurance-1.pdf', 1, 1, '2027-03-12', '2026-03-12'),
(3, 'CustomerID', 'priya-pan.pdf', 'uploads/docs/priya-pan.pdf', '/uploads/docs/priya-pan.pdf', NULL, 2, NULL, '2026-03-09');

-- -----------------------------------------------------
-- SERVICE RECORDS
-- -----------------------------------------------------
INSERT INTO service_record (id, car_id, service_date, service_type, description, cost, serviced_by, next_service_date, warranty_expiry_date) VALUES
(1, 7, '2026-03-10', 'Regular', 'Pre-delivery inspection and detailing completed.', 3500.00, 'DK3 Service Center', '2026-09-10', '2029-03-05'),
(2, 1, '2026-03-14', 'Accessory Fitment', 'Installed seat covers, dashcam, and floor mats.', 12500.00, 'DK3 Accessories Hub', '2026-09-14', '2029-03-25');

-- -----------------------------------------------------
-- NOTIFICATIONS
-- -----------------------------------------------------
INSERT INTO notification (id, title, message, type, read_flag, link, created_at) VALUES
(1, 'Booking Approved', 'Booking #1 has moved to the approved stage.', 'INFO', 0, '/bookings', '2026-03-11 10:30:00'),
(2, 'Pending Finance Review', 'Booking #3 is awaiting finance verification.', 'WARNING', 0, '/finance', '2026-03-12 13:30:00'),
(3, 'Vehicle Delivered', 'Sale #1 has been marked as paid and delivered.', 'ALERT', 1, '/sales', '2026-03-05 18:00:00');

-- -----------------------------------------------------
-- SETTINGS
-- -----------------------------------------------------
INSERT INTO setting (id, name, value) VALUES
(1, 'siteTitle', 'DK3 Cars'),
(2, 'currency', 'INR'),
(3, 'contactEmail', 'support@dk3cars.com'),
(4, 'maintenanceMode', 'false');

-- -----------------------------------------------------
-- BANK DETAILS
-- -----------------------------------------------------
INSERT INTO bank_details (id, bank_name, account_holder_name, account_number_masked, ifsc_code, branch_name, active) VALUES
(1, 'State Bank of India', 'DK3 Cars Pvt Ltd', 'XXXXXX5678', 'SBIN0000456', 'Nashik Main', 1),
(2, 'HDFC Bank', 'DK3 Cars Pvt Ltd', 'XXXXXX4321', 'HDFC0001984', 'Baner', 1);

-- -----------------------------------------------------
-- VERIFICATION TOKENS
-- -----------------------------------------------------
INSERT INTO verification_token (id, token, user_id, expiry_date) VALUES
(1, 'VERIF-TOKEN-ADMIN-001', 1, '2026-03-31 00:00:00'),
(2, 'VERIF-TOKEN-DEMO-001', 6, '2026-03-31 00:00:00');
