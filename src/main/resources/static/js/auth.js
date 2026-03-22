const API_BASE = '/api/auth';

async function handleAuth(path, body) {
    const response = await fetch(`${API_BASE}/${path}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(body)
    });

    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
        const baseMessage = data.message || 'Request failed';
        if (data.validationErrors && typeof data.validationErrors === 'object') {
            const details = Object.entries(data.validationErrors)
                .map(([field, msg]) => `${field}: ${msg}`)
                .join('; ');
            throw new Error(`${baseMessage}: ${details}`);
        }
        throw new Error(baseMessage);
    }

    if (data.token) {
        localStorage.setItem('token', data.token);
    }

    return data;
}

document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const messageEl = document.getElementById('auth-message');
    messageEl.textContent = '';

    try {
        await handleAuth('login', {
            username: document.getElementById('login-username').value,
            password: document.getElementById('login-password').value
        });
        window.location.href = '/index.html';
    } catch (err) {
        messageEl.textContent = err.message;
        messageEl.classList.add('error');
    }
});

document.getElementById('register-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const messageEl = document.getElementById('auth-message');
    messageEl.textContent = '';

    try {
        await handleAuth('register', {
            username: document.getElementById('register-username').value,
            email: document.getElementById('register-email').value,
            password: document.getElementById('register-password').value
        });
        messageEl.textContent = 'Registration successful. You can now log in.';
        messageEl.classList.remove('error');
    } catch (err) {
        messageEl.textContent = err.message;
        messageEl.classList.add('error');
    }
});

