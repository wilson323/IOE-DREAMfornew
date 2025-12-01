/*
 * 用户通知偏好实体
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 用户通知偏好实体
 * 存储用户的通知接收偏好设置
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@Builder
@TableName("t_user_notification_preference")
public class UserNotificationPreferenceEntity {

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
     * 是否启用邮件通知
     */
    private Boolean emailEnabled;

    /**
     * 是否启用短信通知
     */
    private Boolean smsEnabled;

    /**
     * 是否启用推送通知
     */
    private Boolean pushEnabled;

    /**
     * 是否启用微信通知
     */
    private Boolean wechatEnabled;

    /**
     * 通知类型列表（逗号分隔）
     */
    private String notificationTypes;

    /**
     * 免打扰时间段开始（小时，0-23）
     */
    private Integer doNotDisturbStartHour;

    /**
     * 免打扰时间段结束（小时，0-23）
     */
    private Integer doNotDisturbEndHour;

    /**
     * 是否启用免打扰
     */
    private Boolean doNotDisturbEnabled;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}