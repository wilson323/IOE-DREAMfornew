package net.lab1024.sa.access.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.FirmwareUpgradeTaskDao;
import net.lab1024.sa.access.dao.FirmwareUpgradeDeviceDao;
import net.lab1024.sa.access.dao.DeviceFirmwareDao;
import net.lab1024.sa.common.entity.access.FirmwareUpgradeTaskEntity;
import net.lab1024.sa.common.entity.access.FirmwareUpgradeDeviceEntity;
import net.lab1024.sa.common.entity.device.DeviceFirmwareEntity;
import net.lab1024.sa.access.domain.form.FirmwareUpgradeTaskForm;
import net.lab1024.sa.access.domain.form.FirmwareUpgradeTaskQueryForm;
import net.lab1024.sa.access.domain.vo.FirmwareUpgradeDeviceVO;
import net.lab1024.sa.access.domain.vo.FirmwareUpgradeTaskVO;
import net.lab1024.sa.access.service.FirmwareUpgradeService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.exception.BusinessException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 固件升级服务实现
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
public class FirmwareUpgradeServiceImpl implements FirmwareUpgradeService {

    @Resource
    private FirmwareUpgradeTaskDao firmwareUpgradeTaskDao;

    @Resource
    private FirmwareUpgradeDeviceDao firmwareUpgradeDeviceDao;

    @Resource
    private DeviceFirmwareDao deviceFirmwareDao;

    private static final DateTimeFormatter TASK_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // ==================== 任务管理 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUpgradeTask(FirmwareUpgradeTaskForm taskForm, Long operatorId, String operatorName) {
        log.info("[固件升级服务] 创建升级任务: taskName={}, firmwareId={}, deviceCount={}",
                taskForm.getTaskName(), taskForm.getFirmwareId(), taskForm.getDeviceIds().size());

        // 1. 验证固件是否存在
        DeviceFirmwareEntity firmware = deviceFirmwareDao.selectById(taskForm.getFirmwareId());
        if (firmware == null) {
            throw new BusinessException("FIRMWARE_NOT_FOUND", "固件不存在");
        }

        // 2. 生成任务编号
        String taskNo = generateTaskNo();

        // 3. 创建任务实体
        FirmwareUpgradeTaskEntity task = new FirmwareUpgradeTaskEntity();
        BeanUtils.copyProperties(taskForm, task);
        task.setTaskNo(taskNo);
        task.setFirmwareVersion(firmware.getFirmwareVersion());
        task.setTargetDeviceCount(taskForm.getDeviceIds().size());
        task.setTaskStatus(1); // 待执行
        task.setUpgradeStrategy(taskForm.getUpgradeStrategy());
        task.setRollbackSupported(taskForm.getRollbackSupported() != null ? taskForm.getRollbackSupported() : 0);

        // 4. 设置定时/分批参数
        if (taskForm.getUpgradeStrategy() == 2) {
            // 定时升级
            if (taskForm.getScheduleTime() == null) {
                throw new BusinessException("SCHEDULE_TIME_REQUIRED", "定时升级必须指定执行时间");
            }
            task.setScheduleTime(taskForm.getScheduleTime());
        } else if (taskForm.getUpgradeStrategy() == 3) {
            // 分批升级
            if (taskForm.getBatchSize() == null || taskForm.getBatchSize() <= 0) {
                task.setBatchSize(10); // 默认每批10台
            } else {
                task.setBatchSize(taskForm.getBatchSize());
            }
            if (taskForm.getBatchInterval() == null || taskForm.getBatchInterval() <= 0) {
                task.setBatchInterval(300); // 默认间隔5分钟
            } else {
                task.setBatchInterval(taskForm.getBatchInterval());
            }
        }

        // 5. 保存任务
        firmwareUpgradeTaskDao.insert(task);

        // 6. 创建设备明细记录
        List<FirmwareUpgradeDeviceEntity> deviceList = createDeviceDetailList(
                task.getTaskId(),
                taskForm.getDeviceIds(),
                firmware.getFirmwareVersion()
        );
        if (!deviceList.isEmpty()) {
            firmwareUpgradeDeviceDao.insertBatch(deviceList);
        }

        log.info("[固件升级服务] 升级任务创建成功: taskId={}, taskNo={}", task.getTaskId(), taskNo);
        return task.getTaskId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean startUpgradeTask(Long taskId) {
        log.info("[固件升级服务] 启动升级任务: taskId={}", taskId);

        FirmwareUpgradeTaskEntity task = firmwareUpgradeTaskDao.selectById(taskId);
        if (task == null) {
            throw new BusinessException("TASK_NOT_FOUND", "任务不存在");
        }

        if (task.getTaskStatus() != 1) {
            throw new BusinessException("TASK_STATUS_INVALID", "任务状态不允许启动");
        }

        // 更新任务状态为执行中
        task.setTaskStatus(2); // 执行中
        task.setStartTime(LocalDateTime.now());
        firmwareUpgradeTaskDao.updateById(task);

        // 根据策略执行升级
        if (task.getUpgradeStrategy() == 1) {
            // 立即升级 - 启动所有设备升级
            executeImmediateUpgrade(task);
        } else if (task.getUpgradeStrategy() == 3) {
            // 分批升级 - 启动第一批
            executeBatchUpgrade(task, 1);
        }

        log.info("[固件升级服务] 升级任务启动成功: taskId={}", taskId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean pauseUpgradeTask(Long taskId) {
        log.info("[固件升级服务] 暂停升级任务: taskId={}", taskId);

        FirmwareUpgradeTaskEntity task = firmwareUpgradeTaskDao.selectById(taskId);
        if (task == null) {
            throw new BusinessException("TASK_NOT_FOUND", "任务不存在");
        }

        if (task.getTaskStatus() != 2) {
            throw new BusinessException("TASK_STATUS_INVALID", "只有执行中的任务可以暂停");
        }

        // 更新任务状态为已暂停
        task.setTaskStatus(3); // 已暂停
        firmwareUpgradeTaskDao.updateById(task);

        // 将所有升级中的设备状态改为待升级
        LambdaQueryWrapper<FirmwareUpgradeDeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FirmwareUpgradeDeviceEntity::getTaskId, taskId)
                .eq(FirmwareUpgradeDeviceEntity::getUpgradeStatus, 2); // 升级中

        List<FirmwareUpgradeDeviceEntity> upgradingDevices = firmwareUpgradeDeviceDao.selectList(queryWrapper);
        for (FirmwareUpgradeDeviceEntity device : upgradingDevices) {
            device.setUpgradeStatus(1); // 改为待升级
            firmwareUpgradeDeviceDao.updateById(device);
        }

        log.info("[固件升级服务] 升级任务暂停成功: taskId={}", taskId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean resumeUpgradeTask(Long taskId) {
        log.info("[固件升级服务] 恢复升级任务: taskId={}", taskId);

        FirmwareUpgradeTaskEntity task = firmwareUpgradeTaskDao.selectById(taskId);
        if (task == null) {
            throw new BusinessException("TASK_NOT_FOUND", "任务不存在");
        }

        if (task.getTaskStatus() != 3) {
            throw new BusinessException("TASK_STATUS_INVALID", "只有已暂停的任务可以恢复");
        }

        // 更新任务状态为执行中
        task.setTaskStatus(2); // 执行中
        firmwareUpgradeTaskDao.updateById(task);

        // 继续执行待升级的设备
        FirmwareUpgradeTaskEntity finalTask = task;
        new Thread(() -> {
            try {
                executePendingDevices(finalTask);
            } catch (Exception e) {
                log.error("[固件升级服务] 恢复升级任务失败: taskId={}", taskId, e);
            }
        }).start();

        log.info("[固件升级服务] 升级任务恢复成功: taskId={}", taskId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean stopUpgradeTask(Long taskId) {
        log.info("[固件升级服务] 停止升级任务: taskId={}", taskId);

        FirmwareUpgradeTaskEntity task = firmwareUpgradeTaskDao.selectById(taskId);
        if (task == null) {
            throw new BusinessException("TASK_NOT_FOUND", "任务不存在");
        }

        if (task.getTaskStatus() == 4 || task.getTaskStatus() == 5) {
            throw new BusinessException("TASK_STATUS_INVALID", "任务已结束，无法停止");
        }

        // 更新任务状态为已完成
        task.setTaskStatus(4); // 已完成
        task.setEndTime(LocalDateTime.now());
        firmwareUpgradeTaskDao.updateById(task);

        // 将所有待升级和升级中的设备状态标记为失败
        LambdaQueryWrapper<FirmwareUpgradeDeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FirmwareUpgradeDeviceEntity::getTaskId, taskId)
                .in(FirmwareUpgradeDeviceEntity::getUpgradeStatus, List.of(1, 2)); // 待升级、升级中

        List<FirmwareUpgradeDeviceEntity> pendingDevices = firmwareUpgradeDeviceDao.selectList(queryWrapper);
        for (FirmwareUpgradeDeviceEntity device : pendingDevices) {
            device.setUpgradeStatus(4); // 失败
            device.setErrorCode("TASK_STOPPED");
            device.setErrorMessage("任务已停止");
            device.setEndTime(LocalDateTime.now());
            firmwareUpgradeDeviceDao.updateById(device);
        }

        // 更新统计信息
        updateTaskStatistics(taskId);

        log.info("[固件升级服务] 升级任务停止成功: taskId={}", taskId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteUpgradeTask(Long taskId) {
        log.info("[固件升级服务] 删除升级任务: taskId={}", taskId);

        FirmwareUpgradeTaskEntity task = firmwareUpgradeTaskDao.selectById(taskId);
        if (task == null) {
            throw new BusinessException("TASK_NOT_FOUND", "任务不存在");
        }

        if (task.getTaskStatus() == 2) {
            throw new BusinessException("TASK_STATUS_INVALID", "执行中的任务不能删除");
        }

        // 逻辑删除任务
        firmwareUpgradeTaskDao.deleteById(taskId);

        // 逻辑删除设备明细
        LambdaQueryWrapper<FirmwareUpgradeDeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FirmwareUpgradeDeviceEntity::getTaskId, taskId);
        firmwareUpgradeDeviceDao.delete(queryWrapper);

        log.info("[固件升级服务] 升级任务删除成功: taskId={}", taskId);
        return true;
    }

    // ==================== 任务查询 ====================

    @Override
    public PageResult<FirmwareUpgradeTaskVO> queryTasksPage(FirmwareUpgradeTaskQueryForm queryForm) {
        log.debug("[固件升级服务] 分页查询升级任务: taskName={}, taskStatus={}",
                queryForm.getTaskName(), queryForm.getTaskStatus());

        LambdaQueryWrapper<FirmwareUpgradeTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(queryForm.getFirmwareId() != null,
                        FirmwareUpgradeTaskEntity::getFirmwareId, queryForm.getFirmwareId())
                .eq(queryForm.getTaskStatus() != null,
                        FirmwareUpgradeTaskEntity::getTaskStatus, queryForm.getTaskStatus())
                .eq(queryForm.getUpgradeStrategy() != null,
                        FirmwareUpgradeTaskEntity::getUpgradeStrategy, queryForm.getUpgradeStrategy())
                .like(queryForm.getTaskName() != null,
                        FirmwareUpgradeTaskEntity::getTaskName, queryForm.getTaskName())
                .orderByDesc(FirmwareUpgradeTaskEntity::getCreateTime);

        Page<FirmwareUpgradeTaskEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
        Page<FirmwareUpgradeTaskEntity> pageResult = firmwareUpgradeTaskDao.selectPage(page, queryWrapper);

        List<FirmwareUpgradeTaskVO> voList = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, pageResult.getTotal(), queryForm.getPageNum(), queryForm.getPageSize());
    }

    @Override
    public FirmwareUpgradeTaskVO getTaskDetail(Long taskId) {
        log.debug("[固件升级服务] 获取任务详情: taskId={}", taskId);

        FirmwareUpgradeTaskEntity task = firmwareUpgradeTaskDao.selectById(taskId);
        if (task == null) {
            throw new BusinessException("TASK_NOT_FOUND", "任务不存在");
        }

        FirmwareUpgradeTaskVO vo = convertToVO(task);

        // 计算格式化耗时
        if (task.getStartTime() != null && task.getEndTime() != null) {
            long seconds = ChronoUnit.SECONDS.between(task.getStartTime(), task.getEndTime());
            vo.setDurationFormatted(formatDuration(seconds));
        }

        return vo;
    }

    @Override
    public List<FirmwareUpgradeDeviceVO> getTaskDevices(Long taskId) {
        log.debug("[固件升级服务] 获取任务设备列表: taskId={}", taskId);

        List<FirmwareUpgradeDeviceEntity> deviceList = firmwareUpgradeDeviceDao.selectByTaskId(taskId);

        return deviceList.stream()
                .map(this::convertToDeviceVO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getTaskProgress(Long taskId) {
        log.debug("[固件升级服务] 获取任务进度: taskId={}", taskId);

        Map<String, Object> statistics = firmwareUpgradeDeviceDao.selectTaskStatistics(taskId);
        FirmwareUpgradeTaskEntity task = firmwareUpgradeTaskDao.selectById(taskId);

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", taskId);
        result.putAll(statistics);

        if (task != null) {
            result.put("taskStatus", task.getTaskStatus());
            result.put("upgradeProgress", task.getUpgradeProgress());
        }

        return result;
    }

    // ==================== 设备升级管理 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer retryFailedDevices(Long taskId) {
        log.info("[固件升级服务] 重试失败设备: taskId={}", taskId);

        List<FirmwareUpgradeDeviceEntity> failedDevices = firmwareUpgradeDeviceDao.selectFailedDevices(taskId);

        if (failedDevices.isEmpty()) {
            log.info("[固件升级服务] 没有需要重试的设备: taskId={}", taskId);
            return 0;
        }

        int retryCount = 0;
        for (FirmwareUpgradeDeviceEntity device : failedDevices) {
            // 增加重试次数
            firmwareUpgradeDeviceDao.incrementRetryCount(device.getDetailId());

            // 检查是否超过最大重试次数
            if (device.getRetryCount() + 1 >= device.getMaxRetry()) {
                log.warn("[固件升级服务] 设备重试次数已达上限: deviceId={}, retryCount={}",
                        device.getDeviceId(), device.getRetryCount() + 1);
                continue;
            }

            // 重置状态为待升级
            device.setUpgradeStatus(1);
            device.setErrorCode(null);
            device.setErrorMessage(null);
            firmwareUpgradeDeviceDao.updateById(device);
            retryCount++;
        }

        log.info("[固件升级服务] 重试失败设备完成: taskId={}, retryCount={}", taskId, retryCount);
        return retryCount;
    }

    @Override
    public List<FirmwareUpgradeDeviceVO> getPendingDevices(Long taskId) {
        log.debug("[固件升级服务] 获取待升级设备列表: taskId={}", taskId);

        List<FirmwareUpgradeDeviceEntity> deviceList = firmwareUpgradeDeviceDao.selectPendingDevices(taskId);

        return deviceList.stream()
                .map(this::convertToDeviceVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FirmwareUpgradeDeviceVO> getFailedDevices(Long taskId) {
        log.debug("[固件升级服务] 获取失败设备列表: taskId={}", taskId);

        List<FirmwareUpgradeDeviceEntity> deviceList = firmwareUpgradeDeviceDao.selectFailedDevices(taskId);

        return deviceList.stream()
                .map(this::convertToDeviceVO)
                .collect(Collectors.toList());
    }

    // ==================== 回滚管理 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long rollbackUpgradeTask(Long taskId) {
        log.info("[固件升级服务] 回滚升级任务: taskId={}", taskId);

        FirmwareUpgradeTaskEntity originalTask = firmwareUpgradeTaskDao.selectById(taskId);
        if (originalTask == null) {
            throw new BusinessException("TASK_NOT_FOUND", "任务不存在");
        }

        if (originalTask.getRollbackSupported() == null || originalTask.getRollbackSupported() != 1) {
            throw new BusinessException("ROLLBACK_NOT_SUPPORTED", "该任务不支持回滚");
        }

        // 创建回滚任务（这里简化处理，实际应该创建新的任务）
        originalTask.setRollbackTaskId(taskId);
        firmwareUpgradeTaskDao.updateById(originalTask);

        // TODO: 实现完整的回滚逻辑

        log.info("[固件升级服务] 回滚升级任务完成: taskId={}", taskId);
        return taskId;
    }

    @Override
    public Boolean isRollbackSupported(Long taskId) {
        FirmwareUpgradeTaskEntity task = firmwareUpgradeTaskDao.selectById(taskId);
        if (task == null) {
            return false;
        }
        return task.getRollbackSupported() != null && task.getRollbackSupported() == 1;
    }

    // ==================== 定时任务 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processScheduledTasks() {
        log.debug("[固件升级服务] 处理定时升级任务");

        List<FirmwareUpgradeTaskEntity> pendingTasks = firmwareUpgradeTaskDao.selectPendingTasks();

        for (FirmwareUpgradeTaskEntity task : pendingTasks) {
            try {
                log.info("[固件升级服务] 启动定时任务: taskId={}, taskNo={}", task.getTaskId(), task.getTaskNo());
                startUpgradeTask(task.getTaskId());
            } catch (Exception e) {
                log.error("[固件升级服务] 启动定时任务失败: taskId={}", task.getTaskId(), e);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processBatchTasks() {
        log.debug("[固件升级服务] 处理分批升级任务");

        // 查询执行中的分批升级任务
        LambdaQueryWrapper<FirmwareUpgradeTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FirmwareUpgradeTaskEntity::getTaskStatus, 2) // 执行中
                .eq(FirmwareUpgradeTaskEntity::getUpgradeStrategy, 3); // 分批升级

        List<FirmwareUpgradeTaskEntity> batchTasks = firmwareUpgradeTaskDao.selectList(queryWrapper);

        for (FirmwareUpgradeTaskEntity task : batchTasks) {
            try {
                processNextBatch(task);
            } catch (Exception e) {
                log.error("[固件升级服务] 处理分批任务失败: taskId={}", task.getTaskId(), e);
            }
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 生成任务编号
     */
    private String generateTaskNo() {
        return "UPG" + LocalDateTime.now().format(TASK_NO_FORMATTER);
    }

    /**
     * 创建设备明细列表
     */
    private List<FirmwareUpgradeDeviceEntity> createDeviceDetailList(Long taskId, List<Long> deviceIds, String targetVersion) {
        // TODO: 从设备服务获取设备信息
        // 这里简化处理，假设设备ID就是设备信息
        return deviceIds.stream().map(deviceId -> {
            FirmwareUpgradeDeviceEntity device = new FirmwareUpgradeDeviceEntity();
            device.setTaskId(taskId);
            device.setDeviceId(deviceId);
            device.setDeviceCode("DEV-" + deviceId);
            device.setDeviceName("设备-" + deviceId);
            device.setDeviceIp("192.168.1." + (deviceId % 256));
            device.setCurrentVersion("v0.9.0"); // 假设当前版本
            device.setTargetVersion(targetVersion);
            device.setUpgradeStatus(1); // 待升级
            device.setRetryCount(0);
            device.setMaxRetry(3);
            return device;
        }).collect(Collectors.toList());
    }

    /**
     * 执行立即升级
     */
    private void executeImmediateUpgrade(FirmwareUpgradeTaskEntity task) {
        log.info("[固件升级服务] 执行立即升级: taskId={}", task.getTaskId());

        // 异步执行升级
        FirmwareUpgradeTaskEntity finalTask = task;
        new Thread(() -> {
            try {
                executePendingDevices(finalTask);
                checkTaskCompletion(finalTask.getTaskId());
            } catch (Exception e) {
                log.error("[固件升级服务] 立即升级失败: taskId={}", finalTask.getTaskId(), e);
            }
        }).start();
    }

    /**
     * 执行分批升级
     */
    private void executeBatchUpgrade(FirmwareUpgradeTaskEntity task, int batchNumber) {
        log.info("[固件升级服务] 执行分批升级: taskId={}, batchNumber={}", task.getTaskId(), batchNumber);

        // 获取待升级设备
        List<FirmwareUpgradeDeviceEntity> pendingDevices = firmwareUpgradeDeviceDao.selectPendingDevices(task.getTaskId());

        // 计算本批设备
        int batchSize = task.getBatchSize();
        int fromIndex = 0;
        int toIndex = Math.min(batchSize, pendingDevices.size());

        if (fromIndex >= toIndex) {
            log.info("[固件升级服务] 没有待升级设备: taskId={}", task.getTaskId());
            return;
        }

        List<FirmwareUpgradeDeviceEntity> batchDevices = pendingDevices.subList(fromIndex, toIndex);

        // 执行本批升级
        FirmwareUpgradeTaskEntity finalTask = task;
        new Thread(() -> {
            try {
                for (FirmwareUpgradeDeviceEntity device : batchDevices) {
                    upgradeDevice(finalTask, device);
                }
                updateTaskStatistics(finalTask.getTaskId());
                checkTaskCompletion(finalTask.getTaskId());
            } catch (Exception e) {
                log.error("[固件升级服务] 分批升级失败: taskId={}", finalTask.getTaskId(), e);
            }
        }).start();
    }

    /**
     * 处理下一批设备
     */
    private void processNextBatch(FirmwareUpgradeTaskEntity task) {
        List<FirmwareUpgradeDeviceEntity> pendingDevices = firmwareUpgradeDeviceDao.selectPendingDevices(task.getTaskId());

        if (pendingDevices.isEmpty()) {
            log.info("[固件升级服务] 分批升级完成，没有待升级设备: taskId={}", task.getTaskId());
            checkTaskCompletion(task.getTaskId());
            return;
        }

        // 检查是否有正在升级的设备
        boolean hasUpgrading = pendingDevices.stream()
                .anyMatch(d -> d.getUpgradeStatus() == 2);

        if (hasUpgrading) {
            log.debug("[固件升级服务] 仍有设备升级中，跳过本批: taskId={}", task.getTaskId());
            return;
        }

        // 启动下一批
        executeBatchUpgrade(task, 1);
    }

    /**
     * 执行待升级设备
     */
    private void executePendingDevices(FirmwareUpgradeTaskEntity task) {
        List<FirmwareUpgradeDeviceEntity> pendingDevices = firmwareUpgradeDeviceDao.selectPendingDevices(task.getTaskId());

        for (FirmwareUpgradeDeviceEntity device : pendingDevices) {
            upgradeDevice(task, device);
        }

        updateTaskStatistics(task.getTaskId());
    }

    /**
     * 升级单个设备
     */
    private void upgradeDevice(FirmwareUpgradeTaskEntity task, FirmwareUpgradeDeviceEntity device) {
        log.info("[固件升级服务] 升级设备: taskId={}, deviceId={}", task.getTaskId(), device.getDeviceId());

        try {
            // 更新状态为升级中
            device.setUpgradeStatus(2);
            device.setStartTime(LocalDateTime.now());
            firmwareUpgradeDeviceDao.updateById(device);

            // TODO: 调用设备服务执行实际升级
            // 这里模拟升级过程
            Thread.sleep(1000);

            // 升级成功
            device.setUpgradeStatus(3);
            device.setEndTime(LocalDateTime.now());
            long duration = ChronoUnit.SECONDS.between(device.getStartTime(), device.getEndTime());
            device.setDurationSeconds((int) duration);
            firmwareUpgradeDeviceDao.updateById(device);

            log.info("[固件升级服务] 设备升级成功: taskId={}, deviceId={}", task.getTaskId(), device.getDeviceId());
        } catch (Exception e) {
            log.error("[固件升级服务] 设备升级失败: taskId={}, deviceId={}", task.getTaskId(), device.getDeviceId(), e);

            // 升级失败
            device.setUpgradeStatus(4);
            device.setEndTime(LocalDateTime.now());
            device.setErrorCode("UPGRADE_FAILED");
            device.setErrorMessage(e.getMessage());
            firmwareUpgradeDeviceDao.updateById(device);
        }
    }

    /**
     * 更新任务统计信息
     */
    private void updateTaskStatistics(Long taskId) {
        Map<String, Object> statistics = firmwareUpgradeDeviceDao.selectTaskStatistics(taskId);

        Integer totalCount = ((Number) statistics.getOrDefault("totalCount", 0)).intValue();
        Integer successCount = ((Number) statistics.getOrDefault("successCount", 0)).intValue();
        Integer failedCount = ((Number) statistics.getOrDefault("failedCount", 0)).intValue();
        Integer pendingCount = ((Number) statistics.getOrDefault("pendingCount", 0)).intValue();

        BigDecimal upgradeProgress = totalCount > 0
                ? BigDecimal.valueOf(successCount).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        firmwareUpgradeTaskDao.updateTaskStatistics(taskId, successCount, failedCount, pendingCount, upgradeProgress);
    }

    /**
     * 检查任务是否完成
     */
    private void checkTaskCompletion(Long taskId) {
        Map<String, Object> statistics = firmwareUpgradeDeviceDao.selectTaskStatistics(taskId);

        Integer pendingCount = ((Number) statistics.getOrDefault("pendingCount", 0)).intValue();
        Integer upgradingCount = ((Number) statistics.getOrDefault("upgradingCount", 0)).intValue();

        if (pendingCount == 0 && upgradingCount == 0) {
            // 任务完成
            FirmwareUpgradeTaskEntity task = firmwareUpgradeTaskDao.selectById(taskId);
            task.setTaskStatus(4); // 已完成
            task.setEndTime(LocalDateTime.now());
            if (task.getStartTime() != null) {
                long duration = ChronoUnit.SECONDS.between(task.getStartTime(), task.getEndTime());
                task.setDurationSeconds((int) duration);
            }
            firmwareUpgradeTaskDao.updateById(task);

            log.info("[固件升级服务] 升级任务完成: taskId={}", taskId);
        }
    }

    /**
     * 转换为VO
     */
    private FirmwareUpgradeTaskVO convertToVO(FirmwareUpgradeTaskEntity entity) {
        FirmwareUpgradeTaskVO vo = new FirmwareUpgradeTaskVO();
        BeanUtils.copyProperties(entity, vo);

        // 设置状态名称
        vo.setTaskStatusName(getTaskStatusName(entity.getTaskStatus()));
        vo.setUpgradeStrategyName(getUpgradeStrategyName(entity.getUpgradeStrategy()));

        // 设置格式化耗时
        if (entity.getDurationSeconds() != null && entity.getDurationSeconds() > 0) {
            vo.setDurationFormatted(formatDuration(entity.getDurationSeconds()));
        }

        return vo;
    }

    /**
     * 转换为设备VO
     */
    private FirmwareUpgradeDeviceVO convertToDeviceVO(FirmwareUpgradeDeviceEntity entity) {
        FirmwareUpgradeDeviceVO vo = new FirmwareUpgradeDeviceVO();
        BeanUtils.copyProperties(entity, vo);

        // 设置状态名称
        vo.setUpgradeStatusName(getUpgradeStatusName(entity.getUpgradeStatus()));

        // 设置格式化耗时
        if (entity.getDurationSeconds() != null && entity.getDurationSeconds() > 0) {
            vo.setDurationFormatted(formatDuration(entity.getDurationSeconds()));
        }

        return vo;
    }

    /**
     * 获取任务状态名称
     */
    private String getTaskStatusName(Integer taskStatus) {
        if (taskStatus == null) {
            return "未知";
        }
        return switch (taskStatus) {
            case 1 -> "待执行";
            case 2 -> "执行中";
            case 3 -> "已暂停";
            case 4 -> "已完成";
            case 5 -> "已失败";
            default -> "未知";
        };
    }

    /**
     * 获取升级策略名称
     */
    private String getUpgradeStrategyName(Integer upgradeStrategy) {
        if (upgradeStrategy == null) {
            return "未知";
        }
        return switch (upgradeStrategy) {
            case 1 -> "立即升级";
            case 2 -> "定时升级";
            case 3 -> "分批升级";
            default -> "未知";
        };
    }

    /**
     * 获取升级状态名称
     */
    private String getUpgradeStatusName(Integer upgradeStatus) {
        if (upgradeStatus == null) {
            return "未知";
        }
        return switch (upgradeStatus) {
            case 1 -> "待升级";
            case 2 -> "升级中";
            case 3 -> "升级成功";
            case 4 -> "升级失败";
            case 5 -> "已回滚";
            default -> "未知";
        };
    }

    /**
     * 格式化时长
     */
    private String formatDuration(long seconds) {
        if (seconds < 60) {
            return seconds + "秒";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return minutes + "分钟" + (remainingSeconds > 0 ? remainingSeconds + "秒" : "");
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return hours + "小时" + (minutes > 0 ? minutes + "分钟" : "");
        }
    }
}
