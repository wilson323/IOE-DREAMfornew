# 模块API契约与数据字典（设备/人事/考勤/门禁/消费）

本文件列出已实现模块的主要API与核心字段，确保前后端一致性与对接效率。

## 设备管理
- 列表：GET /api/smart/device/page
- 详情：GET /api/smart/device/{deviceId}
- 新增：POST /api/smart/device
- 更新：PUT /api/smart/device
- 删除：DELETE /api/smart/device/{deviceId}
- 启用：POST /api/smart/device/{deviceId}/enable
- 禁用：POST /api/smart/device/{deviceId}/disable

核心字段：deviceId, deviceCode, deviceName, deviceType, deviceStatus, ipAddress, port, protocolType, location, lastOnlineTime

## 人事
- 分页：GET /api/employee/list
- 详情：GET /api/employee/detail/{employeeId}
- 新增：POST /api/employee/add
- 更新：POST /api/employee/update
- 删除：POST /api/employee/delete/{employeeId}

核心字段：employeeId, employeeName, gender, email, phone, departmentId, position, status, joinDate

## 考勤
- 打卡：POST /api/attendance/clock
- 分页记录：GET /api/attendance/records

核心字段：recordId, employeeId, clockTime, clockType, deviceId

## 门禁
- 通行验证：POST /api/smart/access/verify
- 刷卡：POST /api/smart/access/verify/card
- 人脸：POST /api/smart/access/verify/face
- 指纹：POST /api/smart/access/verify/fingerprint
- 密码：POST /api/smart/access/verify/password

核心字段：permissionId, personId, deviceIds, effectiveTime, expireTime, accessCount, accessLimit

## 消费
- 扣费：POST /api/consume/pay
- 分页记录：GET /api/consume/records

核心字段：recordId, personId, personName, amount, currency, payMethod, orderNo, status, payTime, deviceId


