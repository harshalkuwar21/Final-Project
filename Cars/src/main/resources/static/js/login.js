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
