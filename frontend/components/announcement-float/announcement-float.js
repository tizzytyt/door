const { request } = require('../../utils/request.js');

const TAB_BAR_ROUTES = [
  'pages/index/index',
  'pages/myReservations/myReservations',
  'pages/profile/profile'
];

Component({
  data: {
    show: false,
    unreadCount: 0,
    tabBarPage: false
  },

  lifetimes: {
    attached() {
      this.refresh();
    }
  },

  pageLifetimes: {
    show() {
      this.refresh();
    }
  },

  methods: {
    refresh() {
      const user = wx.getStorageSync('user');
      if (!user || user.role === 'admin' || user.role === 'super_admin') {
        this.setData({ show: false, unreadCount: 0 });
        return;
      }

      const pages = getCurrentPages();
      const cur = pages[pages.length - 1];
      const route = cur && cur.route ? cur.route : '';
      const tabBarPage = TAB_BAR_ROUTES.indexOf(route) >= 0;
      if (route === 'pages/announcements/announcements' || route === 'pages/inbox/inbox') {
        this.setData({ show: false, tabBarPage });
        return;
      }

      Promise.all([
        request({ url: '/student/announcement/unread-count', method: 'GET', silent: true }),
        request({ url: '/student/notification/unread-count', method: 'GET', silent: true })
      ])
        .then(([ann, resN]) => {
          const a = typeof ann === 'number' ? ann : parseInt(ann || 0, 10);
          const b = typeof resN === 'number' ? resN : parseInt(resN || 0, 10);
          const n = a + b;
          this.setData({ unreadCount: n, show: n > 0, tabBarPage });
        })
        .catch(() => {
          this.setData({ show: false, unreadCount: 0 });
        });
    },

    onTap() {
      wx.navigateTo({ url: '/pages/inbox/inbox' });
    }
  }
});
