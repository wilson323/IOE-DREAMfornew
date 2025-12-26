import re
from pathlib import Path

# Only DAOs that exist in common-business module
existing_dao_imports = [
    "import net.lab1024.sa.common.organization.dao.AccessRecordDao;",
    "import net.lab1024.sa.common.organization.dao.AreaDao;",
    "import net.lab1024.sa.common.organization.dao.DeviceDao;",
    "import net.lab1024.sa.attendance.dao.AttendanceAnomalyDao;",
    "import net.lab1024.sa.attendance.dao.AttendanceRecordDao;",
    "import net.lab1024.sa.attendance.dao.AttendanceRuleConfigDao;",
    "import net.lab1024.sa.attendance.dao.WorkShiftDao;",
]

existing_dao_mocks = [
    "    @MockBean",
    "    private AccessRecordDao accessRecordDao;",
    "    @MockBean",
    "    private AreaDao areaDao;",
    "    @MockBean",
    "    private DeviceDao deviceDao;",
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

    # Remove non-existent DAO imports
    content = re.sub(r'import net\.lab1024\.sa\.common\.organization\.dao\.DepartmentDao;\n?', '', content)
    content = re.sub(r'import net\.lab1024\.sa\.common\.organization\.dao\.EmployeeDao;\n?', '', content)
    content = re.sub(r'import net\.lab1024\.sa\.common\.organization\.dao\.UserDao;\n?', '', content)

    # Remove non-existent DAO MockBean declarations
    content = re.sub(r'\s+@MockBean\s+private DepartmentDao departmentDao;\n?', '', content)
    content = re.sub(r'\s+@MockBean\s+private EmployeeDao employeeDao;\n?', '', content)
    content = re.sub(r'\s+@MockBean\s+private UserDao userDao;\n?', '', content)

    if content != original_content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"✅ 清理不存在DAO: {test_file}")
    else:
        print(f"⏭️ 无需清理: {test_file}")

print("\n不存在的DAO Mock清理完成！")
