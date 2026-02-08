document.addEventListener("DOMContentLoaded", () => {
    loadStats();
    loadSales();
    loadTopSelling();
    loadInventory();
    loadOverview();
    loadStaff();
    loadShowrooms();
});

function loadOverview(){
    fetch('/api/dashboard/overview').then(r=>r.json()).then(data=>{
        if(document.getElementById('bookingsToday')) document.getElementById('bookingsToday').innerText = data.bookingsToday ?? 0;
        if(document.getElementById('vehiclesAvailable')) document.getElementById('vehiclesAvailable').innerText = data.vehiclesAvailable ?? 0;
        if(document.getElementById('pendingReviews')) document.getElementById('pendingReviews').innerText = data.pendingReviews ?? 0;
    }).catch(()=>{});
}

function loadStaff(){
    fetch('/api/dashboard/staff').then(r=>r.json()).then(list=>{
        const c = document.getElementById('staffList'); if(!c) return; c.innerHTML = '';
        list.forEach(u=>{
            c.innerHTML += `<div class="staff-card"><img src="/images/logo.png" alt=""><div class="staff-info">${u.first} ${u.last}<br>${u.email}</div><div class="staff-actions"><a href=\"/staff/edit/${u.userid}\">Edit</a><a href=\"#\" class=\"delete\" data-id=\"${u.userid}\">Delete</a></div></div>`;
        });
        c.querySelectorAll('.delete').forEach(btn=>btn.onclick = (e)=>{
            e.preventDefault(); const id = btn.dataset.id; if(!confirm('Delete this staff member?')) return; fetch(`/api/dashboard/staff/${id}`,{method:'DELETE'}).then(()=>loadStaff());
        });
    }).catch(()=>{});
}

// STATS -> populates hero stat cards
function loadStats() {
    fetch('/api/dashboard/stats')
        .then(r => r.json())
        .then(data => {
            // New dashboard metrics
            if (document.getElementById('totalVehicles'))
                document.getElementById('totalVehicles').innerText = data.totalVehicles ?? 0;
            if (document.getElementById('carsSoldToday'))
                document.getElementById('carsSoldToday').innerText = data.carsSoldToday ?? 0;
            if (document.getElementById('activeBookings'))
                document.getElementById('activeBookings').innerText = data.activeBookings ?? 0;
            if (document.getElementById('testDrivesToday'))
                document.getElementById('testDrivesToday').innerText = data.testDrivesToday ?? 0;
            if (document.getElementById('monthlyRevenue'))
                document.getElementById('monthlyRevenue').innerText = '₹' + (data.monthlyRevenue ?? 0).toLocaleString();
            if (document.getElementById('lowStockAlerts'))
                document.getElementById('lowStockAlerts').innerText = data.lowStockAlerts ? data.lowStockAlerts.length : 0;

            // Load charts after stats
            loadCharts();
        })
        .catch(err => console.error('Stats fetch failed', err));
}

// CHARTS -> load and render charts
function loadCharts() {
    // Monthly Sales Chart
    fetch('/api/dashboard/charts/monthly-sales')
        .then(r => r.json())
        .then(data => {
            const ctx = document.getElementById('monthlySalesChart');
            if (!ctx) return;

            new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: data.map(d => d.month),
                    datasets: [{
                        label: 'Sales',
                        data: data.map(d => d.sales),
                        backgroundColor: 'rgba(108, 78, 246, 0.6)',
                        borderColor: 'rgba(108, 78, 246, 1)',
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    scales: {
                        y: {
                            beginAtZero: true
                        }
                    }
                }
            });
        });

    // Model-wise Sales Chart
    fetch('/api/dashboard/charts/model-sales')
        .then(r => r.json())
        .then(data => {
            const ctx = document.getElementById('modelSalesChart');
            if (!ctx) return;

            new Chart(ctx, {
                type: 'pie',
                data: {
                    labels: data.map(d => d.model),
                    datasets: [{
                        data: data.map(d => d.count),
                        backgroundColor: [
                            '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0',
                            '#9966FF', '#FF9F40', '#FF6384', '#C9CBCF'
                        ]
                    }]
                },
                options: {
                    responsive: true
                }
            });
        });

    // Revenue Trend Chart
    fetch('/api/dashboard/charts/revenue-trend')
        .then(r => r.json())
        .then(data => {
            const ctx = document.getElementById('revenueTrendChart');
            if (!ctx) return;

            new Chart(ctx, {
                type: 'line',
                data: {
                    labels: data.map(d => d.month),
                    datasets: [{
                        label: 'Revenue (₹)',
                        data: data.map(d => d.revenue),
                        borderColor: 'rgba(75, 192, 192, 1)',
                        backgroundColor: 'rgba(75, 192, 192, 0.2)',
                        tension: 0.1
                    }]
                },
                options: {
                    responsive: true,
                    scales: {
                        y: {
                            beginAtZero: true
                        }
                    }
                }
            });
        });
}

// SALES -> populate sales table
function loadSales() {
    fetch('/api/dashboard/sales')
        .then(r => r.json())
        .then(rows => {
            const tbody = document.getElementById('salesTable');
            if (!tbody) return;
            tbody.innerHTML = '';

            rows.forEach(row => {
                let cls = 'pending';
                const st = (row.status || '').toLowerCase();
                if (st === 'paid' || st === 'approved' || st === 'completed') cls = 'success';
                else if (st === 'rejected') cls = 'rejected';

                tbody.innerHTML += `
                    <tr>
                        <td>${row.model}</td>
                        <td>${row.buyer}</td>
                        <td>${row.date}</td>
                        <td><span class="badge ${cls}">${row.status}</span></td>
                    </tr>
                `;
            });
        })
        .catch(err => console.error('Sales fetch failed', err));
}

// TOP SELLING
function loadTopSelling() {
    fetch('/api/dashboard/top-selling')
        .then(r => r.json())
        .then(rows => {
            // left as future: populate a side list if present
            const el = document.getElementById('topCars');
            if (!el) return;
            el.innerHTML = '';
            rows.forEach(rw => el.innerHTML += `<li>${rw.model} (${rw.totalSold} sold)</li>`);
        })
        .catch(() => {});
}

// INVENTORY
function loadInventory() {
    fetch('/api/dashboard/inventory')
        .then(r => r.json())
        .then(data => {
            const barContainer = document.getElementById('inventoryBars');
            const labelContainer = document.getElementById('inventoryLabels');
            if (!barContainer || !labelContainer) return;
            barContainer.innerHTML = '';
            labelContainer.innerHTML = '';

            const max = Math.max(...data.map(d => d.count), 1);
            data.forEach(item => {
                const h = Math.round((item.count / max) * 100);
                barContainer.innerHTML += `<div title="${item.brand} - ${item.count}" style="height:${h}%"></div>`;
                labelContainer.innerHTML += `${item.brand} (${item.count}) &nbsp; `;
            });
        })
        .catch(() => {});
}

// simple sidebar routing
// const menu = document.querySelectorAll('.sidebar li');
// const sections = document.querySelectorAll('.page-section');
// menu.forEach(item => {
//     item.onclick = () => {
//         menu.forEach(i => i.classList.remove('active'));
//         item.classList.add('active');
//         sections.forEach(sec => sec.classList.remove('active'));
//         if (item.dataset.page) document.getElementById(item.dataset.page).classList.add('active');
//     };
// });

// showroom card rendering cleanup (kept minimal)
const grid = document.getElementById('grid');
let showrooms = [];

function render() {
    if (!grid) return;
    grid.innerHTML = '';
    showrooms.forEach(s => {
        const type = s.type || 'Normal';
        const cls = (type || 'normal').toLowerCase();
        const img = s.img || '/images/car.jpg';
        grid.innerHTML += `
            <div class="showroom-card">
                <img src="${img}" alt="${s.name}">
                <div class="showroom-info">
                    <h4>${s.name}</h4>
                    <p>${s.city}</p>
                    <span class="badge ${cls}">${type}</span>
                </div>
            </div>
        `;
    });
}

function loadShowrooms() {
    fetch('/api/dashboard/showrooms')
        .then(r => r.json())
        .then(list => {
            showrooms = list.map(s => ({ id: s.id, name: s.name, city: s.city, img: s.image }));
            render();
        })
        .catch(err => console.error('Failed to load showrooms', err));
}

function addShowroom() {
    const nameEl = document.getElementById('srName');
    const cityEl = document.getElementById('srCity');
    const imgEl = document.getElementById('srImage');
    const typeEl = document.getElementById('srType');
    const addBtn = document.getElementById('srAddBtn');
    const msg = document.getElementById('srMsg');
    const preview = document.getElementById('srPreview');

    const name = nameEl ? nameEl.value.trim() : '';
    const city = cityEl ? cityEl.value.trim() : '';
    const type = typeEl ? typeEl.value : '';

    if (!name || !city) {
        if (msg) msg.innerText = 'Please enter name and city';
        return;
    }

    if (!imgEl || !imgEl.files || imgEl.files.length === 0) {
        if (msg) msg.innerText = 'Please select an image';
        return;
    }

    const fd = new FormData();
    fd.append('name', name);
    fd.append('city', city);
    if (type) fd.append('type', type);
    fd.append('image', imgEl.files[0]);

    if (msg) msg.innerText = 'Uploading...';
    if (addBtn) { addBtn.disabled = true; addBtn.innerText = 'Uploading...'; }

    fetch('/api/dashboard/showroom', { method: 'POST', body: fd })
        .then(r => {
            if (!r.ok) return r.text().then(t => { throw new Error(t || r.statusText); });
            return r.json();
        })
        .then(newShowroom => {
            if (msg) msg.innerText = 'Showroom added ✅';
            if (nameEl) nameEl.value = '';
            if (cityEl) cityEl.value = '';
            if (imgEl) imgEl.value = '';
            if (typeEl) typeEl.selectedIndex = 0;
            if (preview) { preview.style.display = 'none'; preview.src = ''; }
            loadShowrooms();
        })
        .catch(err => {
            console.error(err);
            if (msg) msg.innerText = 'Failed: ' + (err.message || 'Upload failed');
        })
        .finally(() => {
            if (addBtn) { addBtn.disabled = false; addBtn.innerText = 'Add'; }
            setTimeout(() => { if (msg) msg.innerText = ''; }, 3500);
        });
}

// show a small preview when a file is chosen
(function(){
    const imgEl = document.getElementById('srImage');
    const preview = document.getElementById('srPreview');
    if (!imgEl || !preview) return;
    imgEl.addEventListener('change', () => {
        const f = imgEl.files && imgEl.files[0];
        if (!f) { preview.style.display = 'none'; preview.src = ''; return; }
        preview.src = URL.createObjectURL(f);
        preview.style.display = 'inline-block';
    });
})();

// load showrooms initially
loadShowrooms();
