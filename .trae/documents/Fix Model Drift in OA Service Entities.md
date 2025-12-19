I have identified the compilation errors in `ioedream-oa-service` caused by "Model Drift" where the `ApprovalServiceImpl` and `WorkflowEngineServiceImpl` rely on fields and methods that are missing or misconfigured in `WorkflowTaskEntity` and `WorkflowInstanceEntity`.

I will apply the following fixes:

1.  **Update `WorkflowTaskEntity.java`**:
    *   **Remove Duplicate Field:** Remove the conflicting `private String result;` field (keeping `private Integer result;` which matches the usage).
    *   **Add Missing Fields:** Add `outcome` (Integer), `flowableTaskId` (String), `flowableProcessInstanceId` (String), `assigneeTime` (LocalDateTime), `originalAssigneeId` (Long), `originalAssigneeName` (String).
    *   **Persistence:** Remove `@TableField(exist = false)` from these fields so they can be persisted as expected by the Service layer.
    *   **Add Compatibility Methods:**
        *   `setCompleteTime(LocalDateTime time)` -> delegates to `setEndTime(time)`.
        *   Ensure `setVariables`, `setPriority` (Integer) are available.

2.  **Update `WorkflowInstanceEntity.java`**:
    *   **Add Missing Fields:** Add `flowableInstanceId` (String), `flowableProcessDefId` (String), `variables` (String, for JSON storage).
    *   **Persistence:** Remove `@TableField(exist = false)` from `currentApproverId`, `currentApproverName`, `priority`, `duration`, `reason` to allow persistence.
    *   **Add Compatibility Methods:**
        *   `setProcessKey` <-> `setDefinitionCode`
        *   `setProcessName` <-> `setTitle`
        *   `setProcessDefinitionId` <-> `setDefinitionId`

**Note on Attendance Service:**
I noticed the diagnostics for `ioedream-attendance-service` showing missing types (`LeaveService`, `ShiftService`). These seem to be separate issues (likely missing dependencies or package drift). I will focus on fixing the `ioedream-oa-service` compilation first as it was the primary blocker, and verify if the build progresses.
