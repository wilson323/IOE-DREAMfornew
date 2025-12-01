package net.lab1024.sa.admin.module.access.service;

import net.lab1024.sa.base.module.area.service.AreaService;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.admin.module.access.domain.vo.AccessAreaStrategyVO;
import net.lab1024.sa.admin.module.access.domain.vo.AccessAreaCapacityVO;
import net.lab1024.sa.admin.module.access.domain.vo.AccessAreaPermissionVO;
import net.lab1024.sa.admin.module.access.domain.vo.AccessAreaTreeVO;
import net.lab1024.sa.admin.module.access.domain.form.AccessAreaForm;
import net.lab1024.sa.admin.module.access.domain.entity.AccessAreaEntity;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

/**
 * 门禁区域管理服务接口
 * <p>
 * 严格遵循OpenSpec规范：
 * - 继承现有AreaService，体现"基于现有的增强和完善"原则
 * - 扩展门禁区域特有业务逻辑
 * - 区域门禁策略管理
 * - 基于区域的权限自动分配机制
 * - 区域容量监控和告警
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
public interface AccessAreaService extends AreaService {

    /**
     * 获取区域树形结构（支持参数）
     *
     * @param parentId 父级区域ID
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
     * 分页查询区域
     *
     * @param pageParam 分页参数
     * @param areaName 区域名称
     * @param areaType 区域类型
     * @param status 区域状态
     * @return 分页结果
     */
    PageResult<AccessAreaEntity> getAreaPage(PageParam pageParam, String areaName, Integer areaType, Integer status);

    /**
     * 添加区域
     *
     * @param areaForm 区域表单
     * @return 操作结果
     */
    Long addArea(@Valid AccessAreaForm areaForm);

    /**
     * 更新区域
     *
     * @param areaForm 区域表单
     * @return 操作结果
     */
    Long updateArea(@Valid AccessAreaForm areaForm);

    /**
     * 删除区域
     *
     * @param areaId 区域ID
     * @return 操作结果
     */
    Boolean deleteArea(Long areaId);

    /**
     * 批量删除区域
     *
     * @param areaIds 区域ID列表
     * @return 操作结果
     */
    Boolean batchDeleteAreas(List<Long> areaIds);

    /**
     * 获取区域设备列表
     *
     * @param areaId 区域ID
     * @param includeOffline 是否包含离线设备
     * @return 设备列表
     */
    List<Object> getAreaDevices(Long areaId, Boolean includeOffline);

    /**
     * 移动区域到新的父级
     *
     * @param areaId 区域ID
     * @param newParentId 新父级区域ID
     * @return 操作结果
     */
    Boolean moveArea(Long areaId, Long newParentId);

    /**
     * 更新区域状态
     *
     * @param areaId 区域ID
     * @param status 状态
     * @return 操作结果
     */
    Boolean updateAreaStatus(Long areaId, Integer status);

    /**
     * 验证区域编码唯一性
     *
     * @param areaCode 区域编码
     * @param excludeId 排除的ID
     * @return 是否唯一
     */
    Boolean validateAreaCode(String areaCode, Long excludeId);

    /**
     * 获取区域统计信息
     *
     * @param areaId 区域ID
     * @return 统计信息
     */
    Map<String, Object> getAreaStatistics(Long areaId);

    /**
     * 获取区域门禁策略配置
     *
     * @param areaId 区域ID
     * @return 门禁策略配置
     */
    AccessAreaStrategyVO getAreaAccessStrategy(Long areaId);

    /**
     * 设置区域门禁策略
     *
     * @param areaId 区域ID
     * @param strategy 策略配置
     * @return 操作结果
     */
    Boolean setAreaAccessStrategy(Long areaId, AccessAreaStrategyVO strategy);

    /**
     * 获取区域容量监控信息
     *
     * @param areaId 区域ID
     * @return 容量监控信息
     */
    AccessAreaCapacityVO getAreaCapacityMonitor(Long areaId);

    /**
     * 获取区域容量告警列表
     *
     * @return 告警列表
     */
    List<AccessAreaCapacityVO> getAreaCapacityAlerts();

    /**
     * 基于区域自动分配用户权限
     *
     * @param areaId 区域ID
     * @param userIds 用户ID列表
     * @return 分配结果
     */
    Map<Long, Boolean> autoAssignAreaPermissions(Long areaId, List<Long> userIds);

    /**
     * 批量设置区域门禁策略
     *
     * @param areaIds 区域ID列表
     * @param strategy 策略配置
     * @return 操作结果
     */
    Map<Long, Boolean> batchSetAreaAccessStrategy(List<Long> areaIds, AccessAreaStrategyVO strategy);

    /**
     * 获取用户区域访问权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<AccessAreaPermissionVO> getUserAreaPermissions(Long userId);

    /**
     * 检查区域门禁策略冲突
     *
     * @param areaId 区域ID
     * @param strategy 策略配置
     * @return 冲突检查结果
     */
    Map<String, Object> checkAreaStrategyConflict(Long areaId, AccessAreaStrategyVO strategy);

    /**
     * 获取区域门禁统计信息
     *
     * @param areaId 区域ID
     * @return 统计信息
     */
    Map<String, Object> getAreaAccessStatistics(Long areaId);

    /**
     * 同步区域权限到设备
     *
     * @param areaId 区域ID
     * @return 同步结果
     */
    Boolean syncAreaPermissionsToDevice(Long areaId);

    /**
     * 获取区域紧急状态配置
     *
     * @param areaId 区域ID
     * @return 紧急状态配置
     */
    Map<String, Object> getAreaEmergencyConfig(Long areaId);

    /**
     * 设置区域紧急状态
     *
     * @param areaId 区域ID
     * @param emergencyType 紧急类型
     * @param emergencyLevel 紧急级别
     * @return 操作结果
     */
    Boolean setAreaEmergencyStatus(Long areaId, String emergencyType, Integer emergencyLevel);

    /**
     * 清除区域紧急状态
     *
     * @param areaId 区域ID
     * @return 操作结果
     */
    Boolean clearAreaEmergencyStatus(Long areaId);

    /**
     * 获取区域访客管理配置
     *
     * @param areaId 区域ID
     * @return 访客管理配置
     */
    Map<String, Object> getAreaVisitorConfig(Long areaId);

    /**
     * 更新区域访客管理配置
     *
     * @param areaId 区域ID
     * @param config 访客管理配置
     * @return 操作结果
     */
    Boolean updateAreaVisitorConfig(Long areaId, Map<String, Object> config);

    /**
     * 获取区域时段访问控制
     *
     * @param areaId 区域ID
     * @return 时段控制配置
     */
    Map<String, Object> getAreaTimeSlotControl(Long areaId);

    /**
     * 设置区域时段访问控制
     *
     * @param areaId 区域ID
     * @param timeSlotConfig 时段控制配置
     * @return 操作结果
     */
    Boolean setAreaTimeSlotControl(Long areaId, Map<String, Object> timeSlotConfig);
}