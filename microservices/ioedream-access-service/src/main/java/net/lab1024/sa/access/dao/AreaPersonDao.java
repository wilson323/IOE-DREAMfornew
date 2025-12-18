package net.lab1024.sa.access.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.organization.entity.AreaPersonEntity;

/**
 * 浜哄憳鍖哄煙鎺堟潈DAO
 * <p>
 * 涓ユ牸閬靛惊DAO鏋舵瀯瑙勮寖锛? * - 缁熶竴DAO妯″紡锛屼娇鐢―ao鍛藉悕
 * - 浣跨敤@Mapper娉ㄨВ锛岀姝娇鐢ˊMapper
 * - 鏌ヨ鏂规硶浣跨敤@Transactional(readOnly = true)
 * - 缁ф壙BaseMapper浣跨敤MyBatis-Plus
 * - 鑱岃矗鍗曚竴锛氬彧璐熻矗浜哄憳鍖哄煙鎺堟潈鏁版嵁璁块棶
 * - 浣跨敤鍏叡AreaPersonEntity鏇夸唬鑷畾涔塃ntity
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 * @updated 2025-12-01 閬靛惊DAO鏋舵瀯瑙勮寖閲嶆瀯
 * @updated 2025-12-02 浣跨敤鍏叡AreaPersonEntity锛岄伒寰猺epowiki瑙勮寖
 */
@Mapper
public interface AreaPersonDao extends BaseMapper<AreaPersonEntity> {

    /**
     * 鏍规嵁浜哄憳ID鏌ヨ鍖哄煙ID鍒楄〃
     *
     * @param personId 浜哄憳ID
     * @return 鍖哄煙ID鍒楄〃
     */
    @Transactional(readOnly = true)
    List<Long> getAreaIdsByPersonId(@Param("personId") Long personId);

    /**
     * 鏍规嵁鏃堕棿鑼冨洿鏌ヨ鏈夋晥鏉冮檺
     *
     * @param personId  浜哄憳ID
     * @param startTime 寮€濮嬫椂闂?     * @param endTime   缁撴潫鏃堕棿
     * @return 鏉冮檺鍒楄〃
     */
    @Transactional(readOnly = true)
    List<AreaPersonEntity> getEffectivePermissionsByTimeRange(
            @Param("personId") Long personId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 鏌ヨ鍗冲皢杩囨湡鐨勬潈闄?     *
     * @param days 澶╂暟
     * @return 鍗冲皢杩囨湡鐨勬潈闄愬垪琛?     */
    @Transactional(readOnly = true)
    List<AreaPersonEntity> getExpiringPermissions(@Param("days") Integer days);

    /**
     * 娓呯悊杩囨湡鏉冮檺
     *
     * @param expireTime 杩囨湡鏃堕棿
     * @return 娓呯悊鏁伴噺
     */
    @Transactional(rollbackFor = Exception.class)
    int cleanExpiredPermissions(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 缁熻鍖哄煙鏉冮檺鏁伴噺
     *
     * @param areaId 鍖哄煙ID
     * @return 鏉冮檺鏁伴噺
     */
    @Transactional(readOnly = true)
    Long countAreaPermissions(@Param("areaId") Long areaId);

    /**
     * 缁熻浜哄憳鍦ㄦ寚瀹氬尯鍩熺殑鏉冮檺鏁伴噺
     *
     * @param personId 浜哄憳ID
     * @return 鏉冮檺鏁伴噺
     */
    @Transactional(readOnly = true)
    Long countPersonPermissionsByArea(@Param("personId") Long personId);

    /**
     * 鑾峰彇鍖哄煙鏉冮檺缁熻淇℃伅
     *
     * @return 缁熻淇℃伅
     */
    @Transactional(readOnly = true)
    List<AreaPersonEntity> getAreaPermissionStatistics();

    /**
     * 鏍规嵁浜哄憳ID鍒楄〃鏌ヨ鏉冮檺
     *
     * @param personIds 浜哄憳ID鍒楄〃
     * @return 鏉冮檺鍒楄〃
     */
    @Transactional(readOnly = true)
    List<AreaPersonEntity> selectByPersonIds(@Param("personIds") List<Long> personIds);

    /**
     * 鎵归噺妫€鏌ュ尯鍩熸潈闄?     *
     * @param personAreaMap 浜哄憳ID鍒板尯鍩烮D鍒楄〃鐨勬槧灏?     * @return 妫€鏌ョ粨鏋?     */
    @Transactional(readOnly = true)
    List<AreaPersonEntity> batchCheckAreaPermission(@Param("personAreaMap") Map<Long, List<Long>> personAreaMap);

    /**
     * 妫€鏌ユ槸鍚﹀叿鏈夊尯鍩熸潈闄?     *
     * @param personId 浜哄憳ID
     * @param areaId   鍖哄煙ID
     * @return 鏄惁鍏锋湁鏉冮檺
     */
    @Transactional(readOnly = true)
    boolean hasAreaPermission(@Param("personId") Long personId, @Param("areaId") Long areaId);

    /**
     * 妫€鏌ユ槸鍚﹀叿鏈夊尯鍩熻矾寰勬潈闄?     *
     * @param personId 浜哄憳ID
     * @param areaPath 鍖哄煙璺緞
     * @return 鏄惁鍏锋湁鏉冮檺
     */
    @Transactional(readOnly = true)
    boolean hasAreaPathPermission(@Param("personId") Long personId, @Param("areaPath") String areaPath);

    /**
     * 鏍规嵁鏁版嵁鑼冨洿鑾峰彇鍖哄煙ID鍒楄〃
     *
     * @param personId  浜哄憳ID
     * @param dataScope 鏁版嵁鑼冨洿
     * @return 鍖哄煙ID鍒楄〃
     */
    @Transactional(readOnly = true)
    List<Long> getAreaIdsByDataScope(@Param("personId") Long personId, @Param("dataScope") String dataScope);

    /**
     * 鎵归噺鎻掑叆鍖哄煙鏉冮檺
     *
     * @param areaPersonList 鍖哄煙鏉冮檺鍒楄〃
     * @return 鎻掑叆鏁伴噺
     */
    @Transactional(rollbackFor = Exception.class)
    int batchInsert(@Param("areaPersonList") List<AreaPersonEntity> areaPersonList);

    /**
     * 鏍规嵁鍖哄煙ID鎵归噺鍒犻櫎鏉冮檺
     *
     * @param areaIds    鍖哄煙ID鍒楄〃
     * @param operatorId 鎿嶄綔浜篒D
     * @return 鍒犻櫎鏁伴噺
     */
    @Transactional(rollbackFor = Exception.class)
    int batchDeleteByAreaIds(@Param("areaIds") List<Long> areaIds, @Param("operatorId") Long operatorId);

    /**
     * 鏍规嵁鍖哄煙ID鎵归噺鏇存柊鐘舵€?     *
     * @param areaIds    鍖哄煙ID鍒楄〃
     * @param operatorId 鎿嶄綔浜篒D
     * @param status     鐘舵€?     * @return 鏇存柊鏁伴噺
     */
    @Transactional(rollbackFor = Exception.class)
    int batchUpdateStatusByAreaIds(@Param("areaIds") List<Long> areaIds, @Param("operatorId") Long operatorId,
            @Param("status") Integer status);

    /**
     * 鑾峰彇鎵€鏈夋巿鏉冪殑鍖哄煙ID鍒楄〃
     *
     * @param personId 浜哄憳ID
     * @return 鍖哄煙ID鍒楄〃
     */
    @Transactional(readOnly = true)
    List<Long> getAllAuthorizedAreaIds(@Param("personId") Long personId);

    /**
     * 鑾峰彇鐩存帴鎺堟潈鐨勫尯鍩烮D鍒楄〃
     *
     * @param personId 浜哄憳ID
     * @return 鍖哄煙ID鍒楄〃
     */
    @Transactional(readOnly = true)
    List<Long> getDirectAuthorizedAreaIds(@Param("personId") Long personId);

    /**
     * 鏍规嵁璺緞鍓嶇紑鑾峰彇鍖哄煙ID鍒楄〃
     *
     * @param personId   浜哄憳ID
     * @param pathPrefix 璺緞鍓嶇紑
     * @return 鍖哄煙ID鍒楄〃
     */
    @Transactional(readOnly = true)
    List<Long> getAreaIdsByPathPrefix(@Param("personId") Long personId, @Param("pathPrefix") String pathPrefix);

    /**
     * 鑾峰彇浜哄憳鐨勫尯鍩熻矾寰勫垪琛?     *
     * @param personId 浜哄憳ID
     * @return 鍖哄煙璺緞鍒楄〃
     */
    @Transactional(readOnly = true)
    List<String> getAreaPathsByPersonId(@Param("personId") Long personId);
}

