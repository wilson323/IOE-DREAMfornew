package net.lab1024.sa.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.access.domain.entity.BiometricRecordEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 生物识别记录数据访问层
 *
 * @author AI
 */
@Mapper
public interface BiometricRecordDao extends BaseMapper<BiometricRecordEntity> {
}