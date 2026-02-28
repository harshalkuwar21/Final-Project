document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("carAddForm");
    const status = document.getElementById("carAddStatus");
    const btn = document.getElementById("carAddBtn");

    if (!form) return;

    form.addEventListener("submit", (e) => {
        e.preventDefault();
        status.textContent = "";
        btn.disabled = true;

        const formData = new FormData(form);

        fetch("/api/dashboard/cars/add", {
            method: "POST",
            body: formData
        })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                status.style.color = "#15803d";
                status.textContent = data.message || "Vehicle added successfully!";
                if (data.redirectUrl) {
                    window.location.href = data.redirectUrl;
                }
            } else {
                status.style.color = "#b91c1c";
                status.textContent = data.message || "Failed to add vehicle.";
            }
        })
        .catch(() => {
            status.style.color = "#b91c1c";
            status.textContent = "Network error. Please try again.";
        })
        .finally(() => {
            btn.disabled = false;
        });
    });
});
