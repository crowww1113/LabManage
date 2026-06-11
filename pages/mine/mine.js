const api = require('../../utils/api')

Page({
  data: {
    username: '',
    realName: '',
    roleName: '',
    reservations: [],
    loading: false,
    statusMap: {
      'PENDING': { text: '待审批', color: '#ff9500' },
      'APPROVED': { text: '已通过', color: '#34c759' },
      'REJECTED': { text: '已拒绝', color: '#ff3b30' },
      'CANCELLED': { text: '已取消', color: '#8e8e93' },
      'IN_USE': { text: '使用中', color: '#007aff' },
      'COMPLETED': { text: '已完成', color: '#8e8e93' }
    }
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setSelected(4)
    }
    this.loadUserInfo()
    this.loadReservations()
  },

  loadUserInfo() {
    const userInfo = wx.getStorageSync('userInfo')
    if (userInfo) {
      this.setData({
        username: userInfo.username || '',
        realName: userInfo.realName || userInfo.username || '',
        roleName: userInfo.roleName || '',
        userId: userInfo.userId
      })
    }
  },

  async loadReservations() {
    const userId = this.data.userId
    if (!userId) return

    this.setData({ loading: true })

    try {
      const list = await api.request({
        url: '/api/schedule-reservations',
        data: { teacherId: userId }
      })

      this.setData({
        reservations: Array.isArray(list) ? list : []
      })
    } catch (err) {
      console.error('加载预约记录失败:', err)
    } finally {
      this.setData({ loading: false })
    }
  },

  handleLogout() {
    wx.showModal({
      title: '',
      content: '确定退出登录？',
      confirmText: '退出',
      confirmColor: '#ff3b30',
      success(res) {
        if (res.confirm) {
          wx.clearStorageSync()
          wx.reLaunch({ url: '/pages/login/login' })
        }
      }
    })
  }
});
