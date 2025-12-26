package net.lab1024.sa.visitor.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.entity.visitor.ElectronicPassEntity;
import org.springframework.util.StringUtils;

/**
 * 电子出门单数据访问层
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-04
 */
@Mapper
public interface ElectronicPassDao extends BaseMapper<ElectronicPassEntity> {

    /**
     * 根据出门单ID查询（兼容历史代码）
     *
     * <p>
     * 历史逻辑使用 String passId，这里做一次兼容转换并复用 MyBatis-Plus 的 {@code selectById}。
     * </p>
     *
     * @param passId 出门单ID（字符串形式）
     * @return 出门单实体（可能为空）
     */
    default ElectronicPassEntity selectByPassId(String passId) {
        if (!StringUtils.hasText(passId)) {
            return null;
        }
        try {
            return this.selectById(Long.valueOf(passId));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
