package net.lab1024.sa.device.comm.protocol.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 协议错误信息
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProtocolErrorInfo {

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误描述
     */
    private String errorDescription;

    /**
     * 错误类型 (NETWORK, PROTOCOL, BUSINESS, SYSTEM)
     */
    private String errorType;

    /**
     * 严重程度 (LOW, MEDIUM, HIGH, CRITICAL)
     */
    private String severity;

    /**
     * 是否可重试
     */
    private boolean retryable;

    /**
     * 恢复策略
     */
    private String recoveryStrategy;

    /**
     * 详细说明
     */
    private String details;
}
