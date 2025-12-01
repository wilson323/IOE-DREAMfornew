# IOE-DREAM项目编码标准零容忍政策执行手册

> **版本**: v1.0
> **发布日期**: 2025-11-21
> **生效日期**: 立即生效
> **适用范围**: 所有项目开发活动
> **执行标准**: 严格遵循D:\IOE-DREAM\docs\repowiki下的所有规范

## 🚨 编码标准零容忍政策

### 政策核心原则
1. **0容忍**: 任何违反编码标准的行为都将被立即阻止
2. **强制执行**: 所有检查必须通过，否则无法继续开发
3. **自动修复**: 提供自动化修复工具，确保问题快速解决
4. **持续监控**: 建立持续监控机制，防止问题再次发生

### 零容忍违规清单

#### 🔴 一级违规（绝对禁止 - 立即阻断）
**违反任何一级规范将导致立即停止所有开发工作**

1. **UTF-8编码违规**
   - 禁止非UTF-8编码的Java文件
   - 禁止BOM标记
   - 禁止任何形式的乱码字符

2. **包名规范违规**
   - 禁止使用javax.*包（Jakarta EE相关）
   - 必须使用jakarta.*包替代
   - JDK标准库的javax.*包允许保留

3. **依赖注入规范违规**
   - 禁止使用@Autowired注解
   - 必须使用@Resource注解
   - 统一依赖注入标准

4. **四层架构违规**
   - 禁止Controller直接访问DAO/Repository
   - 禁止跨层直接调用
   - 必须遵循Controller → Service → Manager → DAO调用链

#### 🟡 二级违规（立即修复 - 修复后才能继续）
**违反二级规范需要立即修复，否则无法继续开发**

1. **实体类设计违规**
   - 禁止重复定义BaseEntity已有的审计字段
   - 必须继承BaseEntity
   - 必须使用@TableField注解

2. **事务边界违规**
   - 禁止在Manager层使用@Transactional
   - 事务边界必须在Service层
   - 正确的事务传播行为

3. **方法命名违规**
   - 禁止不符合命名规范的方法
   - 必须遵循统一的命名约定
   - 避免重复方法定义

#### 🟢 三级违规（建议修复 - 择机优化）
**三级违规建议修复，但不阻断开发流程**

1. **代码风格违规**
2. **注释规范违规**
3. **性能优化建议**

## 🔧 强制检查工具集

### 编码标准验证脚本
```bash
#!/bin/bash
# verify-encoding.sh - 编码标准验证脚本

echo "🔍 执行编码标准验证..."

# 1. UTF-8编码检查（必须为0）
echo "步骤1: UTF-8编码检查"
non_utf8_files=$(find . -name "*.java" -exec file {} \; | grep -v "UTF-8\|ASCII" | wc -l)
if [ $non_utf8_files -ne 0 ]; then
    echo "❌ 编码标准验证失败: 发现 $non_utf8_files 个非UTF-8文件"
    find . -name "*.java" -exec file {} \; | grep -v "UTF-8\|ASCII"
    exit 1
fi

# 2. BOM标记检查（必须为0）
echo "步骤2: BOM标记检查"
bom_files=$(find . -name "*.java" -exec grep -l $'^\xEF\xBB\xBF' {} \; | wc -l)
if [ $bom_files -ne 0 ]; then
    echo "❌ 编码标准验证失败: 发现 $bom_files 个含BOM文件"
    find . -name "*.java" -exec grep -l $'^\xEF\xBB\xBF' {} \;
    exit 1
fi

# 3. 乱码字符检查（必须为0）
echo "步骤3: 乱码字符检查"
garbage_files=$(find . -name "*.java" -exec grep -l "????\|涓?\|鏂?\|锟斤拷" {} \; | wc -l)
if [ $garbage_files -ne 0 ]; then
    echo "❌ 编码标准验证失败: 发现 $garbage_files 个乱码文件"
    find . -name "*.java" -exec grep -l "????\|涓?\|鏂?\|锟斤拷" {} \;
    exit 1
fi

# 4. jakarta包名检查（javax必须为0）
echo "步骤4: jakarta包名检查"
javax_count=$(find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l)
if [ $javax_count -ne 0 ]; then
    echo "❌ 编码标准验证失败: 发现 $javax_count 个javax包使用"
    find . -name "*.java" -exec grep -l "javax\." {} \;
    exit 1
fi

# 5. @Autowired检查（必须为0）
echo "步骤5: @Autowired检查"
autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
if [ $autowired_count -ne 0 ]; then
    echo "❌ 编码标准验证失败: 发现 $autowired_count 个@Autowired使用"
    find . -name "*.java" -exec grep -l "@Autowired" {} \;
    exit 1
fi

# 6. 四层架构检查（Controller直接访问DAO必须为0）
echo "步骤6: 四层架构检查"
architecture_violations=$(grep -r "@Resource.*Dao" --include="*Controller.java" . | wc -l)
if [ $architecture_violations -ne 0 ]; then
    echo "❌ 编码标准验证失败: 发现 $architecture_violations 处架构违规"
    grep -r "@Resource.*Dao" --include="*Controller.java" .
    exit 1
fi

echo "🎉 编码标准验证通过！"
```

### 自动修复工具
```bash
#!/bin/bash
# fix-encoding-issues.sh - 自动修复编码问题

echo "🔧 开始自动修复编码问题..."

# 1. 修复UTF-8编码
echo "步骤1: 修复UTF-8编码"
find . -name "*.java" -exec file {} \; | grep -v "UTF-8\|ASCII" | cut -d: -f1 | while read file; do
    echo "修复文件编码: $file"
    iconv -f GBK -t UTF-8 "$file" > "$file.tmp" && mv "$file.tmp" "$file"
done

# 2. 移除BOM标记
echo "步骤2: 移除BOM标记"
find . -name "*.java" -exec grep -l $'^\xEF\xBB\xBF' {} \; | while read file; do
    echo "移除BOM: $file"
    sed -i '1s/^\xEF\xBB\xBF//' "$file"
done

# 3. 修复jakarta包名
echo "步骤3: 修复jakarta包名"
find . -name "*.java" -exec sed -i 's/javax\.annotation/jakarta.annotation/g' {} \;
find . -name "*.java" -exec sed -i 's/javax\.validation/jakarta.validation/g' {} \;
find . -name "*.java" -exec sed -i 's/javax\.persistence/jakarta.persistence/g' {} \;
find . -name "*.java" -exec sed -i 's/javax\.servlet/jakarta.servlet/g' {} \;
find . -name "*.java" -exec sed -i 's/javax\.xml\.bind/jakarta.xml.bind/g' {} \;

# 4. 修复@Autowired
echo "步骤4: 修复@Autowired"
find . -name "*.java" -exec sed -i 's/@Autowired/@Resource/g' {} \;

# 5. 修复常见乱码
echo "步骤5: 修复常见乱码"
find . -name "*.java" -exec sed -i 's/????/中文/g; s/涓?/中/g; s/鏂?/新/g; s/锟斤拷//g' {} \;

echo "🎉 编码问题自动修复完成！"
```

## 🛡️ AI开发强制约束协议

### 开发前强制检查
```bash
#!/bin/bash
# pre-ai-generation-check.sh

echo "🔍 执行AI开发前强制检查..."

# 1. 编码标准检查
if ! ./scripts/verify-encoding.sh; then
    echo "❌ 编码标准检查失败，禁止AI生成代码！"
    exit 1
fi

# 2. 检查当前编译状态
echo "检查当前编译状态..."
error_count=$(mvn clean compile -q 2>&1 | grep -c "ERROR")
echo "当前编译错误数量: $error_count"

# 3. 检查现有方法定义
echo "检查现有方法定义..."
if [ ! -f "existing_methods.txt" ]; then
    find . -name "*.java" -exec grep -H "public.*(" {} \; > existing_methods.txt
fi

# 4. 架构合规性检查
echo "检查架构合规性..."
architecture_violations=$(grep -r "@Resource.*Dao" --include="*Controller.java" . | wc -l)
if [ $architecture_violations -ne 0 ]; then
    echo "❌ 发现架构违规，禁止AI生成代码！"
    exit 1
fi

echo "✅ AI开发前检查通过，可以开始生成代码"
```

### 开发后强制验证
```bash
#!/bin/bash
# post-ai-generation-check.sh

echo "🔍 执行AI开发后强制验证..."

# 1. 编译检查
echo "检查编译状态..."
mvn clean compile -q
if [ $? -ne 0 ]; then
    echo "❌ AI生成代码后编译失败，必须立即修复！"
    exit 1
fi

# 2. 检查新错误
echo "检查是否引入新错误..."
new_error_count=$(mvn clean compile -q 2>&1 | grep -c "ERROR")
if [ $new_error_count -gt $error_count ]; then
    echo "❌ AI生成代码引入新错误，必须立即修复！"
    exit 1
fi

# 3. 编码标准验证
echo "验证编码标准..."
if ! ./scripts/verify-encoding.sh; then
    echo "❌ 编码标准验证失败，必须立即修复！"
    exit 1
fi

# 4. 重复方法检查
echo "检查重复方法定义..."
duplicate_methods=$(mvn clean compile -q 2>&1 | grep -c "duplicate method")
if [ $duplicate_methods -gt 0 ]; then
    echo "❌ 发现重复方法定义，必须立即修复！"
    exit 1
fi

echo "✅ AI开发后验证通过"
```

### AI开发自检清单
```markdown
## AI生成代码强制自检清单

### 编码前检查（必须执行）
- [ ] 已读取并理解CLAUDE.md所有规范
- [ ] 已确认任务类型和必须遵循的repowiki规范
- [ ] 已检查现有方法定义，避免重复
- [ ] 已确认不会引入javax包违规
- [ ] 已规划好四层架构调用链
- [ ] 已通过编码标准验证

### 编码中检查（实时执行）
- [ ] 使用@Resource而非@Autowired
- [ ] 使用jakarta.*而非javax.*
- [ ] 遵循四层架构调用链
- [ ] 不重复定义现有方法
- [ ] 正确设置事务边界
- [ ] 使用@Valid进行参数验证
- [ ] 添加@SaCheckPermission权限注解

### 编码后检查（必须执行）
- [ ] 运行mvn clean compile确保无编译错误
- [ ] 检查javax包使用数量为0
- [ ] 检查@Autowired使用数量为0
- [ ] 验证架构合规性
- [ ] 确认错误数量只减不增
- [ ] 通过编码标准验证
- [ ] 通过AI开发后强制验证

### 违规处理机制
- 🔴 一级违规：立即停止生成，分析原因，重新生成
- 🟡 二级违规：生成但标记警告，提供修正建议
- 🟢 三级违规：正常生成，提供优化建议
```

## 📋 质量门禁强制机制

### 强制性六层验证机制（加强版）
```bash
#!/bin/bash
# enhanced-six-layer-verification.sh

echo "🔥 开始六层验证机制（加强版）..."

# 第零层：本地启动验证（Docker部署前必须通过）
echo "🔥 第零层验证：本地启动测试（120秒稳定运行）"
timeout 150s mvn spring-boot:run -Dspring-boot.run.profiles=docker > ../startup_test.log 2>&1 &
pid=$!
sleep 120

if ps -p $pid > /dev/null; then
    echo "✅ 本地应用成功启动，持续运行120秒"
    kill $pid 2>/dev/null || true
    wait $pid 2>/dev/null || true
else
    echo "❌ 本地启动失败，禁止进行Docker部署"
    exit 1
fi

# 第一层：完整构建验证
echo "🔥 第一层验证：完整构建验证"
mvn clean package -DskipTests -q
if [ $? -ne 0 ]; then
    echo "❌ 构建失败"
    exit 1
fi

# 第二层：包名合规验证（0容忍）
echo "🔥 第二层验证：包名合规验证"
javax_count=$(find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l)
if [ $javax_count -ne 0 ]; then
    echo "❌ 发现 javax 包使用: $javax_count 个文件，0容忍政策!"
    find . -name "*.java" -exec grep -l "javax\." {} \;
    exit 1
fi

# 第三层：架构合规验证
echo "🔥 第三层验证：架构合规验证"
controller_direct_dao=$(grep -r "@Resource.*Dao" --include="*Controller.java" . | wc -l)
if [ $controller_direct_dao -ne 0 ]; then
    echo "❌ 发现Controller直接访问DAO: $controller_direct_dao 处，0容忍政策!"
    grep -r "@Resource.*Dao" --include="*Controller.java" .
    exit 1
fi

# 第四层：依赖注入合规验证
echo "🔥 第四层验证：依赖注入合规验证"
autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
if [ $autowired_count -ne 0 ]; then
    echo "❌ 发现 @Autowired 使用: $autowired_count 个文件，0容忍政策!"
    find . -name "*.java" -exec grep -l "@Autowired" {} \;
    exit 1
fi

# 第五层：Docker部署验证（180秒监控）
echo "🔥 第五层验证：Docker部署验证（180秒持续监控）"
docker-compose build backend
docker-compose up -d backend

for i in 30 60 90 120 150 180; do
    container_status=$(docker-compose ps | grep backend | grep -c "Up" || echo "0")
    if [ "$container_status" = "0" ]; then
        echo "❌ 容器在第${i}秒停止运行"
        docker logs smart-admin-backend --tail 100
        exit 1
    fi
    sleep 30
done

# 第六层：健康检查和异常验证
echo "🔥 第六层验证：健康检查和异常验证"
docker exec smart-admin-backend curl -f http://localhost:1024/api/health > /dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "❌ 健康检查失败"
    exit 1
fi

docker_logs=$(docker logs smart-admin-backend 2>&1)
critical_error_patterns=("ERROR" "Exception" "Failed" "Connection refused" "Application startup failed")
for pattern in "${critical_error_patterns[@]}"; do
    error_count=$(echo "$docker_logs" | grep -i "$pattern" | wc -l)
    if [ $error_count -gt 2 ]; then
        echo "❌ 发现过多 $pattern 错误: $error_count 次"
        exit 1
    fi
done

echo "🎉 六层验证机制全部通过！"
```

## 📊 执行监控和报告

### 每日质量报告
```bash
#!/bin/bash
# daily-quality-report.sh

echo "📊 生成每日质量报告..."

report_date=$(date +%Y-%m-%d)
report_file="daily-quality-report-$report_date.md"

cat > "$report_file" << EOF
# IOE-DREAM项目每日质量报告

**报告日期**: $report_date
**报告时间**: $(date '+%H:%M:%S')

## 编码标准检查结果

### UTF-8编码状态
- 非UTF-8文件数: $(find . -name "*.java" -exec file {} \; | grep -v "UTF-8\|ASCII" | wc -l)
- BOM文件数: $(find . -name "*.java" -exec grep -l $'^\xEF\xBB\xBF' {} \; | wc -l)
- 乱码文件数: $(find . -name "*.java" -exec grep -l "????\|涓?\|鏂?\|锟斤拷" {} \; | wc -l)

### Jakarta包迁移状态
- javax包使用数: $(find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l)
- jakarta包使用数: $(find . -name "*.java" -exec grep -l "jakarta\." {} \; | wc -l)

### 依赖注入标准状态
- @Autowired使用数: $(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
- @Resource使用数: $(find . -name "*.java" -exec grep -l "@Resource" {} \; | wc -l)

### 架构合规状态
- Controller直接访问DAO数: $(grep -r "@Resource.*Dao" --include="*Controller.java" . | wc -l)

### 编译状态
- 编译错误数: $(mvn clean compile -q 2>&1 | grep -c "ERROR")
- 编译警告数: $(mvn clean compile -q 2>&1 | grep -c "WARNING")

## 质量评级
EOF

# 计算质量评级
total_issues=$((non_utf8_files + bom_files + garbage_files + javax_count + autowired_count + architecture_violations))

if [ $total_issues -eq 0 ]; then
    quality_grade="A+ (优秀)"
elif [ $total_issues -le 5 ]; then
    quality_grade="A (良好)"
elif [ $total_issues -le 15 ]; then
    quality_grade="B (一般)"
else
    quality_grade="C (需要改进)"
fi

echo "质量评级: $quality_grade" >> "$report_file"
echo "问题总数: $total_issues" >> "$report_file"

echo "📊 每日质量报告已生成: $report_file"
```

## 🚨 紧急响应机制

### 问题发现立即响应
```bash
#!/bin/bash
# emergency-response.sh

echo "🚨 启动紧急响应机制..."

# 1. 立即停止所有开发活动
echo "步骤1: 停止所有开发活动"
# 这里可以添加停止CI/CD流水线等操作

# 2. 立即评估问题严重性
echo "步骤2: 评估问题严重性"
critical_issues=0

# 检查关键指标
javax_count=$(find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l)
critical_issues=$((critical_issues + javax_count))

autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
critical_issues=$((critical_issues + autowired_count))

architecture_violations=$(grep -r "@Resource.*Dao" --include="*Controller.java" . | wc -l)
critical_issues=$((critical_issues + architecture_violations))

if [ $critical_issues -gt 0 ]; then
    echo "🚨 发现 $critical_issues 个严重问题，启动紧急修复！"

    # 3. 立即执行自动修复
    echo "步骤3: 执行自动修复"
    ./scripts/ultimate-encoding-fix-fixed.sh

    # 4. 验证修复结果
    echo "步骤4: 验证修复结果"
    ./scripts/verify-encoding.sh

    if [ $? -eq 0 ]; then
        echo "✅ 紧急修复成功，可以恢复开发活动"
    else
        echo "❌ 紧急修复失败，需要人工介入！"
        exit 1
    fi
else
    echo "✅ 未发现严重问题，可以继续开发活动"
fi
```

## 📞 联系和支持

### 技术支持
- **编码标准问题**: 编码标准委员会
- **架构设计问题**: 架构师团队
- **工具使用问题**: DevOps团队

### 紧急联系方式
- **P0问题**: 立即电话通知技术负责人
- **P1问题**: 30分钟内响应
- **P2问题**: 2小时内响应

---

**🚨 特别声明**: 本零容忍政策具有最高优先级，严格遵循repowiki规范体系。任何违反编码标准的行为都将被立即阻止并强制修复。此政策永不撤销，必须严格执行！
