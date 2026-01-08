document.addEventListener("DOMContentLoaded", loadShowrooms);

// ================= LOAD SHOWROOMS =================
function loadShowrooms() {
    fetch("/api/dashboard/showrooms")
        .then(res => res.json())
        .then(data => {
            const grid = document.getElementById("showroomGrid");
            grid.innerHTML = "";

            data.forEach(s => {
                grid.innerHTML += `
                    <div class="showroom-card" onclick="loadCars(${s.id}, '${s.name}')">
                        <img src="${s.image}">
                        <div class="info">
                            <h4>${s.name}</h4>
                            <p>${s.city}</p>
                        </div>
                    </div>
                `;
            });
        });
}

// ================= SAVE SHOWROOM =================
function saveShowroom() {

    const form = new FormData();
    form.append("name", document.getElementById("name").value);
    form.append("city", document.getElementById("city").value);
    form.append("image", document.getElementById("image").files[0]);

    fetch("/api/dashboard/showroom", {
        method: "POST",
        body: form
    })
    .then(() => {
        alert("Showroom added successfully!");
        loadShowrooms();
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

                const status = car.sold
                    ? `<span class="paid">Sold</span>`
                    : `<span class="pending">Available</span>`;

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
