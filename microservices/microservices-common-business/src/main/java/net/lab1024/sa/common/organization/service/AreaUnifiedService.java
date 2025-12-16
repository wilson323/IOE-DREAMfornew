package net.lab1024.sa.common.organization.service;

import net.lab1024.sa.common.organization.entity.AreaEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 统一区域空间管理服务接口
 * <p>
 * 贯彻区域空间概念，为所有业务模块提供统一的区域管理能力
 * </p>
 * <p>
 * 核心职责：
 * - 统一区域空间管理
 * - 区域层次结构维护
 * - 区域权限控制
 * - 区域业务属性管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-08
 */
public interface AreaUnifiedService {

    /**
     * 获取完整区域树
     *
     * @return 区域树结构
     */
    List<AreaEntity> getAreaTree();

    /**
     * 根据用户ID获取可访问区域列表
     *
     * @param userId 用户ID
     * @return 可访问区域列表
     */
    List<AreaEntity> getUserAccessibleAreas(Long userId);

    /**
     * 检查用户是否有区域访问权限
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @return 是否有权限
     */
    boolean hasAreaAccess(Long userId, Long areaId);

    /**
     * 根据区域编码获取区域信息
     *
     * @param areaCode 区域编码
     * @return 区域实体
     */
    AreaEntity getAreaByCode(String areaCode);

    /**
     * 获取区域的完整路径
     *
     * @param areaId 区域ID
     * @return 区域路径列表（从根节点到当前节点）
     */
    List<AreaEntity> getAreaPath(Long areaId);

    /**
     * 获取指定区域的所有子区域
     *
     * @param parentAreaId 父区域ID
     * @return 子区域列表
     */
    List<AreaEntity> getChildAreas(Long parentAreaId);

    /**
     * 获取区域业务属性
     *
     * @param areaId 区域ID
     * @param businessModule 业务模块标识（如：access、consume、attendance等）
     * @return 业务属性Map
     */
    Map<String, Object> getAreaBusinessAttributes(Long areaId, String businessModule);

    /**
     * 设置区域业务属性
     *
     * @param areaId 区域ID
     * @param businessModule 业务模块标识
     * @param attributes 业务属性
     * @return 设置结果
     */
    boolean setAreaBusinessAttributes(Long areaId, String businessModule, Map<String, Object> attributes);

    /**
     * 获取区域的设备信息
     *
     * @param areaId 区域ID
     * @param deviceType 设备类型（可选）
     * @return 设备信息列表
     */
    List<Map<String, Object>> getAreaDevices(Long areaId, String deviceType);

    /**
     * 获取区域统计信息
     *
     * @param areaId 区域ID
     * @return 统计信息（包含各业务模块的数据）
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
     * 检查区域是否支持指定业务
     *
     * @param areaId 区域ID
     * @param businessModule 业务模块
     * @return 是否支持
     */
    boolean isAreaSupportBusiness(Long areaId, String businessModule);

    /**
     * 获取区域的所有业务模块支持情况
     *
     * @param areaId 区域ID
     * @return 支持的业务模块列表
     */
    Set<String> getAreaSupportedBusinessModules(Long areaId);
}