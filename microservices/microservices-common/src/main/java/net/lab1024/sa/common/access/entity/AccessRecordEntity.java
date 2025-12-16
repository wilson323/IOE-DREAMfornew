package net.lab1024.sa.common.access.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 门禁记录实体类
 * <p>
 * 对应数据库表 t_access_record
 * 用于记录门禁通行事件，包括成功和失败的通行记录
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
@TableName("t_access_record")
public class AccessRecordEntity extends BaseEntity {

    /**
     * 记录ID（主键）
     */
    @TableId(value = "record_id", type = IdType.AUTO)
    private Long recordId;

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
     * 通行结果
     * <p>
     * 1-成功
     * 2-失败
     * </p>
     */
    @TableField("access_result")
    private Integer accessResult;

    /**
     * 通行时间
     */
    @TableField("access_time")
    private LocalDateTime accessTime;

    /**
     * 通行类型
     * <p>
     * IN-进入
     * OUT-离开
     * </p>
     */
    @TableField("access_type")
    private String accessType;

    /**
     * 验证方式
     * <p>
     * FACE-人脸
     * CARD-刷卡
     * FINGERPRINT-指纹
     * </p>
     */
    @TableField("verify_method")
    private String verifyMethod;

    /**
     * 照片路径
     */
    @TableField("photo_path")
    private String photoPath;
}
