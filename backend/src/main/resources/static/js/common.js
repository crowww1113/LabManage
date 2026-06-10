/**
 * 通用前端工具 — 字典加载、字典渲染、Token 请求封装
 * 依赖：无（自包含）
 */

// ====================== Token 请求封装 ======================
/** 与 api-fetch.js 等价，供未引入 api-fetch.js 的页面使用 */
async function dictFetch(url, options = {}) {
  const token = localStorage.getItem('auth_token') || localStorage.getItem('token');
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  if (token) headers['Authorization'] = 'Bearer ' + token;
  const res = await fetch(url, { ...options, headers });
  if (res.status === 401) { localStorage.clear(); window.location.href = '/login.html'; throw new Error('未授权'); }
  return res.json();
}

// ====================== 字典缓存与加载 ======================
window.sysDict = {};

/** 加载全部字典到 window.sysDict，完成后自动渲染各页面的字典下拉框 */
async function loadAllDictionaries() {
  try {
    const data = await dictFetch('/api/dict/all');
    window.sysDict = data;
  } catch (e) {
    console.error('加载字典失败', e);
  }
}

// ====================== 字典下拉框渲染 ======================
/**
 * 用缓存的字典数据填充 <select>
 * @param {string} selectId       <select> 的 id
 * @param {string} dictType       字典类型，如 "course_type"
 * @param {Array}  defaultOptions 前置固定选项，如 [{value:'all', text:'全部'}]
 */
function renderDictSelectOptions(selectId, dictType, defaultOptions) {
  const select = document.getElementById(selectId);
  if (!select) return;
  select.innerHTML = '';
  if (defaultOptions) {
    defaultOptions.forEach(opt => {
      const o = document.createElement('option');
      if (opt.value !== undefined) o.value = opt.value;
      o.textContent = opt.text;
      select.appendChild(o);
    });
  }
  const items = window.sysDict[dictType] || [];
  items.forEach(item => {
    const option = document.createElement('option');
    option.value = item.dictValue || item.dictKey;
    option.textContent = item.dictValue || item.dictKey;
    select.appendChild(option);
  });
}
