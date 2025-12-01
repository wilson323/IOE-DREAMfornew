package net.lab1024.sa.admin.module.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.consume.domain.entity.AbnormalOperationLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 异常操作日志 DAO
 *
 * @author SmartAdmin Team
 * @since 2025-11-19
 */
@Mapper
public interface AbnormalOperationLogDao extends BaseMapper<AbnormalOperationLogEntity> {

    /**
     * 根据用户ID查询异常日志
     */
    List<AbnormalOperationLogEntity> selectByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 根据异常类型查询
     */
    List<AbnormalOperationLogEntity> selectByAnomalyType(@Param("anomalyType") String anomalyType, @Param("limit") Integer limit);

    /**
     * 根据时间范围查询
     */
    List<AbnormalOperationLogEntity> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 根据风险等级查询
     */
    List<AbnormalOperationLogEntity> selectByRiskLevel(@Param("riskLevel") String riskLevel, @Param("limit") Integer limit);

    /**
     * 查询未处理的异常
     */
    List<AbnormalOperationLogEntity> selectUnhandled();

    /**
     * 根据处理状态查询
     */
    List<AbnormalOperationLogEntity> selectByHandleStatus(@Param("handleStatus") Integer handleStatus);
}