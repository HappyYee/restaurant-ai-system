const config = require('./config');
const { clearSession, getToken } = require('./session');

function request(options = {}) {
  const token = getToken();
  const baseUrls = config.baseUrls && config.baseUrls.length ? config.baseUrls : [config.baseUrl];
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
    const attempt = (index, lastError) => {
      const baseUrl = baseUrls[index];
      if (!baseUrl) {
        const message = `无法连接后端：${baseUrls.join(' / ')}`;
        wx.showToast({
          title: message,
          icon: 'none'
        });
        reject(lastError || new Error(message));
        return;
      }

      wx.request({
        url: `${baseUrl}${options.url}`,
        method: options.method || 'GET',
        data: options.data || {},
        header: headers,
        timeout: options.timeout || 4000,
        success(res) {
          const body = res.data || {};
          if (res.statusCode >= 200 && res.statusCode < 300 && body.code === 200) {
            resolve(body.data);
            return;
          }

          const isAuthExpired = res.statusCode === 401 || body.code === 401;
          if (isAuthExpired || res.statusCode === 403 || body.code === 403) {
            clearSession();
          }

          const message = isAuthExpired ? '登录已失效，请重新登录' : body.message || `请求失败：${res.statusCode}`;
          wx.showToast({
            title: message,
            icon: 'none'
          });
          reject(new Error(message));
        },
        fail(error) {
          const endpoint = `${baseUrl}${options.url}`;
          console.error('request fail:', endpoint, error);
          attempt(index + 1, error);
        }
      });
    };

    attempt(0);
  });
}

module.exports = {
  request
};
