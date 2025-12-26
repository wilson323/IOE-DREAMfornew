package net.lab1024.sa.consume.dao;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.consume.domain.form.ConsumeAccountQueryForm;
import net.lab1024.sa.consume.domain.vo.ConsumeAccountVO;
import net.lab1024.sa.common.entity.consume.ConsumeAccountEntity;

/**
 * 消费账户数据访问对象
 * <p>
 * 遵循MyBatis-Plus规范，使用@Mapper注解而非@Repository
 * 提供账户相关的数据访问操作
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Mapper
public interface ConsumeAccountDao extends BaseMapper<ConsumeAccountEntity> {

    /**
     * 分页查询账户列表（返回IPage，支持MyBatis-Plus分页）
     *
     * @param page 分页对象
     * @param queryForm 查询条件
     * @return 分页结果
     */
    com.baomidou.mybatisplus.core.metadata.IPage<ConsumeAccountVO> selectPage(
        @Param("page") com.baomidou.mybatisplus.extension.plugins.pagination.Page<ConsumeAccountVO> page,
        @Param("queryForm") ConsumeAccountQueryForm queryForm);

    /**
     * 根据账户ID查询账户信息（返回VO）
     *
     * @param accountId 账户ID
     * @return 账户信息
     */
    ConsumeAccountVO selectAccountById(@Param("accountId") Long accountId);

    /**
     * 根据用户ID查询账户信息
     *
     * @param userId 用户ID
     * @return 账户信息
     */
    ConsumeAccountVO selectByUserId(@Param("userId") Long userId);

    /**
     * 分页查询账户列表
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    PageResult<ConsumeAccountVO> queryPage(@Param("queryForm") ConsumeAccountQueryForm queryForm);

    /**
     * 更新账户余额
     *
     * @param accountId 账户ID
     * @param newBalance 新余额
     * @return 更新行数
     */
    int updateBalance(@Param("accountId") Long accountId, @Param("newBalance") BigDecimal newBalance);

    /**
     * 乐观锁更新账户余额（包含版本号验证）
     *
     * @param accountId 账户ID
     * @param newBalance 新余额
     * @param version 当前版本号
     * @return 更新行数
     */
    int updateBalanceWithVersion(@Param("accountId") Long accountId,
                               @Param("newBalance") BigDecimal newBalance,
                               @Param("version") Integer version);

    /**
     * 更新账户状态
     *
     * @param accountId 账户ID
     * @param status 状态 (1-正常 2-冻结 3-注销)
     * @return 更新行数
     */
    int updateStatus(@Param("accountId") Long accountId, @Param("status") Integer status);

    /**
     * 获取账户统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getAccountStatistics();

    /**
     * 批量查询账户信息
     *
     * @param accountIds 账户ID列表
     * @return 账户信息列表
     */
    java.util.List<ConsumeAccountVO> selectBatchByIds(@Param("accountIds") java.util.List<Long> accountIds);
}