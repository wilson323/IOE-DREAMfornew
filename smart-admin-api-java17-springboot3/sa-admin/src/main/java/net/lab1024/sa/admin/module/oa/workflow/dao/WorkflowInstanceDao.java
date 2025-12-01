package net.lab1024.sa.admin.module.oa.workflow.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.oa.workflow.domain.entity.WorkflowInstanceEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 工作流实例DAO
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
public interface WorkflowInstanceDao extends BaseMapper<WorkflowInstanceEntity> {

    /**
     * 分页查询流程实例
     *
     * @param definitionId 定义ID
     * @param processKey 流程Key
     * @param status 状态
     * @param startUserId 发起人ID
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 实例列表
     */
    List<WorkflowInstanceEntity> selectInstancesByCondition(Long definitionId, String processKey,
                                                           String status, Long startUserId,
                                                           LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 查询用户的流程实例
     *
     * @param userId 用户ID
     * @param status 状态
     * @param limit 限制数量
     * @return 实例列表
     */
    List<WorkflowInstanceEntity> selectByUserId(Long userId, String status, Integer limit);

    /**
     * 查询运行中的实例
     *
     * @return 运行中实例列表
     */
    List<WorkflowInstanceEntity> selectRunningInstances();

    /**
     * 查询超期的实例
     *
     * @param currentTime 当前时间
     * @return 超期实例列表
     */
    List<WorkflowInstanceEntity> selectOverdueInstances(LocalDateTime currentTime);

    /**
     * 统计实例状态数量
     *
     * @param definitionId 定义ID(可选)
     * @param startDate 开始日期(可选)
     * @param endDate 结束日期(可选)
     * @return 状态统计
     */
    Map<String, Integer> countByStatus(Long definitionId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 统计平均处理时间
     *
     * @param definitionId 定义ID(可选)
     * @param startDate 开始日期(可选)
     * @param endDate 结束日期(可选)
     * @return 平均处理时间(小时)
     */
    Double getAverageProcessTime(Long definitionId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 查询流程实例历史
     *
     * @param instanceId 实例ID
     * @return 历史记录
     */
    List<Map<String, Object>> selectInstanceHistory(Long instanceId);

    /**
     * 批量更新实例状态
     *
     * @param instanceIds 实例ID列表
     * @param status 新状态
     * @return 更新数量
     */
    Integer batchUpdateStatus(List<Long> instanceIds, String status);

    /**
     * 查询指定时间范围内的实例
     *
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 实例列表
     */
    List<WorkflowInstanceEntity> selectByTimeRange(LocalDateTime startDate, LocalDateTime endDate);
}