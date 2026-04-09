const { request } = require('../../../utils/request.js');

function formatDateTime(dtStr) {
  if (!dtStr) return '';
  const s = dtStr.replace('T', ' ');
  return s.slice(0, 16);
}

function todayStr() {
  const d = new Date();
  const y = d.getFullYear();
  const m = `${d.getMonth() + 1}`.padStart(2, '0');
  const day = `${d.getDate()}`.padStart(2, '0');
  return `${y}-${m}-${day}`;
}

function isActive(expiryDateStr) {
  if (!expiryDateStr) return true; // 永久
  const s = expiryDateStr.replace('T', ' ');
  const t = new Date(s).getTime();
  if (!t) return true;
  return t > Date.now();
}

Page({
  data: {
    loading: false,
    saving: false,
    keyword: '',
    tab: 'active', // all / active / expired

    list: [],
    filteredList: [],

    addVisible: false,
    form: {
      userId: '',
      reason: '',
      permanent: true,
      expiryDate: todayStr()
    }
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
    this.setData({ loading: true });
    return request({
      url: '/admin/blacklist/list',
      method: 'GET'
    }).then((res) => {
      const list = (res || []).map((it) => {
        const expiryText = it.expiryDate ? formatDateTime(it.expiryDate) : '永久/未设置';
        return {
          ...it,
          expiryText,
          isActive: isActive(it.expiryDate)
        };
      });
      this.setData({ list });
      this.applyFilter(list);
    }).catch((err) => {
      console.error('加载黑名单失败', err);
      this.setData({ list: [], filteredList: [] });
    }).finally(() => {
      this.setData({ loading: false });
    });
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

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    this.setData({ tab });
    this.applyFilter();
  },

  applyFilter(listData) {
    const list = listData || this.data.list;
    const kw = (this.data.keyword || '').toLowerCase();
    const { tab } = this.data;

    let filtered = list;
    if (tab === 'active') filtered = filtered.filter((x) => x.isActive);
    if (tab === 'expired') filtered = filtered.filter((x) => !x.isActive);
    if (kw) {
      filtered = filtered.filter((x) => {
        const a = `${x.realName || ''}`.toLowerCase();
        const b = `${x.username || ''}`.toLowerCase();
        const c = `${x.reason || ''}`.toLowerCase();
        return a.includes(kw) || b.includes(kw) || c.includes(kw);
      });
    }
    this.setData({ filteredList: filtered });
  },

  handleRemove(e) {
    const userId = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认移除',
      content: '确定要将该用户移出黑名单吗？',
      success: (res) => {
        if (!res.confirm) return;
        wx.showLoading({ title: '处理中...' });
        request({
          url: `/admin/blacklist/remove/${userId}`,
          method: 'DELETE'
        }).then(() => {
          wx.showToast({ title: '已移除', icon: 'success' });
          this.loadData();
        }).finally(() => {
          wx.hideLoading();
        });
      }
    });
  },

  openAdd() {
    this.setData({
      addVisible: true,
      saving: false,
      form: {
        userId: '',
        reason: '',
        permanent: true,
        expiryDate: todayStr()
      }
    });
  },

  closeAdd() {
    if (this.data.saving) return;
    this.setData({ addVisible: false });
  },

  stopProp() {},

  handleFormInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({
      form: {
        ...this.data.form,
        [field]: e.detail.value
      }
    });
  },

  togglePermanent(e) {
    this.setData({
      form: {
        ...this.data.form,
        permanent: !!e.detail.value
      }
    });
  },

  handleExpiryDate(e) {
    this.setData({
      form: {
        ...this.data.form,
        expiryDate: e.detail.value
      }
    });
  },

  submitAdd() {
    if (this.data.saving) return;
    const userId = (this.data.form.userId || '').trim();
    const reason = (this.data.form.reason || '').trim();
    if (!userId) return wx.showToast({ title: '请输入用户ID', icon: 'none' });
    const uid = Number(userId);
    if (!Number.isFinite(uid) || uid <= 0 || !Number.isInteger(uid)) {
      return wx.showToast({ title: '用户ID须为正整数', icon: 'none' });
    }
    if (!reason) return wx.showToast({ title: '请输入原因', icon: 'none' });

    const expiryDate = this.data.form.permanent
      ? null
      : `${this.data.form.expiryDate}T23:59:59`;

    this.setData({ saving: true });
    wx.showLoading({ title: '新增中...' });
    request({
      url: '/admin/blacklist/add',
      method: 'POST',
      data: {
        userId: uid,
        reason,
        expiryDate
      }
    }).then(() => {
      wx.showToast({ title: '已新增', icon: 'success' });
      this.setData({ addVisible: false });
      this.loadData();
    }).finally(() => {
      wx.hideLoading();
      this.setData({ saving: false });
    });
  }
});