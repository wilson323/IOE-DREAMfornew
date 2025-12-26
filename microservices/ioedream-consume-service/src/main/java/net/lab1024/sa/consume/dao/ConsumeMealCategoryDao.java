package net.lab1024.sa.consume.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.consume.domain.form.ConsumeMealCategoryQueryForm;
import net.lab1024.sa.consume.domain.vo.ConsumeMealCategoryVO;
import net.lab1024.sa.common.entity.consume.ConsumeMealCategoryEntity;

/**
 * 消费餐次分类数据访问对象
 * <p>
 * 遵循MyBatis-Plus规范，使用@Mapper注解而非@Repository
 * 提供餐次分类的完整数据访问操作
 * 支持层级结构查询和排序管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Mapper
public interface ConsumeMealCategoryDao extends BaseMapper<ConsumeMealCategoryEntity> {

    /**
     * 分页查询餐次分类
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    PageResult<ConsumeMealCategoryVO> queryPage(@Param("queryForm") ConsumeMealCategoryQueryForm queryForm);

    /**
     * 根据分类ID查询详细信息
     *
     * @param categoryId 分类ID
     * @return 分类详情
     */
    ConsumeMealCategoryVO selectDetailById(@Param("categoryId") Long categoryId);

    /**
     * 根据分类编码查询分类
     *
     * @param categoryCode 分类编码
     * @return 分类信息
     */
    ConsumeMealCategoryEntity selectByCode(@Param("categoryCode") String categoryCode);

    /**
     * 查询所有启用的餐次分类（按排序号排序）
     *
     * @return 启用的分类列表
     */
    List<ConsumeMealCategoryVO> selectAllEnabled();

    /**
     * 查询根分类列表
     *
     * @param status 状态筛选（可选）
     * @return 根分类列表
     */
    List<ConsumeMealCategoryVO> selectRootCategories(@Param("status") Integer status);

    /**
     * 根据父分类ID查询子分类列表
     *
     * @param parentId 父分类ID
     * @param status 状态筛选（可选）
     * @return 子分类列表
     */
    List<ConsumeMealCategoryVO> selectByParentId(@Param("parentId") Long parentId, @Param("status") Integer status);

    /**
     * 查询分类层级树结构
     *
     * @param status 状态筛选（可选）
     * @return 树形结构分类列表
     */
    List<ConsumeMealCategoryVO> selectCategoryTree(@Param("status") Integer status);

    /**
     * 检查分类编码是否存在
     *
     * @param categoryCode 分类编码
     * @param excludeId 排除的分类ID（用于更新时检查）
     * @return 存在数量
     */
    int countByCode(@Param("categoryCode") String categoryCode, @Param("excludeId") Long excludeId);

    /**
     * 检查是否有子分类
     *
     * @param parentId 父分类ID
     * @return 子分类数量
     */
    int countChildren(@Param("parentId") Long parentId);

    /**
     * 获取分类统计信息
     *
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 统计信息
     */
    Map<String, Object> getCategoryStatistics(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 更新分类状态
     *
     * @param categoryId 分类ID
     * @param status 新状态
     * @return 更新行数
     */
    int updateStatus(@Param("categoryId") Long categoryId, @Param("status") Integer status);

    /**
     * 更新排序号
     *
     * @param categoryId 分类ID
     * @param sortOrder 新排序号
     * @return 更新行数
     */
    int updateSortOrder(@Param("categoryId") Long categoryId, @Param("sortOrder") Integer sortOrder);

    /**
     * 批量更新排序号
     *
     * @param updates 更新列表，包含categoryId和sortOrder
     * @return 更新行数
     */
    int batchUpdateSortOrder(@Param("updates") java.util.List<Map<String, Object>> updates);

    /**
     * 查询热门餐次分类（根据使用频率排序）
     *
     * @param limit 限制数量
     * @return 热门分类列表
     */
    List<ConsumeMealCategoryVO> selectPopularCategories(@Param("limit") Integer limit);

    /**
     * 根据时间段查询可用分类
     *
     * @param currentTime 当前时间
     * @return 可用分类列表
     */
    List<ConsumeMealCategoryVO> selectAvailableByTime(@Param("currentTime") String currentTime);

    /**
     * 查询分类的消费记录数
     *
     * @param categoryId 分类ID
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 消费记录数
     */
    Long countConsumptionRecords(@Param("categoryId") Long categoryId,
                                  @Param("startDate") String startDate,
                                  @Param("endDate") String endDate);

    /**
     * 查询分类消费总金额
     *
     * @param categoryId 分类ID
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 消费总金额
     */
    java.math.BigDecimal sumConsumptionAmount(@Param("categoryId") Long categoryId,
                                             @Param("startDate") String startDate,
                                             @Param("endDate") String endDate);

    /**
     * 查询系统预设分类
     *
     * @return 系统预设分类列表
     */
    List<ConsumeMealCategoryEntity> selectSystemCategories();

    /**
     * 验证分类层级合法性
     *
     * @param parentId 父分类ID
     * @param categoryLevel 分类层级
     * @return 是否合法
     */
    boolean validateCategoryLevel(@Param("parentId") Long parentId, @Param("categoryLevel") Integer categoryLevel);

    /**
     * 获取最大排序号
     *
     * @param parentId 父分类ID（为空时查询根分类）
     * @return 最大排序号
     */
    Integer getMaxSortOrder(@Param("parentId") Long parentId);

    /**
     * 检查分类关联的业务记录
     *
     * @param categoryId 分类ID
     * @return 关联记录数量
     */
    Map<String, Long> countRelatedRecords(@Param("categoryId") Long categoryId);

    /**
     * 查询默认分类
     *
     * @return 默认分类列表
     */
    List<ConsumeMealCategoryEntity> selectDefaultCategory();

    /**
     * 清除所有默认标记
     *
     * @return 更新行数
     */
    int clearAllDefaultFlags();
}