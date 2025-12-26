package net.lab1024.sa.attendance.service;

import net.lab1024.sa.common.entity.attendance.AttendanceShiftEntity;
import net.lab1024.sa.attendance.domain.form.AttendanceShiftForm;

public interface AttendanceShiftService {
    AttendanceShiftEntity submitShiftApplication(AttendanceShiftForm form);
    void updateShiftStatus(String shiftNo, String status, String approvalComment);
}



