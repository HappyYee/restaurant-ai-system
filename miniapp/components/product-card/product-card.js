Component({
  properties: {
    product: {
      type: Object,
      value: {},
      observer() {
        this.setData({
          imageFailed: false
        });
      }
    }
  },

  data: {
    imageFailed: false
  },

  methods: {
    onImageError() {
      this.setData({
        imageFailed: true
      });
    },

    openDetail() {
      this.triggerEvent('cardtap', {
        id: this.data.product.id
      });
    },

    addProduct() {
      this.triggerEvent('addcart', {
        id: this.data.product.id
      });
    },

    minusProduct() {
      this.triggerEvent('minuscart', {
        id: this.data.product.id
      });
    },

    stopTap() {}
  }
});
