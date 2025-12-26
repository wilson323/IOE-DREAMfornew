import re

# Fix AttendanceSummaryServiceImpl.java
file_path = "D:/IOE-DREAM/microservices/ioedream-attendance-service/src/main/java/net/lab1024/sa/attendance/service/impl/AttendanceSummaryServiceImpl.java"

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix 1: Line 81-85 - Add .build() after QueryBuilder chain
content = re.sub(
    r'attendanceSummaryDao\.delete\(\s*QueryBuilder\.of\(AttendanceSummaryEntity\.class\)\s*\.eq\(AttendanceSummaryEntity::getEmployeeId, employeeId\)\s*\.eq\(AttendanceSummaryEntity::getSummaryMonth, summaryMonth\)\s*\);',
    '''attendanceSummaryDao.delete(
                    QueryBuilder.of(AttendanceSummaryEntity.class)
                            .eq(AttendanceSummaryEntity::getEmployeeId, employeeId)
                            .eq(AttendanceSummaryEntity::getSummaryMonth, summaryMonth)
                            .build()
            );''',
    content,
    flags=re.MULTILINE | re.DOTALL
)

# Fix 2: Line 134-138 - Add .build() after QueryBuilder chain (batchGeneratePersonalSummaries)
content = re.sub(
    r'attendanceSummaryDao\.delete\(\s*QueryBuilder\.of\(AttendanceSummaryEntity\.class\)\s*\.eq\(AttendanceSummaryEntity::getEmployeeId, employeeId\)\s*\.eq\(AttendanceSummaryEntity::getSummaryMonth, summaryMonth\)\s*\)\s*;',
    '''attendanceSummaryDao.delete(
                        QueryBuilder.of(AttendanceSummaryEntity.class)
                                .eq(AttendanceSummaryEntity::getEmployeeId, employeeId)
                                .eq(AttendanceSummaryEntity::getSummaryMonth, summaryMonth)
                                .build()
                );''',
    content,
    flags=re.MULTILINE | re.DOTALL
)

# Fix 3: Line 175-179 - Change Entity type and add .build()
content = re.sub(
    r'departmentStatisticsDao\.delete\(\s*QueryBuilder\.of\(AttendanceSummaryEntity\.class\)\s*\.eq\(DepartmentStatisticsEntity::getDepartmentId, departmentId\)\s*\.eq\(DepartmentStatisticsEntity::getStatisticsMonth, statisticsMonth\)\s*\);',
    '''departmentStatisticsDao.delete(
                    QueryBuilder.of(DepartmentStatisticsEntity.class)
                            .eq(DepartmentStatisticsEntity::getDepartmentId, departmentId)
                            .eq(DepartmentStatisticsEntity::getStatisticsMonth, statisticsMonth)
                            .build()
            );''',
    content,
    flags=re.MULTILINE | re.DOTALL
)

# Fix 4: Line 222-226 - Change Entity type and add .build() (batchGenerateDepartmentStatistics)
content = re.sub(
    r'departmentStatisticsDao\.delete\(\s*QueryBuilder\.of\(AttendanceSummaryEntity\.class\)\s*\.eq\(DepartmentStatisticsEntity::getDepartmentId, departmentId\)\s*\.eq\(DepartmentStatisticsEntity::getStatisticsMonth, statisticsMonth\)\s*\)\s*;',
    '''departmentStatisticsDao.delete(
                        QueryBuilder.of(DepartmentStatisticsEntity.class)
                                .eq(DepartmentStatisticsEntity::getDepartmentId, departmentId)
                                .eq(DepartmentStatisticsEntity::getStatisticsMonth, statisticsMonth)
                                .build()
                );''',
    content,
    flags=re.MULTILINE | re.DOTALL
)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("✅ Fixed AttendanceSummaryServiceImpl.java QueryBuilder errors")

# Fix SmartScheduleServiceImpl.java
file_path2 = "D:/IOE-DREAM/microservices/ioedream-attendance-service/src/main/java/net/lab1024/sa/attendance/service/impl/SmartScheduleServiceImpl.java"

with open(file_path2, 'r', encoding='utf-8') as f:
    content2 = f.read()

# Fix 5: Line 287 - Change Entity type and add .build()
content2 = re.sub(
    r'smartScheduleResultDao\.delete\(QueryBuilder\.of\(SmartScheduleEntity\.class\)\s*\.eq\(SmartScheduleResultEntity::getPlanId, planId\)\);',
    '''smartScheduleResultDao.delete(QueryBuilder.of(SmartScheduleResultEntity.class)
                .eq(SmartScheduleResultEntity::getPlanId, planId)
                .build()
        );''',
    content2,
    flags=re.MULTILINE | re.DOTALL
)

with open(file_path2, 'w', encoding='utf-8') as f:
    f.write(content2)

print("✅ Fixed SmartScheduleServiceImpl.java QueryBuilder errors")
print("\n✅ 所有QueryBuilder错误已修复！")
