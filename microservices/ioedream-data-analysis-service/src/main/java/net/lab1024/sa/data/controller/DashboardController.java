package net.lab1024.sa.data.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.data.domain.DataAnalysisDomain.*;
import net.lab1024.sa.data.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 仪表板控制器
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/dashboards")
@Tag(name = "仪表板管理", description = "数据可视化仪表板、实时数据刷新")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    // ==================== 仪表板管理 ====================

    @Operation(summary = "创建仪表板")
    @PostMapping
    public ResponseDTO<Long> createDashboard(@Valid @RequestBody DashboardVO dashboard) {
        log.info("[仪表板API] 创建仪表板: dashboardName={}", dashboard.getDashboardName());

        Long dashboardId = dashboardService.createDashboard(dashboard);

        log.info("[仪表板API] 仪表板创建成功: dashboardId={}", dashboardId);
        return ResponseDTO.ok(dashboardId);
    }

    @Operation(summary = "更新仪表板")
    @PutMapping("/{dashboardId}")
    public ResponseDTO<Void> updateDashboard(@PathVariable Long dashboardId,
                                             @Valid @RequestBody DashboardVO dashboard) {
        log.info("[仪表板API] 更新仪表板: dashboardId={}", dashboardId);

        dashboardService.updateDashboard(dashboardId, dashboard);

        log.info("[仪表板API] 仪表板更新成功: dashboardId={}", dashboardId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除仪表板")
    @DeleteMapping("/{dashboardId}")
    public ResponseDTO<Void> deleteDashboard(@PathVariable Long dashboardId) {
        log.info("[仪表板API] 删除仪表板: dashboardId={}", dashboardId);

        dashboardService.deleteDashboard(dashboardId);

        log.info("[仪表板API] 仪表板删除成功: dashboardId={}", dashboardId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "获取仪表板详情")
    @GetMapping("/{dashboardId}")
    public ResponseDTO<DashboardVO> getDashboardDetail(@PathVariable Long dashboardId) {
        log.info("[仪表板API] 获取仪表板详情: dashboardId={}", dashboardId);

        DashboardVO dashboard = dashboardService.getDashboardDetail(dashboardId);

        return ResponseDTO.ok(dashboard);
    }

    @Operation(summary = "获取仪表板列表")
    @GetMapping
    public ResponseDTO<PageResult<DashboardVO>> getDashboardList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("[仪表板API] 查询仪表板列表: pageNum={}, pageSize={}", pageNum, pageSize);

        PageResult<DashboardVO> result = dashboardService.getDashboardList(pageNum, pageSize);

        return ResponseDTO.ok(result);
    }

    @Operation(summary = "复制仪表板")
    @PostMapping("/{dashboardId}/copy")
    public ResponseDTO<Long> copyDashboard(@PathVariable Long dashboardId,
                                           @RequestParam String newDashboardName) {
        log.info("[仪表板API] 复制仪表板: dashboardId={}, newName={}", dashboardId, newDashboardName);

        Long newDashboardId = dashboardService.copyDashboard(dashboardId, newDashboardName);

        log.info("[仪表板API] 仪表板复制成功: newDashboardId={}", newDashboardId);
        return ResponseDTO.ok(newDashboardId);
    }

    // ==================== 仪表板数据 ====================

    @Operation(summary = "获取仪表板数据")
    @PostMapping("/{dashboardId}/data")
    public ResponseDTO<Map<String, Object>> getDashboardData(@PathVariable Long dashboardId,
                                                             @RequestBody(required = false) Map<String, Object> params) {
        log.info("[仪表板API] 获取仪表板数据: dashboardId={}", dashboardId);

        Map<String, Object> data = dashboardService.getDashboardData(dashboardId,
                                                                      params != null ? params : Map.of());

        return ResponseDTO.ok(data);
    }

    @Operation(summary = "刷新仪表板数据")
    @PostMapping("/{dashboardId}/refresh")
    public ResponseDTO<Map<String, Object>> refreshDashboardData(@PathVariable Long dashboardId) {
        log.info("[仪表板API] 刷新仪表板数据: dashboardId={}", dashboardId);

        Map<String, Object> data = dashboardService.refreshDashboardData(dashboardId);

        return ResponseDTO.ok(data);
    }

    @Operation(summary = "获取组件数据")
    @PostMapping("/{dashboardId}/component/{componentId}")
    public ResponseDTO<Map<String, Object>> getComponentData(@PathVariable Long dashboardId,
                                                              @PathVariable String componentId,
                                                              @RequestBody(required = false) Map<String, Object> params) {
        log.info("[仪表板API] 获取组件数据: dashboardId={}, componentId={}", dashboardId, componentId);

        Map<String, Object> data = dashboardService.getComponentData(dashboardId,
                                                                     componentId,
                                                                     params != null ? params : Map.of());

        return ResponseDTO.ok(data);
    }

    @Operation(summary = "批量获取组件数据")
    @PostMapping("/{dashboardId}/components/batch")
    public ResponseDTO<Map<String, Object>> batchGetComponentData(@PathVariable Long dashboardId,
                                                                    @RequestBody List<String> componentIds) {
        log.info("[仪表板API] 批量获取组件数据: dashboardId={}, count={}",
                 dashboardId, componentIds.size());

        Map<String, Object> data = dashboardService.batchGetComponentData(dashboardId, componentIds);

        return ResponseDTO.ok(data);
    }

    // ==================== 仪表板模板 ====================

    @Operation(summary = "获取仪表板模板")
    @GetMapping("/templates")
    public ResponseDTO<List<DashboardVO>> getDashboardTemplates(
            @RequestParam(required = false) String businessModule) {
        log.info("[仪表板API] 获取仪表板模板: module={}", businessModule);

        List<DashboardVO> templates = dashboardService.getDashboardTemplates(businessModule);

        return ResponseDTO.ok(templates);
    }

    @Operation(summary = "应用仪表板模板")
    @PostMapping("/templates/{templateId}/apply")
    public ResponseDTO<Long> applyDashboardTemplate(@PathVariable Long templateId,
                                                    @RequestParam String dashboardName) {
        log.info("[仪表板API] 应用仪表板模板: templateId={}, name={}", templateId, dashboardName);

        Long dashboardId = dashboardService.applyDashboardTemplate(templateId, dashboardName);

        log.info("[仪表板API] 仪表板创建成功: dashboardId={}", dashboardId);
        return ResponseDTO.ok(dashboardId);
    }

    @Operation(summary = "保存为仪表板模板")
    @PostMapping("/{dashboardId}/save-template")
    public ResponseDTO<Long> saveAsDashboardTemplate(@PathVariable Long dashboardId,
                                                     @RequestParam String templateName) {
        log.info("[仪表板API] 保存为仪表板模板: dashboardId={}, name={}", dashboardId, templateName);

        Long templateId = dashboardService.saveAsDashboardTemplate(dashboardId, templateName);

        log.info("[仪表板API] 模板创建成功: templateId={}", templateId);
        return ResponseDTO.ok(templateId);
    }

    // ==================== 仪表板布局 ====================

    @Operation(summary = "更新仪表板布局")
    @PutMapping("/{dashboardId}/layout")
    public ResponseDTO<Void> updateDashboardLayout(@PathVariable Long dashboardId,
                                                    @RequestBody DashboardConfig layout) {
        log.info("[仪表板API] 更新仪表板布局: dashboardId={}", dashboardId);

        dashboardService.updateDashboardLayout(dashboardId, layout);

        log.info("[仪表板API] 布局更新成功: dashboardId={}", dashboardId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "添加仪表板组件")
    @PostMapping("/{dashboardId}/components")
    public ResponseDTO<String> addDashboardComponent(@PathVariable Long dashboardId,
                                                      @RequestBody DashboardComponent component) {
        log.info("[仪表板API] 添加仪表板组件: dashboardId={}, type={}",
                 dashboardId, component.getComponentType());

        String componentId = dashboardService.addDashboardComponent(dashboardId, component);

        log.info("[仪表板API] 组件添加成功: componentId={}", componentId);
        return ResponseDTO.ok(componentId);
    }

    @Operation(summary = "更新仪表板组件")
    @PutMapping("/{dashboardId}/components/{componentId}")
    public ResponseDTO<Void> updateDashboardComponent(@PathVariable Long dashboardId,
                                                       @PathVariable String componentId,
                                                       @RequestBody DashboardComponent component) {
        log.info("[仪表板API] 更新仪表板组件: dashboardId={}, componentId={}",
                 dashboardId, componentId);

        dashboardService.updateDashboardComponent(dashboardId, componentId, component);

        log.info("[仪表板API] 组件更新成功: componentId={}", componentId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除仪表板组件")
    @DeleteMapping("/{dashboardId}/components/{componentId}")
    public ResponseDTO<Void> removeDashboardComponent(@PathVariable Long dashboardId,
                                                       @PathVariable String componentId) {
        log.info("[仪表板API] 删除仪表板组件: dashboardId={}, componentId={}",
                 dashboardId, componentId);

        dashboardService.removeDashboardComponent(dashboardId, componentId);

        log.info("[仪表板API] 组件删除成功: componentId={}", componentId);
        return ResponseDTO.ok();
    }

    // ==================== 仪表板权限 ====================

    @Operation(summary = "设置仪表板权限")
    @PutMapping("/{dashboardId}/permission")
    public ResponseDTO<Void> setDashboardPermission(@PathVariable Long dashboardId,
                                                     @RequestBody ReportPermission permission) {
        log.info("[仪表板API] 设置仪表板权限: dashboardId={}", dashboardId);

        dashboardService.setDashboardPermission(dashboardId, permission);

        log.info("[仪表板API] 权限设置成功: dashboardId={}", dashboardId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "检查仪表板权限")
    @GetMapping("/{dashboardId}/permission/{permissionType}")
    public ResponseDTO<Boolean> checkDashboardPermission(@PathVariable Long dashboardId,
                                                          @PathVariable String permissionType) {
        log.info("[仪表板API] 检查仪表板权限: dashboardId={}, type={}", dashboardId, permissionType);

        Boolean hasPermission = dashboardService.checkDashboardPermission(dashboardId, permissionType);

        return ResponseDTO.ok(hasPermission);
    }

    // ==================== 仪表板分享 ====================

    @Operation(summary = "生成仪表板分享链接")
    @PostMapping("/{dashboardId}/share")
    public ResponseDTO<String> generateShareLink(@PathVariable Long dashboardId,
                                                  @RequestParam(defaultValue = "24") Integer expireHours) {
        log.info("[仪表板API] 生成分享链接: dashboardId={}, expireHours={}",
                 dashboardId, expireHours);

        String shareLink = dashboardService.generateShareLink(dashboardId, expireHours);

        log.info("[仪表板API] 分享链接生成成功: dashboardId={}", dashboardId);
        return ResponseDTO.ok(shareLink);
    }

    @Operation(summary = "通过分享链接访问仪表板")
    @GetMapping("/share/{shareToken}")
    public ResponseDTO<Map<String, Object>> accessDashboardByShare(@PathVariable String shareToken) {
        log.info("[仪表板API] 通过分享链接访问: token={}", shareToken);

        Map<String, Object> data = dashboardService.accessDashboardByShare(shareToken);

        return ResponseDTO.ok(data);
    }
}
