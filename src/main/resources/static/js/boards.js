const API_BASE = '/api/boards';

function showMessage(message, type = 'success') {
    const messageEl = document.getElementById('message');
    messageEl.textContent = message;
    messageEl.classList.add('show', type);
    setTimeout(() => {
        messageEl.classList.remove('show', type);
    }, 3000);
}

async function loadBoards() {
    try {
        const boards = await makeRequest(API_BASE);
        const boardsList = document.getElementById('boards-list');
        
        if (boards.length === 0) {
            boardsList.innerHTML = '<p>No boards yet. Create your first board!</p>';
            return;
        }

        boardsList.innerHTML = boards.map(board => `
            <div class="board-card">
                <h3>${escapeHtml(board.title)}</h3>
                <p>${escapeHtml(board.description || '')}</p>
                <div class="board-meta">
                    <span>${board.columns ? board.columns.length : 0} columns</span>
                    <span>${new Date(board.createdAt).toLocaleDateString()}</span>
                </div>
                <div class="board-actions">
                    <button class="btn btn-primary btn-sm" onclick="openBoard(${board.id})">Open</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteBoard(${board.id})">Delete</button>
                </div>
            </div>
        `).join('');
    } catch (err) {
        showMessage(err.message, 'error');
    }
}

async function createBoard() {
    const title = document.getElementById('board-title').value.trim();
    const description = document.getElementById('board-description').value.trim();

    if (!title) {
        showMessage('Board title is required', 'error');
        return;
    }

    try {
        await makeRequest(API_BASE, 'POST', {
            title,
            description: description || null
        });
        showMessage('Board created successfully', 'success');
        document.getElementById('board-form').reset();
        document.getElementById('create-board-form').classList.add('hidden');
        loadBoards();
    } catch (err) {
        showMessage(err.message, 'error');
    }
}

function openBoard(boardId) {
    window.location.href = `/board.html?id=${boardId}`;
}

async function deleteBoard(boardId) {
    if (!confirm('Are you sure you want to delete this board? This cannot be undone.')) {
        return;
    }

    try {
        await makeRequest(`${API_BASE}/${boardId}`, 'DELETE');
        showMessage('Board deleted successfully', 'success');
        loadBoards();
    } catch (err) {
        showMessage(err.message, 'error');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    if (!getToken()) {
        window.location.href = '/login.html';
        return;
    }

    loadBoards();

    document.getElementById('create-board-btn').addEventListener('click', () => {
        document.getElementById('create-board-form').classList.toggle('hidden');
    });

    document.getElementById('cancel-create-btn').addEventListener('click', () => {
        document.getElementById('create-board-form').classList.add('hidden');
    });

    document.getElementById('board-form').addEventListener('submit', (e) => {
        e.preventDefault();
        createBoard();
    });

    document.getElementById('logout-btn').addEventListener('click', logout);
});
