/*
 * 考勤管理 API
 * 
 * 根据API契约文档和已创建的Controller实现
 * 
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */
import { getRequest, postRequest } from '/@/lib/axios';

export const attendanceApi = {
  // ==================== 考勤记录管理 ====================

  /**
   * 分页查询考勤记录
   */
  queryAttendanceRecords: (params) => {
    return postRequest('/attendance/record/query', params);
  },

  /**
   * 获取考勤记录统计
   */
  getAttendanceRecordStatistics: (params) => {
    return getRequest('/attendance/record/statistics', params);
  },
};

