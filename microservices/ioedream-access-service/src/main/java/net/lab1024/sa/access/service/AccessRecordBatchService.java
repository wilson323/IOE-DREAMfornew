package net.lab1024.sa.access.service;

import net.lab1024.sa.access.domain.dto.AccessRecordBatchUploadRequest;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 门禁记录批量上传服务接口
 * <p>
 * 负责批量上传通行记录的处理，包括：
 * 1. 批量插入数据库
 * 2. 幂等性检查（防止重复上传）
 * 3. 异常处理和重试机制
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AccessRecordBatchService {

    /**
     * 批量上传通行记录
     * <p>
     * 接收设备批量上传的通行记录，进行批量插入和幂等性检查
     * </p>
     *
     * @param request 批量上传请求
     * @return 处理结果
     */
    ResponseDTO<AccessRecordBatchUploadRequest.BatchUploadResult> batchUploadRecords(
            AccessRecordBatchUploadRequest request);

    /**
     * 查询批量上传状态
     * <p>
     * 查询指定批次的上传状态
     * </p>
     *
     * @param batchId 批次ID
     * @return 上传状态
     */
    ResponseDTO<AccessRecordBatchUploadRequest.BatchUploadResult> getUploadStatus(String batchId);
}
