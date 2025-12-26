package net.lab1024.sa.video.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.entity.video.VideoRecordingPlanEntity;
import net.lab1024.sa.video.domain.form.VideoRecordingPlanAddForm;
import net.lab1024.sa.video.domain.form.VideoRecordingPlanQueryForm;
import net.lab1024.sa.video.domain.form.VideoRecordingPlanUpdateForm;
import net.lab1024.sa.video.domain.vo.VideoRecordingPlanVO;
import net.lab1024.sa.video.service.VideoRecordingPlanService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 视频录像计划服务实现
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Service
@Slf4j
public class VideoRecordingPlanServiceImpl implements VideoRecordingPlanService {

    @Resource
    private net.lab1024.sa.video.dao.VideoRecordingPlanDao videoRecordingPlanDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPlan(VideoRecordingPlanAddForm addForm) {
        log.info("[录像管理] 创建录像计划: planName={}, deviceId={}, planType={}",
                addForm.getPlanName(), addForm.getDeviceId(), addForm.getPlanType());

        // 检查计划名称是否重复
        LambdaQueryWrapper<VideoRecordingPlanEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VideoRecordingPlanEntity::getPlanName, addForm.getPlanName());
        Long count = videoRecordingPlanDao.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException("PLAN_NAME_EXISTS", "计划名称已存在");
        }

        // 创建计划实体
        VideoRecordingPlanEntity entity = new VideoRecordingPlanEntity();
        BeanUtils.copyProperties(addForm, entity);

        // 处理事件类型列表
        if (addForm.getEventTypes() != null && !addForm.getEventTypes().isEmpty()) {
            entity.setEventTypes(String.join(",", addForm.getEventTypes()));
        }

        // 设置默认值
        if (entity.getEnabled() == null) {
            entity.setEnabled(true);
        }
        if (entity.getPriority() == null) {
            entity.setPriority(1);
        }

        videoRecordingPlanDao.insert(entity);

        log.info("[录像管理] 录像计划创建成功: planId={}, planName={}", entity.getPlanId(), entity.getPlanName());
        return entity.getPlanId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updatePlan(VideoRecordingPlanUpdateForm updateForm) {
        log.info("[录像管理] 更新录像计划: planId={}", updateForm.getPlanId());

        // 检查计划是否存在
        VideoRecordingPlanEntity existingPlan = videoRecordingPlanDao.selectById(updateForm.getPlanId());
        if (existingPlan == null) {
            throw new BusinessException("PLAN_NOT_FOUND", "录像计划不存在");
        }

        // 更新计划
        VideoRecordingPlanEntity entity = new VideoRecordingPlanEntity();
        BeanUtils.copyProperties(updateForm, entity);

        // 处理事件类型列表
        if (updateForm.getEventTypes() != null && !updateForm.getEventTypes().isEmpty()) {
            entity.setEventTypes(String.join(",", updateForm.getEventTypes()));
        }

        Integer rows = videoRecordingPlanDao.updateById(entity);

        log.info("[录像管理] 录像计划更新成功: planId={}, rows={}", updateForm.getPlanId(), rows);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deletePlan(Long planId) {
        log.info("[录像管理] 删除录像计划: planId={}", planId);

        // 检查计划是否存在
        VideoRecordingPlanEntity existingPlan = videoRecordingPlanDao.selectById(planId);
        if (existingPlan == null) {
            throw new BusinessException("PLAN_NOT_FOUND", "录像计划不存在");
        }

        Integer rows = videoRecordingPlanDao.deleteById(planId);

        log.info("[录像管理] 录像计划删除成功: planId={}, rows={}", planId, rows);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer enablePlan(Long planId, Boolean enabled) {
        log.info("[录像管理] 启用/禁用录像计划: planId={}, enabled={}", planId, enabled);

        // 检查计划是否存在
        VideoRecordingPlanEntity existingPlan = videoRecordingPlanDao.selectById(planId);
        if (existingPlan == null) {
            throw new BusinessException("PLAN_NOT_FOUND", "录像计划不存在");
        }

        LambdaUpdateWrapper<VideoRecordingPlanEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(VideoRecordingPlanEntity::getPlanId, planId)
                .set(VideoRecordingPlanEntity::getEnabled, enabled);

        Integer rows = videoRecordingPlanDao.update(null, updateWrapper);

        log.info("[录像管理] 录像计划状态更新成功: planId={}, enabled={}, rows={}", planId, enabled, rows);
        return rows;
    }

    @Override
    public VideoRecordingPlanVO getPlan(Long planId) {
        log.debug("[录像管理] 获取录像计划详情: planId={}", planId);

        VideoRecordingPlanEntity entity = videoRecordingPlanDao.selectById(planId);
        if (entity == null) {
            throw new BusinessException("PLAN_NOT_FOUND", "录像计划不存在");
        }

        return convertToVO(entity);
    }

    @Override
    public PageResult<VideoRecordingPlanVO> queryPlans(VideoRecordingPlanQueryForm queryForm) {
        log.info("[录像管理] 分页查询录像计划: pageNum={}, pageSize={}",
                queryForm.getPageNum(), queryForm.getPageSize());

        LambdaQueryWrapper<VideoRecordingPlanEntity> queryWrapper = buildQueryWrapper(queryForm);

        Page<VideoRecordingPlanEntity> page = videoRecordingPlanDao.selectPage(
                new Page<>(queryForm.getPageNum(), queryForm.getPageSize()),
                queryWrapper
        );

        List<VideoRecordingPlanVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        PageResult<VideoRecordingPlanVO> pageResult = new PageResult<>();
        pageResult.setList(voList);
        pageResult.setTotal(page.getTotal());
        pageResult.setPageNum((int) page.getCurrent());
        pageResult.setPageSize((int) page.getSize());
        pageResult.setPages((int) page.getPages());

        log.info("[录像管理] 分页查询完成: total={}, pages={}", page.getTotal(), page.getPages());
        return pageResult;
    }

    @Override
    public List<VideoRecordingPlanVO> getEnabledPlansByDevice(String deviceId) {
        log.debug("[录像管理] 获取设备的启用录像计划: deviceId={}", deviceId);

        List<VideoRecordingPlanEntity> entities = videoRecordingPlanDao.selectEnabledPlansByDevice(deviceId);

        return entities.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean hasEnabledPlan(String deviceId) {
        return videoRecordingPlanDao.existsEnabledPlan(deviceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copyPlan(Long planId, String newPlanName) {
        log.info("[录像管理] 复制录像计划: planId={}, newPlanName={}", planId, newPlanName);

        // 获取原计划
        VideoRecordingPlanEntity originalPlan = videoRecordingPlanDao.selectById(planId);
        if (originalPlan == null) {
            throw new BusinessException("PLAN_NOT_FOUND", "录像计划不存在");
        }

        // 创建新计划
        VideoRecordingPlanEntity newPlan = new VideoRecordingPlanEntity();
        BeanUtils.copyProperties(originalPlan, newPlan);
        newPlan.setPlanId(null);
        newPlan.setPlanName(newPlanName);
        newPlan.setEnabled(false); // 复制的计划默认禁用

        videoRecordingPlanDao.insert(newPlan);

        log.info("[录像管理] 录像计划复制成功: originalPlanId={}, newPlanId={}", planId, newPlan.getPlanId());
        return newPlan.getPlanId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchEnablePlans(List<Long> planIds, Boolean enabled) {
        log.info("[录像管理] 批量启用/禁用录像计划: planIds={}, enabled={}", planIds, enabled);

        if (planIds == null || planIds.isEmpty()) {
            return 0;
        }

        LambdaUpdateWrapper<VideoRecordingPlanEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(VideoRecordingPlanEntity::getPlanId, planIds)
                .set(VideoRecordingPlanEntity::getEnabled, enabled);

        Integer rows = videoRecordingPlanDao.update(null, updateWrapper);

        log.info("[录像管理] 批量更新完成: rows={}", rows);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchDeletePlans(List<Long> planIds) {
        log.info("[录像管理] 批量删除录像计划: planIds={}", planIds);

        if (planIds == null || planIds.isEmpty()) {
            return 0;
        }

        Integer rows = videoRecordingPlanDao.deleteBatchIds(planIds);

        log.info("[录像管理] 批量删除完成: rows={}", rows);
        return rows;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<VideoRecordingPlanEntity> buildQueryWrapper(VideoRecordingPlanQueryForm queryForm) {
        LambdaQueryWrapper<VideoRecordingPlanEntity> queryWrapper = new LambdaQueryWrapper<>();

        // 计划名称模糊查询
        if (StringUtils.hasText(queryForm.getPlanName())) {
            queryWrapper.like(VideoRecordingPlanEntity::getPlanName, queryForm.getPlanName());
        }

        // 计划类型
        if (queryForm.getPlanType() != null) {
            queryWrapper.eq(VideoRecordingPlanEntity::getPlanType, queryForm.getPlanType());
        }

        // 设备ID
        if (StringUtils.hasText(queryForm.getDeviceId())) {
            queryWrapper.eq(VideoRecordingPlanEntity::getDeviceId, queryForm.getDeviceId());
        }

        // 是否启用
        if (queryForm.getEnabled() != null) {
            queryWrapper.eq(VideoRecordingPlanEntity::getEnabled, queryForm.getEnabled());
        }

        // 录像类型
        if (queryForm.getRecordingType() != null) {
            queryWrapper.eq(VideoRecordingPlanEntity::getRecordingType, queryForm.getRecordingType());
        }

        // 开始时间范围
        if (queryForm.getStartTimeBegin() != null) {
            queryWrapper.ge(VideoRecordingPlanEntity::getStartTime, queryForm.getStartTimeBegin());
        }
        if (queryForm.getStartTimeEnd() != null) {
            queryWrapper.le(VideoRecordingPlanEntity::getStartTime, queryForm.getStartTimeEnd());
        }

        // 创建时间范围
        if (queryForm.getCreateTimeBegin() != null) {
            queryWrapper.ge(VideoRecordingPlanEntity::getCreateTime, queryForm.getCreateTimeBegin());
        }
        if (queryForm.getCreateTimeEnd() != null) {
            queryWrapper.le(VideoRecordingPlanEntity::getCreateTime, queryForm.getCreateTimeEnd());
        }

        // 排序
        queryWrapper.orderByDesc(VideoRecordingPlanEntity::getCreateTime);

        return queryWrapper;
    }

    /**
     * 转换为VO对象
     */
    private VideoRecordingPlanVO convertToVO(VideoRecordingPlanEntity entity) {
        VideoRecordingPlanVO vo = new VideoRecordingPlanVO();
        BeanUtils.copyProperties(entity, vo);

        // 转换枚举值
        vo.setPlanType(VideoRecordingPlanEntity.PlanType.fromCode(entity.getPlanType()).name());
        vo.setPlanTypeName(VideoRecordingPlanEntity.PlanType.fromCode(entity.getPlanType()).getDescription());

        vo.setRecordingType(VideoRecordingPlanEntity.RecordingType.fromCode(entity.getRecordingType()).name());
        vo.setRecordingTypeName(VideoRecordingPlanEntity.RecordingType.fromCode(entity.getRecordingType()).getDescription());

        VideoRecordingPlanEntity.RecordingQuality quality = VideoRecordingPlanEntity.RecordingQuality.fromCode(entity.getQuality());
        vo.setQuality(quality.name());
        vo.setQualityName(quality.getDescription());
        vo.setResolution(quality.getResolution());
        vo.setBitrate(quality.getBitrate());

        // 转换星期描述
        if (StringUtils.hasText(entity.getWeekdays())) {
            vo.setWeekdaysDesc(convertWeekdaysDesc(entity.getWeekdays()));
        }

        return vo;
    }

    /**
     * 转换星期描述
     */
    private String convertWeekdaysDesc(String weekdays) {
        List<String> weekdayList = Arrays.asList(weekdays.split(","));
        if (weekdayList.contains("1") && weekdayList.contains("2") && weekdayList.contains("3")
                && weekdayList.contains("4") && weekdayList.contains("5")
                && !weekdayList.contains("6") && !weekdayList.contains("7")) {
            return "工作日";
        } else if (weekdayList.contains("6") && weekdayList.contains("7")
                && !weekdayList.contains("1") && !weekdayList.contains("2") && !weekdayList.contains("3")
                && !weekdayList.contains("4") && !weekdayList.contains("5")) {
            return "周末";
        } else if (weekdayList.size() == 7) {
            return "每天";
        } else {
            return weekdays;
        }
    }
}
