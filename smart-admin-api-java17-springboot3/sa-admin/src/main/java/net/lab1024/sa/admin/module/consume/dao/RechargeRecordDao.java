package net.lab1024.sa.admin.module.consume.dao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.domain.dto.RechargeQueryDTO;
import net.lab1024.sa.admin.module.consume.domain.entity.RechargeRecordEntity;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;

/**
 * 充值记录 DAO
 *
 * <p>
 * 严格遵循repowiki规范:
 * - 使用组合模式，通过baseMapper调用MyBatis-Plus方法
 * - 提供复杂查询方法
 * - 使用QueryWrapper构建查询条件
 * - 包含分页查询支持
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */
@Slf4j
@Repository
public class RechargeRecordDao {

    @Resource
    private com.baomidou.mybatisplus.core.mapper.BaseMapper<RechargeRecordEntity> baseMapper;

    /**
     * 根据日期范围统计充值数量
     */
    public long countByDateRange(LocalDate startDate, LocalDate endDate) {
        try {
            QueryWrapper<RechargeRecordEntity> wrapper = new QueryWrapper<>();
            wrapper.between("recharge_time", startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
            return baseMapper.selectCount(wrapper);
        } catch (Exception e) {
            log.error("统计充值数量失败: startDate={}, endDate={}", startDate, endDate, e);
            return 0L;
        }
    }

    /**
     * 根据日期范围统计充值金额
     */
    public BigDecimal sumAmountByDateRange(LocalDate startDate, LocalDate endDate) {
        try {
            QueryWrapper<RechargeRecordEntity> wrapper = new QueryWrapper<>();
            wrapper.between("recharge_time", startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
            wrapper.eq("status", 2); // 只统计成功的充值
            wrapper.select("IFNULL(SUM(amount), 0) as totalAmount");

            List<Map<String, Object>> maps = baseMapper.selectMaps(wrapper);
            if (maps == null || maps.isEmpty()) {
                return BigDecimal.ZERO;
            }
            Map<String, Object> result = maps.get(0);
            BigDecimal totalAmount = (BigDecimal) result.get("totalAmount");
            return totalAmount != null ? totalAmount : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("统计充值金额失败: startDate={}, endDate={}", startDate, endDate, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 根据日期统计充值数量
     */
    public long countByDate(LocalDate date) {
        return countByDateRange(date, date);
    }

    /**
     * 根据日期统计充值金额
     */
    public BigDecimal sumAmountByDate(LocalDate date) {
        return sumAmountByDateRange(date, date);
    }

    /**
     * 获取账户金额变动记录
     */
    public List<Map<String, Object>> getAccountAmountChangesByDate(LocalDate date) {
        try {
            QueryWrapper<RechargeRecordEntity> wrapper = new QueryWrapper<>();
            wrapper.ge("recharge_time", date.atStartOfDay());
            wrapper.lt("recharge_time", date.plusDays(1).atStartOfDay());
            wrapper.orderByAsc("recharge_time");

            return baseMapper.selectMaps(wrapper);
        } catch (Exception e) {
            log.error("获取账户金额变动记录失败: date={}", date, e);
            return List.of();
        }
    }

    /**
     * 根据充值单号查询
     */
    public RechargeRecordEntity selectByRechargeNo(String rechargeNo) {
        try {
            if (rechargeNo == null || rechargeNo.trim().isEmpty()) {
                return null;
            }

            QueryWrapper<RechargeRecordEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("transaction_no", rechargeNo);
            return baseMapper.selectOne(wrapper);
        } catch (Exception e) {
            log.error("根据充值单号查询失败: rechargeNo={}", rechargeNo, e);
            return null;
        }
    }

    /**
     * 分页查询充值记录
     */
    public PageResult<RechargeRecordEntity> queryPage(RechargeQueryDTO queryDTO, PageParam pageParam) {
        try {
            QueryWrapper<RechargeRecordEntity> wrapper = buildQueryWrapper(queryDTO);

            IPage<RechargeRecordEntity> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
            IPage<RechargeRecordEntity> result = baseMapper.selectPage(page, wrapper);

            return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        } catch (Exception e) {
            log.error("分页查询充值记录失败", e);
            return PageResult.empty();
        }
    }

    /**
     * 根据用户ID查询充值记录
     */
    public List<RechargeRecordEntity> selectByUserId(Long userId, Integer limit) {
        try {
            QueryWrapper<RechargeRecordEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            wrapper.orderByDesc("recharge_time");
            if (limit != null && limit > 0) {
                wrapper.last("LIMIT " + limit);
            }
            return baseMapper.selectList(wrapper);
        } catch (Exception e) {
            log.error("根据用户ID查询充值记录失败: userId={}", userId, e);
            return List.of();
        }
    }

    /**
     * 根据用户ID和日期范围查询充值记录
     */
    public List<RechargeRecordEntity> selectByUserIdAndDateRange(Long userId, LocalDateTime startTime,
            LocalDateTime endTime) {
        try {
            QueryWrapper<RechargeRecordEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            if (startTime != null) {
                wrapper.ge("recharge_time", startTime);
            }
            if (endTime != null) {
                wrapper.le("recharge_time", endTime);
            }
            wrapper.orderByDesc("recharge_time");
            return baseMapper.selectList(wrapper);
        } catch (Exception e) {
            log.error("根据用户ID和日期范围查询充值记录失败: userId={}, startTime={}, endTime={}",
                    userId, startTime, endTime, e);
            return List.of();
        }
    }

    /**
     * 获取用户今日充值金额
     */
    public BigDecimal getTodayRechargeAmount(Long userId) {
        try {
            QueryWrapper<RechargeRecordEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            wrapper.eq("status", 2); // 成功状态
            wrapper.ge("recharge_time", LocalDate.now().atStartOfDay());
            wrapper.lt("recharge_time", LocalDate.now().plusDays(1).atStartOfDay());
            wrapper.select("IFNULL(SUM(amount), 0) as totalAmount");

            List<Map<String, Object>> maps = baseMapper.selectMaps(wrapper);
            if (maps == null || maps.isEmpty()) {
                return BigDecimal.ZERO;
            }
            Map<String, Object> result = maps.get(0);
            BigDecimal totalAmount = (BigDecimal) result.get("totalAmount");
            return totalAmount != null ? totalAmount : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("获取用户今日充值金额失败: userId={}", userId, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 构建查询条件
     */
    private QueryWrapper<RechargeRecordEntity> buildQueryWrapper(RechargeQueryDTO queryDTO) {
        QueryWrapper<RechargeRecordEntity> wrapper = new QueryWrapper<>();

        // 用户ID
        if (queryDTO.getUserId() != null) {
            wrapper.eq("user_id", queryDTO.getUserId());
        }

        // 充值状态
        if (queryDTO.getStatus() != null) {
            wrapper.eq("status", queryDTO.getStatus().getValue());
        }

        // 充值方式（使用rechargeType）
        if (queryDTO.getRechargeType() != null) {
            wrapper.eq("recharge_method", queryDTO.getRechargeType().getValue());
        }

        // 时间范围
        if (queryDTO.getStartTime() != null) {
            wrapper.ge("recharge_time", queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            wrapper.le("recharge_time", queryDTO.getEndTime());
        }

        // 充值单号（使用orderNo或thirdPartyOrderNo）
        if (queryDTO.getOrderNo() != null && !queryDTO.getOrderNo().trim().isEmpty()) {
            wrapper.like("transaction_no", queryDTO.getOrderNo().trim());
        } else if (queryDTO.getThirdPartyOrderNo() != null && !queryDTO.getThirdPartyOrderNo().trim().isEmpty()) {
            wrapper.like("transaction_no", queryDTO.getThirdPartyOrderNo().trim());
        }

        // 金额范围
        if (queryDTO.getMinAmount() != null) {
            wrapper.ge("amount", queryDTO.getMinAmount());
        }
        if (queryDTO.getMaxAmount() != null) {
            wrapper.le("amount", queryDTO.getMaxAmount());
        }

        // 排序
        wrapper.orderByDesc("recharge_time");

        return wrapper;
    }

    /**
     * 根据状态统计数量
     */
    public Map<Integer, Long> countByStatus() {
        try {
            Map<Integer, Long> statusCount = new HashMap<>();

            for (int status = 1; status <= 4; status++) {
                QueryWrapper<RechargeRecordEntity> wrapper = new QueryWrapper<>();
                wrapper.eq("status", status);
                long count = baseMapper.selectCount(wrapper);
                statusCount.put(status, count);
            }

            return statusCount;
        } catch (Exception e) {
            log.error("根据状态统计数量失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 根据充值方式统计数量
     */
    public Map<Integer, Long> countByRechargeMethod() {
        try {
            Map<Integer, Long> methodCount = new HashMap<>();

            for (int method = 1; method <= 5; method++) {
                QueryWrapper<RechargeRecordEntity> wrapper = new QueryWrapper<>();
                wrapper.eq("recharge_method", method);
                long count = baseMapper.selectCount(wrapper);
                methodCount.put(method, count);
            }

            return methodCount;
        } catch (Exception e) {
            log.error("根据充值方式统计数量失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 插入充值记录
     *
     * @param record 充值记录
     * @return 是否成功
     */
    public boolean insert(RechargeRecordEntity record) {
        try {
            return baseMapper.insert(record) > 0;
        } catch (Exception e) {
            log.error("插入充值记录失败", e);
            return false;
        }
    }

    /**
     * 根据ID更新充值记录
     *
     * @param record 充值记录
     * @return 是否成功
     */
    public boolean updateById(RechargeRecordEntity record) {
        try {
            return baseMapper.updateById(record) > 0;
        } catch (Exception e) {
            log.error("更新充值记录失败", e);
            return false;
        }
    }

    /**
     * 根据ID查询充值记录
     *
     * @param id 充值记录ID
     * @return 充值记录
     */
    public RechargeRecordEntity selectById(Long id) {
        try {
            return baseMapper.selectById(id);
        } catch (Exception e) {
            log.error("查询充值记录失败: id={}", id, e);
            return null;
        }
    }
}
