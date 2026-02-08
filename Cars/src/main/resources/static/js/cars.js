document.addEventListener('DOMContentLoaded', () => {
    const params = new URLSearchParams(window.location.search);
    const showroomId = params.get('showroomId');
    const showroomName = params.get('name') ? decodeURIComponent(params.get('name')) : '';
    if (showroomName) document.getElementById('title').innerText = `Cars in ${showroomName}`;

    window.CARS_STATE = { showroomId: showroomId };

    loadCars();

    document.getElementById('search').addEventListener('input', (e) => filterCars(e.target.value));
    document.getElementById('sort').addEventListener('change', (e)=>{ sortCars(e.target.value); });
});

let carList = [];
let filteredList = [];

function loadCars(){
    const id = window.CARS_STATE.showroomId;
    if(!id){document.getElementById('carGrid').innerHTML = '<p style="color:#666">No showroom selected.</p>'; return}

    fetch(`/api/dashboard/showrooms/${id}/cars`)
        .then(r => r.json())
        .then(list => {
            carList = list;
            filteredList = list.slice();
            renderCars(filteredList);
        })
        .catch(err => {
            console.error('Failed to load cars',err);
        });
}

function renderCars(list){
    const grid = document.getElementById('carGrid');
    grid.innerHTML = '';
    if(!list.length){ grid.innerHTML = '<p style="color:#666">No cars available.</p>'; return }

    list.forEach(c => {
        const availClass = c.available ? 'badge-available' : 'badge-sold';
        grid.innerHTML += `
            <div class="car-card" onclick='showCarDetails(${c.id})'>
                <div class="img-wrap"><img src="${c.image || '/images/car.jpg'}" alt="${c.model}"></div>
                <div class="card-body">
                    <h4>${c.model}</h4>
                    <p class="muted">${c.brand}</p>
                    <div class="row">
                        <strong>₹ ${c.price}</strong>
                        <span class="status ${availClass}">${c.available ? 'Available' : 'Sold'}</span>
                    </div>
                </div>
            </div>
        `;
    });
}

function filterCars(keyword){
    const k = (keyword||'').toLowerCase();
    filteredList = carList.filter(c => c.model.toLowerCase().includes(k) || c.brand.toLowerCase().includes(k));
    renderCars(filteredList);
}

function sortCars(option){
    if(option === 'price-asc') filteredList.sort((a,b)=>a.price-b.price);
    else if(option === 'price-desc') filteredList.sort((a,b)=>b.price-a.price);
    else filteredList.sort((a,b)=> b.id - a.id);
    renderCars(filteredList);
}

function showCarDetails(id){
    const car = carList.find(c=>c.id === id);
    if(!car) return;
    document.getElementById('modalImg').src = car.image || '/images/car.jpg';
    document.getElementById('modalModel').innerText = car.model;
    document.getElementById('modalBrand').innerText = car.brand;
    document.getElementById('modalPrice').innerText = 'Price: ₹ ' + car.price;
    document.getElementById('modalAvailability').innerText = car.available ? 'Available' : 'Sold';
    document.getElementById('modalDesc').innerText = car.description || '';

    const btn = document.getElementById('markSoldBtn');
    btn.style.display = car.available ? 'inline-block' : 'none';
    btn.onclick = () => { markCarSold(id); };

    document.getElementById('carModal').style.display = 'flex';
}

function hideModal(){ document.getElementById('carModal').style.display = 'none'; }

function markCarSold(id){
    if(!confirm('Mark this car as sold?')) return;
    fetch(`/api/dashboard/cars/${id}/sell`, { method: 'POST' })
        .then(r => r.json())
        .then(resp => {
            if(resp.ok){
                // update local state
                const car = carList.find(c=>c.id === id);
                if(car){ car.available = false; }
                renderCars(filteredList);
                hideModal();
                alert('Car marked as sold');
            }
        })
        .catch(err => { console.error(err); alert('Failed to mark sold'); });
}