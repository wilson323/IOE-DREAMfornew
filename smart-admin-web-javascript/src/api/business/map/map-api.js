/**
 * 电子地图API接口
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */

import { request } from '@/utils/request';

export const mapApi = {
  /**
   * 获取所有设备位置信息
   * @returns {Promise} 设备位置列表
   */
  getDeviceLocations() {
    return request.get('/api/map/device/locations');
  },

  /**
   * 获取指定区域的设备列表
   * @param {number} areaId - 区域ID
   * @returns {Promise} 设备列表
   */
  getAreaDevices(areaId) {
    return request.get(`/api/map/area/${areaId}/devices`);
  },

  /**
   * 获取设备详情
   * @param {string} deviceId - 设备ID
   * @returns {Promise} 设备详情
   */
  getDeviceDetail(deviceId) {
    return request.get(`/api/map/device/${deviceId}`);
  },

  /**
   * 获取区域列表
   * @returns {Promise} 区域列表
   */
  getAreaList() {
    return request.get('/api/map/areas');
  },

  /**
   * 获取设备实时状态
   * @param {string} deviceId - 设备ID
   * @returns {Promise} 设备状态
   */
  getDeviceStatus(deviceId) {
    return request.get(`/api/map/device/${deviceId}/status`);
  },

  /**
   * 根据业务模块获取设备
   * @param {string} businessModule - 业务模块 (access/attendance/consume/video/visitor)
   * @returns {Promise} 设备列表
   */
  getDevicesByModule(businessModule) {
    return request.get(`/api/map/devices/module/${businessModule}`);
  },

  /**
   * 根据状态获取设备
   * @param {number} status - 设备状态 (1-在线 2-离线 3-故障 4-停用)
   * @returns {Promise} 设备列表
   */
  getDevicesByStatus(status) {
    return request.get(`/api/map/devices/status/${status}`);
  }
};
