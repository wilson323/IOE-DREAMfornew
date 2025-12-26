package net.lab1024.sa.common.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.domain.PageParam;

/**
 * 通用查询表单基类。
 * <p>
 * 说明：
 * - 统一承载分页参数（pageNum/pageSize），避免各业务 QueryForm 重复声明分页字段；
 * - 作为公共契约类型，供各微服务的查询入参表单继承使用。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "通用查询表单基类")
public class BaseQueryForm extends PageParam {
    private static final long serialVersionUID = 1L;
}


