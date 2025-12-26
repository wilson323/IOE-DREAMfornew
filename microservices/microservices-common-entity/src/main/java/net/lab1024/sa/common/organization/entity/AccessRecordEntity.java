package net.lab1024.sa.common.organization.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 门禁记录实体类
 * <p>
 * 对应数据库表: t_access_record
 * 用于记录门禁通行记录
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
@TableName("t_access_record")
public class AccessRecordEntity extends BaseEntity {

    /**
     * 记录ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long recordId;

    /**
     * 记录唯一标识（用于幂等性检查）
     */
    @TableField("record_unique_id")
    private String recordUniqueId;

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
     * 通行结果：1-成功 2-失败
     */
    @TableField("access_result")
    private Integer accessResult;

    /**
     * 通行时间
     */
    @TableField("access_time")
    private LocalDateTime accessTime;

    /**
     * 通行类型：IN-进入 OUT-离开
     */
    @TableField("access_type")
    private String accessType;

    /**
     * 验证方式：FACE-人脸 CARD-刷卡 FINGERPRINT-指纹 PASSWORD-密码 QR-CODE二维码
     */
    @TableField("verify_method")
    private String verifyMethod;

    /**
     * 照片路径
     */
    @TableField("photo_path")
    private String photoPath;

    /**
     * 失败原因
     */
    @TableField("failure_reason")
    private String failureReason;

    /**
     * 体温（摄氏度）
     */
    @TableField("temperature")
    private BigDecimal temperature;

    /**
     * 口罩检测：0-未检测 1-未佩戴 2-已佩戴
     */
    @TableField("mask_detected")
    private Integer maskDetected;

    /**
     * 删除标记
     */
    @TableLogic
    @TableField("deleted_flag")
    private Integer deletedFlag;
}
