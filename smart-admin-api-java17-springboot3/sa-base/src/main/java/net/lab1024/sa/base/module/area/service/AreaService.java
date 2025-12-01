package net.lab1024.sa.base.module.area.service;

import java.util.List;
import java.util.Map;

import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.module.area.domain.entity.AreaEntity;
import net.lab1024.sa.base.module.area.domain.form.AreaForm;
import net.lab1024.sa.base.module.area.domain.vo.AreaVO;
import net.lab1024.sa.base.module.area.domain.vo.AreaTreeVO;

/**
 * 区域服务接口
 * 提供区域管理的核心业务功能
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
public interface AreaService {

    /**
     * 分页查询区域
     *
     * @param form 查询条件
     * @return 分页结果
     */
    ResponseDTO<PageResult<AreaVO>> queryPage(AreaForm form);

    /**
     * 根据ID查询区域
     *
     * @param areaId 区域ID
     * @return 区域信息
     */
    ResponseDTO<AreaVO> getById(Long areaId);

    /**
     * 获取区域树形结构
     *
     * @return 区域树
     */
    ResponseDTO<List<AreaTreeVO>> getAreaTree();

    /**
     * 根据父区域ID查询子区域
     *
     * @param parentId 父区域ID
     * @return 子区域列表
     */
    ResponseDTO<List<AreaVO>> getByParentId(Long parentId);

    /**
     * 根据区域类型查询区域
     *
     * @param areaType 区域类型
     * @return 区域列表
     */
    ResponseDTO<List<AreaVO>> getByAreaType(Integer areaType);

    /**
     * 根据区域ID列表批量查询区域
     *
     * @param areaIds 区域ID列表
     * @return 区域列表
     */
    ResponseDTO<List<AreaVO>> getByIds(List<Long> areaIds);

    /**
     * 新增区域
     *
     * @param form 区域表单
     * @return 区域ID
     */
    ResponseDTO<Long> add(AreaForm form);

    /**
     * 更新区域
     *
     * @param form 区域表单
     * @return 操作结果
     */
    ResponseDTO<Void> update(AreaForm form);

    /**
     * 删除区域
     *
     * @param areaId 区域ID
     * @return 操作结果
     */
    ResponseDTO<Void> delete(Long areaId);

    /**
     * 批量删除区域
     *
     * @param areaIds 区域ID列表
     * @return 操作结果
     */
    ResponseDTO<Void> batchDelete(List<Long> areaIds);

    /**
     * 更新区域状态
     *
     * @param areaId 区域ID
     * @param status 状态
     * @return 操作结果
     */
    ResponseDTO<Void> updateStatus(Long areaId, Integer status);

    /**
     * 获取区域统计信息
     *
     * @return 统计信息
     */
    ResponseDTO<Map<String, Object>> getStatistics();

    /**
     * 检查区域编码是否唯一
     *
     * @param areaCode 区域编码
     * @param excludeId 排除的ID
     * @return 是否唯一
     */
    ResponseDTO<Boolean> checkAreaCodeUnique(String areaCode, Long excludeId);

    /**
     * 获取所有区域（用于下拉选择）
     *
     * @return 区域列表
     */
    ResponseDTO<List<AreaVO>> getAllAreas();

    /**
     * 获取所有启用区域
     *
     * @return 区域列表
     */
    ResponseDTO<List<AreaVO>> getEnabledAreas();

    /**
     * 根据名称搜索区域
     *
     * @param keyword 关键词
     * @return 区域列表
     */
    ResponseDTO<List<AreaVO>> searchByName(String keyword);

    /**
     * 获取用户可访问的区域列表
     *
     * @param userId 用户ID
     * @return 区域列表
     */
    ResponseDTO<List<AreaVO>> getUserAccessibleAreas(Long userId);

    /**
     * 检查用户是否有区域访问权限
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @return 是否有权限
     */
    ResponseDTO<Boolean> hasAreaPermission(Long userId, Long areaId);

    /**
     * 刷新区域缓存
     */
    void refreshCache();

    // ========== 原生实体操作方法（供内部调用） ==========

    /**
     * 保存区域实体
     * 直接操作实体，不使用DTO包装
     *
     * @param areaEntity 区域实体
     * @return 保存结果
     */
    boolean save(AreaEntity areaEntity);

    /**
     * 根据ID更新区域实体
     * 直接操作实体，不使用DTO包装
     *
     * @param areaEntity 区域实体
     * @return 更新结果
     */
    boolean updateById(AreaEntity areaEntity);

    /**
     * 根据ID删除区域实体
     * 直接操作实体，不使用DTO包装
     *
     * @param areaId 区域ID
     * @return 删除结果
     */
    boolean removeById(Long areaId);
}