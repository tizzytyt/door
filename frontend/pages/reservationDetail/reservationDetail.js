const { request } = require('../../utils/request.js');

Page({
  data: {
    detail: null,
    statusText: '',
    statusMap: {
      0: '待审核',
      1: '已通过',
      2: '已拒绝',
      3: '已使用',
      4: '已取消',
      5: '已失效'
    }
  },

  onLoad(options) {
    if (options.data) {
      try {
        const detail = JSON.parse(decodeURIComponent(options.data));
        const statusText = this.data.statusMap[detail.status] || '';
        this.setData({ detail, statusText });
      } catch (e) {
        console.error('解析预约详情失败', e);
      }
    }
  },

  handleCancel() {
    const { detail } = this.data;
    if (!detail || detail.status !== 0) return;
    wx.showModal({
      title: '提示',
      content: '确定要取消该预约吗？',
      success: (res) => {
        if (res.confirm) {
          request({
            url: `/student/reservation/cancel/${detail.id}`,
            method: 'POST'
          }).then(() => {
            wx.showToast({ title: '已取消', icon: 'success' });
            wx.navigateBack();
          });
        }
      }
    });
  },

  handleUse() {
    const { detail } = this.data;
    if (!detail || detail.status !== 1) return;
    wx.showModal({
      title: '提示',
      content: '确认现在使用门禁吗？',
      success: (res) => {
        if (res.confirm) {
          request({
            url: `/student/reservation/confirm/${detail.id}`,
            method: 'POST'
          }).then(() => {
            wx.showToast({ title: '使用成功', icon: 'success' });
            wx.navigateBack();
          });
        }
      }
    });
  }
})
