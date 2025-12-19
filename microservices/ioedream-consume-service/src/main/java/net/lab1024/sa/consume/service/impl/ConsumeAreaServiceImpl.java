package net.lab1024.sa.consume.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.consume.dao.ConsumeAreaDao;
import net.lab1024.sa.consume.domain.form.ConsumeAreaAddForm;
import net.lab1024.sa.consume.domain.form.ConsumeAreaQueryForm;
import net.lab1024.sa.consume.domain.form.ConsumeAreaUpdateForm;
import net.lab1024.sa.consume.domain.vo.ConsumeAreaTreeVO;
import net.lab1024.sa.consume.domain.vo.ConsumeAreaVO;
import net.lab1024.sa.consume.entity.ConsumeAreaEntity;
import net.lab1024.sa.consume.manager.ConsumeAreaManager;
import net.lab1024.sa.consume.service.ConsumeAreaService;

/**
 * 消费区域服务实现类
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Service注解
 * - 使用@Resource注入依赖
 * - 使用@Transactional管理事务
 * - 遵循四层架构：Controller → Service → Manager → DAO
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ConsumeAreaServiceImpl implements ConsumeAreaService {

    @Resource
    private ConsumeAreaDao consumeAreaDao;

    @Resource
    private ConsumeAreaManager consumeAreaManager;

    @Override
    @Observed(name = "consume.area.addArea", contextualName = "consume-area-add")
    public ResponseDTO<Long> addArea(ConsumeAreaAddForm form) {
        log.info("[区域管理] 添加区域，areaName={}, areaCode={}", form.getAreaName(), form.getAreaCode());

        try {
            // 1. 验证区域编码唯一性
            ConsumeAreaEntity existing = consumeAreaDao.selectByCode(form.getAreaCode());
            if (existing != null) {
                throw new BusinessException("AREA_CODE_EXISTS", "区域编码已存在：" + form.getAreaCode());
            }

            // 2. 构建实体
            ConsumeAreaEntity entity = new ConsumeAreaEntity();
            entity.setAreaName(form.getAreaName());
            entity.setAreaCode(form.getAreaCode());
            entity.setParentId(form.getParentId() != null && form.getParentId() > 0 ? form.getParentId() : 0L);
            entity.setAreaType(form.getAreaType());
            entity.setManageMode(form.getManageMode());
            entity.setAreaSubType(form.getAreaSubType());
            entity.setFixedValueConfig(form.getFixedValueConfig());
            entity.setMealCategories(form.getMealCategories());
            entity.setOpenTime(form.getOpenTime());
            entity.setCloseTime(form.getCloseTime());
            entity.setAddress(form.getAddress());
            entity.setContactName(form.getContactName());
            entity.setContactPhone(form.getContactPhone());
            entity.setGpsLocation(form.getGpsLocation());
            entity.setSortOrder(form.getSortOrder() != null ? form.getSortOrder() : 0);
            entity.setInventoryFlag(form.getInventoryFlag() != null ? form.getInventoryFlag() : 0);
            entity.setStatus(1); // 默认启用
            entity.setRemark(form.getRemark());

            // 3. 计算层级和路径
            calculateLevelAndPath(entity);

            // 4. 保存
            int inserted = consumeAreaDao.insert(entity);
            if (inserted <= 0) {
                throw new SystemException("AREA_INSERT_FAILED", "添加区域失败");
            }

            log.info("[区域管理] 添加区域成功，areaId={}, areaCode={}", entity.getId(), entity.getAreaCode());
            return ResponseDTO.ok(entity.getId());

        } catch (BusinessException e) {
            log.warn("[区域管理] 添加区域业务异常，areaCode={}, code={}, message={}", form.getAreaCode(), e.getCode(),
                    e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[区域管理] 添加区域系统异常，areaCode={}", form.getAreaCode(), e);
            throw new SystemException("AREA_ADD_ERROR", "添加区域失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "consume.area.updateArea", contextualName = "consume-area-update")
    @CacheEvict(value = "consume:area:tree", allEntries = true)
    public ResponseDTO<Void> updateArea(ConsumeAreaUpdateForm form) {
        log.info("[区域管理] 更新区域，areaId={}", form.getId());

        try {
            // 1. 查询区域
            ConsumeAreaEntity entity = consumeAreaDao.selectById(form.getId());
            if (entity == null) {
                throw new BusinessException("AREA_NOT_FOUND", "区域不存在：" + form.getId());
            }

            // 2. 如果编码变更，验证唯一性
            if (!entity.getAreaCode().equals(form.getAreaCode())) {
                ConsumeAreaEntity existing = consumeAreaDao.selectByCode(form.getAreaCode());
                if (existing != null && !existing.getId().equals(form.getId())) {
                    throw new BusinessException("AREA_CODE_EXISTS", "区域编码已存在：" + form.getAreaCode());
                }
            }

            // 3. 更新字段
            entity.setAreaName(form.getAreaName());
            entity.setAreaCode(form.getAreaCode());
            entity.setParentId(form.getParentId() != null && form.getParentId() > 0 ? form.getParentId() : 0L);
            entity.setAreaType(form.getAreaType());
            entity.setManageMode(form.getManageMode());
            entity.setAreaSubType(form.getAreaSubType());
            entity.setFixedValueConfig(form.getFixedValueConfig());
            entity.setMealCategories(form.getMealCategories());
            entity.setOpenTime(form.getOpenTime());
            entity.setCloseTime(form.getCloseTime());
            entity.setAddress(form.getAddress());
            entity.setContactName(form.getContactName());
            entity.setContactPhone(form.getContactPhone());
            entity.setGpsLocation(form.getGpsLocation());
            entity.setSortOrder(form.getSortOrder());
            entity.setInventoryFlag(form.getInventoryFlag());
            if (form.getStatus() != null) {
                entity.setStatus(form.getStatus());
            }
            entity.setRemark(form.getRemark());

            // 4. 重新计算层级和路径
            calculateLevelAndPath(entity);

            // 5. 更新
            int updated = consumeAreaDao.updateById(entity);
            if (updated <= 0) {
                throw new SystemException("AREA_UPDATE_FAILED", "更新区域失败");
            }

            log.info("[区域管理] 更新区域成功，areaId={}", form.getId());
            return ResponseDTO.ok();

        } catch (BusinessException e) {
            log.warn("[区域管理] 更新区域业务异常，areaId={}, code={}, message={}", form.getId(), e.getCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[区域管理] 更新区域系统异常，areaId={}", form.getId(), e);
            throw new SystemException("AREA_UPDATE_ERROR", "更新区域失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "consume.area.deleteArea", contextualName = "consume-area-delete")
    @CacheEvict(value = "consume:area:tree", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> deleteArea(Long id) {
        log.info("[区域管理] 删除区域，areaId={}", id);

        try {
            // 1. 检查是否存在子区域
            List<ConsumeAreaEntity> children = consumeAreaDao.selectByParentId(id);
            if (children != null && !children.isEmpty()) {
                throw new BusinessException("AREA_HAS_CHILDREN", "区域存在子区域，不能删除");
            }

            // 2. 逻辑删除
            ConsumeAreaEntity entity = consumeAreaDao.selectById(id);
            if (entity == null) {
                throw new BusinessException("AREA_NOT_FOUND", "区域不存在：" + id);
            }

            entity.setDeletedFlag(1);
            int deleted = consumeAreaDao.updateById(entity);
            if (deleted <= 0) {
                throw new SystemException("AREA_DELETE_FAILED", "删除区域失败");
            }

            log.info("[区域管理] 删除区域成功，areaId={}", id);
            return ResponseDTO.ok();

        } catch (BusinessException e) {
            log.warn("[区域管理] 删除区域业务异常，areaId={}, code={}, message={}", id, e.getCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[区域管理] 删除区域系统异常，areaId={}", id, e);
            throw new SystemException("AREA_DELETE_ERROR", "删除区域失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "consume.area.getAreaById", contextualName = "consume-area-get-by-id")
    @Transactional(readOnly = true)
    @Cacheable(value = "consume:area:detail", key = "'area:' + #id")
    public ResponseDTO<ConsumeAreaVO> getAreaById(Long id) {
        log.debug("[区域管理] 根据ID查询区域，areaId={}", id);

        ConsumeAreaEntity entity = consumeAreaDao.selectById(id);
        if (entity == null || entity.getDeletedFlag() != null && entity.getDeletedFlag() == 1) {
            throw new BusinessException("AREA_NOT_FOUND", "区域不存在：" + id);
        }

        ConsumeAreaVO vo = convertToVO(entity);
        return ResponseDTO.ok(vo);
    }

    @Override
    @Observed(name = "consume.area.getAreaByCode", contextualName = "consume-area-get-by-code")
    @Transactional(readOnly = true)
    public ResponseDTO<ConsumeAreaVO> getAreaByCode(String areaCode) {
        log.debug("[区域管理] 根据编码查询区域，areaCode={}", areaCode);

        ConsumeAreaEntity entity = consumeAreaDao.selectByCode(areaCode);
        if (entity == null) {
            throw new BusinessException("AREA_NOT_FOUND", "区域不存在：" + areaCode);
        }

        ConsumeAreaVO vo = convertToVO(entity);
        return ResponseDTO.ok(vo);
    }

    @Override
    @Observed(name = "consume.area.queryAreaPage", contextualName = "consume-area-query-page")
    @Transactional(readOnly = true)
    public ResponseDTO<PageResult<ConsumeAreaVO>> queryAreaPage(ConsumeAreaQueryForm form) {
        log.info("[区域管理] 分页查询区域，form={}", form);

        int pageNum = form.getPageNum() != null && form.getPageNum() > 0 ? form.getPageNum() : 1;
        int pageSize = form.getPageSize() != null && form.getPageSize() > 0 ? form.getPageSize() : 20;

        LambdaQueryWrapper<ConsumeAreaEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsumeAreaEntity::getDeletedFlag, 0);

        if (StringUtils.hasText(form.getAreaName())) {
            wrapper.like(ConsumeAreaEntity::getAreaName, form.getAreaName().trim());
        }
        if (StringUtils.hasText(form.getAreaCode())) {
            wrapper.eq(ConsumeAreaEntity::getAreaCode, form.getAreaCode().trim());
        }
        if (form.getParentId() != null) {
            wrapper.eq(ConsumeAreaEntity::getParentId, form.getParentId());
        }
        if (form.getAreaType() != null) {
            wrapper.eq(ConsumeAreaEntity::getAreaType, form.getAreaType());
        }
        if (form.getManageMode() != null) {
            wrapper.eq(ConsumeAreaEntity::getManageMode, form.getManageMode());
        }
        if (form.getStatus() != null) {
            wrapper.eq(ConsumeAreaEntity::getStatus, form.getStatus());
        }

        wrapper.orderByAsc(ConsumeAreaEntity::getSortOrder)
                .orderByAsc(ConsumeAreaEntity::getCreateTime);

        Page<ConsumeAreaEntity> page = new Page<>(pageNum, pageSize);
        Page<ConsumeAreaEntity> pageResult = consumeAreaDao.selectPage(page, wrapper);

        List<ConsumeAreaVO> list = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        PageResult<ConsumeAreaVO> result = new PageResult<>();
        result.setList(list);
        result.setTotal(pageResult.getTotal());
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setPages((int) pageResult.getPages());

        return ResponseDTO.ok(result);
    }

    @Override
    @Observed(name = "consume.area.getAreaTree", contextualName = "consume-area-get-tree")
    @Transactional(readOnly = true)
    @Cacheable(value = "consume:area:tree", key = "'tree:' + (#parentId != null ? #parentId : 'root')")
    public ResponseDTO<List<ConsumeAreaTreeVO>> getAreaTree(Long parentId) {
        log.info("[区域管理] 查询区域树，parentId={}", parentId);

        Long searchParentId = (parentId != null && parentId > 0) ? parentId : 0L;
        List<ConsumeAreaEntity> areas = consumeAreaDao.selectByParentId(searchParentId);

        List<ConsumeAreaTreeVO> tree = areas.stream()
                .filter(area -> area.getDeletedFlag() == null || area.getDeletedFlag() == 0)
                .map(this::convertToTreeVO)
                .collect(Collectors.toList());

        // 递归加载子节点
        for (ConsumeAreaTreeVO node : tree) {
            loadChildren(node);
        }

        return ResponseDTO.ok(tree);
    }

    @Override
    @Observed(name = "consume.area.getAreaChildren", contextualName = "consume-area-get-children")
    @Transactional(readOnly = true)
    public ResponseDTO<List<ConsumeAreaVO>> getAreaChildren(Long parentId) {
        log.debug("[区域管理] 查询子区域列表，parentId={}", parentId);

        Long searchParentId = (parentId != null && parentId > 0) ? parentId : 0L;
        List<ConsumeAreaEntity> areas = consumeAreaDao.selectByParentId(searchParentId);

        List<ConsumeAreaVO> list = areas.stream()
                .filter(area -> area.getDeletedFlag() == null || area.getDeletedFlag() == 0)
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return ResponseDTO.ok(list);
    }

    @Override
    @Observed(name = "consume.area.updateAreaStatus", contextualName = "consume-area-update-status")
    @CacheEvict(value = { "consume:area:tree", "consume:area:detail" }, allEntries = true)
    public ResponseDTO<Void> updateAreaStatus(Long id, Integer status) {
        log.info("[区域管理] 更新区域状态，areaId={}, status={}", id, status);

        ConsumeAreaEntity entity = consumeAreaDao.selectById(id);
        if (entity == null) {
            throw new BusinessException("AREA_NOT_FOUND", "区域不存在：" + id);
        }

        entity.setStatus(status);
        int updated = consumeAreaDao.updateById(entity);
        if (updated <= 0) {
            throw new SystemException("AREA_UPDATE_STATUS_FAILED", "更新区域状态失败");
        }

        return ResponseDTO.ok();
    }

    /**
     * 计算区域层级和完整路径
     *
     * @param entity 区域实体
     */
    private void calculateLevelAndPath(ConsumeAreaEntity entity) {
        if (entity.getParentId() == null || entity.getParentId() == 0) {
            // 顶级区域
            entity.setLevel(1);
            entity.setFullPath("/" + entity.getId() + "/");
        } else {
            // 查询父区域
            ConsumeAreaEntity parent = consumeAreaDao.selectById(entity.getParentId());
            if (parent == null) {
                throw new BusinessException("PARENT_AREA_NOT_FOUND", "父级区域不存在：" + entity.getParentId());
            }
            entity.setLevel(parent.getLevel() + 1);
            String parentPath = parent.getFullPath() != null ? parent.getFullPath() : "/" + parent.getId() + "/";
            entity.setFullPath(parentPath + entity.getId() + "/");
        }
    }

    /**
     * 递归加载子节点
     *
     * @param node 当前节点
     */
    private void loadChildren(ConsumeAreaTreeVO node) {
        List<ConsumeAreaEntity> children = consumeAreaDao.selectByParentId(node.getId());
        if (children != null && !children.isEmpty()) {
            List<ConsumeAreaTreeVO> childNodes = children.stream()
                    .filter(area -> area.getDeletedFlag() == null || area.getDeletedFlag() == 0)
                    .map(this::convertToTreeVO)
                    .collect(Collectors.toList());

            node.setChildren(childNodes);
            node.setHasChildren(!childNodes.isEmpty());

            // 递归加载子节点的子节点
            for (ConsumeAreaTreeVO child : childNodes) {
                loadChildren(child);
            }
        } else {
            node.setChildren(new ArrayList<>());
            node.setHasChildren(false);
        }
    }

    /**
     * 将Entity转换为VO
     *
     * @param entity 区域实体
     * @return 区域VO
     */
    private ConsumeAreaVO convertToVO(ConsumeAreaEntity entity) {
        if (entity == null) {
            return null;
        }

        ConsumeAreaVO vo = new ConsumeAreaVO();
        vo.setId(entity.getId());
        vo.setAreaName(entity.getAreaName());
        vo.setAreaCode(entity.getAreaCode());
        vo.setParentId(entity.getParentId());
        vo.setFullPath(entity.getFullPath());
        vo.setLevel(entity.getLevel());
        vo.setAreaType(entity.getAreaType());
        vo.setAreaTypeName(getAreaTypeName(entity.getAreaType()));
        vo.setManageMode(entity.getManageMode());
        vo.setManageModeName(getManageModeName(entity.getManageMode()));
        vo.setAreaSubType(entity.getAreaSubType());
        vo.setFixedValueConfig(entity.getFixedValueConfig());
        vo.setMealCategories(entity.getMealCategories());
        vo.setOpenTime(entity.getOpenTime());
        vo.setCloseTime(entity.getCloseTime());
        vo.setAddress(entity.getAddress());
        vo.setContactName(entity.getContactName());
        vo.setContactPhone(entity.getContactPhone());
        vo.setGpsLocation(entity.getGpsLocation());
        vo.setSortOrder(entity.getSortOrder());
        vo.setInventoryFlag(entity.getInventoryFlag());
        vo.setStatus(entity.getStatus());
        vo.setStatusName(getStatusName(entity.getStatus()));
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());

        return vo;
    }

    /**
     * 将Entity转换为TreeVO
     *
     * @param entity 区域实体
     * @return 区域树VO
     */
    private ConsumeAreaTreeVO convertToTreeVO(ConsumeAreaEntity entity) {
        ConsumeAreaTreeVO treeVO = new ConsumeAreaTreeVO();
        ConsumeAreaVO vo = convertToVO(entity);
        if (vo != null) {
            // 复制VO属性到TreeVO
            treeVO.setId(vo.getId());
            treeVO.setAreaName(vo.getAreaName());
            treeVO.setAreaCode(vo.getAreaCode());
            treeVO.setParentId(vo.getParentId());
            treeVO.setFullPath(vo.getFullPath());
            treeVO.setLevel(vo.getLevel());
            treeVO.setAreaType(vo.getAreaType());
            treeVO.setAreaTypeName(vo.getAreaTypeName());
            treeVO.setManageMode(vo.getManageMode());
            treeVO.setManageModeName(vo.getManageModeName());
            treeVO.setStatus(vo.getStatus());
            treeVO.setStatusName(vo.getStatusName());
            treeVO.setSortOrder(vo.getSortOrder());
            treeVO.setCreateTime(vo.getCreateTime());
            treeVO.setUpdateTime(vo.getUpdateTime());
        }
        treeVO.setChildren(new ArrayList<>());
        treeVO.setHasChildren(false);
        return treeVO;
    }

    /**
     * 获取区域类型名称
     *
     * @param areaType 区域类型
     * @return 类型名称
     */
    private String getAreaTypeName(Integer areaType) {
        if (areaType == null) {
            return "未知";
        }
        switch (areaType) {
            case 1:
                return "餐饮";
            case 2:
                return "超市";
            case 3:
                return "自助";
            default:
                return "未知";
        }
    }

    /**
     * 获取经营模式名称
     *
     * @param manageMode 经营模式
     * @return 模式名称
     */
    private String getManageModeName(Integer manageMode) {
        if (manageMode == null) {
            return "未知";
        }
        switch (manageMode) {
            case 1:
                return "餐别制";
            case 2:
                return "超市制";
            case 3:
                return "混合模式";
            default:
                return "未知";
        }
    }

    /**
     * 获取状态名称
     *
     * @param status 状态
     * @return 状态名称
     */
    private String getStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "禁用";
            case 1:
                return "启用";
            default:
                return "未知";
        }
    }
}
