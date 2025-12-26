I have analyzed the compilation errors and identified the necessary fixes across multiple files.

### 1. `CacheServiceImpl.java` (Common Service)
- **Problem**: Missing implementation of abstract methods from `CacheService` interface (`put`, `clear`, `exists`, `evict`).
- **Fix**: Restore and implement these methods using `RedisTemplate` and `CacheManager`.

### 2. `GatewayServiceClient.java` (Common Core)
- **Problem**: 
  - Missing `callService` method that accepts `TypeReference` for generic return types.
  - Missing specific service call methods (`callAttendanceService`, `callDeviceCommService`).
- **Fix**: 
  - Add `callService` method with `TypeReference` support.
  - Add stub methods for `callAttendanceService` and `callDeviceCommService`.

### 3. `AESUtil.java` (Common Core)
- **Problem**: Static access to instance methods `encrypt(String)` and `decrypt(String)`.
- **Fix**: Change these methods to `static` and use a default key.

### 4. `ExceptionMetricsCollector.java` (Common Business)
- **Problem**: Method signature mismatch. Caller expects `(Exception, long)`, but stub has `(String, String)`.
- **Fix**: Add overload `recordException(Exception e, long duration)`.

### 5. `ConsumeController.java` (Consume Service)
- **Problem**: `log` symbol not found.
- **Fix**: Add missing `@Slf4j` annotation to the class.

### 6. `IoeDreamGatewayProperties.java` (Common Core)
- **Problem**: Missing `getAesKey()` method.
- **Fix**: Add `aesKey` field with getter/setter.

I will apply these changes to resolve the compilation errors.