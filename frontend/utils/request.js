const BASE_URL = 'http://localhost:8080'; // 根据实际情况修改

const request = (options) => {
  return new Promise((resolve, reject) => {
    // 1. 获取token
    const token = wx.getStorageSync('token');

    // 2. 组装header
    const header = {
      'Content-Type': 'application/json',
      ...options.header
    };
    if (token) {
      header['token'] = token;
    }

    const method = (options.method || 'GET').toUpperCase();
    const ct = header['Content-Type'] || header['content-type'] || '';
    let payload = options.data;
    if (
      ct.includes('application/json') &&
      method !== 'GET' &&
      method !== 'HEAD' &&
      payload != null &&
      typeof payload === 'object' &&
      !(payload instanceof ArrayBuffer)
    ) {
      payload = JSON.stringify(payload);
    }

    // 3. 发起请求
    wx.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: payload !== undefined && payload !== null ? payload : {},
      header: header,
      success: (res) => {
        if (res.statusCode === 200) {
          const data = res.data;
          // 业务状态码处理
          if (data.code === 200) {
            resolve(data.data);
          } else if (data.code === 401) { // 未登录或token过期
            wx.removeStorageSync('token');
            wx.removeStorageSync('user');
            wx.showToast({
              title: '登录已过期，请重新登录',
              icon: 'none'
            });
            setTimeout(() => {
              wx.reLaunch({
                url: '/pages/login/login'
              });
            }, 1500);
            reject(data.msg);
          } else {
            wx.showToast({
              title: data.msg || '请求失败',
              icon: 'none'
            });
            reject(data.msg);
          }
        } else {
          wx.showToast({
            title: '服务器异常',
            icon: 'none'
          });
          reject('服务器异常');
        }
      },
      fail: (err) => {
        wx.showToast({
          title: '网络请求失败',
          icon: 'none'
        });
        reject(err);
      }
    });
  });
};

module.exports = {
  request,
  BASE_URL
};
