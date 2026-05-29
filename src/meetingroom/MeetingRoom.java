package meetingroom;

/**
 * 会议室实体类
 * 封装会议室的基本信息，提供数据校验、格式转换和工具方法
 *
 * @author 王锐兵
 * @version 1.0
 * @since 2026-05-30
 */
public class MeetingRoom implements Comparable<MeetingRoom> {

    // ========== 基本属性 ==========
    private String roomId;          // 会议室编号，如 MR-101
    private String roomName;        // 会议室名称，如 "第一会议室"
    private String building;        // 所在教学楼/楼宇
    private int floor;              // 所在楼层
    private int capacity;           // 容纳人数
    private boolean hasProjector;   // 是否有投影仪
    private boolean hasWhiteboard;  // 是否有白板
    private boolean hasVideoConf;   // 是否支持视频会议
    private String status;          // 状态：可用、维修中、已预约
    private String description;     // 描述信息

    // ========== 状态常量 ==========
    public static final String STATUS_AVAILABLE = "可用";
    public static final String STATUS_MAINTENANCE = "维修中";
    public static final String STATUS_RESERVED = "已预约";

    // ========== 构造方法 ==========

    /** 默认构造方法 */
    public MeetingRoom() {
        this.status = STATUS_AVAILABLE;
    }

    /** 完整构造方法 */
    public MeetingRoom(String roomId, String roomName, String building, int floor,
                       int capacity, boolean hasProjector, boolean hasWhiteboard,
                       boolean hasVideoConf, String status, String description) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.building = building;
        this.floor = floor;
        this.capacity = capacity;
        this.hasProjector = hasProjector;
        this.hasWhiteboard = hasWhiteboard;
        this.hasVideoConf = hasVideoConf;
        this.status = status != null ? status : STATUS_AVAILABLE;
        this.description = description;
    }

    /** 简化构造方法 — 快速创建会议室 */
    public MeetingRoom(String roomId, String roomName, String building, int floor, int capacity) {
        this(roomId, roomName, building, floor, capacity,
             false, true, false, STATUS_AVAILABLE, "");
    }

    // ========== Getter / Setter（带校验） ==========

    public String getRoomId() { return roomId; }

    public void setRoomId(String roomId) {
        if (roomId == null || roomId.trim().isEmpty()) {
            throw new IllegalArgumentException("会议室编号不能为空");
        }
        this.roomId = roomId.trim();
    }

    public String getRoomName() { return roomName; }

    public void setRoomName(String roomName) {
        if (roomName == null || roomName.trim().isEmpty()) {
            throw new IllegalArgumentException("会议室名称不能为空");
        }
        this.roomName = roomName.trim();
    }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public int getFloor() { return floor; }

    public void setFloor(int floor) {
        if (floor < -3 || floor > 100) {
            throw new IllegalArgumentException("楼层必须在-3到100之间");
        }
        this.floor = floor;
    }

    public int getCapacity() { return capacity; }

    public void setCapacity(int capacity) {
        if (capacity <= 0 || capacity > 1000) {
            throw new IllegalArgumentException("容纳人数必须在1-1000之间");
        }
        this.capacity = capacity;
    }

    public boolean isHasProjector() { return hasProjector; }
    public void setHasProjector(boolean hasProjector) { this.hasProjector = hasProjector; }

    public boolean isHasWhiteboard() { return hasWhiteboard; }
    public void setHasWhiteboard(boolean hasWhiteboard) { this.hasWhiteboard = hasWhiteboard; }

    public boolean isHasVideoConf() { return hasVideoConf; }
    public void setHasVideoConf(boolean hasVideoConf) { this.hasVideoConf = hasVideoConf; }

    public String getStatus() { return status; }

    public void setStatus(String status) {
        if (!STATUS_AVAILABLE.equals(status) &&
            !STATUS_MAINTENANCE.equals(status) &&
            !STATUS_RESERVED.equals(status)) {
            throw new IllegalArgumentException("无效的状态值：" + status);
        }
        this.status = status;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // ========== 工具方法 ==========

    /** 获取完整位置描述 */
    public String getFullLocation() {
        return building + " " + floor + "层 " + roomName;
    }

    /** 获取设备清单字符串 */
    public String getEquipmentList() {
        StringBuilder sb = new StringBuilder();
        if (hasProjector) sb.append("投影仪 ");
        if (hasWhiteboard) sb.append("白板 ");
        if (hasVideoConf) sb.append("视频会议系统 ");
        return sb.length() > 0 ? sb.toString().trim() : "基础配置";
    }

    /** 判断是否可用 */
    public boolean isAvailable() {
        return STATUS_AVAILABLE.equals(status);
    }

    /** 获取规模分类 */
    public String getSizeCategory() {
        if (capacity <= 10) return "小型";
        else if (capacity <= 30) return "中型";
        else if (capacity <= 100) return "大型";
        else return "超大型";
    }

    /** 获取房间简要信息 */
    public String getBriefInfo() {
        return String.format("[%s] %s | %s | %d人 | %s",
                roomId, roomName, getSizeCategory(), capacity, getEquipmentList());
    }

    // ========== 工厂方法 ==========

    /** 快速创建小型会议室 */
    public static MeetingRoom createSmallRoom(String roomId, String roomName, String building, int floor) {
        return new MeetingRoom(roomId, roomName, building, floor, 8, false, true, false, STATUS_AVAILABLE, "小型会议室");
    }

    /** 快速创建大型报告厅 */
    public static MeetingRoom createLectureHall(String roomId, String roomName, String building, int floor) {
        return new MeetingRoom(roomId, roomName, building, floor, 200, true, true, true, STATUS_AVAILABLE, "大型报告厅");
    }

    /** 快速创建视频会议室 */
    public static MeetingRoom createVideoRoom(String roomId, String roomName, String building, int floor) {
        return new MeetingRoom(roomId, roomName, building, floor, 20, true, true, true, STATUS_AVAILABLE, "视频会议室");
    }

    // ========== 数据校验 ==========

    /** 校验会议室数据完整性 */
    public boolean isValid() {
        return roomId != null && !roomId.trim().isEmpty()
                && roomName != null && !roomName.trim().isEmpty()
                && building != null && !building.trim().isEmpty()
                && capacity > 0 && capacity <= 1000
                && status != null;
    }

    /** 数据校验（返回错误信息） */
    public String validate() {
        if (roomId == null || roomId.trim().isEmpty()) return "会议室编号不能为空";
        if (roomName == null || roomName.trim().isEmpty()) return "会议室名称不能为空";
        if (building == null || building.trim().isEmpty()) return "楼宇信息不能为空";
        if (capacity <= 0 || capacity > 1000) return "容纳人数范围不正确(1-1000)";
        if (floor < -3 || floor > 100) return "楼层范围不正确(-3~100)";
        return null; // 校验通过
    }

    // ========== 序列化方法 ==========

    /** 转换为CSV格式 */
    public String toCsvString() {
        return roomId + "," + roomName + "," + building + "," + floor + ","
                + capacity + "," + hasProjector + "," + hasWhiteboard + ","
                + hasVideoConf + "," + status + "," + (description != null ? description : "");
    }

    /** 从CSV行解析创建 */
    public static MeetingRoom fromCsvString(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            throw new IllegalArgumentException("CSV数据为空");
        }
        String[] fields = csvLine.split(",", -1);
        if (fields.length < 10) {
            throw new IllegalArgumentException("CSV格式不正确，需要10个字段，实际：" + fields.length);
        }
        return new MeetingRoom(
                fields[0], fields[1], fields[2],
                Integer.parseInt(fields[3]), Integer.parseInt(fields[4]),
                Boolean.parseBoolean(fields[5]), Boolean.parseBoolean(fields[6]),
                Boolean.parseBoolean(fields[7]), fields[8], fields[9]
        );
    }

    /** 获取CSV表头 */
    public static String getCsvHeader() {
        return "会议室编号,名称,楼宇,楼层,容纳人数,投影仪,白板,视频会议,状态,描述";
    }

    // ========== 重写 Object 方法 ==========

    @Override
    public String toString() {
        return String.format(
            "MeetingRoom{编号='%s', 名称='%s', 位置='%s %d层', 容纳=%d人, " +
            "设备='%s', 状态='%s', 规模='%s'}",
            roomId, roomName, building, floor, capacity,
            getEquipmentList(), status, getSizeCategory()
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MeetingRoom other = (MeetingRoom) obj;
        return roomId != null && roomId.equals(other.roomId);
    }

    @Override
    public int hashCode() {
        return roomId != null ? roomId.hashCode() : 0;
    }

    @Override
    public int compareTo(MeetingRoom other) {
        if (this.roomId == null && other.roomId == null) return 0;
        if (this.roomId == null) return -1;
        if (other.roomId == null) return 1;
        return this.roomId.compareTo(other.roomId);
    }
}
