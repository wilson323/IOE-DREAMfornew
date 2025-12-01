import re
import os

# 读取AttendanceServiceImpl文件
file_path = "sa-admin/src/main/java/net/lab1024/sa/admin/module/attendance/service/impl/AttendanceServiceImpl.java"

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 修复导入问题
content = content.replace(
    "import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;\n" +
    "import lombok.extern.slf4j.Slf4j;\n" +
    "import net.lab1024.sa.admin.module.attendance.controller.AttendanceController;",
    "import com.baomidou.mybatisplus.core.metadata.IPage;\n" +
    "import com.baomidou.mybatisplus.extension.plugins.pagination.Page;\n" +
    "import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;\n" +
    "import lombok.extern.slf4j.Slf4j;\n" +
    "import net.lab1024.sa.base.common.code.UserErrorCode;\n" +
    "import net.lab1024.sa.admin.module.attendance.repository.AttendanceRecordRepository;"
)

# 修复重复导入
lines = content.split('\n')
new_lines = []
seen_imports = set()

for line in lines:
    if line.strip().startswith('import '):
        if line not in seen_imports:
            new_lines.append(line)
            seen_imports.add(line)
    else:
        new_lines.append(line)

# 修复IPage和Page的导入问题
fixed_content = '\n'.join(new_lines)

# 确保有必要的导入
if 'import com.baomidou.mybatisplus.core.metadata.IPage;' not in fixed_content:
    fixed_content = fixed_content.replace(
        'import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;',
        'import com.baomidou.mybatisplus.core.metadata.IPage;\nimport com.baomidou.mybatisplus.extension.plugins.pagination.Page;\nimport com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;'
    )

if 'import net.lab1024.sa.base.common.code.UserErrorCode;' not in fixed_content:
    fixed_content = fixed_content.replace(
        'import net.lab1024.sa.base.common.exception.SmartException;',
        'import net.lab1024.sa.base.common.code.UserErrorCode;\nimport net.lab1024.sa.base.common.exception.SmartException;'
    )

# 写回文件
with open(file_path, 'w', encoding='utf-8') as f:
    f.write(fixed_content)

print("AttendanceServiceImpl导入修复完成")
