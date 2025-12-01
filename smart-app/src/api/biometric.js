import { getRequest, postRequest } from '@/lib/smart-request';

/**
 * 生物识别相关 API
 */
export const biometricApi = {
  /**
   * 注册生物特征模板
   * @param {Object} param 请求参数
   */
  registerTemplate: (param) => postRequest('/smart/biometric/mobile/register', param),

  /**
   * 验证生物特征
   * @param {Object} param 请求参数
   */
  verify: (param) => postRequest('/smart/biometric/mobile/verify', param),

  /**
   * 申请离线令牌
   * @param {Object} param 请求参数
   */
  requestOfflineToken: (param) => postRequest('/smart/biometric/mobile/offline-token', param),

  /**
   * 获取支持的生物识别类型
   */
  getSupportedTypes: () => getRequest('/smart/biometric/mobile/types'),
};

