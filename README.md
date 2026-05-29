# 校园会议室预约管理系统 (Campus Meeting Room Reservation System)

## 项目简介

本项目是一个基于Java控制台的校园会议室预约管理系统，支持会议室信息管理、预约增删改查、冲突检测、可用会议室查询及多格式数据导出等功能。

## 开发团队

| 姓名 | 角色 | 负责模块 | GitHub分支 |
|------|------|----------|------------|
| 王锐兵 | 质量负责人 & 配置管理员 | MeetingRoom.java 实体类 | wrb |
| 颜赫 | 开发人员 | ReservationManager.java 预约管理 | yanhe |
| 杨海宵 | 开发人员 | RoomDAO.java 数据访问 | yanghaixiao |
| 孙政芳 | 开发人员 | Reservation.java 预约实体 | sunzhengfang |

## 项目结构

```
it/
├── src/meetingroom/
│   ├── MeetingRoom.java          # 会议室实体类（259行）
│   ├── Reservation.java          # 预约记录实体类（305行）
│   ├── RoomDAO.java              # 数据访问层（376行）
│   └── ReservationManager.java   # 预约管理业务逻辑+主程序（545行）
├── .gitignore
└── README.md
```

## 功能特性

- **会议室管理**：会议室信息查询、多条件搜索、可用性筛选
- **预约管理**：增删改查、冲突自动检测、状态流转（确认→完成/取消）
- **时间管理**：按日期查询、时段冲突检测、时长自动计算
- **数据持久化**：CSV文件读写，支持导入导出
- **多格式导出**：支持JSON、XML格式导出会议室数据
- **数据安全**：自动备份和恢复功能
- **统计分析**：使用频率统计、状态统计、每日预约量统计

## 技术栈

- 语言：Java 17+
- 数据格式：CSV / JSON / XML
- 版本控制：Git + GitHub
- 分支策略：Feature Branch Workflow

## 快速开始

### 环境要求

- JDK 17 或更高版本
- Git

### 编译运行

```bash
# 克隆仓库
git clone https://github.com/remchang/it.git
cd it

# 编译
javac -d out src/meetingroom/*.java

# 运行
java -cp out meetingroom.ReservationManager
```

## 代码规范

- **包命名**：全部小写（meetingroom）
- **类命名**：大驼峰（PascalCase）
- **方法/变量**：小驼峰（camelCase）
- **常量**：全大写下划线分隔

## 实验信息

- **课程**：《IT项目管理》
- **实验**：实验3 - 项目质量计划与基于Git的配置管理
- **日期**：2026年5月

## 分支说明

| 分支 | 用途 |
|------|------|
| main | 主分支（稳定版本） |
| wrb | 王锐兵 - MeetingRoom实体类开发 |
| yanhe | 颜赫 - ReservationManager预约管理开发 |
| yanghaixiao | 杨海宵 - RoomDAO数据访问开发 |
| sunzhengfang | 孙政芳 - Reservation预约实体开发 |
