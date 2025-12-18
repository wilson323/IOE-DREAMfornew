package net.lab1024.sa.common.organization.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.organization.entity.InterlockRecordEntity;
import org.apache.ibatis.annotations.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 互锁记录数据访问对象
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Mapper注解标识数据访问层
 * - 继承BaseMapper<Entity>使用MyBatis-Plus
 * - 查询方法使用@Transactional(readOnly = true)
 * - 写操作方法使用@Transactional(rollbackFor = Exception.class)
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface InterlockRecordDao extends BaseMapper<InterlockRecordEntity> {

    /**
     * 查询互锁组中已锁定的设备
     * <p>
     * 用于互锁验证，检查互锁组中是否有其他设备已锁定
     * </p>
     *
     * @param interlockGroupId 互锁组ID
     * @param excludeDeviceId 排除的设备ID（当前设备）
     * @return 已锁定的设备记录列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_access_interlock_record " +
            "WHERE interlock_group_id = #{interlockGroupId} " +
            "AND device_id != #{excludeDeviceId} " +
            "AND lock_status = 1 " +
            "AND deleted_flag = 0 " +
            "ORDER BY lock_time DESC")
    List<InterlockRecordEntity> selectLockedDevicesInGroup(
            @Param("interlockGroupId") Long interlockGroupId,
            @Param("excludeDeviceId") Long excludeDeviceId);

    /**
     * 查询设备的互锁记录
     *
     * @param deviceId 设备ID
     * @return 互锁记录，不存在返回null
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_access_interlock_record " +
            "WHERE device_id = #{deviceId} AND deleted_flag = 0 " +
            "ORDER BY lock_time DESC " +
            "LIMIT 1")
    InterlockRecordEntity selectByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 解锁互锁组中的所有设备
     * <p>
     * 用于互锁验证通过后，解锁互锁组中的其他设备
     * </p>
     *
     * @param interlockGroupId 互锁组ID
     * @param excludeDeviceId 排除的设备ID（当前设备，不需要解锁）
     * @return 解锁的设备数量
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE t_access_interlock_record " +
            "SET lock_status = 0, unlock_time = NOW(), " +
            "lock_duration = TIMESTAMPDIFF(SECOND, lock_time, NOW()) " +
            "WHERE interlock_group_id = #{interlockGroupId} " +
            "AND device_id != #{excludeDeviceId} " +
            "AND lock_status = 1 " +
            "AND deleted_flag = 0")
    int unlockDevicesInGroup(
            @Param("interlockGroupId") Long interlockGroupId,
            @Param("excludeDeviceId") Long excludeDeviceId);
}
