Page({
  data: {
    username: '',
    realName: '',
    roleName: ''
  },

  onShow() {
    this.loadUserInfo();
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

  // 点击功能块
  onTapGrid(e) {
    const type = e.currentTarget.dataset.type;
    wx.showToast({
      title: '功能开发中',
      icon: 'none'
    });
  }
});
