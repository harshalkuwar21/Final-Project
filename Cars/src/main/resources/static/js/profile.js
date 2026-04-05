async function loadProfileToPage() {
    const r = await fetch("/user/profile");
    const msg = document.getElementById("pMsg");
    if (!r.ok) {
        msg.style.color = "red";
        msg.textContent = "Unable to load profile.";
        return;
    }
    const j = await r.json();
    document.getElementById("pFirstPage").value = j.first || "";
    document.getElementById("pLastPage").value = j.last || "";
    document.getElementById("pEmailPage").value = j.email || "";
    document.getElementById("pContactPage").value = String(j.contact || "").replace(/\D/g, "").slice(0, 10);
    if (j.profilePhotoUrl) {
        document.getElementById("photoPreview").src = j.profilePhotoUrl;
    }
}

async function saveProfileFromPage() {
    const contactInput = document.getElementById("pContactPage");
    contactInput.value = contactInput.value.replace(/\D/g, "").slice(0, 10);
    if (contactInput.value && !/^\d{10}$/.test(contactInput.value)) {
        const msg = document.getElementById("pMsg");
        msg.style.color = "red";
        msg.textContent = "Mobile number must be exactly 10 digits.";
        contactInput.focus();
        return;
    }
    const payload = {
        first: document.getElementById("pFirstPage").value.trim(),
        last: document.getElementById("pLastPage").value.trim(),
        email: document.getElementById("pEmailPage").value.trim(),
        contact: contactInput.value.trim()
    };
    const pwd = document.getElementById("pPasswordPage").value;
    if (pwd && pwd.trim().length > 0) payload.password = pwd;

    const btn = document.getElementById("pSavePageBtn");
    const msg = document.getElementById("pMsg");
    btn.disabled = true;
    const r = await fetch("/user/profile", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    });
    btn.disabled = false;
    const j = await r.json();
    if (r.ok && j.ok) {
        msg.style.color = "green";
        msg.textContent = "Profile updated.";
        if (j.user && j.user.profilePhotoUrl) {
            document.getElementById("photoPreview").src = j.user.profilePhotoUrl;
        }
        document.getElementById("pPasswordPage").value = "";
    } else {
        msg.style.color = "red";
        msg.textContent = j.error || "Unable to save profile.";
    }
}

async function uploadPhoto() {
    const fileInput = document.getElementById("photoInput");
    const msg = document.getElementById("pMsg");
    if (!fileInput.files || !fileInput.files[0]) {
        msg.style.color = "red";
        msg.textContent = "Please choose a photo file.";
        return;
    }
    const data = new FormData();
    data.append("photo", fileInput.files[0]);
    const res = await fetch("/user/profile/photo", {
        method: "POST",
        body: data
    });
    const out = await res.json();
    if (res.ok && out.ok) {
        document.getElementById("photoPreview").src = out.profilePhotoUrl;
        msg.style.color = "green";
        msg.textContent = "Photo uploaded.";
    } else {
        msg.style.color = "red";
        msg.textContent = out.error || "Photo upload failed.";
    }
}

window.addEventListener("load", () => {
    loadProfileToPage();
    document.getElementById("pContactPage").addEventListener("input", (e) => {
        e.target.value = e.target.value.replace(/\D/g, "").slice(0, 10);
    });
    document.getElementById("pSavePageBtn").addEventListener("click", saveProfileFromPage);
    document.getElementById("uploadPhotoBtn").addEventListener("click", uploadPhoto);
});

