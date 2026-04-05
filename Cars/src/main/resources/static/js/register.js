document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('register-form');
  const msg = document.getElementById('form-message');
  const btn = document.getElementById('register-btn');
  const contactInput = form ? form.querySelector('input[name="contact"]') : null;

  if (!form) return;

  if (contactInput) {
    contactInput.addEventListener('input', () => {
      contactInput.value = contactInput.value.replace(/\D/g, '').slice(0, 10);
    });
  }

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearMessage();
    btn.disabled = true;
    showMessage('Registering...', 'info');
    const originalBtnText = btn.textContent;
    btn.textContent = 'Registering...';

    const data = {
      first: form.querySelector('input[name="firstName"]').value.trim(),
      last: form.querySelector('input[name="lastName"]').value.trim(),
      email: form.querySelector('input[name="email"]').value.trim(),
      contact: form.querySelector('input[name="contact"]').value.trim(),
      password: form.querySelector('input[name="password"]').value
    }; 

    // Basic client-side validation
    if (!data.first || !data.last || !data.email || !data.password) {
      showMessage('Please fill in all required fields.', 'error');
      btn.disabled = false;
      return;
    }
    data.contact = data.contact.replace(/\D/g, '').slice(0, 10);
    if (!/^\d{10}$/.test(data.contact)) {
      showMessage('Mobile number must be exactly 10 digits.', 'error');
      btn.disabled = false;
      btn.textContent = originalBtnText || 'Register';
      if (contactInput) contactInput.focus();
      return;
    }

    try {
      const res = await fetch('/user/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
      });

      const result = await res.json().catch(() => ({}));

      if (res.ok && result.success) {
        showMessage(result.message || 'Registered successfully. Redirecting...', 'success');
        // Clear sensitive field
        form.querySelector('input[name="password"]').value = '';
        setTimeout(() => window.location.href = '/login', 1400);
      } else {
        showMessage(result.message || 'Registration failed. Please try again.', 'error');
      }

    } catch (err) {
      showMessage('Network error. Please try again later.', 'error');
    } finally {
      btn.disabled = false;
      btn.textContent = originalBtnText || 'Register';
    } 
  });

  function showMessage(text, type) {
    msg.textContent = text;
    msg.className = 'form-message ' + (type === 'success' ? 'success' : (type === 'error' ? 'error' : 'info'));
  } 

  function clearMessage() {
    msg.textContent = '';
    msg.className = 'form-message';
  }
});
