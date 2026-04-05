document.addEventListener('DOMContentLoaded', () => {
    loadShowroomsForStaff();
    loadStaff();
    ['sContact', 'eContact', 'spContact'].forEach(id => {
        const input = document.getElementById(id);
        if (input) {
            input.addEventListener('input', () => {
                input.value = input.value.replace(/\D/g, '').slice(0, 10);
            });
        }
    });

    const bellBtn = document.getElementById('staffBellBtn');
    if (bellBtn) {
        bellBtn.addEventListener('click', () => {
            openStaffNotifications();
        });
    }

    const profileBtn = document.getElementById('staffProfileBtn');
    if (profileBtn) {
        profileBtn.addEventListener('click', openStaffProfile);
    }

    const profileSaveBtn = document.getElementById('spSaveBtn');
    if (profileSaveBtn) {
        profileSaveBtn.addEventListener('click', saveStaffProfile);
    }

    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', async () => {
            if (!confirm('Are you sure you want to logout?')) return;
            try {
                await fetch('/user/logout', { method: 'POST' });
            } catch (_) {
                // ignore network errors and proceed to login
            }
            try {
                sessionStorage.setItem('logoutMessage', 'Logged out successfully.');
            } catch (_) {
                // ignore storage errors
            }
            window.location.href = '/login?logout=1';
        });
    }
});

let showroomOptions = [];

function normalizeContact(value){
    return String(value || '').replace(/\D/g, '').slice(0, 10);
}

function isValidContact(value){
    return value === '' || /^\d{10}$/.test(value);
}

function setStaffProfileMessage(message, isError = false) {
    const msg = document.getElementById('spMsg');
    if (!msg) return;
    msg.style.color = isError ? '#b91c1c' : '#666';
    msg.innerText = message || '';
}

function hideStaffProfile() {
    const modal = document.getElementById('staffProfileModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

function openStaffNotifications() {
    const modal = document.getElementById('staffNotificationModal');
    const text = document.getElementById('staffNotificationText');
    if (!modal) return;
    if (text) {
        text.innerText = 'No new staff notifications right now.';
    }
    modal.style.display = 'flex';
}

function hideStaffNotifications() {
    const modal = document.getElementById('staffNotificationModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

function openStaffProfile() {
    const modal = document.getElementById('staffProfileModal');
    if (!modal) return;
    modal.style.display = 'flex';
    setStaffProfileMessage('Loading profile...');

    fetch('/user/profile')
        .then(async r => {
            const data = await r.json();
            if (!r.ok) {
                throw new Error(data.error || 'Unable to load profile');
            }
            document.getElementById('spFirst').value = data.first || '';
            document.getElementById('spLast').value = data.last || '';
            document.getElementById('spEmail').value = data.email || '';
            document.getElementById('spContact').value = normalizeContact(data.contact || '');
            document.getElementById('spPassword').value = '';
            setStaffProfileMessage('');
        })
        .catch(err => {
            console.error(err);
            setStaffProfileMessage(err.message || 'Unable to load profile', true);
        });
}

function saveStaffProfile() {
    const first = document.getElementById('spFirst').value.trim();
    const last = document.getElementById('spLast').value.trim();
    const email = document.getElementById('spEmail').value.trim();
    const contact = normalizeContact(document.getElementById('spContact').value);
    const password = document.getElementById('spPassword').value;
    const saveBtn = document.getElementById('spSaveBtn');

    if (!email) {
        setStaffProfileMessage('Email is required', true);
        return;
    }
    if (!isValidContact(contact)) {
        setStaffProfileMessage('Contact must be exactly 10 digits', true);
        return;
    }

    const payload = { first, last, email, contact };
    if (password && password.trim()) {
        payload.password = password;
    }

    saveBtn.disabled = true;
    setStaffProfileMessage('Saving profile...');

    fetch('/user/profile', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
        .then(async r => {
            const data = await r.json();
            if (!r.ok || !data.ok) {
                throw new Error(data.error || 'Unable to save profile');
            }
            document.getElementById('spPassword').value = '';
            setStaffProfileMessage('Profile updated successfully.');
        })
        .catch(err => {
            console.error(err);
            setStaffProfileMessage(err.message || 'Unable to save profile', true);
        })
        .finally(() => {
            saveBtn.disabled = false;
        });
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

window.addEventListener('click', (event) => {
    const editModal = document.getElementById('editModal');
    const notificationModal = document.getElementById('staffNotificationModal');
    const profileModal = document.getElementById('staffProfileModal');
    if (event.target === editModal) {
        hideEdit();
    }
    if (event.target === notificationModal) {
        hideStaffNotifications();
    }
    if (event.target === profileModal) {
        hideStaffProfile();
    }
});
