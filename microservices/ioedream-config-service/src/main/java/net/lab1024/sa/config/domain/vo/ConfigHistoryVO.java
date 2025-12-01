package net.lab1024.sa.config.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 配置历史VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
public class ConfigHistoryVO {

    /**
     * 历史ID
     */
    private Long historyId;

    /**
     * 配置ID
     */
    private Long configId;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 旧值
     */
    private String oldValue;

    /**
     * 新值
     */
    private String newValue;

    /**
     * 变更类型
     */
    private String changeType; // CREATE, UPDATE, DELETE, ROLLBACK

    /**
     * 变更人
     */
    private String changedBy;

    /**
     * 变更时间
     */
    private LocalDateTime changeTime;

    /**
     * 版本号
     */
    private Long version;

    /**
     * 变更原因
     */
    private String changeReason;
}