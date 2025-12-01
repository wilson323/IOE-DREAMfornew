/*
 * 安全通知日志实体
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.lab1024.sa.base.common.entity.BaseEntity;

/**
 * 安全通知日志实体
 * 记录所有安全通知的发送日志
 *
 * 严格遵循repowiki规范：继承BaseEntity，不重复定义审计字段
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_security_notification_log")
public class SecurityNotificationLogEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 通知渠道（EMAIL/SMS/PUSH/WECHAT）
     */
    private String channel;

    /**
     * 通知类型
     */
    private String notificationType;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 发送状态（SUCCESS/FAILED/SCHEDULED/CANCELLED）
     */
    private String status;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 消息ID（第三方服务返回的消息ID）
     */
    private String messageId;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 重试时间
     */
    private LocalDateTime retryTime;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 额外数据（JSON格式）
     */
    private String extraData;

    // =================== 重要提示 ===================
    // ❌ 不要重复定义以下审计字段（BaseEntity已包含）:
    // - createTime (创建时间)
    // - updateTime (更新时间)
    // - createUserId (创建人ID)
    // - updateUserId (更新人ID)
    // - deletedFlag (软删除标记)
    // - version (乐观锁版本号)
    //
    // ✅ 如需使用这些字段，直接通过getter/setter访问即可
    // ================================================
}
