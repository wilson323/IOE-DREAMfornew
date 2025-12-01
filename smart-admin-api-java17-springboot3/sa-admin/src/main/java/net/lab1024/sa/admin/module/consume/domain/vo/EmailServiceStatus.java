/*
 * 邮件服务状态
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.domain.vo;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邮件服务状态
 * 封装邮件服务的状态信息
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailServiceStatus {

    /**
     * 服务是否可用
     */
    private boolean available;

    /**
     * 服务提供商
     */
    private String serviceProvider;

    /**
     * 连接状态
     */
    private String connectionStatus;

    /**
     * 最后检查时间
     */
    private LocalDateTime lastCheckTime;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTimeMs;

    /**
     * 状态描述
     */
    private String statusDescription;
}
