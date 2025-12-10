package net.lab1024.sa.common.audit.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.audit.entity.AuditArchiveEntity;

/**
 * 审计归档记录Dao接口
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
public interface AuditArchiveDao extends BaseMapper<AuditArchiveEntity> {

    /**
     * 根据归档编号查询归档记录
     *
     * @param archiveCode 归档编号
     * @return 归档记录
     */
    @Transactional(readOnly = true)
    default AuditArchiveEntity selectByArchiveCode(@Param("archiveCode") String archiveCode) {
        if (archiveCode == null) {
            return null;
        }

        LambdaQueryWrapper<AuditArchiveEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AuditArchiveEntity::getArchiveCode, archiveCode)
                .eq(AuditArchiveEntity::getDeletedFlag, 0)
                .orderByDesc(AuditArchiveEntity::getCreateTime)
                .last("LIMIT 1");

        return selectOne(queryWrapper);
    }

    /**
     * 根据归档时间点查询归档记录
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 归档记录列表
     */
    @Transactional(readOnly = true)
    default List<AuditArchiveEntity> selectByArchiveTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime) {
        LambdaQueryWrapper<AuditArchiveEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AuditArchiveEntity::getDeletedFlag, 0);

        if (startTime != null) {
            queryWrapper.ge(AuditArchiveEntity::getArchiveTimePoint, startTime);
        }
        if (endTime != null) {
            queryWrapper.le(AuditArchiveEntity::getArchiveTimePoint, endTime);
        }

        queryWrapper.orderByDesc(AuditArchiveEntity::getArchiveTimePoint);

        return selectList(queryWrapper);
    }

    /**
     * 根据归档状态查询归档记录
     *
     * @param archiveStatus 归档状态（1-进行中 2-成功 3-失败）
     * @return 归档记录列表
     */
    @Transactional(readOnly = true)
    default List<AuditArchiveEntity> selectByArchiveStatus(@Param("archiveStatus") Integer archiveStatus) {
        if (archiveStatus == null) {
            return new java.util.ArrayList<>();
        }

        LambdaQueryWrapper<AuditArchiveEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AuditArchiveEntity::getArchiveStatus, archiveStatus)
                .eq(AuditArchiveEntity::getDeleted, 0)
                .orderByDesc(AuditArchiveEntity::getCreateTime);

        return selectList(queryWrapper);
    }

    /**
     * 查询最近的归档记录
     *
     * @param limit 查询数量
     * @return 归档记录列表
     */
    @Transactional(readOnly = true)
    default List<AuditArchiveEntity> selectRecentArchives(@Param("limit") Integer limit) {
        LambdaQueryWrapper<AuditArchiveEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AuditArchiveEntity::getDeleted, 0)
                .orderByDesc(AuditArchiveEntity::getCreateTime)
                .last("LIMIT " + (limit != null && limit > 0 ? limit : 10));

        return selectList(queryWrapper);
    }
}
