package net.lab1024.sa.system.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import net.lab1024.sa.system.domain.entity.DepartmentEntity;

/**
 * 部门数据访问层
 * <p>
 * 严格遵循repowiki DAO规范：
 * - 继承BaseMapper
 * - 使用@Mapper和@Repository注解
 * - 提供复杂查询方法
 * - 支持软删除查询
 *
 * @author SmartAdmin Team
 * @since 2025-11-29
 */
@Mapper
@Repository
public interface DepartmentDao extends BaseMapper<DepartmentEntity> {

    /**
     * 根据部门编码查询部门
     *
     * @param departmentCode 部门编码
     * @return 部门实体
     */
    default DepartmentEntity selectByCode(String departmentCode) {
        LambdaQueryWrapper<DepartmentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DepartmentEntity::getDepartmentCode, departmentCode)
                .eq(DepartmentEntity::getDeletedFlag, 0)
                .last("LIMIT 1");
        return this.selectOne(wrapper);
    }

    /**
     * 根据部门名称查询部门
     *
     * @param departmentName 部门名称
     * @return 部门实体列表
     */
    default List<DepartmentEntity> selectByName(String departmentName) {
        LambdaQueryWrapper<DepartmentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(DepartmentEntity::getDepartmentName, departmentName)
                .eq(DepartmentEntity::getDeletedFlag, 0)
                .orderByAsc(DepartmentEntity::getSortNumber)
                .orderByAsc(DepartmentEntity::getCreateTime);
        return this.selectList(wrapper);
    }

    /**
     * 根据父部门ID查询子部门
     *
     * @param parentId 父部门ID
     * @return 子部门列表
     */
    default List<DepartmentEntity> selectByParentId(Long parentId) {
        LambdaQueryWrapper<DepartmentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DepartmentEntity::getParentId, parentId)
                .eq(DepartmentEntity::getDeletedFlag, 0)
                .orderByAsc(DepartmentEntity::getSortNumber)
                .orderByAsc(DepartmentEntity::getCreateTime);
        return this.selectList(wrapper);
    }

    /**
     * 根据层级查询部门
     *
     * @param level 部门层级
     * @return 部门列表
     */
    default List<DepartmentEntity> selectByLevel(Integer level) {
        LambdaQueryWrapper<DepartmentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DepartmentEntity::getLevel, level)
                .eq(DepartmentEntity::getDeletedFlag, 0)
                .orderByAsc(DepartmentEntity::getSortNumber);
        return this.selectList(wrapper);
    }

    /**
     * 根据状态查询部门
     *
     * @param status 部门状态
     * @return 部门列表
     */
    default List<DepartmentEntity> selectByStatus(Integer status) {
        LambdaQueryWrapper<DepartmentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DepartmentEntity::getStatus, status)
                .eq(DepartmentEntity::getDeletedFlag, 0)
                .orderByAsc(DepartmentEntity::getSortNumber)
                .orderByAsc(DepartmentEntity::getCreateTime);
        return this.selectList(wrapper);
    }

    /**
     * 查询所有启用状态的部门（用于下拉选择）
     *
     * @return 启用的部门列表
     */
    default List<DepartmentEntity> selectAllEnabled() {
        LambdaQueryWrapper<DepartmentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DepartmentEntity::getStatus, 1)
                .eq(DepartmentEntity::getDeletedFlag, 0)
                .orderByAsc(DepartmentEntity::getSortNumber)
                .orderByAsc(DepartmentEntity::getCreateTime);
        return this.selectList(wrapper);
    }

    /**
     * 检查部门编码是否存在
     *
     * @param departmentCode 部门编码
     * @param excludeId      排除的部门ID（用于更新时检查）
     * @return 存在的数量
     */
    default int countByCode(@Param("departmentCode") String departmentCode, @Param("excludeId") Long excludeId) {
        LambdaQueryWrapper<DepartmentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DepartmentEntity::getDepartmentCode, departmentCode)
                .eq(DepartmentEntity::getDeletedFlag, 0);

        if (excludeId != null && excludeId > 0) {
            wrapper.ne(DepartmentEntity::getDepartmentId, excludeId);
        }

        Long count = this.selectCount(wrapper);
        return count != null ? count.intValue() : 0;
    }

    /**
     * 检查部门名称是否存在
     *
     * @param departmentName 部门名称
     * @param parentId       父部门ID
     * @param excludeId      排除的部门ID（用于更新时检查）
     * @return 存在的数量
     */
    default int countByName(@Param("departmentName") String departmentName,
            @Param("parentId") Long parentId,
            @Param("excludeId") Long excludeId) {
        LambdaQueryWrapper<DepartmentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DepartmentEntity::getDepartmentName, departmentName)
                .eq(DepartmentEntity::getParentId, parentId)
                .eq(DepartmentEntity::getDeletedFlag, 0);

        if (excludeId != null && excludeId > 0) {
            wrapper.ne(DepartmentEntity::getDepartmentId, excludeId);
        }

        Long count = this.selectCount(wrapper);
        return count != null ? count.intValue() : 0;
    }

    /**
     * 检查是否存在子部门
     *
     * @param parentId 父部门ID
     * @return 子部门数量
     */
    default int countChildren(@Param("parentId") Long parentId) {
        LambdaQueryWrapper<DepartmentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DepartmentEntity::getParentId, parentId)
                .eq(DepartmentEntity::getDeletedFlag, 0);
        Long count = this.selectCount(wrapper);
        return count != null ? count.intValue() : 0;
    }

    /**
     * 根据负责人ID查询部门
     *
     * @param managerUserId 负责人ID
     * @return 部门列表
     */
    default List<DepartmentEntity> selectByManager(Long managerUserId) {
        LambdaQueryWrapper<DepartmentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DepartmentEntity::getManagerUserId, managerUserId)
                .eq(DepartmentEntity::getDeletedFlag, 0)
                .orderByAsc(DepartmentEntity::getSortNumber);
        return this.selectList(wrapper);
    }

    /**
     * 根据路径前缀查询部门及其子部门
     *
     * @param pathPrefix 路径前缀
     * @return 部门列表
     */
    default List<DepartmentEntity> selectByPathPrefix(String pathPrefix) {
        LambdaQueryWrapper<DepartmentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(DepartmentEntity::getDepartmentPath, pathPrefix)
                .eq(DepartmentEntity::getDeletedFlag, 0)
                .orderByAsc(DepartmentEntity::getLevel)
                .orderByAsc(DepartmentEntity::getSortNumber);
        return this.selectList(wrapper);
    }

    /**
     * 分页查询部门
     *
     * @param page           分页对象
     * @param departmentName 部门名称（模糊）
     * @param departmentCode 部门编码（精确）
     * @param parentId       父部门ID
     * @param status         状态
     * @param manager        管理员（模糊）
     * @return 分页结果
     */
    default IPage<DepartmentEntity> selectPageByCondition(Page<DepartmentEntity> page,
            @Param("departmentName") String departmentName,
            @Param("departmentCode") String departmentCode,
            @Param("parentId") Long parentId,
            @Param("status") Integer status,
            @Param("manager") String manager) {
        LambdaQueryWrapper<DepartmentEntity> wrapper = new LambdaQueryWrapper<>();

        if (departmentName != null && !departmentName.trim().isEmpty()) {
            wrapper.like(DepartmentEntity::getDepartmentName, departmentName);
        }
        if (departmentCode != null && !departmentCode.trim().isEmpty()) {
            wrapper.eq(DepartmentEntity::getDepartmentCode, departmentCode);
        }
        if (parentId != null) {
            wrapper.eq(DepartmentEntity::getParentId, parentId);
        }
        if (status != null) {
            wrapper.eq(DepartmentEntity::getStatus, status);
        }
        if (manager != null && !manager.trim().isEmpty()) {
            wrapper.like(DepartmentEntity::getManager, manager);
        }

        wrapper.eq(DepartmentEntity::getDeletedFlag, 0)
                .orderByAsc(DepartmentEntity::getSortNumber)
                .orderByAsc(DepartmentEntity::getCreateTime);

        return this.selectPage(page, wrapper);
    }

    /**
     * 逻辑删除部门（及其子部门）
     *
     * @param departmentId 部门ID
     * @param updateUserId 更新人ID
     * @return 影响行数
     */
    default int logicDeleteWithChildren(@Param("departmentId") Long departmentId,
            @Param("updateUserId") Long updateUserId) {
        // 这里需要实现复杂的逻辑删除逻辑，建议使用Service层处理
        // 或者使用自定义SQL语句
        DepartmentEntity entity = new DepartmentEntity();
        entity.setDeletedFlag(1);
        entity.setUpdateUserId(updateUserId);

        LambdaQueryWrapper<DepartmentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DepartmentEntity::getDepartmentId, departmentId)
                .or()
                .likeRight(DepartmentEntity::getDepartmentPath, departmentId + ",")
                .eq(DepartmentEntity::getDeletedFlag, 0);

        return this.update(entity, wrapper);
    }
}
