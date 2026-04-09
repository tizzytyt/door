const { request } = require('../../utils/request.js');

function formatDateTime(dtStr) {
  if (!dtStr) return '';
  // 兼容 "YYYY-MM-DDTHH:mm:ss" / "YYYY-MM-DD HH:mm:ss"
  const s = dtStr.replace('T', ' ');
  return s.slice(0, 16);
}

Page({
  data: {
    // 提交表单
    typeIndex: 0,
    types: ['报修', '建议'],
    devices: [],
    deviceIndex: -1,
    content: '',
    contact: '',
    submitting: false,

    // 列表筛选
    typeTab: -1, // -1全部，0报修，1建议
    statusTabs: ['全部', '待处理', '处理中', '已完成'],
    statusTab: 0, // 0全部，1待处理，2处理中，3已完成

    list: [],
    filteredList: []
  },

  onShow() {
    this.loadDevices();
    this.loadData();
  },

  loadDevices() {
    return request({
      url: '/student/device/list',
      method: 'GET'
    })
      .then((res) => {
        const devices = res || [];
        const deviceIndex = devices.length > 0 ? 0 : -1;
        this.setData({ devices, deviceIndex });
      })
      .catch(() => {
        this.setData({ devices: [], deviceIndex: -1 });
      });
  },

  loadData() {
    return request({
      url: '/student/feedback/list',
      method: 'GET'
    }).then(res => {
      const list = (res || []).map((it) => {
        const typeText = it.type === 1 ? '报修' : it.type === 3 ? '建议' : it.type === 2 ? '投诉' : '未知';
        const statusText = it.status === 0 ? '待处理' : it.status === 1 ? '处理中' : it.status === 2 ? '已完成' : '未知';
        const createdAtText = formatDateTime(it.createdAt || it.createTime);
        const hasReply = !!(it.adminReply && it.adminReply.trim());
        return {
          ...it,
          typeText,
          statusText,
          createdAtText,
          hasReply,
          deviceName: it.deviceName || ''
        };
      });
      this.setData({ list });
      this.applyFilter(list);
    }).catch(err => {
      console.error('加载反馈列表失败', err);
      this.setData({ list: [], filteredList: [] });
    });
  },

  applyFilter(listData) {
    const list = listData || this.data.list;
    const { typeTab, statusTab } = this.data;

    let filtered = list;
    if (typeTab !== -1) {
      const typeCode = typeTab === 0 ? 1 : 3;
      filtered = filtered.filter((x) => x.type === typeCode);
    }
    if (statusTab !== 0) {
      const statusCode = statusTab === 1 ? 0 : statusTab === 2 ? 1 : 2;
      filtered = filtered.filter((x) => x.status === statusCode);
    }

    this.setData({ filteredList: filtered });
  },

  goChat(e) {
    const id = e.currentTarget.dataset.id;
    if (!id) return;
    wx.navigateTo({
      url: `/pages/feedbackChat/feedbackChat?feedbackId=${id}`
    });
  },

  switchTypeTab(e) {
    const tab = parseInt(e.currentTarget.dataset.tab, 10);
    this.setData({ typeTab: tab });
    this.applyFilter();
  },

  switchStatusTab(e) {
    const tab = parseInt(e.currentTarget.dataset.tab, 10);
    this.setData({ statusTab: tab });
    this.applyFilter();
  },

  handleTypeChange(e) {
    const typeIndex = parseInt(e.detail.value, 10);
    this.setData({ typeIndex });
  },

  handleDeviceChange(e) {
    this.setData({ deviceIndex: parseInt(e.detail.value, 10) });
  },

  handleInputContent(e) {
    this.setData({ content: e.detail.value });
  },

  handleInputContact(e) {
    this.setData({ contact: e.detail.value });
  },

  handleSubmit() {
    if (this.data.submitting) return;

    const { typeIndex, content, contact } = this.data;
    const trimmed = (content || '').trim();
    if (!trimmed) return wx.showToast({ title: '请输入反馈内容', icon: 'none' });

    // 先拼接联系方式再统一长度校验
    const finalContent = contact
      ? `${trimmed}\n联系方式：${contact}`
      : trimmed;

    if (finalContent.length > 500) {
      return wx.showToast({ title: '内容过长（<=500字）', icon: 'none' });
    }

    // 后端 Feedback.type: 1-报修, 3-建议
    const typeCode = typeIndex === 0 ? 1 : 3;

    if (typeCode === 1) {
      const { devices, deviceIndex } = this.data;
      if (!devices.length || deviceIndex < 0 || !devices[deviceIndex]) {
        return wx.showToast({ title: '请选择报修门禁', icon: 'none' });
      }
    }

    this.setData({ submitting: true });
    wx.showLoading({ title: '提交中...' });

    const payload = {
      type: typeCode,
      content: finalContent
    };
    if (typeCode === 1) {
      payload.deviceId = this.data.devices[this.data.deviceIndex].id;
    }

    request({
      url: '/student/feedback/submit',
      method: 'POST',
      data: payload
    }).then(() => {
      wx.showToast({ title: '提交成功', icon: 'success' });
      this.setData({ content: '', contact: '', submitting: false });
      this.loadData();
    }).catch(() => {
      this.setData({ submitting: false });
    }).finally(() => {
      wx.hideLoading();
    });
  }
});
