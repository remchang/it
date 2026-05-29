package meetingroom;

/**
 * 预约记录实体类
 * 封装会议室预约信息，提供时间冲突检测、时长计算和格式转换功能
 *
 * @author 孙政芳
 * @version 1.0
 * @since 2026-05-30
 */
public class Reservation implements Comparable<Reservation> {

    // ========== 基本属性 ==========
    private String reservationId;   // 预约编号，如 RSV-20260530-001
    private String roomId;          // 会议室编号
    private String userId;          // 预约人工号/学号
    private String userName;        // 预约人姓名
    private String title;           // 会议主题
    private String date;            // 预约日期，格式 yyyy-MM-dd
    private String startTime;       // 开始时间，格式 HH:mm
    private String endTime;         // 结束时间，格式 HH:mm
    private int attendees;          // 参会人数
    private String status;          // 状态：已确认、已取消、已完成
    private String remarks;         // 备注信息

    // ========== 状态常量 ==========
    public static final String STATUS_CONFIRMED = "已确认";
    public static final String STATUS_CANCELLED = "已取消";
    public static final String STATUS_COMPLETED = "已完成";

    // ========== 构造方法 ==========

    /** 默认构造方法 */
    public Reservation() {
        this.status = STATUS_CONFIRMED;
    }

    /** 完整构造方法 */
    public Reservation(String reservationId, String roomId, String userId,
                       String userName, String title, String date,
                       String startTime, String endTime, int attendees,
                       String status, String remarks) {
        this.reservationId = reservationId;
        this.roomId = roomId;
        this.userId = userId;
        this.userName = userName;
        this.title = title;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.attendees = attendees;
        this.status = status != null ? status : STATUS_CONFIRMED;
        this.remarks = remarks;
    }

    /** 简化构造方法 — 快速预约 */
    public Reservation(String reservationId, String roomId, String userId,
                       String userName, String title, String date,
                       String startTime, String endTime, int attendees) {
        this(reservationId, roomId, userId, userName, title, date,
             startTime, endTime, attendees, STATUS_CONFIRMED, "");
    }

    // ========== Getter / Setter（带校验） ==========

    public String getReservationId() { return reservationId; }

    public void setReservationId(String reservationId) {
        if (reservationId == null || reservationId.trim().isEmpty()) {
            throw new IllegalArgumentException("预约编号不能为空");
        }
        this.reservationId = reservationId.trim();
    }

    public String getRoomId() { return roomId; }

    public void setRoomId(String roomId) {
        if (roomId == null || roomId.trim().isEmpty()) {
            throw new IllegalArgumentException("会议室编号不能为空");
        }
        this.roomId = roomId.trim();
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getTitle() { return title; }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("会议主题不能为空");
        }
        this.title = title.trim();
    }

    public String getDate() { return date; }

    public void setDate(String date) {
        if (date == null || !date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException("日期格式不正确，应为 yyyy-MM-dd");
        }
        this.date = date;
    }

    public String getStartTime() { return startTime; }

    public void setStartTime(String startTime) {
        if (startTime == null || !startTime.matches("\\d{2}:\\d{2}")) {
            throw new IllegalArgumentException("开始时间格式不正确，应为 HH:mm");
        }
        this.startTime = startTime;
    }

    public String getEndTime() { return endTime; }

    public void setEndTime(String endTime) {
        if (endTime == null || !endTime.matches("\\d{2}:\\d{2}")) {
            throw new IllegalArgumentException("结束时间格式不正确，应为 HH:mm");
        }
        this.endTime = endTime;
    }

    public int getAttendees() { return attendees; }

    public void setAttendees(int attendees) {
        if (attendees <= 0 || attendees > 1000) {
            throw new IllegalArgumentException("参会人数必须在1-1000之间");
        }
        this.attendees = attendees;
    }

    public String getStatus() { return status; }

    public void setStatus(String status) {
        if (!STATUS_CONFIRMED.equals(status) &&
            !STATUS_CANCELLED.equals(status) &&
            !STATUS_COMPLETED.equals(status)) {
            throw new IllegalArgumentException("无效的状态值：" + status);
        }
        this.status = status;
    }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    // ========== 业务方法 ==========

    /**
     * 计算会议时长（分钟）
     * @return 时长（分钟），如果时间格式不正确返回 -1
     */
    public int getDurationMinutes() {
        try {
            String[] startParts = startTime.split(":");
            String[] endParts = endTime.split(":");
            int startMin = Integer.parseInt(startParts[0]) * 60 + Integer.parseInt(startParts[1]);
            int endMin = Integer.parseInt(endParts[0]) * 60 + Integer.parseInt(endParts[1]);

            if (endMin <= startMin) {
                return -1; // 结束时间必须晚于开始时间
            }
            return endMin - startMin;
        } catch (Exception e) {
            return -1;
        }
    }

    /** 获取格式化的时长显示 */
    public String getDurationDisplay() {
        int minutes = getDurationMinutes();
        if (minutes < 0) return "时间无效";
        int hours = minutes / 60;
        int mins = minutes % 60;
        if (hours > 0) {
            return hours + "小时" + (mins > 0 ? mins + "分钟" : "");
        }
        return mins + "分钟";
    }

    /**
     * 检测与另一预约的时间冲突
     * @param other 另一个预约
     * @return 如果时间重叠返回 true
     */
    public boolean conflictsWith(Reservation other) {
        if (other == null) return false;
        if (!this.date.equals(other.date)) return false;
        if (!this.roomId.equals(other.roomId)) return false;
        if (STATUS_CANCELLED.equals(other.status)) return false;

        return this.startTime.compareTo(other.endTime) < 0
                && other.startTime.compareTo(this.endTime) < 0;
    }

    /** 判断预约是否可以取消 */
    public boolean canCancel() {
        return STATUS_CONFIRMED.equals(status);
    }

    /** 判断预约是否已经过期（日期已过） */
    public boolean isExpired() {
        return date.compareTo(java.time.LocalDate.now().toString()) < 0;
    }

    /** 获取预约时间描述 */
    public String getTimeDescription() {
        return date + " " + startTime + "-" + endTime;
    }

    /** 获取简要信息 */
    public String getBriefInfo() {
        return String.format("[%s] %s | 会议室:%s | %s | %d人参会 | %s",
                reservationId, title, roomId, getTimeDescription(), attendees, status);
    }

    // ========== 工厂方法 ==========

    /** 生成预约编号 */
    public static String generateReservationId() {
        String datePart = java.time.LocalDate.now().toString().replace("-", "");
        int seq = (int) (System.currentTimeMillis() % 10000);
        return String.format("RSV-%s-%04d", datePart, seq);
    }

    /** 创建快速预约 */
    public static Reservation createQuickReservation(
            String roomId, String userId, String userName,
            String title, String date, String startTime, String endTime, int attendees) {
        return new Reservation(
                generateReservationId(), roomId, userId, userName,
                title, date, startTime, endTime, attendees
        );
    }

    // ========== 序列化方法 ==========

    /** 转换为CSV格式 */
    public String toCsvString() {
        return reservationId + "," + roomId + "," + userId + ","
                + userName + "," + title + "," + date + ","
                + startTime + "," + endTime + "," + attendees + ","
                + status + "," + (remarks != null ? remarks : "");
    }

    /** 从CSV行解析创建 */
    public static Reservation fromCsvString(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            throw new IllegalArgumentException("CSV数据为空");
        }
        String[] fields = csvLine.split(",", -1);
        if (fields.length < 11) {
            throw new IllegalArgumentException("CSV格式不正确，需要11个字段，实际：" + fields.length);
        }
        return new Reservation(
                fields[0], fields[1], fields[2], fields[3],
                fields[4], fields[5], fields[6], fields[7],
                Integer.parseInt(fields[8]), fields[9], fields[10]
        );
    }

    /** 获取CSV表头 */
    public static String getCsvHeader() {
        return "预约编号,会议室编号,预约人工号,预约人姓名,会议主题,日期,开始时间,结束时间,参会人数,状态,备注";
    }

    // ========== 重写 Object 方法 ==========

    @Override
    public String toString() {
        return String.format(
            "Reservation{编号='%s', 会议室='%s', 预约人='%s', 主题='%s', " +
            "时间='%s %s-%s', 人数=%d, 状态='%s'}",
            reservationId, roomId, userName, title,
            date, startTime, endTime, attendees, status
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Reservation other = (Reservation) obj;
        return reservationId != null && reservationId.equals(other.reservationId);
    }

    @Override
    public int hashCode() {
        return reservationId != null ? reservationId.hashCode() : 0;
    }

    @Override
    public int compareTo(Reservation other) {
        if (this.reservationId == null && other.reservationId == null) return 0;
        if (this.reservationId == null) return -1;
        if (other.reservationId == null) return 1;

        // 先按日期排序，再按开始时间排序
        int dateCmp = this.date.compareTo(other.date);
        if (dateCmp != 0) return dateCmp;
        return this.startTime.compareTo(other.startTime);
    }
}
