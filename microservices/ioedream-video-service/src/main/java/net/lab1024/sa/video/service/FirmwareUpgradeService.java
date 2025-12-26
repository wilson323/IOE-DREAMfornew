package net.lab1024.sa.video.service;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.entity.video.FirmwareUpgradeEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 固件升级服务接口
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
public interface FirmwareUpgradeService {

    /**
     * 创建固件升级任务
     *
     * @param deviceId      设备ID
     * @param targetVersion 目标版本
     * @param firmwareUrl   固件URL
     * @param fileSize      文件大小
     * @param fileMd5       文件MD5
     * @param upgradeType   升级类型
     * @return 升级任务ID
     */
    Long createUpgradeTask(Long deviceId, String targetVersion, String firmwareUrl,
                          Long fileSize, String fileMd5, Integer upgradeType);

    /**
     * 开始执行升级
     *
     * @param upgradeId 升级任务ID
     * @return 是否成功启动
     */
    Boolean startUpgrade(Long upgradeId);

    /**
     * 查询升级任务详情
     *
     * @param upgradeId 升级任务ID
     * @return 升级任务详情
     */
    FirmwareUpgradeEntity getUpgradeDetail(Long upgradeId);

    /**
     * 查询设备升级历史
     *
     * @param deviceId 设备ID
     * @return 升级历史记录
     */
    List<FirmwareUpgradeEntity> getDeviceUpgradeHistory(Long deviceId);

    /**
     * 查询进行中的升级任务
     *
     * @return 升级任务列表
     */
    List<FirmwareUpgradeEntity> getPendingUpgrades();

    /**
     * 查询升级统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getUpgradeStatistics();

    /**
     * 分页查询升级记录
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param deviceId 设备ID（可选）
     * @param status   升级状态（可选）
     * @return 分页结果
     */
    PageResult<FirmwareUpgradeEntity> queryPage(Integer pageNum, Integer pageSize,
                                                Long deviceId, Integer status);
}
