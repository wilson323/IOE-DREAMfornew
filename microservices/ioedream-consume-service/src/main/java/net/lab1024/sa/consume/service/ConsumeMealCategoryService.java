package net.lab1024.sa.consume.service;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.consume.domain.form.ConsumeMealCategoryAddForm;
import net.lab1024.sa.consume.domain.form.ConsumeMealCategoryQueryForm;
import net.lab1024.sa.consume.domain.form.ConsumeMealCategoryUpdateForm;
import net.lab1024.sa.consume.domain.vo.ConsumeMealCategoryVO;

import java.util.List;

/**
 * 消费餐次分类服务接口（临时基础实现）
 * <p>
 * 用于快速解决编译错误
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
public interface ConsumeMealCategoryService {

    /**
     * 分页查询餐次分类
     */
    PageResult<ConsumeMealCategoryVO> queryPage(ConsumeMealCategoryQueryForm queryForm);

    /**
     * 分页查询餐次分类（别名方法，兼容性）
     */
    default PageResult<ConsumeMealCategoryVO> queryMealCategories(ConsumeMealCategoryQueryForm queryForm) {
        return queryPage(queryForm);
    }

    /**
     * 根据ID查询餐次分类
     */
    ConsumeMealCategoryVO getById(Long categoryId);

    /**
     * 获取餐次分类详情（别名方法，兼容性）
     */
    default ConsumeMealCategoryVO getMealCategoryDetail(Long categoryId) {
        return getById(categoryId);
    }

    /**
     * 添加餐次分类
     */
    ConsumeMealCategoryVO add(ConsumeMealCategoryAddForm addForm);

    /**
     * 创建餐次分类（别名方法，兼容性）
     */
    default Long createMealCategory(ConsumeMealCategoryAddForm addForm) {
        ConsumeMealCategoryVO result = add(addForm);
        return result != null ? result.getCategoryId() : null;
    }

    /**
     * 更新餐次分类
     */
    ConsumeMealCategoryVO update(ConsumeMealCategoryUpdateForm updateForm);

    /**
     * 更新餐次分类（别名方法，兼容性）
     */
    default void updateMealCategory(Long categoryId, ConsumeMealCategoryUpdateForm updateForm) {
        updateForm.setCategoryId(categoryId);
        update(updateForm);
    }

    /**
     * 删除餐次分类
     */
    void delete(Long categoryId);

    /**
     * 删除餐次分类（别名方法，兼容性）
     */
    default void deleteMealCategory(Long categoryId) {
        delete(categoryId);
    }

    /**
     * 获取所有餐次分类
     */
    List<ConsumeMealCategoryVO> getAll();

    /**
     * 获取可用餐次分类（别名方法，兼容性）
     */
    default List<ConsumeMealCategoryVO> getAvailableMealCategories() {
        return getAll();
    }

    /**
     * 获取所有餐次分类（别名方法，兼容性）
     */
    default List<ConsumeMealCategoryVO> getAllMealCategories() {
        return getAll();
    }

    // ==================== 餐次分类扩展功能 ====================

    /**
     * 启用餐次分类
     */
    void enableMealCategory(Long categoryId);

    /**
     * 禁用餐次分类
     */
    void disableMealCategory(Long categoryId);

    /**
     * 获取默认餐次分类
     */
    ConsumeMealCategoryVO getDefaultMealCategory();

    /**
     * 设置默认餐次分类
     */
    void setDefaultMealCategory(Long categoryId);

    /**
     * 设置餐次分类价格
     */
    void setMealCategoryPrices(Long categoryId, java.math.BigDecimal basePrice,
                              java.math.BigDecimal staffPrice, java.math.BigDecimal studentPrice);

    /**
     * 获取餐次分类统计
     */
    java.util.Map<String, Object> getMealCategoryStatistics();

    /**
     * 复制餐次分类
     */
    Long copyMealCategory(Long categoryId, String newName);

    /**
     * 批量操作餐次分类
     */
    void batchOperateMealCategories(java.util.List<Long> categoryIds, String operation);
}