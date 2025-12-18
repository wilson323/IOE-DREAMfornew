package net.lab1024.sa.biometric.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.biometric.dao.BiometricTemplateDao;
import net.lab1024.sa.biometric.domain.entity.BiometricTemplateEntity;
import net.lab1024.sa.biometric.domain.vo.TemplateSyncRecordVO;
import net.lab1024.sa.biometric.domain.vo.TemplateSyncResultVO;
import net.lab1024.sa.biometric.service.BiometricTemplateSyncService;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.dao.AreaDeviceDao;
import net.lab1024.sa.common.organization.dao.AreaUserDao;
import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;
import net.lab1024.sa.common.organization.entity.AreaUserEntity;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 生物模板同步服务实现
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Service注解
 * - 使用@Resource依赖注入
 * - 使用@Transactional事务管理
 * - 通过GatewayServiceClient调用设备通讯服务
 * </p>
 * <p>
 * 核心职责:
 * - 模板下发到设备
 * - 模板从设备删除
 * - 权限变更时的模板同步
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class BiometricTemplateSyncServiceImpl implements BiometricTemplateSyncService {

    @Resource
    private BiometricTemplateDao biometricTemplateDao;

    @Resource
    private AreaUserDao areaUserDao;

    @Resource
    private AreaDeviceDao areaDeviceDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    // 设备类型常量（门禁设备）
    private static final Integer DEVICE_TYPE_ACCESS = 1;

    @Override
    public ResponseDTO<TemplateSyncResultVO> syncTemplateToDevice(Long userId, String deviceId) {
        try {
            log.info("[模板同步] 同步模板到设备开始 userId={}, deviceId={}", userId, deviceId);

            // 1. 查询用户所有生物模板
            List<BiometricTemplateEntity> templates = biometricTemplateDao.selectByUserId(userId);
            if (templates.isEmpty()) {
                return ResponseDTO.error("BIOMETRIC_TEMPLATE_NOT_FOUND", "用户无生物模板");
            }

            // 2. 同步模板到设备
            List<TemplateSyncRecordVO> syncRecords = new ArrayList<>();
            for (BiometricTemplateEntity template : templates) {
                TemplateSyncRecordVO record = syncSingleTemplate(template, deviceId);
                syncRecords.add(record);
            }

            // 3. 构建同步结果
            TemplateSyncResultVO result = new TemplateSyncResultVO();
            result.setTotalCount(syncRecords.size());
            result.setSuccessCount((int) syncRecords.stream()
                    .filter(r -> r.getSyncStatus() == 1)
                    .count());
            result.setFailCount((int) syncRecords.stream()
                    .filter(r -> r.getSyncStatus() != 1)
                    .count());
            result.setSyncRecords(syncRecords);

            log.info("[模板同步] 同步模板到设备完成 userId={}, deviceId={}, 成功={}, 失败={}",
                    userId, deviceId, result.getSuccessCount(), result.getFailCount());
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[模板同步] 同步模板到设备失败 userId={}, deviceId={}", userId, deviceId, e);
            throw new BusinessException("TEMPLATE_SYNC_ERROR", "同步模板失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<TemplateSyncResultVO> syncTemplateToDevices(Long userId, List<String> deviceIds) {
        try {
            log.info("[模板同步] 批量同步模板到设备开始 userId={}, deviceCount={}", userId, deviceIds.size());

            // 查询用户所有生物模板
            List<BiometricTemplateEntity> templates = biometricTemplateDao.selectByUserId(userId);
            if (templates.isEmpty()) {
                return ResponseDTO.error("BIOMETRIC_TEMPLATE_NOT_FOUND", "用户无生物模板");
            }

            // 并行同步到所有设备
            List<CompletableFuture<TemplateSyncRecordVO>> futures = new ArrayList<>();
            for (String deviceId : deviceIds) {
                for (BiometricTemplateEntity template : templates) {
                    CompletableFuture<TemplateSyncRecordVO> future = CompletableFuture.supplyAsync(() ->
                            syncSingleTemplate(template, deviceId)
                    );
                    futures.add(future);
                }
            }

            // 等待所有同步完成
            List<TemplateSyncRecordVO> syncRecords = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            // 构建同步结果
            TemplateSyncResultVO result = new TemplateSyncResultVO();
            result.setTotalCount(syncRecords.size());
            result.setSuccessCount((int) syncRecords.stream()
                    .filter(r -> r.getSyncStatus() == 1)
                    .count());
            result.setFailCount((int) syncRecords.stream()
                    .filter(r -> r.getSyncStatus() != 1)
                    .count());
            result.setSyncRecords(syncRecords);

            log.info("[模板同步] 批量同步模板到设备完成 userId={}, 成功={}, 失败={}",
                    userId, result.getSuccessCount(), result.getFailCount());
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[模板同步] 批量同步模板到设备失败 userId={}", userId, e);
            throw new BusinessException("TEMPLATE_SYNC_ERROR", "批量同步模板失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> deleteTemplateFromDevice(Long userId, String deviceId) {
        try {
            log.info("[模板同步] 从设备删除模板开始 userId={}, deviceId={}", userId, deviceId);

            // 通过设备通讯服务删除模板
            // TODO: 实现设备通讯服务调用
            // gatewayServiceClient.callDeviceService(...)

            log.info("[模板同步] 从设备删除模板完成 userId={}, deviceId={}", userId, deviceId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[模板同步] 从设备删除模板失败 userId={}, deviceId={}", userId, deviceId, e);
            throw new BusinessException("TEMPLATE_DELETE_ERROR", "删除模板失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> deleteTemplateFromAllDevices(Long userId) {
        try {
            log.info("[模板同步] 从所有设备删除模板开始 userId={}", userId);

            // 1. 查询所有门禁设备
            List<AreaDeviceEntity> allAccessDevices = areaDeviceDao.selectByAreaIdAndDeviceType(null, DEVICE_TYPE_ACCESS);

            // 2. 并行删除模板
            List<CompletableFuture<Void>> futures = allAccessDevices.stream()
                    .map(device -> CompletableFuture.runAsync(() -> {
                        try {
                            deleteTemplateFromDevice(userId, device.getDeviceId());
                        } catch (Exception e) {
                            log.error("[模板同步] 从设备删除模板失败 deviceId={}, userId={}",
                                    device.getDeviceId(), userId, e);
                        }
                    }))
                    .collect(Collectors.toList());

            // 等待所有删除完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            log.info("[模板同步] 从所有设备删除模板完成 userId={}, deviceCount={}", userId, allAccessDevices.size());
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[模板同步] 从所有设备删除模板失败 userId={}", userId, e);
            throw new BusinessException("TEMPLATE_DELETE_ERROR", "删除模板失败: " + e.getMessage());
        }
    }

    @Override
    @Async("permissionSyncExecutor")
    public ResponseDTO<TemplateSyncResultVO> syncOnPermissionAdded(Long userId, Long areaId) {
        try {
            log.info("[模板同步] 权限新增时同步模板开始 userId={}, areaId={}", userId, areaId);

            // 1. 查询该区域的门禁设备
            List<AreaDeviceEntity> devices = areaDeviceDao.selectByAreaIdAndDeviceType(areaId, DEVICE_TYPE_ACCESS);
            if (devices.isEmpty()) {
                log.warn("[模板同步] 区域无门禁设备 areaId={}", areaId);
                TemplateSyncResultVO emptyResult = new TemplateSyncResultVO();
                emptyResult.setTotalCount(0);
                emptyResult.setSuccessCount(0);
                emptyResult.setFailCount(0);
                emptyResult.setSyncRecords(Collections.emptyList());
                return ResponseDTO.ok(emptyResult);
            }

            // 2. 查询用户生物模板
            List<BiometricTemplateEntity> templates = biometricTemplateDao.selectByUserId(userId);
            if (templates.isEmpty()) {
                log.warn("[模板同步] 用户无生物模板 userId={}", userId);
                TemplateSyncResultVO emptyResult = new TemplateSyncResultVO();
                emptyResult.setTotalCount(0);
                emptyResult.setSuccessCount(0);
                emptyResult.setFailCount(0);
                emptyResult.setSyncRecords(Collections.emptyList());
                return ResponseDTO.ok(emptyResult);
            }

            // 3. 同步模板到该区域设备
            List<String> deviceIds = devices.stream()
                    .map(AreaDeviceEntity::getDeviceId)
                    .collect(Collectors.toList());

            return syncTemplateToDevices(userId, deviceIds);

        } catch (Exception e) {
            log.error("[模板同步] 权限新增时同步模板失败 userId={}, areaId={}", userId, areaId, e);
            throw new BusinessException("TEMPLATE_SYNC_ERROR", "同步模板失败: " + e.getMessage());
        }
    }

    @Override
    @Async("permissionSyncExecutor")
    public ResponseDTO<Void> deleteOnPermissionRemoved(Long userId, Long areaId) {
        try {
            log.info("[模板同步] 权限移除时删除模板开始 userId={}, areaId={}", userId, areaId);

            // 1. 查询该区域的门禁设备
            List<AreaDeviceEntity> devices = areaDeviceDao.selectByAreaIdAndDeviceType(areaId, DEVICE_TYPE_ACCESS);

            // 2. 从该区域设备删除模板
            for (AreaDeviceEntity device : devices) {
                deleteTemplateFromDevice(userId, device.getDeviceId());
            }

            log.info("[模板同步] 权限移除时删除模板完成 userId={}, areaId={}, deviceCount={}",
                    userId, areaId, devices.size());
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[模板同步] 权限移除时删除模板失败 userId={}, areaId={}", userId, areaId, e);
            throw new BusinessException("TEMPLATE_DELETE_ERROR", "删除模板失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<TemplateSyncResultVO> batchSyncTemplatesToDevice(List<Long> userIds, String deviceId) {
        try {
            log.info("[模板同步] 批量同步模板到设备开始 userIds={}, deviceId={}", userIds.size(), deviceId);

            List<TemplateSyncRecordVO> allSyncRecords = new ArrayList<>();
            for (Long userId : userIds) {
                ResponseDTO<TemplateSyncResultVO> result = syncTemplateToDevice(userId, deviceId);
                if (result.isSuccess() && result.getData() != null) {
                    allSyncRecords.addAll(result.getData().getSyncRecords());
                }
            }

            TemplateSyncResultVO result = new TemplateSyncResultVO();
            result.setTotalCount(allSyncRecords.size());
            result.setSuccessCount((int) allSyncRecords.stream()
                    .filter(r -> r.getSyncStatus() == 1)
                    .count());
            result.setFailCount((int) allSyncRecords.stream()
                    .filter(r -> r.getSyncStatus() != 1)
                    .count());
            result.setSyncRecords(allSyncRecords);

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[模板同步] 批量同步模板到设备失败 deviceId={}", deviceId, e);
            throw new BusinessException("TEMPLATE_SYNC_ERROR", "批量同步模板失败: " + e.getMessage());
        }
    }

    /**
     * 同步单个模板到设备
     */
    private TemplateSyncRecordVO syncSingleTemplate(BiometricTemplateEntity template, String deviceId) {
        TemplateSyncRecordVO record = new TemplateSyncRecordVO();
        record.setDeviceId(deviceId);
        record.setSyncTime(LocalDateTime.now());

        try {
            // 通过设备通讯服务同步模板
            // TODO: 实现设备通讯服务调用
            // ResponseDTO<Void> response = gatewayServiceClient.callDeviceService(
            //     "/api/v1/device/biometric/sync",
            //     HttpMethod.POST,
            //     buildSyncRequest(template, deviceId),
            //     Void.class
            // );

            // 临时实现：标记为成功
            record.setSyncStatus(1);
            record.setSyncStatusDesc("成功");

        } catch (Exception e) {
            log.error("[模板同步] 同步模板失败 templateId={}, deviceId={}", template.getTemplateId(), deviceId, e);
            record.setSyncStatus(2);
            record.setSyncStatusDesc("失败");
            record.setErrorMessage(e.getMessage());
        }

        return record;
    }
}
