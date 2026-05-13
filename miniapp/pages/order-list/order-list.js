const { login } = require('../../utils/auth');
const { request } = require('../../utils/request');
const config = require('../../utils/config');
const cartStore = require('../../utils/cart');
const { toMoney } = cartStore;

const statusMap = {
  0: {
    text: '待处理',
    className: 'pending'
  },
  1: {
    text: '制作中',
    className: 'making'
  },
  2: {
    text: '已完成',
    className: 'done'
  },
  3: {
    text: '已取消',
    className: 'cancelled'
  }
};

Page({
  data: {
    loading: true,
    errorMessage: '',
    orders: [],
    hotProducts: []
  },

  onShow() {
    this.loadOrders();
    this.loadHotProducts();
  },

  onPullDownRefresh() {
    this.loadOrders().then(
      () => wx.stopPullDownRefresh(),
      () => wx.stopPullDownRefresh()
    );
  },

  loadHotProducts() {
    return request({
      url: '/products/hot?limit=5'
    })
      .then((products = []) => {
        this.setData({
          hotProducts: products.map((product) => ({
            ...product,
            price: Number(product.memberPrice || product.price || 0),
            priceText: toMoney(product.memberPrice || product.price),
            coverText: product.name ? product.name.slice(0, 1) : '餐'
          }))
        });
      })
      .catch((error) => {
        console.error('load hot products failed:', error);
      });
  },

  addHotProduct(event) {
    const id = Number(event.currentTarget.dataset.id);
    const product = this.data.hotProducts.find((item) => item.id === id);
    if (!product) {
      return;
    }
    cartStore.addToCart(product, 1);
    wx.showToast({
      title: '已加入购物车',
      icon: 'success'
    });
  },

  goProduct(event) {
    const id = event.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/product-detail/product-detail?id=${id}`
    });
  },

  loadOrders() {
    this.setData({
      loading: true
    });

    return login()
      .then(() =>
        request({
          url: '/orders/my'
        })
      )
      .then((orders = []) => {
        const nextOrders = orders.map((order) => {
          const status = statusMap[order.status] || statusMap[0];
          return {
            ...order,
            statusText: status.text,
            statusClass: status.className,
            totalAmountText: toMoney(order.totalAmount),
            createTimeText: this.formatTime(order.createTime),
            itemText: (order.items || []).map((item) => `${item.productName}×${item.quantity}`).join('、')
          };
        });

        this.setData({
          orders: nextOrders,
          errorMessage: '',
          loading: false
        });
      })
      .catch(() => {
        this.setData({
          errorMessage: `未连接到后端：${config.baseUrl}`,
          loading: false
        });
      });
  },

  formatTime(value) {
    if (!value) {
      return '';
    }
    return String(value).replace('T', ' ').slice(0, 16);
  },

  goMenu() {
    wx.switchTab({
      url: '/pages/index/index'
    });
  }
});
