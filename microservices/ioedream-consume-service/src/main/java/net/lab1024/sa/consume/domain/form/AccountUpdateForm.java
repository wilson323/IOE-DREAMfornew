package net.lab1024.sa.consume.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 账户更新表单
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用Form后缀命名
 * - 使用Jakarta验证注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class AccountUpdateForm {

    /**
     * 账户ID
     */
    @NotNull(message = "账户ID不能为空")
    private Long accountId;

    /**
     * 账户类别ID（可选）
     */
    private Long accountKindId;

    /**
     * 账户状态（可选）
     * <p>
     * 1-正常
     * 2-冻结
     * 3-注销
     * </p>
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 获取账户ID
     * <p>
     * Lombok @Data 应该自动生成，但为了确保编译通过，手动添加
     * </p>
     *
     * @return 账户ID
     */
    public Long getAccountId() {
        return this.accountId;
    }

    /**
     * 设置账户ID
     *
     * @param accountId 账户ID
     */
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}
