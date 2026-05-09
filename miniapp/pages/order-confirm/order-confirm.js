const { login } = require('../../utils/auth');
const { request } = require('../../utils/request');
const cartStore = require('../../utils/cart');

Page({
  data: {
    items: [],
    summary: {
      totalQuantity: 0,
      totalAmountText: '0.00'
    },
    remark: '',
    submitting: false
  },

  onShow() {
    this.loadCart();
  },

  loadCart() {
    const items = cartStore.getCart().map((item) => ({
      ...item,
      subtotalText: cartStore.toMoney(Number(item.price || 0) * Number(item.quantity || 0))
    }));

    this.setData({
      items,
      summary: cartStore.getCartSummary(items)
    });
  },

  increase(event) {
    const id = Number(event.currentTarget.dataset.id);
    const item = this.data.items.find((cartItem) => cartItem.id === id);
    if (!item) {
      return;
    }
    cartStore.updateQuantity(id, item.quantity + 1);
    this.loadCart();
  },

  decrease(event) {
    const id = Number(event.currentTarget.dataset.id);
    const item = this.data.items.find((cartItem) => cartItem.id === id);
    if (!item) {
      return;
    }
    cartStore.updateQuantity(id, item.quantity - 1);
    this.loadCart();
  },

  onRemarkInput(event) {
    this.setData({
      remark: event.detail.value
    });
  },

  submitOrder() {
    if (!this.data.items.length || this.data.submitting) {
      return;
    }

    this.setData({
      submitting: true
    });

    login()
      .then(() =>
        request({
          url: '/orders',
          method: 'POST',
          data: {
            items: this.data.items.map((item) => ({
              productId: item.id,
              quantity: item.quantity
            })),
            remark: this.data.remark,
            source: 0
          }
        })
      )
      .then((order) => {
        cartStore.clearCart();
        wx.redirectTo({
          url: `/pages/order-success/order-success?orderNo=${order.orderNo}&amount=${order.totalAmount}`
        });
      })
      .catch(() => {})
      .then(() => {
        this.setData({
          submitting: false
        });
      });
  },

  goMenu() {
    wx.switchTab({
      url: '/pages/index/index'
    });
  }
});
