document.addEventListener("DOMContentLoaded", () => {
    loadStats();
    loadSales();
    loadTopCars();
    loadInventory();
});


// ================== STATS ==================
function loadStats() {
    fetch("/api/dashboard/stats")
        .then(res => res.json())
        .then(data => {
            document.getElementById("totalCars").innerText = data.totalShowroom;
            document.getElementById("soldCars").innerText = data.soldCars;
            document.getElementById("availableCars").innerText = data.availableCars;
            document.getElementById("revenue").innerText = "₹ " + data.revenue;
        });
}

// ================== SALES TABLE ==================
function loadSales() {
    fetch("/api/dashboard/sales")
        .then(res => res.json())
        .then(data => {
            const table = document.getElementById("salesTable");
            table.innerHTML = "";

            data.forEach(row => {
                table.innerHTML += `
                    <tr>
                        <td>${row.model}</td>
                        <td>${row.buyer}</td>
                        <td>${row.date}</td>
                        <td>
                            <span class="${row.status === 'Paid' ? 'paid' : 'pending'}">
                                ${row.status}
                            </span>
                        </td>
                    </tr>
                `;
            });
        });
}

// ================== TOP CARS ==================
function loadTopCars() {
    fetch("/api/dashboard/top-cars")
        .then(res => res.json())
        .then(data => {
            const list = document.getElementById("topCars");
            list.innerHTML = "";

            data.forEach(car => {
                list.innerHTML += `<li>${car}</li>`;
            });
        });

}
function loadInventory() {
    fetch("/api/dashboard/inventory")
        .then(res => res.json())
        .then(data => {

            const barContainer = document.getElementById("inventoryBars");
            const labelContainer = document.getElementById("inventoryLabels");

            barContainer.innerHTML = "";
            labelContainer.innerHTML = "";

            const max = Math.max(...data.map(d => d.count));

            data.forEach(item => {
                const height = (item.count / max) * 100;

                barContainer.innerHTML += `
                    <div title="${item.brand} - ${item.count}"
                         style="height:${height}%"></div>
                `;

                labelContainer.innerHTML += `${item.brand} (${item.count}) &nbsp; `;
            });
        });
}
function loadTopCars() {
    fetch("/api/dashboard/top-selling")
        .then(res => res.json())
        .then(data => {

            const list = document.getElementById("topCars");
            list.innerHTML = "";

            data.forEach(car => {
                list.innerHTML += `
                    <li>${car.model} (${car.totalSold} sold)</li>
                `;
            });
        });
}
