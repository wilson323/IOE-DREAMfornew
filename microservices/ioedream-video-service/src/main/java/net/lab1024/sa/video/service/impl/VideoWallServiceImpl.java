package net.lab1024.sa.video.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.video.dao.VideoWallDao;
import net.lab1024.sa.video.domain.form.VideoDisplayTaskAddForm;
import net.lab1024.sa.video.domain.form.VideoWallAddForm;
import net.lab1024.sa.video.domain.form.VideoWallUpdateForm;
import net.lab1024.sa.video.domain.vo.VideoDisplayTaskVO;
import net.lab1024.sa.video.domain.vo.VideoWallVO;
import net.lab1024.sa.video.entity.VideoDisplayTaskEntity;
import net.lab1024.sa.video.entity.VideoWallEntity;
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
            wall.setDeletedFlag(true);
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
}
