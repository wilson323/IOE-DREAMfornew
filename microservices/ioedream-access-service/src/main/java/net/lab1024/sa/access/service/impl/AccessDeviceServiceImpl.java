package net.lab1024.sa.access.service.impl;

import java.time.LocalDateTime;
import java.util.List;
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
import net.lab1024.sa.access.domain.form.AccessDeviceAddForm;
import net.lab1024.sa.access.domain.form.AccessDeviceQueryForm;
import net.lab1024.sa.access.domain.form.AccessDeviceUpdateForm;
import net.lab1024.sa.access.domain.vo.AccessDeviceVO;
import net.lab1024.sa.access.service.AccessDeviceService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 门禁设备管理服务实现
 * <p>
 * 严格遵循CLAUDE.md规范和ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md架构方案：
 * - 使用公共DeviceEntity（在common-business中）
 * - 只管理门禁设备（deviceType='ACCESS'）
 * - 遵循四层架构：Controller → Service → Manager → DAO
 * - 使用@Resource依赖注入
 * - 使用@Transactional事务管理
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
@Slf4j
@Service
public class AccessDeviceServiceImpl implements AccessDeviceService {

    @Resource
    private AccessDeviceDao accessDeviceDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 分页查询门禁设备列表
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<PageResult<AccessDeviceVO>> queryDeviceList(AccessDeviceQueryForm queryForm) {
        log.info("[门禁设备] 分页查询设备列表: pageNum={}, pageSize={}, keyword={}, areaId={}, deviceStatus={}, enabled={}",
                queryForm.getPageNum(), queryForm.getPageSize(), queryForm.getKeyword(),
                queryForm.getAreaId(), queryForm.getDeviceStatus(), queryForm.getEnabled());

        try {
            // 构建查询条件
            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();
            
            // 设备类型：固定为ACCESS
            wrapper.eq(DeviceEntity::getDeviceType, "ACCESS");
            
            // 关键字查询（设备名称或设备编码）
            if (StringUtils.hasText(queryForm.getKeyword())) {
                wrapper.and(w -> w.like(DeviceEntity::getDeviceName, queryForm.getKeyword())
                        .or()
                        .like(DeviceEntity::getDeviceCode, queryForm.getKeyword()));
            }
            
            // 区域ID
            if (queryForm.getAreaId() != null) {
                wrapper.eq(DeviceEntity::getAreaId, queryForm.getAreaId());
            }
            
            // 设备状态
            if (queryForm.getDeviceStatus() != null) {
                wrapper.eq(DeviceEntity::getDeviceStatus, queryForm.getDeviceStatus());
            }
            
            // 启用状态
            if (queryForm.getEnabled() != null) {
                wrapper.eq(DeviceEntity::getEnabled, queryForm.getEnabled());
            }
            
            // 未删除条件
            wrapper.eq(DeviceEntity::getDeletedFlag, false);
            
            // 按创建时间倒序排列
            wrapper.orderByDesc(DeviceEntity::getCreateTime);
            
            // 分页查询
            Page<DeviceEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
            Page<DeviceEntity> pageResult = accessDeviceDao.selectPage(page, wrapper);
            
            // 转换为VO列表
            List<AccessDeviceVO> voList = pageResult.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            // 构建分页结果
            PageResult<AccessDeviceVO> result = PageResult.from(pageResult);
            result.setRecords(voList);
            
            log.info("[门禁设备] 分页查询设备列表成功: total={}, pageNum={}, pageSize={}",
                    result.getTotal(), queryForm.getPageNum(), queryForm.getPageSize());
            
            return ResponseDTO.ok(result);
            
        } catch (Exception e) {
            log.error("[门禁设备] 分页查询设备列表异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("QUERY_DEVICE_LIST_ERROR", "查询设备列表失败: " + e.getMessage());
        }
    }

    /**
     * 查询门禁设备详情
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<AccessDeviceVO> getDeviceDetail(String deviceId) {
        log.info("[门禁设备] 查询设备详情: deviceId={}", deviceId);
        
        try {
            // 查询设备
            DeviceEntity device = accessDeviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[门禁设备] 设备不存在: deviceId={}", deviceId);
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }
            
            // 确认是否为门禁设备
            if (!"ACCESS".equals(device.getDeviceType())) {
                log.warn("[门禁设备] 设备类型不匹配: deviceId={}, deviceType={}", deviceId, device.getDeviceType());
                return ResponseDTO.error("DEVICE_TYPE_ERROR", "设备类型不匹配，不是门禁设备");
            }
            
            // 转换为VO
            AccessDeviceVO vo = convertToVO(device);
            
            log.info("[门禁设备] 查询设备详情成功: deviceId={}", deviceId);
            return ResponseDTO.ok(vo);
            
        } catch (Exception e) {
            log.error("[门禁设备] 查询设备详情异常: deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseDTO.error("GET_DEVICE_DETAIL_ERROR", "查询设备详情失败: " + e.getMessage());
        }
    }

    /**
     * 添加门禁设备
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> addDevice(AccessDeviceAddForm addForm) {
        log.info("[门禁设备] 添加设备: deviceName={}, deviceCode={}, areaId={}",
                addForm.getDeviceName(), addForm.getDeviceCode(), addForm.getAreaId());
        
        try {
            // 检查设备编码是否已存在
            DeviceEntity existingDevice = accessDeviceDao.selectByDeviceCode(addForm.getDeviceCode());
            if (existingDevice != null && !existingDevice.getDeletedFlag()) {
                log.warn("[门禁设备] 设备编码已存在: deviceCode={}", addForm.getDeviceCode());
                return ResponseDTO.error("DEVICE_CODE_EXISTS", "设备编码已存在");
            }
            
            // 创建设备实体
            DeviceEntity device = new DeviceEntity();
            BeanUtils.copyProperties(addForm, device);
            
            // 设置固定属性
            device.setDeviceType("ACCESS");
            device.setBusinessModule("access");
            device.setDeviceStatus(2); // 默认离线
            device.setDeletedFlag(false);
            device.setCreateTime(LocalDateTime.now());
            device.setUpdateTime(LocalDateTime.now());
            
            // 如果enabled为null，默认为1（启用）
            if (device.getEnabled() == null) {
                device.setEnabled(1);
            }
            
            // 插入数据库
            int result = accessDeviceDao.insert(device);
            if (result <= 0) {
                log.error("[门禁设备] 插入设备失败: deviceCode={}", addForm.getDeviceCode());
                return ResponseDTO.error("ADD_DEVICE_ERROR", "添加设备失败");
            }
            
            log.info("[门禁设备] 添加设备成功: deviceId={}, deviceCode={}", device.getDeviceId(), device.getDeviceCode());
            return ResponseDTO.ok(device.getDeviceId());
            
        } catch (Exception e) {
            log.error("[门禁设备] 添加设备异常: deviceCode={}, error={}", addForm.getDeviceCode(), e.getMessage(), e);
            return ResponseDTO.error("ADD_DEVICE_ERROR", "添加设备失败: " + e.getMessage());
        }
    }

    /**
     * 更新门禁设备
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> updateDevice(AccessDeviceUpdateForm updateForm) {
        log.info("[门禁设备] 更新设备: deviceId={}, deviceName={}", updateForm.getDeviceId(), updateForm.getDeviceName());
        
        try {
            // 查询设备
            DeviceEntity device = accessDeviceDao.selectById(updateForm.getDeviceId());
            if (device == null) {
                log.warn("[门禁设备] 设备不存在: deviceId={}", updateForm.getDeviceId());
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }
            
            // 确认是否为门禁设备
            if (!"ACCESS".equals(device.getDeviceType())) {
                log.warn("[门禁设备] 设备类型不匹配: deviceId={}, deviceType={}", updateForm.getDeviceId(), device.getDeviceType());
                return ResponseDTO.error("DEVICE_TYPE_ERROR", "设备类型不匹配，不是门禁设备");
            }
            
            // 检查设备编码是否已被其他设备使用
            if (StringUtils.hasText(updateForm.getDeviceCode()) 
                    && !updateForm.getDeviceCode().equals(device.getDeviceCode())) {
                DeviceEntity existingDevice = accessDeviceDao.selectByDeviceCode(updateForm.getDeviceCode());
                if (existingDevice != null && !existingDevice.getDeviceId().equals(updateForm.getDeviceId())
                        && !existingDevice.getDeletedFlag()) {
                    log.warn("[门禁设备] 设备编码已被其他设备使用: deviceCode={}", updateForm.getDeviceCode());
                    return ResponseDTO.error("DEVICE_CODE_EXISTS", "设备编码已被其他设备使用");
                }
            }
            
            // 更新设备信息
            BeanUtils.copyProperties(updateForm, device, "deviceId", "deviceType", "deletedFlag", "createTime");
            device.setUpdateTime(LocalDateTime.now());
            
            // 更新数据库
            int result = accessDeviceDao.updateById(device);
            if (result <= 0) {
                log.error("[门禁设备] 更新设备失败: deviceId={}", updateForm.getDeviceId());
                return ResponseDTO.error("UPDATE_DEVICE_ERROR", "更新设备失败");
            }
            
            log.info("[门禁设备] 更新设备成功: deviceId={}", updateForm.getDeviceId());
            return ResponseDTO.ok();
            
        } catch (Exception e) {
            log.error("[门禁设备] 更新设备异常: deviceId={}, error={}", updateForm.getDeviceId(), e.getMessage(), e);
            return ResponseDTO.error("UPDATE_DEVICE_ERROR", "更新设备失败: " + e.getMessage());
        }
    }

    /**
     * 删除门禁设备
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> deleteDevice(String deviceId) {
        log.info("[门禁设备] 删除设备: deviceId={}", deviceId);
        
        try {
            // 查询设备
            DeviceEntity device = accessDeviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[门禁设备] 设备不存在: deviceId={}", deviceId);
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }
            
            // 确认是否为门禁设备
            if (!"ACCESS".equals(device.getDeviceType())) {
                log.warn("[门禁设备] 设备类型不匹配: deviceId={}, deviceType={}", deviceId, device.getDeviceType());
                return ResponseDTO.error("DEVICE_TYPE_ERROR", "设备类型不匹配，不是门禁设备");
            }
            
            // 逻辑删除
            device.setDeletedFlag(true);
            device.setUpdateTime(LocalDateTime.now());
            
            int result = accessDeviceDao.updateById(device);
            if (result <= 0) {
                log.error("[门禁设备] 删除设备失败: deviceId={}", deviceId);
                return ResponseDTO.error("DELETE_DEVICE_ERROR", "删除设备失败");
            }
            
            log.info("[门禁设备] 删除设备成功: deviceId={}", deviceId);
            return ResponseDTO.ok();
            
        } catch (Exception e) {
            log.error("[门禁设备] 删除设备异常: deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseDTO.error("DELETE_DEVICE_ERROR", "删除设备失败: " + e.getMessage());
        }
    }

    /**
     * 更新设备状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> updateDeviceStatus(String deviceId, Integer enabled) {
        log.info("[门禁设备] 更新设备状态: deviceId={}, enabled={}", deviceId, enabled);
        
        try {
            // 查询设备
            DeviceEntity device = accessDeviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[门禁设备] 设备不存在: deviceId={}", deviceId);
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }
            
            // 确认是否为门禁设备
            if (!"ACCESS".equals(device.getDeviceType())) {
                log.warn("[门禁设备] 设备类型不匹配: deviceId={}, deviceType={}", deviceId, device.getDeviceType());
                return ResponseDTO.error("DEVICE_TYPE_ERROR", "设备类型不匹配，不是门禁设备");
            }
            
            // 更新状态
            device.setEnabled(enabled);
            device.setUpdateTime(LocalDateTime.now());
            
            int result = accessDeviceDao.updateById(device);
            if (result <= 0) {
                log.error("[门禁设备] 更新设备状态失败: deviceId={}", deviceId);
                return ResponseDTO.error("UPDATE_DEVICE_STATUS_ERROR", "更新设备状态失败");
            }
            
            log.info("[门禁设备] 更新设备状态成功: deviceId={}, enabled={}", deviceId, enabled);
            return ResponseDTO.ok();
            
        } catch (Exception e) {
            log.error("[门禁设备] 更新设备状态异常: deviceId={}, enabled={}, error={}", deviceId, enabled, e.getMessage(), e);
            return ResponseDTO.error("UPDATE_DEVICE_STATUS_ERROR", "更新设备状态失败: " + e.getMessage());
        }
    }

    /**
     * 统计门禁设备数量
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<Long> countDevices() {
        log.info("[门禁设备] 统计设备数量");
        
        try {
            Long count = accessDeviceDao.countTotalDevices();
            log.info("[门禁设备] 统计设备数量成功: count={}", count);
            return ResponseDTO.ok(count);
            
        } catch (Exception e) {
            log.error("[门禁设备] 统计设备数量异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("COUNT_DEVICES_ERROR", "统计设备数量失败: " + e.getMessage());
        }
    }

    /**
     * 统计在线门禁设备数量
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<Long> countOnlineDevices() {
        log.info("[门禁设备] 统计在线设备数量");
        
        try {
            Long count = accessDeviceDao.countOnlineDevices();
            log.info("[门禁设备] 统计在线设备数量成功: count={}", count);
            return ResponseDTO.ok(count);
            
        } catch (Exception e) {
            log.error("[门禁设备] 统计在线设备数量异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("COUNT_ONLINE_DEVICES_ERROR", "统计在线设备数量失败: " + e.getMessage());
        }
    }

    /**
     * 转换设备实体为VO对象
     * <p>
     * 包括区域名称等关联信息的查询
     * </p>
     */
    private AccessDeviceVO convertToVO(DeviceEntity device) {
        AccessDeviceVO vo = new AccessDeviceVO();
        BeanUtils.copyProperties(device, vo);
        
        // 设置固定属性
        vo.setDeviceType("ACCESS");
        
        // 查询区域名称（如果区域ID存在）
        if (device.getAreaId() != null) {
            try {
                ResponseDTO<AreaEntity> areaResponse = gatewayServiceClient.callCommonService(
                        "/api/v1/organization/area/" + device.getAreaId(),
                        HttpMethod.GET,
                        null,
                        AreaEntity.class
                );
                
                if (areaResponse != null && areaResponse.isSuccess() && areaResponse.getData() != null) {
                    vo.setAreaName(areaResponse.getData().getAreaName());
                } else {
                    // 如果查询失败，使用设备实体中的区域名称
                    vo.setAreaName(device.getAreaName());
                }
            } catch (Exception e) {
                log.debug("[门禁设备] 查询区域信息失败: areaId={}, error={}", device.getAreaId(), e.getMessage());
                // 查询失败时使用设备实体中的区域名称
                vo.setAreaName(device.getAreaName());
            }
        }
        
        return vo;
    }
}
