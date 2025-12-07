/**
 * Jest测试环境设置
 * 
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-05
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

// Mock uni-app全局对象
global.uni = {
  // 存储相关
  getStorageSync: jest.fn(),
  setStorageSync: jest.fn(),
  removeStorageSync: jest.fn(),
  clearStorageSync: jest.fn(),
  
  // 网络请求
  request: jest.fn(),
  uploadFile: jest.fn(),
  downloadFile: jest.fn(),
  
  // 导航相关
  navigateTo: jest.fn(),
  redirectTo: jest.fn(),
  navigateBack: jest.fn(),
  switchTab: jest.fn(),
  reLaunch: jest.fn(),
  
  // UI相关
  showToast: jest.fn(),
  showLoading: jest.fn(),
  hideLoading: jest.fn(),
  showModal: jest.fn(),
  showActionSheet: jest.fn(),
  
  // 图片相关
  chooseImage: jest.fn(),
  previewImage: jest.fn(),
  
  // 网络状态
  getNetworkType: jest.fn(),
  onNetworkStatusChange: jest.fn(),
  
  // 系统信息
  getSystemInfoSync: jest.fn(() => ({
    platform: 'android',
    statusBarHeight: 20,
    windowWidth: 375,
    windowHeight: 667
  })),
  
  // 震动
  vibrateShort: jest.fn(),
  vibrateLong: jest.fn(),
  
  // 扫码
  scanCode: jest.fn(),
  
  // 位置
  getLocation: jest.fn(),
  chooseLocation: jest.fn()
}

// Mock import.meta.env
global.import = {
  meta: {
    env: {
      VITE_APP_API_URL: 'https://api.test.com',
      VITE_APP_WS_URL: 'wss://ws.test.com'
    }
  }
}

// 设置全局测试超时
jest.setTimeout(10000)

