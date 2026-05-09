const { request } = require('../../utils/request');
const cartStore = require('../../utils/cart');

Page({
  data: {
    loading: true,
    product: null,
    tags: [],
    quantity: 1,
    cartQuantity: 0
  },

  onLoad(options) {
    this.productId = Number(options.id);
    this.loadProduct();
  },

  onShow() {
    this.syncCartQuantity();
  },

  loadProduct() {
    request({
      url: `/products/${this.productId}`
    })
      .then((product) => {
        this.setData({
          loading: false,
          product: {
            ...product,
            price: Number(product.price || 0),
            priceText: cartStore.toMoney(product.price),
            stock: Number(product.stock || 0),
            coverText: product.name ? product.name.slice(0, 1) : '餐'
          },
          tags: (product.tasteTags || '').split(',').filter(Boolean)
        });
        this.syncCartQuantity();
      })
      .catch(() => {
        this.setData({
          loading: false
        });
      });
  },

  syncCartQuantity() {
    if (!this.data.product) {
      return;
    }

    const cartItem = cartStore.getCart().find((item) => item.id === this.data.product.id);
    this.setData({
      cartQuantity: cartItem ? cartItem.quantity : 0
    });
  },

  increase() {
    const { product, quantity, cartQuantity } = this.data;
    if (!product) {
      return;
    }

    if (quantity + cartQuantity >= product.stock) {
      wx.showToast({
        title: '库存不足',
        icon: 'none'
      });
      return;
    }

    this.setData({
      quantity: quantity + 1
    });
  },

  decrease() {
    this.setData({
      quantity: Math.max(this.data.quantity - 1, 1)
    });
  },

  addToCart() {
    const { product, quantity } = this.data;
    if (!product) {
      return;
    }

    cartStore.addToCart(product, quantity);
    wx.showToast({
      title: '已加入购物车',
      icon: 'success'
    });
    this.syncCartQuantity();
  },

  goConfirm() {
    this.addToCart();
    setTimeout(() => {
      wx.navigateTo({
        url: '/pages/order-confirm/order-confirm'
      });
    }, 260);
  }
});
