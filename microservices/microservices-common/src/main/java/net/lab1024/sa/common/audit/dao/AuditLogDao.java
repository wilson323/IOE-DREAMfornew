package net.lab1024.sa.common.audit.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.audit.entity.AuditLogEntity;

/**
 * 审计日志Dao接口
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
public interface AuditLogDao extends BaseMapper<AuditLogEntity> {

    /**
     * 分页查询审计日志
     *
     * @param userId 用户ID
     * @param moduleName 模块名称
     * @param operationType 操作类型
     * @param resultStatus 结果状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param clientIp 客户端IP
     * @param keyword 关键词
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 审计日志列表
     */
    @Transactional(readOnly = true)
    default List<AuditLogEntity> selectByPage(
            @Param("userId") Long userId,
            @Param("moduleName") String moduleName,
            @Param("operationType") Integer operationType,
            @Param("resultStatus") Integer resultStatus,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("clientIp") String clientIp,
            @Param("keyword") String keyword,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit) {

        LambdaQueryWrapper<AuditLogEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AuditLogEntity::getDeletedFlag, 0);

        if (userId != null) {
            queryWrapper.eq(AuditLogEntity::getUserId, userId);
        }
        if (moduleName != null && !moduleName.isEmpty()) {
            queryWrapper.eq(AuditLogEntity::getModuleName, moduleName);
        }
        if (operationType != null) {
            queryWrapper.eq(AuditLogEntity::getOperationType, operationType);
        }
        if (resultStatus != null) {
            queryWrapper.eq(AuditLogEntity::getResultStatus, resultStatus);
        }
        if (startTime != null) {
            queryWrapper.ge(AuditLogEntity::getCreateTime, startTime);
        }
        if (endTime != null) {
            queryWrapper.le(AuditLogEntity::getCreateTime, endTime);
        }
        if (clientIp != null && !clientIp.isEmpty()) {
            queryWrapper.eq(AuditLogEntity::getClientIp, clientIp);
        }
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like(AuditLogEntity::getUserName, keyword)
                    .or()
                    .like(AuditLogEntity::getOperationDesc, keyword)
                    .or()
                    .like(AuditLogEntity::getRequestUrl, keyword));
        }

        queryWrapper.orderByDesc(AuditLogEntity::getCreateTime);

        if (offset != null && limit != null) {
            queryWrapper.last("LIMIT " + offset + ", " + limit);
        }

        return selectList(queryWrapper);
    }
}
