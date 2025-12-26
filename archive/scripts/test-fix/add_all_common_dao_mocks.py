import re
from pathlib import Path

# All DAOs in common-business module
all_common_dao_imports = [
    "import net.lab1024.sa.common.organization.dao.AntiPassbackRecordDao;",
    "import net.lab1024.sa.common.organization.dao.AreaAccessExtDao;",
    "import net.lab1024.sa.common.organization.dao.AreaDeviceDao;",
    "import net.lab1024.sa.common.organization.dao.AreaUserDao;",
    "import net.lab1024.sa.common.organization.dao.InterlockRecordDao;",
    "import net.lab1024.sa.common.organization.dao.MultiPersonRecordDao;",
    "import net.lab1024.sa.common.organization.dao.UserAreaPermissionDao;",
    "import net.lab1024.sa.common.preference.dao.UserPreferenceDao;",
]

all_common_dao_mocks = [
    "    @MockBean",
    "    private AntiPassbackRecordDao antiPassbackRecordDao;",
    "    @MockBean",
    "    private AreaAccessExtDao areaAccessExtDao;",
    "    @MockBean",
    "    private AreaDeviceDao areaDeviceDao;",
    "    @MockBean",
    "    private AreaUserDao areaUserDao;",
    "    @MockBean",
    "    private InterlockRecordDao interlockRecordDao;",
    "    @MockBean",
    "    private MultiPersonRecordDao multiPersonRecordDao;",
    "    @MockBean",
    "    private UserAreaPermissionDao userAreaPermissionDao;",
    "    @MockBean",
    "    private UserPreferenceDao userPreferenceDao;",
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
    if "import net.lab1024.sa.common.organization.dao.AntiPassbackRecordDao;" in content:
        print(f"⏭️ 已有所有Common DAO Mock: {test_file}")
        continue

    # Find the position to insert imports (after existing imports, before class declaration)
    # Find the last import line
    import_matches = list(re.finditer(r'^import .*;', content, re.MULTILINE))
    if import_matches:
        last_import = import_matches[-1]
        insert_pos = last_import.end()

        # Insert DAO imports
        content = content[:insert_pos] + "\n" + "\n".join(all_common_dao_imports) + "\n" + content[insert_pos:]

    # Find the position to insert MockBean declarations (after existing MockBean declarations)
    # Find the last MockBean declaration
    mockbean_match = list(re.finditer(r'@MockBean\s+private \w+ \w+;', content))
    if mockbean_match:
        last_mock_pos = mockbean_match[-1].end()
        # Insert DAO MockBean declarations
        content = content[:last_mock_pos] + "\n" + "\n".join(all_common_dao_mocks) + "\n" + content[last_mock_pos:]

    if content != original_content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"✅ 添加Common DAO Mock: {test_file}")
    else:
        print(f"⚠️ 未能添加: {test_file}")

print("\nCommon DAO Mock添加完成！")
