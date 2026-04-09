const { request } = require('../../../utils/request.js');

function formatDateTime(dtStr) {
  if (!dtStr) return '';
  const s = String(dtStr).replace('T', ' ');
  return s.slice(0, 16);
}

Page({
  data: {
    id: null,
    tab: 1, // 1已读 0未读
    announcement: null,
    list: []
  },

  onLoad(options) {
    const id = options && options.id;
    if (!id) return;
    this.setData({ id: id });
    this.loadDetail();
    this.loadList();
  },

  switchTab(e) {
    const tab = parseInt(e.currentTarget.dataset.tab, 10);
    if (tab === this.data.tab) return;
    this.setData({ tab, list: [] });
    this.loadList();
  },

  loadDetail() {
    wx.showLoading({ title: '加载中...' });
    return request({
      url: `/admin/announcement/detail/${this.data.id}`,
      method: 'GET'
    })
      .then((res) => {
        if (!res) return;
        this.setData({
          announcement: {
            ...res,
            createdAtText: formatDateTime(res.createdAt)
          }
        });
      })
      .catch((err) => {
        console.error('加载公告详情失败', err);
      })
      .finally(() => {
        wx.hideLoading();
      });
  },

  loadList() {
    wx.showLoading({ title: '加载中...' });
    const tab = this.data.tab;
    const url = tab === 1
      ? `/admin/announcement/readers/${this.data.id}?read=1`
      : `/admin/announcement/readers/${this.data.id}?read=0`;

    return request({ url, method: 'GET' })
      .then((res) => {
        const list = (res || []).map((it) => ({
          ...it,
          readAtText: formatDateTime(it.readAt || it.read_at)
        }));
        this.setData({ list });
      })
      .catch((err) => {
        console.error('加载阅读明细失败', err);
        this.setData({ list: [] });
      })
      .finally(() => {
        wx.hideLoading();
      });
  }
});

