package net.lab1024.sa.common.system.area.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.annotation.Resource;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.system.area.dao.SystemAreaDao;
import net.lab1024.sa.common.system.area.domain.entity.SystemAreaEntity;
import net.lab1024.sa.common.system.area.domain.form.SystemAreaAddForm;
import net.lab1024.sa.common.system.area.domain.form.SystemAreaQueryForm;
import net.lab1024.sa.common.system.area.domain.form.SystemAreaUpdateForm;
import net.lab1024.sa.common.system.area.domain.vo.SystemAreaVO;
import net.lab1024.sa.common.system.area.service.SystemAreaService;

/**
 * 系统区域服务实现
 *
 * @author IOE-DREAM Team
 * @since 2025-12-13
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SystemAreaServiceImpl implements SystemAreaService {

    @Resource
    private SystemAreaDao systemAreaDao;

    @Override
    @Transactional(readOnly = true)
    public PageResult<SystemAreaVO> queryPage(SystemAreaQueryForm queryForm) {
        LambdaQueryWrapper<SystemAreaEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemAreaEntity::getDeletedFlag, 0);

        if (queryForm.getKeyword() != null && !queryForm.getKeyword().trim().isEmpty()) {
            String keyword = queryForm.getKeyword().trim();
            wrapper.and(w -> w.like(SystemAreaEntity::getAreaName, keyword).or().like(SystemAreaEntity::getAreaCode, keyword));
        }
        if (queryForm.getAreaType() != null && !queryForm.getAreaType().trim().isEmpty()) {
            wrapper.eq(SystemAreaEntity::getAreaType, queryForm.getAreaType().trim());
        }
        if (queryForm.getStatus() != null) {
            wrapper.eq(SystemAreaEntity::getStatus, queryForm.getStatus());
        }

        wrapper.orderByDesc(SystemAreaEntity::getUpdateTime).orderByDesc(SystemAreaEntity::getCreateTime);

        Page<SystemAreaEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
        Page<SystemAreaEntity> resultPage = systemAreaDao.selectPage(page, wrapper);

        List<SystemAreaVO> list = resultPage.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(list, resultPage.getTotal(), queryForm.getPageNum(), queryForm.getPageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SystemAreaVO> getAreaTree() {
        LambdaQueryWrapper<SystemAreaEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemAreaEntity::getDeletedFlag, 0);
        wrapper.orderByAsc(SystemAreaEntity::getSortOrder).orderByAsc(SystemAreaEntity::getAreaId);
        List<SystemAreaEntity> areas = systemAreaDao.selectList(wrapper);
        return buildTree(areas.stream().map(this::toVO).collect(Collectors.toList()));
    }

    @Override
    @Transactional(readOnly = true)
    public SystemAreaVO getDetail(Long areaId) {
        SystemAreaEntity entity = systemAreaDao.selectById(areaId);
        if (entity == null || entity.getDeletedFlag() != null && entity.getDeletedFlag() == 1) {
            throw new ParamException("AREA_NOT_FOUND", "区域不存在");
        }
        return toVO(entity);
    }

    @Override
    public Long add(SystemAreaAddForm form) {
        SystemAreaEntity entity = new SystemAreaEntity();
        entity.setAreaName(form.getAreaName());
        entity.setAreaCode(form.getAreaCode());
        entity.setAreaType(form.getAreaType());
        entity.setParentId(form.getParentId() != null ? form.getParentId() : 0L);
        entity.setLevel(form.getLevel());
        entity.setSortOrder(form.getSortOrder());
        entity.setManagerId(form.getManagerId());
        entity.setCapacity(form.getCapacity());
        entity.setDescription(form.getDescription());
        entity.setStatus(form.getStatus() != null ? form.getStatus() : 1);
        entity.setDeletedFlag(0);
        systemAreaDao.insert(entity);
        return entity.getAreaId();
    }

    @Override
    public void update(SystemAreaUpdateForm form) {
        SystemAreaEntity exist = systemAreaDao.selectById(form.getAreaId());
        if (exist == null || exist.getDeletedFlag() != null && exist.getDeletedFlag() == 1) {
            throw new ParamException("AREA_NOT_FOUND", "区域不存在");
        }

        SystemAreaEntity entity = new SystemAreaEntity();
        entity.setAreaId(form.getAreaId());
        entity.setAreaName(form.getAreaName());
        entity.setAreaCode(form.getAreaCode());
        entity.setAreaType(form.getAreaType());
        entity.setParentId(form.getParentId() != null ? form.getParentId() : 0L);
        entity.setLevel(form.getLevel());
        entity.setSortOrder(form.getSortOrder());
        entity.setManagerId(form.getManagerId());
        entity.setCapacity(form.getCapacity());
        entity.setDescription(form.getDescription());
        entity.setStatus(form.getStatus());
        systemAreaDao.updateById(entity);
    }

    @Override
    public void delete(Long areaId) {
        systemAreaDao.deleteById(areaId);
    }

    @Override
    public void batchDelete(List<Long> areaIds) {
        if (areaIds == null || areaIds.isEmpty()) {
            return;
        }
        LambdaUpdateWrapper<SystemAreaEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(SystemAreaEntity::getAreaId, areaIds);
        systemAreaDao.delete(wrapper);
    }

    private SystemAreaVO toVO(SystemAreaEntity entity) {
        SystemAreaVO vo = new SystemAreaVO();
        vo.setAreaId(entity.getAreaId());
        vo.setAreaName(entity.getAreaName());
        vo.setAreaCode(entity.getAreaCode());
        vo.setAreaType(entity.getAreaType());
        vo.setParentId(entity.getParentId());
        vo.setLevel(entity.getLevel());
        vo.setSortOrder(entity.getSortOrder());
        vo.setManagerId(entity.getManagerId());
        vo.setCapacity(entity.getCapacity());
        vo.setDescription(entity.getDescription());
        vo.setStatus(entity.getStatus());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    private List<SystemAreaVO> buildTree(List<SystemAreaVO> list) {
        Map<Long, SystemAreaVO> idMap = new HashMap<>();
        for (SystemAreaVO vo : list) {
            idMap.put(vo.getAreaId(), vo);
        }

        List<SystemAreaVO> roots = new ArrayList<>();
        for (SystemAreaVO vo : list) {
            Long parentId = vo.getParentId();
            if (parentId == null || parentId == 0L || !idMap.containsKey(parentId)) {
                roots.add(vo);
                continue;
            }
            idMap.get(parentId).getChildren().add(vo);
        }

        Comparator<SystemAreaVO> comparator = Comparator
                .comparing((SystemAreaVO a) -> a.getSortOrder() != null ? a.getSortOrder() : Integer.MAX_VALUE)
                .thenComparing(a -> a.getAreaId() != null ? a.getAreaId() : Long.MAX_VALUE);

        sortTree(roots, comparator);
        return roots;
    }

    private void sortTree(List<SystemAreaVO> nodes, Comparator<SystemAreaVO> comparator) {
        nodes.sort(comparator);
        for (SystemAreaVO node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                sortTree(node.getChildren(), comparator);
            }
        }
    }
}
