package net.lab1024.sa.consume.service;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.consume.domain.form.ConsumeProductQueryForm;
import net.lab1024.sa.consume.domain.vo.ConsumeProductVO;

/**
 * 商品管理Service（管理端）
 *
 * @author IOE-DREAM Team
 * @since 2025-12-13
 */
public interface ConsumeProductService {

    PageResult<ConsumeProductVO> queryProducts(ConsumeProductQueryForm queryForm);

    void setAvailable(String productId, Boolean available);

    void deleteById(String productId);
}




