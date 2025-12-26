I have identified the root causes of the compilation errors and have a comprehensive plan to fix them.

### **1. Missing Common Components**
The following classes are referenced but missing from the codebase (likely due to recent refactoring or deletions). I will recreate them in their appropriate modules:

*   **`StrategyFactory`** (`microservices-common-core`): A generic factory for the Strategy pattern, used by `AttendanceCalculationManager`.
*   **`AuthManager`** (`microservices-common-security`): A security manager handling session, locking, and token logic, used by `AuthServiceImpl`.
*   **`SpringCacheServiceImpl`** (`microservices-common-cache`): A concrete implementation of `CacheService` wrapping Spring's `CacheManager`.

### **2. Service Interface Mismatches**
*   **`PaymentService`** (`ioedream-consume-service`): The interface exists but is empty. I will implement the missing methods (`processPayment`, `applyRefund`, etc.) referenced by the controllers.

### **3. Domain Model Discrepancies**
The test code expects certain convenience methods that are missing in the domain models. I will add them to simplify the API and satisfy the tests:
*   **`BatchCalculationResult`**: Add `isSuccess()`, `getProcessedCount()`, `getSuccessCount()`.
*   **`EmployeeRealtimeStatus`**: Add `getWorkHours()`, `getClockInTime()`, `getClockOutTime()`.

### **4. Test & Controller Fixes**
*   **`AttendanceIntegrationTest`**: Fix the incorrect import for `AttendanceEvent` and resolve type mismatch issues (synchronous vs asynchronous calls).
*   **`ConsumeMobileController`**: Remove the duplicate `@Slf4j` annotation.

### **Execution Plan**
1.  **Create** `StrategyFactory.java`, `AuthManager.java`, and `SpringCacheServiceImpl.java`.
2.  **Update** `PaymentService.java` with method skeletons.
3.  **Enhance** `BatchCalculationResult.java` and `EmployeeRealtimeStatus.java` with missing getters/methods.
4.  **Refactor** `AttendanceIntegrationTest.java` imports and assertions.
5.  **Clean** `ConsumeMobileController.java`.

This will resolve all reported "cannot be resolved" and "undefined method" errors.