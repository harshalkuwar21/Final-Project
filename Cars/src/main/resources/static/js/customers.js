document.addEventListener('DOMContentLoaded', () => {
    loadCustomers();
    document.getElementById('eSaveBtn').addEventListener('click', saveEdit);
});

let editingId = null;

function loadCustomers(){
    fetch('/api/customers')
        .then(r => r.json())
        .then(list => renderCustomers(list))
        .catch(err => console.error('Failed to load customers', err));
}

function renderCustomers(list){
    const body = document.getElementById('customersBody');
    body.innerHTML = '';
    list.forEach(c => {
        const tr = document.createElement('tr');
        const fullName = `${c.first || ''} ${c.last || ''}`.trim();
        const enabled = !!c.enabled;
        const active = c.active !== false;
        const accountBadge = enabled
            ? '<span class="status-chip status-ok">Enabled</span>'
            : '<span class="status-chip status-bad">Blocked</span>';
        const activeBadge = active
            ? '<span class="status-chip status-ok">Active</span>'
            : '<span class="status-chip status-bad">Inactive</span>';
        const fraudAction = enabled || active
            ? `<button class="btn btn-danger" onclick="markFraud(${c.userid})"><i class="fas fa-user-slash"></i> Fraud Block</button>`
            : `<button class="btn btn-secondary" onclick="activateCustomer(${c.userid})"><i class="fas fa-user-check"></i> Activate</button>`;

        tr.innerHTML = `<td>${escapeHtml(fullName)}</td><td>${escapeHtml(c.contact||'')}</td><td>${escapeHtml(c.email||'')}</td><td>${escapeHtml(c.role||'')}</td><td>${accountBadge} ${activeBadge}</td><td><button class="btn btn-edit" onclick="showEdit(${c.userid})"><i class="fas fa-edit"></i> Edit</button> ${fraudAction} <button class="btn btn-danger" onclick="deleteCustomer(${c.userid})"><i class="fas fa-trash"></i> Delete</button></td>`;
        body.appendChild(tr);
    });
}

function addCustomer(){
    const c = {
        first: document.getElementById('first').value,
        last: document.getElementById('last').value,
        contact: document.getElementById('contact').value,
        email: document.getElementById('email').value
    };
    fetch('/api/customers', { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify(c) })
        .then(async r => {
            const data = await r.json().catch(() => ({}));
            if (!r.ok || data.ok === false) {
                document.getElementById('msg').textContent = data.message || 'Error';
                return;
            }
            document.getElementById('msg').textContent = 'Added';
            clearForm();
            loadCustomers();
            setTimeout(()=>{document.getElementById('msg').textContent='';},1500);
        })
        .catch(err => { console.error(err); document.getElementById('msg').textContent = 'Error'; });
}

function deleteCustomer(id){
    if(!confirm('Delete customer?')) return;
    fetch('/api/customers/'+id, { method: 'DELETE' })
        .then(()=> loadCustomers())
        .catch(err => console.error(err));
}

function markFraud(id){
    if(!confirm('Mark this user as fraud? This will block and inactivate the account.')) return;
    fetch(`/api/customers/${id}/fraud-block`, { method: 'PUT' })
        .then(async r => {
            const data = await r.json().catch(() => ({}));
            if (!r.ok || data.ok === false) {
                alert(data.message || 'Failed to block user');
                return;
            }
            loadCustomers();
        })
        .catch(err => {
            console.error(err);
            alert('Failed to block user');
        });
}

function activateCustomer(id){
    fetch(`/api/customers/${id}/activate`, { method: 'PUT' })
        .then(async r => {
            const data = await r.json().catch(() => ({}));
            if (!r.ok || data.ok === false) {
                alert(data.message || 'Failed to activate user');
                return;
            }
            loadCustomers();
        })
        .catch(err => {
            console.error(err);
            alert('Failed to activate user');
        });
}

function showEdit(id){
    fetch('/api/customers/'+id).then(r=>r.json()).then(c=>{
        editingId = id;
        document.getElementById('eFirst').value = c.first || '';
        document.getElementById('eLast').value = c.last || '';
        document.getElementById('eContact').value = c.contact || '';
        document.getElementById('eEmail').value = c.email || '';
        document.getElementById('editModal').style.display = 'block';
    }).catch(err=>console.error(err));
}

function hideEdit(){ document.getElementById('editModal').style.display = 'none'; editingId = null; }

function saveEdit(){
    if(!editingId) return;
    const c = {
        first: document.getElementById('eFirst').value,
        last: document.getElementById('eLast').value,
        contact: document.getElementById('eContact').value,
        email: document.getElementById('eEmail').value
    };
    fetch('/api/customers/'+editingId, { method: 'PUT', headers: {'Content-Type':'application/json'}, body: JSON.stringify(c) })
        .then(async r => {
            const data = await r.json().catch(() => ({}));
            if (!r.ok || data.ok === false) {
                alert(data.message || 'Update failed');
                return;
            }
            hideEdit();
            loadCustomers();
        })
        .catch(err => console.error(err));
}

function clearForm(){ document.getElementById('first').value=''; document.getElementById('last').value=''; document.getElementById('contact').value=''; document.getElementById('email').value=''; }

function escapeHtml(str){ return String(str).replace(/[&<>"'`]/g, s=>({ '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":"&#39;","`":"&#96;" })[s]); }
