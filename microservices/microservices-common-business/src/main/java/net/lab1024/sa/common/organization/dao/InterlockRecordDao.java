package net.lab1024.sa.common.organization.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.organization.entity.InterlockRecordEntity;

/**
 * 互锁记录DAO
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
public interface InterlockRecordDao extends BaseMapper<InterlockRecordEntity> {

    /**
     * 根据设备ID查询互锁记录
     *
     * @param deviceId 设备ID
     * @return 互锁记录，如果不存在则返回null
     */
    default InterlockRecordEntity selectByDeviceId(Long deviceId) {
        return this.selectOne(
                new LambdaQueryWrapper<InterlockRecordEntity>()
                        .eq(InterlockRecordEntity::getDeviceId, deviceId)
                        .orderByDesc(InterlockRecordEntity::getLockTime)
                        .last("LIMIT 1"));
    }

    /**
     * 解锁互锁组中的其他设备（排除指定设备）
     *
     * @param interlockGroupId 互锁组ID
     * @param excludeDeviceId  排除的设备ID
     * @return 解锁的记录数
     */
    default int unlockDevicesInGroup(@Param("interlockGroupId") Long interlockGroupId,
            @Param("excludeDeviceId") Long excludeDeviceId) {
        List<InterlockRecordEntity> records = this.selectList(
                new LambdaQueryWrapper<InterlockRecordEntity>()
                        .eq(InterlockRecordEntity::getInterlockGroupId, interlockGroupId)
                        .ne(excludeDeviceId != null, InterlockRecordEntity::getDeviceId, excludeDeviceId)
                        .eq(InterlockRecordEntity::getLockStatus, InterlockRecordEntity.LockStatus.LOCKED));

        int count = 0;
        for (InterlockRecordEntity record : records) {
            record.setLockStatus(InterlockRecordEntity.LockStatus.UNLOCKED);
            record.setUnlockTime(java.time.LocalDateTime.now());
            int result = this.updateById(record);
            if (result > 0) {
                count++;
            }
        }
        return count;
    }
}
