package net.lab1024.sa.common.entity.visitor;

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
 * 物流司机实体类
 * <p>
 * 物流司机信息管理实体，支持司机基本信息、驾驶证管理、运输记录等功能
 * 严格遵循CLAUDE.md全局架构规范和Entity设计标准
 * </p>
 *
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>司机基本信息管理</li>
 *   <li>驾驶证和资质管理</li>
 *   <li>运输公司管理</li>
 *   <li>黑名单机制</li>
 *   <li>运输记录统计</li>
 * </ul>
 *
 * <p><strong>业务场景：</strong></p>
 * <ul>
 *   <li>供应商司机管理</li>
 *   <li>配送司机管理</li>
 *   <li>物流运输管理</li>
 *   <li>司机资质审核</li>
 * </ul>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 * @see net.lab1024.sa.common.visitor.service.LogisticsDriverService 物流司机服务接口
 * @see net.lab1024.sa.common.visitor.dao.LogisticsDriverDao 物流司机数据访问接口
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_logistics_driver")
public class LogisticsDriverEntity extends BaseEntity {

    /**
     * 司机ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long driverId;

    /**
     * 司机编号
     * <p>
     * 系统生成的唯一司机标识，格式：D + 年月日 + 6位序号
     * 示例：D20251216000001
     * </p>
     */
    @TableField("driver_code")
    private String driverCode;

    /**
     * 姓名
     * <p>
     * 司机真实姓名，必须与身份证件姓名一致
     * 长度限制：2-50个字符
     * </p>
     */
    @TableField("name")
    private String name;

    /**
     * 性别
     * <p>
     * 1-男
     * 2-女
     * </p>
     */
    @TableField("gender")
    private Integer gender;

    /**
     * 证件号
     * <p>
     * 司机身份证号码，用于身份验证
     * 加密存储，符合隐私保护要求
     * </p>
     */
    @TableField("id_card")
    private String idCard;

    /**
     * 手机号
     * <p>
     * 司机联系电话，用于运输通知和紧急联系
     * 加密存储，符合隐私保护要求
     * </p>
     */
    @TableField("phone")
    private String phone;

    /**
     * 驾驶证号
     * <p>
     * 司机驾驶证号码，用于资质验证
     * 必须在有效期内
     * </p>
     */
    @TableField("driver_license")
    private String driverLicense;

    /**
     * 驾照类型
     * <p>
     * 司机持有的驾照类型
     * 示例：A1、A2、B1、B2、C1等
     * 用于车辆类型匹配和资质审核
     * </p>
     */
    @TableField("license_type")
    private String licenseType;

    /**
     * 驾照有效期
     * <p>
     * 驾驶证的有效截止日期
     * 用于驾照有效性验证和到期提醒
     * </p>
     */
    @TableField("license_expire_date")
    private LocalDate licenseExpireDate;

    /**
     * 运输公司
     * <p>
     * 司机所属的运输公司或物流公司名称
     * 用于司机管理和责任追溯
     * </p>
     */
    @TableField("transport_company")
    private String transportCompany;

    /**
     * 公司地址
     * <p>
     * 运输公司的注册地址或经营地址
     * 用于联系和管理
     * </p>
     */
    @TableField("company_address")
    private String companyAddress;

    /**
     * 紧急联系人
     * <p>
     * 司机的紧急联系人姓名
     * 用于紧急情况联系
     * </p>
     */
    @TableField("emergency_contact")
    private String emergencyContact;

    /**
     * 紧急联系电话
     * <p>
     * 紧急联系人的电话号码
     * 用于紧急情况通知
     * </p>
     */
    @TableField("emergency_phone")
    private String emergencyPhone;

    /**
     * 照片URL
     * <p>
     * 司机照片访问地址
     * 用于身份验证和门禁识别
     * 支持JPG、PNG格式，最大5MB
     * </p>
     */
    @TableField("photo_url")
    private String photoUrl;

    /**
     * 司机状态
     * <p>
     * ACTIVE-正常：司机状态正常，可正常执行运输任务
     * BLACKLISTED-黑名单：司机被列入黑名单，禁止进入园区
     * SUSPENDED-暂停：司机资格暂停，临时禁止运输
     * </p>
     */
    @TableField("driver_status")
    private String driverStatus;

    /**
     * 黑名单原因
     * <p>
     * 司机被加入黑名单的具体原因
     * 示例：违规操作、安全隐患、诚信问题等
     * </p>
     */
    @TableField("blacklist_reason")
    private String blacklistReason;

    /**
     * 总运输次数
     * <p>
     * 司机累计完成的运输任务次数
     * 用于司机经验评估和绩效管理
     * </p>
     */
    @TableField("total_trips")
    private Integer totalTrips;

    /**
     * 最后运输时间
     * <p>
     * 司机最近一次完成运输任务的时间
     * 用于司机活跃度分析和调度参考
     * </p>
     */
    @TableField("last_trip_time")
    private LocalDateTime lastTripTime;

    /**
     * 启用状态
     * <p>
     * true-启用：司机可正常接受运输任务
     * false-禁用：司机暂停运输任务
     * </p>
     */
    @TableField("shelves_flag")
    private Boolean shelvesFlag;

    /**
     * 备注
     * <p>
     * 司机相关备注信息
     * 可记录特殊情况、技能特长等
     * </p>
     */
    @TableField("remark")
    private String remark;

    /**
     * 扩展属性（JSON格式）
     * <p>
     * 用于存储司机相关的扩展信息
     * 示例：培训记录、奖惩记录等
     * </p>
     */
    @TableField("extended_attributes")
    private String extendedAttributes;
}
