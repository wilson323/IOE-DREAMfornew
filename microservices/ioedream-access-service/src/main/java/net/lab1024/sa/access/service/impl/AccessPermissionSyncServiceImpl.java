package net.lab1024.sa.access.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.service.AccessPermissionSyncService;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.dao.AreaDeviceDao;
import net.lab1024.sa.common.organization.dao.AreaUserDao;
import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;
import net.lab1024.sa.common.organization.entity.AreaUserEntity;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 门禁权限同步服务实现
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Service注解注册为Spring Bean
 * - 使用@Resource注入依赖
 * - 通过GatewayServiceClient调用设备通讯服务
 * - 异步处理权限同步任务
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
public class AccessPermissionSyncServiceImpl implements AccessPermissionSyncService {

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private AreaDeviceDao areaDeviceDao;

    @Resource
    private AreaUserDao areaUserDao;

    /**
     * 同步权限到设备
     */
    @Override
    @Async("permissionSyncExecutor")
    public ResponseDTO<PermissionSyncResult> syncPermissionToDevices(Long userId, Long areaId) {
        log.info("[权限同步] 开始同步权限到设备: userId={}, areaId={}", userId, areaId);

        PermissionSyncResult result = new PermissionSyncResult();
        List<DeviceSyncRecord> syncRecords = new ArrayList<>();

        try {
            // 1. 查询区域关联的门禁设备
            List<AreaDeviceEntity> devices = areaDeviceDao.selectByAreaIdAndDeviceType(areaId, 1); // 1=门禁设备
            if (devices == null || devices.isEmpty()) {
                log.warn("[权限同步] 区域无门禁设备: areaId={}", areaId);
                result.setTotalCount(0);
                result.setSuccessCount(0);
                result.setFailCount(0);
                result.setSyncRecords(new ArrayList<>());
                return ResponseDTO.ok(result);
            }

            result.setTotalCount(devices.size());

            // 2. 查询用户权限信息
            AreaUserEntity permission = areaUserDao.selectByAreaIdAndUserId(areaId, userId);
            if (permission == null) {
                log.warn("[权限同步] 用户无该区域权限: userId={}, areaId={}", userId, areaId);
                result.setTotalCount(devices.size());
                result.setSuccessCount(0);
                result.setFailCount(devices.size());
                result.setSyncRecords(new ArrayList<>());
                return ResponseDTO.ok(result);
            }

            // 3. 构建权限数据
            Map<String, Object> permissionData = buildPermissionData(permission);

            // 4. 并行同步到所有设备
            List<CompletableFuture<DeviceSyncRecord>> futures = devices.stream()
                    .map(device -> CompletableFuture.supplyAsync(() -> {
                        return syncPermissionToSingleDevice(device, userId, permissionData);
                    }))
                    .collect(Collectors.toList());

            // 5. 等待所有同步完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // 6. 收集同步结果
            int successCount = 0;
            int failCount = 0;
            for (CompletableFuture<DeviceSyncRecord> future : futures) {
                try {
                    DeviceSyncRecord record = future.get();
                    syncRecords.add(record);
                    if (record.getSuccess()) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    log.error("[权限同步] 获取同步结果异常", e);
                    failCount++;
                }
            }

            result.setSuccessCount(successCount);
            result.setFailCount(failCount);
            result.setSyncRecords(syncRecords);

            // 7. 更新同步状态
            updateSyncStatus(permission.getRelationId(), successCount == devices.size() ? 2 : 3, null);

            log.info("[权限同步] 同步完成: userId={}, areaId={}, total={}, success={}, fail={}",
                    userId, areaId, result.getTotalCount(), successCount, failCount);

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[权限同步] 同步权限异常: userId={}, areaId={}, error={}", userId, areaId, e.getMessage(), e);
            result.setTotalCount(0);
            result.setSuccessCount(0);
            result.setFailCount(0);
            result.setSyncRecords(new ArrayList<>());
            return ResponseDTO.error("SYNC_ERROR", "权限同步异常: " + e.getMessage());
        }
    }

    /**
     * 从设备移除权限
     */
    @Override
    @Async("permissionSyncExecutor")
    public ResponseDTO<PermissionSyncResult> removePermissionFromDevices(Long userId, Long areaId) {
        log.info("[权限同步] 开始从设备移除权限: userId={}, areaId={}", userId, areaId);

        PermissionSyncResult result = new PermissionSyncResult();
        List<DeviceSyncRecord> syncRecords = new ArrayList<>();

        try {
            // 1. 查询区域关联的门禁设备
            List<AreaDeviceEntity> devices = areaDeviceDao.selectByAreaIdAndDeviceType(areaId, 1); // 1=门禁设备
            if (devices == null || devices.isEmpty()) {
                log.warn("[权限同步] 区域无门禁设备: areaId={}", areaId);
                result.setTotalCount(0);
                result.setSuccessCount(0);
                result.setFailCount(0);
                result.setSyncRecords(new ArrayList<>());
                return ResponseDTO.ok(result);
            }

            result.setTotalCount(devices.size());

            // 2. 并行从所有设备移除权限
            List<CompletableFuture<DeviceSyncRecord>> futures = devices.stream()
                    .map(device -> CompletableFuture.supplyAsync(() -> {
                        return removePermissionFromSingleDevice(device, userId);
                    }))
                    .collect(Collectors.toList());

            // 3. 等待所有移除完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // 4. 收集移除结果
            int successCount = 0;
            int failCount = 0;
            for (CompletableFuture<DeviceSyncRecord> future : futures) {
                try {
                    DeviceSyncRecord record = future.get();
                    syncRecords.add(record);
                    if (record.getSuccess()) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    log.error("[权限同步] 获取移除结果异常", e);
                    failCount++;
                }
            }

            result.setSuccessCount(successCount);
            result.setFailCount(failCount);
            result.setSyncRecords(syncRecords);

            log.info("[权限同步] 移除完成: userId={}, areaId={}, total={}, success={}, fail={}",
                    userId, areaId, result.getTotalCount(), successCount, failCount);

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[权限同步] 移除权限异常: userId={}, areaId={}, error={}", userId, areaId, e.getMessage(), e);
            result.setTotalCount(0);
            result.setSuccessCount(0);
            result.setFailCount(0);
            result.setSyncRecords(new ArrayList<>());
            return ResponseDTO.error("REMOVE_ERROR", "权限移除异常: " + e.getMessage());
        }
    }

    /**
     * 批量同步权限到设备
     */
    @Override
    @Async("permissionSyncExecutor")
    public ResponseDTO<PermissionSyncResult> batchSyncPermissionToDevices(List<Long> userIds, Long areaId) {
        log.info("[权限同步] 开始批量同步权限: userIds={}, areaId={}", userIds, areaId);

        PermissionSyncResult result = new PermissionSyncResult();
        List<DeviceSyncRecord> syncRecords = new ArrayList<>();

        try {
            int totalCount = 0;
            int successCount = 0;
            int failCount = 0;

            // 逐个用户同步
            for (Long userId : userIds) {
                ResponseDTO<PermissionSyncResult> userResult = syncPermissionToDevices(userId, areaId);
                if (userResult.isSuccess() && userResult.getData() != null) {
                    PermissionSyncResult userSyncResult = userResult.getData();
                    totalCount += userSyncResult.getTotalCount();
                    successCount += userSyncResult.getSuccessCount();
                    failCount += userSyncResult.getFailCount();
                    if (userSyncResult.getSyncRecords() != null) {
                        syncRecords.addAll(userSyncResult.getSyncRecords());
                    }
                } else {
                    failCount++;
                }
            }

            result.setTotalCount(totalCount);
            result.setSuccessCount(successCount);
            result.setFailCount(failCount);
            result.setSyncRecords(syncRecords);

            log.info("[权限同步] 批量同步完成: userIds={}, areaId={}, total={}, success={}, fail={}",
                    userIds, areaId, totalCount, successCount, failCount);

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[权限同步] 批量同步异常: userIds={}, areaId={}, error={}", userIds, areaId, e.getMessage(), e);
            result.setTotalCount(0);
            result.setSuccessCount(0);
            result.setFailCount(0);
            result.setSyncRecords(new ArrayList<>());
            return ResponseDTO.error("BATCH_SYNC_ERROR", "批量同步异常: " + e.getMessage());
        }
    }

    /**
     * 查询权限同步状态
     */
    @Override
    public ResponseDTO<PermissionSyncStatus> getSyncStatus(Long userId, Long areaId) {
        log.debug("[权限同步] 查询同步状态: userId={}, areaId={}", userId, areaId);

        try {
            AreaUserEntity permission = areaUserDao.selectByAreaIdAndUserId(areaId, userId);
            if (permission == null) {
                return ResponseDTO.error("PERMISSION_NOT_FOUND", "权限不存在");
            }

            PermissionSyncStatus status = new PermissionSyncStatus();
            status.setUserId(userId);
            status.setAreaId(areaId);
            status.setSyncStatus(permission.getDeviceSyncStatus());
            status.setLastSyncTime(permission.getLastSyncTime());
            status.setRetryCount(permission.getRetryCount());
            status.setLastErrorMessage(permission.getLastSyncError());

            return ResponseDTO.ok(status);

        } catch (Exception e) {
            log.error("[权限同步] 查询同步状态异常: userId={}, areaId={}, error={}", userId, areaId, e.getMessage(), e);
            return ResponseDTO.error("QUERY_ERROR", "查询同步状态异常: " + e.getMessage());
        }
    }

    /**
     * 重试失败的同步任务
     */
    @Override
    @Async("permissionSyncExecutor")
    public ResponseDTO<PermissionSyncResult> retrySync(Long userId, Long areaId) {
        log.info("[权限同步] 重试同步: userId={}, areaId={}", userId, areaId);

        // 查询当前同步状态
        ResponseDTO<PermissionSyncStatus> statusResult = getSyncStatus(userId, areaId);
        if (!statusResult.isSuccess() || statusResult.getData() == null) {
            return ResponseDTO.error("STATUS_ERROR", "无法获取同步状态");
        }

        PermissionSyncStatus status = statusResult.getData();
        if (status.getSyncStatus() != 3) { // 3=同步失败
            return ResponseDTO.error("NOT_FAILED", "当前状态不是失败状态，无需重试");
        }

        // 执行同步
        return syncPermissionToDevices(userId, areaId);
    }

    /**
     * 同步权限到单个设备
     */
    private DeviceSyncRecord syncPermissionToSingleDevice(AreaDeviceEntity device, Long userId, Map<String, Object> permissionData) {
        DeviceSyncRecord record = new DeviceSyncRecord();
        record.setDeviceId(device.getDeviceId());
        record.setDeviceName(device.getDeviceName());
        record.setSyncTime(System.currentTimeMillis());

        try {
            // 调用设备通讯服务下发权限
            Map<String, Object> request = new HashMap<>();
            request.put("deviceId", device.getDeviceId());
            request.put("userId", userId);
            request.put("permissionData", permissionData);
            request.put("operation", "ADD"); // 新增权限

            ResponseDTO<Map<String, Object>> response = gatewayServiceClient.callDeviceCommService(
                    "/api/v1/device/permission/sync",
                    org.springframework.http.HttpMethod.POST,
                    request,
                    Map.class
            );

            if (response != null && response.isSuccess()) {
                record.setSuccess(true);
                log.debug("[权限同步] 设备同步成功: deviceId={}, userId={}", device.getDeviceId(), userId);
            } else {
                record.setSuccess(false);
                String errorMsg = response != null ? response.getMessage() : "调用设备通讯服务失败";
                record.setErrorMessage(errorMsg);
                log.warn("[权限同步] 设备同步失败: deviceId={}, userId={}, error={}", device.getDeviceId(), userId, errorMsg);
            }

        } catch (Exception e) {
            record.setSuccess(false);
            record.setErrorMessage(e.getMessage());
            log.error("[权限同步] 设备同步异常: deviceId={}, userId={}, error={}", device.getDeviceId(), userId, e.getMessage(), e);
        }

        return record;
    }

    /**
     * 从单个设备移除权限
     */
    private DeviceSyncRecord removePermissionFromSingleDevice(AreaDeviceEntity device, Long userId) {
        DeviceSyncRecord record = new DeviceSyncRecord();
        record.setDeviceId(device.getDeviceId());
        record.setDeviceName(device.getDeviceName());
        record.setSyncTime(System.currentTimeMillis());

        try {
            // 调用设备通讯服务移除权限
            Map<String, Object> request = new HashMap<>();
            request.put("deviceId", device.getDeviceId());
            request.put("userId", userId);
            request.put("operation", "REMOVE"); // 移除权限

            ResponseDTO<Map<String, Object>> response = gatewayServiceClient.callDeviceCommService(
                    "/api/v1/device/permission/sync",
                    org.springframework.http.HttpMethod.POST,
                    request,
                    Map.class
            );

            if (response != null && response.isSuccess()) {
                record.setSuccess(true);
                log.debug("[权限同步] 设备移除成功: deviceId={}, userId={}", device.getDeviceId(), userId);
            } else {
                record.setSuccess(false);
                String errorMsg = response != null ? response.getMessage() : "调用设备通讯服务失败";
                record.setErrorMessage(errorMsg);
                log.warn("[权限同步] 设备移除失败: deviceId={}, userId={}, error={}", device.getDeviceId(), userId, errorMsg);
            }

        } catch (Exception e) {
            record.setSuccess(false);
            record.setErrorMessage(e.getMessage());
            log.error("[权限同步] 设备移除异常: deviceId={}, userId={}, error={}", device.getDeviceId(), userId, e.getMessage(), e);
        }

        return record;
    }

    /**
     * 构建权限数据
     */
    private Map<String, Object> buildPermissionData(AreaUserEntity permission) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", permission.getUserId());
        data.put("username", permission.getUsername());
        data.put("realName", permission.getRealName());
        data.put("areaId", permission.getAreaId());
        data.put("areaCode", permission.getAreaCode());
        data.put("permissionLevel", permission.getPermissionLevel());
        data.put("effectiveTime", permission.getEffectiveTime());
        data.put("expireTime", permission.getExpireTime());
        data.put("permanent", permission.getPermanent());
        data.put("allowStartTime", permission.getAllowStartTime());
        data.put("allowEndTime", permission.getAllowEndTime());
        data.put("workdayOnly", permission.getWorkdayOnly());
        data.put("accessPermissions", permission.getAccessPermissions());
        data.put("extendedAttributes", permission.getExtendedAttributes());
        return data;
    }

    /**
     * 更新同步状态
     */
    private void updateSyncStatus(String relationId, Integer syncStatus, String errorMessage) {
        try {
            areaUserDao.updateSyncStatus(relationId, syncStatus, errorMessage, LocalDateTime.now());
            log.debug("[权限同步] 同步状态已更新: relationId={}, syncStatus={}", relationId, syncStatus);
        } catch (Exception e) {
            log.error("[权限同步] 更新同步状态异常: relationId={}, error={}", relationId, e.getMessage(), e);
        }
    }
}
