# 异常处理规范检查报告

**生成时间**: 2025年12月25日 14:48:07  
**检查范围**: microservices/

---

## 🔍 检查结果

### 1. 过于宽泛的异常捕获 (50 处)
- microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/config/EdgeOfflineRecordReplayConfig.java:52:        } catch (Exception e) {
- microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessAreaController.java:72:        } catch (Exception e) {
- microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessAreaController.java:96:        } catch (Exception e) {
- microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessAreaController.java:123:        } catch (Exception e) {
- microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessAreaController.java:150:        } catch (Exception e) {
- microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessAreaController.java:178:        } catch (Exception e) {
- microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessAreaController.java:203:        } catch (Exception e) {
- microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessAreaController.java:234:        } catch (Exception e) {
- microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessAreaController.java:265:        } catch (Exception e) {
- microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessAreaController.java:289:        } catch (Exception e) {
- microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessBackendAuthController.java:108:        } catch (Exception e) {
- microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessBackendAuthController.java:159:            } catch (Exception e) {
- microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessBackendAuthController.java:172:            } catch (NumberFormatException e) {
- microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessBackendAuthController.java:187:            } catch (NumberFormatException e) {
- microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessBackendAuthController.java:220:        } catch (NumberFormatException e) {
- microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessDeviceController.java:67:        } catch (Exception e) {
- microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessDeviceController.java:91:        } catch (Exception e) {
- microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessDeviceController.java:114:        } catch (Exception e) {
- microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessDeviceController.java:138:        } catch (Exception e) {
- microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessDeviceController.java:163:        } catch (Exception e) {
### 2. 空catch块 (395 处)
建议使用 log.error() 记录异常
### 3. printStackTrace使用 (1 处)
- microservices/microservices-common-core/src/main/java/net/lab1024/sa/common/util/ExceptionMetricsCollector.java:59:            e.printStackTrace();
