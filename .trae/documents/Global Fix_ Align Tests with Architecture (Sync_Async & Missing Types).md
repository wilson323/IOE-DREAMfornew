Based on the diagnostics and the user's strong feedback ("root cause analysis"), I have performed a deep analysis of the errors.

### **Root Cause Analysis (Deep Dive)**

The errors are not just simple typos; they represent a fundamental mismatch between **Test Assumptions** and **Implementation Reality** across multiple services.

1.  **`ioedream-attendance-service` (Test vs. Engine Mismatch)**
    *   **Async Mismatch:** The `RealtimeCalculationEngine` methods (`startup`, `processAttendanceEvent`, etc.) return direct objects (e.g., `EngineStartupResult`), but the test code wraps them in `CompletableFuture`. This suggests the engine was refactored to be synchronous (or was always synchronous), but the test assumes an asynchronous API.
    *   **Builder Pattern Drift:** `TimeRange.builder().startDate(...)` fails because the Lombok builder likely uses the field name `startDate`, but the test might be expecting a different method name or the builder isn't generated as expected.
    *   **Inner Class vs. Import:** The test defines its own static inner classes `Shift` and `Schedule`, but `ShiftService` returns `java.util.List<Object>` (my temporary fix). The test tries to cast `List<Object>` to `CompletableFuture<List<Shift>>`. This is a double mismatch: Type (Object vs Shift) and Async (List vs Future).

2.  **`ioedream-device-comm-service` (Missing Dependencies & Cache Logic)**
    *   **Missing Types:** `BiometricDataManager`, `BiometricController`, `VerifyTypeEnum` are referenced in tests but the compiler can't find them. This usually means:
        *   They are in a different module/package that isn't imported.
        *   They were deleted or renamed.
        *   The test is stale.
    *   **Cache Service Mismatch:** `ProtocolCacheServiceImplTest` calls methods like `getDeviceById` which are undefined on `ProtocolCacheServiceImpl`. This indicates the cache service interface has changed, but the test wasn't updated.

3.  **`ioedream-consume-service` (Missing PaymentService)**
    *   `PaymentRecordServiceImplTest` fails because `PaymentService` cannot be resolved. This is likely a missing class or package import issue.

### **Systematic Fix Plan**

I will not just "patch" errors. I will align the **Tests** to the **Current Architecture**.

#### **Phase 1: Fix Attendance Service Tests (Sync vs Async)**
*   **Action:** Rewrite `AttendanceIntegrationTest.java` to remove `CompletableFuture` usage for methods that are now synchronous (`realtimeEngine.startup()`, `shiftService.getEmployeeShifts()`, etc.).
*   **Action:** Update `ShiftService.java` to return the correct types (`List<Shift>`, `Schedule`) instead of `List<Object>`, and move the `Shift` and `Schedule` inner classes from the Test to the main source (or use existing model classes if they exist). *Correction:* The test defines them as inner classes. I will keep them there for now but update `ShiftService` to return `List<AttendanceIntegrationTest.Shift>` (or better, real model classes if I can find them, but to fix the immediate build, I will make `ShiftService` return what the test expects or adapt the test).
*   *Decision:* I will update `ShiftService` to return proper domain objects and update the test to use them, removing the inner classes if possible, or adapt the test to use the synchronous API.

#### **Phase 2: Fix Device Comm Service (Missing Types)**
*   **Action:** Search for `BiometricDataManager`, `VerifyTypeEnum` to see if they exist.
    *   If they exist: Fix imports.
    *   If they don't: Create skeletons or remove the stale test.
*   **Action:** Update `ProtocolCacheServiceImpl` to implement the methods expected by the test, OR update the test to use the actual methods available in the service (likely `getDevice`, `cacheDevice`, etc.).

#### **Phase 3: Fix Consume Service (PaymentService)**
*   **Action:** Create or Import `PaymentService`.

### **Execution Steps**

1.  **Attendance Service:**
    *   Modify `AttendanceIntegrationTest.java`: Remove `CompletableFuture` wrappers.
    *   Modify `ShiftService.java`: Return proper types.
2.  **Device Comm Service:**
    *   Implement missing methods in `ProtocolCacheServiceImpl` (or fix test).
    *   Fix imports/create types for `Biometric*` classes.
3.  **Consume Service:**
    *   Fix `PaymentService` reference.

I will start by fixing the **Attendance Service** as it has the most complex structural mismatch (Sync/Async).