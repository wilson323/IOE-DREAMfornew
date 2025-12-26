package net.lab1024.sa.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.consume.entity.PosidAreaEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * POSID区域表DAO
 *
 * 对应表: POSID_AREA
 * 职责: 区域配置数据的数据库访问操作
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Mapper
public interface PosidAreaDao extends BaseMapper<PosidAreaEntity> {

    /**
     * 根据区域编码查询区域
     *
     * @param areaCode 区域编码
     * @return 区域实体
     */
    PosidAreaEntity selectByAreaCode(@Param("areaCode") String areaCode);

    /**
     * 查询子区域列表
     *
     * @param parentAreaId 父区域ID
     * @return 子区域列表
     */
    List<PosidAreaEntity> selectByParentAreaId(@Param("parentAreaId") Long parentAreaId);

    /**
     * 查询所有启用的区域
     *
     * @return 启用的区域列表
     */
    List<PosidAreaEntity> selectEnabledAreas();
}
