const { request } = require('../../utils/request.js');

Page({
  data: {
    loading: false,
    list: [],
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
      url: '/student/device/favorites',
      method: 'GET'
    }).then(res => {
      const list = res || [];
      this.setData({ list });
      this.applyFilter(list);
    }).catch(err => {
      console.error('加载收藏列表失败', err);
      this.setData({ list: [], filteredList: [] });
    }).finally(() => {
      this.setData({ loading: false });
    });
  },

  handleSearchInput(e) {
    const keyword = (e.detail.value || '').trim();
    this.setData({ keyword });
    this.applyFilter();
  },

  clearSearch() {
    this.setData({ keyword: '' });
    this.applyFilter();
  },

  applyFilter(listData) {
    const list = listData || this.data.list;
    const kw = (this.data.keyword || '').trim().toLowerCase();
    if (!kw) {
      this.setData({ filteredList: list });
      return;
    }
    const filteredList = list.filter((x) => {
      const name = (x.deviceName || '').toLowerCase();
      const loc = (x.location || '').toLowerCase();
      return name.includes(kw) || loc.includes(kw);
    });
    this.setData({ filteredList });
  },

  handleRemove(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '提示',
      content: '确定要移除该收藏吗？',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '移除中...' });
          request({
            url: `/student/device/favorite/remove/${id}`,
            method: 'POST'
          }).then(() => {
            wx.showToast({ title: '已移除', icon: 'success' });
            this.loadData();
          }).finally(() => {
            wx.hideLoading();
          });
        }
      }
    });
  },

  handleReserve(e) {
    const id = e.currentTarget.dataset.id;
    const name = e.currentTarget.dataset.name;
    wx.navigateTo({
      url: `/pages/reservation/reservation?deviceId=${id}&deviceName=${name}`
    });
  }
})
