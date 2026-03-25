const { request } = require('../../utils/request.js');

function pad2(n) {
  return `${n}`.padStart(2, '0');
}

function addMinutes(hhmm, mins) {
  const [h, m] = hhmm.split(':').map((x) => parseInt(x, 10));
  const total = h * 60 + m + mins;
  const nh = Math.floor((total + 24 * 60) % (24 * 60) / 60);
  const nm = (total + 24 * 60) % 60;
  return `${pad2(nh)}:${pad2(nm)}`;
}

function normalizeTime(t) {
  if (!t) return '';
  // 可能是 "HH:mm:ss" 或 "HH:mm"
  return t.length >= 5 ? t.slice(0, 5) : t;
}

Page({
  data: {
    viewTab: 1, // 0-提交预约 1-预约记录（默认记录）
    name: '',
    phone: '',
    reason: '',
    date: '',
    startTime: '',
    endTime: '',
    list: [],
    currentTab: -1,
    filteredList: [],
    submitting: false,
    listLoading: false
  },

  onLoad() {
    const now = new Date();
    const y = now.getFullYear();
    const m = (now.getMonth() + 1).toString().padStart(2, '0');
    const d = now.getDate().toString().padStart(2, '0');
    const hh = now.getHours().toString().padStart(2, '0');
    const mm = now.getMinutes().toString().padStart(2, '0');
    const start = `${hh}:${mm}`;
    this.setData({
      date: `${y}-${m}-${d}`,
      startTime: start,
      endTime: addMinutes(start, 30)
    });
  },

  onShow() {
    this.loadData();
  },

  onPullDownRefresh() {
    this.loadData().finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  loadData() {
    this.setData({ listLoading: true });
    return request({
      url: '/student/reservation/visitor/list',
      method: 'GET'
    }).then(res => {
      const list = (res || []).map((it) => {
        const statusMap = {
          0: '待审核',
          1: '已通过',
          2: '已拒绝',
          3: '已取消'
        };
        return {
          ...it,
          date: it.visitDate,
          reason: it.visitReason,
          startTimeText: normalizeTime(it.startTime),
          endTimeText: normalizeTime(it.endTime),
          statusText: statusMap[it.status] || '未知状态'
        };
      });
      this.setData({ list });
      this.filterList(list);
    }).catch(err => {
      console.error('加载访客预约失败', err);
      this.setData({ list: [], filteredList: [] });
    }).finally(() => {
      this.setData({ listLoading: false });
    });
  },

  switchViewTab(e) {
    const tab = parseInt(e.currentTarget.dataset.tab, 10);
    this.setData({ viewTab: tab });
  },

  switchTab(e) {
    const status = parseInt(e.currentTarget.dataset.status, 10);
    this.setData({ currentTab: status });
    this.filterList();
  },

  filterList(listData) {
    const list = listData || this.data.list;
    const { currentTab } = this.data;
    const filteredList = currentTab === -1 ? list : list.filter((x) => x.status === currentTab);
    this.setData({ filteredList });
  },

  handleInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [field]: e.detail.value });
  },

  handleDateChange(e) {
    this.setData({ date: e.detail.value });
  },

  handleStartTimeChange(e) {
    const startTime = e.detail.value;
    const endTime = this.data.endTime && this.data.endTime > startTime
      ? this.data.endTime
      : addMinutes(startTime, 30);
    this.setData({ startTime, endTime });
  },

  handleEndTimeChange(e) {
    this.setData({ endTime: e.detail.value });
  },

  validatePhone(phone) {
    // 简单手机号校验（大陆 11 位）
    return /^1\d{10}$/.test(phone);
  },

  handleSubmit() {
    if (this.data.submitting) return;
    const { name, phone, reason, date, startTime, endTime } = this.data;
    if (!name) return wx.showToast({ title: '请输入访客姓名', icon: 'none' });
    if (!phone) return wx.showToast({ title: '请输入访客电话', icon: 'none' });
    if (!this.validatePhone(phone)) return wx.showToast({ title: '手机号格式不正确', icon: 'none' });
    if (!reason) return wx.showToast({ title: '请输入来访事由', icon: 'none' });
    if (startTime >= endTime) return wx.showToast({ title: '结束时间必须晚于开始时间', icon: 'none' });

    this.setData({ submitting: true });
    wx.showLoading({ title: '提交中...' });
    request({
      url: '/student/reservation/visitor/submit',
      method: 'POST',
      data: {
        visitorName: name,
        visitorPhone: phone,
        // 与后端实体字段对齐
        visitReason: reason,
        visitDate: date,
        startTime: startTime + ':00',
        endTime: endTime + ':00'
      }
    }).then(() => {
      wx.showToast({ title: '提交成功', icon: 'success' });
      this.setData({ reason: '' });
      this.loadData();
      this.setData({ viewTab: 1 }); // 提交成功后切回记录
    }).finally(() => {
      wx.hideLoading();
      this.setData({ submitting: false });
    });
  },

  handleCancel(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '提示',
      content: '确定要取消该访客预约吗？',
      success: (res) => {
        if (!res.confirm) return;
        wx.showLoading({ title: '取消中...' });
        request({
          url: `/student/reservation/visitor/cancel/${id}`,
          method: 'POST'
        }).then(() => {
          wx.showToast({ title: '已取消', icon: 'success' });
          this.loadData();
        }).finally(() => {
          wx.hideLoading();
        });
      }
    });
  }
})
