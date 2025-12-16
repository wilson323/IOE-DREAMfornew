package net.lab1024.sa.consume.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.consume.entity.ReimbursementApplicationEntity;

/**
 * 报销申请DAO
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
public interface ReimbursementApplicationDao extends BaseMapper<ReimbursementApplicationEntity> {

    /**
     * 根据报销申请编号查询
     *
     * @param reimbursementNo 报销申请编号
     * @return 报销申请实体
     */
    ReimbursementApplicationEntity selectByReimbursementNo(@Param("reimbursementNo") String reimbursementNo);
}




