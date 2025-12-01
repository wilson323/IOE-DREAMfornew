# 前端权限指令与常量统一用法

## 常量

统一在 `src/constants/permission-codes.js` 中维护：

- 页面/组件权限：`AREA_MANAGE`, `DEVICE_VIEW`, `ATTENDANCE_MANAGE`, `ACCESS_ENTER` 等
- 接口权限（与后端 Sa-Token 权限码一致）：`attendance:clockIn`, `access:record`, `consume:pay` 等（常量名以 `API_` 前缀导出）

示例：

```js
import { PERMISSION_CODES } from '/@/constants/permission-codes'
// 示例：PERMISSION_CODES.AREA_MANAGE、PERMISSION_CODES.API_ACCESS_RECORD
```

## 指令

- 隐藏无权限元素：`v-permission`
- 移除无权限元素：`v-permission-show`
- 禁用无权限元素：`v-permission-disabled`

### 1) 直接操作码检查

```vue
<a-button v-permission="PERMISSION_CODES.AREA_MANAGE">区域维护</a-button>
```

### 2) 带资源修饰符

```vue
<!-- 区域 -->
<a-button v-permission:area.manage>区域管理</a-button>
<a-button v-permission:area.config>区域配置</a-button>

<!-- 设备 -->
<a-button v-permission:device.view>查看设备</a-button>
<a-button v-permission:device.config>配置设备</a-button>

<!-- 考勤 -->
<a-button v-permission:attendance.view>查看考勤</a-button>
<a-button v-permission:attendance.export>导出考勤</a-button>

<!-- 门禁 -->
<a-button v-permission:access.manage>门禁管理</a-button>
```

### 3) 接口权限（与后端对齐）

```vue
<a-button v-permission="PERMISSION_CODES.API_ACCESS_RECORD">新增门禁记录</a-button>
<a-button v-permission="PERMISSION_CODES.API_ATTENDANCE_CLOCK_IN">打卡</a-button>
<a-button v-permission="PERMISSION_CODES.API_CONSUME_PAY">消费支付</a-button>
```

## Store 导出

权限 Store `src/stores/smart-permission.js` 已引入并使用 `PERMISSION_CODES`，外部组件可直接使用常量或 Store 的便捷方法：

```js
const { permissions } = usePermissionStore()
permissions.canManageArea()
permissions.canViewAttendance()
```

## 迁移建议

1. 页面/组件中统一从 `PERMISSION_CODES` 引用权限常量，禁止散落字符串字面量
2. 与后端接口权限码严格一致，命名采用 `kebab:code` 形式
3. 对历史页面进行逐步替换（建议按模块推进）

