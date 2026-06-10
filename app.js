// app.js
App({
  globalData: {
    baseUrl: 'http://localhost:8083',
    token: null
  },

  onLaunch() {
    const token = wx.getStorageSync('token')
    if (token) {
      this.globalData.token = token
    }
  }
})
