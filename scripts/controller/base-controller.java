package {{package}}.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.controller.SupportBaseController;
import net.lab1024.sa.base.common.response.PageResult;
import net.lab1024.sa.base.common.response.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartRequestUtil;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

/**
 * {{EntityName}}管理控制器
 *
 * @author {{author}}
 * @date {{date}}
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/{{apiPath}}")
@Tag(name = "{{EntityName}}管理", description = "{{EntityName}}相关接口")
public class {{EntityName}}Controller extends SupportBaseController {

    @Resource
    private {{EntityName}}Service {{entityName}}Service;

    // ==================== 基础CRUD接口 ====================

    /**
     * 分页查询
     */
    @Operation(summary = "分页查询{{EntityName}}")
    @PostMapping("/page")
    @SaCheckPermission("{{apiPath}}:page")
    public ResponseDTO<PageResult<{{EntityName}}VO>> page(@RequestBody @Valid {{EntityName}}QueryForm queryForm) {
        try {
            return {{entityName}}Service.getPage(queryForm);
        } catch (Exception e) {
            log.error("分页查询{}失败", "{{EntityName}}", e);
            return ResponseDTO.error("查询失败，请稍后重试");
        }
    }

    /**
     * 查询详情
     */
    @Operation(summary = "查询{{EntityName}}详情")
    @GetMapping("/{id}")
    @SaCheckPermission("{{apiPath}}:detail")
    public ResponseDTO<{{EntityName}}VO> getDetail(@PathVariable Long id) {
        try {
            return {{entityName}}Service.getById(id);
        } catch (Exception e) {
            log.error("查询{}详情失败，ID: {}", "{{EntityName}}", id, e);
            return ResponseDTO.error("查询失败，请稍后重试");
        }
    }

    /**
     * 查询详情（包含关联数据）
     */
    @Operation(summary = "查询{{EntityName}}完整详情")
    @GetMapping("/{id}/full")
    @SaCheckPermission("{{apiPath}}:detail")
    public ResponseDTO<{{EntityName}}DetailVO> getFullDetail(@PathVariable Long id) {
        try {
            return {{entityName}}Service.getDetail(id);
        } catch (Exception e) {
            log.error("查询{}完整详情失败，ID: {}", "{{EntityName}}", id, e);
            return ResponseDTO.error("查询失败，请稍后重试");
        }
    }

    /**
     * 添加
     */
    @Operation(summary = "添加{{EntityName}}")
    @PostMapping
    @SaCheckPermission("{{apiPath}}:add")
    public ResponseDTO<Long> add(@RequestBody @Valid {{EntityName}}AddForm addForm) {
        try {
            return {{entityName}}Service.add(addForm);
        } catch (Exception e) {
            log.error("添加{}失败", "{{EntityName}}", e);
            return ResponseDTO.error("添加失败，请稍后重试");
        }
    }

    /**
     * 更新
     */
    @Operation(summary = "更新{{EntityName}}")
    @PutMapping
    @SaCheckPermission("{{apiPath}}:update")
    public ResponseDTO<Boolean> update(@RequestBody @Valid {{EntityName}}UpdateForm updateForm) {
        try {
            return {{entityName}}Service.update(updateForm);
        } catch (Exception e) {
            log.error("更新{}失败，ID: {}", "{{EntityName}}", updateForm.getId(), e);
            return ResponseDTO.error("更新失败，请稍后重试");
        }
    }

    /**
     * 删除
     */
    @Operation(summary = "删除{{EntityName}}")
    @DeleteMapping("/{id}")
    @SaCheckPermission("{{apiPath}}:delete")
    public ResponseDTO<Boolean> delete(@PathVariable Long id) {
        try {
            return {{entityName}}Service.delete(id);
        } catch (Exception e) {
            log.error("删除{}失败，ID: {}", "{{EntityName}}", id, e);
            return ResponseDTO.error("删除失败，请稍后重试");
        }
    }

    /**
     * 批量删除
     */
    @Operation(summary = "批量删除{{EntityName}}")
    @DeleteMapping("/batch")
    @SaCheckPermission("{{apiPath}}:delete")
    public ResponseDTO<Boolean> batchDelete(@RequestBody List<Long> idList) {
        try {
            return {{entityName}}Service.batchDelete(idList);
        } catch (Exception e) {
            log.error("批量删除{}失败", "{{EntityName}}", e);
            return ResponseDTO.error("批量删除失败，请稍后重试");
        }
    }

    // ==================== 业务扩展接口 ====================

    /**
     * 更新状态
     */
    @Operation(summary = "更新{{EntityName}}状态")
    @PutMapping("/{id}/status")
    @SaCheckPermission("{{apiPath}}:update")
    public ResponseDTO<Boolean> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            return {{entityName}}Service.updateStatus(id, status);
        } catch (Exception e) {
            log.error("更新{}状态失败，ID: {}, Status: {}", "{{EntityName}}", id, status, e);
            return ResponseDTO.error("状态更新失败，请稍后重试");
        }
    }

    /**
     * 启用
     */
    @Operation(summary = "启用{{EntityName}}")
    @PutMapping("/{id}/enable")
    @SaCheckPermission("{{apiPath}}:update")
    public ResponseDTO<Boolean> enable(@PathVariable Long id) {
        try {
            return {{entityName}}Service.updateStatus(id, 1);
        } catch (Exception e) {
            log.error("启用{}失败，ID: {}", "{{EntityName}}", id, e);
            return ResponseDTO.error("启用失败，请稍后重试");
        }
    }

    /**
     * 禁用
     */
    @Operation(summary = "禁用{{EntityName}}")
    @PutMapping("/{id}/disable")
    @SaCheckPermission("{{apiPath}}:update")
    public ResponseDTO<Boolean> disable(@PathVariable Long id) {
        try {
            return {{entityName}}Service.updateStatus(id, 0);
        } catch (Exception e) {
            log.error("禁用{}失败，ID: {}", "{{EntityName}}", id, e);
            return ResponseDTO.error("禁用失败，请稍后重试");
        }
    }

    /**
     * 导出Excel
     */
    @Operation(summary = "导出{{EntityName}}Excel")
    @PostMapping("/export")
    @SaCheckPermission("{{apiPath}}:export")
    public void exportExcel(@RequestBody @Valid {{EntityName}}QueryForm queryForm) {
        try {
            // 获取查询结果
            ResponseDTO<PageResult<{{EntityName}}VO>> pageResult = {{entityName}}Service.getPage(queryForm);
            if (!pageResult.getOk()) {
                SmartRequestUtil.writeJsonResponse(getResponse(), pageResult);
                return;
            }

            // 导出Excel
            {{entityName}}ExcelExporter.export(pageResult.getData().getRows(), getResponse());

        } catch (Exception e) {
            log.error("导出{}Excel失败", "{{EntityName}}", e);
            SmartRequestUtil.writeJsonResponse(getResponse(), ResponseDTO.error("导出失败，请稍后重试"));
        }
    }

    /**
     * 导入Excel
     */
    @Operation(summary = "导入{{EntityName}}Excel")
    @PostMapping("/import")
    @SaCheckPermission("{{apiPath}}:import")
    public ResponseDTO<String> importExcel() {
        try {
            // TODO: 实现Excel导入逻辑
            return ResponseDTO.ok("导入成功");
        } catch (Exception e) {
            log.error("导入{}Excel失败", "{{EntityName}}", e);
            return ResponseDTO.error("导入失败，请稍后重试");
        }
    }

    /**
     * 下载导入模板
     */
    @Operation(summary = "下载{{EntityName}}导入模板")
    @GetMapping("/import/template")
    @SaCheckPermission("{{apiPath}}:import")
    public void downloadImportTemplate() {
        try {
            {{entityName}}ExcelExporter.downloadTemplate(getResponse());
        } catch (Exception e) {
            log.error("下载{}导入模板失败", "{{EntityName}}", e);
            SmartRequestUtil.writeJsonResponse(getResponse(), ResponseDTO.error("下载失败，请稍后重试"));
        }
    }

    /**
     * 获取选择器数据
     */
    @Operation(summary = "获取{{EntityName}}选择器数据")
    @GetMapping("/selector")
    @SaCheckPermission("{{apiPath}}:page")
    public ResponseDTO<List<{{EntityName}}SelectorVO>> getSelectorData(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "20") Integer limit) {
        try {
            // TODO: 实现选择器数据查询逻辑
            return ResponseDTO.ok(List.of());
        } catch (Exception e) {
            log.error("获取{}选择器数据失败", "{{EntityName}}", e);
            return ResponseDTO.error("查询失败，请稍后重试");
        }
    }

    /**
     * 获取统计数据
     */
    @Operation(summary = "获取{{EntityName}}统计数据")
    @GetMapping("/statistics")
    @SaCheckPermission("{{apiPath}}:page")
    public ResponseDTO<{{EntityName}}StatisticsVO> getStatistics() {
        try {
            // TODO: 实现统计数据查询逻辑
            return ResponseDTO.ok(new {{EntityName}}StatisticsVO());
        } catch (Exception e) {
            log.error("获取{}统计数据失败", "{{EntityName}}", e);
            return ResponseDTO.error("查询失败，请稍后重试");
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        // TODO: 从SecurityContext获取当前用户ID
        return 1L;
    }

    /**
     * 记录操作日志
     */
    private void logOperation(String operation, Object data) {
        try {
            String logInfo = String.format("用户ID: %d, 操作: %s, 数据: %s",
                getCurrentUserId(), operation, data);
            log.info("{}", logInfo);
            // TODO: 调用操作日志服务记录日志
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }
    }
}