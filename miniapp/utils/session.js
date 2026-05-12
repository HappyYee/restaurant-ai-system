const TOKEN_KEY = 'token';
const USER_KEY = 'userInfo';
const TOKEN_EXPIRES_AT_KEY = 'tokenExpiresAt';
const TOKEN_TTL_MS = 11.5 * 60 * 60 * 1000;

function clearSession() {
  wx.removeStorageSync(TOKEN_KEY);
  wx.removeStorageSync(USER_KEY);
  wx.removeStorageSync(TOKEN_EXPIRES_AT_KEY);
}

function setSession(data, userInfo) {
  if (!data || !data.token) {
    clearSession();
    return;
  }
  wx.setStorageSync(TOKEN_KEY, data.token);
  wx.setStorageSync(USER_KEY, userInfo);
  wx.setStorageSync(TOKEN_EXPIRES_AT_KEY, Date.now() + TOKEN_TTL_MS);
}

function getToken() {
  const token = wx.getStorageSync(TOKEN_KEY);
  const expiresAt = Number(wx.getStorageSync(TOKEN_EXPIRES_AT_KEY) || 0);
  if (!token) {
    return '';
  }
  if (expiresAt && Date.now() > expiresAt) {
    clearSession();
    return '';
  }
  return token;
}

function getCurrentUser() {
  const token = getToken();
  if (!token) {
    return null;
  }
  return wx.getStorageSync(USER_KEY) || null;
}

module.exports = {
  clearSession,
  getCurrentUser,
  getToken,
  setSession
};
