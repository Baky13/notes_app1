const NOTES_API = '/api/notes';
let currentPage = 0;
let currentQuery = '';

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function getAuthHeaders() {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = '/login.html';
        return {};
    }
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
}

async function fetchNotes(page = 0, query = '') {
    const headers = getAuthHeaders();
    const params = new URLSearchParams({ page, size: 10 });
    let url;
    
    if (query) {
        params.append('query', query);
        url = `${NOTES_API}/search?${params.toString()}`;
    } else {
        url = `${NOTES_API}?${params.toString()}`;
    }
    
    const res = await fetch(url, { headers });
    
    if (res.status === 401 || res.status === 403) {
        localStorage.removeItem('token');
        window.location.href = '/login.html';
        throw new Error('Unauthorized');
    }
    
    if (!res.ok) {
        throw new Error('Failed to load notes');
    }
    
    return res.json();
}

function renderNotes(pageData) {
    const listEl = document.getElementById('notes-list');
    listEl.innerHTML = '';

    if (!pageData.content || pageData.content.length === 0) {
        listEl.textContent = 'No notes yet.';
        return;
    }

    pageData.content.forEach(note => {
        const item = document.createElement('div');
        item.className = 'note-item';
        item.innerHTML = `
            <h3>${escapeHtml(note.title)}</h3>
            <p>${escapeHtml(note.content)}</p>
            <div class="note-actions">
                <button data-id="${note.id}" class="edit-btn">Edit</button>
                <button data-id="${note.id}" class="delete-btn">Delete</button>
            </div>
        `;
        listEl.appendChild(item);
    });

    document.getElementById('page-info').textContent = `Page ${pageData.number + 1} of ${pageData.totalPages || 1}`;

    document.querySelectorAll('.edit-btn').forEach(btn => {
        btn.addEventListener('click', () => loadNoteForEdit(btn.dataset.id));
    });

    document.querySelectorAll('.delete-btn').forEach(btn => {
        btn.addEventListener('click', () => deleteNote(btn.dataset.id));
    });
}

async function loadNoteForEdit(id) {
    const res = await fetch(`${NOTES_API}/${id}`, {
        headers: getAuthHeaders()
    });
    if (!res.ok) {
        return;
    }
    const note = await res.json();
    document.getElementById('note-id').value = note.id;
    document.getElementById('note-title').value = note.title;
    document.getElementById('note-content').value = note.content;
}

async function saveNote(e) {
    e.preventDefault();
    const id = document.getElementById('note-id').value;
    const payload = {
        title: document.getElementById('note-title').value,
        content: document.getElementById('note-content').value
    };

    const headers = getAuthHeaders();
    let url = NOTES_API;
    let method = 'POST';
    if (id) {
        url = `${NOTES_API}/${id}`;
        method = 'PUT';
    }

    const res = await fetch(url, {
        method,
        headers,
        body: JSON.stringify(payload)
    });

    if (!res.ok) {
        const msgEl = document.getElementById('notes-message');
        msgEl.textContent = 'Error saving note';
        msgEl.classList.add('error');
        return;
    }

    document.getElementById('note-form').reset();
    document.getElementById('note-id').value = '';
    loadPage();
}

async function deleteNote(id) {
    const res = await fetch(`${NOTES_API}/${id}`, {
        method: 'DELETE',
        headers: getAuthHeaders()
    });
    if (res.ok) {
        loadPage();
    }
}

async function loadPage() {
    try {
        const data = await fetchNotes(currentPage, currentQuery);
        renderNotes(data);
    } catch (e) {
        console.error('Failed to load notes:', e);
        const msgEl = document.getElementById('notes-message');
        if (msgEl) {
            msgEl.textContent = 'Failed to load notes';
            msgEl.classList.add('error');
        }
    }
}

document.getElementById('note-form').addEventListener('submit', saveNote);
document.getElementById('cancel-edit').addEventListener('click', () => {
    document.getElementById('note-form').reset();
    document.getElementById('note-id').value = '';
});

document.getElementById('search-button').addEventListener('click', () => {
    currentQuery = document.getElementById('search-input').value.trim();
    currentPage = 0;
    loadPage();
});

document.getElementById('prev-page').addEventListener('click', () => {
    if (currentPage > 0) {
        currentPage--;
        loadPage();
    }
});

document.getElementById('next-page').addEventListener('click', () => {
    currentPage++;
    loadPage();
});

document.getElementById('logout-button').addEventListener('click', () => {
    localStorage.removeItem('token');
    window.location.href = '/login.html';
});

window.addEventListener('load', () => {
    if (!localStorage.getItem('token')) {
        window.location.href = '/login.html';
        return;
    }
    loadPage();
});
