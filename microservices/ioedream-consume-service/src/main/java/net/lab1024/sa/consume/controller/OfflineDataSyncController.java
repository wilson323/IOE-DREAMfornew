package net.lab1024.sa.consume.controller;

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
import net.lab1024.sa.consume.service.OfflineDataSyncService;
import net.lab1024.sa.consume.domain.vo.OfflineSyncResultVO;

/**
 * 离线数据同步控制器
 * <p>
 * 专门处理离线数据同步功能，包括：
 * - 离线数据包准备和同步管理
 * - 离线业务记录处理和验证
 * - 网络状态感知和智能同步调度
 * - 数据一致性校验和冲突解决
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/consume/offline/sync")
@Tag(name = "离线数据同步", description = "离线数据同步管理")
@PermissionCheck(value = "CONSUME", description = "离线数据同步管理")
public class OfflineDataSyncController {

    @Resource
    private OfflineDataSyncService offlineDataSyncService;

    // ==================== 离线数据管理核心接口 ====================

    /**
     * 准备离线数据包
     * <p>
     * 为指定设备准备完整的离线数据包，包括用户信息、权限、配置等
     * </p>
     *
     * @param deviceId 设备ID
     * @param userId 用户ID（可选）
     * @return 离线数据包准备结果
     */
    @Observed(name = "offline.sync.prepareDataPackage", contextualName = "offline-sync-prepare-data-package")
    @PostMapping("/device/{deviceId}/prepare-package")
    @Operation(
            summary = "准备离线数据包",
            description = "为指定设备准备完整的离线数据包",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true)
            }
    )
    @PermissionCheck(value = "CONSUME_MANAGER", description = "离线数据管理")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> prepareOfflineDataPackage(
            @PathVariable String deviceId,
            @RequestParam(required = false) Long userId) {

        log.info("[离线数据同步] 准备离线数据包，deviceId={}, userId={}", deviceId, userId);

        try {
            Map<String, Object> dataPackage = offlineDataSyncService.prepareOfflineDataPackage(deviceId, userId);

            log.info("[离线数据同步] 离线数据包准备完成，deviceId={}", deviceId);

            return ResponseEntity.ok(ResponseDTO.ok(dataPackage));

        } catch (Exception e) {
            log.error("[离线数据同步] 准备离线数据包异常，deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("PREPARE_PACKAGE_ERROR", "准备离线数据包异常：" + e.getMessage()));
        }
    }

    /**
     * 同步离线数据包到设备
     * <p>
     * 将准备好的离线数据包同步到目标设备
     * </p>
     *
     * @param deviceId 设备ID
     * @param request 同步请求
     * @return 同步结果Future
     */
    @Observed(name = "offline.sync.syncToDevice", contextualName = "offline-sync-to-device")
    @PostMapping("/device/{deviceId}/sync")
    @Operation(
            summary = "同步离线数据包到设备",
            description = "将离线数据包同步到指定设备",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true)
            }
    )
    @PermissionCheck(value = "CONSUME_MANAGER", description = "离线数据管理")
    public ResponseEntity<ResponseDTO<OfflineSyncResultVO>> syncOfflineDataToDevice(
            @PathVariable String deviceId,
            @Valid @RequestBody Map<String, Object> request) {

        log.info("[离线数据同步] 同步离线数据包到设备，deviceId={}", deviceId);

        try {
            String syncType = (String) request.getOrDefault("syncType", "FULL");
            @SuppressWarnings("unchecked")
            Map<String, Object> dataPackage = (Map<String, Object>) request.get("dataPackage");

            Future<OfflineSyncResultVO> result = offlineDataSyncService.syncOfflineDataToDevice(
                    deviceId, dataPackage, syncType);

            log.info("[离线数据同步] 离线数据包同步任务提交成功，deviceId={}", deviceId);

            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[离线数据同步] 同步离线数据包异常，deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("SYNC_TO_DEVICE_ERROR", "同步离线数据包异常：" + e.getMessage()));
        }
    }

    /**
     * 验证离线数据完整性
     * <p>
     * 验证设备上的离线数据是否完整有效
     * </p>
     *
     * @param deviceId 设备ID
     * @return 验证结果
     */
    @Observed(name = "offline.sync.validateIntegrity", contextualName = "offline-sync-validate-integrity")
    @GetMapping("/device/{deviceId}/validate-integrity")
    @Operation(
            summary = "验证离线数据完整性",
            description = "验证设备上的离线数据是否完整有效",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true)
            }
    )
    @PermissionCheck(value = "CONSUME_MANAGER", description = "离线数据管理")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> validateOfflineDataIntegrity(@PathVariable String deviceId) {
        log.info("[离线数据同步] 验证离线数据完整性，deviceId={}", deviceId);

        try {
            Map<String, Object> validation = offlineDataSyncService.validateOfflineDataIntegrity(deviceId);

            log.info("[离线数据同步] 离线数据完整性验证完成，deviceId={}, integrity={}",
                    deviceId, validation.get("integrity"));

            return ResponseEntity.ok(ResponseDTO.ok(validation));

        } catch (Exception e) {
            log.error("[离线数据同步] 验证离线数据完整性异常，deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("VALIDATE_INTEGRITY_ERROR", "验证离线数据完整性异常：" + e.getMessage()));
        }
    }

    /**
     * 检查设备离线状态
     * <p>
     * 检查设备的离线状态和准备情况
     * </p>
     *
     * @param deviceId 设备ID
     * @return 离线状态信息
     */
    @Observed(name = "offline.sync.checkOfflineStatus", contextualName = "offline-sync-check-offline-status")
    @GetMapping("/device/{deviceId}/offline-status")
    @Operation(
            summary = "检查设备离线状态",
            description = "检查设备的离线状态和准备情况",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true)
            }
    )
    @PermissionCheck(value = "CONSUME_USER", description = "离线数据查询")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> checkDeviceOfflineStatus(@PathVariable String deviceId) {
        log.info("[离线数据同步] 检查设备离线状态，deviceId={}", deviceId);

        try {
            Map<String, Object> status = offlineDataSyncService.checkDeviceOfflineStatus(deviceId);

            log.info("[离线数据同步] 设备离线状态检查完成，deviceId={}, status={}",
                    deviceId, status.get("status"));

            return ResponseEntity.ok(ResponseDTO.ok(status));

        } catch (Exception e) {
            log.error("[离线数据同步] 检查设备离线状态异常，deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("CHECK_OFFLINE_STATUS_ERROR", "检查设备离线状态异常：" + e.getMessage()));
        }
    }

    // ==================== 离线业务处理接口 ====================

    /**
     * 处理离线消费记录
     * <p>
     * 处理设备上传的离线消费记录
     * </p>
     *
     * @param deviceId 设备ID
     * @param offlineRecords 离线记录列表
     * @return 处理结果
     */
    @Observed(name = "offline.sync.processRecords", contextualName = "offline-sync-process-records")
    @PostMapping("/device/{deviceId}/process-records")
    @Operation(
            summary = "处理离线消费记录",
            description = "处理设备上传的离线消费记录",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true)
            }
    )
    @PermissionCheck(value = "CONSUME_MANAGER", description = "离线数据管理")
    public ResponseEntity<ResponseDTO<OfflineSyncResultVO>> processOfflineConsumeRecords(
            @PathVariable String deviceId,
            @Valid @RequestBody List<Map<String, Object>> offlineRecords) {

        log.info("[离线数据同步] 处理离线消费记录，deviceId={}, recordCount={}", deviceId, offlineRecords.size());

        try {
            Future<OfflineSyncResultVO> result = offlineDataSyncService.processOfflineConsumeRecords(deviceId, offlineRecords);

            log.info("[离线数据同步] 离线消费记录处理任务提交成功，deviceId={}, recordCount={}",
                    deviceId, offlineRecords.size());

            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[离线数据同步] 处理离线消费记录异常，deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("PROCESS_RECORDS_ERROR", "处理离线消费记录异常：" + e.getMessage()));
        }
    }

    /**
     * 验证离线交易合法性
     * <p>
     * 对离线交易进行完整合法性验证
     * </p>
     *
     * @param transactionData 交易数据
     * @return 验证结果
     */
    @Observed(name = "offline.sync.validateTransaction", contextualName = "offline-sync-validate-transaction")
    @PostMapping("/validate-transaction")
    @Operation(
            summary = "验证离线交易合法性",
            description = "对离线交易进行完整合法性验证"
    )
    @PermissionCheck(value = "CONSUME_MANAGER", description = "离线数据管理")
    public ResponseEntity<ResponseDTO<Boolean>> validateOfflineTransactionLegality(
            @Valid @RequestBody Map<String, Object> transactionData) {

        log.info("[离线数据同步] 验证离线交易合法性，transactionId={}", transactionData.get("transactionId"));

        try {
            boolean isValid = offlineDataSyncService.validateOfflineTransactionLegality(transactionData);

            log.info("[离线数据同步] 离线交易验证完成，transactionId={}, valid={}",
                    transactionData.get("transactionId"), isValid);

            return ResponseEntity.ok(ResponseDTO.ok(isValid));

        } catch (Exception e) {
            log.error("[离线数据同步] 验证离线交易合法性异常，transactionId={}, error={}",
                    transactionData.get("transactionId"), e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("VALIDATE_TRANSACTION_ERROR", "验证离线交易异常：" + e.getMessage()));
        }
    }

    /**
     * 冲突检测和解决
     * <p>
     * 检测和解决离线数据冲突
     * </p>
     *
     * @param conflicts 冲突记录列表
     * @return 冲突解决结果
     */
    @Observed(name = "offline.sync.resolveConflicts", contextualName = "offline-sync-resolve-conflicts")
    @PostMapping("/resolve-conflicts")
    @Operation(
            summary = "冲突检测和解决",
            description = "检测和解决离线数据冲突"
    )
    @PermissionCheck(value = "CONSUME_MANAGER", description = "离线数据管理")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> detectAndResolveConflicts(
            @Valid @RequestBody List<Map<String, Object>> conflicts) {

        log.info("[离线数据同步] 检测和解决冲突，conflictCount={}", conflicts.size());

        try {
            Map<String, Object> resolution = offlineDataSyncService.detectAndResolveConflicts(conflicts);

            log.info("[离线数据同步] 冲突解决完成，total={}, resolved={}",
                    resolution.get("totalConflicts"), resolution.get("resolvedCount"));

            return ResponseEntity.ok(ResponseDTO.ok(resolution));

        } catch (Exception e) {
            log.error("[离线数据同步] 解决冲突异常，error={}", e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("RESOLVE_CONFLICTS_ERROR", "解决冲突异常：" + e.getMessage()));
        }
    }

    /**
     * 数据一致性校验
     * <p>
     * 执行完整的数据一致性校验
     * </p>
     *
     * @param deviceId 设备ID
     * @param checkType 校验类型
     * @return 一致性校验报告
     */
    @Observed(name = "offline.sync.consistencyCheck", contextualName = "offline-sync-consistency-check")
    @PostMapping("/device/{deviceId}/consistency-check")
    @Operation(
            summary = "数据一致性校验",
            description = "执行完整的数据一致性校验",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true),
                    @Parameter(name = "checkType", description = "校验类型 (FULL/PARTIAL)")
            }
    )
    @PermissionCheck(value = "CONSUME_MANAGER", description = "离线数据管理")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> performDataConsistencyCheck(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "PARTIAL") String checkType) {

        log.info("[离线数据同步] 执行数据一致性校验，deviceId={}, checkType={}", deviceId, checkType);

        try {
            Map<String, Object> report = offlineDataSyncService.performDataConsistencyCheck(deviceId, checkType);

            log.info("[离线数据同步] 数据一致性校验完成，deviceId={}, status={}",
                    deviceId, report.get("status"));

            return ResponseEntity.ok(ResponseDTO.ok(report));

        } catch (Exception e) {
            log.error("[离线数据同步] 执行数据一致性校验异常，deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("CONSISTENCY_CHECK_ERROR", "数据一致性校验异常：" + e.getMessage()));
        }
    }

    // ==================== 同步策略管理接口 ====================

    /**
     * 配置设备同步策略
     * <p>
     * 为设备配置个性化的数据同步策略
     * </p>
     *
     * @param deviceId 设备ID
     * @param syncStrategy 同步策略配置
     * @return 配置结果
     */
    @Observed(name = "offline.sync.configureStrategy", contextualName = "offline-sync-configure-strategy")
    @PostMapping("/device/{deviceId}/sync-strategy")
    @Operation(
            summary = "配置设备同步策略",
            description = "为设备配置个性化的数据同步策略",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true)
            }
    )
    @PermissionCheck(value = "CONSUME_MANAGER", description = "离线数据管理")
    public ResponseEntity<ResponseDTO<String>> configureDeviceSyncStrategy(
            @PathVariable String deviceId,
            @Valid @RequestBody Map<String, Object> syncStrategy) {

        log.info("[离线数据同步] 配置设备同步策略，deviceId={}", deviceId);

        try {
            boolean success = offlineDataSyncService.configureDeviceSyncStrategy(deviceId, syncStrategy);

            if (success) {
                log.info("[离线数据同步] 设备同步策略配置成功，deviceId={}", deviceId);
                return ResponseEntity.ok(ResponseDTO.ok("设备同步策略配置成功"));
            } else {
                log.warn("[离线数据同步] 设备同步策略配置失败，deviceId={}", deviceId);
                return ResponseEntity.ok(ResponseDTO.error("CONFIGURE_STRATEGY_FAILED", "设备同步策略配置失败"));
            }

        } catch (Exception e) {
            log.error("[离线数据同步] 配置设备同步策略异常，deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("CONFIGURE_STRATEGY_ERROR", "配置设备同步策略异常：" + e.getMessage()));
        }
    }

    /**
     * 获取设备同步策略
     * <p>
     * 查询设备当前的同步策略配置
     * </p>
     *
     * @param deviceId 设备ID
     * @return 同步策略信息
     */
    @Observed(name = "offline.sync.getStrategy", contextualName = "offline-sync-get-strategy")
    @GetMapping("/device/{deviceId}/sync-strategy")
    @Operation(
            summary = "获取设备同步策略",
            description = "查询设备当前的同步策略配置",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true)
            }
    )
    @PermissionCheck(value = "CONSUME_MANAGER", description = "离线数据管理")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> getDeviceSyncStrategy(@PathVariable String deviceId) {
        log.info("[离线数据同步] 获取设备同步策略，deviceId={}", deviceId);

        try {
            Map<String, Object> strategy = offlineDataSyncService.getDeviceSyncStrategy(deviceId);

            log.info("[离线数据同步] 设备同步策略获取成功，deviceId={}", deviceId);

            return ResponseEntity.ok(ResponseDTO.ok(strategy));

        } catch (Exception e) {
            log.error("[离线数据同步] 获取设备同步策略异常，deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("GET_STRATEGY_ERROR", "获取设备同步策略异常：" + e.getMessage()));
        }
    }

    /**
     * 执行智能同步调度
     * <p>
     * 基于网络状况和优先级进行智能同步
     * </p>
     *
     * @param deviceIds 设备ID列表
     * @return 调度执行结果
     */
    @Observed(name = "offline.sync.intelligentScheduling", contextualName = "offline-sync-intelligent-scheduling")
    @PostMapping("/intelligent-scheduling")
    @Operation(
            summary = "执行智能同步调度",
            description = "基于网络状况和优先级进行智能同步"
    )
    @PermissionCheck(value = "CONSUME_MANAGER", description = "离线数据管理")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> executeIntelligentSyncScheduling(
            @Valid @RequestBody List<String> deviceIds) {

        log.info("[离线数据同步] 执行智能同步调度，deviceCount={}", deviceIds.size());

        try {
            Future<Map<String, Object>> result = offlineDataSyncService.executeIntelligentSyncScheduling(deviceIds);

            log.info("[离线数据同步] 智能同步调度任务提交成功，deviceCount={}", deviceIds.size());

            return ResponseEntity.ok(ResponseDTO.ok(result));

        } catch (Exception e) {
            log.error("[离线数据同步] 执行智能同步调度异常，error={}", e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("INTELLIGENT_SCHEDULING_ERROR", "智能同步调度异常：" + e.getMessage()));
        }
    }

    /**
     * 获取同步统计信息
     * <p>
     * 获取离线数据同步的详细统计
     * </p>
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param deviceId 设备ID（可选）
     * @return 统计信息
     */
    @Observed(name = "offline.sync.statistics", contextualName = "offline-sync-statistics")
    @GetMapping("/statistics")
    @Operation(
            summary = "获取同步统计信息",
            description = "获取离线数据同步的详细统计",
            parameters = {
                    @Parameter(name = "startTime", description = "开始时间", required = true),
                    @Parameter(name = "endTime", description = "结束时间", required = true),
                    @Parameter(name = "deviceId", description = "设备ID（可选）")
            }
    )
    @PermissionCheck(value = "CONSUME_MANAGER", description = "离线数据管理")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> getSyncStatistics(
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(required = false) String deviceId) {

        log.info("[离线数据同步] 获取同步统计信息，startTime={}, endTime={}, deviceId={}",
                startTime, endTime, deviceId);

        try {
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);

            Map<String, Object> statistics = offlineDataSyncService.getSyncStatistics(start, end, deviceId);

            log.info("[离线数据同步] 同步统计信息获取成功，successRate={}%", statistics.get("successRate"));

            return ResponseEntity.ok(ResponseDTO.ok(statistics));

        } catch (Exception e) {
            log.error("[离线数据同步] 获取同步统计信息异常，error={}", e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("GET_STATISTICS_ERROR", "获取同步统计信息异常：" + e.getMessage()));
        }
    }

    // ==================== 网络状态感知接口 ====================

    /**
     * 检测网络连接质量
     * <p>
     * 实时检测网络连接质量状况
     * </p>
     *
     * @param deviceId 设备ID
     * @return 网络质量评估结果
     */
    @Observed(name = "offline.sync.detectNetworkQuality", contextualName = "offline-sync-detect-network-quality")
    @GetMapping("/device/{deviceId}/network-quality")
    @Operation(
            summary = "检测网络连接质量",
            description = "实时检测网络连接质量状况",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true)
            }
    )
    @PermissionCheck(value = "CONSUME_USER", description = "离线数据查询")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> detectNetworkQuality(@PathVariable String deviceId) {
        log.info("[离线数据同步] 检测网络连接质量，deviceId={}", deviceId);

        try {
            Map<String, Object> networkQuality = offlineDataSyncService.detectNetworkQuality(deviceId);

            log.info("[离线数据同步] 网络质量检测完成，deviceId={}, quality={}",
                    deviceId, networkQuality.get("quality"));

            return ResponseEntity.ok(ResponseDTO.ok(networkQuality));

        } catch (Exception e) {
            log.error("[离线数据同步] 检测网络质量异常，deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("DETECT_NETWORK_QUALITY_ERROR", "检测网络质量异常：" + e.getMessage()));
        }
    }

    /**
     * 批量设备离线状态检查
     * <p>
     * 批量检查多个设备的离线状态
     * </p>
     *
     * @param deviceIds 设备ID列表
     * @return 批量状态检查结果
     */
    @Observed(name = "offline.sync.batchCheckStatus", contextualName = "offline-sync-batch-check-status")
    @PostMapping("/batch-check-status")
    @Operation(
            summary = "批量设备离线状态检查",
            description = "批量检查多个设备的离线状态"
    )
    @PermissionCheck(value = "CONSUME_MANAGER", description = "离线数据管理")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> batchCheckOfflineStatus(
            @Valid @RequestBody List<String> deviceIds) {

        log.info("[离线数据同步] 批量检查离线状态，deviceCount={}", deviceIds.size());

        try {
            Map<String, Object> batchResult = offlineDataSyncService.batchCheckOfflineStatus(deviceIds);

            log.info("[离线数据同步] 批量离线状态检查完成，total={}, ready={}",
                    batchResult.get("totalDevices"), batchResult.get("readyDevices"));

            return ResponseEntity.ok(ResponseDTO.ok(batchResult));

        } catch (Exception e) {
            log.error("[离线数据同步] 批量检查离线状态异常，error={}", e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("BATCH_CHECK_STATUS_ERROR", "批量检查离线状态异常：" + e.getMessage()));
        }
    }

    /**
     * 适应网络状况的同步策略
     * <p>
     * 根据网络状况动态调整同步策略
     * </p>
     *
     * @param deviceId 设备ID
     * @param networkStatus 网络状态信息
     * @return 适应性策略配置
     */
    @Observed(name = "offline.sync.adaptNetworkStrategy", contextualName = "offline-sync-adapt-network-strategy")
    @PostMapping("/device/{deviceId}/adapt-network-strategy")
    @Operation(
            summary = "适应网络状况的同步策略",
            description = "根据网络状况动态调整同步策略",
            parameters = {
                    @Parameter(name = "deviceId", description = "设备ID", required = true)
            }
    )
    @PermissionCheck(value = "CONSUME_MANAGER", description = "离线数据管理")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> adaptNetworkConditionStrategy(
            @PathVariable String deviceId,
            @Valid @RequestBody Map<String, Object> networkStatus) {

        log.info("[离线数据同步] 适应网络状况策略，deviceId={}", deviceId);

        try {
            Map<String, Object> adaptiveStrategy = offlineDataSyncService.adaptNetworkConditionStrategy(deviceId, networkStatus);

            log.info("[离线数据同步] 网络适应策略生成完成，deviceId={}, quality={}",
                    deviceId, adaptiveStrategy.get("networkQuality"));

            return ResponseEntity.ok(ResponseDTO.ok(adaptiveStrategy));

        } catch (Exception e) {
            log.error("[离线数据同步] 适应网络状况策略异常，deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseEntity.ok(ResponseDTO.error("ADAPT_NETWORK_STRATEGY_ERROR", "适应网络状况策略异常：" + e.getMessage()));
        }
    }
}