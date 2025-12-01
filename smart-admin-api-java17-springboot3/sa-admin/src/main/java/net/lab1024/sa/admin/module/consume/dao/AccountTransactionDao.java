package net.lab1024.sa.admin.module.consume.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import net.lab1024.sa.admin.module.consume.domain.entity.AccountTransactionEntity;
import net.lab1024.sa.admin.module.consume.domain.vo.AccountTransactionVO;

/**
 * 账户交易记录DAO
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Mapper
public interface AccountTransactionDao extends BaseMapper<AccountTransactionEntity> {

    /**
     * 分页查询账户交易记录
     *
     * @param page            分页参数
     * @param accountId       账户ID
     * @param startTime       开始时间
     * @param endTime         结束时间
     * @param transactionType 交易类型
     * @return 交易记录列表
     */
    List<AccountTransactionVO> queryTransactions(Page<AccountTransactionVO> page,
            @Param("accountId") Long accountId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("transactionType") String transactionType);
}
