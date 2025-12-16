package net.lab1024.sa.consume.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 离线消费记录实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Data
@TableName("t_offline_consume_record")
public class OfflineConsumeRecordEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 离线交易编号（设备端生成）
     */
    private String offlineTransNo;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备序列号
     */
    private String deviceSn;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 卡号
     */
    private String cardNo;

    /**
     * 消费金额
     */
    private BigDecimal amount;

    /**
     * 离线时账户余额
     */
    private BigDecimal balanceBeforeConsume;

    /**
     * 离线消费后余额
     */
    private BigDecimal balanceAfterConsume;

    /**
     * 消费时间（设备端记录）
     */
    private LocalDateTime consumeTime;

    /**
     * 同步状态：PENDING-待同步, SYNCED-已同步, CONFLICT-冲突, FAILED-失败
     */
    private String syncStatus;

    /**
     * 同步时间
     */
    private LocalDateTime syncTime;

    /**
     * 同步结果消息
     */
    private String syncMessage;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 关联的在线消费记录ID
     */
    private Long onlineRecordId;

    /**
     * 冲突类型：BALANCE-余额不一致, DUPLICATE-重复消费, OTHER-其他
     */
    private String conflictType;

    /**
     * 冲突处理状态：PENDING-待处理, RESOLVED-已解决, IGNORED-已忽略
     */
    private String conflictStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
