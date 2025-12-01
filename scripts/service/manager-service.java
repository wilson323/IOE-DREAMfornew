package {{package}};

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.exception.SmartException;
import net.lab1024.sa.base.common.code.SystemErrorCode;
import net.lab1024.sa.base.common.exception.BusinessException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.Resource;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {{EntityName}}管理服务类
 * <p>
 * Manager层负责复杂业务逻辑封装、跨模块调用、业务流程编排
 * 严格遵循repowiki四层架构规范，不直接访问数据库
 * </p>
 *
 * @author {{author}}
 * @since {{date}}
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class {{EntityName}}Manager {

    @Resource
    private {{EntityName}}Service {{entityName}}Service;

    @Resource
    private {{EntityName}}CacheService {{entityName}}CacheService;

    @Resource
    private ObjectMapper objectMapper;

    // 线程池用于异步处理
    private final Executor asyncExecutor = Executors.newFixedThreadPool(5);

    // ==================== 复杂业务逻辑处理 ====================

    /**
     * 创建{{EntityName}}并处理相关业务逻辑
     * 包含完整的业务验证、数据处理、关联操作等
     *
     * @param addForm 添加表单
     * @return 创建结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Long> create{{EntityName}}WithBusiness({{EntityName}}AddForm addForm) {
        try {
            log.info("开始创建{{EntityName}}，业务键: {}", addForm.getBusinessKey());

            // 1. 业务前置验证
            ResponseDTO<Void> validationResult = validateBusinessRulesForCreate(addForm);
            if (!validationResult.getOk()) {
                return ResponseDTO.error(validationResult.getMsg());
            }

            // 2. 业务数据预处理
            {{EntityName}}AddForm processedForm = preprocessBusinessData(addForm);
            if (processedForm == null) {
                return ResponseDTO.error("业务数据预处理失败");
            }

            // 3. 执行创建操作
            ResponseDTO<Long> createResult = {{entityName}}Service.add(processedForm);
            if (!createResult.getOk() || createResult.getData() == null) {
                return createResult;
            }

            Long createdId = createResult.getData();
            log.info("{{EntityName}}创建成功，ID: {}", createdId);

            // 4. 后续业务处理
            try {
                processPostCreationActions(createdId, processedForm);
            } catch (Exception e) {
                log.error("创建{{EntityName}}后处理失败，ID: {}", createdId, e);
                // 不影响主流程，记录错误即可
            }

            // 5. 清理相关缓存
            clearRelatedCache(createdId);

            log.info("{{EntityName}}创建及业务处理完成，ID: {}", createdId);
            return ResponseDTO.ok(createdId);

        } catch (BusinessException e) {
            log.error("创建{{EntityName}}业务异常: {}", e.getMessage(), e);
            return ResponseDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("创建{{EntityName}}失败", e);
            return ResponseDTO.error("创建失败，请稍后重试");
        }
    }

    /**
     * 更新{{EntityName}}并处理相关业务逻辑
     *
     * @param updateForm 更新表单
     * @return 更新结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> update{{EntityName}}WithBusiness({{EntityName}}UpdateForm updateForm) {
        try {
            log.info("开始更新{{EntityName}}，ID: {}", updateForm.get{{EntityName}}Id());

            // 1. 业务前置验证
            ResponseDTO<Void> validationResult = validateBusinessRulesForUpdate(updateForm);
            if (!validationResult.getOk()) {
                return ResponseDTO.error(validationResult.getMsg());
            }

            // 2. 获取当前数据用于对比
            ResponseDTO<{{EntityName}}VO> currentDataResponse = {{entityName}}Service.getDetail(updateForm.get{{EntityName}}Id());
            if (!currentDataResponse.getOk() || currentDataResponse.getData() == null) {
                return ResponseDTO.error("找不到要更新的数据");
            }

            {{EntityName}}VO currentData = currentDataResponse.getData();

            // 3. 业务数据预处理
            {{EntityName}}UpdateForm processedForm = preprocessUpdateData(updateForm, currentData);
            if (processedForm == null) {
                return ResponseDTO.error("更新数据预处理失败");
            }

            // 4. 执行更新操作
            ResponseDTO<Boolean> updateResult = {{entityName}}Service.update(processedForm);
            if (!updateResult.getOk()) {
                return updateResult;
            }

            log.info("{{EntityName}}更新成功，ID: {}", updateForm.get{{EntityName}}Id());

            // 5. 后续业务处理
            try {
                processPostUpdateActions(updateForm.get{{EntityName}}Id(), currentData, processedForm);
            } catch (Exception e) {
                log.error("更新{{EntityName}}后处理失败，ID: {}", updateForm.get{{EntityName}}Id(), e);
            }

            // 6. 清理相关缓存
            clearRelatedCache(updateForm.get{{EntityName}}Id());

            log.info("{{EntityName}}更新及业务处理完成，ID: {}", updateForm.get{{EntityName}}Id());
            return ResponseDTO.ok(true);

        } catch (BusinessException e) {
            log.error("更新{{EntityName}}业务异常: {}", e.getMessage(), e);
            return ResponseDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新{{EntityName}}失败", e);
            return ResponseDTO.error("更新失败，请稍后重试");
        }
    }

    /**
     * 删除{{EntityName}}并处理相关业务逻辑
     *
     * @param id 主键ID
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> delete{{EntityName}}WithBusiness(Long id) {
        try {
            log.info("开始删除{{EntityName}}，ID: {}", id);

            // 1. 业务前置验证
            ResponseDTO<Void> validationResult = validateBusinessRulesForDelete(id);
            if (!validationResult.getOk()) {
                return ResponseDTO.error(validationResult.getMsg());
            }

            // 2. 获取当前数据用于后续处理
            ResponseDTO<{{EntityName}}VO> currentDataResponse = {{entityName}}Service.getDetail(id);
            if (!currentDataResponse.getOk() || currentDataResponse.getData() == null) {
                return ResponseDTO.error("找不到要删除的数据");
            }

            {{EntityName}}VO currentData = currentDataResponse.getData();

            // 3. 执行删除操作
            ResponseDTO<Boolean> deleteResult = {{entityName}}Service.delete(id);
            if (!deleteResult.getOk()) {
                return deleteResult;
            }

            log.info("{{EntityName}}删除成功，ID: {}", id);

            // 4. 后续业务处理
            try {
                processPostDeletionActions(id, currentData);
            } catch (Exception e) {
                log.error("删除{{EntityName}}后处理失败，ID: {}", id, e);
            }

            // 5. 清理相关缓存
            clearRelatedCache(id);

            log.info("{{EntityName}}删除及业务处理完成，ID: {}", id);
            return ResponseDTO.ok(true);

        } catch (BusinessException e) {
            log.error("删除{{EntityName}}业务异常: {}", e.getMessage(), e);
            return ResponseDTO.error(e.getMessage());
        } catch (Exception e) {
            log.error("删除{{EntityName}}失败", e);
            return ResponseDTO.error("删除失败，请稍后重试");
        }
    }

    // ==================== 批量业务处理 ====================

    /**
     * 批量创建{{EntityName}}
     *
     * @param addFormList 添加表单列表
     * @return 批量创建结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<List<Long>> batchCreate{{EntityName}}(List<{{EntityName}}AddForm> addFormList) {
        try {
            if (addFormList == null || addFormList.isEmpty()) {
                return ResponseDTO.error("批量创建数据不能为空");
            }

            log.info("开始批量创建{{EntityName}}，数量: {}", addFormList.size());

            List<Long> successIds = new ArrayList<>();
            List<String> failedReasons = new ArrayList<>();

            for (int i = 0; i < addFormList.size(); i++) {
                {{EntityName}}AddForm addForm = addFormList.get(i);
                try {
                    ResponseDTO<Long> result = create{{EntityName}}WithBusiness(addForm);
                    if (result.getOk() && result.getData() != null) {
                        successIds.add(result.getData());
                    } else {
                        failedReasons.add(String.format("第%d个创建失败: %s", i + 1, result.getMsg()));
                    }
                } catch (Exception e) {
                    log.error("批量创建{{EntityName}}单个失败，索引: {}", i, e);
                    failedReasons.add(String.format("第%d个创建异常: %s", i + 1, e.getMessage()));
                }
            }

            // 记录批量操作结果
            log.info("批量创建{{EntityName}}完成，成功: {}, 失败: {}", successIds.size(), failedReasons.size());

            if (failedReasons.isEmpty()) {
                return ResponseDTO.ok(successIds);
            } else {
                String errorMsg = String.join("; ", failedReasons);
                return ResponseDTO.userErrorParam("部分创建失败: " + errorMsg);
            }

        } catch (Exception e) {
            log.error("批量创建{{EntityName}}失败", e);
            return ResponseDTO.error("批量创建失败，请稍后重试");
        }
    }

    /**
     * 批量更新{{EntityName}}
     *
     * @param updateFormList 更新表单列表
     * @return 批量更新结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Map<String, Object>> batchUpdate{{EntityName}}(List<{{EntityName}}UpdateForm> updateFormList) {
        try {
            if (updateFormList == null || updateFormList.isEmpty()) {
                return ResponseDTO.error("批量更新数据不能为空");
            }

            log.info("开始批量更新{{EntityName}}，数量: {}", updateFormList.size());

            Map<String, Object> result = new HashMap<>();
            List<Long> successIds = new ArrayList<>();
            List<String> failedReasons = new ArrayList<>();

            for (int i = 0; i < updateFormList.size(); i++) {
                {{EntityName}}UpdateForm updateForm = updateFormList.get(i);
                try {
                    ResponseDTO<Boolean> updateResult = update{{EntityName}}WithBusiness(updateForm);
                    if (updateResult.getOk()) {
                        successIds.add(updateForm.get{{EntityName}}Id());
                    } else {
                        failedReasons.add(String.format("ID %d 更新失败: %s",
                                updateForm.get{{EntityName}}Id(), updateResult.getMsg()));
                    }
                } catch (Exception e) {
                    log.error("批量更新{{EntityName}}单个失败，ID: {}", updateForm.get{{EntityName}}Id(), e);
                    failedReasons.add(String.format("ID %d 更新异常: %s",
                            updateForm.get{{EntityName}}Id(), e.getMessage()));
                }
            }

            result.put("total", updateFormList.size());
            result.put("success", successIds.size());
            result.put("failed", failedReasons.size());
            result.put("successIds", successIds);
            result.put("failedReasons", failedReasons);

            log.info("批量更新{{EntityName}}完成，成功: {}, 失败: {}", successIds.size(), failedReasons.size());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("批量更新{{EntityName}}失败", e);
            return ResponseDTO.error("批量更新失败，请稍后重试");
        }
    }

    /**
     * 批量删除{{EntityName}}
     *
     * @param ids ID列表
     * @return 批量删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Map<String, Object>> batchDelete{{EntityName}}(List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return ResponseDTO.error("批量删除ID不能为空");
            }

            log.info("开始批量删除{{EntityName}}，数量: {}", ids.size());

            Map<String, Object> result = new HashMap<>();
            List<Long> successIds = new ArrayList<>();
            List<String> failedReasons = new ArrayList<>();

            for (Long id : ids) {
                try {
                    ResponseDTO<Boolean> deleteResult = delete{{EntityName}}WithBusiness(id);
                    if (deleteResult.getOk()) {
                        successIds.add(id);
                    } else {
                        failedReasons.add(String.format("ID %d 删除失败: %s", id, deleteResult.getMsg()));
                    }
                } catch (Exception e) {
                    log.error("批量删除{{EntityName}}单个失败，ID: {}", id, e);
                    failedReasons.add(String.format("ID %d 删除异常: %s", id, e.getMessage()));
                }
            }

            result.put("total", ids.size());
            result.put("success", successIds.size());
            result.put("failed", failedReasons.size());
            result.put("successIds", successIds);
            result.put("failedReasons", failedReasons);

            log.info("批量删除{{EntityName}}完成，成功: {}, 失败: {}", successIds.size(), failedReasons.size());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("批量删除{{EntityName}}失败", e);
            return ResponseDTO.error("批量删除失败，请稍后重试");
        }
    }

    // ==================== 业务流程编排 ====================

    /**
     * 复杂业务流程：{{EntityName}}状态流转
     *
     * @param id 主键ID
     * @param targetStatus 目标状态
     * @param remark 备注信息
     * @return 处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> processStatusTransition(Long id, String targetStatus, String remark) {
        try {
            log.info("开始处理{{EntityName}}状态流转，ID: {}, 目标状态: {}", id, targetStatus);

            // 1. 获取当前状态
            ResponseDTO<{{EntityName}}VO> currentDataResponse = {{entityName}}Service.getDetail(id);
            if (!currentDataResponse.getOk() || currentDataResponse.getData() == null) {
                return ResponseDTO.error("找不到{{EntityName}}数据");
            }

            {{EntityName}}VO currentData = currentDataResponse.getData();
            String currentStatus = currentData.getStatus();

            // 2. 验证状态流转的合法性
            if (!isValidStatusTransition(currentStatus, targetStatus)) {
                return ResponseDTO.error(String.format("非法状态流转: %s -> %s", currentStatus, targetStatus));
            }

            // 3. 执行状态流转前置处理
            ResponseDTO<Void> preProcessResult = processPreStatusTransition(id, currentStatus, targetStatus);
            if (!preProcessResult.getOk()) {
                return preProcessResult;
            }

            // 4. 更新状态
            ResponseDTO<Boolean> updateResult = update{{EntityName}}Status(id, targetStatus, remark);
            if (!updateResult.getOk()) {
                return updateResult;
            }

            // 5. 执行状态流转后置处理
            processPostStatusTransition(id, currentStatus, targetStatus, remark);

            log.info("{{EntityName}}状态流转完成，ID: {}, {} -> {}", id, currentStatus, targetStatus);
            return ResponseDTO.ok(true);

        } catch (Exception e) {
            log.error("处理{{EntityName}}状态流转失败，ID: {}", id, e);
            return ResponseDTO.error("状态流转失败，请稍后重试");
        }
    }

    /**
     * 复杂业务流程：{{EntityName}}业务审批
     *
     * @param id 主键ID
     * @param approveForm 审批表单
     * @return 审批结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> processBusinessApproval(Long id, {{EntityName}}ApprovalForm approveForm) {
        try {
            log.info("开始处理{{EntityName}}业务审批，ID: {}, 审批结果: {}", id, approveForm.getApprovalResult());

            // 1. 验证审批权限和数据状态
            ResponseDTO<Void> validationResult = validateApprovalPermission(id, approveForm);
            if (!validationResult.getOk()) {
                return validationResult;
            }

            // 2. 执行审批前置处理
            ResponseDTO<Void> preProcessResult = processPreApproval(id, approveForm);
            if (!preProcessResult.getOk()) {
                return preProcessResult;
            }

            // 3. 更新审批状态和相关信息
            ResponseDTO<Boolean> approvalResult = updateApprovalInfo(id, approveForm);
            if (!approvalResult.getOk()) {
                return approvalResult;
            }

            // 4. 执行审批后置处理
            processPostApproval(id, approveForm);

            // 5. 清理相关缓存
            clearRelatedCache(id);

            log.info("{{EntityName}}业务审批完成，ID: {}", id);
            return ResponseDTO.ok(true);

        } catch (Exception e) {
            log.error("处理{{EntityName}}业务审批失败，ID: {}", id, e);
            return ResponseDTO.error("业务审批失败，请稍后重试");
        }
    }

    // ==================== 异步业务处理 ====================

    /**
     * 异步创建{{EntityName}}
     *
     * @param addForm 添加表单
     * @return CompletableFuture
     */
    public CompletableFuture<ResponseDTO<Long>> create{{EntityName}}Async({{EntityName}}AddForm addForm) {
        return CompletableFuture.supplyAsync(() -> create{{EntityName}}WithBusiness(addForm), asyncExecutor);
    }

    /**
     * 异步批量处理
     *
     * @param addFormList 添加表单列表
     * @return CompletableFuture
     */
    public CompletableFuture<ResponseDTO<List<Long>>> batchCreate{{EntityName}}Async(List<{{EntityName}}AddForm> addFormList) {
        return CompletableFuture.supplyAsync(() -> batchCreate{{EntityName}}(addFormList), asyncExecutor);
    }

    /**
     * 异步状态流转
     *
     * @param id 主键ID
     * @param targetStatus 目标状态
     * @param remark 备注信息
     * @return CompletableFuture
     */
    public CompletableFuture<ResponseDTO<Boolean>> processStatusTransitionAsync(Long id, String targetStatus, String remark) {
        return CompletableFuture.supplyAsync(() -> processStatusTransition(id, targetStatus, remark), asyncExecutor);
    }

    // ==================== 私有业务验证方法 ====================

    /**
     * 验证创建业务规则
     */
    private ResponseDTO<Void> validateBusinessRulesForCreate({{EntityName}}AddForm addForm) {
        // 业务键唯一性验证
        if (addForm.getBusinessKey() != null && !addForm.getBusinessKey().trim().isEmpty()) {
            // 这里需要调用service层检查业务键是否已存在
            // 实际实现时需要添加相应的查询方法
        }

        // 其他业务规则验证...
        return ResponseDTO.ok();
    }

    /**
     * 验证更新业务规则
     */
    private ResponseDTO<Void> validateBusinessRulesForUpdate({{EntityName}}UpdateForm updateForm) {
        // 更新业务规则验证...
        return ResponseDTO.ok();
    }

    /**
     * 验证删除业务规则
     */
    private ResponseDTO<Void> validateBusinessRulesForDelete(Long id) {
        // 删除前置条件验证...
        return ResponseDTO.ok();
    }

    // ==================== 私有数据处理方法 ====================

    /**
     * 预处理业务数据
     */
    private {{EntityName}}AddForm preprocessBusinessData({{EntityName}}AddForm addForm) {
        try {
            // 创建表单副本进行预处理
            {{EntityName}}AddForm processedForm = new {{EntityName}}AddForm();
            // 使用ObjectMapper进行深拷贝
            String jsonStr = objectMapper.writeValueAsString(addForm);
            processedForm = objectMapper.readValue(jsonStr, {{EntityName}}AddForm.class);

            // 执行数据预处理逻辑...
            // 例如：格式化、默认值设置、数据转换等

            return processedForm;
        } catch (Exception e) {
            log.error("预处理业务数据失败", e);
            return null;
        }
    }

    /**
     * 预处理更新数据
     */
    private {{EntityName}}UpdateForm preprocessUpdateData({{EntityName}}UpdateForm updateForm, {{EntityName}}VO currentData) {
        try {
            // 创建更新表单副本进行预处理
            {{EntityName}}UpdateForm processedForm = new {{EntityName}}UpdateForm();
            String jsonStr = objectMapper.writeValueAsString(updateForm);
            processedForm = objectMapper.readValue(jsonStr, {{EntityName}}UpdateForm.class);

            // 执行更新数据预处理逻辑...

            return processedForm;
        } catch (Exception e) {
            log.error("预处理更新数据失败", e);
            return null;
        }
    }

    // ==================== 私有后处理方法 ====================

    /**
     * 处理创建后操作
     */
    private void processPostCreationActions(Long createdId, {{EntityName}}AddForm addForm) {
        // 创建后的业务处理逻辑...
        // 例如：发送通知、记录日志、触发其他流程等
    }

    /**
     * 处理更新后操作
     */
    private void processPostUpdateActions(Long id, {{EntityName}}VO currentData, {{EntityName}}UpdateForm updateForm) {
        // 更新后的业务处理逻辑...
    }

    /**
     * 处理删除后操作
     */
    private void processPostDeletionActions(Long id, {{EntityName}}VO deletedData) {
        // 删除后的业务处理逻辑...
    }

    // ==================== 私有工具方法 ====================

    /**
     * 验证状态流转合法性
     */
    private boolean isValidStatusTransition(String fromStatus, String toStatus) {
        // 实现具体的状态流转规则验证
        // 例如：某些状态之间不允许直接转换
        return true;
    }

    /**
     * 处理状态流转前置操作
     */
    private ResponseDTO<Void> processPreStatusTransition(Long id, String fromStatus, String toStatus) {
        // 状态流转前置处理逻辑...
        return ResponseDTO.ok();
    }

    /**
     * 处理状态流转后置操作
     */
    private void processPostStatusTransition(Long id, String fromStatus, String toStatus, String remark) {
        // 状态流转后置处理逻辑...
    }

    /**
     * 更新{{EntityName}}状态
     */
    private ResponseDTO<Boolean> update{{EntityName}}Status(Long id, String targetStatus, String remark) {
        // 实现状态更新的具体逻辑
        // 这里需要调用service层的更新方法
        return ResponseDTO.ok(true);
    }

    /**
     * 验证审批权限
     */
    private ResponseDTO<Void> validateApprovalPermission(Long id, {{EntityName}}ApprovalForm approveForm) {
        // 审批权限验证逻辑...
        return ResponseDTO.ok();
    }

    /**
     * 处理审批前置操作
     */
    private ResponseDTO<Void> processPreApproval(Long id, {{EntityName}}ApprovalForm approveForm) {
        // 审批前置处理逻辑...
        return ResponseDTO.ok();
    }

    /**
     * 更新审批信息
     */
    private ResponseDTO<Boolean> updateApprovalInfo(Long id, {{EntityName}}ApprovalForm approveForm) {
        // 更新审批信息的具体逻辑...
        return ResponseDTO.ok(true);
    }

    /**
     * 处理审批后置操作
     */
    private void processPostApproval(Long id, {{EntityName}}ApprovalForm approveForm) {
        // 审批后置处理逻辑...
    }

    /**
     * 清理相关缓存
     */
    private void clearRelatedCache(Long id) {
        try {
            if (id != null && id > 0) {
                {{entityName}}CacheService.deleteDetail(id);
                log.debug("清理{{EntityName}}相关缓存，ID: {}", id);
            }
        } catch (Exception e) {
            log.warn("清理{{EntityName}}缓存失败，ID: {}", id, e);
        }
    }
}