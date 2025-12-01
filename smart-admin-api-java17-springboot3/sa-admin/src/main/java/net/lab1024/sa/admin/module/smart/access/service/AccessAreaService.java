package net.lab1024.sa.admin.module.smart.access.service;

import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.admin.module.smart.access.domain.entity.AccessAreaEntity;
import net.lab1024.sa.admin.module.smart.access.domain.form.AccessAreaForm;
import net.lab1024.sa.admin.module.smart.access.domain.vo.AccessAreaTreeVO;

import java.util.List;

/**
 * 门禁区域管理服务接口
 * <p>
 * 严格遵循repowiki规范：
 * - 接口定义清晰，职责单一
 * - 方法命名遵循RESTful规范
 * - 完整的业务逻辑封装
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
public interface AccessAreaService {

    /**
     * 获取区域树形结构
     *
     * @param parentId 父级区域ID，0表示根区域
     * @param includeChildren 是否包含子区域
     * @return 区域树结构
     */
    List<AccessAreaTreeVO> getAreaTree(Long parentId, Boolean includeChildren);

    /**
     * 根据ID获取区域详情
     *
     * @param areaId 区域ID
     * @return 区域详情
     */
    AccessAreaEntity getAreaById(Long areaId);

    /**
     * 分页查询区域列表
     *
     * @param pageParam 分页参数
     * @param areaName 区域名称
     * @param areaType 区域类型
     * @param status 区域状态
     * @return 分页结果
     */
    PageResult<AccessAreaEntity> getAreaPage(PageParam pageParam, String areaName, Integer areaType, Integer status);

    /**
     * 创建区域
     *
     * @param areaForm 区域表单
     * @throws RuntimeException 业务异常
     */
    void addArea(AccessAreaForm areaForm);

    /**
     * 更新区域
     *
     * @param areaForm 区域表单
     * @throws RuntimeException 业务异常
     */
    void updateArea(AccessAreaForm areaForm);

    /**
     * 删除区域
     *
     * @param areaId 区域ID
     * @throws RuntimeException 业务异常
     */
    void deleteArea(Long areaId);

    /**
     * 批量删除区域
     *
     * @param areaIds 区域ID列表
     * @throws RuntimeException 业务异常
     */
    void batchDeleteAreas(List<Long> areaIds);

    /**
     * 获取区域下的设备列表
     *
     * @param areaId 区域ID
     * @param includeChildren 是否包含子区域设备
     * @return 设备列表
     */
    List<Object> getAreaDevices(Long areaId, Boolean includeChildren);

    /**
     * 移动区域到指定父级
     *
     * @param areaId 区域ID
     * @param newParentId 新父级区域ID
     * @throws RuntimeException 业务异常
     */
    void moveArea(Long areaId, Long newParentId);

    /**
     * 更新区域状态
     *
     * @param areaId 区域ID
     * @param status 新状态
     * @throws RuntimeException 业务异常
     */
    void updateAreaStatus(Long areaId, Integer status);

    /**
     * 验证区域编码唯一性
     *
     * @param areaCode 区域编码
     * @param excludeAreaId 排除的区域ID
     * @return 是否唯一
     */
    boolean validateAreaCode(String areaCode, Long excludeAreaId);

    /**
     * 获取区域统计信息
     *
     * @param areaId 区域ID
     * @return 统计信息
     */
    Object getAreaStatistics(Long areaId);

    /**
     * 重新计算区域路径
     *
     * @param areaId 区域ID
     */
    void recalculateAreaPath(Long areaId);

    /**
     * 检查区域是否有子区域
     *
     * @param areaId 区域ID
     * @return 是否有子区域
     */
    boolean hasChildAreas(Long areaId);

    /**
     * 检查区域是否关联设备
     *
     * @param areaId 区域ID
     * @return 是否关联设备
     */
    boolean hasAssociatedDevices(Long areaId);
}