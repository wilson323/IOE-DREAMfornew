package net.lab1024.sa.consume.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.consume.dao.ConsumeProductDao;
import net.lab1024.sa.consume.domain.entity.ConsumeProductEntity;
import net.lab1024.sa.consume.domain.form.ConsumeProductQueryForm;
import net.lab1024.sa.consume.domain.vo.ConsumeProductVO;
import net.lab1024.sa.consume.service.ConsumeProductService;

/**
 * 商品管理Service实现（管理端）
 *
 * @author IOE-DREAM Team
 * @since 2025-12-13
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ConsumeProductServiceImpl implements ConsumeProductService {

    @Resource
    private ConsumeProductDao consumeProductDao;

    @Override
    @Transactional(readOnly = true)
    public PageResult<ConsumeProductVO> queryProducts(ConsumeProductQueryForm queryForm) {
        int pageNum = queryForm.getPageNum() != null && queryForm.getPageNum() > 0 ? queryForm.getPageNum() : 1;
        int pageSize = queryForm.getPageSize() != null && queryForm.getPageSize() > 0 ? queryForm.getPageSize() : 20;

        QueryWrapper<ConsumeProductEntity> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(queryForm.getKeyword())) {
            final String keyword = queryForm.getKeyword();
            if (keyword != null) {
                final String trimmedKeyword = keyword.trim();
                wrapper.and(w -> w.like("name", trimmedKeyword).or().like("code", trimmedKeyword));
            }
        }
        if (queryForm.getAvailable() != null) {
            wrapper.eq("available", queryForm.getAvailable());
        }
        wrapper.orderByDesc("id");

        Page<ConsumeProductEntity> page = new Page<>(pageNum, pageSize);
        Page<ConsumeProductEntity> pageResult = consumeProductDao.selectPage(page, wrapper);

        List<ConsumeProductVO> list = pageResult.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(list, pageResult.getTotal(), pageNum, pageSize);
    }

    @Override
    public void setAvailable(String productId, Boolean available) {
        ConsumeProductEntity entity = consumeProductDao.selectById(productId);
        if (entity == null) {
            throw new BusinessException("PRODUCT_NOT_FOUND", "商品不存在: " + productId);
        }

        entity.setAvailable(Boolean.TRUE.equals(available));
        int updated = consumeProductDao.updateById(entity);
        if (updated <= 0) {
            throw new SystemException("PRODUCT_UPDATE_FAILED", "更新商品状态失败: " + productId);
        }
    }

    @Override
    public void deleteById(String productId) {
        ConsumeProductEntity entity = consumeProductDao.selectById(productId);
        if (entity == null) {
            throw new BusinessException("PRODUCT_NOT_FOUND", "商品不存在: " + productId);
        }

        int deleted = consumeProductDao.deleteById(productId);
        if (deleted <= 0) {
            throw new SystemException("PRODUCT_DELETE_FAILED", "删除商品失败: " + productId);
        }
    }

    private ConsumeProductVO toVO(ConsumeProductEntity entity) {
        ConsumeProductVO vo = new ConsumeProductVO();
        vo.setProductId(entity.getId());
        vo.setProductCode(entity.getCode());
        vo.setProductName(entity.getName());
        vo.setImageUrl(entity.getImageUrl());
        vo.setPrice(entity.getPrice());
        vo.setStatus(Boolean.TRUE.equals(entity.getAvailable()) ? 1 : 0);

        vo.setStock(null);
        vo.setCategoryName(null);
        return vo;
    }
}




