package net.lab1024.sa.access.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.access.entity.AreaAccessExtEntity;

/**
 * 鍖哄煙闂ㄧ鎵╁睍Repository
 * <p>
 * 涓ユ牸閬靛惊Repository鏋舵瀯瑙勮寖锛? * - 缁熶竴Repository妯″紡锛岀鐢―ao鍛藉悕
 * - 浣跨敤@Mapper娉ㄨВ鏇夸唬@Mapper
 * - 鏌ヨ鏂规硶浣跨敤@Transactional(readOnly = true)
 * - 缁ф壙BaseMapper浣跨敤MyBatis-Plus
 * - 鍩轰簬AreaAccessExtEntity鎻愪緵鏁版嵁璁块棶鏂规硶
 * - 鍛藉悕瑙勮寖缁熶竴锛氱鍚坽BaseDomain}{Module}ExtRepository鏍囧噯妯″紡
 * - 涓ユ牸閬靛惊椤圭洰鐜版湁浠ｇ爜椋庢牸鍜屾灦鏋勮鑼? * - 鎻愪緵鎵╁睍鏌ヨ鍜屾€ц兘浼樺寲鍔熻兘
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 * @updated 2025-12-01 閬靛惊Repository鏋舵瀯瑙勮寖閲嶆瀯
 */
@Mapper
public interface AreaAccessExtDao extends BaseMapper<AreaAccessExtEntity> {

    /**
     * 鏍规嵁鍖哄煙ID鏌ヨ鎵╁睍淇℃伅
     * 鍏宠仈鏌ヨ鍩虹鍖哄煙淇℃伅锛岀‘淇濇暟鎹竴鑷存€?     */
    @Transactional(readOnly = true)
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_access_area_ext e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE e.area_id = #{areaId} AND a.deleted_flag = 0")
    AreaAccessExtEntity selectByAreaIdWithBaseInfo(@Param("areaId") Long areaId);

    /**
     * 鏍规嵁闂ㄧ绾у埆鏌ヨ鍖哄煙鎵╁睍淇℃伅
     * 鍏宠仈鍩虹鍖哄煙琛紝鏀寔鎸夐棬绂佺骇鍒繃婊?     */
    @Transactional(readOnly = true)
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_access_area_ext e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE e.access_level = #{accessLevel} AND a.deleted_flag = 0 " +
            "ORDER BY a.sort_order ASC")
    List<AreaAccessExtEntity> selectByAccessLevel(@Param("accessLevel") Integer accessLevel);

    /**
     * 鏍规嵁闂ㄧ妯″紡鏌ヨ鍖哄煙鎵╁睍淇℃伅
     * 鏀寔妯＄硦鍖归厤锛屽叧鑱斿熀纭€鍖哄煙琛?     */
    @Transactional(readOnly = true)
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_access_area_ext e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE e.access_mode LIKE CONCAT('%', #{accessMode}, '%') AND a.deleted_flag = 0 " +
            "ORDER BY a.sort_order ASC")
    List<AreaAccessExtEntity> selectByAccessModeLike(@Param("accessMode") String accessMode);

    /**
     * 鏌ヨ鍖呭惈鎸囧畾楠岃瘉鏂瑰紡鐨勫尯鍩?     * 鍏宠仈鍩虹鍖哄煙琛紝鏀寔楠岃瘉鏂瑰紡杩囨护
     */
    @Transactional(readOnly = true)
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_access_area_ext e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE e.access_mode LIKE CONCAT('%', #{verificationMode}, '%') AND a.deleted_flag = 0 " +
            "ORDER BY a.sort_order ASC")
    List<AreaAccessExtEntity> selectByVerificationMode(@Param("verificationMode") String verificationMode);

    /**
     * 鏌ヨ楂橀棬绂佺骇鍒殑鍖哄煙锛坅ccess_level >= 2锛?     * 鍏宠仈鍩虹鍖哄煙琛紝鎸夐棬绂佺骇鍒檷搴忔帓鍒?     */
    @Transactional(readOnly = true)
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_access_area_ext e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE e.access_level >= #{minLevel} AND a.deleted_flag = 0 " +
            "ORDER BY e.access_level DESC, a.sort_order ASC")
    List<AreaAccessExtEntity> selectByMinAccessLevel(@Param("minLevel") Integer minLevel);

    /**
     * 鎵归噺鏌ヨ鍖哄煙鎵╁睍淇℃伅
     * 鍏宠仈鍩虹鍖哄煙琛紝鏀寔澶氫釜鍖哄煙ID
     */
    @Transactional(readOnly = true)
    @Select("<script>" +
            "SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_access_area_ext e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE e.area_id IN " +
            "<foreach collection='areaIds' item='areaId' open='(' separator=',' close=')'>" +
            "#{areaId}" +
            "</foreach>" +
            "AND a.deleted_flag = 0 " +
            "ORDER BY a.sort_order ASC" +
            "</script>")
    List<AreaAccessExtEntity> selectByAreaIdsWithBaseInfo(@Param("areaIds") List<Long> areaIds);

    /**
     * 鏌ヨ璁惧鏁伴噺澶т簬鎸囧畾鏁伴噺鐨勫尯鍩?     * 鍏宠仈鍩虹鍖哄煙琛紝鎸夎澶囨暟閲忛檷搴忔帓鍒?     */
    @Transactional(readOnly = true)
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_access_area_ext e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE e.device_count > #{minDeviceCount} AND a.deleted_flag = 0 " +
            "ORDER BY e.device_count DESC, a.sort_order ASC")
    List<AreaAccessExtEntity> selectByMinDeviceCount(@Param("minDeviceCount") Integer minDeviceCount);

    /**
     * 鏇存柊鍖哄煙闂ㄧ绾у埆
     *
     * @param areaId       鍖哄煙ID
     * @param accessLevel  闂ㄧ绾у埆
     * @param updateUserId 鏇存柊鐢ㄦ埛ID
     * @return 鏇存柊琛屾暟
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE t_access_area_ext SET " +
            "access_level = #{accessLevel}, " +
            "update_time = NOW(), " +
            "update_user_id = #{updateUserId} " +
            "WHERE area_id = #{areaId} AND deleted_flag = 0")
    int updateAccessLevel(@Param("areaId") Long areaId, @Param("accessLevel") Integer accessLevel,
            @Param("updateUserId") Long updateUserId);

    /**
     * 鏇存柊闂ㄧ妯″紡閰嶇疆
     *
     * @param areaId       鍖哄煙ID
     * @param accessMode   闂ㄧ妯″紡
     * @param updateUserId 鏇存柊鐢ㄦ埛ID
     * @return 鏇存柊琛屾暟
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE t_access_area_ext SET " +
            "access_mode = #{accessMode}, " +
            "update_time = NOW(), " +
            "update_user_id = #{updateUserId} " +
            "WHERE area_id = #{areaId} AND deleted_flag = 0")
    int updateAccessMode(@Param("areaId") Long areaId, @Param("accessMode") String accessMode,
            @Param("updateUserId") Long updateUserId);

    /**
     * 鏇存柊鍏宠仈璁惧鏁伴噺
     *
     * @param areaId       鍖哄煙ID
     * @param deviceCount  璁惧鏁伴噺
     * @param updateUserId 鏇存柊鐢ㄦ埛ID
     * @return 鏇存柊琛屾暟
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE t_access_area_ext SET " +
            "device_count = #{deviceCount}, " +
            "update_time = NOW(), " +
            "update_user_id = #{updateUserId} " +
            "WHERE area_id = #{areaId} AND deleted_flag = 0")
    int updateDeviceCount(@Param("areaId") Long areaId, @Param("deviceCount") Integer deviceCount,
            @Param("updateUserId") Long updateUserId);

    /**
     * 杞垹闄ゅ尯鍩熸墿灞曚俊鎭?     *
     * @param areaId       鍖哄煙ID
     * @param updateUserId 鏇存柊鐢ㄦ埛ID
     * @return 鍒犻櫎琛屾暟
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE t_access_area_ext SET " +
            "deleted_flag = 1, " +
            "update_time = NOW(), " +
            "update_user_id = #{updateUserId} " +
            "WHERE area_id = #{areaId}")
    int softDeleteByAreaId(@Param("areaId") Long areaId, @Param("updateUserId") Long updateUserId);

    /**
     * 鎵归噺杞垹闄ゅ尯鍩熸墿灞曚俊鎭?     *
     * @param areaIds      鍖哄煙ID鍒楄〃
     * @param updateUserId 鏇存柊鐢ㄦ埛ID
     * @return 鍒犻櫎琛屾暟
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("<script>" +
            "UPDATE t_access_area_ext SET " +
            "deleted_flag = 1, " +
            "update_time = NOW(), " +
            "update_user_id = #{updateUserId} " +
            "WHERE area_id IN " +
            "<foreach collection='areaIds' item='areaId' open='(' separator=',' close=')'>" +
            "#{areaId}" +
            "</foreach>" +
            "</script>")
    int batchSoftDeleteByAreaIds(@Param("areaIds") List<Long> areaIds, @Param("updateUserId") Long updateUserId);

    /**
     * 缁熻鍖哄煙鎵╁睍淇℃伅鏁伴噺
     *
     * @return 鎬绘暟閲?     */
    @Transactional(readOnly = true)
    @Select("SELECT COUNT(*) FROM t_access_area_ext WHERE deleted_flag = 0")
    long countTotal();

    /**
     * 鏍规嵁闂ㄧ绾у埆缁熻鏁伴噺
     *
     * @return 绾у埆缁熻淇℃伅
     */
    @Transactional(readOnly = true)
    @Select("SELECT " +
            "access_level, " +
            "COUNT(*) as count " +
            "FROM t_access_area_ext " +
            "WHERE deleted_flag = 0 " +
            "GROUP BY access_level " +
            "ORDER BY access_level")
    List<java.util.Map<String, Object>> countByAccessLevel();

    /**
     * 缁熻鍚勯棬绂佹ā寮忕殑鍖哄煙鏁伴噺
     *
     * @return 妯″紡缁熻淇℃伅
     */
    @Transactional(readOnly = true)
    @Select("SELECT " +
            "access_mode, " +
            "COUNT(*) as count " +
            "FROM t_access_area_ext " +
            "WHERE deleted_flag = 0 AND access_mode IS NOT NULL " +
            "GROUP BY access_mode " +
            "ORDER BY count DESC")
    List<java.util.Map<String, Object>> countByAccessMode();

    /**
     * 缁熻璁惧鏁伴噺鍒嗗竷鎯呭喌
     *
     * @return 璁惧鏁伴噺缁熻淇℃伅
     */
    @Transactional(readOnly = true)
    @Select("SELECT " +
            "CASE " +
            "WHEN device_count = 0 THEN '鏃犺澶? " +
            "WHEN device_count BETWEEN 1 AND 5 THEN '1-5鍙? " +
            "WHEN device_count BETWEEN 6 AND 10 THEN '6-10鍙? " +
            "WHEN device_count BETWEEN 11 AND 20 THEN '11-20鍙? " +
            "ELSE '20鍙颁互涓? " +
            "END as device_range, " +
            "COUNT(*) as count " +
            "FROM t_access_area_ext " +
            "WHERE deleted_flag = 0 " +
            "GROUP BY " +
            "CASE " +
            "WHEN device_count = 0 THEN '鏃犺澶? " +
            "WHEN device_count BETWEEN 1 AND 5 THEN '1-5鍙? " +
            "WHEN device_count BETWEEN 6 AND 10 THEN '6-10鍙? " +
            "WHEN device_count BETWEEN 11 AND 20 THEN '11-20鍙? " +
            "ELSE '20鍙颁互涓? " +
            "END " +
            "ORDER BY MIN(device_count)")
    List<java.util.Map<String, Object>> countByDeviceRange();

    /**
     * 鏌ヨ楂橀棬绂佺骇鍒殑鍖哄煙锛坅ccess_level >= 2锛?     * 鍏宠仈鍩虹鍖哄煙琛?     *
     * @return 楂樼骇鍒尯鍩熷垪琛?     */
    @Transactional(readOnly = true)
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_access_area_ext e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE e.access_level >= 2 AND a.deleted_flag = 0 " +
            "ORDER BY e.access_level DESC, a.sort_order ASC")
    List<AreaAccessExtEntity> selectHighAccessLevelAreas();

    /**
     * 鏌ヨ澶氶獙璇佹柟寮忕殑鍖哄煙锛坅ccess_mode鍖呭惈澶氫釜楠岃瘉鏂瑰紡锛?     * 鍏宠仈鍩虹鍖哄煙琛?     *
     * @return 澶氶獙璇佹柟寮忓尯鍩熷垪琛?     */
    @Transactional(readOnly = true)
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_access_area_ext e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE " +
            "(CHAR_LENGTH(e.access_mode) - CHAR_LENGTH(REPLACE(e.access_mode, ',', '')) + 1) > 1 " +
            "AND a.deleted_flag = 0 " +
            "ORDER BY CHAR_LENGTH(e.access_mode) DESC, a.sort_order ASC")
    List<AreaAccessExtEntity> selectMultiVerificationAreas();

    /**
     * 鏌ヨ鎸囧畾鐖跺尯鍩熶笅鐨勬墍鏈夊瓙鍖哄煙鎵╁睍淇℃伅
     * 鍏宠仈鍩虹鍖哄煙琛紝鏀寔灞傜骇鏌ヨ
     *
     * @param parentPath 鐖跺尯鍩熻矾寰?     * @return 瀛愬尯鍩熸墿灞曚俊鎭垪琛?     */
    @Transactional(readOnly = true)
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_access_area_ext e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE a.path LIKE CONCAT(#{parentPath}, '%') AND a.deleted_flag = 0 " +
            "ORDER BY a.sort_order ASC")
    List<AreaAccessExtEntity> selectByParentPath(@Param("parentPath") String parentPath);

    /**
     * 缁熻鍚敤鎵╁睍鍔熻兘鐨勫尯鍩熸暟閲?     * 鍩轰簬鐜版湁瀛楁鍒ゆ柇鏄惁鏈夋墿灞曢厤缃?     *
     * @return 鍚敤鏁伴噺
     */
    @Transactional(readOnly = true)
    @Select("SELECT COUNT(*) FROM t_access_area_ext " +
            "WHERE (access_level > 0 OR device_count > 0 OR access_mode IS NOT NULL) AND deleted_flag = 0")
    long countEnabled();

    /**
     * 鏌ヨ鍖哄煙鎵╁睍閰嶇疆淇℃伅姹囨€荤粺璁?     * 鎻愪緵缁煎悎鐨勭粺璁¤鍥?     *
     * @return 缁熻姹囨€讳俊鎭?     */
    @Transactional(readOnly = true)
    @Select("SELECT " +
            "COUNT(*) as total_areas, " +
            "COUNT(CASE WHEN access_level >= 3 THEN 1 END) as high_security_areas, " +
            "COUNT(CASE WHEN access_mode LIKE '%,%' THEN 1 END) as multi_verification_areas, " +
            "COUNT(CASE WHEN device_count > 0 THEN 1 END) as areas_with_devices, " +
            "SUM(device_count) as total_devices, " +
            "MAX(access_level) as max_access_level " +
            "FROM t_access_area_ext " +
            "WHERE deleted_flag = 0")
    java.util.Map<String, Object> getAreaExtensionStatistics();
}

