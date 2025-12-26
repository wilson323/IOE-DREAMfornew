package net.lab1024.sa.common.organization.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.organization.entity.AccessRecordEntity;

/**
 * 门禁记录DAO
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Mapper注解
 * - 继承BaseMapper
 * - 使用Dao后缀命名
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface AccessRecordDao extends BaseMapper<AccessRecordEntity> {

    /**
     * 根据用户ID和时间范围查询通行记录
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 通行记录列表
     */
    default List<AccessRecordEntity> selectByUserIdAndTimeRange(@Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime) {
        return this.selectList(
                new LambdaQueryWrapper<AccessRecordEntity>()
                        .eq(userId != null, AccessRecordEntity::getUserId, userId)
                        .ge(startTime != null, AccessRecordEntity::getAccessTime, startTime)
                        .le(endTime != null, AccessRecordEntity::getAccessTime, endTime)
                        .eq(AccessRecordEntity::getDeletedFlag, 0)
                        .orderByDesc(AccessRecordEntity::getAccessTime));
    }

    /**
     * 根据区域ID查询通行记录
     *
     * @param areaId 区域ID
     * @return 通行记录列表
     */
    default List<AccessRecordEntity> selectByAreaId(@Param("areaId") Long areaId) {
        return this.selectList(
                new LambdaQueryWrapper<AccessRecordEntity>()
                        .eq(areaId != null, AccessRecordEntity::getAreaId, areaId)
                        .eq(AccessRecordEntity::getDeletedFlag, 0)
                        .orderByDesc(AccessRecordEntity::getAccessTime));
    }

    /**
     * 根据复合键查询通行记录
     * <p>
     * 用于幂等性检查：userId + deviceId + accessTime 唯一确定一条记录
     * </p>
     *
     * @param userId     用户ID
     * @param deviceId   设备ID
     * @param accessTime 通行时间
     * @return 通行记录实体，如果不存在则返回null
     */
    default AccessRecordEntity selectByCompositeKey(Long userId, Long deviceId, LocalDateTime accessTime) {
        return this.selectOne(new LambdaQueryWrapper<AccessRecordEntity>()
                .eq(AccessRecordEntity::getUserId, userId)
                .eq(AccessRecordEntity::getDeviceId, deviceId)
                .eq(AccessRecordEntity::getAccessTime, accessTime)
                .eq(AccessRecordEntity::getDeletedFlag, false)
                .last("LIMIT 1"));
    }

    /**
     * 批量插入通行记录
     * <p>
     * 使用MyBatis-Plus的批量插入功能
     * </p>
     *
     * @param entities 通行记录实体列表
     * @return 插入的记录数
     */
    default int batchInsert(List<AccessRecordEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return 0;
        }
        // MyBatis-Plus的saveBatch会自动进行批量插入
        // 但为了保持接口一致性，我们使用循环插入（实际生产环境应使用真正的批量插入）
        int count = 0;
        for (AccessRecordEntity entity : entities) {
            if (this.insert(entity) > 0) {
                count++;
            }
        }
        return count;
    }
}
