package net.lab1024.sa.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.consume.ConsumeSubsidyTypeEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 消费补贴类型表DAO
 *
 * 对应表: t_consume_subsidy_type
 * 职责: 补贴类型配置数据的数据库访问操作
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-26
 */
@Mapper
public interface ConsumeSubsidyTypeDao extends BaseMapper<ConsumeSubsidyTypeEntity> {

    /**
     * 查询所有启用的补贴类型（按priority升序）
     *
     * @return 启用的补贴类型列表
     */
    List<ConsumeSubsidyTypeEntity> selectEnabledTypes();
}
