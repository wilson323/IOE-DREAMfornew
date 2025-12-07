package net.lab1024.sa.consume.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.consume.domain.entity.RefundApplicationEntity;

/**
 * 退款申请DAO
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Mapper注解
 * - 继承BaseMapper
 * - 使用Dao后缀命名
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface RefundApplicationDao extends BaseMapper<RefundApplicationEntity> {

    /**
     * 根据退款申请编号查询
     *
     * @param refundNo 退款申请编号
     * @return 退款申请实体
     */
    RefundApplicationEntity selectByRefundNo(@Param("refundNo") String refundNo);
}

