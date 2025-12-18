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
 * 门禁通行记录实体类
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
 * - 记录用户的通行记录（设备端验证模式）
 * - 记录后台验证的通行结果
 * - 支持异常检测和统计分析
 * </p>
 * <p>
 * 数据库表：t_access_record
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_access_record")
@Schema(description = "门禁通行记录实体")
public class AccessRecordEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID（主键）
     */
    @TableId(value = "record_id", type = IdType.AUTO)
    @Schema(description = "记录ID", example = "1001")
    private Long recordId;

    /**
     * 用户ID
     */
    @NotNull
    @TableField("user_id")
    @Schema(description = "用户ID", example = "2001", required = true)
    private Long userId;

    /**
     * 设备ID
     */
    @NotNull
    @TableField("device_id")
    @Schema(description = "设备ID", example = "3001", required = true)
    private Long deviceId;

    /**
     * 区域ID
     */
    @TableField("area_id")
    @Schema(description = "区域ID", example = "4001")
    private Long areaId;

    /**
     * 通行结果
     * 1=成功
     * 2=失败
     */
    @NotNull
    @TableField("access_result")
    @Schema(description = "通行结果", example = "1", allowableValues = {"1", "2"}, required = true)
    private Integer accessResult;

    /**
     * 通行时间
     */
    @NotNull
    @TableField("access_time")
    @Schema(description = "通行时间", example = "2025-01-30 10:00:00", required = true)
    private LocalDateTime accessTime;

    /**
     * 通行类型
     * IN=进入
     * OUT=离开
     */
    @TableField("access_type")
    @Schema(description = "通行类型", example = "IN", allowableValues = {"IN", "OUT"})
    private String accessType;

    /**
     * 验证方式
     * FACE=人脸
     * CARD=刷卡
     * FINGERPRINT=指纹
     */
    @TableField("verify_method")
    @Schema(description = "验证方式", example = "FACE", allowableValues = {"FACE", "CARD", "FINGERPRINT"})
    private String verifyMethod;

    /**
     * 照片路径
     */
    @TableField("photo_path")
    @Schema(description = "照片路径", example = "/photos/2025/01/30/xxx.jpg")
    private String photoPath;

    /**
     * 记录唯一标识（用于幂等性检查）
     * <p>
     * 注意：数据库表可能没有此字段，使用组合键（userId + deviceId + accessTime）进行幂等性检查
     * </p>
     */
    @TableField(value = "record_unique_id", exist = false) // exist = false 表示字段不存在于数据库表
    @Schema(description = "记录唯一标识（用于幂等性检查）", example = "REC_20250130_1001_001")
    private String recordUniqueId;
}
