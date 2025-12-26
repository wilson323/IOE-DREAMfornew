package net.lab1024.sa.access.service;

import com.alibaba.fastjson2.JSONObject;
import net.lab1024.sa.access.domain.form.DeviceImportQueryForm;
import net.lab1024.sa.access.domain.vo.DeviceImportBatchVO;
import net.lab1024.sa.access.domain.vo.DeviceImportErrorVO;
import net.lab1024.sa.access.domain.vo.DeviceImportResultVO;
import net.lab1024.sa.access.domain.vo.DeviceImportStatisticsVO;
import net.lab1024.sa.common.domain.PageResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 设备导入服务接口
 * <p>
 * 提供设备批量导入功能：
 * - Excel文件上传和解析
 * - 数据验证
 * - 批量导入
 * - 导入记录查询
 * - 统计分析
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface DeviceImportService {

    /**
     * 上传并解析Excel文件
     *
     * @param file        Excel文件
     * @param batchName   批次名称
     * @param operatorId  操作人ID
     * @param operatorName 操作人姓名
     * @return 导入批次ID
     */
    Long uploadAndParse(MultipartFile file, String batchName, Long operatorId, String operatorName);

    /**
     * 执行导入操作
     *
     * @param batchId 批次ID
     * @return 导入结果
     */
    DeviceImportResultVO executeImport(Long batchId);

    /**
     * 分页查询导入批次
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    PageResult<DeviceImportBatchVO> queryImportBatches(DeviceImportQueryForm queryForm);

    /**
     * 查询导入批次详情
     *
     * @param batchId 批次ID
     * @return 批次详情
     */
    DeviceImportBatchVO getImportBatchDetail(Long batchId);

    /**
     * 查询批次的错误列表
     *
     * @param batchId 批次ID
     * @return 错误列表
     */
    List<DeviceImportErrorVO> queryBatchErrors(Long batchId);

    /**
     * 获取导入统计信息
     *
     * @return 统计信息
     */
    DeviceImportStatisticsVO getImportStatistics();

    /**
     * 删除导入批次（级联删除成功和错误记录）
     *
     * @param batchId 批次ID
     * @return 删除数量
     */
    Integer deleteImportBatch(Long batchId);

    /**
     * 验证设备数据
     *
     * @param rowData 行数据（JSON格式）
     * @param rowNumber 行号
     * @return 验证结果（成功返回null，失败返回错误消息）
     */
    String validateDeviceData(JSONObject rowData, Integer rowNumber);

    /**
     * 下载导入模板
     *
     * @return Excel模板文件
     */
    byte[] downloadTemplate();

    /**
     * 导出错误记录
     *
     * @param batchId 批次ID
     * @return Excel文件（包含错误记录）
     */
    byte[] exportErrors(Long batchId);
}
