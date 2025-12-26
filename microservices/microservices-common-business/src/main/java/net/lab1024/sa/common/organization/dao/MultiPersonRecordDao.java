package net.lab1024.sa.common.organization.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.organization.entity.MultiPersonRecordEntity;

/**
 * 多人验证记录DAO
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
public interface MultiPersonRecordDao extends BaseMapper<MultiPersonRecordEntity> {

    /**
     * 根据会话ID查询多人验证记录
     *
     * @param sessionId 会话ID
     * @return 多人验证记录，如果不存在则返回null
     */
    default MultiPersonRecordEntity selectBySessionId(@Param("sessionId") String sessionId) {
        return this.selectOne(
                new LambdaQueryWrapper<MultiPersonRecordEntity>()
                        .eq(MultiPersonRecordEntity::getVerificationSessionId, sessionId)
                        .orderByDesc(MultiPersonRecordEntity::getStartTime)
                        .last("LIMIT 1"));
    }

    /**
     * 查询指定区域和设备的活跃会话（状态为0=等待中）
     *
     * @param areaId   区域ID
     * @param deviceId 设备ID
     * @return 活跃会话列表
     */
    default List<MultiPersonRecordEntity> selectActiveSessions(@Param("areaId") Long areaId,
            @Param("deviceId") Long deviceId) {
        return this.selectList(
                new LambdaQueryWrapper<MultiPersonRecordEntity>()
                        .eq(areaId != null, MultiPersonRecordEntity::getAreaId, areaId)
                        .eq(deviceId != null, MultiPersonRecordEntity::getDeviceId, deviceId)
                        .eq(MultiPersonRecordEntity::getStatus, 0) // 0=等待中
                        .orderByDesc(MultiPersonRecordEntity::getStartTime));
    }
}
