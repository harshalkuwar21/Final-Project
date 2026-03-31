document.addEventListener('DOMContentLoaded', () => {
    const img = document.getElementById('srImage');
    const contactInput = document.getElementById('srContact');
    if(img){
        img.addEventListener('change', () => {
            updatePreview(img.files && img.files[0]);
        });
    }
    if (contactInput) {
        contactInput.addEventListener('input', () => {
            contactInput.value = contactInput.value.replace(/\D/g, '').slice(0, 10);
        });
    }
    populateTimeOptions();
});

function populateTimeOptions() {
    const openSelect = document.getElementById('srOpenTime');
    const closeSelect = document.getElementById('srCloseTime');
    if (!openSelect || !closeSelect) {
        return;
    }

    const options = [];
    for (let hour = 0; hour < 24; hour++) {
        for (let minute = 0; minute < 60; minute += 30) {
            const value = `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`;
            options.push({ value, label: formatTime(value) });
        }
    }

    const markup = options.map(option => `<option value="${option.value}">${option.label}</option>`).join('');
    openSelect.innerHTML = markup;
    closeSelect.innerHTML = markup;
    openSelect.value = '09:00';
    closeSelect.value = '20:00';
}

function formatTime(value) {
    const [hourText, minuteText] = String(value || '00:00').split(':');
    let hour = Number(hourText);
    const minute = minuteText || '00';
    const suffix = hour >= 12 ? 'PM' : 'AM';
    hour = hour % 12 || 12;
    return `${String(hour).padStart(2, '0')}:${minute} ${suffix}`;
}

function updatePreview(file){
    const preview = document.getElementById('previewImg');
    if(!preview){
        return;
    }
    if(!file){
        preview.style.display='none';
        preview.src='';
        return;
    }
    preview.src = URL.createObjectURL(file);
    preview.style.display = 'inline-block';
}

function previewImage(event){
    updatePreview(event && event.target && event.target.files ? event.target.files[0] : null);
}

function saveShowroom(){
    const name = (document.getElementById('srName')||{}).value || '';
    const city = (document.getElementById('srCity')||{}).value || '';
    const address = (document.getElementById('srAddress')||{}).value || '';
    const contactNumber = (document.getElementById('srContact')||{}).value || '';
    const type = (document.getElementById('srType')||{}).value || '';
    const openTime = (document.getElementById('srOpenTime')||{}).value || '';
    const closeTime = (document.getElementById('srCloseTime')||{}).value || '';
    const imageEl = document.getElementById('srImage');
    const status = document.getElementById('srStatus');
    const btn = document.getElementById('srAddBtn');

    const digits = String(contactNumber).replace(/\D/g, '');
    if(!name.trim() || !city.trim() || !address.trim() || !contactNumber.trim() || !openTime || !closeTime){
        if(status) status.innerText = 'Please fill showroom name, city, address, contact number, and select showroom time.';
        return;
    }

    if(digits.length !== 10){
        if(status) status.innerText = 'Contact number must be exactly 10 digits.';
        return;
    }

    if(openTime === closeTime){
        if(status) status.innerText = 'Opening and closing time cannot be the same.';
        return;
    }

    const workingHours = `${formatTime(openTime)} - ${formatTime(closeTime)}`;

    const fd = new FormData();
    fd.append('name', name.trim());
    fd.append('city', city.trim());
    fd.append('address', address.trim());
    fd.append('contactNumber', digits);
    fd.append('type', type);
    fd.append('workingHours', workingHours);
    if(imageEl && imageEl.files && imageEl.files[0]){
        fd.append('image', imageEl.files[0]);
    }

    if(btn){ btn.disabled = true; btn.innerText = 'Uploading...'; }
    if(status) status.innerText = 'Uploading...';

    fetch('/api/dashboard/showroom', { method: 'POST', body: fd })
        .then(r => {
            if(!r.ok) return r.text().then(t => { throw new Error(t||r.statusText); });
            return r.json();
        })
        .then(s => {
            if(!s.ok){
                throw new Error(s.message || 'Unable to save showroom');
            }
            if(status) status.innerText = 'Showroom added successfully! Redirecting...';
            setTimeout(()=>{ window.location.href = '/showrooms'; }, 1000);
        })
        .catch(err => {
            console.error(err);
            if(status) status.innerText = 'Failed to add showroom: ' + (err.message||'');
        })
        .finally(()=>{ if(btn){ btn.disabled=false; btn.innerText = '+ Add Showroom'; } });
}
