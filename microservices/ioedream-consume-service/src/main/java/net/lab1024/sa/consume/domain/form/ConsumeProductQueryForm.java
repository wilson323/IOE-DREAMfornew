package net.lab1024.sa.consume.domain.form;

import lombok.Data;
import org.springframework.lang.Nullable;

/**
 * 商品分页查询Form
 *
 * @author IOE-DREAM Team
 * @since 2025-12-13
 */
@Data
public class ConsumeProductQueryForm {

    /**
     * 页码（从1开始）
     */
    private Integer pageNum = 1;

    /**
     * 页大小
     */
    private Integer pageSize = 20;

    /**
     * 关键字（商品名称/编码）
     */
    @Nullable
    private String keyword;

    /**
     * 是否上架（null 不筛选）
     */
    private Boolean available;
}




