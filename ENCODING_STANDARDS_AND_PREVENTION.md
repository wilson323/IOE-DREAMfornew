# 🔧 IOE-DREAM 项目编码规范与乱码预防指南

> **版本**: v1.0.0
> **制定时间**: 2025-11-30
> **适用范围**: IOE-DREAM 全体开发人员
> **强制等级**: 🔴 **P0级强制规范**

---

## 📋 编码标准核心要求

### 🔴 一级要求（绝对禁止违反）

#### 1. 文件编码规范
- **强制使用 UTF-8 编码**：所有源代码文件必须使用 UTF-8 无 BOM 编码
- **禁止使用其他编码**：严禁使用 GBK、GB2312、BIG5 等其他编码格式
- **IDE 配置要求**：
  ```properties
  # VSCode settings.json
  "files.encoding": "utf8",
  "files.eol": "\n",
  "files.insertFinalNewline": true,
  "files.trimFinalNewlines": true

  # IntelliJ IDEA
  File > Settings > Editor > File Encodings:
  - Global Encoding: UTF-8
  - Project Encoding: UTF-8
  - Default encoding for properties files: UTF-8
  ```

#### 2. Git 提交信息编码
- **提交信息必须使用 UTF-8 编码**
- **禁止包含特殊字符**：提交信息中不允许出现非ASCII控制字符
- **Git 全局配置**：
  ```bash
  git config --global core.quotepath false
  git config --global i18n.commitencoding utf-8
  git config --global i18n.logoutputencoding utf-8
  ```

#### 3. 文件命名规范
- **文件名使用ASCII字符**：文件名只能包含字母、数字、下划线、连字符
- **禁止使用中文字符**：文件名中不允许包含中文字符
- **正确示例**：`UserService.java`、`订单管理.md` → `order-management.md`

### 🟡 二级要求（强烈建议遵守）

#### 4. 代码注释编码
- **注释使用UTF-8编码**：支持中文注释，但确保文件保存为UTF-8
- **注释规范示例**：
  ```java
  /**
   * 用户服务类
   * 提供用户注册、登录、信息管理等功能
   *
   * @author 开发者姓名
   * @since 2025-11-30
   */
  @Service
  public class UserService {
      // 用户注册方法
      public void registerUser(UserDTO userDTO) {
          // 实现用户注册逻辑
      }
  }
  ```

#### 5. 配置文件编码
- **YAML/JSON/XML文件**：必须使用 UTF-8 编码
- **Properties文件**：使用 UTF-8 编码，添加 `@encoding UTF-8` 声明
- **数据库连接字符串**：明确指定字符集参数

---

## 🛠️ 编码检查工具集

### 自动检查脚本

```bash
#!/bin/bash
# encoding-check.sh - 项目编码检查脚本

echo "🔍 开始项目编码规范检查..."

# 检查文件编码
echo "1. 检查文件编码..."
find . -type f \( -name "*.java" -o -name "*.xml" -o -name "*.yml" -o -name "*.yaml" -o -name "*.properties" -o -name "*.md" \) -exec file {} \; | grep -v "UTF-8\|ASCII" > encoding_issues.txt

if [ -s encoding_issues.txt ]; then
    echo "❌ 发现非UTF-8编码文件："
    cat encoding_issues.txt
    exit 1
else
    echo "✅ 所有文件编码检查通过"
fi

# 检查文件名编码
echo "2. 检查文件名编码..."
find . -type f -name "*[^\x00-\x7F]*" > filename_issues.txt

if [ -s filename_issues.txt ]; then
    echo "❌ 发现非ASCII文件名："
    cat filename_issues.txt
    exit 1
else
    echo "✅ 所有文件名检查通过"
fi

# 检查BOM头
echo "3. 检查BOM头..."
find . -type f -name "*.java" -exec sh -c 'if [ "$(head -c 3 "$1")" = $'\xEF\xBB\xBF' ]; then echo "$1 has BOM"; fi' _ {} \; > bom_issues.txt

if [ -s bom_issues.txt ]; then
    echo "❌ 发现带BOM头的文件："
    cat bom_issues.txt
    exit 1
else
    echo "✅ BOM头检查通过"
fi

echo "🎉 编码规范检查全部通过！"
```

### Git Pre-commit Hook

```bash
#!/bin/sh
# .git/hooks/pre-commit

echo "🔍 Git提交前编码检查..."

# 检查暂存文件的编码
git diff --cached --name-only --diff-filter=ACM | xargs file | grep -v "UTF-8\|ASCII" && {
    echo "❌ 提交包含非UTF-8编码文件，请修复后再提交"
    exit 1
}

# 检查提交信息编码
commit_msg=$(cat "$1")
if echo "$commit_msg" | grep -P '[\x00-\x08\x0B\x0C\x0E-\x1F\x7F-\x9F]'; then
    echo "❌ 提交信息包含控制字符，请使用UTF-8编码"
    exit 1
fi

echo "✅ 编码检查通过"
exit 0
```

---

## ⚠️ 常见问题与解决方案

### 1. IDE编码问题

**问题**：IDE显示乱码
**解决方案**：
- IntelliJ IDEA：File → Settings → Editor → File Encodings → 全部设置为UTF-8
- VSCode：在设置中添加 `"files.encoding": "utf8"`
- Eclipse：Preferences → General → Workspace → Text file encoding 设置为UTF-8

### 2. Git历史乱码修复

**问题**：Git历史提交信息显示乱码
**解决方案**：
```bash
# 查看乱码提交
git log --oneline | grep "?"

# 修复单个提交（谨慎使用）
git rebase -i <commit-id>^
# 在编辑器中将乱码提交改为 'edit'
# 退出编辑器后执行：
git commit --amend -m "正确的中文提交信息"
git rebase --continue
```

### 3. 文件转换编码

**问题**：需要转换文件编码
**解决方案**：
```bash
# 转换单个文件
iconv -f GBK -t UTF-8 input.txt > output.txt

# 批量转换
find . -name "*.java" -exec iconv -f GBK -t UTF-8 {} -o {}.utf8 \;
find . -name "*.java" -exec mv {}.utf8 {} \;
```

### 4. 去除BOM头

**问题**：文件包含BOM头导致问题
**解决方案**：
```bash
# 去除BOM头
sed -i '1s/^\xEF\xBB\xBF//' filename.java

# 批量去除BOM头
find . -name "*.java" -exec sed -i '1s/^\xEF\xBB\xBF//' {} \;
```

---

## 📊 编码合规性检查清单

### ✅ 开发环境检查
- [ ] IDE配置为UTF-8编码
- [ ] Git全局编码配置正确
- [ ] 文件模板使用UTF-8编码
- [ ] 控制台输出使用UTF-8编码

### ✅ 文件创建检查
- [ ] 新建文件使用UTF-8编码
- [ ] 文件名不包含中文字符
- [ ] 代码注释支持中文显示
- [ ] 配置文件使用UTF-8编码

### ✅ 版本控制检查
- [ ] 提交信息使用UTF-8编码
- [ ] 不提交带BOM头的文件
- [ ] Git历史记录显示正常
- [ ] 分支名称使用ASCII字符

---

## 🔒 强制执行机制

### CI/CD 检查

在CI/CD流水线中添加编码检查：

```yaml
# .github/workflows/encoding-check.yml
name: Encoding Standards Check

on: [push, pull_request]

jobs:
  encoding-check:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4

    - name: Check file encoding
      run: |
        # 检查非UTF-8文件
        find . -type f \( -name "*.java" -o -name "*.xml" -o -name "*.yml" -o -name "*.properties" \) -exec file {} \; | grep -v "UTF-8\|ASCII" && exit 1

    - name: Check filename encoding
      run: |
        # 检查非ASCII文件名
        find . -type f -name "*[^\x00-\x7F]*" && exit 1

    - name: Check commit message encoding
      if: github.event_name == 'pull_request'
      run: |
        # 检查PR标题和描述编码
        echo "${{ github.event.pull_request.title }}" | grep -P '[\x00-\x08\x0B\x0C\x0E-\x1F\x7F-\x9F]' && exit 1
```

### 自动修复脚本

```python
# encoding_fix.py - 自动编码修复工具
import os
import chardet

def detect_file_encoding(file_path):
    """检测文件编码"""
    with open(file_path, 'rb') as f:
        raw_data = f.read()
        result = chardet.detect(raw_data)
        return result['encoding']

def convert_to_utf8(file_path, original_encoding):
    """转换文件到UTF-8"""
    with open(file_path, 'r', encoding=original_encoding) as f:
        content = f.read()

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

def main():
    """主函数"""
    for root, dirs, files in os.walk('.'):
        for file in files:
            if file.endswith(('.java', '.xml', '.yml', '.yaml', '.properties', '.md')):
                file_path = os.path.join(root, file)
                try:
                    encoding = detect_file_encoding(file_path)
                    if encoding and encoding.lower() not in ['utf-8', 'ascii']:
                        print(f"Converting {file_path} from {encoding} to UTF-8")
                        convert_to_utf8(file_path, encoding)
                except Exception as e:
                    print(f"Error processing {file_path}: {e}")

if __name__ == "__main__":
    main()
```

---

## 📞 技术支持

**编码问题联系人**：技术架构团队
**紧急响应**：如遇到编码问题导致构建失败，请联系：
- 📧 邮箱：tech-support@ioe-dream.com
- 📱 电话：技术支持热线
- 💬 即时通讯：技术支持群组

---

**⚠️ 特别声明**：本编码规范具有最高优先级，所有开发人员必须严格遵守。违反编码规范的提交将被CI/CD流水线拒绝，并要求立即修复。此规范永不撤销，必须长期执行！

---

**📅 最后更新**：2025-11-30
**📝 版本历史**：
- v1.0.0 (2025-11-30)：初始版本，建立完整的编码规范体系
- v0.9.0 (2025-11-29)：草案版本，内部评审
- v0.1.0 (2025-11-20)：基于乱码问题分析开始制定