package net.lab1024.sa.common.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.domain.PageParam;

/**
 * 分页表单基类（兼容旧代码）
 * <p>
 * 说明：
 * - 为了兼容现有代码中的PageForm引用
 * - 新代码建议使用BaseQueryForm
 * - 提供统一的分页参数承载
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "分页表单基类")
public class PageForm extends PageParam {
    private static final long serialVersionUID = 1L;
}