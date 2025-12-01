# Change: repowiki-compliance-enhancement

## Why
基于repowiki规范体系的深度分析，发现当前IOE-DREAM项目存在一级规范违规（javax包使用）、二级规范不完善（缓存服务实现），以及核心业务功能模块缺失（智能视频监控、企业OA系统），需要进行全面的规范合规性增强和功能模块实现。

## What Changes
- **修复一级规范违规**：将所有javax包替换为jakarta包，确保Spring Boot 3.x编译通过
- **完善二级规范**：实现完整的缓存架构，修复日志使用违规
- **新增智能视频监控系统**：实现视频预览、回放、目标搜索、人脸识别等核心功能
- **完善企业OA系统**：实现工作流引擎、文档管理、审批流程等办公功能
- **增强缓存架构**：实现多级缓存，提升系统性能和响应速度
- **标准化数据库设计**：统一字段命名规范，优化数据模型

**Breaking Changes**:
- 修改javax包导入可能影响部分工具类兼容性
- 新增数据库表结构需要数据库迁移脚本
- 缓存架构变更可能影响现有缓存逻辑

## Impact
- **影响的规格**: smart-access, smart-device, smart-permission, smart-realtime（增强功能）
- **新增规格**: smart-video-surveillance, smart-enterprise-oa, smart-cache-architecture
- **影响的代码**:
  - 加密工具类 (SM2Cipher.java, SM3Digest.java, SM4Cipher.java)
  - 数据库配置类 (DataSourceConfig.java)
  - 缓存服务实现 (CacheService.java)
  - 所有业务模块（新增功能集成）
- **部署影响**: 需要数据库迁移和缓存预热