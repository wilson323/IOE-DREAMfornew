/*
 * 门禁管理-通行记录API接口
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-13
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { getRequest, postRequest, postEncryptRequest } from '/@/lib/axios';

export const accessRecordApi = {
  /**
   * 分页查询通行记录
   */
  queryRecordList: (params) => {
    return postRequest('/access/record/query', params);
  },

  /**
   * 获取通行记录详情
   */
  getRecordDetail: (recordId) => {
    return getRequest(`/access/record/detail/${recordId}`);
  },

  /**
   * 获取用户通行记录
   */
  getUserRecords: (params) => {
    return postRequest('/access/record/user/list', params);
  },

  /**
   * 获取设备通行记录
   */
  getDeviceRecords: (params) => {
    return postRequest('/access/record/device/list', params);
  },

  /**
   * 获取实时通行记录
   */
  getRealTimeRecords: (params = {}) => {
    return postRequest('/access/record/realtime', params);
  },

  /**
   * 导出通行记录
   */
  exportRecords: (params) => {
    return postRequest('/access/record/export', params);
  },

  /**
   * 获取异常通行记录
   */
  getAbnormalRecords: (params) => {
    return postRequest('/access/record/abnormal/list', params);
  },

  /**
   * 标记记录为异常
   */
  markRecordAbnormal: (params) => {
    return postRequest('/access/record/mark/abnormal', params);
  },

  /**
   * 处理异常记录
   */
  handleAbnormalRecord: (params) => {
    return postRequest('/access/record/handle/abnormal', params);
  },

  /**
   * 获取通行统计数据
   */
  getAccessStats: (params) => {
    return postRequest('/access/record/stats', params);
  },

  /**
   * 获取时间段通行统计
   */
  getTimeRangeStats: (params) => {
    return postRequest('/access/record/stats/timerange', params);
  },

  /**
   * 获取用户通行统计
   */
  getUserAccessStats: (params) => {
    return postRequest('/access/record/stats/user', params);
  },

  /**
   * 获取设备通行统计
   */
  getDeviceAccessStats: (params) => {
    return postRequest('/access/record/stats/device', params);
  },

  /**
   * 删除通行记录
   */
  deleteRecord: (recordId) => {
    return getRequest(`/access/record/delete/${recordId}`);
  },

  /**
   * 批量删除通行记录
   */
  batchDeleteRecords: (recordIds) => {
    return postRequest('/access/record/batch/delete', recordIds);
  }
};