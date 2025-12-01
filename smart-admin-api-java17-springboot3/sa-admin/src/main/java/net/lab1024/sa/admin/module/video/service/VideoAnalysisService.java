package net.lab1024.sa.admin.module.video.service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 视频分析服务接口
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-25
 */
public interface VideoAnalysisService {

    /**
     * 执行视频分析
     *
     * @param videoId 视频ID
     * @param analysisType 分析类型
     * @return 分析结果ID
     */
    Long performAnalysis(Long videoId, String analysisType);

    /**
     * 获取分析结果
     *
     * @param analysisId 分析ID
     * @return 分析结果
     */
    Object getAnalysisResult(Long analysisId);

    /**
     * 查询分析历史
     *
     * @param videoId 视频ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分析历史列表
     */
    List<Object> getAnalysisHistory(Long videoId, LocalDateTime startTime, LocalDateTime endTime);
}