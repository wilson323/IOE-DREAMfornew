package net.lab1024.sa.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.access.domain.entity.DeviceImportSuccessEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 设备导入成功记录DAO接口
 * <p>
 * 提供导入成功记录的数据访问操作：
 * - 基础CRUD（继承BaseMapper）
 * - 批次成功记录查询
 * - 设备导入历史追溯
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface DeviceImportSuccessDao extends BaseMapper<DeviceImportSuccessEntity> {

    /**
     * 根据批次ID查询所有成功记录
     *
     * @param batchId 批次ID
     * @return 成功记录列表
     */
    List<DeviceImportSuccessEntity> selectByBatchId(@Param("batchId") Long batchId);

    /**
     * 根据批次ID统计成功数量
     *
     * @param batchId 批次ID
     * @return 成功数量
     */
    Integer countByBatchId(@Param("batchId") Long batchId);

    /**
     * 根据设备ID查询导入记录
     *
     * @param deviceId 设备ID
     * @return 导入记录列表
     */
    List<DeviceImportSuccessEntity> selectByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 根据设备编码查询导入记录
     *
     * @param deviceCode 设备编码
     * @return 导入记录列表
     */
    List<DeviceImportSuccessEntity> selectByDeviceCode(@Param("deviceCode") String deviceCode);

    /**
     * 查询指定批次的指定行号成功记录
     *
     * @param batchId   批次ID
     * @param rowNumber 行号
     * @return 成功记录
     */
    DeviceImportSuccessEntity selectByBatchIdAndRowNumber(@Param("batchId") Long batchId,
                                                           @Param("rowNumber") Integer rowNumber);

    /**
     * 批量插入成功记录
     *
     * @param successList 成功记录列表
     * @return 插入数量
     */
    Integer insertBatch(@Param("successList") List<DeviceImportSuccessEntity> successList);

    /**
     * 根据批次ID删除所有成功记录
     *
     * @param batchId 批次ID
     * @return 删除数量
     */
    Integer deleteByBatchId(@Param("batchId") Long batchId);

    /**
     * 检查设备编码是否在指定批次中已存在
     *
     * @param batchId    批次ID
     * @param deviceCode 设备编码
     * @return 存在数量
     */
    Integer countByBatchIdAndDeviceCode(@Param("batchId") Long batchId,
                                         @Param("deviceCode") String deviceCode);
}
