/**
 * 通知公告API - 移动端版本
 * 企业级通知公告移动端接口
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright: IOE-DREAM (https://ioe-dream.net), Since 2025
 */

import { getRequest, postRequest, putRequest, deleteRequest } from '@/lib/smart-request'

// 通知公告管理
export const noticeApi = {
  /**
   * 获取通知公告列表
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getNoticeList: (params) =>
    getRequest('/api/v1/mobile/oa/notice/list', params),

  /**
   * 获取通知公告详情
   * @param {Number} noticeId 通知公告ID
   * @returns {Promise}
   */
  getNoticeDetail: (noticeId) =>
    getRequest(`/api/v1/mobile/oa/notice/${noticeId}`),

  /**
   * 标记通知为已读
   * @param {Number} noticeId 通知公告ID
   * @returns {Promise}
   */
  markAsRead: (noticeId) =>
    putRequest(`/api/v1/mobile/oa/notice/${noticeId}/read`),

  /**
   * 获取我的通知列表
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getMyNotices: (params) =>
    getRequest('/api/v1/mobile/oa/notice/my-list', params),

  /**
   * 获取未读通知数量
   * @returns {Promise}
   */
  getUnreadCount: () =>
    getRequest('/api/v1/mobile/oa/notice/unread-count'),

  /**
   * 批量标记已读
   * @param {Array} noticeIds 通知公告ID数组
   * @returns {Promise}
   */
  batchMarkAsRead: (noticeIds) =>
    putRequest('/api/v1/mobile/oa/notice/batch-read', { noticeIds }),

  /**
   * 获取通知类型列表
   * @returns {Promise}
   */
  getNoticeTypes: () =>
    getRequest('/api/v1/mobile/oa/notice/types'),

  /**
   * 按类型获取通知
   * @param {String} type 通知类型
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getNoticesByType: (type, params) =>
    getRequest(`/api/v1/mobile/oa/notice/type/${type}`, params),

  /**
   * 搜索通知
   * @param {Object} params 搜索参数
   * @returns {Promise}
   */
  searchNotices: (params) =>
    getRequest('/api/v1/mobile/oa/notice/search', params),

  /**
   * 获取通知统计
   * @returns {Promise}
   */
  getNoticeStatistics: () =>
    getRequest('/api/v1/mobile/oa/notice/statistics'),

  /**
   * 获取我的查看历史
   * @param {Number} userId 用户ID
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getViewHistory: (userId, params) =>
    getRequest(`/api/v1/mobile/oa/notice/view-history/${userId}`, params)
}

// 导出所有API
export default {
  ...noticeApi
}