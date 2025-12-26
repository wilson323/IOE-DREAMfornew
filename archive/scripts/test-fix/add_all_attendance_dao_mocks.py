import re
from pathlib import Path

# All DAOs in attendance-service module (excluding those already added)
attendance_dao_imports = [
    "import net.lab1024.sa.attendance.dao.AttendanceAnomalyApplyDao;",
    "import net.lab1024.sa.attendance.dao.AttendanceLeaveDao;",
    "import net.lab1024.sa.attendance.dao.AttendanceOvertimeApplyDao;",
    "import net.lab1024.sa.attendance.dao.AttendanceOvertimeApprovalDao;",
    "import net.lab1024.sa.attendance.dao.AttendanceOvertimeDao;",
    "import net.lab1024.sa.attendance.dao.AttendanceOvertimeRecordDao;",
    "import net.lab1024.sa.attendance.dao.AttendanceOvertimeRuleDao;",
    "import net.lab1024.sa.attendance.dao.AttendanceRuleDao;",
    "import net.lab1024.sa.attendance.dao.AttendanceShiftDao;",
    "import net.lab1024.sa.attendance.dao.AttendanceSummaryDao;",
    "import net.lab1024.sa.attendance.dao.AttendanceSupplementDao;",
    "import net.lab1024.sa.attendance.dao.AttendanceTravelDao;",
    "import net.lab1024.sa.attendance.dao.DepartmentStatisticsDao;",
    "import net.lab1024.sa.attendance.dao.ScheduleRecordDao;",
    "import net.lab1024.sa.attendance.dao.ScheduleTemplateDao;",
    "import net.lab1024.sa.attendance.dao.SmartSchedulePlanDao;",
    "import net.lab1024.sa.attendance.dao.SmartScheduleResultDao;",
]

attendance_dao_mocks = [
    "    @MockBean",
    "    private AttendanceAnomalyApplyDao attendanceAnomalyApplyDao;",
    "    @MockBean",
    "    private AttendanceLeaveDao attendanceLeaveDao;",
    "    @MockBean",
    "    private AttendanceOvertimeApplyDao attendanceOvertimeApplyDao;",
    "    @MockBean",
    "    private AttendanceOvertimeApprovalDao attendanceOvertimeApprovalDao;",
    "    @MockBean",
    "    private AttendanceOvertimeDao attendanceOvertimeDao;",
    "    @MockBean",
    "    private AttendanceOvertimeRecordDao attendanceOvertimeRecordDao;",
    "    @MockBean",
    "    private AttendanceOvertimeRuleDao attendanceOvertimeRuleDao;",
    "    @MockBean",
    "    private AttendanceRuleDao attendanceRuleDao;",
    "    @MockBean",
    "    private AttendanceShiftDao attendanceShiftDao;",
    "    @MockBean",
    "    private AttendanceSummaryDao attendanceSummaryDao;",
    "    @MockBean",
    "    private AttendanceSupplementDao attendanceSupplementDao;",
    "    @MockBean",
    "    private AttendanceTravelDao attendanceTravelDao;",
    "    @MockBean",
    "    private DepartmentStatisticsDao departmentStatisticsDao;",
    "    @MockBean",
    "    private ScheduleRecordDao scheduleRecordDao;",
    "    @MockBean",
    "    private ScheduleTemplateDao scheduleTemplateDao;",
    "    @MockBean",
    "    private SmartSchedulePlanDao smartSchedulePlanDao;",
    "    @MockBean",
    "    private SmartScheduleResultDao smartScheduleResultDao;",
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
    if "import net.lab1024.sa.attendance.dao.AttendanceAnomalyApplyDao;" in content:
        print(f"⏭️ 已有所有Attendance DAO Mock: {test_file}")
        continue

    # Find the position to insert imports (after existing imports)
    import_matches = list(re.finditer(r'^import .*;', content, re.MULTILINE))
    if import_matches:
        last_import = import_matches[-1]
        insert_pos = last_import.end()

        # Insert DAO imports
        content = content[:insert_pos] + "\n" + "\n".join(attendance_dao_imports) + "\n" + content[insert_pos:]

    # Find the position to insert MockBean declarations (after existing MockBean declarations)
    mockbean_match = list(re.finditer(r'@MockBean\s+private \w+ \w+;', content))
    if mockbean_match:
        last_mock_pos = mockbean_match[-1].end()
        # Insert DAO MockBean declarations
        content = content[:last_mock_pos] + "\n" + "\n".join(attendance_dao_mocks) + "\n" + content[last_mock_pos:]

    if content != original_content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"✅ 添加Attendance DAO Mock: {test_file}")
    else:
        print(f"⚠️ 未能添加: {test_file}")

print("\nAttendance DAO Mock添加完成！")
