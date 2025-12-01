package net.lab1024.sa.admin.module.smart.access.service;

import net.lab1024.sa.admin.module.smart.access.domain.entity.SmartAccessPermissionEntity;
import net.lab1024.sa.admin.module.smart.access.domain.entity.SmartAccessRecordEntity;
import net.lab1024.sa.base.common.domain.ResponseDTO;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 门禁控制服务接口
 *
 * @author SmartAdmin Team
 * @date 2025-11-15
 */
public interface SmartAccessControlService {

    /**
     * 门禁通行验证
     *
     * @param personId 人员ID
     * @param deviceId 设备ID
     * @param accessType 通行方式
     * @param credential 验证凭证(卡片号、人脸特征ID等)
     * @return 验证结果
     */
    ResponseDTO<Map<String, Object>> verifyAccess(Long personId, Long deviceId, String accessType, String credential);

    /**
     * 刷卡验证
     *
     * @param cardNumber 卡片编号
     * @param deviceId 设备ID
     * @return 验证结果
     */
    ResponseDTO<Map<String, Object>> verifyCardAccess(String cardNumber, Long deviceId);

    /**
     * 人脸识别验证
     *
     * @param faceFeatureId 人脸特征ID
     * @param deviceId 设备ID
     * @return 验证结果
     */
    ResponseDTO<Map<String, Object>> verifyFaceAccess(String faceFeatureId, Long deviceId);

    /**
     * 指纹验证
     *
     * @param fingerprintId 指纹特征ID
     * @param deviceId 设备ID
     * @return 验证结果
     */
    ResponseDTO<Map<String, Object>> verifyFingerprintAccess(String fingerprintId, Long deviceId);

    /**
     * 密码验证
     *
     * @param password 密码
     * @param deviceId 设备ID
     * @return 验证结果
     */
    ResponseDTO<Map<String, Object>> verifyPasswordAccess(String password, Long deviceId);

    /**
     * 远程开门
     *
     * @param deviceId 设备ID
     * @param operatorId 操作人ID
     * @param reason 开门原因
     * @return 操作结果
     */
    ResponseDTO<String> remoteOpenDoor(Long deviceId, Long operatorId, String reason);

    /**
     * 检查人员权限
     *
     * @param personId 人员ID
     * @param deviceId 设备ID
     * @param currentTime 当前时间
     * @return 权限信息
     */
    SmartAccessPermissionEntity checkPersonPermission(Long personId, Long deviceId, LocalDateTime currentTime);

    /**
     * 检查时间权限
     *
     * @param permission 权限实体
     * @param currentTime 当前时间
     * @return 是否有时间权限
     */
    boolean checkTimePermission(SmartAccessPermissionEntity permission, LocalDateTime currentTime);

    /**
     * 检查设备权限
     *
     * @param permission 权限实体
     * @param deviceId 设备ID
     * @return 是否有设备权限
     */
    boolean checkDevicePermission(SmartAccessPermissionEntity permission, Long deviceId);

    /**
     * 记录通行事件
     *
     * @param record 通行记录
     * @return 记录结果
     */
    ResponseDTO<String> recordAccessEvent(SmartAccessRecordEntity record);

    /**
     * 生成访问令牌
     *
     * @param personId 人员ID
     * @param deviceId 设备ID
     * @param expireMinutes 过期时间(分钟)
     * @return 访问令牌
     */
    String generateAccessToken(Long personId, Long deviceId, Integer expireMinutes);

    /**
     * 验证访问令牌
     *
     * @param accessToken 访问令牌
     * @return 验证结果
     */
    ResponseDTO<Map<String, Object>> verifyAccessToken(String accessToken);

    /**
     * 获取人员今日通行统计
     *
     * @param personId 人员ID
     * @return 统计信息
     */
    Map<String, Object> getPersonTodayStatistics(Long personId);

    /**
     * 获取设备今日通行统计
     *
     * @param deviceId 设备ID
     * @return 统计信息
     */
    Map<String, Object> getDeviceTodayStatistics(Long deviceId);

    /**
     * 检查是否为强制开门
     *
     * @param deviceId 设备ID
     * @param accessTime 通行时间
     * @return 是否强制开门
     */
    boolean isForcedOpen(Long deviceId, LocalDateTime accessTime);
}