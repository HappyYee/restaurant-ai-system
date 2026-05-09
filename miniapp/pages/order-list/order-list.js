const { login } = require('../../utils/auth');
const { request } = require('../../utils/request');
const config = require('../../utils/config');
const { toMoney } = require('../../utils/cart');

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
    orders: []
  },

  onShow() {
    this.loadOrders();
  },

  onPullDownRefresh() {
    this.loadOrders().then(() => {
      wx.stopPullDownRefresh();
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
