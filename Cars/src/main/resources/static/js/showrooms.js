document.addEventListener("DOMContentLoaded", () => {
    const redirectAfterLogout = (message) => {
        try {
            sessionStorage.setItem("logoutMessage", message || "Logged out successfully.");
        } catch (_) {
            // ignore storage errors
        }
        window.location.href = "/login?logout=1";
    };

    loadViewerContext().finally(loadShowrooms);

    const logoutBtn = document.getElementById("logoutBtn");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", async () => {
            if (!confirm("Are you sure you want to logout?")) return;
            try {
                await fetch("/user/logout", { method: "POST" });
            } catch (_) {
                // Ignore logout request failures and continue to login page.
            }
            redirectAfterLogout("Logged out successfully.");
        });
    }
});

let showroomList = [];
let viewerContext = {
    role: "",
    showroomId: null
};

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

function loadViewerContext() {
    return fetch("/api/dashboard/me")
        .then(res => {
            if (!res.ok) throw new Error("Unable to load viewer context");
            return res.json();
        })
        .then(data => {
            if (data && data.ok) {
                viewerContext = {
                    role: data.role || "",
                    showroomId: data.showroomId ?? null
                };
            }
        })
        .catch(err => {
            console.warn("Viewer context unavailable", err);
        });
}

function canManageShowroom(showroom) {
    if (!showroom) return false;
    if (viewerContext.role === "ROLE_ADMIN") return true;
    return viewerContext.showroomId != null && Number(viewerContext.showroomId) === Number(showroom.id);
}

function editShowroom(id) {
    const showroom = showroomList.find(item => Number(item.id) === Number(id));
    if (!showroom) {
        alert("Showroom not found.");
        return;
    }
    window.location.href = `/showrooms/add?id=${id}`;
}

function deleteShowroom(id) {
    const showroom = showroomList.find(item => Number(item.id) === Number(id));
    if (!showroom) {
        alert("Showroom not found.");
        return;
    }
    if (!confirm(`Delete showroom "${showroom.name}"?`)) {
        return;
    }

    fetch(`/api/dashboard/showroom/${id}`, {
        method: "DELETE"
    })
        .then(res => res.json())
        .then(data => {
            if (!data.ok) {
                throw new Error(data.message || "Unable to delete showroom.");
            }
            alert(data.message || "Showroom deleted successfully.");
            loadShowrooms();
        })
        .catch(err => {
            alert(err.message || "Unable to delete showroom.");
        });
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
                <div class="showroom-actions" onclick="event.stopPropagation()">
                    <button type="button" class="showroom-action-btn view" onclick="openCars(${showroom.id})">
                        <i class="fas fa-eye"></i>
                        <span>View Showroom</span>
                    </button>
                    <a class="showroom-action-btn call" href="${escapeHtml(phoneLink(showroom.contactNumber))}">
                        <i class="fas fa-phone"></i>
                        <span>Call</span>
                    </a>
                    ${canManageShowroom(showroom) ? `
                        <button type="button" class="showroom-action-btn edit" onclick="editShowroom(${showroom.id})">
                            <i class="fas fa-pen"></i>
                            <span>Edit</span>
                        </button>
                        <button type="button" class="showroom-action-btn delete" onclick="deleteShowroom(${showroom.id})">
                            <i class="fas fa-trash"></i>
                            <span>Delete</span>
                        </button>
                    ` : `
                        <span class="showroom-action-note">View only</span>
                    `}
                </div>
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
