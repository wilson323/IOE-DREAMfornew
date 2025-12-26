package net.lab1024.sa.common.organization.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 区域数据访问接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
@Mapper
public interface AreaDao extends BaseMapper<AreaEntity> {

    /**
     * 根据区域编码查询区域
     *
     * @param areaCode 区域编码
     * @return 区域实体
     */
    AreaEntity selectByAreaCode(@Param("areaCode") String areaCode);

    /**
     * 根据区域编码查询区域（别名方法，兼容selectByCode调用）
     *
     * @param areaCode 区域编码
     * @return 区域实体
     */
    default AreaEntity selectByCode(@Param("areaCode") String areaCode) {
        return selectByAreaCode(areaCode);
    }

    /**
     * 根据父区域ID查询子区域列表
     *
     * @param parentId 父区域ID
     * @return 子区域列表
     */
    List<AreaEntity> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 根据父区域ID查询子区域列表（别名方法，兼容selectChildAreas调用）
     *
     * @param parentId 父区域ID
     * @return 子区域列表
     */
    default List<AreaEntity> selectChildAreas(@Param("parentId") Long parentId) {
        return selectByParentId(parentId);
    }

    /**
     * 根据区域类型查询区域列表
     *
     * @param areaType 区域类型
     * @return 区域列表
     */
    List<AreaEntity> selectByAreaType(@Param("areaType") String areaType);

    /**
     * 根据业务类型查询区域列表
     *
     * @param businessType 业务类型
     * @return 区域列表
     */
    List<AreaEntity> selectAreasByBusinessType(@Param("businessType") String businessType);

    /**
     * 根据层级查询区域列表
     *
     * @param level 层级
     * @return 区域列表
     */
    List<AreaEntity> selectByLevel(@Param("level") Integer level);

    /**
     * 获取用户可访问的区域ID列表
     *
     * @param userId 用户ID
     * @return 可访问的区域ID列表
     */
    List<Long> selectAccessibleAreaIds(@Param("userId") Long userId);

    /**
     * 检查用户是否有区域的直接访问权限
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @return 是否有权限
     */
    boolean hasDirectAccess(@Param("userId") Long userId, @Param("areaId") Long areaId);

    /**
     * 获取区域业务属性
     *
     * @param areaId 区域ID
     * @param businessModule 业务模块
     * @return 业务属性JSON字符串
     */
    String selectBusinessAttributes(@Param("areaId") Long areaId, @Param("businessModule") String businessModule);

    /**
     * 更新区域业务属性
     *
     * @param areaId 区域ID
     * @param businessModule 业务模块
     * @param attributesJson 业务属性JSON字符串
     * @return 更新行数
     */
    int updateBusinessAttributes(@Param("areaId") Long areaId, @Param("businessModule") String businessModule, @Param("attributesJson") String attributesJson);

    /**
     * 获取区域关联的设备列表
     *
     * @param areaId 区域ID
     * @param businessModule 业务模块（可选）
     * @return 设备ID列表
     */
    List<Long> selectAreaDevices(@Param("areaId") Long areaId, @Param("businessModule") String businessModule);

    /**
     * 统计子区域数量
     *
     * @param areaId 区域ID
     * @return 子区域数量
     */
    int countChildAreas(@Param("areaId") Long areaId);

    /**
     * 统计门禁设备数量
     *
     * @param areaId 区域ID
     * @return 设备数量
     */
    int countAccessDevices(@Param("areaId") Long areaId);

    /**
     * 统计门禁记录数量
     *
     * @param areaId 区域ID
     * @return 记录数量
     */
    int countAccessRecords(@Param("areaId") Long areaId);

    /**
     * 统计活跃的门禁权限数量
     *
     * @param areaId 区域ID
     * @return 权限数量
     */
    int countActiveAccessPermissions(@Param("areaId") Long areaId);

    /**
     * 统计消费设备数量
     *
     * @param areaId 区域ID
     * @return 设备数量
     */
    int countConsumeDevices(@Param("areaId") Long areaId);

    /**
     * 统计消费金额总和
     *
     * @param areaId 区域ID
     * @return 消费金额总和
     */
    java.math.BigDecimal sumConsumeAmount(@Param("areaId") Long areaId);

    /**
     * 统计消费记录数量
     *
     * @param areaId 区域ID
     * @return 记录数量
     */
    int countConsumeRecords(@Param("areaId") Long areaId);

    /**
     * 统计考勤设备数量
     *
     * @param areaId 区域ID
     * @return 设备数量
     */
    int countAttendanceDevices(@Param("areaId") Long areaId);

    /**
     * 统计考勤记录数量
     *
     * @param areaId 区域ID
     * @return 记录数量
     */
    int countAttendanceRecords(@Param("areaId") Long areaId);

    /**
     * 统计区域用户数量
     *
     * @param areaId 区域ID
     * @return 用户数量
     */
    int countAreaUsers(@Param("areaId") Long areaId);

    /**
     * 统计访客记录数量
     *
     * @param areaId 区域ID
     * @return 记录数量
     */
    int countVisitorRecords(@Param("areaId") Long areaId);

    /**
     * 统计访客预约数量
     *
     * @param areaId 区域ID
     * @return 预约数量
     */
    int countVisitorAppointments(@Param("areaId") Long areaId);

    /**
     * 统计视频设备数量
     *
     * @param areaId 区域ID
     * @return 设备数量
     */
    int countVideoDevices(@Param("areaId") Long areaId);

    /**
     * 获取视频录像大小
     *
     * @param areaId 区域ID
     * @return 录像大小（字节）
     */
    Long getVideoRecordSize(@Param("areaId") Long areaId);
}

