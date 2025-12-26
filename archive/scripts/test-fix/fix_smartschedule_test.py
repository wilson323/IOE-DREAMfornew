import re
from pathlib import Path

file_path = Path("D:/IOE-DREAM/microservices/ioedream-attendance-service/src/test/java/net/lab1024/sa/attendance/controller/SmartScheduleControllerTest.java")

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Add missing imports after line 22 (after @Import)
insert_point = content.find('@Import(net.lab1024.sa.attendance.config.EnhancedTestConfiguration.class)')
insert_point = content.find('\n', insert_point) + 1

imports_to_add = '''
import net.lab1024.sa.common.organization.dao.AccessRecordDao;
import net.lab1024.sa.common.organization.dao.AreaDao;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.attendance.dao.AttendanceAnomalyDao;
import net.lab1024.sa.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.attendance.dao.AttendanceRuleConfigDao;
import net.lab1024.sa.attendance.dao.WorkShiftDao;
import net.lab1024.sa.attendance.monitor.ApiPerformanceMonitor;
'''

content = content[:insert_point] + imports_to_add + content[insert_point:]

# Fix @MockBean formatting issue (line 69)
content = re.sub(r'@MockBean\s+private (AttendanceAnomalyDao|AttendanceRecordDao|AttendanceRuleConfigDao|WorkShiftDao)',
    lambda m: f'    @MockBean\n    private {m.group(1)}', content)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("✅ 修复SmartScheduleControllerTest完成")
