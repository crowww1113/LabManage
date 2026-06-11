const api = require('../../utils/api')

Page({
  data: {
    username: '',
    realName: '',
    roleName: '',
    // 学期相关
    terms: [],
    termIndex: 0
  },

  onShow() {
    // 同步自定义tabBar选中态
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setSelected(0)
    }
    // 鉴权检查
    const token = wx.getStorageSync('token');
    if (!token) {
      wx.reLaunch({
        url: '/pages/login/login'
      });
      return;
    }
    this.loadUserInfo();
    this.loadTerms();
  },

  // 读取用户信息
  loadUserInfo() {
    const userInfo = wx.getStorageSync('userInfo');
    if (userInfo) {
      this.setData({
        username: userInfo.username || '',
        realName: userInfo.realName || userInfo.username || '',
        roleName: userInfo.roleName || ''
      });
    }
  },

  // 加载学期列表
  async loadTerms() {
    try {
      const terms = await api.getTerms()
      if (terms && terms.length > 0) {
        // 找到当前学期（status=1）
        const currentIndex = terms.findIndex(t => t.status === '1')
        const termIndex = currentIndex >= 0 ? currentIndex : 0

        this.setData({ terms, termIndex })

        // 保存到全局
        const app = getApp()
        app.globalData.terms = terms
        app.globalData.currentTerm = terms[termIndex]
        app.globalData.termIndex = termIndex
      }
    } catch (err) {
      console.error('获取学期列表失败:', err)
    }
  },

  // 切换学期
  onTermChange(e) {
    const termIndex = e.detail.value
    const term = this.data.terms[termIndex]

    this.setData({ termIndex })

    // 更新全局
    const app = getApp()
    app.globalData.currentTerm = term
    app.globalData.termIndex = termIndex

    wx.showToast({ title: `已切换到${term.termName}`, icon: 'none' })
  },

  // 点击功能块
  onTapGrid(e) {
    const type = e.currentTarget.dataset.type;
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    });
  }
});
