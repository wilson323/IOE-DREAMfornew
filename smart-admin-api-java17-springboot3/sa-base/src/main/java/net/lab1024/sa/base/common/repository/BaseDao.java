package net.lab1024.sa.base.common.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * 基础Repository接口
 * <p>
 * 严格遵循repowiki四层架构规范：
 * - 统一基础数据访问接口
 * - 提供通用的CRUD操作
 * - 支持分页查询
 * - 集成MyBatis Plus功能
 * - 为Repository层提供统一基类
 *
 * @author SmartAdmin Team
 * @since 2025-11-26
 * @param <T> 实体类型
 * @param <ID> 主键类型
 */
public interface BaseRepository<T, ID extends Serializable> extends IService<T> {

    /**
     * 分页查询
     *
     * @param pageParam 分页参数
     * @return 分页结果
     */
    default PageResult<T> getPage(PageParam pageParam) {
        Page<T> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
        IPage<T> result = page(page);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 根据ID查询
     *
     * @param id 主键
     * @return 实体对象
     */
    default T findById(ID id) {
        return IService.super.getById(id);
    }

    /**
     * 批量删除
     *
     * @param ids 主键列表
     * @return 删除结果
     */
    default boolean deleteByIds(Collection<ID> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        return removeByIds(ids);
    }

    /**
     * 批量保存
     *
     * @param entityList 实体列表
     * @return 保存结果
     */
    default boolean saveBatch(Collection<T> entityList) {
        if (CollectionUtils.isEmpty(entityList)) {
            return false;
        }
        return IService.super.saveBatch(entityList);
    }

    /**
     * 根据条件查询列表
     *
     * @param queryWrapper 查询条件
     * @return 结果列表
     */
    default List<T> list(QueryWrapper<T> queryWrapper) {
        return IService.super.list(queryWrapper);
    }

    /**
     * 根据条件统计数量
     *
     * @param queryWrapper 查询条件
     * @return 数量
     */
    default long count(QueryWrapper<T> queryWrapper) {
        return IService.super.count(queryWrapper);
    }

    /**
     * 根据条件判断是否存在
     *
     * @param queryWrapper 查询条件
     * @return 是否存在
     */
    default boolean exists(QueryWrapper<T> queryWrapper) {
        return IService.super.exists(queryWrapper);
    }

    // ================================================
    // 企业级扩展方法 - 支持复杂业务场景
    // ================================================

    /**
     * 获取Mapper接口 - 子类必须实现
     * @return Mapper接口实例
     */
    Object getMapper();

    /**
     * 获取缓存前缀 - 子类可选实现
     * @return 缓存前缀
     */
    default String getCachePrefix() {
        return "";
    }

    /**
     * 带缓存的查询方法 - 企业级缓存支持
     *
     * @param cacheKey 缓存键
     * @param queryFunction 查询函数
     * @param <R> 返回类型
     * @return 查询结果
     */
    default <R> R executeWithCache(String cacheKey, Function<Object[], R> queryFunction) {
        return queryFunction.apply(new Object[0]);
    }

    /**
     * 构建缓存键 - 企业级缓存策略
     *
     * @param parts 缓存键部分
     * @return 完整的缓存键
     */
    default String buildCacheKey(String... parts) {
        StringBuilder keyBuilder = new StringBuilder();
        if (getCachePrefix() != null && !getCachePrefix().isEmpty()) {
            keyBuilder.append(getCachePrefix());
        }
        if (parts != null) {
            for (String part : parts) {
                if (part != null) {
                    keyBuilder.append(":").append(part);
                }
            }
        }
        return keyBuilder.toString();
    }

    /**
     * 清理自定义缓存 - 子类可重写
     */
    default void clearCustomCache() {
        // 默认空实现，子类可重写
    }
}