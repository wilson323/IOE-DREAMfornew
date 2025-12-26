import re
from pathlib import Path

# Fix .class.class duplication
base_path = Path("D:/IOE-DREAM/microservices/ioedream-attendance-service/src/test/java/net/lab1024/sa/attendance/controller")

test_files = [
    "AttendanceAnomalyControllerTest.java",
    "AttendanceAnomalyApplyControllerTest.java",
    "AttendanceOvertimeApplyControllerTest.java",
    "AttendanceRuleConfigControllerTest.java",
    "WorkShiftControllerTest.java"
]

for test_file in test_files:
    file_path = base_path / test_file
    if not file_path.exists():
        print(f"跳过不存在的文件: {test_file}")
        continue

    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    original_content = content

    # Fix .class.class duplication
    content = re.sub(r'\.class\.class', '.class', content)

    # Also fix @WebMvcTest syntax to use correct format
    content = re.sub(
        r'@WebMvcTest\(value = ([^,]+\.class),',
        r'@WebMvcTest(\1,',
        content
    )

    if content != original_content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"✅ 修复: {test_file}")
    else:
        print(f"⏭️ 无需修复: {test_file}")

print("\nController测试文件语法修复完成！")
