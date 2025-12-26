/**
 * 设备批量导入 API
 */
import { request } from '/@/utils/request';

/**
 * 上传Excel文件并解析
 */
export const uploadFile = (file, batchName) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('batchName', batchName);

  return request({
    url: '/api/v1/access/device-import/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

/**
 * 执行导入操作
 */
export const executeImport = (batchId) => {
  return request({
    url: `/api/v1/access/device-import/${batchId}/execute`,
    method: 'post',
  });
};

/**
 * 分页查询导入批次
 */
export const queryImportBatches = (params) => {
  return request({
    url: '/api/v1/access/device-import/page',
    method: 'get',
    params,
  });
};

/**
 * 查询导入批次详情
 */
export const getImportBatchDetail = (batchId) => {
  return request({
    url: `/api/v1/access/device-import/${batchId}`,
    method: 'get',
  });
};

/**
 * 查询批次的错误列表
 */
export const queryBatchErrors = (batchId) => {
  return request({
    url: `/api/v1/access/device-import/${batchId}/errors`,
    method: 'get',
  });
};

/**
 * 获取导入统计信息
 */
export const getImportStatistics = () => {
  return request({
    url: '/api/v1/access/device-import/statistics',
    method: 'get',
  });
};

/**
 * 删除导入批次
 */
export const deleteImportBatch = (batchId) => {
  return request({
    url: `/api/v1/access/device-import/${batchId}`,
    method: 'delete',
  });
};

/**
 * 下载导入模板
 */
export const downloadTemplate = () => {
  return request({
    url: '/api/v1/access/device-import/template',
    method: 'get',
    responseType: 'blob',
  });
};

/**
 * 导出错误记录
 */
export const exportErrors = (batchId) => {
  return request({
    url: `/api/v1/access/device-import/${batchId}/export-errors`,
    method: 'get',
    responseType: 'blob',
  });
};

// 默认导出所有API
export default {
  uploadFile,
  executeImport,
  queryImportBatches,
  getImportBatchDetail,
  queryBatchErrors,
  getImportStatistics,
  deleteImportBatch,
  downloadTemplate,
  exportErrors,
};
