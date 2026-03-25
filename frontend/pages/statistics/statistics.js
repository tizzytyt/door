const { request } = require('../../utils/request.js');

function getDateStr(date) {
  const y = date.getFullYear();
  const m = `${date.getMonth() + 1}`.padStart(2, '0');
  const d = `${date.getDate()}`.padStart(2, '0');
  return `${y}-${m}-${d}`;
}

function safeDiv(a, b) {
  if (!b) return 0;
  return Math.round((a / b) * 1000) / 10; // 1位小数
}

Page({
  data: {
    loading: true,
    total: 0,
    passed: 0,
    used: 0,
    rejected: 0,
    cancelled: 0,
    passRate: 0,
    useRate: 0,
    recent7Total: 0,
    recent7Used: 0,
    recent7Dates: [],
    topDevices: [],
    lastUpdatedAt: ''
  },

  onShow() {
    this.loadStats();
  },

  loadStats() {
    this.setData({ loading: true });
    request({
      url: '/student/reservation/list',
      method: 'GET'
    }).then((res) => {
      const list = res || [];
      const total = list.length;
      let passed = 0;
      let used = 0;
      let rejected = 0;
      let cancelled = 0;

      const deviceCount = {};
      list.forEach((r) => {
        if (r.status === 1) passed += 1;
        if (r.status === 3) used += 1;
        if (r.status === 2) rejected += 1;
        if (r.status === 4) cancelled += 1;
        const key = r.deviceName || `${r.deviceId || 'unknown'}`;
        deviceCount[key] = (deviceCount[key] || 0) + 1;
      });

      const passRate = safeDiv(passed, total);
      const useRate = safeDiv(used, total);

      // 近7天：按 reservationDate 统计（包含所有状态，另单独统计已使用）
      const recent7Dates = [];
      const recentDateMap = {};
      const now = new Date();
      for (let i = 6; i >= 0; i -= 1) {
        const d = new Date(now.getFullYear(), now.getMonth(), now.getDate() - i);
        const ds = getDateStr(d);
        recent7Dates.push(ds);
        recentDateMap[ds] = { total: 0, used: 0 };
      }
      list.forEach((r) => {
        if (r.reservationDate && recentDateMap[r.reservationDate]) {
          recentDateMap[r.reservationDate].total += 1;
          if (r.status === 3) recentDateMap[r.reservationDate].used += 1;
        }
      });
      let recent7Total = 0;
      let recent7Used = 0;
      recent7Dates.forEach((ds) => {
        recent7Total += recentDateMap[ds].total;
        recent7Used += recentDateMap[ds].used;
      });

      const topDevices = Object.keys(deviceCount)
        .map((name) => ({ name, count: deviceCount[name] }))
        .sort((a, b) => b.count - a.count)
        .slice(0, 5);

      this.setData({
        loading: false,
        total,
        passed,
        used,
        rejected,
        cancelled,
        passRate,
        useRate,
        recent7Total,
        recent7Used,
        recent7Dates,
        topDevices,
        lastUpdatedAt: `${now.toLocaleDateString()} ${now.toLocaleTimeString()}`
      });
    }).catch((err) => {
      console.error('加载个人统计失败', err);
      this.setData({ loading: false });
    });
  }
});