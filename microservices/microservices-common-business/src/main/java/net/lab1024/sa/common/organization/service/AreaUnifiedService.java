package net.lab1024.sa.common.organization.service;

import net.lab1024.sa.common.organization.entity.AreaEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 统一区域管理服务接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
public interface AreaUnifiedService {

    /**
     * 获取区域树
     *
     * @return 区域树列表
     */
    List<AreaEntity> getAreaTree();

    /**
     * 获取用户可访问的区域列表
     *
     * @param userId 用户ID
     * @return 可访问的区域列表
     */
    List<AreaEntity> getUserAccessibleAreas(Long userId);

    /**
     * 检查用户是否有区域访问权限
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @return 是否有权限
     */
    Boolean hasAreaAccess(Long userId, Long areaId);

    /**
     * 根据区域编码获取区域
     *
     * @param areaCode 区域编码
     * @return 区域实体
     */
    AreaEntity getAreaByCode(String areaCode);

    /**
     * 获取区域路径
     *
     * @param areaId 区域ID
     * @return 区域路径列表
     */
    List<AreaEntity> getAreaPath(Long areaId);

    /**
     * 获取子区域列表
     *
     * @param parentAreaId 父区域ID
     * @return 子区域列表
     */
    List<AreaEntity> getChildAreas(Long parentAreaId);

    /**
     * 获取区域统计信息
     *
     * @param areaId 区域ID
     * @return 统计信息
     */
    Map<String, Object> getAreaStatistics(Long areaId);

    /**
     * 根据业务类型获取区域列表
     *
     * @param businessType 业务类型
     * @return 区域列表
     */
    List<AreaEntity> getAreasByBusinessType(String businessType);

    /**
     * 检查区域是否支持业务模块
     *
     * @param areaId 区域ID
     * @param businessModule 业务模块
     * @return 是否支持
     */
    Boolean isAreaSupportBusiness(Long areaId, String businessModule);

    /**
     * 获取区域支持的业务模块列表
     *
     * @param areaId 区域ID
     * @return 业务模块列表
     */
    Set<String> getAreaSupportedBusinessModules(Long areaId);

    /**
     * 获取区域业务属性
     *
     * @param areaId 区域ID
     * @param businessModule 业务模块
     * @return 业务属性Map
     */
    Map<String, Object> getAreaBusinessAttributes(Long areaId, String businessModule);

    /**
     * 设置区域业务属性
     *
     * @param areaId 区域ID
     * @param businessModule 业务模块
     * @param attributes 业务属性Map
     * @return 是否设置成功
     */
    boolean setAreaBusinessAttributes(Long areaId, String businessModule, Map<String, Object> attributes);

    /**
     * 获取区域设备列表
     *
     * @param areaId 区域ID
     * @param deviceType 设备类型（可选）
     * @return 设备列表
     */
    List<Map<String, Object>> getAreaDevices(Long areaId, String deviceType);
}

