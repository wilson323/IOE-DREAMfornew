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
 * 二维码实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_qr_code")
@Schema(description = "二维码实体")
public class QrCodeEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;
    /**
     * 二维码ID
     */
    @TableField("qr_id")
    @Schema(description = "二维码ID")
    private Long qrId;
    /**
     * 二维码内容
     */
    @TableField("qr_code")
    @Schema(description = "二维码内容")
    private String qrCode;
    /**
     * 二维码类型
     */
    @TableField("qr_type")
    @Schema(description = "二维码类型")
    private String qrType;
    /**
     * 关联账户
     */
    @TableField("account_no")
    @Schema(description = "关联账户")
    private String accountNo;
    /**
     * 有效期
     */
    @TableField("valid_time")
    @Schema(description = "有效期")
    private java.time.LocalDateTime validTime;
    /**
     * 状态
     */
    @TableField("status")
    @Schema(description = "状态")
    private Integer status;
    /**
     * 备注
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
    // 注意：createTime, updateTime, createUserId, updateUserId, deletedFlag, version
    // 已由BaseEntity提供，无需重复定义
}
