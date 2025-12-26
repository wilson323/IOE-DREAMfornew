# IOE-DREAM 全局依赖深度分析报告

**生成时间**: 2025-12-20 23:20:39
**分析范围**: 17 个模块
**版本属性**: 42 个

---

## 📊 执行摘要

- **硬编码版本问题**: 26 个
- **缺失版本属性**: 85 个
- **核心模块问题**: 2 个

---

## 🔍 详细分析结果

### 1. 硬编码版本问题

以下依赖使用了硬编码版本号，应该改为使用父POM的properties引用：

| 模块 | GroupId | ArtifactId | 当前版本 | 文件路径 |
|------|---------|------------|---------|----------|
| ioedream-biometric-service | org.openpnp | opencv | 4.5.1-2 | microservices\ioedream-biometric-service\pom.xml |
| ioedream-common-service | com.aliyun | dysmsapi20170525 | 4.3.0 | microservices\ioedream-common-service\pom.xml |
| ioedream-consume-service | org.springdoc | springdoc-openapi-starter-webmvc-ui | 2.2.0 | microservices\ioedream-consume-service\pom.xml |
| ioedream-device-comm-service | io.github.resilience4j | resilience4j-spring-boot3 | 2.1.0 | microservices\ioedream-device-comm-service\pom.xml |
| ioedream-device-comm-service | io.github.resilience4j | resilience4j-retry | 2.1.0 | microservices\ioedream-device-comm-service\pom.xml |
| ioedream-device-comm-service | io.github.resilience4j | resilience4j-timelimiter | 2.1.0 | microservices\ioedream-device-comm-service\pom.xml |
| ioedream-device-comm-service | org.hibernate.validator | hibernate-validator | 8.0.1.Final | microservices\ioedream-device-comm-service\pom.xml |
| ioedream-device-comm-service | org.apache.commons | commons-pool2 | 2.12.0 | microservices\ioedream-device-comm-service\pom.xml |
| ioedream-oa-service | org.flowable | flowable-spring-boot-starter-process | 7.2.0 | microservices\ioedream-oa-service\pom.xml |
| ioedream-oa-service | org.flowable | flowable-spring-boot-starter-cmmn | 7.2.0 | microservices\ioedream-oa-service\pom.xml |
| ioedream-oa-service | org.flowable | flowable-spring-boot-starter-dmn | 7.2.0 | microservices\ioedream-oa-service\pom.xml |
| ioedream-oa-service | org.flowable | flowable-dmn-api | 7.2.0 | microservices\ioedream-oa-service\pom.xml |
| ioedream-video-service | cn.hutool | hutool-all | 5.8.26 | microservices\ioedream-video-service\pom.xml |
| ioedream-video-service | io.github.resilience4j | resilience4j-spring-boot3 | 2.1.0 | microservices\ioedream-video-service\pom.xml |
| ioedream-video-service | io.github.resilience4j | resilience4j-annotations | 2.1.0 | microservices\ioedream-video-service\pom.xml |
| ioedream-video-service | io.github.resilience4j | resilience4j-circuitbreaker | 2.1.0 | microservices\ioedream-video-service\pom.xml |
| ioedream-video-service | io.github.resilience4j | resilience4j-timelimiter | 2.1.0 | microservices\ioedream-video-service\pom.xml |
| microservices-common | com.alibaba | druid-spring-boot-3-starter | 1.2.25 | microservices\microservices-common\pom.xml |
| microservices-common | org.eclipse.jdt | org.eclipse.jdt.annotation | 2.3.0 | microservices\microservices-common\pom.xml |
| microservices-common | io.micrometer | context-propagation | 1.1.1 | microservices\microservices-common\pom.xml |
| microservices-common-core | io.github.resilience4j | resilience4j-spring-boot3 | 2.1.0 | microservices\microservices-common-core\pom.xml |
| microservices-common-core | io.swagger.core.v3 | swagger-annotations | 2.2.0 | microservices\microservices-common-core\pom.xml |
| microservices-common-core | com.baomidou | mybatis-plus-spring-boot3-starter | 3.5.15 | microservices\microservices-common-core\pom.xml |
| microservices-common-storage | io.minio | minio | 8.5.7 | microservices\microservices-common-storage\pom.xml |
| microservices-common-storage | com.aliyun.oss | aliyun-sdk-oss | 3.17.4 | microservices\microservices-common-storage\pom.xml |
| microservices-common-storage | org.apache.tika | tika-core | 2.9.1 | microservices\microservices-common-storage\pom.xml |


### 2. 缺失版本属性

以下依赖引用的版本属性在父POM中未定义：

| 模块 | GroupId | ArtifactId | 属性名 | 文件路径 |
|------|---------|------------|--------|----------|
| ioedream-access-service | net.lab1024.sa | microservices-common-core | ${project.version} | microservices\ioedream-access-service\pom.xml |
| ioedream-access-service | net.lab1024.sa | microservices-common-data | ${project.version} | microservices\ioedream-access-service\pom.xml |
| ioedream-access-service | net.lab1024.sa | microservices-common-security | ${project.version} | microservices\ioedream-access-service\pom.xml |
| ioedream-access-service | net.lab1024.sa | microservices-common-cache | ${project.version} | microservices\ioedream-access-service\pom.xml |
| ioedream-access-service | net.lab1024.sa | microservices-common-monitor | ${project.version} | microservices\ioedream-access-service\pom.xml |
| ioedream-access-service | net.lab1024.sa | microservices-common-business | ${project.version} | microservices\ioedream-access-service\pom.xml |
| ioedream-access-service | net.lab1024.sa | microservices-common-permission | ${project.version} | microservices\ioedream-access-service\pom.xml |
| ioedream-access-service | net.lab1024.sa | microservices-common-storage | ${project.version} | microservices\ioedream-access-service\pom.xml |
| ioedream-attendance-service | net.lab1024.sa | microservices-common-core | ${project.version} | microservices\ioedream-attendance-service\pom.xml |
| ioedream-attendance-service | net.lab1024.sa | microservices-common-data | ${project.version} | microservices\ioedream-attendance-service\pom.xml |
| ioedream-attendance-service | net.lab1024.sa | microservices-common-security | ${project.version} | microservices\ioedream-attendance-service\pom.xml |
| ioedream-attendance-service | net.lab1024.sa | microservices-common-cache | ${project.version} | microservices\ioedream-attendance-service\pom.xml |
| ioedream-attendance-service | net.lab1024.sa | microservices-common-monitor | ${project.version} | microservices\ioedream-attendance-service\pom.xml |
| ioedream-attendance-service | net.lab1024.sa | microservices-common-business | ${project.version} | microservices\ioedream-attendance-service\pom.xml |
| ioedream-attendance-service | net.lab1024.sa | microservices-common-permission | ${project.version} | microservices\ioedream-attendance-service\pom.xml |
| ioedream-attendance-service | net.lab1024.sa | microservices-common-storage | ${project.version} | microservices\ioedream-attendance-service\pom.xml |
| ioedream-biometric-service | net.lab1024.sa | microservices-common-core | ${project.version} | microservices\ioedream-biometric-service\pom.xml |
| ioedream-biometric-service | net.lab1024.sa | microservices-common-data | ${project.version} | microservices\ioedream-biometric-service\pom.xml |
| ioedream-biometric-service | net.lab1024.sa | microservices-common-security | ${project.version} | microservices\ioedream-biometric-service\pom.xml |
| ioedream-biometric-service | net.lab1024.sa | microservices-common-cache | ${project.version} | microservices\ioedream-biometric-service\pom.xml |
| ioedream-biometric-service | net.lab1024.sa | microservices-common-monitor | ${project.version} | microservices\ioedream-biometric-service\pom.xml |
| ioedream-biometric-service | net.lab1024.sa | microservices-common-business | ${project.version} | microservices\ioedream-biometric-service\pom.xml |
| ioedream-biometric-service | net.lab1024.sa | microservices-common-permission | ${project.version} | microservices\ioedream-biometric-service\pom.xml |
| ioedream-common-service | net.lab1024.sa | microservices-common-core | ${project.version} | microservices\ioedream-common-service\pom.xml |
| ioedream-common-service | net.lab1024.sa | microservices-common-data | ${project.version} | microservices\ioedream-common-service\pom.xml |
| ioedream-common-service | net.lab1024.sa | microservices-common-security | ${project.version} | microservices\ioedream-common-service\pom.xml |
| ioedream-common-service | net.lab1024.sa | microservices-common-cache | ${project.version} | microservices\ioedream-common-service\pom.xml |
| ioedream-common-service | net.lab1024.sa | microservices-common-workflow | ${project.version} | microservices\ioedream-common-service\pom.xml |
| ioedream-common-service | net.lab1024.sa | microservices-common-monitor | ${project.version} | microservices\ioedream-common-service\pom.xml |
| ioedream-common-service | net.lab1024.sa | microservices-common-business | ${project.version} | microservices\ioedream-common-service\pom.xml |
| ioedream-common-service | net.lab1024.sa | microservices-common-storage | ${project.version} | microservices\ioedream-common-service\pom.xml |
| ioedream-common-service | net.lab1024.sa | microservices-common-permission | ${project.version} | microservices\ioedream-common-service\pom.xml |
| ioedream-common-service | net.lab1024.sa | microservices-common | ${project.version} | microservices\ioedream-common-service\pom.xml |
| ioedream-consume-service | net.lab1024.sa | microservices-common-core | ${project.version} | microservices\ioedream-consume-service\pom.xml |
| ioedream-consume-service | net.lab1024.sa | microservices-common-data | ${project.version} | microservices\ioedream-consume-service\pom.xml |
| ioedream-consume-service | net.lab1024.sa | microservices-common-security | ${project.version} | microservices\ioedream-consume-service\pom.xml |
| ioedream-consume-service | net.lab1024.sa | microservices-common-business | ${project.version} | microservices\ioedream-consume-service\pom.xml |
| ioedream-consume-service | net.lab1024.sa | microservices-common-cache | ${project.version} | microservices\ioedream-consume-service\pom.xml |
| ioedream-consume-service | net.lab1024.sa | microservices-common-permission | ${project.version} | microservices\ioedream-consume-service\pom.xml |
| ioedream-consume-service | net.lab1024.sa | microservices-common-monitor | ${project.version} | microservices\ioedream-consume-service\pom.xml |
| ioedream-database-service | net.lab1024.sa | microservices-common | ${project.version} | microservices\ioedream-database-service\pom.xml |
| ioedream-database-service | net.lab1024.sa | microservices-common-core | ${project.version} | microservices\ioedream-database-service\pom.xml |
| ioedream-database-service | net.lab1024.sa | microservices-common-business | ${project.version} | microservices\ioedream-database-service\pom.xml |
| ioedream-device-comm-service | net.lab1024.sa | microservices-common-core | ${project.version} | microservices\ioedream-device-comm-service\pom.xml |
| ioedream-device-comm-service | net.lab1024.sa | microservices-common-data | ${project.version} | microservices\ioedream-device-comm-service\pom.xml |
| ioedream-device-comm-service | net.lab1024.sa | microservices-common-security | ${project.version} | microservices\ioedream-device-comm-service\pom.xml |
| ioedream-device-comm-service | net.lab1024.sa | microservices-common-cache | ${project.version} | microservices\ioedream-device-comm-service\pom.xml |
| ioedream-device-comm-service | net.lab1024.sa | microservices-common-monitor | ${project.version} | microservices\ioedream-device-comm-service\pom.xml |
| ioedream-device-comm-service | net.lab1024.sa | microservices-common-business | ${project.version} | microservices\ioedream-device-comm-service\pom.xml |
| ioedream-device-comm-service | net.lab1024.sa | microservices-common-permission | ${project.version} | microservices\ioedream-device-comm-service\pom.xml |
| ioedream-gateway-service | net.lab1024.sa | microservices-common-core | ${project.version} | microservices\ioedream-gateway-service\pom.xml |
| ioedream-gateway-service | net.lab1024.sa | microservices-common | ${project.version} | microservices\ioedream-gateway-service\pom.xml |
| ioedream-gateway-service | net.lab1024.sa | microservices-common-security | ${project.version} | microservices\ioedream-gateway-service\pom.xml |
| ioedream-oa-service | net.lab1024.sa | microservices-common-core | ${project.version} | microservices\ioedream-oa-service\pom.xml |
| ioedream-oa-service | net.lab1024.sa | microservices-common-entity | ${project.version} | microservices\ioedream-oa-service\pom.xml |
| ioedream-oa-service | net.lab1024.sa | microservices-common-data | ${project.version} | microservices\ioedream-oa-service\pom.xml |
| ioedream-oa-service | net.lab1024.sa | microservices-common-security | ${project.version} | microservices\ioedream-oa-service\pom.xml |
| ioedream-oa-service | net.lab1024.sa | microservices-common-cache | ${project.version} | microservices\ioedream-oa-service\pom.xml |
| ioedream-oa-service | net.lab1024.sa | microservices-common-workflow | ${project.version} | microservices\ioedream-oa-service\pom.xml |
| ioedream-oa-service | net.lab1024.sa | microservices-common-monitor | ${project.version} | microservices\ioedream-oa-service\pom.xml |
| ioedream-oa-service | net.lab1024.sa | microservices-common-business | ${project.version} | microservices\ioedream-oa-service\pom.xml |
| ioedream-oa-service | net.lab1024.sa | microservices-common-storage | ${project.version} | microservices\ioedream-oa-service\pom.xml |
| ioedream-oa-service | net.lab1024.sa | microservices-common-permission | ${project.version} | microservices\ioedream-oa-service\pom.xml |
| ioedream-video-service | net.lab1024.sa | microservices-common-core | ${project.version} | microservices\ioedream-video-service\pom.xml |
| ioedream-video-service | net.lab1024.sa | microservices-common-data | ${project.version} | microservices\ioedream-video-service\pom.xml |
| ioedream-video-service | net.lab1024.sa | microservices-common-security | ${project.version} | microservices\ioedream-video-service\pom.xml |
| ioedream-video-service | net.lab1024.sa | microservices-common-cache | ${project.version} | microservices\ioedream-video-service\pom.xml |
| ioedream-video-service | net.lab1024.sa | microservices-common-monitor | ${project.version} | microservices\ioedream-video-service\pom.xml |
| ioedream-video-service | net.lab1024.sa | microservices-common-business | ${project.version} | microservices\ioedream-video-service\pom.xml |
| ioedream-video-service | net.lab1024.sa | microservices-common-permission | ${project.version} | microservices\ioedream-video-service\pom.xml |
| ioedream-visitor-service | net.lab1024.sa | microservices-common-core | ${project.version} | microservices\ioedream-visitor-service\pom.xml |
| ioedream-visitor-service | net.lab1024.sa | microservices-common-data | ${project.version} | microservices\ioedream-visitor-service\pom.xml |
| ioedream-visitor-service | net.lab1024.sa | microservices-common-security | ${project.version} | microservices\ioedream-visitor-service\pom.xml |
| ioedream-visitor-service | net.lab1024.sa | microservices-common-cache | ${project.version} | microservices\ioedream-visitor-service\pom.xml |
| ioedream-visitor-service | net.lab1024.sa | microservices-common-monitor | ${project.version} | microservices\ioedream-visitor-service\pom.xml |
| ioedream-visitor-service | net.lab1024.sa | microservices-common-business | ${project.version} | microservices\ioedream-visitor-service\pom.xml |
| ioedream-visitor-service | net.lab1024.sa | microservices-common-permission | ${project.version} | microservices\ioedream-visitor-service\pom.xml |
| microservices-common | net.lab1024.sa | microservices-common-core | ${project.version} | microservices\microservices-common\pom.xml |
| microservices-common | net.lab1024.sa | microservices-common-security | ${project.version} | microservices\microservices-common\pom.xml |
| microservices-common | net.lab1024.sa | microservices-common-data | ${project.version} | microservices\microservices-common\pom.xml |
| microservices-common | net.lab1024.sa | microservices-common-cache | ${project.version} | microservices\microservices-common\pom.xml |
| microservices-common | net.lab1024.sa | microservices-common-export | ${project.version} | microservices\microservices-common\pom.xml |
| microservices-common | net.lab1024.sa | microservices-common-workflow | ${project.version} | microservices\microservices-common\pom.xml |
| microservices-common | net.lab1024.sa | microservices-common-business | ${project.version} | microservices\microservices-common\pom.xml |
| microservices-common-storage | net.lab1024.sa | microservices-common-core | ${project.version} | microservices\microservices-common-storage\pom.xml |


### 3. 核心模块(microservices-common-core)特殊问题

⚠️ **最小稳定内核模块应避免不必要的依赖**
- 包含spring-boot-starter-web依赖（最小稳定内核应避免）
- 包含 3 个硬编码版本（应使用父POM properties）


---

## 🎯 优化建议

### 优先级 P0 - 立即修复

1. **移除microservices-common-core中的spring-boot-starter-web依赖**
   - 原因：最小稳定内核应尽量纯Java，避免引入Web框架
   - 建议：如果需要Web功能，应在上层模块引入

2. **统一所有硬编码版本为properties引用**
   - 原因：便于统一管理和版本升级
   - 建议：将硬编码版本移到父POM的properties中，子模块使用${}引用

### 优先级 P1 - 短期优化

1. **验证所有依赖版本是否符合企业级标准**
   - 检查是否有安全漏洞
   - 检查是否有已知问题版本
   - 检查版本是否过旧

2. **优化依赖结构**
   - 移除重复依赖
   - 使用<optional>true</optional>标记可选依赖
   - 合理使用<scope>限制依赖范围

---

## 📋 版本属性清单

父POM中定义的所有版本属性：
- **${alipay.version}**: 4.40.572.ALL
- **${aviator.version}**: 5.4.3
- **${bouncycastle.version}**: 1.80
- **${caffeine.version}**: 3.1.8
- **${commons-collections4.version}**: 4.4
- **${commons-lang3.version}**: 3.17.0
- **${commons-text.version}**: 1.13.0
- **${druid.version}**: 1.2.25
- **${easyexcel.version}**: 3.3.4
- **${httpclient5.version}**: 5.4.1
- **${itextpdf.version}**: 8.0.5
- **${jackson.version}**: 2.18.2
- **${jakarta-platform.version}**: 10.0.0
- **${jakarta-servlet.version}**: 6.1.0
- **${jakarta-validation.version}**: 3.0.2
- **${java.version}**: 17
- **${jjwt.version}**: 0.12.6
- **${junit.version}**: 5.11.0
- **${lombok.version}**: 1.18.30
- **${micrometer.version}**: 1.13.6
- **${micrometer-prometheus.version}**: 1.13.6
- **${mockito.version}**: 5.15.2
- **${mybatis-plus.version}**: 3.5.15
- **${mysql.version}**: 8.0.35
- **${redisson.version}**: 3.35.0
- **${resilience4j.version}**: 2.1.0
- **${seata.version}**: 2.0.0
- **${spring-boot.version}**: 3.5.8
- **${spring-cloud.version}**: 2025.0.0
- **${spring-cloud-alibaba.version}**: 2025.0.0.0
- **${springdoc.version}**: 2.6.0
- **${swagger.version}**: 2.2.0
- **${tencentcloud-ocr.version}**: 3.1.1115
- **${wechatpay.version}**: 0.2.17
- **${zxing.version}**: 3.5.4


---

## 🔧 修复脚本

可以使用以下Maven命令检查依赖树：

`ash
# 查看完整依赖树
mvn dependency:tree

# 查看依赖冲突
mvn dependency:tree -Dverbose

# 分析依赖
mvn dependency:analyze
`

---

**报告生成完成** ✅

