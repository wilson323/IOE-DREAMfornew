package net.lab1024.sa.admin.module.consume.domain.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 邮件服务状态枚举
 *
 * <p>定义邮件服务在处理邮件过程中的各种状态，包括待处理、发送中、成功、失败、重试等状态。
 * 用于邮件发送、通知推送等功能的统一状态管理。</p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-28
 */
@Getter
@Schema(description = "邮件服务状态枚举")
public enum EmailServiceStatus implements BaseEnum {

    /**
     * 待处理状态
     *
     * <p>邮件已创建，等待系统处理发送</p>
     */
    PENDING("PENDING", "待处理", 1),

    /**
     * 发送中状态
     *
     * <p>邮件正在发送过程中，系统正在与邮件服务器通信</p>
     */
    SENDING("SENDING", "发送中", 2),

    /**
     * 发送成功状态
     *
     * <p>邮件已成功发送到目标邮箱</p>
     */
    SUCCESS("SUCCESS", "发送成功", 3),

    /**
     * 发送失败状态
     *
     * <p>邮件发送失败，需要检查错误信息</p>
     */
    FAILED("FAILED", "发送失败", 4),

    /**
     * 重试状态
     *
     * <p>邮件发送失败，正在等待重试</p>
     */
    RETRY("RETRY", "重试中", 5),

    /**
     * 已取消状态
     *
     * <p>邮件发送已被取消</p>
     */
    CANCELLED("CANCELLED", "已取消", 6),

    /**
     * 部分成功状态
     *
     * <p>批量邮件中部分发送成功，部分失败</p>
     */
    PARTIAL_SUCCESS("PARTIAL_SUCCESS", "部分成功", 7);

    private final String value;
    private final String description;
    private final int priority;

    EmailServiceStatus(String value, String description, int priority) {
        this.value = value;
        this.description = description;
        this.priority = priority;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getDesc() {
        return description;
    }

    /**
     * 根据值获取枚举
     *
     * @param value 枚举值
     * @return 对应的枚举，如果不存在则返回null
     */
    public static EmailServiceStatus fromValue(String value) {
        for (EmailServiceStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据优先级获取枚举
     *
     * @param priority 优先级数值
     * @return 对应的枚举，如果不存在则返回null
     */
    public static EmailServiceStatus fromPriority(int priority) {
        for (EmailServiceStatus status : values()) {
            if (status.getPriority() == priority) {
                return status;
            }
        }
        return null;
    }

    /**
     * 获取优先级数值
     *
     * @return 优先级数值
     */
    public int getPriority() {
        return priority;
    }

    /**
     * 判断是否为最终状态
     *
     * <p>最终状态包括：SUCCESS、FAILED、CANCELLED、PARTIAL_SUCCESS</p>
     *
     * @return true如果是最终状态
     */
    public boolean isFinalStatus() {
        return this == SUCCESS || this == FAILED || this == CANCELLED || this == PARTIAL_SUCCESS;
    }

    /**
     * 判断是否为处理中状态
     *
     * <p>处理中状态包括：PENDING、SENDING、RETRY</p>
     *
     * @return true如果是处理中状态
     */
    public boolean isProcessingStatus() {
        return this == PENDING || this == SENDING || this == RETRY;
    }

    /**
     * 判断是否为成功状态
     *
     * <p>成功状态包括：SUCCESS、PARTIAL_SUCCESS</p>
     *
     * @return true如果是成功状态
     */
    public boolean isSuccessStatus() {
        return this == SUCCESS || this == PARTIAL_SUCCESS;
    }

    /**
     * 判断是否为失败状态
     *
     * <p>失败状态包括：FAILED、CANCELLED</p>
     *
     * @return true如果是失败状态
     */
    public boolean isFailureStatus() {
        return this == FAILED || this == CANCELLED;
    }

    /**
     * 判断是否可以重试
     *
     * <p>可以重试的状态：FAILED、RETRY</p>
     *
     * @return true如果可以重试
     */
    public boolean canRetry() {
        return this == FAILED || this == RETRY;
    }

    /**
     * 获取状态颜色代码
     *
     * @return 十六进制颜色代码
     */
    public String getColorCode() {
        switch (this) {
            case PENDING:
                return "#8C8C8C";    // 灰色
            case SENDING:
            case RETRY:
                return "#1890FF";    // 蓝色
            case SUCCESS:
                return "#52C41A";    // 绿色
            case FAILED:
            case CANCELLED:
                return "#F5222D";    // 红色
            case PARTIAL_SUCCESS:
                return "#FAAD14";    // 橙色
            default:
                return "#8C8C8C";
        }
    }

    /**
     * 获取状态图标
     *
     * @return 状态图标Unicode字符
     */
    public String getIcon() {
        switch (this) {
            case PENDING:
                return "⏳";
            case SENDING:
                return "📤";
            case SUCCESS:
                return "✅";
            case FAILED:
                return "❌";
            case RETRY:
                return "🔄";
            case CANCELLED:
                return "⏹️";
            case PARTIAL_SUCCESS:
                return "⚠️";
            default:
                return "❓";
        }
    }

    /**
     * 获取建议的超时时间（分钟）
     *
     * @return 建议超时时间
     */
    public int getTimeoutMinutes() {
        switch (this) {
            case PENDING:
                return 5;      // 5分钟内开始处理
            case SENDING:
                return 10;     // 10分钟内完成发送
            case RETRY:
                return 30;     // 30分钟内完成重试
            case SUCCESS:
            case FAILED:
            case CANCELLED:
            case PARTIAL_SUCCESS:
                return 0;      // 已完成，不需要超时
            default:
                return 10;
        }
    }

    /**
     * 获取重试间隔（秒）
     *
     * @return 重试间隔时间
     */
    public int getRetryInterval() {
        switch (this) {
            case FAILED:
                return 60;      // 1分钟后重试
            case RETRY:
                return 300;     // 5分钟后重试
            default:
                return 0;       // 其他状态不重试
        }
    }

    /**
     * 获取最大重试次数
     *
     * @return 最大重试次数
     */
    public int getMaxRetryCount() {
        switch (this) {
            case FAILED:
            case RETRY:
                return 3;       // 最多重试3次
            default:
                return 0;       // 其他状态不重试
        }
    }

    /**
     * 获取状态描述（详细）
     *
     * @return 详细的状态描述
     */
    public String getDetailedDescription() {
        switch (this) {
            case PENDING:
                return "邮件已创建，等待系统处理发送";
            case SENDING:
                return "邮件正在发送过程中，请耐心等待";
            case SUCCESS:
                return "邮件已成功发送到目标邮箱";
            case FAILED:
                return "邮件发送失败，请检查邮箱地址和网络连接";
            case RETRY:
                return "邮件发送失败，系统正在自动重试";
            case CANCELLED:
                return "邮件发送已被用户或系统取消";
            case PARTIAL_SUCCESS:
                return "批量邮件中部分发送成功，部分失败";
            default:
                return "未知状态";
        }
    }

    /**
     * 获取操作建议
     *
     * @return 操作建议
     */
    public String getActionSuggestion() {
        switch (this) {
            case PENDING:
                return "请稍等，系统将自动处理";
            case SENDING:
                return "发送中，请勿重复操作";
            case SUCCESS:
                return "邮件已发送，请查收";
            case FAILED:
                return "请检查网络连接和邮箱地址后重试";
            case RETRY:
                return "系统正在自动重试，请耐心等待";
            case CANCELLED:
                return "如需发送，请重新提交";
            case PARTIAL_SUCCESS:
                return "请查看失败邮件的详细错误信息";
            default:
                return "请联系管理员";
        }
    }
}