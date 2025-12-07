package net.lab1024.sa.common.visitor.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 访客司机实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_visitor_driver")
public class DriverEntity {

    /**
     * 司机ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long driverId;

    /**
     * 司机姓名
     */
    @TableField("driver_name")
    private String driverName;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 身份证号
     */
    @TableField("id_card")
    private String idCard;

    /**
     * 驾照类型（A1/A2/B1/B2/C1等）
     */
    @TableField("license_type")
    private String licenseType;

    /**
     * 驾照号
     */
    @TableField("license_number")
    private String licenseNumber;

    /**
     * 驾照有效期
     */
    @TableField("license_expiry_date")
    private LocalDate licenseExpiryDate;

    /**
     * 所属公司
     */
    @TableField("company_name")
    private String companyName;

    /**
     * 司机照片URL
     */
    @TableField("photo_url")
    private String photoUrl;

    /**
     * 状态（1-正常 0-禁用）
     */
    @TableField("status")
    private Integer status;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 创建人ID
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private Long createUserId;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 更新人ID
     */
    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    private Long updateUserId;

    /**
     * 删除标记（0-未删除 1-已删除）
     */
    @TableLogic
    @TableField("deleted_flag")
    private Integer deletedFlag;
}
