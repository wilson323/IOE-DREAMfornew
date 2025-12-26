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
 * 反潜回记录实体类
 * <p>
 * 对应数据库表: t_access_anti_passback_record
 * 用于记录反潜回验证的进出记录
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
@TableName("t_access_anti_passback_record")
public class AntiPassbackRecordEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 设备ID
     */
    @TableField("device_id")
    private Long deviceId;

    /**
     * 区域ID
     */
    @TableField("area_id")
    private Long areaId;

    /**
     * 进出状态: 1=进, 2=出
     */
    @TableField("in_out_status")
    private Integer inOutStatus;

    /**
     * 记录时间
     */
    @TableField("record_time")
    private LocalDateTime recordTime;

    /**
     * 通行类型: IN=进入, OUT=离开
     */
    @TableField("access_type")
    private String accessType;

    /**
     * 验证方式: 0=密码, 1=指纹, 2=卡, 11=面部等
     */
    @TableField("verify_type")
    private Integer verifyType;

    /**
     * 进出状态枚举
     * <p>
     * 用于类型安全的进出状态常量定义
     * </p>
     */
    public static class InOutStatus {
        /**
         * 进入: 1
         */
        public static final Integer IN = 1;

        /**
         * 离开: 2
         */
        public static final Integer OUT = 2;

        /**
         * 私有构造函数，防止实例化
         */
        private InOutStatus() {
        }
    }
}
