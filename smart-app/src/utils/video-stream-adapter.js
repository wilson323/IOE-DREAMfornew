/**
 * 视频流网络自适应工具
 * 根据网络类型自动调整码流和协议
 */

/**
 * 获取当前网络类型
 * @returns {Promise<string>} 网络类型: wifi, 4g, 3g, 2g, none, unknown
 */
export async function getNetworkType() {
  try {
    const res = await uni.getNetworkType();
    return res.networkType || 'unknown';
  } catch (error) {
    console.error('[NetworkAdapter] 获取网络类型失败:', error);
    return 'unknown';
  }
}

/**
 * 获取最优视频流配置
 * @returns {Promise<Object>} 视频流配置 { quality, protocol, bitrate }
 */
export async function getOptimalStream() {
  const networkType = await getNetworkType();

  switch (networkType) {
    case 'wifi':
      return {
        quality: '720p',
        protocol: 'HLS',
        bitrate: 2000,
        fps: 25,
        resolution: { width: 1280, height: 720 }
      };
    case '4g':
      return {
        quality: '480p',
        protocol: 'HLS',
        bitrate: 1000,
        fps: 20,
        resolution: { width: 854, height: 480 }
      };
    case '3g':
      return {
        quality: '360p',
        protocol: 'HLS',
        bitrate: 600,
        fps: 15,
        resolution: { width: 640, height: 360 }
      };
    case '2g':
      return {
        quality: '240p',
        protocol: 'HLS',
        bitrate: 400,
        fps: 10,
        resolution: { width: 426, height: 240 }
      };
    default:
      return {
        quality: '360p',
        protocol: 'HLS',
        bitrate: 600,
        fps: 15,
        resolution: { width: 640, height: 360 }
      };
  }
}

/**
 * 网络状态变化监听器
 * @param {Function} callback 网络状态变化回调函数
 * @returns {Function} 取消监听函数
 */
export function onNetworkChange(callback) {
  const handler = (res) => {
    console.log('[NetworkAdapter] 网络状态变化:', res.networkType);
    callback(res);
  };

  uni.onNetworkStatusChange(handler);

  // 返回取消监听函数
  return () => {
    uni.offNetworkStatusChange(handler);
  };
}

/**
 * 获取网络类型对应的图标
 * @param {string} networkType 网络类型
 * @returns {string} 图标类型
 */
export function getNetworkIcon(networkType) {
  const iconMap = {
    wifi: 'wifi',
    4g: 'cellular',
    3g: 'cellular',
    2g: 'cellular',
    none: 'closecircle',
    unknown: 'questioncircle'
  };
  return iconMap[networkType] || 'questioncircle';
}

/**
 * 获取网络类型对应的颜色
 * @param {string} networkType 网络类型
 * @returns {string} 颜色值
 */
export function getNetworkColor(networkType) {
  const colorMap = {
    wifi: '#52c41a',      // 绿色 - 最佳
    4g: '#1890ff',       // 蓝色 - 良好
    3g: '#faad14',       // 橙色 - 一般
    2g: '#ff4d4f',       // 红色 - 较差
    none: '#d9d9d9',     // 灰色 - 无网络
    unknown: '#d9d9d9'   // 灰色 - 未知
  };
  return colorMap[networkType] || '#d9d9d9';
}

/**
 * 获取网络类型对应的文本描述
 * @param {string} networkType 网络类型
 * @returns {string} 文本描述
 */
export function getNetworkText(networkType) {
  const textMap = {
    wifi: 'WiFi',
    4g: '4G网络',
    3g: '3G网络',
    2g: '2G网络',
    none: '无网络',
    unknown: '未知网络'
  };
  return textMap[networkType] || '未知网络';
}

/**
 * 检查网络是否可用
 * @returns {Promise<boolean>} 网络是否可用
 */
export async function isNetworkAvailable() {
  const networkType = await getNetworkType();
  return networkType !== 'none' && networkType !== 'unknown';
}

/**
 * 网络质量评分（0-100）
 * @param {string} networkType 网络类型
 * @returns {number} 质量评分
 */
export function getNetworkQuality(networkType) {
  const qualityMap = {
    wifi: 100,
    4g: 80,
    3g: 50,
    2g: 20,
    none: 0,
    unknown: 0
  };
  return qualityMap[networkType] || 0;
}

/**
 * 视频流自适应管理器
 */
export class VideoStreamAdapter {
  constructor() {
    this.currentNetworkType = null;
    this.currentStreamConfig = null;
    this.unsubscribeNetworkChange = null;
    this.streamChangeCallbacks = [];
  }

  /**
   * 初始化
   */
  async init() {
    // 获取初始网络类型
    this.currentNetworkType = await getNetworkType();
    this.currentStreamConfig = await getOptimalStream();

    // 监听网络变化
    this.unsubscribeNetworkChange = onNetworkChange(async (res) => {
      const oldNetworkType = this.currentNetworkType;
      this.currentNetworkType = res.networkType;

      // 网络类型变化时，重新计算最优流配置
      if (oldNetworkType !== res.networkType) {
        this.currentStreamConfig = await getOptimalStream();

        // 通知所有回调
        this.streamChangeCallbacks.forEach(callback => {
          callback({
            oldNetworkType,
            newNetworkType: res.networkType,
            streamConfig: this.currentStreamConfig
          });
        });
      }
    });

    console.log('[VideoStreamAdapter] 初始化完成', {
      networkType: this.currentNetworkType,
      streamConfig: this.currentStreamConfig
    });
  }

  /**
   * 获取当前流配置
   */
  getCurrentStreamConfig() {
    return this.currentStreamConfig;
  }

  /**
   * 获取当前网络类型
   */
  getCurrentNetworkType() {
    return this.currentNetworkType;
  }

  /**
   * 订阅流配置变化
   */
  onStreamChange(callback) {
    this.streamChangeCallbacks.push(callback);

    // 返回取消订阅函数
    return () => {
      const index = this.streamChangeCallbacks.indexOf(callback);
      if (index > -1) {
        this.streamChangeCallbacks.splice(index, 1);
      }
    };
  }

  /**
   * 销毁
   */
  destroy() {
    if (this.unsubscribeNetworkChange) {
      this.unsubscribeNetworkChange();
      this.unsubscribeNetworkChange = null;
    }
    this.streamChangeCallbacks = [];

    console.log('[VideoStreamAdapter] 已销毁');
  }
}

// 导出单例
export const videoStreamAdapter = new VideoStreamAdapter();

export default {
  getNetworkType,
  getOptimalStream,
  onNetworkChange,
  getNetworkIcon,
  getNetworkColor,
  getNetworkText,
  isNetworkAvailable,
  getNetworkQuality,
  VideoStreamAdapter,
  videoStreamAdapter
};
