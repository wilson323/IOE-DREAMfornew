# Maven依赖架构优化分析报告

**生成时间**: 2025-12-22 21:47:12
**扫描范围**: 6 个业务微服务

## 问题统计

- **总问题数**: 19
- **需修复服务**: 6 个

## 详细分析结果
### ioedream-access-service

- **问题数**: 1
- **问题详情**:
  ❌ 重复依赖组: resilience4j (resilience4j-spring-boot3, resilience4j-retry, resilience4j-circuitbreaker, resilience4j-ratelimiter, resilience4j-micrometer)
### ioedream-attendance-service

- **问题数**: 1
- **问题详情**:
  ❌ 重复依赖组: resilience4j (resilience4j-spring-boot3, resilience4j-retry, resilience4j-circuitbreaker, resilience4j-ratelimiter, resilience4j-micrometer)
### ioedream-consume-service

- **问题数**: 10
- **问题详情**:
  ❌ 重复依赖组: resilience4j (resilience4j-spring-boot3, resilience4j-retry, resilience4j-circuitbreaker)
  ⚠️ 可移除版本: core
  ⚠️ 可移除版本: javase
  ⚠️ 可移除版本: jackson-databind
  ⚠️ 可移除版本: alipay-sdk-java
  ⚠️ 可移除版本: wechatpay-java
  ⚠️ 可移除版本: easyexcel
  ⚠️ 可移除版本: kernel
  ⚠️ 可移除版本: layout
  ⚠️ 可移除版本: io
### ioedream-video-service

- **问题数**: 1
- **问题详情**:
  ❌ 重复依赖组: resilience4j (resilience4j-spring-boot3, resilience4j-circuitbreaker)
### ioedream-visitor-service

- **问题数**: 2
- **问题详情**:
  ❌ 重复依赖组: resilience4j (resilience4j-spring-boot3, resilience4j-retry, resilience4j-circuitbreaker, resilience4j-ratelimiter, resilience4j-micrometer)
  ⚠️ 可移除版本: tencentcloud-sdk-java-ocr
### ioedream-device-comm-service

- **问题数**: 4
- **问题详情**:
  ❌ 重复依赖组: resilience4j (resilience4j-spring-boot3, resilience4j-retry)
  ⚠️ 可移除版本: caffeine
  ⚠️ 可移除版本: swagger-annotations
  ⚠️ 可移除版本: jakarta.validation-api
## 优化建议

### 1. 版本管理优化
- 移除业务服务中的硬编码版本声明
- 统一使用父POM的dependencyManagement管理版本
- 保留${project.version}用于内部模块依赖

### 2. 重复依赖清理
- 合并Resilience4j依赖声明
- 使用单一依赖替代多个相关依赖
- 清理不必要的测试依赖重复声明

### 3. 依赖声明标准化
`xml
<!-- ✅ 正确的依赖声明模式 -->
<dependency>
  <groupId>com.baomidou</groupId>
  <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
  <!-- 版本由父POM的dependencyManagement管理，无需声明版本 -->
</dependency>

<!-- ❌ 错误的依赖声明模式 -->
<dependency>
  <groupId>com.baomidou</groupId>
  <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
  <version></version>  <!-- 冗余的版本声明 -->
</dependency>
`

### 4. 构建顺序验证
当前构建顺序符合依赖层次要求：
1. microservices-common-core (第1层)
2. microservices-common-entity (第1层)
3. 细粒度模块 (第2层)
4. 业务服务 (第3层)

## 下一步行动
1. 手动修复每个服务的POM文件
2. 验证Maven编译通过
3. 运行完整构建测试