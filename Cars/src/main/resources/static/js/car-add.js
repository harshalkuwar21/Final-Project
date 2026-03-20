document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("carAddForm");
    const status = document.getElementById("carAddStatus");
    const btn = document.getElementById("carAddBtn");
    const priceDisplay = document.getElementById("priceDisplay");
    const priceUnit = document.getElementById("priceUnit");
    const priceValue = document.getElementById("priceValue");
    const pricePreview = document.getElementById("pricePreview");

    if (!form) return;

    function formatStoredRupees(value) {
        return Math.round(value).toLocaleString("en-IN");
    }

    function formatPriceLabel(value, unit) {
        if (!Number.isFinite(value) || value <= 0) return "Price on request";
        if (unit === "crore") return `Rs ${value.toFixed(2)} Crore`;
        if (unit === "lakh") return `Rs ${value.toFixed(2)} Lakh`;
        const amount = value;
        if (amount >= 10000000) return `Rs ${(amount / 10000000).toFixed(2)} Crore`;
        if (amount >= 100000) return `Rs ${(amount / 100000).toFixed(2)} Lakh`;
        return `Rs ${amount.toLocaleString("en-IN")}`;
    }

    function buildPriceValue() {
        if (!priceDisplay || !priceUnit || !priceValue || !pricePreview) return true;

        const entered = Number(priceDisplay.value);
        if (!Number.isFinite(entered) || entered <= 0) {
            priceValue.value = "";
            pricePreview.textContent = "Enter valid price. DB madhe rupees value store hoil.";
            return false;
        }

        let storedValue = entered;
        if (priceUnit.value === "lakh") storedValue = entered * 100000;
        if (priceUnit.value === "crore") storedValue = entered * 10000000;

        priceValue.value = String(Math.round(storedValue));
        pricePreview.textContent = `Display: ${formatPriceLabel(entered, priceUnit.value)} | DB Store: Rs ${formatStoredRupees(storedValue)}`;
        return true;
    }

    if (priceDisplay && priceUnit) {
        priceDisplay.addEventListener("input", buildPriceValue);
        priceUnit.addEventListener("change", buildPriceValue);
        buildPriceValue();
    }

    form.addEventListener("submit", (e) => {
        e.preventDefault();
        status.textContent = "";

        if (!buildPriceValue()) {
            status.style.color = "#b91c1c";
            status.textContent = "Please enter a valid vehicle price.";
            return;
        }

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
