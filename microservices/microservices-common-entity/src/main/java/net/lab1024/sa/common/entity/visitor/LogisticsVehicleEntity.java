package net.lab1024.sa.common.entity.visitor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 物流车辆实体类
 * <p>
 * 物流车辆信息管理实体，支持车辆基本信息、证件管理、载重管理等功能
 * 严格遵循CLAUDE.md全局架构规范和Entity设计标准
 * </p>
 *
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>车辆基本信息管理</li>
 *   <li>行驶证和证件管理</li>
 *   <li>载重和尺寸管理</li>
 *   <li>车辆状态监控</li>
 *   <li>年检和保险管理</li>
 * </ul>
 *
 * <p><strong>业务场景：</strong></p>
 * <ul>
 *   <li>供应商车辆管理</li>
 *   <li>配送车辆管理</li>
 *   <li>物流运输管理</li>
 *   <li>车辆进出管理</li>
 * </ul>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 * @see net.lab1024.sa.common.visitor.service.LogisticsVehicleService 物流车辆服务接口
 * @see net.lab1024.sa.common.visitor.dao.LogisticsVehicleDao 物流车辆数据访问接口
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_logistics_vehicle")
public class LogisticsVehicleEntity extends BaseEntity {

    /**
     * 车辆ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long vehicleId;

    /**
     * 车辆编号
     * <p>
     * 系统生成的唯一车辆标识，格式：V + 年月日 + 6位序号
     * 示例：V20251216000001
     * </p>
     */
    @TableField("vehicle_code")
    private String vehicleCode;

    /**
     * 车牌号
     * <p>
     * 车辆的车牌号码，用于识别和管理
     * 支持蓝牌、黄牌等不同类型车牌
     * 用于门禁识别和进出记录
     * </p>
     */
    @TableField("plate_number")
    private String plateNumber;

    /**
     * 车辆类型
     * <p>
     * 车辆的类型分类
     * 示例：卡车、货车、面包车、小轿车、电动车等
     * 用于运输管理和调度
     * </p>
     */
    @TableField("vehicle_type")
    private String vehicleType;

    /**
     * 品牌型号
     * <p>
     * 车辆的品牌和型号信息
     * 示例：东风天锦、解放J6、五菱宏光等
     * 用于车辆识别和管理
     * </p>
     */
    @TableField("brand_model")
    private String brandModel;

    /**
     * 车辆颜色
     * <p>
     * 车辆的外观颜色
     * 示例：白色、蓝色、红色、灰色等
     * 用于车辆识别和查找
     * </p>
     */
    @TableField("vehicle_color")
    private String vehicleColor;

    /**
     * 核载重量（吨）
     * <p>
     * 车辆的最大载重能力
     * 用于运输规划和安全检查
     * 精确到小数点后2位
     * </p>
     */
    @TableField("load_capacity")
    private BigDecimal loadCapacity;

    /**
     * 核载人数
     * <p>
     * 车辆的最大载客数量
     * 包括司机在内的最大乘坐人数
     * 用于人员运输管理
     * </p>
     */
    @TableField("seat_capacity")
    private Integer seatCapacity;

    /**
     * 车长（米）
     * <p>
     * 车辆的总长度
     * 用于通行限制和停放管理
     * 精确到小数点后2位
     * </p>
     */
    @TableField("vehicle_length")
    private BigDecimal vehicleLength;

    /**
     * 车宽（米）
     * <p>
     * 车辆的总宽度
     * 用于通行限制和停放管理
     * 精确到小数点后2位
     * </p>
     */
    @TableField("vehicle_width")
    private BigDecimal vehicleWidth;

    /**
     * 车高（米）
     * <p>
     * 车辆的总高度
     * 用于通行限制和停放管理
     * 精确到小数点后2位
     * </p>
     */
    @TableField("vehicle_height")
    private BigDecimal vehicleHeight;

    /**
     * 行驶证号
     * <p>
     * 车辆行驶证编号
     * 用于车辆资质验证和管理
     * 必须在有效期内
     * </p>
     */
    @TableField("registration_number")
    private String registrationNumber;

    /**
     * 注册日期
     * <p>
     * 车辆的注册登记日期
     * 用于车辆管理和年检计算
     * </p>
     */
    @TableField("registration_date")
    private LocalDate registrationDate;

    /**
     * 年检有效期
     * <p>
     * 车辆年检合格的有效截止日期
     * 用于年检管理和到期提醒
     * </p>
     */
    @TableField("inspection_expire_date")
    private LocalDate inspectionExpireDate;

    /**
     * 保险有效期
     * <p>
     * 车辆交强险和商业险的有效截止日期
     * 用于保险管理和到期提醒
     * </p>
     */
    @TableField("insurance_expire_date")
    private LocalDate insuranceExpireDate;

    /**
     * 运输许可证号
     * <p>
     * 货物运输许可证编号
     * 用于营运车辆资质验证
     * 营运车辆必须持有
     * </p>
     */
    @TableField("transport_permit")
    private String transportPermit;

    /**
     * 许可证有效期
     * <p>
     * 运输许可证的有效截止日期
     * 用于许可证管理和到期提醒
     * </p>
     */
    @TableField("permit_expire_date")
    private LocalDate permitExpireDate;

    /**
     * 车辆状态
     * <p>
     * ACTIVE-正常：车辆状态正常，可正常执行运输任务
     * MAINTENANCE-维修中：车辆正在维修，暂停使用
     * SCRAPPED-报废：车辆已报废，停止使用
     * SUSPENDED-停用：车辆暂停使用
     * </p>
     */
    @TableField("vehicle_status")
    private String vehicleStatus;

    /**
     * 当前司机ID
     * <p>
     * 当前使用该车辆的司机ID
     * 关联到LogisticsDriverEntity
     * 用于车辆调度和管理
     * </p>
     */
    @TableField("current_driver_id")
    private Long currentDriverId;

    /**
     * 车辆照片URL
     * <p>
     * 车辆照片访问地址，JSON格式存储
     * 示例：["正面照.jpg", "侧面照.jpg", "后备箱.jpg"]
     * 用于车辆识别和管理
     * </p>
     */
    @TableField("vehicle_photos")
    private String vehiclePhotos;

    /**
     * 启用状态
     * <p>
     * true-启用：车辆可正常执行运输任务
     * false-禁用：车辆暂停运输任务
     * </p>
     */
    @TableField("shelves_flag")
    private Boolean shelvesFlag;

    /**
     * 备注
     * <p>
     * 车辆相关备注信息
     * 可记录特殊情况、维修记录等
     * </p>
     */
    @TableField("remark")
    private String remark;

    /**
     * 扩展属性（JSON格式）
     * <p>
     * 用于存储车辆相关的扩展信息
     * 示例：GPS设备编号、特殊装备等
     * </p>
     */
    @TableField("extended_attributes")
    private String extendedAttributes;
}
