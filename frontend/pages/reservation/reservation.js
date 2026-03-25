const { request } = require('../../utils/request.js');

Page({
  data: {
    deviceIndex: -1,
    devices: [],
    isFavorite: false,
    favoriteLoading: false,
    date: '',
    startDate: '',
    endDate: '',
    minStartTime: '00:00',
    startTime: '',
    endTime: '',
    reason: '',
    loading: false
  },

  onLoad(options) {
    this.initDates();
    this.loadDevices(options.deviceId);
  },

  // HH:mm -> minutes
  timeToMinutes(hhmm) {
    if (!hhmm) return 0;
    const [h, m] = `${hhmm}`.split(':').map((x) => parseInt(x, 10));
    return (h || 0) * 60 + (m || 0);
  },

  minutesToTime(minutes) {
    const mm = Math.max(0, Math.min(23 * 60 + 59, minutes));
    const h = Math.floor(mm / 60);
    const m = mm % 60;
    return `${`${h}`.padStart(2, '0')}:${`${m}`.padStart(2, '0')}`;
  },

  addMinutesCap(hhmm, mins) {
    const base = this.timeToMinutes(hhmm);
    const next = base + (mins || 0);
    return this.minutesToTime(next);
  },

  formatTime(date) {
    const h = `${date.getHours()}`.padStart(2, '0');
    const m = `${date.getMinutes()}`.padStart(2, '0');
    return `${h}:${m}`;
  },

  updateMinStartTimeByDate(dateStr) {
    const selectedDate = dateStr || this.data.date;
    const now = new Date();
    // 只有预约当天，才限制最早时间从“当前时刻”开始
    const minStartTime = selectedDate === this.todayYmd ? this.formatTime(now) : '00:00';
    this.setData({ minStartTime });
  },

  refreshFavoriteByDeviceId(deviceId) {
    const did = Number(deviceId);
    if (!did) {
      this.setData({ isFavorite: false });
      return Promise.resolve(false);
    }

    return request({
      url: '/student/device/favorites',
      method: 'GET'
    }).then((res) => {
      const favorites = res || [];
      const set = new Set(favorites.map((it) => Number(it.deviceId ?? it.id)).filter(Boolean));
      const ok = set.has(did);
      this.setData({ isFavorite: ok });
      return ok;
    }).catch((err) => {
      console.error('加载收藏状态失败', err);
      this.setData({ isFavorite: false });
      return false;
    });
  },

  initDates() {
    const today = new Date();
    this.todayYmd = this.formatDate(today);
    const maxDate = new Date();
    maxDate.setDate(today.getDate() + 7); // 可预约未来7天

    this.setData({
      startDate: this.todayYmd,
      endDate: this.formatDate(maxDate),
      date: this.todayYmd
    });
  },

  formatDate(date) {
    const y = date.getFullYear();
    const m = (date.getMonth() + 1).toString().padStart(2, '0');
    const d = date.getDate().toString().padStart(2, '0');
    return `${y}-${m}-${d}`;
  },

  loadDevices(preSelectedId) {
    request({
      url: '/student/device/list',
      method: 'GET'
    }).then(res => {
      const list = res || [];
      this.setData({ devices: list });

      const did = Number(preSelectedId);
      let index = -1;
      if (did) {
        index = list.findIndex((d) => Number(d.id) === did);
      }
      if (index > -1) {
        this.setData({ deviceIndex: index });
        // 设备列表加载完成后同步收藏状态
        this.refreshFavoriteByDeviceId(list[index]?.id);
      } else {
        // 没找到预选设备时，清空为非收藏
        this.setData({ deviceIndex: -1, isFavorite: false });
      }
    });
  },

  handleDeviceChange(e) {
    const deviceIndex = e.detail.value;
    this.setData({ deviceIndex });
    const { devices } = this.data;
    const selected = devices[deviceIndex];
    if (selected && selected.id) {
      this.refreshFavoriteByDeviceId(selected.id);
    } else {
      this.setData({ isFavorite: false });
    }
  },

  handleDateChange(e) {
    const nextDate = e.detail.value;
    this.setData({ date: nextDate });
  },

  handleStartTimeChange(e) {
    const startTime = e.detail.value;
    const endTime = this.data.endTime;
    this.setData({ startTime });

    // 若结束时间不合法，则给出一个默认的结束时间（当前选择的开始时间 + 30min）
    if (!endTime || this.timeToMinutes(endTime) <= this.timeToMinutes(startTime)) {
      const defaultEnd = this.addMinutesCap(startTime, 30);
      this.setData({ endTime: defaultEnd > startTime ? defaultEnd : '' });
    }
  },

  handleStartTimePickStart() {
    // 仅在进入时间选择（滚动条）时，才设置最早可选时间为当前时刻
    this.updateMinStartTimeByDate(this.data.date);
  },

  handleEndTimePickStart() {
    // 进入结束时间选择时，也同步最早下限
    this.updateMinStartTimeByDate(this.data.date);
  },

  handleEndTimeChange(e) {
    this.setData({ endTime: e.detail.value });
  },

  handleSubmit() {
    const { deviceIndex, devices, date, startTime, endTime, reason } = this.data;

    if (deviceIndex < 0) return this.toast('请选择门禁设备');
    if (!date) return this.toast('请选择预约日期');
    if (!startTime) return this.toast('请选择开始时间');
    if (!endTime) return this.toast('请选择结束时间');
    if (startTime >= endTime) return this.toast('结束时间必须晚于开始时间');
    if (!reason) return this.toast('请输入申请事由');

    this.setData({ loading: true });

    request({
      url: '/student/reservation/submit',
      method: 'POST',
      data: {
        deviceId: devices[deviceIndex].id,
        reservationDate: date,
        startTime: startTime + ':00', // 后端可能需要秒
        endTime: endTime + ':00',
        reason: reason
      }
    }).then(() => {
      wx.showToast({ title: '提交成功', icon: 'success' });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    }).catch(err => {
      this.toast(err || '提交失败');
    }).finally(() => {
      this.setData({ loading: false });
    });
  },

  handleToggleFavorite() {
    const { deviceIndex, devices, isFavorite } = this.data;
    if (deviceIndex < 0) return this.toast('请选择门禁设备');
    const deviceId = devices[deviceIndex]?.id;
    if (!deviceId) return this.toast('门禁设备无效');

    if (this.data.favoriteLoading) return;
    this.setData({ favoriteLoading: true });

    const url = isFavorite
      ? `/student/device/favorite/remove/${deviceId}`
      : `/student/device/favorite/add/${deviceId}`;

    request({
      url,
      method: 'POST'
    }).then(() => {
      this.setData({ isFavorite: !isFavorite });
      wx.showToast({ title: isFavorite ? '已取消收藏' : '已收藏', icon: 'success' });
    }).catch((err) => {
      this.toast(err || '操作失败');
    }).finally(() => {
      this.setData({ favoriteLoading: false });
    });
  },

  toast(msg) {
    wx.showToast({ title: msg, icon: 'none' });
  }
})
