package net.lab1024.sa.audit.controller;

import java.util.Map;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.audit.domain.entity.AuditLogEntity;
import net.lab1024.sa.audit.domain.form.AuditLogExportForm;
import net.lab1024.sa.audit.domain.form.AuditLogQueryForm;
import net.lab1024.sa.audit.domain.form.AuditStatisticsQueryForm;
import net.lab1024.sa.audit.domain.form.ComplianceReportQueryForm;
import net.lab1024.sa.audit.domain.vo.AuditLogVO;
import net.lab1024.sa.audit.domain.vo.AuditStatisticsVO;
import net.lab1024.sa.audit.domain.vo.ComplianceReportVO;
import net.lab1024.sa.audit.service.AuditService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.util.SmartBeanUtil;

/**
 * 审计控制器
 * 提供审计日志查询、统计分析、合规报告等功能
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/audit")
@Validated
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * 查询审计日志（分页）
     */
    @PostMapping("/logs/page")
    public ResponseDTO<PageResult<AuditLogVO>> queryAuditLogPage(@RequestBody @Valid AuditLogQueryForm queryForm) {
        log.info("查询审计日志分页，用户: {}, 模块: {}", queryForm.getUserId(), queryForm.getModuleName());
        return auditService.queryAuditLogPage(queryForm);
    }

    /**
     * 获取审计详情
     */
    @GetMapping("/log/{auditId}")
    public ResponseDTO<AuditLogVO> getAuditDetail(@PathVariable @NotNull Long auditId) {
        log.info("获取审计详情，ID: {}", auditId);
        return auditService.getAuditLogDetail(auditId);
    }

    /**
     * 记录审计日志
     */
    @PostMapping("/log")
    public ResponseDTO<Void> recordAuditLog(@RequestBody AuditLogVO auditLogVO) {
        log.info("记录审计日志，用户: {}, 操作: {}", auditLogVO.getUserId(), auditLogVO.getOperation());

        // 转换为Entity
        AuditLogEntity auditLog = SmartBeanUtil.copy(auditLogVO, AuditLogEntity.class);
        auditService.recordAuditLog(auditLog);

        log.info("审计日志记录成功");
        return ResponseDTO.ok();
    }

    /**
     * 获取审计统计
     */
    @PostMapping("/statistics")
    public ResponseDTO<AuditStatisticsVO> getAuditStatistics(@RequestBody @Valid AuditStatisticsQueryForm queryForm) {
        log.info("获取审计统计，开始时间: {}, 结束时间: {}", queryForm.getStartTime(), queryForm.getEndTime());
        return auditService.getAuditStatistics(queryForm);
    }

    /**
     * 生成合规报告
     */
    @PostMapping("/compliance/report")
    public ResponseDTO<ComplianceReportVO> generateComplianceReport(
            @RequestBody @Valid ComplianceReportQueryForm queryForm) {
        log.info("生成合规报告，类型: {}, 开始时间: {}, 结束时间: {}",
                queryForm.getReportType(), queryForm.getStartTime(), queryForm.getEndTime());
        return auditService.generateComplianceReport(queryForm);
    }

    /**
     * 导出审计日志
     */
    @PostMapping("/export")
    public ResponseDTO<String> exportAuditLogs(@RequestBody @Valid AuditLogExportForm exportForm) {
        log.info("导出审计日志，格式: {}", exportForm.getExportFormat());
        return auditService.exportAuditLogs(exportForm);
    }

    /**
     * 清理过期审计日志
     */
    @PostMapping("/clean/expired")
    public ResponseDTO<String> cleanExpiredAuditLogs(@RequestParam(defaultValue = "365") int retentionDays) {
        log.info("清理过期审计日志，保留天数: {}", retentionDays);
        return auditService.cleanExpiredAuditLogs(retentionDays);
    }

    /**
     * 获取审计服务健康状态
     */
    @GetMapping("/health")
    public ResponseDTO<Map<String, Object>> healthCheck() {
        Map<String, Object> health = Map.of(
                "status", "UP",
                "service", "ioedream-audit-service",
                "port", 8096);
        return ResponseDTO.ok(health);
    }
}
