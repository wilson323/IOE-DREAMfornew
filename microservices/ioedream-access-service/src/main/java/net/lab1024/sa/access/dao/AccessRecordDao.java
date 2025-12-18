package net.lab1024.sa.access.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.common.access.entity.AccessRecordEntity;

/**
 * 门禁记录DAO
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Mapper注解
 * - 继承BaseMapper
 * - 使用Dao后缀命名
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface AccessRecordDao extends BaseMapper<AccessRecordEntity> {

    /**
     * 根据用户ID和时间范围查询记录
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param size 记录数量
     * @return 门禁记录列表
     */
    default List<AccessRecordEntity> selectByUserIdAndTimeRange(Long userId, LocalDateTime startTime, LocalDateTime endTime, Integer size) {
        LambdaQueryWrapper<AccessRecordEntity> wrapper = Wrappers.lambdaQuery(AccessRecordEntity.class)
                .eq(AccessRecordEntity::getUserId, userId)
                .ge(startTime != null, AccessRecordEntity::getAccessTime, startTime)
                .le(endTime != null, AccessRecordEntity::getAccessTime, endTime)
                .orderByDesc(AccessRecordEntity::getAccessTime)
                .last(size != null && size > 0, "LIMIT " + size);
        return selectList(wrapper);
    }

    /**
     * 分页查询门禁记录
     *
     * @param page 分页对象
     * @param userId 用户ID（可选）
     * @param deviceId 设备ID（可选）
     * @param areaId 区域ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param accessResult 通行结果（可选）
     * @return 分页结果
     */
    default Page<AccessRecordEntity> selectPage(Page<AccessRecordEntity> page, Long userId, Long deviceId,
            String areaId, LocalDate startDate, LocalDate endDate, Integer accessResult) {
        LambdaQueryWrapper<AccessRecordEntity> wrapper = Wrappers.lambdaQuery(AccessRecordEntity.class)
                .eq(userId != null, AccessRecordEntity::getUserId, userId)
                .eq(deviceId != null, AccessRecordEntity::getDeviceId, deviceId)
                .eq(areaId != null, AccessRecordEntity::getAreaId, Long.parseLong(areaId))
                .ge(startDate != null, AccessRecordEntity::getAccessTime, startDate.atStartOfDay())
                .le(endDate != null, AccessRecordEntity::getAccessTime, endDate.atTime(23, 59, 59))
                .eq(accessResult != null, AccessRecordEntity::getAccessResult, accessResult)
                .orderByDesc(AccessRecordEntity::getAccessTime);
        return selectPage(page, wrapper);
    }

    /**
     * 统计门禁记录
     * <p>
     * 使用MyBatis-Plus的LambdaQueryWrapper进行统计查询
     * </p>
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param areaId 区域ID（可选）
     * @return 统计结果
     */
    default AccessRecordStatistics selectStatistics(LocalDate startDate, LocalDate endDate, String areaId) {
        LambdaQueryWrapper<AccessRecordEntity> wrapper = Wrappers.lambdaQuery(AccessRecordEntity.class)
                .ge(startDate != null, AccessRecordEntity::getAccessTime, startDate.atStartOfDay())
                .le(endDate != null, AccessRecordEntity::getAccessTime, endDate.atTime(23, 59, 59))
                .eq(areaId != null, AccessRecordEntity::getAreaId, Long.parseLong(areaId));

        // 查询总记录数
        Long totalCount = selectCount(wrapper);

        // 查询成功数
        LambdaQueryWrapper<AccessRecordEntity> successWrapper = wrapper.clone();
        successWrapper.eq(AccessRecordEntity::getAccessResult, 1);
        Long successCount = selectCount(successWrapper);

        // 查询失败数
        LambdaQueryWrapper<AccessRecordEntity> failedWrapper = wrapper.clone();
        failedWrapper.eq(AccessRecordEntity::getAccessResult, 2);
        Long failedCount = selectCount(failedWrapper);

        AccessRecordStatistics statistics = new AccessRecordStatistics();
        statistics.setTotalCount(totalCount != null ? totalCount : 0L);
        statistics.setSuccessCount(successCount != null ? successCount : 0L);
        statistics.setFailedCount(failedCount != null ? failedCount : 0L);

        return statistics;
    }

    /**
     * 统计结果内部类
     */
    class AccessRecordStatistics {
        private Long totalCount;
        private Long successCount;
        private Long failedCount;

        public Long getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(Long totalCount) {
            this.totalCount = totalCount;
        }

        public Long getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(Long successCount) {
            this.successCount = successCount;
        }

        public Long getFailedCount() {
            return failedCount;
        }

        public void setFailedCount(Long failedCount) {
            this.failedCount = failedCount;
        }
    }
}