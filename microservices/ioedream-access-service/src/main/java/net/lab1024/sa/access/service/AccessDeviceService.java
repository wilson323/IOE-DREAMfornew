package net.lab1024.sa.access.service;

import net.lab1024.sa.access.domain.form.AccessDeviceAddForm;
import net.lab1024.sa.access.domain.form.AccessDeviceQueryForm;
import net.lab1024.sa.access.domain.form.AccessDeviceUpdateForm;
import net.lab1024.sa.access.domain.vo.AccessDeviceVO;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 门禁设备管理服务接口
 * <p>
 * 严格遵循CLAUDE.md规范和ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md架构方案：
 * - 使用公共DeviceEntity（在common-business中）
 * - 只管理门禁设备（deviceType='ACCESS'）
 * - 遵循四层架构：Controller → Service → Manager → DAO
 * </p>
 * <p>
 * 核心职责：
 * - 门禁设备的CRUD操作
 * - 设备状态管理
 * - 设备查询和统计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AccessDeviceService {

    /**
     * 分页查询门禁设备列表
     * <p>
     * 支持多条件查询：关键字、区域ID、设备状态、启用状态
     * </p>
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    ResponseDTO<PageResult<AccessDeviceVO>> queryDeviceList(AccessDeviceQueryForm queryForm);

    /**
     * 查询门禁设备详情
     * <p>
     * 根据设备ID查询设备详细信息，包括区域名称等关联信息
     * </p>
     *
     * @param deviceId 设备ID（String类型，与DeviceEntity保持一致）
     * @return 设备详情
     */
    ResponseDTO<AccessDeviceVO> getDeviceDetail(String deviceId);

    /**
     * 添加门禁设备
     * <p>
     * 添加新的门禁设备，自动设置deviceType='ACCESS'
     * </p>
     *
     * @param addForm 添加表单
     * @return 设备ID
     */
    ResponseDTO<String> addDevice(AccessDeviceAddForm addForm);

    /**
     * 更新门禁设备
     * <p>
     * 更新设备基本信息，不包括设备类型（固定为ACCESS）
     * </p>
     *
     * @param updateForm 更新表单
     * @return 操作结果
     */
    ResponseDTO<Void> updateDevice(AccessDeviceUpdateForm updateForm);

    /**
     * 删除门禁设备
     * <p>
     * 逻辑删除设备（设置deleted_flag=1）
     * </p>
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    ResponseDTO<Void> deleteDevice(String deviceId);

    /**
     * 更新设备状态
     * <p>
     * 更新设备的启用/禁用状态
     * </p>
     *
     * @param deviceId 设备ID
     * @param enabled 启用状态（0-禁用，1-启用）
     * @return 操作结果
     */
    ResponseDTO<Void> updateDeviceStatus(String deviceId, Integer enabled);

    /**
     * 统计门禁设备数量
     * <p>
     * 统计系统中的门禁设备总数
     * </p>
     *
     * @return 设备总数
     */
    ResponseDTO<Long> countDevices();

    /**
     * 统计在线门禁设备数量
     * <p>
     * 统计当前在线的门禁设备数量
     * </p>
     *
     * @return 在线设备数量
     */
    ResponseDTO<Long> countOnlineDevices();
}
