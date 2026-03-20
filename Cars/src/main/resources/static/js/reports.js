document.addEventListener('DOMContentLoaded', ()=>{
    loadSummary();
    setupYearSelect();
    const y = new Date().getFullYear(); document.getElementById('yearSelect').value = y;
    loadRevenue();
    loadTopBrands();
});

function loadSummary(){
    fetch('/api/dashboard/reports/summary')
        .then(r=>r.json())
        .then(d=>{
            document.getElementById('rTotalSales').innerText = d.totalSales || 0;
            document.getElementById('rRevenue').innerText = '₹' + (d.revenue || 0);
            document.getElementById('rVehicles').innerText = d.totalVehicles || 0;
            document.getElementById('rSold').innerText = d.soldCars || 0;
        }).catch(()=>{});
}

function setupYearSelect(){
    const sel = document.getElementById('yearSelect');
    const yr = new Date().getFullYear();
    for(let i=yr; i>=yr-5; i--){ sel.innerHTML += `<option value="${i}">${i}</option>`; }
}

function loadSalesRange(){
    const f = document.getElementById('fromDate').value;
    const t = document.getElementById('toDate').value;
    if(!f || !t){ alert('Choose both dates'); return; }
    fetch(`/api/dashboard/reports/sales?from=${f}&to=${t}`)
        .then(r=>r.json())
        .then(list=>{
            const body = document.getElementById('reportSalesBody'); body.innerHTML='';
            list.forEach(s=> body.innerHTML += `<tr><td>${s.date}</td><td>${s.buyer}</td><td>${s.model||''} (${s.brand||''})</td><td>₹ ${s.price||0}</td><td>${s.status||''}</td></tr>`);
        })
        .catch(err=>{ console.error(err); });
}

function exportSalesCsv(){
    const rows = Array.from(document.querySelectorAll('#reportSalesBody tr'));
    if(rows.length===0){ alert('No data'); return; }
    let csv = 'Date,Buyer,Car,Price,Status\n';
    rows.forEach(r=>{
        const cols = Array.from(r.children).map(c=> '"'+c.innerText.replace(/"/g,'""')+'"');
        csv += cols.join(',') + '\n';
    });
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a'); a.href = url; a.download = 'sales-report.csv'; document.body.appendChild(a); a.click(); a.remove(); URL.revokeObjectURL(url);
}

function loadRevenue(){
    const year = document.getElementById('yearSelect').value;
    fetch(`/api/dashboard/reports/revenue-by-year?year=${year}`)
        .then(r=>r.json())
        .then(list=>{
            const container = document.getElementById('revenueBars'); container.innerHTML='';
            const max = Math.max(...list.map(x=>x.revenue),1);
            list.forEach(x=>{
                const h = Math.round((x.revenue/max) * 100);
                container.innerHTML += `<div class="bar"><div class="fill" style="height:${h}%"></div><div class="label">${x.month}</div><div class="amt">₹${Math.round(x.revenue)}</div></div>`;
            });
        });
}

function loadTopBrands(){
    fetch('/api/dashboard/reports/top-brands')
        .then(r=>r.json())
        .then(list=>{
            const c = document.getElementById('topBrandsList'); c.innerHTML='';
            list.forEach(b=> c.innerHTML += `<div class="brand-item"><strong>${b.brand}</strong> — ${b.sold} sold</div>`);
        });
}


    const logoutBtn = document.getElementById("logoutBtn");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", async () => {
            if (!confirm("Are you sure you want to logout?")) return;
            try {
                await fetch("/user/logout", { method: "POST" });
            } catch (_) {
                // ignore network errors and proceed to login
            }
            window.location.href = "/login";
        });
    }