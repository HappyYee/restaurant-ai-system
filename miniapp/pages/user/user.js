const { getCurrentUser, login, logout } = require('../../utils/auth');
const { request } = require('../../utils/request');
const cartStore = require('../../utils/cart');

Page({
  data: {
    userInfo: null,
    memberProfile: {},
    memberProgress: 0,
    cartSummary: {
      totalQuantity: 0,
      totalAmountText: '0.00'
    }
  },

  onShow() {
    this.refresh();
  },

  refresh() {
    const userInfo = getCurrentUser();
    this.setData({
      userInfo,
      cartSummary: cartStore.getCartSummary()
    });
    if (userInfo) {
      this.loadMemberProfile();
    } else {
      this.setData({
        memberProfile: {},
        memberProgress: 0
      });
    }
  },

  loadMemberProfile() {
    request({
      url: '/member/profile'
    })
      .then((profile) => {
        const totalSpent = Number(profile.totalSpent || 0);
        const nextNeed = Number(profile.nextLevelNeed || 0);
        const target = nextNeed === 0 ? totalSpent : totalSpent + nextNeed;
        this.setData({
          memberProfile: {
            ...profile,
            totalSpentText: cartStore.toMoney(profile.totalSpent),
            nextLevelNeedText: cartStore.toMoney(profile.nextLevelNeed)
          },
          memberProgress: target ? Math.min(100, Math.round((totalSpent / target) * 100)) : 100
        });
      })
      .catch(() => {});
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
