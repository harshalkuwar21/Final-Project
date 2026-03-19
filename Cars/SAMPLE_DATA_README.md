# Sample Data Instructions

`sample_data.sql` now matches the current JPA entities closely enough for manual testing in MySQL.

## What it seeds

- 3 showrooms
- 6 users
- 6 customers
- 7 cars
- 4 bookings with different workflow states
- 1 completed sale
- 4 payments
- 3 test drives
- 3 documents
- 2 service records
- 3 notifications
- 4 settings
- 2 bank details
- 2 verification tokens

## Login-friendly users

All seeded users use password: `password`

- `admin@dk3cars.com` / `ROLE_ADMIN`
- `rahul.verma@dk3cars.com` / `ROLE_SALES_EXECUTIVE`
- `anjali.mehta@dk3cars.com` / `ROLE_SALES_EXECUTIVE`
- `karan.patil@dk3cars.com` / `ROLE_STAFF`
- `sneha.iyer@dk3cars.com` / `ROLE_ACCOUNTANT`
- `demo.user@dk3cars.com` / `ROLE_USER`

## Load it

1. Start the app once so Hibernate creates or updates the schema.
2. Run the SQL file against the `Dk3Cars` database.

Example:

```bash
mysql -u root -p Dk3Cars < sample_data.sql
```

## Notes

- The file assumes a mostly empty database because it uses explicit IDs.
- If rows already exist, clear the tables first or remove the ID columns before importing.
- The project also auto-seeds one `Kia Carens Clavis` record from `SampleCarDataInitializer`, so avoid double-loading if that row already exists.

## Suggested test areas

- `/dashboard` for counts and revenue widgets
- `/cars` and showroom-specific inventory views
- `/bookings` for pending, approved, rejected, and payment-verified workflows
- `/finance` for completed vs pending payments
- `/sales`, `/documents`, `/notifications`, and `/settings`
