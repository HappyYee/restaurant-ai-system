const { request } = require('../../utils/request');
const config = require('../../utils/config');
const cartStore = require('../../utils/cart');

Page({
  data: {
    prompt: '',
    products: [],
    loading: true,
    errorMessage: '',
    generating: false,
    plans: [],
    presets: [
      '两个人，60元以内，清淡一点',
      '一个人午餐，要快一点',
      '想吃饱，能接受微辣',
      '主食加饮品，预算40元'
    ]
  },

  onLoad() {
    this.loadProducts();
  },

  loadProducts() {
    request({
      url: '/products'
    })
      .then((products = []) => {
        this.setData({
          products: products.map((product) => ({
            ...product,
            price: Number(product.price || 0),
            stock: Number(product.stock || 0),
            cookTime: Number(product.cookTime || 0)
          })),
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

  usePreset(event) {
    const text = event.currentTarget.dataset.text;
    this.setData({
      prompt: text
    });
    this.generate();
  },

  onInput(event) {
    this.setData({
      prompt: event.detail.value
    });
  },

  generate() {
    if (!this.data.products.length) {
      wx.showToast({
        title: '菜单还没有加载完成',
        icon: 'none'
      });
      return;
    }

    const prompt = this.data.prompt.trim();
    if (!prompt) {
      wx.showToast({
        title: '先说说想吃什么',
        icon: 'none'
      });
      return;
    }

    this.setData({
      generating: true
    });

    setTimeout(() => {
      this.setData({
        plans: this.buildPlans(prompt),
        generating: false
      });
    }, 320);
  },

  buildPlans(prompt) {
    const products = this.data.products.filter((product) => product.stock > 0);
    const text = prompt.toLowerCase();
    const budget = this.extractBudget(text);
    const prefersLight = prompt.includes('清淡') || prompt.includes('健康') || prompt.includes('不辣');
    const prefersFast = prompt.includes('快') || prompt.includes('赶时间');
    const prefersSpicy = prompt.includes('辣');

    const scored = products
      .map((product) => {
        let score = 0;
        const tags = product.tasteTags || '';
        if (prefersLight && (tags.includes('清淡') || tags.includes('健康') || tags.includes('无糖'))) score += 4;
        if (prefersFast && product.cookTime <= 8) score += 3;
        if (prefersSpicy && tags.includes('辣')) score += 3;
        if (tags.includes('热销')) score += 2;
        if (tags.includes('饱腹')) score += 1;
        if (budget && product.price <= budget) score += 1;
        return {
          ...product,
          score
        };
      })
      .sort((a, b) => b.score - a.score || a.price - b.price);

    const first = this.composePlan('推荐方案', scored, budget);
    const second = this.composePlan('加餐方案', scored.slice().reverse(), budget);
    return [first, second].filter((plan) => plan.items.length);
  },

  extractBudget(text) {
    const match = text.match(/(\d+)\s*元/);
    return match ? Number(match[1]) : 0;
  },

  composePlan(name, products, budget) {
    const selected = [];
    let total = 0;

    products.forEach((product) => {
      if (selected.length >= 3) {
        return;
      }
      if (budget && total + product.price > budget && selected.length) {
        return;
      }
      selected.push({
        ...product,
        quantity: 1,
        subtotalText: cartStore.toMoney(product.price)
      });
      total += product.price;
    });

    return {
      name,
      reason: budget ? `按 ${budget} 元预算组合，优先选择库存充足和出餐稳定的菜品。` : '根据口味关键词和当前菜单生成组合。',
      items: selected,
      totalAmount: total,
      totalAmountText: cartStore.toMoney(total)
    };
  },

  addPlan(event) {
    const index = Number(event.currentTarget.dataset.index);
    const plan = this.data.plans[index];
    if (!plan) {
      return;
    }

    plan.items.forEach((item) => {
      cartStore.addToCart(item, item.quantity);
    });

    wx.navigateTo({
      url: '/pages/order-confirm/order-confirm'
    });
  }
});
