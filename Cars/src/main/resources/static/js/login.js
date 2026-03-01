document.getElementById("loginForm").addEventListener("submit", function(e) {
        e.preventDefault();

        const email = document.getElementById("email").value;
        const password = document.getElementById("password").value;
        const msg = document.getElementById("msg");

        fetch("/user/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                email: email,
                password: password
            })
        })
        .then(res => res.json())
        .then(data => {
            if (data.status === "success") {
                msg.style.color = "green";
                msg.innerText = data.message;

                // redirect after success
                setTimeout(() => {
                    window.location.href = data.redirectUrl || "/dashboard";
                }, 1000);

            } else {
                msg.style.color = "red";
                msg.innerText = data.message;
            }
        })
        .catch(err => {
            msg.style.color = "red";
            msg.innerText = "Server error. Please try again.";
        });
    });

document.addEventListener("DOMContentLoaded", () => {
    const msg = document.getElementById("msg");
    const forgotLink = document.getElementById("forgotLink");
    const forgotBox = document.getElementById("forgotBox");
    const sendOtpBtn = document.getElementById("sendOtpBtn");
    const resetPwdBtn = document.getElementById("resetPwdBtn");

    forgotLink?.addEventListener("click", (e) => {
        e.preventDefault();
        forgotBox.style.display = forgotBox.style.display === "none" ? "block" : "none";
    });

    sendOtpBtn?.addEventListener("click", async () => {
        const email = document.getElementById("fpEmail").value.trim();
        if (!email) return;
        const res = await fetch("/user/forgot-password/send-otp", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email })
        });
        const data = await res.json();
        msg.style.color = data.ok ? "green" : "red";
        msg.innerText = data.message || "Unable to send OTP";
    });

    resetPwdBtn?.addEventListener("click", async () => {
        const email = document.getElementById("fpEmail").value.trim();
        const otp = document.getElementById("fpOtp").value.trim();
        const newPassword = document.getElementById("fpNewPassword").value;
        const res = await fetch("/user/forgot-password/verify-otp", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, otp, newPassword })
        });
        const data = await res.json();
        msg.style.color = data.ok ? "green" : "red";
        msg.innerText = data.message || "Unable to reset password";
    });
});
