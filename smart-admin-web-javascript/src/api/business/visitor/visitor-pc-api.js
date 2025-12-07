/*
 * 访客管理 PC端 API
 * 
 * 根据API契约文档和已创建的Controller实现
 * 
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */
import { getRequest, postRequest } from '/@/lib/axios';

export const visitorPcApi = {
  // ==================== 访客管理 ====================

  /**
   * 分页查询访客预约
   */
  queryAppointments: (params) => {
    return postRequest('/visitor/appointment/query', params);
  },

  /**
   * 获取访客统计
   */
  getVisitorStatistics: (params) => {
    return getRequest('/visitor/statistics', params);
  },
};

