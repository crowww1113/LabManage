const { post } = require('../../utils/request');

Page({
  data: {
    username: '',
    password: ''
  },

  // 输入框事件
  onUsernameInput(e) {
    this.setData({ username: e.detail.value });
  },

  onPasswordInput(e) {
    this.setData({ password: e.detail.value });
  },

  // 快速填充演示账号
  fillDemo(e) {
    const { username, password } = e.currentTarget.dataset;
    this.setData({
      username: username,
      password: password
    });
    wx.showToast({
      title: '已填充账号',
      icon: 'none'
    });
  },

  // 登录处理
  async handleLogin() {
    const { username, password } = this.data;

    // 校验账号密码是否为空
    if (!username.trim()) {
      wx.showToast({
        title: '请输入学号/工号',
        icon: 'none'
      });
      return;
    }

    if (!password) {
      wx.showToast({
        title: '请输入密码',
        icon: 'none'
      });
      return;
    }

    // 显示加载状态
    wx.showLoading({
      title: '登录中...',
      mask: true
    });

    try {
      // 发起登录请求
      const data = await post('/auth/login', {
        username: username.trim(),
        password: password
      });

      // 登录成功，存储 token 和用户信息
      wx.setStorageSync('token', data.token);
      wx.setStorageSync('userInfo', {
        userId: data.userId,
        username: data.username,
        realName: data.realName,
        roles: data.roles,
        roleName: data.roleName,
        roleCode: data.roleCode
      });

      wx.showToast({
        title: '登录成功',
        icon: 'success'
      });

      // 延迟跳转到首页
      setTimeout(() => {
        wx.switchTab({
          url: '/pages/index/index'
        });
      }, 1000);

    } catch (err) {
      console.error('登录失败:', err);
      // 错误已在 request.js 中统一处理
    } finally {
      wx.hideLoading();
    }
  }
});
