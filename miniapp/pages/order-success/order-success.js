const { toMoney } = require('../../utils/cart');

Page({
  data: {
    orderNo: '',
    amountText: '0.00'
  },

  onLoad(options) {
    this.setData({
      orderNo: options.orderNo || '',
      amountText: toMoney(options.amount)
    });
  },

  goOrders() {
    wx.switchTab({
      url: '/pages/order-list/order-list'
    });
  },

  goMenu() {
    wx.switchTab({
      url: '/pages/index/index'
    });
  }
});
