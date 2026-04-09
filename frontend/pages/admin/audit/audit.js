const { request } = require('../../../utils/request.js');

function mapStatusText(status) {
  const statusMap = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝',
    3: '已使用',
    4: '已取消',
    5: '已失效'
  };
  return statusMap[status] || '未知';
}

Page({
  data: {
    mode: 'pending', // pending / all
    keyword: '',
    loading: false,
    batching: false,
    batchOpinion: '',

    list: [],
    filteredList: [],
    selectedIds: []
  },

  onShow() {
    this.loadData();
  },

  onPullDownRefresh() {
    this.loadData().finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  switchMode(e) {
    const mode = e.currentTarget.dataset.mode;
    if (mode === this.data.mode) return;
    this.setData({
      mode,
      selectedIds: [],
      batchOpinion: ''
    });
    this.loadData();
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
    const url = this.data.mode === 'pending'
      ? '/admin/reservation/pending'
      : '/admin/reservation/list';

    return request({ url, method: 'GET' })
      .then((res) => {
        const list = (res || []).map((it) => ({
          ...it,
          statusText: mapStatusText(it.status),
          selected: false
        }));
        this.setData({
          list,
          selectedIds: [],
          batchOpinion: ''
        });
        this.applyFilter(list);
      })
      .catch((err) => {
        console.error('加载审核列表失败', err);
        this.setData({ list: [], filteredList: [], selectedIds: [] });
      })
      .finally(() => {
        this.setData({ loading: false });
      });
  },

  applyFilter(listData) {
    const list = listData || this.data.list;
    const kw = (this.data.keyword || '').toLowerCase();
    if (!kw) {
      this.setData({ filteredList: list });
      return;
    }
    const filteredList = list.filter((x) => {
      const a = `${x.realName || ''}`.toLowerCase();
      const b = `${x.deviceName || ''}`.toLowerCase();
      const c = `${x.reason || ''}`.toLowerCase();
      return a.includes(kw) || b.includes(kw) || c.includes(kw);
    });
    this.setData({ filteredList });
  },

  toggleSelect(e) {
    const id = Number(e.currentTarget.dataset.id);
    if (!id) return;
    const selectedIds = new Set(this.data.selectedIds.map((x) => Number(x)));
    if (selectedIds.has(id)) selectedIds.delete(id);
    else selectedIds.add(id);

    const list = this.data.list.map((it) => (
      Number(it.id) === id ? { ...it, selected: selectedIds.has(id) } : it
    ));

    this.setData({
      list,
      selectedIds: Array.from(selectedIds)
    });
    this.applyFilter(list);
  },

  clearSelected() {
    const list = this.data.list.map((it) => ({ ...it, selected: false }));
    this.setData({
      list,
      selectedIds: [],
      batchOpinion: ''
    });
    this.applyFilter(list);
  },

  handleBatchOpinionInput(e) {
    this.setData({ batchOpinion: e.detail.value });
  },

  quickApprove(e) {
    const id = Number(e.currentTarget.dataset.id);
    this.auditOne(id, 1);
  },

  quickReject(e) {
    const id = Number(e.currentTarget.dataset.id);
    wx.showModal({
      title: '拒绝原因',
      editable: true,
      placeholderText: '请输入审核意见（选填）',
      success: (res) => {
        if (!res.confirm) return;
        this.auditOne(id, 2, res.content || '');
      }
    });
  },

  auditOne(id, status, auditOpinion) {
    wx.showLoading({ title: '处理中...' });
    return request({
      url: '/admin/reservation/audit',
      method: 'POST',
      data: {
        id,
        status,
        auditOpinion: auditOpinion || ''
      }
    }).then(() => {
      wx.showToast({ title: '操作成功', icon: 'success' });
      this.loadData();
    }).finally(() => {
      wx.hideLoading();
    });
  },

  batchApprove() {
    this.batchAudit(1);
  },

  batchReject() {
    if (this.data.batching) return;
    wx.showModal({
      title: '确认批量拒绝',
      content: `将拒绝已选 ${this.data.selectedIds.length} 条预约，是否继续？`,
      success: (res) => {
        if (!res.confirm) return;
        this.batchAudit(2);
      }
    });
  },

  batchAudit(status) {
    if (this.data.batching) return;
    const ids = this.data.selectedIds;
    if (!ids || ids.length === 0) return;
    this.setData({ batching: true });
    wx.showLoading({ title: '批量处理中...' });

    request({
      url: '/admin/reservation/batch-audit',
      method: 'POST',
      data: {
        ids,
        status,
        auditOpinion: this.data.batchOpinion || ''
      }
    }).then(() => {
      wx.showToast({ title: '批量完成', icon: 'success' });
      this.loadData();
    }).finally(() => {
      wx.hideLoading();
      this.setData({ batching: false });
    });
  }
});