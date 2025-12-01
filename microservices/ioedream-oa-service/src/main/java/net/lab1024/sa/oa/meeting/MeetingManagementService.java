package net.lab1024.sa.oa.meeting;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 会议管理服务
 * 支持会议室预订、会议安排、会议纪要和提醒功能
 *
 * @author IOE-DREAM
 * @since 2025-11-28
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class MeetingManagementService {

    /**
     * 会议状态枚举
     */
    public enum MeetingStatus {
        SCHEDULED("已安排", "会议已安排，等待开始"),
        IN_PROGRESS("进行中", "会议正在进行"),
        COMPLETED("已完成", "会议已结束"),
        CANCELLED("已取消", "会议被取消"),
        POSTPONED("已延期", "会议延期举行");

        private final String description;
        private final String comment;

        MeetingStatus(String description, String comment) {
            this.description = description;
            this.comment = comment;
        }

        public String getDescription() {
            return description;
        }

        public String getComment() {
            return comment;
        }
    }

    /**
     * 会议类型枚举
     */
    public enum MeetingType {
        REGULAR("例会", "定期例会"),
        PROJECT("项目会议", "项目相关会议"),
        TRAINING("培训会议", "员工培训会议"),
        REVIEW("评审会议", "项目评审或文档评审"),
        BRAINSTORM("头脑风暴", "创意讨论会议"),
        EMERGENCY("紧急会议", "突发事项讨论"),
        CLIENT("客户会议", "与客户会面"),
        DEPARTMENT("部门会议", "部门内部会议");

        private final String description;
        private final String comment;

        MeetingType(String description, String comment) {
            this.description = description;
            this.comment = comment;
        }

        public String getDescription() {
            return description;
        }

        public String getComment() {
            return comment;
        }
    }

    /**
     * 会议室信息
     */
    public static class MeetingRoom {
        private String roomId;
        private String roomName;
        private String location;
        private Integer capacity;
        private List<String> equipment;
        private boolean available;
        private String description;

        // 构造函数
        public MeetingRoom(String roomId, String roomName, String location, Integer capacity) {
            this.roomId = roomId;
            this.roomName = roomName;
            this.location = location;
            this.capacity = capacity;
            this.equipment = new ArrayList<>();
            this.available = true;
        }

        // Getter和Setter方法
        public String getRoomId() { return roomId; }
        public void setRoomId(String roomId) { this.roomId = roomId; }

        public String getRoomName() { return roomName; }
        public void setRoomName(String roomName) { this.roomName = roomName; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public Integer getCapacity() { return capacity; }
        public void setCapacity(Integer capacity) { this.capacity = capacity; }

        public List<String> getEquipment() { return equipment; }
        public void setEquipment(List<String> equipment) { this.equipment = equipment; }

        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    /**
     * 会议参与者信息
     */
    public static class MeetingParticipant {
        private String participantId;
        private String participantName;
        private String email;
        private String department;
        private boolean required;
        private boolean confirmed;
        private LocalDateTime confirmTime;
        private String notes;

        // 构造函数
        public MeetingParticipant(String participantId, String participantName,
                               boolean required) {
            this.participantId = participantId;
            this.participantName = participantName;
            this.required = required;
            this.confirmed = false;
        }

        // Getter和Setter方法
        public String getParticipantId() { return participantId; }
        public void setParticipantId(String participantId) { this.participantId = participantId; }

        public String getParticipantName() { return participantName; }
        public void setParticipantName(String participantName) { this.participantName = participantName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }

        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }

        public boolean isConfirmed() { return confirmed; }
        public void setConfirmed(boolean confirmed) {
            this.confirmed = confirmed;
            if (confirmed) {
                this.confirmTime = LocalDateTime.now();
            }
        }

        public LocalDateTime getConfirmTime() { return confirmTime; }
        public void setConfirmTime(LocalDateTime confirmTime) { this.confirmTime = confirmTime; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    /**
     * 会议信息
     */
    public static class Meeting {
        private String meetingId;
        private String title;
        private String description;
        private MeetingType meetingType;
        private MeetingStatus status;
        private String organizerId;
        private String organizerName;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private MeetingRoom meetingRoom;
        private List<MeetingParticipant> participants;
        private String agenda;
        private String attachments;
        private String meetingMinutes;
        private LocalDateTime createTime;
        private String reminderSettings;
        private boolean recurrent;
        private String recurrencePattern;

        // 构造函数
        public Meeting(String meetingId, String title, MeetingType meetingType,
                      String organizerId, LocalDateTime startTime, LocalDateTime endTime) {
            this.meetingId = meetingId;
            this.title = title;
            this.meetingType = meetingType;
            this.organizerId = organizerId;
            this.startTime = startTime;
            this.endTime = endTime;
            this.status = MeetingStatus.SCHEDULED;
            this.participants = new ArrayList<>();
            this.createTime = LocalDateTime.now();
            this.recurrent = false;
        }

        // Getter和Setter方法
        public String getMeetingId() { return meetingId; }
        public void setMeetingId(String meetingId) { this.meetingId = meetingId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public MeetingType getMeetingType() { return meetingType; }
        public void setMeetingType(MeetingType meetingType) { this.meetingType = meetingType; }

        public MeetingStatus getStatus() { return status; }
        public void setStatus(MeetingStatus status) { this.status = status; }

        public String getOrganizerId() { return organizerId; }
        public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

        public String getOrganizerName() { return organizerName; }
        public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

        public MeetingRoom getMeetingRoom() { return meetingRoom; }
        public void setMeetingRoom(MeetingRoom meetingRoom) { this.meetingRoom = meetingRoom; }

        public List<MeetingParticipant> getParticipants() { return participants; }
        public void setParticipants(List<MeetingParticipant> participants) { this.participants = participants; }

        public String getAgenda() { return agenda; }
        public void setAgenda(String agenda) { this.agenda = agenda; }

        public String getAttachments() { return attachments; }
        public void setAttachments(String attachments) { this.attachments = attachments; }

        public String getMeetingMinutes() { return meetingMinutes; }
        public void setMeetingMinutes(String meetingMinutes) { this.meetingMinutes = meetingMinutes; }

        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

        public String getReminderSettings() { return reminderSettings; }
        public void setReminderSettings(String reminderSettings) { this.reminderSettings = reminderSettings; }

        public boolean isRecurrent() { return recurrent; }
        public void setRecurrent(boolean recurrent) { this.recurrent = recurrent; }

        public String getRecurrencePattern() { return recurrencePattern; }
        public void setRecurrencePattern(String recurrencePattern) { this.recurrencePattern = recurrencePattern; }
    }

    /**
     * 安排会议
     */
    public Meeting scheduleMeeting(String title, String description, MeetingType meetingType,
                                 String organizerId, String organizerName,
                                 LocalDateTime startTime, LocalDateTime endTime,
                                 String roomId, List<String> participantIds) {
        log.info("安排会议: title={}, organizer={}, time={} to {}",
                title, organizerId, startTime, endTime);

        Meeting meeting = new Meeting(
            "MEETING_" + System.currentTimeMillis(),
            title,
            meetingType,
            organizerId,
            startTime,
            endTime
        );

        meeting.setDescription(description);
        meeting.setOrganizerName(organizerName);

        // 检查会议室可用性
        MeetingRoom room = getMeetingRoom(roomId);
        if (room == null || !isRoomAvailable(roomId, startTime, endTime)) {
            log.error("会议室不可用: {}", roomId);
            return null;
        }

        meeting.setMeetingRoom(room);

        // 添加参与者
        for (String participantId : participantIds) {
            MeetingParticipant participant = createParticipant(participantId, participantId, true);
            meeting.getParticipants().add(participant);
        }

        // 发送会议通知
        sendMeetingNotification(meeting, "MEETING_SCHEDULED");

        log.info("会议安排成功: meetingId={}, participants={}",
                meeting.getMeetingId(), meeting.getParticipants().size());

        return meeting;
    }

    /**
     * 更新会议信息
     */
    @CacheEvict(value = "meetings", key = "#meetingId")
    public boolean updateMeeting(String meetingId, String title, String description,
                               LocalDateTime startTime, LocalDateTime endTime, String roomId) {
        log.info("更新会议信息: meetingId={}, time={} to {}", meetingId, startTime, endTime);

        Meeting meeting = getMeeting(meetingId);
        if (meeting == null) {
            log.error("会议不存在: {}", meetingId);
            return false;
        }

        if (meeting.getStatus() == MeetingStatus.IN_PROGRESS ||
            meeting.getStatus() == MeetingStatus.COMPLETED) {
            log.warn("会议状态不允许更新: {}", meeting.getStatus());
            return false;
        }

        // 检查新的会议室可用性
        if (roomId != null && !roomId.equals(meeting.getMeetingRoom().getRoomId())) {
            MeetingRoom room = getMeetingRoom(roomId);
            if (room == null || !isRoomAvailable(roomId, startTime, endTime)) {
                log.error("新会议室不可用: {}", roomId);
                return false;
            }
            meeting.setMeetingRoom(room);
        }

        meeting.setTitle(title);
        meeting.setDescription(description);
        meeting.setStartTime(startTime);
        meeting.setEndTime(endTime);

        // 发送更新通知
        sendMeetingNotification(meeting, "MEETING_UPDATED");

        log.info("会议信息更新成功: meetingId={}", meetingId);
        return true;
    }

    /**
     * 取消会议
     */
    @CacheEvict(value = "meetings", key = "#meetingId")
    public boolean cancelMeeting(String meetingId, String cancelReason) {
        log.info("取消会议: meetingId={}, reason={}", meetingId, cancelReason);

        Meeting meeting = getMeeting(meetingId);
        if (meeting == null) {
            log.error("会议不存在: {}", meetingId);
            return false;
        }

        if (meeting.getStatus() == MeetingStatus.COMPLETED) {
            log.warn("会议已结束，无法取消");
            return false;
        }

        meeting.setStatus(MeetingStatus.CANCELLED);
        meeting.setDescription(meeting.getDescription() + "\n取消原因：" + cancelReason);

        // 发送取消通知
        sendMeetingNotification(meeting, "MEETING_CANCELLED");

        log.info("会议已取消: meetingId={}", meetingId);
        return true;
    }

    /**
     * 参与者确认参会
     */
    public boolean confirmParticipation(String meetingId, String participantId, boolean confirmed) {
        log.info("参会确认: meetingId={}, participant={}, confirmed={}",
                meetingId, participantId, confirmed);

        Meeting meeting = getMeeting(meetingId);
        if (meeting == null) {
            log.error("会议不存在: {}", meetingId);
            return false;
        }

        MeetingParticipant participant = findParticipant(meeting, participantId);
        if (participant == null) {
            log.error("参与者不在会议中: {}", participantId);
            return false;
        }

        participant.setConfirmed(confirmed);

        log.info("参会确认更新成功: participant={}, confirmed={}", participantId, confirmed);
        return true;
    }

    /**
     * 开始会议
     */
    @CacheEvict(value = "meetings", key = "#meetingId")
    public boolean startMeeting(String meetingId) {
        log.info("开始会议: meetingId={}", meetingId);

        Meeting meeting = getMeeting(meetingId);
        if (meeting == null) {
            log.error("会议不存在: {}", meetingId);
            return false;
        }

        if (meeting.getStatus() != MeetingStatus.SCHEDULED) {
            log.warn("会议状态不允许开始: {}", meeting.getStatus());
            return false;
        }

        if (LocalDateTime.now().isBefore(meeting.getStartTime())) {
            log.warn("会议时间未到，不能开始");
            return false;
        }

        meeting.setStatus(MeetingStatus.IN_PROGRESS);

        log.info("会议已开始: meetingId={}", meetingId);
        return true;
    }

    /**
     * 结束会议
     */
    @CacheEvict(value = "meetings", key = "#meetingId")
    public boolean endMeeting(String meetingId, String meetingMinutes) {
        log.info("结束会议: meetingId={}", meetingId);

        Meeting meeting = getMeeting(meetingId);
        if (meeting == null) {
            log.error("会议不存在: {}", meetingId);
            return false;
        }

        if (meeting.getStatus() != MeetingStatus.IN_PROGRESS) {
            log.warn("会议状态不允许结束: {}", meeting.getStatus());
            return false;
        }

        meeting.setStatus(MeetingStatus.COMPLETED);
        meeting.setMeetingMinutes(meetingMinutes);

        log.info("会议已结束: meetingId={}", meetingId);
        return true;
    }

    /**
     * 获取会议室列表
     */
    @Cacheable(value = "meetingRooms", key = "'all'")
    public List<MeetingRoom> getMeetingRooms() {
        log.info("获取会议室列表");

        // 模拟数据 - 实际应该从数据库查询
        List<MeetingRoom> rooms = new ArrayList<>();

        rooms.add(createMeetingRoom("ROOM_001", "大会议室A", "1楼大厅", 50));
        rooms.add(createMeetingRoom("ROOM_002", "中会议室B", "2楼东侧", 20));
        rooms.add(createMeetingRoom("ROOM_003", "小会议室C", "3楼西侧", 10));
        rooms.add(createMeetingRoom("ROOM_004", "VIP会议室D", "4楼顶层", 15));
        rooms.add(createMeetingRoom("ROOM_005", "视频会议室E", "2楼西侧", 30));

        return rooms;
    }

    /**
     * 获取会议室可用性
     */
    public Map<String, Object> getRoomAvailability(String roomId, LocalDateTime date) {
        log.info("获取会议室可用性: roomId={}, date={}", roomId, date);

        Map<String, Object> availability = new HashMap<>();

        // 检查会议室是否存在
        MeetingRoom room = getMeetingRoom(roomId);
        if (room == null) {
            availability.put("available", false);
            availability.put("message", "会议室不存在");
            return availability;
        }

        LocalDateTime dayStart = date.toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = date.toLocalDate().atTime(23, 59, 59);

        // 获取该会议室当天的会议安排
        List<Map<String, Object>> schedules = getRoomSchedule(roomId, dayStart, dayEnd);

        // 计算空闲时间段
        List<Map<String, Object>> availableSlots = calculateAvailableSlots(
            dayStart, dayEnd, schedules);

        availability.put("room", room);
        availability.put("available", !availableSlots.isEmpty());
        availability.put("schedules", schedules);
        availability.put("availableSlots", availableSlots);

        return availability;
    }

    /**
     * 获取我的会议
     */
    @Cacheable(value = "myMeetings", key = "#userId + '_' + #startDate.toString() + '_' + #endDate.toString()")
    public List<Meeting> getMyMeetings(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("获取我的会议: userId={}, timeRange={} to {}", userId, startDate, endDate);

        // 模拟数据 - 实际应该从数据库查询
        return Arrays.asList(
            createSampleMeeting("MEETING_001", "项目周会", MeetingType.REGULAR, userId),
            createSampleMeeting("MEETING_002", "需求评审", MeetingType.REVIEW, userId),
            createSampleMeeting("MEETING_003", "客户演示", MeetingType.CLIENT, userId)
        ).stream()
         .filter(meeting -> isUserParticipant(meeting, userId))
         .collect(Collectors.toList());
    }

    /**
     * 获取会议详情
     */
    @Cacheable(value = "meetings", key = "#meetingId")
    public Meeting getMeeting(String meetingId) {
        log.info("获取会议详情: meetingId={}", meetingId);

        // 模拟数据 - 实际应该从数据库查询
        return createSampleMeeting(meetingId, "项目讨论会", MeetingType.PROJECT, "USER_001");
    }

    /**
     * 获取会议统计信息
     */
    public Map<String, Object> getMeetingStatistics(String userId, String timeRange) {
        log.info("获取会议统计信息: userId={}, timeRange={}", userId, timeRange);

        Map<String, Object> statistics = new HashMap<>();

        // 会议数量统计
        statistics.put("totalMeetings", 24);
        statistics.put("organizedMeetings", 8);
        statistics.put("participatedMeetings", 16);
        statistics.put("completedMeetings", 20);
        statistics.put("cancelledMeetings", 2);

        // 会议类型统计
        Map<String, Integer> typeStatistics = new HashMap<>();
        typeStatistics.put("REGULAR", 10);
        typeStatistics.put("PROJECT", 8);
        typeStatistics.put("REVIEW", 4);
        typeStatistics.put("CLIENT", 2);
        statistics.put("typeStatistics", typeStatistics);

        // 会议时长统计
        statistics.put("totalMeetingHours", 48.5);
        statistics.put("averageMeetingHours", 2.0);

        // 出勤率统计
        statistics.put("attendanceRate", 92.5);
        statistics.put("confirmationRate", 88.0);

        return statistics;
    }

    /**
     * 预订会议室
     */
    public boolean bookMeetingRoom(String roomId, LocalDateTime startTime,
                                 LocalDateTime endTime, String bookerId) {
        log.info("预订会议室: roomId={}, time={} to {}, booker={}",
                roomId, startTime, endTime, bookerId);

        if (!isRoomAvailable(roomId, startTime, endTime)) {
            log.warn("会议室已被预订: {}", roomId);
            return false;
        }

        // 实际应该在数据库中保存预订信息
        log.info("会议室预订成功: roomId={}", roomId);
        return true;
    }

    /**
     * 取消会议室预订
     */
    public boolean cancelRoomBooking(String roomId, LocalDateTime startTime,
                                   LocalDateTime endTime, String bookerId) {
        log.info("取消会议室预订: roomId={}, time={} to {}, booker={}",
                roomId, startTime, endTime, bookerId);

        // 实际应该从数据库中删除预订信息
        log.info("会议室预订取消成功: roomId={}", roomId);
        return true;
    }

    // 私有辅助方法

    /**
     * 获取会议室信息
     */
    private MeetingRoom getMeetingRoom(String roomId) {
        return getMeetingRooms().stream()
                .filter(room -> room.getRoomId().equals(roomId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 检查会议室可用性
     */
    private boolean isRoomAvailable(String roomId, LocalDateTime startTime, LocalDateTime endTime) {
        // 简化检查 - 实际应该查询数据库中的预订记录
        return true;
    }

    /**
     * 创建会议室对象
     */
    private MeetingRoom createMeetingRoom(String roomId, String roomName,
                                        String location, Integer capacity) {
        MeetingRoom room = new MeetingRoom(roomId, roomName, location, capacity);

        // 设置设备信息
        if (capacity >= 30) {
            room.getEquipment().add("投影仪");
            room.getEquipment().add("音响系统");
            room.getEquipment().add("白板");
        }
        if (capacity >= 20) {
            room.getEquipment().add("麦克风");
        }
        room.getEquipment().add("空调");
        room.getEquipment().add("WiFi");

        return room;
    }

    /**
     * 创建会议参与者
     */
    private MeetingParticipant createParticipant(String participantId, String participantName,
                                               boolean required) {
        MeetingParticipant participant = new MeetingParticipant(participantId, participantName, required);
        participant.setEmail(participantId.toLowerCase() + "@company.com");
        participant.setDepartment("技术部"); // 模拟部门信息
        return participant;
    }

    /**
     * 查找会议参与者
     */
    private MeetingParticipant findParticipant(Meeting meeting, String participantId) {
        return meeting.getParticipants().stream()
                .filter(p -> p.getParticipantId().equals(participantId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 检查用户是否参与会议
     */
    private boolean isUserParticipant(Meeting meeting, String userId) {
        return meeting.getOrganizerId().equals(userId) ||
               meeting.getParticipants().stream()
                       .anyMatch(p -> p.getParticipantId().equals(userId));
    }

    /**
     * 发送会议通知
     */
    private void sendMeetingNotification(Meeting meeting, String notificationType) {
        log.info("发送会议通知: meetingId={}, type={}", meeting.getMeetingId(), notificationType);

        // 实际应该通过邮件、短信或系统消息发送通知
        // 这里简化处理，只记录日志
    }

    /**
     * 获取会议室时间安排
     */
    private List<Map<String, Object>> getRoomSchedule(String roomId, LocalDateTime startTime,
                                                    LocalDateTime endTime) {
        // 模拟数据 - 实际应该从数据库查询
        List<Map<String, Object>> schedules = new ArrayList<>();

        Map<String, Object> schedule1 = new HashMap<>();
        schedule1.put("meetingId", "MEETING_001");
        schedule1.put("title", "项目周会");
        schedule1.put("startTime", startTime.withHour(9).withMinute(0));
        schedule1.put("endTime", startTime.withHour(10).withMinute(30));
        schedule1.put("organizer", "张三");
        schedules.add(schedule1);

        return schedules;
    }

    /**
     * 计算可用时间段
     */
    private List<Map<String, Object>> calculateAvailableSlots(LocalDateTime dayStart,
                                                            LocalDateTime dayEnd,
                                                            List<Map<String, Object>> schedules) {
        List<Map<String, Object>> availableSlots = new ArrayList<>();

        // 简化处理 - 假设整天都有空
        Map<String, Object> slot1 = new HashMap<>();
        slot1.put("startTime", dayStart.withHour(8).withMinute(0));
        slot1.put("endTime", dayStart.withHour(9).withMinute(0));
        availableSlots.add(slot1);

        Map<String, Object> slot2 = new HashMap<>();
        slot2.put("startTime", dayStart.withHour(11).withMinute(0));
        slot2.put("endTime", dayStart.withHour(12).withMinute(0));
        availableSlots.add(slot2);

        return availableSlots;
    }

    /**
     * 创建示例会议
     */
    private Meeting createSampleMeeting(String meetingId, String title,
                                      MeetingType meetingType, String organizerId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.plusDays(1).withHour(14).withMinute(0);
        LocalDateTime endTime = startTime.plusHours(2);

        Meeting meeting = new Meeting(meetingId, title, meetingType, organizerId, startTime, endTime);
        meeting.setOrganizerName("员工" + organizerId);
        meeting.setDescription("这是" + title + "的详细内容");

        // 设置会议室
        meeting.setMeetingRoom(getMeetingRoom("ROOM_001"));

        // 添加参与者
        meeting.getParticipants().add(createParticipant("USER_002", "李四", true));
        meeting.getParticipants().add(createParticipant("USER_003", "王五", false));

        return meeting;
    }

    /**
     * 获取会议类型列表
     */
    public List<Map<String, Object>> getMeetingTypes() {
        return Arrays.stream(MeetingType.values())
                .map(type -> {
                    Map<String, Object> typeInfo = new HashMap<>();
                    typeInfo.put("code", type.name());
                    typeInfo.put("description", type.getDescription());
                    typeInfo.put("comment", type.getComment());
                    return typeInfo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取会议状态列表
     */
    public List<Map<String, Object>> getMeetingStatuses() {
        return Arrays.stream(MeetingStatus.values())
                .map(status -> {
                    Map<String, Object> statusInfo = new HashMap<>();
                    statusInfo.put("code", status.name());
                    statusInfo.put("description", status.getDescription());
                    statusInfo.put("comment", status.getComment());
                    return statusInfo;
                })
                .collect(Collectors.toList());
    }
}