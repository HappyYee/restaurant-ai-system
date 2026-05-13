const { getCurrentUser, login, logout } = require('../../utils/auth');
const { request } = require('../../utils/request');
const cartStore = require('../../utils/cart');

Page({
  data: {
    userInfo: null,
    avatarText: 'HI',
    memberProfile: {},
    memberProgress: 0,
    nextLevelName: '银卡会员',
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
      avatarText: this.resolveAvatarText(userInfo),
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
        this.setData({
          memberProfile: {
            ...profile,
            totalSpentText: cartStore.toMoney(profile.totalSpent),
            nextLevelNeedText: cartStore.toMoney(profile.nextLevelNeed)
          },
          memberProgress: this.calcMemberProgress(totalSpent, nextNeed),
          nextLevelName: this.resolveNextLevel(profile.memberLevel)
        });
      })
      .catch((error) => {
        console.error('load member profile failed:', error);
        this.setData({
          memberProfile: {},
          memberProgress: 0
        });
      });
  },

  resolveAvatarText(userInfo) {
    if (!userInfo) {
      return 'HI';
    }
    const nickname = String(userInfo.nickname || '').trim();
    return nickname ? nickname.slice(0, 1) : '会';
  },

  calcMemberProgress(totalSpent, nextNeed) {
    const spent = Math.max(0, Number(totalSpent || 0));
    const need = Math.max(0, Number(nextNeed || 0));
    if (need === 0) {
      return spent > 0 ? 100 : 0;
    }
    const target = spent + need;
    if (!target || !Number.isFinite(target)) {
      return 0;
    }
    return Math.max(0, Math.min(100, Math.round((spent / target) * 100)));
  },

  resolveNextLevel(level) {
    if (level === '金卡会员') {
      return '最高等级';
    }
    if (level === '银卡会员') {
      return '金卡会员';
    }
    return '银卡会员';
  },

  login() {
    login(true)
      .then(() => {
        wx.showToast({
          title: '登录成功',
          icon: 'success'
        });
        this.refresh();
      })
      .catch((error) => {
        wx.showToast({
          title: error.message || '登录失败',
          icon: 'none'
        });
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
