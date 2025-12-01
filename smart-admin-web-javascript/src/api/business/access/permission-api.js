/*
 * 门禁管理-权限API接口
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-13
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { getRequest, postRequest, postEncryptRequest } from '/@/lib/axios';

export const accessPermissionApi = {
  /**
   * 分页查询门禁权限列表
   */
  queryPermissionList: (params) => {
    return postRequest('/access/permission/query', params);
  },

  /**
   * 获取用户门禁权限
   */
  getUserPermissions: (userId) => {
    return getRequest(`/access/permission/user/${userId}`);
  },

  /**
   * 分配门禁权限
   */
  assignPermission: (params) => {
    return postRequest('/access/permission/assign', params);
  },

  /**
   * 批量分配权限
   */
  batchAssignPermissions: (params) => {
    return postRequest('/access/permission/batch/assign', params);
  },

  /**
   * 撤销门禁权限
   */
  revokePermission: (permissionId) => {
    return getRequest(`/access/permission/revoke/${permissionId}`);
  },

  /**
   * 批量撤销权限
   */
  batchRevokePermissions: (permissionIds) => {
    return postRequest('/access/permission/batch/revoke', permissionIds);
  },

  /**
   * 创建临时权限
   */
  createTemporaryPermission: (params) => {
    return postRequest('/access/permission/temporary/create', params);
  },

  /**
   * 更新临时权限
   */
  updateTemporaryPermission: (params) => {
    return postRequest('/access/permission/temporary/update', params);
  },

  /**
   * 取消临时权限
   */
  cancelTemporaryPermission: (permissionId) => {
    return getRequest(`/access/permission/temporary/cancel/${permissionId}`);
  },

  /**
   * 获取权限模板列表
   */
  getPermissionTemplates: () => {
    return getRequest('/access/permission/template/list');
  },

  /**
   * 创建权限模板
   */
  createPermissionTemplate: (params) => {
    return postRequest('/access/permission/template/create', params);
  },

  /**
   * 应用权限模板
   */
  applyPermissionTemplate: (params) => {
    return postRequest('/access/permission/template/apply', params);
  },

  /**
   * 获取权限统计数据
   */
  getPermissionStats: (params) => {
    return postRequest('/access/permission/stats', params);
  },

  /**
   * 权限验证
   */
  validatePermission: (params) => {
    return postRequest('/access/permission/validate', params);
  },

  /**
   * 获取即将过期的权限
   */
  getExpiringPermissions: (days = 7) => {
    return getRequest(`/access/permission/expiring/${days}`);
  }
};