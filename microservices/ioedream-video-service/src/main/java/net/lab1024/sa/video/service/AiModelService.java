package net.lab1024.sa.video.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import net.lab1024.sa.common.entity.video.AiModelEntity;
import net.lab1024.sa.video.domain.form.AiModelQueryForm;
import net.lab1024.sa.video.domain.form.AiModelSyncForm;
import net.lab1024.sa.video.domain.form.AiModelUploadForm;
import net.lab1024.sa.video.domain.vo.AiModelVO;
import net.lab1024.sa.video.domain.vo.DeviceModelSyncProgressVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * AI模型服务接口
 * <p>
 * 定义AI模型管理的业务接口：
 * 1. 模型上传和发布
 * 2. 模型查询和删除
 * 3. 设备同步管理
 * 4. 文件存储管理
 * </p>
 *
 * @author IOE-DREAM AI Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AiModelService {

    /**
     * 上传AI模型
     *
     * @param file     模型文件
     * @param uploadForm 上传表单
     * @return 模型ID
     */
    Long uploadModel(MultipartFile file, AiModelUploadForm uploadForm);

    /**
     * 发布AI模型
     *
     * @param modelId 模型ID
     * @param userId  发布人ID
     */
    void publishModel(Long modelId, Long userId);

    /**
     * 同步模型到设备
     *
     * @param syncForm 同步表单
     * @return 同步任务ID
     */
    String syncModelToDevices(AiModelSyncForm syncForm);

    /**
     * 查询模型列表（分页）
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    IPage<AiModelEntity> queryModelList(AiModelQueryForm queryForm);

    /**
     * 获取模型详情
     *
     * @param modelId 模型ID
     * @return 模型详情
     */
    AiModelVO getModelDetail(Long modelId);

    /**
     * 查询同步进度
     *
     * @param modelId 模型ID
     * @return 同步进度
     */
    DeviceModelSyncProgressVO querySyncProgress(Long modelId);

    /**
     * 删除模型
     *
     * @param modelId 模型ID
     */
    void deleteModel(Long modelId);
}
