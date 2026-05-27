const { request } = require('../../utils/request.js');
const { uploadFile } = require('../../utils/upload.js');
const { resolveAvatarUrl, avatarLetter } = require('../../utils/avatar.js');

Page({
  data: {
    userInfo: {},
    roleText: '',
    avatarUrl: '',
    avatarLetter: '?',
    uploadingAvatar: false
  },

  onShow() {
    this.checkLogin();
    this.syncTabBarVisible();
    this.refreshProfile();
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
      student: '学生',
      admin: '管理员',
      super_admin: '超级管理员'
    };

    this.applyUser(user, roleMap[user.role] || '未知角色');
  },

  applyUser(user, roleText) {
    this.setData({
      userInfo: user,
      roleText: roleText || this.data.roleText,
      avatarUrl: resolveAvatarUrl(user.avatar),
      avatarLetter: avatarLetter(user)
    });
  },

  refreshProfile() {
    return request({
      url: '/user/profile',
      method: 'GET',
      silent: true
    }).then((user) => {
      if (!user) return;
      wx.setStorageSync('user', user);
      const roleMap = {
        student: '学生',
        admin: '管理员',
        super_admin: '超级管理员'
      };
      this.applyUser(user, roleMap[user.role] || '未知角色');
    }).catch(() => {});
  },

  handleChooseAvatar() {
    if (this.data.uploadingAvatar) return;
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      sizeType: ['compressed'],
      success: (res) => {
        const file = res.tempFiles && res.tempFiles[0];
        if (!file || !file.tempFilePath) return;
        this.uploadAvatar(file.tempFilePath);
      }
    });
  },

  uploadAvatar(filePath) {
    this.setData({ uploadingAvatar: true });
    wx.showLoading({ title: '上传中...' });
    uploadFile({ url: '/user/avatar', filePath })
      .then((user) => {
        wx.setStorageSync('user', user);
        const roleMap = {
          student: '学生',
          admin: '管理员',
          super_admin: '超级管理员'
        };
        this.applyUser(user, roleMap[user.role] || this.data.roleText);
        wx.showToast({ title: '头像已更新', icon: 'success' });
      })
      .catch((msg) => {
        wx.showToast({ title: msg || '上传失败', icon: 'none' });
      })
      .finally(() => {
        wx.hideLoading();
        this.setData({ uploadingAvatar: false });
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
