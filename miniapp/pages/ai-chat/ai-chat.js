const { request } = require('../../utils/request');
const config = require('../../utils/config');
const { login } = require('../../utils/auth');
const cartStore = require('../../utils/cart');

Page({
  data: {
    prompt: '',
    sessionId: '',
    thinking: [],
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
            originalPrice: Number(product.price || 0),
            memberPrice: Number(product.memberPrice || product.price || 0),
            price: Number(product.memberPrice || product.price || 0),
            memberDiscountLabel: product.memberDiscountLabel || '会员价',
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
      generating: true,
      thinking: ['正在读取会员档案', '正在分析口味、预算和库存']
    });

    login()
      .then(() =>
        request({
          url: '/ai/order-recommend',
          method: 'POST',
          data: {
            message: prompt,
            sessionId: this.data.sessionId
          }
        })
      )
      .then((response) => {
        const plans = this.normalizeAiPlans(response.plans || []);
        this.setData({
          sessionId: response.sessionId || this.data.sessionId,
          thinking: response.thinking || [],
          plans: plans.length ? plans : this.buildPlans(prompt),
          generating: false
        });
      })
      .catch(() => {
        this.setData({
          thinking: ['后端 AI 暂不可用，已切换本地推荐规则', '仍按会员价、库存和口味生成组合'],
          plans: this.buildPlans(prompt),
          generating: false
        });
      });
  },

  normalizeAiPlans(plans) {
    const productMap = this.data.products.reduce((map, product) => {
      map[product.id] = product;
      return map;
    }, {});

    return plans
      .map((plan) => {
        const items = (plan.items || [])
          .map((item) => {
            const product = productMap[item.productId];
            if (!product) {
              return null;
            }
            const quantity = Number(item.quantity || 1);
            const unitPrice = Number(item.unitPrice || product.price || 0);
            return {
              ...product,
              id: item.productId,
              name: item.productName || item.name || product.name,
              price: unitPrice,
              memberPrice: unitPrice,
              quantity,
              subtotalText: cartStore.toMoney(unitPrice * quantity)
            };
          })
          .filter(Boolean);
        const total = items.reduce((sum, item) => sum + Number(item.price || 0) * Number(item.quantity || 0), 0);
        return {
          name: plan.name || 'AI 推荐方案',
          reason: plan.reason || '根据会员价、库存和需求生成。',
          items,
          totalAmount: total,
          totalAmountText: cartStore.toMoney(total)
        };
      })
      .filter((plan) => plan.items.length);
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
    const firstItemIds = first.items.map((item) => item.id);
    const secondCandidates = [
      ...scored.filter((product) => !firstItemIds.includes(product.id)),
      ...scored.filter((product) => firstItemIds.includes(product.id))
    ];
    const second = this.composePlan('加餐方案', secondCandidates, budget);
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

    wx.setStorageSync('orderSource', 1);
    wx.navigateTo({
      url: '/pages/order-confirm/order-confirm'
    });
  }
});
