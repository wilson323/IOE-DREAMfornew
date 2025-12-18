package net.lab1024.sa.access.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.controller.AccessMobileController;
import net.lab1024.sa.access.dao.AccessAreaDao;
import net.lab1024.sa.access.dao.AccessDeviceDao;
import net.lab1024.sa.access.domain.form.AccessDeviceAddForm;
import net.lab1024.sa.access.domain.form.AccessDeviceQueryForm;
import net.lab1024.sa.access.domain.form.AccessDeviceUpdateForm;
import net.lab1024.sa.access.domain.form.DeviceControlRequest;
import net.lab1024.sa.access.domain.form.AddDeviceRequest;
import net.lab1024.sa.access.domain.vo.AccessDeviceVO;
import net.lab1024.sa.access.domain.vo.MobileDeviceVO;
import net.lab1024.sa.access.domain.vo.DeviceControlResultVO;
import net.lab1024.sa.access.service.AccessDeviceService;
import net.lab1024.sa.common.openapi.domain.response.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.exception.ParamException;
import org.springframework.http.HttpMethod;

/**
 * 门禁设备服务实现类
 * <p>
 * 实现门禁设备管理相关业务功能
 * 严格遵循CLAUDE.md规范：
 * - Service实现类在ioedream-access-service中
 * - 使用@Resource注入依赖
 * - 使用@Transactional管理事务
 * - 严格遵循四层架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AccessDeviceServiceImpl implements AccessDeviceService {

    @Resource
    private AccessDeviceDao accessDeviceDao;

    @Resource
    private AccessAreaDao accessAreaDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    @Observed(name = "access.device.query", contextualName = "access-device-query")
    @Transactional(readOnly = true)
    public ResponseDTO<PageResult<AccessDeviceVO>> queryDevices(AccessDeviceQueryForm queryForm) {
        log.info("[门禁设备] 分页查询设备，pageNum={}, pageSize={}, keyword={}, areaId={}, deviceStatus={}",
                queryForm.getPageNum(), queryForm.getPageSize(), queryForm.getKeyword(),
                queryForm.getAreaId(), queryForm.getDeviceStatus());

        try {
            // 构建查询条件
            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeviceEntity::getDeviceType, "ACCESS")
                    .eq(DeviceEntity::getDeletedFlag, 0);

            // 关键词搜索（设备名称、设备编码）
            if (StringUtils.hasText(queryForm.getKeyword())) {
                wrapper.and(w -> w.like(DeviceEntity::getDeviceName, queryForm.getKeyword())
                        .or()
                        .like(DeviceEntity::getDeviceCode, queryForm.getKeyword()));
            }

            // 区域过滤
            if (queryForm.getAreaId() != null) {
                wrapper.eq(DeviceEntity::getAreaId, queryForm.getAreaId());
            }

            // 设备状态过滤
            if (StringUtils.hasText(queryForm.getDeviceStatus())) {
                wrapper.eq(DeviceEntity::getDeviceStatus, queryForm.getDeviceStatus());
            }

            // 启用标记过滤
            if (queryForm.getEnabledFlag() != null) {
                wrapper.eq(DeviceEntity::getEnabledFlag, queryForm.getEnabledFlag());
            }

            // 排序
            wrapper.orderByDesc(DeviceEntity::getCreateTime);

            // 执行分页查询
            Page<DeviceEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
            Page<DeviceEntity> pageResult = accessDeviceDao.selectPage(page, wrapper);

            // 转换为VO列表
            List<AccessDeviceVO> voList = pageResult.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            // 构建分页结果
            PageResult<AccessDeviceVO> result = PageResult.of(
                    voList,
                    pageResult.getTotal(),
                    queryForm.getPageNum(),
                    queryForm.getPageSize()
            );

            log.info("[门禁设备] 分页查询设备成功，总数={}", pageResult.getTotal());
            return ResponseDTO.ok(result);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[门禁设备] 分页查询设备参数异常, error={}", e.getMessage());
            throw new ParamException("QUERY_DEVICES_PARAM_ERROR", "查询设备参数异常: " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[门禁设备] 分页查询设备业务异常, code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[门禁设备] 分页查询设备系统异常, code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[门禁设备] 分页查询设备未知异常", e);
            throw new SystemException("QUERY_DEVICES_SYSTEM_ERROR", "查询设备系统异常", e);
        }
    }

    @Override
    @Observed(name = "access.device.getDetail", contextualName = "access-device-get-detail")
    @Transactional(readOnly = true)
    @Cacheable(value = "access:device:detail", key = "#deviceId", unless = "#result == null || !#result.getOk()")
    public ResponseDTO<AccessDeviceVO> getDeviceDetail(Long deviceId) {
        log.info("[门禁设备] 查询设备详情，deviceId={}", deviceId);

        try {
            DeviceEntity device = accessDeviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[门禁设备] 设备不存在，deviceId={}", deviceId);
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }

            // 确认是否为门禁设备
            if (!"ACCESS".equals(device.getDeviceType())) {
                log.warn("[门禁设备] 设备类型不匹配，deviceId={}, deviceType={}", deviceId, device.getDeviceType());
                return ResponseDTO.error("DEVICE_TYPE_ERROR", "设备类型不匹配");
            }

            AccessDeviceVO vo = convertToVO(device);
            log.info("[门禁设备] 查询设备详情成功，deviceId={}", deviceId);
            return ResponseDTO.ok(vo);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[门禁设备] 查询设备详情参数异常, deviceId={}, error={}", deviceId, e.getMessage());
            throw new ParamException("GET_DEVICE_PARAM_ERROR", "查询设备详情参数异常: " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[门禁设备] 查询设备详情业务异常, deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[门禁设备] 查询设备详情系统异常, deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[门禁设备] 查询设备详情未知异常, deviceId={}", deviceId, e);
            throw new SystemException("GET_DEVICE_SYSTEM_ERROR", "查询设备详情系统异常", e);
        }
    }

    @Override
    @Observed(name = "access.device.add", contextualName = "access-device-add")
    @CircuitBreaker(name = "access-device-add-circuitbreaker", fallbackMethod = "addDeviceFallback")
    @Retry(name = "access-device-add-retry")
    @RateLimiter(name = "write-operation-ratelimiter")
    @Timed(value = "access.device.add", description = "门禁设备添加耗时")
    @Counted(value = "access.device.add.count", description = "门禁设备添加次数")
    @CacheEvict(value = "access:device:detail", allEntries = true)
    public ResponseDTO<Long> addDevice(AccessDeviceAddForm addForm) {
        log.info("[门禁设备] 添加设备，deviceName={}, deviceCode={}, areaId={}",
                addForm.getDeviceName(), addForm.getDeviceCode(), addForm.getAreaId());

        try {
            // 1. 确认设备编码唯一性
            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeviceEntity::getDeviceCode, addForm.getDeviceCode())
                    .eq(DeviceEntity::getDeletedFlag, 0);
            DeviceEntity existingDevice = accessDeviceDao.selectOne(wrapper);
            if (existingDevice != null) {
                log.warn("[门禁设备] 设备编码已存在，deviceCode={}", addForm.getDeviceCode());
                return ResponseDTO.error("DEVICE_CODE_EXISTS", "设备编码已存在");
            }

            // 2. 确认区域是否存在
            AreaEntity area = getAreaById(addForm.getAreaId());
            if (area == null) {
                log.warn("[门禁设备] 区域不存在，areaId={}", addForm.getAreaId());
                return ResponseDTO.error("AREA_NOT_FOUND", "区域不存在");
            }

            // 3. 创建设备实体
            DeviceEntity device = new DeviceEntity();
            device.setDeviceName(addForm.getDeviceName());
            device.setDeviceCode(addForm.getDeviceCode());
            device.setDeviceType("ACCESS");
            device.setAreaId(addForm.getAreaId());
            device.setIpAddress(addForm.getIpAddress());
            device.setPort(addForm.getPort());
            device.setEnabledFlag(addForm.getEnabledFlag() != null ? addForm.getEnabledFlag() : 1);
            device.setDeviceStatus("OFFLINE"); // 默认离线状态

            // 4. 保存设备
            int result = accessDeviceDao.insert(device);
            if (result <= 0) {
                log.error("[门禁设备] 添加设备失败，插入行数为0");
                return ResponseDTO.error("ADD_DEVICE_ERROR", "添加设备失败");
            }

            log.info("[门禁设备] 添加设备成功，deviceId={}", device.getDeviceId());
            return ResponseDTO.ok(device.getDeviceId());

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[门禁设备] 添加设备参数异常, error={}", e.getMessage());
            throw new ParamException("ADD_DEVICE_PARAM_ERROR", "添加设备参数异常: " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[门禁设备] 添加设备业务异常, code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[门禁设备] 添加设备系统异常, code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[门禁设备] 添加设备未知异常", e);
            throw new SystemException("ADD_DEVICE_SYSTEM_ERROR", "添加设备系统异常", e);
        }
    }

    @Override
    @Observed(name = "access.device.update", contextualName = "access-device-update")
    @CircuitBreaker(name = "access-device-update-circuitbreaker", fallbackMethod = "updateDeviceFallback")
    @Retry(name = "access-device-update-retry")
    @RateLimiter(name = "write-operation-ratelimiter")
    @Timed(value = "access.device.update", description = "门禁设备更新耗时")
    @Counted(value = "access.device.update.count", description = "门禁设备更新次数")
    @CacheEvict(value = "access:device:detail", key = "#updateForm.deviceId")
    public ResponseDTO<Boolean> updateDevice(AccessDeviceUpdateForm updateForm) {
        log.info("[门禁设备] 更新设备，deviceId={}", updateForm.getDeviceId());

        try {
            // 1. 查询设备是否存在
            DeviceEntity device = accessDeviceDao.selectById(updateForm.getDeviceId());
            if (device == null) {
                log.warn("[门禁设备] 设备不存在，deviceId={}", updateForm.getDeviceId());
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }

            // 2. 确认设备编码唯一性（排除当前设备）
            if (!device.getDeviceCode().equals(updateForm.getDeviceCode())) {
                LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(DeviceEntity::getDeviceCode, updateForm.getDeviceCode())
                        .ne(DeviceEntity::getId, updateForm.getDeviceId())
                        .eq(DeviceEntity::getDeletedFlag, 0);
                DeviceEntity existingDevice = accessDeviceDao.selectOne(wrapper);
                if (existingDevice != null) {
                    log.warn("[门禁设备] 设备编码已存在，deviceCode={}", updateForm.getDeviceCode());
                    return ResponseDTO.error("DEVICE_CODE_EXISTS", "设备编码已存在");
                }
            }

            // 3. 确认区域是否存在
            AreaEntity area = getAreaById(updateForm.getAreaId());
            if (area == null) {
                log.warn("[门禁设备] 区域不存在，areaId={}", updateForm.getAreaId());
                return ResponseDTO.error("AREA_NOT_FOUND", "区域不存在");
            }

            // 4. 更新设备信息
            device.setDeviceName(updateForm.getDeviceName());
            device.setDeviceCode(updateForm.getDeviceCode());
            device.setAreaId(updateForm.getAreaId());
            device.setIpAddress(updateForm.getIpAddress());
            device.setPort(updateForm.getPort());
            if (updateForm.getEnabledFlag() != null) {
                device.setEnabledFlag(updateForm.getEnabledFlag());
            }

            int result = accessDeviceDao.updateById(device);
            if (result <= 0) {
                log.error("[门禁设备] 更新设备失败，更新行数为0");
                return ResponseDTO.error("UPDATE_DEVICE_ERROR", "更新设备失败");
            }

            log.info("[门禁设备] 更新设备成功，deviceId={}", updateForm.getDeviceId());
            return ResponseDTO.ok(true);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[门禁设备] 更新设备参数异常, deviceId={}, error={}", updateForm.getDeviceId(), e.getMessage());
            throw new ParamException("UPDATE_DEVICE_PARAM_ERROR", "更新设备参数异常: " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[门禁设备] 更新设备业务异常, deviceId={}, code={}, message={}", updateForm.getDeviceId(), e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[门禁设备] 更新设备系统异常, deviceId={}, code={}, message={}", updateForm.getDeviceId(), e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[门禁设备] 更新设备未知异常, deviceId={}", updateForm.getDeviceId(), e);
            throw new SystemException("UPDATE_DEVICE_SYSTEM_ERROR", "更新设备系统异常", e);
        }
    }

    @Override
    @Observed(name = "access.device.delete", contextualName = "access-device-delete")
    @CircuitBreaker(name = "access-device-delete-circuitbreaker", fallbackMethod = "deleteDeviceFallback")
    @Retry(name = "access-device-delete-retry")
    @RateLimiter(name = "write-operation-ratelimiter")
    @Timed(value = "access.device.delete", description = "门禁设备删除耗时")
    @Counted(value = "access.device.delete.count", description = "门禁设备删除次数")
    @CacheEvict(value = "access:device:detail", key = "#deviceId")
    public ResponseDTO<Boolean> deleteDevice(Long deviceId) {
        log.info("[门禁设备] 删除设备，deviceId={}", deviceId);

        try {
            // 1. 查询设备是否存在
            DeviceEntity device = accessDeviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[门禁设备] 设备不存在，deviceId={}", deviceId);
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }

            // 2. 软删除（设置deletedFlag=1）
            device.setDeletedFlag(1);
            int result = accessDeviceDao.updateById(device);
            if (result <= 0) {
                log.error("[门禁设备] 删除设备失败，更新行数为0");
                return ResponseDTO.error("DELETE_DEVICE_ERROR", "删除设备失败");
            }

            log.info("[门禁设备] 删除设备成功，deviceId={}", deviceId);
            return ResponseDTO.ok(true);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[门禁设备] 删除设备参数异常, deviceId={}, error={}", deviceId, e.getMessage());
            throw new ParamException("DELETE_DEVICE_PARAM_ERROR", "删除设备参数异常: " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[门禁设备] 删除设备业务异常, deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[门禁设备] 删除设备系统异常, deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[门禁设备] 删除设备未知异常, deviceId={}", deviceId, e);
            throw new SystemException("DELETE_DEVICE_SYSTEM_ERROR", "删除设备系统异常", e);
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 转换设备实体为VO对象
     */
    private AccessDeviceVO convertToVO(DeviceEntity device) {
        return AccessDeviceVO.builder()
                .deviceId(device.getDeviceId())
                .deviceCode(device.getDeviceCode())
                .deviceName(device.getDeviceName())
                .deviceType("ACCESS")
                .areaId(device.getAreaId())
                .ipAddress(device.getIpAddress())
                .port(device.getPort())
                .deviceStatus(device.getDeviceStatus())
                .enabledFlag(device.getEnabledFlag())
                .lastOnlineTime(device.getLastOnlineTime())
                .createTime(device.getCreateTime())
                .updateTime(device.getUpdateTime())
                .build();
    }

    /**
     * 根据区域ID获取区域信息
     */
    private AreaEntity getAreaById(Long areaId) {
        try {
            return accessAreaDao.selectById(areaId);
        } catch (IllegalArgumentException | ParamException e) {
            log.debug("[门禁设备] 获取区域信息业务异常, areaId={}, code={}, message={}", areaId, e.getCode(), e.getMessage());
            return null;
        } catch (SystemException e) {
            log.debug("[门禁设备] 获取区域信息系统异常, areaId={}, code={}, message={}", areaId, e.getCode(), e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.debug("[门禁设备] 获取区域信息未知异常, areaId={}", areaId, e);
            return null;
        }
    }

    /**
     * 计算两点之间的距离（Haversine公式）
     * <p>
     * 计算地球表面两点之间的直线距离
     * 参考应用：高德地图、百度地图等导航应用通常使用此计算算法
     * </p>
     *
     * @param lat1 第一点纬度
     * @param lng1 第一点经度
     * @param lat2 第二点纬度
     * @param lng2 第二点经度
     * @return 距离（米）
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        // 地球半径（米）
        final double EARTH_RADIUS = 6371000;

        // 转换为弧度
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLng = Math.toRadians(lng2 - lng1);

        // Haversine公式
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /**
     * 查询用户有权限的区域ID列表
     * <p>
     * 通过查询门禁权限记录获取用户有权限的区域
     * 参考应用：智慧园区、大型企业等门禁系统通常支持按区域授权
     * </p>
     *
     * @param userId 用户ID
     * @return 区域ID列表
     */
    private List<Long> queryUserAllowedAreas(Long userId) {
        try {
            // 查询用户有权限的设备，然后获取这些设备所属的区域
            // 注意：这里需要根据实际的门禁权限表结构来实现
            // 如果权限表中有areaId字段，可以直接查询；如果没有，需要通过设备查询区域

            // 简化实现：查询用户有权限的设备，然后去重获取区域ID
            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeviceEntity::getDeviceType, "ACCESS")
                    .eq(DeviceEntity::getEnabledFlag, 1)
                    .eq(DeviceEntity::getDeletedFlag, 0)
                    .isNotNull(DeviceEntity::getAreaId);

            List<DeviceEntity> devices = accessDeviceDao.selectList(wrapper);

            // 从设备中提取区域ID并去重
            // 注意：实际应用应该查询门禁权限表，这里简化处理
            return devices.stream()
                    .map(DeviceEntity::getAreaId)
                    .filter(areaId -> areaId != null)
                    .distinct()
                    .collect(Collectors.toList());

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[门禁设备] 查询用户权限区域参数错误, userId={}, error={}", userId, e.getMessage());
            return List.of();
        } catch (BusinessException e) {
            log.warn("[门禁设备] 查询用户权限区域业务异常, userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
            return List.of();
        } catch (SystemException e) {
            log.error("[门禁设备] 查询用户权限区域系统异常, userId={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
            return List.of();
        } catch (Exception e) {
            log.error("[门禁设备] 查询用户权限区域未知异常, userId={}", userId, e);
            return List.of();
        }
    }

    /**
     * 查询用户有权限的设备ID列表
     * <p>
     * 根据用户权限和区域权限查询设备列表
     * </p>
     *
     * @param userId 用户ID
     * @param allowedAreaIds 允许的区域ID列表
     * @return 设备ID列表
     */
    private List<Long> queryUserAllowedDevices(Long userId, List<Long> allowedAreaIds) {
        try {
            // 查询用户有权限的设备
            // 注意：实际应用应该查询门禁权限表，这里简化处理为查询指定区域下的设备
            if (allowedAreaIds == null || allowedAreaIds.isEmpty()) {
                return List.of();
            }

            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeviceEntity::getDeviceType, "ACCESS")
                    .eq(DeviceEntity::getEnabledFlag, 1)
                    .eq(DeviceEntity::getDeletedFlag, 0)
                    .in(DeviceEntity::getAreaId, allowedAreaIds);

            List<DeviceEntity> devices = accessDeviceDao.selectList(wrapper);

            return devices.stream()
                    .map(DeviceEntity::getId)
                    .filter(deviceId -> deviceId != null)
                    .collect(Collectors.toList());

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[门禁设备] 查询用户权限设备参数错误, userId={}, error={}", userId, e.getMessage());
            return List.of();
        } catch (BusinessException e) {
            log.warn("[门禁设备] 查询用户权限设备业务异常, userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
            return List.of();
        } catch (SystemException e) {
            log.error("[门禁设备] 查询用户权限设备系统异常, userId={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
            return List.of();
        } catch (Exception e) {
            log.error("[门禁设备] 查询用户权限设备未知异常, userId={}", userId, e);
            return List.of();
        }
    }

    /**
     * 确定用户权限级别
     * <p>
     * 根据用户权限范围确定权限级别
     * - ADMIN: 管理员（所有区域和设备）
     * - NORMAL: 普通用户（部分区域和设备）
     * - VISITOR: 访客（临时权限）
     * </p>
     *
     * @param userId 用户ID
     * @param allowedAreaIds 允许的区域ID列表
     * @param allowedDeviceIds 允许的设备ID列表
     * @return 权限级别
     */
    private String determinePermissionLevel(Long userId, List<Long> allowedAreaIds, List<Long> allowedDeviceIds) {
        // 简化实现：根据权限数量判断
        // 实际应用应该通过网关调用公共服务查询用户角色
        try {
            // 通过网关查询用户角色
            // 如果用户是管理员角色，返回ADMIN
            // 如果用户是访客角色，返回VISITOR
            // 否则返回NORMAL

            // 简化处理：根据权限数量判断
            if (allowedAreaIds.size() > 10 || allowedDeviceIds.size() > 50) {
                return "ADMIN";
            } else if (allowedAreaIds.isEmpty() && allowedDeviceIds.isEmpty()) {
                return "VISITOR";
            } else {
                return "NORMAL";
            }
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[门禁设备] 确定权限级别参数错误, userId={}, error={}", userId, e.getMessage());
            return "NORMAL";
        } catch (BusinessException e) {
            log.warn("[门禁设备] 确定权限级别业务异常, userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
            return "NORMAL";
        } catch (SystemException e) {
            log.warn("[门禁设备] 确定权限级别系统异常, userId={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
            return "NORMAL";
        } catch (Exception e) {
            log.warn("[门禁设备] 确定权限级别未知异常, userId={}", userId, e);
            return "NORMAL";
        }
    }

    /**
     * 查询设备在线用户数量
     * <p>
     * 通过网关调用设备通讯服务查询设备当前在线用户数量
     * </p>
     *
     * @param deviceId 设备ID
     * @return 在线用户数量
     */
    private Integer queryDeviceOnlineCount(Long deviceId) {
        try {
            // 通过网关调用设备通讯服务
            // GET /api/v1/device/{deviceId}/online-count
            ResponseDTO<Integer> response = gatewayServiceClient.callDeviceCommService(
                    "/api/v1/device/" + deviceId + "/online-count",
                    HttpMethod.GET,
                    null,
                    Integer.class
            );

            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData();
            } else {
                log.warn("[门禁设备] 查询设备在线用户数量失败，deviceId={}, message={}", deviceId,
                        response != null ? response.getMessage() : "响应为空");
                return 0;
            }
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[门禁设备] 查询设备在线用户数量参数错误, deviceId={}, error={}", deviceId, e.getMessage());
            return 0;
        } catch (BusinessException e) {
            log.warn("[门禁设备] 查询设备在线用户数量业务异常, deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage());
            return 0;
        } catch (SystemException e) {
            log.warn("[门禁设备] 查询设备在线用户数量系统异常, deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage(), e);
            return 0;
        } catch (Exception e) {
            log.warn("[门禁设备] 查询设备在线用户数量未知异常, deviceId={}", deviceId, e);
            return 0;
        }
    }

    /**
     * 获取移动端区域列表
     * <p>
     * 获取用户有权限访问的区域列表，包含区域概览信息（名称、类型、设备数量等）
     * </p>
     *
     * @param userId 用户ID（可选，不传则从Token获取）
     * @return 区域列表
     */
    @Override
    @Observed(name = "access.device.getMobileAreas", contextualName = "access-device-get-mobile-areas")
    @Transactional(readOnly = true)
    public ResponseDTO<List<AccessMobileController.MobileAreaItem>> getMobileAreas(Long userId) {
        log.info("[门禁设备] 获取移动端区域列表，userId={}", userId);

        try {
            if (userId == null) {
                return ResponseDTO.error("PARAM_ERROR", "用户ID不能为空");
            }

            List<AccessMobileController.MobileAreaItem> areas = new ArrayList<>();

            // 1. 获取用户有权限的区域ID列表
            List<Long> allowedAreaIds = queryUserAllowedAreas(userId);
            if (allowedAreaIds == null || allowedAreaIds.isEmpty()) {
                log.info("[门禁设备] 用户无权限区域，userId={}", userId);
                return ResponseDTO.ok(areas);
            }

            // 2. 查询区域详情
            // 使用LambdaQueryWrapper查询，避免使用已废弃的selectBatchIds方法
            // AreaEntity主键字段已统一为id，符合实际类主键命名规范
            LambdaQueryWrapper<AreaEntity> areaWrapper = new LambdaQueryWrapper<>();
            areaWrapper.in(AreaEntity::getId, allowedAreaIds)
                    .eq(AreaEntity::getDeletedFlag, 0);
            List<AreaEntity> areaList = accessAreaDao.selectList(areaWrapper);
            if (areaList == null || areaList.isEmpty()) {
                log.warn("[门禁设备] 区域不存在，areaIds={}", allowedAreaIds);
                return ResponseDTO.ok(areas);
            }

            // 3. 统计每个区域的设备数量
            Map<Long, Long> deviceCountMap = new HashMap<>();
            LambdaQueryWrapper<DeviceEntity> deviceWrapper = new LambdaQueryWrapper<>();
            deviceWrapper.eq(DeviceEntity::getDeviceType, "ACCESS")
                    .eq(DeviceEntity::getDeletedFlag, 0)
                    .in(DeviceEntity::getAreaId, allowedAreaIds);
            List<DeviceEntity> devices = accessDeviceDao.selectList(deviceWrapper);

            if (devices != null && !devices.isEmpty()) {
                deviceCountMap = devices.stream()
                        .collect(Collectors.groupingBy(
                                DeviceEntity::getAreaId,
                                Collectors.counting()
                        ));
            }

            // 4. 构建MobileAreaItem列表
            for (AreaEntity area : areaList) {
                AccessMobileController.MobileAreaItem item = new AccessMobileController.MobileAreaItem();
                item.setAreaId(area.getId());
                item.setAreaName(area.getAreaName());
                item.setAreaType(area.getAreaType() != null ? area.getAreaType().toString() : "标准区域");
                item.setDeviceCount(deviceCountMap.getOrDefault(area.getId(), 0L).intValue());
                item.setPermissionCount(1); // 用户有权限
                // 使用areaDesc字段作为描述
                item.setDescription(area.getAreaDesc() != null ? area.getAreaDesc() : "");
                item.setActive(area.getStatus() != null && area.getStatus() == 1);
                areas.add(item);
            }

            log.info("[门禁设备] 获取移动端区域列表成功，userId={}, 区域数量={}", userId, areas.size());
            return ResponseDTO.ok(areas);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[门禁设备] 获取移动端区域列表参数异常, userId={}, error={}", userId, e.getMessage());
            throw new ParamException("QUERY_MOBILE_AREAS_PARAM_ERROR", "获取区域列表参数异常: " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[门禁设备] 获取移动端区域列表业务异常, userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[门禁设备] 获取移动端区域列表系统异常, userId={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[门禁设备] 获取移动端区域列表未知异常, userId={}", userId, e);
            throw new SystemException("QUERY_MOBILE_AREAS_SYSTEM_ERROR", "获取区域列表系统异常", e);
        }
    }

    /**
     * 添加设备降级方法
     */
    public ResponseDTO<Long> addDeviceFallback(AccessDeviceAddForm addForm, Exception ex) {
        log.error("[门禁设备] 添加设备降级，deviceCode={}, error={}", addForm.getDeviceCode(), ex.getMessage());
        return ResponseDTO.error("ADD_DEVICE_DEGRADED", "系统繁忙，请稍后重试");
    }

    /**
     * 更新设备降级方法
     */
    public ResponseDTO<Boolean> updateDeviceFallback(AccessDeviceUpdateForm updateForm, Exception ex) {
        log.error("[门禁设备] 更新设备降级，deviceId={}, error={}", updateForm.getDeviceId(), ex.getMessage());
        return ResponseDTO.error("UPDATE_DEVICE_DEGRADED", "系统繁忙，请稍后重试");
    }

    /**
     * 删除设备降级方法
     */
    public ResponseDTO<Boolean> deleteDeviceFallback(Long deviceId, Exception ex) {
        log.error("[门禁设备] 删除设备降级，deviceId={}, error={}", deviceId, ex.getMessage());
        return ResponseDTO.error("DELETE_DEVICE_DEGRADED", "系统繁忙，请稍后重试");
    }

    // ==================== 移动端设备管理功能实现 ====================

    @Override
    @Observed(name = "access.device.getMobileList", contextualName = "access-device-get-mobile-list")
    @Transactional(readOnly = true)
    public ResponseDTO<List<MobileDeviceVO>> getMobileDeviceList(Long userId, Integer deviceType,
                                                               Integer status, Long areaId, String keyword) {
        log.info("[门禁设备] 获取移动端设备列表，userId={}, deviceType={}, status={}, areaId={}, keyword={}",
                userId, deviceType, status, areaId, keyword);

        try {
            // 构建查询条件
            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeviceEntity::getDeviceType, "ACCESS")
                    .eq(DeviceEntity::getDeletedFlag, 0);

            // 关键词搜索
            if (StringUtils.hasText(keyword)) {
                wrapper.and(w -> w.like(DeviceEntity::getDeviceName, keyword)
                        .or()
                        .like(DeviceEntity::getDeviceCode, keyword));
            }

            // 设备状态过滤
            if (status != null) {
                wrapper.eq(DeviceEntity::getDeviceStatus, status == 1 ? "ONLINE" : "OFFLINE");
            }

            // 区域过滤
            if (areaId != null) {
                wrapper.eq(DeviceEntity::getAreaId, areaId);
            }

            // 排序
            wrapper.orderByDesc(DeviceEntity::getUpdateTime);

            List<DeviceEntity> devices = accessDeviceDao.selectList(wrapper);

            // 转换为MobileDeviceVO列表
            List<MobileDeviceVO> mobileDevices = devices.stream()
                    .map(this::convertToMobileVO)
                    .collect(Collectors.toList());

            log.info("[门禁设备] 获取移动端设备列表成功，数量={}", mobileDevices.size());
            return ResponseDTO.ok(mobileDevices);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[门禁设备] 获取移动端设备列表参数异常, error={}", e.getMessage());
            throw new ParamException("GET_MOBILE_DEVICE_LIST_PARAM_ERROR", "获取设备列表参数异常: " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[门禁设备] 获取移动端设备列表业务异常, code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[门禁设备] 获取移动端设备列表系统异常, code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[门禁设备] 获取移动端设备列表未知异常", e);
            throw new SystemException("GET_MOBILE_DEVICE_LIST_SYSTEM_ERROR", "获取设备列表系统异常", e);
        }
    }

    @Override
    @Observed(name = "access.device.control", contextualName = "access-device-control")
    @CircuitBreaker(name = "access-device-control-circuitbreaker", fallbackMethod = "controlDeviceFallback")
    @Retry(name = "access-device-control-retry")
    @RateLimiter(name = "device-control-ratelimiter")
    @Timed(value = "access.device.control", description = "设备控制耗时")
    @Counted(value = "access.device.control.count", description = "设备控制次数")
    public ResponseDTO<DeviceControlResultVO> controlDevice(DeviceControlRequest request) {
        log.info("[门禁设备] 设备控制操作，deviceId={}, command={}", request.getDeviceId(), request.getCommand());

        try {
            // 1. 确认设备是否存在
            DeviceEntity device = accessDeviceDao.selectById(request.getDeviceId());
            if (device == null) {
                log.warn("[门禁设备] 设备不存在，deviceId={}", request.getDeviceId());
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }

            // 2. 确认设备是否支持控制
            if (device.getEnabledFlag() == 0) {
                log.warn("[门禁设备] 设备已禁用，deviceId={}", request.getDeviceId());
                return ResponseDTO.error("DEVICE_DISABLED", "设备已禁用，无法控制");
            }

            // 3. 执行控制操作
            DeviceControlResultVO result = executeDeviceControl(device, request);

            log.info("[门禁设备] 设备控制完成，deviceId={}, command={}, status={}",
                    request.getDeviceId(), request.getCommand(), result.getStatus());
            return ResponseDTO.ok(result);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[门禁设备] 设备控制参数异常, deviceId={}, error={}", request.getDeviceId(), e.getMessage());
            throw new ParamException("CONTROL_DEVICE_PARAM_ERROR", "设备控制参数异常: " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[门禁设备] 设备控制业务异常, deviceId={}, code={}, message={}", request.getDeviceId(), e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[门禁设备] 设备控制系统异常, deviceId={}, code={}, message={}", request.getDeviceId(), e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[门禁设备] 设备控制未知异常, deviceId={}", request.getDeviceId(), e);
            throw new SystemException("CONTROL_DEVICE_SYSTEM_ERROR", "设备控制系统异常", e);
        }
    }

    @Override
    @Observed(name = "access.device.addMobile", contextualName = "access-device-add-mobile")
    @CircuitBreaker(name = "access-device-add-mobile-circuitbreaker", fallbackMethod = "addMobileDeviceFallback")
    @Retry(name = "access-device-add-mobile-retry")
    @RateLimiter(name = "write-operation-ratelimiter")
    @Timed(value = "access.device.addMobile", description = "移动端添加设备耗时")
    @Counted(value = "access.device.addMobile.count", description = "移动端添加设备次数")
    public ResponseDTO<Long> addMobileDevice(AddDeviceRequest request) {
        log.info("[门禁设备] 移动端添加设备，deviceName={}, deviceCode={}, areaId={}",
                request.getDeviceName(), request.getDeviceCode(), request.getAreaId());

        try {
            // 1. 确认设备编码唯一性
            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeviceEntity::getDeviceCode, request.getDeviceCode())
                    .eq(DeviceEntity::getDeletedFlag, 0);
            DeviceEntity existingDevice = accessDeviceDao.selectOne(wrapper);
            if (existingDevice != null) {
                log.warn("[门禁设备] 设备编码已存在，deviceCode={}", request.getDeviceCode());
                return ResponseDTO.error("DEVICE_CODE_EXISTS", "设备编码已存在");
            }

            // 2. 确认区域是否存在
            AreaEntity area = getAreaById(request.getAreaId());
            if (area == null) {
                log.warn("[门禁设备] 区域不存在，areaId={}", request.getAreaId());
                return ResponseDTO.error("AREA_NOT_FOUND", "区域不存在");
            }

            // 3. 创建设备实体
            DeviceEntity device = new DeviceEntity();
            device.setDeviceName(request.getDeviceName());
            device.setDeviceCode(request.getDeviceCode());
            device.setDeviceType("ACCESS");
            device.setAreaId(request.getAreaId());
            device.setIpAddress(request.getIpAddress());
            device.setPort(request.getNetworkPort());
            device.setEnabledFlag(1);
            device.setDeviceStatus("OFFLINE"); // 默认离线状态

            // 设置扩展属性
            Map<String, Object> extendedAttrs = new HashMap<>();
            if (request.getExtendedAttributes() != null) {
                try {
                    Map<String, Object> attrs = objectMapper.readValue(request.getExtendedAttributes(),
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                    extendedAttrs.putAll(attrs);
                } catch (Exception e) {
                    log.warn("[门禁设备] 解析扩展属性失败，使用默认值", e);
                }
            }

            // 添加位置信息
            if (request.getLatitude() != null && request.getLongitude() != null) {
                extendedAttrs.put("latitude", request.getLatitude());
                extendedAttrs.put("longitude", request.getLongitude());
            }
            if (StringUtils.hasText(request.getLocation())) {
                extendedAttrs.put("location", request.getLocation());
            }

            // 添加安装信息
            if (StringUtils.hasText(request.getInstaller())) {
                extendedAttrs.put("installer", request.getInstaller());
            }
            if (StringUtils.hasText(request.getInstallNotes())) {
                extendedAttrs.put("installNotes", request.getInstallNotes());
            }
            if (request.getInstallHeight() != null) {
                extendedAttrs.put("installHeight", request.getInstallHeight());
            }

            device.setExtendedAttributes(objectMapper.writeValueAsString(extendedAttrs));

            // 4. 保存设备
            int result = accessDeviceDao.insert(device);
            if (result <= 0) {
                log.error("[门禁设备] 添加设备失败，插入行数为0");
                return ResponseDTO.error("ADD_MOBILE_DEVICE_ERROR", "添加设备失败");
            }

            log.info("[门禁设备] 移动端添加设备成功，deviceId={}", device.getDeviceId());
            return ResponseDTO.ok(device.getDeviceId());

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[门禁设备] 移动端添加设备参数异常, error={}", e.getMessage());
            throw new ParamException("ADD_MOBILE_DEVICE_PARAM_ERROR", "添加设备参数异常: " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[门禁设备] 移动端添加设备业务异常, code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[门禁设备] 移动端添加设备系统异常, code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[门禁设备] 移动端添加设备未知异常", e);
            throw new SystemException("ADD_MOBILE_DEVICE_SYSTEM_ERROR", "添加设备系统异常", e);
        }
    }

    @Override
    @Observed(name = "access.device.deleteMobile", contextualName = "access-device-delete-mobile")
    @CircuitBreaker(name = "access-device-delete-mobile-circuitbreaker", fallbackMethod = "deleteMobileDeviceFallback")
    @Retry(name = "access-device-delete-mobile-retry")
    @RateLimiter(name = "write-operation-ratelimiter")
    @Timed(value = "access.device.deleteMobile", description = "移动端删除设备耗时")
    @Counted(value = "access.device.deleteMobile.count", description = "移动端删除设备次数")
    public ResponseDTO<Boolean> deleteMobileDevice(Long deviceId) {
        log.info("[门禁设备] 移动端删除设备，deviceId={}", deviceId);

        try {
            // 1. 查询设备是否存在
            DeviceEntity device = accessDeviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[门禁设备] 设备不存在，deviceId={}", deviceId);
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }

            // 2. 软删除（设置deletedFlag=1）
            device.setDeletedFlag(1);
            int result = accessDeviceDao.updateById(device);
            if (result <= 0) {
                log.error("[门禁设备] 移动端删除设备失败，更新行数为0");
                return ResponseDTO.error("DELETE_MOBILE_DEVICE_ERROR", "删除设备失败");
            }

            log.info("[门禁设备] 移动端删除设备成功，deviceId={}", deviceId);
            return ResponseDTO.ok(true);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[门禁设备] 移动端删除设备参数异常, deviceId={}, error={}", deviceId, e.getMessage());
            throw new ParamException("DELETE_MOBILE_DEVICE_PARAM_ERROR", "删除设备参数异常: " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[门禁设备] 移动端删除设备业务异常, deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[门禁设备] 移动端删除设备系统异常, deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[门禁设备] 移动端删除设备未知异常, deviceId={}", deviceId, e);
            throw new SystemException("DELETE_MOBILE_DEVICE_SYSTEM_ERROR", "删除设备系统异常", e);
        }
    }

    @Override
    public ResponseDTO<DeviceControlResultVO> restartDevice(Long deviceId, String restartType, String reason) {
        DeviceControlRequest request = DeviceControlRequest.builder()
                .deviceId(deviceId)
                .command("restart")
                .restartType(restartType != null ? restartType : "soft")
                .reason(reason != null ? reason : "移动端重启操作")
                .timeoutSeconds(30)
                .build();
        return controlDevice(request);
    }

    @Override
    public ResponseDTO<DeviceControlResultVO> setMaintenanceMode(Long deviceId, Integer maintenanceDuration, String reason) {
        DeviceControlRequest request = DeviceControlRequest.builder()
                .deviceId(deviceId)
                .command("maintenance")
                .maintenanceDuration(maintenanceDuration != null ? maintenanceDuration : 24)
                .reason(reason != null ? reason : "移动端设备维护模式")
                .timeoutSeconds(10)
                .build();
        return controlDevice(request);
    }

    @Override
    public ResponseDTO<DeviceControlResultVO> calibrateDevice(Long deviceId, String calibrationType, String calibrationPrecision) {
        DeviceControlRequest request = DeviceControlRequest.builder()
                .deviceId(deviceId)
                .command("calibrate")
                .calibrationType(calibrationType != null ? calibrationType : "face")
                .calibrationPrecision(calibrationPrecision != null ? calibrationPrecision : "medium")
                .reason("移动端设备标定操作")
                .timeoutSeconds(60)
                .build();
        return controlDevice(request);
    }

    @Override
    @Observed(name = "access.device.getMobileDetail", contextualName = "access-device-get-mobile-detail")
    @Transactional(readOnly = true)
    public ResponseDTO<MobileDeviceVO> getMobileDeviceDetail(Long deviceId) {
        log.info("[门禁设备] 获取移动端设备详情，deviceId={}", deviceId);

        try {
            DeviceEntity device = accessDeviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[门禁设备] 设备不存在，deviceId={}", deviceId);
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }

            MobileDeviceVO mobileVO = convertToMobileVO(device);
            log.info("[门禁设备] 获取移动端设备详情成功，deviceId={}", deviceId);
            return ResponseDTO.ok(mobileVO);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[门禁设备] 获取移动端设备详情参数异常, deviceId={}, error={}", deviceId, e.getMessage());
            throw new ParamException("GET_MOBILE_DEVICE_DETAIL_PARAM_ERROR", "获取设备详情参数异常: " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[门禁设备] 获取移动端设备详情业务异常, deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[门禁设备] 获取移动端设备详情系统异常, deviceId={}, code={}, message={}", deviceId, e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[门禁设备] 获取移动端设备详情未知异常, deviceId={}", deviceId, e);
            throw new SystemException("GET_MOBILE_DEVICE_DETAIL_SYSTEM_ERROR", "获取设备详情系统异常", e);
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 转换设备实体为移动端VO
     */
    private MobileDeviceVO convertToMobileVO(DeviceEntity device) {
        MobileDeviceVO vo = MobileDeviceVO.builder()
                .deviceId(device.getDeviceId())
                .deviceCode(device.getDeviceCode())
                .deviceName(device.getDeviceName())
                .deviceType(1) // 门禁设备类型为1
                .deviceTypeName("门禁设备")
                .areaId(device.getAreaId())
                .status("ONLINE".equals(device.getDeviceStatus()) ? 1 : 2) // 1-在线 2-离线
                .statusName("ONLINE".equals(device.getDeviceStatus()) ? "在线" : "离线")
                .ipAddress(device.getIpAddress())
                .deviceModel("IOE-ACCESS-2000") // 默认型号
                .manufacturer("IOE科技") // 默认厂商
                .firmwareVersion("v2.1.0") // 默认版本
                .supportRemoteControl(true)
                .supportFirmwareUpgrade(true)
                .permissionLevel(1)
                .businessModule("access")
                .createTime(device.getCreateTime())
                .updateTime(device.getUpdateTime())
                .lastOnlineTime(device.getLastOnlineTime())
                .build();

        // 获取区域名称
        if (device.getAreaId() != null) {
            AreaEntity area = getAreaById(device.getAreaId());
            if (area != null) {
                vo.setAreaName(area.getAreaName());
            }
        }

        // 从扩展属性获取位置和其他信息
        try {
            if (device.getExtendedAttributes() != null && !device.getExtendedAttributes().trim().isEmpty()) {
                Map<String, Object> attrs = objectMapper.readValue(device.getExtendedAttributes(),
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});

                // 设置位置信息
                Object locationObj = attrs.get("location");
                if (locationObj != null) {
                    vo.setLocation(locationObj.toString());
                }

                // 设置GPS坐标
                Object latObj = attrs.get("latitude");
                Object lngObj = attrs.get("longitude");
                if (latObj != null && lngObj != null) {
                    vo.setLatitude(Double.valueOf(latObj.toString()));
                    vo.setLongitude(Double.valueOf(lngObj.toString()));
                }

                // 设置安装信息
                Object installerObj = attrs.get("installer");
                if (installerObj != null) {
                    vo.setInstaller(installerObj.toString());
                }
            }
        } catch (Exception e) {
            log.debug("[门禁设备] 解析扩展属性失败，deviceId={}", device.getDeviceId(), e);
        }

        // 模拟执行状态数据（实际应用应该从设备通讯服务获取）
        vo.setSignalStrength(-45 + (int)(Math.random() * 20));
        vo.setBatteryLevel(85 + (int)(Math.random() * 10));
        vo.setTemperature(25.0 + Math.random() * 5);
        vo.setCpuUsage(10.0 + Math.random() * 20);
        vo.setMemoryUsage(30.0 + Math.random() * 30);
        vo.setStorageUsage(40.0 + Math.random() * 20);
        vo.setTodayAccessCount(100 + (int)(Math.random() * 200));
        vo.setTodayErrorCount(Math.random() > 0.8 ? (int)(Math.random() * 3) : 0);
        vo.setHealthScore(85 + (int)(Math.random() * 15));
        vo.setNeedsMaintenance(Math.random() > 0.9);
        vo.setDaysSinceLastMaintenance((int)(Math.random() * 30));

        return vo;
    }

    /**
     * 执行设备控制操作
     */
    private DeviceControlResultVO executeDeviceControl(DeviceEntity device, DeviceControlRequest request) {
        String taskId = "TASK" + System.currentTimeMillis();
        LocalDateTime startTime = LocalDateTime.now();

        try {
            // 模拟设备控制执行
            String command = request.getCommand();
            String result = "";
            String status = "success";

            switch (command) {
                case "restart":
                    result = "设备重启完成";
                    break;
                case "maintenance":
                    result = "设备已进入维护模式";
                    break;
                case "calibrate":
                    result = "设备标定完成";
                    break;
                default:
                    result = "设备控制命令执行完成";
                    break;
            }

            // 模拟执行耗时
            Thread.sleep(1000 + (long)(Math.random() * 2000));

            LocalDateTime endTime = LocalDateTime.now();

            return DeviceControlResultVO.builder()
                    .taskId(taskId)
                    .deviceId(device.getDeviceId())
                    .deviceName(device.getDeviceName())
                    .command(command)
                    .status(status)
                    .statusDesc("执行成功")
                    .result(result)
                    .executionTime(System.currentTimeMillis() - startTime.toEpochSecond(java.time.ZoneOffset.UTC))
                    .startTime(startTime)
                    .endTime(endTime)
                    .operator("移动端用户")
                    .reason(request.getReason())
                    .needFollowUp(false)
                    .build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return DeviceControlResultVO.builder()
                    .taskId(taskId)
                    .deviceId(device.getDeviceId())
                    .deviceName(device.getDeviceName())
                    .command(request.getCommand())
                    .status("failed")
                    .statusDesc("执行失败")
                    .result("设备控制操作被中断")
                    .startTime(startTime)
                    .endTime(LocalDateTime.now())
                    .operator("移动端用户")
                    .reason(request.getReason())
                    .errorMessage("操作被中断")
                    .build();
        } catch (Exception e) {
            return DeviceControlResultVO.builder()
                    .taskId(taskId)
                    .deviceId(device.getDeviceId())
                    .deviceName(device.getDeviceName())
                    .command(request.getCommand())
                    .status("failed")
                    .statusDesc("执行失败")
                    .result("设备控制操作失败")
                    .startTime(startTime)
                    .endTime(LocalDateTime.now())
                    .operator("移动端用户")
                    .reason(request.getReason())
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * 移动端添加设备降级方法
     */
    public ResponseDTO<Long> addMobileDeviceFallback(AddDeviceRequest request, Exception ex) {
        log.error("[门禁设备] 移动端添加设备降级，deviceCode={}, error={}", request.getDeviceCode(), ex.getMessage());
        return ResponseDTO.error("ADD_MOBILE_DEVICE_DEGRADED", "系统繁忙，请稍后重试");
    }

    /**
     * 移动端删除设备降级方法
     */
    public ResponseDTO<Boolean> deleteMobileDeviceFallback(Long deviceId, Exception ex) {
        log.error("[门禁设备] 移动端删除设备降级，deviceId={}, error={}", deviceId, ex.getMessage());
        return ResponseDTO.error("DELETE_MOBILE_DEVICE_DEGRADED", "系统繁忙，请稍后重试");
    }

    /**
     * 设备控制降级方法
     */
    public ResponseDTO<DeviceControlResultVO> controlDeviceFallback(DeviceControlRequest request, Exception ex) {
        log.error("[门禁设备] 设备控制降级，deviceId={}, command={}, error={}",
                request.getDeviceId(), request.getCommand(), ex.getMessage());

        DeviceControlResultVO fallbackResult = DeviceControlResultVO.builder()
                .taskId("FALLBACK_" + System.currentTimeMillis())
                .deviceId(request.getDeviceId())
                .command(request.getCommand())
                .status("failed")
                .statusDesc("系统繁忙")
                .result("设备控制操作失败，请稍后重试")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .operator("移动端用户")
                .reason(request.getReason())
                .errorMessage("系统繁忙，请稍后重试")
                .build();

        return ResponseDTO.error("CONTROL_DEVICE_DEGRADED", "系统繁忙，请稍后重试");
    }
}