package net.lab1024.sa.access.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.organization.entity.DeviceEntity;

/**
 * 闂ㄧ璁惧DAO
 * <p>
 * 涓ユ牸閬靛惊DAO鏋舵瀯瑙勮寖锛? * - 缁熶竴DAO妯″紡锛屼娇鐢―ao鍛藉悕
 * - 浣跨敤@Mapper娉ㄨВ锛岀姝娇鐢ˊMapper
 * - 鏌ヨ鏂规硶浣跨敤@Transactional(readOnly = true)
 * - 缁ф壙BaseMapper浣跨敤MyBatis-Plus
 * - 鑱岃矗鍗曚竴锛氬彧璐熻矗璁惧鏁版嵁璁块棶
 * - 鎻愪緵缂撳瓨灞傞渶瑕佺殑鏌ヨ鏂规硶
 * - 浣跨敤鍏叡DeviceEntity鏇夸唬AccessDeviceEntity
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 * @updated 2025-12-02 浣跨敤鍏叡DeviceEntity锛岄伒寰猺epowiki瑙勮寖
 */
@Mapper
public interface AccessDeviceDao extends BaseMapper<DeviceEntity> {

    /**
     * 鏌ヨ鎸囧畾鍖哄煙鐨勮澶囧垪琛?     * 浣跨敤鍏叡璁惧琛細t_common_device
     *
     * @param areaId 鍖哄煙ID
     * @return 璁惧鍒楄〃
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_common_device WHERE area_id = #{areaId} AND deleted_flag = 0 ORDER BY create_time ASC")
    List<DeviceEntity> selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 鏌ヨ鍦ㄧ嚎璁惧鍒楄〃
     *
     * @return 鍦ㄧ嚎璁惧鍒楄〃
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_common_device WHERE device_status = 'ONLINE' AND enabled_flag = 1 AND deleted_flag = 0 ORDER BY last_online_time DESC")
    List<DeviceEntity> selectOnlineDevices();

    /**
     * 鏌ヨ绂荤嚎璁惧鍒楄〃
     *
     * @return 绂荤嚎璁惧鍒楄〃
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_common_device WHERE device_status = 'OFFLINE' AND enabled_flag = 1 AND deleted_flag = 0 ORDER BY last_online_time ASC")
    List<DeviceEntity> selectOfflineDevices();

    /**
     * 鏌ヨ闂ㄧ绫诲瀷璁惧鍒楄〃
     *
     * @return 闂ㄧ璁惧鍒楄〃
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_common_device WHERE device_type = 'ACCESS' AND deleted_flag = 0 ORDER BY create_time ASC")
    List<DeviceEntity> selectAccessDevices();

    /**
     * 鏌ヨ闇€瑕佺淮鎶ょ殑璁惧鍒楄〃
     *
     * @return 闇€瑕佺淮鎶ょ殑璁惧鍒楄〃
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_common_device WHERE device_status = 'MAINTAIN' AND enabled_flag = 1 AND deleted_flag = 0 ORDER BY update_time ASC")
    List<DeviceEntity> selectDevicesNeedingMaintenance();

    /**
     * 鏌ヨ蹇冭烦瓒呮椂鐨勮澶囧垪琛?     *
     * @return 蹇冭烦瓒呮椂鐨勮澶囧垪琛?     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_common_device WHERE " +
            "last_online_time < DATE_SUB(NOW(), INTERVAL 10 MINUTE) " +
            "AND enabled_flag = 1 AND deleted_flag = 0 ORDER BY last_online_time ASC")
    List<DeviceEntity> selectHeartbeatTimeoutDevices();

    /**
     * 鎵归噺鏇存柊璁惧鍦ㄧ嚎鐘舵€?     *
     * @param deviceIds 璁惧ID鍒楄〃
     * @param status    鐘舵€?     * @return 鏇存柊琛屾暟
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("<script>" +
            "UPDATE t_common_device SET device_status = #{status}, update_time = NOW() " +
            "WHERE device_id IN " +
            "<foreach collection='deviceIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateOnlineStatus(@Param("deviceIds") List<Long> deviceIds, @Param("status") String status);

    /**
     * 缁熻璁惧鏁伴噺
     *
     * @return 璁惧鎬绘暟
     */
    @Transactional(readOnly = true)
    @Select("SELECT COUNT(*) FROM t_common_device WHERE deleted_flag = 0")
    Long countTotalDevices();

    /**
     * 缁熻鍦ㄧ嚎璁惧鏁伴噺
     *
     * @return 鍦ㄧ嚎璁惧鏁伴噺
     */
    @Transactional(readOnly = true)
    @Select("SELECT COUNT(*) FROM t_common_device WHERE device_status = 'ONLINE' AND deleted_flag = 0")
    Long countOnlineDevices();

    /**
     * 缁熻鎸囧畾鍖哄煙鐨勮澶囨暟閲?     *
     * @param areaId 鍖哄煙ID
     * @return 璁惧鏁伴噺
     */
    @Transactional(readOnly = true)
    @Select("SELECT COUNT(*) FROM t_common_device WHERE area_id = #{areaId} AND deleted_flag = 0")
    Long countDevicesByArea(@Param("areaId") Long areaId);

    /**
     * 鏍规嵁璁惧绫诲瀷缁熻璁惧鏁伴噺
     *
     * @return 缁熻缁撴灉
     */
    @Transactional(readOnly = true)
    @Select("SELECT device_type, COUNT(*) as count " +
            "FROM t_common_device WHERE deleted_flag = 0 " +
            "GROUP BY device_type ORDER BY count DESC")
    List<Object> countDevicesByType();

    /**
     * 鎵归噺鏌ヨ澶氫釜鍖哄煙鐨勮澶囧垪琛紙鎬ц兘浼樺寲锛氫娇鐢↖N鏌ヨ閬垮厤N+1闂锛?     *
     * @param areaIds 鍖哄煙ID鍒楄〃
     * @return 璁惧鍒楄〃
     */
    @Transactional(readOnly = true)
    @Select("<script>" +
            "SELECT * FROM t_common_device WHERE deleted_flag = 0 " +
            "AND area_id IN " +
            "<foreach collection='areaIds' item='areaId' open='(' separator=',' close=')'>" +
            "#{areaId}" +
            "</foreach>" +
            " ORDER BY create_time ASC" +
            "</script>")
    List<DeviceEntity> selectByAreaIds(@Param("areaIds") List<Long> areaIds);

    /**
     * 鏍规嵁璁惧鐘舵€佺粺璁¤澶囨暟閲?     *
     * @return 缁熻缁撴灉
     */
    @Transactional(readOnly = true)
    @Select("SELECT device_status, COUNT(*) as count " +
            "FROM t_common_device WHERE deleted_flag = 0 " +
            "GROUP BY device_status ORDER BY count DESC")
    List<Object> countDevicesByStatus();
}

