package net.lab1024.sa.admin.module.consume.service.validator;

import lombok.Data;

import java.util.List;

/**
 * 消费权限批量验证结果
 *
 * @author OpenSpec Task 1.2 Implementation
 * @version 1.0
 * @since 2025-11-17
 */
@Data
public class ConsumePermissionBatchResult {

    /**
     * 是否全部通过验证
     */
    private boolean allPassed;

    /**
     * 验证结果列表
     */
    private List<ConsumePermissionResult> results;

    /**
     * 通过验证的数量
     */
    private int passedCount;

    /**
     * 失败验证的数量
     */
    private int failedCount;

    /**
     * 创建成功的批量验证结果
     */
    public static ConsumePermissionBatchResult success(List<ConsumePermissionResult> results) {
        ConsumePermissionBatchResult batchResult = new ConsumePermissionBatchResult();
        batchResult.setResults(results);

        int passedCount = 0;
        int failedCount = 0;

        for (ConsumePermissionResult result : results) {
            if (result.isSuccess()) {
                passedCount++;
            } else {
                failedCount++;
            }
        }

        batchResult.setPassedCount(passedCount);
        batchResult.setFailedCount(failedCount);
        batchResult.setAllPassed(failedCount == 0);

        return batchResult;
    }

    /**
     * 创建失败的批量验证结果
     */
    public static ConsumePermissionBatchResult fail(List<ConsumePermissionResult> results) {
        ConsumePermissionBatchResult batchResult = new ConsumePermissionBatchResult();
        batchResult.setResults(results);
        batchResult.setAllPassed(false);
        batchResult.setPassedCount(0);
        batchResult.setFailedCount(results.size());

        return batchResult;
    }

    /**
     * 获取第一个失败的结果（如果有）
     */
    public ConsumePermissionResult getFirstFailure() {
        if (results != null) {
            for (ConsumePermissionResult result : results) {
                if (!result.isSuccess()) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * 获取所有失败的结果
     */
    public List<ConsumePermissionResult> getFailures() {
        if (results == null) {
            return List.of();
        }

        return results.stream()
                .filter(result -> !result.isSuccess())
                .collect(java.util.stream.Collectors.toList());
    }
}