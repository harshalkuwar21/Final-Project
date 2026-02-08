// Bookings Management JavaScript
document.addEventListener('DOMContentLoaded', function() {
    loadBookings();

    // Form submission
    document.getElementById('bookingForm').addEventListener('submit', function(e) {
        e.preventDefault();
        saveBooking();
    });
});

let currentDeleteId = null;

// Load all bookings
function loadBookings() {
    const tbody = document.getElementById('bookingsTableBody');
    tbody.innerHTML = '<tr><td colspan="10" class="loading"><div class="spinner"></div>Loading bookings...</td></tr>';

    fetch('/bookings/api')
        .then(response => response.json())
        .then(bookings => {
            displayBookings(bookings);
        })
        .catch(error => {
            console.error('Error loading bookings:', error);
            tbody.innerHTML = '<tr><td colspan="10" class="empty-state"><i class="fas fa-exclamation-triangle"></i><br>Failed to load bookings</td></tr>';
        });
}

// Display bookings in table
function displayBookings(bookings) {
    const tbody = document.getElementById('bookingsTableBody');

    if (bookings.length === 0) {
        tbody.innerHTML = '<tr><td colspan="10" class="empty-state"><i class="fas fa-calendar-check"></i><br>No bookings found</td></tr>';
        return;
    }

    tbody.innerHTML = bookings.map(booking => `
        <tr>
            <td>${booking.id}</td>
            <td>${booking.customer ? booking.customer.name : 'N/A'}</td>
            <td>${booking.car ? booking.car.brand + ' ' + booking.car.model : 'N/A'}</td>
            <td>${booking.salesExecutive ? booking.salesExecutive.first + ' ' + booking.salesExecutive.last : 'N/A'}</td>
            <td><span class="status-badge status-${booking.status.toLowerCase()}">${booking.status}</span></td>
            <td>₹${booking.bookingAmount.toLocaleString()}</td>
            <td>${booking.paymentMode}</td>
            <td>${booking.expectedDeliveryDate || 'N/A'}</td>
            <td>${booking.bookingDate}</td>
            <td class="actions">
                <button class="action-btn edit-btn" onclick="editBooking(${booking.id})">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="action-btn delete-btn" onclick="deleteBooking(${booking.id}, '${booking.customer ? booking.customer.name : 'Unknown'}', '${booking.car ? booking.car.brand + ' ' + booking.car.model : 'Unknown'}')">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

// Filter bookings
function filterBookings() {
    const statusFilter = document.getElementById('statusFilter').value.toLowerCase();
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    const rows = document.querySelectorAll('#bookingsTableBody tr');

    rows.forEach(row => {
        if (row.cells.length === 1) return; // Skip loading/empty state rows

        const status = row.cells[4].textContent.toLowerCase();
        const customer = row.cells[1].textContent.toLowerCase();
        const car = row.cells[2].textContent.toLowerCase();

        const statusMatch = !statusFilter || status.includes(statusFilter);
        const searchMatch = !searchTerm ||
            customer.includes(searchTerm) ||
            car.includes(searchTerm) ||
            status.includes(searchTerm);

        row.style.display = statusMatch && searchMatch ? '' : 'none';
    });
}

// Show add booking modal
function showAddModal() {
    document.getElementById('modalTitle').textContent = 'Add New Booking';
    document.getElementById('bookingForm').reset();
    document.getElementById('bookingId').value = '';
    document.getElementById('bookingModal').style.display = 'block';
}

// Edit booking
function editBooking(id) {
    fetch(`/bookings/api/${id}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const booking = data.booking;
                document.getElementById('modalTitle').textContent = 'Edit Booking';
                document.getElementById('bookingId').value = booking.id;
                document.getElementById('customerId').value = booking.customer ? booking.customer.id : '';
                document.getElementById('carId').value = booking.car ? booking.car.id : '';
                document.getElementById('salesExecutiveId').value = booking.salesExecutive ? booking.salesExecutive.userid : '';
                document.getElementById('status').value = booking.status;
                document.getElementById('bookingAmount').value = booking.bookingAmount;
                document.getElementById('paymentMode').value = booking.paymentMode;
                document.getElementById('expectedDeliveryDate').value = booking.expectedDeliveryDate || '';
                document.getElementById('bookingModal').style.display = 'block';
            } else {
                alert('Failed to load booking details: ' + data.error);
            }
        })
        .catch(error => {
            console.error('Error loading booking:', error);
            alert('Failed to load booking details');
        });
}

// Save booking
function saveBooking() {
    const formData = new FormData(document.getElementById('bookingForm'));
    const bookingData = Object.fromEntries(formData.entries());

    // Remove empty values
    Object.keys(bookingData).forEach(key => {
        if (bookingData[key] === '') {
            delete bookingData[key];
        }
    });

    const isEdit = bookingData.id;
    const url = isEdit ? `/bookings/api/${bookingData.id}` : '/bookings/api';
    const method = isEdit ? 'PUT' : 'POST';

    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(bookingData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            closeModal();
            loadBookings();
            showNotification(isEdit ? 'Booking updated successfully!' : 'Booking created successfully!', 'success');
        } else {
            alert('Failed to save booking: ' + data.error);
        }
    })
    .catch(error => {
        console.error('Error saving booking:', error);
        alert('Failed to save booking');
    });
}

// Delete booking
function deleteBooking(id, customerName, carModel) {
    currentDeleteId = id;
    document.getElementById('deleteBookingInfo').textContent = `${customerName} - ${carModel}`;
    document.getElementById('deleteModal').style.display = 'block';
}

function confirmDelete() {
    if (!currentDeleteId) return;

    fetch(`/bookings/api/${currentDeleteId}`, {
        method: 'DELETE'
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            closeDeleteModal();
            loadBookings();
            showNotification('Booking deleted successfully!', 'success');
        } else {
            alert('Failed to delete booking: ' + data.error);
        }
    })
    .catch(error => {
        console.error('Error deleting booking:', error);
        alert('Failed to delete booking');
    });
}

// Modal functions
function closeModal() {
    document.getElementById('bookingModal').style.display = 'none';
    document.getElementById('bookingForm').reset();
}

function closeDeleteModal() {
    document.getElementById('deleteModal').style.display = 'none';
    currentDeleteId = null;
}

// Close modals when clicking outside
window.onclick = function(event) {
    const bookingModal = document.getElementById('bookingModal');
    const deleteModal = document.getElementById('deleteModal');

    if (event.target === bookingModal) {
        closeModal();
    }
    if (event.target === deleteModal) {
        closeDeleteModal();
    }
}

// Notification system
function showNotification(message, type = 'info') {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.innerHTML = `
        <i class="fas ${type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle'}"></i>
        ${message}
    `;

    // Add styles
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${type === 'success' ? '#d4edda' : '#f8d7da'};
        color: ${type === 'success' ? '#155724' : '#721c24'};
        padding: 15px 20px;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        z-index: 1001;
        display: flex;
        align-items: center;
        gap: 10px;
        animation: slideInRight 0.3s ease;
    `;

    document.body.appendChild(notification);

    // Auto remove after 3 seconds
    setTimeout(() => {
        notification.style.animation = 'slideOutRight 0.3s ease';
        setTimeout(() => {
            document.body.removeChild(notification);
        }, 300);
    }, 3000);
}

// Add notification styles
const style = document.createElement('style');
style.textContent = `
    @keyframes slideInRight {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
    @keyframes slideOutRight {
        from { transform: translateX(0); opacity: 1; }
        to { transform: translateX(100%); opacity: 0; }
    }
`;
document.head.appendChild(style);