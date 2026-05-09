const { request } = require('../../utils/request');
const config = require('../../utils/config');
const cartStore = require('../../utils/cart');

Page({
  data: {
    loading: true,
    errorMessage: '',
    products: [],
    viewProducts: [],
    categories: [],
    activeCategory: '全部',
    cartSummary: {
      totalQuantity: 0,
      totalAmountText: '0.00'
    },
    storeStats: {
      productCount: 0,
      fastCount: 0
    }
  },

  onLoad() {
    this.loadProducts();
  },

  onShow() {
    this.refreshCart();
  },

  onPullDownRefresh() {
    this.loadProducts().then(() => {
      wx.stopPullDownRefresh();
    });
  },

  loadProducts() {
    this.setData({
      loading: true
    });

    return request({
      url: '/products'
    })
      .then((products = []) => {
        const normalized = products.map((product) => ({
          ...product,
          price: Number(product.price || 0),
          priceText: cartStore.toMoney(product.price),
          stock: Number(product.stock || 0),
          cookTime: Number(product.cookTime || 0),
          cartQuantity: 0,
          coverText: product.name ? product.name.slice(0, 1) : '餐',
          tags: (product.tasteTags || '').split(',').filter(Boolean)
        }));

        this.setData({
          products: normalized,
          loading: false,
          errorMessage: '',
          storeStats: {
            productCount: normalized.length,
            fastCount: normalized.filter((item) => item.cookTime <= 8).length
          }
        });
        this.refreshCart();
      })
      .catch(() => {
        this.setData({
          loading: false,
          errorMessage: `未连接到后端：${config.baseUrl}`
        });
      });
  },

  refreshCart() {
    const currentCart = cartStore.getCart();
    const cartMap = currentCart.reduce((map, item) => {
      map[item.id] = item.quantity;
      return map;
    }, {});

    const products = this.data.products.map((product) => ({
      ...product,
      cartQuantity: cartMap[product.id] || 0
    }));
    const categories = this.buildCategories(products);

    this.setData({
      products,
      categories,
      viewProducts: this.filterProducts(products, this.data.activeCategory),
      cartSummary: cartStore.getCartSummary(currentCart)
    });
  },

  buildCategories(products) {
    const categoryCount = products.reduce(
      (map, product) => {
        const category = product.category || '其他';
        map[category] = (map[category] || 0) + 1;
        map['全部'] += 1;
        return map;
      },
      {
        全部: 0
      }
    );

    return Object.keys(categoryCount).map((name) => ({
      name,
      count: categoryCount[name]
    }));
  },

  filterProducts(products, category) {
    if (!category || category === '全部') {
      return products;
    }
    return products.filter((product) => product.category === category);
  },

  setCategory(event) {
    const category = event.currentTarget.dataset.category;
    this.setData({
      activeCategory: category,
      viewProducts: this.filterProducts(this.data.products, category)
    });
  },

  findProduct(productId) {
    return this.data.products.find((product) => product.id === Number(productId));
  },

  addProduct(event) {
    const product = this.findProduct(event.detail.id || event.currentTarget.dataset.id);
    if (!product || product.stock <= product.cartQuantity) {
      wx.showToast({
        title: '库存不足',
        icon: 'none'
      });
      return;
    }

    cartStore.addToCart(product, 1);
    this.refreshCart();
  },

  minusProduct(event) {
    const productId = Number(event.detail.id || event.currentTarget.dataset.id);
    const product = this.findProduct(productId);
    if (!product) {
      return;
    }

    cartStore.updateQuantity(productId, Math.max(product.cartQuantity - 1, 0));
    this.refreshCart();
  },

  goDetail(event) {
    const id = event.detail.id || event.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/product-detail/product-detail?id=${id}`
    });
  },

  goConfirm() {
    if (!this.data.cartSummary.totalQuantity) {
      return;
    }
    wx.navigateTo({
      url: '/pages/order-confirm/order-confirm'
    });
  },

  stopTap() {}
});
