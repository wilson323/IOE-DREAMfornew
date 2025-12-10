package net.lab1024.sa.access.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.controller.AccessMobileController;
import net.lab1024.sa.access.dao.AccessEventDao;
import net.lab1024.sa.access.domain.form.AccessRecordAddForm;
import net.lab1024.sa.access.domain.form.AccessRecordQueryForm;
import net.lab1024.sa.access.domain.vo.AccessRecordStatisticsVO;
import net.lab1024.sa.access.domain.vo.AccessRecordVO;
import net.lab1024.sa.access.service.AccessEventService;
import net.lab1024.sa.common.audit.entity.AuditLogEntity;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.entity.DeviceEntity;

/**
 * 门禁事件服务实现类
 * <p>
 * 实现门禁事件管理相关业务功能
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
public class AccessEventServiceImpl implements AccessEventService {

    @Resource
    private AccessEventDao accessEventDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<PageResult<AccessRecordVO>> queryAccessRecords(AccessRecordQueryForm queryForm) {
        log.info("[门禁记录] 分页查询门禁记录，pageNum={}, pageSize={}, userId={}, deviceId={}, areaId={}, startDate={}, endDate={}, accessResult={}",
                queryForm.getPageNum(), queryForm.getPageSize(), queryForm.getUserId(),
                queryForm.getDeviceId(), queryForm.getAreaId(), queryForm.getStartDate(),
                queryForm.getEndDate(), queryForm.getAccessResult());

        try {
            // 构建时间范围
            LocalDateTime startTime = null;
            LocalDateTime endTime = null;
            if (queryForm.getStartDate() != null) {
                startTime = queryForm.getStartDate().atStartOfDay();
            }
            if (queryForm.getEndDate() != null) {
                endTime = queryForm.getEndDate().atTime(LocalTime.MAX);
            }

            // 构建业务ID（设备ID或用户ID）
            String businessId = null;
            if (queryForm.getDeviceId() != null) {
                businessId = String.valueOf(queryForm.getDeviceId());
            } else if (queryForm.getUserId() != null) {
                businessId = String.valueOf(queryForm.getUserId());
            }

            // 执行分页查询
            Page<AuditLogEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
            Page<AuditLogEntity> pageResult = accessEventDao.selectAccessEventPage(
                    page,
                    businessId,
                    queryForm.getAccessResult(),
                    startTime,
                    endTime
            );

            // 转换为VO列表
            List<AccessRecordVO> voList = pageResult.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            // 构建分页结果
            PageResult<AccessRecordVO> result = PageResult.of(
                    voList,
                    pageResult.getTotal(),
                    queryForm.getPageNum(),
                    queryForm.getPageSize()
            );

            log.info("[门禁记录] 分页查询门禁记录成功，总数={}", pageResult.getTotal());
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[门禁记录] 分页查询门禁记录失败", e);
            return ResponseDTO.error("QUERY_ACCESS_RECORDS_ERROR", "查询门禁记录失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<AccessRecordStatisticsVO> getAccessRecordStatistics(
            LocalDate startDate, LocalDate endDate, String areaId) {
        log.info("[门禁记录] 获取门禁记录统计，startDate={}, endDate={}, areaId={}", startDate, endDate, areaId);

        try {
            // 构建时间范围
            LocalDateTime startTime = startDate.atStartOfDay();
            LocalDateTime endTime = endDate.atTime(LocalTime.MAX);

            // 查询统计数据
            List<Map<String, Object>> statistics = accessEventDao.selectAccessEventStatistics(startTime, endTime);

            // 构建统计结果
            AccessRecordStatisticsVO statisticsVO = new AccessRecordStatisticsVO();

            // 计算总记录数、成功数、失败数、异常数
            long totalCount = 0;
            long successCount = 0;
            long failedCount = 0;
            long abnormalCount = 0;

            for (Map<String, Object> stat : statistics) {
                Long count = getLongValue(stat, "count");
                Long success = getLongValue(stat, "success_count");
                Integer result = getIntegerValue(stat, "result");

                if (count != null) {
                    totalCount += count;
                }
                if (success != null) {
                    successCount += success;
                }
                if (result != null) {
                    if (result == 2) {
                        failedCount += (count != null ? count : 0) - (success != null ? success : 0);
                    } else if (result == 3) {
                        abnormalCount += (count != null ? count : 0) - (success != null ? success : 0);
                    }
                }
            }

            statisticsVO.setTotalCount(totalCount);
            statisticsVO.setSuccessCount(successCount);
            statisticsVO.setFailedCount(failedCount);
            statisticsVO.setAbnormalCount(abnormalCount);

            // 按操作类型统计
            List<Map<String, Object>> operationStats = accessEventDao.selectVerifyMethodStatistics(startTime, endTime);
            statisticsVO.setStatisticsByOperation(operationStats);

            // 按区域统计（如果有区域ID）
            if (StringUtils.hasText(areaId)) {
                List<Long> areaIds = new ArrayList<>();
                try {
                    areaIds.add(Long.parseLong(areaId));
                    List<Map<String, Object>> areaStats = accessEventDao.selectAreaAccessStatistics(areaIds, startTime, endTime);
                    statisticsVO.setStatisticsByArea(areaStats);
                } catch (NumberFormatException e) {
                    log.warn("[门禁记录] 区域ID格式错误，areaId={}", areaId);
                }
            } else {
                statisticsVO.setStatisticsByArea(new ArrayList<>());
            }

            // 按设备统计
            List<Map<String, Object>> deviceStats = accessEventDao.selectDeviceAccessStatistics(startTime, endTime);
            statisticsVO.setStatisticsByDevice(deviceStats);

            // 按日期统计（简化实现，按日统计）
            statisticsVO.setStatisticsByDate(new ArrayList<>());

            log.info("[门禁记录] 获取门禁记录统计成功");
            return ResponseDTO.ok(statisticsVO);

        } catch (Exception e) {
            log.error("[门禁记录] 获取门禁记录统计失败", e);
            return ResponseDTO.error("GET_STATISTICS_ERROR", "获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取移动端访问记录
     * <p>
     * 查询指定用户的最近访问记录，用于移动端展示
     * </p>
     *
     * @param userId 用户ID
     * @param size 记录数量（默认20条）
     * @return 访问记录列表
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<AccessMobileController.MobileAccessRecord>> getMobileAccessRecords(
            Long userId, Integer size) {
        log.info("[门禁记录] 移动端访问记录查询，userId={}, size={}", userId, size);

        try {
            // 参数验证和默认值
            if (userId == null) {
                return ResponseDTO.error("USER_ID_REQUIRED", "用户ID不能为空");
            }
            if (size == null || size <= 0) {
                size = 20; // 默认20条
            }
            if (size > 100) {
                size = 100; // 最大100条，防止查询过多数据
            }

            // 查询用户最近访问记录（通过审计日志表查询）
            // 注意：这里需要查询门禁相关的审计日志
            List<AuditLogEntity> accessEvents = accessEventDao.selectUserRecentAccessEvents(userId, size);

            // 转换为移动端记录格式
            List<AccessMobileController.MobileAccessRecord> records =
                    accessEvents.stream()
                            .map(auditLog -> convertToMobileAccessRecord(auditLog))
                            .collect(Collectors.toList());

            log.info("[门禁记录] 移动端访问记录查询成功，userId={}, 返回记录数={}", userId, records.size());
            return ResponseDTO.ok(records);

        } catch (Exception e) {
            log.error("[门禁记录] 移动端访问记录查询失败，userId={}", userId, e);
            return ResponseDTO.error("QUERY_MOBILE_RECORDS_ERROR", "查询移动端访问记录失败: " + e.getMessage());
        }
    }

    /**
     * 创建门禁记录
     * <p>
     * 用于设备协议推送门禁记录
     * 通过审计日志记录门禁事件
     * </p>
     *
     * @param form 门禁记录创建表单
     * @return 创建的门禁记录ID（审计日志ID）
     */
    @Override
    public ResponseDTO<Long> createAccessRecord(AccessRecordAddForm form) {
        log.info("[门禁记录] 创建门禁记录，userId={}, deviceId={}, passType={}",
                form.getUserId(), form.getDeviceId(), form.getPassType());

        try {
            // 构建审计日志实体（门禁事件通过审计日志记录）
            AuditLogEntity auditLog = new AuditLogEntity();
            auditLog.setUserId(form.getUserId());
            auditLog.setModuleName("ACCESS"); // 模块名称：门禁
            auditLog.setOperationType(2); // 2-新增
            auditLog.setResourceType(form.getAreaId() != null ? String.valueOf(form.getAreaId()) : null);
            auditLog.setResourceId(form.getDeviceId() != null ? String.valueOf(form.getDeviceId()) : null);

            // 构建操作描述
            StringBuilder operationDesc = new StringBuilder();
            if (form.getPassType() != null) {
                operationDesc.append(form.getPassType() == 0 ? "进入" : "离开");
            }
            if (form.getPassMethod() != null) {
                String methodName = switch (form.getPassMethod()) {
                    case 0 -> "卡片";
                    case 1 -> "人脸";
                    case 2 -> "指纹";
                    default -> "未知";
                };
                operationDesc.append("（").append(methodName).append("）");
            }
            if (form.getDoorNo() != null) {
                operationDesc.append(" - 门号：").append(form.getDoorNo());
            }
            auditLog.setOperationDesc(operationDesc.toString());

            // 处理通行时间（支持时间戳和LocalDateTime）
            LocalDateTime passTime = null;
            if (form.getPassTime() != null) {
                if (form.getPassTime() instanceof Number) {
                    // 时间戳（秒）
                    long timestamp = ((Number) form.getPassTime()).longValue();
                    passTime = LocalDateTime.ofEpochSecond(timestamp, 0,
                            java.time.ZoneOffset.of("+8"));
                } else if (form.getPassTime() instanceof LocalDateTime) {
                    passTime = (LocalDateTime) form.getPassTime();
                } else if (form.getPassTime() instanceof String) {
                    // 字符串格式的时间
                    try {
                        passTime = LocalDateTime.parse((String) form.getPassTime());
                    } catch (Exception e) {
                        log.warn("[门禁记录] 时间格式解析失败，使用当前时间，passTime={}", form.getPassTime());
                        passTime = LocalDateTime.now();
                    }
                } else {
                    passTime = LocalDateTime.now();
                }
            } else {
                passTime = LocalDateTime.now();
            }
            auditLog.setCreateTime(passTime);

            // 设置结果状态（1-成功，2-失败）
            if (form.getAccessResult() != null) {
                auditLog.setResultStatus(form.getAccessResult() == 1 ? 1 : 2);
            } else {
                auditLog.setResultStatus(1); // 默认成功
            }

            // 构建请求参数（JSON格式）
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("deviceId", form.getDeviceId());
            requestParams.put("deviceCode", form.getDeviceCode());
            requestParams.put("userId", form.getUserId());
            requestParams.put("passTime", passTime.toString());
            requestParams.put("passType", form.getPassType());
            requestParams.put("doorNo", form.getDoorNo());
            requestParams.put("passMethod", form.getPassMethod());
            requestParams.put("accessResult", form.getAccessResult());
            requestParams.put("areaId", form.getAreaId());
            requestParams.put("remark", form.getRemark());

            try {
                auditLog.setRequestParams(objectMapper.writeValueAsString(requestParams));
            } catch (Exception e) {
                log.warn("[门禁记录] 请求参数序列化失败", e);
            }

            // 通过网关调用公共服务保存审计日志
            ResponseDTO<Long> response = gatewayServiceClient.callCommonService(
                    "/api/v1/audit/log/create",
                    org.springframework.http.HttpMethod.POST,
                    auditLog,
                    Long.class
            );

            if (response != null && response.isSuccess() && response.getData() != null) {
                log.info("[门禁记录] 门禁记录创建成功，logId={}, userId={}",
                        response.getData(), form.getUserId());
                return ResponseDTO.ok(response.getData());
            } else {
                log.warn("[门禁记录] 门禁记录创建失败，错误={}",
                        response != null ? response.getMessage() : "响应为空");
                return ResponseDTO.error("CREATE_ACCESS_RECORD_ERROR",
                        "创建门禁记录失败: " + (response != null ? response.getMessage() : "响应为空"));
            }

        } catch (Exception e) {
            log.error("[门禁记录] 创建门禁记录异常，userId={}", form.getUserId(), e);
            return ResponseDTO.error("CREATE_ACCESS_RECORD_ERROR",
                    "创建门禁记录失败: " + e.getMessage());
        }
    }

    /**
     * 转换为移动端访问记录格式
     *
     * @param auditLog 审计日志实体
     * @return 移动端访问记录
     */
    private AccessMobileController.MobileAccessRecord convertToMobileAccessRecord(
            AuditLogEntity auditLog) {
        AccessMobileController.MobileAccessRecord record =
                new AccessMobileController.MobileAccessRecord();

        record.setRecordId(auditLog.getId());
        record.setUserId(auditLog.getUserId());

        // 解析设备ID
        if (auditLog.getResourceId() != null && !auditLog.getResourceId().isEmpty()) {
            try {
                record.setDeviceId(Long.parseLong(auditLog.getResourceId()));
            } catch (NumberFormatException e) {
                log.debug("[门禁记录] 设备ID格式错误，resourceId={}", auditLog.getResourceId());
            }
        }

        // 获取设备名称（通过网关调用公共服务）
        if (record.getDeviceId() != null) {
            try {
                ResponseDTO<DeviceEntity> deviceResult = gatewayServiceClient.callCommonService(
                        "/api/v1/devices/" + record.getDeviceId(),
                        org.springframework.http.HttpMethod.GET,
                        null,
                        DeviceEntity.class
                );
                if (deviceResult != null && deviceResult.isSuccess() && deviceResult.getData() != null) {
                    record.setDeviceName(deviceResult.getData().getDeviceName());
                }
            } catch (Exception e) {
                log.debug("[门禁记录] 获取设备名称失败，deviceId={}", record.getDeviceId(), e);
            }
        }

        // 访问时间
        if (auditLog.getCreateTime() != null) {
            record.setAccessTime(auditLog.getCreateTime().toString());
        }

        // 访问类型（从操作描述中提取）
        record.setAccessType(auditLog.getOperationDesc() != null ? auditLog.getOperationDesc() : "未知");

        // 访问结果（resultStatus: 1-成功, 2-失败, 3-异常）
        if (auditLog.getResultStatus() != null) {
            record.setAccessResult(auditLog.getResultStatus() == 1);
        } else {
            record.setAccessResult(false);
        }

        return record;
    }

    // ==================== 私有方法 ====================

    /**
     * 转换审计日志实体为VO
     *
     * @param auditLog 审计日志实体
     * @return 门禁记录VO
     */
    private AccessRecordVO convertToVO(AuditLogEntity auditLog) {
        AccessRecordVO vo = new AccessRecordVO();
        vo.setLogId(auditLog.getId());
        vo.setUserId(auditLog.getUserId());
        vo.setUserName(auditLog.getUserName());
        vo.setDeviceId(auditLog.getResourceId());
        vo.setAreaId(auditLog.getResourceType());
        vo.setOperation(auditLog.getOperationDesc());
        vo.setResult(auditLog.getResultStatus());
        vo.setCreateTime(auditLog.getCreateTime());
        vo.setIpAddress(auditLog.getClientIp());
        vo.setRemark(auditLog.getErrorMessage());

        // 通过网关获取设备名称（如果resourceId不为空）
        if (StringUtils.hasText(auditLog.getResourceId())) {
            try {
                Long deviceId = Long.parseLong(auditLog.getResourceId());
                // 通过网关获取设备信息
                ResponseDTO<DeviceEntity> deviceResult = gatewayServiceClient.callCommonService(
                        "/api/v1/devices/" + deviceId,
                        org.springframework.http.HttpMethod.GET,
                        null,
                        DeviceEntity.class
                );
                if (deviceResult != null && deviceResult.isSuccess() && deviceResult.getData() != null) {
                    vo.setDeviceName(deviceResult.getData().getDeviceName());
                }
            } catch (NumberFormatException e) {
                log.debug("[门禁记录] 设备ID格式错误，resourceId={}", auditLog.getResourceId());
            } catch (Exception e) {
                log.warn("[门禁记录] 获取设备信息失败，resourceId={}", auditLog.getResourceId(), e);
            }
        }

        // 通过网关获取区域名称（如果resourceType不为空）
        if (StringUtils.hasText(auditLog.getResourceType())) {
            try {
                Long areaId = Long.parseLong(auditLog.getResourceType());
                // 通过网关获取区域信息
                ResponseDTO<AreaEntity> areaResult = gatewayServiceClient.callCommonService(
                        "/api/v1/areas/" + areaId,
                        org.springframework.http.HttpMethod.GET,
                        null,
                        AreaEntity.class
                );
                if (areaResult != null && areaResult.isSuccess() && areaResult.getData() != null) {
                    vo.setAreaName(areaResult.getData().getAreaName());
                }
            } catch (NumberFormatException e) {
                log.debug("[门禁记录] 区域ID格式错误，resourceType={}", auditLog.getResourceType());
            } catch (Exception e) {
                log.warn("[门禁记录] 获取区域信息失败，resourceType={}", auditLog.getResourceType(), e);
            }
        }

        return vo;
    }

    /**
     * 从Map中获取Long值
     *
     * @param map Map对象
     * @param key 键
     * @return Long值
     */
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    /**
     * 从Map中获取Integer值
     *
     * @param map Map对象
     * @param key 键
     * @return Integer值
     */
    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }
}

