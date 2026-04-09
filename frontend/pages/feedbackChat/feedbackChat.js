const { request } = require('../../utils/request.js');

function formatDateTime(dtStr) {
  if (!dtStr) return '';
  const s = `${dtStr}`.replace('T', ' ');
  return s.slice(0, 16);
}

function statusText(status) {
  const map = {
    0: '待处理',
    1: '处理中',
    2: '已完成'
  };
  return map[status] || '未知';
}

Page({
  data: {
    feedbackId: 0,
    detail: {},
    messages: [],

    // 学生/管理员聊天输入
    content: '',
    sending: false,

    // 管理员更新进度输入
    statusRemark: '',

    role: '',
    currentUserId: null,

    loading: false
  },

  onLoad(options) {
    const user = wx.getStorageSync('user') || {};
    const role = user.role || '';
    const currentUserId = user.id ? Number(user.id) : null;
    const feedbackId = options && options.feedbackId ? Number(options.feedbackId) : 0;

    this.setData({
      role,
      currentUserId,
      feedbackId,
      loading: false
    });

    this.loadDetail();
    this.loadMessages();
  },

  onShow() {
    // 回到页面时刷新，避免状态已更新但消息未拉取
    this.loadMessages();
  },

  loadDetail() {
    if (!this.data.feedbackId) return;
    return request({
      url: `/feedback/chat/${this.data.feedbackId}`,
      method: 'GET'
    }).then((res) => {
      const d = res || {};
      d.statusText = statusText(d.status);
      this.setData({ detail: d });
    }).catch(() => {
      this.setData({ detail: {} });
    });
  },

  loadMessages() {
    if (!this.data.feedbackId) return;
    return request({
      url: `/feedback/chat/messages/${this.data.feedbackId}`,
      method: 'GET'
    }).then((res) => {
      const list = (res || []).map((m) => {
        const senderUserId = m.senderUserId != null ? Number(m.senderUserId) : null;
        return {
          ...m,
          senderUserId,
          isMe: this.data.currentUserId != null && senderUserId === this.data.currentUserId,
          createdAtText: formatDateTime(m.createdAt || m.createTime)
        };
      });
      this.setData({ messages: list });
    });
  },

  handleSend() {
    if (this.data.sending) return;
    const text = (this.data.content || '').trim();
    if (!text) return wx.showToast({ title: '请输入消息', icon: 'none' });
    if (!this.data.feedbackId) return wx.showToast({ title: '反馈ID无效', icon: 'none' });

    this.setData({ sending: true });
    wx.showLoading({ title: '发送中...' });
    return request({
      url: `/feedback/chat/send/${this.data.feedbackId}`,
      method: 'POST',
      data: { content: text }
    }).then(() => {
      this.setData({ content: '' });
      return this.loadMessages();
    }).finally(() => {
      wx.hideLoading();
      this.setData({ sending: false });
    });
  },

  handleStatusRemarkInput(e) {
    this.setData({ statusRemark: e.detail.value });
  },

  handleContentInput(e) {
    this.setData({ content: e.detail.value });
  },

  handleAdminUpdate(e) {
    const status = Number(e.currentTarget.dataset.status);
    if (this.data.role === 'student') return;
    if (this.data.sending) return;

    const replyText = (this.data.statusRemark || '').trim();
    const adminReply = replyText || (status === 1 ? '开始处理' : '已标记完成');
    if (!adminReply) return wx.showToast({ title: '请输入处理说明', icon: 'none' });

    this.setData({ sending: true });
    wx.showLoading({ title: '更新进度中...' });

    return request({
      url: '/admin/feedback/update',
      method: 'POST',
      data: {
        id: this.data.feedbackId,
        status,
        adminReply
      }
    }).then(() => {
      // 把进度说明也作为聊天消息发送，便于学生端在对话里看到
      return request({
        url: `/feedback/chat/send/${this.data.feedbackId}`,
        method: 'POST',
        data: { content: adminReply }
      });
    }).then(() => {
      this.setData({ statusRemark: '' });
      return Promise.all([this.loadDetail(), this.loadMessages()]);
    }).then(() => {
      wx.showToast({ title: '更新成功', icon: 'success' });
    }).finally(() => {
      wx.hideLoading();
      this.setData({ sending: false });
    });
  }
});

