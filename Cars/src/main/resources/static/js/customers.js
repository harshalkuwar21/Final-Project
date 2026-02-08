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
        tr.innerHTML = `<td>${escapeHtml(c.name||'')}</td><td>${escapeHtml(c.mobile||'')}</td><td>${escapeHtml(c.email||'')}</td><td><span style="background:#e8f5e9;padding:4px 10px;border-radius:6px;color:#2e7d32;font-size:12px;font-weight:600">${escapeHtml(c.leadSource||'')}</span></td><td>${c.nextFollowUpDate||''}</td><td><button class="btn btn-edit" onclick="showEdit(${c.id})"><i class="fas fa-edit"></i> Edit</button> <button class="btn btn-danger" onclick="deleteCustomer(${c.id})"><i class="fas fa-trash"></i> Delete</button></td>`;
        body.appendChild(tr);
    });
}

function addCustomer(){
    const c = {
        name: document.getElementById('name').value,
        mobile: document.getElementById('mobile').value,
        email: document.getElementById('email').value,
        address: document.getElementById('address').value,
        leadSource: document.getElementById('leadSource').value,
        nextFollowUpDate: document.getElementById('nextFollowUpDate').value || null
    };
    fetch('/api/customers', { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify(c) })
        .then(r => r.json())
        .then(_ => { document.getElementById('msg').textContent = 'Added'; clearForm(); loadCustomers(); setTimeout(()=>{document.getElementById('msg').textContent='';},1500); })
        .catch(err => { console.error(err); document.getElementById('msg').textContent = 'Error'; });
}

function deleteCustomer(id){
    if(!confirm('Delete customer?')) return;
    fetch('/api/customers/'+id, { method: 'DELETE' })
        .then(()=> loadCustomers())
        .catch(err => console.error(err));
}

function showEdit(id){
    fetch('/api/customers/'+id).then(r=>r.json()).then(c=>{
        editingId = id;
        document.getElementById('eName').value = c.name || '';
        document.getElementById('eMobile').value = c.mobile || '';
        document.getElementById('eEmail').value = c.email || '';
        document.getElementById('eAddress').value = c.address || '';
        document.getElementById('eLeadSource').value = c.leadSource || 'Walk-in';
        document.getElementById('eNextFollowUpDate').value = c.nextFollowUpDate || '';
        document.getElementById('editModal').style.display = 'block';
    }).catch(err=>console.error(err));
}

function hideEdit(){ document.getElementById('editModal').style.display = 'none'; editingId = null; }

function saveEdit(){
    if(!editingId) return;
    const c = {
        name: document.getElementById('eName').value,
        mobile: document.getElementById('eMobile').value,
        email: document.getElementById('eEmail').value,
        address: document.getElementById('eAddress').value,
        leadSource: document.getElementById('eLeadSource').value,
        nextFollowUpDate: document.getElementById('eNextFollowUpDate').value || null
    };
    fetch('/api/customers/'+editingId, { method: 'PUT', headers: {'Content-Type':'application/json'}, body: JSON.stringify(c) })
        .then(r=>r.json()).then(()=>{ hideEdit(); loadCustomers(); })
        .catch(err => console.error(err));
}

function clearForm(){ document.getElementById('name').value=''; document.getElementById('mobile').value=''; document.getElementById('email').value=''; document.getElementById('address').value=''; }

function escapeHtml(str){ return String(str).replace(/[&<>"'`]/g, s=>({ '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":"&#39;","`":"&#96;" })[s]); }
