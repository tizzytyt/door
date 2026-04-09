const { request } = require('../../utils/request.js');

function formatDateTime(dtStr) {
  if (!dtStr) return '';
  const s = String(dtStr).replace('T', ' ');
  return s.slice(0, 16);
}

function makePreview(content) {
  const s = (content || '').replace(/\s+/g, ' ').trim();
  if (!s) return '';
  return s.length > 60 ? s.slice(0, 60) + '...' : s;
}

Page({
  data: {
    list: [],
    unreadCount: 0,
    loading: false
  },

  onShow() {
    this.loadAll();
  },

  loadAll() {
    if (this.data.loading) return Promise.resolve();
    this.setData({ loading: true });
    wx.showLoading({ title: '加载中...' });

    Promise.all([
      request({ url: '/student/announcement/list', method: 'GET' }),
      request({ url: '/student/announcement/unread-count', method: 'GET' })
    ])
      .then(([listRes, unreadRes]) => {
        const list = (listRes || []).map((it) => ({
          ...it,
          isRead: typeof it.isRead === 'number' ? it.isRead : parseInt(it.isRead || 0, 10),
          createdAtText: formatDateTime(it.createdAt),
          preview: makePreview(it.content)
        }));
        this.setData({ list, unreadCount: unreadRes || 0 });
      })
      .catch((err) => {
        console.error('加载公告失败', err);
        this.setData({ list: [], unreadCount: 0 });
      })
      .finally(() => {
        wx.hideLoading();
        this.setData({ loading: false });
      });
  },

  openDetail(e) {
    const id = e.currentTarget.dataset.id;
    if (!id) return;
    const item = this.data.list.find((x) => String(x.id) === String(id));
    if (!item) return;

    wx.showModal({
      title: item.title || '系统公告',
      content: item.content || '',
      showCancel: false,
      confirmText: '我知道了',
      success: () => {
        // 查看即标记已读
        this.markRead(id);
      }
    });
  },

  markRead(id) {
    const current = (this.data.list || []).find((x) => String(x.id) === String(id));
    const shouldDec = current && (current.isRead === 0 || String(current.isRead) === '0');
    return request({
      url: `/student/announcement/read/${id}`,
      method: 'POST'
    })
      .then(() => {
        const list = (this.data.list || []).map((it) => {
          if (String(it.id) === String(id)) {
            return { ...it, isRead: 1 };
          }
          return it;
        });
        const unreadCount = shouldDec ? Math.max(0, (this.data.unreadCount || 0) - 1) : (this.data.unreadCount || 0);
        this.setData({ list, unreadCount });
      })
      .catch((err) => {
        console.error('标记已读失败', err);
      });
  }
});

