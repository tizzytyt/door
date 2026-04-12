const { request } = require('../../utils/request.js');

function makePreview(content) {
  const s = (content || '').replace(/\s+/g, ' ').trim();
  if (!s) return '';
  return s.length > 60 ? s.slice(0, 60) + '...' : s;
}

Page({
  data: {
    list: [],
    announcementUnread: 0,
    reservationUnread: 0,
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
      request({ url: '/student/notification/list', method: 'GET' }),
      request({ url: '/student/notification/unread-count', method: 'GET' }),
      request({ url: '/student/announcement/unread-count', method: 'GET' })
    ])
      .then(([listRes, resUnread, annUnread]) => {
        const list = (listRes || []).map((it) => ({
          ...it,
          isRead: typeof it.isRead === 'number' ? it.isRead : parseInt(it.isRead || 0, 10),
          preview: makePreview(it.content)
        }));
        this.setData({
          list,
          reservationUnread: resUnread || 0,
          announcementUnread: annUnread || 0
        });
      })
      .catch((err) => {
        console.error('加载消息失败', err);
        this.setData({ list: [], reservationUnread: 0, announcementUnread: 0 });
      })
      .finally(() => {
        wx.hideLoading();
        this.setData({ loading: false });
      });
  },

  goAnnouncements() {
    wx.navigateTo({ url: '/pages/announcements/announcements' });
  },

  openDetail(e) {
    const id = e.currentTarget.dataset.id;
    if (!id) return;
    const item = this.data.list.find((x) => String(x.id) === String(id));
    if (!item) return;

    wx.showModal({
      title: item.title || '消息',
      content: item.content || '',
      showCancel: false,
      confirmText: '我知道了',
      success: () => {
        this.markRead(id);
      }
    });
  },

  markRead(id) {
    const current = (this.data.list || []).find((x) => String(x.id) === String(id));
    const shouldDec = current && (current.isRead === 0 || String(current.isRead) === '0');
    return request({
      url: `/student/notification/read/${id}`,
      method: 'POST'
    })
      .then(() => {
        const list = (this.data.list || []).map((it) => {
          if (String(it.id) === String(id)) {
            return { ...it, isRead: 1 };
          }
          return it;
        });
        const reservationUnread = shouldDec
          ? Math.max(0, (this.data.reservationUnread || 0) - 1)
          : this.data.reservationUnread || 0;
        this.setData({ list, reservationUnread });
      })
      .catch((err) => {
        console.error('标记已读失败', err);
      });
  }
});
