(function () {
  const API_BASE = '/inv';
  const STORE = {
    userId: 'aiInterviewUserId',
    accessToken: 'aiInterviewAccessToken',
    refreshToken: 'aiInterviewRefreshToken'
  };

  const tabLogin = document.getElementById('tabLogin');
  const tabRegister = document.getElementById('tabRegister');
  const loginForm = document.getElementById('loginForm');
  const registerForm = document.getElementById('registerForm');

  const loginHint = document.getElementById('loginHint');
  const regHint = document.getElementById('regHint');
  const loginToast = document.getElementById('loginToast');
  const registerToast = document.getElementById('registerToast');

  function switchTab(type) {
    const isLogin = type === 'login';
    tabLogin.classList.toggle('active', isLogin);
    tabRegister.classList.toggle('active', !isLogin);
    loginForm.classList.toggle('active', isLogin);
    registerForm.classList.toggle('active', !isLogin);
    loginHint.textContent = '';
    regHint.textContent = '';
    hideToast(loginToast);
    hideToast(registerToast);
  }

  function showToast(el, type, text) {
    el.className = `toast show ${type}`;
    el.textContent = text;
    window.setTimeout(() => hideToast(el), 2600);
  }

  function hideToast(el) {
    el.className = 'toast';
    el.textContent = '';
  }

  async function request(path, method, body) {
    const res = await fetch(API_BASE + path, {
      method,
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json'
      },
      body: JSON.stringify(body || {})
    });

    let payload = null;
    try {
      payload = await res.json();
    } catch (e) {
      throw new Error('服务端返回不是 JSON');
    }

    if (!res.ok) {
      throw new Error((payload && payload.message) || `HTTP ${res.status}`);
    }

    if (!payload || payload.code !== 0) {
      throw new Error((payload && payload.message) || '请求失败');
    }

    return payload.data;
  }

  function saveAuth(data) {
    const userId = data && data.userId ? data.userId : 1;
    localStorage.setItem(STORE.userId, String(userId));
    localStorage.setItem(STORE.accessToken, (data && data.accessToken) || '');
    localStorage.setItem(STORE.refreshToken, (data && data.refreshToken) || '');
  }

  function validateRegister(data) {
    if (!data.username || !data.phone || !data.email || !data.password || !data.password2) {
      return '请完整填写注册信息';
    }
    if (!/^1[3-9]\d{9}$/.test(data.phone)) {
      return '手机号格式不正确';
    }
    if (!/^\S+@\S+\.\S+$/.test(data.email)) {
      return '邮箱格式不正确';
    }
    if (data.password.length < 8) {
      return '密码至少8位';
    }
    if (data.password !== data.password2) {
      return '两次输入的密码不一致';
    }
    if (!data.agree) {
      return '请先勾选协议';
    }
    return '';
  }

  tabLogin.addEventListener('click', () => switchTab('login'));
  tabRegister.addEventListener('click', () => switchTab('register'));

  loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const username = document.getElementById('loginAccount').value.trim();
    const password = document.getElementById('loginPassword').value;
    const rememberDays = document.getElementById('rememberMe').checked ? 7 : 1;

    if (!username || !password) {
      loginHint.textContent = '请输入账号和密码';
      return;
    }

    loginHint.textContent = '';

    try {
      const data = await request('/auth/login', 'POST', {
        username,
        password,
        rememberDays,
        deviceLabel: 'Web-Login-Page'
      });

      saveAuth(data || {});
      showToast(loginToast, 'ok', '登录成功，正在跳转到主界面...');
      setTimeout(() => {
        window.location.href = './app.html';
      }, 700);
    } catch (err) {
      showToast(loginToast, 'error', err.message || '登录失败');
    }
  });

  registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const data = {
      username: document.getElementById('regUsername').value.trim(),
      phone: document.getElementById('regPhone').value.trim(),
      email: document.getElementById('regEmail').value.trim(),
      password: document.getElementById('regPwd').value,
      password2: document.getElementById('regPwd2').value,
      agree: document.getElementById('regAgree').checked
    };

    const msg = validateRegister(data);
    if (msg) {
      regHint.textContent = msg;
      return;
    }

    regHint.textContent = '';

    try {
      await request('/auth/register', 'POST', {
        username: data.username,
        password: data.password,
        phone: data.phone,
        email: data.email,
        termsAccepted: true,
        termsVersion: '2026.03'
      });

      showToast(registerToast, 'ok', '注册成功，请切换到登录');
      setTimeout(() => switchTab('login'), 800);
    } catch (err) {
      showToast(registerToast, 'error', err.message || '注册失败');
    }
  });
})();

