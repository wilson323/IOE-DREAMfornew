package net.lab1024.sa.common.audit.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.audit.entity.AuditLogEntity;
import net.lab1024.sa.common.audit.service.AuditService;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 审计日志控制器
 * <p>
 * 提供审计日志的创建、查询等功能
 * 严格遵循CLAUDE.md规范：
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 使用@Valid参数校验
 * - 返回统一ResponseDTO格式
 * </p>
 * <p>
 * 业务场景：
 * - 设备协议推送审计日志
 * - 系统操作记录
 * - 合规性审计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/audit/log")
@Tag(name = "审计日志管理", description = "审计日志的创建、查询等API")
public class AuditLogController {

    @Resource
    private AuditService auditService;

    /**
     * 创建审计日志
     * <p>
     * 用于设备协议推送等场景，需要返回日志ID
     * </p>
     *
     * @param auditLog 审计日志实体
     * @return 创建的日志ID
     * @apiNote 示例请求：
     * <pre>
     * POST /api/v1/audit/log/create
     * {
     *   "userId": 1001,
     *   "moduleName": "ACCESS",
     *   "operationType": 2,
     *   "operationDesc": "进入（人脸）",
     *   "resourceType": "1",
     *   "resourceId": "1",
     *   "resultStatus": 1
     * }
     * </pre>
     */
    @PostMapping("/create")
    @Operation(
        summary = "创建审计日志",
        description = "用于设备协议推送等场景，创建审计日志并返回日志ID",
        tags = {"审计日志管理"}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "创建成功",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Long.class)
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "参数错误"
    )
    public ResponseDTO<Long> createAuditLog(@Valid @RequestBody AuditLogEntity auditLog) {
        log.info("[审计日志] 创建审计日志，userId={}, moduleName={}, operationType={}",
                auditLog.getUserId(), auditLog.getModuleName(), auditLog.getOperationType());

        try {
            Long logId = auditService.createAuditLog(auditLog);
            log.info("[审计日志] 审计日志创建成功，logId={}", logId);
            return ResponseDTO.ok(logId);
        } catch (Exception e) {
            log.error("[审计日志] 审计日志创建失败，userId={}", auditLog.getUserId(), e);
            return ResponseDTO.error("CREATE_AUDIT_LOG_ERROR", "创建审计日志失败: " + e.getMessage());
        }
    }
}

