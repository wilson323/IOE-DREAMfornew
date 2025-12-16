package net.lab1024.sa.consume.domain.vo;

import lombok.Data;

/**
 * 消费权限验证结果VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeValidateResultVO {

    /**
     * 是否有效
     */
    private Boolean valid;

    /**
     * 验证消息
     */
    private String message;

    /**
     * 拒绝原因
     */
    private String denyReason;
}



