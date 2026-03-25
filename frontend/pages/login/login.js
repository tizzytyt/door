const { request } = require('../../utils/request.js');

Page({
  data: {
    username: '',
    password: '',
    role: 'student',
    loading: false
  },

  goRegister() {
    wx.navigateTo({ url: '/pages/register/register' });
  },

  handleRoleChange(e) {
    this.setData({
      role: e.detail.value
    });
  },

  async handleLogin() {
    if (!this.data.username || !this.data.password) {
      wx.showToast({
        title: '请输入账号和密码',
        icon: 'none'
      });
      return;
    }
    
    this.setData({ loading: true });
    try {
      const res = await request({
        url: '/login',
        method: 'POST',
        data: {
          username: this.data.username,
          password: this.data.password,
          role: this.data.role
        }
      });
      
      // 登录成功
      wx.setStorageSync('token', res.token);
      wx.setStorageSync('user', res.user);
      
      wx.showToast({
        title: '登录成功',
        icon: 'success'
      });
      
      // 根据角色跳转不同首页
      const role = res.user && res.user.role;
      const targetUrl = role === 'admin' || role === 'super_admin'
        ? '/pages/profile/profile'
        : '/pages/index/index';

      setTimeout(() => {
        wx.reLaunch({
          url: targetUrl
        });
      }, 1000);
      
    } catch (e) {
      console.error(e);
      // 错误提示已在request中处理
    } finally {
      this.setData({ loading: false });
    }
  }
})
