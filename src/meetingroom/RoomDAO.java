package meetingroom;

import java.io.*;
import java.util.*;

/**
 * 会议室数据访问层
 * 负责会议室数据的持久化读写、多格式导出和搜索统计功能
 *
 * @author 杨海宵
 * @version 1.0
 * @since 2026-05-30
 */
public class RoomDAO {

    /** 会议室数据文件路径 */
    private String filePath;

    // ========== 构造方法 ==========

    public RoomDAO() {
        this("rooms.csv");
    }

    public RoomDAO(String filePath) {
        this.filePath = filePath;
    }

    // ========== Getter / Setter ==========

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    // ========== 文件加载与保存 ==========

    /**
     * 从CSV文件加载会议室列表
     * @return 会议室列表
     */
    public List<MeetingRoom> loadRooms() {
        List<MeetingRoom> roomList = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("会议室数据文件不存在，将使用默认数据。文件路径：" + filePath);
            return createDefaultRooms();
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            int lineNumber = 0;
            int successCount = 0;
            int errorCount = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                // 跳过空行、注释行和表头行
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("会议室编号")) {
                    continue;
                }
                try {
                    MeetingRoom room = MeetingRoom.fromCsvString(line);
                    roomList.add(room);
                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                    System.err.println("警告：第" + lineNumber + "行解析失败 - " + e.getMessage());
                }
            }
            System.out.printf("会议室数据加载完成：成功 %d 条, 失败 %d 条%n", successCount, errorCount);
        } catch (IOException e) {
            System.err.println("读取会议室数据文件失败：" + e.getMessage());
            return createDefaultRooms();
        }

        if (roomList.isEmpty()) {
            System.out.println("未加载到有效数据，使用默认会议室数据。");
            return createDefaultRooms();
        }
        return roomList;
    }

    /**
     * 保存会议室列表到CSV文件
     * @param roomList 会议室列表
     * @return 保存是否成功
     */
    public boolean saveRooms(List<MeetingRoom> roomList) {
        if (roomList == null) {
            System.err.println("错误：会议室列表为空，无法保存");
            return false;
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"))) {
            // 写入文件头注释
            writer.write("# 校园会议室预约管理系统 - 会议室数据文件");
            writer.newLine();
            writer.write("# 保存时间：" + java.time.LocalDateTime.now());
            writer.newLine();
            writer.write("# " + MeetingRoom.getCsvHeader());
            writer.newLine();

            for (MeetingRoom room : roomList) {
                writer.write(room.toCsvString());
                writer.newLine();
            }

            System.out.println("成功保存 " + roomList.size() + " 间会议室数据到：" + filePath);
            return true;
        } catch (IOException e) {
            System.err.println("保存会议室数据失败：" + e.getMessage());
            return false;
        }
    }

    // ========== 默认数据 ==========

    /** 创建默认会议室数据 */
    private List<MeetingRoom> createDefaultRooms() {
        List<MeetingRoom> defaults = new ArrayList<>();
        defaults.add(new MeetingRoom("MR-101", "第一会议室", "勤学楼", 1, 15,
                false, true, false, MeetingRoom.STATUS_AVAILABLE, "小型讨论会议室"));
        defaults.add(new MeetingRoom("MR-102", "第二会议室", "勤学楼", 1, 20,
                true, true, false, MeetingRoom.STATUS_AVAILABLE, "标准会议室"));
        defaults.add(new MeetingRoom("MR-201", "创新研讨室", "博学楼", 2, 8,
                false, true, false, MeetingRoom.STATUS_AVAILABLE, "适合小组讨论"));
        defaults.add(new MeetingRoom("MR-202", "视频会议室", "博学楼", 2, 25,
                true, true, true, MeetingRoom.STATUS_AVAILABLE, "支持远程视频会议"));
        defaults.add(new MeetingRoom("MR-301", "学术报告厅", "行知楼", 3, 200,
                true, true, true, MeetingRoom.STATUS_AVAILABLE, "大型学术报告和讲座"));
        defaults.add(new MeetingRoom("MR-302", "多功能厅", "行知楼", 3, 80,
                true, true, true, MeetingRoom.STATUS_AVAILABLE, "适用于中型活动和培训"));
        defaults.add(new MeetingRoom("MR-401", "贵宾接待室", "行政楼", 4, 12,
                true, true, true, MeetingRoom.STATUS_AVAILABLE, "接待重要来宾"));
        defaults.add(new MeetingRoom("MR-402", "评审会议室", "行政楼", 4, 30,
                true, true, false, MeetingRoom.STATUS_AVAILABLE, "项目评审和答辩"));
        System.out.println("已创建 " + defaults.size() + " 间默认会议室");
        return defaults;
    }

    // ========== 导出功能 ==========

    /**
     * 导出会议室数据为JSON格式
     * @param roomList 会议室列表
     * @param jsonPath JSON文件路径
     * @return 导出是否成功
     */
    public boolean exportToJson(List<MeetingRoom> roomList, String jsonPath) {
        if (roomList == null || roomList.isEmpty()) {
            System.err.println("错误：没有数据可以导出");
            return false;
        }
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(jsonPath), "UTF-8"))) {
            writer.write("[");
            writer.newLine();
            for (int i = 0; i < roomList.size(); i++) {
                MeetingRoom r = roomList.get(i);
                writer.write("  {");
                writer.write("\"roomId\":\"" + escapeJson(r.getRoomId()) + "\",");
                writer.write("\"roomName\":\"" + escapeJson(r.getRoomName()) + "\",");
                writer.write("\"building\":\"" + escapeJson(r.getBuilding()) + "\",");
                writer.write("\"floor\":" + r.getFloor() + ",");
                writer.write("\"capacity\":" + r.getCapacity() + ",");
                writer.write("\"hasProjector\":" + r.isHasProjector() + ",");
                writer.write("\"hasWhiteboard\":" + r.isHasWhiteboard() + ",");
                writer.write("\"hasVideoConf\":" + r.isHasVideoConf() + ",");
                writer.write("\"status\":\"" + escapeJson(r.getStatus()) + "\",");
                writer.write("\"equipment\":\"" + escapeJson(r.getEquipmentList()) + "\",");
                writer.write("\"size\":\"" + escapeJson(r.getSizeCategory()) + "\"");
                writer.write("}");
                if (i < roomList.size() - 1) writer.write(",");
                writer.newLine();
            }
            writer.write("]");
            writer.newLine();
            System.out.println("成功导出JSON到：" + jsonPath + "（共 " + roomList.size() + " 间会议室）");
            return true;
        } catch (IOException e) {
            System.err.println("导出JSON失败：" + e.getMessage());
            return false;
        }
    }

    /**
     * 导出会议室数据为XML格式
     * @param roomList 会议室列表
     * @param xmlPath XML文件路径
     * @return 导出是否成功
     */
    public boolean exportToXml(List<MeetingRoom> roomList, String xmlPath) {
        if (roomList == null || roomList.isEmpty()) {
            System.err.println("错误：没有数据可以导出");
            return false;
        }
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(xmlPath), "UTF-8"))) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.newLine();
            writer.write("<meetingRooms>");
            writer.newLine();
            for (MeetingRoom r : roomList) {
                writer.write("  <room>");
                writer.newLine();
                writer.write("    <roomId>" + escapeXml(r.getRoomId()) + "</roomId>");
                writer.newLine();
                writer.write("    <roomName>" + escapeXml(r.getRoomName()) + "</roomName>");
                writer.newLine();
                writer.write("    <building>" + escapeXml(r.getBuilding()) + "</building>");
                writer.newLine();
                writer.write("    <floor>" + r.getFloor() + "</floor>");
                writer.newLine();
                writer.write("    <capacity>" + r.getCapacity() + "</capacity>");
                writer.newLine();
                writer.write("    <equipment>" + escapeXml(r.getEquipmentList()) + "</equipment>");
                writer.newLine();
                writer.write("    <status>" + escapeXml(r.getStatus()) + "</status>");
                writer.newLine();
                writer.write("  </room>");
                writer.newLine();
            }
            writer.write("</meetingRooms>");
            writer.newLine();
            System.out.println("成功导出XML到：" + xmlPath + "（共 " + roomList.size() + " 间会议室）");
            return true;
        } catch (IOException e) {
            System.err.println("导出XML失败：" + e.getMessage());
            return false;
        }
    }

    // ========== 搜索功能 ==========

    /**
     * 根据关键字搜索会议室
     * @param roomList 会议室列表
     * @param keyword 搜索关键字
     * @return 匹配的会议室列表
     */
    public List<MeetingRoom> searchRooms(List<MeetingRoom> roomList, String keyword) {
        List<MeetingRoom> results = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();
        for (MeetingRoom room : roomList) {
            if (room.getRoomId().toLowerCase().contains(lowerKeyword) ||
                room.getRoomName().toLowerCase().contains(lowerKeyword) ||
                room.getBuilding().toLowerCase().contains(lowerKeyword) ||
                room.getDescription().toLowerCase().contains(lowerKeyword) ||
                room.getEquipmentList().toLowerCase().contains(lowerKeyword)) {
                results.add(room);
            }
        }
        return results;
    }

    /**
     * 按条件筛选会议室
     * @param roomList 会议室列表
     * @param minCapacity 最低容纳人数
     * @param needProjector 是否需要投影仪
     * @param needVideoConf 是否需要视频会议
     * @return 筛选结果
     */
    public List<MeetingRoom> filterRooms(List<MeetingRoom> roomList,
                                          int minCapacity, boolean needProjector,
                                          boolean needVideoConf) {
        List<MeetingRoom> results = new ArrayList<>();
        for (MeetingRoom room : roomList) {
            if (!room.isAvailable()) continue;
            if (room.getCapacity() < minCapacity) continue;
            if (needProjector && !room.isHasProjector()) continue;
            if (needVideoConf && !room.isHasVideoConf()) continue;
            results.add(room);
        }
        return results;
    }

    // ========== 数据维护 ==========

    /** 检查数据文件是否存在 */
    public boolean fileExists() {
        return new File(filePath).exists() && new File(filePath).isFile();
    }

    /** 备份数据文件 */
    public boolean backupData() {
        if (!fileExists()) {
            System.err.println("源文件不存在，无法备份");
            return false;
        }
        String backupPath = filePath + ".bak";
        try {
            java.nio.file.Files.copy(
                    new File(filePath).toPath(),
                    new File(backupPath).toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("数据已备份到：" + backupPath);
            return true;
        } catch (IOException e) {
            System.err.println("备份失败：" + e.getMessage());
            return false;
        }
    }

    /** 从备份恢复数据 */
    public boolean restoreFromBackup() {
        String backupPath = filePath + ".bak";
        if (!new File(backupPath).exists()) {
            System.err.println("备份文件不存在：" + backupPath);
            return false;
        }
        try {
            java.nio.file.Files.copy(
                    new File(backupPath).toPath(),
                    new File(filePath).toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("已从备份恢复数据");
            return true;
        } catch (IOException e) {
            System.err.println("恢复失败：" + e.getMessage());
            return false;
        }
    }

    /** 获取数据文件统计信息 */
    public Map<String, Object> getDataStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        File file = new File(filePath);
        stats.put("文件路径", filePath);
        stats.put("文件大小(KB)", file.exists() ? Math.round(file.length() / 1024.0 * 100.0) / 100.0 : 0);
        stats.put("最后修改时间", file.exists() ? new java.util.Date(file.lastModified()).toString() : "文件不存在");
        stats.put("是否为文件", file.isFile());
        return stats;
    }

    /** 统计文件行数 */
    public int countFileLines() {
        if (!fileExists()) return 0;
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            while (reader.readLine() != null) count++;
        } catch (IOException e) {
            System.err.println("读取文件失败：" + e.getMessage());
        }
        return count;
    }

    // ========== 工具方法 ==========

    /** JSON字符串转义 */
    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    /** XML字符串转义 */
    private String escapeXml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&apos;");
    }
}
