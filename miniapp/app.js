App({
  globalData: {
    storeName: '星禾小馆'
  },

  onLaunch() {
    const systemInfo = wx.getSystemInfoSync();
    this.globalData.statusBarHeight = systemInfo.statusBarHeight || 0;
  }
});
