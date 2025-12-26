package net.lab1024.sa.common.organization.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 多人验证记录实体类
 * <p>
 * 对应数据库表: t_access_multi_person_record
 * 用于记录多人验证会话和验证状态
 * </p>
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 字段数控制在30个以内
 * - 行数控制在200行以内
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_access_multi_person_record")
public class MultiPersonRecordEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 验证会话ID
     */
    @TableField("verification_session_id")
    private String verificationSessionId;

    /**
     * 区域ID
     */
    @TableField("area_id")
    private Long areaId;

    /**
     * 设备ID
     */
    @TableField("device_id")
    private Long deviceId;

    /**
     * 需要验证的人数
     */
    @TableField("required_count")
    private Integer requiredCount;

    /**
     * 当前已验证人数
     */
    @TableField("current_count")
    private Integer currentCount;

    /**
     * 已验证用户ID列表(JSON格式)
     */
    @TableField("user_ids")
    private String userIds;

    /**
     * 状态常量
     */
    public static class Status {
        public static final Integer WAITING = 0; // 等待中
        public static final Integer COMPLETED = 1; // 已完成
        public static final Integer TIMEOUT = 2; // 已超时
        public static final Integer FAILED = 3; // 失败
    }

    /**
     * 状态: 0=等待中, 1=已完成, 2=已超时, 3=失败
     */
    @TableField("status")
    private Integer status;

    /**
     * 开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    @TableField("complete_time")
    private LocalDateTime completeTime;

    /**
     * 过期时间
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;
}
