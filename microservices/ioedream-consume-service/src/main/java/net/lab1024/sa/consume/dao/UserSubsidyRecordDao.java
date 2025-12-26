package net.lab1024.sa.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.consume.entity.UserSubsidyRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 用户补贴记录DAO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
@Mapper
public interface UserSubsidyRecordDao extends BaseMapper<UserSubsidyRecordEntity> {

    /**
     * 查询用户补贴记录
     */
    List<UserSubsidyRecordEntity> selectByUserId(@Param("userId") Long userId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    /**
     * 查询用户日期补贴记录
     */
    UserSubsidyRecordEntity selectByUserAndDate(@Param("userId") Long userId,
                                               @Param("subsidyDate") LocalDate subsidyDate,
                                               @Param("subsidyType") Integer subsidyType);

    /**
     * 统计用户补贴总额
     */
    BigDecimal sumSubsidyByUser(@Param("userId") Long userId,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);

    /**
     * 统计日期补贴总额
     */
    BigDecimal sumSubsidyByDate(@Param("subsidyDate") LocalDate subsidyDate,
                                @Param("subsidyType") Integer subsidyType);

    /**
     * 查询规则补贴记录
     */
    List<UserSubsidyRecordEntity> selectByRuleId(@Param("ruleId") Long ruleId,
                                                @Param("limit") Integer limit);
}
