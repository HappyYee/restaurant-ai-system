const { request } = require('./request');
const { clearSession, getCurrentUser, getToken, setSession } = require('./session');

function login(force = false) {
  const token = getToken();
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
            setSession(data, nextUser);
            resolve(nextUser);
          })
          .catch(reject);
      },
      fail: reject
    });
  });
}

function logout() {
  clearSession();
}

module.exports = {
  getCurrentUser,
  login,
  logout
};
