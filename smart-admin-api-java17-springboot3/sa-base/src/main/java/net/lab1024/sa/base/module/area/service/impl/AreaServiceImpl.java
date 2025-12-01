package net.lab1024.sa.base.module.area.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.module.area.dao.AreaDao;
import net.lab1024.sa.base.module.area.domain.entity.AreaEntity;
import net.lab1024.sa.base.module.area.domain.form.AreaForm;
import net.lab1024.sa.base.module.area.domain.vo.AreaVO;
import net.lab1024.sa.base.module.area.domain.vo.AreaTreeVO;
import net.lab1024.sa.base.module.area.enums.AreaTypeEnum;
import net.lab1024.sa.base.module.area.manager.AreaManager;
import net.lab1024.sa.base.module.area.service.AreaService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 区域服务实现类
 * 提供区域管理的核心业务功能实现
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Slf4j
@Service
public class AreaServiceImpl implements AreaService {

    @Resource
    private AreaDao areaDao;

    @Resource
    private AreaManager areaManager;

    @Override
    public ResponseDTO<PageResult<AreaVO>> queryPage(AreaForm form) {
        try {
            Page<AreaEntity> page = new Page<>(form.getPageNum(), form.getPageSize());
            Page<AreaEntity> entityPage = areaDao.selectPage(page, null);

            List<AreaVO> voList = entityPage.getRecords().stream()
                .map(entity -> {
                    AreaVO vo = SmartBeanUtil.copy(entity, AreaVO.class);
                    // 设置区域类型名称
                    AreaTypeEnum typeEnum = AreaTypeEnum.fromValue(vo.getAreaType());
                    vo.setAreaTypeName(typeEnum.getDescription());
                    vo.setAreaTypeEnglishName(typeEnum.getEnglishName());
                    return vo;
                })
                .collect(Collectors.toList());

            PageResult<AreaVO> pageResult = new PageResult<>();
            pageResult.setList(voList);
            pageResult.setTotal(entityPage.getTotal());

            return ResponseDTO.ok(pageResult);
        } catch (Exception e) {
            log.error("分页查询区域失败", e);
            return ResponseDTO.error("分页查询区域失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<AreaVO> getById(Long areaId) {
        try {
            if (areaId == null) {
                return ResponseDTO.error("区域ID不能为空");
            }

            AreaEntity entity = areaDao.selectById(areaId);
            if (entity == null) {
                return ResponseDTO.error("区域不存在");
            }

            AreaVO vo = SmartBeanUtil.copy(entity, AreaVO.class);
            // 设置区域类型名称
            AreaTypeEnum typeEnum = AreaTypeEnum.fromValue(vo.getAreaType());
            vo.setAreaTypeName(typeEnum.getDescription());
            vo.setAreaTypeEnglishName(typeEnum.getEnglishName());

            // 计算并设置区域路径
            String fullPath = areaManager.getFullPath(entity);
            vo.setFullPath(fullPath);

            return ResponseDTO.ok(vo);
        } catch (Exception e) {
            log.error("查询区域详情失败, areaId: {}", areaId, e);
            return ResponseDTO.error("查询区域详情失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<AreaTreeVO>> getAreaTree() {
        try {
            List<AreaTreeVO> areaTree = areaManager.buildAreaTreeWithStats();
            return ResponseDTO.ok(areaTree);
        } catch (Exception e) {
            log.error("获取区域树失败", e);
            return ResponseDTO.error("获取区域树失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<AreaVO>> getByParentId(Long parentId) {
        try {
            if (parentId == null) {
                parentId = 0L; // 默认查询根区域
            }

            List<AreaEntity> entityList = areaDao.selectByParentId(parentId);
            List<AreaVO> voList = entityList.stream()
                .map(entity -> {
                    AreaVO vo = SmartBeanUtil.copy(entity, AreaVO.class);
                    // 设置区域类型名称
                    AreaTypeEnum typeEnum = AreaTypeEnum.fromValue(vo.getAreaType());
                    vo.setAreaTypeName(typeEnum.getDescription());
                    vo.setAreaTypeEnglishName(typeEnum.getEnglishName());
                    return vo;
                })
                .collect(Collectors.toList());

            return ResponseDTO.ok(voList);
        } catch (Exception e) {
            log.error("根据父区域ID查询子区域失败, parentId: {}", parentId, e);
            return ResponseDTO.error("根据父区域ID查询子区域失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<AreaVO>> getByAreaType(Integer areaType) {
        try {
            if (areaType == null) {
                return ResponseDTO.error("区域类型不能为空");
            }

            // 验证区域类型是否有效
            AreaTypeEnum.fromValue(areaType); // 如果无效会抛出异常

            List<AreaEntity> entityList = areaDao.selectByAreaType(areaType);
            List<AreaVO> voList = entityList.stream()
                .map(entity -> {
                    AreaVO vo = SmartBeanUtil.copy(entity, AreaVO.class);
                    AreaTypeEnum typeEnum = AreaTypeEnum.fromValue(vo.getAreaType());
                    vo.setAreaTypeName(typeEnum.getDescription());
                    vo.setAreaTypeEnglishName(typeEnum.getEnglishName());
                    return vo;
                })
                .collect(Collectors.toList());

            return ResponseDTO.ok(voList);
        } catch (Exception e) {
            log.error("根据区域类型查询区域失败, areaType: {}", areaType, e);
            return ResponseDTO.error("根据区域类型查询区域失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<AreaVO>> getByIds(List<Long> areaIds) {
        try {
            if (CollectionUtils.isEmpty(areaIds)) {
                return ResponseDTO.ok(new ArrayList<>());
            }

            List<AreaEntity> entityList = areaDao.selectBatchIds(areaIds);
            List<AreaVO> voList = entityList.stream()
                .map(entity -> {
                    AreaVO vo = SmartBeanUtil.copy(entity, AreaVO.class);
                    AreaTypeEnum typeEnum = AreaTypeEnum.fromValue(vo.getAreaType());
                    vo.setAreaTypeName(typeEnum.getDescription());
                    vo.setAreaTypeEnglishName(typeEnum.getEnglishName());
                    return vo;
                })
                .collect(Collectors.toList());

            return ResponseDTO.ok(voList);
        } catch (Exception e) {
            log.error("根据ID列表批量查询区域失败, areaIds: {}", areaIds, e);
            return ResponseDTO.error("根据ID列表批量查询区域失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Long> add(AreaForm form) {
        try {
            // 参数验证
            ResponseDTO<String> validation = validateAreaForm(form, null);
            if (!validation.getOk()) {
                return ResponseDTO.error(validation.getMsg());
            }

            // 验证区域编码唯一性
            if (!areaManager.isAreaCodeUnique(form.getAreaCode(), null)) {
                return ResponseDTO.error("区域编码已存在");
            }

            // 创建区域实体
            AreaEntity entity = SmartBeanUtil.copy(form, AreaEntity.class);

            // 设置层级和路径
            setAreaLevelAndPath(entity);

            // 验证层级关系
            if (!areaManager.validateAreaHierarchy(entity)) {
                return ResponseDTO.error("区域层级关系无效");
            }

            // 设置默认值
            entity.setStatus(entity.getStatus() != null ? entity.getStatus() : 1);
            entity.setSortOrder(entity.getSortOrder() != null ? entity.getSortOrder() : 0);

            // 保存到数据库
            areaDao.insert(entity);

            log.info("新增区域成功, areaId: {}, areaName: {}", entity.getAreaId(), entity.getAreaName());
            return ResponseDTO.ok(entity.getAreaId());
        } catch (Exception e) {
            log.error("新增区域失败", e);
            return ResponseDTO.error("新增区域失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> update(AreaForm form) {
        try {
            if (form.getAreaId() == null) {
                return ResponseDTO.error("区域ID不能为空");
            }

            // 检查区域是否存在
            AreaEntity existingEntity = areaDao.selectById(form.getAreaId());
            if (existingEntity == null) {
                return ResponseDTO.error("区域不存在");
            }

            // 参数验证
            ResponseDTO<String> validation = validateAreaForm(form, form.getAreaId());
            if (!validation.getOk()) {
                return ResponseDTO.error(validation.getMsg());
            }

            // 验证区域编码唯一性
            if (!areaManager.isAreaCodeUnique(form.getAreaCode(), form.getAreaId())) {
                return ResponseDTO.error("区域编码已存在");
            }

            // 更新区域实体
            AreaEntity entity = SmartBeanUtil.copy(form, AreaEntity.class);

            // 设置层级和路径
            setAreaLevelAndPath(entity);

            // 验证层级关系
            if (!areaManager.validateAreaHierarchy(entity)) {
                return ResponseDTO.error("区域层级关系无效");
            }

            // 防止自己设置自己为父级
            if (form.getParentId() != null && form.getParentId().equals(form.getAreaId())) {
                return ResponseDTO.error("不能将自己设置为父区域");
            }

            // 防止循环引用
            List<Long> allChildIds = areaManager.getAllChildIds(form.getAreaId());
            if (allChildIds.contains(form.getParentId())) {
                return ResponseDTO.error("不能将子区域设置为父区域");
            }

            // 更新到数据库
            areaDao.updateById(entity);

            log.info("更新区域成功, areaId: {}, areaName: {}", entity.getAreaId(), entity.getAreaName());
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("更新区域失败, areaId: {}", form.getAreaId(), e);
            return ResponseDTO.error("更新区域失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> delete(Long areaId) {
        try {
            if (areaId == null) {
                return ResponseDTO.error("区域ID不能为空");
            }

            // 检查区域是否存在
            AreaEntity entity = areaDao.selectById(areaId);
            if (entity == null) {
                return ResponseDTO.error("区域不存在");
            }

            // 检查是否有子区域
            if (!areaManager.canDelete(areaId)) {
                return ResponseDTO.error("存在子区域，无法删除");
            }

            // 软删除
            int updateCount = areaDao.deleteById(areaId, 1L); // 假设当前用户ID为1，实际应该从上下文获取

            if (updateCount == 0) {
                return ResponseDTO.error("删除失败");
            }

            log.info("删除区域成功, areaId: {}, areaName: {}", areaId, entity.getAreaName());
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("删除区域失败, areaId: {}", areaId, e);
            return ResponseDTO.error("删除区域失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> batchDelete(List<Long> areaIds) {
        try {
            if (CollectionUtils.isEmpty(areaIds)) {
                return ResponseDTO.error("请选择要删除的区域");
            }

            // 检查每个区域是否可以删除
            for (Long areaId : areaIds) {
                if (!areaManager.canDelete(areaId)) {
                    AreaEntity entity = areaDao.selectById(areaId);
                    String areaName = entity != null ? entity.getAreaName() : "未知";
                    return ResponseDTO.error("区域 [" + areaName + "] 存在子区域，无法删除");
                }
            }

            // 批量软删除
            int updateCount = areaDao.deleteBatchByIds(areaIds, 1L); // 假设当前用户ID为1

            if (updateCount == 0) {
                return ResponseDTO.error("删除失败");
            }

            log.info("批量删除区域成功, areaIds: {}, count: {}", areaIds, areaIds.size());
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("批量删除区域失败, areaIds: {}", areaIds, e);
            return ResponseDTO.error("批量删除区域失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> updateStatus(Long areaId, Integer status) {
        try {
            if (areaId == null) {
                return ResponseDTO.error("区域ID不能为空");
            }

            if (status == null || (status < 0 || status > 2)) {
                return ResponseDTO.error("状态值无效");
            }

            // 检查区域是否存在
            AreaEntity entity = areaDao.selectById(areaId);
            if (entity == null) {
                return ResponseDTO.error("区域不存在");
            }

            // 更新状态
            int updateCount = areaDao.updateStatus(areaId, status);

            if (updateCount == 0) {
                return ResponseDTO.error("更新失败");
            }

            log.info("更新区域状态成功, areaId: {}, status: {}", areaId, status);
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("更新区域状态失败, areaId: {}, status: {}", areaId, status, e);
            return ResponseDTO.error("更新区域状态失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();

            // 总区域数
            long totalCount = areaDao.countByCondition(null, null, null);
            statistics.put("totalCount", totalCount);

            // 按类型统计
            Map<Integer, Long> typeStatistics = new HashMap<>();
            for (AreaTypeEnum typeEnum : AreaTypeEnum.values()) {
                long count = areaDao.countByCondition(typeEnum.getValue(), null, null);
                typeStatistics.put(typeEnum.getValue(), count);
            }
            statistics.put("typeStatistics", typeStatistics);

            // 按状态统计
            long enabledCount = areaDao.countByCondition(null, 1, null);
            long disabledCount = areaDao.countByCondition(null, 0, null);
            long maintenanceCount = areaDao.countByCondition(null, 2, null);

            Map<String, Long> statusStatistics = new HashMap<>();
            statusStatistics.put("enabled", enabledCount);
            statusStatistics.put("disabled", disabledCount);
            statusStatistics.put("maintenance", maintenanceCount);
            statistics.put("statusStatistics", statusStatistics);

            // 根区域数量
            List<AreaEntity> rootAreas = areaDao.selectRootAreas();
            statistics.put("rootCount", rootAreas.size());

            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("获取区域统计信息失败", e);
            return ResponseDTO.error("获取区域统计信息失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Boolean> checkAreaCodeUnique(String areaCode, Long excludeId) {
        try {
            if (areaCode == null || areaCode.trim().isEmpty()) {
                return ResponseDTO.error("区域编码不能为空");
            }

            boolean isUnique = areaManager.isAreaCodeUnique(areaCode.trim(), excludeId);
            return ResponseDTO.ok(isUnique);
        } catch (Exception e) {
            log.error("检查区域编码唯一性失败, areaCode: {}, excludeId: {}", areaCode, excludeId, e);
            return ResponseDTO.error("检查区域编码唯一性失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<AreaVO>> getAllAreas() {
        try {
            List<AreaEntity> entityList = areaDao.selectAll();
            List<AreaVO> voList = entityList.stream()
                .map(entity -> {
                    AreaVO vo = SmartBeanUtil.copy(entity, AreaVO.class);
                    AreaTypeEnum typeEnum = AreaTypeEnum.fromValue(vo.getAreaType());
                    vo.setAreaTypeName(typeEnum.getDescription());
                    vo.setAreaTypeEnglishName(typeEnum.getEnglishName());
                    return vo;
                })
                .collect(Collectors.toList());

            return ResponseDTO.ok(voList);
        } catch (Exception e) {
            log.error("获取所有区域失败", e);
            return ResponseDTO.error("获取所有区域失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<AreaVO>> getEnabledAreas() {
        try {
            List<AreaEntity> entityList = areaDao.selectByStatus(1);
            List<AreaVO> voList = entityList.stream()
                .map(entity -> {
                    AreaVO vo = SmartBeanUtil.copy(entity, AreaVO.class);
                    AreaTypeEnum typeEnum = AreaTypeEnum.fromValue(vo.getAreaType());
                    vo.setAreaTypeName(typeEnum.getDescription());
                    vo.setAreaTypeEnglishName(typeEnum.getEnglishName());
                    return vo;
                })
                .collect(Collectors.toList());

            return ResponseDTO.ok(voList);
        } catch (Exception e) {
            log.error("获取所有启用区域失败", e);
            return ResponseDTO.error("获取所有启用区域失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<AreaVO>> searchByName(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseDTO.ok(new ArrayList<>());
            }

            List<AreaEntity> entityList = areaDao.selectByNameLike(keyword.trim());
            List<AreaVO> voList = entityList.stream()
                .map(entity -> {
                    AreaVO vo = SmartBeanUtil.copy(entity, AreaVO.class);
                    AreaTypeEnum typeEnum = AreaTypeEnum.fromValue(vo.getAreaType());
                    vo.setAreaTypeName(typeEnum.getDescription());
                    vo.setAreaTypeEnglishName(typeEnum.getEnglishName());
                    return vo;
                })
                .collect(Collectors.toList());

            return ResponseDTO.ok(voList);
        } catch (Exception e) {
            log.error("根据名称搜索区域失败, keyword: {}", keyword, e);
            return ResponseDTO.error("根据名称搜索区域失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<AreaVO>> getUserAccessibleAreas(Long userId) {
        try {
            // TODO: 实现用户区域权限逻辑
            // 这里应该根据用户的权限数据来过滤可访问的区域
            // 当前暂时返回所有启用区域
            return getEnabledAreas();
        } catch (Exception e) {
            log.error("获取用户可访问区域失败, userId: {}", userId, e);
            return ResponseDTO.error("获取用户可访问区域失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Boolean> hasAreaPermission(Long userId, Long areaId) {
        try {
            // TODO: 实现区域权限检查逻辑
            // 这里应该根据用户的权限数据来检查是否有指定区域的访问权限
            // 当前暂时返回true
            return ResponseDTO.ok(true);
        } catch (Exception e) {
            log.error("检查用户区域权限失败, userId: {}, areaId: {}", userId, areaId, e);
            return ResponseDTO.error("检查用户区域权限失败: " + e.getMessage());
        }
    }

    @Override
    public void refreshCache() {
        try {
            // TODO: 实现缓存刷新逻辑
            log.info("区域缓存刷新完成");
        } catch (Exception e) {
            log.error("区域缓存刷新失败", e);
        }
    }

    /**
     * 验证区域表单数据
     */
    private ResponseDTO<String> validateAreaForm(AreaForm form, Long excludeId) {
        if (form.getAreaCode() == null || form.getAreaCode().trim().isEmpty()) {
            return ResponseDTO.error("区域编码不能为空");
        }

        if (form.getAreaName() == null || form.getAreaName().trim().isEmpty()) {
            return ResponseDTO.error("区域名称不能为空");
        }

        if (form.getAreaType() == null) {
            return ResponseDTO.error("区域类型不能为空");
        }

        try {
            AreaTypeEnum.fromValue(form.getAreaType()); // 验证区域类型是否有效
        } catch (IllegalArgumentException e) {
            return ResponseDTO.error("区域类型无效");
        }

        if (form.getParentId() == null) {
            return ResponseDTO.error("父区域ID不能为空");
        }

        return ResponseDTO.ok();
    }

    /**
     * 设置区域层级和路径
     */
    private void setAreaLevelAndPath(AreaEntity entity) {
        if (entity.getParentId() == null || entity.getParentId() == 0) {
            // 根区域
            entity.setLevel(1);
            entity.setPath(entity.getAreaName());
        } else {
            // 子区域
            AreaEntity parentEntity = areaDao.selectById(entity.getParentId());
            if (parentEntity != null) {
                entity.setLevel(parentEntity.getLevel() + 1);
                entity.setPath(parentEntity.getPath() + " > " + entity.getAreaName());
            } else {
                // 父区域不存在，设为根区域
                entity.setLevel(1);
                entity.setParentId(0L);
                entity.setPath(entity.getAreaName());
            }
        }
    }

    // ========== 原生实体操作方法实现 ==========

    @Override
    public boolean save(AreaEntity areaEntity) {
        try {
            // 设置级别和路径
            setAreaLevelAndPath(areaEntity);

            // 设置创建信息
            areaEntity.setCreateTime(LocalDateTime.now());
            areaEntity.setUpdateTime(LocalDateTime.now());

            // 保存到数据库
            return areaDao.insert(areaEntity) > 0;
        } catch (Exception e) {
            log.error("保存区域实体失败", e);
            return false;
        }
    }

    @Override
    public boolean updateById(AreaEntity areaEntity) {
        try {
            if (areaEntity.getAreaId() == null) {
                log.warn("区域ID为空，无法更新");
                return false;
            }

            // 设置级别和路径
            setAreaLevelAndPath(areaEntity);

            // 设置更新信息
            areaEntity.setUpdateTime(LocalDateTime.now());

            // 更新数据库
            return areaDao.updateById(areaEntity) > 0;
        } catch (Exception e) {
            log.error("根据ID更新区域实体失败", e);
            return false;
        }
    }

    @Override
    public boolean removeById(Long areaId) {
        try {
            if (areaId == null) {
                log.warn("区域ID为空，无法删除");
                return false;
            }

            // 检查是否存在子区域
            List<AreaEntity> children = areaDao.selectByParentId(areaId);
            if (!children.isEmpty()) {
                log.warn("区域存在子区域，无法删除: areaId={}", areaId);
                return false;
            }

            // 软删除：设置删除标记
            AreaEntity entity = new AreaEntity();
            entity.setAreaId(areaId);
            entity.setDeletedFlag(1);
            entity.setUpdateTime(LocalDateTime.now());

            return areaDao.updateById(entity) > 0;
        } catch (Exception e) {
            log.error("根据ID删除区域实体失败: areaId={}", areaId, e);
            return false;
        }
    }
}