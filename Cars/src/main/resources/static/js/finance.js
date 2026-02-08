document.addEventListener('DOMContentLoaded', () => {
    loadSummary();
    loadPayments();
    document.getElementById('paymentDate').valueAsDate = new Date();
});

let editingId = null;

function loadSummary(){
    fetch('/api/finance/summary')
        .then(r => r.json())
        .then(data => {
            document.getElementById('todayCollection').textContent = (data.todayCashCollection || 0).toFixed(2);
            document.getElementById('pendingAmount').textContent = (data.totalPending || 0).toFixed(2);
            document.getElementById('totalCompleted').textContent = (data.totalCompleted || 0).toFixed(2);
            document.getElementById('totalTransactions').textContent = data.totalPayments || 0;
        })
        .catch(err => console.error('Failed to load summary', err));
}

function loadPayments(){
    fetch('/api/finance/payments')
        .then(r => r.json())
        .then(list => renderPayments(list))
        .catch(err => console.error('Failed to load payments', err));
}

function renderPayments(list){
    const body = document.getElementById('paymentsBody');
    body.innerHTML = '';
    if(list.length === 0){
        body.innerHTML = '<tr><td colspan="6" style="text-align:center;padding:20px;color:#999;">No payments recorded yet</td></tr>';
        return;
    }
    list.forEach(p => {
        const tr = document.createElement('tr');
        const statusClass = 'status-' + (p.status || 'pending').toLowerCase();
        tr.innerHTML = `<td>#${p.id}</td><td>₹${(p.amount || 0).toFixed(2)}</td><td>${escapeHtml(p.paymentMethod||'')}</td><td>${p.paymentDate||''}</td><td><span class="status-badge ${statusClass}">${escapeHtml(p.status||'')}</span></td><td><button class="btn btn-edit" onclick="showEdit(${p.id})"><i class="fas fa-edit"></i> Edit</button> <button class="btn btn-danger" onclick="deletePayment(${p.id})"><i class="fas fa-trash"></i> Delete</button></td>`;
        body.appendChild(tr);
    });
}

function addPayment(){
    const p = {
        amount: parseFloat(document.getElementById('amount').value) || 0,
        paymentMethod: document.getElementById('paymentMethod').value,
        status: document.getElementById('status').value,
        transactionId: document.getElementById('transactionId').value,
        paymentDate: document.getElementById('paymentDate').value
    };
    
    if(p.amount <= 0){
        document.getElementById('msg').textContent = 'Invalid amount';
        return;
    }
    
    fetch('/api/finance/payments', { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify(p) })
        .then(r => r.json())
        .then(_ => { document.getElementById('msg').textContent = 'Payment added'; clearForm(); loadPayments(); loadSummary(); setTimeout(()=>{document.getElementById('msg').textContent='';},1500); })
        .catch(err => { console.error(err); document.getElementById('msg').textContent = 'Error'; });
}

function deletePayment(id){
    if(!confirm('Delete payment record?')) return;
    fetch('/api/finance/payments/'+id, { method: 'DELETE' })
        .then(()=> {loadPayments(); loadSummary();})
        .catch(err => console.error(err));
}

function showEdit(id){
    fetch('/api/finance/payments/'+id).then(r=>r.json()).then(p=>{
        editingId = id;
        document.getElementById('eAmount').value = p.amount || '';
        document.getElementById('ePaymentMethod').value = p.paymentMethod || 'Cash';
        document.getElementById('eStatus').value = p.status || 'Pending';
        document.getElementById('eTransactionId').value = p.transactionId || '';
        document.getElementById('ePaymentDate').value = p.paymentDate || '';
        document.getElementById('editModal').style.display = 'block';
    }).catch(err=>console.error(err));
}

function hideEdit(){ document.getElementById('editModal').style.display = 'none'; editingId = null; }

function saveEdit(){
    if(!editingId) return;
    const p = {
        amount: parseFloat(document.getElementById('eAmount').value) || 0,
        paymentMethod: document.getElementById('ePaymentMethod').value,
        status: document.getElementById('eStatus').value,
        transactionId: document.getElementById('eTransactionId').value,
        paymentDate: document.getElementById('ePaymentDate').value
    };
    fetch('/api/finance/payments/'+editingId, { method: 'PUT', headers: {'Content-Type':'application/json'}, body: JSON.stringify(p) })
        .then(r=>r.json()).then(()=>{ hideEdit(); loadPayments(); loadSummary(); })
        .catch(err => console.error(err));
}

function clearForm(){ 
    document.getElementById('amount').value=''; 
    document.getElementById('transactionId').value='';
    document.getElementById('paymentMethod').value='Cash';
    document.getElementById('status').value='Completed';
    document.getElementById('paymentDate').valueAsDate = new Date();
}

function escapeHtml(str){ return String(str).replace(/[&<>"'`]/g, s=>({ '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":"&#39;","`":"&#96;" })[s]); }
