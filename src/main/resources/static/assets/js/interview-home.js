(function () {
  const API_BASE = '';
  const STORE = {
    userId: 'aiInterviewUserId',
    accessToken: 'aiInterviewAccessToken',
    refreshToken: 'aiInterviewRefreshToken'
  };

  const state = {
    userId: 1,
    accessToken: '',
    roles: [],
    sessions: [],
    currentSessionId: null,
    currentSession: null,
    turns: [],
    currentTurnId: null,
    timerRef: null,
    timerRemainSec: 0
  };

  const sessionListEl = document.getElementById('sessionList');
  const searchInputEl = document.getElementById('searchInput');
  const newSessionBtn = document.getElementById('newSessionBtn');

  const userNameEl = document.getElementById('userName');
  const userRoleEl = document.getElementById('userRole');
  const avatarEl = document.getElementById('avatar');
  const logoutBtn = document.getElementById('logoutBtn');

  const titleEl = document.getElementById('title');
  const subtitleEl = document.getElementById('subtitle');
  const timerChipEl = document.getElementById('timerChip');
  const openReportBtn = document.getElementById('openReportBtn');
  const openGrowthBtn = document.getElementById('openGrowthBtn');
  const closeDrawerBtn = document.getElementById('closeDrawer');
  const drawerTitle = document.getElementById('drawerTitle');
  const drawerContent = document.getElementById('drawerContent');

  const chatEl = document.getElementById('chat');
  const roleSelectEl = document.getElementById('roleSelect');
  const modeSelectEl = document.getElementById('modeSelect');
  const answerInputEl = document.getElementById('answerInput');
  const resumeFileEl = document.getElementById('resumeFile');
  const resumeLabelEl = document.getElementById('resumeLabel');
  const startBtn = document.getElementById('startBtn');
  const endBtn = document.getElementById('endBtn');
  const voiceBtn = document.getElementById('voiceBtn');
  const sendBtn = document.getElementById('sendBtn');
  const composerTipEl = document.getElementById('composerTip');

  // 语音识别状态
  let recognition = null;
  let isRecording = false;
  // 语音合成
  let speechSynthesis = window.speechSynthesis;
  let speechSynthesisUtterance = null;

  const reportDrawer = document.getElementById('reportDrawer');
  const closeDrawerBtn = document.getElementById('closeDrawer');
  const totalScoreEl = document.getElementById('totalScore');
  const reportSummaryEl = document.getElementById('reportSummary');
  const totalBarEl = document.getElementById('totalBar');
  const barCorrectnessEl = document.getElementById('barCorrectness');
  const barDepthEl = document.getElementById('barDepth');
  const barLogicEl = document.getElementById('barLogic');
  const barMatchEl = document.getElementById('barMatch');
  const barExpressionEl = document.getElementById('barExpression');
  const reportHighlightsEl = document.getElementById('reportHighlights');
  const reportImprovementsEl = document.getElementById('reportImprovements');

  // 语音通话弹窗元素
  const voiceCallModal = document.getElementById('voiceCallModal');
  const startCallBtn = document.getElementById('startCallBtn');
  const hangupBtn = document.getElementById('hangupBtn');
  const muteBtn = document.getElementById('muteBtn');
  const callStatusEl = document.getElementById('callStatus');
  const callTextEl = document.getElementById('callText');
  const waveAnimationEl = document.getElementById('waveAnimation');

  // 语音通话状态
  let isInCall = false;
  let isMuted = false;
  let callRecognition = null;
  let isCallRecording = false;
  let currentCallQuestion = null;
  let callSessionId = null;

  // 视频通话弹窗元素
  const videoCallModal = document.getElementById('videoCallModal');
  const videoStartCallBtn = document.getElementById('videoStartCallBtn');
  const videoHangupBtn = document.getElementById('videoHangupBtn');
  const videoMuteBtn = document.getElementById('videoMuteBtn');
  const videoCameraBtn = document.getElementById('videoCameraBtn');
  const videoCallStatusEl = document.getElementById('videoCallStatus');
  const videoCallTextEl = document.getElementById('videoCallText');
  const videoWaveAnimationEl = document.getElementById('videoWaveAnimation');
  const localVideo = document.getElementById('localVideo');
  const videoPlaceholder = document.getElementById('videoPlaceholder');

  // 视频通话状态
  let isInVideoCall = false;
  let isVideoMuted = false;
  let isCameraOff = false;
  let videoRecognition = null;
  let isVideoRecording = false;
  let currentVideoQuestion = null;
  let videoSessionId = null;
  let localStream = null; // 摄像头流

  // ========== 沉默检测相关 ==========
  let silenceTimerRef = null;      // 沉默计时器
  let silenceCountdownSec = 0;      // 沉默倒计时秒数
  let silenceTimeoutSec = 60;       // 沉默超时时间（秒），超过这个时间没说话就判定为沉默
  let isSilenceWarningShown = false; // 是否已显示过警告（30秒时）

  function parsePositiveInt(v, fallback) {
    const n = Number(v);
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

  function roleName(roleId) {
    const hit = state.roles.find((r) => r.id === roleId);
    return hit ? hit.name : `Role-${roleId || '-'}`;
  }

  function normalizeInterviewType(mode) {
    const value = String(mode || '').toUpperCase();
    if (value === 'VOICE' || value === 'VIDEO' || value === 'RESUME' || value === 'TEXT') {
      return value;
    }
    return 'TEXT';
  }

  function parseDateTime(value) {
    if (!value) return null;
    const d = new Date(String(value).replace(' ', 'T'));
    if (Number.isNaN(d.getTime())) return null;
    return d;
  }

  function statusText(status) {
    const map = {
      DRAFT: '待开始',
      IN_PROGRESS: '进行中',
      FINISHED: '已结束',
      ABANDONED: '已放弃',
      PENDING: '未开始',
      PROCESSING: '处理中',
      DONE: '已完成',
      FAILED: '失败'
    };
    return map[status] || (status || '-');
  }

  function setTip(text, isError) {
    if (!composerTipEl) return;
    try {
      composerTipEl.style.color = isError ? '#ce4a5f' : '#5a6e88';
      composerTipEl.textContent = text;
    } catch (e) {
      console.warn('设置提示失败:', e);
    }
  }

  // ========== 沉默检测功能 ==========
  
  /**
   * 开始沉默计时器 - 当显示新题目时调用
   * 沉默超过指定时间，面试官会判定你不会，直接跳题
   */
  function startSilenceTimer() {
    stopSilenceTimer();
    
    // 只有语音或视频面试才启用沉默检测
    const interviewType = currentInterviewType();
    if (interviewType !== 'VOICE' && interviewType !== 'VIDEO') {
      return; // 文字面试不需要沉默检测
    }
    
    console.log('开始沉默计时，当前面试类型:', interviewType);
    
    silenceCountdownSec = silenceTimeoutSec;
    isSilenceWarningShown = false;
    
    silenceTimerRef = setInterval(() => {
      silenceCountdownSec--;
      
      // 倒计时到30秒时显示警告
      if (silenceCountdownSec === 30 && !isSilenceWarningShown) {
        isSilenceWarningShown = true;
        setTip('你沉默时间较长，请尽快作答！还剩 30 秒时间', true);
        
        // 语音播报警告
        if (interviewType === 'VOICE' || interviewType === 'VIDEO') {
          speakText('你沉默时间较长，还剩30秒时间，请尽快作答！');
        }
      }
      
      // 倒计时到5秒时再次警告
      if (silenceCountdownSec === 5 && isSilenceWarningShown) {
        setTip('即将自动判定为不会，5秒后将跳到下一题', true);
        
        // 语音播报最后警告
        if (interviewType === 'VOICE' || interviewType === 'VIDEO') {
          speakText('即将自动判定为不会');
        }
      }
      
      // 倒计时到0，沉默超时
      if (silenceCountdownSec <= 0) {
        stopSilenceTimer();
        handleSilenceTimeout();
      }
    }, 1000);
  }
  
  /**
   * 停止沉默计时器 - 用户输入时调用
   */
  function stopSilenceTimer() {
    if (silenceTimerRef) {
      clearInterval(silenceTimerRef);
      silenceTimerRef = null;
    }
    silenceCountdownSec = 0;
    isSilenceWarningShown = false;
  }
  
  /**
   * 重置沉默计时器 - 用户开始输入时调用
   */
  function resetSilenceTimer() {
    stopSilenceTimer();
    // 不立即重启，给用户一点时间
    // 如果需要可以在这里加一个延迟启动
  }
  
  /**
   * 处理沉默超时 - 自动判定用户不会
   */
  async function handleSilenceTimeout() {
    if (!state.currentSessionId || !state.currentTurnId) {
      console.log('沉默超时但没有当前题目，跳过');
      return;
    }
    
    console.log('沉默超时，自动提交"不知道"回答');
    
    const interviewType = currentInterviewType();
    
    // 语音播报告知用户
    if (interviewType === 'VOICE' || interviewType === 'VIDEO') {
      speakText('你沉默时间过长，系统判定为不会，自动跳到下一题');
    }
    
    // 在聊天区显示沉默提示
    appendBubble('ai', '面试官', '看来你需要更多时间思考，我们先看下一题。');
    chatEl.scrollTop = chatEl.scrollHeight;
    
    // 提交"不知道"作为回答
    try {
      const req = {
        turnId: state.currentTurnId,
        answerMode: interviewType,
        answerText: '（沉默超时，系统自动判定为不会）',
        responseSec: silenceTimeoutSec
      };
      
      const resp = await api(`/inv/interview/sessions/${state.currentSessionId}/answers`, {
        method: 'POST',
        body: req
      });
      
      // 处理响应
      if (resp && resp.nextTurn) {
        state.currentTurnId = resp.nextTurn.id;
        if (resp.nextTurn.questionText) {
          const questionNo = resp.nextTurn.roundNo || 1;
          appendBubble('ai', `第 ${questionNo} 题`, resp.nextTurn.questionText);
          chatEl.scrollTop = chatEl.scrollHeight;
          
          // 语音播报新题目
          if (interviewType === 'VOICE' || interviewType === 'VIDEO') {
            speakText('请听第' + questionNo + '题：' + resp.nextTurn.questionText);
          }
        }
      }
      
      // 为下一题启动沉默计时器
      startSilenceTimer();
      
    } catch (err) {
      console.error('沉默超时处理失败:', err);
      setTip('沉默超时处理失败: ' + (err.message || String(err)), true);
    }
  }
  
  // ========== 语音合成功能 ==========
  function speakText(text, callback) {
    if (!window.speechSynthesis) return;
    window.speechSynthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = 'zh-CN';
    utterance.rate = 1.0;
    utterance.pitch = 1.0;
    if (callback) utterance.onend = callback;
    window.speechSynthesis.speak(utterance);
  }

  function loadAuth() {
    state.userId = parsePositiveInt(localStorage.getItem(STORE.userId), 0);
    state.accessToken = localStorage.getItem(STORE.accessToken) || '';
    
    // 如果没有token，跳转到登录页
    if (!state.accessToken) {
      window.location.href = './index.html';
      return false;
    }
    return true;
  }

  function clearAuthAndBackLogin() {
    localStorage.removeItem(STORE.accessToken);
    localStorage.removeItem(STORE.refreshToken);
    window.location.href = './index.html';
  }

  async function api(path, options) {
    const opt = options || {};
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

    const url = new URL(API_BASE + path, window.location.origin);
    if (opt.query) {
      Object.entries(opt.query).forEach(([k, v]) => {
        if (v === undefined || v === null || v === '') return;
        url.searchParams.set(k, String(v));
      });
    }

    const res = await fetch(url.toString(), {
      method: opt.method || 'GET',
      headers,
      body
    });

    let payload;
    try {
      payload = await res.json();
    } catch (e) {
      throw new Error('服务端返回格式异常');
    }

    if (!res.ok) {
      throw new Error((payload && payload.message) || `HTTP ${res.status}`);
    }
    if (!payload || payload.code !== 0) {
      throw new Error((payload && payload.message) || '请求失败');
    }
    return payload.data;
  }

  function stopTimer() {
    if (state.timerRef) {
      clearInterval(state.timerRef);
      state.timerRef = null;
    }
  }

  function updateTimerUI() {
    if (!timerChipEl) return;
    const sec = Math.max(0, state.timerRemainSec);
    const mm = String(Math.floor(sec / 60)).padStart(2, '0');
    const ss = String(sec % 60).padStart(2, '0');
    timerChipEl.textContent = `倒计时 ${mm}:${ss}`;
  }

  function startTimerBySession(session) {
    stopTimer();
    if (!timerChipEl) return;
    if (!session || session.status !== 'IN_PROGRESS') {
      timerChipEl.textContent = '倒计时 --:--';
      return;
    }

    const now = Date.now();
    const autoFinishAt = parseDateTime(session.autoFinishAt);
    if (autoFinishAt) {
      state.timerRemainSec = Math.max(0, Math.floor((autoFinishAt.getTime() - now) / 1000));
    } else {
      state.timerRemainSec = parsePositiveInt(session.timeLimitSec, 1500);
    }

    updateTimerUI();
    state.timerRef = setInterval(() => {
      state.timerRemainSec -= 1;
      if (state.timerRemainSec <= 0) {
        state.timerRemainSec = 0;
        updateTimerUI();
        stopTimer();
        setTip('本会话已到时，请结束会话并查看报告。', true);
        return;
      }
      updateTimerUI();
    }, 1000);
  }

  function updateHeaderBySession(session) {
    if (!session) {
      // 不改变标题和副标题，保留当前选择
      if (timerChipEl) timerChipEl.textContent = '倒计时 25:00';
      return;
    }

    // 只更新标题，保留副标题
    if (titleEl) titleEl.textContent = session.title || `${roleName(session.roleId)} - 面试`;
    if (timerChipEl) timerChipEl.textContent = '倒计时 25:00';
    startTimerBySession(session);
  }

  function appendBubble(type, label, text) {
    const node = document.createElement('article');
    node.className = `bubble ${type}`;
    node.innerHTML = `<span class="meta">${escapeHtml(label)}</span>${escapeHtml(text)}`;
    chatEl.appendChild(node);
  }

  // 检查是否选择了岗位和面试类型
  function isInterviewConfigSelected() {
    const roleId = roleSelectEl.value;
    const mode = modeSelectEl.value;
    // 检查是否选择了有效的岗位和面试类型（不能是空值）
    return roleId && roleId !== '' && mode && mode !== '';
  }

  // 更新按钮状态：开始面试后显示结束按钮
  function updateButtonState(isInterviewing) {
    // 如果不在面试中，检查是否选择了岗位和面试类型
    if (!isInterviewing) {
      const isSelected = isInterviewConfigSelected();
      startBtn.disabled = !isSelected;
      startBtn.style.opacity = isSelected ? '1' : '0.5';
      startBtn.style.cursor = isSelected ? 'pointer' : 'not-allowed';
      
      if (!isSelected) {
        setTip('请先选择岗位和面试类型');
      }
    }
    
    startBtn.style.display = isInterviewing ? 'none' : 'block';
    endBtn.style.display = isInterviewing ? 'block' : 'none';
  }

  // 根据面试类型显示/隐藏上传简历按钮
  function updateResumeVisibility() {
    const mode = currentInterviewType();
    resumeLabelEl.style.display = mode === 'RESUME' ? 'inline-flex' : 'none';
  }

  // 根据输入框内容切换语音/发送按钮
  function updateVoiceSendButton() {
    // 强制获取最新DOM元素
    const voiceBtnEl = document.getElementById('voiceBtn');
    const sendBtnEl = document.getElementById('sendBtn');
    const inputEl = document.getElementById('answerInput');
    
    if (!inputEl || !voiceBtnEl || !sendBtnEl) {
      console.error('按钮元素不存在!', {inputEl, voiceBtnEl, sendBtnEl});
      return;
    }
    
    const hasText = inputEl.value.trim().length > 0;
    console.log('切换按钮状态 - 有文字:', hasText);
    
    // 强制设置display属性
    voiceBtnEl.style.display = hasText ? 'none' : 'flex';
    sendBtnEl.style.display = hasText ? 'flex' : 'none';
  }

  // 初始化语音识别
  function initSpeechRecognition() {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SpeechRecognition) {
      console.warn('浏览器不支持语音识别');
      return;
    }

    recognition = new SpeechRecognition();
    recognition.lang = 'zh-CN';
    recognition.continuous = false;
    recognition.interimResults = true;

    recognition.onstart = () => {
      isRecording = true;
      voiceBtn.classList.add('recording');
      setTip('正在识别语音，请说话...');
    };

    recognition.onresult = (event) => {
      let transcript = '';
      for (let i = event.resultIndex; i < event.results.length; i++) {
        transcript += event.results[i][0].transcript;
      }
      answerInputEl.value = transcript;
      updateVoiceSendButton();
    };

    recognition.onend = () => {
      isRecording = false;
      voiceBtn.classList.remove('recording');
      if (answerInputEl.value.trim()) {
        setTip('语音识别完成，点击发送按钮提交');
      } else {
        setTip('未检测到语音，请重试');
      }
    };

    recognition.onerror = (event) => {
      isRecording = false;
      voiceBtn.classList.remove('recording');
      setTip(`语音识别错误: ${event.error}`, true);
    };
  }

  // 开始/停止语音识别
  function toggleVoiceRecognition() {
    if (!recognition) {
      setTip('浏览器不支持语音识别', true);
      return;
    }

    // 防止重复启动
    if (isRecording) {
      try {
        recognition.stop();
      } catch (e) {
        console.warn('停止语音识别失败:', e);
      }
    } else {
      try {
        recognition.start();
      } catch (e) {
        console.error('启动语音识别失败:', e);
        setTip('语音识别启动失败，请重试', true);
      }
    }
  }

  // 语音合成 - 播放AI回复（优化为更专业的面试官声音）
  function speakText(text, callback) {
    if (!speechSynthesis) {
      console.warn('浏览器不支持语音合成');
      if (callback) callback();
      return;
    }
    
    // 如果正在播放，先停止
    if (speechSynthesis.speaking) {
      speechSynthesis.cancel();
    }
    
    // 等待语音列表加载完成
    const loadVoices = () => {
      return new Promise((resolve) => {
        let voices = speechSynthesis.getVoices();
        if (voices.length > 0) {
          resolve(voices);
        } else {
          speechSynthesis.onvoiceschanged = () => {
            voices = speechSynthesis.getVoices();
            resolve(voices);
          };
        }
      });
    };
    
    // 创建语音合成实例
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = 'zh-CN';
    utterance.rate = 0.9; // 语速稍慢，更清晰
    utterance.pitch = 0.9; // 音调稍低，更成熟专业
    utterance.volume = 1.0; // 音量最大
    
    // 选择最佳中文语音 - 优先选择女声或专业语音
    const voices = speechSynthesis.getVoices();
    // 尝试找到更好的中文语音
    let selectedVoice = null;
    
    // 优先查找中文女声（通常更清晰）
    const zhFemale = voices.find(v => v.lang.includes('zh-CN') && v.name.includes('Female'));
    if (zhFemale) {
      selectedVoice = zhFemale;
    } else {
      // 次选普通中文语音
      const zhVoice = voices.find(v => v.lang.includes('zh'));
      if (zhVoice) {
        selectedVoice = zhVoice;
      }
    }
    
    if (selectedVoice) {
      utterance.voice = selectedVoice;
      console.log('使用语音:', selectedVoice.name);
    } else {
      console.warn('未找到中文语音，使用默认');
    }
    
    utterance.onend = () => {
      console.log('语音播放完成');
      if (callback) callback();
    };
    
    utterance.onerror = (e) => {
      console.error('语音播放错误:', e);
      if (callback) callback();
    };
    
    speechSynthesis.speak(utterance);
  }

  // 停止语音播放
  function stopSpeaking() {
    if (speechSynthesis && speechSynthesis.speaking) {
      speechSynthesis.cancel();
    }
  }

  // ============ 语音通话功能 ============
  
  // 初始化通话用的语音识别
  function initCallRecognition() {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SpeechRecognition) {
      console.warn('浏览器不支持语音识别');
      return false;
    }

    callRecognition = new SpeechRecognition();
    callRecognition.lang = 'zh-CN';
    callRecognition.continuous = false;
    callRecognition.interimResults = true;

    callRecognition.onstart = () => {
      isCallRecording = true;
      callStatusEl.textContent = '正在倾听...';
      waveAnimationEl.style.display = 'flex';
    };

    callRecognition.onresult = (event) => {
      let transcript = '';
      for (let i = event.resultIndex; i < event.results.length; i++) {
        transcript += event.results[i][0].transcript;
      }
      callTextEl.textContent = '你说: ' + transcript;
      
      // 如果是最终结果，发送到后端
      if (event.results[event.results.length - 1].isFinal) {
        handleUserAnswer(transcript);
      }
    };

    callRecognition.onend = () => {
      isCallRecording = false;
      waveAnimationEl.style.display = 'none';
    };

    callRecognition.onerror = (event) => {
      console.error('语音识别错误:', event.error);
      isCallRecording = false;
      waveAnimationEl.style.display = 'none';
      callStatusEl.textContent = '识别出错，请重试';
      // 自动重新开始监听
      setTimeout(() => {
        if (isInCall && !isCallRecording) {
          startCallRecognition();
        }
      }, 1000);
    };

    return true;
  }

  // 开始语音识别监听
  function startCallRecognition() {
    if (!callRecognition) {
      if (!initCallRecognition()) {
        callStatusEl.textContent = '语音识别不可用';
        return;
      }
    }
    try {
      callRecognition.start();
    } catch (e) {
      console.error('启动语音识别失败:', e);
    }
  }

  // 停止语音识别
  function stopCallRecognition() {
    if (callRecognition && isCallRecording) {
      try {
        callRecognition.stop();
      } catch (e) {}
    }
    isCallRecording = false;
  }

  // 处理用户回答
  async function handleUserAnswer(answerText) {
    if (!answerText || !answerText.trim()) {
      callStatusEl.textContent = '未检测到语音，请继续';
      startCallRecognition();
      return;
    }

    callStatusEl.textContent = 'AI思考中...';
    callTextEl.textContent = '你说: ' + answerText;
    stopSpeaking();

    // 在聊天区域显示用户的回答
    appendBubble('user', '你', answerText);
    chatEl.scrollTop = chatEl.scrollHeight;

    try {
      // 发送到后端
      const resp = await api(`/inv/interview/sessions/${callSessionId}/answers`, {
        method: 'POST',
        body: {
          turnId: state.currentTurnId,
          answerMode: 'VOICE',
          answerText: answerText,
          responseSec: 60
        }
      });

      // 获取AI回复（不再显示AI反馈，直接显示下一题）
      // 检查用户是否回答"不知道"或"不会"
      const userSaidDontKnow = answerText.includes('不知道') || answerText.includes('不会') || answerText.includes('不清楚');
      
      // 显示下一题提示
      appendBubble('ai', '下一题', userSaidDontKnow ? '好的，那我们来看下一题。' : '好的，让我们继续。');
      chatEl.scrollTop = chatEl.scrollHeight;

      // 更新当前轮次ID
      if (resp.nextTurn && resp.nextTurn.id) {
        state.currentTurnId = resp.nextTurn.id;
      }

      // 显示下一道题目
      if (resp.nextTurn && resp.nextTurn.questionText) {
        const questionNo = resp.nextTurn.roundNo || 1;
        appendBubble('ai', `第 ${questionNo} 题`, resp.nextTurn.questionText);
        chatEl.scrollTop = chatEl.scrollHeight;
        // 朗读下一题
        speakText(resp.nextTurn.questionText, () => {
          callStatusEl.textContent = '请开始回答...';
          callTextEl.textContent = '请说话...';
          startCallRecognition();
        });
      } else {
        callStatusEl.textContent = '面试结束...';
        callTextEl.textContent = '感谢参与面试';
      }
    } catch (err) {
      console.error('回答处理失败:', err);
      callStatusEl.textContent = '处理失败，请重试';
      startCallRecognition();
    }
  }

  // 开始语音通话
  async function startVoiceCall() {
    // 创建会话
    const roleId = Number(roleSelectEl.value);
    const req = {
      roleId,
      difficulty: 'MIDDLE',
      totalRounds: 5,
      timeLimitSec: 1500,
      interviewStage: 'TECH_FIRST',
      followupMode: 'AUTO',
      interviewType: 'VOICE',
      voiceEnabled: true,
      videoEnabled: false
    };

    try {
      const data = await api('/inv/interview/sessions', { method: 'POST', body: req });
      callSessionId = data.sessionId;
      state.currentSessionId = callSessionId;

      // 获取第一道题目
      await selectSession(callSessionId);

      // 显示通话界面
      voiceCallModal.classList.add('active');
      startCallBtn.style.display = 'none';
      hangupBtn.style.display = 'flex';
      muteBtn.style.display = 'flex';
      isInCall = true;

      // 找到第一道题目
      const firstTurn = state.turns.find(t => t.turnType === 'QUESTION');
      if (firstTurn) {
        state.currentTurnId = firstTurn.id;
        currentCallQuestion = firstTurn.questionText;
        callStatusEl.textContent = 'AI面试官';
        callTextEl.textContent = '面试开始...';
        
        // 在聊天区域显示AI问题
        appendBubble('ai', '第一题', currentCallQuestion);
        chatEl.scrollTop = chatEl.scrollHeight;
        
        // 朗读第一道题目
        setTimeout(() => {
          speakText('你好，我是AI面试官。现在开始面试。' + currentCallQuestion, () => {
            callStatusEl.textContent = '请开始回答...';
            callTextEl.textContent = '请说话...'
            startCallRecognition();
          });
        }, 500);
      }

    } catch (err) {
      console.error('创建会话失败:', err);
      alert('创建面试会话失败: ' + err.message);
    }
  }

  // 结束语音通话
  async function endVoiceCall() {
    isInCall = false;
    stopSpeaking();
    stopCallRecognition();

    // 关闭通话界面
    voiceCallModal.classList.remove('active');
    startCallBtn.style.display = 'flex';
    hangupBtn.style.display = 'none';
    muteBtn.style.display = 'none';

    // 手动挂断等于结束面试
    if (callSessionId) {
      try {
        await api(`/inv/interview/sessions/${callSessionId}/finish`, {
          method: 'POST',
          body: { finishReason: 'MANUAL', clientElapsedSec: 0 }
        });
        setTip('面试已结束，可查看报告。');
        // 更新会话列表
        await loadSessions();
        // 重新选择会话以刷新聊天记录
        await selectSession(callSessionId);
      } catch (e) {
        console.error('结束会话失败:', e);
      }
    }

    callSessionId = null;
    callStatusEl.textContent = '等待接听...';
    callTextEl.textContent = '点击开始与AI面试官通话';
    // 更新按钮状态
    updateButtonState(false);
  }

  // 切换静音状态
  function toggleMute() {
    isMuted = !isMuted;
    if (isMuted) {
      muteBtn.classList.add('muted');
      stopCallRecognition();
      callStatusEl.textContent = '已静音';
    } else {
      muteBtn.classList.remove('muted');
      callStatusEl.textContent = '请开始回答...';
      startCallRecognition();
    }
  }

  // 绑定通话事件
  function bindCallEvents() {
    if (startCallBtn) {
      startCallBtn.addEventListener('click', () => {
        console.log('开始语音通话');
        startVoiceCall();
      });
    }

    if (hangupBtn) {
      hangupBtn.addEventListener('click', () => {
        console.log('结束语音通话');
        endVoiceCall();
      });
    }

    if (muteBtn) {
      muteBtn.addEventListener('click', toggleMute);
    }
  }

  // ============ 视频通话功能 ============

  // 初始化视频通话用的语音识别
  function initVideoRecognition() {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SpeechRecognition) {
      console.warn('浏览器不支持语音识别');
      return false;
    }

    videoRecognition = new SpeechRecognition();
    videoRecognition.lang = 'zh-CN';
    videoRecognition.continuous = false;
    videoRecognition.interimResults = true;

    videoRecognition.onstart = () => {
      isVideoRecording = true;
      videoCallStatusEl.textContent = '正在倾听...';
      videoWaveAnimationEl.style.display = 'flex';
    };

    videoRecognition.onresult = (event) => {
      let transcript = '';
      for (let i = event.resultIndex; i < event.results.length; i++) {
        transcript += event.results[i][0].transcript;
      }
      videoCallTextEl.textContent = '你说: ' + transcript;
      
      if (event.results[event.results.length - 1].isFinal) {
        handleVideoAnswer(transcript);
      }
    };

    videoRecognition.onend = () => {
      isVideoRecording = false;
      videoWaveAnimationEl.style.display = 'none';
    };

    videoRecognition.onerror = (event) => {
      console.error('视频语音识别错误:', event.error);
      isVideoRecording = false;
      videoWaveAnimationEl.style.display = 'none';
      
      if (!isInVideoCall) {
        console.log('视频通话已结束，忽略识别错误');
        return;
      }
      
      if (event.error === 'no-speech') {
        videoCallStatusEl.textContent = '请开始回答...';
        startVideoRecognition();
        return;
      }
      
      videoCallStatusEl.textContent = '识别出错，请重试';
      setTimeout(() => {
        if (isInVideoCall && !isVideoRecording) {
          startVideoRecognition();
        }
      }, 1000);
    };

    return true;
  }

  // 开始视频语音识别
  function startVideoRecognition() {
    if (!videoRecognition) {
      if (!initVideoRecognition()) {
        videoCallStatusEl.textContent = '语音识别不可用';
        return;
      }
    }
    try {
      videoRecognition.start();
    } catch (e) {
      console.error('启动视频语音识别失败:', e);
    }
  }

  // 停止视频语音识别
  function stopVideoRecognition() {
    if (videoRecognition) {
      try {
        if (isVideoRecording) {
          videoRecognition.stop();
        }
      } catch (e) {
        console.log('停止视频语音识别:', e.message);
      }
    }
    isVideoRecording = false;
    videoWaveAnimationEl.style.display = 'none';
  }

  // 开启摄像头
  async function startCamera() {
    try {
      localStream = await navigator.mediaDevices.getUserMedia({ 
        video: true, 
        audio: true 
      });
      localVideo.srcObject = localStream;
      videoPlaceholder.classList.add('hidden');
      isCameraOff = false;
      videoCameraBtn.classList.remove('off');
      return true;
    } catch (err) {
      console.error('开启摄像头失败:', err);
      videoCallStatusEl.textContent = '无法开启摄像头';
      return false;
    }
  }

  // 关闭摄像头
  function stopCamera() {
    if (localStream) {
      localStream.getTracks().forEach(track => track.stop());
      localStream = null;
    }
    localVideo.srcObject = null;
    videoPlaceholder.classList.remove('hidden');
    isCameraOff = true;
    videoCameraBtn.classList.add('off');
  }

  // 处理视频用户的回答
  async function handleVideoAnswer(answerText) {
    if (!answerText || !answerText.trim()) {
      videoCallStatusEl.textContent = '未检测到语音，请继续';
      startVideoRecognition();
      return;
    }

    videoCallStatusEl.textContent = 'AI思考中...';
    videoCallTextEl.textContent = '你说: ' + answerText;

    // 在聊天区域显示用户的回答
    appendBubble('user', '你', answerText);
    chatEl.scrollTop = chatEl.scrollHeight;

    try {
      const resp = await api(`/inv/interview/sessions/${videoSessionId}/answers`, {
        method: 'POST',
        body: {
          turnId: state.currentTurnId,
          answerMode: 'VIDEO',
          answerText: answerText,
          responseSec: 60
        }
      });

      // 获取AI回复（不再显示AI反馈，直接显示下一题）
      // 检查用户是否回答"不知道"或"不会"
      const userSaidDontKnow = answerText.includes('不知道') || answerText.includes('不会') || answerText.includes('不清楚');

      // 显示下一题提示
      appendBubble('ai', '下一题', userSaidDontKnow ? '好的，那我们来看下一题。' : '好的，让我们继续。');
      chatEl.scrollTop = chatEl.scrollHeight;

      if (resp.nextTurn && resp.nextTurn.id) {
        state.currentTurnId = resp.nextTurn.id;
      }

      // 显示下一道题目
      if (resp.nextTurn && resp.nextTurn.questionText) {
        const questionNo = resp.nextTurn.roundNo || 1;
        appendBubble('ai', `第 ${questionNo} 题`, resp.nextTurn.questionText);
        chatEl.scrollTop = chatEl.scrollHeight;
        // 朗读下一题
        speakText(resp.nextTurn.questionText, () => {
          videoCallStatusEl.textContent = '请开始回答...';
          videoCallTextEl.textContent = '请说话...';
          startVideoRecognition();
        });
      } else {
        videoCallStatusEl.textContent = '面试结束...';
        videoCallTextEl.textContent = '感谢参与面试';
      }
    } catch (err) {
      console.error('视频回答处理失败:', err);
      videoCallStatusEl.textContent = '处理失败，请重试';
      startVideoRecognition();
    }
  }

  // 开始视频通话
  async function startVideoCall() {
    // 创建会话
    const roleId = Number(roleSelectEl.value);
    const req = {
      roleId,
      difficulty: 'MIDDLE',
      totalRounds: 5,
      timeLimitSec: 1500,
      interviewStage: 'TECH_FIRST',
      followupMode: 'AUTO',
      interviewType: 'VIDEO',
      voiceEnabled: true,
      videoEnabled: true
    };

    try {
      const data = await api('/inv/interview/sessions', { method: 'POST', body: req });
      videoSessionId = data.sessionId;
      state.currentSessionId = videoSessionId;

      // 获取第一道题目
      await selectSession(videoSessionId);

      // 开启摄像头
      const cameraReady = await startCamera();
      if (!cameraReady) {
        alert('无法开启摄像头，请检查权限设置');
        return;
      }

      // 显示视频通话界面
      videoCallModal.classList.add('active');
      videoStartCallBtn.style.display = 'none';
      videoHangupBtn.style.display = 'flex';
      videoMuteBtn.style.display = 'flex';
      videoCameraBtn.style.display = 'flex';
      isInVideoCall = true;

      // 找到第一道题目
      const firstTurn = state.turns.find(t => t.turnType === 'QUESTION');
      if (firstTurn) {
        state.currentTurnId = firstTurn.id;
        currentVideoQuestion = firstTurn.questionText;
        videoCallStatusEl.textContent = 'AI面试官';
        videoCallTextEl.textContent = '面试开始...';
        
        // 在聊天区域显示AI问题
        appendBubble('ai', '第一题', currentVideoQuestion);
        chatEl.scrollTop = chatEl.scrollHeight;
        
        // 朗读第一道题目
        setTimeout(() => {
          speakText('你好，我是AI面试官。现在开始面试。' + currentVideoQuestion, () => {
            videoCallStatusEl.textContent = '请开始回答...';
            videoCallTextEl.textContent = '请说话...';
            startVideoRecognition();
          });
        }, 500);
      }

    } catch (err) {
      console.error('创建视频会话失败:', err);
      alert('创建视频面试会话失败: ' + err.message);
    }
  }

  // 结束视频通话
  async function endVideoCall() {
    isInVideoCall = false;
    stopSpeaking();
    stopVideoRecognition();
    stopCamera();

    // 关闭视频通话界面
    videoCallModal.classList.remove('active');
    videoStartCallBtn.style.display = 'flex';
    videoHangupBtn.style.display = 'none';
    videoMuteBtn.style.display = 'none';
    videoCameraBtn.style.display = 'none';

    // 手动挂断等于结束面试
    if (videoSessionId) {
      try {
        await api(`/inv/interview/sessions/${videoSessionId}/finish`, {
          method: 'POST',
          body: { finishReason: 'MANUAL', clientElapsedSec: 0 }
        });
        setTip('视频面试已结束，可查看报告。');
        await loadSessions();
        await selectSession(videoSessionId);
      } catch (e) {
        console.error('结束视频会话失败:', e);
      }
    }

    videoSessionId = null;
    videoCallStatusEl.textContent = '等待接听...';
    videoCallTextEl.textContent = '点击开始与AI面试官视频通话';
    updateButtonState(false);
  }

  // 切换视频静音状态
  function toggleVideoMute() {
    isVideoMuted = !isVideoMuted;
    if (isVideoMuted) {
      videoMuteBtn.classList.add('muted');
      stopVideoRecognition();
      videoCallStatusEl.textContent = '已静音';
      if (localStream) {
        localStream.getAudioTracks().forEach(track => track.enabled = false);
      }
    } else {
      videoMuteBtn.classList.remove('muted');
      videoCallStatusEl.textContent = '请开始回答...';
      if (localStream) {
        localStream.getAudioTracks().forEach(track => track.enabled = true);
      }
      startVideoRecognition();
    }
  }

  // 切换摄像头开关
  function toggleVideoCamera() {
    if (isCameraOff) {
      startCamera();
      videoCallStatusEl.textContent = '请开始回答...';
    } else {
      stopCamera();
      videoCallStatusEl.textContent = '摄像头已关闭';
    }
  }

  // 绑定视频通话事件
  function bindVideoCallEvents() {
    if (videoStartCallBtn) {
      videoStartCallBtn.addEventListener('click', () => {
        console.log('开始视频通话');
        startVideoCall();
      });
    }

    if (videoHangupBtn) {
      videoHangupBtn.addEventListener('click', () => {
        console.log('结束视频通话');
        endVideoCall();
      });
    }

    if (videoMuteBtn) {
      videoMuteBtn.addEventListener('click', toggleVideoMute);
    }

    if (videoCameraBtn) {
      videoCameraBtn.addEventListener('click', toggleVideoCamera);
    }
  }

  function renderChat(turns) {
    chatEl.innerHTML = '';
    if (!turns || !turns.length) {
      state.currentTurnId = null;
      return;
    }

    const sorted = turns.slice().sort((a, b) => {
      if ((a.roundNo || 0) !== (b.roundNo || 0)) return (a.roundNo || 0) - (b.roundNo || 0);
      return (a.id || 0) - (b.id || 0);
    });

    state.currentTurnId = null;

    sorted.forEach((t) => {
      // 显示题目：用"第X题"格式，删除轮次和类型标识
      const questionNo = t.roundNo || '-';
      appendBubble('ai', `第 ${questionNo} 题`, t.questionText || '');

      const answerText = t.answerText || t.asrText;
      if (answerText) {
        appendBubble('user', '你', answerText);
      } else if (!state.currentTurnId) {
        state.currentTurnId = t.id;
        console.log('设置currentTurnId:', t.id, 'Turn数据:', JSON.stringify(t));
        // 启动沉默计时器（语音/视频面试）
        startSilenceTimer();
      }
      // 删除AI反馈和AI建议的显示
    });

    chatEl.scrollTop = chatEl.scrollHeight;

    if (state.currentTurnId) {
      setTip(`当前待回答 Turn: ${state.currentTurnId}`);
    } else {
      setTip('当前会话没有待回答题目，可结束会话查看报告。');
    }
  }

  function renderSessionList() {
    const keyword = (searchInputEl.value || '').trim().toLowerCase();
    const rows = state.sessions.filter((s) => {
      const text = `${s.title || ''} ${roleName(s.roleId)} ${s.status || ''}`.toLowerCase();
      return text.includes(keyword);
    });

    sessionListEl.innerHTML = rows.map((s) => {
      const active = s.id === state.currentSessionId ? 'active' : '';
      const desc = `${statusText(s.status)} · ${s.createdAt || '-'}`;
      return `
        <article class="session-item ${active}" data-id="${s.id}">
          <h4>${escapeHtml(s.title || roleName(s.roleId))}</h4>
          <p>${escapeHtml(desc)}</p>
        </article>
      `;
    }).join('');

    sessionListEl.querySelectorAll('.session-item').forEach((node) => {
      node.addEventListener('click', () => {
        const id = Number(node.getAttribute('data-id'));
        if (id) {
          selectSession(id).catch((err) => setTip(err.message || String(err), true));
        }
      });
    });
  }

  function fillRoleSelect() {
    // 使用固定的后端岗位和前端岗位
    roleSelectEl.innerHTML = '<option value=\"1\">后端岗位</option><option value=\"2\">前端岗位</option>';
    // 默认选择第一个
    roleSelectEl.value = '1';
  }

  // 页面加载时设置默认的面试类型
  function initDefaultSelections() {
    roleSelectEl.value = '1'; // 默认选择后端岗位
    modeSelectEl.value = 'TEXT'; // 默认选择文字面试
    updateTitleByRole(); // 更新标题和副标题
  }

  // 根据岗位选择更新标题
  function updateTitleByRole() {
    if (!titleEl || !subtitleEl) return;
    const roleId = roleSelectEl.value;
    if (roleId === '1') {
      titleEl.textContent = '后端面试';
      subtitleEl.textContent = '点击"开始面试"进入正式问答，结束后可查看报告。';
    } else if (roleId === '2') {
      titleEl.textContent = '前端面试';
      subtitleEl.textContent = '点击"开始面试"进入正式问答，结束后可查看报告。';
    }
  }

  async function loadProfile() {
    try {
      const me = await api('/inv/users/me');
      if (!me) return;
      userNameEl.textContent = me.username || `User-${state.userId}`;
      userRoleEl.textContent = `用户ID ${me.id || state.userId}`;
      avatarEl.textContent = (me.username || 'U').slice(0, 1).toUpperCase();
    } catch (err) {
      userNameEl.textContent = `User-${state.userId}`;
      userRoleEl.textContent = '读取用户信息失败';
    }
  }

  async function loadRoles() {
    state.roles = await api('/inv/roles');
    fillRoleSelect();
  }

  async function loadSessions() {
    const pageData = await api('/inv/interview/sessions', {
      query: { page: 1, pageSize: 200 }
    });

    state.sessions = (pageData && pageData.list) || [];
    renderSessionList();

    if (state.currentSessionId) {
      const exists = state.sessions.find((s) => s.id === state.currentSessionId);
      if (!exists) {
        state.currentSessionId = null;
        state.currentSession = null;
      }
    }

    // 优先选择没有进行中的会话，显示"开始面试"按钮
    if (!state.currentSessionId && state.sessions.length) {
      // 优先选择DRAFT状态的会话（新建未开始的）
      const draftSession = state.sessions.find((s) => s.status === 'DRAFT');
      // 如果没有DRAFT，找FINISHED（已结束的）
      const finishedSession = state.sessions.find((s) => s.status === 'FINISHED');
      // 如果都没有，才选第一个
      const preferred = draftSession || finishedSession || state.sessions[0];
      
      // 只有DRAFT状态才自动选中，其他情况不自动选会话
      if (preferred && preferred.status === 'DRAFT') {
        await selectSession(preferred.id);
      } else {
        // 没有DRAFT状态，不选中任何会话，显示"开始面试"按钮
        state.currentSessionId = null;
        state.currentSession = null;
        updateButtonState(false);
        updateHeaderBySession(null);
      }
    }

    // 如果没有任何会话，也需要更新按钮状态
    if (!state.currentSessionId) {
      updateButtonState(false);
    }
  }

  async function selectSession(sessionId) {
    state.currentSessionId = sessionId;
    renderSessionList();

    const detail = await api(`/inv/interview/sessions/${sessionId}`);
    state.currentSession = detail;
    updateHeaderBySession(detail);

    // 更新按钮状态：面试进行中显示结束按钮
    updateButtonState(detail && detail.status === 'IN_PROGRESS');

    if (detail && detail.roleId) {
      roleSelectEl.value = String(detail.roleId);
    }
    modeSelectEl.value = normalizeInterviewType(detail && detail.interviewType);
    updateResumeVisibility();

    const turnPage = await api(`/inv/interview/sessions/${sessionId}/turns`, {
      query: { page: 1, pageSize: 200 }
    });
    state.turns = (turnPage && turnPage.list) || [];
    renderChat(state.turns);
  }

  function currentInterviewType() {
    return normalizeInterviewType(modeSelectEl.value);
  }

  async function createSession() {
    const roleId = Number(roleSelectEl.value);
    if (!roleId) throw new Error('请先选择岗位');

    const interviewType = currentInterviewType();
    const req = {
      roleId,
      difficulty: 'MIDDLE',
      totalRounds: 5,
      timeLimitSec: 1500,
      interviewStage: 'TECH_FIRST',
      followupMode: 'AUTO',
      interviewType,
      voiceEnabled: interviewType === 'VOICE',
      videoEnabled: interviewType === 'VIDEO'
    };

    const data = await api('/inv/interview/sessions', { method: 'POST', body: req });
    setTip('会话创建成功，开始答题。');

    await loadSessions();
    if (data && data.sessionId) {
      await selectSession(data.sessionId);
    }
  }

  async function submitAnswer() {
    if (!state.currentSessionId) throw new Error('请先选择会话');
    if (!state.currentTurnId) throw new Error('当前没有待回答问题');

    const text = answerInputEl.value.trim();
    if (!text) throw new Error('请输入回答内容');

    // 用户开始输入，停止沉默计时器
    stopSilenceTimer();
    
    const req = {
      turnId: state.currentTurnId,
      answerMode: currentInterviewType(),
      answerText: text,
      responseSec: 60
    };

    const resp = await api(`/inv/interview/sessions/${state.currentSessionId}/answers`, {
      method: 'POST',
      body: req
    });

    answerInputEl.value = '';
    
    // 在聊天区域显示用户回答
    appendBubble('user', '你', text);
    
    // 检查是否有提示信息（无效回答时的提示）
    if (resp && resp.aiReplyText) {
      // 显示后端返回的提示
      appendBubble('ai', '提示', resp.aiReplyText);
      chatEl.scrollTop = chatEl.scrollHeight;
      
      // 如果有下一题，更新 currentTurnId 并显示下一题
      if (resp.nextTurn && resp.nextTurn.id) {
        state.currentTurnId = resp.nextTurn.id;
        if (resp.nextTurn.questionText) {
          const questionNo = resp.nextTurn.roundNo || 1;
          appendBubble('ai', `第 ${questionNo} 题`, resp.nextTurn.questionText);
          chatEl.scrollTop = chatEl.scrollHeight;
        }
      }
      await loadSessions();
      setTip('请重新回答或继续下一题');
      return;
    }
    
    // 正常回答处理：更新 currentTurnId 并刷新
    if (resp && resp.nextTurn && resp.nextTurn.id) {
      state.currentTurnId = resp.nextTurn.id;
      // 显示下一题
      if (resp.nextTurn.questionText) {
        const questionNo = resp.nextTurn.roundNo || 1;
        appendBubble('ai', `第 ${questionNo} 题`, resp.nextTurn.questionText);
        chatEl.scrollTop = chatEl.scrollHeight;
      }
    }

    await selectSession(state.currentSessionId);
    await loadSessions();
    setTip(`提交成功，轮次得分：${resp && resp.roundScore != null ? resp.roundScore : '-'}`);
  }

  async function uploadResume(file) {
    if (!state.currentSessionId) throw new Error('请先创建或选择会话');
    if (!file) return;

    const form = new FormData();
    form.append('file', file);
    form.append('sessionId', String(state.currentSessionId));
    if (state.currentTurnId) {
      form.append('turnId', String(state.currentTurnId));
    }
    form.append('fileType', 'RESUME');

    const data = await api('/inv/attachments/upload', {
      method: 'POST',
      formData: form
    });

    setTip(`简历上传成功：${data && data.fileName ? data.fileName : file.name}`);
  }

  function renderReport(report, scores) {
    const latestScore = scores && scores.length ? scores[scores.length - 1] : null;
    const total = report && report.overallScore != null
      ? Number(report.overallScore)
      : latestScore && latestScore.totalScore != null
        ? Number(latestScore.totalScore)
        : 0;

    totalScoreEl.textContent = String(Math.round(total));
    totalBarEl.style.width = `${Math.max(0, Math.min(100, total))}%`;

    const dim = report && report.dimensionScore ? report.dimensionScore : {};
    const c = Number(dim.correctness || 0);
    const d = Number(dim.depth || 0);
    const l = Number(dim.logic || 0);
    const m = Number(dim.match || 0);
    const e = Number(dim.expression || 0);

    barCorrectnessEl.style.width = `${c}%`;
    barDepthEl.style.width = `${d}%`;
    barLogicEl.style.width = `${l}%`;
    barMatchEl.style.width = `${m}%`;
    barExpressionEl.style.width = `${e}%`;

    reportSummaryEl.textContent = report && report.summary ? report.summary : '暂无报告摘要';

    const highlights = report && report.highlightPoints && report.highlightPoints.length
      ? report.highlightPoints.map((x) => `- ${x}`).join('\n')
      : '- 暂无亮点数据';

    const improvements = report && report.improvementPoints && report.improvementPoints.length
      ? report.improvementPoints.map((x) => `- ${x}`).join('\n')
      : '- 暂无改进建议';

    reportHighlightsEl.textContent = highlights;
    reportImprovementsEl.textContent = improvements;
  }

  async function openReportDrawer() {
    if (!state.currentSessionId) throw new Error('请先选择会话');

    drawerTitle.textContent = '面试评估报告';

    const [report, scores] = await Promise.all([
      api(`/inv/interview/sessions/${state.currentSessionId}/report`),
      api(`/inv/interview/sessions/${state.currentSessionId}/scores`)
    ]);

    renderReportContent(report, scores || []);
    reportDrawer.classList.add('open');
  }

  function closeReportDrawer() {
    reportDrawer.classList.remove('open');
  }

  // 渲染报告内容
  function renderReportContent(report, scores) {
    if (!report || !scores) {
      drawerContent.innerHTML = '<div class="hint">暂无报告数据</div>';
      return;
    }

    drawerContent.innerHTML = `
      <section class="block">
        <h4>总分</h4>
        <div class="score">
          <strong>${report.totalScore || 0}</strong>
          <div style="flex:1">
            <div class="hint">${report.summary || '暂无报告'}</div>
            <div class="bar"><i style="width: ${(report.totalScore || 0) / 100 * 100}%"></i></div>
          </div>
        </div>
      </section>

      <section class="block">
        <h4>维度细分</h4>
        ${scores.map(s => `
          <div>
            <div class="hint">${s.dimension || ''}</div>
            <div class="bar"><i style="width: ${(s.score || 0) / 100 * 100}%"></i></div>
          </div>
        `).join('')}
      </section>

      <section class="block">
        <h4>亮点与建议</h4>
        <div class="hint">${report.highlights || '暂无'}</div>
        <div class="hint">${report.improvements || '暂无'}</div>
      </section>
    `;
  }

  // 打开成长曲线抽屉
  async function openGrowthDrawer() {
    if (!state.userId) {
      alert('请先登录');
      return;
    }

    drawerTitle.textContent = '成长曲线';

    try {
      const res = await api(`/inv/interview/growth?userId=${state.userId}`);
      const data = await res.json();

      // 显示成长曲线
      showGrowthChart(data);
    } catch (err) {
      throw new Error(err.message || '获取成长数据失败');
    }
  }

  // 显示成长曲线图表
  function showGrowthChart(data) {
    const drawer = document.getElementById('reportDrawer');
    const drawerBody = document.getElementById('drawerContent');

    // 清空原有内容
    drawerBody.innerHTML = '';

    const section = document.createElement('section');
    section.className = 'block';

    const h4 = document.createElement('h4');
    h4.textContent = '成长曲线';
    section.appendChild(h4);

    // 创建图表容器
    const chartContainer = document.createElement('div');
    chartContainer.style.height = '300px';
    chartContainer.style.marginTop = '20px';
    section.appendChild(chartContainer);

    drawerBody.appendChild(section);

    // 使用 Chart.js 绘制成长曲线
    const ctx = chartContainer.getContext ? null : document.createElement('canvas');
    if (!chartContainer.getContext) {
      chartContainer.appendChild(ctx);
    } else {
      ctx = chartContainer.getContext('2d');
    }

    const chart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: data.dates || ['第1次', '第2次', '第3次', '第4次', '第5次'],
        datasets: [{
          label: '面试得分',
          data: data.scores || [70, 75, 80, 78, 85],
          borderColor: '#6366f1',
          backgroundColor: 'rgba(99, 102, 241, 0.1)',
          tension: 0.4,
          fill: true
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            max: 100,
            ticks: {
              stepSize: 20
            }
          }
        }
      }
    }
    });

    drawer.classList.add('open');
  }

  // 结束面试
  async function onEndInterview() {
    if (!state.currentSessionId) return;
    
    // 停止沉默计时器
    stopSilenceTimer();
    
    try {
      await api(`/inv/interview/sessions/${state.currentSessionId}/finish`, {
        method: 'POST',
        body: { finishReason: 'MANUAL', clientElapsedSec: 0 }
      });
      setTip('面试已结束，可查看报告。');
      updateButtonState(false);
      await loadSessions();
    } catch (err) {
      setTip(err.message || String(err), true);
    }
  }

  async function onStartInterview() {
    console.log('开始面试按钮被点击, 面试类型:', currentInterviewType());
    
    // 如果是语音面试模式，弹出语音通话界面
    if (currentInterviewType() === 'VOICE') {
      console.log('检测到语音面试模式，显示通话弹窗');
      if (voiceCallModal) {
        console.log('voiceCallModal元素存在:', voiceCallModal);
        voiceCallModal.classList.add('active');
        startCallBtn.style.display = 'flex';
        hangupBtn.style.display = 'none';
        muteBtn.style.display = 'none';
        callStatusEl.textContent = '等待接听...';
        callTextEl.textContent = '点击开始与AI面试官通话';
        waveAnimationEl.style.display = 'none';
        console.log('语音通话弹窗已显示');
      } else {
        console.error('voiceCallModal元素不存在!');
        setTip('语音通话界面加载失败', true);
      }
      return;
    }
    
    // 如果是视频面试模式，弹出视频通话界面
    if (currentInterviewType() === 'VIDEO') {
      console.log('检测到视频面试模式，显示视频通话弹窗');
      if (videoCallModal) {
        console.log('videoCallModal元素存在:', videoCallModal);
        videoCallModal.classList.add('active');
        videoStartCallBtn.style.display = 'flex';
        videoHangupBtn.style.display = 'none';
        videoMuteBtn.style.display = 'none';
        videoCameraBtn.style.display = 'none';
        videoCallStatusEl.textContent = '等待接听...';
        videoCallTextEl.textContent = '点击开始与AI面试官视频通话';
        videoWaveAnimationEl.style.display = 'none';
        console.log('视频通话弹窗已显示');
      } else {
        console.error('videoCallModal元素不存在!');
        setTip('视频通话界面加载失败', true);
      }
      return;
    }
    
    if (!state.currentSession || state.currentSession.status !== 'IN_PROGRESS') {
      await createSession();
      
      // 重新加载会话以获取后端已创建的第一个Turn
      await selectSession(state.currentSessionId);
      
      // 显示第一道题目提示
      if (state.currentTurnId) {
        setTip('请回答上面的问题');
      } else {
        setTip('开始面试成功，请回答问题');
      }
      
      updateButtonState(true);
      return;
    }

    await selectSession(state.currentSessionId);
    setTip('已进入当前进行中的会话，请直接回答。');
  }

  function bindEvents() {
    searchInputEl.addEventListener('input', renderSessionList);

    newSessionBtn.addEventListener('click', () => {
      createSession().catch((err) => setTip(err.message || String(err), true));
    });

    startBtn.addEventListener('click', () => {
      onStartInterview().catch((err) => setTip(err.message || String(err), true));
    });

    endBtn.addEventListener('click', () => {
      onEndInterview().catch((err) => setTip(err.message || String(err), true));
    });

    // 语音按钮点击开始/停止语音识别
    voiceBtn.addEventListener('click', toggleVoiceRecognition);

    // 发送按钮点击发送回答
    sendBtn.addEventListener('click', () => {
      submitAnswer().catch((err) => setTip(err.message || String(err), true));
    });

    // 输入框内容变化时切换语音/发送按钮，同时停止沉默计时器
    answerInputEl.addEventListener('input', () => {
      updateVoiceSendButton();
      stopSilenceTimer();
    });

    // Enter键发送回答
    answerInputEl.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        submitAnswer().catch((err) => setTip(err.message || String(err), true));
      }
    });

    // 岗位选择变化时更新标题
    roleSelectEl.addEventListener('change', updateTitleByRole);

    // 面试类型切换时显示/隐藏上传简历按钮
    modeSelectEl.addEventListener('change', updateResumeVisibility);

    resumeFileEl.addEventListener('change', () => {
      const file = resumeFileEl.files && resumeFileEl.files[0];
      uploadResume(file).catch((err) => setTip(err.message || String(err), true));
    });

    openReportBtn.addEventListener('click', () => {
      openReportDrawer().catch((err) => setTip(err.message || String(err), true));
    });

    openGrowthBtn.addEventListener('click', () => {
      openGrowthDrawer().catch((err) => setTip(err.message || String(err), true));
    });

    closeDrawerBtn.addEventListener('click', closeReportDrawer);

    logoutBtn.addEventListener('click', clearAuthAndBackLogin);
  }

  async function init() {
    // 检查登录状态，如果未登录则跳转
    if (!loadAuth()) {
      return;
    }
      
    bindEvents();
    
    // 绑定语音通话事件
    bindCallEvents();
    
    // 绑定视频通话事件
    bindVideoCallEvents();
      
    // 初始化语音识别
    initSpeechRecognition();
      
    // 初始化默认选择：后端岗位 + 文字面试
    initDefaultSelections();
      
    // 初始化上传简历按钮状态
    updateResumeVisibility();
      
    // 初始化语音/发送按钮状态
    updateVoiceSendButton();
    
    // 延迟再次确保按钮状态正确（解决DOM渲染时序问题）
    setTimeout(() => {
      updateVoiceSendButton();
    }, 100);
  
    try {
      await loadRoles();
      await loadProfile();
      await loadSessions();
      if (!state.sessions.length) {
        updateHeaderBySession(null);
        setTip('当前没有会话，点击"新建面试会话"开始。');
      }
    } catch (err) {
      // 如果是401错误，跳转到登录页
      if (err.message && err.message.includes('401')) {
        clearAuthAndBackLogin();
        return;
      }
      setTip(err.message || String(err), true);
    }
  }

  init();
})();



