package net.lab1024.sa.access.service;

import java.util.List;
import java.util.Map;

import net.lab1024.sa.access.domain.entity.AccessAreaEntity;
import net.lab1024.sa.access.domain.form.AccessAreaForm;
import net.lab1024.sa.access.domain.query.AccessAreaQuery;
import net.lab1024.sa.access.domain.vo.AccessAreaCapacityVO;
import net.lab1024.sa.access.domain.vo.AccessAreaPermissionVO;
import net.lab1024.sa.access.domain.vo.AccessAreaStrategyVO;
import net.lab1024.sa.access.domain.vo.AccessAreaTreeVO;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;

/**
 * 门禁区域管理服务接口
 * <p>
 * 微服务架构下的门禁区域管理服务，提供完整的区域管理功能：
 * - 区域门禁策略管理
 * - 基于区域的权限自动分配机制
 * - 区域容量监控和告警
 * - 区域树形结构管理
 * - 区域设备关联管理
 * - 区域访问控制配置
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
public interface AccessAreaService {

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
     * @param areaId   区域ID
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
     * @param areaId  区域ID
     * @param userIds 用户ID列表
     * @return 分配结果
     */
    Map<Long, Boolean> autoAssignAreaPermissions(Long areaId, List<Long> userIds);

    /**
     * 批量设置区域门禁策略
     *
     * @param areaIds  区域ID列表
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
     * @param areaId   区域ID
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
     * @param areaId         区域ID
     * @param emergencyType  紧急类型
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
     * @param areaId         区域ID
     * @param timeSlotConfig 时段控制配置
     * @return 操作结果
     */
    Boolean setAreaTimeSlotControl(Long areaId, Map<String, Object> timeSlotConfig);

    // ==================== 基础CRUD方法（兼容Controller调用） ====================

    /**
     * 获取区域树形结构（兼容方法）
     *
     * @param parentId        父级区域ID
     * @param includeChildren 是否包含子区域
     * @return 区域树形结构
     */
    List<AccessAreaTreeVO> getAreaTree(Long parentId, Boolean includeChildren);

    /**
     * 根据ID获取区域信息（兼容方法）
     *
     * @param areaId 区域ID
     * @return 区域信息
     */
    AccessAreaEntity getAreaById(Long areaId);

    /**
     * 分页查询区域（兼容方法）
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<AccessAreaEntity> getAreaPage(AccessAreaQuery query);

    /**
     * 添加区域（兼容方法）
     *
     * @param form 区域表单
     * @return 操作结果
     */
    ResponseDTO<String> addArea(AccessAreaForm form);

    /**
     * 更新区域（兼容方法）
     *
     * @param form 区域表单
     * @return 操作结果
     */
    ResponseDTO<String> updateArea(AccessAreaForm form);

    /**
     * 删除区域（兼容方法）
     *
     * @param areaId 区域ID
     * @return 操作结果
     */
    ResponseDTO<String> deleteArea(Long areaId);

    // ==================== 扩展业务方法 ====================

    /**
     * 批量删除区域
     *
     * @param areaIds 区域ID列表
     * @return 操作结果
     */
    ResponseDTO<String> batchDeleteAreas(List<Long> areaIds);

    /**
     * 获取区域设备列表
     *
     * @param areaId          区域ID
     * @param includeChildren 是否包含子区域
     * @return 设备列表
     */
    List<Object> getAreaDevices(Long areaId, Boolean includeChildren);

    /**
     * 移动区域
     *
     * @param areaId      区域ID
     * @param newParentId 新父级区域ID
     * @return 操作结果
     */
    ResponseDTO<String> moveArea(Long areaId, Long newParentId);

    /**
     * 更新区域状态
     *
     * @param areaId 区域ID
     * @param status 状态
     * @return 操作结果
     */
    ResponseDTO<String> updateAreaStatus(Long areaId, Integer status);

    // ==================== 额外需要的业务方法 ====================

    /**
     * 验证区域编码是否唯一
     *
     * @param areaCode      区域编码
     * @param excludeAreaId 排除的区域ID（编辑时使用）
     * @return 是否唯一
     */
    Boolean validateAreaCode(String areaCode, Long excludeAreaId);

    /**
     * 获取区域统计信息
     *
     * @param areaId 区域ID
     * @return 统计信息
     */
    Map<String, Object> getAreaStatistics(Long areaId);
}
