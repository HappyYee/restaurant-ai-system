const { request } = require('./request');

function getCurrentUser() {
  return wx.getStorageSync('userInfo') || null;
}

function login(force = false) {
  const token = wx.getStorageSync('token');
  const userInfo = getCurrentUser();

  if (token && userInfo && !force) {
    return Promise.resolve(userInfo);
  }

  return new Promise((resolve, reject) => {
    wx.login({
      success(result) {
        if (!result.code) {
          reject(new Error('微信登录失败'));
          return;
        }

        request({
          url: '/auth/wx-login',
          method: 'POST',
          data: {
            code: result.code,
            nickname: '微信用户'
          }
        })
          .then((data) => {
            const nextUser = {
              userId: data.userId,
              nickname: data.nickname || '微信用户'
            };
            wx.setStorageSync('token', data.token);
            wx.setStorageSync('userInfo', nextUser);
            resolve(nextUser);
          })
          .catch(reject);
      },
      fail: reject
    });
  });
}

function logout() {
  wx.removeStorageSync('token');
  wx.removeStorageSync('userInfo');
}

module.exports = {
  getCurrentUser,
  login,
  logout
};
