// ============================================
// DOCUMENTS PAGE JAVASCRIPT
// ============================================

let currentDocumentId = null;
let allDocuments = [];

// Initialize page on load
document.addEventListener('DOMContentLoaded', function() {
    loadDocuments();
    loadCarsAndCustomers();
    setupFormSubmit();
    setupSearch();
});

// ============================================
// LOAD DOCUMENTS
// ============================================

function loadDocuments() {
    fetch('/api/documents/all')
        .then(response => response.json())
        .then(data => {
            allDocuments = data;
            displayDocuments(data);
            updateStatistics(data);
        })
        .catch(error => {
            console.error('Error loading documents:', error);
            showToast('Error loading documents', 'error');
        });
}

function displayDocuments(documents) {
    const tbody = document.getElementById('documentsTable');
    
    if (documents.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7">
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

    tbody.innerHTML = documents.map(doc => `
        <tr class="document-row">
            <td>
                <span class="doc-type-badge ${getDocTypeClass(doc.documentType)}">
                    ${doc.documentType}
                </span>
            </td>
            <td>${doc.fileName}</td>
            <td>
                ${doc.car ? `<strong>Car:</strong> ${doc.car.brand} ${doc.car.model}` : 
                  (doc.customer ? `<strong>Customer:</strong> ${doc.customer.firstName} ${doc.customer.lastName}` : 
                   '—')}
            </td>
            <td>${formatDate(doc.uploadDate)}</td>
            <td>${doc.expiryDate ? formatDate(doc.expiryDate) : '—'}</td>
            <td>${getStatusBadge(doc)}</td>
            <td>
                <div class="doc-actions">
                    <button class="action-icon download" title="Download" onclick="downloadDocument(${doc.id})">
                        <i class="fas fa-download"></i>
                    </button>
                    <button class="action-icon" title="View" onclick="viewDocument(${doc.id})">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="action-icon delete" title="Delete" onclick="deleteDocument(${doc.id})">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

// ============================================
// STATISTICS
// ============================================

function updateStatistics(documents) {
    const now = new Date();
    const futureDate = new Date();
    futureDate.setDate(futureDate.getDate() + 30); // 30 days from now

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
    const searchInput = document.getElementById('searchInput');
    searchInput.addEventListener('input', function() {
        applyFilters();
    });
}

function applyFilters() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    const typeFilter = document.getElementById('filterType').value;
    const statusFilter = document.getElementById('filterStatus').value;

    const filtered = allDocuments.filter(doc => {
        // Search filter
        const matchesSearch = !searchTerm || 
            doc.fileName.toLowerCase().includes(searchTerm) ||
            doc.documentType.toLowerCase().includes(searchTerm);

        // Type filter
        const matchesType = !typeFilter || doc.documentType === typeFilter;

        // Status filter
        let matchesStatus = true;
        if (statusFilter) {
            const status = getDocumentStatus(doc);
            matchesStatus = status === statusFilter;
        }

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

// ============================================
// MODAL FUNCTIONS
// ============================================

function openAddDocumentModal() {
    document.getElementById('addDocumentModal').style.display = 'block';
}

function closeAddDocumentModal() {
    document.getElementById('addDocumentModal').style.display = 'none';
    document.getElementById('addDocumentForm').reset();
}

function closeViewDocumentModal() {
    document.getElementById('viewDocumentModal').style.display = 'none';
}

// Close modal when clicking outside
window.onclick = function(event) {
    const modal = event.target;
    if (modal.classList.contains('modal')) {
        modal.style.display = 'none';
    }
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

function viewDocument(id) {
    const doc = allDocuments.find(d => d.id === id);
    if (!doc) return;

    currentDocumentId = id;
    const content = document.getElementById('viewDocumentContent');
    
    const documentInfo = `
        <div class="file-info">
            <p><span class="file-info-label">Type:</span> ${doc.documentType}</p>
            <p><span class="file-info-label">File:</span> ${doc.fileName}</p>
            <p><span class="file-info-label">Uploaded:</span> ${formatDate(doc.uploadDate)}</p>
            ${doc.expiryDate ? `<p><span class="file-info-label">Expiry:</span> ${formatDate(doc.expiryDate)}</p>` : ''}
            ${doc.car ? `<p><span class="file-info-label">Car:</span> ${doc.car.brand} ${doc.car.model}</p>` : ''}
            ${doc.customer ? `<p><span class="file-info-label">Customer:</span> ${doc.customer.firstName} ${doc.customer.lastName}</p>` : ''}
            <p><span class="file-info-label">Status:</span> ${getStatusBadge(doc)}</p>
        </div>
        <iframe src="${doc.fileUrl}" style="width: 100%; height: 500px; border-radius: 12px; border: 1px solid #ddd;" title="Document Preview"></iframe>
    `;
    
    content.innerHTML = documentInfo;
    document.getElementById('downloadBtn').onclick = function() {
        downloadDocument(id);
    };
    document.getElementById('viewDocumentModal').style.display = 'block';
}

function downloadDocument(id) {
    const doc = allDocuments.find(d => d.id === id);
    if (doc && doc.fileUrl) {
        const link = document.createElement('a');
        link.href = doc.fileUrl;
        link.download = doc.fileName;
        link.click();
    }
}

function deleteDocument(id) {
    if (confirm('Are you sure you want to delete this document?')) {
        fetch(`/api/documents/${id}`, {
            method: 'DELETE'
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showToast('Document deleted successfully', 'success');
                loadDocuments();
                closeViewDocumentModal();
            } else {
                showToast('Failed to delete document', 'error');
            }
        })
        .catch(error => {
            console.error('Error deleting document:', error);
            showToast('Error deleting document', 'error');
        });
    }
}

function deleteCurrentDocument() {
    if (currentDocumentId) {
        deleteDocument(currentDocumentId);
    }
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
            option.textContent = `${customer.firstName} ${customer.lastName} (${customer.email})`;
            customerSelect.appendChild(option);
        });
    })
    .catch(error => console.error('Error loading cars/customers:', error));
}

// ============================================
// HELPER FUNCTIONS
// ============================================

function getStatusBadge(doc) {
    const now = new Date();
    const futureDate = new Date();
    futureDate.setDate(futureDate.getDate() + 30);

    if (!doc.expiryDate) {
        return '<span class="badge badge-active">Active</span>';
    }

    const expiryDateObj = new Date(doc.expiryDate);
    
    if (expiryDateObj < now) {
        return '<span class="badge badge-expired">Expired</span>';
    } else if (expiryDateObj <= futureDate) {
        return '<span class="badge badge-expiring">Expiring Soon</span>';
    } else {
        return '<span class="badge badge-active">Active</span>';
    }
}

function getDocumentStatus(doc) {
    const now = new Date();
    const futureDate = new Date();
    futureDate.setDate(futureDate.getDate() + 30);

    if (!doc.expiryDate) {
        return 'active';
    }

    const expiryDateObj = new Date(doc.expiryDate);
    
    if (expiryDateObj < now) {
        return 'expired';
    } else if (expiryDateObj <= futureDate) {
        return 'expiring';
    } else {
        return 'active';
    }
}

function getDocTypeClass(type) {
    const typeMap = {
        'RC': 'rc',
        'Insurance': 'insurance',
        'Invoice': 'invoice',
        'Form20': 'form',
        'Form21': 'form',
        'CustomerID': 'id'
    };
    return typeMap[type] || 'default';
}

function formatDate(dateString) {
    if (!dateString) return '—';
    const options = { year: 'numeric', month: 'short', day: 'numeric' };
    return new Date(dateString).toLocaleDateString('en-US', options);
}

function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast-message ${type}`;
    toast.innerHTML = `<i class="fas fa-${type === 'success' ? 'check' : type === 'error' ? 'exclamation' : 'info'}-circle"></i> ${message}`;
    document.body.appendChild(toast);

    setTimeout(() => {
        toast.style.animation = 'slideUp 0.3s ease reverse';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// ============================================
// EXPORT FUNCTIONS (for other scripts if needed)
// ============================================

window.loadDocuments = loadDocuments;
window.displayDocuments = displayDocuments;
window.updateStatistics = updateStatistics;
window.openAddDocumentModal = openAddDocumentModal;
window.closeAddDocumentModal = closeAddDocumentModal;
window.closeViewDocumentModal = closeViewDocumentModal;
window.viewDocument = viewDocument;
window.downloadDocument = downloadDocument;
window.deleteDocument = deleteDocument;
window.deleteCurrentDocument = deleteCurrentDocument;
window.applyFilters = applyFilters;
window.clearFilters = clearFilters;
window.showToast = showToast;
