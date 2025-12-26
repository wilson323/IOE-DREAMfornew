/**
 * 考勤规则模板 API
 * 提供规则模板管理相关接口
 */
import { request } from '/@/utils/request';

/**
 * 分页查询规则模板
 */
export const queryPage = (params) => {
  return request.get('/api/v1/attendance/rule-template/page', { params });
};

/**
 * 查询系统模板列表
 */
export const getSystemTemplates = (category) => {
  return request.get('/api/v1/attendance/rule-template/system', {
    params: { category }
  });
};

/**
 * 查询用户自定义模板列表
 */
export const getUserTemplates = (userId, category) => {
  return request.get(`/api/v1/attendance/rule-template/user/${userId}`, {
    params: { category }
  });
};

/**
 * 查询模板详情
 */
export const getDetail = (templateId) => {
  return request.get(`/api/v1/attendance/rule-template/${templateId}`);
};

/**
 * 根据编码查询模板
 */
export const getByCode = (templateCode) => {
  return request.get(`/api/v1/attendance/rule-template/code/${templateCode}`);
};

/**
 * 创建规则模板
 */
export const create = (data) => {
  return request.post('/api/v1/attendance/rule-template', data);
};

/**
 * 更新规则模板
 */
export const update = (templateId, data) => {
  return request.put(`/api/v1/attendance/rule-template/${templateId}`, data);
};

/**
 * 删除规则模板
 */
export const delete = (templateId) => {
  return request.delete(`/api/v1/attendance/rule-template/${templateId}`);
};

/**
 * 批量删除规则模板
 */
export const batchDelete = (templateIds) => {
  return request.delete('/api/v1/attendance/rule-template/batch', {
    data: templateIds
  });
};

/**
 * 应用模板到规则
 */
export const applyToRule = (templateId, ruleId) => {
  return request.post(`/api/v1/attendance/rule-template/${templateId}/apply/${ruleId}`);
};

/**
 * 导出模板
 */
export const export = (templateId) => {
  return request.get(`/api/v1/attendance/rule-template/${templateId}/export`);
};

/**
 * 导入模板
 */
export const import = (templateJson) => {
  return request.post('/api/v1/attendance/rule-template/import', templateJson, {
    headers: {
      'Content-Type': 'application/json'
    }
  });
};

/**
 * 复制模板
 */
export const copy = (templateId, newTemplateName) => {
  return request.post(`/api/v1/attendance/rule-template/${templateId}/copy`, null, {
    params: { newTemplateName }
  });
};

// 默认导出
export default {
  queryPage,
  getSystemTemplates,
  getUserTemplates,
  getDetail,
  getByCode,
  create,
  update,
  delete,
  batchDelete,
  applyToRule,
  export,
  import,
  copy
};
