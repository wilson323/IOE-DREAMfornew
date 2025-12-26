# Fix AttendanceAnomalyApprovalServiceTest.java - wrong DAO name
file_path = "D:/IOE-DREAM/microservices/ioedream-attendance-service/src/test/java/net/lab1024/sa/attendance/service/AttendanceAnomalyApprovalServiceTest.java"

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace wrong DAO import
content = content.replace(
    "import net.lab1024.sa.attendance.dao.AttendanceAnomalyApprovalRecordDao;",
    "import net.lab1024.sa.attendance.dao.AttendanceAnomalyDao;\nimport net.lab1024.sa.attendance.dao.AttendanceRecordDao;"
)

# Replace wrong DAO mock declaration
content = content.replace(
    "    @Mock\n    private AttendanceAnomalyApprovalRecordDao approvalRecordDao;",
    """    @Mock
    private AttendanceAnomalyDao anomalyDao;

    @Mock
    private AttendanceRecordDao recordDao;"""
)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("✅ Fixed AttendanceAnomalyApprovalServiceTest.java - replaced wrong DAO name")
