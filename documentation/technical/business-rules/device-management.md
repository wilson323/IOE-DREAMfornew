# 设备管理业务规则文档

## BR-DEV-001: 设备状态流转规则

### 规则描述
设备状态必须按照预定义的状态机流转，不允许跳跃状态。

### 状态定义
- **0: 离线 (OFFLINE)** - 设备未连接或断开连接
- **1: 在线 (ONLINE)** - 设备正常工作状态
- **2: 维护中 (MAINTENANCE)** - 设备进入维护模式
- **3: 故障 (FAULT)** - 设备检测到故障

### 状态流转图
```
OFFLINE → ONLINE → MAINTENANCE → ONLINE
           ↓                      ↑
        FAULT ←――――――――――――――――――┘
```

### 允许的状态变更

| 当前状态 | 允许变更到 | 触发条件 | 权限要求 |
|---------|-----------|---------|----------|
| OFFLINE | ONLINE | 设备上线 | device:online |
| ONLINE | OFFLINE | 设备断线 | 自动 |
| ONLINE | MAINTENANCE | 进入维护 | device:maintain |
| ONLINE | FAULT | 故障检测 | 自动 |
| MAINTENANCE | ONLINE | 维护完成 | device:maintain |
| FAULT | MAINTENANCE | 进入维修 | device:maintain |
| FAULT | ONLINE | 故障恢复 | device:recover |

### 禁止的状态变更
- ❌ OFFLINE → MAINTENANCE（必须先上线）
- ❌ OFFLINE → FAULT（必须先上线）
- ❌ FAULT → OFFLINE（必须先修复或进入维护）
- ❌ MAINTENANCE → FAULT（维护状态不能直接变为故障）

### 后端验证逻辑

```java
public void validateStateTransition(Integer fromState, Integer toState) {
    // 定义允许的状态转换
    Map<Integer, List<Integer>> allowedTransitions = Map.of(
        DeviceStatus.OFFLINE.getCode(), List.of(DeviceStatus.ONLINE.getCode()),
        DeviceStatus.ONLINE.getCode(), List.of(
            DeviceStatus.OFFLINE.getCode(),
            DeviceStatus.MAINTENANCE.getCode(),
            DeviceStatus.FAULT.getCode()
        ),
        DeviceStatus.MAINTENANCE.getCode(), List.of(DeviceStatus.ONLINE.getCode()),
        DeviceStatus.FAULT.getCode(), List.of(
            DeviceStatus.MAINTENANCE.getCode(),
            DeviceStatus.ONLINE.getCode()
        )
    );
    
    List<Integer> allowed = allowedTransitions.get(fromState);
    if (allowed == null || !allowed.contains(toState)) {
        throw new BusinessException(
            String.format("不允许从状态 %s 变更到状态 %s", fromState, toState)
        );
    }
}
```

### 前端验证逻辑

```javascript
// 前端状态常量（与后端保持一致）
export const DEVICE_STATUS = {
  OFFLINE: 0,
  ONLINE: 1,
  MAINTENANCE: 2,
  FAULT: 3
}

// 前端状态转换验证
export function canTransitionTo(fromStatus, toStatus) {
  const transitions = {
    [DEVICE_STATUS.OFFLINE]: [DEVICE_STATUS.ONLINE],
    [DEVICE_STATUS.ONLINE]: [
      DEVICE_STATUS.OFFLINE,
      DEVICE_STATUS.MAINTENANCE,
      DEVICE_STATUS.FAULT
    ],
    [DEVICE_STATUS.MAINTENANCE]: [DEVICE_STATUS.ONLINE],
    [DEVICE_STATUS.FAULT]: [
      DEVICE_STATUS.MAINTENANCE,
      DEVICE_STATUS.ONLINE
    ]
  }
  
  return transitions[fromStatus]?.includes(toStatus) || false
}
```

### 单元测试

```java
@Test
public void testDeviceStateTransition() {
    // 正常流转
    assertTrue(canTransition(OFFLINE, ONLINE));
    assertTrue(canTransition(ONLINE, MAINTENANCE));
    assertTrue(canTransition(MAINTENANCE, ONLINE));
    assertTrue(canTransition(ONLINE, FAULT));
    assertTrue(canTransition(FAULT, ONLINE));
    
    // 非法流转
    assertFalse(canTransition(OFFLINE, MAINTENANCE));
    assertFalse(canTransition(OFFLINE, FAULT));
    assertFalse(canTransition(FAULT, OFFLINE));
    assertFalse(canTransition(MAINTENANCE, FAULT));
}
```

---

## BR-DEV-002: 设备权限验证规则

### 规则描述
所有设备操作必须进行权限验证，确保用户只能操作其有权限的设备。

### 权限定义

| 权限标识 | 权限名称 | 说明 |
|---------|---------|------|
| device:query | 查询设备 | 查看设备列表和详情 |
| device:add | 新增设备 | 添加新设备 |
| device:update | 更新设备 | 修改设备信息 |
| device:delete | 删除设备 | 删除设备（软删除） |
| device:online | 设备上线 | 控制设备上线 |
| device:maintain | 设备维护 | 进入/退出维护模式 |
| device:recover | 故障恢复 | 从故障状态恢复 |

### 实施规范

**后端Controller必须添加权限注解**：
```java
@PostMapping("/update")
@SaCheckPermission("device:update")
public ResponseDTO<String> update(@RequestBody @Validated DeviceUpdateForm form) {
    return deviceService.update(form);
}
```

**前端路由配置权限**：
```javascript
{
  path: '/device/manage',
  meta: {
    permission: 'device:query'
  }
}
```

---

## BR-DEV-003: 设备数据验证规则

### 必填字段验证

| 字段名 | 验证规则 | 错误提示 |
|-------|---------|----------|
| deviceName | 非空，长度2-50 | 设备名称长度必须在2-50之间 |
| deviceCode | 非空，唯一，长度5-20 | 设备编码长度必须在5-20之间且不能重复 |
| deviceType | 非空，必须在枚举范围内 | 设备类型无效 |

### 后端验证示例

```java
public class DeviceAddForm {
    @NotBlank(message = "设备名称不能为空")
    @Length(min = 2, max = 50, message = "设备名称长度必须在2-50之间")
    private String deviceName;
    
    @NotBlank(message = "设备编码不能为空")
    @Pattern(regexp = "^[A-Z0-9]{5,20}$", message = "设备编码格式错误")
    private String deviceCode;
    
    @NotNull(message = "设备类型不能为空")
    private Integer deviceType;
}
```

---

## AI开发注意事项

### ✅ 必须遵守
1. 所有状态变更必须调用`validateStateTransition()`验证
2. 所有接口必须添加`@SaCheckPermission`权限注解
3. 前后端状态常量必须保持一致
4. 所有验证规则必须前后端双重验证

### ❌ 禁止操作
1. 禁止跳过状态验证直接更新数据库
2. 禁止在前端跳过权限检查
3. 禁止硬编码状态值（使用枚举常量）
4. 禁止物理删除设备数据

### 🔍 检查清单
- [ ] 是否实现了状态流转验证？
- [ ] 是否添加了权限注解？
- [ ] 是否前后端验证规则一致？
- [ ] 是否编写了单元测试？
