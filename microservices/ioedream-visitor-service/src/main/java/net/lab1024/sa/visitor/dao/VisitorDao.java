package net.lab1024.sa.visitor.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.visitor.domain.entity.VisitorEntity;
import net.lab1024.sa.visitor.domain.vo.VisitorSearchVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 访客数据访问层
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @date 2025-01-27
 */
@Mapper
@Repository
public interface VisitorDao extends BaseMapper<VisitorEntity> {

    /**
     * 查询访客列表（带搜索条件）
     *
     * @param searchVO 搜索条件
     * @return 访客列表
     */
    List<VisitorEntity> selectVisitorList(@Param("search") VisitorSearchVO searchVO);

    /**
     * 根据身份证号查询访客
     *
     * @param idNumber 身份证号
     * @return 访客信息
     */
    VisitorEntity selectByIdNumber(@Param("idNumber") String idNumber);

    /**
     * 查询指定时间段内有访客记录的访客
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 访客列表
     */
    List<VisitorEntity> selectVisitorByTimeRange(@Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 查询被访人相关的访客记录
     *
     * @param visiteeId 被访人ID
     * @param limit 限制数量
     * @return 访客列表
     */
    List<VisitorEntity> selectVisitorByVisiteeId(@Param("visiteeId") Long visiteeId,
                                                 @Param("limit") Integer limit);

    /**
     * 查询即将到期的访客预约
     *
     * @param hours 小时数（未来N小时内到期）
     * @return 访客列表
     */
    List<VisitorEntity> selectExpiringVisitors(@Param("hours") Integer hours);

    /**
     * 统计访客数量（按状态）
     *
     * @param status 访客状态
     * @return 数量
     */
    Long countByStatus(@Param("status") Integer status);

    /**
     * 批量更新访客状态
     *
     * @param visitorIds 访客ID列表
     * @param status 状态
     * @param updateUserId 更新人
     * @return 影响行数
     */
    int batchUpdateStatus(@Param("visitorIds") List<Long> visitorIds,
                          @Param("status") Integer status,
                          @Param("updateUserId") Long updateUserId);

    /**
     * 更新访客最后访问时间
     *
     * @param visitorId 访客ID
     * @param lastVisitTime 最后访问时间
     * @return 影响行数
     */
    int updateLastVisitTime(@Param("visitorId") Long visitorId,
                            @Param("lastVisitTime") LocalDateTime lastVisitTime);
}