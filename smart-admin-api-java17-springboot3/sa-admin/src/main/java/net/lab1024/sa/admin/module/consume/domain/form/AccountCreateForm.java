package net.lab1024.sa.admin.module.consume.domain.form;

import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 账户创建表单
 * <p>
 * 严格遵循repowiki规范:
 * - 使用@Data注解自动生成getter/setter
 * - 使用@Accessors启用链式调用
 * - 使用@NotNull、@NotBlank进行参数验证
 * - 提供完整的创建字段
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */
@Data
@Accessors(chain = true)
public class AccountCreateForm {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 账户类型：1-个人账户 2-企业账户
     */
    @NotNull(message = "账户类型不能为空")
    private Integer accountType;

    /**
     * 账户名称
     */
    @NotBlank(message = "账户名称不能为空")
    private String accountName;

    /**
     * 初始余额
     */
    private BigDecimal initialBalance;

    /**
     * 信用额度
     */
    private BigDecimal creditLimit;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 备注
     */
    private String remark;
}