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
    loading: false,
    keyword: '',
    statusTab: -1,
    date: '',
    userId: '',
    userName: '',

    list: [],
    filteredList: []
  },

  onLoad(options) {
    const userId = options && options.userId ? String(options.userId) : '';
    const userName = options && options.name ? decodeURIComponent(options.name) : '';
    if (userId) {
      this.setData({ userId, userName });
      wx.setNavigationBarTitle({ title: userName ? `预约查询·${userName}` : '预约查询·用户' });
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
    const url = this.data.userId
      ? `/admin/reservation/user/${this.data.userId}`
      : '/admin/reservation/list';
    return request({
      url,
      method: 'GET'
    }).then((res) => {
      const list = (res || []).map((it) => ({
        ...it,
        statusText: mapStatusText(it.status)
      }));
      this.setData({ list });
      this.applyFilter(list);
    }).catch((err) => {
      console.error('加载预约列表失败', err);
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

  switchStatusTab(e) {
    const statusTab = parseInt(e.currentTarget.dataset.tab, 10);
    this.setData({ statusTab });
    this.applyFilter();
  },

  handleDateChange(e) {
    this.setData({ date: e.detail.value });
    this.applyFilter();
  },

  clearDate() {
    this.setData({ date: '' });
    this.applyFilter();
  },

  clearUser() {
    this.setData({ userId: '', userName: '' });
    wx.setNavigationBarTitle({ title: '预约查询' });
    this.loadData();
  },

  applyFilter(listData) {
    const list = listData || this.data.list;
    const kw = (this.data.keyword || '').toLowerCase();
    const { statusTab, date } = this.data;

    let filtered = list;
    if (statusTab !== -1) filtered = filtered.filter((x) => x.status === statusTab);
    if (date) filtered = filtered.filter((x) => x.reservationDate === date);
    if (kw) {
      filtered = filtered.filter((x) => {
        const a = `${x.realName || ''}`.toLowerCase();
        const b = `${x.deviceName || ''}`.toLowerCase();
        const c = `${x.reason || ''}`.toLowerCase();
        return a.includes(kw) || b.includes(kw) || c.includes(kw);
      });
    }
    this.setData({ filteredList: filtered });
  }
});