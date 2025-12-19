package net.lab1024.sa.video.manager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.video.dao.VideoPTZDao;
import net.lab1024.sa.video.entity.VideoPTZEntity;

/**
 * 视频云台控制管理器
 * <p>
 * 提供PTZ云台控制管理相关的业务编排功能，包括云台控制、预置位管理、巡航管理等
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
public class VideoPTZManager {

    private final VideoPTZDao videoPTZDao;

    // 设备当前位置缓存
    private final Map<String, VideoPTZEntity> currentPositionCache = new ConcurrentHashMap<>();

    // 预置位缓存
    private final Map<String, Map<Integer, VideoPTZEntity>> presetCache = new ConcurrentHashMap<>();

    // 执行中的控制任务
    private final Map<Long, VideoPTZEntity> executingControls = new ConcurrentHashMap<>();

    // 定时任务执行器
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    /**
     * 构造函数注入依赖
     *
     * @param videoPTZDao 视频PTZ数据访问层
     */
    public VideoPTZManager(VideoPTZDao videoPTZDao) {
        this.videoPTZDao = videoPTZDao;
        initScheduledTasks();
    }

    /**
     * 执行云台控制
     *
     * @param controlEntity 控制实体
     * @return 是否成功执行
     */
    public boolean executePTZControl(VideoPTZEntity controlEntity) {
        log.info("[云台控制] 执行PTZ控制，deviceId={}, channelNo={}, command={}",
                controlEntity.getDeviceId(), controlEntity.getChannelNo(), controlEntity.getControlCommand());

        try {
            // 设置默认值
            setDefaultValues(controlEntity);

            // 插入控制记录
            int result = videoPTZDao.insert(controlEntity);
            if (result <= 0) {
                throw new RuntimeException("插入PTZ控制记录失败");
            }

            // 开始执行
            boolean success = startExecution(controlEntity);

            log.info("[云台控制] PTZ控制{}，controlId={}, command={}",
                    success ? "成功执行" : "执行失败", controlEntity.getPtzId(), controlEntity.getControlCommand());
            return success;

        } catch (Exception e) {
            log.error("[云台控制] 执行PTZ控制失败，deviceId={}, channelNo={}, command={}",
                    controlEntity.getDeviceId(), controlEntity.getChannelNo(), controlEntity.getControlCommand(), e);
            return false;
        }
    }

    /**
     * 移动云台
     *
     * @param deviceId 设备ID
     * @param channelNo 通道号
     * @param command 控制命令
     * @param moveSpeed 移动速度
     * @return 是否成功移动
     */
    public boolean movePTZ(Long deviceId, Integer channelNo, String command, Integer moveSpeed) {
        log.info("[云台控制] 移动云台，deviceId={}, channelNo={}, command={}, speed={}",
                deviceId, channelNo, command, moveSpeed);

        try {
            VideoPTZEntity control = new VideoPTZEntity();
            control.setDeviceId(deviceId);
            control.setChannelNo(channelNo);
            control.setControlType(1); // 云台移动
            control.setControlCommand(command);
            control.setMoveSpeed(moveSpeed);
            control.setUserId(getCurrentUserId());
            control.setSessionId(getCurrentSessionId());

            return executePTZControl(control);

        } catch (Exception e) {
            log.error("[云台控制] 移动云台失败，deviceId={}, channelNo={}, command={}",
                    deviceId, channelNo, command, e);
            return false;
        }
    }

    /**
     * 变倍控制
     *
     * @param deviceId 设备ID
     * @param channelNo 通道号
     * @param zoomRatio 变倍倍数
     * @return 是否成功变倍
     */
    public boolean zoomControl(Long deviceId, Integer channelNo, Double zoomRatio) {
        log.info("[云台控制] 变倍控制，deviceId={}, channelNo={}, zoomRatio={}",
                deviceId, channelNo, zoomRatio);

        try {
            VideoPTZEntity control = new VideoPTZEntity();
            control.setDeviceId(deviceId);
            control.setChannelNo(channelNo);
            control.setControlType(2); // 变倍控制
            control.setControlCommand("ZOOM_CONTROL");
            control.setZoomRatio(zoomRatio);
            control.setUserId(getCurrentUserId());
            control.setSessionId(getCurrentSessionId());

            return executePTZControl(control);

        } catch (Exception e) {
            log.error("[云台控制] 变倍控制失败，deviceId={}, channelNo={}, zoomRatio={}",
                    deviceId, channelNo, zoomRatio, e);
            return false;
        }
    }

    /**
     * 设置预置位
     *
     * @param deviceId 设备ID
     * @param channelNo 通道号
     * @param presetNumber 预置位编号
     * @param presetName 预置位名称
     * @return 是否成功设置
     */
    public boolean setPreset(Long deviceId, Integer channelNo, Integer presetNumber, String presetName) {
        log.info("[云台控制] 设置预置位，deviceId={}, channelNo={}, presetNumber={}, name={}",
                deviceId, channelNo, presetNumber, presetName);

        try {
            // 获取当前位置
            String deviceKey = generateDeviceKey(deviceId, channelNo);
            VideoPTZEntity currentPos = currentPositionCache.get(deviceKey);

            if (currentPos == null) {
                log.warn("[云台控制] 无法获取当前位置，deviceId={}, channelNo={}", deviceId, channelNo);
                return false;
            }

            VideoPTZEntity preset = new VideoPTZEntity();
            preset.setDeviceId(deviceId);
            preset.setChannelNo(channelNo);
            preset.setControlType(3); // 预置位
            preset.setControlCommand("SET_PRESET");
            preset.setPresetNumber(presetNumber);
            preset.setPresetName(presetName);
            preset.setPanAngle(currentPos.getPanAngle());
            preset.setTiltAngle(currentPos.getTiltAngle());
            preset.setZoomRatio(currentPos.getZoomRatio());
            preset.setUserId(getCurrentUserId());
            preset.setSessionId(getCurrentSessionId());

            // 删除已存在的预置位
            deletePreset(deviceId, presetNumber);

            return executePTZControl(preset);

        } catch (Exception e) {
            log.error("[云台控制] 设置预置位失败，deviceId={}, channelNo={}, presetNumber={}",
                    deviceId, channelNo, presetNumber, e);
            return false;
        }
    }

    /**
     * 调用预置位
     *
     * @param deviceId 设备ID
     * @param channelNo 通道号
     * @param presetNumber 预置位编号
     * @return 是否成功调用
     */
    public boolean gotoPreset(Long deviceId, Integer channelNo, Integer presetNumber) {
        log.info("[云台控制] 调用预置位，deviceId={}, channelNo={}, presetNumber={}",
                deviceId, channelNo, presetNumber);

        try {
            VideoPTZEntity control = new VideoPTZEntity();
            control.setDeviceId(deviceId);
            control.setChannelNo(channelNo);
            control.setControlType(3); // 预置位
            control.setControlCommand("GOTO_PRESET");
            control.setPresetNumber(presetNumber);
            control.setUserId(getCurrentUserId());
            control.setSessionId(getCurrentSessionId());

            return executePTZControl(control);

        } catch (Exception e) {
            log.error("[云台控制] 调用预置位失败，deviceId={}, channelNo={}, presetNumber={}",
                    deviceId, channelNo, presetNumber, e);
            return false;
        }
    }

    /**
     * 删除预置位
     *
     * @param deviceId 设备ID
     * @param presetNumber 预置位编号
     * @return 是否成功删除
     */
    public boolean deletePreset(Long deviceId, Integer presetNumber) {
        log.info("[云台控制] 删除预置位，deviceId={}, presetNumber={}", deviceId, presetNumber);

        try {
            VideoPTZEntity preset = videoPTZDao.selectPresetByNumber(deviceId, presetNumber);
            if (preset != null) {
                videoPTZDao.deleteById(preset.getPtzId());

                // 更新缓存
                String deviceKey = generateDeviceKey(deviceId, preset.getChannelNo());
                Map<Integer, VideoPTZEntity> devicePresets = presetCache.get(deviceKey);
                if (devicePresets != null) {
                    devicePresets.remove(presetNumber);
                }
            }

            log.info("[云台控制] 预置位删除成功，deviceId={}, presetNumber={}", deviceId, presetNumber);
            return true;

        } catch (Exception e) {
            log.error("[云台控制] 删除预置位失败，deviceId={}, presetNumber={}", deviceId, presetNumber, e);
            return false;
        }
    }

    /**
     * 获取设备预置位列表
     *
     * @param deviceId 设备ID
     * @param channelNo 通道号
     * @return 预置位列表
     */
    public List<VideoPTZEntity> getDevicePresets(Long deviceId, Integer channelNo) {
        try {
            String deviceKey = generateDeviceKey(deviceId, channelNo);
            Map<Integer, VideoPTZEntity> devicePresets = presetCache.get(deviceKey);

            if (devicePresets == null) {
                List<VideoPTZEntity> presets = videoPTZDao.selectPresetsByDevice(deviceId);
                devicePresets = new ConcurrentHashMap<>();
                for (VideoPTZEntity preset : presets) {
                    devicePresets.put(preset.getPresetNumber(), preset);
                }
                presetCache.put(deviceKey, devicePresets);
            }

            return List.copyOf(devicePresets.values());

        } catch (Exception e) {
            log.error("[云台控制] 获取设备预置位失败，deviceId={}, channelNo={}", deviceId, channelNo, e);
            return List.of();
        }
    }

    /**
     * 开始巡航
     *
     * @param deviceId 设备ID
     * @param channelNo 通道号
     * @param cruisePathId 巡航路径ID
     * @return 是否成功开始
     */
    public boolean startCruise(Long deviceId, Integer channelNo, String cruisePathId) {
        log.info("[云台控制] 开始巡航，deviceId={}, channelNo={}, cruisePathId={}",
                deviceId, channelNo, cruisePathId);

        try {
            VideoPTZEntity control = new VideoPTZEntity();
            control.setDeviceId(deviceId);
            control.setChannelNo(channelNo);
            control.setControlType(4); // 巡航
            control.setControlCommand("START_CRUISE");
            control.setCruisePathId(cruisePathId);
            control.setUserId(getCurrentUserId());
            control.setSessionId(getCurrentSessionId());

            return executePTZControl(control);

        } catch (Exception e) {
            log.error("[云台控制] 开始巡航失败，deviceId={}, channelNo={}, cruisePathId={}",
                    deviceId, channelNo, cruisePathId, e);
            return false;
        }
    }

    /**
     * 停止巡航
     *
     * @param deviceId 设备ID
     * @param channelNo 通道号
     * @return 是否成功停止
     */
    public boolean stopCruise(Long deviceId, Integer channelNo) {
        log.info("[云台控制] 停止巡航，deviceId={}, channelNo={}", deviceId, channelNo);

        try {
            VideoPTZEntity control = new VideoPTZEntity();
            control.setDeviceId(deviceId);
            control.setChannelNo(channelNo);
            control.setControlType(4); // 巡航
            control.setControlCommand("STOP_CRUISE");
            control.setUserId(getCurrentUserId());
            control.setSessionId(getCurrentSessionId());

            return executePTZControl(control);

        } catch (Exception e) {
            log.error("[云台控制] 停止巡航失败，deviceId={}, channelNo={}", deviceId, channelNo, e);
            return false;
        }
    }

    /**
     * 更新云台当前位置
     *
     * @param deviceId 设备ID
     * @param channelNo 通道号
     * @param panAngle 水平角度
     * @param tiltAngle 垂直角度
     * @param zoomRatio 变倍倍数
     * @return 是否成功更新
     */
    public boolean updateCurrentPosition(Long deviceId, Integer channelNo,
                                         Double panAngle, Double tiltAngle, Double zoomRatio) {
        try {
            int result = videoPTZDao.updateCurrentPosition(deviceId, channelNo, panAngle, tiltAngle, zoomRatio);

            if (result > 0) {
                // 更新缓存
                String deviceKey = generateDeviceKey(deviceId, channelNo);
                VideoPTZEntity position = new VideoPTZEntity();
                position.setDeviceId(deviceId);
                position.setChannelNo(channelNo);
                position.setPanAngle(panAngle);
                position.setTiltAngle(tiltAngle);
                position.setZoomRatio(zoomRatio);
                position.setUpdateTime(LocalDateTime.now());

                currentPositionCache.put(deviceKey, position);

                log.debug("[云台控制] 位置更新成功，deviceId={}, channelNo={}, pan={}, tilt={}, zoom={}",
                        deviceId, channelNo, panAngle, tiltAngle, zoomRatio);
                return true;
            }
            return false;

        } catch (Exception e) {
            log.error("[云台控制] 更新云台位置失败，deviceId={}, channelNo={}", deviceId, channelNo, e);
            return false;
        }
    }

    /**
     * 获取云台控制统计
     *
     * @param deviceId 设备ID
     * @param hours 统计时间范围（小时）
     * @return 控制频率统计
     */
    public Map<String, Object> getPTZStatistics(Long deviceId, Integer hours) {
        try {
            Map<String, Object> statistics = new java.util.HashMap<>();

            // 获取控制频率统计
            List<Map<String, Object>> frequencyStats = videoPTZDao.getDevicePTZFrequency(deviceId, hours);
            statistics.put("frequencyStatistics", frequencyStats);

            // 获取总数统计
            statistics.put("totalControls", videoPTZDao.countPTZControls(deviceId, null, null, null));

            // 按控制类型统计
            Map<String, Object> typeStats = new java.util.HashMap<>();
            videoPTZDao.countPTZByType().forEach(row -> {
                typeStats.put("type_" + row.get("control_type"), row.get("count"));
            });
            statistics.put("typeStatistics", typeStats);

            return statistics;

        } catch (Exception e) {
            log.error("[云台控制] 获取PTZ统计失败，deviceId={}", deviceId, e);
            return new java.util.HashMap<>();
        }
    }

    /**
     * 开始执行控制
     *
     * @param control 控制实体
     * @return 是否成功开始
     */
    private boolean startExecution(VideoPTZEntity control) {
        try {
            LocalDateTime startTime = LocalDateTime.now();

            int result = videoPTZDao.startExecution(control.getPtzId(), startTime);
            if (result <= 0) {
                log.warn("[云台控制] 开始执行失败，controlId={}", control.getPtzId());
                return false;
            }

            // 更新状态
            control.setControlStatus(1);
            control.setExecuteStartTime(startTime);
            executingControls.put(control.getPtzId(), control);

            // 模拟设备响应（实际应该调用设备SDK）
            simulateDeviceResponse(control);

            return true;

        } catch (Exception e) {
            log.error("[云台控制] 开始执行异常，controlId={}", control.getPtzId(), e);
            return false;
        }
    }

    /**
     * 模拟设备响应
     *
     * @param control 控制实体
     */
    private void simulateDeviceResponse(VideoPTZEntity control) {
        // 异步处理设备响应
        scheduler.schedule(() -> {
            try {
                // 模拟设备执行时间
                Thread.sleep(calculateExecutionTime(control));

                // 完成执行
                completeExecution(control.getPtzId(), true);

            } catch (Exception e) {
                log.error("[云台控制] 模拟设备响应异常，controlId={}", control.getPtzId(), e);
                completeExecution(control.getPtzId(), false);
            }
        }, calculateExecutionTime(control), TimeUnit.MILLISECONDS);
    }

    /**
     * 完成执行
     *
     * @param controlId 控制ID
     * @param success 是否成功
     */
    private void completeExecution(Long controlId, boolean success) {
        try {
            VideoPTZEntity control = executingControls.remove(controlId);
            if (control == null) {
                return;
            }

            LocalDateTime endTime = LocalDateTime.now();
            long duration = java.time.Duration.between(control.getExecuteStartTime(), endTime).toMillis();

            Integer result = success ? 1 : 3; // 1-成功，3-失败

            videoPTZDao.completeExecution(controlId, endTime, duration, result);

            // 更新缓存位置
            if (success && control.getControlType() == 1) {
                updateCurrentPosition(
                    control.getDeviceId(),
                    control.getChannelNo(),
                    control.getPanAngle(),
                    control.getTiltAngle(),
                    control.getZoomRatio()
                );
            }

            log.info("[云台控制] 控制{}，controlId={}, command={}, duration={}ms",
                    success ? "成功完成" : "执行失败",
                    controlId, control.getControlCommand(), duration);

        } catch (Exception e) {
            log.error("[云台控制] 完成执行异常，controlId={}", controlId, e);
        }
    }

    /**
     * 计算执行时间
     *
     * @param control 控制实体
     * @return 执行时间（毫秒）
     */
    private long calculateExecutionTime(VideoPTZEntity control) {
        // 根据控制类型和速度计算执行时间
        switch (control.getControlType()) {
            case 1: // 云台移动
                if (control.getMoveSpeed() != null) {
                    return 2000 / control.getMoveSpeed(); // 速度越快，时间越短
                }
                return 1000;
            case 2: // 变倍控制
                return 1500;
            case 3: // 预置位
                return 2000;
            case 4: // 巡航
                return 500;
            case 5: // 扫描
                return 3000;
            default:
                return 1000;
        }
    }

    /**
     * 设置默认值
     *
     * @param control 控制实体
     */
    private void setDefaultValues(VideoPTZEntity control) {
        if (control.getControlStatus() == null) {
            control.setControlStatus(1); // 默认执行中
        }
        if (control.getMoveSpeed() == null) {
            control.setMoveSpeed(3); // 默认正常速度
        }
        if (control.getPermissionLevel() == null) {
            control.setPermissionLevel(2); // 默认控制权限
        }
        if (control.getControlSource() == null) {
            control.setControlSource(2); // 默认远程控制
        }
    }

    /**
     * 生成设备缓存键
     *
     * @param deviceId 设备ID
     * @param channelNo 通道号
     * @return 缓存键
     */
    private String generateDeviceKey(Long deviceId, Integer channelNo) {
        return deviceId + "_" + channelNo;
    }

    /**
     * 获取当前用户ID（实际应该从上下文获取）
     *
     * @return 用户ID
     */
    private Long getCurrentUserId() {
        // TODO: 从安全上下文获取当前用户ID
        return 1L;
    }

    /**
     * 获取当前会话ID（实际应该从上下文获取）
     *
     * @return 会话ID
     */
    private String getCurrentSessionId() {
        // TODO: 从上下文获取当前会话ID
        return "current_session";
    }

    /**
     * 初始化定时任务
     */
    private void initScheduledTasks() {
        // 每分钟检查超时的控制任务
        scheduler.scheduleAtFixedRate(this::checkTimeoutControls, 1, 1, TimeUnit.MINUTES);

        // 每小时清理缓存
        scheduler.scheduleAtFixedRate(this::cleanupCache, 1, 1, TimeUnit.HOURS);
    }

    /**
     * 检查超时控制
     */
    private void checkTimeoutControls() {
        try {
            int timeoutSeconds = 30; // 30秒超时
            List<VideoPTZEntity> timeoutControls = videoPTZDao.selectTimeoutControls(timeoutSeconds);

            for (VideoPTZEntity control : timeoutControls) {
                log.warn("[云台控制] 发现超时控制，controlId={}, command={}, executeStartTime={}",
                        control.getPtzId(), control.getControlCommand(), control.getExecuteStartTime());

                // 标记为失败
                completeExecution(control.getPtzId(), false);
            }

        } catch (Exception e) {
            log.error("[云台控制] 检查超时控制失败", e);
        }
    }

    /**
     * 清理缓存
     */
    private void cleanupCache() {
        try {
            currentPositionCache.clear();
            presetCache.clear();
            executingControls.clear();
            log.debug("[云台控制] 缓存清理完成");
        } catch (Exception e) {
            log.error("[云台控制] 清理缓存失败", e);
        }
    }
}

