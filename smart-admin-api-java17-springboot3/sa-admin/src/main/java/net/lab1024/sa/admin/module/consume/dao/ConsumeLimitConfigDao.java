package net.lab1024.sa.admin.module.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.consume.domain.entity.ConsumeLimitConfigEntity;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 消费限额配置 DAO
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Mapper
public interface ConsumeLimitConfigDao extends BaseMapper<ConsumeLimitConfigEntity> {

    /**
     * 根据人员ID查询有效的限额配置
     *
     * @param personId 人员ID
     * @return 限额配置列表
     */
    List<ConsumeLimitConfigEntity> selectValidLimitsByPersonId(Long personId);

    /**
     * 根据人员ID和限额类型查询限额配置
     *
     * @param personId 人员ID
     * @param limitType 限额类型
     * @return 限额配置
     */
    ConsumeLimitConfigEntity selectByPersonIdAndLimitType(Long personId, String limitType);

    /**
     * 更新已使用金额
     *
     * @param limitConfigId 限额配置ID
     * @param usedAmount 已使用金额
     * @return 更新行数
     */
    int updateUsedAmount(Long limitConfigId, BigDecimal usedAmount);

    /**
     * 查询即将过期的限额配置
     *
     * @param expireTime 过期时间阈值
     * @return 限额配置列表
     */
    List<ConsumeLimitConfigEntity> selectExpiringLimits(LocalDateTime expireTime);

    /**
     * 根据限额状态查询配置
     *
     * @param limitStatus 限额状态
     * @return 限额配置列表
     */
    List<ConsumeLimitConfigEntity> selectByLimitStatus(String limitStatus);

    /**
     * 批量更新限额状态
     *
     * @param configIds 配置ID列表
     * @param limitStatus 限额状态
     * @return 更新行数
     */
    int batchUpdateLimitStatus(List<Long> configIds, String limitStatus);
}