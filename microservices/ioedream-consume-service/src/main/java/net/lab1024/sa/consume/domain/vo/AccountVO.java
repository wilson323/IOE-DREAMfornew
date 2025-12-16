package net.lab1024.sa.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户VO
 *
 * @author IOE-DREAM
 * @since 2025-12-09
 */
@Data
@Schema(description = "账户VO")
public class AccountVO {

    /**
     * 账户ID
     */
    @Schema(description = "账户ID", example = "1")
    private Long accountId;

    /**
     * 账户编号
     */
    @Schema(description = "账户编号", example = "ACC001")
    private String accountNo;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 用户姓名
     */
    @Schema(description = "用户姓名", example = "张三")
    private String userName;

    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "13800138000")
    private String mobile;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    /**
     * 账户状态
     */
    @Schema(description = "账户状态", allowableValues = {"0", "1"}, example = "1")
    private Integer status;

    /**
     * 账户状态描述
     */
    @Schema(description = "账户状态描述", example = "正常")
    private String statusDesc;

    /**
     * 账户类型
     */
    @Schema(description = "账户类型", allowableValues = {"STAFF", "STUDENT", "VISITOR"}, example = "STAFF")
    private String accountType;

    /**
     * 账户类型描述
     */
    @Schema(description = "账户类型描述", example = "员工账户")
    private String accountTypeDesc;

    /**
     * 账户余额
     */
    @Schema(description = "账户余额", example = "500.00")
    private BigDecimal balance;

    /**
     * 冻结金额
     */
    @Schema(description = "冻结金额", example = "0.00")
    private BigDecimal frozenAmount;

    /**
     * 可用余额
     */
    @Schema(description = "可用余额", example = "500.00")
    private BigDecimal availableBalance;

    /**
     * 累计充值
     */
    @Schema(description = "累计充值", example = "1000.00")
    private BigDecimal totalRecharge;

    /**
     * 累计消费
     */
    @Schema(description = "累计消费", example = "500.00")
    private BigDecimal totalConsume;

    /**
     * 累计补贴
     */
    @Schema(description = "累计补贴", example = "50.00")
    private BigDecimal totalSubsidy;

    /**
     * 信用额度
     */
    @Schema(description = "信用额度", example = "0.00")
    private BigDecimal creditLimit;

    /**
     * 币种
     */
    @Schema(description = "币种", example = "CNY")
    private String currency;

    /**
     * 卡号
     */
    @Schema(description = "卡号", example = "1234567890")
    private String cardNo;

    /**
     * 卡状态
     */
    @Schema(description = "卡状态", allowableValues = {"0", "1"}, example = "1")
    private Integer cardStatus;

    /**
     * 卡状态描述
     */
    @Schema(description = "卡状态描述", example = "正常")
    private String cardStatusDesc;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    /**
     * 区域ID
     */
    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    /**
     * 区域名称
     */
    @Schema(description = "区域名称", example = "总部大楼")
    private String areaName;

    /**
     * 职位
     */
    @Schema(description = "职位", example = "软件工程师")
    private String position;

    /**
     * 工号
     */
    @Schema(description = "工号", example = "EMP001")
    private String employeeNo;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-01-01T10:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-12-09T15:30:00")
    private LocalDateTime updateTime;

    /**
     * 最后消费时间
     */
    @Schema(description = "最后消费时间", example = "2025-12-09T12:00:00")
    private LocalDateTime lastConsumeTime;

    /**
     * 最后充值时间
     */
    @Schema(description = "最后充值时间", example = "2025-12-08T10:00:00")
    private LocalDateTime lastRechargeTime;

    /**
     * 头像
     */
    @Schema(description = "头像", example = "/images/avatar/default.png")
    private String avatar;

    /**
     * 性别
     */
    @Schema(description = "性别", allowableValues = {"M", "F", "U"}, example = "M")
    private String gender;

    /**
     * 生日
     */
    @Schema(description = "生日", example = "1990-01-01")
    private String birthday;

    /**
     * 地址
     */
    @Schema(description = "地址", example = "北京市朝阳区")
    private String address;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "正常账户")
    private String remark;

    /**
     * 扩展属性
     */
    @Schema(description = "扩展属性")
    private String extendAttrs;
}


