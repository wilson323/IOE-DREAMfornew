package net.lab1024.sa.biometric.service;

import net.lab1024.sa.biometric.domain.vo.TemplateSyncResultVO;
import net.lab1024.sa.common.dto.ResponseDTO;

import java.util.List;

/**
 * 生物模板同步服务接口
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - Service层只定义接口，不包含实现
 * - 使用@Resource依赖注入
 * - 完整的业务方法定义
 * </p>
 * <p>
 * 核心职责:
 * - 模板下发到设备
 * - 模板从设备删除
 * - 权限变更时的模板同步
 * - 批量同步操作
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
public interface BiometricTemplateSyncService {

    /**
     * 同步用户模板到设备
     * <p>
     * 场景：用户新增模板时，自动同步到有权限的设备
     * </p>
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 同步结果
     */
    ResponseDTO<TemplateSyncResultVO> syncTemplateToDevice(Long userId, String deviceId);

    /**
     * 同步用户模板到多个设备
     *
     * @param userId 用户ID
     * @param deviceIds 设备ID列表
     * @return 同步结果
     */
    ResponseDTO<TemplateSyncResultVO> syncTemplateToDevices(Long userId, List<String> deviceIds);

    /**
     * 从设备删除用户模板
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 删除结果
     */
    ResponseDTO<Void> deleteTemplateFromDevice(Long userId, String deviceId);

    /**
     * 从所有设备删除用户模板
     * <p>
     * 场景：用户离职时，从所有设备删除模板
     * </p>
     *
     * @param userId 用户ID
     * @return 删除结果
     */
    ResponseDTO<Void> deleteTemplateFromAllDevices(Long userId);

    /**
     * 权限变更时同步模板
     * <p>
     * 场景：用户新增区域权限时，同步模板到该区域设备
     * </p>
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @return 同步结果
     */
    ResponseDTO<TemplateSyncResultVO> syncOnPermissionAdded(Long userId, Long areaId);

    /**
     * 权限移除时删除模板
     * <p>
     * 场景：用户移除区域权限时，从该区域设备删除模板
     * </p>
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @return 删除结果
     */
    ResponseDTO<Void> deleteOnPermissionRemoved(Long userId, Long areaId);

    /**
     * 批量同步模板到设备
     *
     * @param userIds 用户ID列表
     * @param deviceId 设备ID
     * @return 同步结果
     */
    ResponseDTO<TemplateSyncResultVO> batchSyncTemplatesToDevice(List<Long> userIds, String deviceId);
}
