package net.lab1024.sa.visitor.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.visitor.VisitRecordEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 访客访问记录DAO
 * <p>
 * 严格遵循CLAUDE.md全局架构规范
 * 使用@Mapper注解而非@Repository
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0
 * @since 2025-12-26
 */
@Mapper
public interface VisitRecordDao extends BaseMapper<VisitRecordEntity> {
}
