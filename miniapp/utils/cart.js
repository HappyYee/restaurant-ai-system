const CART_KEY = 'restaurant_cart';

function getCart() {
  return wx.getStorageSync(CART_KEY) || [];
}

function saveCart(cart) {
  wx.setStorageSync(CART_KEY, cart);
}

function toMoney(value) {
  return Number(value || 0).toFixed(2);
}

function normalizeProduct(product) {
  return {
    id: product.id,
    name: product.name,
    category: product.category,
    price: Number(product.price || 0),
    stock: Number(product.stock || 0),
    imageUrl: product.imageUrl || '',
    cookTime: product.cookTime || 0,
    tasteTags: product.tasteTags || ''
  };
}

function addToCart(product, quantity = 1) {
  const nextProduct = normalizeProduct(product);
  const cart = getCart();
  const index = cart.findIndex((item) => item.id === nextProduct.id);

  if (index >= 0) {
    const current = cart[index];
    cart[index] = {
      ...current,
      quantity: Math.min(current.quantity + quantity, nextProduct.stock || current.quantity + quantity)
    };
  } else {
    cart.push({
      ...nextProduct,
      quantity: Math.max(1, quantity)
    });
  }

  saveCart(cart);
  return cart;
}

function updateQuantity(productId, quantity) {
  const cart = getCart()
    .map((item) => {
      if (item.id !== productId) {
        return item;
      }
      return {
        ...item,
        quantity: Math.max(0, Math.min(quantity, item.stock || quantity))
      };
    })
    .filter((item) => item.quantity > 0);

  saveCart(cart);
  return cart;
}

function removeFromCart(productId) {
  const cart = getCart().filter((item) => item.id !== productId);
  saveCart(cart);
  return cart;
}

function clearCart() {
  saveCart([]);
}

function getCartSummary(cart = getCart()) {
  const totalQuantity = cart.reduce((sum, item) => sum + Number(item.quantity || 0), 0);
  const totalAmount = cart.reduce((sum, item) => sum + Number(item.price || 0) * Number(item.quantity || 0), 0);

  return {
    totalQuantity,
    totalAmount,
    totalAmountText: toMoney(totalAmount)
  };
}

module.exports = {
  addToCart,
  clearCart,
  getCart,
  getCartSummary,
  removeFromCart,
  toMoney,
  updateQuantity
};
