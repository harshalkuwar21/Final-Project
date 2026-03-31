document.addEventListener("DOMContentLoaded", () => {
    loadShowrooms();

    const logoutBtn = document.getElementById("logoutBtn");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", async () => {
            if (!confirm("Are you sure you want to logout?")) return;
            try {
                await fetch("/user/logout", { method: "POST" });
            } catch (_) {
                // Ignore logout request failures and continue to login page.
            }
            window.location.href = "/login";
        });
    }
});

let showroomList = [];

function escapeHtml(value) {
    return String(value ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function showroomImage(showroom) {
    return showroom.imageUrl || showroom.image || "/images/background.jpg";
}

function showroomType(showroom) {
    return (showroom.type || "Normal").trim() || "Normal";
}

function badgeClass(showroom) {
    return showroomType(showroom).toLowerCase().includes("premium")
        ? "badge-premium"
        : "badge-normal";
}

function phoneLink(contactNumber) {
    const cleaned = String(contactNumber || "").replace(/[^\d+]/g, "");
    return cleaned ? `tel:${cleaned}` : "#";
}

function loadShowrooms() {
    fetch("/api/dashboard/showrooms")
        .then(res => {
            if (!res.ok) {
                throw new Error("Unable to load showrooms");
            }
            return res.json();
        })
        .then(data => {
            showroomList = Array.isArray(data) ? data : [];
            renderShowrooms(showroomList);
        })
        .catch(err => {
            console.error(err);
            const grid = document.getElementById("showroomGrid");
            if (grid) {
                grid.innerHTML = `<div class="showroom-card"><div class="info"><h4>Unable to load showrooms</h4><p>${escapeHtml(err.message || "Please try again.")}</p></div></div>`;
            }
        });
}

function renderShowrooms(list) {
    const grid = document.getElementById("showroomGrid");
    if (!grid) return;

    if (!Array.isArray(list) || !list.length) {
        grid.innerHTML = `
            <div class="showroom-card">
                <div class="info">
                    <h4>No showrooms found</h4>
                    <p>Add a showroom from the admin page to see it here.</p>
                </div>
            </div>
        `;
        return;
    }

    grid.innerHTML = list.map(showroom => `
        <div class="showroom-card" onclick="openCars(${showroom.id})">
            <div class="image-box">
                <img src="${escapeHtml(showroomImage(showroom))}" alt="${escapeHtml(showroom.name || "Showroom")}">
                <span class="badge ${badgeClass(showroom)}">${escapeHtml(showroomType(showroom))}</span>
            </div>
            <div class="info">
                <h4>${escapeHtml(showroom.name || "-")}</h4>
                <p><strong>City:</strong> ${escapeHtml(showroom.city || "-")}</p>
                <p><strong>Address:</strong> ${escapeHtml(showroom.address || "-")}</p>
                <p><strong>Contact:</strong> ${escapeHtml(showroom.contactNumber || "-")}</p>
                <p><strong>Working:</strong> ${escapeHtml(showroom.workingHours || "-")}</p>
                <small onclick="event.stopPropagation(); window.location.href='${escapeHtml(phoneLink(showroom.contactNumber))}';">Call</small>
            </div>
        </div>
    `).join("");
}

function filterShowrooms(keyword) {
    const query = String(keyword || "").trim().toLowerCase();
    if (!query) {
        renderShowrooms(showroomList);
        return;
    }

    const filtered = showroomList.filter(showroom =>
        String(showroom.name || "").toLowerCase().includes(query) ||
        String(showroom.city || "").toLowerCase().includes(query) ||
        String(showroom.address || "").toLowerCase().includes(query) ||
        String(showroom.type || "").toLowerCase().includes(query)
    );
    renderShowrooms(filtered);
}

function openCars(id) {
    window.location.href = `/cars/showroom/${id}`;
}
