const API_BASE_URL_STORAGE_KEY = 'restaurant_api_base_url';
const ENV_BASE_URLS = {
  develop: 'http://127.0.0.1:8080/api',
  trial: 'https://your-domain.example.com/api',
  release: 'https://your-domain.example.com/api'
};

function getEnvVersion() {
  try {
    return wx.getAccountInfoSync().miniProgram.envVersion || 'develop';
  } catch (error) {
    return 'develop';
  }
}

function normalizeBaseUrl(value) {
  return String(value || '').trim().replace(/\/+$/, '');
}

function getBaseUrl() {
  const customBaseUrl = normalizeBaseUrl(wx.getStorageSync(API_BASE_URL_STORAGE_KEY));
  if (customBaseUrl) {
    return customBaseUrl;
  }
  return ENV_BASE_URLS[getEnvVersion()] || ENV_BASE_URLS.develop;
}

const config = {
  get baseUrl() {
    return getBaseUrl();
  },
  apiBaseUrlStorageKey: API_BASE_URL_STORAGE_KEY,
  envBaseUrls: ENV_BASE_URLS,
  setBaseUrl(value) {
    const nextValue = normalizeBaseUrl(value);
    if (nextValue) {
      wx.setStorageSync(API_BASE_URL_STORAGE_KEY, nextValue);
    }
  },
  clearBaseUrl() {
    wx.removeStorageSync(API_BASE_URL_STORAGE_KEY);
  }
};

module.exports = config;
