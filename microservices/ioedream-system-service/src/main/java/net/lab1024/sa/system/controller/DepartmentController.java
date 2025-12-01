package net.lab1024.sa.system.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.annotation.SaCheckLogin;
import net.lab1024.sa.common.annotation.SaCheckPermission;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.util.SmartResponseUtil;
import net.lab1024.sa.system.domain.form.DepartmentAddForm;
import net.lab1024.sa.system.domain.form.DepartmentQueryForm;
import net.lab1024.sa.system.domain.form.DepartmentUpdateForm;
import net.lab1024.sa.system.domain.vo.DepartmentVO;
import net.lab1024.sa.system.service.DepartmentService;

/**
 * 部门管理控制器
 * <p>
 * 严格遵循repowiki Controller规范：
 * - 使用jakarta包名
 * - 使用@Resource依赖注入
 * - 完整的权限控制
 * - 统一的响应格式
 * - 完整的Swagger文档
 *
 * @author SmartAdmin Team
 * @since 2025-11-29
 */
@Slf4j
@RestController
@Tag(name = "部门管理", description = "部门管理相关接口")
@RequestMapping("/api/department")
@SaCheckLogin
public class DepartmentController {

    @Resource
    private DepartmentService departmentService;

    // ========== 查询接口 ==========

    @Operation(summary = "分页查询部门", description = "分页查询部门列表")
    @SaCheckPermission("department:page:query")
    @PostMapping("/page")
    public ResponseDTO<?> queryDepartmentPage(
            @Parameter(description = "查询条件") @Valid @RequestBody DepartmentQueryForm queryForm) {
        try {
            log.info("分页查询部门，查询条件：{}", queryForm);
            var result = departmentService.queryDepartmentPage(queryForm);
            log.info("部门分页查询成功，总数：{}", result.getTotal());
            return SmartResponseUtil.success(result);
        } catch (Exception e) {
            log.error("部门分页查询失败", e);
            return ResponseDTO.error("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "查询部门列表", description = "查询部门列表（不分页）")
    @SaCheckPermission("department:list:query")
    @PostMapping("/list")
    public ResponseDTO<List<DepartmentVO>> queryDepartmentList(
            @Parameter(description = "查询条件") @RequestBody DepartmentQueryForm queryForm) {
        try {
            log.info("查询部门列表，查询条件：{}", queryForm);
            List<DepartmentVO> result = departmentService.queryDepartmentList(queryForm);
            log.info("部门列表查询成功，数量：{}", result.size());
            return SmartResponseUtil.success(result);
        } catch (Exception e) {
            log.error("部门列表查询失败", e);
            return ResponseDTO.error("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取部门树", description = "获取部门树形结构")
    @SaCheckPermission("department:tree:query")
    @GetMapping("/tree")
    public ResponseDTO<List<DepartmentVO>> getDepartmentTree(
            @Parameter(description = "是否只查询启用的部门") @RequestParam(required = false, defaultValue = "true") Boolean onlyEnabled) {
        try {
            log.info("获取部门树，onlyEnabled：{}", onlyEnabled);
            List<DepartmentVO> result = departmentService.getDepartmentTree(onlyEnabled);
            log.info("部门树获取成功，根节点数量：{}", result.size());
            return SmartResponseUtil.success(result);
        } catch (Exception e) {
            log.error("获取部门树失败", e);
            return ResponseDTO.error("获取失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取部门详情", description = "根据部门ID获取部门详情")
    @SaCheckPermission("department:detail:query")
    @GetMapping("/{departmentId}")
    public ResponseDTO<DepartmentVO> getDepartmentById(
            @Parameter(description = "部门ID") @PathVariable Long departmentId) {
        try {
            log.info("获取部门详情，departmentId：{}", departmentId);
            ResponseDTO<DepartmentVO> result = departmentService.getDepartmentById(departmentId);
            if (result.getOk()) {
                log.info("部门详情获取成功，departmentId：{}", departmentId);
            } else {
                log.warn("部门详情获取失败，departmentId：{}，错误：{}", departmentId, result.getMsg());
            }
            return result;
        } catch (Exception e) {
            log.error("获取部门详情失败，departmentId：{}", departmentId, e);
            return ResponseDTO.error("获取失败：" + e.getMessage());
        }
    }

    @Operation(summary = "根据编码获取部门", description = "根据部门编码获取部门详情")
    @SaCheckPermission("department:code:query")
    @GetMapping("/code/{departmentCode}")
    public ResponseDTO<DepartmentVO> getDepartmentByCode(
            @Parameter(description = "部门编码") @PathVariable String departmentCode) {
        try {
            log.info("根据编码获取部门，departmentCode：{}", departmentCode);
            ResponseDTO<DepartmentVO> result = departmentService.getDepartmentByCode(departmentCode);
            if (result.getOk()) {
                log.info("根据编码获取部门成功，departmentCode：{}", departmentCode);
            } else {
                log.warn("根据编码获取部门失败，departmentCode：{}，错误：{}", departmentCode, result.getMsg());
            }
            return result;
        } catch (Exception e) {
            log.error("根据编码获取部门失败，departmentCode：{}", departmentCode, e);
            return ResponseDTO.error("获取失败：" + e.getMessage());
        }
    }

    // ========== 新增接口 ==========

    @Operation(summary = "新增部门", description = "新增部门")
    @SaCheckPermission("department:add")
    @PostMapping("/add")
    public ResponseDTO<Long> addDepartment(
            @Parameter(description = "新增表单") @Valid @RequestBody DepartmentAddForm addForm,
            @Parameter(description = "操作人ID") @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            log.info("新增部门，form：{}，userId：{}", addForm, userId);
            ResponseDTO<Long> result = departmentService.addDepartment(addForm, userId != null ? userId : 1L);
            if (result.getOk()) {
                log.info("新增部门成功，departmentId：{}", result.getData());
            } else {
                log.warn("新增部门失败，错误：{}", result.getMsg());
            }
            return result;
        } catch (Exception e) {
            log.error("新增部门失败", e);
            return ResponseDTO.error("新增失败：" + e.getMessage());
        }
    }

    // ========== 更新接口 ==========

    @Operation(summary = "更新部门", description = "更新部门信息")
    @SaCheckPermission("department:update")
    @PostMapping("/update")
    public ResponseDTO<String> updateDepartment(
            @Parameter(description = "更新表单") @Valid @RequestBody DepartmentUpdateForm updateForm,
            @Parameter(description = "操作人ID") @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            log.info("更新部门，form：{}，userId：{}", updateForm, userId);
            ResponseDTO<String> result = departmentService.updateDepartment(updateForm, userId != null ? userId : 1L);
            if (result.getOk()) {
                log.info("更新部门成功，departmentId：{}", updateForm.getDepartmentId());
            } else {
                log.warn("更新部门失败，错误：{}", result.getMsg());
            }
            return result;
        } catch (Exception e) {
            log.error("更新部门失败", e);
            return ResponseDTO.error("更新失败：" + e.getMessage());
        }
    }

    // ========== 删除接口 ==========

    @Operation(summary = "删除部门", description = "删除部门（逻辑删除）")
    @SaCheckPermission("department:delete")
    @DeleteMapping("/{departmentId}")
    public ResponseDTO<String> deleteDepartment(
            @Parameter(description = "部门ID") @PathVariable Long departmentId,
            @Parameter(description = "操作人ID") @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            log.info("删除部门，departmentId：{}，userId：{}", departmentId, userId);
            ResponseDTO<String> result = departmentService.deleteDepartment(departmentId, userId != null ? userId : 1L);
            if (result.getOk()) {
                log.info("删除部门成功，departmentId：{}", departmentId);
            } else {
                log.warn("删除部门失败，错误：{}", result.getMsg());
            }
            return result;
        } catch (Exception e) {
            log.error("删除部门失败", e);
            return ResponseDTO.error("删除失败：" + e.getMessage());
        }
    }

    @Operation(summary = "批量删除部门", description = "批量删除部门")
    @SaCheckPermission("department:batch:delete")
    @DeleteMapping("/batch")
    public ResponseDTO<String> batchDeleteDepartment(
            @Parameter(description = "部门ID列表") @RequestParam List<Long> departmentIds,
            @Parameter(description = "操作人ID") @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            log.info("批量删除部门，departmentIds：{}，userId：{}", departmentIds, userId);
            ResponseDTO<String> result = departmentService.batchDeleteDepartment(departmentIds,
                    userId != null ? userId : 1L);
            if (result.getOk()) {
                log.info("批量删除部门成功，数量：{}", departmentIds.size());
            } else {
                log.warn("批量删除部门失败，错误：{}", result.getMsg());
            }
            return result;
        } catch (Exception e) {
            log.error("批量删除部门失败", e);
            return ResponseDTO.error("批量删除失败：" + e.getMessage());
        }
    }

    // ========== 状态管理接口 ==========

    @Operation(summary = "修改部门状态", description = "启用或禁用部门")
    @SaCheckPermission("department:status:update")
    @PostMapping("/status/{departmentId}")
    public ResponseDTO<String> changeDepartmentStatus(
            @Parameter(description = "部门ID") @PathVariable Long departmentId,
            @Parameter(description = "状态（1-启用，0-禁用）") @RequestParam Integer status,
            @Parameter(description = "操作人ID") @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            log.info("修改部门状态，departmentId：{}，status：{}，userId：{}", departmentId, status, userId);
            ResponseDTO<String> result = departmentService.changeDepartmentStatus(departmentId, status,
                    userId != null ? userId : 1L);
            if (result.getOk()) {
                log.info("修改部门状态成功，departmentId：{}，status：{}", departmentId, status);
            } else {
                log.warn("修改部门状态失败，错误：{}", result.getMsg());
            }
            return result;
        } catch (Exception e) {
            log.error("修改部门状态失败", e);
            return ResponseDTO.error("修改状态失败：" + e.getMessage());
        }
    }

    @Operation(summary = "批量修改部门状态", description = "批量启用或禁用部门")
    @SaCheckPermission("department:status:batch:update")
    @PostMapping("/status/batch")
    public ResponseDTO<String> batchChangeDepartmentStatus(
            @Parameter(description = "部门ID列表") @RequestParam List<Long> departmentIds,
            @Parameter(description = "状态（1-启用，0-禁用）") @RequestParam Integer status,
            @Parameter(description = "操作人ID") @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            log.info("批量修改部门状态，departmentIds：{}，status：{}，userId：{}", departmentIds, status, userId);
            ResponseDTO<String> result = departmentService.batchChangeDepartmentStatus(departmentIds, status,
                    userId != null ? userId : 1L);
            if (result.getOk()) {
                log.info("批量修改部门状态成功，数量：{}，status：{}", departmentIds.size(), status);
            } else {
                log.warn("批量修改部门状态失败，错误：{}", result.getMsg());
            }
            return result;
        } catch (Exception e) {
            log.error("批量修改部门状态失败", e);
            return ResponseDTO.error("批量修改状态失败：" + e.getMessage());
        }
    }

    // ========== 移动接口 ==========

    @Operation(summary = "移动部门", description = "移动部门到新的父部门")
    @SaCheckPermission("department:move")
    @PostMapping("/move/{departmentId}")
    public ResponseDTO<String> moveDepartment(
            @Parameter(description = "部门ID") @PathVariable Long departmentId,
            @Parameter(description = "新父部门ID") @RequestParam Long newParentId,
            @Parameter(description = "操作人ID") @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            log.info("移动部门，departmentId：{}，newParentId：{}，userId：{}", departmentId, newParentId, userId);
            ResponseDTO<String> result = departmentService.moveDepartment(departmentId, newParentId,
                    userId != null ? userId : 1L);
            if (result.getOk()) {
                log.info("移动部门成功，departmentId：{}，newParentId：{}", departmentId, newParentId);
            } else {
                log.warn("移动部门失败，错误：{}", result.getMsg());
            }
            return result;
        } catch (Exception e) {
            log.error("移动部门失败", e);
            return ResponseDTO.error("移动失败：" + e.getMessage());
        }
    }

    // ========== 工具接口 ==========

    @Operation(summary = "获取部门及其子部门ID", description = "获取部门及其所有子部门的ID列表")
    @SaCheckPermission("department:children:query")
    @GetMapping("/{departmentId}/children")
    public ResponseDTO<List<Long>> getDepartmentSelfAndChildrenIds(
            @Parameter(description = "部门ID") @PathVariable Long departmentId) {
        try {
            log.info("获取部门及其子部门ID，departmentId：{}", departmentId);
            List<Long> result = departmentService.getDepartmentSelfAndChildrenIds(departmentId);
            log.info("获取部门及其子部门ID成功，departmentId：{}，数量：{}", departmentId, result.size());
            return SmartResponseUtil.success(result);
        } catch (Exception e) {
            log.error("获取部门及其子部门ID失败", e);
            return ResponseDTO.error("获取失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取部门路径", description = "获取部门路径字符串")
    @SaCheckPermission("department:path:query")
    @GetMapping("/{departmentId}/path")
    public ResponseDTO<String> getDepartmentPath(
            @Parameter(description = "部门ID") @PathVariable Long departmentId) {
        try {
            log.info("获取部门路径，departmentId：{}", departmentId);
            String result = departmentService.getDepartmentPath(departmentId);
            log.info("获取部门路径成功，departmentId：{}，路径：{}", departmentId, result);
            return SmartResponseUtil.success(result);
        } catch (Exception e) {
            log.error("获取部门路径失败", e);
            return ResponseDTO.error("获取失败：" + e.getMessage());
        }
    }

    @Operation(summary = "检查部门编码", description = "检查部门编码是否存在")
    @SaCheckPermission("department:code:check")
    @GetMapping("/check/code")
    public ResponseDTO<Boolean> checkDepartmentCode(
            @Parameter(description = "部门编码") @RequestParam String departmentCode,
            @Parameter(description = "排除的部门ID") @RequestParam(required = false) Long excludeId) {
        try {
            log.info("检查部门编码，departmentCode：{}，excludeId：{}", departmentCode, excludeId);
            boolean exists = departmentService.checkDepartmentCodeExists(departmentCode, excludeId);
            log.info("检查部门编码完成，departmentCode：{}，exists：{}", departmentCode, exists);
            return SmartResponseUtil.success(exists);
        } catch (Exception e) {
            log.error("检查部门编码失败", e);
            return ResponseDTO.error("检查失败：" + e.getMessage());
        }
    }

    @Operation(summary = "检查部门名称", description = "检查部门名称在同一父部门下是否存在")
    @SaCheckPermission("department:name:check")
    @GetMapping("/check/name")
    public ResponseDTO<Boolean> checkDepartmentName(
            @Parameter(description = "部门名称") @RequestParam String departmentName,
            @Parameter(description = "父部门ID") @RequestParam Long parentId,
            @Parameter(description = "排除的部门ID") @RequestParam(required = false) Long excludeId) {
        try {
            log.info("检查部门名称，departmentName：{}，parentId：{}，excludeId：{}", departmentName, parentId, excludeId);
            boolean exists = departmentService.checkDepartmentNameExists(departmentName, parentId, excludeId);
            log.info("检查部门名称完成，departmentName：{}，exists：{}", departmentName, exists);
            return SmartResponseUtil.success(exists);
        } catch (Exception e) {
            log.error("检查部门名称失败", e);
            return ResponseDTO.error("检查失败：" + e.getMessage());
        }
    }

    @Operation(summary = "检查子部门", description = "检查部门是否有子部门")
    @SaCheckPermission("department:children:check")
    @GetMapping("/{departmentId}/has-children")
    public ResponseDTO<Boolean> hasChildren(
            @Parameter(description = "部门ID") @PathVariable Long departmentId) {
        try {
            log.info("检查子部门，departmentId：{}", departmentId);
            boolean hasChildren = departmentService.hasChildren(departmentId);
            log.info("检查子部门完成，departmentId：{}，hasChildren：{}", departmentId, hasChildren);
            return SmartResponseUtil.success(hasChildren);
        } catch (Exception e) {
            log.error("检查子部门失败", e);
            return ResponseDTO.error("检查失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取启用部门", description = "获取所有启用的部门（用于下拉选择）")
    @SaCheckPermission("department:enabled:query")
    @GetMapping("/enabled")
    public ResponseDTO<List<DepartmentVO>> getAllEnabledDepartments() {
        try {
            log.info("获取所有启用的部门");
            List<DepartmentVO> result = departmentService.getAllEnabledDepartments();
            log.info("获取所有启用部门成功，数量：{}", result.size());
            return SmartResponseUtil.success(result);
        } catch (Exception e) {
            log.error("获取所有启用部门失败", e);
            return ResponseDTO.error("获取失败：" + e.getMessage());
        }
    }

    @Operation(summary = "根据负责人查询部门", description = "根据负责人ID查询部门")
    @SaCheckPermission("department:manager:query")
    @GetMapping("/manager/{managerUserId}")
    public ResponseDTO<List<DepartmentVO>> getDepartmentsByManager(
            @Parameter(description = "负责人ID") @PathVariable Long managerUserId) {
        try {
            log.info("根据负责人查询部门，managerUserId：{}", managerUserId);
            List<DepartmentVO> result = departmentService.getDepartmentsByManager(managerUserId);
            log.info("根据负责人查询部门成功，managerUserId：{}，数量：{}", managerUserId, result.size());
            return SmartResponseUtil.success(result);
        } catch (Exception e) {
            log.error("根据负责人查询部门失败", e);
            return ResponseDTO.error("查询失败：" + e.getMessage());
        }
    }

    // ========== 统计接口 ==========

    @Operation(summary = "获取部门统计", description = "获取部门统计信息")
    @SaCheckPermission("department:statistics:query")
    @GetMapping("/statistics")
    public ResponseDTO<Map<String, Object>> getDepartmentStatistics() {
        try {
            log.info("获取部门统计信息");
            Map<String, Object> result = departmentService.getDepartmentStatistics();
            log.info("获取部门统计信息成功");
            return SmartResponseUtil.success(result);
        } catch (Exception e) {
            log.error("获取部门统计信息失败", e);
            return ResponseDTO.error("获取失败：" + e.getMessage());
        }
    }

    // ========== 导入导出接口 ==========

    @Operation(summary = "导出部门", description = "导出部门数据")
    @SaCheckPermission("department:export")
    @PostMapping("/export")
    public ResponseDTO<List<Map<String, Object>>> exportDepartmentData(
            @Parameter(description = "查询条件") @RequestBody DepartmentQueryForm queryForm) {
        try {
            log.info("导出部门数据，查询条件：{}", queryForm);
            List<Map<String, Object>> result = departmentService.exportDepartmentData(queryForm);
            log.info("导出部门数据成功，数量：{}", result.size());
            return SmartResponseUtil.success(result);
        } catch (Exception e) {
            log.error("导出部门数据失败", e);
            return ResponseDTO.error("导出失败：" + e.getMessage());
        }
    }

    @Operation(summary = "导入部门", description = "导入部门数据")
    @SaCheckPermission("department:import")
    @PostMapping("/import")
    public ResponseDTO<Map<String, Object>> importDepartmentData(
            @Parameter(description = "导入数据") @RequestBody List<Map<String, Object>> importData,
            @Parameter(description = "操作人ID") @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            log.info("导入部门数据，数据量：{}，userId：{}", importData.size(), userId);
            ResponseDTO<Map<String, Object>> result = departmentService.importDepartmentData(importData,
                    userId != null ? userId : 1L);
            if (result.getOk()) {
                log.info("导入部门数据成功");
            } else {
                log.warn("导入部门数据失败，错误：{}", result.getMsg());
            }
            return result;
        } catch (Exception e) {
            log.error("导入部门数据失败", e);
            return ResponseDTO.error("导入失败：" + e.getMessage());
        }
    }

    // ========== 验证接口 ==========

    @Operation(summary = "验证部门数据", description = "验证部门数据完整性")
    @SaCheckPermission("department:validate")
    @GetMapping("/{departmentId}/validate")
    public ResponseDTO<Map<String, Object>> validateDepartmentData(
            @Parameter(description = "部门ID") @PathVariable Long departmentId) {
        try {
            log.info("验证部门数据，departmentId：{}", departmentId);
            ResponseDTO<Map<String, Object>> result = departmentService.validateDepartmentData(departmentId);
            if (result.getOk()) {
                log.info("验证部门数据成功，departmentId：{}", departmentId);
            } else {
                log.warn("验证部门数据失败，错误：{}", result.getMsg());
            }
            return result;
        } catch (Exception e) {
            log.error("验证部门数据失败", e);
            return ResponseDTO.error("验证失败：" + e.getMessage());
        }
    }
}
