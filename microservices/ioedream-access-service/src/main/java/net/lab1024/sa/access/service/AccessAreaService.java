package net.lab1024.sa.access.service;

import net.lab1024.sa.access.domain.form.AccessAreaQueryForm;
import net.lab1024.sa.access.domain.vo.AccessAreaOverviewVO;
import net.lab1024.sa.access.domain.vo.AccessAreaPersonVO;
import net.lab1024.sa.access.domain.vo.AccessAreaPermissionMatrixVO;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;

import java.util.List;

/**
 * 门禁区域空间管理服务接口
 * <p>
 * 严格遵循CLAUDE.md规范和ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md架构方案：
 * - 遵循四层架构：Controller → Service → Manager → DAO
 * - 区域信息管理、门管理、人员管理、权限管理、区域监控
 * </p>
 * <p>
 * 核心职责：
 * - 区域信息查询和概览统计
 * - 区域内人员权限管理
 * - 区域权限自动分配
 * - 区域通行监控
 * </p>
 * <p>
 * 注意：区域基本信息由公共模块统一维护，本服务主要负责门禁相关的区域管理功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AccessAreaService {

    /**
     * 查询区域列表
     * <p>
     * 查询门禁相关的区域列表，支持分页和多条件查询
     * </p>
     *
     * @param queryForm 查询表单
     * @return 区域列表
     */
    ResponseDTO<PageResult<AccessAreaOverviewVO>> queryAreaList(AccessAreaQueryForm queryForm);

    /**
     * 查询区域概览
     * <p>
     * 查询指定区域的详细概览信息，包括设备数、人员数、通行统计等
     * </p>
     *
     * @param areaId 区域ID
     * @return 区域概览信息
     */
    ResponseDTO<AccessAreaOverviewVO> getAreaOverview(Long areaId);

    /**
     * 查询区域内人员列表
     * <p>
     * 查询指定区域内有权限的人员列表，包括权限信息
     * </p>
     *
     * @param areaId 区域ID
     * @param queryForm 查询表单（用于分页）
     * @return 人员列表
     */
    ResponseDTO<PageResult<AccessAreaPersonVO>> queryAreaPersons(Long areaId, AccessAreaQueryForm queryForm);

    /**
     * 分配人员到区域
     * <p>
     * 将人员分配到指定区域，并自动分配区域内所有设备的通行权限
     * </p>
     *
     * @param areaId 区域ID
     * @param userId 用户ID
     * @return 操作结果
     */
    ResponseDTO<Void> assignPersonToArea(Long areaId, Long userId);

    /**
     * 移除区域人员
     * <p>
     * 从区域中移除人员，并回收该人员在该区域的所有设备权限
     * </p>
     *
     * @param areaId 区域ID
     * @param userId 用户ID
     * @return 操作结果
     */
    ResponseDTO<Void> removePersonFromArea(Long areaId, Long userId);

    /**
     * 查询权限矩阵
     * <p>
     * 查询指定区域的人员-设备权限关系矩阵
     * </p>
     *
     * @param areaId 区域ID
     * @return 权限矩阵
     */
    ResponseDTO<AccessAreaPermissionMatrixVO> queryPermissionMatrix(Long areaId);

    /**
     * 批量分配权限
     * <p>
     * 批量分配人员对设备的通行权限
     * </p>
     *
     * @param areaId 区域ID
     * @param userIds 用户ID列表
     * @param deviceIds 设备ID列表（为空表示区域内所有设备）
     * @return 操作结果
     */
    ResponseDTO<Void> batchAssignPermissions(Long areaId, List<Long> userIds, List<String> deviceIds);

    /**
     * 批量回收权限
     * <p>
     * 批量回收人员对设备的通行权限
     * </p>
     *
     * @param areaId 区域ID
     * @param userIds 用户ID列表
     * @param deviceIds 设备ID列表（为空表示区域内所有设备）
     * @return 操作结果
     */
    ResponseDTO<Void> batchRevokePermissions(Long areaId, List<Long> userIds, List<String> deviceIds);

    /**
     * 查询区域监控数据
     * <p>
     * 查询指定区域的监控数据，包括设备状态、人员通行、容量等
     * </p>
     *
     * @param areaId 区域ID
     * @return 区域监控数据
     */
    ResponseDTO<net.lab1024.sa.access.domain.vo.AccessAreaMonitorVO> getAreaMonitorData(Long areaId);
}
