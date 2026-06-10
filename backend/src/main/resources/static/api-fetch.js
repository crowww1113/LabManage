/**
 * 全局请求封装 — 所有页面共用
 * 自动注入 Authorization Token、Content-Type、401 拦截跳转、业务异常处理
 */
async function apiFetch(url, options = {}) {
    let token = localStorage.getItem('auth_token');
    if (!token) {
        try {
            const loginUser = JSON.parse(localStorage.getItem('loginUser'));
            token = loginUser?.token;
        } catch (e) { /* ignore */ }
    }
    if (!token) {
        token = localStorage.getItem('token');
    }

    // 自动补全 /api/ 前缀
    if (!url.startsWith('http') && !url.startsWith('/api/')) {
        url = url.startsWith('/') ? '/api' + url : '/api/' + url;
    }

    const headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {})
    };
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }

    let response;
    try {
        response = await fetch(url, { ...options, headers });
    } catch (networkError) {
        throw new Error('网络请求失败: ' + networkError.message);
    }

    // 401 → 清除登录态，跳转登录页
    if (response.status === 401) {
        localStorage.clear();
        window.location.href = '/login.html';
        throw new Error('登录已过期，请重新登录');
    }

    // HTTP 状态码异常
    if (!response.ok) {
        const message = await response.text().catch(() => '');
        const err = new Error(message || '请求失败 (状态码: ' + response.status + ')');
        err.status = response.status;
        throw err;
    }

    // 204 No Content
    if (response.status === 204) {
        return null;
    }

    const text = await response.text();
    if (!text) {
        return null;
    }

    let data;
    try {
        data = JSON.parse(text);
    } catch (e) {
        return text; // 非 JSON 响应，直接返回原文
    }

    // 拦截后端业务异常：HTTP 200 但 code 为错误码
    if (data && typeof data === 'object' && !Array.isArray(data)) {
        const code = data.code;
        if (code !== undefined && code !== null && code !== 0 && code !== 200) {
            const err = new Error(data.message || data.msg || '业务处理失败');
            err.code = code;
            throw err;
        }
    }

    return data;
}
