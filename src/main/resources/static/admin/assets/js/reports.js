(function () {
  let roles = [];
  let rows = [];
  const roleMap = new Map();

  function fillRoleFilter() {
    document.getElementById('fRole').innerHTML = ['<option value="">All</option>']
      .concat(roles.map((r) => `<option value="${r.id}">${AdminUI.escapeHtml(r.name)}</option>`))
      .join('');
  }

  function currentFilter() {
    return {
      roleId: document.getElementById('fRole').value,
      status: document.getElementById('fStatus').value,
      evalStatus: document.getElementById('fEval').value,
      keyword: document.getElementById('fKeyword').value.trim().toLowerCase()
    };
  }

  function applyFilter(list, f) {
    return list.filter((s) => {
      if (f.roleId && String(s.roleId) !== String(f.roleId)) return false;
      if (f.evalStatus && s.evaluationStatus !== f.evalStatus) return false;
      if (f.keyword) {
        const roleName = roleMap.get(s.roleId) || '';
        const t = `${s.title || ''} ${roleName}`.toLowerCase();
        if (!t.includes(f.keyword)) return false;
      }
      return true;
    });
  }

  async function loadAll() {
    const f = currentFilter();
    const [roleRows, pageData] = await Promise.all([
      AdminAPI.request('/admin/roles'),
      AdminAPI.request('/interview/sessions', {
        query: {
          page: 1,
          pageSize: 300,
          status: f.status
        }
      })
    ]);

    roles = roleRows || [];
    roleMap.clear();
    roles.forEach((r) => roleMap.set(r.id, r.name));

    rows = (pageData && pageData.list) || [];
    fillRoleFilter();
    render();
  }

  function render() {
    const filtered = applyFilter(rows, currentFilter());
    const body = document.getElementById('sessionBody');
    body.innerHTML = filtered
      .slice()
      .sort((a, b) => (a.id < b.id ? 1 : -1))
      .map((s) => {
        const scoreText = s.overallScore == null ? '-' : s.overallScore;
        const scoreCls = AdminUI.scoreClass(s.overallScore);
        const roleName = roleMap.get(s.roleId) || `Role-${s.roleId || '-'}`;
        return `
          <tr>
            <td>${s.id}</td>
            <td>${AdminAPI.getUserId()}</td>
            <td>${AdminUI.escapeHtml(roleName)}</td>
            <td>${AdminUI.statusBadge(s.status)}</td>
            <td>${AdminUI.statusBadge(s.evaluationStatus)}</td>
            <td><span class="${scoreCls}">${scoreText}</span></td>
            <td>${AdminUI.escapeHtml(s.interviewType || '-')}</td>
            <td>${AdminUI.escapeHtml(s.createdAt || '-')}</td>
            <td><button class="btn btn-xs btn-outline-primary" onclick="window.viewReport(${s.id})">View Report</button></td>
          </tr>
        `;
      })
      .join('');
  }

  function renderList(items) {
    if (!items || !items.length) return '<div class="text-muted">-</div>';
    return `<ul class="mb-2">${items.map((x) => `<li>${AdminUI.escapeHtml(x)}</li>`).join('')}</ul>`;
  }

  function renderDimension(d) {
    if (!d) return '<div class="text-muted">No dimension score.</div>';
    const fields = [
      ['correctness', d.correctness],
      ['depth', d.depth],
      ['logic', d.logic],
      ['match', d.match],
      ['expression', d.expression]
    ];
    return fields.map(([k, v]) => `
      <div class="mb-2">
        <div class="d-flex justify-content-between"><span>${k}</span><strong>${v == null ? '-' : v}</strong></div>
        <div class="progress progress-xs"><div class="progress-bar bg-info" style="width:${v == null ? 0 : Number(v)}%"></div></div>
      </div>
    `).join('');
  }

  window.viewReport = async function (sessionId) {
    try {
      const [report, scores] = await Promise.all([
        AdminAPI.request(`/interview/sessions/${sessionId}/report`),
        AdminAPI.request(`/interview/sessions/${sessionId}/scores`)
      ]);

      const latestScore = scores && scores.length ? scores[scores.length - 1] : null;

      document.getElementById('reportModalBody').innerHTML = `
        <div class="mb-2"><strong>Session:</strong> ${sessionId}</div>
        <div class="mb-2"><strong>Report Score:</strong> ${report && report.overallScore != null ? report.overallScore : '-'}</div>
        <div class="mb-2"><strong>Latest Round Score:</strong> ${latestScore && latestScore.totalScore != null ? latestScore.totalScore : '-'}</div>
        <div class="mb-3"><strong>Summary:</strong> ${AdminUI.escapeHtml((report && report.summary) || '-')}</div>

        <div class="mb-2"><strong>Dimension</strong></div>
        ${renderDimension(report && report.dimensionScore)}

        <div class="mt-3 mb-2"><strong>Highlights</strong></div>
        ${renderList(report && report.highlightPoints)}

        <div class="mt-3 mb-2"><strong>Improvements</strong></div>
        ${renderList(report && report.improvementPoints)}

        <div class="mt-3 mb-2"><strong>Suggestions</strong></div>
        ${renderList(report && report.suggestions)}
      `;

      $('#reportModal').modal('show');
    } catch (err) {
      AdminAPI.showError(err);
    }
  };

  document.getElementById('searchBtn').addEventListener('click', () => {
    loadAll().catch(AdminAPI.showError);
  });

  loadAll().catch(AdminAPI.showError);
})();
