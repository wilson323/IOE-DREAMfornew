package net.lab1024.sa.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.consume.entity.PosidSubsidyTypeEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * POSID补贴类型表DAO
 *
 * 对应表: POSID_SUBSIDY_TYPE
 * 职责: 补贴类型配置数据的数据库访问操作
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Mapper
public interface PosidSubsidyTypeDao extends BaseMapper<PosidSubsidyTypeEntity> {

    /**
     * 查询所有启用的补贴类型（按priority升序）
     *
     * @return 启用的补贴类型列表
     */
    List<PosidSubsidyTypeEntity> selectEnabledTypes();
}
