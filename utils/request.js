/**
 * 全局请求封装 — 微信小程序版
 * 自动注入 Authorization Token、401 拦截跳转、业务异常处理
 */

// 基础配置
const BASE_URL = 'http://127.0.0.1:8083'; // 开发环境，生产环境需修改

/**
 * 封装 wx.request 为 Promise
 * @param {string} url - 请求路径（相对路径会自动补全 /api/ 前缀）
 * @param {object} options - 请求配置
 * @param {string} options.method - 请求方法，默认 GET
 * @param {object} options.data - 请求数据
 * @param {object} options.headers - 自定义请求头
 * @returns {Promise} - 返回请求结果
 */
function request(url, options = {}) {
  return new Promise((resolve, reject) => {
    // 获取 token
    const token = wx.getStorageSync('token');

    // 自动补全 /api/ 前缀
    if (!url.startsWith('http') && !url.startsWith('/api/')) {
      url = url.startsWith('/') ? '/api' + url : '/api/' + url;
    }

    // 拼接完整 URL
    const fullUrl = url.startsWith('http') ? url : BASE_URL + url;

    // 构建请求头
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers
    };

    // 注入 Token
    if (token) {
      headers['Authorization'] = 'Bearer ' + token;
    }

    // 发起请求
    wx.request({
      url: fullUrl,
      method: options.method || 'GET',
      data: options.data || {},
      header: headers,
      timeout: 30000,
      success: (res) => {
        const { statusCode, data } = res;

        // 401 未授权 → 清除登录态，跳转登录页
        if (statusCode === 401) {
          wx.removeStorageSync('token');
          wx.removeStorageSync('userInfo');
          wx.showToast({
            title: '登录已过期',
            icon: 'none',
            duration: 1500
          });
          setTimeout(() => {
            wx.reLaunch({
              url: '/pages/login/login'
            });
          }, 1500);
          reject(new Error('登录已过期，请重新登录'));
          return;
        }

        // HTTP 状态码异常（非 200/201）
        if (statusCode !== 200 && statusCode !== 201) {
          const message = (data && data.message) || '请求失败 (状态码: ' + statusCode + ')';
          wx.showToast({
            title: message,
            icon: 'none',
            duration: 2000
          });
          reject(new Error(message));
          return;
        }

        // 204 No Content
        if (statusCode === 204) {
          resolve(null);
          return;
        }

        // 拦截后端业务异常：HTTP 200 但 code 为错误码
        if (data && typeof data === 'object' && !Array.isArray(data)) {
          const code = data.code;
          if (code !== undefined && code !== null && code !== 0 && code !== 200) {
            const message = data.message || data.msg || '业务处理失败';
            wx.showToast({
              title: message,
              icon: 'none',
              duration: 2000
            });
            const err = new Error(message);
            err.code = code;
            reject(err);
            return;
          }
        }

        // 成功返回数据
        resolve(data);
      },
      fail: (err) => {
        wx.showToast({
          title: '网络请求失败',
          icon: 'none',
          duration: 2000
        });
        reject(new Error('网络请求失败: ' + err.errMsg));
      }
    });
  });
}

/**
 * GET 请求
 */
function get(url, data, options = {}) {
  return request(url, { ...options, method: 'GET', data });
}

/**
 * POST 请求
 */
function post(url, data, options = {}) {
  return request(url, { ...options, method: 'POST', data });
}

/**
 * PUT 请求
 */
function put(url, data, options = {}) {
  return request(url, { ...options, method: 'PUT', data });
}

/**
 * DELETE 请求
 */
function del(url, data, options = {}) {
  return request(url, { ...options, method: 'DELETE', data });
}

module.exports = {
  request,
  get,
  post,
  put,
  del
};
