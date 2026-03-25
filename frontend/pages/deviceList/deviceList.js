const { request } = require('../../utils/request.js');

Page({
  data: {
    loading: false,
    devices: [],
    filteredList: [],
    keyword: ''
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
      url: '/student/device/list',
      method: 'GET'
    }).then((res) => {
      const list = res || [];
      this.setData({ devices: list });
      this.applyFilter(list);
    }).catch((err) => {
      console.error('加载门禁列表失败', err);
      this.setData({ devices: [], filteredList: [] });
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

  applyFilter(listData) {
    const list = listData || this.data.devices;
    const kw = (this.data.keyword || '').trim().toLowerCase();

    if (!kw) {
      this.setData({ filteredList: list });
      return;
    }

    const filteredList = list.filter((x) => {
      const name = `${x.name || ''}`.toLowerCase();
      const loc = `${x.location || ''}`.toLowerCase();
      return name.includes(kw) || loc.includes(kw);
    });

    this.setData({ filteredList });
  },

  handleReserve(e) {
    const id = e.currentTarget.dataset.id;
    const name = e.currentTarget.dataset.name;
    wx.navigateTo({
      url: `/pages/reservation/reservation?deviceId=${id}&deviceName=${name}`
    });
  }
});

