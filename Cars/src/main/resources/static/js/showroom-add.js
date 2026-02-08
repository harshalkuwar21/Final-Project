document.addEventListener('DOMContentLoaded', () => {
    const img = document.getElementById('srImage');
    const preview = document.getElementById('previewImg');
    if(img){
        img.addEventListener('change', () => {
            const f = img.files && img.files[0];
            if(!f){ preview.style.display='none'; preview.src=''; return; }
            preview.src = URL.createObjectURL(f);
            preview.style.display = 'inline-block';
        });
    }
});

function saveShowroom(){
    const name = (document.getElementById('srName')||{}).value || '';
    const city = (document.getElementById('srCity')||{}).value || '';
    const type = (document.getElementById('srType')||{}).value || '';
    const imageEl = document.getElementById('srImage');
    const status = document.getElementById('srStatus');
    const btn = document.getElementById('srAddBtn');

    if(!name.trim() || !city.trim() || !imageEl || !imageEl.files || imageEl.files.length===0){
        if(status) status.innerText = 'Please fill all fields and choose an image.';
        return;
    }

    const fd = new FormData();
    fd.append('name', name.trim());
    fd.append('city', city.trim());
    fd.append('type', type);
    fd.append('image', imageEl.files[0]);

    if(btn){ btn.disabled = true; btn.innerText = 'Uploading...'; }
    if(status) status.innerText = 'Uploading...';

    fetch('/api/dashboard/showroom', { method: 'POST', body: fd })
        .then(r => {
            if(!r.ok) return r.text().then(t => { throw new Error(t||r.statusText); });
            return r.json();
        })
        .then(s => {
            if(status) status.innerText = 'Showroom added successfully! Redirecting...';
            setTimeout(()=>{ window.location.href = '/showrooms'; }, 1000);
        })
        .catch(err => {
            console.error(err);
            if(status) status.innerText = 'Failed to add showroom: ' + (err.message||'');
        })
        .finally(()=>{ if(btn){ btn.disabled=false; btn.innerText = '+ Add Showroom'; } });
}