package net.lab1024.sa.attendance.service;

import net.lab1024.sa.common.entity.attendance.AttendanceOvertimeEntity;
import net.lab1024.sa.attendance.domain.form.AttendanceOvertimeForm;

public interface AttendanceOvertimeService {
    AttendanceOvertimeEntity submitOvertimeApplication(AttendanceOvertimeForm form);
    void updateOvertimeStatus(String overtimeNo, String status, String approvalComment);
}



