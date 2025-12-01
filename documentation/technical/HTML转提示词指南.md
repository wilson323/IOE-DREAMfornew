# HTML 代码转 AI 提示词指南

> **目的**: 将已有的 HTML 代码转换成标准的 AI 提示词，让 AI 按照 SmartAdmin 规范重新生成代码

---

## 📋 目录

1. [转换流程](#1-转换流程)
2. [信息提取方法](#2-信息提取方法)
3. [提示词生成模板](#3-提示词生成模板)
4. [完整转换示例](#4-完整转换示例)
5. [常见场景转换](#5-常见场景转换)

---

## 1. 转换流程

### 步骤总览

```
HTML 代码
    ↓
分析页面结构
    ↓
提取关键信息
    ↓
填写提示词模板
    ↓
提供给 AI
    ↓
生成符合规范的 Vue 3 代码
```

### 详细步骤

**第一步：分析 HTML 代码**
- 识别页面类型（列表页、表单页、详情页、Dashboard等）
- 找出关键元素（表格、表单、图表、卡片等）
- 记录页面功能（增删改查、筛选、导出等）

**第二步：提取信息**
- 提取字段列表及属性
- 提取功能权限
- 提取特殊需求

**第三步：生成提示词**
- 使用标准模板
- 填写提取的信息
- 补充技术要求

**第四步：交给 AI**
- 附带《AI二次开发指令文档》
- 提供生成的提示词
- AI 自动生成规范代码

---

## 2. 信息提取方法

### 2.1 提取页面类型

**识别方法：**

| HTML 特征 | 页面类型 | 提示词描述 |
|----------|---------|-----------|
| `<table>` + 搜索框 + 按钮 | 列表页 | 标准的增删改查列表页 |
| `<form>` 表单为主 | 表单页 | 表单提交页面 |
| 多个 `<div class="card">` | Dashboard | 数据可视化看板 |
| `<canvas>` 或图表库 | 图表页 | 包含图表的页面 |
| 分步骤结构 | 多步骤表单 | 向导式表单页面 |
| 主表 + 明细表 | 主从表页面 | 订单详情类页面 |

### 2.2 提取字段信息

**从 HTML 表单中提取：**

```html
<!-- 示例 HTML -->
<form>
  <div class="form-group">
    <label>产品名称</label>
    <input type="text" name="productName" required maxlength="50">
  </div>
  
  <div class="form-group">
    <label>价格</label>
    <input type="number" name="price" required min="0" step="0.01">
  </div>
  
  <div class="form-group">
    <label>状态</label>
    <select name="status">
      <option value="1">上架</option>
      <option value="0">下架</option>
    </select>
  </div>
  
  <div class="form-group">
    <label>描述</label>
    <textarea name="description" rows="4"></textarea>
  </div>
</form>
```

**提取结果：**

```
字段列表：
- 产品名称：字符串，必填，最长50字符
- 价格：数字，必填，大于等于0，保留2位小数
- 状态：枚举（1-上架，0-下架），必填
- 描述：文本域，非必填
```

### 2.3 提取表格列信息

**从 HTML 表格中提取：**

```html
<!-- 示例 HTML -->
<table>
  <thead>
    <tr>
      <th>产品名称</th>
      <th>分类</th>
      <th>价格</th>
      <th>库存</th>
      <th>状态</th>
      <th>创建时间</th>
      <th>操作</th>
    </tr>
  </thead>
  <tbody>
    <!-- 数据行 -->
  </tbody>
</table>
```

**提取结果：**

```
表格列：
- 产品名称
- 分类
- 价格
- 库存
- 状态
- 创建时间
- 操作（编辑、删除）
```

### 2.4 提取功能按钮

**从 HTML 按钮中提取：**

```html
<!-- 示例 HTML -->
<div class="button-group">
  <button class="btn-add">添加产品</button>
  <button class="btn-export">导出</button>
  <button class="btn-import">导入</button>
  <button class="btn-delete">批量删除</button>
</div>
```

**提取结果：**

```
操作权限：
- business:product:add（添加产品）
- business:product:export（导出）
- business:product:import（导入）
- business:product:delete（批量删除）
```

### 2.5 提取搜索条件

**从搜索表单中提取：**

```html
<!-- 示例 HTML -->
<div class="search-form">
  <input type="text" placeholder="产品名称">
  <select>
    <option>全部分类</option>
    <option>电子产品</option>
    <option>服装</option>
  </select>
  <select>
    <option>全部状态</option>
    <option>上架</option>
    <option>下架</option>
  </select>
  <input type="date" placeholder="开始日期">
  <input type="date" placeholder="结束日期">
  <button>搜索</button>
  <button>重置</button>
</div>
```

**提取结果：**

```
搜索条件：
- 产品名称：文本输入
- 产品分类：下拉选择
- 状态：下拉选择
- 日期范围：日期范围选择
```

---

## 3. 提示词生成模板

### 3.1 标准列表页模板

```
请基于 SmartAdmin 项目为我开发一个【{模块名称}】功能，需求如下：

1. 功能描述：
   - 管理{业务对象}信息
   - 支持{业务对象}的增删改查
   - 支持按{条件1}、{条件2}搜索
   - 支持{其他功能}

2. 字段列表：
   - {字段1}：{类型}，{是否必填}，{验证规则}
   - {字段2}：{类型}，{是否必填}，{验证规则}
   - {字段3}：{类型}，{是否必填}，{验证规则}
   ...

3. 表格列：
   - {列1}
   - {列2}
   - {列3}
   ...
   - 操作（{操作1}、{操作2}）

4. 搜索条件：
   - {条件1}：{控件类型}
   - {条件2}：{控件类型}
   ...

5. 操作权限：
   - {模块分类}:{模块名}:add（添加）
   - {模块分类}:{模块名}:update（编辑）
   - {模块分类}:{模块名}:delete（删除）
   - {模块分类}:{模块名}:query（查询）
   - {模块分类}:{模块名}:export（导出）

6. 特殊需求：
   - {特殊需求1}
   - {特殊需求2}
   ...
```

### 3.2 Dashboard 页面模板

```
请基于 SmartAdmin 项目开发一个【{页面名称}】页面，需求如下：

1. 页面布局：
   - 顶部：{描述顶部布局}
   - 中部：{描述中部布局}
   - 底部：{描述底部布局}

2. 统计卡片：
   - {卡片1标题}：{数据类型}，{趋势显示}
   - {卡片2标题}：{数据类型}，{趋势显示}
   ...

3. 图表需求：
   - {图表1名称}：{图表类型}，{数据维度}
   - {图表2名称}：{图表类型}，{数据维度}
   ...

4. 数据接口：
   - 获取{数据1}
   - 获取{数据2}
   ...

5. 交互功能：
   - 支持{功能1}
   - 支持{功能2}
   ...

6. 技术要求：
   - 图表使用 ECharts
   - 组件化拆分
   - 遵循项目规范
```

### 3.3 多步骤表单模板

```
请基于 SmartAdmin 项目开发一个【{功能名称}】多步骤表单，需求如下：

1. 步骤流程：
   - 步骤1：{步骤1描述}
   - 步骤2：{步骤2描述}
   - 步骤3：{步骤3描述}
   - 步骤4：{步骤4描述}

2. 各步骤字段：
   
   步骤1字段：
   - {字段1}：{类型}，{验证规则}
   - {字段2}：{类型}，{验证规则}
   
   步骤2字段：
   - {字段3}：{类型}，{验证规则}
   - {字段4}：{类型}，{验证规则}
   ...

3. 交互要求：
   - 支持上一步、下一步
   - 每步验证通过才能进入下一步
   - 最后一步提交{业务对象}
   - 支持保存草稿

4. 操作权限：
   - {模块分类}:{模块名}:add（提交）
   - {模块分类}:{模块名}:draft（保存草稿）
```

### 3.4 主从表页面模板

```
请基于 SmartAdmin 项目开发一个【{业务对象}】主从表页面，需求如下：

1. 页面布局：
   - 上半部分：{主表名称}主表信息（{字段1}、{字段2}等）
   - 下半部分：{从表名称}明细表（{字段3}、{字段4}等）

2. 主表字段：
   - {字段1}：{类型}，{是否必填}，{验证规则}
   - {字段2}：{类型}，{是否必填}，{验证规则}
   ...

3. 明细表字段：
   - {字段1}：{类型}，{是否必填}，{验证规则}
   - {字段2}：{类型}，{是否必填}，{验证规则}
   ...

4. 功能要求：
   - 可以新增、编辑、删除明细
   - 自动计算{计算字段}
   - 支持保存草稿
   - 支持提交审核

5. 操作权限：
   - {模块分类}:{模块名}:add（添加）
   - {模块分类}:{模块名}:update（编辑）
   - {模块分类}:{模块名}:submit（提交审核）
```

---

## 4. 完整转换示例

### 示例1：列表页转换

**原始 HTML 代码：**

```html
<!DOCTYPE html>
<html>
<head>
    <title>产品管理</title>
</head>
<body>
    <div class="container">
        <!-- 搜索表单 -->
        <div class="search-form">
            <input type="text" placeholder="产品名称" id="productName">
            <select id="categoryId">
                <option value="">全部分类</option>
                <option value="1">电子产品</option>
                <option value="2">服装</option>
            </select>
            <select id="status">
                <option value="">全部状态</option>
                <option value="1">上架</option>
                <option value="0">下架</option>
            </select>
            <button onclick="search()">搜索</button>
            <button onclick="reset()">重置</button>
        </div>

        <!-- 操作按钮 -->
        <div class="button-group">
            <button onclick="addProduct()">添加产品</button>
            <button onclick="batchDelete()">批量删除</button>
            <button onclick="exportData()">导出</button>
        </div>

        <!-- 数据表格 -->
        <table id="productTable">
            <thead>
                <tr>
                    <th><input type="checkbox" id="selectAll"></th>
                    <th>产品名称</th>
                    <th>产品编码</th>
                    <th>分类</th>
                    <th>价格</th>
                    <th>库存</th>
                    <th>状态</th>
                    <th>创建时间</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <!-- 数据行 -->
            </tbody>
        </table>

        <!-- 分页 -->
        <div class="pagination">
            <!-- 分页组件 -->
        </div>
    </div>

    <!-- 表单弹窗 -->
    <div id="productModal" style="display:none;">
        <form id="productForm">
            <div class="form-group">
                <label>产品名称 *</label>
                <input type="text" name="productName" required maxlength="50">
            </div>
            <div class="form-group">
                <label>产品编码 *</label>
                <input type="text" name="productCode" required maxlength="30">
            </div>
            <div class="form-group">
                <label>产品分类 *</label>
                <select name="categoryId" required>
                    <option value="">请选择</option>
                    <option value="1">电子产品</option>
                    <option value="2">服装</option>
                </select>
            </div>
            <div class="form-group">
                <label>价格 *</label>
                <input type="number" name="price" required min="0" step="0.01">
            </div>
            <div class="form-group">
                <label>库存 *</label>
                <input type="number" name="stock" required min="0">
            </div>
            <div class="form-group">
                <label>状态 *</label>
                <select name="status" required>
                    <option value="1">上架</option>
                    <option value="0">下架</option>
                </select>
            </div>
            <div class="form-group">
                <label>产品描述</label>
                <textarea name="description" rows="4"></textarea>
            </div>
            <div class="form-group">
                <label>产品图片</label>
                <input type="file" name="imageUrl">
            </div>
        </form>
    </div>
</body>
</html>
```

**转换后的提示词：**

```
请基于 SmartAdmin 项目为我开发一个【产品管理】功能，需求如下：

1. 功能描述：
   - 管理产品信息
   - 支持产品的增删改查
   - 支持按产品名称、分类、状态搜索
   - 支持批量删除
   - 支持数据导出

2. 字段列表：
   - 产品名称：字符串，必填，最长50字符
   - 产品编码：字符串，必填，最长30字符，唯一
   - 产品分类：下拉选择，必填，枚举（1-电子产品，2-服装）
   - 价格：数字，必填，大于等于0，保留2位小数
   - 库存：整数，必填，大于等于0
   - 状态：枚举（1-上架，0-下架），必填
   - 产品描述：文本域，非必填，最长500字符
   - 产品图片：图片上传，非必填

3. 表格列：
   - 产品名称
   - 产品编码
   - 分类
   - 价格
   - 库存
   - 状态（显示标签，上架为绿色，下架为灰色）
   - 创建时间
   - 操作（编辑、删除）

4. 搜索条件：
   - 产品名称：文本输入
   - 产品分类：下拉选择（全部、电子产品、服装）
   - 状态：下拉选择（全部、上架、下架）

5. 操作权限：
   - business:product:add（添加产品）
   - business:product:update（编辑产品）
   - business:product:delete（删除产品）
   - business:product:query（查询产品）
   - business:product:export（导出产品）

6. 特殊需求：
   - 产品编码不可重复
   - 列表支持批量删除
   - 状态列显示标签（上架显示绿色success标签，下架显示灰色default标签）
   - 价格列右对齐，显示货币符号
   - 图片支持预览
```

### 示例2：Dashboard 转换

**原始 HTML 代码：**

```html
<!DOCTYPE html>
<html>
<head>
    <title>销售数据看板</title>
    <script src="echarts.min.js"></script>
</head>
<body>
    <!-- 日期筛选 -->
    <div class="filter">
        <input type="date" id="startDate">
        <input type="date" id="endDate">
        <button onclick="query()">查询</button>
    </div>

    <!-- 统计卡片 -->
    <div class="statistics">
        <div class="card">
            <h3>今日销售额</h3>
            <p class="value" id="todaySales">¥0</p>
            <p class="trend" id="salesTrend">环比 +0%</p>
        </div>
        <div class="card">
            <h3>订单数</h3>
            <p class="value" id="orderCount">0</p>
            <p class="trend" id="orderTrend">环比 +0%</p>
        </div>
        <div class="card">
            <h3>客户数</h3>
            <p class="value" id="customerCount">0</p>
            <p class="trend" id="customerTrend">环比 +0%</p>
        </div>
        <div class="card">
            <h3>转化率</h3>
            <p class="value" id="conversionRate">0%</p>
            <p class="trend" id="conversionTrend">环比 +0%</p>
        </div>
    </div>

    <!-- 图表 -->
    <div class="charts">
        <div class="chart-container">
            <h3>销售趋势</h3>
            <div id="trendChart" style="width:100%;height:400px;"></div>
        </div>
        <div class="chart-container">
            <h3>销售分类</h3>
            <div id="categoryChart" style="width:100%;height:400px;"></div>
        </div>
    </div>

    <!-- 排行榜 -->
    <div class="ranking">
        <h3>产品销售排行</h3>
        <table id="rankingTable">
            <thead>
                <tr>
                    <th>排名</th>
                    <th>产品名称</th>
                    <th>销售额</th>
                    <th>销售量</th>
                </tr>
            </thead>
            <tbody>
                <!-- 数据行 -->
            </tbody>
        </table>
    </div>
</body>
</html>
```

**转换后的提示词：**

```
请基于 SmartAdmin 项目开发一个【销售数据看板】页面，需求如下：

1. 页面布局：
   - 顶部：日期范围筛选器（默认显示近30天）
   - 第二行：4个统计卡片（今日销售额、订单数、客户数、转化率）
   - 第三行：左侧销售趋势折线图（占2/3宽度），右侧销售分类饼图（占1/3宽度）
   - 底部：产品销售排行榜表格（Top 10）

2. 统计卡片：
   - 今日销售额：金额类型，显示货币符号，显示环比趋势
   - 订单数：数字类型，显示单位"单"，显示环比趋势
   - 客户数：数字类型，显示单位"人"，显示环比趋势
   - 转化率：百分比类型，显示单位"%"，显示环比趋势

3. 图表需求：
   - 销售趋势图：折线图，X轴为日期，Y轴左侧为销售额，Y轴右侧为订单量
   - 销售分类图：饼图，显示各产品分类的销售占比

4. 排行榜表格：
   - 排名：序号
   - 产品名称
   - 销售额：右对齐，显示货币符号
   - 销售量

5. 数据接口：
   - 获取统计数据（包含4个卡片数据）
   - 获取趋势数据（近30天每日销售数据）
   - 获取分类数据（各分类销售占比）
   - 获取排行数据（Top 10产品）

6. 交互功能：
   - 支持日期范围筛选，默认近30天
   - 图表支持鼠标悬浮显示详情
   - 图表支持缩放和平移
   - 排行榜表格支持排序

7. 技术要求：
   - 图表使用 ECharts
   - 统计卡片拆分为独立组件
   - 趋势图拆分为独立组件
   - 分类图拆分为独立组件
   - 排行榜拆分为独立组件
   - 遵循项目规范
```

---

## 5. 常见场景转换

### 5.1 表单字段类型对照表

| HTML 元素 | Vue 3 组件 | 提示词描述 |
|----------|-----------|-----------|
| `<input type="text">` | `<a-input>` | 文本输入，字符串类型 |
| `<input type="number">` | `<a-input-number>` | 数字输入，数字类型 |
| `<input type="email">` | `<a-input>` | 邮箱输入，邮箱格式验证 |
| `<input type="tel">` | `<a-input>` | 手机号输入，手机号格式验证 |
| `<input type="password">` | `<a-input type="password">` | 密码输入 |
| `<input type="date">` | `<a-date-picker>` | 日期选择 |
| `<input type="checkbox">` | `<a-checkbox>` | 复选框，布尔类型 |
| `<input type="radio">` | `<a-radio-group>` | 单选框，枚举类型 |
| `<select>` | `<a-select>` | 下拉选择，枚举类型 |
| `<textarea>` | `<a-textarea>` | 文本域，多行文本 |
| `<input type="file">` | `<FileUpload>` | 文件上传 |
| 富文本编辑器 | `<WangEditor>` | 富文本输入 |

### 5.2 验证规则对照表

| HTML 属性 | 提示词描述 | Vue 3 规则 |
|----------|-----------|-----------|
| `required` | 必填 | `{ required: true, message: '请输入XXX' }` |
| `maxlength="50"` | 最长50字符 | `{ max: 50, message: '最长50字符' }` |
| `minlength="6"` | 最短6字符 | `{ min: 6, message: '最短6字符' }` |
| `min="0"` | 大于等于0 | `{ type: 'number', min: 0 }` |
| `max="100"` | 小于等于100 | `{ type: 'number', max: 100 }` |
| `pattern="..."` | 正则验证 | `{ pattern: /.../, message: '格式不正确' }` |
| `type="email"` | 邮箱格式 | `{ type: 'email', message: '邮箱格式不正确' }` |

### 5.3 状态显示对照表

| HTML 显示 | 提示词描述 | Vue 3 实现 |
|----------|-----------|-----------|
| `<span class="badge-success">启用</span>` | 状态标签，启用显示绿色 | `<a-tag color="success">启用</a-tag>` |
| `<span class="badge-danger">禁用</span>` | 状态标签，禁用显示红色 | `<a-tag color="error">禁用</a-tag>` |
| `<span class="text-success">+10%</span>` | 趋势箭头，上涨显示绿色 | 使用 `<ArrowUpOutlined>` 图标 |
| `<span class="text-danger">-5%</span>` | 趋势箭头，下跌显示红色 | 使用 `<ArrowDownOutlined>` 图标 |

---

## 6. 快速转换工具表

### 分析检查表

在转换前，使用此检查表确保信息提取完整：

- [ ] 页面类型已识别
- [ ] 所有字段已提取
- [ ] 字段类型已确认
- [ ] 必填/非必填已标记
- [ ] 验证规则已记录
- [ ] 表格列已列出
- [ ] 搜索条件已提取
- [ ] 操作按钮已记录
- [ ] 权限标识已定义
- [ ] 特殊需求已说明
- [ ] 模块分类已确定（business/support/system）

### 提示词检查表

生成提示词后，使用此检查表验证完整性：

- [ ] 功能描述清晰完整
- [ ] 字段列表包含所有字段
- [ ] 每个字段都有类型说明
- [ ] 每个字段都有必填说明
- [ ] 验证规则详细明确
- [ ] 表格列完整
- [ ] 搜索条件明确
- [ ] 权限命名符合规范（模块分类:模块名:操作）
- [ ] 特殊需求描述清楚
- [ ] 附带了《AI二次开发指令文档》

---

## 7. 实用技巧

### 技巧1：分批转换

如果页面很复杂，可以分批转换：

```
第一步：先转换列表页主体
第二步：再转换表单弹窗
第三步：最后转换特殊功能
```

### 技巧2：使用注释标记

在分析HTML时，可以添加注释标记关键信息：

```html
<!-- 【字段】产品名称：字符串，必填，最长50字符 -->
<input type="text" name="productName" required maxlength="50">

<!-- 【字段】价格：数字，必填，大于等于0，保留2位小数 -->
<input type="number" name="price" required min="0" step="0.01">
```

### 技巧3：保留原HTML作为参考

在提示词中可以附加原HTML的关键部分：

```
请基于 SmartAdmin 项目开发一个【产品管理】功能...

参考原HTML结构：
- 搜索表单：包含产品名称、分类、状态筛选
- 操作按钮：添加、批量删除、导出
- 数据表格：8列（选择框、产品名称、编码、分类、价格、库存、状态、操作）
- 表单弹窗：8个字段
```

### 技巧4：明确数据关联

如果有下拉选择，明确数据来源：

```
产品分类：下拉选择，数据从分类管理模块获取
供应商：下拉选择，数据从供应商管理模块获取
```

---

## 8. 完整工作流程示例

### 场景：将一个旧的产品管理页面转换为SmartAdmin规范代码

**步骤1：准备工作**
```
1. 打开原HTML文件
2. 创建一个文本文档用于记录提取的信息
3. 打开《AI二次开发指令文档》
```

**步骤2：信息提取**
```
1. 识别页面类型：标准列表页
2. 提取字段信息：8个字段
3. 提取表格列：8列
4. 提取搜索条件：3个条件
5. 提取操作按钮：5个按钮
6. 确定权限命名：business:product:xxx
```

**步骤3：填写提示词**
```
使用模板填写完整的提示词
检查信息完整性
补充特殊需求
```

**步骤4：提供给AI**
```
1. 复制《AI二次开发指令文档》全文
2. 粘贴生成的提示词
3. 说明：请严格按照文档规范生成代码
```

**步骤5：验证代码**
```
1. 检查生成的文件结构
2. 检查命名规范
3. 检查代码组织
4. 测试功能完整性
```

---

## 9. 常见问题

### Q1：HTML中没有明确的必填标记怎么办？
**A**：根据业务逻辑判断，一般主键、名称、状态等字段为必填，描述、备注等为非必填。

### Q2：HTML中的JavaScript业务逻辑要转换吗？
**A**：不需要转换JavaScript代码，只需要描述功能需求，AI会重新实现。

### Q3：原HTML使用的是Bootstrap，能转换吗？
**A**：可以，只提取功能和数据结构，UI会使用Ant Design Vue重新实现。

### Q4：表单很复杂，有几十个字段怎么办？
**A**：可以分组描述，或者只列出核心字段，其他字段简化说明。

### Q5：原页面有自定义的特殊组件怎么办？
**A**：在"特殊需求"中详细描述该组件的功能和交互，AI会用标准组件实现或提供建议。

---

## 10. 转换模板速查

### 快速模板（简化版）

```
请基于 SmartAdmin 为我开发【{模块名}】功能：

字段：
- {字段1}：{类型}，{必填}
- {字段2}：{类型}，{必填}

功能：
- {功能1}
- {功能2}

权限：
- {权限1}
- {权限2}
```

### 标准模板（完整版）

参考第3章的详细模板。

---

## 结语

通过本指南，你可以快速将任何HTML代码转换成AI可理解的提示词，让AI按照SmartAdmin规范重新生成高质量的Vue 3代码。

**核心要点**：
- ✅ 提取信息要完整
- ✅ 描述需求要清晰
- ✅ 权限命名要规范
- ✅ 附带指令文档

**记住**：不要纠结于原HTML的实现细节，重点是业务需求和数据结构！

---

**相关文档**：
- AI二次开发指令文档.md
- 使用说明-README.md

