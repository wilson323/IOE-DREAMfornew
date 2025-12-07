# IOE-DREAM 测试指南

**最后更新**: 2025-12-05  
**测试状态**: ✅ 配置完成，准备执行

---

## 🚀 快速开始

### 执行所有测试

```powershell
# 使用全局测试脚本
cd D:\IOE-DREAM
.\scripts\run-global-tests.ps1 -All -Coverage
```

### 分别执行测试

```bash
# 前端测试
cd smart-admin-web-javascript
npm install
npm run test
npm run test:coverage

# 移动端测试
cd smart-app
npm install
npm run test
npm run test:coverage

# 后端测试
cd microservices
mvn clean test
mvn jacoco:report
```

---

## 📋 测试配置概览

### 前端测试 (Vitest)
- **配置文件**: `vitest.config.js`
- **测试脚本**: `npm run test`
- **覆盖率**: `npm run test:coverage`
- **测试用例**: 95+个

### 移动端测试 (Jest)
- **配置文件**: `jest.config.js`
- **测试脚本**: `npm run test`
- **覆盖率**: `npm run test:coverage`
- **测试用例**: 30+个

### 后端测试 (JUnit 5)
- **构建工具**: Maven
- **测试脚本**: `mvn test`
- **覆盖率**: `mvn jacoco:report`
- **测试用例**: 36+个测试类

---

## 📊 测试覆盖率目标

| 模块 | 目标覆盖率 | 当前状态 |
|------|-----------|---------|
| 前端 | ≥80% | ⚠️ 待执行 |
| 移动端 | ≥80% | ⚠️ 待执行 |
| 后端 | ≥80% | ⚠️ 待执行 |

---

**测试负责人**: IOE-DREAM Team
