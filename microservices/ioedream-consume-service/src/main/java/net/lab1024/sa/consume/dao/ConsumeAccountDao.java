package net.lab1024.sa.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.consume.entity.ConsumeAccountEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * 消费账户DAO接口
 */
@Mapper
public interface ConsumeAccountDao extends BaseMapper<ConsumeAccountEntity> {

    /**
     * 根据用户ID查询账户
     */
    @Select("SELECT * FROM t_consume_account WHERE user_id = #{userId} AND deleted_flag = 0")
    ConsumeAccountEntity selectByUserId(@Param("userId") Long userId);

    /**
     * 根据账户编号查询账户
     */
    @Select("SELECT * FROM t_consume_account WHERE account_no = #{accountNo} AND deleted_flag = 0")
    ConsumeAccountEntity selectByAccountNo(@Param("accountNo") String accountNo);

    /**
     * 更新账户余额
     */
    @Update("UPDATE t_consume_account SET balance = #{balance}, update_time = NOW() WHERE id = #{accountId} AND deleted_flag = 0")
    int updateBalance(@Param("accountId") Long accountId, @Param("balance") BigDecimal balance);

    /**
     * 扣减账户余额
     */
    @Update("UPDATE t_consume_account SET balance = balance - #{amount}, update_time = NOW() WHERE id = #{accountId} AND balance >= #{amount} AND deleted_flag = 0")
    int deductBalance(@Param("accountId") Long accountId, @Param("amount") BigDecimal amount);

    /**
     * 增加账户余额
     */
    @Update("UPDATE t_consume_account SET balance = balance + #{amount}, update_time = NOW() WHERE id = #{accountId} AND deleted_flag = 0")
    int addBalance(@Param("accountId") Long accountId, @Param("amount") BigDecimal amount);
}