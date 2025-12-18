package net.lab1024.sa.common.exception;

import lombok.Getter;

/**
 * AI分析异常
 * <p>
 * 用于AI分析相关操作的异常处理
 * 严格遵循CLAUDE.md规范：统一异常处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Getter
public class AIAnalysisException extends BusinessException {

    /**
     * 分析类型
     */
    private final String analysisType;

    /**
     * 构造函数
     *
     * @param analysisType 分析类型
     * @param errorCode 错误码
     * @param message 错误消息
     */
    public AIAnalysisException(String analysisType, String errorCode, String message) {
        super(errorCode, message);
        this.analysisType = analysisType;
    }

    /**
     * 构造函数（带原因）
     *
     * @param analysisType 分析类型
     * @param errorCode 错误码
     * @param message 错误消息
     * @param cause 原因
     */
    public AIAnalysisException(String analysisType, String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
        this.analysisType = analysisType;
    }
}
