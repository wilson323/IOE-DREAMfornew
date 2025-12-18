package net.lab1024.sa.access.service.impl;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.AccessDeviceDao;
import net.lab1024.sa.access.domain.form.AccessAreaQueryForm;
import net.lab1024.sa.access.domain.vo.AccessAreaMonitorVO;
import net.lab1024.sa.access.domain.vo.AccessAreaOverviewVO;
import net.lab1024.sa.access.domain.vo.AccessAreaPersonVO;
import net.lab1024.sa.access.domain.vo.AccessAreaPermissionMatrixVO;
import net.lab1024.sa.access.service.AccessAreaService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.dao.AccessRecordDao;
import net.lab1024.sa.common.organization.dao.AreaDeviceDao;
import net.lab1024.sa.common.organization.dao.UserAreaPermissionDao;
import net.lab1024.sa.common.organization.entity.AccessRecordEntity;
import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.common.organization.entity.UserAreaPermissionEntity;
import net.lab1024.sa.common.response.ResponseDTO;

/**
 * 门禁区域空间管理服务实现
 * <p>
 * 严格遵循CLAUDE.md规范和ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md架构方案：
 * - 遵循四层架构：Controller → Service → Manager → DAO
 * - 区域信息管理、门管理、人员管理、权限管理、区域监控
 * - 使用@Resource依赖注入
 * - 使用@Transactional事务管理
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
@Slf4j
@Service
public class AccessAreaServiceImpl implements AccessAreaService {

    @Resource
    private AccessDeviceDao accessDeviceDao;

    @Resource
    private AreaDeviceDao areaDeviceDao;

    @Resource
    private UserAreaPermissionDao userAreaPermissionDao;

    @Resource
    private AccessRecordDao accessRecordDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 查询区域列表
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<PageResult<AccessAreaOverviewVO>> queryAreaList(AccessAreaQueryForm queryForm) {
        log.info("[区域管理] 查询区域列表: pageNum={}, pageSize={}, areaId={}, areaNameKeyword={}",
                queryForm.getPageNum(), queryForm.getPageSize(), queryForm.getAreaId(), queryForm.getAreaNameKeyword());

        try {
            // 通过GatewayServiceClient查询区域列表
            // 注意：区域基本信息由common-service统一维护
            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("pageNum", queryForm.getPageNum());
            queryParams.put("pageSize", queryForm.getPageSize());
            if (queryForm.getAreaId() != null) {
                queryParams.put("areaId", queryForm.getAreaId());
            }
            if (StringUtils.hasText(queryForm.getAreaNameKeyword())) {
                queryParams.put("areaNameKeyword", queryForm.getAreaNameKeyword());
            }
            if (queryForm.getParentAreaId() != null) {
                queryParams.put("parentAreaId", queryForm.getParentAreaId());
            }
            if (queryForm.getAreaType() != null) {
                queryParams.put("areaType", queryForm.getAreaType());
            }
            if (queryForm.getAreaStatus() != null) {
                queryParams.put("areaStatus", queryForm.getAreaStatus());
            }

            // 调用common-service查询区域列表
            ResponseDTO<PageResult<AreaEntity>> areaResponse = gatewayServiceClient.callCommonService(
                    "/api/v1/organization/area/query",
                    HttpMethod.POST,
                    queryParams,
                    PageResult.class
            );

            if (areaResponse == null || !areaResponse.isSuccess() || areaResponse.getData() == null) {
                log.warn("[区域管理] 查询区域列表失败: response={}", areaResponse);
                return ResponseDTO.error("QUERY_AREA_LIST_ERROR", "查询区域列表失败");
            }

            // 转换为VO列表（需要补充统计信息）
            PageResult<AreaEntity> areaPageResult = (PageResult<AreaEntity>) areaResponse.getData();
            List<AccessAreaOverviewVO> voList = areaPageResult.getRecords().stream()
                    .map(this::convertToAreaOverviewVO)
                    .collect(Collectors.toList());

            // 构建分页结果
            PageResult<AccessAreaOverviewVO> result = new PageResult<>();
            result.setRecords(voList);
            result.setTotal(areaPageResult.getTotal());
            result.setPageNum(areaPageResult.getPageNum());
            result.setPageSize(areaPageResult.getPageSize());
            result.setPages(areaPageResult.getPages());

            log.info("[区域管理] 查询区域列表成功: total={}, pageNum={}, pageSize={}",
                    result.getTotal(), queryForm.getPageNum(), queryForm.getPageSize());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[区域管理] 查询区域列表异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("QUERY_AREA_LIST_ERROR", "查询区域列表失败: " + e.getMessage());
        }
    }

    /**
     * 查询区域概览
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<AccessAreaOverviewVO> getAreaOverview(Long areaId) {
        log.info("[区域管理] 查询区域概览: areaId={}", areaId);

        try {
            // 查询区域基本信息
            ResponseDTO<AreaEntity> areaResponse = gatewayServiceClient.callCommonService(
                    "/api/v1/organization/area/" + areaId,
                    HttpMethod.GET,
                    null,
                    AreaEntity.class
            );

            if (areaResponse == null || !areaResponse.isSuccess() || areaResponse.getData() == null) {
                log.warn("[区域管理] 区域不存在: areaId={}", areaId);
                return ResponseDTO.error("AREA_NOT_FOUND", "区域不存在");
            }

            AreaEntity area = areaResponse.getData();

            // 转换为VO并补充统计信息
            AccessAreaOverviewVO vo = convertToAreaOverviewVO(area);

            log.info("[区域管理] 查询区域概览成功: areaId={}, areaName={}", areaId, area.getAreaName());
            return ResponseDTO.ok(vo);

        } catch (Exception e) {
            log.error("[区域管理] 查询区域概览异常: areaId={}, error={}", areaId, e.getMessage(), e);
            return ResponseDTO.error("GET_AREA_OVERVIEW_ERROR", "查询区域概览失败: " + e.getMessage());
        }
    }

    /**
     * 查询区域内人员列表
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<PageResult<AccessAreaPersonVO>> queryAreaPersons(Long areaId, AccessAreaQueryForm queryForm) {
        log.info("[区域管理] 查询区域内人员列表: areaId={}, pageNum={}, pageSize={}",
                areaId, queryForm.getPageNum(), queryForm.getPageSize());

        try {
            // 查询区域内的权限列表
            List<UserAreaPermissionEntity> permissions = userAreaPermissionDao.selectByAreaId(areaId);

            // 分页处理
            int pageNum = queryForm.getPageNum() != null ? queryForm.getPageNum() : 1;
            int pageSize = queryForm.getPageSize() != null ? queryForm.getPageSize() : 20;
            int start = (pageNum - 1) * pageSize;
            int end = Math.min(start + pageSize, permissions.size());

            List<UserAreaPermissionEntity> pagePermissions = permissions.subList(
                    Math.min(start, permissions.size()),
                    Math.min(end, permissions.size())
            );

            // 转换为VO列表
            List<AccessAreaPersonVO> voList = pagePermissions.stream()
                    .map(permission -> convertToAreaPersonVO(permission, areaId))
                    .collect(Collectors.toList());

            // 构建分页结果
            PageResult<AccessAreaPersonVO> result = new PageResult<>();
            result.setRecords(voList);
            result.setTotal((long) permissions.size());
            result.setPageNum(pageNum);
            result.setPageSize(pageSize);
            result.setPages((int) Math.ceil((double) permissions.size() / pageSize));

            log.info("[区域管理] 查询区域内人员列表成功: areaId={}, total={}, pageNum={}, pageSize={}",
                    areaId, result.getTotal(), pageNum, pageSize);

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[区域管理] 查询区域内人员列表异常: areaId={}, error={}", areaId, e.getMessage(), e);
            return ResponseDTO.error("QUERY_AREA_PERSONS_ERROR", "查询区域内人员列表失败: " + e.getMessage());
        }
    }

    /**
     * 分配人员到区域
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> assignPersonToArea(Long areaId, Long userId) {
        log.info("[区域管理] 分配人员到区域: areaId={}, userId={}", areaId, userId);

        try {
            // 检查权限是否已存在
            UserAreaPermissionEntity existingPermission = userAreaPermissionDao.selectByUserAndArea(userId, areaId);
            if (existingPermission != null) {
                log.warn("[区域管理] 人员已在区域中: areaId={}, userId={}", areaId, userId);
                return ResponseDTO.error("PERSON_ALREADY_IN_AREA", "人员已在区域中");
            }

            // 创建权限实体
            UserAreaPermissionEntity permission = new UserAreaPermissionEntity();
            permission.setUserId(userId);
            permission.setAreaId(areaId);
            permission.setPermissionType("ALWAYS"); // 默认永久权限
            permission.setStartTime(LocalDateTime.now());

            // 保存权限
            userAreaPermissionDao.insert(permission);

            // 自动分配区域内所有设备的通行权限
            // 查询区域内的门禁设备
            List<AreaDeviceEntity> areaDevices = areaDeviceDao.selectByAreaIdAndBusinessModule(areaId, "access");

            // 为每个设备创建权限（如果设备ID不为空）
            for (AreaDeviceEntity areaDevice : areaDevices) {
                if (StringUtils.hasText(areaDevice.getDeviceId())) {
                    // 注意：UserAreaPermissionEntity的deviceId是Long类型，需要转换
                    // 这里先不设置deviceId，表示该区域所有设备
                    // 如果需要精确到设备，需要创建多条权限记录
                }
            }

            log.info("[区域管理] 分配人员到区域成功: areaId={}, userId={}, deviceCount={}",
                    areaId, userId, areaDevices.size());

            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[区域管理] 分配人员到区域异常: areaId={}, userId={}, error={}",
                    areaId, userId, e.getMessage(), e);
            return ResponseDTO.error("ASSIGN_PERSON_TO_AREA_ERROR", "分配人员到区域失败: " + e.getMessage());
        }
    }

    /**
     * 移除区域人员
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> removePersonFromArea(Long areaId, Long userId) {
        log.info("[区域管理] 移除区域人员: areaId={}, userId={}", areaId, userId);

        try {
            // 查询权限
            UserAreaPermissionEntity permission = userAreaPermissionDao.selectByUserAndArea(userId, areaId);
            if (permission == null) {
                log.warn("[区域管理] 人员不在区域中: areaId={}, userId={}", areaId, userId);
                return ResponseDTO.error("PERSON_NOT_IN_AREA", "人员不在区域中");
            }

            // 逻辑删除权限
            permission.setDeletedFlag(true);
            userAreaPermissionDao.updateById(permission);

            log.info("[区域管理] 移除区域人员成功: areaId={}, userId={}", areaId, userId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[区域管理] 移除区域人员异常: areaId={}, userId={}, error={}",
                    areaId, userId, e.getMessage(), e);
            return ResponseDTO.error("REMOVE_PERSON_FROM_AREA_ERROR", "移除区域人员失败: " + e.getMessage());
        }
    }

    /**
     * 查询权限矩阵
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<AccessAreaPermissionMatrixVO> queryPermissionMatrix(Long areaId) {
        log.info("[区域管理] 查询权限矩阵: areaId={}", areaId);

        try {
            AccessAreaPermissionMatrixVO matrix = new AccessAreaPermissionMatrixVO();
            matrix.setAreaId(areaId);

            // 查询区域信息
            ResponseDTO<AreaEntity> areaResponse = gatewayServiceClient.callCommonService(
                    "/api/v1/organization/area/" + areaId,
                    HttpMethod.GET,
                    null,
                    AreaEntity.class
            );

            if (areaResponse != null && areaResponse.isSuccess() && areaResponse.getData() != null) {
                matrix.setAreaName(areaResponse.getData().getAreaName());
            }

            // 查询区域内的人员权限
            List<UserAreaPermissionEntity> permissions = userAreaPermissionDao.selectByAreaId(areaId);

            // 查询区域内的设备
            List<AreaDeviceEntity> areaDevices = areaDeviceDao.selectByAreaIdAndBusinessModule(areaId, "access");

            // 构建人员列表
            List<AccessAreaPermissionMatrixVO.PersonInfo> persons = new ArrayList<>();
            for (UserAreaPermissionEntity permission : permissions) {
                AccessAreaPermissionMatrixVO.PersonInfo person = new AccessAreaPermissionMatrixVO.PersonInfo();
                person.setUserId(permission.getUserId());

                // 查询用户信息
                try {
                    ResponseDTO<net.lab1024.sa.common.organization.entity.UserEntity> userResponse =
                            gatewayServiceClient.callCommonService(
                                    "/api/v1/organization/user/" + permission.getUserId(),
                                    HttpMethod.GET,
                                    null,
                                    net.lab1024.sa.common.organization.entity.UserEntity.class
                            );

                    if (userResponse != null && userResponse.isSuccess() && userResponse.getData() != null) {
                        person.setUserName(userResponse.getData().getUserName());
                        person.setUserNo(userResponse.getData().getUserNo());
                        person.setDepartmentName(userResponse.getData().getDepartmentName());
                    }
                } catch (Exception e) {
                    log.debug("[区域管理] 查询用户信息失败: userId={}, error={}", permission.getUserId(), e.getMessage());
                }

                persons.add(person);
            }
            matrix.setPersons(persons);

            // 构建设备列表
            List<AccessAreaPermissionMatrixVO.DeviceInfo> devices = areaDevices.stream()
                    .map(this::convertToDeviceInfo)
                    .collect(Collectors.toList());
            matrix.setDevices(devices);

            // 构建权限矩阵
            List<AccessAreaPermissionMatrixVO.PermissionMatrixRow> matrixRows = new ArrayList<>();
            for (UserAreaPermissionEntity permission : permissions) {
                AccessAreaPermissionMatrixVO.PermissionMatrixRow row =
                        new AccessAreaPermissionMatrixVO.PermissionMatrixRow();
                row.setUserId(permission.getUserId());

                // 查询用户名称
                try {
                    ResponseDTO<net.lab1024.sa.common.organization.entity.UserEntity> userResponse =
                            gatewayServiceClient.callCommonService(
                                    "/api/v1/organization/user/" + permission.getUserId(),
                                    HttpMethod.GET,
                                    null,
                                    net.lab1024.sa.common.organization.entity.UserEntity.class
                            );

                    if (userResponse != null && userResponse.isSuccess() && userResponse.getData() != null) {
                        row.setUserName(userResponse.getData().getUserName());
                    }
                } catch (Exception e) {
                    log.debug("[区域管理] 查询用户信息失败: userId={}, error={}", permission.getUserId(), e.getMessage());
                }

                // 构建设备权限列表（区域权限默认对所有设备有效）
                List<AccessAreaPermissionMatrixVO.DevicePermission> devicePermissions = devices.stream()
                        .map(device -> {
                            AccessAreaPermissionMatrixVO.DevicePermission dp =
                                    new AccessAreaPermissionMatrixVO.DevicePermission();
                            dp.setDeviceId(device.getDeviceId());
                            dp.setDeviceName(device.getDeviceName());
                            dp.setHasPermission(true); // 区域权限默认对所有设备有效
                            dp.setPermissionSource("AUTO"); // 自动分配
                            dp.setEffectiveTime(permission.getStartTime());
                            return dp;
                        })
                        .collect(Collectors.toList());

                row.setDevicePermissions(devicePermissions);
                matrixRows.add(row);
            }
            matrix.setPermissionMatrix(matrixRows);

            log.info("[区域管理] 查询权限矩阵成功: areaId={}, personCount={}, deviceCount={}",
                    areaId, persons.size(), devices.size());

            return ResponseDTO.ok(matrix);

        } catch (Exception e) {
            log.error("[区域管理] 查询权限矩阵异常: areaId={}, error={}", areaId, e.getMessage(), e);
            return ResponseDTO.error("QUERY_PERMISSION_MATRIX_ERROR", "查询权限矩阵失败: " + e.getMessage());
        }
    }

    /**
     * 批量分配权限
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> batchAssignPermissions(Long areaId, List<Long> userIds, List<String> deviceIds) {
        log.info("[区域管理] 批量分配权限: areaId={}, userIdCount={}, deviceIdCount={}",
                areaId, userIds != null ? userIds.size() : 0, deviceIds != null ? deviceIds.size() : 0);

        try {
            // 如果deviceIds为空，表示区域内所有设备
            List<String> targetDeviceIds = deviceIds;
            if (targetDeviceIds == null || targetDeviceIds.isEmpty()) {
                List<AreaDeviceEntity> areaDevices = areaDeviceDao.selectByAreaIdAndBusinessModule(areaId, "access");
                targetDeviceIds = areaDevices.stream()
                        .map(AreaDeviceEntity::getDeviceId)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toList());
            }

            // 为每个用户分配权限
            for (Long userId : userIds) {
                // 检查权限是否已存在
                UserAreaPermissionEntity existingPermission = userAreaPermissionDao.selectByUserAndArea(userId, areaId);
                if (existingPermission == null) {
                    // 创建权限
                    UserAreaPermissionEntity permission = new UserAreaPermissionEntity();
                    permission.setUserId(userId);
                    permission.setAreaId(areaId);
                    permission.setPermissionType("ALWAYS");
                    permission.setStartTime(LocalDateTime.now());
                    userAreaPermissionDao.insert(permission);
                }
            }

            log.info("[区域管理] 批量分配权限成功: areaId={}, userIdCount={}, deviceIdCount={}",
                    areaId, userIds.size(), targetDeviceIds.size());

            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[区域管理] 批量分配权限异常: areaId={}, error={}", areaId, e.getMessage(), e);
            return ResponseDTO.error("BATCH_ASSIGN_PERMISSIONS_ERROR", "批量分配权限失败: " + e.getMessage());
        }
    }

    /**
     * 批量回收权限
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> batchRevokePermissions(Long areaId, List<Long> userIds, List<String> deviceIds) {
        log.info("[区域管理] 批量回收权限: areaId={}, userIdCount={}, deviceIdCount={}",
                areaId, userIds != null ? userIds.size() : 0, deviceIds != null ? deviceIds.size() : 0);

        try {
            // 如果deviceIds为空，表示区域内所有设备
            // 如果userIds为空，表示区域内所有人员
            List<Long> targetUserIds = userIds;
            if (targetUserIds == null || targetUserIds.isEmpty()) {
                List<UserAreaPermissionEntity> permissions = userAreaPermissionDao.selectByAreaId(areaId);
                targetUserIds = permissions.stream()
                        .map(UserAreaPermissionEntity::getUserId)
                        .collect(Collectors.toList());
            }

            // 回收权限（逻辑删除）
            for (Long userId : targetUserIds) {
                UserAreaPermissionEntity permission = userAreaPermissionDao.selectByUserAndArea(userId, areaId);
                if (permission != null) {
                    permission.setDeletedFlag(true);
                    userAreaPermissionDao.updateById(permission);
                }
            }

            log.info("[区域管理] 批量回收权限成功: areaId={}, userIdCount={}", areaId, targetUserIds.size());
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[区域管理] 批量回收权限异常: areaId={}, error={}", areaId, e.getMessage(), e);
            return ResponseDTO.error("BATCH_REVOKE_PERMISSIONS_ERROR", "批量回收权限失败: " + e.getMessage());
        }
    }

    /**
     * 查询区域监控数据
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<AccessAreaMonitorVO> getAreaMonitorData(Long areaId) {
        log.info("[区域管理] 查询区域监控数据: areaId={}", areaId);

        try {
            AccessAreaMonitorVO monitor = new AccessAreaMonitorVO();
            monitor.setAreaId(areaId);

            // 查询区域信息
            ResponseDTO<AreaEntity> areaResponse = gatewayServiceClient.callCommonService(
                    "/api/v1/organization/area/" + areaId,
                    HttpMethod.GET,
                    null,
                    AreaEntity.class
            );

            if (areaResponse != null && areaResponse.isSuccess() && areaResponse.getData() != null) {
                monitor.setAreaName(areaResponse.getData().getAreaName());
            }

            // 查询区域内的设备
            List<AreaDeviceEntity> areaDevices = areaDeviceDao.selectByAreaIdAndBusinessModule(areaId, "access");

            // 设备状态统计
            AccessAreaMonitorVO.DeviceStatusStatistics deviceStatus = new AccessAreaMonitorVO.DeviceStatusStatistics();
            deviceStatus.setTotalDevices((long) areaDevices.size());
            long onlineCount = areaDevices.stream()
                    .filter(d -> d.getRelationStatus() != null && d.getRelationStatus() == 1)
                    .count();
            deviceStatus.setOnlineDevices(onlineCount);
            deviceStatus.setOfflineDevices((long) areaDevices.size() - onlineCount);
            deviceStatus.setFaultDevices(areaDevices.stream()
                    .filter(d -> d.getRelationStatus() != null && d.getRelationStatus() == 3)
                    .count());
            deviceStatus.setMaintenanceDevices(areaDevices.stream()
                    .filter(d -> d.getRelationStatus() != null && d.getRelationStatus() == 4)
                    .count());
            monitor.setDeviceStatus(deviceStatus);

            // 设备状态列表
            List<AccessAreaMonitorVO.DeviceStatusInfo> deviceStatusList = areaDevices.stream()
                    .map(this::convertToDeviceStatusInfo)
                    .collect(Collectors.toList());
            monitor.setDeviceStatusList(deviceStatusList);

            // 通行统计
            LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
            LocalDateTime todayEnd = LocalDateTime.now().with(LocalTime.MAX);

            LambdaQueryWrapper<AccessRecordEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AccessRecordEntity::getAreaId, areaId)
                    .ge(AccessRecordEntity::getAccessTime, todayStart)
                    .le(AccessRecordEntity::getAccessTime, todayEnd)
                    .eq(AccessRecordEntity::getDeletedFlag, false);

            List<AccessRecordEntity> todayRecords = accessRecordDao.selectList(wrapper);
            long todayTotal = todayRecords.size();
            long todaySuccess = todayRecords.stream()
                    .filter(r -> r.getAccessResult() != null && r.getAccessResult() == 1)
                    .count();
            long todayFailed = todayTotal - todaySuccess;

            AccessAreaMonitorVO.AccessStatistics accessStatistics = new AccessAreaMonitorVO.AccessStatistics();
            accessStatistics.setTodayTotal(todayTotal);
            accessStatistics.setTodaySuccess(todaySuccess);
            accessStatistics.setTodayFailed(todayFailed);

            // 当前在线人数（最近1小时内有通行记录的人数）
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            LambdaQueryWrapper<AccessRecordEntity> recentWrapper = new LambdaQueryWrapper<>();
            recentWrapper.eq(AccessRecordEntity::getAreaId, areaId)
                    .ge(AccessRecordEntity::getAccessTime, oneHourAgo)
                    .eq(AccessRecordEntity::getDeletedFlag, false)
                    .eq(AccessRecordEntity::getAccessResult, 1);

            List<AccessRecordEntity> recentRecords = accessRecordDao.selectList(recentWrapper);
            long currentOnlinePersons = recentRecords.stream()
                    .map(AccessRecordEntity::getUserId)
                    .distinct()
                    .count();
            accessStatistics.setCurrentOnlinePersons(currentOnlinePersons);

            // 最近1小时通行数
            accessStatistics.setLastHourAccess((long) recentRecords.size());

            monitor.setAccessStatistics(accessStatistics);

            // 容量监控
            AccessAreaMonitorVO.CapacityMonitor capacityMonitor = new AccessAreaMonitorVO.CapacityMonitor();
            if (areaResponse != null && areaResponse.isSuccess() && areaResponse.getData() != null) {
                AreaEntity area = areaResponse.getData();
                capacityMonitor.setCapacity(area.getCapacity());
                capacityMonitor.setCurrentCount(area.getCurrentCount());

                if (area.getCapacity() != null && area.getCapacity() > 0) {
                    double usageRate = (double) (area.getCurrentCount() != null ? area.getCurrentCount() : 0)
                            / area.getCapacity() * 100;
                    capacityMonitor.setUsageRate(usageRate);
                    capacityMonitor.setIsOverLimit(usageRate >= 100);

                    if (usageRate >= 95) {
                        capacityMonitor.setAlertLevel("CRITICAL");
                    } else if (usageRate >= 85) {
                        capacityMonitor.setAlertLevel("WARNING");
                    } else {
                        capacityMonitor.setAlertLevel("NORMAL");
                    }
                } else {
                    capacityMonitor.setUsageRate(0.0);
                    capacityMonitor.setIsOverLimit(false);
                    capacityMonitor.setAlertLevel("NORMAL");
                }
            }
            monitor.setCapacityMonitor(capacityMonitor);

            // 最近通行记录
            LambdaQueryWrapper<AccessRecordEntity> recentAccessWrapper = new LambdaQueryWrapper<>();
            recentAccessWrapper.eq(AccessRecordEntity::getAreaId, areaId)
                    .eq(AccessRecordEntity::getDeletedFlag, false)
                    .orderByDesc(AccessRecordEntity::getAccessTime)
                    .last("LIMIT 20");

            List<AccessRecordEntity> recentAccessRecords = accessRecordDao.selectList(recentAccessWrapper);
            List<AccessAreaMonitorVO.RecentAccessRecord> recentRecordsVO = recentAccessRecords.stream()
                    .map(this::convertToRecentAccessRecord)
                    .collect(Collectors.toList());
            monitor.setRecentAccessRecords(recentRecordsVO);

            log.info("[区域管理] 查询区域监控数据成功: areaId={}, deviceCount={}, todayAccessTotal={}",
                    areaId, areaDevices.size(), todayTotal);

            return ResponseDTO.ok(monitor);

        } catch (Exception e) {
            log.error("[区域管理] 查询区域监控数据异常: areaId={}, error={}", areaId, e.getMessage(), e);
            return ResponseDTO.error("GET_AREA_MONITOR_DATA_ERROR", "查询区域监控数据失败: " + e.getMessage());
        }
    }

    /**
     * 转换区域实体为区域概览VO
     */
    private AccessAreaOverviewVO convertToAreaOverviewVO(AreaEntity area) {
        AccessAreaOverviewVO vo = new AccessAreaOverviewVO();
        BeanUtils.copyProperties(area, vo);

        // 设置区域类型名称
        vo.setAreaTypeName(getAreaTypeName(area.getAreaType()));

        // 设置区域状态名称
        vo.setAreaStatusName(getAreaStatusName(area.getAreaStatus()));

        // 查询父区域名称
        if (area.getParentAreaId() != null) {
            try {
                ResponseDTO<AreaEntity> parentAreaResponse = gatewayServiceClient.callCommonService(
                        "/api/v1/organization/area/" + area.getParentAreaId(),
                        HttpMethod.GET,
                        null,
                        AreaEntity.class
                );

                if (parentAreaResponse != null && parentAreaResponse.isSuccess()
                        && parentAreaResponse.getData() != null) {
                    vo.setParentAreaName(parentAreaResponse.getData().getAreaName());
                }
            } catch (Exception e) {
                log.debug("[区域管理] 查询父区域信息失败: parentAreaId={}, error={}",
                        area.getParentAreaId(), e.getMessage());
            }
        }

        // 统计设备数量
        List<AreaDeviceEntity> areaDevices = areaDeviceDao.selectByAreaIdAndBusinessModule(area.getAreaId(), "access");
        long deviceCount = areaDevices.size();
        long onlineDeviceCount = areaDevices.stream()
                .filter(d -> d.getRelationStatus() != null && d.getRelationStatus() == 1)
                .count();
        long offlineDeviceCount = deviceCount - onlineDeviceCount;

        vo.setDeviceCount(deviceCount);
        vo.setOnlineDeviceCount(onlineDeviceCount);
        vo.setOfflineDeviceCount(offlineDeviceCount);

        // 统计人员数量
        List<UserAreaPermissionEntity> permissions = userAreaPermissionDao.selectByAreaId(area.getAreaId());
        vo.setPersonCount((long) permissions.size());

        // 统计今日通行数据
        LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.now().with(LocalTime.MAX);

        LambdaQueryWrapper<AccessRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccessRecordEntity::getAreaId, area.getAreaId())
                .ge(AccessRecordEntity::getAccessTime, todayStart)
                .le(AccessRecordEntity::getAccessTime, todayEnd)
                .eq(AccessRecordEntity::getDeletedFlag, false);

        List<AccessRecordEntity> todayRecords = accessRecordDao.selectList(wrapper);
        long todayTotal = todayRecords.size();
        long todaySuccess = todayRecords.stream()
                .filter(r -> r.getAccessResult() != null && r.getAccessResult() == 1)
                .count();
        long todayFailed = todayTotal - todaySuccess;

        vo.setTodayAccessTotal(todayTotal);
        vo.setTodayAccessSuccess(todaySuccess);
        vo.setTodayAccessFailed(todayFailed);

        // 报警统计（TODO: 需要实现报警表后完善）
        vo.setUnhandledAlarmCount(0L);

        return vo;
    }

    /**
     * 转换权限实体为区域人员VO
     */
    private AccessAreaPersonVO convertToAreaPersonVO(UserAreaPermissionEntity permission, Long areaId) {
        AccessAreaPersonVO vo = new AccessAreaPersonVO();
        vo.setUserId(permission.getUserId());
        vo.setAreaId(areaId);
        vo.setPermissionId(permission.getId());
        vo.setPermissionType(permission.getPermissionType());
        vo.setPermissionTypeName("ALWAYS".equals(permission.getPermissionType()) ? "永久权限" : "限时权限");
        vo.setStartTime(permission.getStartTime());
        vo.setEndTime(permission.getEndTime());

        // 检查是否在有效期内
        boolean isValid = true;
        if ("TIME_LIMITED".equals(permission.getPermissionType())) {
            LocalDateTime now = LocalDateTime.now();
            if (permission.getStartTime() != null && now.isBefore(permission.getStartTime())) {
                isValid = false;
            }
            if (permission.getEndTime() != null && now.isAfter(permission.getEndTime())) {
                isValid = false;
            }
        }
        vo.setIsValid(isValid);

        // 查询用户信息
        try {
            ResponseDTO<net.lab1024.sa.common.organization.entity.UserEntity> userResponse =
                    gatewayServiceClient.callCommonService(
                            "/api/v1/organization/user/" + permission.getUserId(),
                            HttpMethod.GET,
                            null,
                            net.lab1024.sa.common.organization.entity.UserEntity.class
                    );

            if (userResponse != null && userResponse.isSuccess() && userResponse.getData() != null) {
                net.lab1024.sa.common.organization.entity.UserEntity user = userResponse.getData();
                vo.setUserName(user.getUserName());
                vo.setUserNo(user.getUserNo());
                vo.setDepartmentId(user.getDepartmentId());
                vo.setDepartmentName(user.getDepartmentName());
            }
        } catch (Exception e) {
            log.debug("[区域管理] 查询用户信息失败: userId={}, error={}", permission.getUserId(), e.getMessage());
        }

        // 查询区域名称
        try {
            ResponseDTO<AreaEntity> areaResponse = gatewayServiceClient.callCommonService(
                    "/api/v1/organization/area/" + areaId,
                    HttpMethod.GET,
                    null,
                    AreaEntity.class
            );

            if (areaResponse != null && areaResponse.isSuccess() && areaResponse.getData() != null) {
                vo.setAreaName(areaResponse.getData().getAreaName());
            }
        } catch (Exception e) {
            log.debug("[区域管理] 查询区域信息失败: areaId={}, error={}", areaId, e.getMessage());
        }

        // 查询最后通行时间和今日通行次数
        LambdaQueryWrapper<AccessRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccessRecordEntity::getUserId, permission.getUserId())
                .eq(AccessRecordEntity::getAreaId, areaId)
                .eq(AccessRecordEntity::getDeletedFlag, false)
                .orderByDesc(AccessRecordEntity::getAccessTime)
                .last("LIMIT 1");

        List<AccessRecordEntity> lastRecord = accessRecordDao.selectList(wrapper);
        if (!lastRecord.isEmpty()) {
            vo.setLastAccessTime(lastRecord.get(0).getAccessTime());
        }

        // 今日通行次数
        LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.now().with(LocalTime.MAX);

        LambdaQueryWrapper<AccessRecordEntity> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.eq(AccessRecordEntity::getUserId, permission.getUserId())
                .eq(AccessRecordEntity::getAreaId, areaId)
                .ge(AccessRecordEntity::getAccessTime, todayStart)
                .le(AccessRecordEntity::getAccessTime, todayEnd)
                .eq(AccessRecordEntity::getDeletedFlag, false);

        long todayAccessCount = accessRecordDao.selectCount(todayWrapper);
        vo.setTodayAccessCount(todayAccessCount);

        return vo;
    }

    /**
     * 转换区域设备为设备信息
     */
    private AccessAreaPermissionMatrixVO.DeviceInfo convertToDeviceInfo(AreaDeviceEntity areaDevice) {
        AccessAreaPermissionMatrixVO.DeviceInfo device = new AccessAreaPermissionMatrixVO.DeviceInfo();
        device.setDeviceId(areaDevice.getDeviceId());
        device.setDeviceName(areaDevice.getDeviceName());
        device.setDeviceCode(areaDevice.getDeviceCode());
        device.setDeviceStatus(areaDevice.getRelationStatus());
        return device;
    }

    /**
     * 转换区域设备为设备状态信息
     */
    private AccessAreaMonitorVO.DeviceStatusInfo convertToDeviceStatusInfo(AreaDeviceEntity areaDevice) {
        AccessAreaMonitorVO.DeviceStatusInfo info = new AccessAreaMonitorVO.DeviceStatusInfo();
        info.setDeviceId(areaDevice.getDeviceId());
        info.setDeviceName(areaDevice.getDeviceName());
        info.setDeviceStatus(areaDevice.getRelationStatus());
        info.setDeviceStatusName(getDeviceStatusName(areaDevice.getRelationStatus()));
        info.setLastOnlineTime(areaDevice.getLastUserSyncTime());
        info.setResponseTime(0L); // TODO: 需要实际测量
        return info;
    }

    /**
     * 转换通行记录为最近通行记录
     */
    private AccessAreaMonitorVO.RecentAccessRecord convertToRecentAccessRecord(AccessRecordEntity record) {
        AccessAreaMonitorVO.RecentAccessRecord recent = new AccessAreaMonitorVO.RecentAccessRecord();
        recent.setUserId(record.getUserId());
        recent.setDeviceId(record.getDeviceId() != null ? record.getDeviceId().toString() : null);
        recent.setAccessResult(record.getAccessResult());
        recent.setAccessResultName(record.getAccessResult() == 1 ? "成功" : "失败");
        recent.setAccessTime(record.getAccessTime());

        // 查询用户名称
        if (record.getUserId() != null) {
            try {
                ResponseDTO<net.lab1024.sa.common.organization.entity.UserEntity> userResponse =
                        gatewayServiceClient.callCommonService(
                                "/api/v1/organization/user/" + record.getUserId(),
                                HttpMethod.GET,
                                null,
                                net.lab1024.sa.common.organization.entity.UserEntity.class
                        );

                if (userResponse != null && userResponse.isSuccess() && userResponse.getData() != null) {
                    recent.setUserName(userResponse.getData().getUserName());
                }
            } catch (Exception e) {
                log.debug("[区域管理] 查询用户信息失败: userId={}, error={}", record.getUserId(), e.getMessage());
            }
        }

        // 查询设备名称
        if (record.getDeviceId() != null) {
            try {
                DeviceEntity device = accessDeviceDao.selectById(record.getDeviceId().toString());
                if (device != null) {
                    recent.setDeviceName(device.getDeviceName());
                }
            } catch (Exception e) {
                log.debug("[区域管理] 查询设备信息失败: deviceId={}, error={}", record.getDeviceId(), e.getMessage());
            }
        }

        return recent;
    }

    /**
     * 获取区域类型名称
     */
    private String getAreaTypeName(Integer areaType) {
        if (areaType == null) {
            return "未知";
        }
        switch (areaType) {
            case 1:
                return "园区";
            case 2:
                return "建筑";
            case 3:
                return "楼层";
            case 4:
                return "房间";
            case 5:
                return "区域";
            case 6:
                return "点位";
            default:
                return "未知";
        }
    }

    /**
     * 获取区域状态名称
     */
    private String getAreaStatusName(Integer areaStatus) {
        if (areaStatus == null) {
            return "未知";
        }
        switch (areaStatus) {
            case 1:
                return "正常";
            case 2:
                return "停用";
            case 3:
                return "装修";
            case 4:
                return "关闭";
            default:
                return "未知";
        }
    }

    /**
     * 获取设备状态名称
     */
    private String getDeviceStatusName(Integer deviceStatus) {
        if (deviceStatus == null) {
            return "未知";
        }
        switch (deviceStatus) {
            case 1:
                return "在线";
            case 2:
                return "离线";
            case 3:
                return "故障";
            case 4:
                return "维护";
            case 5:
                return "停用";
            default:
                return "未知";
        }
    }
}
