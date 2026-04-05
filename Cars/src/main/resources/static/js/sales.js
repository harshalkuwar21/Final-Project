document.addEventListener('DOMContentLoaded', ()=>{
    loadCarsSelect();
    loadSales();
    document.getElementById('searchSales').addEventListener('input', e=>filterSales(e.target.value));
});

let SALES = [];
let SALES_FILTERED = [];

function loadCarsSelect(){
    fetch('/api/dashboard/cars')
        .then(r=>r.json())
        .then(list=>{
            const sel = document.getElementById('sCar'); if(!sel) return; sel.innerHTML='';
            list.forEach(c=> sel.innerHTML += `<option value="${c.id}">${c.model} - ${c.brand} (${c.price})</option>`);
        });
}

function loadSales(){
    fetch('/api/dashboard/sales/all')
        .then(r=>r.json())
        .then(list=>{ SALES = list; SALES_FILTERED = list.slice(); renderSales(SALES_FILTERED); })
        .catch(err=>console.error('sales load failed',err));
}

function renderSales(list){
    const body = document.getElementById('salesBody'); if(!body) return; body.innerHTML='';
    list.forEach(s=>{
        body.innerHTML += `<tr data-id="${s.id}">
            <td>${s.model||''} (${s.brand||''})</td>
            <td>${s.buyer||''}</td>
            <td>${s.date||''}</td>
            <td>${s.status||''}</td>
            <td>
                <button class="btn-inline" onclick="openSale(${s.id})">View</button>
                <button class="btn-inline btn-danger" onclick="deleteSale(${s.id})">Delete</button>
            </td>
        </tr>`;
    });
}

function filterSales(k){
    const q = (k||'').toLowerCase();
    SALES_FILTERED = SALES.filter(s=> (s.buyer||'').toLowerCase().includes(q) || (s.model||'').toLowerCase().includes(q));
    renderSales(SALES_FILTERED);
}

function createSale(){
    const carId = document.getElementById('sCar').value;
    const buyer = document.getElementById('sBuyer').value.trim();
    const status = document.getElementById('sStatus').value;
    const msg = document.getElementById('sMsg');
    if(!carId || !buyer){ if(msg) msg.innerText='Select car and enter buyer'; return; }
    if(msg) msg.innerText = 'Creating...';

    fetch('/api/dashboard/sales', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({carId, buyer, status}) })
        .then(r=>r.json())
        .then(resp=>{ if(resp.ok){ if(msg) msg.innerText='Created'; loadSales(); document.getElementById('sBuyer').value=''; } else { if(msg) msg.innerText = resp.message||'Failed'; } })
        .catch(err=>{ console.error(err); if(msg) msg.innerText='Failed'; })
        .finally(()=> setTimeout(()=>{ if(msg) msg.innerText=''; },2500));
}

function openSale(id){
    fetch(`/api/dashboard/sales/${id}`)
        .then(r=>r.json())
        .then(s=>{
            document.getElementById('saleImg').src = s.image || '/images/car.jpg';
            document.getElementById('saleCar').innerText = `${s.model||''} (${s.brand||''})`;
            document.getElementById('saleBuyer').innerText = `Buyer: ${s.buyer||''}`;
            document.getElementById('saleDate').innerText = `Date: ${s.date||''}`;
            document.getElementById('saleStatus').innerText = `Status: ${s.status||''}`;
            document.getElementById('saleStatusSelect').value = s.status || 'Paid';
            document.getElementById('saveSaleBtn').onclick = ()=> saveSaleChanges(id);
            document.getElementById('saleModal').style.display = 'flex';
        });
}

function hideSaleModal(){ document.getElementById('saleModal').style.display='none'; }

function saveSaleChanges(id){
    const status = document.getElementById('saleStatusSelect').value;
    const btn = document.getElementById('saveSaleBtn'); btn.disabled = true; btn.innerText='Saving...';
    fetch(`/api/dashboard/sales/${id}`, { method:'PUT', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ status }) })
        .then(r=>r.json())
        .then(resp=>{ if(resp.ok){ hideSaleModal(); loadSales(); alert('Saved'); } else alert(resp.message||'Failed'); })
        .catch(err=>{ console.error(err); alert('Failed'); })
        .finally(()=>{ btn.disabled=false; btn.innerText='Save'; });
}

function deleteSale(id){ if(!confirm('Delete this sale?')) return; fetch(`/api/dashboard/sales/${id}`, { method:'DELETE' }).then(r=>r.json()).then(resp=>{ if(resp.ok){ loadSales(); } else alert(resp.message||'Failed'); }).catch(err=>{ console.error(err); alert('Failed'); }); }



    const logoutBtn = document.getElementById("logoutBtn");
    const redirectAfterLogout = (message) => {
        try {
            sessionStorage.setItem("logoutMessage", message || "Logged out successfully.");
        } catch (_) {
            // ignore storage errors
        }
        window.location.href = "/login?logout=1";
    };
    if (logoutBtn) {
        logoutBtn.addEventListener("click", async () => {
            if (!confirm("Are you sure you want to logout?")) return;
            try {
                await fetch("/user/logout", { method: "POST" });
            } catch (_) {
                // ignore network errors and proceed to login
            }
            redirectAfterLogout("Logged out successfully.");
        });
    }
