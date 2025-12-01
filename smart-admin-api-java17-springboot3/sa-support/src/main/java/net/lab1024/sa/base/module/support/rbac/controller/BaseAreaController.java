package net.lab1024.sa.base.module.support.rbac.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.module.support.rbac.annotation.RequireAreaPermission;
import net.lab1024.sa.base.module.support.rbac.service.AreaPermissionService;
import net.lab1024.sa.base.module.support.auth.LoginHelper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 区域权限验证控制器基类
 * <p>
 * 为需要区域权限验证的Controller提供通用的权限管理和查询功能
 * 支持DataScope.AREA数据权限控制
 * 严格遵循四层架构规范：Controller→Service→Manager→DAO
 *
 * @author SmartAdmin Team
 * @date 2025/11/25
 */
@Slf4j
public abstract class BaseAreaController {

    @Resource
    protected AreaPermissionService areaPermissionService;

    /**
     * 获取当前用户所有授权区域
     *
     * @return 授权区域列表
     */
    @GetMapping("/authorized-areas")
    public ResponseDTO<List<Long>> getUserAuthorizedAreas() {
        try {
            Long userId = LoginHelper.getLoginUserId();
            List<Long> areaIds = areaPermissionService.getUserAuthorizedAreaIds(userId);
            return ResponseDTO.ok(areaIds);
        } catch (Exception e) {
            log.error("获取用户授权区域失败", e);
            return ResponseDTO.error("获取授权区域失败");
        }
    }

    /**
     * 获取当前用户所有授权区域（包括子区域）
     *
     * @return 所有授权区域列表
     */
    @GetMapping("/all-authorized-areas")
    public ResponseDTO<List<Long>> getAllAuthorizedAreas() {
        try {
            Long userId = LoginHelper.getLoginUserId();
            List<Long> areaIds = areaPermissionService.getAllAuthorizedAreaIds(userId);
            return ResponseDTO.ok(areaIds);
        } catch (Exception e) {
            log.error("获取用户所有授权区域失败", e);
            return ResponseDTO.error("获取所有授权区域失败");
        }
    }

    /**
     * 获取当前用户授权区域路径列表
     *
     * @return 授权区域路径列表
     */
    @GetMapping("/authorized-area-paths")
    public ResponseDTO<List<String>> getUserAuthorizedAreaPaths() {
        try {
            Long userId = LoginHelper.getLoginUserId();
            List<String> areaPaths = areaPermissionService.getUserAuthorizedAreaPaths(userId);
            return ResponseDTO.ok(areaPaths);
        } catch (Exception e) {
            log.error("获取用户授权区域路径失败", e);
            return ResponseDTO.error("获取授权区域路径失败");
        }
    }

    /**
     * 检查用户是否具有指定区域权限
     *
     * @param areaId 区域ID
     * @return 是否有权限
     */
    @GetMapping("/check-permission/{areaId}")
    @RequireAreaPermission(paramIndex = 0, required = false)
    public ResponseDTO<Boolean> checkAreaPermission(@PathVariable Long areaId) {
        try {
            Long userId = LoginHelper.getLoginUserId();
            boolean hasPermission = areaPermissionService.hasAreaPermission(userId, areaId);
            return ResponseDTO.ok(hasPermission);
        } catch (Exception e) {
            log.error("检查区域权限失败: areaId={}", areaId, e);
            return ResponseDTO.error("检查区域权限失败");
        }
    }

    /**
     * 检查用户是否具有指定区域路径权限
     *
     * @param areaPath 区域路径
     * @return 是否有权限
     */
    @GetMapping("/check-path-permission")
    public ResponseDTO<Boolean> checkAreaPathPermission(@RequestParam String areaPath) {
        try {
            Long userId = LoginHelper.getLoginUserId();
            boolean hasPermission = areaPermissionService.hasAreaPathPermission(userId, areaPath);
            return ResponseDTO.ok(hasPermission);
        } catch (Exception e) {
            log.error("检查区域路径权限失败: areaPath={}", areaPath, e);
            return ResponseDTO.error("检查区域路径权限失败");
        }
    }

    /**
     * 批量授权用户区域权限
     *
     * @param areaIds 区域ID列表
     * @param dataScope 数据域
     * @param effectiveTime 生效时间
     * @param expireTime 失效时间
     * @return 授权结果
     */
    @PostMapping("/batch-grant")
    @RequireAreaPermission(mode = RequireAreaPermission.PermissionMode.GRANT, adminBypass = false)
    public ResponseDTO<Boolean> batchGrantAreaPermissions(@RequestBody List<Long> areaIds,
                                                          @RequestParam(defaultValue = "AREA") String dataScope,
                                                          @RequestParam(required = false) LocalDateTime effectiveTime,
                                                          @RequestParam(required = false) LocalDateTime expireTime) {
        try {
            Long userId = LoginHelper.getLoginUserId();
            boolean result = areaPermissionService.batchGrantAreaPermissions(
                userId, areaIds, dataScope, effectiveTime, expireTime);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("批量授权区域权限失败: areaIds={}", areaIds, e);
            return ResponseDTO.error("批量授权失败");
        }
    }

    /**
     * 批量撤销用户区域权限
     *
     * @param areaIds 区域ID列表
     * @return 撤销结果
     */
    @DeleteMapping("/batch-revoke")
    @RequireAreaPermission(mode = RequireAreaPermission.PermissionMode.REVOKE, adminBypass = false)
    public ResponseDTO<Boolean> batchRevokeAreaPermissions(@RequestBody List<Long> areaIds) {
        try {
            Long userId = LoginHelper.getLoginUserId();
            boolean result = areaPermissionService.batchRevokeAreaPermissions(userId, areaIds);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("批量撤销区域权限失败: areaIds={}", areaIds, e);
            return ResponseDTO.error("批量撤销失败");
        }
    }

    /**
     * 获取用户区域权限数量统计
     *
     * @return 权限数量
     */
    @GetMapping("/permission-count")
    public ResponseDTO<Integer> getUserPermissionCount() {
        try {
            Long userId = LoginHelper.getLoginUserId();
            int count = areaPermissionService.countUserAreaPermissions(userId);
            return ResponseDTO.ok(count);
        } catch (Exception e) {
            log.error("获取用户权限数量失败", e);
            return ResponseDTO.error("获取权限数量失败");
        }
    }

    /**
     * 获取即将过期的权限列表
     *
     * @param days 天数
     * @return 即将过期的权限列表
     */
    @GetMapping("/expiring-permissions")
    public ResponseDTO<List<Object>> getExpiringPermissions(@RequestParam(defaultValue = "7") Integer days) {
        try {
            // TODO: 实现权限列表DTO转换
            List<Object> permissions = areaPermissionService.getExpiringPermissions(days).stream()
                .map(Object.class::cast)
                .toList();
            return ResponseDTO.ok(permissions);
        } catch (Exception e) {
            log.error("获取即将过期的权限失败: days={}", days, e);
            return ResponseDTO.error("获取即将过期权限失败");
        }
    }

    /**
     * 清理过期权限
     *
     * @return 清理结果
     */
    @DeleteMapping("/clean-expired")
    @RequireAreaPermission(adminBypass = false)
    public ResponseDTO<Integer> cleanExpiredPermissions() {
        try {
            int count = areaPermissionService.cleanExpiredPermissions();
            return ResponseDTO.ok(count);
        } catch (Exception e) {
            log.error("清理过期权限失败", e);
            return ResponseDTO.error("清理过期权限失败");
        }
    }

    /**
     * 清除用户权限缓存
     *
     * @param userId 用户ID（可选，不提供则清除当前用户）
     * @return 操作结果
     */
    @PostMapping("/clear-cache")
    @RequireAreaPermission(adminBypass = false)
    public ResponseDTO<String> clearUserPermissionCache(@RequestParam(required = false) Long userId) {
        try {
            if (userId == null) {
                userId = LoginHelper.getLoginUserId();
            }
            areaPermissionService.clearUserPermissionCache(userId);
            return ResponseDTO.ok("缓存清除成功");
        } catch (Exception e) {
            log.error("清除用户权限缓存失败: userId={}", userId, e);
            return ResponseDTO.error("清除缓存失败");
        }
    }

    /**
     * 预热用户权限缓存
     *
     * @param userId 用户ID（可选，不提供则预热当前用户）
     * @return 操作结果
     */
    @PostMapping("/warmup-cache")
    @RequireAreaPermission(adminBypass = false)
    public ResponseDTO<String> warmupUserPermissionCache(@RequestParam(required = false) Long userId) {
        try {
            if (userId == null) {
                userId = LoginHelper.getLoginUserId();
            }
            areaPermissionService.warmupUserPermissionCache(userId);
            return ResponseDTO.ok("缓存预热成功");
        } catch (Exception e) {
            log.error("预热用户权限缓存失败: userId={}", userId, e);
            return ResponseDTO.error("预热缓存失败");
        }
    }
}