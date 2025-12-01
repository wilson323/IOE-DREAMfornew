package net.lab1024.sa.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.access.domain.entity.BiometricTemplateEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 生物特征模板数据访问层
 *
 * @author AI
 */
@Mapper
public interface BiometricTemplateDao extends BaseMapper<BiometricTemplateEntity> {
}