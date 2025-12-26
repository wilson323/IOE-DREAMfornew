package net.lab1024.sa.access.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.form.AccessLinkageRuleAddForm;
import net.lab1024.sa.access.domain.form.AccessLinkageRuleQueryForm;
import net.lab1024.sa.access.domain.form.AccessLinkageRuleUpdateForm;
import net.lab1024.sa.access.domain.vo.AccessLinkageRuleVO;
import net.lab1024.sa.access.service.AccessLinkageService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

/**
 * 门禁联动规则控制器
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Slf4j
@RestController
@Tag(name = "门禁联动管理")
@RequestMapping("/api/v1/access/linkage")
public class AccessLinkageController {

    @Resource
    private AccessLinkageService linkageService;

    @Operation(summary = "分页查询联动规则")
    @PostMapping("/page")
    public ResponseDTO<PageResult<AccessLinkageRuleVO>> queryPage(@Valid @RequestBody AccessLinkageRuleQueryForm queryForm) {
        log.info("[联动管理] 分页查询联动规则: queryForm={}", queryForm);
        PageResult<AccessLinkageRuleVO> pageResult = linkageService.queryPage(queryForm);
        return ResponseDTO.ok(pageResult);
    }

    @Operation(summary = "查询联动规则详情")
    @GetMapping("/{ruleId}")
    public ResponseDTO<AccessLinkageRuleVO> getById(@PathVariable Long ruleId) {
        log.info("[联动管理] 查询联动规则详情: ruleId={}", ruleId);
        AccessLinkageRuleVO vo = linkageService.getById(ruleId);
        return ResponseDTO.ok(vo);
    }

    @Operation(summary = "新增联动规则")
    @PostMapping("/add")
    public ResponseDTO<Long> add(@Valid @RequestBody AccessLinkageRuleAddForm addForm) {
        log.info("[联动管理] 新增联动规则: addForm={}", addForm);
        Long ruleId = linkageService.add(addForm);
        return ResponseDTO.ok(ruleId);
    }

    @Operation(summary = "更新联动规则")
    @PostMapping("/update/{ruleId}")
    public ResponseDTO<Void> update(@PathVariable Long ruleId, @Valid @RequestBody AccessLinkageRuleUpdateForm updateForm) {
        log.info("[联动管理] 更新联动规则: ruleId={}, updateForm={}", ruleId, updateForm);
        linkageService.update(ruleId, updateForm);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除联动规则")
    @PostMapping("/delete/{ruleId}")
    public ResponseDTO<Void> delete(@PathVariable Long ruleId) {
        log.info("[联动管理] 删除联动规则: ruleId={}", ruleId);
        linkageService.delete(ruleId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "启用/禁用联动规则")
    @PostMapping("/enabled/{ruleId}")
    public ResponseDTO<Void> updateEnabled(@PathVariable Long ruleId, @RequestParam Integer enabled) {
        log.info("[联动管理] 更新联动规则启用状态: ruleId={}, enabled={}", ruleId, enabled);
        linkageService.updateEnabled(ruleId, enabled);
        return ResponseDTO.ok();
    }

    @Operation(summary = "触发联动")
    @PostMapping("/trigger")
    public ResponseDTO<String> triggerLinkage(
            @RequestParam Long triggerDeviceId,
            @RequestParam(required = false) Long triggerDoorId,
            @RequestParam String triggerEvent) {
        log.info("[联动管理] 触发联动: triggerDeviceId={}, triggerDoorId={}, triggerEvent={}",
                triggerDeviceId, triggerDoorId, triggerEvent);
        String result = linkageService.triggerLinkage(triggerDeviceId, triggerDoorId, triggerEvent);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "测试联动规则")
    @PostMapping("/test/{ruleId}")
    public ResponseDTO<String> testRule(@PathVariable Long ruleId) {
        log.info("[联动管理] 测试联动规则: ruleId={}", ruleId);
        String result = linkageService.testRule(ruleId);
        return ResponseDTO.ok(result);
    }
}
