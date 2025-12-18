package net.lab1024.sa.device.comm.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.device.comm.entity.ProtocolConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 协议配置数据访问层
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Mapper
public interface ProtocolConfigDao extends BaseMapper<ProtocolConfigEntity> {

    /**
     * 根据协议类型查询配置
     *
     * @param protocolType 协议类型
     * @return 配置信息
     */
    ProtocolConfigEntity selectByProtocolType(@Param("protocolType") String protocolType);

    /**
     * 查询所有启用的协议配置
     *
     * @return 配置列表
     */
    List<ProtocolConfigEntity> selectAllEnabled();
}
