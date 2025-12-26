package net.lab1024.sa.attendance.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.dao.AttendanceRuleConfigDao;
import net.lab1024.sa.attendance.entity.AttendanceRuleConfigEntity;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 考勤规则配置Controller
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/attendance/rule-config")
@Tag(name = "考勤规则配置管理")
public class AttendanceRuleConfigController {

    private final AttendanceRuleConfigDao ruleConfigDao;

    public AttendanceRuleConfigController(AttendanceRuleConfigDao ruleConfigDao) {
        this.ruleConfigDao = ruleConfigDao;
    }

    /**
     * 分页查询规则配置
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询规则配置")
    public ResponseDTO<Page<AttendanceRuleConfigEntity>> pageRules(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String ruleName,
            @RequestParam(required = false) String applyScope,
            @RequestParam(required = false) Integer ruleStatus) {

        log.info("[规则配置] 分页查询规则: pageNum={}, pageSize={}, ruleName={}", pageNum, pageSize, ruleName);

        QueryWrapper<AttendanceRuleConfigEntity> queryWrapper = new QueryWrapper<>();

        if (ruleName != null && !ruleName.isEmpty()) {
            queryWrapper.like("rule_name", ruleName);
        }
        if (applyScope != null) {
            queryWrapper.eq("apply_scope", applyScope);
        }
        if (ruleStatus != null) {
            queryWrapper.eq("rule_status", ruleStatus);
        }

        queryWrapper.orderByDesc("create_time");

        Page<AttendanceRuleConfigEntity> page = new Page<>(pageNum, pageSize);
        // TODO: 调用DAO分页查询

        return ResponseDTO.ok(page);
    }

    /**
     * 查询所有启用的规则
     */
    @GetMapping("/enabled")
    @Operation(summary = "查询所有启用的规则")
    public ResponseDTO<List<AttendanceRuleConfigEntity>> getEnabledRules() {
        log.info("[规则配置] 查询启用的规则");

        List<AttendanceRuleConfigEntity> rules = ruleConfigDao.selectEnabledRules();
        return ResponseDTO.ok(rules);
    }

    /**
     * 查询全局规则
     */
    @GetMapping("/global")
    @Operation(summary = "查询全局规则")
    public ResponseDTO<AttendanceRuleConfigEntity> getGlobalRule() {
        log.info("[规则配置] 查询全局规则");

        AttendanceRuleConfigEntity rule = ruleConfigDao.selectGlobalRule();
        return ResponseDTO.ok(rule);
    }

    /**
     * 查询规则详情
     */
    @GetMapping("/{configId}")
    @Operation(summary = "查询规则详情")
    public ResponseDTO<AttendanceRuleConfigEntity> getRuleDetail(@PathVariable Long configId) {
        log.info("[规则配置] 查询规则详情: configId={}", configId);

        AttendanceRuleConfigEntity rule = ruleConfigDao.selectById(configId);
        return ResponseDTO.ok(rule);
    }

    /**
     * 创建规则配置
     */
    @PostMapping
    @Operation(summary = "创建规则配置")
    public ResponseDTO<Long> createRule(@RequestBody AttendanceRuleConfigEntity rule) {
        log.info("[规则配置] 创建规则: ruleName={}, scope={}", rule.getRuleName(), rule.getApplyScope());

        try {
            ruleConfigDao.insert(rule);
            log.info("[规则配置] 规则创建成功: configId={}", rule.getConfigId());
            return ResponseDTO.ok(rule.getConfigId());
        } catch (Exception e) {
            log.error("[规则配置] 创建规则失败: error={}", e.getMessage(), e);
            throw new RuntimeException("创建规则失败: " + e.getMessage());
        }
    }

    /**
     * 更新规则配置
     */
    @PutMapping("/{configId}")
    @Operation(summary = "更新规则配置")
    public ResponseDTO<Void> updateRule(@PathVariable Long configId,
                                         @RequestBody AttendanceRuleConfigEntity rule) {
        log.info("[规则配置] 更新规则: configId={}, ruleName={}", configId, rule.getRuleName());

        try {
            rule.setConfigId(configId);
            ruleConfigDao.updateById(rule);
            log.info("[规则配置] 规则更新成功: configId={}", configId);
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("[规则配置] 更新规则失败: error={}", e.getMessage(), e);
            throw new RuntimeException("更新规则失败: " + e.getMessage());
        }
    }

    /**
     * 启用/禁用规则
     */
    @PutMapping("/{configId}/status")
    @Operation(summary = "启用/禁用规则")
    public ResponseDTO<Void> updateRuleStatus(@PathVariable Long configId,
                                              @RequestParam Integer status) {
        log.info("[规则配置] 更新规则状态: configId={}, status={}", configId, status);

        try {
            AttendanceRuleConfigEntity rule = ruleConfigDao.selectById(configId);
            if (rule == null) {
                throw new RuntimeException("规则不存在");
            }

            rule.setRuleStatus(status);
            ruleConfigDao.updateById(rule);

            log.info("[规则配置] 规则状态更新成功: configId={}", configId);
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("[规则配置] 更新规则状态失败: error={}", e.getMessage(), e);
            throw new RuntimeException("更新规则状态失败: " + e.getMessage());
        }
    }

    /**
     * 删除规则配置
     */
    @DeleteMapping("/{configId}")
    @Operation(summary = "删除规则配置")
    public ResponseDTO<Void> deleteRule(@PathVariable Long configId) {
        log.info("[规则配置] 删除规则: configId={}", configId);

        try {
            // 检查是否为全局规则
            AttendanceRuleConfigEntity rule = ruleConfigDao.selectById(configId);
            if (rule == null) {
                throw new RuntimeException("规则不存在");
            }

            if ("ALL".equals(rule.getApplyScope())) {
                throw new RuntimeException("全局规则不允许删除");
            }

            ruleConfigDao.deleteById(configId);
            log.info("[规则配置] 规则删除成功: configId={}", configId);
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("[规则配置] 删除规则失败: error={}", e.getMessage(), e);
            throw new RuntimeException("删除规则失败: " + e.getMessage());
        }
    }
}
