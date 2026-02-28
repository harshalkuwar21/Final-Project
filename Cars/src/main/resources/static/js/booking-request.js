document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("bookingRequestForm");
    const statusMessage = document.getElementById("statusMessage");

    form.addEventListener("submit", (event) => {
        event.preventDefault();
        statusMessage.textContent = "";
        statusMessage.className = "status";

        const formData = new FormData(form);
        const payload = Object.fromEntries(formData.entries());

        fetch("/booking-request/api", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    statusMessage.textContent = `Booking submitted successfully. Your booking ID is #${data.bookingId}.`;
                    statusMessage.classList.add("success");
                    form.reset();
                } else {
                    statusMessage.textContent = data.error || "Unable to submit booking.";
                    statusMessage.classList.add("error");
                }
            })
            .catch(() => {
                statusMessage.textContent = "Network error. Please try again.";
                statusMessage.classList.add("error");
            });
    });
});
