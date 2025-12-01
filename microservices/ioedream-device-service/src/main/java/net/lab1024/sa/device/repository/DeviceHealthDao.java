package net.lab1024.sa.device.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.lab1024.sa.device.domain.entity.DeviceHealthEntity;

/**
 * 设备健康数据访问层
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Repository
public interface DeviceHealthRepository extends JpaRepository<DeviceHealthEntity, Long> {

    /**
     * 根据设备ID查询最新的健康记录
     */
    @Query("SELECT h FROM DeviceHealthEntity h WHERE h.deviceId = :deviceId ORDER BY h.checkTime DESC")
    List<DeviceHealthEntity> findLatestByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 根据设备ID查询最新的健康记录（单条）
     */
    @Query(value = "SELECT * FROM t_device_health WHERE device_id = :deviceId ORDER BY check_time DESC LIMIT 1", nativeQuery = true)
    DeviceHealthEntity findLatestOneByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 根据设备ID和时间范围查询健康历史记录
     */
    @Query("SELECT h FROM DeviceHealthEntity h WHERE h.deviceId = :deviceId " +
            "AND h.checkTime >= :startTime AND h.checkTime <= :endTime " +
            "ORDER BY h.checkTime DESC")
    List<DeviceHealthEntity> findByDeviceIdAndTimeRange(
            @Param("deviceId") Long deviceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 根据健康等级查询设备健康记录
     */
    @Query("SELECT h FROM DeviceHealthEntity h WHERE h.healthLevel = :healthLevel " +
            "AND h.checkTime >= :startTime ORDER BY h.checkTime DESC")
    List<DeviceHealthEntity> findByHealthLevel(
            @Param("healthLevel") Integer healthLevel,
            @Param("startTime") LocalDateTime startTime);

    /**
     * 查询所有设备的最新健康记录
     */
    @Query("SELECT h FROM DeviceHealthEntity h WHERE h.healthId IN " +
            "(SELECT MAX(h2.healthId) FROM DeviceHealthEntity h2 GROUP BY h2.deviceId)")
    List<DeviceHealthEntity> findAllLatestHealth();

    /**
     * 根据设备ID列表查询最新健康记录
     */
    @Query("SELECT h FROM DeviceHealthEntity h WHERE h.deviceId IN :deviceIds " +
            "AND h.healthId IN " +
            "(SELECT MAX(h2.healthId) FROM DeviceHealthEntity h2 " +
            "WHERE h2.deviceId IN :deviceIds GROUP BY h2.deviceId)")
    List<DeviceHealthEntity> findLatestByDeviceIds(@Param("deviceIds") List<Long> deviceIds);

    /**
     * 删除指定时间之前的健康历史记录
     */
    @Query("DELETE FROM DeviceHealthEntity h WHERE h.checkTime < :beforeTime")
    void deleteByCheckTimeBefore(@Param("beforeTime") LocalDateTime beforeTime);
}
