const api = require('../../utils/api')

Page({
  data: {
    labId: null,
    buildingId: null,
    buildingName: '',
    labInfo: null,
    availability: [],
    loading: true,
    currentWeek: 1,
    currentDay: 1,
    weeks: [],
    days: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
    timeSlotIds: [],
    // 学期相关
    terms: [],
    termIndex: 0
  },

  onLoad(options) {
    const labId = options.id
    const buildingId = options.buildingId
    const buildingName = options.buildingName ? decodeURIComponent(options.buildingName) : ''
    const termIndex = options.termIndex ? parseInt(options.termIndex) : 0
    const weekIndex = options.weekIndex ? parseInt(options.weekIndex) : 0
    const dayIndex = options.dayIndex ? parseInt(options.dayIndex) : 0

    this.setData({
      labId,
      buildingId,
      buildingName,
      termIndex,
      weekIndex,
      currentWeek: weekIndex + 1,
      currentDay: dayIndex + 1
    })

    this.initWeeks()
    this.loadLabDetail()
  },

  initWeeks() {
    const weeks = []
    for (let i = 1; i <= 20; i++) {
      weeks.push(i)
    }
    const now = new Date()
    const dayOfWeek = now.getDay() || 7
    this.setData({
      weeks,
      currentDay: dayOfWeek
    })
  },

  onShow() {
    // 每次显示页面时刷新数据
    this.loadAvailability()
  },

  async loadLabDetail() {
    const { labId, buildingId, termIndex } = this.data
    this.setData({ loading: true })

    try {
      // 从全局获取当前学期
      const app = getApp()
      const terms = app.globalData.terms || []

      // 获取时间段
      const timeSlots = await api.getTimeSlots()
      const timeSlotIds = timeSlots.map(t => t.id)

      this.setData({
        timeSlotIds,
        terms
      })

      // 获取房间信息
      const rooms = await api.getRooms(buildingId)
      const lab = rooms.find(r => r.id == labId)

      if (lab) {
        this.setData({ labInfo: lab })
        await this.loadAvailability()
      }
    } catch (err) {
      console.error('加载详情失败:', err)
      this.useMockData()
    }

    this.setData({ loading: false })
  },

  useMockData() {
    this.setData({
      labInfo: {
        id: this.data.labId,
        code: 'Lab-101',
        seats: 40,
        roomType: '机房',
        floor: 1,
        area: '80㎡',
        intro: '配备高性能计算机，适合编程、设计等课程使用'
      }
    })
  },

  async loadAvailability() {
    const { currentWeek, currentDay, labInfo, buildingName, terms, termIndex } = this.data

    // 如果没有学期，不请求
    if (!terms || terms.length === 0) {
      console.warn('学期为空，跳过查询')
      return
    }

    const termId = terms[termIndex]?.id
    const dayIndex = currentDay - 1 // 转换为0-6索引

    try {
      // 使用新的课表矩阵接口
      const timetableData = await api.getTimetableMatrix({
        termId: termId,
        weekNo: currentWeek,
        buildingName: buildingName
      })

      console.log('详情页课表数据:', timetableData)

      // 从矩阵中提取当前房间当天的所有课程
      const dayData = timetableData.matrix?.[dayIndex] || []
      const roomConflicts = []

      dayData.forEach((slotItems, slotIndex) => {
        if (slotItems && Array.isArray(slotItems)) {
          slotItems.forEach(item => {
            // 只保留当前房间的课程
            if (item.roomNumber === labInfo?.code) {
              roomConflicts.push({
                courseName: item.courseName || '未知课程',
                teacherName: item.teacherName || '',
                clazzName: item.clazzName || '',
                timeSlotName: timetableData.timeSlots?.[slotIndex]?.slotName || '',
                sourceType: item.sourceType || '',
                status: item.status || ''
              })
            }
          })
        }
      })

      this.setData({
        availability: roomConflicts
      })
    } catch (err) {
      console.error('加载空闲状态失败:', err)
    }
  },

  onWeekChange(e) {
    this.setData({ currentWeek: parseInt(e.detail.value, 10) + 1 })
    this.loadAvailability()
  },

  onTermChange(e) {
    const termIndex = e.detail.value
    const term = this.data.terms[termIndex]

    // 重新计算当前周次
    const now = new Date()
    const startDate = new Date(term.startDate)
    const diffDays = Math.floor((now - startDate) / (1000 * 60 * 60 * 24))
    const currentWeek = Math.max(1, Math.floor(diffDays / 7) + 1)

    this.setData({
      termIndex,
      currentWeek
    })
    this.loadAvailability()
  },

  onDayTap(e) {
    const day = e.currentTarget.dataset.day
    this.setData({ currentDay: day })
    this.loadAvailability()
  },

  onBookingTap() {
    wx.showToast({ title: '预约功能开发中', icon: 'none' })
  }
})
