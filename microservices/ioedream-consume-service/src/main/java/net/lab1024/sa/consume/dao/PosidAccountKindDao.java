package net.lab1024.sa.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.consume.entity.PosidAccountKindEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * POSID账户类别表DAO
 *
 * 对应表: POSID_ACCOUNTKIND
 * 职责: 账户类别配置数据的数据库访问操作
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Mapper
public interface PosidAccountKindDao extends BaseMapper<PosidAccountKindEntity> {
    // 继承BaseMapper，提供基本的CRUD操作
    // 自定义查询方法可在此添加
}
