const { request } = require('../../../utils/request.js');

function formatExpectedTime(dtStr) {
  if (!dtStr) return { date: '', time: '' };
  const s = dtStr.replace('T', ' ');
  const [date, timeFull] = s.split(' ');
  const time = (timeFull || '').slice(0, 5);
  return { date, time };
}

function mapItem(it) {
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
    statusText: statusMap[it.status] || '未知',
    dateText: t.date,
    timeText: t.time
  };
}

Page({
  data: {
    mode: 'pending',
    keyword: '',
    loading: false,
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

  switchMode(e) {
    const mode = e.currentTarget.dataset.mode;
    if (mode === this.data.mode) return;
    this.setData({ mode, keyword: '' });
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
    const url = this.data.mode === 'pending' ? '/admin/report/pending' : '/admin/report/list';
    return request({ url, method: 'GET' })
      .then((res) => {
        const list = (res || []).map(mapItem);
        this.setData({ list });
        this.applyFilter(list);
      })
      .catch((err) => {
        console.error('加载报备列表失败', err);
        this.setData({ list: [], filteredList: [] });
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
      const b = `${x.username || ''}`.toLowerCase();
      const c = `${x.reason || ''}`.toLowerCase();
      const d = `${x.typeText || ''}`.toLowerCase();
      return a.includes(kw) || b.includes(kw) || c.includes(kw) || d.includes(kw);
    });
    this.setData({ filteredList });
  },

  quickApprove(e) {
    const id = Number(e.currentTarget.dataset.id);
    this.auditOne(id, 1, '');
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
      url: '/admin/report/audit',
      method: 'POST',
      data: {
        id,
        status,
        auditOpinion: auditOpinion || ''
      }
    })
      .then(() => {
        wx.showToast({ title: '操作成功', icon: 'success' });
        this.loadData();
      })
      .finally(() => {
        wx.hideLoading();
      });
  }
});
