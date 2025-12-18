# 服务优化检查清单

**版本**: v1.0.0  
**日期**: 2025-12-18  
**状态**: 进行中

---

## 📋 优化目标

根据ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求，逐个服务检查并优化：

1. **缓存使用优化** - 所有查询操作使用多级缓存
2. **数据库查询优化** - 消除全表扫描、优化深度分页
3. **索引优化** - 确保所有查询都有合适索引

---

## ✅ 服务优化进度

### 1. ioedream-common-service (8088) - 公共业务服务

#### 缓存使用检查
- ✅ `AreaUnifiedServiceImpl.getAreaTree()` - 已使用@Cacheable
- ✅ `AreaUnifiedServiceImpl.getUserAccessibleAreas()` - 已使用@Cacheable
- ✅ `AreaUnifiedServiceImpl.getAreaByCode()` - 已使用@Cacheable
- ⚠️ `AreaUnifiedServiceImpl.hasAreaAccess()` - 递归查询，建议添加缓存
- ⚠️ `AreaUnifiedServiceImpl.getAreaPath()` - 多次查询，建议添加缓存
- ⚠️ `AreaUnifiedServiceImpl.getAreaById()` - 直接查询，建议添加缓存
- ⚠️ `RegionalHierarchyServiceImpl` - 多处直接查询，建议添加缓存

#### 数据库查询优化
- ⚠️ `AreaUnifiedServiceImpl.getAreaPath()` - 循环查询父区域，建议优化为单次查询
- ⚠️ `RegionalHierarchyServiceImpl` - 多处使用selectById，建议批量查询

#### 索引检查
- ✅ `t_common_area` 表已有基础索引
- ⚠️ 需要检查是否有按parentId查询的索引

---

### 2. ioedream-access-service (8090) - 门禁管理服务

#### 缓存使用检查
- ✅ `AccessDeviceServiceImpl.getDeviceDetail()` - 已使用@Cacheable
- ✅ `AccessDeviceServiceImpl.addDevice()` - 已使用@CacheEvict（清除所有缓存）
- ✅ `AccessDeviceServiceImpl.updateDevice()` - 已使用@CacheEvict（清除指定设备缓存）
- ✅ `AccessDeviceServiceImpl.deleteDevice()` - 已使用@CacheEvict（清除指定设备缓存）
- ✅ `AccessDeviceServiceImpl.updateDeviceStatus()` - 已使用@CacheEvict（清除指定设备缓存）

#### 数据库查询优化
- ⏳ 待检查

#### 索引检查
- ✅ 已有索引优化脚本：`V20251216__01_optimize_access_service_indexes.sql`

---

### 3. ioedream-attendance-service (8091) - 考勤管理服务

#### 缓存使用检查
- ✅ `AttendanceRecordServiceImpl.getAttendanceRecordStatistics()` - 已使用@Cacheable（统计查询缓存）
- ⚠️ `ScheduleServiceImpl` - 多处使用selectById，建议添加缓存
- ⏳ 其他服务类待检查

#### 数据库查询优化
- ⏳ 待检查

#### 索引检查
- ⏳ 待检查

---

### 4. ioedream-consume-service (8094) - 消费管理服务

#### 缓存使用检查
- ⏳ 待检查

#### 数据库查询优化
- ⏳ 待检查

#### 索引检查
- ⏳ 待检查

---

### 5. ioedream-visitor-service (8095) - 访客管理服务

#### 缓存使用检查
- ⏳ 待检查

#### 数据库查询优化
- ⏳ 待检查

#### 索引检查
- ✅ 已有索引优化：访客黑名单、审批记录等表

---

### 6. ioedream-video-service (8092) - 视频监控服务

#### 缓存使用检查
- ⏳ 待检查

#### 数据库查询优化
- ⏳ 待检查

#### 索引检查
- ✅ 已有基础索引

---

### 7. ioedream-device-comm-service (8087) - 设备通讯服务

#### 缓存使用检查
- ⏳ 待检查

#### 数据库查询优化
- ⏳ 待检查

#### 索引检查
- ⏳ 待检查

---

### 8. ioedream-oa-service (8089) - OA办公服务

#### 缓存使用检查
- ⏳ 待检查

#### 数据库查询优化
- ⏳ 待检查

#### 索引检查
- ⏳ 待检查

---

### 9. ioedream-biometric-service (8096) - 生物模板管理服务

#### 缓存使用检查
- ⏳ 待检查

#### 数据库查询优化
- ⏳ 待检查

#### 索引检查
- ⏳ 待检查

---

## 🔧 优化建议

### 缓存优化建议

1. **递归查询缓存** - 对于递归查询（如getAreaPath），建议添加缓存避免重复查询
2. **批量查询优化** - 对于循环查询（如多次selectById），建议改为批量查询
3. **缓存键规范** - 统一缓存键命名规范：`{module}:{entity}:{id}`

### 数据库查询优化建议

1. **深度分页优化** - 使用CursorPagination工具类替代LIMIT offset, size
2. **批量查询** - 使用selectBatchIds或IN查询替代循环查询
3. **索引优化** - 确保所有WHERE条件字段都有索引

---

## 📝 下一步行动

1. 逐个服务检查并优化缓存使用
2. 逐个服务检查并优化数据库查询
3. 验证索引优化效果
