# 模块验收检查清单（设备/人事/考勤/门禁/消费）

## 架构与规范
- [ ] 四层架构：Controller → Service（事务） → Manager → DAO
- [ ] 依赖注入使用 @Resource，无 @Autowired
- [ ] 包名使用 jakarta.* 而非 javax.*
- [ ] Controller 不直达 DAO
- [ ] 统一返回 ResponseDTO
- [ ] 关键接口添加 @RequireResource 和 DataScope

## 数据库与迁移
- [ ] 各模块表结构具备主键、审计字段、软删除、乐观锁
- [ ] Flyway 迁移脚本齐全，版本与变更可追溯
- [ ] 索引与唯一约束齐全

## API 契约与文档
- [ ] Swagger 注释完整
- [ ] docs/IMPLEMENTATION_PLAN/api-contracts.md 已更新
- [ ] RBAC 资源脚本已补充（docs/IMPLEMENTATION_PLAN/rbac-resources.sql）

## 质量门禁
- [ ] Windows PowerShell 质量脚本 scripts/quality-gates.ps1 通过
- [ ] 禁止项扫描：javax.* / @Autowired = 0
- [ ] 权限注解覆盖率检查无明显缺口
- [ ] Mapper 一致性：自定义 Dao 存在对应 XML 或确认使用 MyBatis-Plus 通用 CRUD

## 安全与日志
- [ ] 统一异常、审计日志
- [ ] 无敏感信息硬编码

## 运行验证
- [ ] 本地可启动并访问模块核心接口
- [ ] 关键用例手动回归成功

## 规范引用（repowiki）
- [ ] 已对齐 docs/repowiki/zh/content/开发规范体系/API设计规范.md
- [ ] 已对齐 docs/repowiki/zh/content/开发规范体系/系统安全规范.md
- [ ] 已对齐 docs/repowiki/zh/content/开发规范体系/代码质量标准.md
- [ ] 已对齐 docs/repowiki/zh/content/开发规范体系/Java编码规范.md


