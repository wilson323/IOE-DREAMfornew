package net.lab1024.sa.common.monitor.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.monitor.domain.entity.AlertEntity;

/**
 * 告警数据访问接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
@Mapper
public interface AlertDao extends BaseMapper<AlertEntity> {

    /**
     * 查询最近的告警记录
     *
     * @param limit 限制数量
     * @return 告警列表
     */
    List<AlertEntity> selectRecentAlerts(@Param("limit") Integer limit);
}
