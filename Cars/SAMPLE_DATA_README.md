# Sample Data Instructions

## How to Load Sample Data

### Option 1: Using MySQL Command Line
```bash
mysql -u your_username -p your_database_name < sample_data.sql
```

### Option 2: Using Spring Boot data.sql
1. Copy the contents of `sample_data.sql` to `src/main/resources/data.sql`
2. Make sure your application.properties has:
   ```
   spring.jpa.defer-datasource-initialization=true
   spring.sql.init.mode=always
   ```
3. Restart your application

### Option 3: Using H2 Console (if using H2 database)
1. Start your application
2. Go to http://localhost:8080/h2-console
3. Run the SQL statements from `sample_data.sql`

## Sample Data Overview

### Customers (8 records)
- Rajesh Kumar, Priya Sharma, Amit Patel, Sneha Reddy, Vikram Singh
- Deepak Jain, Meera Iyer, Ravi Kumar

### Cars (10 records)
- Toyota Fortuner, Honda City, Mahindra Scorpio, Hyundai Creta
- Tata Nexon, Kia Seltos, Maruti Suzuki Vitara Brezza, Ford EcoSport
- Volkswagen Taigun, Renault Kwid

### Sales Executives (4 records)
- Rahul Verma, Anjali Mehta, Suresh Nair, Kavita Sharma

### Bookings (11 records)
- Mix of Pending, Confirmed, and Cancelled statuses
- Various payment modes: Cash, Credit Card, Bank Transfer, Cheque
- Different expected delivery dates
- Booking amounts ranging from ₹500,000 to ₹3,500,000

## Testing Scenarios

### 1. View All Bookings
- Navigate to `/bookings`
- Should display all 11 bookings in a table

### 2. Filter by Status
- Use the status dropdown to filter bookings
- Test "Pending" (4 records), "Confirmed" (5 records), "Cancelled" (1 record)

### 3. Search Functionality
- Search by customer name: "Rajesh" should show 2 bookings
- Search by car model: "City" should show 1 booking
- Search by status: "Confirmed" should show 5 bookings

### 4. Add New Booking
- Click "Add New Booking"
- Fill form with sample data
- Submit and verify it appears in the table

### 5. Edit Booking
- Click edit button on any booking
- Modify details and save
- Verify changes are reflected

### 6. Delete Booking
- Click delete button on any booking
- Confirm deletion
- Verify booking is removed from table

### 7. Form Validation
- Try submitting empty form (should show validation errors)
- Try invalid dates (past dates for delivery)

## Expected Results

After loading sample data, you should see:
- **Dashboard**: Updated stats showing bookings and revenue
- **Bookings Page**: Full table with 11 bookings
- **Real-time Updates**: AJAX calls working without page refresh
- **Responsive Design**: Working on mobile and desktop
- **Status Colors**: Green for Confirmed, Yellow for Pending, Red for Cancelled

## Troubleshooting

If data doesn't appear:
1. Check database connection
2. Verify table creation (check if entities are properly mapped)
3. Check application logs for errors
4. Ensure foreign key constraints are satisfied

If AJAX calls fail:
1. Check browser console for JavaScript errors
2. Verify controller endpoints are accessible
3. Check CORS settings if needed