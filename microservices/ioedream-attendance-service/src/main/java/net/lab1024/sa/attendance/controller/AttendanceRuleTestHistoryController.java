package net.lab1024.sa.attendance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.lab1024.sa.attendance.domain.form.RuleTestHistoryQueryForm;
import net.lab1024.sa.attendance.domain.vo.RuleTestHistoryDetailVO;
import net.lab1024.sa.attendance.domain.vo.RuleTestHistoryVO;
import net.lab1024.sa.attendance.service.RuleTestHistoryService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;

import java.util.List;

/**
 * 规则测试历史控制器
 * <p>
 * 提供规则测试历史管理相关API接口
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/attendance/rule-test-history")
@Tag(name = "规则测试历史管理")
public class AttendanceRuleTestHistoryController {

    @Resource
    private RuleTestHistoryService ruleTestHistoryService;

    /**
     * 分页查询测试历史
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    @Observed(name = "ruleTestHistory.queryPage", contextualName = "query-history-page")
    @PostMapping("/page")
    @Operation(summary = "分页查询测试历史", description = "根据条件分页查询规则测试历史记录")
    public ResponseDTO<PageResult<RuleTestHistoryVO>> queryHistoryPage(@Valid @RequestBody RuleTestHistoryQueryForm queryForm) {
        log.info("[测试历史] 分页查询测试历史: queryForm={}", queryForm);

        PageResult<RuleTestHistoryVO> result = ruleTestHistoryService.queryHistoryPage(queryForm);

        log.info("[测试历史] 分页查询成功: total={}, pageNum={}, pageSize={}",
                result.getTotal(), result.getPageNum(), result.getPageSize());
        return ResponseDTO.ok(result);
    }

    /**
     * 查询历史详情
     *
     * @param historyId 历史ID
     * @return 历史详情
     */
    @Observed(name = "ruleTestHistory.getDetail", contextualName = "get-history-detail")
    @GetMapping("/{historyId}")
    @Operation(summary = "查询历史详情", description = "根据历史ID查询完整的测试历史详情")
    public ResponseDTO<RuleTestHistoryDetailVO> getHistoryDetail(
            @PathVariable @Parameter(description = "历史ID", required = true) Long historyId) {
        log.info("[测试历史] 查询历史详情: historyId={}", historyId);

        RuleTestHistoryDetailVO detailVO = ruleTestHistoryService.getHistoryDetail(historyId);

        if (detailVO == null) {
            log.warn("[测试历史] 历史记录不存在: historyId={}", historyId);
            return ResponseDTO.error("HISTORY_NOT_FOUND", "历史记录不存在");
        }

        log.info("[测试历史] 查询历史详情成功: historyId={}, testResult={}",
                historyId, detailVO.getTestResult());
        return ResponseDTO.ok(detailVO);
    }

    /**
     * 查询规则的所有测试历史
     *
     * @param ruleId 规则ID
     * @return 测试历史列表
     */
    @Observed(name = "ruleTestHistory.getByRule", contextualName = "get-rule-history")
    @GetMapping("/rule/{ruleId}")
    @Operation(summary = "查询规则的测试历史", description = "查询指定规则的所有测试历史记录")
    public ResponseDTO<List<RuleTestHistoryVO>> getRuleTestHistory(
            @PathVariable @Parameter(description = "规则ID", required = true) Long ruleId) {
        log.info("[测试历史] 查询规则测试历史: ruleId={}", ruleId);

        List<RuleTestHistoryVO> historyList = ruleTestHistoryService.getRuleTestHistory(ruleId);

        log.info("[测试历史] 查询规则测试历史成功: ruleId={}, count={}", ruleId, historyList.size());
        return ResponseDTO.ok(historyList);
    }

    /**
     * 查询最近的测试历史
     *
     * @param limit 限制数量
     * @return 测试历史列表
     */
    @Observed(name = "ruleTestHistory.getRecent", contextualName = "get-recent-history")
    @GetMapping("/recent")
    @Operation(summary = "查询最近的测试历史", description = "查询最近执行的测试历史记录")
    public ResponseDTO<List<RuleTestHistoryVO>> getRecentHistory(
            @RequestParam(defaultValue = "10") @Parameter(description = "限制数量", required = false) Integer limit) {
        log.info("[测试历史] 查询最近测试历史: limit={}", limit);

        List<RuleTestHistoryVO> historyList = ruleTestHistoryService.getRecentHistory(limit);

        log.info("[测试历史] 查询最近测试历史成功: count={}", historyList.size());
        return ResponseDTO.ok(historyList);
    }

    /**
     * 统计规则测试次数
     *
     * @param ruleId 规则ID
     * @return 测试次数
     */
    @Observed(name = "ruleTestHistory.count", contextualName = "count-rule-tests")
    @GetMapping("/count/{ruleId}")
    @Operation(summary = "统计规则测试次数", description = "统计指定规则的测试执行次数")
    public ResponseDTO<Integer> countRuleTests(
            @PathVariable @Parameter(description = "规则ID", required = true) Long ruleId) {
        log.info("[测试历史] 统计规则测试次数: ruleId={}", ruleId);

        Integer count = ruleTestHistoryService.countRuleTests(ruleId);

        log.info("[测试历史] 统计规则测试次数成功: ruleId={}, count={}", ruleId, count);
        return ResponseDTO.ok(count);
    }

    /**
     * 删除测试历史
     *
     * @param historyId 历史ID
     * @return 操作结果
     */
    @Observed(name = "ruleTestHistory.delete", contextualName = "delete-history")
    @DeleteMapping("/{historyId}")
    @Operation(summary = "删除测试历史", description = "删除指定的测试历史记录")
    public ResponseDTO<Void> deleteHistory(
            @PathVariable @Parameter(description = "历史ID", required = true) Long historyId) {
        log.info("[测试历史] 删除测试历史: historyId={}", historyId);

        ruleTestHistoryService.deleteHistory(historyId);

        log.info("[测试历史] 删除测试历史成功: historyId={}", historyId);
        return ResponseDTO.ok();
    }

    /**
     * 批量删除测试历史
     *
     * @param historyIds 历史ID列表
     * @return 操作结果
     */
    @Observed(name = "ruleTestHistory.batchDelete", contextualName = "batch-delete-history")
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除测试历史", description = "批量删除多条测试历史记录")
    public ResponseDTO<Void> batchDeleteHistory(
            @RequestBody @Parameter(description = "历史ID列表", required = true) List<Long> historyIds) {
        log.info("[测试历史] 批量删除测试历史: count={}", historyIds.size());

        ruleTestHistoryService.batchDeleteHistory(historyIds);

        log.info("[测试历史] 批量删除测试历史成功: count={}", historyIds.size());
        return ResponseDTO.ok();
    }

    /**
     * 清理过期历史
     *
     * @param days 保留天数
     * @return 清理数量
     */
    @Observed(name = "ruleTestHistory.cleanup", contextualName = "cleanup-expired-history")
    @DeleteMapping("/cleanup")
    @Operation(summary = "清理过期历史", description = "删除指定天数之前的测试历史记录")
    public ResponseDTO<Integer> cleanExpiredHistory(
            @RequestParam @Parameter(description = "保留天数", required = true) Integer days) {
        log.info("[测试历史] 清理过期历史: days={}", days);

        Integer count = ruleTestHistoryService.cleanExpiredHistory(days);

        log.info("[测试历史] 清理过期历史成功: days={}, count={}", days, count);
        return ResponseDTO.ok(count);
    }

    /**
     * 导出测试历史为JSON文件
     *
     * @param historyIds 历史ID列表
     * @return JSON文件
     */
    @Observed(name = "ruleTestHistory.export", contextualName = "export-history-json")
    @PostMapping("/export")
    @Operation(summary = "导出测试历史", description = "导出选中的测试历史为JSON文件")
    public ResponseDTO<String> exportHistoryToJson(
            @RequestBody @Parameter(description = "历史ID列表", required = true) List<Long> historyIds) {
        log.info("[测试历史] 导出测试历史: count={}", historyIds.size());

        String jsonData = ruleTestHistoryService.exportHistoryToJson(historyIds);

        log.info("[测试历史] 导出测试历史成功: count={}", historyIds.size());
        return ResponseDTO.ok(jsonData);
    }

    /**
     * 导入测试历史从JSON文件
     *
     * @param jsonData JSON数据
     * @return 导入结果
     */
    @Observed(name = "ruleTestHistory.import", contextualName = "import-history-json")
    @PostMapping("/import")
    @Operation(summary = "导入测试历史", description = "从JSON文件导入测试历史记录")
    public ResponseDTO<Integer> importHistoryFromJson(
            @RequestBody @Parameter(description = "JSON数据", required = true) String jsonData) {
        log.info("[测试历史] 导入测试历史: dataLength={}", jsonData != null ? jsonData.length() : 0);

        Integer count = ruleTestHistoryService.importHistoryFromJson(jsonData);

        log.info("[测试历史] 导入测试历史成功: count={}", count);
        return ResponseDTO.ok(count);
    }

    /**
     * 导出测试配置模板
     *
     * @return JSON模板
     */
    @Observed(name = "ruleTestHistory.exportTemplate", contextualName = "export-test-template")
    @GetMapping("/export-template")
    @Operation(summary = "导出测试配置模板", description = "导出测试配置JSON模板文件")
    public ResponseDTO<String> exportTestTemplate() {
        log.info("[测试历史] 导出测试配置模板");

        String template = ruleTestHistoryService.exportTestTemplate();

        log.info("[测试历史] 导出测试配置模板成功");
        return ResponseDTO.ok(template);
    }
}
