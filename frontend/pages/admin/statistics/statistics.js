const { request, BASE_URL } = require('../../../utils/request.js');

function pct(part, total) {
  if (!total) return 0;
  return Math.round((part / total) * 1000) / 10; // 1位小数
}

function maxCount(arr) {
  let m = 0;
  (arr || []).forEach((x) => {
    const c = Number(x.count || 0);
    if (c > m) m = c;
  });
  return m || 1;
}

function statusTextMap() {
  return {
    0: '待审核',
    1: '已通过',
    2: '已拒绝',
    3: '已使用',
    4: '已取消',
    5: '已失效'
  };
}

Page({
  data: {
    loading: true,
    stats: {
      totalReservations: 0,
      pendingCount: 0,
      successCount: 0,
      deviceCount: 0,
      userCount: 0,
      successRate: 0,
      pendingRate: 0
    },
    trend7: [],
    trend7Max: 1,
    statusDistList: [],
    statusDistMax: 1,
    topDevices: [],
    topDevicesMax: 1,
    updatedAt: '',

    insideOnly: false,
    insideLoading: false,
    insideList: []
  },

  onShow() {
    this.loadStats();
  },

  onPullDownRefresh() {
    this.loadStats().finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  roleTextMap() {
    return {
      student: '学生',
      admin: '管理员',
      super_admin: '超级管理员'
    };
  },

  normalizeTime(t) {
    const s = t == null ? '' : String(t);
    // 兼容 "HH:mm:ss" / "HH:mm"
    return s.length >= 5 ? s.slice(0, 5) : s;
  },

  onInsideOnlyChange(e) {
    const insideOnly = !!e.detail.value;
    this.setData({ insideOnly });
    if (insideOnly) {
      this.loadInsidePeople();
    } else {
      this.setData({ insideList: [] });
    }
  },

  loadInsidePeople() {
    this.setData({ insideLoading: true });
    return request({
      url: '/admin/dashboard/inside-people',
      method: 'GET'
    }).then((res) => {
      const roleMap = this.roleTextMap();
      const list = (res || []).map((it) => {
        const roleText = roleMap[it.role] || '';
        const start = this.normalizeTime(it.startTime);
        const end = this.normalizeTime(it.endTime);
        const date = it.reservationDate ? String(it.reservationDate) : '';
        return {
          ...it,
          roleText,
          reservationTimeText: `${date}${start && end ? ' ' : ''}${start}-${end}`
        };
      });
      this.setData({ insideList: list });
    }).catch((err) => {
      console.error('加载在校人员失败', err);
      this.setData({ insideList: [] });
    }).finally(() => {
      this.setData({ insideLoading: false });
    });
  },

  loadStats() {
    this.setData({ loading: true });
    return request({
      url: '/admin/dashboard/stats',
      method: 'GET'
    }).then((res) => {
      const total = Number(res.totalReservations || 0);
      const pending = Number(res.pendingCount || 0);
      const success = Number(res.successCount || 0);

      // 近7天趋势
      const trend7 = (res.trend7 || []).map((x) => ({
        date: x.date,
        shortDate: (x.date || '').slice(5),
        count: Number(x.count || 0)
      }));
      const trend7Max = maxCount(trend7);

      // 状态分布
      const m = statusTextMap();
      const statusDist = res.statusDist || {};
      const statusDistList = Object.keys(m).map((k) => {
        const key = Number(k);
        return {
          status: key,
          label: m[key],
          count: Number(statusDist[String(key)] || 0)
        };
      });
      const statusDistMax = maxCount(statusDistList);

      // Top门禁
      const topDevices = (res.topDevices || []).map((x) => ({
        name: x.name,
        count: Number(x.count || 0)
      }));
      const topDevicesMax = maxCount(topDevices);

      const now = new Date();
      this.setData({
        stats: {
          totalReservations: total,
          pendingCount: pending,
          successCount: success,
          deviceCount: Number(res.deviceCount || 0),
          userCount: Number(res.userCount || 0),
          successRate: pct(success, total),
          pendingRate: pct(pending, total)
        },
        trend7,
        trend7Max,
        statusDistList,
        statusDistMax,
        topDevices,
        topDevicesMax,
        updatedAt: `${now.toLocaleDateString()} ${now.toLocaleTimeString()}`
      });
    }).catch((err) => {
      console.error('加载统计失败', err);
    }).finally(() => {
      this.setData({ loading: false });
    });
  }

  ,

  exportExcel() {
    const token = wx.getStorageSync('token');
    wx.showLoading({ title: '导出中...' });
    wx.downloadFile({
      url: `${BASE_URL}/admin/dashboard/stats/export`,
      header: token ? { token } : {},
      success: (res) => {
        if (res.statusCode !== 200) {
          wx.showToast({ title: '导出失败', icon: 'none' });
          return;
        }
        wx.openDocument({
          filePath: res.tempFilePath,
          fileType: 'xlsx',
          showMenu: true
        });
      },
      fail: () => {
        wx.showToast({ title: '下载失败', icon: 'none' });
      },
      complete: () => {
        wx.hideLoading();
      }
    });
  }
});