package net.lab1024.sa.oa.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.oa.document.domain.entity.DocumentEntity;

/**
 * 文档数据访问层
 *
 * @author IOE-DREAM Team
 */
@Mapper
public interface DocumentDao extends BaseMapper<DocumentEntity> {
}
