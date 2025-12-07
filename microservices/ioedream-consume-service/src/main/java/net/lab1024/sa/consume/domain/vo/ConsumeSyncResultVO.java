package net.lab1024.sa.consume.domain.vo;

import lombok.Data;

/**
 * 消费同步结果VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeSyncResultVO {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 消息
     */
    private String message;

    /**
     * 同步数量
     */
    private Integer syncCount;

    /**
     * 失败数量
     */
    private Integer failCount;
}
