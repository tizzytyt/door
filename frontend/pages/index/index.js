const { request } = require('../../utils/request.js');

Page({
  data: {
    userInfo: {},
    roleText: '',
    devices: [],
    latestReservation: null
  },

  onShow() {
    wx.showTabBar();
    this.checkLogin();
    this.loadData();
  },

  onPullDownRefresh() {
    this.loadData();
    wx.stopPullDownRefresh();
  },

  checkLogin() {
    const user = wx.getStorageSync('user');
    if (!user) {
      wx.reLaunch({
        url: '/pages/login/login'
      });
      return;
    }
    
    const roleMap = {
      'student': '学生',
      'admin': '管理员',
      'super_admin': '超级管理员'
    };
    
    this.setData({
      userInfo: user,
      roleText: roleMap[user.role] || '未知角色'
    });
  },

  loadData() {
    // 加载可预约设备
    request({
      url: '/student/device/list',
      method: 'GET'
    }).then(res => {
      this.setData({ devices: res || [] });
    }).catch(err => {
      console.error('加载设备失败', err);
    });

    // 加载最近一条预约
    request({
      url: '/student/reservation/list',
      method: 'GET'
    }).then(res => {
      if (res && res.length > 0) {
        // 取最新的一条
        const latest = res[0];
        // 状态文本转换
        const statusMap = {
          0: '待审核',
          1: '已通过',
          2: '已拒绝',
          3: '已使用',
          4: '已取消',
          5: '已失效'
        };
        latest.statusText = statusMap[latest.status];
        this.setData({ latestReservation: latest });
      } else {
        this.setData({ latestReservation: null });
      }
    }).catch(err => {
      console.error('加载预约失败', err);
    });
  },

  navTo(e) {
    const url = e.currentTarget.dataset.url;
    wx.navigateTo({
      url: url
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
