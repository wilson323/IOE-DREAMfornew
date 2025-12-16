package net.lab1024.sa.access.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.access.service.OfflineAccessService;
import net.lab1024.sa.access.domain.vo.OfflineAccessResultVO;

/**
 * 离线门禁控制器
 * <p>
 * 专门处理离线门禁功能，包括：
 * - 离线身份验证和权限校验
 * - 本地生物识别验证和活体检测
 * - 离线通行记录缓存和同步
 * - 应急门禁策略和安全监控
 * - 网络状态感知和自动切换
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/access/offline")
@Tag(name = "离线门禁", description = "离线门禁管理")
@PermissionCheck(value = "ACCESS", description = "离线门禁管理")
public class OfflineAccessController {

    @Resource
    private OfflineAccessService offlineAccessService;

    // ==================== 离线身份验证核心接口 ====================

    /**
     * 执行离线门禁验证
     * <p>
     * 在网络中断或设备离线状态下执行门禁验证
     * </p>
     *
     * @param verificationRequest 验证请求数据
     * @return 离线验证结果Future
     */
    @Observed(name = "offline.access.verification", contextualName = "offline-access-verification")
    @PostMapping("/verify")
    @Operation(
            summary = "执行离线门禁验证",
            description = "在网络中断或设备离线状态下执行门禁验证"
    )
    @PermissionCheck(value = "ACCESS_USER", description = "执行离线门禁验证")
    public ResponseEntity<ResponseDTO<OfflineAccessResultVO>> performOfflineAccessVerification(
            @Valid @RequestBody Map<String, Object> verificationRequest) {

        log.info("[离线门禁] 执行离线门禁验证，deviceId={}, userId={}",
                verificationRequest.get("deviceId"), verificationRequest.get("userId"));

        try {
            Future<OfflineAccessResultVO> result = offlineAccessService.performOfflineAccessVerification(verificationRequest);

            log.info("[离线门禁] 离线门禁验证任务提交成功，deviceId={}",
                    verificationRequest.get("deviceId"));

            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[离线门禁] 离线门禁验证异常，error={}", e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("OFFLINE_VERIFICATION_ERROR", "离线门禁验证异常：" + e.getMessage()));
        }
    }

    /**
     * 离线生物特征验证
     * <p>
     * 专门处理离线环境下的生物特征验证
     * </p>
     *
     * @param verificationRequest 验证请求（包含生物特征数据）
     * @return 生物特征验证结果
     */
    @Observed(name = "offline.access.biometric", contextualName = "offline-access-biometric")
    @PostMapping("/biometric/verify")
    @Operation(
            summary = "离线生物特征验证",
            description = "专门处理离线环境下的生物特征验证"
    )
    @PermissionCheck(value = "ACCESS_USER", description = "执行离线门禁验证")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> performOfflineBiometricVerification(
            @Valid @RequestBody Map<String, Object> verificationRequest) {

        log.info("[离线门禁] 执行离线生物特征验证，deviceId={}, biometricType={}",
                verificationRequest.get("deviceId"), verificationRequest.get("biometricType"));

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> biometricData = (Map<String, Object>) verificationRequest.get("biometricData");
            @SuppressWarnings("unchecked")
            Map<String, Object> deviceInfo = (Map<String, Object>) verificationRequest.get("deviceInfo");

            Map<String, Object> result = offlineAccessService.performOfflineBiometricVerification(
                    biometricData, deviceInfo);

            log.info("[离线门禁] 离线生物特征验证完成，deviceId={}, verified={}",
                    verificationRequest.get("deviceId"), result.get("verified"));

            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[离线门禁] 离线生物特征验证异常，error={}", e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("OFFLINE_BIOMETRIC_ERROR", "离线生物特征验证异常：" + e.getMessage()));
        }
    }

    /**
     * 多因素离线认证
     * <p>
     * 执行多因素离线身份认证
     * </p>
     *
     * @param authRequest 认证请求（包含多个认证因子）
     * @return 多因素认证结果
     */
    @Observed(name = "offline.access.multiFactor", contextualName = "offline-access-multi-factor")
    @PostMapping("/multi-factor/verify")
    @Operation(
            summary = "多因素离线认证",
            description = "执行多因素离线身份认证"
    )
    @PermissionCheck(value = "ACCESS_USER", description = "执行离线门禁验证")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> performMultiFactorOfflineAuth(
            @Valid @RequestBody Map<String, Object> authRequest) {

        log.info("[离线门禁] 执行多因素离线认证，deviceId={}, factorCount={}",
                authRequest.get("deviceId"), authRequest.get("factorCount"));

        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> authFactors = (List<Map<String, Object>>) authRequest.get("authFactors");
            String accessLevel = (String) authRequest.getOrDefault("accessLevel", "NORMAL");

            Map<String, Object> result = offlineAccessService.performMultiFactorOfflineAuth(authFactors, accessLevel);

            log.info("[离线门禁] 多因素离线认证完成，deviceId={}, authenticated={}",
                    authRequest.get("deviceId"), result.get("authenticated"));

            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[离线门禁] 多因素离线认证异常，error={}", e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("OFFLINE_MULTI_FACTOR_ERROR", "多因素离线认证异常：" + e.getMessage()));
        }
    }

    /**
     * 离线权限实时检查
     * <p>
     * 检查用户的离线访问权限
     * </p>
     *
     * @param permissionRequest 权限检查请求
     * @return 权限检查结果
     */
    @Observed(name = "offline.access.checkPermission", contextualName = "offline-access-check-permission")
    @PostMapping("/check-permission")
    @Operation(
            summary = "离线权限实时检查",
            description = "检查用户的离线访问权限"
    )
    @PermissionCheck(value = "ACCESS_USER", description = "执行离线门禁验证")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> checkOfflineAccessPermissions(
            @Valid @RequestBody Map<String, Object> permissionRequest) {

        log.info("[离线门禁] 检查离线访问权限，userId={}, deviceId={}",
                permissionRequest.get("userId"), permissionRequest.get("deviceId"));

        try {
            Long userId = Long.valueOf(permissionRequest.get("userId").toString());
            String deviceId = (String) permissionRequest.get("deviceId");
            @SuppressWarnings("unchecked")
            Map<String, Object> accessPoint = (Map<String, Object>) permissionRequest.get("accessPoint");

            Map<String, Object> result = offlineAccessService.checkOfflineAccessPermissions(userId, deviceId, accessPoint);

            log.info("[离线门禁] 离线权限检查完成，userId={}, deviceId={}, hasPermission={}",
                    userId, deviceId, result.get("hasPermission"));

            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[离线门禁] 离线权限检查异常，error={}", e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("OFFLINE_PERMISSION_CHECK_ERROR", "离线权限检查异常：" + e.getMessage()));
        }
    }

    // ==================== 离线数据管理接口 ====================

    /**
     * 准备离线访问权限数据
     * <p>
     * 为指定设备准备离线访问所需的权限数据
     * </p>
     *
     * @param deviceId 设备ID
     * @param userIds 用户ID列表（可选）
     * @return 离线权限数据包
     */
    @Observed(name = "offline.access.prepareData", contextualName = "offline-access-prepare-data")
    @PostMapping("/device/{deviceId}/prepare-data")
    @Operation(
            summary = "准备离线访问权限数据",
            description = "为指定设备准备离线访问所需的权限数据",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true)
            }
    )
    @PermissionCheck(value = "ACCESS_MANAGER", description = "离线数据管理")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> prepareOfflineAccessData(
            @PathVariable String deviceId,
            @RequestBody(required = false) List<Long> userIds) {

        log.info("[离线门禁] 准备离线访问权限数据，deviceId={}, userCount={}",
                deviceId, userIds != null ? userIds.size() : 0);

        try {
            Map<String, Object> accessData = offlineAccessService.prepareOfflineAccessData(deviceId, userIds);

            log.info("[离线门禁] 离线访问权限数据准备完成，deviceId={}", deviceId);

            return ResponseEntity.ok(ResponseDTO.ok(accessData));

        } catch (Exception e) {
            log.error("[离线门禁] 准备离线访问权限数据异常，deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("PREPARE_OFFLINE_DATA_ERROR", "准备离线访问权限数据异常：" + e.getMessage()));
        }
    }

    /**
     * 同步离线访问数据到设备
     * <p>
     * 将访问权限数据同步到门禁设备
     * </p>
     *
     * @param deviceId 设备ID
     * @param syncRequest 同步请求
     * @return 同步结果Future
     */
    @Observed(name = "offline.access.syncData", contextualName = "offline-access-sync-data")
    @PostMapping("/device/{deviceId}/sync-data")
    @Operation(
            summary = "同步离线访问数据到设备",
            description = "将访问权限数据同步到门禁设备",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true)
            }
    )
    @PermissionCheck(value = "ACCESS_MANAGER", description = "离线数据管理")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> syncOfflineAccessDataToDevice(
            @PathVariable String deviceId,
            @Valid @RequestBody Map<String, Object> syncRequest) {

        log.info("[离线门禁] 同步离线访问数据到设备，deviceId={}", deviceId);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> accessData = (Map<String, Object>) syncRequest.get("accessData");

            Future<Map<String, Object>> result = offlineAccessService.syncOfflineAccessDataToDevice(deviceId, accessData);

            log.info("[离线门禁] 离线访问数据同步任务提交成功，deviceId={}", deviceId);

            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[离线门禁] 同步离线访问数据异常，deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("SYNC_OFFLINE_DATA_ERROR", "同步离线访问数据异常：" + e.getMessage()));
        }
    }

    /**
     * 验证离线访问数据完整性
     * <p>
     * 验证设备上的离线访问数据完整性
     * </p>
     *
     * @param deviceId 设备ID
     * @return 数据完整性验证结果
     */
    @Observed(name = "offline.access.validateDataIntegrity", contextualName = "offline-access-validate-data-integrity")
    @GetMapping("/device/{deviceId}/validate-data-integrity")
    @Operation(
            summary = "验证离线访问数据完整性",
            description = "验证设备上的离线访问数据完整性",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true)
            }
    )
    @PermissionCheck(value = "ACCESS_MANAGER", description = "离线数据管理")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> validateOfflineAccessDataIntegrity(@PathVariable String deviceId) {
        log.info("[离线门禁] 验证离线访问数据完整性，deviceId={}", deviceId);

        try {
            Map<String, Object> validation = offlineAccessService.validateOfflineAccessDataIntegrity(deviceId);

            log.info("[离线门禁] 离线访问数据完整性验证完成，deviceId={}, integrity={}",
                    deviceId, validation.get("integrity"));

            return ResponseEntity.ok(ResponseDTO.ok(validation));

        } catch (Exception e) {
            log.error("[离线门禁] 验证离线访问数据完整性异常，deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("VALIDATE_DATA_INTEGRITY_ERROR", "验证离线访问数据完整性异常：" + e.getMessage()));
        }
    }

    // ==================== 离线记录管理接口 ====================

    /**
     * 缓存离线通行记录
     * <p>
     * 在设备离线状态下缓存通行记录
     * </p>
     *
     * @param recordRequest 记录缓存请求
     * @return 缓存结果
     */
    @Observed(name = "offline.access.cacheRecord", contextualName = "offline-access-cache-record")
    @PostMapping("/cache-record")
    @Operation(
            summary = "缓存离线通行记录",
            description = "在设备离线状态下缓存通行记录"
    )
    @PermissionCheck(value = "ACCESS_MANAGER", description = "离线数据管理")
    public ResponseEntity<ResponseDTO<Boolean>> cacheOfflineAccessRecord(
            @Valid @RequestBody Map<String, Object> recordRequest) {

        log.info("[离线门禁] 缓存离线通行记录，deviceId={}, recordId={}",
                recordRequest.get("deviceId"), recordRequest.get("recordId"));

        try {
            String deviceId = (String) recordRequest.get("deviceId");
            @SuppressWarnings("unchecked")
            Map<String, Object> accessRecord = (Map<String, Object>) recordRequest.get("accessRecord");

            boolean success = offlineAccessService.cacheOfflineAccessRecord(deviceId, accessRecord);

            log.info("[离线门禁] 离线通行记录缓存完成，deviceId={}, success={}", deviceId, success);

            return ResponseEntity.ok(ResponseDTO.ok(success));

        } catch (Exception e) {
            log.error("[离线门禁] 缓存离线通行记录异常，error={}", e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("CACHE_OFFLINE_RECORD_ERROR", "缓存离线通行记录异常：" + e.getMessage()));
        }
    }

    /**
     * 批量上传离线通行记录
     * <p>
     * 将缓存的离线记录批量上传到云端
     * </p>
     *
     * @param deviceId 设备ID
     * @param offlineRecords 离线记录列表
     * @return 上传结果Future
     */
    @Observed(name = "offline.access.uploadRecords", contextualName = "offline-access-upload-records")
    @PostMapping("/device/{deviceId}/upload-records")
    @Operation(
            summary = "批量上传离线通行记录",
            description = "将缓存的离线记录批量上传到云端",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true)
            }
    )
    @PermissionCheck(value = "ACCESS_MANAGER", description = "离线数据管理")
    public ResponseEntity<ResponseDTO<OfflineAccessResultVO>> batchUploadOfflineAccessRecords(
            @PathVariable String deviceId,
            @Valid @RequestBody List<Map<String, Object>> offlineRecords) {

        log.info("[离线门禁] 批量上传离线通行记录，deviceId={}, recordCount={}", deviceId, offlineRecords.size());

        try {
            Future<OfflineAccessResultVO> result = offlineAccessService.batchUploadOfflineAccessRecords(deviceId, offlineRecords);

            log.info("[离线门禁] 离线通行记录上传任务提交成功，deviceId={}, recordCount={}", deviceId, offlineRecords.size());

            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[离线门禁] 批量上传离线通行记录异常，deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("UPLOAD_OFFLINE_RECORDS_ERROR", "批量上传离线通行记录异常：" + e.getMessage()));
        }
    }

    /**
     * 获取离线记录统计信息
     * <p>
     * 获取设备离线记录的统计信息
     * </p>
     *
     * @param deviceId 设备ID
     * @return 统计信息Map
     */
    @Observed(name = "offline.access.recordStatistics", contextualName = "offline-access-record-statistics")
    @GetMapping("/device/{deviceId}/record-statistics")
    @Operation(
            summary = "获取离线记录统计信息",
            description = "获取设备离线记录的统计信息",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true)
            }
    )
    @PermissionCheck(value = "ACCESS_MANAGER", description = "离线数据管理")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> getOfflineAccessRecordStatistics(@PathVariable String deviceId) {
        log.info("[离线门禁] 获取离线记录统计信息，deviceId={}", deviceId);

        try {
            Map<String, Object> statistics = offlineAccessService.getOfflineAccessRecordStatistics(deviceId);

            log.info("[离线门禁] 离线记录统计信息获取成功，deviceId={}", deviceId);

            return ResponseEntity.ok(ResponseDTO.ok(statistics));

        } catch (Exception e) {
            log.error("[离线门禁] 获取离线记录统计信息异常，deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("GET_RECORD_STATISTICS_ERROR", "获取离线记录统计信息异常：" + e.getMessage()));
        }
    }

    // ==================== 应急门禁策略接口 ====================

    /**
     * 启用应急门禁模式
     * <p>
     * 在特殊情况下启用应急门禁策略
     * </p>
     *
     * @param emergencyRequest 应急模式启用请求
     * @return 应急模式启用结果
     */
    @Observed(name = "offline.access.enableEmergency", contextualName = "offline-access-enable-emergency")
    @PostMapping("/enable-emergency-mode")
    @Operation(
            summary = "启用应急门禁模式",
            description = "在特殊情况下启用应急门禁策略"
    )
    @PermissionCheck(value = "ACCESS_MANAGER", description = "离线数据管理")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> enableEmergencyAccessMode(
            @Valid @RequestBody Map<String, Object> emergencyRequest) {

        log.info("[离线门禁] 启用应急门禁模式，deviceId={}, emergencyType={}",
                emergencyRequest.get("deviceId"), emergencyRequest.get("emergencyType"));

        try {
            String deviceId = (String) emergencyRequest.get("deviceId");
            String emergencyType = (String) emergencyRequest.get("emergencyType");
            @SuppressWarnings("unchecked")
            List<String> authorizedRoles = (List<String>) emergencyRequest.get("authorizedRoles");

            Map<String, Object> result = offlineAccessService.enableEmergencyAccessMode(deviceId, emergencyType, authorizedRoles);

            log.info("[离线门禁] 应急门禁模式启用完成，deviceId={}, enabled={}",
                    deviceId, result.get("enabled"));

            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[离线门禁] 启用应急门禁模式异常，error={}", e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("ENABLE_EMERGENCY_MODE_ERROR", "启用应急门禁模式异常：" + e.getMessage()));
        }
    }

    /**
     * 执行应急权限验证
     * <p>
     * 在应急模式下执行特殊的权限验证
     * </p>
     *
     * @param verificationRequest 应急验证请求
     * @return 应急验证结果
     */
    @Observed(name = "offline.access.emergencyVerification", contextualName = "offline-access-emergency-verification")
    @PostMapping("/emergency-verify")
    @Operation(
            summary = "执行应急权限验证",
            description = "在应急模式下执行特殊的权限验证"
    )
    @PermissionCheck(value = "ACCESS_USER", description = "执行离线门禁验证")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> performEmergencyAccessVerification(
            @Valid @RequestBody Map<String, Object> verificationRequest) {

        log.info("[离线门禁] 执行应急权限验证，deviceId={}, emergencyType={}",
                verificationRequest.get("deviceId"), verificationRequest.get("emergencyType"));

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> emergencyContext = (Map<String, Object>) verificationRequest.get("emergencyContext");

            Map<String, Object> result = offlineAccessService.performEmergencyAccessVerification(verificationRequest, emergencyContext);

            log.info("[离线门禁] 应急权限验证完成，deviceId={}, verified={}",
                    verificationRequest.get("deviceId"), result.get("verified"));

            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[离线门禁] 应急权限验证异常，error={}", e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("EMERGENCY_VERIFICATION_ERROR", "应急权限验证异常：" + e.getMessage()));
        }
    }

    /**
     * 退出应急门禁模式
     * <p>
     * 从应急模式恢复正常门禁操作
     * </p>
     *
     * @param exitRequest 退出应急模式请求
     * @return 退出应急模式结果
     */
    @Observed(name = "offline.access.exitEmergency", contextualName = "offline-access-exit-emergency")
    @PostMapping("/exit-emergency-mode")
    @Operation(
            summary = "退出应急门禁模式",
            description = "从应急模式恢复正常门禁操作"
    )
    @PermissionCheck(value = "ACCESS_MANAGER", description = "离线数据管理")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> exitEmergencyAccessMode(
            @Valid @RequestBody Map<String, Object> exitRequest) {

        log.info("[离线门禁] 退出应急门禁模式，deviceId={}, reason={}",
                exitRequest.get("deviceId"), exitRequest.get("exitReason"));

        try {
            String deviceId = (String) exitRequest.get("deviceId");
            String exitReason = (String) exitRequest.get("exitReason");

            Map<String, Object> result = offlineAccessService.exitEmergencyAccessMode(deviceId, exitReason);

            log.info("[离线门禁] 应急门禁模式退出完成，deviceId={}, success={}",
                    deviceId, result.get("success"));

            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[离线门禁] 退出应急门禁模式异常，error={}", e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("EXIT_EMERGENCY_MODE_ERROR", "退出应急门禁模式异常：" + e.getMessage()));
        }
    }

    // ==================== 设备离线状态监控接口 ====================

    /**
     * 检查设备离线状态
     * <p>
     * 检查门禁设备的离线状态和能力
     * </p>
     *
     * @param deviceId 设备ID
     * @return 设备离线状态信息
     */
    @Observed(name = "offline.access.deviceStatus", contextualName = "offline-access-device-status")
    @GetMapping("/device/{deviceId}/offline-status")
    @Operation(
            summary = "检查设备离线状态",
            description = "检查门禁设备的离线状态和能力",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true)
            }
    )
    @PermissionCheck(value = "ACCESS_USER", description = "执行离线门禁验证")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> checkDeviceOfflineStatus(@PathVariable String deviceId) {
        log.info("[离线门禁] 检查设备离线状态，deviceId={}", deviceId);

        try {
            Map<String, Object> status = offlineAccessService.checkDeviceOfflineStatus(deviceId);

            log.info("[离线门禁] 设备离线状态检查完成，deviceId={}, offlineMode={}",
                    deviceId, status.get("offlineMode"));

            return ResponseEntity.ok(ResponseDTO.ok(status));

        } catch (Exception e) {
            log.error("[离线门禁] 检查设备离线状态异常，deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("CHECK_DEVICE_STATUS_ERROR", "检查设备离线状态异常：" + e.getMessage()));
        }
    }

    /**
     * 预测设备离线风险
     * <p>
     * 预测设备可能出现的离线风险
     * </p>
     *
     * @param deviceId 设备ID
     * @param riskTimeRange 风险预测时间范围（小时）
     * @return 风险评估报告
     */
    @Observed(name = "offline.access.predictRisks", contextualName = "offline-access-predict-risks")
    @GetMapping("/device/{deviceId}/predict-risks")
    @Operation(
            summary = "预测设备离线风险",
            description = "预测设备可能出现的离线风险",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true),
                    @Parameter(name = "riskTimeRange", description = "风险预测时间范围（小时）")
            }
    )
    @PermissionCheck(value = "ACCESS_MANAGER", description = "离线数据管理")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> predictDeviceOfflineRisks(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "24") Integer riskTimeRange) {

        log.info("[离线门禁] 预测设备离线风险，deviceId={}, riskTimeRange={}h", deviceId, riskTimeRange);

        try {
            Map<String, Object> riskReport = offlineAccessService.predictDeviceOfflineRisks(deviceId, riskTimeRange);

            log.info("[离线门禁] 设备离线风险预测完成，deviceId={}, riskLevel={}",
                    deviceId, riskReport.get("riskLevel"));

            return ResponseEntity.ok(ResponseDTO.ok(riskReport));

        } catch (Exception e) {
            log.error("[离线门禁] 预测设备离线风险异常，deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("PREDICT_OFFLINE_RISKS_ERROR", "预测设备离线风险异常：" + e.getMessage()));
        }
    }

    /**
     * 获取网络历史状态分析
     * <p>
     * 分析设备的历史网络状态模式
     * </p>
     *
     * @param deviceId 设备ID
     * @param analysisDays 分析天数
     * @return 网络状态分析报告
     */
    @Observed(name = "offline.access.networkAnalysis", contextualName = "offline-access-network-analysis")
    @GetMapping("/device/{deviceId}/network-analysis")
    @Operation(
            summary = "获取网络历史状态分析",
            description = "分析设备的历史网络状态模式",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true),
                    @Parameter(name = "analysisDays", description = "分析天数")
            }
    )
    @PermissionCheck(value = "ACCESS_MANAGER", description = "离线数据管理")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> getNetworkHistoryAnalysis(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "7") Integer analysisDays) {

        log.info("[离线门禁] 获取网络历史状态分析，deviceId={}, analysisDays={}d", deviceId, analysisDays);

        try {
            Map<String, Object> analysis = offlineAccessService.getNetworkHistoryAnalysis(deviceId, analysisDays);

            log.info("[离线门禁] 网络历史状态分析完成，deviceId={}", deviceId);

            return ResponseEntity.ok(ResponseDTO.ok(analysis));

        } catch (Exception e) {
            log.error("[离线门禁] 获取网络历史状态分析异常，deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("GET_NETWORK_ANALYSIS_ERROR", "获取网络历史状态分析异常：" + e.getMessage()));
        }
    }
}