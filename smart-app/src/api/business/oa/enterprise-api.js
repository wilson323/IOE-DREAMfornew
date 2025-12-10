/**
 * 企业管理API - 移动端版本
 * 企业级企业管理移动端接口
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright: IOE-DREAM (https://ioe-dream.net), Since 2025
 */

import { getRequest, postRequest, putRequest, deleteRequest } from '@/lib/smart-request'

// 企业基本信息管理
export const enterpriseApi = {
  /**
   * 创建企业
   * @param {Object} data 企业数据
   * @param {String} data.enterpriseName 企业名称
   * @param {String} data.enterpriseCode 企业编码
   * @param {String} data.contactPerson 联系人
   * @param {String} data.contactPhone 联系电话
   * @param {String} data.contactEmail 联系邮箱
   * @param {String} data.address 企业地址
   * @returns {Promise}
   */
  createEnterprise: (data) => postRequest('/api/v1/mobile/oa/enterprise/create', data),

  /**
   * 获取企业详情
   * @param {Number} enterpriseId 企业ID
   * @returns {Promise}
   */
  getEnterpriseDetail: (enterpriseId) =>
    getRequest(`/api/v1/mobile/oa/enterprise/${enterpriseId}`),

  /**
   * 分页查询企业列表
   * @param {Object} params 查询参数
   * @param {String} params.enterpriseName 企业名称
   * @param {String} params.enterpriseCode 企业编码
   * @param {String} params.status 状态
   * @param {Number} params.pageNum 页码
   * @param {Number} params.pageSize 页大小
   * @returns {Promise}
   */
  getEnterpriseList: (params) =>
    postRequest('/api/v1/mobile/oa/enterprise/page', params),

  /**
   * 更新企业信息
   * @param {Number} enterpriseId 企业ID
   * @param {Object} data 企业数据
   * @returns {Promise}
   */
  updateEnterprise: (enterpriseId, data) =>
    putRequest(`/api/v1/mobile/oa/enterprise/${enterpriseId}`, data),

  /**
   * 删除企业
   * @param {Number} enterpriseId 企业ID
   * @returns {Promise}
   */
  deleteEnterprise: (enterpriseId) =>
    deleteRequest(`/api/v1/mobile/oa/enterprise/${enterpriseId}`),

  /**
   * 启用/禁用企业
   * @param {Number} enterpriseId 企业ID
   * @param {Boolean} enabled 是否启用
   * @returns {Promise}
   */
  updateEnterpriseStatus: (enterpriseId, enabled) =>
    putRequest(`/api/v1/mobile/oa/enterprise/${enterpriseId}/status`, { enabled })
}

// 企业部门管理
export const departmentApi = {
  /**
   * 获取企业部门列表
   * @param {Number} enterpriseId 企业ID
   * @returns {Promise}
   */
  getDepartmentList: (enterpriseId) =>
    getRequest(`/api/v1/mobile/oa/enterprise/${enterpriseId}/departments`),

  /**
   * 获取部门详情
   * @param {Number} departmentId 部门ID
   * @returns {Promise}
   */
  getDepartmentDetail: (departmentId) =>
    getRequest(`/api/v1/mobile/oa/department/${departmentId}`),

  /**
   * 创建部门
   * @param {Object} data 部门数据
   * @returns {Promise}
   */
  createDepartment: (data) =>
    postRequest('/api/v1/mobile/oa/department/create', data),

  /**
   * 更新部门
   * @param {Number} departmentId 部门ID
   * @param {Object} data 部门数据
   * @returns {Promise}
   */
  updateDepartment: (departmentId, data) =>
    putRequest(`/api/v1/mobile/oa/department/${departmentId}`, data)
}

// 企业员工管理
export const employeeApi = {
  /**
   * 获取企业员工列表
   * @param {Number} enterpriseId 企业ID
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getEmployeeList: (enterpriseId, params) =>
    getRequest(`/api/v1/mobile/oa/enterprise/${enterpriseId}/employees`, params),

  /**
   * 获取员工详情
   * @param {Number} employeeId 员工ID
   * @returns {Promise}
   */
  getEmployeeDetail: (employeeId) =>
    getRequest(`/api/v1/mobile/oa/employee/${employeeId}`),

  /**
   * 创建员工
   * @param {Object} data 员工数据
   * @returns {Promise}
   */
  createEmployee: (data) =>
    postRequest('/api/v1/mobile/oa/employee/create', data),

  /**
   * 更新员工
   * @param {Number} employeeId 员工ID
   * @param {Object} data 员工数据
   * @returns {Promise}
   */
  updateEmployee: (employeeId, data) =>
    putRequest(`/api/v1/mobile/oa/employee/${employeeId}`, data)
}

// 导出所有API
export default {
  ...enterpriseApi,
  ...departmentApi,
  ...employeeApi
}

