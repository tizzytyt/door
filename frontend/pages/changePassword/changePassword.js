const { request } = require('../../utils/request.js');

Page({
  data: {
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
    submitting: false
  },

  handleInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [field]: e.detail.value });
  },

  handleSubmit() {
    if (this.data.submitting) return;
    const oldPassword = (this.data.oldPassword || '').trim();
    const newPassword = (this.data.newPassword || '').trim();
    const confirmPassword = (this.data.confirmPassword || '').trim();

    if (!oldPassword) return wx.showToast({ title: '请输入旧密码', icon: 'none' });
    if (!newPassword) return wx.showToast({ title: '请输入新密码', icon: 'none' });
    if (newPassword.length < 6) return wx.showToast({ title: '新密码至少6位', icon: 'none' });
    if (newPassword !== confirmPassword) return wx.showToast({ title: '两次新密码不一致', icon: 'none' });
    if (oldPassword === newPassword) return wx.showToast({ title: '新密码不能与旧密码相同', icon: 'none' });

    this.setData({ submitting: true });
    wx.showLoading({ title: '提交中...' });
    request({
      url: '/student/user/change-password',
      method: 'POST',
      data: {
        oldPassword,
        newPassword
      }
    }).then(() => {
      wx.showToast({ title: '修改成功', icon: 'success' });
      // 修改成功后引导重新登录
      wx.removeStorageSync('token');
      wx.removeStorageSync('user');
      setTimeout(() => {
        wx.reLaunch({ url: '/pages/login/login' });
      }, 800);
    }).finally(() => {
      wx.hideLoading();
      this.setData({ submitting: false });
    });
  }
});

