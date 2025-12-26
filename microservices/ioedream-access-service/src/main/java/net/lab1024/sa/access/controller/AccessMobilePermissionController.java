package net.lab1024.sa.access.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.form.PermissionQueryForm;
import net.lab1024.sa.access.domain.form.PermissionRenewForm;
import net.lab1024.sa.access.domain.vo.AccessPermissionStatisticsVO;
import net.lab1024.sa.access.domain.vo.AccessPermissionVO;
import net.lab1024.sa.common.entity.access.AccessUserPermissionEntity;
import net.lab1024.sa.access.service.AccessUserPermissionService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 移动端门禁权限管理控制器
 *
 * @author IOE-DREAM Team
 * @since 2025-12-24
 */
@Slf4j
@RestController
@Tag(name = "移动端门禁权限管理")
@RequestMapping("/api/v1/mobile/access/permission")
public class AccessMobilePermissionController {

    @Resource
    private AccessUserPermissionService accessUserPermissionService;

    @GetMapping("/list")
    @Operation(summary = "获取用户权限列表", description = "获取当前登录用户的权限列表，支持按状态、类型、区域过滤")
    public ResponseDTO<PageResult<AccessPermissionVO>> getUserPermissions(PermissionQueryForm queryForm) {
        log.info("[移动端权限管理] 查询用户权限列表: {}", queryForm);

        try {
            // TODO: 从SecurityContext获取当前登录用户ID
            Long userId = 1L; // 临时硬编码，实际应从Token获取

            // 构建分页参数
            Page<AccessUserPermissionEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());

            // 查询权限列表
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<AccessPermissionVO> pageResult =
                accessUserPermissionService.getUserPermissionPage(userId, queryForm, page);

            // 转换为 PageResult
            PageResult<AccessPermissionVO> result = PageResult.of(
                pageResult.getRecords(),
                pageResult.getTotal(),
                (int) pageResult.getCurrent(),
                (int) pageResult.getSize()
            );

            log.info("[移动端权限管理] 查询成功，共{}条", result.getTotal());
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[移动端权限管理] 查询失败", e);
            return ResponseDTO.error("500", "查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取权限统计", description = "获取用户权限的统计数据")
    public ResponseDTO<AccessPermissionStatisticsVO> getUserPermissionStatistics() {
        log.info("[移动端权限管理] 查询用户权限统计");

        try {
            // TODO: 从SecurityContext获取当前登录用户ID
            Long userId = 1L;

            AccessPermissionStatisticsVO statistics = accessUserPermissionService.getUserPermissionStatistics(userId);

            log.info("[移动端权限管理] 统计查询成功: {}", statistics);
            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("[移动端权限管理] 统计查询失败", e);
            return ResponseDTO.error("500", "查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{permissionId}")
    @Operation(summary = "获取权限详情", description = "根据权限ID获取详细信息")
    public ResponseDTO<AccessPermissionVO> getPermissionDetail(@PathVariable Long permissionId) {
        log.info("[移动端权限管理] 查询权限详情: permissionId={}", permissionId);

        try {
            AccessPermissionVO detail = accessUserPermissionService.getPermissionDetail(permissionId);

            if (detail == null) {
                return ResponseDTO.error("404", "权限不存在");
            }

            return ResponseDTO.ok(detail);
        } catch (Exception e) {
            log.error("[移动端权限管理] 查询详情失败", e);
            return ResponseDTO.error("500", "查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{permissionId}/qrcode")
    @Operation(summary = "获取权限二维码", description = "生成权限的通行二维码")
    public ResponseDTO<Map<String, String>> getPermissionQRCode(@PathVariable Long permissionId) {
        log.info("[移动端权限管理] 生成权限二维码: permissionId={}", permissionId);

        try {
            // TODO: 调用二维码生成服务
            String qrCode = accessUserPermissionService.generatePermissionQRCode(permissionId);

            Map<String, String> result = new HashMap<>();
            result.put("qrCode", qrCode);
            result.put("permissionId", permissionId.toString());
            result.put("expireTime", "60"); // 二维码有效期60秒

            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[移动端权限管理] 生成二维码失败", e);
            return ResponseDTO.error("500", "生成失败: " + e.getMessage());
        }
    }

    @GetMapping("/{permissionId}/records")
    @Operation(summary = "获取通行记录", description = "获取该权限的通行记录列表")
    public ResponseDTO<List<Map<String, Object>>> getPermissionRecords(
            @PathVariable Long permissionId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("[移动端权限管理] 查询通行记录: permissionId={}, pageNum={}, pageSize={}",
                permissionId, pageNum, pageSize);

        try {
            // TODO: 查询通行记录
            List<Map<String, Object>> records = accessUserPermissionService.getPermissionRecords(
                    permissionId, pageNum, pageSize);

            return ResponseDTO.ok(records);
        } catch (Exception e) {
            log.error("[移动端权限管理] 查询记录失败", e);
            return ResponseDTO.error("500", "查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{permissionId}/history")
    @Operation(summary = "获取权限历史", description = "获取权限的变更历史记录")
    public ResponseDTO<List<Map<String, Object>>> getPermissionHistory(@PathVariable Long permissionId) {
        log.info("[移动端权限管理] 查询权限历史: permissionId={}", permissionId);

        try {
            // TODO: 查询权限变更历史
            List<Map<String, Object>> history = accessUserPermissionService.getPermissionHistory(permissionId);

            return ResponseDTO.ok(history);
        } catch (Exception e) {
            log.error("[移动端权限管理] 查询历史失败", e);
            return ResponseDTO.error("500", "查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/{permissionId}/renew")
    @Operation(summary = "续期权限", description = "延长权限的有效期")
    public ResponseDTO<Void> renewPermission(
            @PathVariable Long permissionId,
            @RequestBody PermissionRenewForm renewForm) {
        log.info("[移动端权限管理] 续期权限: permissionId={}, duration={}天",
                permissionId, renewForm.getDuration());

        try {
            accessUserPermissionService.renewPermission(permissionId, renewForm);

            log.info("[移动端权限管理] 续期成功");
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("[移动端权限管理] 续期失败", e);
            return ResponseDTO.error("500", "续期失败: " + e.getMessage());
        }
    }

    @PostMapping("/{permissionId}/transfer")
    @Operation(summary = "转移权限", description = "将权限转移给另一个用户")
    public ResponseDTO<Void> transferPermission(
            @PathVariable Long permissionId,
            @RequestBody Map<String, Long> params) {
        log.info("[移动端权限管理] 转移权限: permissionId={}, targetUserId={}",
                permissionId, params.get("targetUserId"));

        try {
            Long targetUserId = params.get("targetUserId");
            if (targetUserId == null) {
                return ResponseDTO.error("400", "目标用户ID不能为空");
            }

            accessUserPermissionService.transferPermission(permissionId, targetUserId);

            log.info("[移动端权限管理] 转移成功");
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("[移动端权限管理] 转移失败", e);
            return ResponseDTO.error("500", "转移失败: " + e.getMessage());
        }
    }

    @PostMapping("/{permissionId}/freeze")
    @Operation(summary = "冻结权限", description = "暂时冻结权限，禁止使用")
    public ResponseDTO<Void> freezePermission(@PathVariable Long permissionId) {
        log.info("[移动端权限管理] 冻结权限: permissionId={}", permissionId);

        try {
            accessUserPermissionService.freezePermission(permissionId);

            log.info("[移动端权限管理] 冻结成功");
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("[移动端权限管理] 冻结失败", e);
            return ResponseDTO.error("500", "冻结失败: " + e.getMessage());
        }
    }

    @PostMapping("/{permissionId}/unfreeze")
    @Operation(summary = "解冻权限", description = "解除权限的冻结状态")
    public ResponseDTO<Void> unfreezePermission(@PathVariable Long permissionId) {
        log.info("[移动端权限管理] 解冻权限: permissionId={}", permissionId);

        try {
            accessUserPermissionService.unfreezePermission(permissionId);

            log.info("[移动端权限管理] 解冻成功");
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("[移动端权限管理] 解冻失败", e);
            return ResponseDTO.error("500", "解冻失败: " + e.getMessage());
        }
    }

    @GetMapping("/expiring/statistics")
    @Operation(summary = "获取过期统计", description = "获取即将过期权限的统计数据")
    public ResponseDTO<Map<String, Object>> getExpiringStatistics() {
        log.info("[移动端权限管理] 查询过期统计");

        try {
            // TODO: 从SecurityContext获取当前登录用户ID
            Long userId = 1L;

            Map<String, Object> statistics = accessUserPermissionService.getExpiringStatistics(userId);

            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("[移动端权限管理] 统计查询失败", e);
            return ResponseDTO.error("500", "查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/expiring/list")
    @Operation(summary = "获取即将过期列表", description = "获取即将过期的权限列表")
    public ResponseDTO<List<AccessPermissionVO>> getExpiringPermissions(
            @RequestParam(defaultValue = "30") Integer days) {
        log.info("[移动端权限管理] 查询即将过期列表: days={}", days);

        try {
            // TODO: 从SecurityContext获取当前登录用户ID
            Long userId = 1L;

            List<AccessPermissionVO> list = accessUserPermissionService.getExpiringPermissions(userId, days);

            return ResponseDTO.ok(list);
        } catch (Exception e) {
            log.error("[移动端权限管理] 查询失败", e);
            return ResponseDTO.error("500", "查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/batch-renew")
    @Operation(summary = "批量续期", description = "批量续期多个权限")
    public ResponseDTO<Map<String, Object>> batchRenewPermissions(@RequestBody Map<String, Object> params) {
        log.info("[移动端权限管理] 批量续期: {}", params);

        try {
            @SuppressWarnings("unchecked")
            List<Long> permissionIds = (List<Long>) params.get("permissionIds");
            Integer duration = (Integer) params.get("duration");

            if (permissionIds == null || permissionIds.isEmpty()) {
                return ResponseDTO.error("400", "权限ID列表不能为空");
            }

            Map<String, Object> result = accessUserPermissionService.batchRenewPermissions(permissionIds, duration);

            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[移动端权限管理] 批量续期失败", e);
            return ResponseDTO.error("500", "批量续期失败: " + e.getMessage());
        }
    }
}
