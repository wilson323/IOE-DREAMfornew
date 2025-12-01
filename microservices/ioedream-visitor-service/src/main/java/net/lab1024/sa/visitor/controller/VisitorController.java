package net.lab1024.sa.visitor.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.visitor.domain.entity.VisitorEntity;
import net.lab1024.sa.visitor.domain.form.VisitorForm;
import net.lab1024.sa.visitor.domain.query.VisitorQueryVO;
import net.lab1024.sa.visitor.domain.vo.VisitorVO;
import net.lab1024.sa.visitor.service.VisitorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 访客管理控制器
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-27
 */
@Slf4j
@RestController
@RequestMapping("/api/visitor")
@Tag(name = "访客管理", description = "访客管理相关接口")
public class VisitorController {

    @Resource
    private VisitorService visitorService;

    /**
     * 分页查询访客列表
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询访客列表")
    @SaCheckLogin
    @SaCheckPermission("visitor:page")
    public ResponseDTO<PageResult<VisitorVO>> page(@RequestBody @Valid VisitorQueryVO query) {
        try {
            // 直接调用访客服务
            ResponseDTO<PageResult<VisitorVO>> response = visitorService.queryVisitors(query);
            return response;
        } catch (Exception e) {
            log.error("分页查询访客列表异常", e);
            return ResponseDTO.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询访客详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询访客详情")
    @SaCheckLogin
    @SaCheckPermission("visitor:query")
    public ResponseDTO<VisitorVO> getById(@Parameter(description = "访客ID") @PathVariable Long id) {
        try {
            VisitorVO visitor = visitorService.getById(id);
            return ResponseDTO.ok(visitor);
        } catch (Exception e) {
            log.error("查询访客详情异常, id: {}", id, e);
            return ResponseDTO.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 新增访客
     */
    @PostMapping("/add")
    @Operation(summary = "新增访客")
    @SaCheckLogin
    @SaCheckPermission("visitor:add")
    public ResponseDTO<Long> add(@RequestBody @Valid VisitorForm form) {
        try {
            Long id = visitorService.add(form);
            return ResponseDTO.ok(id);
        } catch (Exception e) {
            log.error("新增访客异常", e);
            return ResponseDTO.error("新增失败: " + e.getMessage());
        }
    }

    /**
     * 更新访客信息
     */
    @PutMapping("/update")
    @Operation(summary = "更新访客信息")
    @SaCheckLogin
    @SaCheckPermission("visitor:update")
    public ResponseDTO<Boolean> update(@RequestBody @Valid VisitorForm form) {
        try {
            boolean result = visitorService.update(form);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("更新访客信息异常", e);
            return ResponseDTO.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除访客
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除访客")
    @SaCheckLogin
    @SaCheckPermission("visitor:delete")
    public ResponseDTO<Boolean> delete(@Parameter(description = "访客ID") @PathVariable Long id) {
        try {
            boolean result = visitorService.delete(id);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("删除访客异常, id: {}", id, e);
            return ResponseDTO.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除访客
     */
    @DeleteMapping("/batchDelete")
    @Operation(summary = "批量删除访客")
    @SaCheckLogin
    @SaCheckPermission("visitor:delete")
    public ResponseDTO<Boolean> batchDelete(@RequestBody List<Long> ids) {
        try {
            boolean result = visitorService.batchDelete(ids);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("批量删除访客异常, ids: {}", ids, e);
            return ResponseDTO.error("批量删除失败: " + e.getMessage());
        }
    }

    /**
     * 根据手机号查询访客信息
     */
    @GetMapping("/byPhone/{phone}")
    @Operation(summary = "根据手机号查询访客信息")
    @SaCheckLogin
    @SaCheckPermission("visitor:query")
    public ResponseDTO<List<VisitorVO>> getByPhone(@Parameter(description = "手机号") @PathVariable String phone) {
        try {
            List<VisitorVO> visitors = visitorService.getByPhone(phone);
            return ResponseDTO.ok(visitors);
        } catch (Exception e) {
            log.error("根据手机号查询访客异常, phone: {}", phone, e);
            return ResponseDTO.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询当前在访客列表
     */
    @GetMapping("/currentVisitors")
    @Operation(summary = "查询当前在访客列表")
    @SaCheckLogin
    @SaCheckPermission("visitor:query")
    public ResponseDTO<List<VisitorVO>> getCurrentVisitors() {
        try {
            List<VisitorVO> visitors = visitorService.getCurrentVisitors();
            return ResponseDTO.ok(visitors);
        } catch (Exception e) {
            log.error("查询当前在访客列表异常", e);
            return ResponseDTO.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取访客统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取访客统计信息")
    @SaCheckLogin
    @SaCheckPermission("visitor:statistics")
    public ResponseDTO<Object> getStatistics() {
        try {
            Object statistics = visitorService.getStatistics();
            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("获取访客统计信息异常", e);
            return ResponseDTO.error("查询统计信息失败: " + e.getMessage());
        }
    }
}