package net.lab1024.sa.consume.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 离线消费记录实体类
 * <p>
 * 根据chonggou.txt和业务模块文档要求创建
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 字段设计全面，支持完整业务流程
 * </p>
 * <p>
 * 业务场景：
 * - 设备端离线消费记录（网络中断时的消费记录）
 * - 离线记录同步（网络恢复后同步到服务器）
 * - 冲突检测和处理（余额不一致、重复消费等）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_offline_consume_record")
@Schema(description = "离线消费记录实体")
public class OfflineConsumeRecordEntity extends BaseEntity {

    /**
     * 记录ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "记录ID")
    private Long id;

    /**
     * 离线交易编号（设备端生成，唯一）
     */
    @TableField("offline_trans_no")
    @Schema(description = "离线交易编号")
    private String offlineTransNo;

    /**
     * 设备ID
     */
    @TableField("device_id")
    @Schema(description = "设备ID")
    private Long deviceId;

    /**
     * 设备序列号
     */
    @TableField("device_sn")
    @Schema(description = "设备序列号")
    private String deviceSn;

    /**
     * 用户ID
     */
    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 账户ID
     * <p>
     * 根据chonggou.txt要求添加
     * </p>
     */
    @TableField("account_id")
    @Schema(description = "账户ID")
    private Long accountId;

    /**
     * 卡号
     */
    @TableField("card_no")
    @Schema(description = "卡号")
    private String cardNo;

    /**
     * 消费金额
     */
    @TableField("amount")
    @Schema(description = "消费金额")
    private BigDecimal amount;

    /**
     * 离线时账户余额
     */
    @TableField("balance_before_consume")
    @Schema(description = "离线时账户余额")
    private BigDecimal balanceBeforeConsume;

    /**
     * 离线消费后余额
     */
    @TableField("balance_after_consume")
    @Schema(description = "离线消费后余额")
    private BigDecimal balanceAfterConsume;

    /**
     * 消费时间（设备端记录）
     */
    @TableField("consume_time")
    @Schema(description = "消费时间")
    private LocalDateTime consumeTime;

    /**
     * 同步状态
     * <p>
     * 根据chonggou.txt要求添加
     * PENDING-待同步, SYNCED-已同步, CONFLICT-冲突, FAILED-失败
     * </p>
     */
    @TableField("sync_status")
    @Schema(description = "同步状态")
    private String syncStatus;

    /**
     * 同步时间
     */
    @TableField("sync_time")
    @Schema(description = "同步时间")
    private LocalDateTime syncTime;

    /**
     * 同步结果消息
     */
    @TableField("sync_message")
    @Schema(description = "同步结果消息")
    private String syncMessage;

    /**
     * 重试次数
     * <p>
     * 根据chonggou.txt要求添加
     * </p>
     */
    @TableField("retry_count")
    @Schema(description = "重试次数")
    private Integer retryCount;

    /**
     * 关联的在线消费记录ID
     */
    @TableField("online_record_id")
    @Schema(description = "关联的在线消费记录ID")
    private Long onlineRecordId;

    /**
     * 冲突类型
     */
    @TableField("conflict_type")
    @Schema(description = "冲突类型")
    private String conflictType;

    /**
     * 冲突处理状态
     */
    @TableField("conflict_status")
    @Schema(description = "冲突处理状态")
    private String conflictStatus;

    // 注意：createTime, updateTime, createUserId, updateUserId, deletedFlag, version
    // 已由BaseEntity提供，无需重复定义

    /**
     * 设置同步状态
     * <p>
     * 根据chonggou.txt要求添加显式setter方法
     * </p>
     *
     * @param syncStatus 同步状态
     */
    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    /**
     * 设置重试次数
     * <p>
     * 根据chonggou.txt要求添加显式setter方法
     * </p>
     *
     * @param retryCount 重试次数
     */
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    /**
     * 设置重试次数（Integer类型）
     *
     * @param retryCount 重试次数
     */
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }
}