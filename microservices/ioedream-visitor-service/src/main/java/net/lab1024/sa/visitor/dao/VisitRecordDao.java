package net.lab1024.sa.visitor.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.visitor.domain.entity.VisitRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 访客记录数据访问层
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @date 2025-01-27
 */
@Mapper
@Repository
public interface VisitRecordDao extends BaseMapper<VisitRecordEntity> {

    /**
     * 查询访客记录列表
     *
     * @param visitorId 访客ID
     * @return 记录列表
     */
    List<VisitRecordEntity> selectByVisitorId(@Param("visitorId") Long visitorId);

    /**
     * 查询指定时间段的访客记录
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 记录列表
     */
    List<VisitRecordEntity> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 查询正在访问中的记录
     *
     * @return 记录列表
     */
    List<VisitRecordEntity> selectActiveVisits();

    /**
     * 查询区域访问记录
     *
     * @param areaId 区域ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 记录列表
     */
    List<VisitRecordEntity> selectByAreaAndTimeRange(@Param("areaId") Long areaId,
                                                    @Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 查询超时未离开的记录
     *
     * @param timeoutMinutes 超时分钟数
     * @return 记录列表
     */
    List<VisitRecordEntity> selectTimeoutRecords(@Param("timeoutMinutes") Integer timeoutMinutes);

    /**
     * 统计访问次数
     *
     * @param visitorId 访客ID
     * @return 访问次数
     */
    Long countVisitTimes(@Param("visitorId") Long visitorId);

    /**
     * 更新离开时间和验证方式
     *
     * @param recordId 记录ID
     * @param exitTime 离开时间
     * @param exitVerificationMethod 出门验证方式
     * @return 影响行数
     */
    int updateExitInfo(@Param("recordId") Long recordId,
                       @Param("exitTime") LocalDateTime exitTime,
                       @Param("exitVerificationMethod") Integer exitVerificationMethod);
}