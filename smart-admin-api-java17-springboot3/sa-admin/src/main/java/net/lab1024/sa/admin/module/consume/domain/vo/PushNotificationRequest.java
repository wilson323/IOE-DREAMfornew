/*
 * 推送通知请求
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.domain.vo;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.admin.module.consume.domain.enums.PushPriority;

/**
 * 推送通知请求
 * 封装App推送的请求数据
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushNotificationRequest {

    /**
     * 设备令牌列表
     */
    @NotEmpty(message = "设备令牌不能为空")
    private List<String> deviceTokens;

    /**
     * 推送标题
     */
    @NotBlank(message = "推送标题不能为空")
    private String title;

    /**
     * 推送内容
     */
    @NotBlank(message = "推送内容不能为空")
    private String content;

    /**
     * 推送副标题（可选）
     */
    private String subtitle;

    /**
     * 点击跳转URL
     */
    private String actionUrl;

    /**
     * 额外数据（透传参数）
     */
    private Map<String, Object> extraData;

    /**
     * 推送优先级
     */
    private PushPriority priority;

    /**
     * 过期时间（秒）
     */
    private Integer ttl;

    /**
     * 是否显示在通知栏
     */
    private boolean showInNotification;

    /**
     * 是否震动
     */
    private boolean vibrate;

    /**
     * 是否响铃
     */
    private boolean sound;

    /**
     * 声音文件名（可选）
     */
    private String soundFile;

    /**
     * 图标URL
     */
    private String iconUrl;

    /**
     * 大图片URL
     */
    private String imageUrl;

    /**
     * 推送类型
     */
    private String pushType;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 请求ID（用于追踪）
     */
    private String requestId;

    /**
     * 发送时间（定时发送，null表示立即发送）
     */
    private java.time.LocalDateTime sendTime;

    /**
     * 标签列表（按标签推送时使用）
     */
    private List<String> tags;

    /**
     * 用户ID列表（按用户推送时使用）
     */
    private List<Long> userIds;

    /**
     * 是否为静默推送
     */
    private boolean silentPush;

    /**
     * 推送平台（ANDROID/IOS/ALL）
     */
    private String platform;

    /**
     * 自定义参数
     */
    private Map<String, String> customParams;

    /**
     * 验证推送请求参数
     */
    public boolean isValid() {
        // 检查设备令牌
        if (deviceTokens == null || deviceTokens.isEmpty()) {
            // 如果没有设备令牌，检查是否有标签或用户ID
            if ((tags == null || tags.isEmpty()) && (userIds == null || userIds.isEmpty())) {
                return false;
            }
        }

        // 检查标题和内容（静默推送除外）
        if (!silentPush) {
            if (title == null || title.trim().isEmpty()) {
                return false;
            }

            if (content == null || content.trim().isEmpty()) {
                return false;
            }

            // 检查内容长度限制
            if (title.length() > 50 || content.length() > 200) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检查是否为多设备推送
     */
    public boolean isMultiDevicePush() {
        return deviceTokens != null && deviceTokens.size() > 1;
    }

    /**
     * 检查是否为标签推送
     */
    public boolean isTagPush() {
        return tags != null && !tags.isEmpty();
    }

    /**
     * 检查是否为用户推送
     */
    public boolean isUserPush() {
        return userIds != null && !userIds.isEmpty();
    }

    /**
     * 检查是否为定时发送
     */
    public boolean isScheduledSend() {
        return sendTime != null && sendTime.isAfter(java.time.LocalDateTime.now());
    }

    /**
     * 获取目标设备数量
     */
    public int getTargetDeviceCount() {
        if (isMultiDevicePush()) {
            return deviceTokens.size();
        } else if (isUserPush()) {
            return userIds.size();
        } else if (deviceTokens != null && !deviceTokens.isEmpty()) {
            return 1;
        } else {
            return 0; // 标签推送无法确定具体数量
        }
    }

    /**
     * 创建简单推送请求
     */
    public static PushNotificationRequest createSimple(String deviceToken, String title, String content) {
        return PushNotificationRequest.builder()
                .deviceTokens(java.util.Arrays.asList(deviceToken))
                .title(title)
                .content(content)
                .priority(PushPriority.NORMAL)
                .showInNotification(true)
                .vibrate(true)
                .sound(true)
                .platform("ALL")
                .build();
    }

    /**
     * 创建安全通知推送请求
     */
    public static PushNotificationRequest createSecurityNotification(String deviceToken, String title, String content,
            String actionUrl) {
        return PushNotificationRequest.builder()
                .deviceTokens(java.util.Arrays.asList(deviceToken))
                .title("【安全通知】" + title)
                .content(content)
                .actionUrl(actionUrl)
                .priority(PushPriority.HIGH)
                .showInNotification(true)
                .vibrate(true)
                .sound(true)
                .ttl(3600) // 1小时过期
                .businessType("SECURITY_NOTIFICATION")
                .platform("ALL")
                .build();
    }

    /**
     * 创建静默推送请求
     */
    public static PushNotificationRequest createSilentPush(String deviceToken, Map<String, Object> extraData) {
        return PushNotificationRequest.builder()
                .deviceTokens(java.util.Arrays.asList(deviceToken))
                .silentPush(true)
                .extraData(extraData)
                .priority(PushPriority.LOW)
                .showInNotification(false)
                .platform("ALL")
                .build();
    }

    /**
     * 创建广播推送请求
     */
    public static PushNotificationRequest createBroadcast(String title, String content) {
        return PushNotificationRequest.builder()
                .title(title)
                .content(content)
                .priority(PushPriority.NORMAL)
                .showInNotification(true)
                .platform("ALL")
                .build();
    }

    /**
     * 创建标签推送请求
     */
    public static PushNotificationRequest createTagPush(List<String> tags, String title, String content) {
        return PushNotificationRequest.builder()
                .tags(tags)
                .title(title)
                .content(content)
                .priority(PushPriority.NORMAL)
                .showInNotification(true)
                .platform("ALL")
                .build();
    }

    /**
     * 获取推送摘要
     */
    public String getPushSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("推送请求摘要:\n");

        if (isMultiDevicePush()) {
            summary.append(String.format("目标设备数: %d\n", deviceTokens.size()));
        } else if (isUserPush()) {
            summary.append(String.format("目标用户数: %d\n", userIds.size()));
        } else if (isTagPush()) {
            summary.append(String.format("标签: %s\n", String.join(",", tags)));
        }

        summary.append(String.format("标题: %s\n", title));
        summary.append(String.format("内容: %s\n", content.length() > 50 ? content.substring(0, 50) + "..." : content));
        summary.append(String.format("优先级: %s\n", priority != null ? priority.name() : "NORMAL"));
        summary.append(String.format("平台: %s\n", platform != null ? platform : "ALL"));

        if (actionUrl != null) {
            summary.append(String.format("跳转链接: %s\n", actionUrl));
        }

        if (extraData != null && !extraData.isEmpty()) {
            summary.append(String.format("额外数据: %d项\n", extraData.size()));
        }

        return summary.toString();
    }
}
