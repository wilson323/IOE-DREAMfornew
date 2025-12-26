package net.lab1024.sa.access.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.form.AccessInterlockRuleAddForm;
import net.lab1024.sa.access.domain.form.AccessInterlockRuleQueryForm;
import net.lab1024.sa.access.domain.form.AccessInterlockRuleUpdateForm;
import net.lab1024.sa.access.domain.vo.AccessInterlockRuleVO;
import net.lab1024.sa.access.service.AccessInterlockService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

/**
 * 门禁全局互锁规则 控制器
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Slf4j
@RestController
@Tag(name = "门禁全局互锁管理")
@RequestMapping("/api/v1/access/interlock")
public class AccessInterlockController {

    @Resource
    private AccessInterlockService interlockService;

    @Operation(summary = "分页查询互锁规则")
    @PostMapping("/page")
    public ResponseDTO<PageResult<AccessInterlockRuleVO>> queryPage(@RequestBody AccessInterlockRuleQueryForm queryForm) {
        log.info("[互锁管理] 分页查询互锁规则: queryForm={}", queryForm);
        PageResult<AccessInterlockRuleVO> pageResult = interlockService.queryPage(queryForm);
        return ResponseDTO.ok(pageResult);
    }

    @Operation(summary = "获取规则详情")
    @GetMapping("/{ruleId}")
    public ResponseDTO<AccessInterlockRuleVO> getById(@PathVariable Long ruleId) {
        log.info("[互锁管理] 获取规则详情: ruleId={}", ruleId);
        AccessInterlockRuleVO rule = interlockService.getById(ruleId);
        return ResponseDTO.ok(rule);
    }

    @Operation(summary = "新增互锁规则")
    @PostMapping("/add")
    public ResponseDTO<Long> addRule(@RequestBody @Valid AccessInterlockRuleAddForm addForm) {
        log.info("[互锁管理] 新增互锁规则: addForm={}", addForm);
        Long ruleId = interlockService.addRule(addForm);
        return ResponseDTO.ok(ruleId);
    }

    @Operation(summary = "更新互锁规则")
    @PostMapping("/update/{ruleId}")
    public ResponseDTO<Void> updateRule(
            @PathVariable Long ruleId,
            @RequestBody @Valid AccessInterlockRuleUpdateForm updateForm) {
        log.info("[互锁管理] 更新互锁规则: ruleId={}, updateForm={}", ruleId, updateForm);
        interlockService.updateRule(ruleId, updateForm);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除互锁规则")
    @PostMapping("/delete/{ruleId}")
    public ResponseDTO<Void> deleteRule(@PathVariable Long ruleId) {
        log.info("[互锁管理] 删除互锁规则: ruleId={}", ruleId);
        interlockService.deleteRule(ruleId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "启用/禁用互锁规则")
    @PostMapping("/enabled/{ruleId}")
    public ResponseDTO<Void> updateEnabled(
            @PathVariable Long ruleId,
            @RequestParam Integer enabled) {
        log.info("[互锁管理] 更新启用状态: ruleId={}, enabled={}", ruleId, enabled);
        interlockService.updateEnabled(ruleId, enabled);
        return ResponseDTO.ok();
    }

    @Operation(summary = "触发互锁")
    @PostMapping("/trigger")
    public ResponseDTO<String> triggerInterlock(
            @RequestParam Long areaId,
            @RequestParam(required = false) Long doorId,
            @RequestParam String action) {
        log.info("[互锁管理] 触发互锁: areaId={}, doorId={}, action={}", areaId, doorId, action);
        String result = interlockService.triggerInterlock(areaId, doorId, action);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "手动解锁")
    @PostMapping("/unlock")
    public ResponseDTO<Void> manualUnlock(
            @RequestParam Long ruleId,
            @RequestParam Long areaId) {
        log.info("[互锁管理] 手动解锁: ruleId={}, areaId={}", ruleId, areaId);
        interlockService.manualUnlock(ruleId, areaId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "测试互锁规则")
    @PostMapping("/test/{ruleId}")
    public ResponseDTO<String> testRule(@PathVariable Long ruleId) {
        log.info("[互锁管理] 测试互锁规则: ruleId={}", ruleId);
        String result = interlockService.testRule(ruleId);
        return ResponseDTO.ok(result);
    }
}
