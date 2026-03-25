const { request } = require('../../utils/request.js');

Page({
  data: {
    currentTab: -1,
    list: [],
    filteredList: []
  },

  onShow() {
    wx.showTabBar();
    this.loadData();
  },

  loadData() {
    request({
      url: '/student/reservation/list',
      method: 'GET'
    }).then(res => {
      const list = res || [];
      // 处理状态文本
      const statusMap = {
        0: '待审核',
        1: '已通过',
        2: '已拒绝',
        3: '已使用',
        4: '已取消',
        5: '已失效'
      };
      list.forEach(item => {
        item.statusText = statusMap[item.status];
      });
      this.setData({ list });
      this.filterList(list);
    }).catch(err => {
      console.error('加载列表失败', err);
    });
  },

  switchTab(e) {
    const status = parseInt(e.currentTarget.dataset.status);
    this.setData({ currentTab: status });
    this.filterList();
  },

  filterList(listData) {
    const list = listData || this.data.list;
    const { currentTab } = this.data;
    let filtered = [];
    if (currentTab === -1) {
      filtered = list;
    } else {
      filtered = list.filter(item => item.status === currentTab);
    }
    this.setData({ filteredList: filtered });
  },

  handleCancel(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '提示',
      content: '确定要取消该预约吗？',
      success: (res) => {
        if (res.confirm) {
          request({
            url: `/student/reservation/cancel/${id}`,
            method: 'POST'
          }).then(() => {
            wx.showToast({ title: '已取消', icon: 'success' });
            this.loadData();
          });
        }
      }
    });
  },

  handleUse(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '提示',
      content: '确认现在使用门禁吗？',
      success: (res) => {
        if (res.confirm) {
          request({
            url: `/student/reservation/confirm/${id}`,
            method: 'POST'
          }).then(() => {
            wx.showToast({ title: '使用成功', icon: 'success' });
            // 成功后自动切到“已使用”，方便立刻看到状态变化
            this.setData({ currentTab: 3 });
          }).catch(() => {
            // 错误提示已在 request 内处理
          }).finally(() => {
            // 无论成功/失败都刷新，确保过期自动失效后按钮及时消失
            this.loadData();
          });
        }
      }
    });
  },

  navToDetail(e) {
    const id = e.currentTarget.dataset.id;
    const item = this.data.list.find(r => r.id === id);
    if (!item) return;
    const dataStr = encodeURIComponent(JSON.stringify(item));
    wx.navigateTo({
      url: `/pages/reservationDetail/reservationDetail?data=${dataStr}`
    });
  },

  navToCalendar() {
    wx.navigateTo({
      url: '/pages/calendar/calendar'
    });
  },

  stopProp() {}
})
