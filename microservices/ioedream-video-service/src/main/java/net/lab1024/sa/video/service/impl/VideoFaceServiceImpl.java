package net.lab1024.sa.video.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.openapi.domain.response.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.util.StringUtils;
import net.lab1024.sa.video.domain.form.VideoFaceAddForm;
import net.lab1024.sa.video.domain.form.VideoFaceSearchForm;
import net.lab1024.sa.video.domain.vo.VideoFaceVO;
import net.lab1024.sa.video.service.VideoFaceService;
import net.lab1024.sa.video.entity.VideoFaceEntity;
import net.lab1024.sa.video.entity.VideoFaceDetectionEntity;
import net.lab1024.sa.video.manager.VideoFaceManager;
import net.lab1024.sa.video.dao.VideoFaceDao;
import net.lab1024.sa.video.dao.VideoFaceDetectionDao;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 人脸识别服务实现类
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class VideoFaceServiceImpl implements VideoFaceService {

    @Resource
    private VideoFaceDao videoFaceDao;

    @Resource
    private VideoFaceDetectionDao videoFaceDetectionDao;

    @Resource
    private VideoFaceManager videoFaceManager;

    @Override
    public ResponseDTO<VideoFaceVO> addFace(@Valid VideoFaceAddForm addForm) {
        log.info("[人脸识别] 添加人脸，personCode={}, personName={}", addForm.getPersonCode(), addForm.getPersonName());

        try {
            // 转换为实体对象
            VideoFaceEntity faceEntity = new VideoFaceEntity();
            BeanUtils.copyProperties(addForm, faceEntity);

            // 设置采集时间（如果未设置）
            if (faceEntity.getCaptureTime() == null) {
                faceEntity.setCaptureTime(LocalDateTime.now());
            }

            // 调用Manager层添加人脸
            VideoFaceEntity savedFace = videoFaceManager.addFace(faceEntity);

            // 转换为VO对象
            VideoFaceVO faceVO = convertToVO(savedFace);

            log.info("[人脸识别] 添加人脸成功，faceId={}, personCode={}", savedFace.getFaceId(), savedFace.getPersonCode());
            return ResponseDTO.ok(faceVO);

        } catch (Exception e) {
            log.error("[人脸识别] 添加人脸失败，personCode={}, error={}", addForm.getPersonCode(), e.getMessage(), e);
            return ResponseDTO.error("FACE_ADD_ERROR", "添加人脸失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<VideoFaceVO> updateFace(Long faceId, @Valid VideoFaceAddForm updateForm) {
        log.info("[人脸识别] 更新人脸信息，faceId={}", faceId);

        try {
            // 获取现有人脸信息
            VideoFaceEntity existingFace = videoFaceDao.selectById(faceId);
            if (existingFace == null) {
                return ResponseDTO.error("FACE_NOT_FOUND", "人脸不存在");
            }

            // 转换为实体对象
            VideoFaceEntity faceEntity = new VideoFaceEntity();
            BeanUtils.copyProperties(updateForm, faceEntity);
            faceEntity.setFaceId(faceId);

            // 调用Manager层更新人脸
            VideoFaceEntity updatedFace = videoFaceManager.updateFace(faceEntity);

            // 转换为VO对象
            VideoFaceVO faceVO = convertToVO(updatedFace);

            log.info("[人脸识别] 更新人脸信息成功，faceId={}", faceId);
            return ResponseDTO.ok(faceVO);

        } catch (Exception e) {
            log.error("[人脸识别] 更新人脸信息失败，faceId={}, error={}", faceId, e.getMessage(), e);
            return ResponseDTO.error("FACE_UPDATE_ERROR", "更新人脸信息失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> deleteFace(Long faceId) {
        log.info("[人脸识别] 删除人脸，faceId={}", faceId);

        try {
            boolean success = videoFaceManager.deleteFace(faceId);
            if (!success) {
                return ResponseDTO.error("FACE_NOT_FOUND", "人脸不存在");
            }

            log.info("[人脸识别] 删除人脸成功，faceId={}", faceId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[人脸识别] 删除人脸失败，faceId={}, error={}", faceId, e.getMessage(), e);
            return ResponseDTO.error("FACE_DELETE_ERROR", "删除人脸失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> batchDeleteFaces(List<Long> faceIds) {
        log.info("[人脸识别] 批量删除人脸，faceIds={}", faceIds);

        try {
            if (faceIds == null || faceIds.isEmpty()) {
                return ResponseDTO.error("PARAM_ERROR", "人脸ID列表不能为空");
            }

            int successCount = 0;
            List<String> failedIds = new ArrayList<>();

            for (Long faceId : faceIds) {
                try {
                    boolean success = videoFaceManager.deleteFace(faceId);
                    if (success) {
                        successCount++;
                    } else {
                        failedIds.add(String.valueOf(faceId));
                    }
                } catch (Exception e) {
                    failedIds.add(String.valueOf(faceId));
                    log.warn("[人脸识别] 删除人脸失败，faceId={}, error={}", faceId, e.getMessage());
                }
            }

            log.info("[人脸识别] 批量删除人脸完成，总数={}, 成功={}, 失败={}", faceIds.size(), successCount, failedIds.size());

            if (failedIds.isEmpty()) {
                return ResponseDTO.ok();
            } else {
                return ResponseDTO.error("PARTIAL_SUCCESS", String.format("部分删除失败，成功：%d，失败：%d", successCount, failedIds.size()));
            }

        } catch (Exception e) {
            log.error("[人脸识别] 批量删除人脸失败，faceIds={}, error={}", faceIds, e.getMessage(), e);
            return ResponseDTO.error("BATCH_DELETE_ERROR", "批量删除人脸失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> deleteFacesByPersonId(Long personId) {
        log.info("[人脸识别] 删除人员的所有人脸，personId={}", personId);

        try {
            int deletedCount = videoFaceManager.deleteFacesByPersonId(personId);
            log.info("[人脸识别] 删除人员的所有人脸完成，personId={}, 删除数量={}", personId, deletedCount);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[人脸识别] 删除人员的所有人脸失败，personId={}, error={}", personId, e.getMessage(), e);
            return ResponseDTO.error("DELETE_BY_PERSON_ERROR", "删除人员人脸失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<VideoFaceVO> getFaceDetail(Long faceId) {
        log.info("[人脸识别] 获取人脸详情，faceId={}", faceId);

        try {
            VideoFaceEntity faceEntity = videoFaceDao.selectById(faceId);
            if (faceEntity == null) {
                return ResponseDTO.error("FACE_NOT_FOUND", "人脸不存在");
            }

            VideoFaceVO faceVO = convertToVO(faceEntity);
            return ResponseDTO.ok(faceVO);

        } catch (Exception e) {
            log.error("[人脸识别] 获取人脸详情失败，faceId={}, error={}", faceId, e.getMessage(), e);
            return ResponseDTO.error("GET_FACE_DETAIL_ERROR", "获取人脸详情失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<VideoFaceVO>> getFacesByPersonId(Long personId) {
        log.info("[人脸识别] 根据人员ID查询人脸，personId={}", personId);

        try {
            List<VideoFaceEntity> faceEntities = videoFaceManager.getFacesByPersonId(personId);
            List<VideoFaceVO> faceVOs = faceEntities.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            return ResponseDTO.ok(faceVOs);

        } catch (Exception e) {
            log.error("[人脸识别] 根据人员ID查询人脸失败，personId={}, error={}", personId, e.getMessage(), e);
            return ResponseDTO.error("GET_FACES_BY_PERSON_ERROR", "查询人员人脸失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<VideoFaceVO>> getFacesByPersonCode(String personCode) {
        log.info("[人脸识别] 根据人员编号查询人脸，personCode={}", personCode);

        try {
            List<VideoFaceEntity> faceEntities = videoFaceManager.getFacesByPersonCode(personCode);
            List<VideoFaceVO> faceVOs = faceEntities.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            return ResponseDTO.ok(faceVOs);

        } catch (Exception e) {
            log.error("[人脸识别] 根据人员编号查询人脸失败，personCode={}, error={}", personCode, e.getMessage(), e);
            return ResponseDTO.error("GET_FACES_BY_CODE_ERROR", "查询人员编号人脸失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<PageResult<VideoFaceVO>> pageFaces(PageParam pageParam, String personCode, String personName,
                                                           Integer personType, Long departmentId, Integer faceStatus) {
        log.info("[人脸识别] 分页查询人脸，page={}, personCode={}, personName={}", pageParam.getPageNum(), personCode, personName);

        try {
            // 构建查询条件
            LambdaQueryWrapper<VideoFaceEntity> queryWrapper = new LambdaQueryWrapper<>();

            if (StringUtils.isNotBlank(personCode)) {
                queryWrapper.like(VideoFaceEntity::getPersonCode, personCode);
            }
            if (StringUtils.isNotBlank(personName)) {
                queryWrapper.like(VideoFaceEntity::getPersonName, personName);
            }
            if (personType != null) {
                queryWrapper.eq(VideoFaceEntity::getPersonType, personType);
            }
            if (departmentId != null) {
                queryWrapper.eq(VideoFaceEntity::getDepartmentId, departmentId);
            }
            if (faceStatus != null) {
                queryWrapper.eq(VideoFaceEntity::getFaceStatus, faceStatus);
            }

            queryWrapper.orderByDesc(VideoFaceEntity::getCreateTime);

            // 执行分页查询
            IPage<VideoFaceEntity> page = videoFaceDao.selectPage(new Page<>(pageParam.getPageNum(), pageParam.getPageSize()), queryWrapper);

            // 转换为VO对象
            List<VideoFaceVO> faceVOs = page.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            PageResult<VideoFaceVO> pageResult = new PageResult<>();
            pageResult.setList(faceVOs);
            pageResult.setTotal(page.getTotal());
            pageResult.setPageNum((int) page.getCurrent());
            pageResult.setPageSize((int) page.getSize());
            pageResult.setPages((int) page.getPages());

            return ResponseDTO.ok(pageResult);

        } catch (Exception e) {
            log.error("[人脸识别] 分页查询人脸失败，error={}", e.getMessage(), e);
            return ResponseDTO.error("PAGE_FACES_ERROR", "分页查询人脸失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> getFaceLibraryStatistics() {
        log.info("[人脸识别] 获取人脸库统计信息");

        try {
            Map<String, Object> statistics = videoFaceManager.getFaceLibraryStatistics();
            return ResponseDTO.ok(statistics);

        } catch (Exception e) {
            log.error("[人脸识别] 获取人脸库统计信息失败，error={}", e.getMessage(), e);
            return ResponseDTO.error("GET_STATISTICS_ERROR", "获取统计信息失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<VideoFaceVO>> searchFaces(String keyword, Integer limit) {
        log.info("[人脸识别] 搜索人脸，keyword={}, limit={}", keyword, limit);

        try {
            if (StringUtils.isBlank(keyword)) {
                return ResponseDTO.error("PARAM_ERROR", "搜索关键词不能为空");
            }

            List<VideoFaceEntity> faceEntities = videoFaceDao.selectByNameKeyword(keyword);

            if (limit != null && limit > 0) {
                faceEntities = faceEntities.stream().limit(limit).collect(Collectors.toList());
            }

            List<VideoFaceVO> faceVOs = faceEntities.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            return ResponseDTO.ok(faceVOs);

        } catch (Exception e) {
            log.error("[人脸识别] 搜索人脸失败，keyword={}, error={}", keyword, e.getMessage(), e);
            return ResponseDTO.error("SEARCH_FACES_ERROR", "搜索人脸失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<VideoFaceVO>> getRecentFaces(Integer limit) {
        log.info("[人脸识别] 获取最近添加的人脸，limit={}", limit);

        try {
            if (limit == null || limit <= 0) {
                limit = 10; // 默认10条
            }

            List<VideoFaceEntity> faceEntities = videoFaceDao.selectRecentFaces(limit);
            List<VideoFaceVO> faceVOs = faceEntities.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            return ResponseDTO.ok(faceVOs);

        } catch (Exception e) {
            log.error("[人脸识别] 获取最近添加的人脸失败，limit={}, error={}", limit, e.getMessage(), e);
            return ResponseDTO.error("GET_RECENT_FACES_ERROR", "获取最近人脸失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<VideoFaceVO>> getHighQualityFaces(BigDecimal qualityThreshold, Integer limit) {
        log.info("[人脸识别] 获取高质量人脸，qualityThreshold={}, limit={}", qualityThreshold, limit);

        try {
            if (qualityThreshold == null) {
                qualityThreshold = new BigDecimal("85.0"); // 默认阈值85分
            }

            List<VideoFaceEntity> faceEntities = videoFaceDao.selectHighQualityFaces(qualityThreshold.doubleValue());

            if (limit != null && limit > 0) {
                faceEntities = faceEntities.stream().limit(limit).collect(Collectors.toList());
            }

            List<VideoFaceVO> faceVOs = faceEntities.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            return ResponseDTO.ok(faceVOs);

        } catch (Exception e) {
            log.error("[人脸识别] 获取高质量人脸失败，qualityThreshold={}, error={}", qualityThreshold, e.getMessage(), e);
            return ResponseDTO.error("GET_HIGH_QUALITY_FACES_ERROR", "获取高质量人脸失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<VideoFaceVO>> getValidFaces() {
        log.info("[人脸识别] 获取有效期内的人脸");

        try {
            List<VideoFaceEntity> faceEntities = videoFaceManager.getValidFaces();
            List<VideoFaceVO> faceVOs = faceEntities.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            return ResponseDTO.ok(faceVOs);

        } catch (Exception e) {
            log.error("[人脸识别] 获取有效期内的人脸失败，error={}", e.getMessage(), e);
            return ResponseDTO.error("GET_VALID_FACES_ERROR", "获取有效人脸失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<VideoFaceVO>> getExpiredFaces() {
        log.info("[人脸识别] 获取过期人脸");

        try {
            List<VideoFaceEntity> faceEntities = videoFaceDao.selectExpiredFaces(LocalDateTime.now());
            List<VideoFaceVO> faceVOs = faceEntities.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            return ResponseDTO.ok(faceVOs);

        } catch (Exception e) {
            log.error("[人脸识别] 获取过期人脸失败，error={}", e.getMessage(), e);
            return ResponseDTO.error("GET_EXPIRED_FACES_ERROR", "获取过期人脸失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Integer> batchUpdateFaceStatus(List<Long> faceIds, Integer faceStatus) {
        log.info("[人脸识别] 批量更新人脸状态，faceIds={}, faceStatus={}", faceIds, faceStatus);

        try {
            if (faceIds == null || faceIds.isEmpty()) {
                return ResponseDTO.error("PARAM_ERROR", "人脸ID列表不能为空");
            }

            String idsStr = faceIds.stream().map(String::valueOf).collect(Collectors.joining(","));
            int updatedCount = videoFaceDao.batchUpdateFaceStatus(idsStr, faceStatus);

            log.info("[人脸识别] 批量更新人脸状态完成，更新数量={}", updatedCount);
            return ResponseDTO.ok(updatedCount);

        } catch (Exception e) {
            log.error("[人脸识别] 批量更新人脸状态失败，faceIds={}, error={}", faceIds, e.getMessage(), e);
            return ResponseDTO.error("BATCH_UPDATE_STATUS_ERROR", "批量更新状态失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Integer> syncFacesToSystem(List<Long> faceIds) {
        log.info("[人脸识别] 同步人脸到其他系统，faceIds={}", faceIds);

        try {
            if (faceIds == null || faceIds.isEmpty()) {
                return ResponseDTO.error("PARAM_ERROR", "人脸ID列表不能为空");
            }

            // 先获取需要同步的人脸
            List<VideoFaceEntity> unsyncedFaces = videoFaceDao.selectUnsyncedFaces();
            List<Long> targetFaceIds = new ArrayList<>();

            if (faceIds.contains(-1L)) {
                // 同步所有未同步的人脸
                targetFaceIds = unsyncedFaces.stream().map(VideoFaceEntity::getFaceId).collect(Collectors.toList());
            } else {
                // 同步指定的人脸
                targetFaceIds = new ArrayList<>(faceIds);
            }

            if (targetFaceIds.isEmpty()) {
                return ResponseDTO.ok(0);
            }

            // 更新同步状态
            String idsStr = targetFaceIds.stream().map(String::valueOf).collect(Collectors.joining(","));
            int syncedCount = videoFaceDao.batchUpdateSyncFlag(idsStr, 1);

            log.info("[人脸识别] 同步人脸完成，同步数量={}", syncedCount);
            return ResponseDTO.ok(syncedCount);

        } catch (Exception e) {
            log.error("[人脸识别] 同步人脸失败，faceIds={}, error={}", faceIds, e.getMessage(), e);
            return ResponseDTO.error("SYNC_FACES_ERROR", "同步人脸失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Integer> cleanExpiredFaces() {
        log.info("[人脸识别] 清理过期人脸");

        try {
            int cleanedCount = videoFaceManager.cleanExpiredFaces();
            log.info("[人脸识别] 清理过期人脸完成，清理数量={}", cleanedCount);
            return ResponseDTO.ok(cleanedCount);

        } catch (Exception e) {
            log.error("[人脸识别] 清理过期人脸失败，error={}", e.getMessage(), e);
            return ResponseDTO.error("CLEAN_EXPIRED_FACES_ERROR", "清理过期人脸失败：" + e.getMessage());
        }
    }

    // ==================== 人脸搜索实现 ====================

    @Override
    public ResponseDTO<Map<String, Object>> faceSearch(@Valid VideoFaceSearchForm searchForm) {
        log.info("[人脸识别] 人脸搜索，searchType={}, searchLibrary={}, threshold={}",
                searchForm.getSearchType(), searchForm.getSearchLibrary(), searchForm.getSimilarityThreshold());

        try {
            // 调用Manager层执行人脸搜索
            Map<String, Object> searchResult = videoFaceManager.performFaceSearch(
                    searchForm.getSearchFaceUrl(),
                    searchForm.getSearchLibrary(),
                    searchForm.getSimilarityThreshold(),
                    searchForm.getMaxResults()
            );

            log.info("[人脸识别] 人脸搜索完成，匹配数量={}, 耗时={}ms",
                    searchResult.get("matchedCount"), searchResult.get("duration"));
            return ResponseDTO.ok(searchResult);

        } catch (Exception e) {
            log.error("[人脸识别] 人脸搜索失败，error={}", e.getMessage(), e);
            return ResponseDTO.error("FACE_SEARCH_ERROR", "人脸搜索失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> faceCompare(String sourceFaceUrl, String targetFaceUrl, BigDecimal similarityThreshold) {
        log.info("[人脸识别] 人脸比对，threshold={}", similarityThreshold);

        try {
            if (StringUtils.isBlank(sourceFaceUrl) || StringUtils.isBlank(targetFaceUrl)) {
                return ResponseDTO.error("PARAM_ERROR", "人脸图片URL不能为空");
            }

            if (similarityThreshold == null) {
                similarityThreshold = new BigDecimal("80.0"); // 默认阈值80分
            }

            // 调用Manager层执行人脸比对
            Map<String, Object> compareResult = videoFaceManager.performFaceCompare(sourceFaceUrl, targetFaceUrl, similarityThreshold);

            log.info("[人脸识别] 人脸比对完成，相似度={}, 是否匹配={}, 耗时={}ms",
                    compareResult.get("similarity"), compareResult.get("isMatch"), compareResult.get("duration"));
            return ResponseDTO.ok(compareResult);

        } catch (Exception e) {
            log.error("[人脸识别] 人脸比对失败，error={}", e.getMessage(), e);
            return ResponseDTO.error("FACE_COMPARE_ERROR", "人脸比对失败：" + e.getMessage());
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 实体对象转换为VO对象
     */
    private VideoFaceVO convertToVO(VideoFaceEntity entity) {
        VideoFaceVO vo = new VideoFaceVO();
        BeanUtils.copyProperties(entity, vo);

        // 转换枚举值描述
        vo.setIdCardTypeDesc(convertIdCardTypeDesc(entity.getIdCardType()));
        vo.setPersonTypeDesc(convertPersonTypeDesc(entity.getPersonType()));
        vo.setGenderDesc(convertGenderDesc(entity.getGender()));
        vo.setAlgorithmTypeDesc(convertAlgorithmTypeDesc(entity.getAlgorithmType()));
        vo.setLivenessCheckDesc(convertLivenessCheckDesc(entity.getLivenessCheck()));
        vo.setFaceQualityGrade(convertQualityGrade(entity.getFaceQualityScore()));
        vo.setSyncFlagDesc(convertSyncFlagDesc(entity.getSyncFlag()));
        vo.setFaceStatusDesc(convertFaceStatusDesc(entity.getFaceStatus()));

        // 脱敏敏感信息
        vo.setIdCardNumberMasked(maskIdCardNumber(entity.getIdCardNumber()));
        vo.setPhoneNumberMasked(maskPhoneNumber(entity.getPhoneNumber()));

        // 计算剩余有效期天数
        if (entity.getValidEndTime() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (entity.getValidEndTime().isAfter(now)) {
                vo.setRemainingValidDays((int) java.time.temporal.ChronoUnit.DAYS.between(now, entity.getValidEndTime()));
                vo.setIsExpired(false);
            } else {
                vo.setRemainingValidDays(0);
                vo.setIsExpired(true);
            }
        } else {
            vo.setRemainingValidDays(null);
            vo.setIsExpired(false);
        }

        // 格式化时间显示
        if (entity.getCaptureTime() != null) {
            vo.setCaptureTimeDesc(entity.getCaptureTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (entity.getCreateTime() != null) {
            vo.setCreateTimeDesc(entity.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        return vo;
    }

    // 转换描述信息的方法
    private String convertIdCardTypeDesc(Integer idCardType) {
        if (idCardType == null) return null;
        return switch (idCardType) {
            case 1 -> "身份证";
            case 2 -> "护照";
            case 3 -> "军官证";
            case 4 -> "港澳通行证";
            case 5 -> "台湾通行证";
            default -> "其他";
        };
    }

    private String convertPersonTypeDesc(Integer personType) {
        if (personType == null) return null;
        return switch (personType) {
            case 1 -> "员工";
            case 2 -> "访客";
            case 3 -> "黑名单";
            case 4 -> "白名单";
            case 5 -> "重点关注";
            default -> "其他";
        };
    }

    private String convertGenderDesc(Integer gender) {
        if (gender == null) return null;
        return switch (gender) {
            case 0 -> "未知";
            case 1 -> "男";
            case 2 -> "女";
            default -> "其他";
        };
    }

    private String convertAlgorithmTypeDesc(Integer algorithmType) {
        if (algorithmType == null) return null;
        return switch (algorithmType) {
            case 1 -> "商汤";
            case 2 -> "旷视";
            case 3 -> "依图";
            case 4 -> "百度";
            case 5 -> "腾讯优图";
            case 6 -> "虹软";
            case 7 -> "华为";
            case 8 -> "阿里云";
            default -> "其他";
        };
    }

    private String convertLivenessCheckDesc(Integer livenessCheck) {
        if (livenessCheck == null) return null;
        return switch (livenessCheck) {
            case 0 -> "未检测";
            case 1 -> "通过";
            case 2 -> "未通过";
            default -> "其他";
        };
    }

    private String convertQualityGrade(BigDecimal qualityScore) {
        if (qualityScore == null) return null;
        double score = qualityScore.doubleValue();
        if (score >= 95) return "优秀";
        if (score >= 85) return "良好";
        if (score >= 75) return "一般";
        if (score >= 60) return "较差";
        return "极差";
    }

    private String convertSyncFlagDesc(Integer syncFlag) {
        if (syncFlag == null) return null;
        return switch (syncFlag) {
            case 0 -> "未同步";
            case 1 -> "已同步";
            case 2 -> "同步失败";
            default -> "其他";
        };
    }

    private String convertFaceStatusDesc(Integer faceStatus) {
        if (faceStatus == null) return null;
        return switch (faceStatus) {
            case 1 -> "正常";
            case 2 -> "禁用";
            case 3 -> "过期";
            case 4 -> "审核中";
            case 5 -> "审核拒绝";
            default -> "其他";
        };
    }

    // 脱敏处理
    private String maskIdCardNumber(String idCardNumber) {
        if (StringUtils.isBlank(idCardNumber) || idCardNumber.length() < 8) {
            return idCardNumber;
        }
        return idCardNumber.substring(0, 3) + "***********" + idCardNumber.substring(idCardNumber.length() - 4);
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (StringUtils.isBlank(phoneNumber) || phoneNumber.length() < 7) {
            return phoneNumber;
        }
        return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(phoneNumber.length() - 4);
    }

    // ==================== 其他方法实现（简化版）====================

    @Override
    public ResponseDTO<Map<String, Object>> searchByFaceImage(String imageUrl, Integer searchLibrary, BigDecimal similarityThreshold, Integer maxResults) {
        // TODO: 实现图片搜索功能
        return ResponseDTO.error("NOT_IMPLEMENTED", "功能暂未实现");
    }

    @Override
    public ResponseDTO<Map<String, Object>> getFaceSearchSuggestions(String personCode, String personName) {
        try {
            Map<String, Object> suggestions = videoFaceManager.getFaceSearchSuggestions(personCode, personName);
            return ResponseDTO.ok(suggestions);
        } catch (Exception e) {
            log.error("[人脸识别] 获取人脸搜索建议失败，error={}", e.getMessage(), e);
            return ResponseDTO.error("GET_SUGGESTIONS_ERROR", "获取搜索建议失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<PageResult<Map<String, Object>>> getSearchHistory(PageParam pageParam, LocalDateTime startTime, LocalDateTime endTime) {
        // TODO: 实现获取搜索历史记录
        return ResponseDTO.error("NOT_IMPLEMENTED", "功能暂未实现");
    }

    @Override
    public ResponseDTO<Map<String, Object>> evaluateFaceQuality(String faceImageUrl) {
        try {
            Map<String, Object> qualityResult = videoFaceManager.evaluateFaceQuality(faceImageUrl);
            return ResponseDTO.ok(qualityResult);
        } catch (Exception e) {
            log.error("[人脸识别] 评估人脸质量失败，imageUrl={}, error={}", faceImageUrl, e.getMessage(), e);
            return ResponseDTO.error("EVALUATE_QUALITY_ERROR", "评估人脸质量失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> checkFaceQualityForRegister(String faceImageUrl) {
        try {
            Map<String, Object> qualityResult = videoFaceManager.evaluateFaceQuality(faceImageUrl);
            BigDecimal qualityScore = (BigDecimal) qualityResult.get("qualityScore");
            boolean isAcceptable = videoFaceManager.checkFaceQualityForRegister(qualityScore);

            qualityResult.put("isAcceptableForRegister", isAcceptable);
            return ResponseDTO.ok(qualityResult);
        } catch (Exception e) {
            log.error("[人脸识别] 检查人脸注册质量失败，imageUrl={}, error={}", faceImageUrl, e.getMessage(), e);
            return ResponseDTO.error("CHECK_REGISTER_QUALITY_ERROR", "检查注册质量失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> checkFaceQualityForCompare(String faceImageUrl) {
        try {
            Map<String, Object> qualityResult = videoFaceManager.evaluateFaceQuality(faceImageUrl);
            BigDecimal qualityScore = (BigDecimal) qualityResult.get("qualityScore");
            boolean isAcceptable = videoFaceManager.checkFaceQualityForCompare(qualityScore);

            qualityResult.put("isAcceptableForCompare", isAcceptable);
            return ResponseDTO.ok(qualityResult);
        } catch (Exception e) {
            log.error("[人脸识别] 检查人脸比对质量失败，imageUrl={}, error={}", faceImageUrl, e.getMessage(), e);
            return ResponseDTO.error("CHECK_COMPARE_QUALITY_ERROR", "检查比对质量失败：" + e.getMessage());
        }
    }

    // 其他方法的简化实现...
    @Override
    public ResponseDTO<PageResult<Map<String, Object>>> getDetectionRecords(PageParam pageParam, Long deviceId, Long personId, LocalDateTime startTime, LocalDateTime endTime, Integer processStatus) {
        return ResponseDTO.error("NOT_IMPLEMENTED", "功能暂未实现");
    }

    @Override
    public ResponseDTO<List<Map<String, Object>>> getUnprocessedAlarms() {
        return ResponseDTO.error("NOT_IMPLEMENTED", "功能暂未实现");
    }

    @Override
    public ResponseDTO<Void> processDetectionAlarm(Long detectionId, Integer processStatus, String remark) {
        return ResponseDTO.error("NOT_IMPLEMENTED", "功能暂未实现");
    }

    @Override
    public ResponseDTO<Integer> batchProcessDetectionAlarms(List<Long> detectionIds, Integer processStatus, String remark) {
        return ResponseDTO.error("NOT_IMPLEMENTED", "功能暂未实现");
    }

    @Override
    public ResponseDTO<Map<String, Object>> getDeviceDetectionStatistics(Long deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        return ResponseDTO.error("NOT_IMPLEMENTED", "功能暂未实现");
    }

    @Override
    public ResponseDTO<Map<String, Object>> getPersonActivityStatistics(Long personId, LocalDateTime startTime, LocalDateTime endTime) {
        return ResponseDTO.error("NOT_IMPLEMENTED", "功能暂未实现");
    }

    @Override
    public ResponseDTO<Map<String, Object>> getFaceLibraryAnalysisReport() {
        return ResponseDTO.error("NOT_IMPLEMENTED", "功能暂未实现");
    }

    @Override
    public ResponseDTO<Map<String, Object>> getRecognitionTrendAnalysis(LocalDateTime startTime, LocalDateTime endTime) {
        return ResponseDTO.error("NOT_IMPLEMENTED", "功能暂未实现");
    }

    @Override
    public ResponseDTO<Map<String, Object>> getDepartmentFaceDistribution() {
        return ResponseDTO.error("NOT_IMPLEMENTED", "功能暂未实现");
    }

    @Override
    public ResponseDTO<Map<String, Object>> getAlgorithmPerformanceAnalysis() {
        return ResponseDTO.error("NOT_IMPLEMENTED", "功能暂未实现");
    }

    @Override
    public ResponseDTO<Map<String, Object>> getFaceQualityDistribution() {
        return ResponseDTO.error("NOT_IMPLEMENTED", "功能暂未实现");
    }

    @Override
    public ResponseDTO<String> generateFaceFeature(String faceImageUrl, Integer algorithmType) {
        return ResponseDTO.error("NOT_IMPLEMENTED", "功能暂未实现");
    }

    @Override
    public ResponseDTO<String> preprocessFaceImage(String originalImageUrl) {
        return ResponseDTO.error("NOT_IMPLEMENTED", "功能暂未实现");
    }

    @Override
    public ResponseDTO<String> enhanceFaceImageQuality(String faceImageUrl) {
        return ResponseDTO.error("NOT_IMPLEMENTED", "功能暂未实现");
    }

    @Override
    public ResponseDTO<Boolean> validateFaceFeature(String faceFeature, Integer featureDimension) {
        return ResponseDTO.error("NOT_IMPLEMENTED", "功能暂未实现");
    }
}
