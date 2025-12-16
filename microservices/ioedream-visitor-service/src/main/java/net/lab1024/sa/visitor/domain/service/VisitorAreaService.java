package net.lab1024.sa.visitor.domain.service;

import net.lab1024.sa.visitor.domain.entity.VisitorAreaEntity;

import java.util.List;
import java.util.Map;

/**
 * 访客区域管理服务接口
 * <p>
 * 核心职责：管理访客专用区域配置，支持精细化访客权限控制
 * </p>
 * <p>
 * 主要功能：
 * - 访客区域配置管理
 * - 访客权限级别控制
 * - 访客容量管理
 * - 访客设备配置
 * - 访客统计分析
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-08
 */
public interface VisitorAreaService {

    /**
     * 创建访客区域配置
     *
     * @param visitorArea 访客区域实体
     * @return 操作结果
     */
    boolean createVisitorArea(VisitorAreaEntity visitorArea);

    /**
     * 更新访客区域配置
     *
     * @param visitorArea 访客区域实体
     * @return 操作结果
     */
    boolean updateVisitorArea(VisitorAreaEntity visitorArea);

    /**
     * 删除访客区域配置
     *
     * @param visitorAreaId 访客区域ID
     * @return 操作结果
     */
    boolean deleteVisitorArea(Long visitorAreaId);

    /**
     * 根据区域ID获取访客区域配置
     *
     * @param areaId 区域ID
     * @return 访客区域配置
     */
    VisitorAreaEntity getVisitorAreaByAreaId(Long areaId);

    /**
     * 根据访问类型获取访客区域列表
     *
     * @param visitType 访问类型
     * @return 访客区域列表
     */
    List<VisitorAreaEntity> getVisitorAreasByVisitType(Integer visitType);

    /**
     * 根据访问权限级别获取访客区域列表
     *
     * @param accessLevel 访问权限级别
     * @return 访客区域列表
     */
    List<VisitorAreaEntity> getVisitorAreasByAccessLevel(Integer accessLevel);

    /**
     * 获取需要接待人员的访客区域列表
     *
     * @return 需要接待的区域列表
     */
    List<VisitorAreaEntity> getReceptionRequiredAreas();

    /**
     * 根据接待人员获取访客区域列表
     *
     * @param receptionistId 接待人员ID
     * @return 访客区域列表
     */
    List<VisitorAreaEntity> getVisitorAreasByReceptionistId(Long receptionistId);

    /**
     * 获取当前访客数量超限的区域列表
     *
     * @return 超限区域列表
     */
    List<VisitorAreaEntity> getOverCapacityAreas();

    /**
     * 获取当前时段开放的访客区域列表
     *
     * @return 开放区域列表
     */
    List<VisitorAreaEntity> getOpenVisitorAreas();

    /**
     * 检查区域是否支持指定访问类型
     *
     * @param areaId 区域ID
     * @param visitType 访问类型
     * @return 是否支持
     */
    boolean isSupportVisitType(Long areaId, Integer visitType);

    /**
     * 检查用户是否有访客区域管理权限
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @return 是否有权限
     */
    boolean hasManagePermission(Long userId, Long areaId);

    /**
     * 更新区域当前访客数量
     *
     * @param areaId 区域ID
     * @param visitorCount 当前访客数量
     * @return 操作结果
     */
    boolean updateCurrentVisitors(Long areaId, Integer visitorCount);

    /**
     * 增加区域访客数量
     *
     * @param areaId 区域ID
     * @param increment 增加数量
     * @return 操作结果（是否成功增加）
     */
    boolean incrementVisitors(Long areaId, Integer increment);

    /**
     * 减少区域访客数量
     *
     * @param areaId 区域ID
     * @param decrement 减少数量
     * @return 操作结果
     */
    boolean decrementVisitors(Long areaId, Integer decrement);

    /**
     * 获取访客区域统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getVisitorAreaStatistics();

    /**
     * 按访问类型统计访客区域分布
     *
     * @return 分布统计
     */
    List<Map<String, Object>> getAreaStatisticsByVisitType();

    /**
     * 获取用户可管理的访客区域列表
     *
     * @param userId 用户ID
     * @return 可管理的区域列表
     */
    List<VisitorAreaEntity> getUserManageableVisitorAreas(Long userId);

    /**
     * 验证访客区域配置
     *
     * @param visitorArea 访客区域配置
     * @return 验证结果
     */
    Map<String, String> validateVisitorAreaConfig(VisitorAreaEntity visitorArea);

    /**
     * 获取访客区域访问建议
     *
     * @param areaId 区域ID
     * @param visitType 访问类型
     * @return 访问建议
     */
    List<String> getVisitorAreaAccessSuggestions(Long areaId, Integer visitType);

    /**
     * 批量更新访客区域状态
     *
     * @param areaIds 区域ID列表
     * @param enabled 启用状态
     * @return 操作结果
     */
    boolean batchUpdateVisitorAreaStatus(List<Long> areaIds, Boolean enabled);

    /**
     * 检查区域访客容量状态
     *
     * @param areaId 区域ID
     * @param additionalVisitors 额外访客数量
     * @return 容量状态信息
     */
    Map<String, Object> checkAreaCapacityStatus(Long areaId, Integer additionalVisitors);

    /**
     * 获取区域访客设备配置
     *
     * @param areaId 区域ID
     * @return 设备配置
     */
    Map<String, String> getAreaVisitorDevices(Long areaId);

    /**
     * 更新区域访客设备配置
     *
     * @param areaId 区域ID
     * @param devices 设备配置
     * @return 操作结果
     */
    boolean updateAreaVisitorDevices(Long areaId, Map<String, String> devices);

    /**
     * 获取区域健康检查标准
     *
     * @param areaId 区域ID
     * @return 健康检查标准
     */
    Map<String, Boolean> getAreaHealthCheckStandard(Long areaId);

    /**
     * 获取区域开放时间配置
     *
     * @param areaId 区域ID
     * @return 开放时间配置
     */
    Map<String, String> getAreaOpenHours(Long areaId);

    /**
     * 检查区域当前是否开放
     *
     * @param areaId 区域ID
     * @return 是否开放
     */
    boolean isAreaCurrentlyOpen(Long areaId);

    /**
     * 获取区域访客须知
     *
     * @param areaId 区域ID
     * @return 访客须知
     */
    String getAreaVisitorInstructions(Long areaId);

    /**
     * 获取区域安全注意事项
     *
     * @param areaId 区域ID
     * @return 安全注意事项
     */
    String getAreaSafetyNotes(Long areaId);

    /**
     * 获取区域紧急联系人信息
     *
     * @param areaId 区域ID
     * @return 紧急联系人信息
     */
    Map<String, String> getAreaEmergencyContact(Long areaId);
}

