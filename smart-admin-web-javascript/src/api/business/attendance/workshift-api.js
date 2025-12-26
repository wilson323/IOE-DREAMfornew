/**
 * 班次配置API
 * 提供班次配置管理的所有后端接口调用
 */
import { request } from '@/utils/request';

export const workShiftApi = {
  /**
   * 查询所有班次
   */
  getAllWorkShifts() {
    return request.get('/api/v1/attendance/workshift/list');
  },

  /**
   * 根据类型查询班次
   * @param {number} shiftType - 班次类型（1-固定 2-弹性 3-轮班 4-临时）
   */
  getWorkShiftsByType(shiftType) {
    return request.get(`/api/v1/attendance/workshift/type/${shiftType}`);
  },

  /**
   * 查询班次详情
   * @param {number} shiftId - 班次ID
   */
  getWorkShiftDetail(shiftId) {
    return request.get(`/api/v1/attendance/workshift/${shiftId}`);
  },

  /**
   * 创建班次
   * @param {Object} data - 班次数据
   * @param {string} data.shiftName - 班次名称
   * @param {number} data.shiftType - 班次类型
   * @param {string} data.startTime - 上班时间 (HH:mm)
   * @param {string} data.endTime - 下班时间 (HH:mm)
   * @param {boolean} data.isOvernight - 是否跨天
   * @param {string} data.crossDayRule - 跨天规则
   */
  createWorkShift(data) {
    return request.post('/api/v1/attendance/workshift', data);
  },

  /**
   * 更新班次
   * @param {number} shiftId - 班次ID
   * @param {Object} data - 班次数据
   */
  updateWorkShift(shiftId, data) {
    return request.put(`/api/v1/attendance/workshift/${shiftId}`, data);
  },

  /**
   * 删除班次
   * @param {number} shiftId - 班次ID
   */
  deleteWorkShift(shiftId) {
    return request.delete(`/api/v1/attendance/workshift/${shiftId}`);
  },

  /**
   * 检测跨天班次
   * @param {string} startTime - 上班时间 (HH:mm)
   * @param {string} endTime - 下班时间 (HH:mm)
   */
  checkCrossDay(startTime, endTime) {
    return request.get('/api/v1/attendance/workshift/check-cross-day', {
      params: { startTime, endTime }
    });
  }
};

export default workShiftApi;
