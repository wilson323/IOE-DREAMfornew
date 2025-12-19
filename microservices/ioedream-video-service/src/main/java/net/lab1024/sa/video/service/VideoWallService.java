package net.lab1024.sa.video.service;

import jakarta.validation.Valid;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.openapi.domain.response.PageResult;
import net.lab1024.sa.video.domain.form.VideoDisplayTaskAddForm;
import net.lab1024.sa.video.domain.form.VideoWallAddForm;
import net.lab1024.sa.video.domain.form.VideoWallPresetAddForm;
import net.lab1024.sa.video.domain.form.VideoWallTourAddForm;
import net.lab1024.sa.video.domain.form.VideoWallUpdateForm;
import net.lab1024.sa.video.domain.vo.VideoDisplayTaskVO;
import net.lab1024.sa.video.domain.vo.VideoWallPresetVO;
import net.lab1024.sa.video.domain.vo.VideoWallTourVO;
import net.lab1024.sa.video.domain.vo.VideoWallVO;

import java.util.List;

/**
 * 解码上墙服务接口
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - Service接口在ioedream-video-service中
 * - 提供统一的业务接口
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface VideoWallService {

    /**
     * 创建电视墙
     *
     * @param addForm 新增表单
     * @return 电视墙ID
     */
    ResponseDTO<Long> createWall(@Valid VideoWallAddForm addForm);

    /**
     * 更新电视墙
     *
     * @param updateForm 更新表单
     * @return 操作结果
     */
    ResponseDTO<Void> updateWall(@Valid VideoWallUpdateForm updateForm);

    /**
     * 删除电视墙
     *
     * @param wallId 电视墙ID
     * @return 操作结果
     */
    ResponseDTO<Void> deleteWall(Long wallId);

    /**
     * 查询电视墙详情
     *
     * @param wallId 电视墙ID
     * @return 电视墙详情
     */
    ResponseDTO<VideoWallVO> getWallById(Long wallId);

    /**
     * 查询电视墙列表
     *
     * @param regionId 区域ID（可选）
     * @return 电视墙列表
     */
    ResponseDTO<List<VideoWallVO>> getWallList(Long regionId);

    /**
     * 创建上墙任务
     *
     * @param addForm 新增表单
     * @return 任务ID
     */
    ResponseDTO<Long> createDisplayTask(@Valid VideoDisplayTaskAddForm addForm);

    /**
     * 取消上墙任务
     *
     * @param taskId 任务ID
     * @return 操作结果
     */
    ResponseDTO<Void> cancelDisplayTask(Long taskId);

    /**
     * 查询上墙任务列表
     *
     * @param wallId 电视墙ID
     * @return 任务列表
     */
    ResponseDTO<List<VideoDisplayTaskVO>> getTaskList(Long wallId);

    /**
     * 创建预案
     *
     * @param addForm 新增表单
     * @return 预案ID
     */
    ResponseDTO<Long> createPreset(@Valid VideoWallPresetAddForm addForm);

    /**
     * 删除预案
     *
     * @param presetId 预案ID
     * @return 操作结果
     */
    ResponseDTO<Void> deletePreset(Long presetId);

    /**
     * 查询预案列表
     *
     * @param wallId 电视墙ID
     * @return 预案列表
     */
    ResponseDTO<List<VideoWallPresetVO>> getPresetList(Long wallId);

    /**
     * 调用预案
     *
     * @param presetId 预案ID
     * @return 操作结果
     */
    ResponseDTO<Void> applyPreset(Long presetId);

    /**
     * 创建轮巡
     *
     * @param addForm 新增表单
     * @return 轮巡ID
     */
    ResponseDTO<Long> createTour(@Valid VideoWallTourAddForm addForm);

    /**
     * 删除轮巡
     *
     * @param tourId 轮巡ID
     * @return 操作结果
     */
    ResponseDTO<Void> deleteTour(Long tourId);

    /**
     * 查询轮巡列表
     *
     * @param wallId 电视墙ID
     * @return 轮巡列表
     */
    ResponseDTO<List<VideoWallTourVO>> getTourList(Long wallId);

    /**
     * 启动轮巡
     *
     * @param tourId 轮巡ID
     * @return 操作结果
     */
    ResponseDTO<Void> startTour(Long tourId);

    /**
     * 停止轮巡
     *
     * @param tourId 轮巡ID
     * @return 操作结果
     */
    ResponseDTO<Void> stopTour(Long tourId);
}
