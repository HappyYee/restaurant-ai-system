const config = require('./config');

function request(options = {}) {
  const token = wx.getStorageSync('token');
  const headers = Object.assign(
    {
      'content-type': 'application/json'
    },
    options.header || {}
  );

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  return new Promise((resolve, reject) => {
    wx.request({
      url: `${config.baseUrl}${options.url}`,
      method: options.method || 'GET',
      data: options.data || {},
      header: headers,
      success(res) {
        const body = res.data || {};
        if (res.statusCode >= 200 && res.statusCode < 300 && body.code === 200) {
          resolve(body.data);
          return;
        }

        const message = body.message || `请求失败：${res.statusCode}`;
        wx.showToast({
          title: message,
          icon: 'none'
        });
        reject(new Error(message));
      },
      fail(error) {
        const endpoint = `${config.baseUrl}${options.url}`;
        wx.showToast({
          title: `无法连接后端：${config.baseUrl}`,
          icon: 'none'
        });
        console.error('request fail:', endpoint, error);
        reject(error);
      }
    });
  });
}

module.exports = {
  request
};
