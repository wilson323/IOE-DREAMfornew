package net.lab1024.sa.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.consume.domain.entity.ReconciliationRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 消费记录对账DAO
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Mapper
public interface ReconciliationRecordDao extends BaseMapper<ReconciliationRecordEntity> {

    /**
     * 查询指定日期范围的对账记录
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 对账记录列表
     */
    @Select("SELECT * FROM t_consume_reconciliation_record " +
            "WHERE start_date >= #{startDate} AND end_date <= #{endDate} " +
            "ORDER BY create_time DESC")
    List<ReconciliationRecordEntity> queryByDateRange(@Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    /**
     * 查询最新的对账记录
     *
     * @param limit 限制数量
     * @return 对账记录列表
     */
    @Select("SELECT * FROM t_consume_reconciliation_record " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<ReconciliationRecordEntity> queryRecentRecords(@Param("limit") int limit);
}
