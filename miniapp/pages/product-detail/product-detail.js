const { request } = require('../../utils/request');
const cartStore = require('../../utils/cart');

Page({
  data: {
    loading: true,
    product: null,
    tags: [],
    quantity: 1,
    cartQuantity: 0,
    imageFailed: false
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
            originalPrice: Number(product.price || 0),
            memberPrice: Number(product.memberPrice || product.price || 0),
            price: Number(product.memberPrice || product.price || 0),
            priceText: cartStore.toMoney(product.memberPrice || product.price),
            originalPriceText: cartStore.toMoney(product.price),
            stock: Number(product.stock || 0),
            coverText: product.name ? product.name.slice(0, 1) : '餐',
            stockWarningText: this.stockWarningText(Number(product.stock || 0))
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

  onImageError() {
    this.setData({
      imageFailed: true
    });
  },

  stockWarningText(stock) {
    if (stock <= 0) {
      return '已售罄';
    }
    if (stock <= 5) {
      return `仅剩 ${stock} 份，手慢无`;
    }
    return '';
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
      quantity: Math.max(this.data.quantity - 1, 0)
    });
  },

  addToCart() {
    const { product, quantity, cartQuantity } = this.data;
    if (!product) {
      return false;
    }

    if (quantity <= 0) {
      cartStore.removeFromCart(product.id);
      wx.showToast({
        title: cartQuantity ? '已移除购物车' : '请选择数量',
        icon: 'none'
      });
      this.syncCartQuantity();
      return false;
    }

    if (quantity + cartQuantity > product.stock) {
      wx.showToast({
        title: '库存不足',
        icon: 'none'
      });
      return false;
    }

    cartStore.addToCart(product, quantity);
    wx.showToast({
      title: '已加入购物车',
      icon: 'success'
    });
    this.syncCartQuantity();
    return true;
  },

  goConfirm() {
    const added = this.addToCart();
    if (!added) {
      return;
    }

    wx.setStorageSync('orderSource', 0);
    wx.navigateTo({
      url: '/pages/order-confirm/order-confirm'
    });
  }
});
