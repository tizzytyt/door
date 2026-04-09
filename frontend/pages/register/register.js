const { request } = require('../../utils/request.js');

Page({
  data: {
    username: '',
    realName: '',
    phone: '',
    password: '',
    confirmPassword: '',
    loading: false
  },

  goLogin() {
    wx.navigateTo({ url: '/pages/login/login' });
  },

  /** 大陆手机号：11 位，1 开头 */
  validatePhone(phone) {
    return /^1\d{10}$/.test(phone);
  },

  async handleRegister() {
    const { username, realName, phone, password, confirmPassword } = this.data;
    const phoneTrim = (phone || '').trim();

    if (!username) return wx.showToast({ title: '请输入账号', icon: 'none' });
    if (!realName) return wx.showToast({ title: '请输入真实姓名', icon: 'none' });
    if (!password) return wx.showToast({ title: '请输入密码', icon: 'none' });
    if (password.length < 6) return wx.showToast({ title: '密码至少6位', icon: 'none' });
    if (!confirmPassword) return wx.showToast({ title: '请确认密码', icon: 'none' });
    if (password !== confirmPassword) return wx.showToast({ title: '两次密码不一致', icon: 'none' });
    if (phoneTrim && !this.validatePhone(phoneTrim)) {
      return wx.showToast({ title: '手机号格式不正确', icon: 'none' });
    }

    this.setData({ loading: true });
    try {
      await request({
        url: '/register',
        method: 'POST',
        data: {
          username,
          realName,
          phone: phoneTrim,
          password
        }
      });

      wx.showToast({ title: '注册成功，请登录', icon: 'success' });
      setTimeout(() => {
        wx.reLaunch({ url: '/pages/login/login' });
      }, 1000);
    } catch (e) {
      // request 内部已弹窗提示
    } finally {
      this.setData({ loading: false });
    }
  }
});

