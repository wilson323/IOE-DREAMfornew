# 消费业务逻辑重构 - 规格说明

**Capability**: consume-business-logic
**Change ID**: consume-module-complete-implementation
**类型**: 业务逻辑升级

---

## ADDED Requirements

### Requirement: 6种消费模式支持

系统MUST支持6种消费模式，每种模式有独立的计算逻辑和配置

**优先级**: P0

#### Scenario: 固定金额模式（FIXED_AMOUNT）

**Given** 账户类别配置为FIXED_AMOUNT模式
**And** mode_config包含SIMPLE子类型，金额25元
**When** 用户发起消费请求
**Then** 应扣款固定金额25.00元

#### Scenario: 固定金额模式 - KEYVALUE子类型

**Given** 账户类别配置为FIXED_AMOUNT-KEYVALUE模式
**And** mode_config包含不同餐别的金额映射
**When** 用户在午餐时段消费
**Then** 应扣款午餐对应的金额（如25元）

#### Scenario: 固定金额模式 - SECTION子类型

**Given** 账户类别配置为FIXED_AMOUNT-SECTION模式
**And** mode_config包含时段区间金额配置
**When** 用户在12:00消费（匹配11:00-13:00时段）
**Then** 应扣款该时段对应的金额

#### Scenario: 自由金额模式（FREE_AMOUNT）

**Given** 账户类别配置为FREE_AMOUNT模式
**When** 用户输入消费金额35.50元
**Then** 应扣款35.50元（用户输入金额）
**And** 金额不应超过maxAmount限制

#### Scenario: 计量计费模式（METERED）- TIMING子类型

**Given** 账户类别配置为METERED-TIMING模式
**And** mode_config包含单价0.50元/分钟
**When** 用户消费时长130分钟
**Then** 应扣款65.00元（130 * 0.50）

#### Scenario: 计量计费模式（METERED）- COUNTING子类型

**Given** 账户类别配置为METERED-COUNTING模式
**And** mode_config包含单价2.00元/单位
**When** 用户消费18个单位
**Then** 应扣款36.00元（18 * 2.00）

#### Scenario: 商品模式（PRODUCT）

**Given** 账户类别配置为PRODUCT模式
**When** 用户购买商品A（15元）和商品B（8.50元）
**Then** 应扣款23.50元（商品价格之和）

#### Scenario: 订餐模式（ORDER）

**Given** 账户类别配置为ORDER模式
**When** 用户提前1天订餐
**Then** 应创建订餐记录
**And** 扣款时间配置为取餐时段

#### Scenario: 智能模式（INTELLIGENCE）

**Given** 账户类别配置为INTELLIGENCE模式
**When** 用户请求智能推荐
**Then** 应基于历史消费推荐菜品
**And** 返回推荐菜品列表（按概率排序）

---

### Requirement: 3种区域经营模式

系统MUST支持3种区域经营模式：餐别制、超市制、混合模式

**优先级**: P0

#### Scenario: 餐别制模式（manage_mode=1）

**Given** 区域配置为餐别制（manage_mode=1）
**And** 当前时间为12:30（午餐时段）
**When** 用户在该区域消费
**Then** 应检查用户是否有午餐消费权限
**And** 应扣款午餐定值金额

#### Scenario: 超市制模式（manage_mode=2）

**Given** 区域配置为超市制（manage_mode=2）
**When** 用户在该区域消费
**Then** 不检查餐别权限
**And** 支持多次消费
**And** 按实际商品金额扣款

#### Scenario: 混合模式（manage_mode=3）

**Given** 区域配置为混合模式（manage_mode=3）
**And** 该区域包含"餐吧"和"超市"两个子区域
**When** 用户在"餐吧"消费
**Then** 按餐别制规则处理
**When** 用户在"超市"消费
**Then** 按超市制规则处理

---

### Requirement: 定值计算优先级系统

定值金额计算MUST遵循三级优先级：账户类别 > 区域 > 全局默认

**优先级**: P0

#### Scenario: 账户类别mode_config优先级最高

**Given** 账户类别mode_config配置午餐25元
**And** 区域fixed_value_config配置午餐20元
**And** 全局默认配置午餐15元
**When** 用户在午餐时段消费
**Then** 应使用账户类别配置：25元（最高优先级）

#### Scenario: 区域fixed_value_config优先级中等

**Given** 账户类别mode_config未配置午餐定值
**And** 区域fixed_value_config配置午餐20元
**And** 全局默认配置午餐15元
**When** 用户在午餐时段消费
**Then** 应使用区域配置：20元（中等优先级）

#### Scenario: 全局默认配置优先级最低

**Given** 账户类别和区域均未配置午餐定值
**And** 全局默认配置午餐15元
**When** 用户在午餐时段消费
**Then** 应使用全局默认：15元（最低优先级）

---

### Requirement: 区域权限验证

MUST验证用户是否有权在指定区域消费

**优先级**: P0

#### Scenario: 区域权限验证通过

**Given** 用户账户类别配置了区域权限
**And** area_config包含main_campus，includeSubAreas=true
**When** 用户在"main_campus"或其子区域消费
**Then** 权限验证通过
**And** 允许消费

#### Scenario: 区域权限验证失败

**Given** 用户账户类别配置了区域权限
**And** area_config仅包含"main_campus"
**When** 用户在"branch_campus"消费
**Then** 权限验证失败
**And** 返回"无区域权限"错误

#### Scenario: 子区域权限继承

**Given** 用户账户类别配置了主校区权限
**And** area_config中includeSubAreas=true
**And** 主校区包含：A栋、B栋、C栋
**When** 用户在"A栋"消费
**Then** 权限验证通过（继承自主校区）

---

### Requirement: 消费流程完整性

标准消费流程MUST包含7个步骤

**优先级**: P0

#### Scenario: 标准消费流程 - 7步完整执行

**Given** 用户发起消费请求
**When** 执行消费流程
**Then** 应依次执行以下步骤：
1. 权限验证
2. 定值计算
3. 余额检查
4. 扣款处理
5. 记录生成
6. 通知推送
7. 日志记录
**And** 任一步骤失败时回滚

#### Scenario: 消费流程异常回滚

**Given** 用户发起消费请求
**When** 执行到第4步"扣款处理"时失败
**Then** 应回滚已执行的操作
**And** 返回"消费失败"错误
**And** 记录异常日志

---

## MODIFIED Requirements

### Requirement: ConsumeController接口适配

MUST 现有消费接口需适配新的6种消费模式

**优先级**: P0

#### Scenario: 适配6种消费模式接口

**Given** ConsumeController存在消费接口
**When** 用户发起消费请求，携带consumeMode参数
**Then** 应根据consumeMode路由到对应的策略
**And** 返回统一的消费结果格式

#### Scenario: 保持API向后兼容

**Given** 前端使用旧的消费接口（不指定consumeMode）
**When** 发起消费请求
**Then** 应使用默认的FIXED_AMOUNT模式
**And** 返回格式保持不变
**And** 前端无需修改代码

---

## 验收标准

### 功能验收
- [ ] 6种消费模式全部实现并可配置
- [ ] 3种区域经营模式全部实现
- [ ] 定值计算三级优先级正确工作
- [ ] 区域权限验证完整
- [ ] 标准消费流程7步完整执行

### 性能验收
- [ ] 消费接口平均响应时间 ≤ 50ms
- [ ] 定值计算逻辑执行时间 ≤ 10ms
- [ ] 权限验证执行时间 ≤ 5ms

---

**版本**: v1.0
**最后更新**: 2025-12-23
