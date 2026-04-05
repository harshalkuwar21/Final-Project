document.addEventListener("DOMContentLoaded", function () {
    loadBookings();
    document.getElementById("bookingForm").addEventListener("submit", function (e) {
        e.preventDefault();
        saveBooking();
    });
});

let currentDeleteId = null;
let currentBookingTab = "active";

function loadBookings() {
    const tbody = document.getElementById("bookingsTableBody");
    tbody.innerHTML = '<tr><td colspan="11" class="loading"><div class="spinner"></div>Loading bookings...</td></tr>';
    const endpoint = currentBookingTab === "delivered" ? "/bookings/api/delivered" : "/bookings/api";

    fetch(endpoint)
        .then((response) => response.json())
        .then((bookings) => displayBookings(bookings))
        .catch((error) => {
            console.error("Error loading bookings:", error);
            tbody.innerHTML = '<tr><td colspan="11" class="empty-state"><i class="fas fa-exclamation-triangle"></i><br>Failed to load bookings</td></tr>';
        });
}

function displayBookings(bookings) {
    window.__bookingsCache = Array.isArray(bookings) ? bookings : [];
    const tbody = document.getElementById("bookingsTableBody");
    if (bookings.length === 0) {
        const emptyText = currentBookingTab === "delivered" ? "No delivered bookings found" : "No bookings found";
        tbody.innerHTML = `<tr><td colspan="11" class="empty-state"><i class="fas fa-calendar-check"></i><br>${emptyText}</td></tr>`;
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
            <td class="actions">${renderBookingActions(booking)}</td>
        </tr>
    `).join("");
}

function statusClass(status) {
    return String(status || "pending").toLowerCase().replace(/\s+/g, "-");
}

function renderDocuments(booking) {
    const hasAnyDocument = Boolean(
        booking.aadhaarPhotoUrl ||
        booking.panPhotoUrl ||
        booking.signaturePhotoUrl ||
        booking.passportPhotoUrl ||
        booking.paymentScreenshotUrl ||
        booking.bookingReceiptUrl ||
        booking.proformaInvoiceUrl ||
        booking.allotmentLetterUrl ||
        booking.deliveryConfirmationLetterUrl
    );
    return hasAnyDocument ? "Available (use View Documents)" : "N/A";
}

function viewBookingDocuments(id) {
    window.open(`/bookings/details/${id}`, "_blank");
}

function renderBookingActions(booking) {
    if (currentBookingTab === "delivered") {
        return `<button class="action-btn" style="background:#0ea5e9;color:#fff;" title="View Documents" onclick="viewBookingDocuments(${booking.id})"><i class="fas fa-file-lines"></i></button>`;
    }

    return `
        ${showPreVerifyButton(booking) ? `<button class="action-btn" style="background:#0f766e;color:#fff;" title="Pre-Verify Documents" onclick="preVerifyBooking(${booking.id})"><i class="fas fa-user-check"></i></button>` : ""}
        ${showLoanApproveButton(booking) ? `<button class="action-btn" style="background:#7c3aed;color:#fff;" title="Approve Loan" onclick="approveLoan(${booking.id})"><i class="fas fa-building-columns"></i> Approve Loan</button>` : ""}
        ${showReverificationButton(booking) ? `<button class="action-btn" style="background:#1d4ed8;color:#fff;" title="Request Re-Verification" onclick="requestReverification(${booking.id})"><i class="fas fa-rotate"></i></button>` : ""}
        ${showFullPaymentCompleteButton(booking) ? `<button class="action-btn" style="background:#2563eb;color:#fff;" title="Mark Payment Paid" onclick="completeFullPayment(${booking.id})"><i class="fas fa-wallet"></i></button>` : ""}
        ${showConfirmButton(booking) ? `<button class="action-btn" style="background:#2dd36f;color:#fff;" title="${getConfirmActionTitle(booking)}" onclick="confirmBooking(${booking.id}, '${booking.expectedDeliveryDate || ""}')"><i class="fas fa-truck"></i> ${getConfirmActionLabel(booking)}</button>` : ""}
        ${showDownPaymentVerifyButton(booking) ? `<button class="action-btn" style="background:#f59e0b;color:#fff;" title="Verify Down Payment" onclick="verifyDownPayment(${booking.id})"><i class="fas fa-money-check-dollar"></i></button>` : ""}
        <button class="action-btn" style="background:#0ea5e9;color:#fff;" title="View Documents" onclick="viewBookingDocuments(${booking.id})"><i class="fas fa-file-lines"></i></button>
        <button class="action-btn edit-btn" title="Edit" onclick="editBooking(${booking.id})">
            <i class="fas fa-edit"></i>
        </button>
        <button class="action-btn delete-btn" title="Delete" onclick="deleteBooking(${booking.id}, '${safeText(booking.customer ? booking.customer.name : "Unknown")}', '${safeText(booking.car ? booking.car.brand + " " + booking.car.model : "Unknown")}')">
            <i class="fas fa-trash"></i>
        </button>
    `;
}
function showConfirmButton(booking) {
    const s = String(booking.workflowStatus || booking.status || "").toLowerCase();
    const pre = String(booking.preVerificationStatus || "").toLowerCase();
    const paymentOption = String(booking.paymentOption || "").toLowerCase();
    if (["approved", "confirmed", "rejected", "cancelled", "delivered"].includes(s)) {
        return false;
    }
    if (paymentOption.includes("loan")) {
        if (s.includes("loan approved")) return false;
        if (s.includes("re-verification pending")) return true;
    }
    return pre === "pre-verified" || s.includes("pre-verified");
}

function showPreVerifyButton(booking) {
    const s = String(booking.workflowStatus || booking.status || "").toLowerCase();
    const pre = String(booking.preVerificationStatus || "").toLowerCase();
    if (["approved", "confirmed", "rejected", "cancelled", "delivered"].includes(s)) {
        return false;
    }
    if (pre === "pre-verified" || s.includes("pre-verified")) {
        return false;
    }
    return true;
}

function showFullPaymentCompleteButton(booking) {
    const paymentOption = String(booking.paymentOption || "").toLowerCase();
    const s = String(booking.workflowStatus || booking.status || "").toLowerCase();
    if (["approved", "confirmed", "rejected", "cancelled", "delivered"].includes(s)) {
        return false;
    }
    if (booking.downPaymentVerified !== true) return false;
    if (!paymentOption.includes("full")) {
        return false;
    }

    const deliveryDate = booking.expectedDeliveryDate ? new Date(`${booking.expectedDeliveryDate}T00:00:00`) : null;
    if (!deliveryDate) return false;
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    if (today < deliveryDate) return false;

    const remainingAmount = Number(booking.remainingAmount || 0);
    return remainingAmount > 0;
}

function preVerifyBooking(id) {
    const nameMatched = confirm("Does customer name match across Aadhaar/PAN and booking details?\n\nOK = Yes, names match\nCancel = No, mismatch found");
    const remarks = prompt("Enter pre-verification remarks (optional):", "") || "";
    const query = `?nameMatched=${nameMatched}${remarks.trim() ? `&remarks=${encodeURIComponent(remarks.trim())}` : ""}`;

    fetch(`/staff/bookings/${id}/pre-verify${query}`, { method: "POST" })
        .then((response) => response.json())
        .then((data) => {
            if (data.ok) {
                loadBookings();
                showNotification(nameMatched ? "Booking pre-verified successfully." : "Booking rejected due to name mismatch.", "success");
            } else {
                alert(data.error || "Failed to complete pre-verification");
            }
        })
        .catch(() => alert("Failed to complete pre-verification"));
}

function showLoanApproveButton(booking) {
    const paymentOption = String(booking.paymentOption || "").toLowerCase();
    if (!paymentOption.includes("loan")) return false;

    const s = String(booking.workflowStatus || booking.status || "").toLowerCase();
    const pre = String(booking.preVerificationStatus || "").toLowerCase();
    if (["rejected", "cancelled", "delivered"].includes(s)) {
        return false;
    }
    if (s.includes("re-verification pending")) return false;
    if (s.includes("loan approved")) return false;
    if (!(pre === "pre-verified" || s.includes("pre-verified"))) return false;
    if (booking.downPaymentVerified !== true) return false;
    return true;
}

function showReverificationButton(booking) {
    const paymentOption = String(booking.paymentOption || "").toLowerCase();
    const s = String(booking.workflowStatus || booking.status || "").toLowerCase();
    if (["rejected", "cancelled", "delivered"].includes(s)) return false;
    if (s.includes("re-verification pending")) return false;

    if (paymentOption.includes("loan")) {
        return s.includes("loan approved");
    }

    const remainingAmount = Number(booking.remainingAmount || 0);
    const fullyPaid = remainingAmount <= 0;
    return fullyPaid && (s.includes("approved") || s.includes("payment verified"));
}

function approveLoan(id) {
    const bankInput = prompt(
        "Enter loan bank name exactly as below:\n1) State Bank of India\n2) HDFC Bank",
        "State Bank of India"
    );
    if (!bankInput || !bankInput.trim()) return;

    const raw = bankInput.trim();
    let bankName = raw;
    if (raw === "1" || raw.toLowerCase() === "sbi") bankName = "State Bank of India";
    if (raw === "2" || raw.toLowerCase() === "hdfc") bankName = "HDFC Bank";

    const query = `?bankName=${encodeURIComponent(bankName.trim())}`;
    fetch(`/staff/bookings/${id}/loan-approve${query}`, { method: "POST" })
        .then((response) => response.json())
        .then((data) => {
            if (data.ok) {
                loadBookings();
                showNotification("Loan approved successfully.", "success");
            } else {
                alert(data.error || "Failed to approve loan");
            }
        })
        .catch(() => alert("Failed to approve loan"));
}

function requestReverification(id) {
    const remarks = prompt("Enter re-verification remarks (optional):", "") || "";
    const query = remarks.trim() ? `?remarks=${encodeURIComponent(remarks.trim())}` : "";
    fetch(`/staff/bookings/${id}/request-reverification${query}`, { method: "POST" })
        .then((response) => response.json())
        .then((data) => {
            if (data.ok) {
                loadBookings();
                showNotification("Re-verification requested.", "success");
            } else {
                alert(data.error || "Failed to request re-verification");
            }
        })
        .catch(() => alert("Failed to request re-verification"));
}

function showDownPaymentVerifyButton(booking) {
    const downPayment = Number(booking.downPaymentAmount || 0);
    const paymentOption = String(booking.paymentOption || "").toLowerCase();
    const reference = String(booking.downPaymentReference || "").trim();
    const method = String(booking.downPaymentMethod || "").trim();
    const receipt = String(booking.downPaymentReceiptUrl || "").trim();

    const hasPendingDownPaymentSignal =
        downPayment > 0 ||
        paymentOption === "down payment" ||
        reference.length > 0 ||
        method.length > 0 ||
        receipt.length > 0;

    if (!hasPendingDownPaymentSignal) return false;
    return booking.downPaymentVerified !== true;
}

function completeFullPayment(id) {
    const paymentMethod = prompt("Enter payment method (Cash / UPI / Card / Bank Transfer):", "Cash") || "Cash";
    const reference = prompt("Enter transaction/reference number (optional):", "") || "";
    const params = new URLSearchParams();
    if (paymentMethod && paymentMethod.trim()) params.set("paymentMethod", paymentMethod.trim());
    if (reference && reference.trim()) params.set("reference", reference.trim());
    const query = params.toString() ? `?${params.toString()}` : "";

    fetch(`/staff/bookings/${id}/complete-full-payment${query}`, { method: "POST" })
        .then((response) => response.json())
        .then((data) => {
            if (data.ok) {
                loadBookings();
                showNotification("Full payment marked completed at showroom.", "success");
            } else {
                alert(data.error || "Failed to complete full payment");
            }
        })
        .catch(() => alert("Failed to complete full payment"));
}

function confirmBooking(id, existingDeliveryDate) {
    const booking = window.__bookingsCache ? window.__bookingsCache.find((b) => Number(b.id) === Number(id)) : null;
    if (booking && isReverificationPending(booking)) {
        completeDelivery(id);
        return;
    }

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

function completeDelivery(id) {
    fetch(`/staff/bookings/${id}/complete-delivery`, { method: "POST" })
        .then((response) => response.json())
        .then((data) => {
            if (data.ok) {
                loadBookings();
                showNotification("Car delivered successfully.", "success");
            } else {
                alert(data.error || "Failed to deliver car");
            }
        })
        .catch(() => alert("Failed to deliver car"));
}

function verifyDownPayment(id) {
    const reference = prompt("Enter down payment transaction/reference number (optional):", "") || "";
    const query = reference ? `?reference=${encodeURIComponent(reference)}` : "";

    fetch(`/staff/bookings/${id}/verify-down-payment${query}`, { method: "POST" })
        .then((response) => response.json())
        .then((data) => {
            if (data.ok) {
                loadBookings();
                showNotification("Down payment marked completed.", "success");
            } else {
                alert(data.error || "Failed to verify down payment");
            }
        })
        .catch(() => alert("Failed to verify down payment"));
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

function switchBookingTab(tabName) {
    currentBookingTab = tabName === "delivered" ? "delivered" : "active";
    document.getElementById("activeTabBtn").classList.toggle("active", currentBookingTab === "active");
    document.getElementById("deliveredTabBtn").classList.toggle("active", currentBookingTab === "delivered");
    const addBtn = document.getElementById("addBookingBtn");
    if (addBtn) addBtn.style.display = currentBookingTab === "delivered" ? "none" : "inline-flex";
    loadBookings();
}

function isReverificationPending(booking) {
    const s = String(booking.workflowStatus || booking.status || "").toLowerCase();
    return s.includes("re-verification pending");
}

function getConfirmActionLabel(booking) {
    const s = String(booking.workflowStatus || booking.status || "").toLowerCase();
    if (s.includes("re-verification pending")) {
        return "Deliver Car";
    }
    return "Confirm Booking";
}

function getConfirmActionTitle(booking) {
    const s = String(booking.workflowStatus || booking.status || "").toLowerCase();
    if (s.includes("re-verification pending")) {
        return "Deliver Car";
    }
    return "Confirm Booking";
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
