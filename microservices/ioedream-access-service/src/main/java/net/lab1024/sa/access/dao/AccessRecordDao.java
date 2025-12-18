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
 * 闂ㄧ璁板綍DAO
 * <p>
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛? * - 浣跨敤@Mapper娉ㄨВ
 * - 缁ф壙BaseMapper
 * - 浣跨敤Dao鍚庣紑鍛藉悕
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface AccessRecordDao extends BaseMapper<AccessRecordEntity> {

    /**
     * 鏍规嵁鐢ㄦ埛ID鍜屾椂闂磋寖鍥存煡璇㈣褰?     *
     * @param userId 鐢ㄦ埛ID
     * @param startTime 寮€濮嬫椂闂?     * @param endTime 缁撴潫鏃堕棿
     * @param size 璁板綍鏁伴噺
     * @return 闂ㄧ璁板綍鍒楄〃
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
     * 鍒嗛〉鏌ヨ闂ㄧ璁板綍
     *
     * @param page 鍒嗛〉瀵硅薄
     * @param userId 鐢ㄦ埛ID锛堝彲閫夛級
     * @param deviceId 璁惧ID锛堝彲閫夛級
     * @param areaId 鍖哄煙ID锛堝彲閫夛級
     * @param startDate 寮€濮嬫棩鏈燂紙鍙€夛級
     * @param endDate 缁撴潫鏃ユ湡锛堝彲閫夛級
     * @param accessResult 閫氳缁撴灉锛堝彲閫夛級
     * @return 鍒嗛〉缁撴灉
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
     * 缁熻闂ㄧ璁板綍
     * <p>
     * 浣跨敤MyBatis-Plus鐨凩ambdaQueryWrapper杩涜缁熻鏌ヨ
     * </p>
     *
     * @param startDate 寮€濮嬫棩鏈?     * @param endDate 缁撴潫鏃ユ湡
     * @param areaId 鍖哄煙ID锛堝彲閫夛級
     * @return 缁熻缁撴灉
     */
    default AccessRecordStatistics selectStatistics(LocalDate startDate, LocalDate endDate, String areaId) {
        LambdaQueryWrapper<AccessRecordEntity> wrapper = Wrappers.lambdaQuery(AccessRecordEntity.class)
                .ge(startDate != null, AccessRecordEntity::getAccessTime, startDate.atStartOfDay())
                .le(endDate != null, AccessRecordEntity::getAccessTime, endDate.atTime(23, 59, 59))
                .eq(areaId != null, AccessRecordEntity::getAreaId, Long.parseLong(areaId));

        // 鏌ヨ鎬昏褰曟暟
        Long totalCount = selectCount(wrapper);

        // 鏌ヨ鎴愬姛鏁?        LambdaQueryWrapper<AccessRecordEntity> successWrapper = wrapper.clone();
        successWrapper.eq(AccessRecordEntity::getAccessResult, 1);
        Long successCount = selectCount(successWrapper);

        // 鏌ヨ澶辫触鏁?        LambdaQueryWrapper<AccessRecordEntity> failedWrapper = wrapper.clone();
        failedWrapper.eq(AccessRecordEntity::getAccessResult, 2);
        Long failedCount = selectCount(failedWrapper);

        AccessRecordStatistics statistics = new AccessRecordStatistics();
        statistics.setTotalCount(totalCount != null ? totalCount : 0L);
        statistics.setSuccessCount(successCount != null ? successCount : 0L);
        statistics.setFailedCount(failedCount != null ? failedCount : 0L);

        return statistics;
    }

    /**
     * 缁熻缁撴灉鍐呴儴绫?     */
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
