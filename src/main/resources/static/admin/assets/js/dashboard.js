(function () {
  const roleMap = new Map();
  let sessions = [];
  let questions = [];

  async function loadData() {
    try {
      const [roles, qPage, sPage, trend] = await Promise.all([
        AdminAPI.request('/admin/roles'),
        AdminAPI.request('/admin/questions', { query: { page: 1, pageSize: 500 } }),
        AdminAPI.request('/interview/sessions', { query: { page: 1, pageSize: 200 } }),
        AdminAPI.request('/progress/trend', { query: { limit: 20 } })
      ]);

      roles.forEach((r) => roleMap.set(r.id, r.name));
      sessions = (sPage && sPage.list) || [];
      questions = (qPage && qPage.list) || [];

      renderKpi(roles, qPage, sessions, trend || []);
      renderOverview(sessions, questions);
      renderTable(sessions);
      renderTrendChart(trend || []);
    } catch (err) {
      AdminAPI.showError(err);
    }
  }

  function avgScoreFromSessions(list) {
    const valid = list.filter((x) => x && x.overallScore != null);
    if (!valid.length) return 0;
    const sum = valid.reduce((s, x) => s + Number(x.overallScore), 0);
    return Math.round((sum / valid.length) * 10) / 10;
  }

  function renderKpi(roles, qPage, sessionRows, trendRows) {
    const avgBySession = avgScoreFromSessions(sessionRows);
    const avgByTrend = trendRows.length
      ? trendRows.reduce((s, x) => s + Number(x.overallScore || 0), 0) / trendRows.length
      : 0;
    const avg = avgBySession > 0 ? avgBySession : Math.round(avgByTrend * 10) / 10;

    document.getElementById('kpiRoles').textContent = String((roles || []).length);
    document.getElementById('kpiQuestions').textContent = String((qPage && qPage.total) || 0);
    document.getElementById('kpiSessions').textContent = String(sessionRows.length);
    document.getElementById('kpiAvgScore').textContent = String(avg || 0);
  }

  function renderOverview(sessionRows, questionRows) {
    const inProgress = sessionRows.filter((s) => s.status === 'IN_PROGRESS').length;
    const evalDone = sessionRows.filter((s) => s.evaluationStatus === 'DONE').length;
    const activeQuestions = questionRows.filter((q) => q.active).length;
    const activeRatio = questionRows.length ? Math.round((activeQuestions / questionRows.length) * 100) : 0;

    document.getElementById('overviewBox').innerHTML = `
      <div class="summary-kpi mb-2"><h5>In Progress Sessions</h5><strong>${inProgress}</strong></div>
      <div class="summary-kpi mb-2"><h5>Evaluation Done Rate</h5><strong>${sessionRows.length ? Math.round((evalDone / sessionRows.length) * 100) : 0}%</strong></div>
      <div class="summary-kpi"><h5>Active Question Rate</h5><strong>${activeRatio}%</strong></div>
    `;
  }

  function renderTable(list) {
    const body = document.getElementById('sessionTableBody');
    body.innerHTML = list
      .slice()
      .sort((a, b) => (a.id < b.id ? 1 : -1))
      .map((s) => {
        const roleName = roleMap.get(s.roleId) || `Role-${s.roleId || '-'}`;
        const scoreText = s.overallScore == null ? '-' : s.overallScore;
        const scoreCls = AdminUI.scoreClass(s.overallScore);
        return `
          <tr>
            <td>${s.id}</td>
            <td>${AdminAPI.getUserId()}</td>
            <td>${AdminUI.escapeHtml(roleName)}</td>
            <td>${AdminUI.statusBadge(s.status)}</td>
            <td>${AdminUI.statusBadge(s.evaluationStatus)}</td>
            <td><span class="${scoreCls}">${scoreText}</span></td>
            <td>${AdminUI.escapeHtml(s.createdAt || '-')}</td>
          </tr>
        `;
      })
      .join('');
  }

  function renderTrendChart(trendRows) {
    const canvas = document.getElementById('scoreTrend');
    if (!canvas || typeof Chart === 'undefined') return;

    const sorted = trendRows
      .slice()
      .sort((a, b) => (a.endedAt || '') > (b.endedAt || '') ? 1 : -1);
    const labels = sorted.map((x) => (x.endedAt || '').slice(5, 10));
    const values = sorted.map((x) => Number(x.overallScore || 0));

    new Chart(canvas, {
      type: 'line',
      data: {
        labels,
        datasets: [{
          label: 'Score',
          data: values,
          borderColor: '#0f7196',
          backgroundColor: 'rgba(15,113,150,0.12)',
          fill: true,
          tension: 0.35,
          pointRadius: 3
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: { y: { min: 0, max: 100 } },
        plugins: { legend: { display: false } }
      }
    });
  }

  loadData();
})();
