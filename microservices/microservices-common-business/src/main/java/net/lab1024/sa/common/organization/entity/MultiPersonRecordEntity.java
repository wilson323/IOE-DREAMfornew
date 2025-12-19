package net.lab1024.sa.common.organization.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 多人验证记录实体类
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 字段数量控制在30个以内
 * - 类行数控制在200行以内
 * - 纯数据模型，无业务逻辑方法
 * </p>
 * <p>
 * 业务场景：
 * - 记录多人验证会话
 * - 用于多人验证（需要多人同时验证才能开门）
 * - 支持会话管理和超时清理
 * </p>
 * <p>
 * 数据库表：t_access_multi_person_record
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_access_multi_person_record")
@Schema(description = "多人验证记录实体")
public class MultiPersonRecordEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID", example = "1001")
    private Long id;

    /**
     * 验证会话ID（UUID）
     */
    @NotNull
    @TableField("verification_session_id")
    @Schema(description = "验证会话ID", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    private String verificationSessionId;

    /**
     * 区域ID
     */
    @NotNull
    @TableField("area_id")
    @Schema(description = "区域ID", example = "2001", required = true)
    private Long areaId;

    /**
     * 设备ID
     */
    @NotNull
    @TableField("device_id")
    @Schema(description = "设备ID", example = "3001", required = true)
    private Long deviceId;

    /**
     * 需要验证的人数
     */
    @NotNull
    @TableField("required_count")
    @Schema(description = "需要验证的人数", example = "2", required = true)
    private Integer requiredCount;

    /**
     * 当前已验证人数
     */
    @NotNull
    @TableField("current_count")
    @Schema(description = "当前已验证人数", example = "1", required = true)
    private Integer currentCount;

    /**
     * 已验证用户ID列表（JSON格式）
     * 格式：[1001, 1002, 1003]
     */
    @TableField("user_ids")
    @Schema(description = "已验证用户ID列表（JSON格式）", example = "[1001, 1002]")
    private String userIds;

    /**
     * 状态
     * 0=等待中
     * 1=已完成
     * 2=已超时
     */
    @NotNull
    @TableField("status")
    @Schema(description = "状态", example = "0", allowableValues = {"0", "1", "2"}, required = true)
    private Integer status;

    /**
     * 开始时间
     */
    @NotNull
    @TableField("start_time")
    @Schema(description = "开始时间", example = "2025-01-30 10:00:00", required = true)
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    @TableField("complete_time")
    @Schema(description = "完成时间", example = "2025-01-30 10:02:00")
    private LocalDateTime completeTime;

    /**
     * 过期时间
     */
    @TableField("expire_time")
    @Schema(description = "过期时间", example = "2025-01-30 10:05:00")
    private LocalDateTime expireTime;

    // ==================== 状态常量 ====================

    /**
     * 状态常量类
     */
    public static class Status {
        /**
         * 等待中
         */
        public static final int WAITING = 0;

        /**
         * 已完成
         */
        public static final int COMPLETED = 1;

        /**
         * 已超时
         */
        public static final int TIMEOUT = 2;
    }
}
