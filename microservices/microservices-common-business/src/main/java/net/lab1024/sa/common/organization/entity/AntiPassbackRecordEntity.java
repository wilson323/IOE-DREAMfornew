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
 * 反潜记录实体类
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
 * - 记录用户的进出状态
 * - 用于反潜验证（防止同一用户连续进入）
 * - 支持时间窗口管理
 * </p>
 * <p>
 * 数据库表：t_access_anti_passback_record
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_access_anti_passback_record")
@Schema(description = "反潜记录实体")
public class AntiPassbackRecordEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID", example = "1001")
    private Long id;

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
     * 进出状态
     * 1=进
     * 2=出
     */
    @NotNull
    @TableField("in_out_status")
    @Schema(description = "进出状态", example = "1", allowableValues = {"1", "2"}, required = true)
    private Integer inOutStatus;

    /**
     * 记录时间
     */
    @NotNull
    @TableField("record_time")
    @Schema(description = "记录时间", example = "2025-01-30 10:00:00", required = true)
    private LocalDateTime recordTime;

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
     * 0=密码
     * 1=指纹
     * 2=卡
     * 11=面部
     */
    @TableField("verify_type")
    @Schema(description = "验证方式", example = "1")
    private Integer verifyType;

    // ==================== 进出状态常量 ====================

    /**
     * 进出状态常量类
     */
    public static class InOutStatus {
        /**
         * 进入
         */
        public static final int IN = 1;

        /**
         * 离开
         */
        public static final int OUT = 2;
    }

    /**
     * 通行类型常量类
     */
    public static class AccessType {
        /**
         * 进入
         */
        public static final String IN = "IN";

        /**
         * 离开
         */
        public static final String OUT = "OUT";
    }
}
