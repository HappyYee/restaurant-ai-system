const { getCurrentUser, login } = require('../../utils/auth');
const { request } = require('../../utils/request');
const cartStore = require('../../utils/cart');

Page({
  data: {
    items: [],
    summary: {
      totalQuantity: 0,
      totalAmountText: '0.00'
    },
    memberProfile: {},
    pointOptions: [],
    selectedPoints: 0,
    pointsDiscountText: '0.00',
    payableText: '0.00',
    remark: '',
    submitting: false
  },

  onShow() {
    this.loadCart();
    this.loadMemberProfile();
  },

  loadCart() {
    const items = cartStore.getCart().map((item) => ({
      ...item,
      subtotalText: cartStore.toMoney(Number(item.price || 0) * Number(item.quantity || 0)),
      originalSubtotalText: cartStore.toMoney(Number(item.originalPrice || item.price || 0) * Number(item.quantity || 0))
    }));

    this.setData({
      items,
      summary: cartStore.getCartSummary(items)
    }, () => this.refreshPointPreview());
  },

  loadMemberProfile() {
    if (!getCurrentUser()) {
      this.setData({
        memberProfile: {},
        pointOptions: [],
        selectedPoints: 0
      }, () => this.refreshPointPreview());
      return;
    }
    request({
      url: '/member/profile'
    })
      .then((profile) => {
        this.setData({
          memberProfile: profile || {}
        }, () => this.refreshPointPreview());
      })
      .catch(() => {
        this.setData({
          memberProfile: {},
          pointOptions: [],
          selectedPoints: 0
        }, () => this.refreshPointPreview());
      });
  },

  refreshPointPreview() {
    const totalAmount = Number(this.data.summary.totalAmount || 0);
    const profile = this.data.memberProfile || {};
    const pointOptions = this.buildPointOptions(totalAmount, profile);
    const maxPoints = pointOptions.length ? pointOptions[pointOptions.length - 1].points : 0;
    const selectedPoints = Math.min(Number(this.data.selectedPoints || 0), maxPoints);
    const discount = selectedPoints / 100;
    const payable = Math.max(0, totalAmount - discount);
    this.setData({
      pointOptions,
      selectedPoints,
      pointsDiscountText: cartStore.toMoney(discount),
      payableText: cartStore.toMoney(payable)
    });
  },

  buildPointOptions(totalAmount, profile) {
    const availablePoints = Math.floor(Number(profile.points || 0) / 50) * 50;
    if (!availablePoints || !totalAmount) {
      return [];
    }
    const level = profile.memberLevel || '普通会员';
    const levelCap = level === '金卡会员' ? 1500 : level === '银卡会员' ? 1000 : 500;
    const orderCap = Math.floor((totalAmount * 0.1) / 0.5) * 50;
    const maxPoints = Math.min(availablePoints, levelCap, orderCap);
    const candidates = [];
    for (let points = 50; points <= maxPoints; points += 50) {
      candidates.push(points);
    }
    return candidates.map((points) => ({
      points,
      discountText: cartStore.toMoney(points / 100)
    }));
  },

  selectPoints(event) {
    const points = Number(event.currentTarget.dataset.points || 0);
    this.setData({
      selectedPoints: points
    }, () => this.refreshPointPreview());
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
            source: wx.getStorageSync('orderSource') || 0,
            redeemPoints: this.data.selectedPoints
          }
        })
      )
      .then((order) => {
        cartStore.clearCart();
        wx.removeStorageSync('orderSource');
        wx.redirectTo({
          url: `/pages/order-success/order-success?orderNo=${order.orderNo}&amount=${order.totalAmount}&pointsUsed=${order.pointsUsed || 0}&pointsEarned=${order.pointsEarned || 0}&pointsDiscount=${order.pointsDiscount || 0}`
        });
      })
      .catch((error) => {
        console.error('submit order failed:', error);
        wx.showToast({
          title: error.message || '订单提交失败',
          icon: 'none'
        });
      })
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
