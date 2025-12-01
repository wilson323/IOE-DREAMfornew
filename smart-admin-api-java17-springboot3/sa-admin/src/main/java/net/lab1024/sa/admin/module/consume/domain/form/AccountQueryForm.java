package net.lab1024.sa.admin.module.consume.domain.form;

import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.Min;

/**
 * 账户查询表单
 * <p>
 * 严格遵循repowiki规范:
 * - 使用@Data注解自动生成getter/setter
 * - 使用@Accessors启用链式调用
 * - 使用@Min进行参数验证
 * - 提供完整的查询字段
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */
@Data
@Accessors(chain = true)
public class AccountQueryForm {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 账户状态：1-正常 2-冻结 3-注销
     */
    private Integer status;

    /**
     * 账户类型：1-个人账户 2-企业账户
     */
    private Integer accountType;

    /**
     * 关键词搜索（用户名、手机号）
     */
    private String keyword;

    /**
     * 页码
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    /**
     * 页大小
     */
    @Min(value = 1, message = "页大小必须大于0")
    private Integer pageSize = 20;

    /**
     * 排序字段
     */
    private String sortField = "createTime";

    /**
     * 排序方向：asc/desc
     */
    private String sortDirection = "desc";
}