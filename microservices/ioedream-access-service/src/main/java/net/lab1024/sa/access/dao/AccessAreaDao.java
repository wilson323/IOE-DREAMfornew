package net.lab1024.sa.access.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.organization.entity.AreaEntity;

/**
 * 闂ㄧ鍖哄煙DAO
 * <p>
 * 涓ユ牸閬靛惊鍥涘眰鏋舵瀯瑙勮寖锛? * - 缁熶竴DAO妯″紡锛屼娇鐢―ao鍛藉悕
 * - 浣跨敤@Mapper娉ㄨВ锛岀姝娇鐢ˊMapper
 * - 鏌ヨ鏂规硶浣跨敤@Transactional(readOnly = true)
 * - 缁ф壙BaseMapper浣跨敤MyBatis-Plus
 * - 鑱岃矗鍗曚竴锛氬彧璐熻矗闂ㄧ鍖哄煙鏁版嵁璁块棶
 * - 鎻愪緵Manager灞傞渶瑕佺殑鏌ヨ鏂规硶
 * - 浣跨敤鍏叡AreaEntity鏇夸唬AccessAreaEntity
 *
 * @author SmartAdmin Team
 * @since 2025-12-01
 * @updated 2025-12-02 浣跨敤鍏叡AreaEntity锛岄伒寰猺epowiki瑙勮寖
 */
@Mapper
public interface AccessAreaDao extends BaseMapper<AreaEntity> {

    /**
     * 鏌ヨ鏍圭骇闂ㄧ鍖哄煙鍒楄〃
     * 浣跨敤鍏叡鍖哄煙琛細t_common_area
     *
     * @return 鏍圭骇鍖哄煙鍒楄〃
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_common_area WHERE parent_id IS NULL OR parent_id = 0 AND deleted_flag = 0 ORDER BY sort_index")
    List<AreaEntity> selectRootAreas();

    /**
     * 鏌ヨ鎸囧畾鍖哄煙鐨勬墍鏈夊瓙鍖哄煙
     *
     * @param parentId 鐖跺尯鍩烮D
     * @return 瀛愬尯鍩熷垪琛?     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_common_area WHERE parent_id = #{parentId} AND deleted_flag = 0 ORDER BY sort_index")
    List<AreaEntity> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 鏌ヨ鏈夋晥鐨勯棬绂佸尯鍩熷垪琛?     *
     * @return 鏈夋晥鍖哄煙鍒楄〃
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_common_area WHERE status = 1 AND deleted_flag = 0 ORDER BY sort_index")
    List<AreaEntity> selectActiveAreas();

    /**
     * 鏍规嵁鍖哄煙绫诲瀷鏌ヨ鍖哄煙鍒楄〃
     *
     * @param areaType 鍖哄煙绫诲瀷
     * @return 鍖哄煙鍒楄〃
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_common_area WHERE area_type = #{areaType} AND deleted_flag = 0 ORDER BY sort_index")
    List<AreaEntity> selectByAreaType(@Param("areaType") Integer areaType);

    /**
     * 鏌ヨ鎸囧畾鍖哄煙鐨勬墍鏈夊瓙鍖哄煙ID
     *
     * @param parentId 鐖跺尯鍩烮D
     * @return 瀛愬尯鍩烮D鍒楄〃
     */
    @Transactional(readOnly = true)
    @Select("SELECT area_id FROM t_common_area WHERE parent_id = #{parentId} AND deleted_flag = 0 ORDER BY sort_index")
    List<Long> selectChildrenIds(@Param("parentId") Long parentId);
}

