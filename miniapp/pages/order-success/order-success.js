const { toMoney } = require('../../utils/cart');

Page({
  data: {
    orderNo: '',
    amountText: '0.00',
    pointsUsed: 0,
    pointsEarned: 0,
    pointsDiscountText: '0.00'
  },

  onLoad(options) {
    this.setData({
      orderNo: options.orderNo || '',
      amountText: toMoney(options.amount),
      pointsUsed: Number(options.pointsUsed || 0),
      pointsEarned: Number(options.pointsEarned || 0),
      pointsDiscountText: toMoney(options.pointsDiscount)
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
