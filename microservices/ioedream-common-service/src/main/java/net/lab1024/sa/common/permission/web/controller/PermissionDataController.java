package net.lab1024.sa.common.permission.web.controller;

import lombok.extern.slf4j.Slf4j;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import net.lab1024.sa.common.permission.domain.vo.PermissionDataVO;
import net.lab1024.sa.common.permission.service.PermissionDataService;
import net.lab1024.sa.common.permission.domain.vo.MenuPermissionVO;
import net.lab1024.sa.common.permission.domain.vo.UserPermissionVO;
import net.lab1024.sa.common.permission.domain.vo.PermissionStatsVO;

import java.util.List;
import java.util.Set;

/**
 * 权限数据统一接口控制器
 * <p>
 * 提供前后端权限一致性保障的统一权限数据接口：
 * - 用户权限列表查询（支持webPerms格式）
 * - 菜单权限树结构查询
 * - 权限数据实时同步机制
 * - 权限变更通知接口
 * </p>
 * <p>
 * 关键特性：
 * 1. 统一权限数据格式，确保前后端一致性
 * 2. 支持增量权限数据更新，提升性能
 * 3. 完整的权限树结构，支持菜单和按钮级权限控制
 * 4. 实时权限变更通知，保障数据同步
 * 5. 权限数据版本控制，支持缓存优化
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/permission-data")
@Tag(name = "权限数据管理", description = "前后端权限数据统一接口")
@PermissionCheck(value = "SYSTEM_PERMISSION", description = "系统权限管理模块权限")
 public class PermissionDataController {


    @Resource
    private PermissionDataService permissionDataService;

    /**
     * 获取用户完整权限数据（包含webPerms格式）
     *
     * @param userId 用户ID（可选，为空则获取当前用户）
     * @return 用户权限数据
     */
    @Operation(summary = "获取用户权限数据", description = "获取用户完整权限信息，包含详细权限和菜单结构")
    @PermissionCheck(value = {"SYSTEM_PERMISSION_VIEW", "USER_PERMISSION_QUERY"}, description = "系统权限查看或用户权限查询权限")
    public ResponseDTO<UserPermissionVO> getUserPermissions(
            @Parameter(description = "用户ID，为空则获取当前用户")
            Long userId) {

        log.info("[权限数据] 获取用户权限数据: userId={}", userId);

        try {
            UserPermissionVO userPermissionVO = permissionDataService.getUserPermissions(userId);
            return ResponseDTO.ok(userPermissionVO);

        } catch (Exception e) {
            log.error("[权限数据] 获取用户权限数据失败: userId={}", userId, e);
            return ResponseDTO.error("GET_PERMISSIONS_ERROR", "获取权限数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取菜单权限树结构
     *
     * @param userId 用户ID（可选，为空则获取当前用户）
     * @return 菜单权限树
     */
    @Operation(summary = "获取菜单权限树", description = "获取用户可访问的菜单树结构，包含详细权限信息")
    @PermissionCheck(value = {"SYSTEM_PERMISSION_VIEW", "MENU_PERMISSION_QUERY"}, description = "系统权限查看或菜单权限查询权限")
    public ResponseDTO<List<MenuPermissionVO>> getMenuPermissions(
            @Parameter(description = "用户ID，为空则获取当前用户")
            Long userId) {

        log.info("[权限数据] 获取菜单权限树: userId={}", userId);

        try {
            List<MenuPermissionVO> menuPermissions = permissionDataService.getMenuPermissions(userId);
            return ResponseDTO.ok(menuPermissions);

        } catch (Exception e) {
            log.error("[权限数据] 获取菜单权限树失败: userId={}", userId, e);
            return ResponseDTO.error("GET_MENU_PERMISSIONS_ERROR", "获取菜单权限失败: " + e.getMessage());
        }
    }

    /**
     * 批量获取用户权限数据
     *
     * @param userIds 用户ID列表
     * @return 用户权限数据列表
     */
    @Operation(summary = "批量获取用户权限", description = "批量获取多个用户的完整权限数据")
    @PermissionCheck(value = {"SYSTEM_PERMISSION_BATCH"}, description = "系统权限批量操作权限")
    public ResponseDTO<List<UserPermissionVO>> getBatchUserPermissions(
            @Parameter(description = "用户ID列表")
            List<Long> userIds) {

        log.info("[权限数据] 批量获取用户权限数据: userIdCount={}", userIds != null ? userIds.size() : 0);

        try {
            List<UserPermissionVO> userPermissions = permissionDataService.getBatchUserPermissions(userIds);
            return ResponseDTO.ok(userPermissions);

        } catch (Exception e) {
            log.error("[权限数据] 批量获取用户权限数据失败", e);
            return ResponseDTO.error("BATCH_GET_PERMISSIONS_ERROR", "批量获取权限失败: " + e.getMessage());
        }
    }

    /**
     * 获取权限数据变更通知
     *
     * @param lastSyncTime 最后同步时间（毫秒）
     * @return 权限变更列表
     */
    @Operation(summary = "获取权限变更通知", description = "获取指定时间后的权限变更通知")
    @PermissionCheck(value = {"SYSTEM_PERMISSION_SYNC"}, description = "系统权限同步权限")
    public ResponseDTO<List<PermissionDataVO>> getPermissionChanges(
            @Parameter(description = "最后同步时间戳（毫秒）")
            Long lastSyncTime) {

        log.info("[权限数据] 获取权限变更通知: lastSyncTime={}", lastSyncTime);

        try {
            List<PermissionDataVO> permissionChanges = permissionDataService.getPermissionChanges(lastSyncTime);
            return ResponseDTO.ok(permissionChanges);

        } catch (Exception e) {
            log.error("[权限数据] 获取权限变更通知失败: lastSyncTime={}", lastSyncTime, e);
            return ResponseDTO.error("GET_PERMISSION_CHANGES_ERROR", "获取权限变更失败: " + e.getMessage());
        }
    }

    /**
     * 权限数据同步确认
     *
     * @param userId 用户ID
     * @param dataVersion 数据版本号
     * @param syncType 同步类型（FULL/INCREMENTAL）
     * @return 同步结果
     */
    @Operation(summary = "权限数据同步确认", description = "确认权限数据同步完成")
    @PermissionCheck(value = {"SYSTEM_PERMISSION_SYNC"}, description = "系统权限同步权限")
    public ResponseDTO<Void> confirmPermissionSync(
            @Parameter(description = "用户ID")
            Long userId,
            @Parameter(description = "数据版本号")
            String dataVersion,
            @Parameter(description = "同步类型：FULL/INCREMENTAL")
            String syncType) {

        log.info("[权限数据] 权限数据同步确认: userId={}, dataVersion={}, syncType={}",
                 userId, dataVersion, syncType);

        try {
            permissionDataService.confirmPermissionSync(userId, dataVersion, syncType);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[权限数据] 权限数据同步确认失败: userId={}", userId, e);
            return ResponseDTO.error("CONFIRM_SYNC_ERROR", "确认同步失败: " + e.getMessage());
        }
    }

    /**
     * 清除用户权限缓存
     *
     * @param userId 用户ID
     * @return 清除结果
     */
    @Operation(summary = "清除用户权限缓存", description = "清除指定用户的权限缓存数据")
    @PermissionCheck(value = {"SYSTEM_PERMISSION_MANAGE"}, description = "系统权限管理权限")
    public ResponseDTO<Void> clearUserPermissionCache(
            @Parameter(description = "用户ID")
            Long userId) {

        log.info("[权限数据] 清除用户权限缓存: userId={}", userId);

        try {
            permissionDataService.clearUserPermissionCache(userId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[权限数据] 清除用户权限缓存失败: userId={}", userId, e);
            return ResponseDTO.error("CLEAR_CACHE_ERROR", "清除缓存失败: " + e.getMessage());
        }
    }

    /**
     * 批量清除权限缓存
     *
     * @param userIds 用户ID列表
     * @return 清除结果
     */
    @Operation(summary = "批量清除权限缓存", description = "批量清除多个用户的权限缓存")
    @PermissionCheck(value = {"SYSTEM_PERMISSION_MANAGE"}, description = "系统权限管理权限")
    public ResponseDTO<Void> clearBatchPermissionCache(
            @Parameter(description = "用户ID列表")
            List<Long> userIds) {

        log.info("[权限数据] 批量清除权限缓存: userIdCount={}", userIds != null ? userIds.size() : 0);

        try {
            permissionDataService.clearBatchPermissionCache(userIds);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[权限数据] 批量清除权限缓存失败", e);
            return ResponseDTO.error("BATCH_CLEAR_CACHE_ERROR", "批量清除缓存失败: " + e.getMessage());
        }
    }

    /**
     * 获取权限统计数据
     *
     * @return 权限统计信息
     */
    @Operation(summary = "获取权限统计", description = "获取权限验证和使用统计数据")
    @PermissionCheck(value = {"SYSTEM_PERMISSION_STATS"}, description = "系统权限统计权限")
    public ResponseDTO<PermissionStatsVO> getPermissionStats() {

        log.info("[权限数据] 获取权限统计数据");

        try {
            PermissionStatsVO stats = permissionDataService.getPermissionStats();
            return ResponseDTO.ok(stats);

        } catch (Exception e) {
            log.error("[权限数据] 获取权限统计数据失败", e);
            return ResponseDTO.error("GET_STATS_ERROR", "获取统计数据失败: " + e.getMessage());
        }
    }
}

