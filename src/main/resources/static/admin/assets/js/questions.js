(function () {
  let roles = [];
  let rows = [];

  const body = document.getElementById('questionBody');

  function roleName(roleId) {
    const role = roles.find((r) => r.id === roleId);
    return role ? role.name : `Role-${roleId || '-'}`;
  }

  function fillRoleOptions() {
    const filterOptions = ['<option value="">All</option>']
      .concat(roles.map((r) => `<option value="${r.id}">${AdminUI.escapeHtml(r.name)}</option>`));
    document.getElementById('fRole').innerHTML = filterOptions.join('');

    document.getElementById('qRole').innerHTML = roles
      .map((r) => `<option value="${r.id}">${AdminUI.escapeHtml(r.name)}</option>`)
      .join('');
  }

  function currentFilter() {
    const activeRaw = document.getElementById('fActive').value;
    return {
      roleId: document.getElementById('fRole').value,
      questionType: document.getElementById('fType').value,
      difficulty: document.getElementById('fDifficulty').value,
      active: activeRaw === '' ? '' : activeRaw === '1',
      keyword: document.getElementById('fKeyword').value.trim()
    };
  }

  async function loadRoles() {
    roles = await AdminAPI.request('/admin/roles');
    fillRoleOptions();
  }

  async function loadQuestions() {
    const f = currentFilter();
    const pageData = await AdminAPI.request('/admin/questions', {
      query: {
        roleId: f.roleId,
        questionType: f.questionType,
        difficulty: f.difficulty,
        active: f.active,
        keyword: f.keyword,
        page: 1,
        pageSize: 300
      }
    });
    rows = (pageData && pageData.list) || [];
    render();
  }

  function render() {
    body.innerHTML = rows
      .slice()
      .sort((a, b) => (a.id < b.id ? 1 : -1))
      .map((q) => `
        <tr>
          <td>${q.id}</td>
          <td>${AdminUI.escapeHtml(roleName(q.roleId))}</td>
          <td><span class="badge badge-info">${AdminUI.escapeHtml(q.questionType)}</span></td>
          <td><span class="badge badge-secondary">${AdminUI.escapeHtml(q.difficulty)}</span></td>
          <td style="max-width:360px;white-space:normal;line-height:1.5;">${AdminUI.escapeHtml(q.questionText)}</td>
          <td style="max-width:220px;white-space:normal;">${AdminUI.escapeHtml(q.keywords || '-')}</td>
          <td>${q.active ? '<span class="badge badge-success">ACTIVE</span>' : '<span class="badge badge-secondary">DISABLED</span>'}</td>
          <td>${AdminUI.escapeHtml(q.createdAt || '-')}</td>
          <td>
            <button class="btn btn-xs btn-outline-primary" onclick="window.editQuestion(${q.id})">Edit</button>
            <button class="btn btn-xs btn-outline-${q.active ? 'warning' : 'success'}" onclick="window.toggleQuestion(${q.id}, ${q.active})">${q.active ? 'Disable' : 'Enable'}</button>
          </td>
        </tr>
      `)
      .join('');
  }

  function clearForm() {
    document.getElementById('qId').value = '';
    document.getElementById('qType').value = 'TECH';
    document.getElementById('qDifficulty').value = 'MIDDLE';
    document.getElementById('qText').value = '';
    document.getElementById('qKeywords').value = '';
  }

  window.editQuestion = function (id) {
    const q = rows.find((x) => x.id === id);
    if (!q) return;
    document.getElementById('qModalTitle').textContent = 'Edit Question';
    document.getElementById('qId').value = String(q.id);
    document.getElementById('qRole').value = String(q.roleId);
    document.getElementById('qType').value = q.questionType;
    document.getElementById('qDifficulty').value = q.difficulty;
    document.getElementById('qText').value = q.questionText || '';
    document.getElementById('qKeywords').value = q.keywords || '';
    $('#questionModal').modal('show');
  };

  window.toggleQuestion = async function (id, active) {
    try {
      await AdminAPI.request(`/admin/questions/${id}/active`, {
        method: 'PATCH',
        body: { active: !active }
      });
      await loadQuestions();
    } catch (err) {
      AdminAPI.showError(err);
    }
  };

  document.getElementById('addBtn').addEventListener('click', () => {
    document.getElementById('qModalTitle').textContent = 'Create Question';
    clearForm();
    $('#questionModal').modal('show');
  });

  document.getElementById('saveQuestionBtn').addEventListener('click', async () => {
    const id = Number(document.getElementById('qId').value || 0);
    const payload = {
      roleId: Number(document.getElementById('qRole').value),
      questionType: document.getElementById('qType').value,
      difficulty: document.getElementById('qDifficulty').value,
      questionText: document.getElementById('qText').value.trim(),
      keywords: document.getElementById('qKeywords').value.trim(),
      expectedPoints: ''
    };

    if (!payload.questionText) {
      window.alert('Question text is required');
      return;
    }

    try {
      if (id > 0) {
        const old = rows.find((x) => x.id === id);
        await AdminAPI.request(`/admin/questions/${id}`, {
          method: 'PUT',
          body: {
            ...payload,
            active: old ? !!old.active : true
          }
        });
      } else {
        await AdminAPI.request('/admin/questions', {
          method: 'POST',
          body: payload
        });
      }
      $('#questionModal').modal('hide');
      await loadQuestions();
    } catch (err) {
      AdminAPI.showError(err);
    }
  });

  document.getElementById('searchBtn').addEventListener('click', () => {
    loadQuestions().catch(AdminAPI.showError);
  });

  (async function init() {
    try {
      await loadRoles();
      await loadQuestions();
    } catch (err) {
      AdminAPI.showError(err);
    }
  })();
})();
