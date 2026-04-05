// ============================================
// DOCUMENTS PAGE JAVASCRIPT
// ============================================

let currentDocumentId = null;
let currentGroup = null;
let allDocuments = [];

document.addEventListener('DOMContentLoaded', function() {
    loadDocuments();
    loadCarsAndCustomers();
    setupFormSubmit();
    setupSearch();
    setupDocumentModal();
});

// ============================================
// LOAD DOCUMENTS
// ============================================

function loadDocuments() {
    fetch('/api/documents/all')
        .then(response => response.json())
        .then(data => {
            allDocuments = Array.isArray(data) ? data : [];
            refreshTypeFilter(allDocuments);
            displayDocuments(allDocuments);
            updateStatistics(allDocuments);
        })
        .catch(error => {
            console.error('Error loading documents:', error);
            showToast('Error loading documents', 'error');
        });
}

function displayDocuments(documents) {
    const tbody = document.getElementById('documentsTable');
    const groups = groupDocuments(documents);

    if (groups.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8">
                    <div class="empty-state">
                        <div class="empty-state-icon"><i class="fas fa-file"></i></div>
                        <h3>No Documents Found</h3>
                        <p>Start by adding your first document</p>
                    </div>
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = groups.map((group, index) => `
        <tr class="document-row">
            <td>${index + 1}</td>
            <td>${escapeHtml(group.customerLabel)}</td>
            <td>${escapeHtml(group.carLabel)}</td>
            <td>
                <div style="font-size:12px;color:#64748b;">${escapeHtml(group.sourceLabel)}</div>
                ${group.bookingId ? `<div style="font-size:12px;color:#64748b;">Booking #${group.bookingId}</div>` : ''}
            </td>
            <td>${renderDocumentTags(group.documents)}</td>
            <td>${formatDate(group.latestUploadDate)}</td>
            <td>${renderGroupStatus(group.documents)}</td>
            <td>
                <div class="doc-actions">
                    <button class="action-icon" title="View All" onclick="viewGroupDocuments('${escapeJsValue(group.groupId)}')">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="action-icon download" title="Download All" onclick="downloadGroupDocuments('${escapeJsValue(group.groupId)}')">
                        <i class="fas fa-download"></i>
                    </button>
                    <button class="action-icon" title="Delete Row" onclick="deleteGroupDocuments('${escapeJsValue(group.groupId)}')">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

function groupDocuments(documents) {
    const groupedMap = new Map();

    documents.forEach(doc => {
        const groupId = getGroupId(doc);
        if (!groupedMap.has(groupId)) {
            groupedMap.set(groupId, {
                groupId,
                bookingId: doc.bookingId || null,
                customerLabel: getCustomerLabel(doc.customer) || 'Unknown Customer',
                carLabel: getCarLabel(doc.car) || '-',
                sourceLabel: doc.source === 'BOOKING' ? 'Booking Documents' : 'Manual Upload',
                latestUploadDate: doc.uploadDate || null,
                documents: []
            });
        }

        const group = groupedMap.get(groupId);
        group.documents.push(doc);

        if (doc.uploadDate && (!group.latestUploadDate || String(doc.uploadDate) > String(group.latestUploadDate))) {
            group.latestUploadDate = doc.uploadDate;
        }

        if (doc.source === 'BOOKING') {
            group.sourceLabel = 'Booking Documents';
        }

        if (!group.carLabel || group.carLabel === '-') {
            group.carLabel = getCarLabel(doc.car) || '-';
        }
    });

    return [...groupedMap.values()]
        .map(group => {
            group.documents.sort(compareDocuments);
            return group;
        })
        .sort((a, b) => {
            const aBooking = a.sourceLabel === 'Booking Documents';
            const bBooking = b.sourceLabel === 'Booking Documents';
            if (aBooking !== bBooking) return aBooking ? -1 : 1;
            return String(b.latestUploadDate || '').localeCompare(String(a.latestUploadDate || ''));
        });
}

function getGroupId(doc) {
    if (doc.bookingId) {
        return `booking-${doc.bookingId}`;
    }
    if (doc.customer && doc.customer.id) {
        return `customer-${doc.customer.id}`;
    }
    if (doc.car && doc.car.id) {
        return `car-${doc.car.id}`;
    }
    return `single-${doc.id}`;
}

function compareDocuments(left, right) {
    const leftBooking = left.source === 'BOOKING';
    const rightBooking = right.source === 'BOOKING';
    if (leftBooking !== rightBooking) return leftBooking ? -1 : 1;
    return String(right.uploadDate || '').localeCompare(String(left.uploadDate || ''));
}

function renderDocumentTags(documents) {
    return documents.map(doc => `
        <button type="button"
                class="badge"
                style="margin:4px 6px 4px 0;border:none;cursor:pointer;"
                onclick="viewDocument('${escapeJsValue(doc.id)}')">
            ${escapeHtml(doc.documentType)}
        </button>
    `).join('');
}

function renderGroupStatus(documents) {
    if (documents.some(doc => getDocumentStatus(doc) === 'expired')) {
        return '<span class="badge badge-expired">Has Expired</span>';
    }
    if (documents.some(doc => getDocumentStatus(doc) === 'expiring')) {
        return '<span class="badge badge-expiring">Expiring Soon</span>';
    }
    return '<span class="badge badge-active">Complete</span>';
}

// ============================================
// STATISTICS
// ============================================

function updateStatistics(documents) {
    const now = new Date();
    const futureDate = new Date();
    futureDate.setDate(futureDate.getDate() + 30);

    let totalDocs = documents.length;
    let activeDocs = 0;
    let expiringDocs = 0;
    let expiredDocs = 0;

    documents.forEach(doc => {
        if (!doc.expiryDate) {
            activeDocs++;
        } else {
            const expiryDateObj = new Date(doc.expiryDate);
            if (expiryDateObj < now) {
                expiredDocs++;
            } else if (expiryDateObj <= futureDate) {
                expiringDocs++;
            } else {
                activeDocs++;
            }
        }
    });

    document.getElementById('totalDocs').textContent = totalDocs;
    document.getElementById('activeDocs').textContent = activeDocs;
    document.getElementById('expiringDocs').textContent = expiringDocs;
    document.getElementById('expiredDocs').textContent = expiredDocs;
}

// ============================================
// FILTER & SEARCH
// ============================================

function setupSearch() {
    document.getElementById('searchInput').addEventListener('input', function() {
        applyFilters();
    });
}

function applyFilters() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    const typeFilter = document.getElementById('filterType').value;
    const statusFilter = document.getElementById('filterStatus').value;

    const filtered = allDocuments.filter(doc => {
        const matchesSearch = !searchTerm ||
            String(doc.fileName || '').toLowerCase().includes(searchTerm) ||
            String(doc.documentType || '').toLowerCase().includes(searchTerm) ||
            getCustomerLabel(doc.customer).toLowerCase().includes(searchTerm) ||
            getCarLabel(doc.car).toLowerCase().includes(searchTerm);

        const matchesType = !typeFilter || (doc.documentCategory || doc.documentType) === typeFilter;
        const matchesStatus = !statusFilter || getDocumentStatus(doc) === statusFilter;

        return matchesSearch && matchesType && matchesStatus;
    });

    displayDocuments(filtered);
}

function clearFilters() {
    document.getElementById('searchInput').value = '';
    document.getElementById('filterType').value = '';
    document.getElementById('filterStatus').value = '';
    displayDocuments(allDocuments);
}

function refreshTypeFilter(documents) {
    const filterType = document.getElementById('filterType');
    const selectedValue = filterType.value;
    const uniqueTypes = [...new Set(documents.map(doc => doc.documentCategory || doc.documentType).filter(Boolean))]
        .sort((a, b) => a.localeCompare(b));

    filterType.innerHTML = '<option value="">All Types</option>' +
        uniqueTypes.map(type => `<option value="${escapeHtml(type)}">${escapeHtml(formatTypeLabel(type))}</option>`).join('');

    filterType.value = selectedValue;
}

// ============================================
// MODAL FUNCTIONS
// ============================================

function openAddDocumentModal() {
    document.getElementById('addDocumentModal').style.display = 'block';
    document.body.style.overflow = 'hidden';
}

function closeAddDocumentModal() {
    document.getElementById('addDocumentModal').style.display = 'none';
    document.getElementById('addDocumentForm').reset();
    document.body.style.overflow = '';
}

function closeViewDocumentModal() {
    currentDocumentId = null;
    currentGroup = null;
    document.getElementById('viewDocumentModal').style.display = 'none';
    document.getElementById('viewDocumentContent').innerHTML = '';
    document.body.style.overflow = '';
}

function setupDocumentModal() {
    document.addEventListener('click', function(event) {
        if (event.target.id === 'addDocumentModal') {
            closeAddDocumentModal();
        }
        if (event.target.id === 'viewDocumentModal') {
            closeViewDocumentModal();
        }
    });

    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            closeAddDocumentModal();
            closeViewDocumentModal();
        }
    });
}

// ============================================
// FORM SUBMISSION
// ============================================

function setupFormSubmit() {
    const form = document.getElementById('addDocumentForm');
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        uploadDocument();
    });
}

function uploadDocument() {
    const formData = new FormData();
    const fileInput = document.getElementById('docFile');

    if (!fileInput.files[0]) {
        showToast('Please select a file', 'error');
        return;
    }

    formData.append('file', fileInput.files[0]);
    formData.append('documentType', document.getElementById('docType').value);
    formData.append('carId', document.getElementById('docCar').value || null);
    formData.append('customerId', document.getElementById('docCustomer').value || null);
    formData.append('expiryDate', document.getElementById('docExpiry').value || null);

    fetch('/api/documents/upload', {
        method: 'POST',
        body: formData
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showToast('Document uploaded successfully', 'success');
                closeAddDocumentModal();
                loadDocuments();
            } else {
                showToast(data.message || 'Failed to upload document', 'error');
            }
        })
        .catch(error => {
            console.error('Error uploading document:', error);
            showToast('Error uploading document', 'error');
        });
}

// ============================================
// DOCUMENT ACTIONS
// ============================================

function viewGroupDocuments(groupId) {
    const group = groupDocuments(allDocuments).find(item => item.groupId === groupId);
    if (!group) return;

    currentGroup = group;
    currentDocumentId = group.documents[0] ? group.documents[0].id : null;

    const content = document.getElementById('viewDocumentContent');
    content.innerHTML = `
        <div class="file-info" style="margin-bottom:20px;">
            <p><span class="file-info-label">Customer:</span> ${escapeHtml(group.customerLabel)}</p>
            <p><span class="file-info-label">Car:</span> ${escapeHtml(group.carLabel)}</p>
            ${group.bookingId ? `<p><span class="file-info-label">Booking ID:</span> ${group.bookingId}</p>` : ''}
            <p><span class="file-info-label">Documents:</span> ${group.documents.length}</p>
        </div>
        ${group.documents.map(doc => renderDocumentPreviewCard(doc)).join('')}
    `;

    document.getElementById('downloadBtn').onclick = function() {
        downloadGroupDocuments(groupId);
    };
    document.getElementById('deleteDocBtn').style.display = 'none';
    document.getElementById('viewDocumentModal').style.display = 'block';
    document.body.style.overflow = 'hidden';
}

function viewDocument(id) {
    const doc = allDocuments.find(item => String(item.id) === String(id));
    if (!doc) return;

    currentDocumentId = id;
    currentGroup = null;
    const content = document.getElementById('viewDocumentContent');

    content.innerHTML = renderDocumentPreviewCard(doc, true);
    document.getElementById('downloadBtn').onclick = function() {
        downloadDocument(id);
    };
    document.getElementById('deleteDocBtn').style.display = canDeleteDocument(doc) ? 'inline-flex' : 'none';
    document.getElementById('viewDocumentModal').style.display = 'block';
    document.body.style.overflow = 'hidden';
}

function renderDocumentPreviewCard(doc, singleView = false) {
    return `
        <div style="border:1px solid #e5e7eb;border-radius:14px;padding:16px;margin-bottom:16px;background:#fff;">
            <div class="file-info" style="margin-bottom:14px;">
                <p><span class="file-info-label">Type:</span> ${escapeHtml(doc.documentType || '-')}</p>
                <p><span class="file-info-label">File:</span> ${escapeHtml(doc.fileName || '-')}</p>
                ${doc.sourceLabel ? `<p><span class="file-info-label">Source:</span> ${escapeHtml(doc.sourceLabel)}</p>` : ''}
                ${doc.bookingId ? `<p><span class="file-info-label">Booking ID:</span> ${doc.bookingId}</p>` : ''}
                <p><span class="file-info-label">Uploaded:</span> ${formatDate(doc.uploadDate)}</p>
                ${doc.customer ? `<p><span class="file-info-label">Customer:</span> ${escapeHtml(getCustomerLabel(doc.customer))}</p>` : ''}
                ${doc.car ? `<p><span class="file-info-label">Car:</span> ${escapeHtml(getCarLabel(doc.car))}</p>` : ''}
                <p><span class="file-info-label">Status:</span> ${getStatusBadge(doc)}</p>
            </div>
            <div style="display:flex;gap:10px;flex-wrap:wrap;margin-bottom:14px;">
                <button type="button" class="btn btn-secondary" onclick="downloadDocument('${escapeJsValue(doc.id)}')">
                    <i class="fas fa-download"></i> Download
                </button>
                ${canDeleteDocument(doc) && !singleView ? `
                    <button type="button" class="btn btn-secondary" onclick="deleteDocumentBySource('${escapeJsValue(doc.id)}')">
                        <i class="fas fa-trash"></i> Delete
                    </button>
                ` : ''}
                <button type="button" class="btn btn-secondary" onclick="openDocumentInNewTab('${escapeJsValue(doc.id)}')">
                    <i class="fas fa-up-right-from-square"></i> Open
                </button>
            </div>
            ${renderInlinePreview(doc)}
        </div>
    `;
}

function renderInlinePreview(doc) {
    const fileUrl = doc.fileUrl || '';
    const extension = getFileExtension(doc.fileName || fileUrl);

    if (['png', 'jpg', 'jpeg', 'gif', 'webp', 'bmp'].includes(extension)) {
        return `
            <img src="${fileUrl}"
                 alt="${escapeHtml(doc.fileName || 'Document preview')}"
                 style="width:100%;max-height:520px;object-fit:contain;border-radius:12px;border:1px solid #ddd;background:#f8fafc;" />
        `;
    }

    if (extension === 'pdf') {
        return `
            <iframe src="${fileUrl}"
                    style="width:100%;height:420px;border-radius:12px;border:1px solid #ddd;"
                    title="Document Preview"></iframe>
        `;
    }

    return `
        <div style="padding:28px;border:1px dashed #cbd5e1;border-radius:12px;background:#f8fafc;text-align:center;color:#475569;">
            <div style="font-size:16px;font-weight:600;margin-bottom:8px;">Inline preview not available for this file type</div>
            <div style="font-size:13px;margin-bottom:14px;">Open the file in a new tab or download it.</div>
            <div style="display:flex;gap:10px;justify-content:center;flex-wrap:wrap;">
                <button type="button" class="btn btn-secondary" onclick="openDocumentInNewTab('${escapeJsValue(doc.id)}')">
                    <i class="fas fa-up-right-from-square"></i> Open in New Tab
                </button>
                <button type="button" class="btn btn-secondary" onclick="downloadDocument('${escapeJsValue(doc.id)}')">
                    <i class="fas fa-download"></i> Download
                </button>
            </div>
        </div>
    `;
}

function downloadDocument(id) {
    const doc = allDocuments.find(item => String(item.id) === String(id));
    if (!doc || !doc.fileUrl) return;

    const link = document.createElement('a');
    link.href = doc.fileUrl;
    link.download = doc.fileName || 'document';
    link.click();
}

function openDocumentInNewTab(id) {
    const doc = allDocuments.find(item => String(item.id) === String(id));
    if (!doc || !doc.fileUrl) return;
    window.open(doc.fileUrl, '_blank', 'noopener,noreferrer');
}

function downloadGroupDocuments(groupId) {
    const group = groupDocuments(allDocuments).find(item => item.groupId === groupId);
    if (!group) return;
    group.documents.forEach(doc => downloadDocument(doc.id));
}

function deleteDocumentRequest(id) {
    fetch(`/api/documents/${id}`, {
        method: 'DELETE'
    })
        .then(response => response.json());
}

function deleteDocument(id) {
    if (!confirm('Are you sure you want to delete this document?')) {
        return;
    }

    deleteDocumentRequest(id)
        .then(data => {
            if (data.success) {
                showToast('Document deleted successfully', 'success');
                closeViewDocumentModal();
                loadDocuments();
            } else {
                showToast('Failed to delete document', 'error');
            }
        })
        .catch(error => {
            console.error('Error deleting document:', error);
            showToast('Error deleting document', 'error');
        });
}

function deleteBookingDocumentRequest(bookingId, documentCategory) {
    fetch(`/api/documents/booking/${bookingId}/${encodeURIComponent(documentCategory)}`, {
        method: 'DELETE'
    })
        .then(response => response.json());
}

function deleteBookingDocument(bookingId, documentCategory) {
    if (!confirm('Are you sure you want to delete this booking document?')) {
        return;
    }

    deleteBookingDocumentRequest(bookingId, documentCategory)
        .then(data => {
            if (data.success) {
                showToast('Document deleted successfully', 'success');
                closeViewDocumentModal();
                loadDocuments();
            } else {
                showToast(data.message || 'Failed to delete document', 'error');
            }
        })
        .catch(error => {
            console.error('Error deleting booking document:', error);
            showToast('Error deleting document', 'error');
        });
}

function canDeleteDocument(doc) {
    return !!doc && (doc.source === 'DOCUMENT' || doc.source === 'BOOKING');
}

function deleteDocumentBySource(id) {
    const doc = allDocuments.find(item => String(item.id) === String(id));
    if (!doc) return;

    if (doc.source === 'BOOKING') {
        deleteBookingDocument(doc.bookingId, doc.documentCategory);
        return;
    }

    if (doc.documentId) {
        deleteDocument(doc.documentId);
    }
}

function deleteCurrentDocument() {
    const doc = allDocuments.find(item => String(item.id) === String(currentDocumentId));
    if (!doc) return;
    deleteDocumentBySource(doc.id);
}

function deleteDocumentByRecord(doc) {
    if (!doc) return Promise.resolve({ success: false, message: 'Document not found' });
    if (doc.source === 'BOOKING') {
        return deleteBookingDocumentRequest(doc.bookingId, doc.documentCategory);
    }
    if (doc.documentId) {
        return deleteDocumentRequest(doc.documentId);
    }
    return Promise.resolve({ success: false, message: 'Document not deletable' });
}

async function deleteGroupDocuments(groupId) {
    const group = groupDocuments(allDocuments).find(item => item.groupId === groupId);
    if (!group || !Array.isArray(group.documents) || !group.documents.length) return;

    const confirmed = confirm(`Delete this row and all ${group.documents.length} documents for ${group.customerLabel}?`);
    if (!confirmed) return;

    let deletedCount = 0;
    let failedCount = 0;

    for (const doc of group.documents) {
        try {
            const result = await deleteDocumentByRecord(doc);
            if (result && result.success) {
                deletedCount++;
            } else {
                failedCount++;
            }
        } catch (_) {
            failedCount++;
        }
    }

    closeViewDocumentModal();
    loadDocuments();

    if (deletedCount && !failedCount) {
        showToast('Row deleted successfully', 'success');
        return;
    }
    if (deletedCount && failedCount) {
        showToast(`Deleted ${deletedCount} documents. ${failedCount} failed.`, 'info');
        return;
    }
    showToast('Failed to delete row documents', 'error');
}

// ============================================
// LOAD CARS & CUSTOMERS
// ============================================

function loadCarsAndCustomers() {
    Promise.all([
        fetch('/api/documents/cars').then(r => r.json()),
        fetch('/api/documents/customers').then(r => r.json())
    ])
        .then(([cars, customers]) => {
            const carSelect = document.getElementById('docCar');
            const customerSelect = document.getElementById('docCustomer');

            cars.forEach(car => {
                const option = document.createElement('option');
                option.value = car.id;
                option.textContent = `${car.brand} ${car.model} (${car.vin})`;
                carSelect.appendChild(option);
            });

            customers.forEach(customer => {
                const option = document.createElement('option');
                option.value = customer.id;
                option.textContent = `${getCustomerLabel(customer)} (${customer.email})`;
                customerSelect.appendChild(option);
            });
        })
        .catch(error => console.error('Error loading cars/customers:', error));
}

// ============================================
// HELPER FUNCTIONS
// ============================================

function getStatusBadge(doc) {
    const status = getDocumentStatus(doc);
    if (status === 'expired') {
        return '<span class="badge badge-expired">Expired</span>';
    }
    if (status === 'expiring') {
        return '<span class="badge badge-expiring">Expiring Soon</span>';
    }
    return '<span class="badge badge-active">Active</span>';
}

function getDocumentStatus(doc) {
    if (!doc.expiryDate) return 'active';

    const now = new Date();
    const futureDate = new Date();
    futureDate.setDate(futureDate.getDate() + 30);
    const expiryDateObj = new Date(doc.expiryDate);

    if (expiryDateObj < now) return 'expired';
    if (expiryDateObj <= futureDate) return 'expiring';
    return 'active';
}

function formatTypeLabel(type) {
    const labelMap = {
        AadhaarPhoto: 'Aadhaar Card',
        PanPhoto: 'PAN Card',
        SignaturePhoto: 'Signature',
        PassportPhoto: 'Passport Photo',
        PaymentScreenshot: 'Payment Screenshot',
        DownPaymentReceipt: 'Down Payment Receipt',
        BookingReceipt: 'Booking Receipt',
        ProformaInvoice: 'Proforma Invoice',
        AllotmentLetter: 'Allotment Letter',
        DeliveryConfirmation: 'Delivery Confirmation',
        InsurancePolicy: 'Insurance Policy',
        TemporaryRegistration: 'Temporary Registration',
        FinalInvoice: 'Final Invoice / Sale Bill',
        RegistrationCertificate: 'Registration Certificate',
        PucCertificate: 'PUC Certificate',
        WarrantyDocument: 'Warranty Document',
        RoadTaxReceipt: 'Road Tax Receipt',
        FinanceSanctionLetter: 'Finance Sanction Letter',
        LoanDocument: 'Loan Document'
    };
    return labelMap[type] || type;
}

function formatDate(dateString) {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}

function getCarLabel(car) {
    if (!car) return '';
    return `${car.brand || ''} ${car.model || ''}`.trim();
}

function getCustomerLabel(customer) {
    if (!customer) return '';
    if (customer.name) return customer.name;
    return `${customer.firstName || ''} ${customer.lastName || ''}`.trim();
}

function getFileExtension(fileName) {
    const safeName = String(fileName || '');
    const dotIndex = safeName.lastIndexOf('.');
    return dotIndex >= 0 ? safeName.substring(dotIndex + 1).toLowerCase() : '';
}

function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function escapeJsValue(value) {
    return String(value ?? '').replace(/\\/g, '\\\\').replace(/'/g, "\\'");
}

function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast-message ${type}`;
    toast.innerHTML = `<i class="fas fa-${type === 'success' ? 'check' : type === 'error' ? 'exclamation' : 'info'}-circle"></i> ${escapeHtml(message)}`;
    document.body.appendChild(toast);

    setTimeout(() => {
        toast.style.animation = 'slideUp 0.3s ease reverse';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// ============================================
// EXPORT FUNCTIONS
// ============================================

window.loadDocuments = loadDocuments;
window.displayDocuments = displayDocuments;
window.updateStatistics = updateStatistics;
window.openAddDocumentModal = openAddDocumentModal;
window.closeAddDocumentModal = closeAddDocumentModal;
window.closeViewDocumentModal = closeViewDocumentModal;
window.viewDocument = viewDocument;
window.viewGroupDocuments = viewGroupDocuments;
window.downloadDocument = downloadDocument;
window.downloadGroupDocuments = downloadGroupDocuments;
window.openDocumentInNewTab = openDocumentInNewTab;
window.deleteDocument = deleteDocument;
window.deleteCurrentDocument = deleteCurrentDocument;
window.applyFilters = applyFilters;
window.clearFilters = clearFilters;
window.showToast = showToast;


    const logoutBtn = document.getElementById("logoutBtn");
    const redirectAfterLogout = (message) => {
        try {
            sessionStorage.setItem("logoutMessage", message || "Logged out successfully.");
        } catch (_) {
            // ignore storage errors
        }
        window.location.href = "/login?logout=1";
    };
    if (logoutBtn) {
        logoutBtn.addEventListener("click", async () => {
            if (!confirm("Are you sure you want to logout?")) return;
            try {
                await fetch("/user/logout", { method: "POST" });
            } catch (_) {
                // ignore network errors and proceed to login
            }
            redirectAfterLogout("Logged out successfully.");
        });
    }
