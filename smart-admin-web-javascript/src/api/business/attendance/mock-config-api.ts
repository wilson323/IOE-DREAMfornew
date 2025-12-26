/**
 * Mock配置 API
 * 提供Mock配置管理和数据生成相关接口
 */
import { request } from '/@/utils/request';

/**
 * 创建Mock配置
 */
export const createMockConfig = (data) => {
  return request.post('/api/v1/attendance/mock-config', data);
};

/**
 * 更新Mock配置
 */
export const updateMockConfig = (data) => {
  return request.put('/api/v1/attendance/mock-config', data);
};

/**
 * 删除Mock配置
 */
export const deleteMockConfig = (configId) => {
  return request.delete(`/api/v1/attendance/mock-config/${configId}`);
};

/**
 * 查询Mock配置
 */
export const getMockConfig = (configId) => {
  return request.get(`/api/v1/attendance/mock-config/${configId}`);
};

/**
 * 查询所有Mock配置
 */
export const getAllMockConfigs = () => {
  return request.get('/api/v1/attendance/mock-config/list');
};

/**
 * 按类型查询Mock配置
 */
export const getMockConfigsByType = (configType) => {
  return request.get(`/api/v1/attendance/mock-config/type/${configType}`);
};

/**
 * 生成Mock数据
 */
export const generateMockData = (configId) => {
  return request.post(`/api/v1/attendance/mock-config/generate/${configId}`);
};

/**
 * 按类型生成Mock数据
 */
export const generateMockDataByType = (configType, mockScenario = 'NORMAL') => {
  return request.post('/api/v1/attendance/mock-config/generate', null, {
    params: { configType, mockScenario }
  });
};

/**
 * 启用Mock配置
 */
export const enableMockConfig = (configId) => {
  return request.post(`/api/v1/attendance/mock-config/${configId}/enable`);
};

/**
 * 禁用Mock配置
 */
export const disableMockConfig = (configId) => {
  return request.post(`/api/v1/attendance/mock-config/${configId}/disable`);
};

/**
 * 检查Mock是否启用
 */
export const isMockEnabled = (configType) => {
  return request.get(`/api/v1/attendance/mock-config/check/${configType}`);
};

// 默认导出
export default {
  createMockConfig,
  updateMockConfig,
  deleteMockConfig,
  getMockConfig,
  getAllMockConfigs,
  getMockConfigsByType,
  generateMockData,
  generateMockDataByType,
  enableMockConfig,
  disableMockConfig,
  isMockEnabled
};
