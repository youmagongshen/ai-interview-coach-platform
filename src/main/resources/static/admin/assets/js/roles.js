(function () {
  let rows = [];

  const body = document.getElementById('roleTableBody');
  const refreshBtn = document.getElementById('refreshBtn');
  const saveRoleBtn = document.getElementById('saveRoleBtn');
  const inputs = ['w1', 'w2', 'w3', 'w4', 'w5'].map((id) => document.getElementById(id));

  function sumWeights() {
    return inputs.reduce((sum, el) => sum + Number(el.value || 0), 0);
  }

  function paintTotal() {
    const total = sumWeights();
    document.getElementById('wTotal').value = String(total);
    document.getElementById('weightWarn').style.display = total === 100 ? 'none' : 'block';
  }

  function toNum(v) {
    return Number(v || 0);
  }

  async function loadRoles() {
    try {
      rows = await AdminAPI.request('/admin/roles');
      render();
    } catch (err) {
      AdminAPI.showError(err);
    }
  }

  function render() {
    body.innerHTML = rows.map((r) => {
      const total = toNum(r.weightCorrectness) + toNum(r.weightDepth) + toNum(r.weightLogic) + toNum(r.weightMatch) + toNum(r.weightExpression);
      const totalCls = total === 100 ? 'badge badge-success' : 'badge badge-warning';
      return `
        <tr>
          <td>${r.id}</td>
          <td><span class="badge badge-soft">${AdminUI.escapeHtml(r.code)}</span></td>
          <td>${AdminUI.escapeHtml(r.name)}</td>
          <td>${AdminUI.escapeHtml(r.description || '-')}</td>
          <td>${toNum(r.weightCorrectness)}</td>
          <td>${toNum(r.weightDepth)}</td>
          <td>${toNum(r.weightLogic)}</td>
          <td>${toNum(r.weightMatch)}</td>
          <td>${toNum(r.weightExpression)}</td>
          <td><span class="${totalCls}">${total}</span></td>
          <td><button class="btn btn-xs btn-outline-primary" onclick="window.editRole(${r.id})">Edit</button></td>
        </tr>
      `;
    }).join('');
  }

  window.editRole = function (roleId) {
    const r = rows.find((x) => x.id === roleId);
    if (!r) return;
    document.getElementById('roleId').value = String(r.id);
    document.getElementById('roleCode').value = r.code;
    document.getElementById('roleName').value = r.name;
    document.getElementById('roleDesc').value = r.description || '';
    document.getElementById('w1').value = String(toNum(r.weightCorrectness));
    document.getElementById('w2').value = String(toNum(r.weightDepth));
    document.getElementById('w3').value = String(toNum(r.weightLogic));
    document.getElementById('w4').value = String(toNum(r.weightMatch));
    document.getElementById('w5').value = String(toNum(r.weightExpression));
    paintTotal();
    $('#roleModal').modal('show');
  };

  inputs.forEach((el) => el.addEventListener('input', paintTotal));

  saveRoleBtn.addEventListener('click', async () => {
    const id = Number(document.getElementById('roleId').value);
    const payload = {
      description: document.getElementById('roleDesc').value.trim(),
      weightCorrectness: Number(document.getElementById('w1').value),
      weightDepth: Number(document.getElementById('w2').value),
      weightLogic: Number(document.getElementById('w3').value),
      weightMatch: Number(document.getElementById('w4').value),
      weightExpression: Number(document.getElementById('w5').value)
    };

    try {
      const resp = await AdminAPI.request(`/admin/roles/${id}/weights`, {
        method: 'PUT',
        body: payload
      });
      if (!resp || !resp.updated) {
        window.alert('Save failed: weight total must be 100.');
        return;
      }
      $('#roleModal').modal('hide');
      await loadRoles();
    } catch (err) {
      AdminAPI.showError(err);
    }
  });

  refreshBtn.addEventListener('click', loadRoles);

  loadRoles();
})();
