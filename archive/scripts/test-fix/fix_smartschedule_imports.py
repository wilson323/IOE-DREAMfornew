import re
from pathlib import Path

file_path = Path("D:/IOE-DREAM/microservices/ioedream-attendance-service/src/test/java/net/lab1024/sa/attendance/controller/SmartScheduleControllerTest.java")

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Remove incorrectly placed imports after @Import
content = re.sub(r'@Import\(net\.lab1024\.sa\.attendance\.config\.EnhancedTestConfiguration\.class\)\s*\nimport net\.lab1024\.sa\.common\.organization\.dao\.AccessRecordDao;\s*\nimport net\.lab1024\.sa\.common\.organization\.dao\.AreaDao;\s*\nimport net\.lab1024\.sa\.common\.organization\.dao\.DeviceDao;\s*\nimport net\.lab1024\.sa\.attendance\.dao\.AttendanceAnomalyDao;\s*\nimport net\.lab1024\.sa\.attendance\.dao\.AttendanceRecordDao;\s*\nimport net\.lab1024\.sa\.attendance\.dao\.AttendanceRuleConfigDao;\s*\nimport net\.lab1024\.sa\.attendance\.dao\.WorkShiftDao;\s*\nimport net\.lab1024\.sa\.attendance\.monitor\.ApiPerformanceMonitor;\s*\n',
    '@Import(net.lab1024.sa.attendance.config.EnhancedTestConfiguration.class)\n',
    content)

# Find the position to add imports (after existing @Import import)
# Add imports after line 21 (@Import annotation import)
insert_match = content.find('import org.springframework.context.annotation.Import;')
if insert_match != -1:
    insert_pos = content.find('\n', insert_match) + 1

    imports_to_add = '''import net.lab1024.sa.common.organization.dao.AccessRecordDao;
import net.lab1024.sa.common.organization.dao.AreaDao;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.attendance.dao.AttendanceAnomalyDao;
import net.lab1024.sa.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.attendance.dao.AttendanceRuleConfigDao;
import net.lab1024.sa.attendance.dao.WorkShiftDao;
import net.lab1024.sa.attendance.monitor.ApiPerformanceMonitor;

'''

    content = content[:insert_pos] + imports_to_add + content[insert_pos:]

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("✅ 修复SmartScheduleControllerTest imports位置完成")
