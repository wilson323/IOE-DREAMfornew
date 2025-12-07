package net.lab1024.sa.access.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.audit.entity.AuditLogEntity;

/**
 * Access record DAO
 * <p>
 * 严格遵循DAO架构规范：
 * - 统一DAO模式，使用Dao命名
 * - 使用@Mapper注解，禁止使用@Repository
 * - 查询方法使用@Transactional(readOnly = true)
 * - 继承BaseMapper使用MyBatis-Plus
 * - 职责单一：只负责门禁记录数据访问
 * - 使用公共AuditLogEntity替代AccessRecordEntity
 *
 * @author IOE-DREAM Team
 * @since 2025-11-16
 * @updated 2025-12-02 使用公共AuditLogEntity，遵循repowiki规范
 */
@Mapper
public interface AccessRecordDao extends BaseMapper<AuditLogEntity> {
}
