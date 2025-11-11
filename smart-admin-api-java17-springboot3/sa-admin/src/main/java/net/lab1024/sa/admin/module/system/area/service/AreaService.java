package net.lab1024.sa.admin.module.system.area.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import net.lab1024.sa.admin.module.system.area.dao.AreaDao;
import net.lab1024.sa.admin.module.system.area.domain.entity.AreaEntity;
import net.lab1024.sa.admin.module.system.area.domain.form.AreaAddForm;
import net.lab1024.sa.admin.module.system.area.domain.form.AreaQueryForm;
import net.lab1024.sa.admin.module.system.area.domain.form.AreaUpdateForm;
import net.lab1024.sa.admin.module.system.area.domain.vo.AreaTreeVO;
import net.lab1024.sa.admin.module.system.area.domain.vo.AreaVO;
import net.lab1024.sa.admin.module.system.area.manager.AreaManager;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.exception.BusinessException;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartRequestUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 区域Service
 *
 * @author SmartAdmin
 * @date 2025-01-10
 */
@Service
public class AreaService {

    @Resource
    private AreaDao areaDao;

    @Resource
    private AreaManager areaManager;

    /**
     * 分页查询区域
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    public PageResult<AreaVO> queryPage(AreaQueryForm queryForm) {
        return areaManager.queryPage(queryForm);
    }

    /**
     * 获取区域树
     *
     * @return 区域树
     */
    public List<AreaTreeVO> getAreaTree() {
        return areaManager.getAreaTree();
    }

    /**
     * 获取区域详情
     *
     * @param areaId 区域ID
     * @return 区域详情
     */
    public AreaVO getDetail(Long areaId) {
        AreaVO areaVO = areaDao.selectAreaVO(areaId);
        if (areaVO == null) {
            throw new BusinessException("区域不存在");
        }
        return areaVO;
    }

    /**
     * 新增区域
     *
     * @param addForm 新增表单
     * @return 区域ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long add(AreaAddForm addForm) {
        // 1. 检查区域编码是否重复
        if (areaManager.checkAreaCodeExists(addForm.getAreaCode(), null)) {
            throw new BusinessException("区域编码已存在");
        }

        // 2. 转换实体
        AreaEntity areaEntity = SmartBeanUtil.copy(addForm, AreaEntity.class);
        areaEntity.setDeletedFlag(0);
        areaEntity.setVersion(1);
        areaEntity.setCreateTime(LocalDateTime.now());
        areaEntity.setUpdateTime(LocalDateTime.now());

        // 设置创建人
        Long currentUserId = SmartRequestUtil.getRequestUserId();
        if (currentUserId != null) {
            areaEntity.setCreateUserId(currentUserId);
            areaEntity.setUpdateUserId(currentUserId);
        }

        // 3. 设置默认值
        if (addForm.getStatus() == null) {
            areaEntity.setStatus(1);
        }
        if (addForm.getSortOrder() == null) {
            areaEntity.setSortOrder(0);
        }
        if (addForm.getAreaLevel() == null) {
            // 如果没有指定层级,根据父区域自动计算
            if (addForm.getParentId() != null && addForm.getParentId() != 0) {
                AreaEntity parentArea = areaDao.selectById(addForm.getParentId());
                if (parentArea != null) {
                    areaEntity.setAreaLevel(parentArea.getAreaLevel() + 1);
                }
            } else {
                areaEntity.setAreaLevel(1);
            }
        }

        // 4. 保存
        areaDao.insert(areaEntity);

        return areaEntity.getAreaId();
    }

    /**
     * 更新区域
     *
     * @param updateForm 更新表单
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(AreaUpdateForm updateForm) {
        // 1. 检查区域是否存在
        AreaEntity existingArea = areaDao.selectById(updateForm.getAreaId());
        if (existingArea == null || existingArea.getDeletedFlag() == 1) {
            throw new BusinessException("区域不存在");
        }

        // 2. 检查区域编码是否重复（排除自己）
        if (areaManager.checkAreaCodeExists(updateForm.getAreaCode(), updateForm.getAreaId())) {
            throw new BusinessException("区域编码已存在");
        }

        // 3. 检查父区域是否为自己或子区域
        if (updateForm.getParentId().equals(updateForm.getAreaId())) {
            throw new BusinessException("父区域不能是自己");
        }

        // 4. 转换实体
        AreaEntity areaEntity = SmartBeanUtil.copy(updateForm, AreaEntity.class);
        areaEntity.setUpdateTime(LocalDateTime.now());

        // 设置更新人
        Long currentUserId = SmartRequestUtil.getRequestUserId();
        if (currentUserId != null) {
            areaEntity.setUpdateUserId(currentUserId);
        }

        // 5. 更新
        areaDao.updateById(areaEntity);
    }

    /**
     * 删除区域（软删除）
     *
     * @param areaId 区域ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long areaId) {
        // 1. 检查区域是否存在
        AreaEntity existingArea = areaDao.selectById(areaId);
        if (existingArea == null || existingArea.getDeletedFlag() == 1) {
            throw new BusinessException("区域不存在");
        }

        // 2. 检查是否有子区域
        Integer childCount = areaDao.countByParentId(areaId);
        if (childCount > 0) {
            throw new BusinessException("存在子区域,无法删除");
        }

        // TODO: 检查是否有关联的设备或人员

        // 3. 软删除
        AreaEntity updateEntity = new AreaEntity();
        updateEntity.setAreaId(areaId);
        updateEntity.setDeletedFlag(1);
        updateEntity.setUpdateTime(LocalDateTime.now());

        // 设置更新人
        Long currentUserId = SmartRequestUtil.getRequestUserId();
        if (currentUserId != null) {
            updateEntity.setUpdateUserId(currentUserId);
        }

        areaDao.updateById(updateEntity);
    }

    /**
     * 批量删除区域
     *
     * @param areaIds 区域ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> areaIds) {
        for (Long areaId : areaIds) {
            delete(areaId);
        }
    }

}
