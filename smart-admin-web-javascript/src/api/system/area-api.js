/*
 * 区域管理
 *
 * @Author:    SmartAdmin
 * @Date:      2025-01-10
 * @Copyright  SmartAdmin
 */
import { getRequest, postRequest } from '/@/lib/axios';

export const areaApi = {
  /**
   * 分页查询区域
   */
  queryPage: (param) => {
    return postRequest('/system/area/page', param);
  },

  /**
   * 查询区域树
   */
  getAreaTree: () => {
    return getRequest('/system/area/tree');
  },

  /**
   * 查询区域详情
   */
  getDetail: (areaId) => {
    return getRequest(`/system/area/detail/${areaId}`);
  },

  /**
   * 新增区域
   */
  add: (param) => {
    return postRequest('/system/area/add', param);
  },

  /**
   * 更新区域
   */
  update: (param) => {
    return postRequest('/system/area/update', param);
  },

  /**
   * 删除区域
   */
  delete: (areaId) => {
    return postRequest(`/system/area/delete/${areaId}`);
  },

  /**
   * 批量删除区域
   */
  batchDelete: (areaIds) => {
    return postRequest('/system/area/batchDelete', areaIds);
  },
};
