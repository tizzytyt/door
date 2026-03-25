const { request } = require('../../../utils/request.js');

function formatDateTime(dtStr) {
  if (!dtStr) return '';
  // 兼容 "YYYY-MM-DDTHH:mm:ss" / "YYYY-MM-DD HH:mm:ss"
  const s = `${dtStr}`.replace('T', ' ');
  return s.slice(0, 16);
}

function mapStatusText(status) {
  const statusMap = {
    0: '待处理',
    1: '处理中',
    2: '已完成'
  };
  return statusMap[status] || '未知';
}

Page({
  data: {
    keyword: '',
    loading: false,
    statusTab: 0, // 0全部 1待处理 2处理中 3已完成
    list: [],
    filteredList: []
  },

  onShow() {
    this.loadData();
  },

  onPullDownRefresh() {
    this.loadData().finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  switchStatusTab(e) {
    const statusTab = parseInt(e.currentTarget.dataset.tab, 10);
    if (statusTab === this.data.statusTab) return;
    this.setData({ statusTab });
    this.applyFilter();
  },

  handleKeywordInput(e) {
    const keyword = (e.detail.value || '').trim();
    this.setData({ keyword });
    this.applyFilter();
  },

  clearKeyword() {
    this.setData({ keyword: '' });
    this.applyFilter();
  },

  loadData() {
    this.setData({ loading: true });
    return request({ url: '/admin/feedback/list', method: 'GET' })
      .then((res) => {
        const raw = res || [];
        // 设备维护对应：报修(type=1)
        const list = raw
          .filter((it) => it.type === 1)
          .map((it) => ({
            ...it,
            createdAtText: formatDateTime(it.createdAt || it.createTime),
            statusText: mapStatusText(it.status),
            hasReply: !!(it.adminReply && it.adminReply.trim())
          }));
        this.setData({ list, filteredList: [] });
        this.applyFilter(list);
      })
      .catch((err) => {
        console.error('加载报修记录失败', err);
        this.setData({ list: [], filteredList: [] });
      })
      .finally(() => {
        this.setData({ loading: false });
      });
  },

  applyFilter(listData) {
    const list = listData || this.data.list;
    const kw = (this.data.keyword || '').toLowerCase();
    const { statusTab } = this.data;

    let filtered = list;
    if (statusTab !== 0) {
      const statusCode = statusTab === 1 ? 0 : statusTab === 2 ? 1 : 2;
      filtered = filtered.filter((x) => x.status === statusCode);
    }

    if (kw) {
      filtered = filtered.filter((x) => {
        const a = `${x.content || ''}`.toLowerCase();
        return a.includes(kw);
      });
    }

    this.setData({ filteredList: filtered });
  },

  handleStart(e) {
    const id = e.currentTarget.dataset.id;
    this.openReplyModal(id, 1, '开始处理');
  },

  handleDone(e) {
    const id = e.currentTarget.dataset.id;
    this.openReplyModal(id, 2, '标记完成');
  },

  openReplyModal(id, status, title) {
    wx.showModal({
      title,
      editable: true,
      placeholderText: '管理员回复（可选）',
      success: (res) => {
        if (!res.confirm) return;
        const adminReply = res.content || '';
        this.updateFeedback(id, status, adminReply);
      }
    });
  },

  updateFeedback(id, status, adminReply) {
    wx.showLoading({ title: '处理中...' });
    return request({
      url: '/admin/feedback/update',
      method: 'POST',
      data: {
        id,
        status,
        adminReply
      }
    }).then(() => {
      wx.showToast({ title: '操作成功', icon: 'success' });
      this.loadData();
    }).finally(() => {
      wx.hideLoading();
    });
  }
});