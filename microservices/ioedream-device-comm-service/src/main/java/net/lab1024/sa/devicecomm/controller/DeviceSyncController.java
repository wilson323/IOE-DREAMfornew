package net.lab1024.sa.devicecomm.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.devicecomm.service.DeviceSyncService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 设备同步控制器
 * <p>
 * 提供设备用户同步、权限管理、健康检查、性能指标等接口
 * 严格遵循CLAUDE.md规范：
 * - 使用@RestController注解
 * - 使用@Resource注入依赖
 * - 统一使用ResponseDTO封装响应
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/device")
public class DeviceSyncController {

    @Resource
    private DeviceSyncService deviceSyncService;

    /**
     * 同步用户信息到设备
     * <p>
     * 将用户信息同步到指定设备
     * 支持异步处理和状态回调
     * </p>
     *
     * @param request 同步请求（包含deviceId, userId, userInfo等）
     * @return 同步结果
     */
    @PostMapping("/user/sync")
    public ResponseDTO<Map<String, Object>> syncUser(@RequestBody Map<String, Object> request) {
        log.info("[设备同步] 同步用户信息, request={}", request);

        try {
            // 参数验证
            Object deviceIdObj = request.get("deviceId");
            Object userIdObj = request.get("userId");

            if (deviceIdObj == null || userIdObj == null) {
                return ResponseDTO.error("PARAM_ERROR", "deviceId和userId不能为空");
            }

            String deviceId = deviceIdObj.toString();
            Long userId = Long.parseLong(userIdObj.toString());

            // 调用服务层同步用户信息
            Map<String, Object> result = deviceSyncService.syncUserInfo(deviceId, userId, request);

            return ResponseDTO.ok(result);

        } catch (NumberFormatException e) {
            log.error("[设备同步] 用户ID格式错误, request={}", request, e);
            return ResponseDTO.error("PARAM_ERROR", "userId格式错误");
        } catch (Exception e) {
            log.error("[设备同步] 同步用户信息异常, request={}", request, e);
            return ResponseDTO.error("SYNC_ERROR", "同步用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 撤销用户在设备上的权限
     * <p>
     * 从指定设备上撤销用户的权限
     * 支持单个和批量撤销
     * </p>
     *
     * @param deviceId 设备ID
     * @param userId 用户ID
     * @return 撤销结果
     */
    @DeleteMapping("/user/{deviceId}/{userId}")
    public ResponseDTO<Map<String, Object>> revokeUserPermission(
            @PathVariable("deviceId") String deviceId,
            @PathVariable("userId") String userId) {
        log.info("[设备同步] 撤销用户权限, deviceId={}, userId={}", deviceId, userId);

        try {
            Long userIdLong = Long.parseLong(userId);
            Map<String, Object> result = deviceSyncService.revokeUserPermission(deviceId, userIdLong);

            return ResponseDTO.ok(result);

        } catch (NumberFormatException e) {
            log.error("[设备同步] 用户ID格式错误, deviceId={}, userId={}", deviceId, userId, e);
            return ResponseDTO.error("PARAM_ERROR", "userId格式错误");
        } catch (Exception e) {
            log.error("[设备同步] 撤销用户权限异常, deviceId={}, userId={}", deviceId, userId, e);
            return ResponseDTO.error("REVOKE_ERROR", "撤销用户权限失败：" + e.getMessage());
        }
    }

    /**
     * 获取设备上的用户列表
     * <p>
     * 查询指定设备上的所有用户ID列表
     * 支持分页查询（如果用户数量大）
     * </p>
     *
     * @param deviceId 设备ID
     * @return 用户ID列表
     */
    @GetMapping("/users/{deviceId}")
    public ResponseDTO<List<String>> getDeviceUsers(@PathVariable("deviceId") String deviceId) {
        log.debug("[设备同步] 获取设备用户列表, deviceId={}", deviceId);

        try {
            List<String> users = deviceSyncService.getDeviceUsers(deviceId);
            return ResponseDTO.ok(users);

        } catch (Exception e) {
            log.error("[设备同步] 获取设备用户列表异常, deviceId={}", deviceId, e);
            return ResponseDTO.error("QUERY_ERROR", "获取设备用户列表失败：" + e.getMessage());
        }
    }

    /**
     * 同步业务属性到设备
     * <p>
     * 将业务属性同步到指定设备
     * 支持部分属性更新和版本控制
     * </p>
     *
     * @param request 同步请求（包含deviceId, attributes等）
     * @return 同步结果
     */
    @PostMapping("/business-attributes/sync")
    public ResponseDTO<Map<String, Object>> syncBusinessAttributes(@RequestBody Map<String, Object> request) {
        log.info("[设备同步] 同步业务属性, deviceId={}", request.get("deviceId"));

        try {
            Object deviceIdObj = request.get("deviceId");
            Object attributesObj = request.get("attributes");

            if (deviceIdObj == null) {
                return ResponseDTO.error("PARAM_ERROR", "deviceId不能为空");
            }

            String deviceId = deviceIdObj.toString();
            @SuppressWarnings("unchecked")
            Map<String, Object> attributes = (Map<String, Object>) attributesObj;

            Map<String, Object> result = deviceSyncService.syncBusinessAttributes(deviceId, attributes);

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[设备同步] 同步业务属性异常, request={}", request, e);
            return ResponseDTO.error("SYNC_ERROR", "同步业务属性失败：" + e.getMessage());
        }
    }

    /**
     * 设备健康检查
     * <p>
     * 检查指定设备的健康状态
     * 支持超时控制（默认3秒）
     * </p>
     *
     * @param deviceId 设备ID
     * @return 健康检查结果
     */
    @GetMapping("/health/check/{deviceId}")
    public ResponseDTO<Map<String, Object>> healthCheck(@PathVariable("deviceId") String deviceId) {
        log.debug("[设备同步] 设备健康检查, deviceId={}", deviceId);

        try {
            Map<String, Object> healthStatus = deviceSyncService.checkDeviceHealth(deviceId);
            return ResponseDTO.ok(healthStatus);

        } catch (Exception e) {
            log.error("[设备同步] 设备健康检查异常, deviceId={}", deviceId, e);
            return ResponseDTO.error("HEALTH_CHECK_ERROR", "设备健康检查失败：" + e.getMessage());
        }
    }

    /**
     * 获取设备性能指标
     * <p>
     * 获取指定设备的性能指标（CPU、内存、网络延迟、响应时间、错误率等）
     * </p>
     *
     * @param deviceId 设备ID
     * @return 性能指标
     */
    @GetMapping("/metrics/{deviceId}")
    public ResponseDTO<Map<String, Object>> getDeviceMetrics(@PathVariable("deviceId") String deviceId) {
        log.debug("[设备同步] 获取设备性能指标, deviceId={}", deviceId);

        try {
            Map<String, Object> metrics = deviceSyncService.getDeviceMetrics(deviceId);
            return ResponseDTO.ok(metrics);

        } catch (Exception e) {
            log.error("[设备同步] 获取设备性能指标异常, deviceId={}", deviceId, e);
            return ResponseDTO.error("METRICS_ERROR", "获取设备性能指标失败：" + e.getMessage());
        }
    }

    /**
     * 设备心跳接口
     * <p>
     * 接收设备心跳请求，用于设备响应性检查
     * </p>
     *
     * @param request 心跳请求（包含deviceId, timestamp等）
     * @return 心跳响应
     */
    @PostMapping("/heartbeat")
    public ResponseDTO<Map<String, Object>> heartbeat(@RequestBody Map<String, Object> request) {
        log.debug("[设备同步] 设备心跳, deviceId={}", request.get("deviceId"));

        try {
            Object deviceIdObj = request.get("deviceId");
            if (deviceIdObj == null) {
                return ResponseDTO.error("PARAM_ERROR", "deviceId不能为空");
            }

            String deviceId = deviceIdObj.toString();
            Map<String, Object> result = deviceSyncService.processHeartbeat(deviceId, request);

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[设备同步] 处理设备心跳异常, request={}", request, e);
            return ResponseDTO.error("HEARTBEAT_ERROR", "处理设备心跳失败：" + e.getMessage());
        }
    }
}

