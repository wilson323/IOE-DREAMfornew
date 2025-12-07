package net.lab1024.sa.common.scheduler.dao;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.scheduler.domain.entity.ScheduledJobEntity;

/**
 * 定时任务Dao接口
 * <p>
 * 严格遵循四层架构规范：
 * - 使用@Mapper注解，禁止使用@Repository
 * - 统一使用BaseMapper作为基础接口
 * - 只负责数据访问，不包含业务逻辑
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface ScheduledJobDao extends BaseMapper<ScheduledJobEntity> {

    /**
     * 根据任务名称和分组查询任务
     * <p>
     * 用于任务唯一性校验
     * </p>
     *
     * @param jobName 任务名称
     * @param jobGroup 任务分组
     * @return 任务实体
     */
    @Transactional(readOnly = true)
    default ScheduledJobEntity selectByJobNameAndGroup(String jobName, String jobGroup) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ScheduledJobEntity>()
                .eq(ScheduledJobEntity::getJobName, jobName)
                .eq(ScheduledJobEntity::getJobGroup, jobGroup)
                .eq(ScheduledJobEntity::getDeletedFlag, 0));
    }

    /**
     * 根据状态查询任务列表
     *
     * @param status 状态（1-启用 2-暂停 3-停止）
     * @return 任务列表
     */
    @Transactional(readOnly = true)
    default java.util.List<ScheduledJobEntity> selectByStatus(Integer status) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ScheduledJobEntity>()
                .eq(ScheduledJobEntity::getStatus, status)
                .eq(ScheduledJobEntity::getDeletedFlag, 0)
                .orderByAsc(ScheduledJobEntity::getPriority)
                .orderByAsc(ScheduledJobEntity::getNextExecutionTime));
    }
}
