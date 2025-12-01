package net.lab1024.sa.admin.module.device.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.device.domain.entity.DeviceMaintenancePlanEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备维护计划DAO
 *
 * @author SmartAdmin Team
 * @date 2025-11-25
 */
@Mapper
public interface DeviceMaintenancePlanDao extends BaseMapper<DeviceMaintenancePlanEntity> {

    /**
     * 获取设备当前有效的维护计划
     */
    List<DeviceMaintenancePlanEntity> getActivePlansByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 获取待处理的维护计划
     */
    List<DeviceMaintenancePlanEntity> getPendingPlans(@Param("assignedTo") Long assignedTo);

    /**
     * 获取超期的维护计划
     */
    List<DeviceMaintenancePlanEntity> getOverduePlans(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 获取指定时间范围内的维护计划
     */
    List<DeviceMaintenancePlanEntity> getPlansByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 获取维护计划统计信息
     */
    MaintenanceStatistics getMaintenanceStatistics(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 更新维护计划状态
     */
    int updatePlanStatus(
            @Param("planId") Long planId,
            @Param("oldStatus") String oldStatus,
            @Param("newStatus") String newStatus,
            @Param("updateUserId") Long updateUserId
    );

    /**
     * 获取维护人员工作负载
     */
    List<MaintenanceWorkload> getMaintenanceWorkload(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 获取设备维护历史
     */
    List<DeviceMaintenancePlanEntity> getMaintenanceHistory(
            @Param("deviceId") Long deviceId,
            @Param("limit") Integer limit
    );

    /**
     * 维护统计信息
     */
    class MaintenanceStatistics {
        private Long totalPlans;
        private Long completedPlans;
        private Long pendingPlans;
        private Long inProgressPlans;
        private Long cancelledPlans;
        private Double completionRate;
        private Double onTimeCompletionRate;

        // getters and setters
        public Long getTotalPlans() { return totalPlans; }
        public void setTotalPlans(Long totalPlans) { this.totalPlans = totalPlans; }
        public Long getCompletedPlans() { return completedPlans; }
        public void setCompletedPlans(Long completedPlans) { this.completedPlans = completedPlans; }
        public Long getPendingPlans() { return pendingPlans; }
        public void setPendingPlans(Long pendingPlans) { this.pendingPlans = pendingPlans; }
        public Long getInProgressPlans() { return inProgressPlans; }
        public void setInProgressPlans(Long inProgressPlans) { this.inProgressPlans = inProgressPlans; }
        public Long getCancelledPlans() { return cancelledPlans; }
        public void setCancelledPlans(Long cancelledPlans) { this.cancelledPlans = cancelledPlans; }
        public Double getCompletionRate() { return completionRate; }
        public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }
        public Double getOnTimeCompletionRate() { return onTimeCompletionRate; }
        public void setOnTimeCompletionRate(Double onTimeCompletionRate) { this.onTimeCompletionRate = onTimeCompletionRate; }
    }

    /**
     * 维护工作负载
     */
    class MaintenanceWorkload {
        private Long userId;
        private String userName;
        private Long pendingCount;
        private Long inProgressCount;
        private Long completedCount;
        private Double avgCompletionTime;

        // getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public Long getPendingCount() { return pendingCount; }
        public void setPendingCount(Long pendingCount) { this.pendingCount = pendingCount; }
        public Long getInProgressCount() { return inProgressCount; }
        public void setInProgressCount(Long inProgressCount) { this.inProgressCount = inProgressCount; }
        public Long getCompletedCount() { return completedCount; }
        public void setCompletedCount(Long completedCount) { this.completedCount = completedCount; }
        public Double getAvgCompletionTime() { return avgCompletionTime; }
        public void setAvgCompletionTime(Double avgCompletionTime) { this.avgCompletionTime = avgCompletionTime; }
    }
}