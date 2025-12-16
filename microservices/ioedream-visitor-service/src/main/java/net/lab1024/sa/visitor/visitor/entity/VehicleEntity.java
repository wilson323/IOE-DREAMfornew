package net.lab1024.sa.visitor.visitor.entity;

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
 * 访客车辆实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_visitor_vehicle")
public class VehicleEntity {

    /**
     * 车辆ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列vehicle_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "vehicle_id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 车牌号
     */
    @TableField("vehicle_number")
    private String vehicleNumber;

    /**
     * 车辆类型（1-小型车 2-中型车 3-大型车）
     */
    @TableField("vehicle_type")
    private Integer vehicleType;

    /**
     * 车辆颜色
     */
    @TableField("vehicle_color")
    private String vehicleColor;

    /**
     * 车辆品牌
     */
    @TableField("vehicle_brand")
    private String vehicleBrand;

    /**
     * 车辆型号
     */
    @TableField("vehicle_model")
    private String vehicleModel;

    /**
     * 所属公司
     */
    @TableField("company_name")
    private String companyName;

    /**
     * 司机ID
     */
    @TableField("driver_id")
    private Long driverId;

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


