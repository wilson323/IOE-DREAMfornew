package net.lab1024.sa.access.service;

import com.alibaba.fastjson2.JSONObject;
import net.lab1024.sa.access.domain.form.AccessPermissionImportQueryForm;
import net.lab1024.sa.access.domain.vo.AccessPermissionImportBatchVO;
import net.lab1024.sa.access.domain.vo.AccessPermissionImportErrorVO;
import net.lab1024.sa.access.domain.vo.AccessPermissionImportResultVO;
import net.lab1024.sa.access.domain.vo.AccessPermissionImportStatisticsVO;
import net.lab1024.sa.common.domain.PageResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 门禁权限批量导入服务接口
 * <p>
 * 提供门禁权限批量导入功能：
 * - Excel文件上传和解析
 * - 数据验证（用户ID、区域ID、时间段等）
 * - 批量导入（支持事务回滚）
 * - 异步导入（大数据量）
 * - 导入记录查询
 * - 统计分析
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
public interface AccessPermissionImportService {

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
    AccessPermissionImportResultVO executeImport(Long batchId);

    /**
     * 异步执行导入操作（大数据量）
     *
     * @param batchId 批次ID
     * @return 异步任务ID
     */
    String executeImportAsync(Long batchId);

    /**
     * 分页查询导入批次
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    PageResult<AccessPermissionImportBatchVO> queryImportBatches(AccessPermissionImportQueryForm queryForm);

    /**
     * 查询导入批次详情
     *
     * @param batchId 批次ID
     * @return 批次详情
     */
    AccessPermissionImportBatchVO getImportBatchDetail(Long batchId);

    /**
     * 查询批次的错误列表
     *
     * @param batchId 批次ID
     * @return 错误列表
     */
    List<AccessPermissionImportErrorVO> queryBatchErrors(Long batchId);

    /**
     * 获取导入统计信息
     *
     * @return 统计信息
     */
    AccessPermissionImportStatisticsVO getImportStatistics();

    /**
     * 删除导入批次（级联删除成功和错误记录）
     *
     * @param batchId 批次ID
     * @return 删除数量
     */
    Integer deleteImportBatch(Long batchId);

    /**
     * 验证权限数据
     *
     * @param rowData 行数据（JSON格式）
     * @param rowNumber 行号
     * @return 验证结果（成功返回null，失败返回错误消息）
     */
    String validatePermissionData(JSONObject rowData, Integer rowNumber);

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

    /**
     * 取消导入任务
     *
     * @param batchId 批次ID
     * @return 取消结果
     */
    Boolean cancelImport(Long batchId);

    /**
     * 查询异步任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态
     */
    JSONObject getTaskStatus(String taskId);
}
