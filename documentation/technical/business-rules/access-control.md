# 门禁控制业务规则文档

## BR-ACC-001: 门禁开门规则

### 规则描述
门禁开门必须满足多重验证条件，确保安全性和合规性。

### 验证流程
```
1. 人员验证 → 2. 时间验证 → 3. 权限验证 → 4. 状态验证 → 5. 开门
```

### 验证规则

#### 1. 人员验证
- 人员必须在系统中注册
- 人员状态必须为"启用"
- 人员证件必须在有效期内

#### 2. 时间验证
- 当前时间必须在允许时间段内
- 工作日/节假日规则校验
- 特殊时间段（如夜间）需要额外权限

#### 3. 权限验证
- 人员必须具有该门禁点的通行权限
- 权限必须在有效期内
- VIP通道需要特殊权限

#### 4. 状态验证
- 门禁设备状态必须为"在线"
- 门禁设备未被锁定
- 门禁设备未处于维护模式

### 开门模式

| 模式 | 说明 | 权限要求 | 有效时长 |
|-----|------|---------|----------|
| 正常开门 | 刷卡/人脸识别开门 | access:normal | 5秒 |
| 远程开门 | 管理员远程开门 | access:remote | 10秒 |
| 常开模式 | 门禁保持开启状态 | access:always-open | 直到关闭 |
| 临时授权 | 临时访客开门 | access:temp | 指定时长 |

### 后端验证逻辑

```java
public ResponseDTO<String> openDoor(AccessRequest request) {
    // 1. 人员验证
    Person person = personService.getById(request.getPersonId());
    if (person == null || person.getStatus() != PersonStatus.ENABLED) {
        return ResponseDTO.error("人员信息无效");
    }
    
    // 2. 时间验证
    if (!isInAllowedTimeRange(request.getAccessPointId(), LocalDateTime.now())) {
        return ResponseDTO.error("当前时间不允许通行");
    }
    
    // 3. 权限验证
    if (!hasAccessPermission(request.getPersonId(), request.getAccessPointId())) {
        return ResponseDTO.error("无通行权限");
    }
    
    // 4. 状态验证
    AccessPoint accessPoint = accessPointService.getById(request.getAccessPointId());
    if (accessPoint.getStatus() != AccessPointStatus.ONLINE) {
        return ResponseDTO.error("门禁设备离线");
    }
    
    // 5. 执行开门
    return executeOpen(request);
}
```

---

## BR-ACC-002: 访客管理规则

### 访客类型

| 类型 | 有效期 | 审批流程 | 通行范围 |
|-----|-------|---------|----------|
| 临时访客 | 当天 | 无需审批 | 指定区域 |
| 短期访客 | 7天内 | 部门主管审批 | 指定区域 |
| 长期访客 | 30天内 | 安保部门审批 | 全部区域（限定） |
| VIP访客 | 自定义 | 总经理审批 | 全部区域 |

### 访客登记流程
```
1. 前台登记 → 2. 拍照存档 → 3. 发放临时卡 → 4. 授权开通 → 5. 访问监控
```

---

## AI开发注意事项

### ✅ 必须遵守
1. 所有开门操作必须完整验证四个条件
2. 所有门禁记录必须保存日志
3. 异常开门必须触发告警
4. 访客权限必须设置有效期

### ❌ 禁止操作
1. 禁止跳过任何验证步骤
2. 禁止删除门禁记录（只能归档）
3. 禁止给访客永久权限
4. 禁止绕过审批流程
