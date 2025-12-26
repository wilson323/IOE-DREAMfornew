package net.lab1024.sa.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.device.DeviceFirmwareEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 设备固件数据访问接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface DeviceFirmwareDao extends BaseMapper<DeviceFirmwareEntity> {

    /**
     * 更新下载次数
     *
     * @param firmwareId 固件ID
     * @return 更新行数
     */
    int incrementDownloadCount(@Param("firmwareId") Long firmwareId);

    /**
     * 更新升级次数
     *
     * @param firmwareId 固件ID
     * @return 更新行数
     */
    int incrementUpgradeCount(@Param("firmwareId") Long firmwareId);

    /**
     * 查询可用固件列表
     * <p>
     * 条件：
     * 1. 设备类型匹配
     * 2. 设备型号匹配（如果指定）
     * 3. 固件状态为正式发布（2）
     * 4. 启用状态为1
     * 5. 按版本号降序排序
     * </p>
     *
     * @param deviceType  设备类型
     * @param deviceModel 设备型号（可选）
     * @return 可用固件列表
     */
    List<DeviceFirmwareEntity> selectAvailableFirmware(@Param("deviceType") Integer deviceType,
                                                      @Param("deviceModel") String deviceModel);
}
