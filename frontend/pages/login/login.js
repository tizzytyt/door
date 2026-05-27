const { request } = require('../../utils/request.js');

Page({
  data: {
    username: '',
    password: '',
    captchaCode: '',
    captchaKey: '',
    captchaImage: '',
    role: 'student',
    loading: false
  },

  onLoad() {
    this.loadCaptcha();
  },

  loadCaptcha() {
    return request({
      url: '/captcha',
      method: 'GET',
      silent: true
    }).then((res) => {
      this.setData({
        captchaKey: res.captchaKey || '',
        captchaImage: res.captchaImage || '',
        captchaCode: ''
      });
    }).catch(() => {
      wx.showToast({ title: '验证码加载失败', icon: 'none' });
    });
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
    if (!this.data.captchaCode || !this.data.captchaKey) {
      wx.showToast({ title: '请输入验证码', icon: 'none' });
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
          role: this.data.role,
          captchaKey: this.data.captchaKey,
          captchaCode: this.data.captchaCode
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
      this.loadCaptcha();
    } finally {
      this.setData({ loading: false });
    }
  }
})
