package net.lab1024.sa.access.advanced.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.advanced.service.GlobalApbService;
import net.lab1024.sa.access.advanced.domain.form.AntiPassbackRuleForm;
import net.lab1024.sa.access.advanced.domain.vo.AntiPassbackStatusVO;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 全局反潜控制器
 * 严格遵循四层架构规范：
 * - Controller层只负责参数验证和调用Service
 * - 使用统一响应格式ResponseDTO
 * - 权限控制注解@SaCheckPermission
 *
 * @author SmartAdmin Team
 * @since 2025-12-01
 */
@RestController
@RequestMapping("/api/access/advanced/apb")
@Slf4j
public class GlobalApbController {

    @Resource
    private GlobalApbService globalApbService;

    /**
     * 创建反潜规则
     *
     * @param form 反潜规则表单
     * @return 创建结果
     */
    @PostMapping("/rule")
    @SaCheckPermission("access:advanced:apb:rule:create")
    public ResponseDTO<String> createAntiPassbackRule(@Valid @RequestBody AntiPassbackRuleForm form) {
        log.info("[GlobalApbController] 创建反潜规则: ruleName={}, areaId={}, ruleType={}",
                form.getRuleName(), form.getAreaId(), form.getRuleType());

        String result = globalApbService.createAntiPassbackRule(form);
        return ResponseDTO.userOk(result);
    }

    /**
     * 更新反潜规则
     *
     * @param ruleId 规则ID
     * @param form 反潜规则表单
     * @return 更新结果
     */
    @PutMapping("/rule/{ruleId}")
    @SaCheckPermission("access:advanced:apb:rule:update")
    public ResponseDTO<String> updateAntiPassbackRule(@PathVariable Long ruleId,
                                                    @Valid @RequestBody AntiPassbackRuleForm form) {
        log.info("[GlobalApbController] 更新反潜规则: ruleId={}, ruleName={}", ruleId, form.getRuleName());

        String result = globalApbService.updateAntiPassbackRule(ruleId, form);
        return ResponseDTO.userOk(result);
    }

    /**
     * 删除反潜规则
     *
     * @param ruleId 规则ID
     * @return 删除结果
     */
    @DeleteMapping("/rule/{ruleId}")
    @SaCheckPermission("access:advanced:apb:rule:delete")
    public ResponseDTO<String> deleteAntiPassbackRule(@PathVariable Long ruleId) {
        log.info("[GlobalApbController] 删除反潜规则: ruleId={}", ruleId);

        String result = globalApbService.deleteAntiPassbackRule(ruleId);
        return ResponseDTO.userOk(result);
    }

    /**
     * 启用/禁用反潜规则
     *
     * @param ruleId 规则ID
     * @param enabled 是否启用
     * @return 操作结果
     */
    @PostMapping("/rule/{ruleId}/toggle")
    @SaCheckPermission("access:advanced:apb:rule:toggle")
    public ResponseDTO<String> toggleAntiPassbackRule(@PathVariable Long ruleId,
                                                    @RequestParam Boolean enabled) {
        log.info("[GlobalApbController] 切换反潜规则状态: ruleId={}, enabled={}", ruleId, enabled);

        String result = globalApbService.toggleAntiPassbackRule(ruleId, enabled);
        return ResponseDTO.userOk(result);
    }

    /**
     * 获取区域反潜规则列表
     *
     * @param areaId 区域ID
     * @return 规则列表
     */
    @GetMapping("/rules/area/{areaId}")
    @SaCheckPermission("access:advanced:apb:rule:list")
    public ResponseDTO<List<AntiPassbackStatusVO>> getAreaAntiPassbackRules(@PathVariable Long areaId) {
        log.debug("[GlobalApbController] 获取区域反潜规则: areaId={}", areaId);

        List<AntiPassbackStatusVO> rules = globalApbService.getAreaAntiPassbackRules(areaId);
        return ResponseDTO.userOk(rules);
    }

    /**
     * 获取所有反潜规则
     *
     * @return 规则列表
     */
    @GetMapping("/rules")
    @SaCheckPermission("access:advanced:apb:rule:list")
    public ResponseDTO<List<AntiPassbackStatusVO>> getAllAntiPassbackRules() {
        log.debug("[GlobalApbController] 获取所有反潜规则");

        List<AntiPassbackStatusVO> rules = globalApbService.getAllAntiPassbackRules();
        return ResponseDTO.userOk(rules);
    }

    /**
     * 验证反潜规则
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param accessDirection 进出方向 (IN/OUT)
     * @return 验证结果
     */
    @PostMapping("/validate")
    @SaCheckPermission("access:advanced:apb:validate")
    public ResponseDTO<Map<String, Object>> validateAntiPassback(@RequestParam Long userId,
                                                               @RequestParam String deviceId,
                                                               @RequestParam String accessDirection) {
        log.info("[GlobalApbController] 验证反潜规则: userId={}, deviceId={}, direction={}",
                userId, deviceId, accessDirection);

        Map<String, Object> result = globalApbService.validateAntiPassback(userId, deviceId, accessDirection);
        return ResponseDTO.userOk(result);
    }

    /**
     * 清除用户反潜记录
     *
     * @param userId 用户ID
     * @param areaId 区域ID (可选)
     * @return 清除结果
     */
    @PostMapping("/clear-user-record")
    @SaCheckPermission("access:advanced:apb:clear")
    public ResponseDTO<String> clearUserAntiPassbackRecord(@RequestParam Long userId,
                                                         @RequestParam(required = false) Long areaId) {
        log.info("[GlobalApbController] 清除用户反潜记录: userId={}, areaId={}", userId, areaId);

        String result = globalApbService.clearUserAntiPassbackRecord(userId, areaId);
        return ResponseDTO.userOk(result);
    }

    /**
     * 清除所有反潜记录
     *
     * @param areaId 区域ID (可选，为空则清除所有区域)
     * @return 清除结果
     */
    @PostMapping("/clear-all-records")
    @SaCheckPermission("access:advanced:apb:clear-all")
    public ResponseDTO<String> clearAllAntiPassbackRecords(@RequestParam(required = false) Long areaId) {
        log.warn("[GlobalApbController] 清除所有反潜记录: areaId={}", areaId);

        String result = globalApbService.clearAllAntiPassbackRecords(areaId);
        return ResponseDTO.userOk(result);
    }

    /**
     * 获取反潜统计信息
     *
     * @param areaId 区域ID (可选)
     * @param days 统计天数
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @SaCheckPermission("access:advanced:apb:statistics")
    public ResponseDTO<Map<String, Object>> getAntiPassbackStatistics(@RequestParam(required = false) Long areaId,
                                                                    @RequestParam(defaultValue = "7") Integer days) {
        log.debug("[GlobalApbController] 获取反潜统计: areaId={}, days={}", areaId, days);

        Map<String, Object> statistics = globalApbService.getAntiPassbackStatistics(areaId, days);
        return ResponseDTO.userOk(statistics);
    }

    /**
     * 获取用户反潜状态
     *
     * @param userId 用户ID
     * @return 用户反潜状态
     */
    @GetMapping("/user/{userId}/status")
    @SaCheckPermission("access:advanced:apb:user:status")
    public ResponseDTO<Map<String, Object>> getUserAntiPassbackStatus(@PathVariable Long userId) {
        log.debug("[GlobalApbController] 获取用户反潜状态: userId={}", userId);

        Map<String, Object> status = globalApbService.getUserAntiPassbackStatus(userId);
        return ResponseDTO.userOk(status);
    }

    /**
     * 重置反潜计数器
     *
     * @param userId 用户ID
     * @param deviceId 设备ID (可选)
     * @param areaId 区域ID (可选)
     * @return 重置结果
     */
    @PostMapping("/reset-counter")
    @SaCheckPermission("access:advanced:apb:reset")
    public ResponseDTO<String> resetAntiPassbackCounter(@RequestParam Long userId,
                                                      @RequestParam(required = false) String deviceId,
                                                      @RequestParam(required = false) Long areaId) {
        log.info("[GlobalApbController] 重置反潜计数器: userId={}, deviceId={}, areaId={}",
                userId, deviceId, areaId);

        String result = globalApbService.resetAntiPassbackCounter(userId, deviceId, areaId);
        return ResponseDTO.userOk(result);
    }

    /**
     * 获取反潜警报列表
     *
     * @param areaId 区域ID (可选)
     * @param startTime 开始时间 (可选)
     * @param endTime 结束时间 (可选)
     * @return 警报列表
     */
    @GetMapping("/alerts")
    @SaCheckPermission("access:advanced:apb:alerts")
    public ResponseDTO<List<Map<String, Object>>> getAntiPassbackAlerts(@RequestParam(required = false) Long areaId,
                                                                     @RequestParam(required = false) String startTime,
                                                                     @RequestParam(required = false) String endTime) {
        log.debug("[GlobalApbController] 获取反潜警报: areaId={}, startTime={}, endTime={}",
                areaId, startTime, endTime);

        List<Map<String, Object>> alerts = globalApbService.getAntiPassbackAlerts(areaId, startTime, endTime);
        return ResponseDTO.userOk(alerts);
    }

    /**
     * 测试反潜规则
     *
     * @param ruleId 规则ID
     * @param testParameters 测试参数
     * @return 测试结果
     */
    @PostMapping("/rule/{ruleId}/test")
    @SaCheckPermission("access:advanced:apb:rule:test")
    public ResponseDTO<Map<String, Object>> testAntiPassbackRule(@PathVariable Long ruleId,
                                                               @RequestBody Map<String, Object> testParameters) {
        log.info("[GlobalApbController] 测试反潜规则: ruleId={}", ruleId);

        Map<String, Object> testResult = globalApbService.testAntiPassbackRule(ruleId, testParameters);
        return ResponseDTO.userOk(testResult);
    }

    /**
     * 获取反潜规则执行历史
     *
     * @param ruleId 规则ID
     * @param pageSize 页面大小
     * @param pageNum 页码
     * @return 执行历史
     */
    @GetMapping("/rule/{ruleId}/history")
    @SaCheckPermission("access:advanced:apb:rule:history")
    public ResponseDTO<Map<String, Object>> getRuleExecutionHistory(@PathVariable Long ruleId,
                                                                   @RequestParam(defaultValue = "20") Integer pageSize,
                                                                   @RequestParam(defaultValue = "1") Integer pageNum) {
        log.debug("[GlobalApbController] 获取反潜规则执行历史: ruleId={}, pageSize={}, pageNum={}",
                ruleId, pageSize, pageNum);

        Map<String, Object> history = globalApbService.getRuleExecutionHistory(ruleId, pageSize, pageNum);
        return ResponseDTO.userOk(history);
    }
}