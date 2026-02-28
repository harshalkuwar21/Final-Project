document.addEventListener("DOMContentLoaded", loadShowrooms);
let showroomList = [];

function loadShowrooms() {
    fetch("/api/dashboard/showrooms")
        .then(res => res.json())
        .then(data => {
            showroomList = data;
            renderShowrooms(data);
        });
}

function renderShowrooms(list) {

    const grid = document.getElementById("showroomGrid");
    grid.innerHTML = "";

    list.forEach(s => {

        const styleBadge =
            s.name.toLowerCase().includes("premium")
                ? "badge-premium"
                : "badge-normal";

        // use encodeURIComponent to avoid quote issues in names
        const encodedName = encodeURIComponent(s.name);

        grid.innerHTML += `
            <div class="showroom-card" onclick="openCars(${s.id}, '${encodedName}')">

                <div class="image-box">
                    <img src="${s.image}" alt="${s.name}">
                    <span class="badge ${styleBadge}">
                        ${styleBadge === 'badge-premium' ? 'Premium' : 'Normal'}
                    </span>
                </div>

                <div class="info">
                    <h4>${s.name}</h4>
                    <p>📍 ${s.city}</p>
                    <small onclick="event.stopPropagation(); loadCars(${s.id}, decodeURIComponent('${encodedName}'))" style="cursor:pointer;color:var(--accent)">View Cars →</small>
                </div>

            </div>
        `;
    });
}

function filterShowrooms(keyword) {
    const filtered = showroomList.filter(s =>
        s.name.toLowerCase().includes(keyword.toLowerCase()) ||
        s.city.toLowerCase().includes(keyword.toLowerCase())
    );
    renderShowrooms(filtered);
}

function openCars(id, encodedName){
    // navigate to dedicated showroom cars page
    const url = `/cars/showroom/${id}`;
    window.location.href = url;
}

// ================= SAVE SHOWROOM =================
function saveShowroom(){

    const name = document.getElementById("srName").value;
    const city = document.getElementById("srCity").value;
    const type = document.getElementById("srType").value;
    const image = document.getElementById("srImage").files[0];

    if(!name || !city || !image){
        alert("Please fill all fields");
        return;
    }

    const formData = new FormData();
    formData.append("name", name);
    formData.append("city", city);
    formData.append("type", type);
    formData.append("image", image);

    fetch("/api/dashboard/showroom", {
        method: "POST",
        body: formData
    })
    .then(res => {
        if(res.ok){
            alert("Showroom added successfully!");
            loadShowrooms();     // refresh showroom list
        }
    });
}


// ================= LOAD CARS =================
function loadCars(id, name) {

    document.getElementById("selectedTitle").innerText =
        "Cars in " + name;

    fetch(`/api/dashboard/showrooms/${id}/cars`)
        .then(res => res.json())
        .then(data => {

            const table = document.getElementById("carTable");
            table.innerHTML = "";

            data.forEach(car => {

                const status = car.available
                    ? `<span class="pending">Available</span>`
                    : `<span class="paid">Sold</span>`;

                table.innerHTML += `
                    <tr>
                        <td>${car.model}</td>
                        <td>${car.brand}</td>
                        <td>₹ ${car.price}</td>
                        <td>${status}</td>
                    </tr>
                `;
            });
        });
}
function previewImage(event){
    const img = document.getElementById("previewImg");
    img.src = URL.createObjectURL(event.target.files[0]);
}

