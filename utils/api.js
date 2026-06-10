const app = getApp()

const BASE_URL = 'http://localhost:8083'

/**
 * 处理数组参数，转换为 Spring 可识别的格式
 */
const serializeParams = (data) => {
  if (!data || typeof data !== 'object') return ''

  const parts = []
  for (const key of Object.keys(data)) {
    const value = data[key]
    if (Array.isArray(value)) {
      // 数组转为 key=val1&key=val2 格式
      value.forEach(item => {
        parts.push(`${encodeURIComponent(key)}=${encodeURIComponent(item)}`)
      })
    } else if (value !== undefined && value !== null) {
      parts.push(`${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    }
  }
  return parts.join('&')
}

/**
 * 封装请求方法
 */
const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token')

    // 处理 GET 请求的数组参数
    let url = `${BASE_URL}${options.url}`
    if (options.method === 'GET' || !options.method) {
      const params = serializeParams(options.data)
      if (params) {
        url += (url.includes('?') ? '&' : '?') + params
      }
    }

    console.log('请求URL:', url) // 调试用

    wx.request({
      url,
      method: options.method || 'GET',
      data: options.method && options.method !== 'GET' ? options.data : {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      success: (res) => {
        if (res.statusCode === 200) {
          resolve(res.data)
        } else if (res.statusCode === 401) {
          wx.showToast({ title: '请先登录', icon: 'none' })
          wx.navigateTo({ url: '/pages/login/login' })
          reject(new Error('未授权'))
        } else {
          wx.showToast({ title: '请求失败', icon: 'none' })
          reject(new Error(res.data?.message || '请求失败'))
        }
      },
      fail: (err) => {
        wx.showToast({ title: '网络错误', icon: 'none' })
        reject(err)
      }
    })
  })
}

/**
 * 获取楼栋列表
 */
const getBuildings = () => {
  return request({ url: '/api/buildings' })
}

/**
 * 获取房间列表
 */
const getRooms = (buildingId) => {
  return request({
    url: '/api/rooms',
    data: { buildingId }
  })
}

/**
 * 获取实验室空闲状态
 */
const getLabAvailability = (params) => {
  return request({
    url: '/api/schedule-reservations/labs/availability',
    data: params
  })
}

/**
 * 获取课表矩阵数据（按天+节次组织）
 */
const getTimetableMatrix = (params) => {
  return request({
    url: '/api/timetable/matrix',
    data: params
  })
}

/**
 * 获取学期列表
 */
const getTerms = () => {
  return request({ url: '/api/terms' })
}

/**
 * 获取时间段列表
 */
const getTimeSlots = () => {
  return request({ url: '/api/time-slots' })
}

module.exports = {
  request,
  getBuildings,
  getRooms,
  getLabAvailability,
  getTimetableMatrix,
  getTerms,
  getTimeSlots
}
