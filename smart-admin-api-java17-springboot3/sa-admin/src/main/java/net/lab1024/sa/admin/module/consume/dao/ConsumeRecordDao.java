package net.lab1024.sa.admin.module.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.consume.domain.entity.ConsumeRecordEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConsumeRecordDao extends BaseMapper<ConsumeRecordEntity> {
}

