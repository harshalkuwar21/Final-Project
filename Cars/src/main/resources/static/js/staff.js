document.addEventListener('DOMContentLoaded', () => {
    loadShowroomsForStaff();
    loadStaff();
    ['sContact', 'eContact'].forEach(id => {
        const input = document.getElementById(id);
        if (input) {
            input.addEventListener('input', () => {
                input.value = input.value.replace(/\D/g, '').slice(0, 10);
            });
        }
    });
});

let showroomOptions = [];

function normalizeContact(value){
    return String(value || '').replace(/\D/g, '').slice(0, 10);
}

function isValidContact(value){
    return value === '' || /^\d{10}$/.test(value);
}

function loadShowroomsForStaff(){
    fetch('/api/dashboard/showrooms')
        .then(r => r.json())
        .then(list => {
            showroomOptions = Array.isArray(list) ? list : [];
            populateShowroomSelect('sShowroomId', null);
            populateShowroomSelect('eShowroomId', null);
        })
        .catch(err => console.error('Failed to load showrooms', err));
}

function populateShowroomSelect(selectId, selectedId){
    const el = document.getElementById(selectId);
    if (!el) return;
    el.innerHTML = '<option value="">No Showroom</option>';
    showroomOptions.forEach(s => {
        const option = document.createElement('option');
        option.value = s.id;
        option.textContent = s.name ? `${s.name} (#${s.id})` : `Showroom #${s.id}`;
        if (selectedId != null && String(selectedId) === String(s.id)) {
            option.selected = true;
        }
        el.appendChild(option);
    });
}

function loadStaff(){
    fetch('/api/dashboard/staff')
        .then(r=>r.json())
        .then(list=>{
            const tbody = document.getElementById('staffBody'); if(!tbody) return; tbody.innerHTML = '';
            list.forEach(u=>{
                const showroomText = u.showroomName
                    ? `${u.showroomName} (#${u.showroomId})`
                    : (u.showroomId ? `Showroom #${u.showroomId}` : 'Not Assigned');
                tbody.innerHTML += `
                    <tr data-id="${u.userid}">
                        <td>${u.first} ${u.last}</td>
                        <td>${u.email}</td>
                        <td>${u.contact||''}</td>
                        <td>${u.role||'ROLE_STAFF'}</td>
                        <td data-showroom-id="${u.showroomId||''}">${showroomText}</td>
                        <td>
                            <button class="btn-inline" onclick="openEdit(${u.userid})">Edit</button>
                            <button class="btn-inline btn-danger" onclick="deleteStaff(${u.userid})">Delete</button>
                        </td>
                    </tr>
                `;
            });
        });
}

function addStaff(){
    const first = document.getElementById('sFirst').value.trim();
    const last = document.getElementById('sLast').value.trim();
    const email = document.getElementById('sEmail').value.trim();
    const contact = normalizeContact(document.getElementById('sContact').value);
    const role = document.getElementById('sRole').value;
    const showroomId = document.getElementById('sShowroomId').value;
    const password = document.getElementById('sPassword').value;
    const msg = document.getElementById('sMsg');
    const btn = document.getElementById('sAddBtn');

    if(!first || !email){ if(msg) msg.innerText = 'First and email required'; return; }
    if(!isValidContact(contact)){ if(msg) msg.innerText = 'Contact must be exactly 10 digits'; return; }

    const body = { first, last, email, contact, role };
    body.showroomId = showroomId || null;
    if(password) body.password = password;

    if(btn){ btn.disabled = true; btn.innerText = 'Adding...'; }
    if(msg) msg.innerText = 'Adding...';

    fetch('/api/dashboard/staff', { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify(body) })
        .then(r=>r.json())
        .then(resp=>{
            if(resp.ok){ if(msg) msg.innerText = 'Added successfully'; document.getElementById('sFirst').value=''; document.getElementById('sLast').value=''; document.getElementById('sEmail').value=''; document.getElementById('sContact').value=''; document.getElementById('sPassword').value=''; document.getElementById('sShowroomId').value=''; loadStaff(); }
            else { if(msg) msg.innerText = resp.message || 'Failed to add'; }
        })
        .catch(err=>{ console.error(err); if(msg) msg.innerText = 'Failed'; })
        .finally(()=>{ if(btn){ btn.disabled=false; btn.innerText = 'Add Staff'; } setTimeout(()=>{ if(msg) msg.innerText=''; },2500); });
}

function openEdit(id){
    const row = document.querySelector(`tr[data-id='${id}']`);
    if(!row) return;
    const cols = row.children;
    const name = cols[0].innerText.split(' ');
    document.getElementById('eFirst').value = name[0]||'';
    document.getElementById('eLast').value = name[1]||'';
    document.getElementById('eContact').value = cols[2].innerText||'';
    document.getElementById('eRole').value = cols[3].innerText||'ROLE_STAFF';
    const showroomId = cols[4].dataset.showroomId || '';
    populateShowroomSelect('eShowroomId', showroomId || null);
    document.getElementById('eShowroomId').value = showroomId;
    document.getElementById('eEnabled').checked = true;
    document.getElementById('ePassword').value = '';

    document.getElementById('eSaveBtn').onclick = () => saveEdit(id);
    document.getElementById('editModal').style.display = 'flex';
}

function hideEdit(){ document.getElementById('editModal').style.display = 'none'; }

function saveEdit(id){
    const first = document.getElementById('eFirst').value.trim();
    const last = document.getElementById('eLast').value.trim();
    const contact = normalizeContact(document.getElementById('eContact').value);
    const role = document.getElementById('eRole').value;
    const showroomId = document.getElementById('eShowroomId').value;
    const enabled = document.getElementById('eEnabled').checked;
    const password = document.getElementById('ePassword').value;

    if(!isValidContact(contact)){ alert('Contact must be exactly 10 digits'); return; }

    const body = { first, last, contact, role, enabled, showroomId: showroomId || null };
    if(password) body.password = password;

    const btn = document.getElementById('eSaveBtn');
    btn.disabled = true; btn.innerText = 'Saving...';

    fetch(`/api/dashboard/staff/${id}`, { method: 'PUT', headers: {'Content-Type':'application/json'}, body: JSON.stringify(body) })
        .then(r=>r.json())
        .then(resp=>{
            if(resp.ok){ hideEdit(); loadStaff(); alert('Saved'); }
            else alert(resp.message || 'Failed to save');
        })
        .catch(err=>{ console.error(err); alert('Failed'); })
        .finally(()=>{ btn.disabled = false; btn.innerText = 'Save'; });
}

function deleteStaff(id){
    if(!confirm('Delete this staff member?')) return;
    fetch(`/api/dashboard/staff/${id}`, { method: 'DELETE' })
        .then(()=>{ loadStaff(); })
        .catch(err=>{ console.error(err); alert('Failed to delete'); });
}
