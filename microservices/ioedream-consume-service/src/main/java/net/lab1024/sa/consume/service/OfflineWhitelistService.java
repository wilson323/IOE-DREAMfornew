package net.lab1024.sa.consume.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.lab1024.sa.consume.entity.OfflineWhitelistEntity;

import java.util.List;

/**
 * 离线消费白名单Service接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
public interface OfflineWhitelistService extends IService<OfflineWhitelistEntity> {

    /**
     * 查询用户有效白名单
     *
     * @param userId 用户ID
     * @return 有效白名单列表
     */
    List<OfflineWhitelistEntity> getValidWhitelistByUserId(Long userId);

    /**
     * 查询设备有效白名单
     *
     * @param deviceId 设备ID
     * @return 有效白名单列表
     */
    List<OfflineWhitelistEntity> getValidWhitelistByDeviceId(Long deviceId);

    /**
     * 查询区域有效白名单
     *
     * @param areaId 区域ID
     * @return 有效白名单列表
     */
    List<OfflineWhitelistEntity> getValidWhitelistByAreaId(Long areaId);

    /**
     * 验证用户是否在白名单中
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 是否有效
     */
    boolean isValidWhitelistUser(Long userId, Long deviceId);

    /**
     * 批量添加白名单
     *
     * @param whitelist 白名单列表
     * @return 添加成功数量
     */
    Integer batchAddWhitelist(List<OfflineWhitelistEntity> whitelist);
}
