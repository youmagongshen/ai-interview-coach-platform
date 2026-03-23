(function () {
  let roles = [];
  let docs = [];
  let currentDocId = null;

  function roleNameById(roleId) {
    const role = roles.find((r) => r.id === roleId);
    return role ? role.name : `Role-${roleId || '-'}`;
  }

  function fillRoles() {
    const all = ['<option value="">All</option>']
      .concat(roles.map((r) => `<option value="${r.id}">${AdminUI.escapeHtml(r.name)}</option>`));
    document.getElementById('fRole').innerHTML = all.join('');
    document.getElementById('dRole').innerHTML = roles
      .map((r) => `<option value="${r.id}">${AdminUI.escapeHtml(r.name)}</option>`)
      .join('');
  }

  function currentFilter() {
    return {
      roleId: document.getElementById('fRole').value,
      status: document.getElementById('fStatus').value,
      docType: document.getElementById('fDocType').value,
      keyword: document.getElementById('fKeyword').value.trim()
    };
  }

  async function loadRoles() {
    roles = await AdminAPI.request('/admin/roles');
    fillRoles();
  }

  async function loadDocs() {
    const f = currentFilter();
    const pageData = await AdminAPI.request('/knowledge/documents', {
      query: {
        roleId: f.roleId,
        status: f.status,
        keyword: f.keyword,
        page: 1,
        pageSize: 300
      }
    });

    const serverRows = (pageData && pageData.list) || [];
    docs = f.docType ? serverRows.filter((x) => x.docType === f.docType) : serverRows;
    renderDocs();
  }

  async function loadLogs() {
    const f = currentFilter();
    const pageData = await AdminAPI.request('/knowledge/retrieval-logs', {
      query: {
        roleId: f.roleId,
        page: 1,
        pageSize: 100
      }
    });
    const logs = (pageData && pageData.list) || [];
    renderLogs(logs);
  }

  function renderDocs() {
    document.getElementById('docBody').innerHTML = docs
      .slice()
      .sort((a, b) => (a.id < b.id ? 1 : -1))
      .map((d) => `
        <tr>
          <td>${d.id}</td>
          <td>${AdminUI.escapeHtml(roleNameById(d.roleId))}</td>
          <td style="max-width:260px;white-space:normal;">${AdminUI.escapeHtml(d.title)}</td>
          <td><span class="badge badge-info">${AdminUI.escapeHtml(d.docType)}</span></td>
          <td>${d.status === 'ACTIVE' ? '<span class="badge badge-success">ACTIVE</span>' : '<span class="badge badge-secondary">DISABLED</span>'}</td>
          <td>${d.version || 1}</td>
          <td>${AdminUI.escapeHtml(d.updatedAt || d.createdAt || '-')}</td>
          <td>
            <button class="btn btn-xs btn-outline-primary" onclick="window.editDoc(${d.id})">Edit</button>
            <button class="btn btn-xs btn-outline-info" onclick="window.openChunk(${d.id})">Chunks</button>
          </td>
        </tr>
      `)
      .join('');
  }

  function renderLogs(logs) {
    document.getElementById('logBody').innerHTML = logs
      .slice()
      .sort((a, b) => (a.id < b.id ? 1 : -1))
      .map((l) => `
        <tr>
          <td>${l.id}</td>
          <td>${l.sessionId || '-'}</td>
          <td>${AdminUI.escapeHtml(roleNameById(l.roleId))}</td>
          <td style="max-width:280px;white-space:normal;">${AdminUI.escapeHtml(l.queryText || '')}</td>
          <td>${l.topK || '-'}</td>
          <td>${l.latencyMs || '-'}</td>
          <td>${AdminUI.escapeHtml(l.createdAt || '-')}</td>
        </tr>
      `)
      .join('');
  }

  function resetDocForm() {
    document.getElementById('dId').value = '';
    document.getElementById('dType').value = 'MARKDOWN';
    document.getElementById('dStatus').value = 'ACTIVE';
    document.getElementById('dTitle').value = '';
    document.getElementById('dSourceName').value = '';
    document.getElementById('dSourceUrl').value = '';
    document.getElementById('dStoragePath').value = '';
    document.getElementById('dSummary').value = '';
  }

  window.editDoc = function (id) {
    const d = docs.find((x) => x.id === id);
    if (!d) return;
    document.getElementById('docModalTitle').textContent = 'Edit Document';
    document.getElementById('dId').value = String(d.id);
    document.getElementById('dRole').value = String(d.roleId);
    document.getElementById('dType').value = d.docType || 'MARKDOWN';
    document.getElementById('dStatus').value = d.status || 'ACTIVE';
    document.getElementById('dTitle').value = d.title || '';
    document.getElementById('dSourceName').value = d.sourceName || '';
    document.getElementById('dSourceUrl').value = d.sourceUrl || '';
    document.getElementById('dStoragePath').value = d.storagePath || '';
    document.getElementById('dSummary').value = d.summary || '';
    $('#docModal').modal('show');
  };

  async function loadChunks(docId) {
    const pageData = await AdminAPI.request(`/knowledge/documents/${docId}/chunks`, {
      query: { page: 1, pageSize: 200 }
    });
    const chunks = (pageData && pageData.list) || [];
    document.getElementById('chunkBody').innerHTML = chunks
      .slice()
      .sort((a, b) => (a.chunkIndex || 0) - (b.chunkIndex || 0))
      .map((c) => `
        <tr>
          <td>${c.chunkIndex || '-'}</td>
          <td style="white-space:normal;">${AdminUI.escapeHtml(c.chunkText || '')}</td>
          <td>${AdminUI.escapeHtml(c.keywords || '-')}</td>
          <td>${c.tokenCount || '-'}</td>
        </tr>
      `)
      .join('');
  }

  window.openChunk = async function (docId) {
    currentDocId = docId;
    const d = docs.find((x) => x.id === docId);
    document.getElementById('chunkTitle').textContent = `Chunks: ${d ? d.title : docId}`;
    document.getElementById('cText').value = '';
    document.getElementById('cKeywords').value = '';
    try {
      await loadChunks(docId);
      $('#chunkModal').modal('show');
    } catch (err) {
      AdminAPI.showError(err);
    }
  };

  document.getElementById('addDocBtn').addEventListener('click', () => {
    document.getElementById('docModalTitle').textContent = 'Create Document';
    resetDocForm();
    $('#docModal').modal('show');
  });

  document.getElementById('saveDocBtn').addEventListener('click', async () => {
    const id = Number(document.getElementById('dId').value || 0);
    const payload = {
      roleId: Number(document.getElementById('dRole').value),
      title: document.getElementById('dTitle').value.trim(),
      docType: document.getElementById('dType').value,
      sourceName: document.getElementById('dSourceName').value.trim(),
      sourceUrl: document.getElementById('dSourceUrl').value.trim(),
      storagePath: document.getElementById('dStoragePath').value.trim(),
      summary: document.getElementById('dSummary').value.trim(),
      status: document.getElementById('dStatus').value
    };

    if (!payload.title) {
      window.alert('Title is required');
      return;
    }

    try {
      if (id > 0) {
        await AdminAPI.request(`/knowledge/documents/${id}`, {
          method: 'PUT',
          body: {
            title: payload.title,
            sourceName: payload.sourceName,
            sourceUrl: payload.sourceUrl,
            storagePath: payload.storagePath,
            summary: payload.summary,
            status: payload.status
          }
        });
      } else {
        await AdminAPI.request('/knowledge/documents', {
          method: 'POST',
          body: payload
        });
      }
      $('#docModal').modal('hide');
      await Promise.all([loadDocs(), loadLogs()]);
    } catch (err) {
      AdminAPI.showError(err);
    }
  });

  document.getElementById('addChunkBtn').addEventListener('click', async () => {
    if (!currentDocId) return;
    const chunkText = document.getElementById('cText').value.trim();
    if (!chunkText) {
      window.alert('Chunk text is required');
      return;
    }
    try {
      await AdminAPI.request(`/knowledge/documents/${currentDocId}/chunks`, {
        method: 'POST',
        body: {
          chunkText,
          keywords: document.getElementById('cKeywords').value.trim(),
          embeddingModel: 'text-embedding-v1'
        }
      });
      document.getElementById('cText').value = '';
      document.getElementById('cKeywords').value = '';
      await loadChunks(currentDocId);
    } catch (err) {
      AdminAPI.showError(err);
    }
  });

  document.getElementById('searchBtn').addEventListener('click', async () => {
    try {
      await Promise.all([loadDocs(), loadLogs()]);
    } catch (err) {
      AdminAPI.showError(err);
    }
  });

  (async function init() {
    try {
      await loadRoles();
      await Promise.all([loadDocs(), loadLogs()]);
    } catch (err) {
      AdminAPI.showError(err);
    }
  })();
})();
