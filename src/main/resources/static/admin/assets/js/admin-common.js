(function () {
  function activeMenuByFile() {
    const path = window.location.pathname;
    const file = path.substring(path.lastIndexOf('/') + 1) || 'index.html';
    document.querySelectorAll('[data-menu]').forEach((a) => {
      if (a.getAttribute('data-menu') === file) {
        a.classList.add('active');
      }
    });
  }

  function scoreClass(score) {
    if (score == null) return 'score-pill score-mid';
    if (score >= 85) return 'score-pill score-high';
    if (score >= 70) return 'score-pill score-mid';
    return 'score-pill score-low';
  }

  function escapeHtml(s) {
    return String(s || '')
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;');
  }

  function statusBadge(status) {
    const map = {
      FINISHED: 'badge badge-success',
      IN_PROGRESS: 'badge badge-warning',
      DRAFT: 'badge badge-secondary',
      ABANDONED: 'badge badge-danger',
      DONE: 'badge badge-success',
      PROCESSING: 'badge badge-info',
      PENDING: 'badge badge-secondary',
      FAILED: 'badge badge-danger'
    };
    const cls = map[status] || 'badge badge-light';
    return `<span class="${cls}">${escapeHtml(status)}</span>`;
  }

  function ensureAdminBadge() {
    const rightNav = document.querySelector('.main-header .navbar-nav.ml-auto');
    if (!rightNav || rightNav.querySelector('.admin-user-badge')) return;

    const userId = window.AdminAPI ? window.AdminAPI.getUserId() : 1;
    const item = document.createElement('li');
    item.className = 'nav-item d-flex align-items-center mr-2';
    item.innerHTML = `<button class="admin-user-badge border-0" id="adminUserBtn" title="Click to switch X-User-Id"><i class="fas fa-user-shield mr-1"></i>User ${userId}</button>`;
    rightNav.prepend(item);

    const btn = document.getElementById('adminUserBtn');
    if (!btn || !window.AdminAPI) return;

    btn.addEventListener('click', () => {
      const raw = window.prompt('Set X-User-Id for admin page', String(window.AdminAPI.getUserId()));
      if (raw == null) return;
      try {
        window.AdminAPI.setUserId(raw);
        window.location.reload();
      } catch (err) {
        window.alert(err.message || String(err));
      }
    });
  }

  function handleLogout() {
    if (confirm('确定要退出登录吗？')) {
      localStorage.removeItem('adminToken');
      localStorage.removeItem('adminUserId');
      window.location.href = './index.html';
    }
  }

  document.addEventListener('DOMContentLoaded', () => {
    activeMenuByFile();
    ensureAdminBadge();
  });

  window.AdminUI = {
    scoreClass,
    escapeHtml,
    statusBadge
  };
})();

