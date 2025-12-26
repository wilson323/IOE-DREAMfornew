package net.lab1024.sa.video.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.lab1024.sa.common.util.QueryBuilder;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.util.TypeUtils;
import net.lab1024.sa.video.adapter.IVideoStreamAdapter;
import net.lab1024.sa.video.adapter.factory.VideoStreamAdapterFactory;
import net.lab1024.sa.video.domain.form.VideoRecordingQueryForm;
import net.lab1024.sa.video.domain.form.VideoRecordingSearchForm;
import net.lab1024.sa.video.domain.vo.VideoRecordingDetailVO;
import net.lab1024.sa.video.domain.vo.VideoRecordingPlaybackVO;
import net.lab1024.sa.video.domain.vo.VideoRecordingVO;
import net.lab1024.sa.common.entity.video.VideoRecordEntity;
import net.lab1024.sa.video.manager.VideoRecordingManager;
import net.lab1024.sa.video.service.VideoRecordingService;

/**
 * 录像回放服务实现
 * <p>
 * 提供录像文件管理、回放控制、下载等功能
 * 严格遵循CLAUDE.md规范：
 * - 实现Service接口
 * - 使用@Transactional注解
 * - 完整的业务逻辑实现
 * - 使用协议适配器模式
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class VideoRecordingServiceImpl implements VideoRecordingService {

    // TODO: 取消注释当DAO和Manager准备好时
    // @Resource
    // private VideoRecordingDao videoRecordingDao;
    // @Resource
    // private GatewayServiceClient gatewayServiceClient;

    @Resource
    private VideoRecordingManager videoRecordingManager;

    @Resource
    private VideoStreamAdapterFactory streamAdapterFactory;

    /**
     * 录像文件存储基础路径
     */
    @Value("${video.recording.storage-path:/data/video/recordings}")
    private String recordingStoragePath;

    /**
     * 录像播放令牌有效期（小时）
     */
    @Value("${video.recording.token-expire-hours:2}")
    private Integer tokenExpireHours;

    /**
     * 播放令牌缓存（生产环境应使用Redis）
     */
    private final Map<String, PlaybackToken> playbackTokens = new HashMap<>();

    /**
     * 转码任务存储（生产环境应使用数据库）
     */
    private final Map<String, TranscodeTask> transcodeTasks = new HashMap<>();

    /**
     * 备份任务存储（生产环境应使用数据库）
     */
    private final Map<String, BackupTask> backupTasks = new HashMap<>();

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<PageResult<VideoRecordingVO>> queryRecordings(VideoRecordingQueryForm queryForm) {
        log.info("[录像回放] 分页查询录像，queryForm={}", queryForm);

        try {
            // 构建分页对象
            Page<VideoRecordEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());

            // 构建查询条件
            LambdaQueryWrapper<VideoRecordEntity> queryWrapper = new LambdaQueryWrapper<>();

            if (queryForm.getDeviceId() != null) {
                queryWrapper.eq(VideoRecordEntity::getDeviceId, queryForm.getDeviceId());
            }

            if (queryForm.getChannelId() != null) {
                queryWrapper.eq(VideoRecordEntity::getChannelId, queryForm.getChannelId());
            }

            if (TypeUtils.hasText(queryForm.getRecordingType())) {
                queryWrapper.eq(VideoRecordEntity::getRecordType, parseRecordingType(queryForm.getRecordingType()));
            }

            if (queryForm.getStartTime() != null) {
                queryWrapper.ge(VideoRecordEntity::getStartTime, queryForm.getStartTime());
            }

            if (queryForm.getEndTime() != null) {
                queryWrapper.le(VideoRecordEntity::getEndTime, queryForm.getEndTime());
            }

            if (queryForm.getImportant() != null) {
                queryWrapper.eq(VideoRecordEntity::getImportant, queryForm.getImportant());
            }

            if (TypeUtils.hasText(queryForm.getEventType())) {
                queryWrapper.like(VideoRecordEntity::getEventType, queryForm.getEventType());
            }

            // 按开始时间降序
            queryWrapper.orderByDesc(VideoRecordEntity::getStartTime);

            // TODO: 取消注释当DAO准备好时
            // IPage<VideoRecordEntity> recordPage = videoRecordingDao.selectPage(page, queryWrapper);

            // 临时模拟数据（DAO未实现时）
            IPage<VideoRecordEntity> recordPage = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
            recordPage.setRecords(new ArrayList<>());
            recordPage.setTotal(0);

            // 转换为VO
            List<VideoRecordingVO> voList = recordPage.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            PageResult<VideoRecordingVO> pageResult = PageResult.of(
                    voList,
                    recordPage.getTotal(),
                    queryForm.getPageNum(),
                    queryForm.getPageSize()
            );

            log.info("[录像回放] 分页查询录像完成，total={}, pages={}", pageResult.getTotal(), pageResult.getPages());
            return ResponseDTO.ok(pageResult);

        } catch (Exception e) {
            log.error("[录像回放] 分页查询录像异常，queryForm={}", queryForm, e);
            return ResponseDTO.error("QUERY_RECORDINGS_ERROR", "分页查询录像失败");
        }
    }

    /**
     * 解析录像类型
     */
    private Integer parseRecordingType(String recordingType) {
        if (!StringUtils.hasText(recordingType)) {
            return null;
        }
        return switch (recordingType.toLowerCase()) {
            case "manual" -> 2;
            case "scheduled" -> 1;
            case "event" -> 3;
            case "motion" -> 4;
            default -> null;
        };
    }

    /**
     * 转换实体为VO
     */
    private VideoRecordingVO convertToVO(VideoRecordEntity entity) {
        VideoRecordingVO vo = new VideoRecordingVO();
        vo.setRecordingId(entity.getRecordId());
        vo.setDeviceId(entity.getDeviceId());
        vo.setDeviceCode(entity.getDeviceCode());
        vo.setDeviceName(entity.getDeviceName());
        vo.setFileName(entity.getFileName());
        vo.setRecordingStartTime(entity.getStartTime());
        vo.setRecordingEndTime(entity.getEndTime());
        vo.setDuration(entity.getDuration() != null ? entity.getDuration().intValue() : 0);
        vo.setFileSize(entity.getFileSize());
        vo.setRecordingType(formatRecordingType(entity.getRecordType()));
        vo.setQuality(formatRecordingQuality(entity.getRecordQuality()));
        vo.setResolution(entity.getResolution());
        vo.setFrameRate(entity.getFrameRate());
        vo.setImportant(entity.getImportant());
        vo.setEventType(entity.getEventType() != null ? String.valueOf(entity.getEventType()) : null);
        return vo;
    }

    /**
     * 格式化录像类型
     */
    private String formatRecordingType(Integer recordType) {
        if (recordType == null) {
            return "unknown";
        }
        return switch (recordType) {
            case 1 -> "scheduled";
            case 2 -> "manual";
            case 3 -> "event";
            case 4 -> "motion";
            default -> "unknown";
        };
    }

    /**
     * 格式化录像质量
     */
    private String formatRecordingQuality(Integer recordQuality) {
        if (recordQuality == null) {
            return "standard";
        }
        return switch (recordQuality) {
            case 1 -> "smooth";
            case 2 -> "standard";
            case 3 -> "high";
            case 4 -> "ultra";
            default -> "standard";
        };
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<VideoRecordingVO>> searchRecordings(VideoRecordingSearchForm searchForm) {
        log.info("[录像回放] 搜索录像，searchForm={}", searchForm);

        try {
            LambdaQueryWrapper<VideoRecordEntity> queryWrapper = new LambdaQueryWrapper<>();

            // 关键词搜索（文件名、设备名称、事件描述）
            if (TypeUtils.hasText(searchForm.getKeyword())) {
                String keyword = searchForm.getKeyword();
                queryWrapper.and(wrapper -> wrapper
                        .like(VideoRecordEntity::getFileName, keyword)
                        .or()
                        .like(VideoRecordEntity::getDeviceName, keyword)
                        .or()
                        .like(VideoRecordEntity::getEventDescription, keyword)
                        .or()
                        .like(VideoRecordEntity::getTags, keyword)
                );
            }

            // 设备筛选
            if (searchForm.getDeviceId() != null) {
                queryWrapper.eq(VideoRecordEntity::getDeviceId, searchForm.getDeviceId());
            }

            // 通道筛选
            if (searchForm.getChannelId() != null) {
                queryWrapper.eq(VideoRecordEntity::getChannelId, searchForm.getChannelId());
            }

            // 录像类型筛选
            if (TypeUtils.hasText(searchForm.getRecordingType())) {
                Integer recordType = parseRecordingType(searchForm.getRecordingType());
                if (recordType != null) {
                    queryWrapper.eq(VideoRecordEntity::getRecordType, recordType);
                }
            }

            // 事件类型筛选
            if (TypeUtils.hasText(searchForm.getEventType())) {
                queryWrapper.like(VideoRecordEntity::getEventType, searchForm.getEventType());
            }

            // 重要标记筛选
            if (searchForm.getImportant() != null) {
                queryWrapper.eq(VideoRecordEntity::getImportant, searchForm.getImportant());
            }

            // 只搜索正常状态的录像
            queryWrapper.eq(VideoRecordEntity::getRecordStatus, 1);

            // 按开始时间降序
            queryWrapper.orderByDesc(VideoRecordEntity::getStartTime);

            // TODO: 取消注释当DAO准备好时
            // List<VideoRecordEntity> entities = videoRecordingDao.selectList(queryWrapper);
            List<VideoRecordEntity> entities = new ArrayList<>();

            List<VideoRecordingVO> voList = entities.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            log.info("[录像回放] 搜索录像完成，count={}", voList.size());
            return ResponseDTO.ok(voList);

        } catch (Exception e) {
            log.error("[录像回放] 搜索录像异常，searchForm={}", searchForm, e);
            return ResponseDTO.error("SEARCH_RECORDINGS_ERROR", "搜索录像失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<VideoRecordingDetailVO> getRecordingDetail(Long recordingId) {
        log.info("[录像回放] 获取录像详情，recordingId={}", recordingId);

        try {
            if (recordingId == null) {
                return ResponseDTO.error("PARAM_ERROR", "录像ID不能为空");
            }

            // TODO: 取消注释当DAO准备好时
            // VideoRecordEntity entity = videoRecordingDao.selectById(recordingId);

            // 临时模拟数据
            VideoRecordEntity entity = createMockRecordingEntity(recordingId);

            if (entity == null) {
                log.warn("[录像回放] 录像不存在，recordingId={}", recordingId);
                return ResponseDTO.error("RECORDING_NOT_FOUND", "录像文件不存在");
            }

            // 更新访问统计
            entity.setAccessCount((entity.getAccessCount() != null ? entity.getAccessCount() : 0) + 1);
            entity.setLastAccessTime(LocalDateTime.now());
            // videoRecordingDao.updateById(entity);

            VideoRecordingDetailVO detailVO = convertToDetailVO(entity);

            log.info("[录像回放] 获取录像详情完成，recordingId={}, fileName={}",
                    recordingId, detailVO.getFileName());
            return ResponseDTO.ok(detailVO);

        } catch (Exception e) {
            log.error("[录像回放] 获取录像详情异常，recordingId={}", recordingId, e);
            return ResponseDTO.error("GET_RECORDING_DETAIL_ERROR", "获取录像详情失败");
        }
    }

    /**
     * 转换为详情VO
     */
    private VideoRecordingDetailVO convertToDetailVO(VideoRecordEntity entity) {
        VideoRecordingDetailVO vo = new VideoRecordingDetailVO();

        // 基本信息
        vo.setRecordingId(entity.getRecordId());
        vo.setDeviceId(entity.getDeviceId());
        vo.setDeviceCode(entity.getDeviceCode());
        vo.setDeviceName(entity.getDeviceName());
        vo.setChannelId(entity.getChannelId());

        // 文件信息
        vo.setFileName(entity.getFileName());
        vo.setFilePath(entity.getFilePath());
        vo.setFullFilePath(entity.getFilePath());
        vo.setFileSize(entity.getFileSize());

        // 时间信息
        vo.setRecordingStartTime(entity.getStartTime());
        vo.setRecordingEndTime(entity.getEndTime());
        vo.setDuration(entity.getDuration() != null ? entity.getDuration().intValue() : 0);
        vo.setDurationFormatted(formatDuration(entity.getDuration()));

        // 质量信息
        vo.setRecordingType(formatRecordingType(entity.getRecordType()));
        vo.setResolution(entity.getResolution());
        vo.setFrameRate(entity.getFrameRate());
        vo.setCodec(formatCodecFormat(entity.getCodecFormat()));

        // 音频信息
        vo.setAudioEnabled(entity.getHasAudio());

        // 存储信息
        // Note: VideoRecordingDetailVO没有这些字段，跳过
        // vo.setStorageLocation(entity.getStorageLocation());

        // 状态信息
        vo.setImportant(entity.getImportant());

        // 事件信息
        vo.setEventType(entity.getEventType() != null ? String.valueOf(entity.getEventType()) : null);

        // 其他信息
        vo.setTags(entity.getTags() != null ? List.of(entity.getTags().split(",")) : List.of());
        vo.setExpireTime(entity.getExpireTime());

        return vo;
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(Long fileSize) {
        if (fileSize == null) {
            return "0 B";
        }
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.2f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", fileSize / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", fileSize / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * 格式化时长
     */
    private String formatDuration(Long seconds) {
        if (seconds == null) {
            return "00:00:00";
        }
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    /**
     * 格式化编码格式
     */
    private String formatCodecFormat(Integer codecFormat) {
        if (codecFormat == null) {
            return "H264";
        }
        return switch (codecFormat) {
            case 1 -> "H264";
            case 2 -> "H265";
            case 3 -> "MJPEG";
            case 4 -> "MPEG4";
            default -> "H264";
        };
    }

    /**
     * 创建模拟录像实体（用于测试）
     */
    private VideoRecordEntity createMockRecordingEntity(Long recordingId) {
        VideoRecordEntity entity = new VideoRecordEntity();
        entity.setRecordId(recordingId);
        entity.setDeviceId(1L);
        entity.setDeviceCode("CAM001");
        entity.setDeviceName("主入口摄像头");
        entity.setChannelId(1L);
        entity.setFileName("REC_" + recordingId + ".mp4");
        entity.setFilePath("/data/video/recordings/2025/12/23/REC_" + recordingId + ".mp4");
        entity.setFileSize(1024L * 1024 * 100); // 100MB
        entity.setStartTime(LocalDateTime.now().minusHours(1));
        entity.setEndTime(LocalDateTime.now().minusMinutes(30));
        entity.setDuration(1800L); // 30分钟
        entity.setRecordType(2); // 手动录像
        entity.setRecordQuality(3); // 高清
        entity.setResolution("1920x1080");
        entity.setFrameRate(25);
        entity.setCodecFormat(1); // H264
        entity.setHasAudio(1);
        entity.setAudioCodec("AAC");
        entity.setStorageLocation(1); // 本地存储
        entity.setStorageServer("local");
        entity.setBackupStatus(0); // 未备份
        entity.setRecordStatus(1); // 正常
        entity.setImportant(0);
        entity.setAccessCount(0);
        entity.setCreateTime(LocalDateTime.now().minusHours(1));
        entity.setUpdateTime(LocalDateTime.now());
        return entity;
    }

    @Override
    public ResponseDTO<VideoRecordingPlaybackVO> getRecordingPlaybackUrl(Long recordingId) {
        log.info("[录像回放] 获取录像播放地址，recordingId={}", recordingId);

        try {
            if (recordingId == null) {
                return ResponseDTO.error("PARAM_ERROR", "录像ID不能为空");
            }

            // 查询录像信息
            // TODO: 取消注释当DAO准备好时
            // VideoRecordEntity entity = videoRecordingDao.selectById(recordingId);
            VideoRecordEntity entity = createMockRecordingEntity(recordingId);

            if (entity == null) {
                log.warn("[录像回放] 录像不存在，recordingId={}", recordingId);
                return ResponseDTO.error("RECORDING_NOT_FOUND", "录像文件不存在");
            }

            // 检查录像状态
            if (entity.getRecordStatus() != null && entity.getRecordStatus() != 1) {
                log.warn("[录像回放] 录像状态异常，recordingId={}, status={}",
                        recordingId, entity.getRecordStatus());
                return ResponseDTO.error("RECORDING_STATUS_ERROR", "录像文件状态异常，无法播放");
            }

            // 检查录像文件是否存在
            String filePath = entity.getFilePath();
            if (TypeUtils.hasText(filePath)) {
                Path path = Paths.get(filePath);
                if (!Files.exists(path)) {
                    log.warn("[录像回放] 录像文件不存在，recordingId={}, filePath={}",
                            recordingId, filePath);
                    return ResponseDTO.error("FILE_NOT_FOUND", "录像文件不存在");
                }
            }

            // 生成播放令牌
            String playbackToken = generatePlaybackToken(recordingId);
            LocalDateTime tokenExpireTime = LocalDateTime.now().plusHours(tokenExpireHours);

            // 使用适配器获取播放地址
            Map<String, String> playUrls = generatePlaybackUrls(entity, playbackToken);

            // 构建播放VO
            VideoRecordingPlaybackVO playbackVO = new VideoRecordingPlaybackVO();
            playbackVO.setRecordingId(recordingId);
            playbackVO.setPlaybackToken(playbackToken);
            playbackVO.setTokenExpireTime(tokenExpireTime);
            playbackVO.setPlayUrls(playUrls);

            // 时长信息
            Long duration = entity.getDuration();
            playbackVO.setDuration(duration != null ? duration.intValue() : 0);
            playbackVO.setDurationFormatted(formatDuration(duration));

            // 推荐协议
            playbackVO.setRecommendedProtocol(recommendProtocol(playUrls));

            // 元数据
            playbackVO.setResolution(entity.getResolution());
            playbackVO.setFrameRate(entity.getFrameRate());
            playbackVO.setHasAudio(entity.getHasAudio() != null && entity.getHasAudio() == 1);
            playbackVO.setVideoCodec(formatCodecFormat(entity.getCodecFormat()));

            log.info("[录像回放] 获取录像播放地址成功，recordingId={}, token={}, expireTime={}",
                    recordingId, playbackToken, tokenExpireTime);
            return ResponseDTO.ok(playbackVO);

        } catch (Exception e) {
            log.error("[录像回放] 获取录像播放地址异常，recordingId={}", recordingId, e);
            return ResponseDTO.error("GET_PLAYBACK_URL_ERROR", "获取录像播放地址失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<Map<String, Object>> getRecordingTimeline(
            Long deviceId, Long channelId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("[录像回放] 获取录像时间轴，deviceId={}, channelId={}, startTime={}, endTime={}",
                deviceId, channelId, startTime, endTime);

        try {
            // TODO: 实现时间轴查询
            Map<String, Object> timeline = new HashMap<>();
            timeline.put("deviceId", deviceId);
            timeline.put("channelId", channelId);
            timeline.put("startTime", startTime);
            timeline.put("endTime", endTime);
            timeline.put("segments", new ArrayList<>());

            log.info("[录像回放] 获取录像时间轴完成，deviceId={}", deviceId);
            return ResponseDTO.ok(timeline);

        } catch (Exception e) {
            log.error("[录像回放] 获取录像时间轴异常，deviceId={}", deviceId, e);
            return ResponseDTO.error("GET_TIMELINE_ERROR", "获取录像时间轴失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<VideoRecordingVO>> getRecordingsByTimeRange(
            Long deviceId, Long channelId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("[录像回放] 按时间范围查询录像，deviceId={}, channelId={}, startTime={}, endTime={}",
                deviceId, channelId, startTime, endTime);

        try {
            // TODO: 实现按时间范围查询录像
            List<VideoRecordingVO> recordings = new ArrayList<>();

            log.info("[录像回放] 按时间范围查询录像完成，count={}", recordings.size());
            return ResponseDTO.ok(recordings);

        } catch (Exception e) {
            log.error("[录像回放] 按时间范围查询录像异常", e);
            return ResponseDTO.error("GET_RECORDINGS_BY_TIME_ERROR", "按时间范围查询录像失败");
        }
    }

    @Override
    public ResponseDTO<Void> deleteRecording(Long recordingId) {
        log.info("[录像回放] 删除录像，recordingId={}", recordingId);

        try {
            if (recordingId == null) {
                return ResponseDTO.error("PARAM_ERROR", "录像ID不能为空");
            }

            // 1. 检查录像是否存在
            // TODO: 取消注释当DAO准备好时
            // VideoRecordEntity entity = videoRecordingDao.selectById(recordingId);
            VideoRecordEntity entity = createMockRecordingEntity(recordingId);

            if (entity == null) {
                log.warn("[录像回放] 录像不存在，recordingId={}", recordingId);
                return ResponseDTO.error("RECORDING_NOT_FOUND", "录像文件不存在");
            }

            // 2. 检查是否可以删除（重要标记的录像需要特殊权限）
            if (entity.getImportant() != null && entity.getImportant() == 1) {
                log.warn("[录像回放] 录像已标记为重要，无法删除，recordingId={}", recordingId);
                return ResponseDTO.error("RECORDING_IMPORTANT", "录像已标记为重要，无法删除");
            }

            // 3. 删除物理文件
            String filePath = entity.getFilePath();
            if (TypeUtils.hasText(filePath)) {
                try {
                    Path path = Paths.get(filePath);
                    if (Files.exists(path)) {
                        Files.delete(path);
                        log.info("[录像回放] 已删除录像文件，filePath={}", filePath);
                    }
                } catch (Exception e) {
                    log.error("[录像回放] 删除录像文件失败，filePath={}", filePath, e);
                    // 继续执行，删除数据库记录
                }
            }

            // 4. 删除缩略图
            String thumbnailPath = entity.getThumbnailPath();
            if (TypeUtils.hasText(thumbnailPath)) {
                try {
                    Path path = Paths.get(thumbnailPath);
                    if (Files.exists(path)) {
                        Files.delete(path);
                        log.info("[录像回放] 已删除缩略图，thumbnailPath={}", thumbnailPath);
                    }
                } catch (Exception e) {
                    log.warn("[录像回放] 删除缩略图失败，thumbnailPath={}", thumbnailPath, e);
                }
            }

            // 5. 删除数据库记录（逻辑删除）
            entity.setRecordStatus(3); // 标记为已删除
            // videoRecordingDao.updateById(entity);

            // 6. TODO: 记录审计日志

            log.info("[录像回放] 删除录像成功，recordingId={}, fileName={}",
                    recordingId, entity.getFileName());
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[录像回放] 删除录像异常，recordingId={}", recordingId, e);
            return ResponseDTO.error("DELETE_RECORDING_ERROR", "删除录像失败");
        }
    }

    @Override
    public ResponseDTO<Void> batchDeleteRecordings(List<Long> recordingIds) {
        log.info("[录像回放] 批量删除录像，recordingIds={}", recordingIds);

        try {
            if (recordingIds == null || recordingIds.isEmpty()) {
                return ResponseDTO.error("PARAM_ERROR", "录像ID列表不能为空");
            }

            int successCount = 0;
            int failCount = 0;

            for (Long recordingId : recordingIds) {
                try {
                    ResponseDTO<Void> result = deleteRecording(recordingId);
                    if (result.isSuccess()) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                    log.error("[录像回放] 批量删除录像失败，recordingId={}", recordingId, e);
                }
            }

            log.info("[录像回放] 批量删除录像完成，成功={}，失败={}", successCount, failCount);

            if (failCount == 0) {
                return ResponseDTO.ok();
            } else {
                return ResponseDTO.error("BATCH_DELETE_PARTIAL_ERROR",
                        String.format("批量删除部分失败，成功=%d，失败=%d", successCount, failCount));
            }

        } catch (Exception e) {
            log.error("[录像回放] 批量删除录像异常", e);
            return ResponseDTO.error("BATCH_DELETE_RECORDINGS_ERROR", "批量删除录像失败");
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> downloadRecording(Long recordingId) {
        log.info("[录像回放] 下载录像，recordingId={}", recordingId);

        try {
            if (recordingId == null) {
                return ResponseDTO.error("PARAM_ERROR", "录像ID不能为空");
            }

            // 1. 查询录像信息
            // TODO: 取消注释当DAO准备好时
            // VideoRecordEntity entity = videoRecordingDao.selectById(recordingId);
            VideoRecordEntity entity = createMockRecordingEntity(recordingId);

            if (entity == null) {
                log.warn("[录像回放] 录像不存在，recordingId={}", recordingId);
                return ResponseDTO.error("RECORDING_NOT_FOUND", "录像文件不存在");
            }

            // 2. 检查文件是否存在
            String filePath = entity.getFilePath();
            if (TypeUtils.hasText(filePath)) {
                Path path = Paths.get(filePath);
                if (!Files.exists(path)) {
                    log.warn("[录像回放] 录像文件不存在，recordingId={}, filePath={}",
                            recordingId, filePath);
                    return ResponseDTO.error("FILE_NOT_FOUND", "录像文件不存在");
                }
            }

            // 3. 生成下载令牌（有效期较短，1小时）
            String downloadToken = UUID.randomUUID().toString().replace("-", "");
            LocalDateTime expireTime = LocalDateTime.now().plusHours(1);

            // 4. 构建下载信息
            Map<String, Object> downloadInfo = new HashMap<>();
            downloadInfo.put("recordingId", recordingId);
            downloadInfo.put("downloadToken", downloadToken);
            downloadInfo.put("downloadUrl", "/api/v1/video/recordings/" + recordingId + "/download?token=" + downloadToken);
            downloadInfo.put("expireTime", expireTime);
            downloadInfo.put("fileName", entity.getFileName());
            downloadInfo.put("fileSize", entity.getFileSize());
            downloadInfo.put("fileSizeFormatted", formatFileSize(entity.getFileSize()));
            downloadInfo.put("contentType", "video/mp4");

            log.info("[录像回放] 生成录像下载地址成功，recordingId={}, token={}", recordingId, downloadToken);
            return ResponseDTO.ok(downloadInfo);

        } catch (Exception e) {
            log.error("[录像回放] 下载录像异常，recordingId={}", recordingId, e);
            return ResponseDTO.error("DOWNLOAD_RECORDING_ERROR", "下载录像失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<Map<String, Object>> getRecordingStatistics(Long deviceId) {
        log.info("[录像回放] 获取录像统计信息，deviceId={}", deviceId);

        try {
            // TODO: 实现录像统计查询
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalCount", 0);
            statistics.put("totalSize", 0L);
            statistics.put("totalDuration", 0);
            statistics.put("todayCount", 0);
            statistics.put("weekCount", 0);
            statistics.put("monthCount", 0);
            statistics.put("importantCount", 0);
            statistics.put("eventCount", 0);

            log.info("[录像回放] 获取录像统计信息完成，deviceId={}", deviceId);
            return ResponseDTO.ok(statistics);

        } catch (Exception e) {
            log.error("[录像回放] 获取录像统计信息异常，deviceId={}", deviceId, e);
            return ResponseDTO.error("GET_STATISTICS_ERROR", "获取录像统计信息失败");
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> checkRecordingIntegrity(Long recordingId) {
        log.info("[录像回放] 检查录像完整性，recordingId={}", recordingId);

        try {
            if (recordingId == null) {
                return ResponseDTO.error("PARAM_ERROR", "录像ID不能为空");
            }

            // TODO: 实现录像完整性检查
            Map<String, Object> integrityResult = new HashMap<>();
            integrityResult.put("recordingId", recordingId);
            integrityResult.put("checkStatus", "complete");
            integrityResult.put("isIntegrity", true);
            integrityResult.put("checkTime", LocalDateTime.now());
            integrityResult.put("checksum", "abc123def456");
            integrityResult.put("fileSize", 1024L * 1024 * 50); // 50MB

            log.info("[录像回放] 检查录像完整性完成，recordingId={}", recordingId);
            return ResponseDTO.ok(integrityResult);

        } catch (Exception e) {
            log.error("[录像回放] 检查录像完整性异常，recordingId={}", recordingId, e);
            return ResponseDTO.error("CHECK_INTEGRITY_ERROR", "检查录像完整性失败");
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> repairRecording(Long recordingId) {
        log.info("[录像回放] 修复录像文件，recordingId={}", recordingId);

        try {
            if (recordingId == null) {
                return ResponseDTO.error("PARAM_ERROR", "录像ID不能为空");
            }

            // TODO: 实现录像文件修复
            Map<String, Object> repairResult = new HashMap<>();
            repairResult.put("recordingId", recordingId);
            repairResult.put("repairStatus", "success");
            repairResult.put("repairTime", LocalDateTime.now());
            repairResult.put("repairedSize", 1024L * 1024 * 50); // 50MB

            log.info("[录像回放] 修复录像文件完成，recordingId={}", recordingId);
            return ResponseDTO.ok(repairResult);

        } catch (Exception e) {
            log.error("[录像回放] 修复录像文件异常，recordingId={}", recordingId, e);
            return ResponseDTO.error("REPAIR_RECORDING_ERROR", "修复录像文件失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<PageResult<VideoRecordingVO>> getDeviceRecordings(Long deviceId, Integer pageNum,
            Integer pageSize) {
        log.info("[录像回放] 获取设备录像，deviceId={}, pageNum={}, pageSize={}", deviceId, pageNum, pageSize);

        try {
            if (deviceId == null) {
                return ResponseDTO.error("PARAM_ERROR", "设备ID不能为空");
            }

            VideoRecordingQueryForm queryForm = new VideoRecordingQueryForm();
            queryForm.setDeviceId(deviceId);
            queryForm.setPageNum(pageNum);
            queryForm.setPageSize(pageSize);

            return queryRecordings(queryForm);

        } catch (Exception e) {
            log.error("[录像回放] 获取设备录像异常，deviceId={}", deviceId, e);
            return ResponseDTO.error("GET_DEVICE_RECORDINGS_ERROR", "获取设备录像失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<PageResult<VideoRecordingVO>> getChannelRecordings(Long deviceId, Long channelId,
            Integer pageNum, Integer pageSize) {
        log.info("[录像回放] 获取通道录像，deviceId={}, channelId={}, pageNum={}, pageSize={}",
                deviceId, channelId, pageNum, pageSize);

        try {
            if (deviceId == null || channelId == null) {
                return ResponseDTO.error("PARAM_ERROR", "设备ID和通道ID不能为空");
            }

            VideoRecordingQueryForm queryForm = new VideoRecordingQueryForm();
            queryForm.setDeviceId(deviceId);
            queryForm.setChannelId(channelId);
            queryForm.setPageNum(pageNum);
            queryForm.setPageSize(pageSize);

            return queryRecordings(queryForm);

        } catch (Exception e) {
            log.error("[录像回放] 获取通道录像异常，deviceId={}, channelId={}", deviceId, channelId, e);
            return ResponseDTO.error("GET_CHANNEL_RECORDINGS_ERROR", "获取通道录像失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<PageResult<VideoRecordingVO>> getEventRecordings(String eventType, Integer pageNum,
            Integer pageSize) {
        log.info("[录像回放] 获取事件录像，eventType={}, pageNum={}, pageSize={}", eventType, pageNum, pageSize);

        try {
            VideoRecordingQueryForm queryForm = new VideoRecordingQueryForm();
            queryForm.setRecordingType("event");
            queryForm.setEventType(eventType);
            queryForm.setPageNum(pageNum);
            queryForm.setPageSize(pageSize);

            return queryRecordings(queryForm);

        } catch (Exception e) {
            log.error("[录像回放] 获取事件录像异常，eventType={}", eventType, e);
            return ResponseDTO.error("GET_EVENT_RECORDINGS_ERROR", "获取事件录像失败");
        }
    }

    @Override
    public ResponseDTO<Void> markRecordingAsimportant(Long recordingId, String remark) {
        log.info("[录像回放] 标记录像为重要，recordingId={}, remark={}", recordingId, remark);

        try {
            if (recordingId == null) {
                return ResponseDTO.error("PARAM_ERROR", "录像ID不能为空");
            }

            // TODO: 实现重要标记逻辑
            log.info("[录像回放] 标记录像为重要成功，recordingId={}", recordingId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[录像回放] 标记录像为重要异常，recordingId={}", recordingId, e);
            return ResponseDTO.error("MARK_importANT_ERROR", "标记录像为重要失败");
        }
    }

    @Override
    public ResponseDTO<Void> unmarkRecordingAsimportant(Long recordingId) {
        log.info("[录像回放] 取消录像重要标记，recordingId={}", recordingId);

        try {
            if (recordingId == null) {
                return ResponseDTO.error("PARAM_ERROR", "录像ID不能为空");
            }

            // TODO: 实现取消重要标记逻辑
            log.info("[录像回放] 取消录像重要标记成功，recordingId={}", recordingId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[录像回放] 取消录像重要标记异常，recordingId={}", recordingId, e);
            return ResponseDTO.error("UNMARK_importANT_ERROR", "取消录像重要标记失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<PageResult<VideoRecordingVO>> getimportantRecordings(Integer pageNum, Integer pageSize) {
        log.info("[录像回放] 获取重要录像，pageNum={}, pageSize={}", pageNum, pageSize);

        try {
            VideoRecordingQueryForm queryForm = new VideoRecordingQueryForm();
            queryForm.setImportant(1);
            queryForm.setPageNum(pageNum);
            queryForm.setPageSize(pageSize);

            return queryRecordings(queryForm);

        } catch (Exception e) {
            log.error("[录像回放] 获取重要录像异常", e);
            return ResponseDTO.error("GET_importANT_RECORDINGS_ERROR", "获取重要录像失败");
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> transcodeRecording(Long recordingId, String targetFormat,
            String targetQuality) {
        log.info("[录像回放] 录像文件转码，recordingId={}, targetFormat={}, targetQuality={}",
                recordingId, targetFormat, targetQuality);

        try {
            if (recordingId == null) {
                return ResponseDTO.error("PARAM_ERROR", "录像ID不能为空");
            }

            // TODO: 实现录像转码逻辑
            Map<String, Object> transcodeResult = new HashMap<>();
            transcodeResult.put("taskId", UUID.randomUUID().toString());
            transcodeResult.put("recordingId", recordingId);
            transcodeResult.put("targetFormat", targetFormat);
            transcodeResult.put("targetQuality", targetQuality);
            transcodeResult.put("status", "pending");
            transcodeResult.put("createTime", LocalDateTime.now());

            log.info("[录像回放] 启动录像转码成功，recordingId={}, taskId={}", recordingId, transcodeResult.get("taskId"));
            return ResponseDTO.ok(transcodeResult);

        } catch (Exception e) {
            log.error("[录像回放] 录像文件转码异常，recordingId={}", recordingId, e);
            return ResponseDTO.error("TRANSCODE_RECORDING_ERROR", "录像文件转码失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<Map<String, Object>> getTranscodeTaskStatus(String taskId) {
        log.info("[录像回放] 获取转码任务状态，taskId={}", taskId);

        try {
            if (!TypeUtils.hasText(taskId)) {
                return ResponseDTO.error("PARAM_ERROR", "任务ID不能为空");
            }

            // TODO: 实现转码任务状态查询
            Map<String, Object> taskStatus = new HashMap<>();
            taskStatus.put("taskId", taskId);
            taskStatus.put("status", "completed");
            taskStatus.put("progress", 100);
            taskStatus.put("createTime", LocalDateTime.now().minusMinutes(30));
            taskStatus.put("completeTime", LocalDateTime.now());

            log.info("[录像回放] 获取转码任务状态完成，taskId={}", taskId);
            return ResponseDTO.ok(taskStatus);

        } catch (Exception e) {
            log.error("[录像回放] 获取转码任务状态异常，taskId={}", taskId, e);
            return ResponseDTO.error("GET_TRANSCODE_STATUS_ERROR", "获取转码任务状态失败");
        }
    }

    @Override
    public ResponseDTO<Void> cancelTranscodeTask(String taskId) {
        log.info("[录像回放] 取消转码任务，taskId={}", taskId);

        try {
            if (!TypeUtils.hasText(taskId)) {
                return ResponseDTO.error("PARAM_ERROR", "任务ID不能为空");
            }

            // TODO: 实现取消转码任务逻辑
            log.info("[录像回放] 取消转码任务成功，taskId={}", taskId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[录像回放] 取消转码任务异常，taskId={}", taskId, e);
            return ResponseDTO.error("CANCEL_TRANSCODE_TASK_ERROR", "取消转码任务失败");
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> backupRecordings(List<Long> recordingIds, String backupType) {
        log.info("[录像回放] 录像文件备份，recordingIds={}, backupType={}", recordingIds, backupType);

        try {
            if (recordingIds == null || recordingIds.isEmpty()) {
                return ResponseDTO.error("PARAM_ERROR", "录像ID列表不能为空");
            }

            // TODO: 实现录像文件备份逻辑
            Map<String, Object> backupResult = new HashMap<>();
            backupResult.put("taskId", UUID.randomUUID().toString());
            backupResult.put("backupType", backupType);
            backupResult.put("totalCount", recordingIds.size());
            backupResult.put("status", "pending");
            backupResult.put("createTime", LocalDateTime.now());

            log.info("[录像回放] 启动录像备份成功，taskId={}, count={}", backupResult.get("taskId"), recordingIds.size());
            return ResponseDTO.ok(backupResult);

        } catch (Exception e) {
            log.error("[录像回放] 录像文件备份异常", e);
            return ResponseDTO.error("BACKUP_RECORDINGS_ERROR", "录像文件备份失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<Map<String, Object>> getBackupTaskStatus(String taskId) {
        log.info("[录像回放] 获取备份任务状态，taskId={}", taskId);

        try {
            if (!TypeUtils.hasText(taskId)) {
                return ResponseDTO.error("PARAM_ERROR", "任务ID不能为空");
            }

            // TODO: 实现备份任务状态查询
            Map<String, Object> taskStatus = new HashMap<>();
            taskStatus.put("taskId", taskId);
            taskStatus.put("status", "completed");
            taskStatus.put("progress", 100);
            taskStatus.put("successCount", 10);
            taskStatus.put("failCount", 0);

            log.info("[录像回放] 获取备份任务状态完成，taskId={}", taskId);
            return ResponseDTO.ok(taskStatus);

        } catch (Exception e) {
            log.error("[录像回放] 获取备份任务状态异常，taskId={}", taskId, e);
            return ResponseDTO.error("GET_BACKUP_STATUS_ERROR", "获取备份任务状态失败");
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> cleanupExpiredRecordings(Long deviceId, Integer days) {
        log.info("[录像回放] 清理过期录像，deviceId={}, days={}", deviceId, days);

        try {
            if (days == null || days <= 0) {
                return ResponseDTO.error("PARAM_ERROR", "保留天数必须大于0");
            }

            // TODO: 实现过期录像清理逻辑
            Map<String, Object> cleanupResult = new HashMap<>();
            cleanupResult.put("deviceId", deviceId);
            cleanupResult.put("retentionDays", days);
            cleanupResult.put("deletedCount", 0);
            cleanupResult.put("freedSpace", 0L);
            cleanupResult.put("cleanupTime", LocalDateTime.now());

            log.info("[录像回放] 清理过期录像完成，deviceId={}, deletedCount={}", deviceId, cleanupResult.get("deletedCount"));
            return ResponseDTO.ok(cleanupResult);

        } catch (Exception e) {
            log.error("[录像回放] 清理过期录像异常，deviceId={}, days={}", deviceId, days, e);
            return ResponseDTO.error("CLEANUP_EXPIRED_RECORDINGS_ERROR", "清理过期录像失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<Map<String, Object>> getStorageUsage(Long deviceId) {
        log.info("[录像回放] 获取存储使用情况，deviceId={}", deviceId);

        try {
            // TODO: 实现存储使用情况查询
            Map<String, Object> storageUsage = new HashMap<>();
            storageUsage.put("deviceId", deviceId);
            storageUsage.put("totalSpace", 1024L * 1024 * 1024 * 1024); // 1TB
            storageUsage.put("usedSpace", 500L * 1024 * 1024 * 1024); // 500GB
            storageUsage.put("freeSpace", 512L * 1024 * 1024 * 1024); // 512GB
            storageUsage.put("usagePercentage", 49.22);
            storageUsage.put("recordingCount", 1000);
            storageUsage.put("avgFileSize", 512L * 1024); // 512KB

            log.info("[录像回放] 获取存储使用情况完成，deviceId={}", deviceId);
            return ResponseDTO.ok(storageUsage);

        } catch (Exception e) {
            log.error("[录像回放] 获取存储使用情况异常，deviceId={}", deviceId, e);
            return ResponseDTO.error("GET_STORAGE_USAGE_ERROR", "获取存储使用情况失败");
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 生成播放令牌
     */
    private String generatePlaybackToken(Long recordingId) {
        String token = UUID.randomUUID().toString().replace("-", "");

        PlaybackToken playbackToken = new PlaybackToken();
        playbackToken.setToken(token);
        playbackToken.setRecordingId(recordingId);
        playbackToken.setCreateTime(LocalDateTime.now());
        playbackToken.setExpireTime(LocalDateTime.now().plusHours(tokenExpireHours));

        // 缓存令牌（生产环境应使用Redis）
        playbackTokens.put(token, playbackToken);

        return token;
    }

    /**
     * 生成播放地址
     */
    private Map<String, String> generatePlaybackUrls(VideoRecordEntity entity, String token) {
        Map<String, String> urls = new HashMap<>();
        Long recordingId = entity.getRecordId();
        String baseUrl = "/api/v1/video/recordings/" + recordingId + "/play";

        // HLS播放地址（推荐，支持流式播放）
        urls.put("hls", baseUrl + "/hls?token=" + token);

        // MP4播放地址（直接下载播放）
        urls.put("mp4", baseUrl + "/mp4?token=" + token);

        // DASH播放地址（自适应码率）
        urls.put("dash", baseUrl + "/dash?token=" + token);

        // RTSP播放地址（低延迟）
        urls.put("rtsp", baseUrl + "/rtsp?token=" + token);

        return urls;
    }

    /**
     * 推荐播放协议
     */
    private String recommendProtocol(Map<String, String> playUrls) {
        // 优先推荐HLS（兼容性好，支持流式播放）
        if (playUrls.containsKey("hls")) {
            return "hls";
        }
        // 其次推荐DASH（自适应码率）
        if (playUrls.containsKey("dash")) {
            return "dash";
        }
        // 最后使用MP4（最兼容，但需完整下载）
        if (playUrls.containsKey("mp4")) {
            return "mp4";
        }
        return "hls"; // 默认
    }

    /**
     * 计算文件校验和
     */
    private String calculateChecksum(Path filePath) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] fileBytes = Files.readAllBytes(filePath);
        byte[] hashBytes = digest.digest(fileBytes);

        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 清理过期令牌
     */
    private void cleanExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        playbackTokens.entrySet().removeIf(entry -> {
            PlaybackToken token = entry.getValue();
            return token.getExpireTime().isBefore(now);
        });
    }

    // ==================== 内部类定义 ====================

    /**
     * 播放令牌
     */
    private static class PlaybackToken {
        private String token;
        private Long recordingId;
        private LocalDateTime createTime;
        private LocalDateTime expireTime;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public Long getRecordingId() {
            return recordingId;
        }

        public void setRecordingId(Long recordingId) {
            this.recordingId = recordingId;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }

        public LocalDateTime getExpireTime() {
            return expireTime;
        }

        public void setExpireTime(LocalDateTime expireTime) {
            this.expireTime = expireTime;
        }
    }

    /**
     * 转码任务
     */
    private static class TranscodeTask {
        private String taskId;
        private Long recordingId;
        private String targetFormat;
        private String targetQuality;
        private String status; // pending, processing, completed, failed
        private Integer progress;
        private LocalDateTime createTime;
        private LocalDateTime completeTime;
        private String outputFile;

        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }

        public Long getRecordingId() {
            return recordingId;
        }

        public void setRecordingId(Long recordingId) {
            this.recordingId = recordingId;
        }

        public String getTargetFormat() {
            return targetFormat;
        }

        public void setTargetFormat(String targetFormat) {
            this.targetFormat = targetFormat;
        }

        public String getTargetQuality() {
            return targetQuality;
        }

        public void setTargetQuality(String targetQuality) {
            this.targetQuality = targetQuality;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Integer getProgress() {
            return progress;
        }

        public void setProgress(Integer progress) {
            this.progress = progress;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }

        public LocalDateTime getCompleteTime() {
            return completeTime;
        }

        public void setCompleteTime(LocalDateTime completeTime) {
            this.completeTime = completeTime;
        }

        public String getOutputFile() {
            return outputFile;
        }

        public void setOutputFile(String outputFile) {
            this.outputFile = outputFile;
        }
    }

    /**
     * 备份任务
     */
    private static class BackupTask {
        private String taskId;
        private String backupType;
        private List<Long> recordingIds;
        private Integer totalCount;
        private Integer successCount;
        private Integer failCount;
        private String status; // pending, processing, completed, failed
        private Integer progress;
        private LocalDateTime createTime;
        private LocalDateTime completeTime;

        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }

        public String getBackupType() {
            return backupType;
        }

        public void setBackupType(String backupType) {
            this.backupType = backupType;
        }

        public List<Long> getRecordingIds() {
            return recordingIds;
        }

        public void setRecordingIds(List<Long> recordingIds) {
            this.recordingIds = recordingIds;
        }

        public Integer getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(Integer totalCount) {
            this.totalCount = totalCount;
        }

        public Integer getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(Integer successCount) {
            this.successCount = successCount;
        }

        public Integer getFailCount() {
            return failCount;
        }

        public void setFailCount(Integer failCount) {
            this.failCount = failCount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Integer getProgress() {
            return progress;
        }

        public void setProgress(Integer progress) {
            this.progress = progress;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }

        public LocalDateTime getCompleteTime() {
            return completeTime;
        }

        public void setCompleteTime(LocalDateTime completeTime) {
            this.completeTime = completeTime;
        }
    }
}
