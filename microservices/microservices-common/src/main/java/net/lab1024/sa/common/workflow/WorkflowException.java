package net.lab1024.sa.common.workflow;

/**
 * 工作流异常
 * <p>
 * 工作流执行过程中的异常
 * 包含错误代码和详细信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
public class WorkflowException extends RuntimeException {

    private final String errorCode;

    public WorkflowException(String message) {
        super(message);
        this.errorCode = "WORKFLOW_ERROR";
    }

    public WorkflowException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "WORKFLOW_ERROR";
    }

    public WorkflowException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public WorkflowException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 工作流错误代码常量
     */
    public static class ErrorCodes {
        public static final String WORKFLOW_NOT_FOUND = "WORKFLOW_NOT_FOUND";
        public static final String WORKFLOW_INSTANCE_NOT_FOUND = "WORKFLOW_INSTANCE_NOT_FOUND";
        public static final String NODE_EXECUTOR_NOT_FOUND = "NODE_EXECUTOR_NOT_FOUND";
        public static final String NODE_EXECUTION_ERROR = "NODE_EXECUTION_ERROR";
        public static final String CONDITION_EVALUATION_ERROR = "CONDITION_EVALUATION_ERROR";
        public static final String WORKFLOW_CONFIGURATION_ERROR = "WORKFLOW_CONFIGURATION_ERROR";
        public static final String TIMEOUT_ERROR = "TIMEOUT_ERROR";
        public static final String PARALLEL_EXECUTION_ERROR = "PARALLEL_EXECUTION_ERROR";
        public static final String DATA_VALIDATION_ERROR = "DATA_VALIDATION_ERROR";
        public static final String NOTIFICATION_ERROR = "NOTIFICATION_ERROR";
    }
}