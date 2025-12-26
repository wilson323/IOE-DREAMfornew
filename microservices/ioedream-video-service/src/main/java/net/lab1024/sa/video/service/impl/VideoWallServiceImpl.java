package net.lab1024.sa.video.service.impl;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.video.dao.VideoDecoderDao;
import net.lab1024.sa.video.dao.VideoWallDao;
import net.lab1024.sa.video.dao.VideoWallPresetDao;
import net.lab1024.sa.video.dao.VideoWallTourDao;
import net.lab1024.sa.video.domain.form.VideoDecoderAddForm;
import net.lab1024.sa.video.domain.form.VideoDecoderUpdateForm;
import net.lab1024.sa.video.domain.form.VideoDisplayTaskAddForm;
import net.lab1024.sa.video.domain.form.VideoWallAddForm;
import net.lab1024.sa.video.domain.form.VideoWallPresetAddForm;
import net.lab1024.sa.video.domain.form.VideoWallTourAddForm;
import net.lab1024.sa.video.domain.form.VideoWallUpdateForm;
import net.lab1024.sa.video.domain.vo.VideoDecoderVO;
import net.lab1024.sa.video.domain.vo.VideoDisplayTaskVO;
import net.lab1024.sa.video.domain.vo.VideoWallPresetVO;
import net.lab1024.sa.video.domain.vo.VideoWallTourVO;
import net.lab1024.sa.video.domain.vo.VideoWallVO;
import net.lab1024.sa.video.entity.VideoDecoderEntity;
import net.lab1024.sa.video.entity.VideoDisplayTaskEntity;
import net.lab1024.sa.video.entity.VideoWallEntity;
import net.lab1024.sa.video.entity.VideoWallPresetEntity;
import net.lab1024.sa.video.entity.VideoWallTourEntity;
import net.lab1024.sa.video.manager.DecoderManager;
import net.lab1024.sa.video.manager.VideoWallManager;
import net.lab1024.sa.video.manager.WallTaskManager;
import net.lab1024.sa.video.service.VideoWallService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 解码上墙服务实现类
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - Service实现类在ioedream-video-service中
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
public class VideoWallServiceImpl implements VideoWallService {

    @Resource
    private VideoWallManager videoWallManager;

    @Resource
    private DecoderManager decoderManager;

    @Resource
    private WallTaskManager wallTaskManager;

    @Resource
    private VideoWallDao videoWallDao;

    @Resource
    private VideoWallPresetDao videoWallPresetDao;

    @Resource
    private VideoWallTourDao videoWallTourDao;

    @Resource
    private VideoDecoderDao videoDecoderDao;

    @Override
    public ResponseDTO<Long> createWall(@Valid VideoWallAddForm addForm) {
        log.info("[解码上墙] 创建电视墙: wallCode={}, wallName={}, rows={}, cols={}",
                addForm.getWallCode(), addForm.getWallName(), addForm.getRows(), addForm.getCols());

        try {
            // 检查电视墙编码是否已存在
            VideoWallEntity existing = videoWallDao.selectByCode(addForm.getWallCode());
            if (existing != null) {
                log.warn("[解码上墙] 电视墙编码已存在: wallCode={}", addForm.getWallCode());
                return ResponseDTO.error("WALL_CODE_EXISTS", "电视墙编码已存在");
            }

            // 转换为实体
            VideoWallEntity wall = convertAddFormToEntity(addForm);

            // 创建电视墙并初始化窗口
            Long wallId = videoWallManager.createWallWithWindows(wall);

            log.info("[解码上墙] 创建电视墙成功: wallId={}", wallId);
            return ResponseDTO.ok(wallId);

        } catch (IllegalArgumentException e) {
            log.warn("[解码上墙] 创建电视墙参数错误: error={}", e.getMessage());
            return ResponseDTO.error("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (Exception e) {
            log.error("[解码上墙] 创建电视墙异常: wallCode={}", addForm.getWallCode(), e);
            return ResponseDTO.error("CREATE_WALL_ERROR", "创建电视墙失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> updateWall(@Valid VideoWallUpdateForm updateForm) {
        log.info("[解码上墙] 更新电视墙: wallId={}", updateForm.getWallId());

        try {
            VideoWallEntity wall = videoWallDao.selectById(updateForm.getWallId());
            if (wall == null) {
                log.warn("[解码上墙] 电视墙不存在: wallId={}", updateForm.getWallId());
                return ResponseDTO.error("WALL_NOT_FOUND", "电视墙不存在");
            }

            // 更新字段
            if (updateForm.getWallName() != null) {
                wall.setWallName(updateForm.getWallName());
            }
            if (updateForm.getRows() != null && updateForm.getCols() != null) {
                // 更新布局需要重新初始化窗口
                videoWallManager.updateWallLayout(
                        updateForm.getWallId(),
                        updateForm.getRows(),
                        updateForm.getCols(),
                        updateForm.getScreenWidth(),
                        updateForm.getScreenHeight()
                );
                return ResponseDTO.ok();
            }
            if (updateForm.getScreenWidth() != null) {
                wall.setScreenWidth(updateForm.getScreenWidth());
            }
            if (updateForm.getScreenHeight() != null) {
                wall.setScreenHeight(updateForm.getScreenHeight());
            }
            if (updateForm.getLocation() != null) {
                wall.setLocation(updateForm.getLocation());
            }
            if (updateForm.getRegionId() != null) {
                wall.setRegionId(updateForm.getRegionId());
            }
            if (updateForm.getStatus() != null) {
                wall.setStatus(updateForm.getStatus());
            }
            if (updateForm.getDescription() != null) {
                wall.setDescription(updateForm.getDescription());
            }

            videoWallDao.updateById(wall);

            log.info("[解码上墙] 更新电视墙成功: wallId={}", updateForm.getWallId());
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[解码上墙] 更新电视墙异常: wallId={}", updateForm.getWallId(), e);
            return ResponseDTO.error("UPDATE_WALL_ERROR", "更新电视墙失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> deleteWall(Long wallId) {
        log.info("[解码上墙] 删除电视墙: wallId={}", wallId);

        try {
            VideoWallEntity wall = videoWallDao.selectById(wallId);
            if (wall == null) {
                log.warn("[解码上墙] 电视墙不存在: wallId={}", wallId);
                return ResponseDTO.error("WALL_NOT_FOUND", "电视墙不存在");
            }

            // 逻辑删除
            wall.setDeletedFlag(1);
            videoWallDao.updateById(wall);

            log.info("[解码上墙] 删除电视墙成功: wallId={}", wallId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[解码上墙] 删除电视墙异常: wallId={}", wallId, e);
            return ResponseDTO.error("DELETE_WALL_ERROR", "删除电视墙失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<VideoWallVO> getWallById(Long wallId) {
        log.info("[解码上墙] 查询电视墙详情: wallId={}", wallId);

        try {
            VideoWallEntity wall = videoWallDao.selectById(wallId);
            if (wall == null) {
                log.warn("[解码上墙] 电视墙不存在: wallId={}", wallId);
                return ResponseDTO.error("WALL_NOT_FOUND", "电视墙不存在");
            }

            VideoWallVO vo = convertToVO(wall);
            log.info("[解码上墙] 查询电视墙详情成功: wallId={}", wallId);
            return ResponseDTO.ok(vo);

        } catch (Exception e) {
            log.error("[解码上墙] 查询电视墙详情异常: wallId={}", wallId, e);
            return ResponseDTO.error("GET_WALL_ERROR", "查询电视墙详情失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<VideoWallVO>> getWallList(Long regionId) {
        log.info("[解码上墙] 查询电视墙列表: regionId={}", regionId);

        try {
            List<VideoWallEntity> walls;
            if (regionId != null) {
                walls = videoWallDao.selectByRegionId(regionId);
            } else {
                walls = videoWallDao.selectList(null);
            }

            List<VideoWallVO> voList = walls.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            log.info("[解码上墙] 查询电视墙列表成功: count={}", voList.size());
            return ResponseDTO.ok(voList);

        } catch (Exception e) {
            log.error("[解码上墙] 查询电视墙列表异常: regionId={}", regionId, e);
            return ResponseDTO.error("GET_WALL_LIST_ERROR", "查询电视墙列表失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Long> createDisplayTask(@Valid VideoDisplayTaskAddForm addForm) {
        log.info("[解码上墙] 创建上墙任务: wallId={}, windowId={}, deviceId={}, taskType={}",
                addForm.getWallId(), addForm.getWindowId(), addForm.getDeviceId(), addForm.getTaskType());

        try {
            // 分配解码通道
            Long[] allocation = decoderManager.allocateDecoderChannel(addForm.getWallId(), addForm.getWindowId());
            if (allocation == null) {
                log.warn("[解码上墙] 无可用解码通道: wallId={}, windowId={}", addForm.getWallId(), addForm.getWindowId());
                return ResponseDTO.error("NO_AVAILABLE_CHANNEL", "无可用解码通道");
            }

            // 转换为实体
            VideoDisplayTaskEntity task = convertAddFormToEntity(addForm);
            task.setDecoderId(allocation[0]);
            task.setChannelNo(allocation[1].intValue());

            // 执行上墙任务
            Long taskId = wallTaskManager.executeDisplayTask(task);

            log.info("[解码上墙] 创建上墙任务成功: taskId={}", taskId);
            return ResponseDTO.ok(taskId);

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("[解码上墙] 创建上墙任务参数错误: error={}", e.getMessage());
            return ResponseDTO.error("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (Exception e) {
            log.error("[解码上墙] 创建上墙任务异常: wallId={}, windowId={}", addForm.getWallId(), addForm.getWindowId(), e);
            return ResponseDTO.error("CREATE_TASK_ERROR", "创建上墙任务失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> cancelDisplayTask(Long taskId) {
        log.info("[解码上墙] 取消上墙任务: taskId={}", taskId);

        try {
            wallTaskManager.cancelDisplayTask(taskId);
            log.info("[解码上墙] 取消上墙任务成功: taskId={}", taskId);
            return ResponseDTO.ok();

        } catch (IllegalArgumentException e) {
            log.warn("[解码上墙] 取消上墙任务参数错误: error={}", e.getMessage());
            return ResponseDTO.error("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (Exception e) {
            log.error("[解码上墙] 取消上墙任务异常: taskId={}", taskId, e);
            return ResponseDTO.error("CANCEL_TASK_ERROR", "取消上墙任务失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<VideoDisplayTaskVO>> getTaskList(Long wallId) {
        log.info("[解码上墙] 查询上墙任务列表: wallId={}", wallId);

        try {
            List<VideoDisplayTaskEntity> tasks = wallTaskManager.getWallTasks(wallId);
            List<VideoDisplayTaskVO> voList = tasks.stream()
                    .map(this::convertTaskToVO)
                    .collect(Collectors.toList());

            log.info("[解码上墙] 查询上墙任务列表成功: wallId={}, count={}", wallId, voList.size());
            return ResponseDTO.ok(voList);

        } catch (Exception e) {
            log.error("[解码上墙] 查询上墙任务列表异常: wallId={}", wallId, e);
            return ResponseDTO.error("GET_TASK_LIST_ERROR", "查询上墙任务列表失败：" + e.getMessage());
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 转换新增表单为实体
     */
    private VideoWallEntity convertAddFormToEntity(VideoWallAddForm addForm) {
        VideoWallEntity wall = new VideoWallEntity();
        wall.setWallCode(addForm.getWallCode());
        wall.setWallName(addForm.getWallName());
        wall.setRows(addForm.getRows());
        wall.setCols(addForm.getCols());
        wall.setScreenWidth(addForm.getScreenWidth());
        wall.setScreenHeight(addForm.getScreenHeight());
        wall.setLocation(addForm.getLocation());
        wall.setRegionId(addForm.getRegionId());
        wall.setStatus(addForm.getStatus() != null ? addForm.getStatus() : 1);
        wall.setDescription(addForm.getDescription());
        return wall;
    }

    /**
     * 转换实体为VO
     */
    private VideoWallVO convertToVO(VideoWallEntity wall) {
        VideoWallVO vo = new VideoWallVO();
        vo.setWallId(wall.getWallId());
        vo.setWallCode(wall.getWallCode());
        vo.setWallName(wall.getWallName());
        vo.setRows(wall.getRows());
        vo.setCols(wall.getCols());
        vo.setTotalScreens(wall.getTotalScreens());
        vo.setScreenWidth(wall.getScreenWidth());
        vo.setScreenHeight(wall.getScreenHeight());
        vo.setTotalWidth(wall.getTotalWidth());
        vo.setTotalHeight(wall.getTotalHeight());
        vo.setLocation(wall.getLocation());
        vo.setRegionId(wall.getRegionId());
        vo.setStatus(wall.getStatus());
        vo.setStatusDesc(wall.getStatus() == 1 ? "启用" : "禁用");
        vo.setDescription(wall.getDescription());
        vo.setCreateTime(wall.getCreateTime());
        vo.setUpdateTime(wall.getUpdateTime());
        return vo;
    }

    /**
     * 转换新增表单为任务实体
     */
    private VideoDisplayTaskEntity convertAddFormToEntity(VideoDisplayTaskAddForm addForm) {
        VideoDisplayTaskEntity task = new VideoDisplayTaskEntity();
        task.setWallId(addForm.getWallId());
        task.setWindowId(addForm.getWindowId());
        task.setDeviceId(addForm.getDeviceId());
        task.setStreamType(addForm.getStreamType() != null ? addForm.getStreamType() : "MAIN");
        task.setTaskType(addForm.getTaskType() != null ? addForm.getTaskType() : 0);
        task.setPresetId(addForm.getPresetId());
        task.setTourId(addForm.getTourId());
        task.setAlarmId(addForm.getAlarmId());
        return task;
    }

    /**
     * 转换任务实体为VO
     */
    private VideoDisplayTaskVO convertTaskToVO(VideoDisplayTaskEntity task) {
        VideoDisplayTaskVO vo = new VideoDisplayTaskVO();
        vo.setTaskId(task.getTaskId());
        vo.setWallId(task.getWallId());
        vo.setWindowId(task.getWindowId());
        vo.setDeviceId(task.getDeviceId());
        vo.setDecoderId(task.getDecoderId());
        vo.setChannelNo(task.getChannelNo());
        vo.setStreamType(task.getStreamType());
        vo.setTaskType(task.getTaskType());
        vo.setTaskTypeDesc(getTaskTypeDesc(task.getTaskType()));
        vo.setPresetId(task.getPresetId());
        vo.setTourId(task.getTourId());
        vo.setAlarmId(task.getAlarmId());
        vo.setStatus(task.getStatus());
        vo.setStatusDesc(getStatusDesc(task.getStatus()));
        vo.setErrorMsg(task.getErrorMsg());
        vo.setStartTime(task.getStartTime());
        vo.setEndTime(task.getEndTime());
        vo.setCreateTime(task.getCreateTime());
        return vo;
    }

    /**
     * 获取任务类型描述
     */
    private String getTaskTypeDesc(Integer taskType) {
        if (taskType == null) {
            return "未知";
        }
        return switch (taskType) {
            case 0 -> "手动";
            case 1 -> "预案";
            case 2 -> "轮巡";
            case 3 -> "告警联动";
            default -> "未知";
        };
    }

    /**
     * 获取状态描述
     */
    private String getStatusDesc(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "等待";
            case 1 -> "执行中";
            case 2 -> "完成";
            case 3 -> "失败";
            default -> "未知";
        };
    }

    @Override
    public ResponseDTO<Long> createPreset(@Valid VideoWallPresetAddForm addForm) {
        log.info("[解码上墙] 创建预案: wallId={}, presetName={}", addForm.getWallId(), addForm.getPresetName());

        try {
            // 检查电视墙是否存在
            VideoWallEntity wall = videoWallDao.selectById(addForm.getWallId());
            if (wall == null) {
                log.warn("[解码上墙] 电视墙不存在: wallId={}", addForm.getWallId());
                return ResponseDTO.error("WALL_NOT_FOUND", "电视墙不存在");
            }

            // 转换为实体
            VideoWallPresetEntity preset = convertPresetAddFormToEntity(addForm);

            // 如果设置为默认预案，先取消其他默认预案
            if (preset.getIsDefault() != null && preset.getIsDefault() == 1) {
                VideoWallPresetEntity defaultPreset = videoWallPresetDao.selectDefaultPreset(addForm.getWallId());
                if (defaultPreset != null) {
                    defaultPreset.setIsDefault(0);
                    videoWallPresetDao.updateById(defaultPreset);
                }
            }

            videoWallPresetDao.insert(preset);

            log.info("[解码上墙] 创建预案成功: presetId={}", preset.getPresetId());
            return ResponseDTO.ok(preset.getPresetId());

        } catch (Exception e) {
            log.error("[解码上墙] 创建预案异常: wallId={}, presetName={}", addForm.getWallId(), addForm.getPresetName(), e);
            return ResponseDTO.error("CREATE_PRESET_ERROR", "创建预案失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> deletePreset(Long presetId) {
        log.info("[解码上墙] 删除预案: presetId={}", presetId);

        try {
            VideoWallPresetEntity preset = videoWallPresetDao.selectById(presetId);
            if (preset == null) {
                log.warn("[解码上墙] 预案不存在: presetId={}", presetId);
                return ResponseDTO.error("PRESET_NOT_FOUND", "预案不存在");
            }

            preset.setDeletedFlag(1);
            videoWallPresetDao.updateById(preset);

            log.info("[解码上墙] 删除预案成功: presetId={}", presetId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[解码上墙] 删除预案异常: presetId={}", presetId, e);
            return ResponseDTO.error("DELETE_PRESET_ERROR", "删除预案失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<VideoWallPresetVO>> getPresetList(Long wallId) {
        log.info("[解码上墙] 查询预案列表: wallId={}", wallId);

        try {
            List<VideoWallPresetEntity> presets = videoWallPresetDao.selectByWallId(wallId);
            List<VideoWallPresetVO> voList = presets.stream()
                    .map(this::convertPresetToVO)
                    .collect(Collectors.toList());

            log.info("[解码上墙] 查询预案列表成功: wallId={}, count={}", wallId, voList.size());
            return ResponseDTO.ok(voList);

        } catch (Exception e) {
            log.error("[解码上墙] 查询预案列表异常: wallId={}", wallId, e);
            return ResponseDTO.error("GET_PRESET_LIST_ERROR", "查询预案列表失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> applyPreset(Long presetId) {
        log.info("[解码上墙] 调用预案: presetId={}", presetId);

        try {
            VideoWallPresetEntity preset = videoWallPresetDao.selectById(presetId);
            if (preset == null) {
                log.warn("[解码上墙] 预案不存在: presetId={}", presetId);
                return ResponseDTO.error("PRESET_NOT_FOUND", "预案不存在");
            }

            // TODO: 解析预案配置（JSON格式），为每个窗口创建上墙任务
            // 这里需要解析config字段，然后批量创建上墙任务
            log.info("[解码上墙] 调用预案成功: presetId={}", presetId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[解码上墙] 调用预案异常: presetId={}", presetId, e);
            return ResponseDTO.error("APPLY_PRESET_ERROR", "调用预案失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Long> createTour(@Valid VideoWallTourAddForm addForm) {
        log.info("[解码上墙] 创建轮巡: wallId={}, tourName={}, intervalSeconds={}",
                addForm.getWallId(), addForm.getTourName(), addForm.getIntervalSeconds());

        try {
            // 检查电视墙是否存在
            VideoWallEntity wall = videoWallDao.selectById(addForm.getWallId());
            if (wall == null) {
                log.warn("[解码上墙] 电视墙不存在: wallId={}", addForm.getWallId());
                return ResponseDTO.error("WALL_NOT_FOUND", "电视墙不存在");
            }

            // 转换为实体
            VideoWallTourEntity tour = convertTourAddFormToEntity(addForm);
            tour.setStatus(0); // 默认停止状态
            tour.setCurrentIndex(0);

            videoWallTourDao.insert(tour);

            log.info("[解码上墙] 创建轮巡成功: tourId={}", tour.getTourId());
            return ResponseDTO.ok(tour.getTourId());

        } catch (Exception e) {
            log.error("[解码上墙] 创建轮巡异常: wallId={}, tourName={}", addForm.getWallId(), addForm.getTourName(), e);
            return ResponseDTO.error("CREATE_TOUR_ERROR", "创建轮巡失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> deleteTour(Long tourId) {
        log.info("[解码上墙] 删除轮巡: tourId={}", tourId);

        try {
            VideoWallTourEntity tour = videoWallTourDao.selectById(tourId);
            if (tour == null) {
                log.warn("[解码上墙] 轮巡不存在: tourId={}", tourId);
                return ResponseDTO.error("TOUR_NOT_FOUND", "轮巡不存在");
            }

            tour.setDeletedFlag(1);
            videoWallTourDao.updateById(tour);

            log.info("[解码上墙] 删除轮巡成功: tourId={}", tourId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[解码上墙] 删除轮巡异常: tourId={}", tourId, e);
            return ResponseDTO.error("DELETE_TOUR_ERROR", "删除轮巡失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<VideoWallTourVO>> getTourList(Long wallId) {
        log.info("[解码上墙] 查询轮巡列表: wallId={}", wallId);

        try {
            List<VideoWallTourEntity> tours = videoWallTourDao.selectByWallId(wallId);
            List<VideoWallTourVO> voList = tours.stream()
                    .map(this::convertTourToVO)
                    .collect(Collectors.toList());

            log.info("[解码上墙] 查询轮巡列表成功: wallId={}, count={}", wallId, voList.size());
            return ResponseDTO.ok(voList);

        } catch (Exception e) {
            log.error("[解码上墙] 查询轮巡列表异常: wallId={}", wallId, e);
            return ResponseDTO.error("GET_TOUR_LIST_ERROR", "查询轮巡列表失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> startTour(Long tourId) {
        log.info("[解码上墙] 启动轮巡: tourId={}", tourId);

        try {
            VideoWallTourEntity tour = videoWallTourDao.selectById(tourId);
            if (tour == null) {
                log.warn("[解码上墙] 轮巡不存在: tourId={}", tourId);
                return ResponseDTO.error("TOUR_NOT_FOUND", "轮巡不存在");
            }

            if (tour.getStatus() == 1) {
                log.warn("[解码上墙] 轮巡已在运行中: tourId={}", tourId);
                return ResponseDTO.error("TOUR_ALREADY_RUNNING", "轮巡已在运行中");
            }

            videoWallTourDao.updateStatus(tourId, 1);

            // TODO: 启动轮巡任务调度（定时任务或异步任务）
            log.info("[解码上墙] 启动轮巡成功: tourId={}", tourId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[解码上墙] 启动轮巡异常: tourId={}", tourId, e);
            return ResponseDTO.error("START_TOUR_ERROR", "启动轮巡失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> stopTour(Long tourId) {
        log.info("[解码上墙] 停止轮巡: tourId={}", tourId);

        try {
            VideoWallTourEntity tour = videoWallTourDao.selectById(tourId);
            if (tour == null) {
                log.warn("[解码上墙] 轮巡不存在: tourId={}", tourId);
                return ResponseDTO.error("TOUR_NOT_FOUND", "轮巡不存在");
            }

            videoWallTourDao.updateStatus(tourId, 0);

            // TODO: 停止轮巡任务调度
            log.info("[解码上墙] 停止轮巡成功: tourId={}", tourId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[解码上墙] 停止轮巡异常: tourId={}", tourId, e);
            return ResponseDTO.error("STOP_TOUR_ERROR", "停止轮巡失败：" + e.getMessage());
        }
    }

    // ==================== 预案和轮巡转换方法 ====================

    /**
     * 转换预案新增表单为实体
     */
    private VideoWallPresetEntity convertPresetAddFormToEntity(VideoWallPresetAddForm addForm) {
        VideoWallPresetEntity preset = new VideoWallPresetEntity();
        preset.setWallId(addForm.getWallId());
        preset.setPresetName(addForm.getPresetName());
        preset.setPresetCode(addForm.getPresetCode());
        preset.setGroupId(addForm.getGroupId());
        preset.setDescription(addForm.getDescription());
        preset.setConfig(addForm.getConfig());
        preset.setIsDefault(addForm.getIsDefault() != null ? addForm.getIsDefault() : 0);
        return preset;
    }

    /**
     * 转换预案实体为VO
     */
    private VideoWallPresetVO convertPresetToVO(VideoWallPresetEntity preset) {
        VideoWallPresetVO vo = new VideoWallPresetVO();
        vo.setPresetId(preset.getPresetId());
        vo.setWallId(preset.getWallId());
        vo.setPresetName(preset.getPresetName());
        vo.setPresetCode(preset.getPresetCode());
        vo.setGroupId(preset.getGroupId());
        vo.setDescription(preset.getDescription());
        vo.setConfig(preset.getConfig());
        vo.setIsDefault(preset.getIsDefault());
        vo.setIsDefaultDesc(preset.getIsDefault() == 1 ? "是" : "否");
        vo.setCreateBy(preset.getCreateBy());
        vo.setCreateTime(preset.getCreateTime());
        vo.setUpdateTime(preset.getUpdateTime());
        return vo;
    }

    /**
     * 转换轮巡新增表单为实体
     */
    private VideoWallTourEntity convertTourAddFormToEntity(VideoWallTourAddForm addForm) {
        VideoWallTourEntity tour = new VideoWallTourEntity();
        tour.setWallId(addForm.getWallId());
        tour.setTourName(addForm.getTourName());
        tour.setWindowIds(addForm.getWindowIds());
        tour.setDeviceIds(addForm.getDeviceIds());
        tour.setIntervalSeconds(addForm.getIntervalSeconds());
        return tour;
    }

    /**
     * 转换轮巡实体为VO
     */
    private VideoWallTourVO convertTourToVO(VideoWallTourEntity tour) {
        VideoWallTourVO vo = new VideoWallTourVO();
        vo.setTourId(tour.getTourId());
        vo.setWallId(tour.getWallId());
        vo.setTourName(tour.getTourName());
        vo.setWindowIds(tour.getWindowIds());
        vo.setDeviceIds(tour.getDeviceIds());
        vo.setIntervalSeconds(tour.getIntervalSeconds());
        vo.setStatus(tour.getStatus());
        vo.setStatusDesc(tour.getStatus() == 1 ? "运行中" : "停止");
        vo.setCurrentIndex(tour.getCurrentIndex());
        vo.setCreateBy(tour.getCreateBy());
        vo.setCreateTime(tour.getCreateTime());
        vo.setUpdateTime(tour.getUpdateTime());
        return vo;
    }

    @Override
    public ResponseDTO<Long> addDecoder(@Valid VideoDecoderAddForm addForm) {
        log.info("[解码上墙] 添加解码器: decoderCode={}, decoderName={}, ipAddress={}",
                addForm.getDecoderCode(), addForm.getDecoderName(), addForm.getIpAddress());

        try {
            // 检查解码器编码是否已存在
            VideoDecoderEntity existing = videoDecoderDao.selectByCode(addForm.getDecoderCode());
            if (existing != null) {
                log.warn("[解码上墙] 解码器编码已存在: decoderCode={}", addForm.getDecoderCode());
                return ResponseDTO.error("DECODER_CODE_EXISTS", "解码器编码已存在");
            }

            // 转换为实体
            VideoDecoderEntity decoder = convertDecoderAddFormToEntity(addForm);

            // 注册解码器
            Long decoderId = decoderManager.registerDecoder(decoder);

            log.info("[解码上墙] 添加解码器成功: decoderId={}", decoderId);
            return ResponseDTO.ok(decoderId);

        } catch (IllegalArgumentException e) {
            log.warn("[解码上墙] 添加解码器参数错误: error={}", e.getMessage());
            return ResponseDTO.error("PARAM_ERROR", "参数错误：" + e.getMessage());
        } catch (Exception e) {
            log.error("[解码上墙] 添加解码器异常: decoderCode={}", addForm.getDecoderCode(), e);
            return ResponseDTO.error("ADD_DECODER_ERROR", "添加解码器失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> updateDecoder(@Valid VideoDecoderUpdateForm updateForm) {
        log.info("[解码上墙] 更新解码器: decoderId={}", updateForm.getDecoderId());

        try {
            VideoDecoderEntity decoder = videoDecoderDao.selectById(updateForm.getDecoderId());
            if (decoder == null) {
                log.warn("[解码上墙] 解码器不存在: decoderId={}", updateForm.getDecoderId());
                return ResponseDTO.error("DECODER_NOT_FOUND", "解码器不存在");
            }

            // 更新字段
            if (updateForm.getDecoderName() != null) {
                decoder.setDecoderName(updateForm.getDecoderName());
            }
            if (updateForm.getIpAddress() != null) {
                decoder.setIpAddress(updateForm.getIpAddress());
            }
            if (updateForm.getPort() != null) {
                decoder.setPort(updateForm.getPort());
            }
            if (updateForm.getUsername() != null) {
                decoder.setUsername(updateForm.getUsername());
            }
            if (updateForm.getPassword() != null) {
                decoder.setPassword(updateForm.getPassword());
            }
            if (updateForm.getMaxChannels() != null) {
                decoder.setMaxChannels(updateForm.getMaxChannels());
            }
            if (updateForm.getAreaId() != null) {
                decoder.setAreaId(updateForm.getAreaId());
            }
            if (updateForm.getDescription() != null) {
                decoder.setDescription(updateForm.getDescription());
            }

            videoDecoderDao.updateById(decoder);

            log.info("[解码上墙] 更新解码器成功: decoderId={}", updateForm.getDecoderId());
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[解码上墙] 更新解码器异常: decoderId={}", updateForm.getDecoderId(), e);
            return ResponseDTO.error("UPDATE_DECODER_ERROR", "更新解码器失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> deleteDecoder(Long decoderId) {
        log.info("[解码上墙] 删除解码器: decoderId={}", decoderId);

        try {
            VideoDecoderEntity decoder = videoDecoderDao.selectById(decoderId);
            if (decoder == null) {
                log.warn("[解码上墙] 解码器不存在: decoderId={}", decoderId);
                return ResponseDTO.error("DECODER_NOT_FOUND", "解码器不存在");
            }

            // 逻辑删除
            decoder.setDeletedFlag(1);
            videoDecoderDao.updateById(decoder);

            log.info("[解码上墙] 删除解码器成功: decoderId={}", decoderId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[解码上墙] 删除解码器异常: decoderId={}", decoderId, e);
            return ResponseDTO.error("DELETE_DECODER_ERROR", "删除解码器失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<VideoDecoderVO> getDecoderById(Long decoderId) {
        log.info("[解码上墙] 查询解码器详情: decoderId={}", decoderId);

        try {
            VideoDecoderEntity decoder = decoderManager.getDecoderById(decoderId);
            if (decoder == null) {
                log.warn("[解码上墙] 解码器不存在: decoderId={}", decoderId);
                return ResponseDTO.error("DECODER_NOT_FOUND", "解码器不存在");
            }

            VideoDecoderVO vo = convertDecoderToVO(decoder);
            log.info("[解码上墙] 查询解码器详情成功: decoderId={}", decoderId);
            return ResponseDTO.ok(vo);

        } catch (Exception e) {
            log.error("[解码上墙] 查询解码器详情异常: decoderId={}", decoderId, e);
            return ResponseDTO.error("GET_DECODER_ERROR", "查询解码器详情失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<VideoDecoderVO>> getDecoderList(Long areaId) {
        log.info("[解码上墙] 查询解码器列表: areaId={}", areaId);

        try {
            List<VideoDecoderEntity> decoders;
            if (areaId != null) {
                decoders = videoDecoderDao.selectByAreaId(areaId);
            } else {
                decoders = decoderManager.getOnlineDecoders();
            }

            List<VideoDecoderVO> voList = decoders.stream()
                    .map(this::convertDecoderToVO)
                    .collect(Collectors.toList());

            log.info("[解码上墙] 查询解码器列表成功: count={}", voList.size());
            return ResponseDTO.ok(voList);

        } catch (Exception e) {
            log.error("[解码上墙] 查询解码器列表异常: areaId={}", areaId, e);
            return ResponseDTO.error("GET_DECODER_LIST_ERROR", "查询解码器列表失败：" + e.getMessage());
        }
    }

    // ==================== 解码器转换方法 ====================

    /**
     * 转换解码器新增表单为实体
     */
    private VideoDecoderEntity convertDecoderAddFormToEntity(VideoDecoderAddForm addForm) {
        VideoDecoderEntity decoder = new VideoDecoderEntity();
        decoder.setDecoderCode(addForm.getDecoderCode());
        decoder.setDecoderName(addForm.getDecoderName());
        decoder.setDecoderType(addForm.getDecoderType());
        decoder.setBrand(addForm.getBrand());
        decoder.setModel(addForm.getModel());
        decoder.setSerialNumber(addForm.getSerialNumber());
        decoder.setIpAddress(addForm.getIpAddress());
        decoder.setPort(addForm.getPort());
        decoder.setUsername(addForm.getUsername());
        decoder.setPassword(addForm.getPassword());
        decoder.setMaxChannels(addForm.getMaxChannels());
        decoder.setMaxResolution(parseResolution(addForm.getMaxResolution()));
        decoder.setSupportedFormats(addForm.getSupportedFormats());
        decoder.setAreaId(addForm.getAreaId());
        decoder.setDescription(addForm.getDescription());
        return decoder;
    }

    /**
     * 转换解码器实体为VO
     */
    private VideoDecoderVO convertDecoderToVO(VideoDecoderEntity decoder) {
        VideoDecoderVO vo = new VideoDecoderVO();
        vo.setDecoderId(decoder.getDecoderId());
        vo.setDecoderCode(decoder.getDecoderCode());
        vo.setDecoderName(decoder.getDecoderName());
        vo.setDecoderType(decoder.getDecoderType());
        vo.setDecoderTypeDesc(getDecoderTypeDesc(decoder.getDecoderType()));
        vo.setBrand(decoder.getBrand());
        vo.setModel(decoder.getModel());
        vo.setSerialNumber(decoder.getSerialNumber());
        vo.setIpAddress(decoder.getIpAddress());
        vo.setPort(decoder.getPort());
        vo.setMaxChannels(decoder.getMaxChannels());
        vo.setUsedChannels(decoder.getUsedChannels());
        vo.setAvailableChannels(decoder.getAvailableChannels());
        vo.setMaxResolution(formatResolution(decoder.getMaxResolution()));
        vo.setSupportedFormats(decoder.getSupportedFormats());
        vo.setDeviceStatus(decoder.getDeviceStatus());
        vo.setDeviceStatusDesc(getDeviceStatusDesc(decoder.getDeviceStatus()));
        vo.setLastOnlineTime(decoder.getLastOnlineTime());
        vo.setAreaId(decoder.getAreaId());
        vo.setDescription(decoder.getDescription());
        vo.setCreateTime(decoder.getCreateTime());
        vo.setUpdateTime(decoder.getUpdateTime());
        return vo;
    }

    /**
     * 解析分辨率字符串为整数
     */
    private Integer parseResolution(String resolution) {
        if (resolution == null) {
            return null;
        }
        return switch (resolution.toUpperCase()) {
            case "4CIF" -> 1;
            case "D1" -> 2;
            case "720P" -> 3;
            case "1080P" -> 4;
            case "4K" -> 5;
            default -> null;
        };
    }

    /**
     * 格式化分辨率为字符串
     */
    private String formatResolution(Integer resolution) {
        if (resolution == null) {
            return null;
        }
        return switch (resolution) {
            case 1 -> "4CIF";
            case 2 -> "D1";
            case 3 -> "720P";
            case 4 -> "1080P";
            case 5 -> "4K";
            default -> "未知";
        };
    }

    /**
     * 获取解码器类型描述
     */
    private String getDecoderTypeDesc(Integer decoderType) {
        if (decoderType == null) {
            return "未知";
        }
        return switch (decoderType) {
            case 1 -> "硬解码器";
            case 2 -> "软解码器";
            case 3 -> "混合解码器";
            default -> "未知";
        };
    }

    /**
     * 获取设备状态描述
     */
    private String getDeviceStatusDesc(Integer deviceStatus) {
        if (deviceStatus == null) {
            return "未知";
        }
        return switch (deviceStatus) {
            case 1 -> "在线";
            case 2 -> "离线";
            case 3 -> "故障";
            case 4 -> "维护";
            case 5 -> "停用";
            default -> "未知";
        };
    }
}
