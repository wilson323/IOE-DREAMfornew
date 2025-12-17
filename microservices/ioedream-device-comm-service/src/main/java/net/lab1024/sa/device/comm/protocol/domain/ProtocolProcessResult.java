package net.lab1024.sa.device.comm.protocol.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 协议处理结果
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProtocolProcessResult {

    /**
     * 处理是否成功
     */
    private boolean success;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 处理结果数据
     */
    private Map<String, Object> resultData;

    /**
     * 消息
     */
    private String message;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    public static ProtocolProcessResult success(String businessType, Map<String, Object> resultData) {
        return ProtocolProcessResult.builder()
                .success(true)
                .businessType(businessType)
                .resultData(resultData)
                .message("处理成功")
                .build();
    }

    public static ProtocolProcessResult failure(String businessType, String errorCode, String errorMessage) {
        return ProtocolProcessResult.builder()
                .success(false)
                .businessType(businessType)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }
}
