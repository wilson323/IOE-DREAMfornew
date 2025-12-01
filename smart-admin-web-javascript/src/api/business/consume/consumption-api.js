/*
 * 消费管理 API
 *
 * @Author:    SmartAdmin
 * @Date:      2025-11-04
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */
import { getRequest, postRequest } from '/@/lib/axios';

export const consumptionApi = {
  /**
   * 查询Dashboard统计数据
   */
  queryDashboardStats: () => {
    return getRequest('/consumption/dashboard/stats');
  },

  /**
   * 查询最近活动
   */
  queryRecentActivities: (params) => {
    return postRequest('/consumption/dashboard/activities', params);
  },

  /**
   * 查询系统通知
   */
  querySystemNotice: () => {
    return getRequest('/consumption/dashboard/notice');
  },
};

