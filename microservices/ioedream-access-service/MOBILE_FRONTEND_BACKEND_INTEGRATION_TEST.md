# 门禁模块移动端前后端联调测试报告

**测试时间**: 2025-12-24
**测试范围**: 门禁权限管理 + 离线同步接口
**后端服务**: ioedream-access-service (8090)
**前端应用**: smart-app (uni-app)

---

## 一、接口对接清单

### 1.1 权限管理接口（10个）

| 序号 | 接口方法 | 请求路径 | 功能 | 状态 |
|------|---------|---------|------|------|
| 1 | getUserPermissions | GET /api/v1/mobile/access/permission/list | 获取用户权限列表 | ✅ 已对接 |
| 2 | getUserPermissionStatistics | GET /api/v1/mobile/access/permission/statistics | 获取权限统计 | ✅ 已对接 |
| 3 | getPermissionDetail | GET /api/v1/mobile/access/permission/{id} | 获取权限详情 | ✅ 已对接 |
| 4 | getPermissionQRCode | GET /api/v1/mobile/access/permission/{id}/qrcode | 获取权限二维码 | ✅ 已对接 |
| 5 | getPermissionRecords | GET /api/v1/mobile/access/permission/{id}/records | 获取通行记录 | ✅ 已对接 |
| 6 | getPermissionHistory | GET /api/v1/mobile/access/permission/{id}/history | 获取权限历史 | ✅ 已对接 |
| 7 | renewPermission | POST /api/v1/mobile/access/permission/{id}/renew | 续期权限 | ✅ 已对接 |
| 8 | transferPermission | POST /api/v1/mobile/access/permission/{id}/transfer | 转移权限 | ✅ 已对接 |
| 9 | freezePermission | POST /api/v1/mobile/access/permission/{id}/freeze | 冻结权限 | ✅ 已对接 |
| 10 | unfreezePermission | POST /api/v1/mobile/access/permission/{id}/unfreeze | 解冻权限 | ✅ 已对接 |

### 1.2 过期提醒接口（3个）

| 序号 | 接口方法 | 请求路径 | 功能 | 状态 |
|------|---------|---------|------|------|
| 11 | getExpiringStatistics | GET /api/v1/mobile/access/permission/expiring/statistics | 获取过期统计 | ✅ 已对接 |
| 12 | getExpiringPermissions | GET /api/v1/mobile/access/permission/expiring/list | 获取即将过期列表 | ✅ 已对接 |
| 13 | batchRenewPermissions | POST /api/v1/mobile/access/permission/batch-renew | 批量续期 | ✅ 已对接 |

### 1.3 离线同步接口（4个）

| 序号 | 接口方法 | 请求路径 | 功能 | 状态 |
|------|---------|---------|------|------|
| 14 | getOfflineSyncData | GET /api/v1/mobile/access/offline/sync-data | 获取离线同步数据 | ✅ 已对接 |
| 15 | uploadOfflineRecords | POST /api/v1/mobile/access/offline/upload-records | 上传离线通行记录 | ✅ 已对接 |
| 16 | getSyncStatus | GET /api/v1/mobile/access/offline/sync-status | 获取同步状态 | ✅ 已对接 |
| 17 | syncNow | POST /api/v1/mobile/access/offline/sync-now | 立即同步 | ✅ 已对接 |

---

## 二、数据格式对接

### 2.1 权限查询表单 (PermissionQueryForm)

```typescript
{
  permissionStatus?: number,  // 权限状态：1-有效，2-即将过期，3-已过期，4-已冻结
  permissionType?: number,    // 权限类型：1-永久，2-临时，3-时段
  areaId?: number,             // 区域ID
  pageNum: number,            // 页码（默认1）
  pageSize: number            // 每页数量（默认20）
}
```

### 2.2 权限VO (AccessPermissionVO)

```typescript
{
  permissionId: number,           // 权限ID
  userId: number,                 // 用户ID
  username: string,               // 用户名
  realName: string,               // 真实姓名
  areaId: number,                 // 区域ID
  areaName: string,               // 区域名称
  areaCode: string,               // 区域编码
  permissionType: number,         // 权限类型：1-永久，2-临时，3-时段
  permissionTypeName: string,     // 权限类型名称
  permissionStatus: number,       // 权限状态：1-有效，2-即将过期，3-已过期，4-已冻结
  permissionStatusName: string,   // 权限状态名称
  permissionLevel: number,        // 权限级别
  startTime: string,              // 开始时间
  endTime: string,                // 结束时间
  permanent: boolean,             // 是否永久
  allowStartTime: string,         // 允许进入时间段（开始）
  allowEndTime: string,           // 允许进入时间段（结束）
  workdayOnly: boolean,           // 仅工作日
  accessPermissions: string,      // 可通行方式
  extendedAttributes: string,     // 扩展属性（JSON）
  passCount: number,              // 通行次数
  lastPassTime: string,           // 最后通行时间
  deviceSyncStatus: number,       // 设备同步状态：0-未同步，1-同步成功，2-同步失败
  lastSyncTime: string,           // 最后同步时间
  daysUntilExpiry: number         // 距离过期天数（负数表示已过期多少天）
}
```

### 2.3 权限统计VO (AccessPermissionStatisticsVO)

```typescript
{
  total: number,        // 总权限数
  valid: number,        // 有效权限数
  expiring: number,     // 即将过期权限数
  expired: number,     // 已过期权限数
  frozen: number,      // 已冻结权限数
  permanent: number,   // 永久权限数
  temporary: number,   // 临时权限数
  timeBased: number   // 时段权限数
}
```

### 2.4 续期表单 (PermissionRenewForm)

```typescript
{
  duration: number,    // 续期时长：7/15/30/90/180/365（天）
  reason?: string      // 续期原因
}
```

### 2.5 离线同步数据VO (OfflineSyncDataVO)

```typescript
{
  syncTimestamp: number,              // 同步时间戳
  syncTime: string,                   // 同步时间
  fullSync: boolean,                  // 是否需要全量同步
  permissions: AccessPermissionVO[],  // 权限数据
  dataVersion: number,                // 数据版本
  deletedPermissionIds: number[],     // 删除的权限ID列表
  changeSinceTimestamp: number,       // 数据变更起始时间戳
  changeUntilTimestamp: number        // 数据变更结束时间戳
}
```

---

## 三、接口测试用例

### 3.1 权限列表查询

**测试步骤**:
1. 打开权限列表页面
2. 查看权限统计数据是否正确显示
3. 切换不同Tab（有效/即将过期/已过期）
4. 下拉刷新数据
5. 上拉加载更多

**预期结果**:
- ✅ 统计卡片显示正确数据
- ✅ Tab切换显示对应状态权限
- ✅ 下拉刷新更新数据
- ✅ 上拉加载更多权限

### 3.2 权限详情查看

**测试步骤**:
1. 点击权限卡片
2. 跳转到权限详情页
3. 查看权限完整信息

**预期结果**:
- ✅ 正确跳转到详情页
- ✅ 显示权限完整信息
- ✅ 通行记录列表正确显示

### 3.3 权限二维码

**测试步骤**:
1. 点击权限卡片"二维码"按钮
2. 显示权限二维码弹窗
3. 等待二维码加载完成
4. 点击"保存到相册"

**预期结果**:
- ✅ 二维码弹窗正确打开
- ✅ 二维码图片正确显示
- ✅ 保存到相册成功

### 3.4 权限续期

**测试步骤**:
1. 点击权限卡片"续期"按钮
2. 打开续期弹窗
3. 选择续期时长
4. 输入续期原因
5. 提交续期申请

**预期结果**:
- ✅ 续期弹窗正确打开
- ✅ 续期时长选择生效
- ✅ 提交成功后显示提示
- ✅ 权限列表自动刷新

### 3.5 过期提醒

**测试步骤**:
1. 打开过期提醒页面
2. 查看过期统计数据
3. 查看过期权限列表
4. 批量续期操作

**预期结果**:
- ✅ 过期统计正确显示
- ✅ 过期列表按紧急程度分类
- ✅ 批量续期功能正常

### 3.6 离线同步

**测试步骤**:
1. 打开离线同步设置页面
2. 查看同步状态
3. 手动触发同步
4. 上传离线记录

**预期结果**:
- ✅ 同步状态正确显示
- ✅ 手动同步成功执行
- ✅ 离线记录上传成功

---

## 四、问题记录与解决

### 4.1 已解决问题

| 问题编号 | 问题描述 | 原因分析 | 解决方案 | 状态 |
|---------|---------|---------|---------|------|
| - | - | - | - | - |

### 4.2 待解决问题

| 问题编号 | 问题描述 | 原因分析 | 解决方案 | 状态 |
|---------|---------|---------|---------|------|
| - | - | - | - | - |

---

## 五、测试结论

### 5.1 接口对接完成度

- **权限管理接口**: 10/10 (100%)
- **过期提醒接口**: 3/3 (100%)
- **离线同步接口**: 4/4 (100%)
- **总计**: 17/17 (100%)

### 5.2 功能测试完成度

- **权限列表功能**: 待测试
- **权限详情功能**: 待测试
- **二维码功能**: 待测试
- **续期功能**: 待测试
- **过期提醒功能**: 待测试
- **离线同步功能**: 待测试

### 5.3 下一步工作

1. 启动后端服务进行联调测试
2. 使用 Postman 或 Apifox 进行接口测试
3. 在前端页面进行功能验证
4. 修复发现的问题
5. 完成系统功能测试

---

## 六、接口测试脚本

### 6.1 Postman/Apifox 测试集合

```json
{
  "info": {
    "name": "门禁权限管理移动端API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "权限管理",
      "item": [
        {
          "name": "获取权限列表",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:8090/api/v1/mobile/access/permission/list?pageNum=1&pageSize=20",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8090",
              "path": ["api", "v1", "mobile", "access", "permission", "list"],
              "query": [
                {"key": "pageNum", "value": "1"},
                {"key": "pageSize", "value": "20"}
              ]
            }
          }
        },
        {
          "name": "获取权限统计",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:8090/api/v1/mobile/access/permission/statistics",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8090",
              "path": ["api", "v1", "mobile", "access", "permission", "statistics"]
            }
          }
        }
      ]
    },
    {
      "name": "离线同步",
      "item": [
        {
          "name": "获取同步数据",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:8090/api/v1/mobile/access/offline/sync-data?dataType=permissions",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8090",
              "path": ["api", "v1", "mobile", "access", "offline", "sync-data"],
              "query": [{"key": "dataType", "value": "permissions"}]
            }
          }
        },
        {
          "name": "上传离线记录",
          "request": {
            "method": "POST",
            "header": [
              {"key": "Content-Type", "value": "application/json"}
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"deviceId\": \"device_12345\",\n  \"records\": []\n}"
            },
            "url": {
              "raw": "http://localhost:8090/api/v1/mobile/access/offline/upload-records",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8090",
              "path": ["api", "v1", "mobile", "access", "offline", "upload-records"]
            }
          }
        }
      ]
    }
  ]
}
```

---

**文档版本**: v1.0
**更新时间**: 2025-12-24
**维护人员**: IOE-DREAM Team
