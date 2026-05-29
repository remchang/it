package meetingroom;

import java.io.*;
import java.util.*;

/**
 * 预约管理业务类 — 负责会议室预约的核心业务逻辑与系统主控
 * 提供预约增删改查、冲突检测、会议室查询和统计功能
 *
 * @author 颜赫
 * @version 1.0
 * @since 2026-05-30
 */
public class ReservationManager {

    // ========== 数据存储 ==========
    private List<MeetingRoom> roomList;
    private List<Reservation> reservationList;
    private RoomDAO roomDAO;

    /** 预约数据文件 */
    private static final String RESERVATION_FILE = "reservations.csv";

    // ========== 构造方法 ==========

    public ReservationManager() {
        this.roomDAO = new RoomDAO("rooms.csv");
        this.roomList = new ArrayList<>();
        this.reservationList = new ArrayList<>();
    }

    /** 初始化：加载会议室数据和预约记录 */
    public void initialize() {
        roomList = roomDAO.loadRooms();
        reservationList = loadReservations();
        System.out.println("系统初始化完成 — " + roomList.size() + " 间会议室, "
                + reservationList.size() + " 条预约记录");
    }

    // ========== 预约管理 CRUD ==========

    /** 添加预约（带冲突检测） */
    public String addReservation(Reservation res) {
        if (res == null) return "预约信息不能为空";

        // 检查会议室是否存在
        MeetingRoom room = findRoomById(res.getRoomId());
        if (room == null) return "会议室 " + res.getRoomId() + " 不存在";
        if (!room.isAvailable()) return "会议室 " + res.getRoomId() + " 当前" + room.getStatus();

        // 检查容纳人数
        if (res.getAttendees() > room.getCapacity()) {
            return "参会人数(" + res.getAttendees() + ")超过会议室容量(" + room.getCapacity() + ")";
        }

        // 检查时间冲突
        List<Reservation> conflicts = findConflicts(res);
        if (!conflicts.isEmpty()) {
            return "预约时间与已有预约冲突：" + conflicts.get(0).getTitle()
                    + " (" + conflicts.get(0).getTimeDescription() + ")";
        }

        reservationList.add(res);
        return "预约成功：" + res.getReservationId();
    }

    /** 取消预约 */
    public String cancelReservation(String reservationId) {
        Reservation res = findReservationById(reservationId);
        if (res == null) return "未找到预约记录：" + reservationId;
        if (!res.canCancel()) return "该预约状态为" + res.getStatus() + "，无法取消";
        res.setStatus(Reservation.STATUS_CANCELLED);
        return "已取消预约：" + reservationId;
    }

    /** 完成预约 */
    public String completeReservation(String reservationId) {
        Reservation res = findReservationById(reservationId);
        if (res == null) return "未找到预约记录：" + reservationId;
        if (Reservation.STATUS_CANCELLED.equals(res.getStatus())) return "已取消的预约无法完成";
        res.setStatus(Reservation.STATUS_COMPLETED);
        return "预约已完成：" + reservationId;
    }

    /** 按ID查找预约 */
    public Reservation findReservationById(String id) {
        for (Reservation r : reservationList) {
            if (r.getReservationId().equals(id)) return r;
        }
        return null;
    }

    /** 查找与指定预约冲突的记录 */
    public List<Reservation> findConflicts(Reservation target) {
        List<Reservation> conflicts = new ArrayList<>();
        for (Reservation r : reservationList) {
            if (Reservation.STATUS_CANCELLED.equals(r.getStatus())) continue;
            if (r.getReservationId().equals(target.getReservationId())) continue;
            if (target.conflictsWith(r)) {
                conflicts.add(r);
            }
        }
        return conflicts;
    }

    /** 删除预约记录 */
    public boolean deleteReservation(String reservationId) {
        Reservation res = findReservationById(reservationId);
        if (res == null) return false;
        reservationList.remove(res);
        System.out.println("已删除预约记录：" + reservationId);
        return true;
    }

    // ========== 查询 ==========

    /** 按日期查询预约 */
    public List<Reservation> findByDate(String date) {
        List<Reservation> results = new ArrayList<>();
        for (Reservation r : reservationList) {
            if (r.getDate().equals(date) && !Reservation.STATUS_CANCELLED.equals(r.getStatus())) {
                results.add(r);
            }
        }
        results.sort(null);
        return results;
    }

    /** 按会议室查询预约 */
    public List<Reservation> findByRoom(String roomId) {
        List<Reservation> results = new ArrayList<>();
        for (Reservation r : reservationList) {
            if (r.getRoomId().equals(roomId)) results.add(r);
        }
        results.sort(null);
        return results;
    }

    /** 按预约人查询 */
    public List<Reservation> findByUser(String userId) {
        List<Reservation> results = new ArrayList<>();
        for (Reservation r : reservationList) {
            if (userId.equals(r.getUserId())) results.add(r);
        }
        results.sort(null);
        return results;
    }

    /** 查找可用会议室 */
    public List<MeetingRoom> findAvailableRooms(String date, String startTime,
                                                  String endTime, int minCapacity) {
        List<MeetingRoom> available = new ArrayList<>();
        for (MeetingRoom room : roomList) {
            if (!room.isAvailable()) continue;
            if (room.getCapacity() < minCapacity) continue;
            // 检查该时段是否有冲突预约
            boolean hasConflict = false;
            for (Reservation r : reservationList) {
                if (Reservation.STATUS_CANCELLED.equals(r.getStatus())) continue;
                if (!r.getRoomId().equals(room.getRoomId())) continue;
                if (!r.getDate().equals(date)) continue;
                // 时间重叠检测
                if (startTime.compareTo(r.getEndTime()) < 0
                        && r.getStartTime().compareTo(endTime) < 0) {
                    hasConflict = true;
                    break;
                }
            }
            if (!hasConflict) available.add(room);
        }
        return available;
    }

    /** 按ID查找会议室 */
    public MeetingRoom findRoomById(String roomId) {
        for (MeetingRoom r : roomList) {
            if (r.getRoomId().equals(roomId)) return r;
        }
        return null;
    }

    /** 获取所有可用会议室 */
    public List<MeetingRoom> getAvailableRooms() {
        List<MeetingRoom> available = new ArrayList<>();
        for (MeetingRoom r : roomList) {
            if (r.isAvailable()) available.add(r);
        }
        return available;
    }

    // ========== 统计 ==========

    /** 获取预约总数 */
    public int getReservationCount() {
        return reservationList.size();
    }

    /** 获取某日预约数 */
    public int getDailyCount(String date) {
        int count = 0;
        for (Reservation r : reservationList) {
            if (r.getDate().equals(date) && !Reservation.STATUS_CANCELLED.equals(r.getStatus())) {
                count++;
            }
        }
        return count;
    }

    /** 获取各会议室使用频率 */
    public Map<String, Long> getRoomUsageStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        for (Reservation r : reservationList) {
            if (!Reservation.STATUS_CANCELLED.equals(r.getStatus())) {
                stats.merge(r.getRoomId(), 1L, Long::sum);
            }
        }
        return stats;
    }

    /** 按状态统计预约数 */
    public Map<String, Long> getStatusStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        for (Reservation r : reservationList) {
            stats.merge(r.getStatus(), 1L, Long::sum);
        }
        return stats;
    }

    // ========== 数据持久化 ==========

    /** 加载预约记录 */
    private List<Reservation> loadReservations() {
        List<Reservation> list = new ArrayList<>();
        File file = new File(RESERVATION_FILE);
        if (!file.exists()) {
            System.out.println("预约数据文件不存在，从空列表开始。");
            return list;
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("预约编号")) continue;
                try {
                    list.add(Reservation.fromCsvString(line));
                } catch (Exception e) {
                    System.err.println("解析预约数据失败：" + e.getMessage());
                }
            }
            System.out.println("已加载 " + list.size() + " 条预约记录");
        } catch (IOException e) {
            System.err.println("读取预约文件失败：" + e.getMessage());
        }
        return list;
    }

    /** 保存预约记录 */
    public boolean saveReservations() {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(RESERVATION_FILE), "UTF-8"))) {
            writer.write("# 校园会议室预约管理系统 - 预约数据文件");
            writer.newLine();
            writer.write("# 保存时间：" + java.time.LocalDateTime.now());
            writer.newLine();
            writer.write("# " + Reservation.getCsvHeader());
            writer.newLine();
            for (Reservation r : reservationList) {
                writer.write(r.toCsvString());
                writer.newLine();
            }
            System.out.println("已保存 " + reservationList.size() + " 条预约记录");
            return true;
        } catch (IOException e) {
            System.err.println("保存预约数据失败：" + e.getMessage());
            return false;
        }
    }

    /** 关机前的全部保存 */
    public void saveAll() {
        roomDAO.saveRooms(roomList);
        saveReservations();
        roomDAO.backupData();
    }

    // ========== 获取全部数据 ==========

    public List<MeetingRoom> getRoomList() { return new ArrayList<>(roomList); }
    public List<Reservation> getReservationList() { return new ArrayList<>(reservationList); }

    // =====================================================================
    //  主程序 — 控制台交互界面
    // =====================================================================
    public static void main(String[] args) {
        ReservationManager manager = new ReservationManager();
        manager.initialize();
        Scanner scanner = new Scanner(System.in);

        displayWelcome();
        boolean running = true;
        while (running) {
            displayMenu();
            System.out.print("请选择操作 [0-12]：");
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("请输入有效数字！");
                continue;
            }

            switch (choice) {
                case 1  -> doAddReservation(manager, scanner);
                case 2  -> doCancelReservation(manager, scanner);
                case 3  -> doViewByDate(manager, scanner);
                case 4  -> doViewByRoom(manager, scanner);
                case 5  -> doFindAvailable(manager, scanner);
                case 6  -> doListRooms(manager);
                case 7  -> doRoomDetail(manager, scanner);
                case 8  -> doSearchRooms(manager, scanner);
                case 9  -> doViewAllReservations(manager);
                case 10 -> doStatistics(manager);
                case 11 -> doExportData(manager, scanner);
                case 12 -> doCompleteReservation(manager, scanner);
                case 0  -> { running = false; doExit(manager); }
                default -> System.out.println("无效选项，请重新输入！");
            }
            if (running) {
                System.out.print("\n按回车键继续...");
                scanner.nextLine();
            }
        }
        scanner.close();
    }

    // ========== 主界面显示 ==========

    private static void displayWelcome() {
        System.out.println("\n╔════════════════════════════════════════════════════╗");
        System.out.println("║                                                    ║");
        System.out.println("║          校 园 会 议 室 预 约 管 理 系 统          ║");
        System.out.println("║      Campus Meeting Room Reservation System        ║");
        System.out.println("║                                                    ║");
        System.out.println("║          版本：V1.0    日期：2026-05              ║");
        System.out.println("║                                                    ║");
        System.out.println("╚════════════════════════════════════════════════════╝");
    }

    private static void displayMenu() {
        System.out.println("\n┌──────────────────────────────────────────────────┐");
        System.out.println("│  1. 新增预约      2. 取消预约   3. 按日期查询    │");
        System.out.println("│  4. 按会议室查询  5. 查找可用   6. 会议室列表    │");
        System.out.println("│  7. 会议室详情    8. 搜索会议室 9. 全部预约      │");
        System.out.println("│ 10. 统计信息     11. 导出数据  12. 完成预约      │");
        System.out.println("│  0. 退出系统                                      │");
        System.out.println("└──────────────────────────────────────────────────┘");
    }

    // ========== 功能操作 ==========

    private static void doAddReservation(ReservationManager mgr, Scanner sc) {
        System.out.println("\n--- 新增预约 ---");
        System.out.print("会议室编号：");          String roomId = sc.nextLine().trim();
        System.out.print("预约人工号/学号：");    String userId = sc.nextLine().trim();
        System.out.print("预约人姓名：");          String userName = sc.nextLine().trim();
        System.out.print("会议主题：");            String title = sc.nextLine().trim();
        System.out.print("日期(yyyy-MM-dd)：");   String date = sc.nextLine().trim();
        System.out.print("开始时间(HH:mm)：");     String startTime = sc.nextLine().trim();
        System.out.print("结束时间(HH:mm)：");     String endTime = sc.nextLine().trim();
        System.out.print("参会人数：");
        int attendees;
        try { attendees = Integer.parseInt(sc.nextLine().trim()); }
        catch (NumberFormatException e) { System.out.println("人数格式错误！"); return; }

        try {
            Reservation r = new Reservation(
                    Reservation.generateReservationId(), roomId, userId,
                    userName, title, date, startTime, endTime, attendees);
            String result = mgr.addReservation(r);
            System.out.println(result);
        } catch (IllegalArgumentException e) {
            System.out.println("输入数据有误：" + e.getMessage());
        }
    }

    private static void doCancelReservation(ReservationManager mgr, Scanner sc) {
        System.out.println("\n--- 取消预约 ---");
        System.out.print("请输入预约编号：");
        String id = sc.nextLine().trim();
        System.out.println(mgr.cancelReservation(id));
    }

    private static void doViewByDate(ReservationManager mgr, Scanner sc) {
        System.out.println("\n--- 按日期查询预约 ---");
        System.out.print("请输入日期(yyyy-MM-dd)：");
        String date = sc.nextLine().trim();
        List<Reservation> results = mgr.findByDate(date);
        displayReservations(results);
    }

    private static void doViewByRoom(ReservationManager mgr, Scanner sc) {
        System.out.println("\n--- 按会议室查询预约 ---");
        System.out.print("请输入会议室编号：");
        String roomId = sc.nextLine().trim();
        List<Reservation> results = mgr.findByRoom(roomId);
        displayReservations(results);
    }

    private static void doFindAvailable(ReservationManager mgr, Scanner sc) {
        System.out.println("\n--- 查找可用会议室 ---");
        System.out.print("日期(yyyy-MM-dd)：");   String date = sc.nextLine().trim();
        System.out.print("开始时间(HH:mm)：");     String start = sc.nextLine().trim();
        System.out.print("结束时间(HH:mm)：");     String end = sc.nextLine().trim();
        System.out.print("最低容纳人数：");
        int cap;
        try { cap = Integer.parseInt(sc.nextLine().trim()); }
        catch (NumberFormatException e) { cap = 0; }

        List<MeetingRoom> available = mgr.findAvailableRooms(date, start, end, cap);
        if (available.isEmpty()) {
            System.out.println("抱歉，该时段没有满足条件的可用会议室。");
        } else {
            System.out.println("找到 " + available.size() + " 间可用会议室：");
            for (MeetingRoom r : available) {
                System.out.println("  " + r.getBriefInfo());
            }
        }
    }

    private static void doListRooms(ReservationManager mgr) {
        System.out.println("\n========== 会议室列表 ==========");
        List<MeetingRoom> rooms = mgr.getAvailableRooms();
        System.out.printf("%-10s %-12s %-12s %-6s %-8s %-20s %s%n",
                "编号", "名称", "位置", "楼层", "容量", "设备", "规模");
        System.out.println("─".repeat(85));
        for (MeetingRoom r : rooms) {
            System.out.printf("%-10s %-12s %-12s %-6d %-8d %-20s %s%n",
                    r.getRoomId(), r.getRoomName(), r.getBuilding(),
                    r.getFloor(), r.getCapacity(), r.getEquipmentList(),
                    r.getSizeCategory());
        }
    }

    private static void doRoomDetail(ReservationManager mgr, Scanner sc) {
        System.out.println("\n--- 会议室详情 ---");
        System.out.print("请输入会议室编号：");
        String roomId = sc.nextLine().trim();
        MeetingRoom room = mgr.findRoomById(roomId);
        if (room == null) {
            System.out.println("未找到该会议室。");
        } else {
            System.out.println(room);
            List<Reservation> reservations = mgr.findByRoom(roomId);
            System.out.println("该会议室共有 " + reservations.size() + " 条预约记录。");
        }
    }

    private static void doSearchRooms(ReservationManager mgr, Scanner sc) {
        System.out.println("\n--- 搜索会议室 ---");
        System.out.print("请输入关键字：");
        String keyword = sc.nextLine().trim();
        List<MeetingRoom> results = mgr.roomDAO.searchRooms(mgr.roomList, keyword);
        if (results.isEmpty()) {
            System.out.println("未找到匹配的会议室。");
        } else {
            System.out.println("找到 " + results.size() + " 间匹配的会议室：");
            for (MeetingRoom r : results) {
                System.out.println("  " + r.getBriefInfo());
            }
        }
    }

    private static void doViewAllReservations(ReservationManager mgr) {
        System.out.println("\n========== 全部预约记录 ==========");
        displayReservations(mgr.getReservationList());
    }

    private static void doStatistics(ReservationManager mgr) {
        System.out.println("\n========== 统计信息 ==========");
        System.out.println("会议室总数：" + mgr.roomList.size());
        System.out.println("可用会议室：" + mgr.getAvailableRooms().size());
        System.out.println("预约总数：" + mgr.getReservationCount());

        Map<String, Long> statusStats = mgr.getStatusStats();
        System.out.println("\n按状态统计：");
        for (Map.Entry<String, Long> entry : statusStats.entrySet()) {
            System.out.println("  " + entry.getKey() + "：" + entry.getValue() + " 条");
        }

        Map<String, Long> roomStats = mgr.getRoomUsageStats();
        if (!roomStats.isEmpty()) {
            System.out.println("\n会议室使用频率：");
            for (Map.Entry<String, Long> entry : roomStats.entrySet()) {
                MeetingRoom room = mgr.findRoomById(entry.getKey());
                String name = room != null ? room.getRoomName() : entry.getKey();
                System.out.println("  " + entry.getKey() + " " + name + "：" + entry.getValue() + " 次");
            }
        }
    }

    private static void doExportData(ReservationManager mgr, Scanner sc) {
        System.out.println("\n--- 导出数据 ---");
        System.out.println("1. 导出会议室JSON  2. 导出会议室XML");
        System.out.print("请选择：");
        String choice = sc.nextLine().trim();
        switch (choice) {
            case "1" -> mgr.roomDAO.exportToJson(mgr.roomList, "rooms_export.json");
            case "2" -> mgr.roomDAO.exportToXml(mgr.roomList, "rooms_export.xml");
            default -> System.out.println("无效选项");
        }
    }

    private static void doCompleteReservation(ReservationManager mgr, Scanner sc) {
        System.out.println("\n--- 完成预约 ---");
        System.out.print("请输入预约编号：");
        String id = sc.nextLine().trim();
        System.out.println(mgr.completeReservation(id));
    }

    private static void doExit(ReservationManager mgr) {
        System.out.println("\n正在保存数据...");
        mgr.saveAll();
        System.out.println("感谢使用校园会议室预约管理系统，再见！");
    }

    /** 统一格式化显示预约列表 */
    private static void displayReservations(List<Reservation> list) {
        if (list.isEmpty()) {
            System.out.println("暂无预约记录。");
            return;
        }
        System.out.printf("%-20s %-10s %-10s %-14s %-12s %-10s %-6s %s%n",
                "预约编号", "会议室", "预约人", "主题", "日期", "时间", "人数", "状态");
        System.out.println("─".repeat(100));
        for (Reservation r : list) {
            System.out.printf("%-20s %-10s %-10s %-14s %-12s %-10s %-6d %s%n",
                    r.getReservationId(), r.getRoomId(), r.getUserName(),
                    r.getTitle(), r.getDate(),
                    r.getStartTime() + "-" + r.getEndTime(),
                    r.getAttendees(), r.getStatus());
        }
        System.out.println("共 " + list.size() + " 条记录");
    }
}
