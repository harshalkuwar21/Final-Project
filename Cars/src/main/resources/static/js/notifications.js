// Notifications page JavaScript
let notifications = [];
let filtered = [];

document.addEventListener('DOMContentLoaded', () => {
    loadNotifications();
    setupSearch();
});

function loadNotifications() {
    fetch('/api/notifications/all')
        .then(r => r.json())
        .then(data => {
            notifications = data;
            filtered = data;
            renderList(filtered);
            updateStats(data);
        })
        .catch(err => console.error('Failed to load notifications', err));
}

function renderList(list) {
    const el = document.getElementById('notificationsList');
    if (!list || list.length === 0) {
        el.innerHTML = '<li class="empty">No notifications</li>';
        return;
    }

    el.innerHTML = list.map(n => {
        const unreadClass = n.readFlag ? '' : 'unread';
        const created = new Date(n.createdAt).toLocaleString();
        return `
            <li class="${unreadClass}" data-id="${n.id}">
                <div class="not-left">
                    <div class="not-title">${escapeHtml(n.title)}</div>
                    <div class="not-msg">${escapeHtml(n.message)}</div>
                    <div class="not-meta">${n.type || ''} • ${created}</div>
                </div>
                <div class="not-actions">
                    <button class="action-btn" onclick="markRead(${n.id})">Mark Read</button>
                    <button class="action-btn" onclick="viewNotification(${n.id})">View</button>
                    <button class="action-btn delete" onclick="deleteNotification(${n.id})">Delete</button>
                </div>
            </li>
        `;
    }).join('');
}

function updateStats(list) {
    document.getElementById('totalNotifications').textContent = list.length;
    document.getElementById('unreadNotifications').textContent = list.filter(n => !n.readFlag).length;
    const last24 = list.filter(n => {
        const d = new Date(n.createdAt);
        return (Date.now() - d.getTime()) <= 24 * 60 * 60 * 1000;
    }).length;
    document.getElementById('recent24').textContent = last24;
}

function setupSearch() {
    const input = document.getElementById('searchInput');
    input.addEventListener('input', () => applyFilters());
}

function applyFilters() {
    const q = (document.getElementById('searchInput').value || '').toLowerCase();
    const type = document.getElementById('filterType').value;
    filtered = notifications.filter(n => {
        const matchesQ = !q || (n.title && n.title.toLowerCase().includes(q)) || (n.message && n.message.toLowerCase().includes(q));
        const matchesType = !type || (n.type === type);
        return matchesQ && matchesType;
    });
    renderList(filtered);
}

function clearFilters() {
    document.getElementById('searchInput').value = '';
    document.getElementById('filterType').value = '';
    filtered = notifications;
    renderList(filtered);
}

function markRead(id) {
    fetch(`/api/notifications/mark-read/${id}`, { method: 'POST' })
        .then(r => r.json())
        .then(() => loadNotifications())
        .catch(err => console.error(err));
}

function markAllRead() {
    fetch('/api/notifications/mark-all-read', { method: 'POST' })
        .then(r => r.json())
        .then(() => loadNotifications())
        .catch(err => console.error(err));
}

function deleteNotification(id) {
    if (!confirm('Delete this notification?')) return;
    fetch(`/api/notifications/${id}`, { method: 'DELETE' })
        .then(r => r.json())
        .then(() => loadNotifications())
        .catch(err => console.error(err));
}

function viewNotification(id) {
    const n = notifications.find(x => x.id === id);
    if (!n) return;
    alert(`${n.title}\n\n${n.message}`);
    if (!n.readFlag) markRead(id);
}

function openCreateModal() { document.getElementById('createModal').style.display = 'block'; }
function closeCreateModal() { document.getElementById('createModal').style.display = 'none'; document.getElementById('nTitle').value=''; document.getElementById('nMessage').value=''; }

function createNotification() {
    const title = document.getElementById('nTitle').value.trim();
    const message = document.getElementById('nMessage').value.trim();
    const type = document.getElementById('nType').value;
    if (!title || !message) { alert('Provide title and message'); return; }

    const payload = { title, message, type };

    fetch('/api/notifications', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
        .then(r => r.json())
        .then(() => { closeCreateModal(); loadNotifications(); })
        .catch(err => console.error(err));
}

// utility
function escapeHtml(unsafe) {
    if (!unsafe) return '';
    return unsafe.replace(/[&<>"]+/g, function(m) { return ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[m]); });
}

window.applyFilters = applyFilters; window.clearFilters = clearFilters; window.markAllRead = markAllRead; window.openCreateModal = openCreateModal; window.closeCreateModal = closeCreateModal;


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
