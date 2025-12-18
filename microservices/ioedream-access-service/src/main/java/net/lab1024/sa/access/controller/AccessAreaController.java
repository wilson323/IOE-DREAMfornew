package net.lab1024.sa.access.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.form.AccessAreaQueryForm;
import net.lab1024.sa.access.domain.vo.AccessAreaMonitorVO;
import net.lab1024.sa.access.domain.vo.AccessAreaOverviewVO;
import net.lab1024.sa.access.domain.vo.AccessAreaPersonVO;
import net.lab1024.sa.access.domain.vo.AccessAreaPermissionMatrixVO;
import net.lab1024.sa.access.service.AccessAreaService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.response.ResponseDTO;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 门禁区域空间管理控制器
 * <p>
 * 严格遵循CLAUDE.md规范和ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md架构方案：
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 遵循四层架构：Controller → Service → Manager → DAO
 * - 区域信息管理、门管理、人员管理、权限管理、区域监控
 * </p>
 * <p>
 * 核心职责：
 * - 区域信息查询和概览统计
 * - 区域内人员权限管理
 * - 区域权限自动分配
 * - 区域通行监控
 * </p>
 * <p>
 * 注意：区域基本信息由公共模块统一维护，本控制器主要负责门禁相关的区域管理功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/access/area")
@Tag(name = "门禁区域空间管理", description = "门禁区域空间管理接口，包括区域信息管理、人员管理、权限管理、区域监控")
public class AccessAreaController {

    @Resource
    private AccessAreaService accessAreaService;

    /**
     * 查询区域列表
     * <p>
     * 查询门禁相关的区域列表，支持分页和多条件查询
     * </p>
     *
     * @param queryForm 查询表单
     * @return 区域列表
     */
    @PostMapping("/query")
    @Operation(summary = "查询区域列表", description = "查询门禁相关的区域列表，支持分页和多条件查询")
    public ResponseDTO<PageResult<AccessAreaOverviewVO>> queryAreaList(
            @Valid @RequestBody AccessAreaQueryForm queryForm) {
        log.info("[区域管理] 查询区域列表: pageNum={}, pageSize={}", 
                queryForm.getPageNum(), queryForm.getPageSize());
        
        try {
            return accessAreaService.queryAreaList(queryForm);
        } catch (Exception e) {
            log.error("[区域管理] 查询区域列表异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("QUERY_AREA_LIST_ERROR", "查询区域列表失败: " + e.getMessage());
        }
    }

    /**
     * 查询区域概览
     * <p>
     * 查询指定区域的详细概览信息，包括设备数、人员数、通行统计等
     * </p>
     *
     * @param areaId 区域ID
     * @return 区域概览信息
     */
    @GetMapping("/{areaId}/overview")
    @Operation(summary = "查询区域概览", description = "查询指定区域的详细概览信息，包括设备数、人员数、通行统计等")
    public ResponseDTO<AccessAreaOverviewVO> getAreaOverview(
            @Parameter(description = "区域ID", required = true)
            @PathVariable Long areaId) {
        log.info("[区域管理] 查询区域概览: areaId={}", areaId);
        
        try {
            return accessAreaService.getAreaOverview(areaId);
        } catch (Exception e) {
            log.error("[区域管理] 查询区域概览异常: areaId={}, error={}", areaId, e.getMessage(), e);
            return ResponseDTO.error("GET_AREA_OVERVIEW_ERROR", "查询区域概览失败: " + e.getMessage());
        }
    }

    /**
     * 查询区域内人员列表
     * <p>
     * 查询指定区域内有权限的人员列表，包括权限信息
     * </p>
     *
     * @param areaId 区域ID
     * @param queryForm 查询表单（用于分页）
     * @return 人员列表
     */
    @PostMapping("/{areaId}/persons/query")
    @Operation(summary = "查询区域内人员列表", description = "查询指定区域内有权限的人员列表，包括权限信息")
    public ResponseDTO<PageResult<AccessAreaPersonVO>> queryAreaPersons(
            @Parameter(description = "区域ID", required = true)
            @PathVariable Long areaId,
            @Valid @RequestBody AccessAreaQueryForm queryForm) {
        log.info("[区域管理] 查询区域内人员列表: areaId={}, pageNum={}, pageSize={}", 
                areaId, queryForm.getPageNum(), queryForm.getPageSize());
        
        try {
            return accessAreaService.queryAreaPersons(areaId, queryForm);
        } catch (Exception e) {
            log.error("[区域管理] 查询区域内人员列表异常: areaId={}, error={}", areaId, e.getMessage(), e);
            return ResponseDTO.error("QUERY_AREA_PERSONS_ERROR", "查询区域内人员列表失败: " + e.getMessage());
        }
    }

    /**
     * 分配人员到区域
     * <p>
     * 将人员分配到指定区域，并自动分配区域内所有设备的通行权限
     * </p>
     *
     * @param areaId 区域ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("/{areaId}/persons/{userId}")
    @Operation(summary = "分配人员到区域", description = "将人员分配到指定区域，并自动分配区域内所有设备的通行权限")
    public ResponseDTO<Void> assignPersonToArea(
            @Parameter(description = "区域ID", required = true)
            @PathVariable Long areaId,
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        log.info("[区域管理] 分配人员到区域: areaId={}, userId={}", areaId, userId);
        
        try {
            return accessAreaService.assignPersonToArea(areaId, userId);
        } catch (Exception e) {
            log.error("[区域管理] 分配人员到区域异常: areaId={}, userId={}, error={}", 
                    areaId, userId, e.getMessage(), e);
            return ResponseDTO.error("ASSIGN_PERSON_TO_AREA_ERROR", "分配人员到区域失败: " + e.getMessage());
        }
    }

    /**
     * 移除区域人员
     * <p>
     * 从区域中移除人员，并回收该人员在该区域的所有设备权限
     * </p>
     *
     * @param areaId 区域ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{areaId}/persons/{userId}")
    @Operation(summary = "移除区域人员", description = "从区域中移除人员，并回收该人员在该区域的所有设备权限")
    public ResponseDTO<Void> removePersonFromArea(
            @Parameter(description = "区域ID", required = true)
            @PathVariable Long areaId,
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        log.info("[区域管理] 移除区域人员: areaId={}, userId={}", areaId, userId);
        
        try {
            return accessAreaService.removePersonFromArea(areaId, userId);
        } catch (Exception e) {
            log.error("[区域管理] 移除区域人员异常: areaId={}, userId={}, error={}", 
                    areaId, userId, e.getMessage(), e);
            return ResponseDTO.error("REMOVE_PERSON_FROM_AREA_ERROR", "移除区域人员失败: " + e.getMessage());
        }
    }

    /**
     * 查询权限矩阵
     * <p>
     * 查询指定区域的人员-设备权限关系矩阵
     * </p>
     *
     * @param areaId 区域ID
     * @return 权限矩阵
     */
    @GetMapping("/{areaId}/permission-matrix")
    @Operation(summary = "查询权限矩阵", description = "查询指定区域的人员-设备权限关系矩阵")
    public ResponseDTO<AccessAreaPermissionMatrixVO> queryPermissionMatrix(
            @Parameter(description = "区域ID", required = true)
            @PathVariable Long areaId) {
        log.info("[区域管理] 查询权限矩阵: areaId={}", areaId);
        
        try {
            return accessAreaService.queryPermissionMatrix(areaId);
        } catch (Exception e) {
            log.error("[区域管理] 查询权限矩阵异常: areaId={}, error={}", areaId, e.getMessage(), e);
            return ResponseDTO.error("QUERY_PERMISSION_MATRIX_ERROR", "查询权限矩阵失败: " + e.getMessage());
        }
    }

    /**
     * 批量分配权限
     * <p>
     * 批量分配人员对设备的通行权限
     * </p>
     *
     * @param areaId 区域ID
     * @param userIds 用户ID列表
     * @param deviceIds 设备ID列表（可选，为空表示区域内所有设备）
     * @return 操作结果
     */
    @PostMapping("/{areaId}/permissions/batch-assign")
    @Operation(summary = "批量分配权限", description = "批量分配人员对设备的通行权限")
    public ResponseDTO<Void> batchAssignPermissions(
            @Parameter(description = "区域ID", required = true)
            @PathVariable Long areaId,
            @Parameter(description = "用户ID列表", required = true)
            @RequestParam List<Long> userIds,
            @Parameter(description = "设备ID列表（可选，为空表示区域内所有设备）")
            @RequestParam(required = false) List<String> deviceIds) {
        log.info("[区域管理] 批量分配权限: areaId={}, userIdCount={}, deviceIdCount={}", 
                areaId, userIds != null ? userIds.size() : 0, deviceIds != null ? deviceIds.size() : 0);
        
        try {
            return accessAreaService.batchAssignPermissions(areaId, userIds, deviceIds);
        } catch (Exception e) {
            log.error("[区域管理] 批量分配权限异常: areaId={}, error={}", areaId, e.getMessage(), e);
            return ResponseDTO.error("BATCH_ASSIGN_PERMISSIONS_ERROR", "批量分配权限失败: " + e.getMessage());
        }
    }

    /**
     * 批量回收权限
     * <p>
     * 批量回收人员对设备的通行权限
     * </p>
     *
     * @param areaId 区域ID
     * @param userIds 用户ID列表（可选，为空表示区域内所有人员）
     * @param deviceIds 设备ID列表（可选，为空表示区域内所有设备）
     * @return 操作结果
     */
    @PostMapping("/{areaId}/permissions/batch-revoke")
    @Operation(summary = "批量回收权限", description = "批量回收人员对设备的通行权限")
    public ResponseDTO<Void> batchRevokePermissions(
            @Parameter(description = "区域ID", required = true)
            @PathVariable Long areaId,
            @Parameter(description = "用户ID列表（可选，为空表示区域内所有人员）")
            @RequestParam(required = false) List<Long> userIds,
            @Parameter(description = "设备ID列表（可选，为空表示区域内所有设备）")
            @RequestParam(required = false) List<String> deviceIds) {
        log.info("[区域管理] 批量回收权限: areaId={}, userIdCount={}, deviceIdCount={}", 
                areaId, userIds != null ? userIds.size() : 0, deviceIds != null ? deviceIds.size() : 0);
        
        try {
            return accessAreaService.batchRevokePermissions(areaId, userIds, deviceIds);
        } catch (Exception e) {
            log.error("[区域管理] 批量回收权限异常: areaId={}, error={}", areaId, e.getMessage(), e);
            return ResponseDTO.error("BATCH_REVOKE_PERMISSIONS_ERROR", "批量回收权限失败: " + e.getMessage());
        }
    }

    /**
     * 查询区域监控数据
     * <p>
     * 查询指定区域的监控数据，包括设备状态、人员通行、容量等
     * </p>
     *
     * @param areaId 区域ID
     * @return 区域监控数据
     */
    @GetMapping("/{areaId}/monitor")
    @Operation(summary = "查询区域监控数据", description = "查询指定区域的监控数据，包括设备状态、人员通行、容量等")
    public ResponseDTO<AccessAreaMonitorVO> getAreaMonitorData(
            @Parameter(description = "区域ID", required = true)
            @PathVariable Long areaId) {
        log.info("[区域管理] 查询区域监控数据: areaId={}", areaId);
        
        try {
            return accessAreaService.getAreaMonitorData(areaId);
        } catch (Exception e) {
            log.error("[区域管理] 查询区域监控数据异常: areaId={}, error={}", areaId, e.getMessage(), e);
            return ResponseDTO.error("GET_AREA_MONITOR_DATA_ERROR", "查询区域监控数据失败: " + e.getMessage());
        }
    }
}
