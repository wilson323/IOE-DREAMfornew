# SmartAdmin AI 开发指令文档 - 使用说明

## 📌 这份文档是做什么的？

这是一份**让AI自动生成符合项目规范代码**的指令文档。有了它，你只需要提供简单的需求描述，AI就能自动生成完整的功能模块，且所有代码都符合项目的编码规范和风格。

---

## 🎯 能实现什么效果？

### 传统开发方式：
```
你：我需要开发一个客户管理功能
AI：好的，我来写代码...
结果：代码风格不统一、文件结构混乱、命名不规范、需要大量修改
```

### 使用本文档后：
```
你：【把指令文档发给AI】
    请基于 SmartAdmin 项目为我开发一个【客户管理】功能，需求如下：
    1. 功能描述：管理客户信息
    2. 字段：客户名称、联系电话、客户等级...
    3. 权限：添加、编辑、删除、查询

AI：【自动生成4个文件，所有代码完全符合规范】
    ✅ src/api/business/customer/customer-api.js
    ✅ src/constants/business/customer-const.js  
    ✅ src/views/business/customer/index.vue
    ✅ src/views/business/customer/components/customer-form-modal/index.vue

结果：代码可以直接使用，无需修改！
```

---

## 🚀 快速开始

### 方式一：给AI使用（推荐）

**步骤1：准备文档**
- 打开 `AI二次开发指令文档.md` 文件

**步骤2：发送给AI**
```
【复制整个文档内容，发送给AI】

然后说：
请严格按照这份《SmartAdmin AI 二次开发指令文档》的规范，
为我开发一个【XXX管理】功能，需求如下：

1. 功能描述：
   - 管理XXX信息
   - 支持增删改查
   - 支持按XX搜索

2. 字段列表：
   - 字段1：类型、是否必填、说明
   - 字段2：类型、是否必填、说明
   - ...

3. 操作权限：
   - business:xxx:add（添加）
   - business:xxx:update（编辑）
   - business:xxx:delete（删除）
   - business:xxx:query（查询）
```

**步骤3：AI自动生成**
AI会根据文档中的模板和规范，自动生成所有需要的文件，包括：
- API 接口文件
- 常量定义文件
- 列表页面组件
- 表单弹窗组件

**所有代码都会自动符合：**
- ✅ 项目的文件结构规范
- ✅ 统一的命名规范（kebab-case、camelCase、PascalCase）
- ✅ 标准的文件头注释
- ✅ 一致的代码组织顺序
- ✅ 权限控制规范
- ✅ 错误处理规范

---

### 方式二：手动使用模板

**适用场景**：不想用AI，自己手写代码

**步骤1：找到对应模板**
打开 `AI二次开发指令文档.md`，在第2章节找到对应的模板：
- 2.1 API 接口文件模板
- 2.2 常量定义文件模板
- 2.3 列表页面组件模板
- 2.4 表单弹窗组件模板

**步骤2：复制模板代码**
- 复制完整的模板代码
- 替换所有的 `{模块名}`、`{模块分类}` 等占位符
- 根据实际需求调整字段和逻辑

**步骤3：创建文件**
按照文档中的目录结构，将代码保存到对应位置：
```
src/
├── api/业务模块/xxx-api.js
├── constants/业务模块/xxx-const.js
└── views/业务模块/xxx/
    ├── index.vue
    └── components/xxx-form-modal/index.vue
```

---

## 📖 完整使用示例

### 示例需求：开发"供应商管理"功能

**发给AI的完整指令：**

```
请严格按照《SmartAdmin AI 二次开发指令文档》的规范，
为我开发一个【供应商管理】功能，需求如下：

1. 功能描述：
   - 管理公司供应商信息
   - 支持供应商的增删改查
   - 支持按供应商名称、联系人搜索
   - 支持供应商启用/禁用

2. 字段列表：
   - 供应商名称：字符串，必填，2-100字符
   - 供应商编码：字符串，必填，唯一
   - 联系人：字符串，必填，2-50字符
   - 联系电话：字符串，必填，手机号格式
   - 联系邮箱：字符串，非必填，邮箱格式
   - 地址：字符串，非必填，最多200字符
   - 状态：枚举（启用/禁用），必填
   - 备注：文本域，非必填，最多500字符

3. 操作权限：
   - business:supplier:add（添加供应商）
   - business:supplier:update（编辑供应商）
   - business:supplier:delete（删除供应商）
   - business:supplier:query（查询供应商）
   - business:supplier:export（导出供应商）

4. 特殊需求：
   - 列表页支持批量删除
   - 列表页显示状态标签（启用显示绿色，禁用显示灰色）
   - 表单验证联系电话必须是手机号格式
   - 供应商编码不可重复
```

**AI将自动生成：**

✅ **文件1**: `src/api/business/supplier/supplier-api.js`
```javascript
// 完整的API接口定义，包含：
// - querySupplier (分页查询)
// - addSupplier (添加)
// - updateSupplier (更新)
// - deleteSupplier (删除)
// - batchDeleteSupplier (批量删除)
// - exportSupplier (导出)
```

✅ **文件2**: `src/constants/business/supplier-const.js`
```javascript
// 完整的常量定义，包含：
// - SUPPLIER_STATUS_ENUM (状态枚举)
```

✅ **文件3**: `src/views/business/supplier/index.vue`
```vue
// 完整的列表页，包含：
// - 查询表单（名称、联系人搜索）
// - 操作按钮（新建、批量删除）
// - 数据表格（含状态标签显示）
// - 分页组件
// - 权限控制
```

✅ **文件4**: `src/views/business/supplier/components/supplier-form-modal/index.vue`
```vue
// 完整的表单弹窗，包含：
// - 所有字段的表单项
// - 完整的验证规则（手机号、邮箱、长度等）
// - 新增/编辑模式切换
// - 提交逻辑
```

---

## ✨ 核心优势

### 1. 代码风格完全一致
所有生成的代码都严格遵循项目规范：
- 文件命名：`supplier-api.js`、`supplier-const.js`
- 变量命名：`supplierApi`、`SUPPLIER_STATUS_ENUM`
- 组件命名：`SupplierFormModal`
- 路径使用：`/@/` 别名

### 2. 文件结构标准统一
```
src/
├── api/business/supplier/
│   └── supplier-api.js
├── constants/business/
│   └── supplier-const.js
└── views/business/supplier/
    ├── index.vue
    └── components/
        └── supplier-form-modal/
            └── index.vue
```

### 3. 功能实现完整
- ✅ 增删改查功能齐全
- ✅ 权限控制完善
- ✅ 表单验证完整
- ✅ 错误处理规范
- ✅ 加载状态处理
- ✅ 二次确认弹窗

### 4. 注释文档规范
```javascript
/*
 * 供应商管理 API
 *
 * @Author:    1024创新实验室-主任：卓大
 * @Date:      2025-11-03 10:00:00
 * @Wechat:    zhuda1024
 * @Email:     lab1024@163.com
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */
```

### 5. 可直接使用
生成的代码无需修改（或仅需少量调整），可以直接集成到项目中使用。

---

## 📋 需求描述模板

为了让AI更好地理解你的需求，建议按以下模板描述：

```
请基于 SmartAdmin 项目为我开发一个【模块名称】功能，需求如下：

1. 功能描述：
   - 核心功能说明
   - 支持哪些操作
   - 特殊业务逻辑

2. 字段列表：
   - 字段名：数据类型，是否必填，长度限制，其他说明
   - 字段名：数据类型，是否必填，验证规则
   - ...

3. 操作权限：
   - {模块分类}:{模块名}:add（添加）
   - {模块分类}:{模块名}:update（编辑）
   - {模块分类}:{模块名}:delete（删除）
   - {模块分类}:{模块名}:query（查询）

4. 特殊需求（可选）：
   - UI展示要求
   - 特殊验证规则
   - 特殊业务逻辑
```

---

## 🔧 常见字段类型说明

在描述字段时，可以使用以下类型：

| 字段类型 | 说明 | 示例 |
|---------|------|------|
| 字符串 | 普通文本输入 | 姓名、编码、地址 |
| 数字 | 数字输入框 | 价格、数量、年龄 |
| 枚举 | 下拉选择/单选框 | 状态、分类、性别 |
| 日期 | 日期选择器 | 创建日期、生效日期 |
| 日期范围 | 日期范围选择 | 有效期、活动时间 |
| 布尔值 | 开关/复选框 | 是否启用、是否推荐 |
| 文本域 | 多行文本输入 | 备注、描述、地址 |
| 富文本 | 富文本编辑器 | 详情、公告内容 |
| 图片 | 图片上传 | 封面图、头像 |
| 文件 | 文件上传 | 附件、证书 |
| 部门 | 部门树选择 | 所属部门 |
| 员工 | 员工选择 | 负责人、创建人 |

---

## ⚠️ 注意事项

### 1. 模块分类
根据业务性质，将模块放在合适的分类下：
- **business/** - 业务模块（客户、订单、产品等）
- **support/** - 支撑模块（字典、文件、日志等）
- **system/** - 系统模块（用户、角色、权限等）

### 2. 权限命名
权限命名必须遵循格式：`{模块分类}:{模块名}:{操作}`
```javascript
// 正确
business:customer:add
business:product:update
support:file:upload

// 错误
customer:add          // 缺少模块分类
business-customer:add // 使用了短横线
```

### 3. 字段验证
描述字段时，尽量提供详细的验证要求：
```
❌ 不好的描述：
- 手机号：字符串

✅ 好的描述：
- 手机号：字符串，必填，手机号格式验证
```

### 4. 枚举定义
如果有枚举类型字段，建议提供枚举值：
```
❌ 不好的描述：
- 状态：枚举

✅ 好的描述：
- 状态：枚举（1-启用，0-禁用），必填
```

---

## 🎓 学习资源

### 查看已有模块
项目中已有很多模块可以作为参考：
- 员工管理：`src/views/system/employee/`
- 角色管理：`src/views/system/role/`
- 部门管理：`src/views/system/department/`

### 核心文件位置
- API 封装：`src/lib/axios.js`
- 通用常量：`src/constants/common-const.js`
- 本地存储：`src/utils/local-util.js`
- 错误监控：`src/lib/smart-sentry.js`

### 项目组件
- 枚举选择：`/@/components/framework/smart-enum-select`
- 文件上传：`/@/components/support/file-upload`
- 富文本：`/@/components/framework/wangeditor`
- 部门选择：`/@/components/system/department-tree-select`
- 员工选择：`/@/components/system/employee-select`

---

## 📞 问题反馈

如果遇到问题或有改进建议：
1. 检查生成的代码是否符合检查清单
2. 确认需求描述是否清晰完整
3. 查看指令文档中的示例代码
4. 参考项目中已有的类似模块

---

## 📄 相关文档

- **AI二次开发指令文档.md** - 完整的技术规范和代码模板
- **vue3-admin-system-prompt.md** - Vue 3 系统提示词（如果有）
- **README.md** - 项目说明文档

---

## ✅ 使用检查清单

在生成代码后，检查以下事项：

**文件结构**
- [ ] API 文件位置正确
- [ ] 常量文件位置正确
- [ ] 视图文件位置正确
- [ ] 组件文件位置正确

**命名规范**
- [ ] 文件名使用 kebab-case
- [ ] 组件名使用 PascalCase
- [ ] API 对象名使用 camelCase
- [ ] 常量名使用 UPPER_SNAKE_CASE

**代码质量**
- [ ] 所有文件包含文件头注释
- [ ] 导入路径使用 /@/ 别名
- [ ] 权限指令正确使用
- [ ] 错误处理使用 smartSentry
- [ ] 表单验证完整

**功能完整性**
- [ ] 增删改查功能齐全
- [ ] 查询条件正确
- [ ] 分页功能正常
- [ ] 表单验证完整
- [ ] 删除有二次确认

---

## 🎉 开始使用吧！

现在你可以：
1. 打开 `AI二次开发指令文档.md`
2. 复制文档内容发送给AI
3. 描述你的需求
4. 等待AI生成代码
5. 将代码复制到项目中
6. 运行测试

祝开发顺利！🚀

---

**更新日期**：2025-11-03  
**版本**：1.0.0  
**项目**：SmartAdmin 3.X

