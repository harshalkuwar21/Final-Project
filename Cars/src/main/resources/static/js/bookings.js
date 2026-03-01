document.addEventListener("DOMContentLoaded", function () {
    loadBookings();
    document.getElementById("bookingForm").addEventListener("submit", function (e) {
        e.preventDefault();
        saveBooking();
    });
});

let currentDeleteId = null;

function loadBookings() {
    const tbody = document.getElementById("bookingsTableBody");
    tbody.innerHTML = '<tr><td colspan="11" class="loading"><div class="spinner"></div>Loading bookings...</td></tr>';

    fetch("/bookings/api")
        .then((response) => response.json())
        .then((bookings) => displayBookings(bookings))
        .catch((error) => {
            console.error("Error loading bookings:", error);
            tbody.innerHTML = '<tr><td colspan="11" class="empty-state"><i class="fas fa-exclamation-triangle"></i><br>Failed to load bookings</td></tr>';
        });
}

function displayBookings(bookings) {
    const tbody = document.getElementById("bookingsTableBody");
    if (bookings.length === 0) {
        tbody.innerHTML = '<tr><td colspan="11" class="empty-state"><i class="fas fa-calendar-check"></i><br>No bookings found</td></tr>';
        return;
    }

    tbody.innerHTML = bookings.map((booking) => `
        <tr>
            <td>${booking.id}</td>
            <td>${booking.customer ? booking.customer.name : "N/A"}</td>
            <td>${booking.car ? booking.car.brand + " " + booking.car.model : "N/A"}</td>
            <td>${booking.salesExecutive ? booking.salesExecutive.first + " " + booking.salesExecutive.last : "N/A"}</td>
            <td><span class="status-badge status-${statusClass(booking.workflowStatus || booking.status)}">${booking.workflowStatus || booking.status || "Pending"}</span></td>
            <td>Rs ${Number(booking.bookingAmount || 0).toLocaleString()}</td>
            <td>${booking.paymentMode || "N/A"}</td>
            <td>${booking.expectedDeliveryDate || "N/A"}</td>
            <td>${booking.bookingDate || "N/A"}</td>
            <td>${renderDocuments(booking)}</td>
            <td class="actions">
                ${showConfirmButton(booking) ? `<button class="action-btn" style="background:#2dd36f;color:#fff;" title="Confirm Booking" onclick="confirmBooking(${booking.id}, '${booking.expectedDeliveryDate || ""}')"><i class="fas fa-check"></i></button>` : ""}
                <button class="action-btn edit-btn" title="Edit" onclick="editBooking(${booking.id})">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="action-btn delete-btn" title="Delete" onclick="deleteBooking(${booking.id}, '${safeText(booking.customer ? booking.customer.name : "Unknown")}', '${safeText(booking.car ? booking.car.brand + " " + booking.car.model : "Unknown")}')">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        </tr>
    `).join("");
}

function statusClass(status) {
    return String(status || "pending").toLowerCase().replace(/\s+/g, "-");
}

function renderDocuments(booking) {
    const docs = [];
    if (booking.aadhaarPhotoUrl) docs.push(`<a href="${booking.aadhaarPhotoUrl}" target="_blank">Aadhaar</a>`);
    if (booking.panPhotoUrl) docs.push(`<a href="${booking.panPhotoUrl}" target="_blank">PAN</a>`);
    if (booking.signaturePhotoUrl) docs.push(`<a href="${booking.signaturePhotoUrl}" target="_blank">Sign</a>`);
    if (booking.passportPhotoUrl) docs.push(`<a href="${booking.passportPhotoUrl}" target="_blank">Photo</a>`);
    if (booking.paymentScreenshotUrl) docs.push(`<a href="${booking.paymentScreenshotUrl}" target="_blank">Payment</a>`);
    if (booking.bookingReceiptUrl) docs.push(`<a href="${booking.bookingReceiptUrl}" target="_blank">Receipt</a>`);
    if (booking.proformaInvoiceUrl) docs.push(`<a href="${booking.proformaInvoiceUrl}" target="_blank">Invoice</a>`);
    if (booking.allotmentLetterUrl) docs.push(`<a href="${booking.allotmentLetterUrl}" target="_blank">Allotment</a>`);
    if (booking.deliveryConfirmationLetterUrl) docs.push(`<a href="${booking.deliveryConfirmationLetterUrl}" target="_blank">Delivery</a>`);
    return docs.length ? docs.join(" | ") : "N/A";
}

function showConfirmButton(booking) {
    const s = String(booking.workflowStatus || booking.status || "").toLowerCase();
    return !["approved", "confirmed", "rejected", "cancelled", "delivered"].includes(s);
}

function confirmBooking(id, existingDeliveryDate) {
    const dateInput = existingDeliveryDate || prompt("Enter confirmed delivery date (YYYY-MM-DD), optional:", "") || "";
    const query = dateInput ? `?confirmedDeliveryDate=${encodeURIComponent(dateInput)}` : "";

    fetch(`/staff/bookings/${id}/approve${query}`, { method: "POST" })
        .then((response) => response.json())
        .then((data) => {
            if (data.ok) {
                loadBookings();
                showNotification("Booking confirmed successfully!", "success");
                return;
            }
            fallbackConfirm(id);
        })
        .catch(() => fallbackConfirm(id));
}

function fallbackConfirm(id) {
    fetch(`/bookings/api/${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ status: "Confirmed" })
    })
        .then((response) => response.json())
        .then((data) => {
            if (data.success) {
                loadBookings();
                showNotification("Booking confirmed successfully!", "success");
            } else {
                alert("Failed to confirm booking");
            }
        })
        .catch(() => alert("Failed to confirm booking"));
}

function filterBookings() {
    const statusFilter = document.getElementById("statusFilter").value.toLowerCase();
    const searchTerm = document.getElementById("searchInput").value.toLowerCase();
    const rows = document.querySelectorAll("#bookingsTableBody tr");

    rows.forEach((row) => {
        if (row.cells.length === 1) return;
        const status = row.cells[4].textContent.toLowerCase();
        const customer = row.cells[1].textContent.toLowerCase();
        const car = row.cells[2].textContent.toLowerCase();

        const statusMatch = !statusFilter || status.includes(statusFilter);
        const searchMatch = !searchTerm || customer.includes(searchTerm) || car.includes(searchTerm) || status.includes(searchTerm);
        row.style.display = statusMatch && searchMatch ? "" : "none";
    });
}

function showAddModal() {
    document.getElementById("modalTitle").textContent = "Add New Booking";
    document.getElementById("bookingForm").reset();
    document.getElementById("bookingId").value = "";
    document.getElementById("bookingModal").style.display = "block";
}

function editBooking(id) {
    fetch(`/bookings/api/${id}`)
        .then((response) => response.json())
        .then((data) => {
            if (!data.success) {
                alert("Failed to load booking details: " + (data.error || ""));
                return;
            }
            const booking = data.booking;
            document.getElementById("modalTitle").textContent = "Edit Booking";
            document.getElementById("bookingId").value = booking.id;
            document.getElementById("customerId").value = booking.customer ? booking.customer.id : "";
            document.getElementById("carId").value = booking.car ? booking.car.id : "";
            document.getElementById("salesExecutiveId").value = booking.salesExecutive ? booking.salesExecutive.userid : "";
            document.getElementById("status").value = booking.status || "Pending";
            document.getElementById("bookingAmount").value = booking.bookingAmount || 0;
            document.getElementById("paymentMode").value = booking.paymentMode || "Cash";
            document.getElementById("expectedDeliveryDate").value = booking.expectedDeliveryDate || "";
            document.getElementById("bookingModal").style.display = "block";
        })
        .catch((error) => {
            console.error("Error loading booking:", error);
            alert("Failed to load booking details");
        });
}

function saveBooking() {
    const formData = new FormData(document.getElementById("bookingForm"));
    const bookingData = Object.fromEntries(formData.entries());
    Object.keys(bookingData).forEach((key) => {
        if (bookingData[key] === "") delete bookingData[key];
    });

    const isEdit = bookingData.id;
    const url = isEdit ? `/bookings/api/${bookingData.id}` : "/bookings/api";
    const method = isEdit ? "PUT" : "POST";

    fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(bookingData)
    })
        .then((response) => response.json())
        .then((data) => {
            if (!data.success) {
                alert("Failed to save booking: " + (data.error || ""));
                return;
            }
            closeModal();
            loadBookings();
            showNotification(isEdit ? "Booking updated successfully!" : "Booking created successfully!", "success");
        })
        .catch((error) => {
            console.error("Error saving booking:", error);
            alert("Failed to save booking");
        });
}

function deleteBooking(id, customerName, carModel) {
    currentDeleteId = id;
    document.getElementById("deleteBookingInfo").textContent = `${customerName} - ${carModel}`;
    document.getElementById("deleteModal").style.display = "block";
}

function confirmDelete() {
    if (!currentDeleteId) return;

    fetch(`/bookings/api/${currentDeleteId}`, { method: "DELETE" })
        .then((response) => response.json())
        .then((data) => {
            if (!data.success) {
                alert("Failed to delete booking: " + (data.error || ""));
                return;
            }
            closeDeleteModal();
            loadBookings();
            showNotification("Booking deleted successfully!", "success");
        })
        .catch((error) => {
            console.error("Error deleting booking:", error);
            alert("Failed to delete booking");
        });
}

function closeModal() {
    document.getElementById("bookingModal").style.display = "none";
    document.getElementById("bookingForm").reset();
}

function closeDeleteModal() {
    document.getElementById("deleteModal").style.display = "none";
    currentDeleteId = null;
}

window.onclick = function (event) {
    const bookingModal = document.getElementById("bookingModal");
    const deleteModal = document.getElementById("deleteModal");
    if (event.target === bookingModal) closeModal();
    if (event.target === deleteModal) closeDeleteModal();
};

function showNotification(message, type = "info") {
    const notification = document.createElement("div");
    notification.className = `notification notification-${type}`;
    notification.innerHTML = `<i class="fas ${type === "success" ? "fa-check-circle" : "fa-exclamation-circle"}"></i>${message}`;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${type === "success" ? "#d4edda" : "#f8d7da"};
        color: ${type === "success" ? "#155724" : "#721c24"};
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
    setTimeout(() => {
        notification.style.animation = "slideOutRight 0.3s ease";
        setTimeout(() => {
            if (notification.parentNode) notification.parentNode.removeChild(notification);
        }, 300);
    }, 3000);
}

function safeText(v) {
    return String(v || "").replace(/'/g, "\\'");
}

const style = document.createElement("style");
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

