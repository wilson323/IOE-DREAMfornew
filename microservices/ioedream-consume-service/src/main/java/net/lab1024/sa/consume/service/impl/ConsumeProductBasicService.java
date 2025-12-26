package net.lab1024.sa.consume.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.consume.dao.ConsumeProductDao;
import net.lab1024.sa.common.entity.consume.ConsumeProductEntity;
import net.lab1024.sa.consume.domain.form.ConsumeProductAddForm;
import net.lab1024.sa.consume.domain.form.ConsumeProductQueryForm;
import net.lab1024.sa.consume.domain.form.ConsumeProductUpdateForm;
import net.lab1024.sa.consume.domain.vo.ConsumeProductVO;
import net.lab1024.sa.consume.exception.ConsumeProductException;
import net.lab1024.sa.consume.manager.ConsumeProductManager;

import lombok.extern.slf4j.Slf4j;

/**
 * 消费产品基础服务
 * <p>
 * 负责产品的基础CRUD操作
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ConsumeProductBasicService {

    @Resource
    private ConsumeProductDao productDao;

    @Resource
    private ConsumeProductManager productManager;

    /**
     * 添加产品
     */
    public ConsumeProductVO add (@Valid ConsumeProductAddForm addForm) {
        log.info ("[产品基础] 添加产品: {}", addForm.getProductName ());

        try {
            // 1. 数据验证
            validateProductData (addForm);

            // 2. 转换实体
            ConsumeProductEntity entity = new ConsumeProductEntity ();
            BeanUtils.copyProperties (addForm, entity);
            entity.setCreateTime (LocalDateTime.now ());
            entity.setUpdateTime (LocalDateTime.now ());

            // 3. 保存产品
            productDao.insert (entity);

            // 4. 转换VO
            ConsumeProductVO vo = new ConsumeProductVO ();
            BeanUtils.copyProperties (entity, vo);

            log.info ("[产品基础] 产品添加成功: productId={}, productName={}", entity.getProductId (), entity.getProductName ());
            return vo;

        } catch (Exception e) {
            log.error ("[产品基础] 添加产品失败: productName={}, error={}", addForm.getProductName (), e.getMessage (), e);
            throw ConsumeProductException.databaseError ("添加产品", e.getMessage ());
        }
    }

    /**
     * 更新产品
     */
    public ConsumeProductVO update (@Valid ConsumeProductUpdateForm updateForm) {
        log.info ("[产品基础] 更新产品: {}", updateForm.getProductId ());

        try {
            // 1. 检查产品是否存在
            ConsumeProductEntity existingEntity = productDao.selectById (updateForm.getProductId ());
            if (existingEntity == null) {
                throw ConsumeProductException.notFound (updateForm.getProductId ());
            }

            // 2. 数据验证
            validateProductData (updateForm);

            // 3. 转换实体
            ConsumeProductEntity entity = new ConsumeProductEntity ();
            BeanUtils.copyProperties (updateForm, entity);
            entity.setUpdateTime (LocalDateTime.now ());

            // 4. 更新产品
            productDao.updateById (entity);

            // 5. 转换VO
            ConsumeProductVO vo = new ConsumeProductVO ();
            BeanUtils.copyProperties (entity, vo);

            log.info ("[产品基础] 产品更新成功: productId={}, productName={}", entity.getProductId (), entity.getProductName ());
            return vo;

        } catch (Exception e) {
            log.error ("[产品基础] 更新产品失败: productId={}, error={}", updateForm.getProductId (), e.getMessage (), e);
            throw ConsumeProductException.databaseError ("更新产品", e.getMessage ());
        }
    }

    /**
     * 删除产品
     */
    public void delete (Long productId) {
        log.info ("[产品基础] 删除产品: {}", productId);

        try {
            // 1. 检查产品是否存在
            ConsumeProductEntity entity = productDao.selectById (productId);
            if (entity == null) {
                throw ConsumeProductException.notFound (productId);
            }

            // 2. 检查是否可以删除
            Map<String, Object> canDeleteResult = productManager.checkDeleteProduct (productId);
            if (!(Boolean) canDeleteResult.get ("canDelete")) {
                throw ConsumeProductException.businessRuleViolation ("产品无法删除: " + canDeleteResult.get ("reason"));
            }

            // 3. 删除产品
            productDao.deleteById (productId);

            log.info ("[产品基础] 产品删除成功: productId={}, productName={}", productId, entity.getProductName ());

        } catch (Exception e) {
            log.error ("[产品基础] 删除产品失败: productId={}, error={}", productId, e.getMessage (), e);
            throw ConsumeProductException.databaseError ("删除产品", e.getMessage ());
        }
    }

    /**
     * 批量删除产品
     */
    public Integer batchDelete (List<Long> productIds) {
        log.info ("[产品基础] 批量删除产品: {}", productIds);

        int successCount = 0;

        try {
            for (Long productId : productIds) {
                try {
                    delete (productId);
                    successCount++;
                } catch (Exception e) {
                    log.warn ("[产品基础] 批量删除产品时跳过失败项: productId={}, error={}", productId, e.getMessage ());
                }
            }

            log.info ("[产品基础] 批量删除产品完成: totalCount={}, successCount={}", productIds.size (), successCount);
            return successCount;

        } catch (Exception e) {
            log.error ("[产品基础] 批量删除产品失败: productIds={}, error={}", productIds, e.getMessage (), e);
            throw ConsumeProductException.businessRuleViolation ("批量删除产品失败: " + e.getMessage ());
        }
    }

    /**
     * 上架产品
     */
    public void putOnSale (Long productId) {
        log.info ("[产品基础] 产品上架: {}", productId);

        try {
            ConsumeProductEntity entity = productDao.selectById (productId);
            if (entity == null) {
                throw ConsumeProductException.notFound (productId);
            }

            if (entity.getStock () <= 0) {
                throw ConsumeProductException.stockViolation ("库存不足，无法上架");
            }

            entity.setStatus (1); // 上架状态
            entity.setUpdateTime (LocalDateTime.now ());
            productDao.updateById (entity);

            log.info ("[产品基础] 产品上架成功: productId={}, productName={}", productId, entity.getProductName ());

        } catch (Exception e) {
            log.error ("[产品基础] 产品上架失败: productId={}, error={}", productId, e.getMessage (), e);
            throw ConsumeProductException.businessRuleViolation ("产品上架失败: " + e.getMessage ());
        }
    }

    /**
     * 下架产品
     */
    public void putOffSale (Long productId) {
        log.info ("[产品基础] 产品下架: {}", productId);

        try {
            ConsumeProductEntity entity = productDao.selectById (productId);
            if (entity == null) {
                throw ConsumeProductException.notFound (productId);
            }

            entity.setStatus (0); // 下架状态
            entity.setUpdateTime (LocalDateTime.now ());
            productDao.updateById (entity);

            log.info ("[产品基础] 产品下架成功: productId={}, productName={}", productId, entity.getProductName ());

        } catch (Exception e) {
            log.error ("[产品基础] 产品下架失败: productId={}, error={}", productId, e.getMessage (), e);
            throw ConsumeProductException.businessRuleViolation ("产品下架失败: " + e.getMessage ());
        }
    }

    /**
     * 批量更新产品状态
     */
    public Integer batchUpdateStatus (List<Long> productIds, Integer status) {
        log.info ("[产品基础] 批量更新产品状态: productIds={}, status={}", productIds, status);

        int successCount = 0;

        try {
            for (Long productId : productIds) {
                try {
                    ConsumeProductEntity entity = productDao.selectById (productId);
                    if (entity != null) {
                        entity.setStatus (status);
                        entity.setUpdateTime (LocalDateTime.now ());
                        productDao.updateById (entity);
                        successCount++;
                    }
                } catch (Exception e) {
                    log.warn ("[产品基础] 批量更新状态时跳过失败项: productId={}, error={}", productId, e.getMessage ());
                }
            }

            log.info ("[产品基础] 批量更新产品状态完成: totalCount={}, successCount={}", productIds.size (), successCount);
            return successCount;

        } catch (Exception e) {
            log.error ("[产品基础] 批量更新产品状态失败: productIds={}, status={}, error={}", productIds, status, e.getMessage (), e);
            throw ConsumeProductException.businessRuleViolation ("批量更新产品状态失败: " + e.getMessage ());
        }
    }

    /**
     * 验证产品数据
     */
    private void validateProductData (Object formData) {
        // 可以在这里添加通用的数据验证逻辑
        // 例如：检查产品名称、价格、库存等
    }

    // ==================== 缺失方法补充 ====================

    /**
     * 分页查询产品
     */
    public PageResult<ConsumeProductVO> queryPage (ConsumeProductQueryForm queryForm) {
        log.info ("[产品基础服务] 分页查询产品: queryForm={}", queryForm);

        try {
            LambdaQueryWrapper<ConsumeProductEntity> wrapper = new LambdaQueryWrapper<> ();

            // 构建查询条件
            if (StringUtils.hasText (queryForm.getProductName ())) {
                wrapper.like (ConsumeProductEntity::getProductName, queryForm.getProductName ());
            }
            if (queryForm.getCategoryId () != null) {
                wrapper.eq (ConsumeProductEntity::getCategoryId, queryForm.getCategoryId ());
            }
            if (queryForm.getStatus () != null) {
                wrapper.eq (ConsumeProductEntity::getStatus, queryForm.getStatus ());
            }

            // 排序
            wrapper.orderByDesc (ConsumeProductEntity::getCreateTime);

            // 分页查询
            Page<ConsumeProductEntity> page = new Page<> (queryForm.getPageNum (), queryForm.getPageSize ());
            Page<ConsumeProductEntity> result = productDao.selectPage (page, wrapper);

            // 转换为VO
            List<ConsumeProductVO> voList = result.getRecords ().stream ().map (this::convertToVO)
                    .collect (Collectors.toList ());

            return PageResult.of (voList, result.getTotal (), queryForm.getPageNum (), queryForm.getPageSize ());

        } catch (Exception e) {
            log.error ("[产品基础服务] 分页查询产品失败: queryForm={}, error={}", queryForm, e.getMessage (), e);
            throw ConsumeProductException.databaseError ("分页查询", e.getMessage ());
        }
    }

    /**
     * 根据ID查询产品
     */
    public ConsumeProductVO getById (Long productId) {
        log.info ("[产品基础服务] 根据ID查询产品: {}", productId);

        ConsumeProductEntity entity = productDao.selectById (productId);
        if (entity == null) {
            throw ConsumeProductException.notFound (productId);
        }

        return convertToVO (entity);
    }

    /**
     * 获取所有在售产品
     */
    public List<ConsumeProductVO> getAllOnSale () {
        log.info ("[产品基础服务] 获取所有在售产品");

        LambdaQueryWrapper<ConsumeProductEntity> wrapper = new LambdaQueryWrapper<> ();
        wrapper.eq (ConsumeProductEntity::getStatus, 1).orderByDesc (ConsumeProductEntity::getCreateTime);

        List<ConsumeProductEntity> entities = productDao.selectList (wrapper);
        return entities.stream ().map (this::convertToVO).collect (Collectors.toList ());
    }

    /**
     * 获取推荐产品
     */
    public List<ConsumeProductVO> getRecommendedProducts (Integer limit) {
        log.info ("[产品基础服务] 获取推荐产品: limit={}", limit);

        if (limit == null || limit <= 0) {
            limit = 10;
        }

        LambdaQueryWrapper<ConsumeProductEntity> wrapper = new LambdaQueryWrapper<> ();
        wrapper.eq (ConsumeProductEntity::getIsRecommended, 1).eq (ConsumeProductEntity::getStatus, 1)
                .orderByDesc (ConsumeProductEntity::getCreateTime).last ("LIMIT " + limit);

        List<ConsumeProductEntity> entities = productDao.selectList (wrapper);
        return entities.stream ().map (this::convertToVO).collect (Collectors.toList ());
    }

    /**
     * 根据分类ID获取产品
     */
    public List<ConsumeProductVO> getByCategoryId (Long categoryId) {
        log.info ("[产品基础服务] 根据分类ID获取产品: categoryId={}", categoryId);

        LambdaQueryWrapper<ConsumeProductEntity> wrapper = new LambdaQueryWrapper<> ();
        wrapper.eq (ConsumeProductEntity::getCategoryId, categoryId).eq (ConsumeProductEntity::getStatus, 1)
                .orderByDesc (ConsumeProductEntity::getCreateTime);

        List<ConsumeProductEntity> entities = productDao.selectList (wrapper);
        return entities.stream ().map (this::convertToVO).collect (Collectors.toList ());
    }

    /**
     * 获取热销产品
     */
    public List<ConsumeProductVO> getHotSales (Integer limit) {
        log.info ("[产品基础服务] 获取热销产品: limit={}", limit);

        if (limit == null || limit <= 0) {
            limit = 20;
        }

        // 简单实现：按创建时间排序，实际应该按销量排序
        LambdaQueryWrapper<ConsumeProductEntity> wrapper = new LambdaQueryWrapper<> ();
        wrapper.eq (ConsumeProductEntity::getStatus, 1).orderByDesc (ConsumeProductEntity::getCreateTime)
                .last ("LIMIT " + limit);

        List<ConsumeProductEntity> entities = productDao.selectList (wrapper);
        return entities.stream ().map (this::convertToVO).collect (Collectors.toList ());
    }

    /**
     * 获取高分评价产品
     */
    public List<ConsumeProductVO> getHighRated (Integer limit, BigDecimal minRating) {
        log.info ("[产品基础服务] 获取高分评价产品: limit={}, minRating={}", limit, minRating);

        if (limit == null || limit <= 0) {
            limit = 10;
        }
        if (minRating == null) {
            minRating = new BigDecimal ("4.0");
        }

        LambdaQueryWrapper<ConsumeProductEntity> wrapper = new LambdaQueryWrapper<> ();
        wrapper.ge (ConsumeProductEntity::getRating, minRating).eq (ConsumeProductEntity::getStatus, 1)
                .orderByDesc (ConsumeProductEntity::getRating).last ("LIMIT " + limit);

        List<ConsumeProductEntity> entities = productDao.selectList (wrapper);
        return entities.stream ().map (this::convertToVO).collect (Collectors.toList ());
    }

    /**
     * 搜索产品
     */
    public List<ConsumeProductVO> searchProducts (String keyword, Integer limit) {
        log.info ("[产品基础服务] 搜索产品: keyword={}, limit={}", keyword, limit);

        if (!StringUtils.hasText (keyword)) {
            return new ArrayList<> ();
        }

        if (limit == null || limit <= 0) {
            limit = 20;
        }

        LambdaQueryWrapper<ConsumeProductEntity> wrapper = new LambdaQueryWrapper<> ();
        wrapper.and (w -> w.like (ConsumeProductEntity::getProductName, keyword).or ()
                .like (ConsumeProductEntity::getDescription, keyword)).eq (ConsumeProductEntity::getStatus, 1)
                .orderByDesc (ConsumeProductEntity::getCreateTime).last ("LIMIT " + limit);

        List<ConsumeProductEntity> entities = productDao.selectList (wrapper);
        return entities.stream ().map (this::convertToVO).collect (Collectors.toList ());
    }

    /**
     * 实体转VO
     */
    private ConsumeProductVO convertToVO (ConsumeProductEntity entity) {
        ConsumeProductVO vo = new ConsumeProductVO ();
        BeanUtils.copyProperties (entity, vo);
        return vo;
    }
}
