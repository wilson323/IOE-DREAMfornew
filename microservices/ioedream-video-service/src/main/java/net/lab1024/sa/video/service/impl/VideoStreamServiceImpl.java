package net.lab1024.sa.video.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.openapi.domain.response.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.video.dao.VideoStreamDao;
import net.lab1024.sa.common.video.entity.VideoStreamEntity;
import net.lab1024.sa.common.video.manager.VideoStreamManager;
import net.lab1024.sa.video.domain.form.VideoStreamQueryForm;
import net.lab1024.sa.video.domain.form.VideoStreamStartForm;
import net.lab1024.sa.video.domain.vo.VideoStreamSessionVO;
import net.lab1024.sa.video.domain.vo.VideoStreamVO;
import net.lab1024.sa.video.service.VideoStreamService;

/**
 * 视频流服务实现类
 * <p>
 * 实现实时视频流管理相关业务功能
 * 严格遵循CLAUDE.md规范：
 * - Service实现类在ioedream-video-service中
 * - 使用@Resource注入依赖
 * - 使用@Transactional管理事务
 * - 严格遵循四层架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class VideoStreamServiceImpl implements VideoStreamService {

    @Resource
    private VideoStreamDao videoStreamDao;

    @Resource
    private VideoStreamManager videoStreamManager;

    @Override
    @Observed(name = "video.stream.query", contextualName = "video-stream-query")
    @Transactional(readOnly = true)
    public PageResult<VideoStreamVO> queryStreams(VideoStreamQueryForm queryForm) {
        log.info("[视频流] 分页查询流，pageNum={}, pageSize={}, keyword={}, deviceId={}, streamType={}",
                queryForm.getPageNum(), queryForm.getPageSize(), queryForm.getKeyword(),
                queryForm.getDeviceId(), queryForm.getStreamType());

        try {
            // 构建查询条件
            LambdaQueryWrapper<VideoStreamEntity> wrapper = new LambdaQueryWrapper<VideoStreamEntity>()
                    .eq(VideoStreamEntity::getDeletedFlag, 0);

            // 关键词搜索（流名称、设备名称）
            if (StringUtils.hasText(queryForm.getKeyword())) {
                wrapper.and(w -> w.like(VideoStreamEntity::getStreamName, queryForm.getKeyword())
                        .or()
                        .like(VideoStreamEntity::getDeviceName, queryForm.getKeyword()));
            }

            // 设备筛选
            if (queryForm.getDeviceId() != null) {
                wrapper.eq(VideoStreamEntity::getDeviceId, queryForm.getDeviceId());
            }

            // 设备编号筛选
            if (StringUtils.hasText(queryForm.getDeviceCode())) {
                wrapper.like(VideoStreamEntity::getDeviceCode, queryForm.getDeviceCode());
            }

            // 流类型筛选
            if (StringUtils.hasText(queryForm.getStreamType())) {
                wrapper.eq(VideoStreamEntity::getStreamType, queryForm.getStreamType());
            }

            // 流协议筛选
            if (queryForm.getProtocol() != null) {
                wrapper.eq(VideoStreamEntity::getProtocol, queryForm.getProtocol());
            }

            // 流状态筛选
            if (queryForm.getStreamStatus() != null) {
                wrapper.eq(VideoStreamEntity::getStreamStatus, queryForm.getStreamStatus());
            }

            // 视频质量筛选
            if (StringUtils.hasText(queryForm.getQuality())) {
                wrapper.eq(VideoStreamEntity::getQuality, queryForm.getQuality());
            }

            // 区域筛选
            if (queryForm.getAreaId() != null) {
                wrapper.eq(VideoStreamEntity::getAreaId, queryForm.getAreaId());
            }

            // 音频启用筛选
            if (queryForm.getAudioEnabled() != null) {
                wrapper.eq(VideoStreamEntity::getAudioEnabled, queryForm.getAudioEnabled());
            }

            // 录制状态筛选
            if (queryForm.getRecording() != null) {
                wrapper.eq(VideoStreamEntity::getRecording, queryForm.getRecording());
            }

            // 客户端IP筛选
            if (StringUtils.hasText(queryForm.getClientIp())) {
                wrapper.like(VideoStreamEntity::getClientIp, queryForm.getClientIp());
            }

            // 观看人数范围筛选
            if (queryForm.getMinViewerCount() != null) {
                wrapper.ge(VideoStreamEntity::getViewerCount, queryForm.getMinViewerCount());
            }
            if (queryForm.getMaxViewerCount() != null) {
                wrapper.le(VideoStreamEntity::getViewerCount, queryForm.getMaxViewerCount());
            }

            // 时间范围筛选
            if (StringUtils.hasText(queryForm.getStartTime())) {
                try {
                    LocalDateTime startTime = LocalDateTime.parse(queryForm.getStartTime(),
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    wrapper.ge(VideoStreamEntity::getCreateTime, startTime);
                } catch (Exception e) {
                    log.warn("[视频流] 开始时间格式错误: {}", queryForm.getStartTime());
                }
            }
            if (StringUtils.hasText(queryForm.getEndTime())) {
                try {
                    LocalDateTime endTime = LocalDateTime.parse(queryForm.getEndTime(),
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    wrapper.le(VideoStreamEntity::getCreateTime, endTime);
                } catch (Exception e) {
                    log.warn("[视频流] 结束时间格式错误: {}", queryForm.getEndTime());
                }
            }

            // 排序
            if (StringUtils.hasText(queryForm.getSortBy())) {
                if ("desc".equals(queryForm.getSortOrder())) {
                    wrapper.orderByDesc(VideoStreamEntity::getCreateTime);
                } else {
                    wrapper.orderByAsc(VideoStreamEntity::getCreateTime);
                }
            } else {
                wrapper.orderByDesc(VideoStreamEntity::getCreateTime);
            }

            // 分页查询
            Page<VideoStreamEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
            Page<VideoStreamEntity> pageResult = videoStreamDao.selectPage(page, wrapper);

            // 转换为VO列表
            List<VideoStreamVO> voList = pageResult.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            // 构建分页结果
            PageResult<VideoStreamVO> result = PageResult.of(
                    voList,
                    pageResult.getTotal(),
                    queryForm.getPageNum(),
                    queryForm.getPageSize()
            );

            log.info("[视频流] 分页查询流成功，总数={}", pageResult.getTotal());
            return result;

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[视频流] 分页查询流参数错误: error={}", e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[视频流] 分页查询流业务异常: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[视频流] 分页查询流系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[视频流] 分页查询流未知异常: error={}", e.getMessage(), e);
            throw new SystemException("QUERY_STREAMS_ERROR", "查询视频流失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<VideoStreamVO> getStreamDetail(Long streamId) {
        log.info("[视频流] 查询流详情，streamId={}", streamId);

        try {
            if (streamId == null) {
                return ResponseDTO.error("PARAM_ERROR", "流ID不能为空");
            }

            VideoStreamEntity stream = videoStreamDao.selectById(streamId);
            if (stream == null) {
                return ResponseDTO.error("STREAM_NOT_FOUND", "视频流不存在");
            }

            if (stream.getDeletedFlag() == 1) {
                return ResponseDTO.error("STREAM_DELETED", "视频流已删除");
            }

            VideoStreamVO vo = convertToVO(stream);
            log.info("[视频流] 查询流详情成功，streamId={}", streamId);
            return ResponseDTO.ok(vo);

        } catch (ParamException e) {
            log.warn("[视频流] 查询流详情参数错误: streamId={}, error={}", streamId, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[视频流] 查询流详情业务异常: streamId={}, code={}, message={}", streamId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[视频流] 查询流详情系统异常: streamId={}, code={}, message={}", streamId, e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[视频流] 查询流详情未知异常: streamId={}", streamId, e);
            throw new SystemException("GET_STREAM_DETAIL_ERROR", "查询视频流详情失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "video.stream.start", contextualName = "video-stream-start")
    public ResponseDTO<VideoStreamSessionVO> startStream(@Valid VideoStreamStartForm startForm) {
        log.info("[视频流] 启动流，deviceId={}, channelId={}, streamType={}, protocol={}",
                startForm.getDeviceId(), startForm.getChannelId(),
                startForm.getStreamType(), startForm.getProtocol());

        try {
            // 验证设备是否存在
            // TODO: 调用设备服务验证设备状态
            // DeviceEntity device = deviceService.getById(startForm.getDeviceId());
            // if (device == null || device.getDeviceStatus() != 1) {
            //     return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在或未在线");
            // }

            // 创建流记录
            VideoStreamEntity stream = new VideoStreamEntity();
            stream.setStreamId(generateStreamId());
            stream.setStreamKey(generateStreamKey(startForm.getDeviceId(), startForm.getChannelId()));
            stream.setDeviceId(startForm.getDeviceId());
            stream.setChannelId(startForm.getChannelId() != null ? startForm.getChannelId() : 1L);
            stream.setStreamType(StringUtils.hasText(startForm.getStreamType()) ? startForm.getStreamType() : "main");
            stream.setProtocol(startForm.getProtocol() != null ? startForm.getProtocol() : 1);
            stream.setQuality(StringUtils.hasText(startForm.getQuality()) ? startForm.getQuality() : "medium");
            stream.setResolution(startForm.getResolution());
            stream.setFrameRate(startForm.getFrameRate() != null ? startForm.getFrameRate() : 25);
            stream.setBitrate(startForm.getBitrate() != null ? startForm.getBitrate() : 2048);
            stream.setStreamStatus(1); // 1-活跃
            stream.setAudioEnabled(startForm.getAudioEnabled() != null ? startForm.getAudioEnabled() : 1);
            stream.setRecordEnabled(startForm.getRecordEnabled() != null ? startForm.getRecordEnabled() : 0);
            stream.setRecordDuration(startForm.getRecordDuration());
            stream.setClientIp(startForm.getClientIp());
            stream.setUserAgent(startForm.getUserAgent());
            stream.setSessionTimeout(startForm.getSessionTimeout() != null ? startForm.getSessionTimeout() : 120);
            stream.setDynamicBitrate(startForm.getDynamicBitrate() != null ? startForm.getDynamicBitrate() : 1);
            stream.setViewerCount(0);
            stream.setMaxViewerCount(1000);
            stream.setExtendedParams(startForm.getExtendedParams());
            stream.setCreateTime(LocalDateTime.now());
            stream.setUpdateTime(LocalDateTime.now());
            stream.setDeletedFlag(0);

            // 保存流记录
            videoStreamDao.insert(stream);

            // 创建流会话
            Map<String, Object> sessionResult = videoStreamManager.createStreamSession(
                    startForm.getDeviceId(), startForm.getChannelId(), startForm.getStreamType());

            // 转换为会话VO
            VideoStreamSessionVO sessionVO = convertToSessionVO(sessionResult, stream);

            log.info("[视频流] 启动流成功，streamId={}, sessionId={}",
                    stream.getStreamId(), sessionVO.getSessionId());
            return ResponseDTO.ok(sessionVO);

        } catch (BusinessException e) {
            log.warn("[视频流] 启动流业务异常: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[视频流] 启动流系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[视频流] 启动流未知异常: error={}", e.getMessage(), e);
            throw new SystemException("START_STREAM_ERROR", "启动视频流失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "video.stream.stop", contextualName = "video-stream-stop")
    public ResponseDTO<Void> stopStream(Long streamId) {
        log.info("[视频流] 停止流，streamId={}", streamId);

        try {
            if (streamId == null) {
                return ResponseDTO.error("PARAM_ERROR", "流ID不能为空");
            }

            // 查询流记录
            VideoStreamEntity stream = videoStreamDao.selectById(streamId);
            if (stream == null) {
                return ResponseDTO.error("STREAM_NOT_FOUND", "视频流不存在");
            }

            if (stream.getDeletedFlag() == 1) {
                return ResponseDTO.error("STREAM_DELETED", "视频流已删除");
            }

            if (stream.getStreamStatus() != 1) {
                return ResponseDTO.error("STREAM_NOT_ACTIVE", "视频流未处于活跃状态");
            }

            // 更新流状态
            LambdaUpdateWrapper<VideoStreamEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(VideoStreamEntity::getStreamId, streamId)
                    .set(VideoStreamEntity::getStreamStatus, 3) // 3-停止
                    .set(VideoStreamEntity::getEndTime, LocalDateTime.now())
                    .set(VideoStreamEntity::getUpdateTime, LocalDateTime.now());

            int result = videoStreamDao.update(null, updateWrapper);
            if (result <= 0) {
                return ResponseDTO.error("STOP_STREAM_FAILED", "停止视频流失败");
            }

            // 关闭流会话
            if (StringUtils.hasText(stream.getSessionId())) {
                videoStreamManager.closeStreamSession(stream.getSessionId());
            }

            log.info("[视频流] 停止流成功，streamId={}", streamId);
            return ResponseDTO.ok();

        } catch (BusinessException e) {
            log.warn("[视频流] 停止流业务异常: streamId={}, code={}, message={}", streamId, e.getCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[视频流] 停止流系统异常: streamId={}, code={}, message={}", streamId, e.getCode(), e.getMessage(), e);
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[视频流] 停止流未知异常: streamId={}", streamId, e);
            return ResponseDTO.error("STOP_STREAM_ERROR", "停止视频流失败");
        }
    }

    @Override
    public ResponseDTO<Void> batchStopStreams(List<Long> streamIds) {
        log.info("[视频流] 批量停止流，streamIds={}", streamIds);

        try {
            if (streamIds == null || streamIds.isEmpty()) {
                return ResponseDTO.error("PARAM_ERROR", "流ID列表不能为空");
            }

            int successCount = 0;
            int failCount = 0;
            List<String> errors = new ArrayList<>();

            for (Long streamId : streamIds) {
                try {
                    ResponseDTO<Void> result = stopStream(streamId);
                    if (result.getCode() == 200) {
                        successCount++;
                    } else {
                        failCount++;
                        errors.add("流ID " + streamId + ": " + result.getMessage());
                    }
                } catch (Exception e) {
                    failCount++;
                    errors.add("流ID " + streamId + ": " + e.getMessage());
                }
            }

            if (failCount == 0) {
                log.info("[视频流] 批量停止流成功，总数={}", successCount);
                return ResponseDTO.ok();
            } else if (successCount > 0) {
                log.warn("[视频流] 批量停止流部分成功，成功={}, 失败={}", successCount, failCount);
                return ResponseDTO.error("BATCH_STOP_PARTIAL_FAILED",
                        String.format("批量停止流部分成功：成功%d个，失败%d个", successCount, failCount));
            } else {
                log.error("[视频流] 批量停止流全部失败，总数={}", failCount);
                return ResponseDTO.error("BATCH_STOP_ALL_FAILED",
                        String.format("批量停止流全部失败：失败%d个", failCount));
            }

        } catch (Exception e) {
            log.error("[视频流] 批量停止流异常: streamIds={}", streamIds, e);
            return ResponseDTO.error("BATCH_STOP_STREAMS_ERROR", "批量停止视频流失败");
        }
    }

    @Override
    @Observed(name = "video.stream.restart", contextualName = "video-stream-restart")
    public ResponseDTO<VideoStreamSessionVO> restartStream(Long streamId) {
        log.info("[视频流] 重启流，streamId={}", streamId);

        try {
            if (streamId == null) {
                return ResponseDTO.error("PARAM_ERROR", "流ID不能为空");
            }

            // 查询流记录
            VideoStreamEntity stream = videoStreamDao.selectById(streamId);
            if (stream == null) {
                return ResponseDTO.error("STREAM_NOT_FOUND", "视频流不存在");
            }

            if (stream.getDeletedFlag() == 1) {
                return ResponseDTO.error("STREAM_DELETED", "视频流已删除");
            }

            // 停止流
            stopStream(streamId);

            // 重新创建启动表单
            VideoStreamStartForm startForm = new VideoStreamStartForm();
            startForm.setDeviceId(stream.getDeviceId());
            startForm.setChannelId(stream.getChannelId());
            startForm.setStreamType(stream.getStreamType());
            startForm.setProtocol(stream.getProtocol());
            startForm.setQuality(stream.getQuality());
            startForm.setResolution(stream.getResolution());
            startForm.setFrameRate(stream.getFrameRate());
            startForm.setBitrate(stream.getBitrate());
            startForm.setAudioEnabled(stream.getAudioEnabled());
            startForm.setRecordEnabled(stream.getRecordEnabled());
            startForm.setRecordDuration(stream.getRecordDuration());
            startForm.setClientIp(stream.getClientIp());
            startForm.setUserAgent(stream.getUserAgent());
            startForm.setSessionTimeout(stream.getSessionTimeout());
            startForm.setDynamicBitrate(stream.getDynamicBitrate());
            startForm.setExtendedParams(stream.getExtendedParams());

            // 启动流
            return startStream(startForm);

        } catch (Exception e) {
            log.error("[视频流] 重启流异常: streamId={}", streamId, e);
            return ResponseDTO.error("RESTART_STREAM_ERROR", "重启视频流失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<VideoStreamSessionVO> getStreamSession(String sessionId) {
        log.info("[视频流] 获取流会话，sessionId={}", sessionId);

        try {
            if (!StringUtils.hasText(sessionId)) {
                return ResponseDTO.error("PARAM_ERROR", "会话ID不能为空");
            }

            // 根据会话ID查询流记录
            LambdaQueryWrapper<VideoStreamEntity> wrapper = new LambdaQueryWrapper<VideoStreamEntity>()
                    .eq(VideoStreamEntity::getSessionId, sessionId)
                    .eq(VideoStreamEntity::getDeletedFlag, 0);

            VideoStreamEntity stream = videoStreamDao.selectOne(wrapper);
            if (stream == null) {
                return ResponseDTO.error("SESSION_NOT_FOUND", "流会话不存在");
            }

            // 获取会话详情
            Map<String, Object> sessionResult = videoStreamManager.getStreamQualityStats(sessionId);

            // 转换为会话VO
            VideoStreamSessionVO sessionVO = convertToSessionVO(sessionResult, stream);

            log.info("[视频流] 获取流会话成功，sessionId={}", sessionId);
            return ResponseDTO.ok(sessionVO);

        } catch (Exception e) {
            log.error("[视频流] 获取流会话异常: sessionId={}", sessionId, e);
            return ResponseDTO.error("GET_STREAM_SESSION_ERROR", "获取流会话失败");
        }
    }

    @Override
    public ResponseDTO<Void> closeStreamSession(String sessionId) {
        log.info("[视频流] 关闭流会话，sessionId={}", sessionId);

        try {
            if (!StringUtils.hasText(sessionId)) {
                return ResponseDTO.error("PARAM_ERROR", "会话ID不能为空");
            }

            // 根据会话ID查询流记录
            LambdaQueryWrapper<VideoStreamEntity> wrapper = new LambdaQueryWrapper<VideoStreamEntity>()
                    .eq(VideoStreamEntity::getSessionId, sessionId)
                    .eq(VideoStreamEntity::getDeletedFlag, 0);

            VideoStreamEntity stream = videoStreamDao.selectOne(wrapper);
            if (stream == null) {
                return ResponseDTO.error("SESSION_NOT_FOUND", "流会话不存在");
            }

            // 关闭会话
            Map<String, Object> result = videoStreamManager.closeStreamSession(sessionId);
            if (!(Boolean) result.get("success")) {
                return ResponseDTO.error("CLOSE_SESSION_FAILED", "关闭会话失败：" + result.get("message"));
            }

            // 更新流状态
            LambdaUpdateWrapper<VideoStreamEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(VideoStreamEntity::getStreamId, stream.getStreamId())
                    .set(VideoStreamEntity::getStreamStatus, 3) // 3-停止
                    .set(VideoStreamEntity::getEndTime, LocalDateTime.now())
                    .set(VideoStreamEntity::getUpdateTime, LocalDateTime.now());

            videoStreamDao.update(null, updateWrapper);

            log.info("[视频流] 关闭流会话成功，sessionId={}", sessionId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[视频流] 关闭流会话异常: sessionId={}", sessionId, e);
            return ResponseDTO.error("CLOSE_STREAM_SESSION_ERROR", "关闭流会话失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<VideoStreamVO>> getStreamsByDeviceId(Long deviceId) {
        log.info("[视频流] 根据设备查询流，deviceId={}", deviceId);

        try {
            if (deviceId == null) {
                return ResponseDTO.error("PARAM_ERROR", "设备ID不能为空");
            }

            LambdaQueryWrapper<VideoStreamEntity> wrapper = new LambdaQueryWrapper<VideoStreamEntity>()
                    .eq(VideoStreamEntity::getDeviceId, deviceId)
                    .eq(VideoStreamEntity::getDeletedFlag, 0)
                    .orderByDesc(VideoStreamEntity::getCreateTime);

            List<VideoStreamEntity> streams = videoStreamDao.selectList(wrapper);
            List<VideoStreamVO> voList = streams.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            log.info("[视频流] 根据设备查询流完成，deviceId={}, count={}", deviceId, voList.size());
            return ResponseDTO.ok(voList);

        } catch (Exception e) {
            log.error("[视频流] 根据设备查询流异常: deviceId={}", deviceId, e);
            return ResponseDTO.error("GET_STREAMS_BY_DEVICE_ERROR", "根据设备查询视频流失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<VideoStreamVO>> getActiveStreams() {
        log.info("[视频流] 查询活跃流");

        try {
            LambdaQueryWrapper<VideoStreamEntity> wrapper = new LambdaQueryWrapper<VideoStreamEntity>()
                    .eq(VideoStreamEntity::getStreamStatus, 1) // 1-活跃
                    .eq(VideoStreamEntity::getDeletedFlag, 0)
                    .orderByDesc(VideoStreamEntity::getViewerCount);

            List<VideoStreamEntity> streams = videoStreamDao.selectList(wrapper);
            List<VideoStreamVO> voList = streams.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            log.info("[视频流] 查询活跃流完成，count={}", voList.size());
            return ResponseDTO.ok(voList);

        } catch (Exception e) {
            log.error("[视频流] 查询活跃流异常", e);
            return ResponseDTO.error("GET_ACTIVE_STREAMS_ERROR", "查询活跃视频流失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<Map<String, Object>> captureSnapshot(Long deviceId, Long channelId) {
        log.info("[视频流] 获取截图，deviceId={}, channelId={}", deviceId, channelId);

        try {
            // 验证设备是否存在
            // TODO: 调用设备服务验证设备状态

            // 获取截图
            Map<String, Object> result = videoStreamManager.captureSnapshot(deviceId, channelId);

            if (!(Boolean) result.get("success")) {
                return ResponseDTO.error("CAPTURE_SNAPSHOT_FAILED", "获取截图失败：" + result.get("message"));
            }

            log.info("[视频流] 获取截图成功，deviceId={}, channelId={}", deviceId, channelId);
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[视频流] 获取截图异常: deviceId={}, channelId={}", deviceId, channelId, e);
            return ResponseDTO.error("CAPTURE_SNAPSHOT_ERROR", "获取视频截图失败");
        }
    }

    @Override
    public ResponseDTO<Void> switchStreamQuality(Long streamId, String quality) {
        log.info("[视频流] 切换流质量，streamId={}, quality={}", streamId, quality);

        try {
            if (streamId == null) {
                return ResponseDTO.error("PARAM_ERROR", "流ID不能为空");
            }

            if (!StringUtils.hasText(quality)) {
                return ResponseDTO.error("PARAM_ERROR", "质量参数不能为空");
            }

            // 查询流记录
            VideoStreamEntity stream = videoStreamDao.selectById(streamId);
            if (stream == null) {
                return ResponseDTO.error("STREAM_NOT_FOUND", "视频流不存在");
            }

            if (stream.getDeletedFlag() == 1) {
                return ResponseDTO.error("STREAM_DELETED", "视频流已删除");
            }

            // 验证质量参数
            if (!"high".equals(quality) && !"medium".equals(quality) && !"low".equals(quality)) {
                return ResponseDTO.error("INVALID_QUALITY", "无效的质量参数，只能是high、medium或low");
            }

            // 切换质量
            Map<String, Object> result = videoStreamManager.switchStreamQuality(stream.getSessionId(), quality);
            if (!(Boolean) result.get("success")) {
                return ResponseDTO.error("SWITCH_QUALITY_FAILED", "切换质量失败：" + result.get("message"));
            }

            // 更新数据库记录
            LambdaUpdateWrapper<VideoStreamEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(VideoStreamEntity::getStreamId, streamId)
                    .set(VideoStreamEntity::getQuality, quality)
                    .set(VideoStreamEntity::getUpdateTime, LocalDateTime.now());

            videoStreamDao.update(null, updateWrapper);

            log.info("[视频流] 切换流质量成功，streamId={}, quality={}", streamId, quality);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[视频流] 切换流质量异常: streamId={}, quality={}", streamId, quality, e);
            return ResponseDTO.error("SWITCH_QUALITY_ERROR", "切换视频流质量失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<Map<String, Object>> getStreamQualityStats(Long streamId) {
        log.info("[视频流] 获取流质量统计，streamId={}", streamId);

        try {
            if (streamId == null) {
                return ResponseDTO.error("PARAM_ERROR", "流ID不能为空");
            }

            // 查询流记录
            VideoStreamEntity stream = videoStreamDao.selectById(streamId);
            if (stream == null) {
                return ResponseDTO.error("STREAM_NOT_FOUND", "视频流不存在");
            }

            if (!StringUtils.hasText(stream.getSessionId())) {
                return ResponseDTO.error("SESSION_ID_EMPTY", "会话ID为空");
            }

            // 获取质量统计
            Map<String, Object> stats = videoStreamManager.getStreamQualityStats(stream.getSessionId());

            if (!(Boolean) stats.get("success")) {
                return ResponseDTO.error("GET_STATS_FAILED", "获取质量统计失败：" + stats.get("message"));
            }

            log.info("[视频流] 获取流质量统计成功，streamId={}", streamId);
            return ResponseDTO.ok(stats);

        } catch (Exception e) {
            log.error("[视频流] 获取流质量统计异常: streamId={}", streamId, e);
            return ResponseDTO.error("GET_STREAM_QUALITY_STATS_ERROR", "获取流质量统计失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<Object> getStreamStatistics() {
        log.info("[视频流] 获取流统计信息");

        try {
            Map<String, Object> statistics = new HashMap<>();

            // 总流数
            LambdaQueryWrapper<VideoStreamEntity> totalWrapper = new LambdaQueryWrapper<>();
            totalWrapper.eq(VideoStreamEntity::getDeletedFlag, 0);
            long totalStreams = videoStreamDao.selectCount(totalWrapper);
            statistics.put("totalStreams", totalStreams);

            // 活跃流数
            LambdaQueryWrapper<VideoStreamEntity> activeWrapper = new LambdaQueryWrapper<>();
            activeWrapper.eq(VideoStreamEntity::getStreamStatus, 1)
                    .eq(VideoStreamEntity::getDeletedFlag, 0);
            long activeStreams = videoStreamDao.selectCount(activeWrapper);
            statistics.put("activeStreams", activeStreams);

            // 暂停流数
            LambdaQueryWrapper<VideoStreamEntity> pausedWrapper = new LambdaQueryWrapper<>();
            pausedWrapper.eq(VideoStreamEntity::getStreamStatus, 2)
                    .eq(VideoStreamEntity::getDeletedFlag, 0);
            long pausedStreams = videoStreamDao.selectCount(pausedWrapper);
            statistics.put("pausedStreams", pausedStreams);

            // 停止流数
            LambdaQueryWrapper<VideoStreamEntity> stoppedWrapper = new LambdaQueryWrapper<>();
            stoppedWrapper.eq(VideoStreamEntity::getStreamStatus, 3)
                    .eq(VideoStreamEntity::getDeletedFlag, 0);
            long stoppedStreams = videoStreamDao.selectCount(stoppedWrapper);
            statistics.put("stoppedStreams", stoppedStreams);

            // 错误流数
            LambdaQueryWrapper<VideoStreamEntity> errorWrapper = new LambdaQueryWrapper<>();
            errorWrapper.eq(VideoStreamEntity::getStreamStatus, 4)
                    .eq(VideoStreamEntity::getDeletedFlag, 0);
            long errorStreams = videoStreamDao.selectCount(errorWrapper);
            statistics.put("errorStreams", errorStreams);

            // 总观看人数
            LambdaQueryWrapper<VideoStreamEntity> viewerWrapper = new LambdaQueryWrapper<>();
            viewerWrapper.eq(VideoStreamEntity::getStreamStatus, 1)
                    .eq(VideoStreamEntity::getDeletedFlag, 0);
            List<VideoStreamEntity> activeStreamsList = videoStreamDao.selectList(viewerWrapper);
            int totalViewers = activeStreamsList.stream()
                    .mapToInt(stream -> stream.getViewerCount() != null ? stream.getViewerCount() : 0)
                    .sum();
            statistics.put("totalViewers", totalViewers);

            // 录制中流数
            LambdaQueryWrapper<VideoStreamEntity> recordingWrapper = new LambdaQueryWrapper<>();
            recordingWrapper.eq(VideoStreamEntity::getRecording, 1)
                    .eq(VideoStreamEntity::getDeletedFlag, 0);
            long recordingStreams = videoStreamDao.selectCount(recordingWrapper);
            statistics.put("recordingStreams", recordingStreams);

            // 按协议统计
            Map<Integer, Long> protocolStats = new HashMap<>();
            for (int protocol = 1; protocol <= 5; protocol++) {
                LambdaQueryWrapper<VideoStreamEntity> protocolWrapper = new LambdaQueryWrapper<>();
                protocolWrapper.eq(VideoStreamEntity::getProtocol, protocol)
                        .eq(VideoStreamEntity::getDeletedFlag, 0);
                long count = videoStreamDao.selectCount(protocolWrapper);
                protocolStats.put(protocol, count);
            }
            statistics.put("protocolStats", protocolStats);

            // 按质量统计
            Map<String, Long> qualityStats = new HashMap<>();
            qualityStats.put("high", 0L);
            qualityStats.put("medium", 0L);
            qualityStats.put("low", 0L);

            List<VideoStreamEntity> allStreams = videoStreamDao.selectList(
                    new LambdaQueryWrapper<VideoStreamEntity>()
                            .eq(VideoStreamEntity::getDeletedFlag, 0));

            for (VideoStreamEntity stream : allStreams) {
                String quality = stream.getQuality();
                if (qualityStats.containsKey(quality)) {
                    qualityStats.put(quality, qualityStats.get(quality) + 1);
                }
            }
            statistics.put("qualityStats", qualityStats);

            log.info("[视频流] 获取流统计信息成功");
            return ResponseDTO.ok(statistics);

        } catch (Exception e) {
            log.error("[视频流] 获取流统计信息异常", e);
            return ResponseDTO.error("GET_STREAM_STATISTICS_ERROR", "获取流统计信息失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<Map<String, Object>> checkStreamStatus(Long streamId) {
        log.info("[视频流] 检测流状态，streamId={}", streamId);

        try {
            if (streamId == null) {
                return ResponseDTO.error("PARAM_ERROR", "流ID不能为空");
            }

            // 查询流记录
            VideoStreamEntity stream = videoStreamDao.selectById(streamId);
            if (stream == null) {
                return ResponseDTO.error("STREAM_NOT_FOUND", "视频流不存在");
            }

            Map<String, Object> status = new HashMap<>();
            status.put("streamId", streamId);
            status.put("streamStatus", stream.getStreamStatus());
            status.put("streamStatusDesc", getStreamStatusDesc(stream.getStreamStatus()));
            status.put("viewerCount", stream.getViewerCount());
            status.put("bitrate", stream.getBitrate());
            status.put("quality", stream.getQuality());
            status.put("protocol", stream.getProtocol());
            status.put("isRecording", stream.getRecording() == 1);
            status.put("checkTime", LocalDateTime.now());

            // 如果有会话ID，获取会话状态
            if (StringUtils.hasText(stream.getSessionId())) {
                Map<String, Object> sessionStats = videoStreamManager.getStreamQualityStats(stream.getSessionId());
                status.put("sessionStats", sessionStats);
            }

            log.info("[视频流] 检测流状态成功，streamId={}, status={}", streamId, stream.getStreamStatus());
            return ResponseDTO.ok(status);

        } catch (Exception e) {
            log.error("[视频流] 检测流状态异常: streamId={}", streamId, e);
            return ResponseDTO.error("CHECK_STREAM_STATUS_ERROR", "检测流状态失败");
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> recordStream(Long streamId, Integer duration) {
        log.info("[视频流] 录制流，streamId={}, duration={}", streamId, duration);

        try {
            if (streamId == null) {
                return ResponseDTO.error("PARAM_ERROR", "流ID不能为空");
            }

            // 查询流记录
            VideoStreamEntity stream = videoStreamDao.selectById(streamId);
            if (stream == null) {
                return ResponseDTO.error("STREAM_NOT_FOUND", "视频流不存在");
            }

            if (stream.getStreamStatus() != 1) {
                return ResponseDTO.error("STREAM_NOT_ACTIVE", "视频流未处于活跃状态");
            }

            // 启用录制
            LambdaUpdateWrapper<VideoStreamEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(VideoStreamEntity::getStreamId, streamId)
                    .set(VideoStreamEntity::getRecordEnabled, 1)
                    .set(VideoStreamEntity::getRecordStartTime, LocalDateTime.now())
                    .set(VideoStreamEntity::getRecordDuration, duration)
                    .set(VideoStreamEntity::getUpdateTime, LocalDateTime.now());

            int result = videoStreamDao.update(null, updateWrapper);
            if (result <= 0) {
                return ResponseDTO.error("RECORD_STREAM_FAILED", "启用录制失败");
            }

            Map<String, Object> recordResult = new HashMap<>();
            recordResult.put("streamId", streamId);
            recordResult.put("recordEnabled", true);
            recordResult.put("recordStartTime", stream.getRecordStartTime());
            recordResult.put("recordDuration", duration);

            log.info("[视频流] 录制流成功，streamId={}", streamId);
            return ResponseDTO.ok(recordResult);

        } catch (Exception e) {
            log.error("[视频流] 录制流异常: streamId={}, duration={}", streamId, duration, e);
            return ResponseDTO.error("RECORD_STREAM_ERROR", "录制视频流失败");
        }
    }

    @Override
    public ResponseDTO<Void> stopRecording(Long streamId) {
        log.info("[视频流] 停止录制，streamId={}", streamId);

        try {
            if (streamId == null) {
                return ResponseDTO.error("PARAM_ERROR", "流ID不能为空");
            }

            // 查询流记录
            VideoStreamEntity stream = videoStreamDao.selectById(streamId);
            if (stream == null) {
                return ResponseDTO.error("STREAM_NOT_FOUND", "视频流不存在");
            }

            // 停止录制
            LambdaUpdateWrapper<VideoStreamEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(VideoStreamEntity::getStreamId, streamId)
                    .set(VideoStreamEntity::getRecordEnabled, 0)
                    .set(VideoStreamEntity::getRecordEndTime, LocalDateTime.now())
                    .set(VideoStreamEntity::getUpdateTime, LocalDateTime.now());

            int result = videoStreamDao.update(null, updateWrapper);
            if (result <= 0) {
                return ResponseDTO.error("STOP_RECORDING_FAILED", "停止录制失败");
            }

            log.info("[视频流] 停止录制成功，streamId={}", streamId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[视频流] 停止录制异常: streamId={}", streamId, e);
            return ResponseDTO.error("STOP_RECORDING_ERROR", "停止录制失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<Map<String, Object>>> getRecordings(Long deviceId) {
        log.info("[视频流] 获取录制列表，deviceId={}", deviceId);

        try {
            if (deviceId == null) {
                return ResponseDTO.error("PARAM_ERROR", "设备ID不能为空");
            }

            // TODO: 实现实际的录制列表查询
            List<Map<String, Object>> recordings = new ArrayList<>();

            // 模拟数据
            Map<String, Object> recording1 = new HashMap<>();
            recording1.put("recordingId", "rec_1001_001");
            recording1.put("deviceId", deviceId);
            recording1.put("deviceName", "摄像头001");
            recording1.put("channelId", 1);
            recording1.put("channelName", "主通道");
            recording1.put("recordStartTime", LocalDateTime.now().minusMinutes(30));
            recording1.put("recordEndTime", LocalDateTime.now().minusMinutes(25));
            recording1.put("duration", 300);
            recording1.put("fileSize", 1024 * 1024 * 50); // 50MB
            recording1.put("filePath", "/recordings/device_1001/rec_1001_001.mp4");
            recordings.add(recording1);

            log.info("[视频流] 获取录制列表成功，deviceId={}, count={}", deviceId, recordings.size());
            return ResponseDTO.ok(recordings);

        } catch (Exception e) {
            log.error("[视频流] 获取录制列表异常: deviceId={}", deviceId, e);
            return ResponseDTO.error("GET_RECORDINGS_ERROR", "获取录制列表失败");
        }
    }

    /**
     * 转换Entity为VO
     *
     * @param entity 流实体
     * @return 流VO
     */
    private VideoStreamVO convertToVO(VideoStreamEntity entity) {
        VideoStreamVO vo = new VideoStreamVO();
        vo.setStreamId(entity.getStreamId());
        vo.setStreamKey(entity.getStreamKey());
        vo.setDeviceId(entity.getDeviceId());
        vo.setDeviceCode(entity.getDeviceCode());
        vo.setDeviceName(entity.getDeviceName());
        vo.setChannelId(entity.getChannelId());
        vo.setChannelName(entity.getChannelName());
        vo.setStreamType(entity.getStreamType());
        vo.setStreamTypeDesc(getStreamTypeDesc(entity.getStreamType()));
        vo.setProtocol(entity.getProtocol());
        vo.setProtocolDesc(getProtocolDesc(entity.getProtocol()));
        vo.setStreamStatus(entity.getStreamStatus());
        vo.setStreamStatusDesc(getStreamStatusDesc(entity.getStreamStatus()));
        vo.setAreaId(entity.getAreaId());
        vo.setAreaName(entity.getAreaName());
        vo.setQuality(entity.getQuality());
        vo.setQualityDesc(getQualityDesc(entity.getQuality()));
        vo.setResolution(entity.getResolution());
        vo.setFrameRate(entity.getFrameRate());
        vo.setBitrate(entity.getBitrate());
        vo.setCodec(entity.getCodec());
        vo.setRtspUrl(entity.getRtspUrl());
        vo.setRtmpUrl(entity.getRtmpUrl());
        vo.setHlsUrl(entity.getHlsUrl());
        vo.setWebrtcUrl(entity.getWebrtcUrl());
        vo.setFlvUrl(entity.getFlvUrl());
        vo.setAudioEnabled(entity.getAudioEnabled());
        vo.setAudioEnabledDesc(entity.getAudioEnabled() == 1 ? "启用" : "禁用");
        vo.setAudioCodec(entity.getAudioCodec());
        vo.setRecording(entity.getRecording());
        vo.setRecordingDesc(entity.getRecording() == 1 ? "录制中" : "未录制");
        vo.setViewerCount(entity.getViewerCount());
        vo.setMaxViewerCount(entity.getMaxViewerCount());
        vo.setStreamServer(entity.getStreamServer());
        vo.setClientIp(entity.getClientIp());
        vo.setUserAgent(entity.getUserAgent());
        vo.setSessionId(entity.getSessionId());
        vo.setSessionTimeout(entity.getSessionTimeout());
        vo.setDynamicBitrate(entity.getDynamicBitrate());
        vo.setLatency(entity.getLatency());
        vo.setPacketLoss(entity.getPacketLoss());
        vo.setJitter(entity.getJitter());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        vo.setStartTime(entity.getStartTime());
        vo.setEndTime(entity.getEndTime());
        vo.setCreateUserId(entity.getCreateUserId());
        vo.setCreateUserName(entity.getCreateUserName());
        vo.setUpdateUserId(entity.getUpdateUserId());
        vo.setUpdateUserName(entity.getUpdateUserName());
        vo.setRemark(entity.getRemark());

        return vo;
    }

    /**
     * 转换为会话VO
     *
     * @param sessionResult 会话结果
     * @param stream 流实体
     * @return 会话VO
     */
    private VideoStreamSessionVO convertToSessionVO(Map<String, Object> sessionResult, VideoStreamEntity stream) {
        VideoStreamSessionVO sessionVO = new VideoStreamSessionVO();

        sessionVO.setSessionId((String) sessionResult.get("sessionId"));
        sessionVO.setStreamId(stream.getStreamId());
        sessionVO.setStreamKey(stream.getStreamKey());
        sessionVO.setDeviceId(stream.getDeviceId());
        sessionVO.setDeviceCode(stream.getDeviceCode());
        sessionVO.setDeviceName(stream.getDeviceName());
        sessionVO.setChannelId(stream.getChannelId());
        sessionVO.setChannelName(stream.getChannelName());
        sessionVO.setStreamType(stream.getStreamType());
        sessionVO.setProtocol(stream.getProtocol());
        sessionVO.setQuality(stream.getQuality());
        sessionVO.setResolution(stream.getResolution());
        sessionVO.setFrameRate(stream.getFrameRate());
        sessionVO.setBitrate(stream.getBitrate());

        // 构建流地址映射
        Map<String, String> streamUrls = new HashMap<>();
        streamUrls.put("rtsp", stream.getRtspUrl());
        streamUrls.put("rtmp", stream.getRtmpUrl());
        streamUrls.put("hls", stream.getHlsUrl());
        streamUrls.put("webrtc", stream.getWebrtcUrl());
        streamUrls.put("flv", stream.getFlvUrl());
        sessionVO.setStreamUrls(streamUrls);

        sessionVO.setRtspUrl(stream.getRtspUrl());
        sessionVO.setRtmpUrl(stream.getRtmpUrl());
        sessionVO.setHlsUrl(stream.getHlsUrl());
        sessionVO.setWebrtcUrl(stream.getWebrtcUrl());
        sessionVO.setFlvUrl(stream.getFlvUrl());
        sessionVO.setAudioEnabled(stream.getAudioEnabled());
        sessionVO.setAudioCodec(stream.getAudioCodec());
        sessionVO.setRecordEnabled(stream.getRecordEnabled());
        sessionVO.setRecordFilePath(stream.getRecordFilePath());
        sessionVO.setRecordStartTime(stream.getRecordStartTime());
        sessionVO.setRecordDuration(stream.getRecordDuration());

        // 会话状态
        if (stream.getStreamStatus() == 1) {
            sessionVO.setSessionStatus("active");
            sessionVO.setSessionStatusDesc("活跃");
        } else if (stream.getStreamStatus() == 2) {
            sessionVO.setSessionStatus("paused");
            sessionVO.setSessionStatusDesc("暂停");
        } else if (stream.getStreamStatus() == 3) {
            sessionVO.setSessionStatus("closed");
            sessionVO.setSessionStatusDesc("已关闭");
        } else {
            sessionVO.setSessionStatus("error");
            sessionVO.setSessionStatusDesc("错误");
        }

        sessionVO.setClientIp(stream.getClientIp());
        sessionVO.setUserAgent(stream.getUserAgent());
        sessionVO.setCreateTime(stream.getCreateTime());
        sessionVO.setUpdateTime(stream.getUpdateTime());
        sessionVO.setStartTime(stream.getStartTime());
        sessionVO.setEndTime(stream.getEndTime());
        sessionVO.setSessionTimeout(stream.getSessionTimeout());
        sessionVO.setDynamicBitrate(stream.getDynamicBitrate());
        sessionVO.setCurrentBitrate((Integer) sessionResult.get("bitrate"));
        sessionVO.setNetworkQuality(getNetworkQuality((Integer) sessionResult.get("latency")));
        sessionVO.setLatency((Integer) sessionResult.get("latency"));
        sessionVO.setPacketLoss((Double) sessionResult.get("packetLoss"));
        sessionVO.setJitter((Integer) sessionResult.get("jitter"));
        sessionVO.setExtendedParams(stream.getExtendedParams());
        sessionVO.setRemark(stream.getRemark());

        return sessionVO;
    }

    /**
     * 生成流ID
     */
    private Long generateStreamId() {
        return System.currentTimeMillis();
    }

    /**
     * 生成流标识
     */
    private String generateStreamKey(Long deviceId, Long channelId) {
        return String.format("stream_%d_%d_%d",
                deviceId,
                channelId != null ? channelId : 1,
                System.currentTimeMillis());
    }

    /**
     * 获取流类型描述
     */
    private String getStreamTypeDesc(String streamType) {
        if (streamType == null) {
            return "未知";
        }
        switch (streamType) {
            case "main":
                return "主流";
            case "sub":
                return "子流";
            case "mobile":
                return "移动流";
            default:
                return "未知";
        }
    }

    /**
     * 获取协议描述
     */
    private String getProtocolDesc(Integer protocol) {
        if (protocol == null) {
            return "未知";
        }
        switch (protocol) {
            case 1:
                return "RTSP";
            case 2:
                return "RTMP";
            case 3:
                return "HLS";
            case 4:
                return "WebRTC";
            case 5:
                return "HTTP-FLV";
            default:
                return "未知";
        }
    }

    /**
     * 获取流状态描述
     */
    private String getStreamStatusDesc(Integer streamStatus) {
        if (streamStatus == null) {
            return "未知";
        }
        switch (streamStatus) {
            case 1:
                return "活跃";
            case 2:
                return "暂停";
            case 3:
                return "停止";
            case 4:
                return "错误";
            default:
                return "未知";
        }
    }

    /**
     * 获取质量描述
     */
    private String getQualityDesc(String quality) {
        if (quality == null) {
            return "未知";
        }
        switch (quality) {
            case "high":
                return "高清";
            case "medium":
                return "标清";
            case "low":
                return "流畅";
            default:
                return "未知";
        }
    }

    /**
     * 获取网络质量描述
     */
    private String getNetworkQuality(Integer latency) {
        if (latency == null) {
            return "未知";
        }
        if (latency < 100) {
            return "excellent";
        } else if (latency < 300) {
            return "good";
        } else if (latency < 500) {
            return "fair";
        } else {
            return "poor";
        }
    }
}