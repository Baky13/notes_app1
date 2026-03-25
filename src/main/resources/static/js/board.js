let currentBoardId = null;
let columnsCache = [];

function getBoardIdFromUrl() {
    const params = new URLSearchParams(window.location.search);
    return params.get('id');
}

function showMessage(message, type = 'success') {
    const messageEl = document.getElementById('message');
    messageEl.textContent = message;
    messageEl.classList.add('show', type);
    setTimeout(() => {
        messageEl.classList.remove('show', type);
    }, 3000);
}

async function loadBoard() {
    currentBoardId = getBoardIdFromUrl();
    if (!currentBoardId) {
        window.location.href = '/boards.html';
        return;
    }

    try {
        const board = await makeRequest(`/api/boards/${currentBoardId}`);
        
        document.getElementById('board-title').textContent = escapeHtml(board.title);
        document.getElementById('board-description').textContent = escapeHtml(board.description || '');
        document.getElementById('edit-board-title').value = board.title;
        document.getElementById('edit-board-description').value = board.description || '';

        renderColumns(board.columns || []);
    } catch (err) {
        showMessage(err.message, 'error');
        setTimeout(() => {
            window.location.href = '/boards.html';
        }, 2000);
    }
}

function renderColumns(columns) {
    columnsCache = columns.sort((a, b) => a.position - b.position);
    const columnsContainer = document.getElementById('board-columns');
    columnsContainer.innerHTML = columnsCache
        .map((column, index) => `
            <div class="kanban-column" data-column-id="${column.id}">
                <div class="column-header">
                    <div>${escapeHtml(column.title)}</div>
                </div>
                <div class="column-content" data-column-id="${column.id}">
                    ${(column.tasks || [])
                        .sort((a, b) => a.position - b.position)
                        .map(task => renderTask(task, column.id, index))
                        .join('')}
                </div>
                <button class="btn btn-primary add-task-btn" onclick="openCreateTaskModal(${column.id})">+ Add Task</button>
            </div>
        `).join('');
}

function renderTask(task, columnId, columnIndex) {
    const priorityClass = task.priority ? ` task-priority ${task.priority}` : '';
    const isFirstColumn = columnIndex === 0;
    const isLastColumn = columnIndex === columnsCache.length - 1;
    
    return `
        <div class="task-card" data-task-id="${task.id}">
            <h4>${escapeHtml(task.title)}</h4>
            <p>${escapeHtml(task.description || '')}</p>
            ${task.priority ? `<div class="task-priority${priorityClass}">${escapeHtml(task.priority)}</div>` : ''}
            <div class="task-actions">
                <button class="btn btn-sm" onclick="openEditTaskModal(${currentBoardId}, ${columnId}, ${task.id})">Edit</button>
                <button class="btn btn-danger btn-sm" onclick="deleteTask(${currentBoardId}, ${columnId}, ${task.id})">Delete</button>
                ${!isFirstColumn ? `<button class="btn btn-sm" onclick="moveTaskToAdjacentColumn(${columnId}, ${task.id}, ${columnsCache[columnIndex - 1].id}, ${task.position})">←</button>` : ''}
                ${!isLastColumn ? `<button class="btn btn-sm" onclick="moveTaskToAdjacentColumn(${columnId}, ${task.id}, ${columnsCache[columnIndex + 1].id}, 0)">→</button>` : ''}
            </div>
        </div>
    `;
}

async function editBoard() {
    const title = document.getElementById('edit-board-title').value.trim();
    const description = document.getElementById('edit-board-description').value.trim();

    if (!title) {
        showMessage('Board title is required', 'error');
        return;
    }

    try {
        await makeRequest(`/api/boards/${currentBoardId}`, 'PUT', {
            title,
            description: description || null
        });
        showMessage('Board updated successfully', 'success');
        document.getElementById('edit-board-form').classList.add('hidden');
        loadBoard();
    } catch (err) {
        showMessage(err.message, 'error');
    }
}

async function deleteBoard() {
    if (!confirm('Are you sure you want to delete this board? This will delete all columns and tasks. This cannot be undone.')) {
        return;
    }

    try {
        await makeRequest(`/api/boards/${currentBoardId}`, 'DELETE');
        showMessage('Board deleted successfully', 'success');
        setTimeout(() => {
            window.location.href = '/boards.html';
        }, 1000);
    } catch (err) {
        showMessage(err.message, 'error');
    }
}

function openCreateTaskModal(columnId) {
    showTaskModal(columnId, null);
}

function openEditTaskModal(boardId, columnId, taskId) {
    showTaskModal(columnId, taskId);
}

function showTaskModal(columnId, taskId) {
    // Simple modal implementation using prompt for MVP
    // In production, use a proper modal component
    const title = prompt('Task title:');
    if (title === null) return;

    if (!title.trim()) {
        showMessage('Task title is required', 'error');
        return;
    }

    const description = prompt('Task description (optional):');
    const priorityStr = prompt('Priority (LOW, MEDIUM, HIGH - optional):');

    const taskData = {
        title: title.trim(),
        description: description ? description.trim() : null,
        priority: priorityStr ? priorityStr.toUpperCase() : null
    };

    if (taskId) {
        updateTask(columnId, taskId, taskData);
    } else {
        createTask(columnId, taskData);
    }
}

async function createTask(columnId, taskData) {
    try {
        await makeRequest(`/api/boards/${currentBoardId}/columns/${columnId}/tasks`, 'POST', taskData);
        showMessage('Task created successfully', 'success');
        loadBoard();
    } catch (err) {
        showMessage(err.message, 'error');
    }
}

async function updateTask(columnId, taskId, taskData) {
    try {
        await makeRequest(
            `/api/boards/${currentBoardId}/columns/${columnId}/tasks/${taskId}`,
            'PUT',
            taskData
        );
        showMessage('Task updated successfully', 'success');
        loadBoard();
    } catch (err) {
        showMessage(err.message, 'error');
    }
}

async function deleteTask(boardId, columnId, taskId) {
    if (!confirm('Are you sure you want to delete this task?')) {
        return;
    }

    try {
        await makeRequest(
            `/api/boards/${boardId}/columns/${columnId}/tasks/${taskId}`,
            'DELETE'
        );
        showMessage('Task deleted successfully', 'success');
        loadBoard();
    } catch (err) {
        showMessage(err.message, 'error');
    }
}

async function moveTaskToAdjacentColumn(fromColumnId, taskId, toColumnId, position) {
    try {
        await makeRequest(
            `/api/boards/${currentBoardId}/columns/${fromColumnId}/tasks/${taskId}/move`,
            'PATCH',
            { columnId: toColumnId, position: position }
        );
        showMessage('Task moved successfully', 'success');
        loadBoard();
    } catch (err) {
        showMessage(err.message, 'error');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    if (!getToken()) {
        window.location.href = '/login.html';
        return;
    }

    loadBoard();

    document.getElementById('edit-board-btn').addEventListener('click', () => {
        document.getElementById('edit-board-form').classList.toggle('hidden');
    });

    document.getElementById('cancel-edit-btn').addEventListener('click', () => {
        document.getElementById('edit-board-form').classList.add('hidden');
    });

    document.getElementById('board-edit-form').addEventListener('submit', (e) => {
        e.preventDefault();
        editBoard();
    });

    document.getElementById('delete-board-btn').addEventListener('click', deleteBoard);

    document.getElementById('logout-btn').addEventListener('click', logout);
});
