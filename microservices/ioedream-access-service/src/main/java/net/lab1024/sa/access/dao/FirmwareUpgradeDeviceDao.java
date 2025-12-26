package net.lab1024.sa.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.access.FirmwareUpgradeDeviceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 固件升级设备明细数据访问接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface FirmwareUpgradeDeviceDao extends BaseMapper<FirmwareUpgradeDeviceEntity> {

    /**
     * 根据任务ID查询设备列表
     *
     * @param taskId 任务ID
     * @return 设备明细列表
     */
    List<FirmwareUpgradeDeviceEntity> selectByTaskId(@Param("taskId") Long taskId);

    /**
     * 查询待升级的设备列表
     * <p>
     * 条件：
     * 1. 指定任务ID
     * 2. 升级状态为待升级（1）
     * 3. 按设备ID排序
     * </p>
     *
     * @param taskId 任务ID
     * @return 待升级设备列表
     */
    List<FirmwareUpgradeDeviceEntity> selectPendingDevices(@Param("taskId") Long taskId);

    /**
     * 批量插入设备明细
     *
     * @param deviceList 设备明细列表
     * @return 插入行数
     */
    int insertBatch(@Param("deviceList") List<FirmwareUpgradeDeviceEntity> deviceList);

    /**
     * 更新设备升级状态
     *
     * @param detailId      明细ID
     * @param upgradeStatus 升级状态
     * @param errorCode     错误代码
     * @param errorMessage  错误信息
     * @return 更新行数
     */
    int updateUpgradeStatus(@Param("detailId") Long detailId,
                            @Param("upgradeStatus") Integer upgradeStatus,
                            @Param("errorCode") String errorCode,
                            @Param("errorMessage") String errorMessage);

    /**
     * 增加重试次数
     *
     * @param detailId 明细ID
     * @return 更新行数
     */
    int incrementRetryCount(@Param("detailId") Long detailId);

    /**
     * 查询升级失败的设备列表
     *
     * @param taskId 任务ID
     * @return 失败设备列表
     */
    List<FirmwareUpgradeDeviceEntity> selectFailedDevices(@Param("taskId") Long taskId);

    /**
     * 查询任务统计信息
     *
     * @param taskId 任务ID
     * @return 统计信息（包含各状态的数量）
     */
    java.util.Map<String, Object> selectTaskStatistics(@Param("taskId") Long taskId);
}
