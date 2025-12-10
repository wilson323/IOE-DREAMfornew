package net.lab1024.sa.common.audit.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 配置变更查询表单
 * <p>
 * 用于配置变更记录的条件查询，支持多维度筛选：
 * - 时间范围筛选
 * - 配置类型和变更类型筛选
 * - 操作人筛选
 * - 风险等级和状态筛选
 * - 关键词搜索
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-09
 */
@Data
@Schema(description = "配置变更查询表单")
public class ConfigChangeQueryForm {

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "配置类型", example = "SYSTEM_CONFIG")
    private String configType;

    @Schema(description = "变更类型", example = "UPDATE")
    private String changeType;

    @Schema(description = "配置键", example = "system.app.name")
    private String configKey;

    @Schema(description = "配置名称", example = "应用名称")
    private String configName;

    @Schema(description = "配置分组", example = "system")
    private String configGroup;

    @Schema(description = "操作用户ID")
    private Long operatorId;

    @Schema(description = "操作用户名", example = "admin")
    private String operatorName;

    @Schema(description = "操作用户角色", example = "ADMIN")
    private String operatorRole;

    @Schema(description = "客户端IP", example = "192.168.1.100")
    private String clientIp;

    @Schema(description = "变更批次ID", example = "BATCH_1701234567890")
    private String changeBatchId;

    @Schema(description = "风险等级", example = "HIGH")
    @Pattern(regexp = "^(LOW|MEDIUM|HIGH|CRITICAL)$", message = "风险等级必须是LOW、MEDIUM、HIGH或CRITICAL")
    private String riskLevel;

    @Schema(description = "变更状态", example = "SUCCESS")
    @Pattern(regexp = "^(PENDING|EXECUTING|SUCCESS|FAILED|ROLLED_BACK)$", message = "变更状态必须是PENDING、EXECUTING、SUCCESS、FAILED或ROLLED_BACK")
    private String changeStatus;

    @Schema(description = "是否需要审批", example = "1")
    private Integer requireApproval;

    @Schema(description = "审批人ID")
    private Long approverId;

    @Schema(description = "是否为敏感配置", example = "0")
    private Integer isSensitive;

    @Schema(description = "影响范围", example = "SYSTEM")
    @Pattern(regexp = "^(SINGLE|MODULE|SYSTEM|GLOBAL)$", message = "影响范围必须是SINGLE、MODULE、SYSTEM或GLOBAL")
    private String impactScope;

    @Schema(description = "变更来源", example = "WEB")
    @Pattern(regexp = "^(WEB|API|BATCH|IMPORT|SYSTEM|MIGRATION)$", message = "变更来源必须是WEB、API、BATCH、IMPORT、SYSTEM或MIGRATION")
    private String changeSource;

    @Schema(description = "业务模块", example = "system")
    private String businessModule;

    @Schema(description = "通知状态", example = "SENT")
    @Pattern(regexp = "^(NOT_SENT|SENT|FAILED|READ)$", message = "通知状态必须是NOT_SENT、SENT、FAILED或READ")
    private String notificationStatus;

    @Schema(description = "关键词搜索（搜索配置名称、配置键、变更原因等）")
    private String keyword;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "20")
    private Integer pageSize = 20;

    @Schema(description = "排序字段", example = "change_time")
    private String orderBy = "change_time";

    @Schema(description = "排序方向", example = "DESC")
    @Pattern(regexp = "^(ASC|DESC)$", message = "排序方向必须是ASC或DESC")
    private String orderDirection = "DESC";
}