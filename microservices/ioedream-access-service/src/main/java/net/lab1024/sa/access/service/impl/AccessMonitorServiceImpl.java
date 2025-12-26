package net.lab1024.sa.access.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.AccessAlarmDao;
import net.lab1024.sa.access.dao.AccessDeviceDao;
import net.lab1024.sa.access.domain.form.AccessMonitorQueryForm;
import net.lab1024.sa.access.domain.vo.AccessAlarmVO;
import net.lab1024.sa.access.domain.vo.AccessDeviceStatusVO;
import net.lab1024.sa.access.domain.vo.AccessEventVO;
import net.lab1024.sa.access.domain.vo.AccessMonitorStatisticsVO;
import net.lab1024.sa.access.domain.vo.AccessPersonTrackVO;
import net.lab1024.sa.access.service.AccessMonitorService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.dao.AccessRecordDao;
import net.lab1024.sa.common.organization.entity.AccessRecordEntity;
import net.lab1024.sa.access.domain.entity.AccessAlarmEntity;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.common.organization.entity.UserEntity;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 门禁实时监控服务实现
 * <p>
 * 严格遵循CLAUDE.md规范和ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md架构方案：
 * - 遵循四层架构：Controller → Service → Manager → DAO
 * - 实时状态监控、报警处理、视频联动、人员追踪
 * - 使用@Resource依赖注入
 * - 使用@Transactional事务管理
 * </p>
 * <p>
 * 核心职责：
 * - 实时设备状态监控
 * - 报警接收和处理
 * - 视频联动触发
 * - 人员轨迹追踪
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
public class AccessMonitorServiceImpl implements AccessMonitorService {

    @Resource
    private AccessDeviceDao accessDeviceDao;

    @Resource
    private AccessRecordDao accessRecordDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private AccessAlarmDao accessAlarmDao;

    /**
     * 查询实时设备状态列表
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<PageResult<AccessDeviceStatusVO>> queryDeviceStatusList(AccessMonitorQueryForm queryForm) {
        log.info("[实时监控] 查询设备状态列表: pageNum={}, pageSize={}, deviceId={}, areaId={}",
                queryForm.getPageNum(), queryForm.getPageSize(), queryForm.getDeviceId(), queryForm.getAreaId());

        try {
            // 构建查询条件
            LambdaQueryWrapper<DeviceEntity> wrapper = new LambdaQueryWrapper<>();

            // 设备类型：固定为ACCESS
            wrapper.eq(DeviceEntity::getDeviceType, "ACCESS");

            // 设备ID
            if (StringUtils.hasText(queryForm.getDeviceId())) {
                wrapper.eq(DeviceEntity::getDeviceId, queryForm.getDeviceId());
            }

            // 区域ID
            if (queryForm.getAreaId() != null) {
                wrapper.eq(DeviceEntity::getAreaId, queryForm.getAreaId());
            }

            // 未删除条件
            wrapper.eq(DeviceEntity::getDeletedFlag, false);

            // 按最后在线时间倒序排列
            wrapper.orderByDesc(DeviceEntity::getLastOnlineTime);

            // 分页查询
            Page<DeviceEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
            Page<DeviceEntity> pageResult = accessDeviceDao.selectPage(page, wrapper);

            // 转换为VO列表
            List<AccessDeviceStatusVO> voList = pageResult.getRecords().stream()
                    .map(this::convertToDeviceStatusVO)
                    .collect(Collectors.toList());

            // 构建分页结果
            PageResult<AccessDeviceStatusVO> result = new PageResult<>();
            result.setRecords(voList);
            result.setTotal(pageResult.getTotal());
            result.setPageNum((int) pageResult.getCurrent());
            result.setPageSize((int) pageResult.getSize());
            result.setPages((int) pageResult.getPages());

            log.info("[实时监控] 查询设备状态列表成功: total={}, pageNum={}, pageSize={}",
                    result.getTotal(), queryForm.getPageNum(), queryForm.getPageSize());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[实时监控] 查询设备状态列表异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("QUERY_DEVICE_STATUS_ERROR", "查询设备状态列表失败: " + e.getMessage());
        }
    }

    /**
     * 查询单个设备实时状态
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<AccessDeviceStatusVO> getDeviceStatus(String deviceId) {
        log.info("[实时监控] 查询设备实时状态: deviceId={}", deviceId);

        try {
            // 查询设备
            DeviceEntity device = accessDeviceDao.selectById(deviceId);
            if (device == null) {
                log.warn("[实时监控] 设备不存在: deviceId={}", deviceId);
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }

            // 确认是否为门禁设备
            if (!"ACCESS".equals(device.getDeviceType())) {
                log.warn("[实时监控] 设备类型不匹配: deviceId={}, deviceType={}", deviceId, device.getDeviceType());
                return ResponseDTO.error("DEVICE_TYPE_ERROR", "设备类型不匹配，不是门禁设备");
            }

            // 转换为VO
            AccessDeviceStatusVO vo = convertToDeviceStatusVO(device);

            log.info("[实时监控] 查询设备实时状态成功: deviceId={}", deviceId);
            return ResponseDTO.ok(vo);

        } catch (Exception e) {
            log.error("[实时监控] 查询设备实时状态异常: deviceId={}, error={}", deviceId, e.getMessage(), e);
            return ResponseDTO.error("GET_DEVICE_STATUS_ERROR", "查询设备实时状态失败: " + e.getMessage());
        }
    }

    /**
     * 查询报警列表
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<PageResult<AccessAlarmVO>> queryAlarmList(AccessMonitorQueryForm queryForm) {
        log.info("[实时监控] 查询报警列表: pageNum={}, pageSize={}, alarmLevel={}, startTime={}, endTime={}",
                queryForm.getPageNum(), queryForm.getPageSize(), queryForm.getAlarmLevel(),
                queryForm.getStartTime(), queryForm.getEndTime());

        try {
            // 1. 构建查询条件
            LambdaQueryWrapper<AccessAlarmEntity> wrapper = new LambdaQueryWrapper<>();

            // 报警级别过滤
            if (queryForm.getAlarmLevel() != null) {
                wrapper.eq(AccessAlarmEntity::getAlarmLevel, queryForm.getAlarmLevel());
            }

            // 时间范围过滤
            if (queryForm.getStartTime() != null) {
                wrapper.ge(AccessAlarmEntity::getAlarmTime, queryForm.getStartTime());
            }
            if (queryForm.getEndTime() != null) {
                wrapper.le(AccessAlarmEntity::getAlarmTime, queryForm.getEndTime());
            }

            // 处理状态过滤（通过processedTime是否为空判断）
            // TODO: 在AccessMonitorQueryForm中添加processed字段后启用此过滤
            // if (queryForm.getProcessed() != null) {
            //     if (queryForm.getProcessed()) {
            //         wrapper.isNotNull(AccessAlarmEntity::getProcessedTime);
            //     } else {
            //         wrapper.isNull(AccessAlarmEntity::getProcessedTime);
            //     }
            // }

            // 设备ID过滤
            if (StringUtils.hasText(queryForm.getDeviceId())) {
                wrapper.eq(AccessAlarmEntity::getDeviceId, Long.parseLong(queryForm.getDeviceId()));
            }

            // 区域ID过滤
            if (queryForm.getAreaId() != null) {
                wrapper.eq(AccessAlarmEntity::getAreaId, queryForm.getAreaId());
            }

            // 未删除条件
            wrapper.eq(AccessAlarmEntity::getDeletedFlag, false);

            // 按报警时间倒序排列（最新的在前）
            wrapper.orderByDesc(AccessAlarmEntity::getAlarmTime);

            // 2. 分页查询
            Page<AccessAlarmEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
            Page<AccessAlarmEntity> pageResult = accessAlarmDao.selectPage(page, wrapper);

            // 3. 转换为VO列表
            List<AccessAlarmVO> voList = pageResult.getRecords().stream()
                    .map(this::convertToAlarmVO)
                    .collect(Collectors.toList());

            // 4. 构建分页结果
            PageResult<AccessAlarmVO> result = new PageResult<>();
            result.setRecords(voList);
            result.setTotal(pageResult.getTotal());
            result.setPageNum((int) pageResult.getCurrent());
            result.setPageSize((int) pageResult.getSize());
            result.setPages((int) pageResult.getPages());

            log.info("[实时监控] 查询报警列表成功: total={}, pageNum={}, pageSize={}",
                    result.getTotal(), queryForm.getPageNum(), queryForm.getPageSize());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[实时监控] 查询报警列表异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("QUERY_ALARM_LIST_ERROR", "查询报警列表失败: " + e.getMessage());
        }
    }

    /**
     * 处理报警
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> handleAlarm(Long alarmId, String handleRemark) {
        log.info("[实时监控] 处理报警: alarmId={}, handleRemark={}", alarmId, handleRemark);

        try {
            // 1. 查询报警记录
            AccessAlarmEntity alarm = accessAlarmDao.selectById(alarmId);
            if (alarm == null) {
                log.warn("[实时监控] 报警记录不存在: alarmId={}", alarmId);
                return ResponseDTO.error("ALARM_NOT_FOUND", "报警记录不存在");
            }

            // 2. 检查是否已处理
            if (Boolean.TRUE.equals(alarm.getProcessed())) {
                log.warn("[实时监控] 报警已处理，无需重复处理: alarmId={}", alarmId);
                return ResponseDTO.error("ALARM_ALREADY_PROCESSED", "报警已处理");
            }

            // 3. 更新处理状态
            alarm.setProcessed(true);
            alarm.setProcessStatus(2); // 2-已处理
            alarm.setHandleTime(LocalDateTime.now());
            alarm.setHandleRemark(handleRemark);
            alarm.setAlarmStatus("RESOLVED");
            alarm.setHandleResult("已处理");

            // 4. 更新数据库
            int updateResult = accessAlarmDao.updateById(alarm);
            if (updateResult <= 0) {
                log.error("[实时监控] 更新报警处理状态失败: alarmId={}", alarmId);
                return ResponseDTO.error("UPDATE_ALARM_FAILED", "更新报警处理状态失败");
            }

            // 5. 发送处理通知（可选）
            sendAlarmHandledNotification(alarm);

            log.info("[实时监控] 处理报警成功: alarmId={}, alarmType={}", alarmId, alarm.getAlarmType());
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[实时监控] 处理报警异常: alarmId={}, error={}", alarmId, e.getMessage(), e);
            return ResponseDTO.error("HANDLE_ALARM_ERROR", "处理报警失败: " + e.getMessage());
        }
    }

    /**
     * 触发视频联动
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> triggerVideoLinkage(String deviceId, String eventType) {
        log.info("[实时监控] 触发视频联动: deviceId={}, eventType={}", deviceId, eventType);

        try {
            // 构建视频联动请求
            Map<String, Object> videoLinkageRequest = new HashMap<>();
            videoLinkageRequest.put("deviceId", deviceId);
            videoLinkageRequest.put("eventType", eventType);
            videoLinkageRequest.put("duration", Duration.ofMinutes(5).toMinutes());
            videoLinkageRequest.put("timestamp", LocalDateTime.now());

            // 发送到RabbitMQ
            rabbitTemplate.convertAndSend(
                    "ioedream.video.exchange",
                    "video.linkage.route",
                    videoLinkageRequest
            );

            log.info("[实时监控] 触发视频联动成功: deviceId={}, eventType={}", deviceId, eventType);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[实时监控] 触发视频联动异常: deviceId={}, eventType={}, error={}",
                    deviceId, eventType, e.getMessage(), e);
            return ResponseDTO.error("TRIGGER_VIDEO_LINKAGE_ERROR", "触发视频联动失败: " + e.getMessage());
        }
    }

    /**
     * 查询人员轨迹
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<AccessPersonTrackVO> queryPersonTrack(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("[实时监控] 查询人员轨迹: userId={}, startTime={}, endTime={}", userId, startTime, endTime);

        try {
            // 查询通行记录
            List<AccessRecordEntity> records = accessRecordDao.selectByUserIdAndTimeRange(userId, startTime, endTime);

            // 构建轨迹点列表
            List<AccessPersonTrackVO.TrackingPoint> trackingPoints = records.stream()
                    .map(this::convertToTrackingPoint)
                    .collect(Collectors.toList());

            // 构建人员轨迹VO
            AccessPersonTrackVO trackVO = new AccessPersonTrackVO();
            trackVO.setUserId(userId);

            // 查询用户信息（通过GatewayServiceClient）
            try {
                ResponseDTO<UserEntity> userResponse =
                        gatewayServiceClient.callCommonService(
                                "/api/v1/organization/user/" + userId,
                                HttpMethod.GET,
                                null,
                                new TypeReference<ResponseDTO<UserEntity>>() {}
                        );

                if (userResponse != null && userResponse.isSuccess() && userResponse.getData() != null) {
                    UserEntity user = userResponse.getData();
                    trackVO.setUserName(user.getRealName() != null ? user.getRealName() : user.getUsername());
                    trackVO.setDepartmentId(userResponse.getData().getDepartmentId());
                }
            } catch (Exception e) {
                log.debug("[实时监控] 查询用户信息失败: userId={}, error={}", userId, e.getMessage());
            }

            // 设置当前区域和设备（根据最后一条记录）
            if (!trackingPoints.isEmpty()) {
                AccessPersonTrackVO.TrackingPoint lastPoint = trackingPoints.get(0);
                trackVO.setCurrentAreaId(lastPoint.getAreaId());
                trackVO.setCurrentAreaName(lastPoint.getAreaName());
                trackVO.setCurrentDeviceId(lastPoint.getDeviceId());
                trackVO.setCurrentDeviceName(lastPoint.getDeviceName());
                trackVO.setLastAccessTime(lastPoint.getAccessTime());
            }

            trackVO.setTrackingPoints(trackingPoints);

            // 异常检测（简单实现：检查是否有失败记录）
            boolean isAbnormal = records.stream()
                    .anyMatch(r -> r.getAccessResult() != null && r.getAccessResult() == 2);
            trackVO.setIsAbnormal(isAbnormal);

            if (isAbnormal) {
                trackVO.setAbnormalDescription("检测到通行失败记录");
            }

            log.info("[实时监控] 查询人员轨迹成功: userId={}, pointCount={}", userId, trackingPoints.size());
            return ResponseDTO.ok(trackVO);

        } catch (Exception e) {
            log.error("[实时监控] 查询人员轨迹异常: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseDTO.error("QUERY_PERSON_TRACK_ERROR", "查询人员轨迹失败: " + e.getMessage());
        }
    }

    /**
     * 查询实时通行事件
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<AccessEventVO>> queryRealtimeEvents(Integer limit) {
        log.info("[实时监控] 查询实时通行事件: limit={}", limit);

        try {
            // 查询最近的通行记录
            LambdaQueryWrapper<AccessRecordEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AccessRecordEntity::getDeletedFlag, false)
                    .orderByDesc(AccessRecordEntity::getAccessTime)
                    .last("LIMIT " + (limit != null && limit > 0 ? limit : 20));

            List<AccessRecordEntity> records = accessRecordDao.selectList(wrapper);

            // 转换为VO列表
            List<AccessEventVO> eventList = records.stream()
                    .map(this::convertToEventVO)
                    .collect(Collectors.toList());

            log.info("[实时监控] 查询实时通行事件成功: count={}", eventList.size());
            return ResponseDTO.ok(eventList);

        } catch (Exception e) {
            log.error("[实时监控] 查询实时通行事件异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("QUERY_REALTIME_EVENTS_ERROR", "查询实时通行事件失败: " + e.getMessage());
        }
    }

    /**
     * 统计监控数据
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<AccessMonitorStatisticsVO> getMonitorStatistics() {
        log.info("[实时监控] 统计监控数据");

        try {
            AccessMonitorStatisticsVO statistics = new AccessMonitorStatisticsVO();

            // 统计设备数量
            Long totalDevices = accessDeviceDao.countTotalDevices();
            Long onlineDevices = accessDeviceDao.countOnlineDevices();
            statistics.setTotalDevices(totalDevices);
            statistics.setOnlineDevices(onlineDevices);
            statistics.setOfflineDevices(totalDevices - onlineDevices);
            statistics.setFaultDevices(0L); // TODO: 需要统计故障设备

            // 统计今日通行数据
            LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
            LocalDateTime todayEnd = LocalDateTime.now().with(LocalTime.MAX);

            LambdaQueryWrapper<AccessRecordEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(AccessRecordEntity::getAccessTime, todayStart)
                    .le(AccessRecordEntity::getAccessTime, todayEnd)
                    .eq(AccessRecordEntity::getDeletedFlag, false);

            List<AccessRecordEntity> todayRecords = accessRecordDao.selectList(wrapper);
            long todayTotal = todayRecords.size();
            long todaySuccess = todayRecords.stream()
                    .filter(r -> r.getAccessResult() != null && r.getAccessResult() == 1)
                    .count();
            long todayFailed = todayTotal - todaySuccess;

            statistics.setTodayAccessTotal(todayTotal);
            statistics.setTodayAccessSuccess(todaySuccess);
            statistics.setTodayAccessFailed(todayFailed);

            // 报警统计（TODO: 需要实现报警表后完善）
            statistics.setUnhandledAlarms(0L);
            statistics.setCriticalAlarms(0L);

            // 当前在线人数（简单估算：最近1小时内有通行记录的人数）
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            LambdaQueryWrapper<AccessRecordEntity> recentWrapper = new LambdaQueryWrapper<>();
            recentWrapper.ge(AccessRecordEntity::getAccessTime, oneHourAgo)
                    .eq(AccessRecordEntity::getDeletedFlag, false)
                    .eq(AccessRecordEntity::getAccessResult, 1);

            List<AccessRecordEntity> recentRecords = accessRecordDao.selectList(recentWrapper);
            long currentOnlinePersons = recentRecords.stream()
                    .map(AccessRecordEntity::getUserId)
                    .distinct()
                    .count();

            statistics.setCurrentOnlinePersons(currentOnlinePersons);

            log.info("[实时监控] 统计监控数据成功: totalDevices={}, onlineDevices={}, todayAccessTotal={}",
                    totalDevices, onlineDevices, todayTotal);

            return ResponseDTO.ok(statistics);

        } catch (Exception e) {
            log.error("[实时监控] 统计监控数据异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("GET_MONITOR_STATISTICS_ERROR", "统计监控数据失败: " + e.getMessage());
        }
    }

    /**
     * 转换设备实体为设备状态VO
     */
    private AccessDeviceStatusVO convertToDeviceStatusVO(DeviceEntity device) {
        AccessDeviceStatusVO vo = new AccessDeviceStatusVO();
        BeanUtils.copyProperties(device, vo);

        // 设置设备状态名称
        vo.setDeviceStatusName(getDeviceStatusName(device.getDeviceStatus()));

        // 查询区域名称
        if (device.getAreaId() != null) {
            try {
                ResponseDTO<AreaEntity> areaResponse = gatewayServiceClient.callCommonService(
                        "/api/v1/organization/area/" + device.getAreaId(),
                        HttpMethod.GET,
                        null,
                        new TypeReference<ResponseDTO<AreaEntity>>() {}
                );

                if (areaResponse != null && areaResponse.isSuccess() && areaResponse.getData() != null) {
                    vo.setAreaName(areaResponse.getData().getAreaName());
                } else {
                    vo.setAreaName("未知区域");
                }
            } catch (Exception e) {
                log.debug("[实时监控] 查询区域信息失败: areaId={}, error={}", device.getAreaId(), e.getMessage());
                vo.setAreaName("未知区域");
            }
        }

        // 计算网络质量（简单实现）
        if (device.getLastOnlineTime() != null) {
            long minutesSinceOnline = java.time.Duration.between(device.getLastOnlineTime(), LocalDateTime.now()).toMinutes();
            if (minutesSinceOnline < 5) {
                vo.setNetworkQuality("EXCELLENT");
            } else if (minutesSinceOnline < 30) {
                vo.setNetworkQuality("GOOD");
            } else if (minutesSinceOnline < 60) {
                vo.setNetworkQuality("FAIR");
            } else {
                vo.setNetworkQuality("POOR");
            }
        } else {
            vo.setNetworkQuality("POOR");
        }

        // 响应时间（TODO: 需要实际测量，这里先返回0）
        vo.setResponseTime(0L);

        return vo;
    }

    /**
     * 转换通行记录为轨迹点
     */
    private AccessPersonTrackVO.TrackingPoint convertToTrackingPoint(AccessRecordEntity record) {
        AccessPersonTrackVO.TrackingPoint point = new AccessPersonTrackVO.TrackingPoint();
        // AccessRecordEntity.deviceId是Long类型，需要转换为String
        String deviceIdStr = record.getDeviceId() != null ? record.getDeviceId().toString() : null;
        point.setDeviceId(deviceIdStr);
        point.setAreaId(record.getAreaId());
        point.setAccessTime(record.getAccessTime());
        point.setAccessType(record.getAccessType());
        point.setAccessResult(record.getAccessResult() == 1 ? "SUCCESS" : "FAILED");

        // 查询设备名称
        if (deviceIdStr != null) {
            try {
                DeviceEntity device = accessDeviceDao.selectById(deviceIdStr);
                if (device != null) {
                    point.setDeviceName(device.getDeviceName());
                }
            } catch (Exception e) {
                log.debug("[实时监控] 查询设备信息失败: deviceId={}, error={}", deviceIdStr, e.getMessage());
            }
        }

        // 查询区域名称
        if (record.getAreaId() != null) {
            try {
                ResponseDTO<AreaEntity> areaResponse = gatewayServiceClient.callCommonService(
                        "/api/v1/organization/area/" + record.getAreaId(),
                        HttpMethod.GET,
                        null,
                        new TypeReference<ResponseDTO<AreaEntity>>() {}
                );

                if (areaResponse != null && areaResponse.isSuccess() && areaResponse.getData() != null) {
                    point.setAreaName(areaResponse.getData().getAreaName());
                }
            } catch (Exception e) {
                log.debug("[实时监控] 查询区域信息失败: areaId={}, error={}", record.getAreaId(), e.getMessage());
            }
        }

        return point;
    }

    /**
     * 转换通行记录为事件VO
     */
    private AccessEventVO convertToEventVO(AccessRecordEntity record) {
        AccessEventVO vo = new AccessEventVO();
        BeanUtils.copyProperties(record, vo);

        // 设置设备ID（转换为String）
        if (record.getDeviceId() != null) {
            vo.setDeviceId(record.getDeviceId().toString());
        }

        // 设置通行结果名称
        vo.setAccessResultName(record.getAccessResult() == 1 ? "成功" : "失败");

        // 设置通行类型名称
        if ("IN".equals(record.getAccessType())) {
            vo.setAccessTypeName("进入");
        } else if ("OUT".equals(record.getAccessType())) {
            vo.setAccessTypeName("离开");
        }

        // 设置验证方式名称
        if (StringUtils.hasText(record.getVerifyMethod())) {
            switch (record.getVerifyMethod().toUpperCase()) {
                case "FACE":
                    vo.setVerifyMethodName("人脸识别");
                    break;
                case "FINGERPRINT":
                    vo.setVerifyMethodName("指纹识别");
                    break;
                case "CARD":
                    vo.setVerifyMethodName("卡片");
                    break;
                case "PASSWORD":
                    vo.setVerifyMethodName("密码");
                    break;
                case "QR_CODE":
                    vo.setVerifyMethodName("二维码");
                    break;
                default:
                    vo.setVerifyMethodName(record.getVerifyMethod());
            }
        }

        // 查询设备名称（AccessRecordEntity.deviceId是Long类型，需要转换为String）
        if (record.getDeviceId() != null) {
            try {
                String deviceIdStr = record.getDeviceId().toString();
                DeviceEntity device = accessDeviceDao.selectById(deviceIdStr);
                if (device != null) {
                    vo.setDeviceName(device.getDeviceName());
                }
            } catch (Exception e) {
                log.debug("[实时监控] 查询设备信息失败: deviceId={}, error={}", record.getDeviceId(), e.getMessage());
            }
        }

        // 查询区域名称
        if (record.getAreaId() != null) {
            try {
                ResponseDTO<AreaEntity> areaResponse = gatewayServiceClient.callCommonService(
                        "/api/v1/organization/area/" + record.getAreaId(),
                        HttpMethod.GET,
                        null,
                        new TypeReference<ResponseDTO<AreaEntity>>() {}
                );

                if (areaResponse != null && areaResponse.isSuccess() && areaResponse.getData() != null) {
                    vo.setAreaName(areaResponse.getData().getAreaName());
                }
            } catch (Exception e) {
                log.debug("[实时监控] 查询区域信息失败: areaId={}, error={}", record.getAreaId(), e.getMessage());
            }
        }

        // 查询用户名称
        if (record.getUserId() != null) {
            try {
                ResponseDTO<UserEntity> userResponse =
                        gatewayServiceClient.callCommonService(
                                "/api/v1/organization/user/" + record.getUserId(),
                                HttpMethod.GET,
                                null,
                                new TypeReference<ResponseDTO<UserEntity>>() {}
                        );

                if (userResponse != null && userResponse.isSuccess() && userResponse.getData() != null) {
                    UserEntity user = userResponse.getData();
                    vo.setUserName(user.getRealName() != null ? user.getRealName() : user.getUsername());
                }
            } catch (Exception e) {
                log.debug("[实时监控] 查询用户信息失败: userId={}, error={}", record.getUserId(), e.getMessage());
            }
        }

        return vo;
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

    /**
     * 转换报警实体为VO
     */
    private AccessAlarmVO convertToAlarmVO(AccessAlarmEntity entity) {
        AccessAlarmVO vo = new AccessAlarmVO();
        BeanUtils.copyProperties(entity, vo);

        // 设置设备ID（转换为String）
        if (entity.getDeviceId() != null) {
            vo.setDeviceId(entity.getDeviceId().toString());
        }

        // 设置报警级别名称
        vo.setAlarmLevelName(getAlarmLevelName(entity.getAlarmLevel()));

        // 设置报警类型名称
        vo.setAlarmTypeName(getAlarmTypeName(entity.getAlarmType()));

        // 设置处理状态名称
        vo.setProcessStatusName(getProcessStatusName(entity.getProcessStatus()));

        // 设置报警状态名称
        vo.setAlarmStatusName(getAlarmStatusName(entity.getAlarmStatus()));

        // 设置是否已处理
        vo.setProcessedName(Boolean.TRUE.equals(entity.getProcessed()) ? "是" : "否");

        return vo;
    }

    /**
     * 获取报警级别名称
     */
    private String getAlarmLevelName(Integer alarmLevel) {
        if (alarmLevel == null) {
            return "未知";
        }
        switch (alarmLevel) {
            case 1:
                return "低级";
            case 2:
                return "中级";
            case 3:
                return "高级";
            case 4:
                return "紧急";
            case 5:
                return "严重";
            default:
                return "未知";
        }
    }

    /**
     * 获取报警类型名称
     */
    private String getAlarmTypeName(String alarmType) {
        if (!StringUtils.hasText(alarmType)) {
            return "未知";
        }
        switch (alarmType) {
            case "DEVICE_OFFLINE":
                return "设备离线";
            case "ILLEGAL_ENTRY":
                return "非法闯入";
            case "DOOR_TIMEOUT":
                return "门超时未关";
            case "FORCED_ENTRY":
                return "强力破门";
            case "DURESS_ALARM":
                return "胁迫报警";
            case "CARD_ABNORMAL":
                return "卡片异常";
            default:
                return alarmType;
        }
    }

    /**
     * 获取处理状态名称
     */
    private String getProcessStatusName(Integer processStatus) {
        if (processStatus == null) {
            return "未知";
        }
        switch (processStatus) {
            case 0:
                return "未处理";
            case 1:
                return "处理中";
            case 2:
                return "已处理";
            case 3:
                return "已忽略";
            default:
                return "未知";
        }
    }

    /**
     * 获取报警状态名称
     */
    private String getAlarmStatusName(String alarmStatus) {
        if (!StringUtils.hasText(alarmStatus)) {
            return "未知";
        }
        switch (alarmStatus) {
            case "ACTIVE":
                return "活跃";
            case "ACKNOWLEDGED":
                return "已确认";
            case "PROCESSING":
                return "处理中";
            case "RESOLVED":
                return "已解决";
            case "CLOSED":
                return "已关闭";
            case "IGNORED":
                return "已忽略";
            default:
                return alarmStatus;
        }
    }

    /**
     * 发送报警处理通知
     */
    private void sendAlarmHandledNotification(AccessAlarmEntity alarm) {
        try {
            // 构建通知消息
            Map<String, Object> message = new HashMap<>();
            message.put("alarmId", alarm.getAlarmId());
            message.put("alarmType", alarm.getAlarmType());
            message.put("alarmLevel", alarm.getAlarmLevel());
            message.put("deviceId", alarm.getDeviceId());
            message.put("deviceName", alarm.getDeviceName());
            message.put("handlerId", alarm.getHandlerId());
            message.put("handlerName", alarm.getHandlerName());
            message.put("handleTime", alarm.getHandleTime());
            message.put("handleRemark", alarm.getHandleRemark());
            message.put("handleResult", alarm.getHandleResult());

            // 发送到RabbitMQ（如果配置了消息队列）
            rabbitTemplate.convertAndSend("access.alarm.handled", message);

            log.info("[实时监控] 发送报警处理通知成功: alarmId={}, alarmType={}, handlerId={}",
                    alarm.getAlarmId(), alarm.getAlarmType(), alarm.getHandlerId());

        } catch (Exception e) {
            // 通知发送失败不影响主流程，仅记录日志
            log.warn("[实时监控] 发送报警处理通知失败: alarmId={}, error={}",
                    alarm.getAlarmId(), e.getMessage());
        }
    }
}
