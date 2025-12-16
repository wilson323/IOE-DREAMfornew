package net.lab1024.sa.oa.workflow.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流程部署异常
 * <p>
 * 专门用于流程部署过程中的异常处理
 * 提供详细的部署阶段信息和错误上下文
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProcessDeploymentException extends RuntimeException {

    /**
     * 流程Key
     */
    private String processKey;

    /**
     * 流程名称
     */
    private String processName;

    /**
     * 部署阶段
     */
    private String deploymentStage;

    /**
     * BPMN内容（可能为空，出于安全考虑）
     */
    private String bpmnContent;

    /**
     * 部署版本
     */
    private Integer version;

    /**
     * 部署者ID
     */
    private Long deployerId;

    // ==================== 部署阶段常量 ====================

    public static final String STAGE_VALIDATION = "VALIDATION"; // XML验证阶段
    public static final String STAGE_PARSING = "PARSING"; // BPMN解析阶段
    public static final String STAGE_DEPLOYMENT = "DEPLOYMENT"; // 部署到引擎阶段
    public static final String STAGE_DATABASE = "DATABASE"; // 数据库存储阶段
    public static final String STAGE_ACTIVATION = "ACTIVATION"; // 激活阶段

    // ==================== 构造方法 ====================

    public ProcessDeploymentException(String processKey, String deploymentStage, String message, Throwable cause) {
        super(message, cause);
        this.processKey = processKey;
        this.deploymentStage = deploymentStage;
    }

    public ProcessDeploymentException(String processKey, String processName, String deploymentStage, String message, Throwable cause) {
        super(message, cause);
        this.processKey = processKey;
        this.processName = processName;
        this.deploymentStage = deploymentStage;
    }

    public ProcessDeploymentException(String processKey, String processName, String deploymentStage, Integer version, Long deployerId, String message, Throwable cause) {
        super(message, cause);
        this.processKey = processKey;
        this.processName = processName;
        this.deploymentStage = deploymentStage;
        this.version = version;
        this.deployerId = deployerId;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * XML验证失败
     */
    public static ProcessDeploymentException validationError(String processKey, String xmlError) {
        return new ProcessDeploymentException(processKey, STAGE_VALIDATION,
                "BPMN XML验证失败: " + xmlError, null);
    }

    /**
     * BPMN解析失败
     */
    public static ProcessDeploymentException parsingError(String processKey, String parsingError) {
        return new ProcessDeploymentException(processKey, STAGE_PARSING,
                "BPMN解析失败: " + parsingError, null);
    }

    /**
     * 部署到引擎失败
     */
    public static ProcessDeploymentException deploymentError(String processKey, String deploymentError, Throwable cause) {
        return new ProcessDeploymentException(processKey, STAGE_DEPLOYMENT,
                "流程部署失败: " + deploymentError, cause);
    }

    /**
     * 数据库存储失败
     */
    public static ProcessDeploymentException databaseError(String processKey, String dbError, Throwable cause) {
        return new ProcessDeploymentException(processKey, STAGE_DATABASE,
                "数据库存储失败: " + dbError, cause);
    }

    /**
     * 流程激活失败
     */
    public static ProcessDeploymentException activationError(String processKey, String activationError, Throwable cause) {
        return new ProcessDeploymentException(processKey, STAGE_ACTIVATION,
                "流程激活失败: " + activationError, cause);
    }

    /**
     * 版本冲突
     */
    public static ProcessDeploymentException versionConflict(String processKey, Integer conflictVersion) {
        return new ProcessDeploymentException(processKey, STAGE_DEPLOYMENT,
                "版本冲突，已存在版本: " + conflictVersion, null);
    }
}