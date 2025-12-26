import re
from pathlib import Path

# DAO Mock imports
dao_imports = [
    "import net.lab1024.sa.common.organization.dao.AccessRecordDao;",
    "import net.lab1024.sa.common.organization.dao.AreaDao;",
    "import net.lab1024.sa.common.organization.dao.DeviceDao;",
    "import net.lab1024.sa.common.organization.dao.DepartmentDao;",
    "import net.lab1024.sa.common.organization.dao.EmployeeDao;",
    "import net.lab1024.sa.common.organization.dao.UserDao;",
    "import net.lab1024.sa.attendance.dao.AttendanceAnomalyDao;",
    "import net.lab1024.sa.attendance.dao.AttendanceRecordDao;",
    "import net.lab1024.sa.attendance.dao.AttendanceRuleConfigDao;",
    "import net.lab1024.sa.attendance.dao.WorkShiftDao;",
]

# DAO MockBean declarations
dao_mocks = [
    "    @MockBean",
    "    private AccessRecordDao accessRecordDao;",
    "    @MockBean",
    "    private AreaDao areaDao;",
    "    @MockBean",
    "    private DeviceDao deviceDao;",
    "    @MockBean",
    "    private DepartmentDao departmentDao;",
    "    @MockBean",
    "    private EmployeeDao employeeDao;",
    "    @MockBean",
    "    private UserDao userDao;",
    "    @MockBean",
    "    private AttendanceAnomalyDao attendanceAnomalyDao;",
    "    @MockBean",
    "    private AttendanceRecordDao attendanceRecordDao;",
    "    @MockBean",
    "    private AttendanceRuleConfigDao attendanceRuleConfigDao;",
    "    @MockBean",
    "    private WorkShiftDao workShiftDao;",
]

base_path = Path("D:/IOE-DREAM/microservices/ioedream-attendance-service/src/test/java/net/lab1024/sa/attendance/controller")

test_files = [
    "AttendanceAnomalyControllerTest.java",
    "AttendanceAnomalyApplyControllerTest.java",
    "AttendanceOvertimeApplyControllerTest.java",
    "AttendanceRuleConfigControllerTest.java",
    "WorkShiftControllerTest.java",
    "SmartScheduleControllerTest.java"
]

for test_file in test_files:
    file_path = base_path / test_file
    if not file_path.exists():
        print(f"跳过不存在的文件: {test_file}")
        continue

    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    original_content = content

    # Check if DAO imports already exist
    if "import net.lab1024.sa.common.organization.dao.AccessRecordDao;" in content:
        print(f"⏭️ 已有DAO Mock: {test_file}")
        continue

    # Find the position to insert imports (after existing imports)
    import_match = re.search(r'(import net\.lab1024\.sa\.attendance\.monitor\.ApiPerformanceMonitor;)', content)
    if import_match:
        insert_pos = import_match.end()
        # Insert DAO imports
        content = content[:insert_pos] + "\n" + "\n".join(dao_imports) + "\n" + content[insert_pos:]

    # Find the position to insert MockBean declarations (after existing MockBean declarations)
    # Find the last MockBean declaration
    mockbean_match = list(re.finditer(r'@MockBean\s+private \w+ \w+;', content))
    if mockbean_match:
        last_mock_pos = mockbean_match[-1].end()
        # Insert DAO MockBean declarations
        content = content[:last_mock_pos] + "\n" + "\n".join(dao_mocks) + "\n" + content[last_mock_pos:]

    if content != original_content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"✅ 添加DAO Mock: {test_file}")
    else:
        print(f"⚠️ 未能添加: {test_file}")

print("\nController测试DAO Mock添加完成！")
