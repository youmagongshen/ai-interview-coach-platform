(function () {
  const BASE = '/inv';
  const STORE = {
    userId: 'aiInterviewUserId',
    accessToken: 'aiInterviewAccessToken',
    refreshToken: 'aiInterviewRefreshToken'
  };

  const state = {
    userId: 1,
    accessToken: '',
    refreshToken: '',
    roles: [],
    sessions: [],
    currentSessionId: null
  };

  function el(id) {
    return document.getElementById(id);
  }

  function parsePositiveInt(value, fallback) {
    const n = Number(value);
    if (!Number.isFinite(n) || n <= 0) return fallback;
    return Math.floor(n);
  }

  function escapeHtml(s) {
    return String(s || '')
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;');
  }

  function toPretty(data) {
    return JSON.stringify(data, null, 2);
  }

  function showJson(id, data) {
    el(id).textContent = toPretty(data);
  }

  function showStatus(text, type) {
    const cls = type === 'warn' ? 'warn' : type === 'ok' ? 'ok' : 'mid';
    el('globalStatus').innerHTML = `<span class="badge ${cls}">${escapeHtml(text)}</span>`;
  }

  function roleName(roleId) {
    const role = state.roles.find((r) => r.id === roleId);
    return role ? role.name : `Role-${roleId || '-'}`;
  }

  function persistAuth() {
    localStorage.setItem(STORE.userId, String(state.userId));
    localStorage.setItem(STORE.accessToken, state.accessToken || '');
    localStorage.setItem(STORE.refreshToken, state.refreshToken || '');
  }

  function loadPersistedAuth() {
    state.userId = parsePositiveInt(localStorage.getItem(STORE.userId), 1);
    state.accessToken = localStorage.getItem(STORE.accessToken) || '';
    state.refreshToken = localStorage.getItem(STORE.refreshToken) || '';
    el('cfgUserId').value = String(state.userId);
    el('tokenAccess').value = state.accessToken;
    el('tokenRefresh').value = state.refreshToken;
  }

  function syncUserIdFromInput() {
    state.userId = parsePositiveInt(el('cfgUserId').value, 1);
    el('cfgUserId').value = String(state.userId);
    persistAuth();
  }

  function buildUrl(path, query) {
    const url = new URL(BASE + path, window.location.origin);
    if (query) {
      Object.entries(query).forEach(([k, v]) => {
        if (v === undefined || v === null || v === '') return;
        url.searchParams.set(k, String(v));
      });
    }
    return url.toString();
  }

  async function request(path, options) {
    const opt = options || {};
    syncUserIdFromInput();

    const headers = {
      Accept: 'application/json',
      'X-User-Id': String(state.userId)
    };

    if (state.accessToken) {
      headers.Authorization = `Bearer ${state.accessToken}`;
    }

    let body;
    if (opt.formData) {
      body = opt.formData;
    } else if (opt.body !== undefined) {
      headers['Content-Type'] = 'application/json';
      body = JSON.stringify(opt.body);
    }

    const res = await fetch(buildUrl(path, opt.query), {
      method: opt.method || 'GET',
      headers,
      body
    });

    let payload;
    try {
      payload = await res.json();
    } catch (err) {
      throw new Error('Server response is not JSON');
    }

    if (!res.ok) {
      throw new Error((payload && payload.message) || `HTTP ${res.status}`);
    }

    if (!payload || payload.code !== 0) {
      throw new Error((payload && payload.message) || 'API error');
    }

    return payload.data;
  }

  async function safeRun(fn) {
    try {
      await fn();
      showStatus('Request success', 'ok');
    } catch (err) {
      showStatus(err.message || String(err), 'warn');
    }
  }

  function fillRoleSelectors() {
    const html = state.roles
      .map((r) => `<option value="${r.id}">${escapeHtml(r.name)} (${escapeHtml(r.code)})</option>`)
      .join('');

    ['qRoleId', 'createRoleId', 'progressRoleId', 'knowledgeRoleId', 'docRoleId'].forEach((id) => {
      const node = el(id);
      if (!node) return;
      const keep = node.value;
      const hasAll = id === 'qRoleId' || id === 'progressRoleId' || id === 'knowledgeRoleId';
      node.innerHTML = hasAll ? `<option value="">All</option>${html}` : html;
      if (keep) node.value = keep;
    });
  }

  async function loadRoles() {
    state.roles = (await request('/roles')) || [];
    fillRoleSelectors();
    showJson('rolesResult', state.roles);
  }

  async function loadQuestions() {
    const pageData = await request('/questions', {
      query: {
        roleId: el('qRoleId').value,
        questionType: el('qType').value,
        difficulty: el('qDifficulty').value,
        keyword: el('qKeyword').value.trim(),
        active: el('qActive').value,
        page: 1,
        pageSize: 50
      }
    });
    showJson('questionResult', pageData || {});
  }

  function renderSessions(list) {
    const body = el('sessionTableBody');
    body.innerHTML = (list || []).map((s) => `
      <tr>
        <td>${s.id}</td>
        <td>${escapeHtml(roleName(s.roleId))}</td>
        <td>${escapeHtml(s.status || '-')}</td>
        <td>${escapeHtml(s.interviewType || '-')}</td>
        <td>${s.currentRound || 0}/${s.totalRounds || 0}</td>
        <td>${s.overallScore == null ? '-' : s.overallScore}</td>
        <td>${escapeHtml(s.evaluationStatus || '-')}</td>
        <td>
          <button class="small" onclick="window.appPickSession(${s.id})">Select</button>
          <button class="small secondary" onclick="window.appLoadTurns(${s.id})">Turns</button>
          <button class="small secondary" onclick="window.appLoadReport(${s.id})">Report</button>
          <button class="small secondary" onclick="window.appLoadScores(${s.id})">Scores</button>
        </td>
      </tr>
    `).join('');
  }

  async function loadSessions() {
    const pageData = await request('/interview/sessions', {
      query: {
        status: el('sessionStatus').value,
        interviewType: el('sessionInterviewType').value,
        page: 1,
        pageSize: 100
      }
    });
    state.sessions = (pageData && pageData.list) || [];
    renderSessions(state.sessions);
    showJson('sessionResult', pageData || {});
  }

  async function createSession() {
    const req = {
      roleId: Number(el('createRoleId').value),
      difficulty: el('createDifficulty').value,
      totalRounds: parsePositiveInt(el('createRounds').value, 5),
      timeLimitSec: parsePositiveInt(el('createTime').value, 1500),
      interviewStage: 'TECH_FIRST',
      followupMode: 'AUTO',
      interviewType: el('createType').value,
      voiceEnabled: el('createType').value === 'VOICE',
      videoEnabled: el('createType').value === 'VIDEO'
    };

    const data = await request('/interview/sessions', { method: 'POST', body: req });
    showJson('sessionResult', data || {});

    if (data && data.sessionId) {
      pickSession(data.sessionId);
    }

    await loadSessions();
  }

  function pickSession(sessionId) {
    state.currentSessionId = Number(sessionId);
    el('currentSessionId').textContent = String(state.currentSessionId);
    el('answerSessionId').value = String(state.currentSessionId);
    el('finishSessionId').value = String(state.currentSessionId);
    el('attachmentSessionId').value = String(state.currentSessionId);
    el('taskSessionId').value = String(state.currentSessionId);
  }

  async function loadSessionDetail(sessionId) {
    const data = await request(`/interview/sessions/${sessionId}`);
    showJson('sessionDetailResult', data || {});
    return data;
  }

  function renderTurns(pageData) {
    const rows = (pageData && pageData.list) || [];
    el('turnTableBody').innerHTML = rows.map((t) => `
      <tr>
        <td>${t.id}</td>
        <td>${t.roundNo || '-'}</td>
        <td>${escapeHtml(t.turnType || '-')}</td>
        <td style="max-width:340px;">${escapeHtml(t.questionText || '-')}</td>
        <td style="max-width:240px;">${escapeHtml((t.answerText || t.asrText || '-'))}</td>
        <td><button class="small secondary" onclick="window.appUseTurn(${t.id})">Use</button></td>
      </tr>
    `).join('');
  }

  async function loadTurns(sessionId) {
    const data = await request(`/interview/sessions/${sessionId}/turns`, {
      query: { page: 1, pageSize: 100 }
    });
    renderTurns(data || {});
    showJson('turnResult', data || {});
    return data;
  }

  async function submitAnswer() {
    const sessionId = parsePositiveInt(el('answerSessionId').value, state.currentSessionId || 0);
    const turnId = parsePositiveInt(el('answerTurnId').value, 0);
    if (!sessionId || !turnId) {
      throw new Error('Session ID and Turn ID are required');
    }

    const req = {
      turnId,
      answerMode: el('answerMode').value,
      answerText: el('answerText').value,
      responseSec: parsePositiveInt(el('answerResponseSec').value, 60)
    };

    const data = await request(`/interview/sessions/${sessionId}/answers`, {
      method: 'POST',
      body: req
    });

    showJson('answerResult', data || {});
    if (data && data.nextTurn && data.nextTurn.id) {
      el('answerTurnId').value = String(data.nextTurn.id);
    }
    await loadTurns(sessionId);
  }

  async function finishSession() {
    const sessionId = parsePositiveInt(el('finishSessionId').value, state.currentSessionId || 0);
    if (!sessionId) throw new Error('Session ID is required');

    const req = {
      finishReason: 'MANUAL',
      clientElapsedSec: parsePositiveInt(el('finishElapsed').value, 600)
    };

    const data = await request(`/interview/sessions/${sessionId}/finish`, {
      method: 'POST',
      body: req
    });
    showJson('finishResult', data);
    await loadSessions();
  }

  async function loadEvalStatus(sessionId) {
    const data = await request(`/interview/sessions/${sessionId}/evaluation-status`);
    showJson('evaluationResult', data || {});
  }

  async function loadReport(sessionId) {
    const data = await request(`/interview/sessions/${sessionId}/report`);
    showJson('reportResult', data || {});
  }

  async function loadScores(sessionId) {
    const data = await request(`/interview/sessions/${sessionId}/scores`);
    showJson('scoreResult', data || []);
  }

  function renderTasks(pageData) {
    const rows = (pageData && pageData.list) || [];
    el('taskTableBody').innerHTML = rows.map((t) => `
      <tr>
        <td>${t.id}</td>
        <td>${escapeHtml(t.title || '')}</td>
        <td>${escapeHtml(t.taskType || '')}</td>
        <td>${escapeHtml(t.status || '')}</td>
        <td>${escapeHtml(t.dueDate || '-')}</td>
        <td>
          <button class="small secondary" onclick="window.appTaskStatus(${t.id}, 'DONE')">Done</button>
          <button class="small warn" onclick="window.appTaskDelete(${t.id})">Delete</button>
        </td>
      </tr>
    `).join('');
  }

  async function loadTasks() {
    const data = await request('/tasks', {
      query: {
        status: el('taskFilterStatus').value,
        page: 1,
        pageSize: 100
      }
    });
    renderTasks(data || {});
    showJson('taskResult', data || {});
  }

  async function createTask() {
    const req = {
      sessionId: el('taskSessionId').value ? Number(el('taskSessionId').value) : null,
      title: el('taskTitle').value.trim(),
      taskType: el('taskType').value,
      content: el('taskContent').value.trim(),
      dueDate: el('taskDueDate').value || null
    };
    if (!req.title || !req.content) throw new Error('Task title/content required');

    const data = await request('/tasks', { method: 'POST', body: req });
    showJson('taskResult', data || {});
    await loadTasks();
  }

  async function updateTaskStatus(taskId, status) {
    const data = await request(`/tasks/${taskId}/status`, {
      method: 'PATCH',
      body: { status }
    });
    showJson('taskResult', data || {});
    await loadTasks();
  }

  async function deleteTask(taskId) {
    const data = await request(`/tasks/${taskId}`, { method: 'DELETE' });
    showJson('taskResult', data || {});
    await loadTasks();
  }

  async function loadProgress() {
    const roleId = el('progressRoleId').value;
    const [trend, weak] = await Promise.all([
      request('/progress/trend', { query: { roleId, limit: 20 } }),
      request('/progress/latest-weak-points', { query: { roleId } })
    ]);
    showJson('progressTrendResult', trend || []);
    showJson('progressWeakResult', weak || []);
  }

  async function loadKnowledgeDocs() {
    const data = await request('/knowledge/documents', {
      query: {
        roleId: el('knowledgeRoleId').value,
        status: el('knowledgeStatus').value,
        keyword: el('knowledgeKeyword').value.trim(),
        page: 1,
        pageSize: 100
      }
    });
    showJson('knowledgeDocResult', data || {});

    const list = (data && data.list) || [];
    el('knowledgeDocTable').innerHTML = list.map((d) => `
      <tr>
        <td>${d.id}</td>
        <td>${escapeHtml(roleName(d.roleId))}</td>
        <td>${escapeHtml(d.title || '')}</td>
        <td>${escapeHtml(d.docType || '')}</td>
        <td>${escapeHtml(d.status || '')}</td>
        <td><button class="small secondary" onclick="window.appUseDoc(${d.id})">Use</button></td>
      </tr>
    `).join('');
  }

  async function createKnowledgeDoc() {
    const req = {
      roleId: Number(el('docRoleId').value),
      title: el('docTitle').value.trim(),
      docType: el('docType').value,
      sourceName: el('docSourceName').value.trim(),
      sourceUrl: el('docSourceUrl').value.trim(),
      storagePath: el('docStoragePath').value.trim(),
      summary: el('docSummary').value.trim()
    };
    if (!req.title) throw new Error('Document title required');

    const data = await request('/knowledge/documents', {
      method: 'POST',
      body: req
    });
    showJson('knowledgeDocResult', data || {});
    if (data && data.id) {
      el('chunkDocId').value = String(data.id);
    }
    await loadKnowledgeDocs();
  }

  async function loadDocChunks() {
    const docId = parsePositiveInt(el('chunkDocId').value, 0);
    if (!docId) throw new Error('Doc ID required');
    const data = await request(`/knowledge/documents/${docId}/chunks`, {
      query: { page: 1, pageSize: 200 }
    });
    showJson('knowledgeChunkResult', data || {});
  }

  async function addDocChunk() {
    const docId = parsePositiveInt(el('chunkDocId').value, 0);
    if (!docId) throw new Error('Doc ID required');
    const req = {
      chunkText: el('chunkText').value.trim(),
      keywords: el('chunkKeywords').value.trim(),
      embeddingModel: 'text-embedding-v1'
    };
    if (!req.chunkText) throw new Error('Chunk text required');
    const data = await request(`/knowledge/documents/${docId}/chunks`, {
      method: 'POST',
      body: req
    });
    showJson('knowledgeChunkResult', data || {});
    await loadDocChunks();
  }

  async function loadRetrievalLogs() {
    const data = await request('/knowledge/retrieval-logs', {
      query: {
        sessionId: el('logSessionId').value,
        roleId: el('knowledgeRoleId').value,
        page: 1,
        pageSize: 100
      }
    });
    showJson('knowledgeLogResult', data || {});
  }

  async function uploadAttachment() {
    const sessionId = parsePositiveInt(el('attachmentSessionId').value, 0);
    if (!sessionId) throw new Error('Session ID required');
    const file = el('attachmentFile').files[0];
    if (!file) throw new Error('Please select file');

    const form = new FormData();
    form.append('file', file);
    form.append('sessionId', String(sessionId));
    if (el('attachmentTurnId').value) form.append('turnId', el('attachmentTurnId').value);
    form.append('fileType', el('attachmentType').value);

    const data = await request('/attachments/upload', {
      method: 'POST',
      formData: form
    });
    showJson('attachmentResult', data || {});
  }

  async function listAttachments() {
    const sessionId = parsePositiveInt(el('attachmentSessionId').value, 0);
    if (!sessionId) throw new Error('Session ID required');
    const data = await request('/attachments', {
      query: {
        sessionId,
        fileType: el('attachmentType').value
      }
    });
    showJson('attachmentResult', data || []);
  }

  async function asrRecognize() {
    const file = el('asrFile').files[0];
    if (!file) throw new Error('Please select audio file');

    const form = new FormData();
    form.append('file', file);

    const data = await request('/speech/asr', {
      method: 'POST',
      formData: form
    });
    showJson('asrResult', data || {});
  }

  async function loadMe() {
    const data = await request('/users/me');
    showJson('profileResult', data || {});
    if (data && data.id) {
      state.userId = data.id;
      el('cfgUserId').value = String(state.userId);
      persistAuth();
    }
  }

  async function updateMe() {
    const req = {
      phone: el('profilePhone').value.trim(),
      email: el('profileEmail').value.trim()
    };
    const data = await request('/users/me', {
      method: 'PUT',
      body: req
    });
    showJson('profileResult', data || {});
  }

  async function register() {
    const req = {
      username: el('regUsername').value.trim(),
      password: el('regPassword').value,
      phone: el('regPhone').value.trim(),
      email: el('regEmail').value.trim(),
      termsAccepted: true,
      termsVersion: '2026.03'
    };
    const data = await request('/auth/register', { method: 'POST', body: req });
    showJson('authResult', data || {});
  }

  async function login() {
    const req = {
      username: el('loginUsername').value.trim(),
      password: el('loginPassword').value,
      rememberDays: parsePositiveInt(el('loginRememberDays').value, 7),
      deviceLabel: 'StaticWeb'
    };
    const data = await request('/auth/login', { method: 'POST', body: req });
    showJson('authResult', data || {});

    state.accessToken = (data && data.accessToken) || '';
    state.refreshToken = (data && data.refreshToken) || '';
    if (data && data.userId) state.userId = data.userId;

    el('cfgUserId').value = String(state.userId);
    el('tokenAccess').value = state.accessToken;
    el('tokenRefresh').value = state.refreshToken;
    persistAuth();
  }

  async function refreshToken() {
    const refreshToken = el('tokenRefresh').value.trim() || state.refreshToken;
    const data = await request('/auth/refresh', {
      method: 'POST',
      body: { refreshToken }
    });
    showJson('authResult', data || {});

    state.accessToken = (data && data.accessToken) || '';
    state.refreshToken = (data && data.refreshToken) || '';
    el('tokenAccess').value = state.accessToken;
    el('tokenRefresh').value = state.refreshToken;
    persistAuth();
  }

  async function logout() {
    const refreshToken = el('tokenRefresh').value.trim() || state.refreshToken;
    const data = await request('/auth/logout', {
      method: 'POST',
      body: { refreshToken }
    });
    showJson('authResult', data || {});

    state.accessToken = '';
    state.refreshToken = '';
    el('tokenAccess').value = '';
    el('tokenRefresh').value = '';
    persistAuth();
  }

  function bindEvents() {
    el('saveUserIdBtn').addEventListener('click', () => {
      syncUserIdFromInput();
      showStatus(`X-User-Id set to ${state.userId}`, 'mid');
    });

    el('btnRegister').addEventListener('click', () => safeRun(register));
    el('btnLogin').addEventListener('click', () => safeRun(login));
    el('btnRefreshToken').addEventListener('click', () => safeRun(refreshToken));
    el('btnLogout').addEventListener('click', () => safeRun(logout));

    el('btnLoadMe').addEventListener('click', () => safeRun(loadMe));
    el('btnUpdateMe').addEventListener('click', () => safeRun(updateMe));

    el('btnLoadRoles').addEventListener('click', () => safeRun(loadRoles));
    el('btnLoadQuestions').addEventListener('click', () => safeRun(loadQuestions));

    el('btnCreateSession').addEventListener('click', () => safeRun(createSession));
    el('btnLoadSessions').addEventListener('click', () => safeRun(loadSessions));
    el('btnLoadSessionDetail').addEventListener('click', () => safeRun(async () => {
      const sid = parsePositiveInt(el('answerSessionId').value, state.currentSessionId || 0);
      if (!sid) throw new Error('Session ID required');
      await loadSessionDetail(sid);
    }));
    el('btnSubmitAnswer').addEventListener('click', () => safeRun(submitAnswer));
    el('btnFinishSession').addEventListener('click', () => safeRun(finishSession));
    el('btnEvalStatus').addEventListener('click', () => safeRun(async () => {
      const sid = parsePositiveInt(el('finishSessionId').value, state.currentSessionId || 0);
      if (!sid) throw new Error('Session ID required');
      await loadEvalStatus(sid);
    }));

    el('btnCreateTask').addEventListener('click', () => safeRun(createTask));
    el('btnLoadTasks').addEventListener('click', () => safeRun(loadTasks));

    el('btnLoadProgress').addEventListener('click', () => safeRun(loadProgress));

    el('btnLoadDocs').addEventListener('click', () => safeRun(loadKnowledgeDocs));
    el('btnCreateDoc').addEventListener('click', () => safeRun(createKnowledgeDoc));
    el('btnLoadChunks').addEventListener('click', () => safeRun(loadDocChunks));
    el('btnAddChunk').addEventListener('click', () => safeRun(addDocChunk));
    el('btnLoadLogs').addEventListener('click', () => safeRun(loadRetrievalLogs));

    el('btnUploadAttachment').addEventListener('click', () => safeRun(uploadAttachment));
    el('btnListAttachment').addEventListener('click', () => safeRun(listAttachments));
    el('btnAsr').addEventListener('click', () => safeRun(asrRecognize));
  }

  window.appPickSession = function (sessionId) {
    pickSession(sessionId);
    showStatus(`Current session: ${sessionId}`, 'mid');
  };

  window.appLoadTurns = function (sessionId) {
    safeRun(async () => {
      pickSession(sessionId);
      await loadTurns(sessionId);
    });
  };

  window.appLoadReport = function (sessionId) {
    safeRun(async () => {
      pickSession(sessionId);
      await loadReport(sessionId);
    });
  };

  window.appLoadScores = function (sessionId) {
    safeRun(async () => {
      pickSession(sessionId);
      await loadScores(sessionId);
    });
  };

  window.appUseTurn = function (turnId) {
    el('answerTurnId').value = String(turnId);
    showStatus(`Turn ${turnId} selected`, 'mid');
  };

  window.appTaskStatus = function (taskId, status) {
    safeRun(() => updateTaskStatus(taskId, status));
  };

  window.appTaskDelete = function (taskId) {
    safeRun(() => deleteTask(taskId));
  };

  window.appUseDoc = function (docId) {
    el('chunkDocId').value = String(docId);
    showStatus(`Doc ${docId} selected`, 'mid');
  };

  async function init() {
    loadPersistedAuth();
    bindEvents();
    await safeRun(async () => {
      await loadRoles();
      await loadSessions();
      await loadTasks();
    });
  }

  init();
})();

