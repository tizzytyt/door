const { request } = require('../../utils/request.js');

function formatDateTime(dtStr) {
  if (!dtStr) return '';
  // 兼容 "YYYY-MM-DDTHH:mm:ss" / "YYYY-MM-DD HH:mm:ss"
  const s = dtStr.replace('T', ' ');
  return s.slice(0, 16);
}

Page({
  data: {
    loading: false,
    list: [],
    filteredList: [],
    typeTab: 0,   // 0-全部 1-进入 2-离开
    statusTab: 0  // 0-全部 1-正常 2-异常
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
      url: '/student/reservation/access-records',
      method: 'GET'
    }).then(res => {
      const list = (res || []).map((it) => {
        const typeText = it.type === 1 ? '进入' : it.type === 2 ? '离开' : '未知';
        const statusText = it.status === 1 ? '正常' : it.status === 0 ? '异常' : '未知';
        return {
          ...it,
          accessTimeText: formatDateTime(it.accessTime || it.createTime),
          typeText,
          statusText
        };
      });
      this.setData({ list });
      this.applyFilter(list);
    }).catch(err => {
      console.error('加载出入记录失败', err);
    }).finally(() => {
      this.setData({ loading: false });
    });
  },

  switchType(e) {
    const typeTab = parseInt(e.currentTarget.dataset.tab, 10);
    this.setData({ typeTab });
    this.applyFilter();
  },

  switchStatus(e) {
    const statusTab = parseInt(e.currentTarget.dataset.tab, 10);
    this.setData({ statusTab });
    this.applyFilter();
  },

  applyFilter(listData) {
    const list = listData || this.data.list;
    const { typeTab, statusTab } = this.data;
    let filtered = list;
    if (typeTab === 1) filtered = filtered.filter((x) => x.type === 1);
    if (typeTab === 2) filtered = filtered.filter((x) => x.type === 2);
    if (statusTab === 1) filtered = filtered.filter((x) => x.status === 1);
    if (statusTab === 2) filtered = filtered.filter((x) => x.status === 0);
    this.setData({ filteredList: filtered });
  }
})
