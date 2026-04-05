document.addEventListener("DOMContentLoaded", () => {
    const msg = document.getElementById("msg");
    const loginForm = document.getElementById("loginForm");
    const forgotLink = document.getElementById("forgotLink");
    const forgotBox = document.getElementById("forgotBox");
    const sendOtpBtn = document.getElementById("sendOtpBtn");
    const resetPwdBtn = document.getElementById("resetPwdBtn");
    const params = new URLSearchParams(window.location.search);

    function setMessage(text, color) {
        msg.style.color = color;
        msg.innerText = text;
    }

    try {
        const storedLogoutMessage = sessionStorage.getItem("logoutMessage");
        if (storedLogoutMessage) {
            setMessage(storedLogoutMessage, "green");
            sessionStorage.removeItem("logoutMessage");
        } else if (params.get("logout") === "1") {
            setMessage("Logged out successfully.", "green");
        }
    } catch (_) {
        if (params.get("logout") === "1") {
            setMessage("Logged out successfully.", "green");
        }
    }

    loginForm?.addEventListener("submit", function(e) {
        e.preventDefault();

        const email = document.getElementById("email").value;
        const password = document.getElementById("password").value;

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
                setMessage(data.message, "green");

                setTimeout(() => {
                    window.location.href = data.redirectUrl || "/dashboard";
                }, 1000);

            } else {
                setMessage(data.message, "red");
            }
        })
        .catch(() => {
            setMessage("Server error. Please try again.", "red");
        });
    });

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
        setMessage(data.message || "Unable to send OTP", data.ok ? "green" : "red");
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
        setMessage(data.message || "Unable to reset password", data.ok ? "green" : "red");
    });
});
