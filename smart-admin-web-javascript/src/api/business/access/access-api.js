/*
 * 门禁管理 API
 * 
 * 根据API契约文档和已创建的Controller实现
 * 
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */
import { getRequest, postRequest, putRequest, deleteRequest } from '/@/lib/axios';

export const accessApi = {
  // ==================== 门禁记录管理 ====================

  /**
   * 分页查询门禁记录
   */
  queryAccessRecords: (params) => {
    return postRequest('/access/record/query', params);
  },

  /**
   * 获取门禁记录统计
   */
  getAccessRecordStatistics: (params) => {
    return getRequest('/access/record/statistics', params);
  },

  // ==================== 设备管理 ====================

  /**
   * 分页查询设备
   */
  queryDevices: (params) => {
    return postRequest('/access/device/query', params);
  },

  /**
   * 查询设备详情
   */
  getDeviceDetail: (deviceId) => {
    return getRequest(`/access/device/${deviceId}`);
  },

  /**
   * 添加设备
   */
  addDevice: (data) => {
    return postRequest('/access/device/add', data);
  },

  /**
   * 更新设备
   */
  updateDevice: (data) => {
    return putRequest('/access/device/update', data);
  },

  /**
   * 删除设备
   */
  deleteDevice: (deviceId) => {
    return deleteRequest(`/access/device/${deviceId}`);
  },

  /**
   * 更新设备状态
   */
  updateDeviceStatus: (params) => {
    return postRequest('/access/device/status/update', null, params);
  },
};

