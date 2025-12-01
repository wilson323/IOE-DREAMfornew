# 统一鉴权中间层设计（RAC + DataScope）

## 目标
在不侵入业务 Controller 的前提下，提供统一的资源-动作-条件（RAC）与数据域（DataScope）判定能力，覆盖门禁/考勤/消费三大业务模块的 API 访问与数据权限控制。

## 组件
1) AuthorizationContext
- 责任：承载当前请求上下文（登录用户、角色、资源码、作用域、组织/区域信息、租户、客户端信息等）
- 主要字段（示例）：
  - userId: Long
  - roleCodes: Set<String>
  - resourceCode: String
  - requestedAction: String
  - dataScope: String  // AREA | DEPT | SELF | CUSTOM
  - areaIds: Set<Long>
  - deptIds: Set<Long>
  - attributes: Map<String, Object>

2) PolicyEvaluator
- 责任：基于统一资源码与动作，匹配角色-资源策略以及条件（RAC），得出允许/拒绝
- 输入：AuthorizationContext
- 输出：PolicyDecision { allowed: boolean; reason: String; conditionMatched: boolean }
- 行为：支持组合策略、条件表达式（如 JSON 条件）、审计打点

3) DataScopeResolver
- 责任：根据 dataScope 解析数据域过滤条件（如区域层级、部门范围、本人维度、自定义集合）
- 输入：AuthorizationContext
- 输出：DataScopeDecision { filterType, filterArgs, sqlSegments or predicate }
- 行为：与 Area path/level/path_hash 索引协同，生成高效过滤

4) 注解与拦截
- 注解：@RequireResource(code = "area:view", scope = DataScope.AREA, action = "READ")
- 拦截链：Spring MVC HandlerInterceptor / AOP
  - 解析注解 → 构建 AuthorizationContext → PolicyEvaluator.evaluate → DataScopeResolver.resolve
  - 拒绝则抛出统一异常（对接统一异常处理）
  - 通过则在 RequestAttributes 中注入数据域过滤信息供 DAO 层或 Service 使用

## 交互流程
1. Controller 方法标注 @RequireResource
2. 拦截器从 Sa-Token/session 中解析登录信息，补齐角色/区域/部门
3. PolicyEvaluator 根据 resourceCode + action 检查角色-资源映射（t_rbac_role_resource）
4. DataScopeResolver 根据 scope 生成过滤条件（结合 t_area_info 的 path/level/path_hash）
5. 将过滤条件透传到 Service/DAO（MyBatis-Plus Wrapper 组装或启用数据权限插件）

## 集成点
- sa-support：放置 AuthorizationContext、PolicyEvaluator、DataScopeResolver、注解与拦截器、统一异常/审计
- sa-admin：业务 Controller 上使用注解；Service/DAO 读取数据域上下文应用过滤

## 失败与审计
- 所有拒绝决策写入审计日志（资源码、动作、用户、原因、上下文摘要）
- 异常统一转为 ResponseDTO 错误码：AUTH_DENIED / SCOPE_VIOLATION / RESOURCE_NOT_FOUND

## 兼容性
- 不改变既有登录/会话实现；逐步替换各 Controller 内散落的权限判断

## 验证
- 覆盖率目标：策略判定与数据域决策测试 ≥ 80%
- 用例：跨区域拒绝、本人数据通过、角色-资源缺失拒绝、条件匹配失败拒绝


