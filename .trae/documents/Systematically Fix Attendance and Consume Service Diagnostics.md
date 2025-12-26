Based on the analysis, I have identified the root causes and solutions for the reported diagnostics:

### 1. `ioedream-attendance-service` Issues

**A. `RealtimeCalculationEngine` Test Mismatch**
*   **Cause:** `AttendanceIntegrationTest.java` incorrectly references model classes as inner static classes of `RealtimeCalculationEngine` (e.g., `RealtimeCalculationEngine.TimeRange`), whereas they are actually standalone classes in the `net.lab1024.sa.attendance.realtime.model` package.
*   **Fix:** Refactor `AttendanceIntegrationTest.java` to import the model classes directly.

**B. `MobileLeaveApplicationRequest` Missing Fields**
*   **Cause:** The test attempts to call `setStartTime(LocalTime)` and `setEndTime(LocalTime)`, but `MobileLeaveApplicationRequest` only has `startDate` and `endDate` fields (LocalData).
*   **Fix:** Add `startTime` and `endTime` fields (LocalTime) to `MobileLeaveApplicationRequest` to support finer-grained leave requests as expected by the test.

**C. `ShiftService` Missing Methods**
*   **Cause:** The skeleton `ShiftService` I created is empty.
*   **Fix:** Add the missing methods `getEmployeeShifts` and `getEmployeeSchedule` to `ShiftService.java`.

### 2. `ioedream-consume-service` Issues

**A. Missing Types/Imports**
*   **Cause:** The files `AccountVO`, `AccountAddForm`, `MultiPaymentManager`, `ConsumeExecutionManager`, etc., **do exist** in the correct locations (verified via Glob). The errors are likely due to "Package Drift" (incorrect package declaration inside the files) or simply missing import statements in the consumers.
*   **Fix:**
    *   I will systematically check the package declarations of the found files.
    *   I will add the correct imports to `ManagerConfiguration.java`, `AccountController.java`, `ConsumeAccountController.java`, and `ConsumeController.java`.

### Detailed Plan

1.  **Refactor `AttendanceIntegrationTest.java`**: Fix imports and class references.
2.  **Update `MobileLeaveApplicationRequest.java`**: Add `startTime` and `endTime` fields.
3.  **Update `ShiftService.java`**: Implement method stubs.
4.  **Fix `ioedream-consume-service` Imports**:
    *   Read the package declarations of the "missing" files to confirm correctness.
    *   Update `ManagerConfiguration.java` with correct imports.
    *   Update Controllers with correct imports for VOs and Forms.
5.  **Verify**: Run `mvn compile` on both services.

I will start with the **Attendance Service** fixes first, then move to **Consume Service**.