# Database Version Manager Skill

专门负责IOE-DREAM数据库脚本版本管理的AI技能，确保严格按照规范执行数据库初始化和版本控制。

## 技能定位

**核心职责**:
- 确保数据库脚本严格遵循版本体系规范
- 自动化执行顺序验证和依赖检查
- 监控脚本质量和合规性
- 提供版本升级和回滚支持

**适用场景**:
- 数据库脚本开发和维护
- 版本升级和发布
- 脚本质量检查
- 问题诊断和修复

## 专业能力

### 1. 版本管理专家

**版本规范执行**:
- 严格执行版本命名规范 (`major.minor.patch-environment`)
- 验证文件命名和目录结构符合标准
- 确保执行顺序依赖关系正确
- 维护版本配置文件完整性

**版本生命周期管理**:
- 版本创建和升级流程
- 测试环境和生产环境版本同步
- 版本回滚机制
- 变更记录维护

### 2. 脚本质量检查专家

**语法和规范检查**:
```sql
-- ✅ 标准脚本格式示例
-- =====================================================
-- 模块: 考勤管理数据库表结构
-- 版本: 1.0.1-FIX
-- 说明: 修复AttendanceShiftEntity表名匹配问题
-- 依赖: 02-common-schema.sql
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE ioedream_attendance_db;

-- 创建调班表 (匹配AttendanceShiftEntity)
DROP TABLE IF EXISTS attendance_shift;
CREATE TABLE attendance_shift (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    shift_no VARCHAR(50),
    employee_id BIGINT,
    shift_date DATE,
    -- 完整审计字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user_id BIGINT,
    update_user_id BIGINT,
    deleted_flag TINYINT DEFAULT 0,
    version INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤调班表';

-- 索引优化
CREATE INDEX idx_employee_date ON attendance_shift(employee_id, shift_date);
CREATE INDEX idx_status ON attendance_shift(status);

SET FOREIGN_KEY_CHECKS = 1;
```

**质量检查清单**:
- [ ] 文件命名符合规范 (前缀+功能描述)
- [ ] 目录结构符合层级要求
- [ ] SQL语法正确性验证
- [ ] 实体类与表结构一致性检查
- [ ] 审计字段完整性验证
- [ ] 索引设计合理性检查
- [ ] 依赖关系正确性验证

### 3. 依赖关系管理专家

**执行顺序验证**:
```
00-environment/    ← 无依赖，必须最先执行
01-databases/      ← 依赖 00-environment
02-common-schema/  ← 依赖 01-databases
03-business-schema/← 依赖 02-common-schema
99-flyway-schema/  ← 依赖 03-business-schema
data/             ← 依赖 99-flyway-schema
rollback/         ← 独立存在，用于回滚
```

**依赖关系检查**:
- 验证脚本执行顺序是否正确
- 检查前置依赖是否满足
- 确保跨脚本引用关系有效
- 维护依赖关系图

### 4. 自动化执行专家

**执行配置管理**:
```yaml
# 执行配置自动生成
execution_plan:
  version: "1.0.1-prod"
  environment: "production"
  execution_mode: "sequential"

  steps:
    - name: "环境准备"
      script: "00-environment/00-charset.sql"
      required: true
      timeout: 300

    - name: "数据库创建"
      script: "01-databases/01-create-databases.sql"
      required: true
      timeout: 600
      depends_on: ["环境准备"]

    - name: "公共表结构"
      script: "02-common-schema/02-common-tables.sql"
      required: true
      timeout: 1200
      depends_on: ["数据库创建"]
```

**自动化验证**:
- 脚本执行前后状态检查
- 数据库对象创建验证
- 数据完整性检查
- 性能基准测试

### 5. 问题诊断专家

**常见问题诊断**:

**问题1: 表名不匹配**
```bash
# 诊断命令
check-entity-matching.sh

# 输出示例
❌ 发现问题: AttendanceShiftEntity
   实体类表名: attendance_shift
   脚本表名: t_work_shift
   修复建议: 修改脚本表名为 attendance_shift
```

**问题2: 字段类型不一致**
```bash
# 诊断命令
check-field-types.sh

# 输出示例
❌ 发现问题: AccountEntity.balance
   实体类类型: BigDecimal
   脚本类型: LONGLONG
   修复建议: 统一使用 DECIMAL(12,2)
```

**问题3: 缺少审计字段**
```bash
# 诊断命令
check-audit-fields.sh

# 输出示例
❌ 发现问题: t_consume_record 表
   缺少字段: create_time, update_time, deleted_flag
   修复建议: 添加标准审计字段
```

## 工作流程

### 1. 开发阶段支持

```bash
# 创建新脚本的标准化流程
create-new-script.sh --module consume --type schema --version 1.0.2

# 自动生成脚本模板
# - 标准文件头注释
# - 目录结构创建
# - 依赖关系配置
# - 质量检查项
```

### 2. 测试阶段验证

```bash
# 执行完整测试流程
test-database-scripts.sh --env test

# 执行内容:
# 1. 语法检查
# 2. 依赖关系验证
# 3. 实体类一致性检查
# 4. 性能测试
# 5. 安全检查
```

### 3. 生产阶段监控

```bash
# 生产部署前检查
pre-deploy-check.sh --env prod

# 执行内容:
# 1. 版本兼容性检查
# 2. 回滚脚本验证
# 3. 数据备份确认
# 4. 影响范围评估
```

## 最佳实践指导

### 1. 脚本开发最佳实践

**文件命名规范**:
```sql
-- ✅ 正确命名
03-consume-schema-fixed.sql     -- 业务模块-类型-修复版本
02-common-tables-v1.2.sql      -- 公共模块-功能-版本
99-flyway-schema.sql           -- 固定功能-标准命名

-- ❌ 错误命名
consume.sql                   -- 太简单
schema_v2.sql                -- 版本号位置错误
fix_tables_20251208.sql      -- 日期在文件名中
```

**SQL编写规范**:
```sql
-- ✅ 正确的SQL编写
-- 1. 使用标准字符集
SET NAMES utf8mb4;

-- 2. 临时禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 3. 使用指定数据库
USE ioedream_consume_db;

-- 4. 表结构创建
DROP TABLE IF EXISTS t_consume_account;
CREATE TABLE t_consume_account (
    account_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    -- 完整字段定义
    -- 审计字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user_id BIGINT,
    update_user_id BIGINT,
    deleted_flag TINYINT DEFAULT 0,
    version INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费账户表';

-- 5. 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;
```

### 2. 版本管理最佳实践

**版本升级流程**:
```
1. 开发版本 (1.0.2-dev)
   ├── 修改脚本文件
   ├── 本地测试验证
   └── 更新 CHANGELOG.md

2. 测试版本 (1.0.2-test)
   ├── 测试环境部署
   ├── 功能完整性验证
   └── 性能基准测试

3. 预发版本 (1.0.2-pre-prod)
   ├── 生产环境模拟
   ├── 数据迁移测试
   └── 回滚流程验证

4. 生产版本 (1.0.2-prod)
   ├── 生产数据备份
   ├── 正式版本发布
   └── 监控和验证
```

### 3. 错误处理最佳实践

**错误分类和处理**:
```sql
-- 错误处理示例
DELIMITER $$
CREATE PROCEDURE execute_script_with_error_handling()
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1
        @errno = MYSQL_ERRNO,
        @text = MESSAGE_TEXT;

        -- 记录错误信息
        INSERT INTO script_error_log (
            error_time, error_code, error_message, script_name
        ) VALUES (
            NOW(), @errno, @text, 'current_script'
        );

        -- 根据错误级别决定是否继续
        IF @errno IN (1045, 1142, 1143) THEN  -- 权限错误
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '权限不足，停止执行';
        END IF;
    END;

    -- 正常执行逻辑
    -- ... SQL statements ...
END$$
DELIMITER ;
```

## 工具和脚本

### 1. 质量检查脚本

```bash
#!/bin/bash
# check-script-quality.sh - 脚本质量检查工具

check_script_quality() {
    local script_file=$1
    local issues=()

    # 检查文件命名规范
    if [[ ! "$script_file" =~ ^[0-9]{2}-[a-z]+-.*\.sql$ ]]; then
        issues+=("文件命名不符合规范")
    fi

    # 检查SQL语法
    if ! mysql --default-character-set=utf8mb4 -e "" < "$script_file" 2>/dev/null; then
        issues+=("SQL语法错误")
    fi

    # 检查字符集设置
    if ! grep -q "SET NAMES utf8mb4" "$script_file"; then
        issues+=("缺少字符集设置")
    fi

    # 检查外键处理
    if ! grep -q "SET FOREIGN_KEY_CHECKS" "$script_file"; then
        issues+=("缺少外键检查设置")
    fi

    # 输出检查结果
    if [ ${#issues[@]} -eq 0 ]; then
        echo "✅ 脚本质量检查通过: $script_file"
    else
        echo "❌ 脚本质量问题: $script_file"
        for issue in "${issues[@]}"; do
            echo "   - $issue"
        done
    fi
}
```

### 2. 版本比较工具

```bash
#!/bin/bash
# compare-versions.sh - 版本比较工具

compare_versions() {
    local version1=$1
    local version2=$2

    # 解析版本号
    IFS='.' read -ra ADDR1 <<< "$version1"
    IFS='.' read -ra ADDR2 <<< "$version2"

    # 比较主版本号
    if [ ${ADDR1[0]} -gt ${ADDR2[0]} ]; then
        echo "greater"
    elif [ ${ADDR1[0]} -lt ${ADDR2[0]} ]; then
        echo "less"
    else
        # 比较次版本号
        if [ ${ADDR1[1]} -gt ${ADDR2[1]} ]; then
            echo "greater"
        elif [ ${ADDR1[1]} -lt ${ADDR2[1]} ]; then
            echo "less"
        else
            # 比较修订号
            if [ ${ADDR1[2]} -gt ${ADDR2[2]} ]; then
                echo "greater"
            elif [ ${ADDR1[2]} -lt ${ADDR2[2]} ]; then
                echo "less"
            else
                echo "equal"
            fi
        fi
    fi
}
```

## 常见问题解答

### Q1: 如何处理版本冲突？

**A**: 版本冲突处理流程：
1. 识别冲突范围 (语法/结构/数据)
2. 评估影响范围和风险
3. 制定合并策略
4. 创建测试用例验证
5. 协调相关团队解决

### Q2: 如何确保脚本与代码一致性？

**A**: 一致性保障措施：
1. 定期运行实体类扫描工具
2. 自动化比较脚本和代码
3. 建立CI/CD检查门禁
4. 代码变更时同步更新脚本
5. 定期进行一致性审计

### Q3: 如何处理大版本升级？

**A**: 大版本升级策略：
1. 详细的影响分析
2. 制定升级计划和时间表
3. 准备回滚方案
4. 分阶段实施
5. 全面监控和验证

## 技能使用指南

### 基础使用

```bash
# 初始化技能环境
skill database-version-manager init

# 检查当前脚本状态
skill database-version-manager status

# 执行质量检查
skill database-version-manager check --quality

# 执行一致性检查
skill database-version-manager check --consistency
```

### 高级功能

```bash
# 生成版本升级计划
skill database-version-manager plan --from 1.0.1 --to 1.0.2

# 执行自动化测试
skill database-version-manager test --environment staging

# 生成执行报告
skill database-version-manager report --format html --output report.html

# 执行版本发布
skill database-version-manager release --version 1.0.2 --environment production
```

---

**技能维护者**: 数据库架构委员会
**最后更新**: 2025-12-08
**版本**: v1.0.0