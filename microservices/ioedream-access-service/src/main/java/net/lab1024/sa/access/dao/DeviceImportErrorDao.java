package net.lab1024.sa.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.access.DeviceImportErrorEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 设备导入错误明细DAO接口
 * <p>
 * 提供导入错误记录的数据访问操作：
 * - 基础CRUD（继承BaseMapper）
 * - 批次错误查询
 * - 错误码统计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface DeviceImportErrorDao extends BaseMapper<DeviceImportErrorEntity> {

    /**
     * 根据批次ID查询所有错误记录
     *
     * @param batchId 批次ID
     * @return 错误记录列表
     */
    List<DeviceImportErrorEntity> selectByBatchId(@Param("batchId") Long batchId);

    /**
     * 根据批次ID统计错误数量
     *
     * @param batchId 批次ID
     * @return 错误数量
     */
    Integer countByBatchId(@Param("batchId") Long batchId);

    /**
     * 根据错误码统计错误数量
     *
     * @param errorCode 错误码
     * @return 错误数量
     */
    Integer countByErrorCode(@Param("errorCode") String errorCode);

    /**
     * 查询指定批次的指定行号错误
     *
     * @param batchId   批次ID
     * @param rowNumber 行号
     * @return 错误记录
     */
    DeviceImportErrorEntity selectByBatchIdAndRowNumber(@Param("batchId") Long batchId,
                                                         @Param("rowNumber") Integer rowNumber);

    /**
     * 批量插入错误记录
     *
     * @param errorList 错误记录列表
     * @return 插入数量
     */
    Integer insertBatch(@Param("errorList") List<DeviceImportErrorEntity> errorList);

    /**
     * 根据批次ID删除所有错误记录
     *
     * @param batchId 批次ID
     * @return 删除数量
     */
    Integer deleteByBatchId(@Param("batchId") Long batchId);
}
