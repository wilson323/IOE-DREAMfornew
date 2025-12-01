package net.lab1024.sa.audit.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户活跃度统计VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
@Accessors(chain = true)
public class UserActivityStatisticsVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 操作数量
     */
    private Long operationCount;

    /**
     * 成功操作数
     */
    private Long successCount;

    /**
     * 失败操作数
     */
    private Long failureCount;
}
