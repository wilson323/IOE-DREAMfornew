package net.lab1024.sa.video.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.video.entity.FirmwareUpgradeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 固件升级数据访问对象
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Mapper
public interface FirmwareUpgradeDao extends BaseMapper<FirmwareUpgradeEntity> {

    /**
     * 查询指定设备的升级记录
     *
     * @param deviceId 设备ID
     * @return 升级记录列表
     */
    @Select("SELECT * FROM t_video_firmware_upgrade " +
            "WHERE device_id = #{deviceId} " +
            "ORDER BY create_time DESC")
    List<FirmwareUpgradeEntity> queryByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 查询进行中的升级任务
     *
     * @return 升级任务列表
     */
    @Select("SELECT * FROM t_video_firmware_upgrade " +
            "WHERE upgrade_status IN (1, 2) " +
            "ORDER BY create_time ASC")
    List<FirmwareUpgradeEntity> queryPendingUpgrades();

    /**
     * 查询指定状态范围内的升级记录
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 升级记录列表
     */
    @Select("SELECT * FROM t_video_firmware_upgrade " +
            "WHERE create_time >= #{startTime} AND create_time <= #{endTime} " +
            "ORDER BY create_time DESC")
    List<FirmwareUpgradeEntity> queryByDateRange(@Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 查询最新升级记录
     *
     * @param limit 限制数量
     * @return 升级记录列表
     */
    @Select("SELECT * FROM t_video_firmware_upgrade " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<FirmwareUpgradeEntity> queryRecentUpgrades(@Param("limit") int limit);
}
