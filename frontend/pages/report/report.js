const { request } = require('../../utils/request.js');

function pad2(n) {
  return `${n}`.padStart(2, '0');
}

function formatExpectedTime(dtStr) {
  if (!dtStr) return { date: '', time: '' };
  // 兼容 "YYYY-MM-DDTHH:mm:ss" / "YYYY-MM-DD HH:mm:ss"
  const s = dtStr.replace('T', ' ');
  const [date, timeFull] = s.split(' ');
  const time = (timeFull || '').slice(0, 5);
  return { date, time };
}

Page({
  data: {
    typeIndex: 0,
    types: ['晚归', '外出'],
    date: '',
    time: '',
    reason: '',
    list: [],
    currentTab: -1,
    filteredList: [],
    submitting: false
  },

  onLoad() {
    const now = new Date();
    const y = now.getFullYear();
    const m = (now.getMonth() + 1).toString().padStart(2, '0');
    const d = now.getDate().toString().padStart(2, '0');
    const hh = now.getHours().toString().padStart(2, '0');
    const mm = now.getMinutes().toString().padStart(2, '0');
    this.setData({
      date: `${y}-${m}-${d}`,
      time: `${hh}:${mm}`
    });
  },

  onShow() {
    this.loadData();
  },

  loadData() {
    request({
      url: '/student/reservation/report/list',
      method: 'GET'
    }).then(res => {
      const list = (res || []).map((it) => {
        const typeText = it.type === 1 ? '晚归' : it.type === 2 ? '外出' : '未知';
        const statusMap = {
          0: '待审核',
          1: '已通过',
          2: '已拒绝',
          3: '已撤销'
        };
        const t = formatExpectedTime(it.expectedTime);
        return {
          ...it,
          typeText,
          statusText: statusMap[it.status] || '未知状态',
          dateText: t.date,
          timeText: t.time
        };
      });
      this.setData({ list });
      this.filterList(list);
    }).catch(err => {
      console.error('加载报备记录失败', err);
    });
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

  handleTypeChange(e) {
    this.setData({ typeIndex: parseInt(e.detail.value, 10) });
  },

  handleDateChange(e) {
    this.setData({ date: e.detail.value });
  },

  handleTimeChange(e) {
    this.setData({ time: e.detail.value });
  },

  handleReasonInput(e) {
    this.setData({ reason: e.detail.value });
  },

  handleSubmit() {
    if (this.data.submitting) return;
    const { typeIndex, date, time, reason } = this.data;
    if (!reason) {
      return wx.showToast({ title: '请输入报备原因', icon: 'none' });
    }
    // 后端 Report.type 为数值：1-晚归, 2-临时外出
    const typeCode = typeIndex === 0 ? 1 : 2;
    // 后端 expectedTime 为 LocalDateTime，这里拼接 ISO 字符串
    const expectedTime = `${date}T${time}:00`;

    this.setData({ submitting: true });
    wx.showLoading({ title: '提交中...' });
    request({
      url: '/student/reservation/report/submit',
      method: 'POST',
      data: {
        type: typeCode,
        expectedTime,
        reason
      }
    }).then(() => {
      wx.showToast({ title: '提交成功', icon: 'success' });
      this.setData({ reason: '' });
      this.loadData();
    }).finally(() => {
      wx.hideLoading();
      this.setData({ submitting: false });
    });
  },

  handleCancel(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '提示',
      content: '确定要撤销该报备吗？',
      success: (res) => {
        if (!res.confirm) return;
        wx.showLoading({ title: '撤销中...' });
        request({
          url: `/student/reservation/report/cancel/${id}`,
          method: 'POST'
        }).then(() => {
          wx.showToast({ title: '已撤销', icon: 'success' });
          this.loadData();
        }).finally(() => {
          wx.hideLoading();
        });
      }
    });
  }
})
