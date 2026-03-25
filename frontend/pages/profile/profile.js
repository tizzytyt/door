const { request } = require('../../utils/request.js');

Page({
  data: {
    userInfo: {},
    roleText: ''
  },

  onShow() {
    this.checkLogin();
    this.syncTabBarVisible();
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

  syncTabBarVisible() {
    const user = wx.getStorageSync('user');
    if (user && (user.role === 'admin' || user.role === 'super_admin')) {
      wx.hideTabBar();
    } else {
      wx.showTabBar();
    }
  },

  navTo(e) {
    const url = e.currentTarget.dataset.url;
    wx.navigateTo({
      url: url
    });
  },

  handleLogout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync('token');
          wx.removeStorageSync('user');
          wx.reLaunch({
            url: '/pages/login/login'
          });
        }
      }
    });
  }
})
