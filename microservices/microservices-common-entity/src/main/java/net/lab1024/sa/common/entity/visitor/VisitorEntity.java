package net.lab1024.sa.common.entity.visitor;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 访客实体类
 * <p>
 * 访客信息管理实体，支持访客基本信息、黑名单管理、访客等级等功能
 * 严格遵循CLAUDE.md全局架构规范和Entity设计标准
 * </p>
 *
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>访客基本信息管理</li>
 *   <li>黑名单机制管理</li>
 *   <li>访客等级权限管理</li>
 *   <li>访客照片管理</li>
 *   <li>访问历史记录</li>
 * </ul>
 *
 * <p><strong>业务场景：</strong></p>
 * <ul>
 *   <li>预约访客登记</li>
 *   <li>临时访客管理</li>
 *   <li>VIP访客管理</li>
 *   <li>黑名单人员管控</li>
 * </ul>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 * @see net.lab1024.sa.common.visitor.service.VisitorService 访客服务接口
 * @see net.lab1024.sa.common.visitor.dao.VisitorDao 访客数据访问接口
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_visitor")
public class VisitorEntity extends BaseEntity {

    /**
     * 访客ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long visitorId;

    /**
     * 访客编号
     * <p>
     * 系统生成的唯一访客标识，格式：V + 年月日 + 6位序号
     * 示例：V20251216000001
     * </p>
     */
    @TableField("visitor_code")
    private String visitorCode;

    /**
     * 访客姓名
     * <p>
     * 访客真实姓名，必须与身份证件姓名一致
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
     * 身份证号或其他有效证件号码
     * 支持身份证、护照、港澳通行证等
     * 加密存储，符合隐私保护要求
     * </p>
     */
    @TableField("id_card")
    private String idCard;

    /**
     * 手机号
     * <p>
     * 访客联系电话，用于验证码发送和通知
     * 支持国内手机号格式
     * 加密存储，符合隐私保护要求
     * </p>
     */
    @TableField("phone")
    private String phone;

    /**
     * 邮箱
     * <p>
     * 访客电子邮箱，用于发送预约确认和访问通知
     * 非必填项
     * </p>
     */
    @TableField("email")
    private String email;

    /**
     * 公司名称
     * <p>
     * 访客所属公司或单位名称
     * 用于企业访客管理和统计分析
     * </p>
     */
    @TableField("company_name")
    private String companyName;

    /**
     * 照片URL
     * <p>
     * 访客照片访问地址
     * 用于人脸识别验证和门禁通行
     * 支持JPG、PNG格式，最大5MB
     * </p>
     */
    @TableField("photo_url")
    private String photoUrl;

    /**
     * 访客等级
     * <p>
     * 访客权限等级，影响访问区域和有效期
     * NORMAL-普通访客
     * VIP-重要访客
     * CONTRACTOR-承包商访客
     * DELIVERY-配送访客
     * </p>
     */
    @TableField("visitor_level")
    private String visitorLevel;

    /**
     * 是否黑名单
     * <p>
     * 0-否：正常访客
     * 1-是：黑名单人员，禁止访问
     * </p>
     */
    @TableField("blacklisted")
    private Integer blacklisted;

    /**
     * 黑名单原因
     * <p>
     * 加入黑名单的具体原因
     * 示例：违规行为、安全威胁、禁止访问等
     * </p>
     */
    @TableField("blacklist_reason")
    private String blacklistReason;

    /**
     * 加入黑名单时间
     * <p>
     * 访客被加入黑名单的具体时间
     * 用于黑名单管理和审计
     * </p>
     */
    @TableField("blacklist_time")
    private LocalDateTime blacklistTime;

    /**
     * 黑名单操作人
     * <p>
     * 执行黑名单操作的管理员姓名
     * 用于责任追溯和审计
     * </p>
     */
    @TableField("blacklist_operator")
    private String blacklistOperator;

    /**
     * 最后访问时间
     * <p>
     * 访客最近一次成功访问的时间
     * 用于访客活跃度分析和历史记录
     * </p>
     */
    @TableField("last_visit_time")
    private LocalDateTime lastVisitTime;

    /**
     * 访客权限ID
     * <p>
     * 关联的访客权限配置ID
     * 决定访客可访问的区域和权限范围
     * </p>
     */
    @TableField("access_level_id")
    private Long accessLevelId;

    /**
     * 启用状态
     * <p>
     * true-启用：可以正常预约和访问
     * false-禁用：暂停预约和访问权限
     * </p>
     */
    @TableField("shelves_flag")
    private Boolean shelvesFlag;

    /**
     * 备注
     * <p>
     * 访客相关备注信息
     * 可记录特殊要求、注意事项等
     * </p>
     */
    @TableField("remark")
    private String remark;

    /**
     * 扩展属性（JSON格式）
     * <p>
     * 用于存储访客相关的扩展信息
     * 示例：偏好设置、特殊需求等
     * </p>
     */
    @TableField("extended_attributes")
    private String extendedAttributes;
}
