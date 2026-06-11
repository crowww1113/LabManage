const api = require('../../utils/api')

Page({
  data: {
    // 角色
    isStudent: false,
    // 楼栋相关
    buildings: [],
    buildingIndex: 0,

    // 搜索和筛选
    searchText: '',
    filterType: 'all', // all | free | busy

    // 列表数据
    labList: [],
    filteredList: [],
    loading: true,

    // 学期相关
    terms: [],
    termIndex: 0,
    weeks: [],
    weekIndex: 0,
    days: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
    dayIndex: 0,
    timeSlotIds: []
  },

  onLoad() {
    this.initWeeks()
    this.initData()
  },

  onShow() {
    // 同步自定义tabBar选中态
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setSelected(1)
    }
    // 角色检查
    const userInfo = wx.getStorageSync('userInfo')
    const isStudent = userInfo && userInfo.roleCode === 'STUDENT'
    this.setData({ isStudent })
    // 从全局获取学期信息
    const app = getApp()
    if (app.globalData.terms && app.globalData.terms.length > 0) {
      const termIndex = app.globalData.termIndex || 0
      this.setData({
        terms: app.globalData.terms,
        termIndex
      })
    }
  },

  initWeeks() {
    const weeks = []
    for (let i = 1; i <= 20; i++) {
      weeks.push(i)
    }
    // 设置当前星期
    const now = new Date()
    const dayIndex = (now.getDay() || 7) - 1 // 转换为0-6索引
    this.setData({ weeks, dayIndex })
  },

  async initData() {
    try {
      // 并行获取楼栋和时间段
      const [buildings, timeSlots] = await Promise.all([
        api.getBuildings(),
        api.getTimeSlots()
      ])

      const timeSlotIds = timeSlots.map(t => t.id)
      this.setData({ buildings, timeSlotIds })

      if (buildings.length > 0) {
        this.loadRooms()
      } else {
        this.setData({ loading: false })
      }
    } catch (err) {
      console.error('初始化失败:', err)
      this.setData({ loading: false })
      this.useMockData()
    }
  },

  useMockData() {
    const mockBuildings = [
      { id: 1, name: '实训楼A' },
      { id: 2, name: '实训楼B' }
    ]
    const mockLabs = [
      {
        id: 1, code: 'Lab-101', buildingId: 1, seats: 40,
        roomType: '机房', isAvailable: true, conflicts: []
      },
      {
        id: 2, code: 'Lab-102', buildingId: 1, seats: 30,
        roomType: '实验室', isAvailable: false,
        conflicts: [
          { courseName: 'Java程序设计', teacherName: '张老师', timeSlotName: '第1-2节' },
          { courseName: '数据结构', teacherName: '李老师', timeSlotName: '第5-6节' }
        ]
      },
      {
        id: 3, code: 'Lab-201', buildingId: 2, seats: 50,
        roomType: '多媒体教室', isAvailable: true, conflicts: []
      }
    ]
    this.setData({
      buildings: mockBuildings,
      labList: mockLabs,
      filteredList: mockLabs,
      loading: false
    })
  },

  async loadRooms() {
    const { buildings, buildingIndex, terms, termIndex, weekIndex, dayIndex } = this.data
    const buildingId = buildings[buildingIndex]?.id
    const buildingName = buildings[buildingIndex]?.name

    if (!buildingId || !buildingName) return

    this.setData({ loading: true })

    try {
      // 获取学期信息
      let termId = 1
      let weekNo = parseInt(weekIndex, 10) + 1

      if (terms && terms.length > 0) {
        termId = terms[termIndex]?.id || 1
      } else {
        // 如果没有学期信息，尝试获取
        const app = getApp()
        if (app.globalData.terms && app.globalData.terms.length > 0) {
          this.setData({
            terms: app.globalData.terms,
            termIndex: app.globalData.termIndex || 0
          })
          termId = app.globalData.currentTerm?.id || 1
        } else {
          const fetchedTerms = await api.getTerms()
          if (fetchedTerms && fetchedTerms.length > 0) {
            const currentIndex = fetchedTerms.findIndex(t => t.status === '1')
            const idx = currentIndex >= 0 ? currentIndex : 0
            this.setData({ terms: fetchedTerms, termIndex: idx })
            termId = fetchedTerms[idx].id
          }
        }
      }

      // 获取课表矩阵数据（一次请求获取整个楼栋）
      const queryParams = {
        termId: termId,
        weekNo: weekNo,
        buildingName: buildingName
      }

      console.log('请求课表矩阵参数:', queryParams)

      const timetableData = await api.getTimetableMatrix(queryParams)

      console.log('课表矩阵返回数据:', timetableData)

      // 获取房间列表
      const rooms = await api.getRooms(buildingId)

      // 当前选择的星期索引（0-6）
      const currentDayIndex = parseInt(dayIndex, 10)

      // 从矩阵中提取当天的所有课程数据
      const dayData = timetableData.matrix?.[currentDayIndex] || []

      // 收集当天所有被占用的房间和对应的课程
      const roomOccupancyMap = {} // roomNumber -> { conflicts: [], available: false }

      dayData.forEach((slotItems, slotIndex) => {
        if (slotItems && Array.isArray(slotItems)) {
          slotItems.forEach(item => {
            const roomNum = item.roomNumber
            if (roomNum) {
              if (!roomOccupancyMap[roomNum]) {
                roomOccupancyMap[roomNum] = { conflicts: [], available: false }
              }
              roomOccupancyMap[roomNum].conflicts.push({
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

      // 组装每个房间的数据
      const labList = rooms.map(room => {
        const occupancy = roomOccupancyMap[room.code]
        if (occupancy) {
          return {
            ...room,
            isAvailable: false,
            conflicts: occupancy.conflicts
          }
        } else {
          return {
            ...room,
            isAvailable: true,
            conflicts: []
          }
        }
      })

      this.setData({ labList, loading: false })
      this.filterList()
    } catch (err) {
      console.error('加载房间失败:', err)
      this.setData({ loading: false })
      this.useMockData()
    }
  },

  // 楼栋切换
  onBuildingChange(e) {
    this.setData({ buildingIndex: e.detail.value })
    this.loadRooms()
  },

  // 学期切换
  onTermChange(e) {
    this.setData({ termIndex: e.detail.value })
    this.loadRooms()
  },

  // 周次切换
  onWeekChange(e) {
    this.setData({ weekIndex: parseInt(e.detail.value, 10) })
    this.loadRooms()
  },

  // 星期切换
  onDayChange(e) {
    this.setData({ dayIndex: parseInt(e.detail.value, 10) })
    this.loadRooms()
  },

  // 搜索输入
  onSearchInput(e) {
    this.setData({ searchText: e.detail.value })
    this.filterList()
  },

  // 清空搜索
  onSearchClear() {
    this.setData({ searchText: '' })
    this.filterList()
  },

  // 筛选类型切换
  onFilterChange(e) {
    const type = e.currentTarget.dataset.type
    this.setData({ filterType: type })
    this.filterList()
  },

  // 过滤列表
  filterList() {
    const { labList, searchText, filterType } = this.data

    let filtered = [...labList]

    // 搜索过滤
    if (searchText) {
      const keyword = searchText.toLowerCase()
      filtered = filtered.filter(lab =>
        lab.code.toLowerCase().includes(keyword)
      )
    }

    // 状态过滤
    if (filterType === 'free') {
      filtered = filtered.filter(lab => lab.isAvailable)
    } else if (filterType === 'busy') {
      filtered = filtered.filter(lab => !lab.isAvailable)
    }

    this.setData({ filteredList: filtered })
  },

  // 跳转到详情页
  onLabTap(e) {
    const labId = e.currentTarget.dataset.id
    const buildingId = this.data.buildings[this.data.buildingIndex]?.id
    const buildingName = this.data.buildings[this.data.buildingIndex]?.name
    const { termIndex, weekIndex, dayIndex } = this.data
    wx.navigateTo({
      url: `/pages/lab/detail?id=${labId}&buildingId=${buildingId}&buildingName=${encodeURIComponent(buildingName)}&termIndex=${termIndex}&weekIndex=${weekIndex}&dayIndex=${dayIndex}`
    })
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.loadRooms().then(() => {
      wx.stopPullDownRefresh()
    })
  }
})
