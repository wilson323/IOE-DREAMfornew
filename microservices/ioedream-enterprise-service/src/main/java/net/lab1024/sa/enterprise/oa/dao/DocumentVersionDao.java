package net.lab1024.sa.enterprise.oa.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.enterprise.oa.document.domain.entity.DocumentVersionEntity;

/**
 * 文档版本数据访问层
 *
 * @author IOE-DREAM Team
 */
@Mapper
public interface DocumentVersionDao extends BaseMapper<DocumentVersionEntity> {
}
