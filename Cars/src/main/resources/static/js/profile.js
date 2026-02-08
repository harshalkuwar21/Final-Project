let currentProfileId = null;

async function loadProfileToModal() {
    const r = await fetch('/user/profile');
    if (!r.ok) return;
    const j = await r.json();
    currentProfileId = j.id;
    document.getElementById('pFirst').value = j.first || '';
    document.getElementById('pLast').value = j.last || '';
    document.getElementById('pEmail').value = j.email || '';
    document.getElementById('pContact').value = j.contact || '';
}

async function openProfile() {
    await loadProfileToModal();
    document.getElementById('profileModal').style.display = 'block';
}
function hideProfile() { document.getElementById('profileModal').style.display = 'none'; }

async function saveProfileFromModal() {
    const payload = { id: String(currentProfileId), first: document.getElementById('pFirst').value.trim(), last: document.getElementById('pLast').value.trim(), email: document.getElementById('pEmail').value.trim(), contact: document.getElementById('pContact').value.trim() };
    const pwd = document.getElementById('pPassword').value;
    if (pwd && pwd.trim().length>0) payload.password = pwd;
    const btn = document.getElementById('pSaveBtn'); btn.disabled = true;
    const r = await fetch('/user/profile', { method: 'PUT', headers: {'Content-Type':'application/json'}, body: JSON.stringify(payload) });
    btn.disabled = false;
    const j = await r.json();
    if (r.ok && j.ok) {
        showToast('Saved');
        hideProfile();
    } else {
        showToast(j.error || 'Error saving', true);
    }
}

// page functions
async function loadProfileToPage() {
    const r = await fetch('/user/profile');
    if (!r.ok) return;
    const j = await r.json();
    currentProfileId = j.id;
    document.getElementById('pFirstPage').value = j.first || '';
    document.getElementById('pLastPage').value = j.last || '';
    document.getElementById('pEmailPage').value = j.email || '';
    document.getElementById('pContactPage').value = j.contact || '';
}

async function saveProfileFromPage() {
    const payload = { id: String(currentProfileId), first: document.getElementById('pFirstPage').value.trim(), last: document.getElementById('pLastPage').value.trim(), email: document.getElementById('pEmailPage').value.trim(), contact: document.getElementById('pContactPage').value.trim() };
    const pwd = document.getElementById('pPasswordPage').value;
    if (pwd && pwd.trim().length>0) payload.password = pwd;
    const btn = document.getElementById('pSavePageBtn'); btn.disabled = true;
    const r = await fetch('/user/profile', { method: 'PUT', headers: {'Content-Type':'application/json'}, body: JSON.stringify(payload) });
    btn.disabled = false;
    const j = await r.json();
    if (r.ok && j.ok) {
        document.getElementById('pMsg').textContent = 'Saved';
        setTimeout(()=>document.getElementById('pMsg').textContent = '', 2000);
    } else {
        document.getElementById('pMsg').textContent = j.error || 'Error saving';
    }
}

function showToast(msg, err) {
    let t = document.getElementById('toast');
    if (!t) { t = document.createElement('div'); t.id = 'toast'; document.body.appendChild(t); }
    t.textContent = msg; t.style.background = err ? '#fdd' : '#dfd'; t.style.color = '#222'; t.style.border = '1px solid ' + (err ? '#f99' : '#9f9'); t.style.padding = '8px 12px'; t.style.position = 'fixed'; t.style.right = '16px'; t.style.bottom = '16px'; t.style.zIndex = 9999;
    setTimeout(()=>{ try { t.remove(); } catch(e){} }, 2200);
}

window.addEventListener('load', ()=>{
    const ui = document.getElementById('userIcon');
    if (ui) ui.addEventListener('click', openProfile);
    const saveBtn = document.getElementById('pSaveBtn'); if (saveBtn) saveBtn.addEventListener('click', saveProfileFromModal);
    const savePageBtn = document.getElementById('pSavePageBtn'); if (savePageBtn) { savePageBtn.addEventListener('click', saveProfileFromPage); loadProfileToPage(); }
});