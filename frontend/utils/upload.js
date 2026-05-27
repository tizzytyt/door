const { BASE_URL } = require('./request.js');

/**
 * 上传文件（如头像），返回接口 JSON 的 data 字段
 */
function uploadFile({ url, filePath, name = 'file' }) {
  const token = wx.getStorageSync('token');
  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: BASE_URL + url,
      filePath,
      name,
      header: token ? { token } : {},
      success: (res) => {
        if (res.statusCode !== 200) {
          reject('服务器异常');
          return;
        }
        let body;
        try {
          body = typeof res.data === 'string' ? JSON.parse(res.data) : res.data;
        } catch (e) {
          reject('响应解析失败');
          return;
        }
        if (body.code === 200) {
          resolve(body.data);
        } else if (body.code === 401) {
          wx.removeStorageSync('token');
          wx.removeStorageSync('user');
          reject(body.msg || 'NOT_LOGIN');
        } else {
          reject(body.msg || '上传失败');
        }
      },
      fail: () => reject('网络请求失败')
    });
  });
}

module.exports = { uploadFile };
