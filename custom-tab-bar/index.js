Component({
  data: {
    selected: 0,
    list: [
      { pagePath: "/pages/index/index", text: "首页" },
      { pagePath: "/pages/lab/list", text: "实验室" },
      { pagePath: "/pages/course/list", text: "课程" },
      { pagePath: "/pages/message/list", text: "消息" },
      { pagePath: "/pages/mine/mine", text: "我的" }
    ]
  },

  methods: {
    switchTab(e) {
      const index = e.currentTarget.dataset.index
      const item = this.data.list[index]
      wx.switchTab({ url: item.pagePath })
    },

    setSelected(index) {
      if (this.data.selected !== index) {
        this.setData({ selected: index })
      }
    }
  }
})
