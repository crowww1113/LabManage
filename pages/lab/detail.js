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
    termIndex: 0,
    // 预约表单
    showBooking: false,
    bookingDate: '',
    bookingTimeSlotIndex: 0,
    bookingTimeSlots: [],
    bookingClazzes: [],
    bookingClazzIndex: 0,
    bookingPeople: '',
    bookingPurpose: '',
    bookingSubmitting: false
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
    this.setData({ weeks, currentDay: dayOfWeek })
  },

  onShow() {
    this.loadAvailability()
  },

  async loadLabDetail() {
    const { labId, buildingId } = this.data
    this.setData({ loading: true })

    try {
      const app = getApp()
      const terms = app.globalData.terms || []

      const [timeSlots, rooms] = await Promise.all([
        api.getTimeSlots(),
        api.getRooms(buildingId)
      ])

      const timeSlotIds = timeSlots.map(t => t.id)
      const lab = rooms.find(r => r.id == labId)

      this.setData({
        timeSlotIds,
        terms,
        bookingTimeSlots: timeSlots
      })

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

    if (!terms || terms.length === 0) {
      console.warn('学期为空，跳过查询')
      return
    }

    const termId = terms[termIndex]?.id
    const dayIndex = currentDay - 1

    try {
      const timetableData = await api.getTimetableMatrix({
        termId: termId,
        weekNo: currentWeek,
        buildingName: buildingName
      })

      const dayData = timetableData.matrix?.[dayIndex] || []
      const roomConflicts = []

      dayData.forEach((slotItems, slotIndex) => {
        if (slotItems && Array.isArray(slotItems)) {
          slotItems.forEach(item => {
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

      this.setData({ availability: roomConflicts })
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

    const now = new Date()
    const startDate = new Date(term.startDate)
    const diffDays = Math.floor((now - startDate) / (1000 * 60 * 60 * 24))
    const currentWeek = Math.max(1, Math.floor(diffDays / 7) + 1)

    this.setData({ termIndex, currentWeek })
    this.loadAvailability()
  },

  onDayTap(e) {
    const day = e.currentTarget.dataset.day
    this.setData({ currentDay: day })
    this.loadAvailability()
  },

  // ========== 预约表单 ==========

  async onBookingTap() {
    const today = this.formatDate(new Date())
    this.setData({
      showBooking: true,
      bookingDate: today,
      bookingTimeSlotIndex: 0,
      bookingClazzIndex: 0,
      bookingPeople: '',
      bookingPurpose: '',
      bookingSubmitting: false
    })

    // 加载班级列表
    try {
      const clazzes = await api.getClazzes()
      this.setData({ bookingClazzes: clazzes || [] })
    } catch (err) {
      console.error('加载班级列表失败:', err)
      this.setData({ bookingClazzes: [] })
    }
  },

  onBookingClose() {
    this.setData({ showBooking: false })
  },

  onBookingDateChange(e) {
    this.setData({ bookingDate: e.detail.value })
  },

  onBookingTimeSlotChange(e) {
    this.setData({ bookingTimeSlotIndex: e.detail.value })
  },

  onBookingClazzChange(e) {
    this.setData({ bookingClazzIndex: e.detail.value })
  },

  onBookingPeopleInput(e) {
    this.setData({ bookingPeople: e.detail.value })
  },

  onBookingPurposeInput(e) {
    this.setData({ bookingPurpose: e.detail.value })
  },

  onBookingSubmit() {
    const { bookingDate, bookingTimeSlotIndex, bookingTimeSlots, bookingClazzes, bookingClazzIndex, bookingPeople, bookingPurpose } = this.data

    if (!bookingDate) {
      wx.showToast({ title: '请选择使用日期', icon: 'none' })
      return
    }
    if (!bookingPeople || parseInt(bookingPeople) <= 0) {
      wx.showToast({ title: '请输入使用人数', icon: 'none' })
      return
    }
    if (!bookingPurpose.trim()) {
      wx.showToast({ title: '请输入使用目的', icon: 'none' })
      return
    }

    const timeSlot = bookingTimeSlots[bookingTimeSlotIndex]
    const clazz = bookingClazzes[bookingClazzIndex]

    wx.showModal({
      title: '确认预约',
      content: `${bookingDate} ${timeSlot.slotName}\n${bookingPeople}人 · ${bookingPurpose}`,
      confirmText: '提交',
      confirmColor: '#007aff',
      success: (res) => {
        if (res.confirm) {
          this.submitBooking()
        }
      }
    })
  },

  async submitBooking() {
    const { bookingDate, bookingTimeSlotIndex, bookingTimeSlots, bookingClazzes, bookingClazzIndex, bookingPeople, bookingPurpose, labInfo, buildingName, terms, termIndex, bookingSubmitting } = this.data

    if (bookingSubmitting) return
    this.setData({ bookingSubmitting: true })

    const timeSlot = bookingTimeSlots[bookingTimeSlotIndex]
    const clazz = bookingClazzes[bookingClazzIndex]
    const term = terms[termIndex]

    // 从日期计算 weekNo 和 dayOfWeek
    const dateObj = new Date(bookingDate)
    const dayOfWeek = dateObj.getDay() || 7 // 1=周一 ... 7=周日

    // 计算 weekNo（基于学期开始日期）
    let weekNo = 1
    if (term && term.startDate) {
      const startDate = new Date(term.startDate)
      const diffDays = Math.floor((dateObj - startDate) / (1000 * 60 * 60 * 24))
      weekNo = Math.max(1, Math.floor(diffDays / 7) + 1)
    }

    // 格式化时间为 HH:mm:ss
    const formatTime = (t) => {
      if (!t) return null
      // 如果已经是 HH:mm:ss 格式直接返回
      if (/^\d{2}:\d{2}:\d{2}$/.test(t)) return t
      // 如果是 HH:mm 格式补秒
      if (/^\d{2}:\d{2}$/.test(t)) return t + ':00'
      return t
    }

    const body = {
      termId: term?.id,
      clazzId: clazz?.id,
      buildingName: buildingName,
      roomNumber: labInfo?.code,
      useDate: bookingDate,
      weekNo: weekNo,
      dayOfWeek: dayOfWeek,
      timeSlotId: timeSlot?.id,
      startTime: formatTime(timeSlot?.startTime),
      endTime: formatTime(timeSlot?.endTime),
      studentCount: parseInt(bookingPeople),
      experimentContent: bookingPurpose.trim()
    }

    console.log('提交预约参数:', body)

    try {
      await api.createReservation(body)
      wx.showToast({ title: '预约成功', icon: 'success' })
      this.setData({ showBooking: false })
      this.loadAvailability()
    } catch (err) {
      console.error('预约失败:', err)
      wx.showToast({ title: err.message || '预约失败', icon: 'none' })
    } finally {
      this.setData({ bookingSubmitting: false })
    }
  },

  formatDate(date) {
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, '0')
    const d = String(date.getDate()).padStart(2, '0')
    return `${y}-${m}-${d}`
  },

  preventScroll() {}
})
