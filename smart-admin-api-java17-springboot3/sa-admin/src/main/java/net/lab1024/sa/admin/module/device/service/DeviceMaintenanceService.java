package net.lab1024.sa.admin.module.device.service;

import net.lab1024.sa.admin.module.device.domain.vo.DeviceMaintenancePlanVO;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 设备维护计划服务接口
 *
 * @author SmartAdmin Team
 * @date 2025-11-25
 */
public interface DeviceMaintenanceService {

    /**
     * 获取维护计划详情
     */
    ResponseDTO<DeviceMaintenancePlanVO> getMaintenancePlan(Long planId);

    /**
     * 获取设备维护计划列表
     */
    ResponseDTO<PageResult<DeviceMaintenancePlanVO>> getMaintenancePlanList(Integer pageNum, Integer pageSize, Long deviceId, String planStatus, Long assignedTo);

    /**
     * 创建维护计划
     */
    ResponseDTO<Long> createMaintenancePlan(DeviceMaintenancePlanVO maintenancePlan);

    /**
     * 更新维护计划
     */
    ResponseDTO<Void> updateMaintenancePlan(DeviceMaintenancePlanVO maintenancePlan);

    /**
     * 删除维护计划
     */
    ResponseDTO<Void> deleteMaintenancePlan(Long planId);

    /**
     * 开始维护计划
     */
    ResponseDTO<Void> startMaintenancePlan(Long planId);

    /**
     * 完成维护计划
     */
    ResponseDTO<Void> completeMaintenancePlan(Long planId, String resultNote, String partsUsed, Double costAmount);

    /**
     * 取消维护计划
     */
    ResponseDTO<Void> cancelMaintenancePlan(Long planId, String reason);

    /**
     * 指派维护计划
     */
    ResponseDTO<Void> assignMaintenancePlan(Long planId, Long assignedTo);

    /**
     * 获取我的维护任务
     */
    ResponseDTO<List<DeviceMaintenancePlanVO>> getMyMaintenanceTasks(Long userId, String planStatus);

    /**
     * 获取待处理的维护任务
     */
    ResponseDTO<List<DeviceMaintenancePlanVO>> getPendingMaintenanceTasks();

    /**
     * 获取超期维护任务
     */
    ResponseDTO<List<DeviceMaintenancePlanVO>> getOverdueMaintenanceTasks();

    /**
     * 根据健康评分自动创建维护计划
     */
    ResponseDTO<Long> createMaintenancePlanByHealthScore(Long deviceId, BigDecimal currentScore, String triggerReason);

    /**
     * 获取维护统计信息
     */
    ResponseDTO<Map<String, Object>> getMaintenanceStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取维护人员工作负载
     */
    ResponseDTO<List<Map<String, Object>>> getMaintenanceWorkload(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取设备维护历史
     */
    ResponseDTO<List<DeviceMaintenancePlanVO>> getDeviceMaintenanceHistory(Long deviceId, Integer limit);

    /**
     * 导出维护报告
     */
    ResponseDTO<String> exportMaintenanceReport(LocalDateTime startTime, LocalDateTime endTime, Long assignedTo);

    /**
     * 批量指派维护任务
     */
    ResponseDTO<Integer> batchAssignMaintenanceTasks(List<Long> planIds, Long assignedTo);

    /**
     * 获取维护建议
     */
    ResponseDTO<List<String>> getMaintenanceSuggestions(Long deviceId);
}