const API_BASE_URL_STORAGE_KEY = 'restaurant_api_base_url';
const LOCALHOST_BASE_URL = 'http://127.0.0.1:8080/api';
const LOCALHOST_NAME_BASE_URL = 'http://localhost:8080/api';
const ENV_BASE_URLS = {
  develop: LOCALHOST_BASE_URL,
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

function isLoopbackUrl(value) {
  return /^https?:\/\/(127\.0\.0\.1|localhost)(:\d+)?(\/|$)/i.test(value);
}

function uniqueUrls(urls) {
  return urls.map(normalizeBaseUrl).filter(Boolean).filter((url, index, list) => list.indexOf(url) === index);
}

function getBaseUrl() {
  return getBaseUrls()[0];
}

function getBaseUrls() {
  const envVersion = getEnvVersion();
  const envBaseUrl = ENV_BASE_URLS[envVersion] || ENV_BASE_URLS.develop;
  const customBaseUrl = normalizeBaseUrl(wx.getStorageSync(API_BASE_URL_STORAGE_KEY));
  const urls = [];
  if (customBaseUrl) {
    urls.push(customBaseUrl);
  }
  urls.push(envBaseUrl);
  if (envVersion === 'develop') {
    urls.push(LOCALHOST_BASE_URL, LOCALHOST_NAME_BASE_URL);
  }
  return uniqueUrls(urls);
}

const config = {
  get baseUrl() {
    return getBaseUrl();
  },
  get baseUrls() {
    return getBaseUrls();
  },
  apiBaseUrlStorageKey: API_BASE_URL_STORAGE_KEY,
  envBaseUrls: ENV_BASE_URLS,
  isLoopbackUrl,
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
