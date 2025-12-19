package net.lab1024.sa.device.comm.protocol.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 协议验证结果
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProtocolValidationResult {

    /**
     * 验证是否通过
     */
    private boolean valid;

    /**
     * 验证是否允许（别名）
     */
    private boolean allowed;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 验证失败的字段列表
     */
    @Builder.Default
    private List<String> failedFields = new ArrayList<>();

    /**
     * 详细验证信息
     */
    private String details;

    /**
     * 验证详情（别名）
     */
    private String validationDetails;

    /**
     * 创建成功的验证结果
     */
    public static ProtocolValidationResult success() {
        return ProtocolValidationResult.builder()
                .valid(true)
                .build();
    }

    /**
     * 创建失败的验证结果
     */
    public static ProtocolValidationResult failure(String errorCode, String errorMessage) {
        return ProtocolValidationResult.builder()
                .valid(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }
}
