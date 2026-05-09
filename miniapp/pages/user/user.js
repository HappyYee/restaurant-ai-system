const { getCurrentUser, login, logout } = require('../../utils/auth');
const cartStore = require('../../utils/cart');

Page({
  data: {
    userInfo: null,
    cartSummary: {
      totalQuantity: 0,
      totalAmountText: '0.00'
    }
  },

  onShow() {
    this.refresh();
  },

  refresh() {
    this.setData({
      userInfo: getCurrentUser(),
      cartSummary: cartStore.getCartSummary()
    });
  },

  login() {
    login(true).then(() => {
      wx.showToast({
        title: '登录成功',
        icon: 'success'
      });
      this.refresh();
    });
  },

  logout() {
    logout();
    wx.showToast({
      title: '已退出',
      icon: 'none'
    });
    this.refresh();
  },

  goMenu() {
    wx.switchTab({
      url: '/pages/index/index'
    });
  },

  goOrders() {
    wx.switchTab({
      url: '/pages/order-list/order-list'
    });
  },

  goAi() {
    wx.switchTab({
      url: '/pages/ai-chat/ai-chat'
    });
  }
});
