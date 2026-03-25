const { request } = require('../../utils/request.js');

Page({
  data: {
    deviceIndex: -1,
    devices: [],
    date: '',
    startDate: '',
    endDate: '',
    startTime: '',
    endTime: '',
    reason: '',
    loading: false
  },

  onLoad(options) {
    this.initDates();
    this.loadDevices(options.deviceId);
  },

  initDates() {
    const today = new Date();
    const maxDate = new Date();
    maxDate.setDate(today.getDate() + 7); // 可预约未来7天

    this.setData({
      startDate: this.formatDate(today),
      endDate: this.formatDate(maxDate),
      date: this.formatDate(today)
    });
  },

  formatDate(date) {
    const y = date.getFullYear();
    const m = (date.getMonth() + 1).toString().padStart(2, '0');
    const d = date.getDate().toString().padStart(2, '0');
    return `${y}-${m}-${d}`;
  },

  loadDevices(preSelectedId) {
    request({
      url: '/student/device/list',
      method: 'GET'
    }).then(res => {
      this.setData({ devices: res || [] });
      if (preSelectedId) {
        const index = res.findIndex(d => d.id == preSelectedId);
        if (index > -1) {
          this.setData({ deviceIndex: index });
        }
      }
    });
  },

  handleDeviceChange(e) {
    this.setData({ deviceIndex: e.detail.value });
  },

  handleDateChange(e) {
    this.setData({ date: e.detail.value });
  },

  handleStartTimeChange(e) {
    this.setData({ startTime: e.detail.value });
  },

  handleEndTimeChange(e) {
    this.setData({ endTime: e.detail.value });
  },

  handleSubmit() {
    const { deviceIndex, devices, date, startTime, endTime, reason } = this.data;

    if (deviceIndex < 0) return this.toast('请选择门禁设备');
    if (!date) return this.toast('请选择预约日期');
    if (!startTime) return this.toast('请选择开始时间');
    if (!endTime) return this.toast('请选择结束时间');
    if (startTime >= endTime) return this.toast('结束时间必须晚于开始时间');
    if (!reason) return this.toast('请输入申请事由');

    this.setData({ loading: true });

    request({
      url: '/student/reservation/submit',
      method: 'POST',
      data: {
        deviceId: devices[deviceIndex].id,
        reservationDate: date,
        startTime: startTime + ':00', // 后端可能需要秒
        endTime: endTime + ':00',
        reason: reason
      }
    }).then(() => {
      wx.showToast({ title: '提交成功', icon: 'success' });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    }).catch(err => {
      this.toast(err || '提交失败');
    }).finally(() => {
      this.setData({ loading: false });
    });
  },

  toast(msg) {
    wx.showToast({ title: msg, icon: 'none' });
  }
})
