const { request } = require('../../../utils/request.js');

Page({
  data: {
    role: '',
    stats: {
      totalReservations: 0,
      pendingCount: 0,
      pendingReportCount: 0,
      successCount: 0,
      deviceCount: 0,
      userCount: 0
    }
  },

  onShow() {
    const user = wx.getStorageSync('user');
    this.setData({ role: (user && user.role) || '' });
    this.loadStats();
  },

  loadStats() {
    request({
      url: '/admin/dashboard/stats',
      method: 'GET'
    }).then(res => {
      this.setData({ stats: res || this.data.stats });
    }).catch(err => {
      console.error('加载统计数据失败', err);
    });
  },

  navTo(e) {
    const url = e.currentTarget.dataset.url;
    wx.navigateTo({ url });
  }
});

