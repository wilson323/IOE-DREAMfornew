package net.lab1024.sa.admin.module.system.area.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.system.area.domain.entity.AreaEntity;
import net.lab1024.sa.admin.module.system.area.domain.form.AreaQueryForm;
import net.lab1024.sa.admin.module.system.area.domain.vo.AreaVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 区域DAO
 *
 * @author SmartAdmin
 * @date 2025-01-10
 */
@Mapper
public interface AreaDao extends BaseMapper<AreaEntity> {

    /**
     * 分页查询区域
     *
     * @param page 分页参数
     * @param queryForm 查询条件
     * @return 区域列表
     */
    List<AreaVO> queryPage(Page<?> page, @Param("queryForm") AreaQueryForm queryForm);

    /**
     * 根据区域ID查询区域详情
     *
     * @param areaId 区域ID
     * @return 区域详情
     */
    AreaVO selectAreaVO(@Param("areaId") Long areaId);

    /**
     * 查询所有区域列表
     *
     * @return 区域列表
     */
    List<AreaVO> listAll();

    /**
     * 根据父区域ID查询子区域数量
     *
     * @param parentId 父区域ID
     * @return 子区域数量
     */
    Integer countByParentId(@Param("parentId") Long parentId);

    /**
     * 根据区域编码查询区域
     *
     * @param areaCode 区域编码
     * @return 区域
     */
    AreaEntity selectByAreaCode(@Param("areaCode") String areaCode);

    /**
     * 查询某个区域下的所有子区域（递归）
     *
     * @param areaId 区域ID
     * @return 子区域ID列表
     */
    List<Long> selectChildrenAreaIds(@Param("areaId") Long areaId);

}
