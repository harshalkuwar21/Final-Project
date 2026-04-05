async function loadSettings() {
    const res = await fetch('/api/dashboard/settings/list');
    if (!res.ok) return;
    const data = await res.json();
    const tbody = document.getElementById('settingsBody');
    tbody.innerHTML = '';
    data.forEach(s => {
        const tr = document.createElement('tr');
        tr.dataset.id = s.id;
        const nameTd = document.createElement('td');
        const nameInp = document.createElement('input');
        nameInp.value = s.name;
        nameInp.style.width = '100%';
        nameTd.appendChild(nameInp);

        const valTd = document.createElement('td');
        const valInp = document.createElement('input');
        valInp.value = s.value;
        valInp.style.width = '100%';
        valTd.appendChild(valInp);

        const actionsTd = document.createElement('td');
        const saveBtn = document.createElement('button');
        saveBtn.textContent = 'Save';
        saveBtn.className = 'btn-primary';
        saveBtn.onclick = async () => {
            const id = tr.dataset.id;
            const payload = { name: nameInp.value.trim(), value: valInp.value };
            if (!payload.name) { showToast('Name required', true); return; }
            saveBtn.disabled = true;
            const r = await fetch('/api/dashboard/settings/' + id, {
                method: 'PUT', headers: {'Content-Type':'application/json'}, body: JSON.stringify(payload)
            });
            saveBtn.disabled = false;
            const j = await r.json();
            if (r.ok && j.ok) {
                showToast('Saved');
                loadSettings();
            } else {
                showToast(j.error || 'Error saving', true);
            }
        };
        const delBtn = document.createElement('button');
        delBtn.textContent = 'Delete';
        delBtn.className = 'btn-danger';
        delBtn.onclick = async () => {
            if (!confirm('Delete setting "' + nameInp.value + '"?')) return;
            delBtn.disabled = true;
            const r = await fetch('/api/dashboard/settings/id/' + tr.dataset.id, {method: 'DELETE'});
            delBtn.disabled = false;
            if (r.ok) {
                const j = await r.json();
                if (j.ok) {
                    showToast('Deleted');
                    loadSettings();
                } else showToast(j.error || 'Not found', true);
            } else showToast('Delete failed', true);
        };
        actionsTd.appendChild(saveBtn);
        actionsTd.appendChild(delBtn);
        tr.appendChild(nameTd);
        tr.appendChild(valTd);
        tr.appendChild(actionsTd);
        tbody.appendChild(tr);
    });
}

function showMsg(msg, err) {
    const el = document.getElementById('sMsg');
    el.textContent = msg;
    el.style.color = err ? 'red' : '#666';
    setTimeout(()=>el.textContent = '', 2500);
}

function showToast(msg, err) {
    let t = document.getElementById('toast');
    if (!t) {
        t = document.createElement('div'); t.id = 'toast'; document.body.appendChild(t);
    }
    t.textContent = msg;
    t.style.background = err ? '#fdd' : '#dfd';
    t.style.color = '#222';
    t.style.border = '1px solid ' + (err ? '#f99' : '#9f9');
    t.style.padding = '8px 12px';
    t.style.position = 'fixed';
    t.style.right = '16px';
    t.style.bottom = '16px';
    t.style.zIndex = 9999;
    setTimeout(()=>{ try { t.remove(); } catch(e){} }, 2200);
}

async function saveSetting() {
    const name = document.getElementById('newName').value.trim();
    const value = document.getElementById('newValue').value.trim();
    if (!name) { showToast('Name required', true); return; }
    const payload = { name: name, value: value };
    const r = await fetch('/api/dashboard/settings', {
        method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify(payload)
    });
    const j = await r.json();
    if (r.ok && j.ok) {
        showToast('Saved');
        document.getElementById('newName').value = '';
        document.getElementById('newValue').value = '';
        loadSettings();
    } else {
        showToast(j.error || 'Error saving', true);
    }
}

window.addEventListener('load', ()=>{
    loadSettings();
});