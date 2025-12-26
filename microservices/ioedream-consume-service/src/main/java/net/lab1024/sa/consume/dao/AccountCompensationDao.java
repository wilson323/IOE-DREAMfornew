package net.lab1024.sa.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.consume.AccountCompensationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 账户服务补偿记录DAO
 * <p>
 * 提供补偿记录的数据访问操作
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Mapper
public interface AccountCompensationDao extends BaseMapper<AccountCompensationEntity> {

    /**
     * 查询待处理的补偿记录
     *
     * @param limit 限制数量
     * @return 待处理的补偿记录列表
     */
    @Select("SELECT * FROM t_account_compensation" +
            " WHERE status = 'PENDING'" +
            "   AND deleted_flag = 0" +
            "   AND next_retry_time <= #{now}" +
            "   AND retry_count < max_retry_count" +
            " ORDER BY next_retry_time ASC" +
            " LIMIT #{limit}")
    List<AccountCompensationEntity> selectPendingCompensations(@Param("now") LocalDateTime now, @Param("limit") int limit);

    /**
     * 根据业务编号查询补偿记录
     *
     * @param businessNo 业务编号
     * @return 补偿记录
     */
    @Select("SELECT * FROM t_account_compensation" +
            " WHERE business_no = #{businessNo}" +
            "   AND deleted_flag = 0" +
            " LIMIT 1")
    AccountCompensationEntity selectByBusinessNo(@Param("businessNo") String businessNo);

    /**
     * 查询用户的补偿记录
     *
     * @param userId 用户ID
     * @return 补偿记录列表
     */
    @Select("SELECT * FROM t_account_compensation" +
            " WHERE user_id = #{userId}" +
            "   AND deleted_flag = 0" +
            " ORDER BY create_time DESC")
    List<AccountCompensationEntity> selectByUserId(@Param("userId") Long userId);

    /**
     * 统计待处理的补偿记录数量
     *
     * @return 待处理数量
     */
    @Select("SELECT COUNT(*) FROM t_account_compensation" +
            " WHERE status = 'PENDING'" +
            "   AND deleted_flag = 0" +
            "   AND next_retry_time <= #{now}" +
            "   AND retry_count < max_retry_count")
    int countPendingCompensations(@Param("now") LocalDateTime now);
}
