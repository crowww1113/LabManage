package com.example.labmanage.config;

import com.example.labmanage.entity.BuildingEntity;
import com.example.labmanage.entity.CampusEntity;
import com.example.labmanage.entity.ClazzEntity;
import com.example.labmanage.entity.Course;
import com.example.labmanage.entity.EquipmentBorrowRecordEntity;
import com.example.labmanage.entity.EquipmentCategoryEntity;
import com.example.labmanage.entity.EquipmentEntity;
import com.example.labmanage.entity.EquipmentLocationEntity;
import com.example.labmanage.entity.EquipmentRepairRecordEntity;
import com.example.labmanage.entity.ExpTask;
import com.example.labmanage.entity.MajorEntity;
import com.example.labmanage.entity.NotificationEntity;
import com.example.labmanage.entity.OperationLogEntity;
import com.example.labmanage.entity.OrgEntity;
import com.example.labmanage.entity.RoleEntity;
import com.example.labmanage.entity.RoomEntity;
import com.example.labmanage.entity.ScheduleApplicationEntity;
import com.example.labmanage.entity.ScheduleReservationEntity;
import com.example.labmanage.entity.SysDictEntity;
import com.example.labmanage.entity.TeachingTaskEntity;
import com.example.labmanage.entity.TermEntity;
import com.example.labmanage.entity.TimeSlotEntity;
import com.example.labmanage.entity.UserEntity;
import com.example.labmanage.entity.UserRoleEntity;
import com.example.labmanage.repository.BuildingRepository;
import com.example.labmanage.repository.CampusRepository;
import com.example.labmanage.repository.ClazzRepository;
import com.example.labmanage.repository.CourseRepository;
import com.example.labmanage.repository.EquipmentBorrowRecordRepository;
import com.example.labmanage.repository.EquipmentCategoryRepository;
import com.example.labmanage.repository.EquipmentLocationRepository;
import com.example.labmanage.repository.EquipmentRepairRecordRepository;
import com.example.labmanage.repository.EquipmentRepository;
import com.example.labmanage.repository.ExpTaskRepository;
import com.example.labmanage.repository.MajorRepository;
import com.example.labmanage.repository.NotificationRepository;
import com.example.labmanage.repository.OperationLogRepository;
import com.example.labmanage.repository.OrgRepository;
import com.example.labmanage.repository.RoleRepository;
import com.example.labmanage.repository.RoomRepository;
import com.example.labmanage.repository.ScheduleApplicationRepository;
import com.example.labmanage.repository.ScheduleReservationRepository;
import com.example.labmanage.repository.SysDictRepository;
import com.example.labmanage.repository.TeachingTaskRepository;
import com.example.labmanage.repository.TermRepository;
import com.example.labmanage.repository.TimeSlotRepository;
import com.example.labmanage.repository.UserRepository;
import com.example.labmanage.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final CampusRepository campusRepository;
    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;
    private final SysDictRepository sysDictRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final MajorRepository majorRepository;
    private final ClazzRepository clazzRepository;
    private final OrgRepository orgRepository;
    private final CourseRepository courseRepository;
    private final ExpTaskRepository expTaskRepository;
    private final TermRepository termRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final TeachingTaskRepository teachingTaskRepository;
    private final ScheduleReservationRepository scheduleReservationRepository;
    private final ScheduleApplicationRepository scheduleApplicationRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentCategoryRepository equipmentCategoryRepository;
    private final EquipmentLocationRepository equipmentLocationRepository;
    private final EquipmentBorrowRecordRepository equipmentBorrowRecordRepository;
    private final EquipmentRepairRecordRepository equipmentRepairRecordRepository;
    private final OperationLogRepository operationLogRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // ===== Step 1: 清理旧脏数据 =====
        cleanupOldDirtyData();

        // ===== Step 2: 初始化基准参考数据（幂等） =====
        initCampuses();
        initBuildings();
        initRooms();
        initDict();
        initTerms();

        // ===== Step 3: 原有业务初始化（幂等） =====
        initRoles();
        initUsers();
        initOrgs();
        initMajors();
        initClazzes();
        initCourses();
        initTimeSlots();
        initTeachingTasks();
        initExpTasks();

        // ===== Step 4: 测试业务数据（使用新楼栋/房间体系） =====
        initScheduleReservations();
        initMoreReservations();
        initScheduleApplications();
        initScheduleApplicationsExtra();
        fixApplicationTypes();

        // ===== Step 5: 设备管理模块测试数据 =====
        migrateEquipmentLocations();
        initEquipmentCategories();
        initEquipmentLocations();
        initEquipmentData();
        initEquipmentBorrowRecords();
        initEquipmentRepairRecords();
        initOperationLogs();
        initNotifications();
    }

    // ========================================================================
    // Step 1: 清理
    // ========================================================================

    private void cleanupOldDirtyData() {
        jdbcTemplate.update("UPDATE schedule_reservation SET deleted = true WHERE deleted = false");
        jdbcTemplate.update("UPDATE schedule_usage_record SET deleted = true WHERE deleted = false");
        jdbcTemplate.update("UPDATE schedule_application SET deleted = true WHERE deleted = false");
        jdbcTemplate.update("DELETE FROM schedule_operation_log");
        jdbcTemplate.update("DELETE FROM schedule_notice");
        // 清理设备借还旧数据（因为审批流程升级为两级审批）
        jdbcTemplate.update("DELETE FROM equipment_borrow_record");
        // 清理设备维修旧数据
        jdbcTemplate.update("DELETE FROM equipment_repair_record");
        // 清理操作日志和消息通知旧数据
        jdbcTemplate.update("DELETE FROM operation_log");
        jdbcTemplate.update("DELETE FROM notification");
        // 清理基准参考数据（先删子表，再删父表）
        jdbcTemplate.update("DELETE FROM sys_room");
        jdbcTemplate.update("DELETE FROM sys_building");
        jdbcTemplate.update("DELETE FROM sys_dict");
        System.out.println("[DataInitializer] 旧业务数据 + 基准参考数据 + 设备借还数据清理完成");
    }

    // ========================================================================
    // Step 2: 基准参考数据
    // ========================================================================

    private void initCampuses() {
        if (campusRepository.count() > 0) return;
        campusRepository.saveAll(List.of(
                createCampus("主校区", "主校区地址"),
                createCampus("分校区", "分校区地址")
        ));
        System.out.println("[DataInitializer] 已初始化 2 个校区");
    }

    private void initBuildings() {
        if (buildingRepository.count() > 0) return;
        buildingRepository.saveAll(List.of(
                createBuilding("实验大楼", "主校区", 1),
                createBuilding("理工楼", "主校区", 2),
                createBuilding("文科楼", "主校区", 3)
        ));
        System.out.println("[DataInitializer] 已初始化 3 栋楼");
    }

    private void initRooms() {
        Map<String, Long> buildingMap = buildingRepository.findAll().stream()
                .collect(Collectors.toMap(BuildingEntity::getName, BuildingEntity::getId));

        List<RoomEntity> rooms = new ArrayList<>();
        // 实验大楼
        addRoom(rooms, buildingMap.get("实验大楼"), "101", 1, 60, "120", "计算机基础实验室", "lab");
        addRoom(rooms, buildingMap.get("实验大楼"), "102", 1, 45, "90", "高级计算机实验室", "lab");
        addRoom(rooms, buildingMap.get("实验大楼"), "103", 1, 30, "60", "网络实验室", "lab");
        addRoom(rooms, buildingMap.get("实验大楼"), "201", 2, 50, "100", "物理实验室", "lab");
        addRoom(rooms, buildingMap.get("实验大楼"), "202", 2, 40, "80", "嵌入式实验室", "lab");
        addRoom(rooms, buildingMap.get("实验大楼"), "203", 2, 35, "70", "电子实验室", "lab");
        addRoom(rooms, buildingMap.get("实验大楼"), "301", 3, 65, "130", "人工智能实验室", "lab");
        addRoom(rooms, buildingMap.get("实验大楼"), "302", 3, 25, "50", "小型会议室", "meeting");
        // 理工楼
        addRoom(rooms, buildingMap.get("理工楼"), "101", 1, 55, "110", "机械实验室", "lab");
        addRoom(rooms, buildingMap.get("理工楼"), "102", 1, 40, "80", "材料实验室", "lab");
        addRoom(rooms, buildingMap.get("理工楼"), "201", 2, 35, "70", "化学实验室", "lab");
        addRoom(rooms, buildingMap.get("理工楼"), "202", 2, 50, "100", "土木工程实验室", "lab");
        // 文科楼
        addRoom(rooms, buildingMap.get("文科楼"), "101", 1, 70, "140", "多媒体教室", "classroom");
        addRoom(rooms, buildingMap.get("文科楼"), "102", 1, 30, "60", "学术会议室", "meeting");
        addRoom(rooms, buildingMap.get("文科楼"), "201", 2, 45, "90", "语言实验室", "lab");
        addRoom(rooms, buildingMap.get("文科楼"), "202", 2, 20, "40", "小型讨论室", "meeting");

        roomRepository.saveAll(rooms);
        System.out.println("[DataInitializer] 已初始化 16 个房间");
    }

    private void initDict() {
        List<SysDictEntity> dicts = new ArrayList<>();

        // 1. 修读性质
        addDict(dicts, "course_type", "COMPULSORY", "必修", 1, "课程修读性质");
        addDict(dicts, "course_type", "ELECTIVE", "选修", 2, "课程修读性质");
        addDict(dicts, "course_type", "LIMITED", "限选", 3, "课程修读性质");

        // 2. 课程类别
        addDict(dicts, "course_category", "PUBLIC_COMPULSORY", "公共必修课", 1, "实验项目课程类别");
        addDict(dicts, "course_category", "MAJOR_BASIC_COMPULSORY", "专业基础必修课", 2, "实验项目课程类别");
        addDict(dicts, "course_category", "MAJOR_COMPULSORY", "专业必修课", 3, "实验项目课程类别");
        addDict(dicts, "course_category", "MAJOR_ELECTIVE", "专业选修课", 4, "实验项目课程类别");

        // 3. 考核方式
        addDict(dicts, "assessment_method", "EXAM", "考试", 1, "课程/实训考核方式");
        addDict(dicts, "assessment_method", "CHECK", "考查", 2, "课程/实训考核方式");
        addDict(dicts, "assessment_method", "OPERATION", "操作", 3, "课程/实训考核方式");
        addDict(dicts, "assessment_method", "REPORT", "报告", 4, "课程/实训考核方式");
        addDict(dicts, "assessment_method", "PRACTICE", "实操", 5, "课程/实训考核方式");
        addDict(dicts, "assessment_method", "OTHER", "其他", 6, "课程/实训考核方式");

        // 4. 学生层次
        addDict(dicts, "student_level", "JUNIOR_COLLEGE", "专科", 1, "学生层次");
        addDict(dicts, "student_level", "UNDERGRADUATE", "本科", 2, "学生层次");
        addDict(dicts, "student_level", "GRADUATE", "研究生", 3, "学生层次");

        // 5. 实验类别
        addDict(dicts, "experiment_type", "BASIC", "基础", 1, "实验类别");
        addDict(dicts, "experiment_type", "COMPREHENSIVE", "综合", 2, "实验类别");
        addDict(dicts, "experiment_type", "DESIGN", "设计", 3, "实验类别");

        // 6. 实验要求
        addDict(dicts, "experiment_requirement", "REQUIRED", "必做", 1, "实验要求");
        addDict(dicts, "experiment_requirement", "OPTIONAL", "选做", 2, "实验要求");

        // 7. 是否
        addDict(dicts, "yes_no", "TRUE", "是", 1, "是否类选择");
        addDict(dicts, "yes_no", "FALSE", "否", 2, "是否类选择");

        // 8. 实训组织方式
        addDict(dicts, "training_org_mode", "ON_CAMPUS_CENTRALIZED", "校内集中", 1, "实训教学组织方式");
        addDict(dicts, "training_org_mode", "ON_CAMPUS_DISPERSED", "校内分散", 2, "实训教学组织方式");
        addDict(dicts, "training_org_mode", "OFF_CAMPUS_CENTRALIZED", "校外集中", 3, "实训教学组织方式");
        addDict(dicts, "training_org_mode", "OFF_CAMPUS_DISPERSED", "校外分散", 4, "实训教学组织方式");

        // 9. 组织级别
        addDict(dicts, "org_level", "UNIVERSITY", "校级", 1, "组织级别");
        addDict(dicts, "org_level", "COLLEGE", "院级", 2, "组织级别");
        addDict(dicts, "org_level", "CENTER", "中心级", 3, "组织级别");
        addDict(dicts, "org_level", "LAB", "实验室级", 4, "组织级别");
        addDict(dicts, "org_level", "RESEARCH_GROUP", "课题组级", 5, "组织级别");

        // 10. 房间类型
        addDict(dicts, "room_type", "LAB", "实验室", 1, "房间/场馆类型");
        addDict(dicts, "room_type", "CLASSROOM", "教室", 2, "房间/场馆类型");
        addDict(dicts, "room_type", "MEETING", "会议室", 3, "房间/场馆类型");

        // 11. 教学/设备/安全状态
        addDict(dicts, "check_status", "NORMAL", "正常", 1, "教学/设备/安全情况");
        addDict(dicts, "check_status", "ABNORMAL", "异常", 2, "教学/设备/安全情况");

        // 12. 场馆状态
        addDict(dicts, "venue_status", "FREE", "空闲", 1, "场馆占用状态");
        addDict(dicts, "venue_status", "OCCUPIED", "占用", 2, "场馆占用状态");
        addDict(dicts, "venue_status", "RESERVED", "预约", 3, "场馆占用状态");
        addDict(dicts, "venue_status", "MAINTENANCE", "维修", 4, "场馆占用状态");

        // 13. 业务类型
        addDict(dicts, "biz_type", "RESERVATION", "集中排课", 1, "日志业务类型");
        addDict(dicts, "biz_type", "APPLICATION", "排课申请", 2, "日志业务类型");
        addDict(dicts, "biz_type", "USAGE_RECORD", "使用记录", 3, "日志业务类型");
        addDict(dicts, "biz_type", "NOTICE", "通知消息", 4, "日志业务类型");

        // 14. 排课状态
        addDict(dicts, "schedule_status", "PENDING", "待审批", 1, "排课记录状态");
        addDict(dicts, "schedule_status", "APPROVED", "已通过", 2, "排课记录状态");
        addDict(dicts, "schedule_status", "REJECTED", "已驳回", 3, "排课记录状态");
        addDict(dicts, "schedule_status", "CANCELLED", "已取消", 4, "排课记录状态");
        addDict(dicts, "schedule_status", "IN_USE", "使用中", 5, "排课记录状态");
        addDict(dicts, "schedule_status", "COMPLETED", "已完成", 6, "排课记录状态");

        // 15. 数据范围
        addDict(dicts, "data_scope", "ALL", "全部数据", 1, "权限数据范围");
        addDict(dicts, "data_scope", "SCHOOL", "本校数据", 2, "权限数据范围");
        addDict(dicts, "data_scope", "DEPARTMENT", "本院系数据", 3, "权限数据范围");
        addDict(dicts, "data_scope", "ORG_AND_SUB", "本组织及下级", 4, "权限数据范围");
        addDict(dicts, "data_scope", "SELF", "仅本人数据", 5, "权限数据范围");
        addDict(dicts, "data_scope", "CUSTOM", "自定义组织", 6, "权限数据范围");

        sysDictRepository.saveAll(dicts);
        System.out.println("[DataInitializer] 已初始化 " + dicts.size() + " 条字典数据");
    }

    private void initTerms() {
        if (termRepository.count() > 0) return;

        termRepository.saveAll(List.of(
                createTerm("2024-2025学年第二学期",
                        LocalDate.of(2025, 2, 24), LocalDate.of(2025, 7, 10), 20,
                        "[\"2025-04-04\",\"2025-04-05\",\"2025-04-06\",\"2025-05-01\",\"2025-05-02\",\"2025-05-03\",\"2025-06-28\",\"2025-06-29\",\"2025-06-30\"]",
                        "[\"2025-02-24\",\"2025-02-25\"]",
                        LocalDate.of(2025, 2, 15), LocalDate.of(2025, 3, 1),
                        LocalDate.of(2025, 4, 21), LocalDate.of(2025, 6, 30),
                        LocalDate.of(2025, 9, 1), LocalDate.of(2025, 6, 15),
                        LocalDate.of(2025, 2, 24), LocalDate.of(2025, 4, 18),
                        LocalDate.of(2025, 5, 15), LocalDate.of(2025, 6, 20),
                        LocalDate.of(2025, 6, 1), LocalDate.of(2025, 2, 20),
                        LocalDate.of(2025, 3, 15), LocalDate.of(2025, 2, 25),
                        LocalDate.of(2025, 5, 20), LocalDate.of(2025, 4, 15)),
                createTerm("2025-2026学年第一学期",
                        LocalDate.of(2025, 9, 1), LocalDate.of(2026, 1, 15), 20,
                        "[\"2025-10-01\",\"2025-10-02\",\"2025-10-03\",\"2025-12-30\",\"2025-12-31\",\"2026-01-01\"]",
                        "[\"2025-09-01\",\"2025-09-02\"]",
                        LocalDate.of(2025, 8, 15), LocalDate.of(2025, 9, 15),
                        LocalDate.of(2025, 11, 3), LocalDate.of(2026, 1, 5),
                        LocalDate.of(2026, 2, 20), LocalDate.of(2026, 1, 10),
                        LocalDate.of(2025, 9, 1), LocalDate.of(2025, 10, 23),
                        LocalDate.of(2025, 11, 15), LocalDate.of(2026, 1, 10),
                        LocalDate.of(2025, 12, 1), LocalDate.of(2025, 8, 20),
                        LocalDate.of(2025, 9, 15), LocalDate.of(2025, 9, 2),
                        LocalDate.of(2025, 11, 20), LocalDate.of(2025, 10, 15)),
                createTerm("2025-2026学年第二学期",
                        LocalDate.of(2026, 2, 24), LocalDate.of(2026, 7, 10), 20,
                        "[\"2026-04-04\",\"2026-04-05\",\"2026-04-06\",\"2026-05-01\",\"2026-05-02\",\"2026-05-03\"]",
                        "[\"2026-02-24\",\"2026-02-25\"]",
                        LocalDate.of(2026, 2, 15), LocalDate.of(2026, 3, 1),
                        LocalDate.of(2026, 4, 20), LocalDate.of(2026, 6, 29),
                        LocalDate.of(2026, 9, 1), LocalDate.of(2026, 6, 14),
                        LocalDate.of(2026, 2, 24), LocalDate.of(2026, 4, 17),
                        LocalDate.of(2026, 5, 15), LocalDate.of(2026, 6, 19),
                        LocalDate.of(2026, 6, 1), LocalDate.of(2026, 2, 20),
                        LocalDate.of(2026, 3, 14), LocalDate.of(2026, 2, 25),
                        LocalDate.of(2026, 5, 19), LocalDate.of(2026, 4, 14))
        ));
        System.out.println("[DataInitializer] 已初始化 3 个学期");
    }

    // ========================================================================
    // Step 3: 原有业务初始化（保持幂等）
    // ========================================================================

    private void initRoles() {
        // 幂等纠偏：每次启动确保只有 4 条标准角色
        List<String> expectedCodes = List.of("LAB_ADMIN", "TEACHER", "STUDENT", "EQUIPMENT_ADMIN");
        for (RoleEntity r : roleRepository.findAll()) {
            if (!expectedCodes.contains(r.getCode())) {
                jdbcTemplate.update("DELETE FROM sys_role_permission WHERE role_id = ?", r.getId());
                jdbcTemplate.update("DELETE FROM sys_user_role WHERE role_id = ?", r.getId());
                roleRepository.delete(r);
            }
        }

        upsertRole("LAB_ADMIN", "实验管理员", 1, "全部");
        upsertRole("TEACHER", "授课教师", 2, "本组织");
        upsertRole("STUDENT", "学生/其他用户", 3, "仅本人");
        upsertRole("EQUIPMENT_ADMIN", "设备管理员", 1, "全部");

        // 保证一用户一角色
        deduplicateUserRoles();
    }

    private void initUsers() {
        if (userRepository.count() > 0) return;

        if (userRepository.findByUsername("admin").isEmpty()) {
            UserEntity admin = new UserEntity();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRealName("实验管理员");
            admin.setStatus("启用");
            UserEntity savedAdmin = userRepository.save(admin);
            bindRole(savedAdmin.getId(), "LAB_ADMIN");
        }

        if (userRepository.findByUsername("lab_wang").isEmpty()) {
            UserEntity labAdmin = new UserEntity();
            labAdmin.setUsername("lab_wang");
            labAdmin.setPassword(passwordEncoder.encode("lab123456"));
            labAdmin.setRealName("王老师");
            labAdmin.setJobNo("LAB001");
            labAdmin.setStatus("启用");
            UserEntity savedLabAdmin = userRepository.save(labAdmin);
            bindRole(savedLabAdmin.getId(), "LAB_ADMIN");
        }

        if (userRepository.findByUsername("teacher_liu").isEmpty()) {
            UserEntity teacher = new UserEntity();
            teacher.setUsername("teacher_liu");
            teacher.setPassword(passwordEncoder.encode("teacher123"));
            teacher.setRealName("刘老师");
            teacher.setJobNo("T2021001");
            teacher.setStatus("启用");
            UserEntity savedTeacher = userRepository.save(teacher);
            bindRole(savedTeacher.getId(), "TEACHER");
        }

        if (userRepository.findByUsername("stu_chen").isEmpty()) {
            UserEntity student = new UserEntity();
            student.setUsername("stu_chen");
            student.setPassword(passwordEncoder.encode("student123"));
            student.setRealName("陈同学");
            student.setJobNo("S2023001");
            student.setStatus("启用");
            UserEntity savedStudent = userRepository.save(student);
            bindRole(savedStudent.getId(), "STUDENT");
        }
    }

    private void initOrgs() {
        if (orgRepository.count() > 0) return;
        orgRepository.saveAll(List.of(
                createOrg("SCH-001", "韶关学院", "校级", null, "校级管理员"),
                createOrg("COL-IT", "信息工程学院", "院级", 1L, "张院长"),
                createOrg("COL-EDU", "教育科学学院", "院级", 1L, "李院长"),
                createOrg("COL-MATH", "数学与统计学院", "院级", 1L, "王院长"),
                createOrg("COL-LIT", "文学与传媒学院", "院级", 1L, "赵院长")
        ));
    }

    private void initMajors() {
        if (majorRepository.count() > 0) return;

        majorRepository.saveAll(List.of(
                createMajor("CS", "计算机科学与技术", 2L),
                createMajor("SE", "软件工程", 2L),
                createMajor("NE", "网络工程", 2L),
                createMajor("BD", "大数据技术", 2L)
        ));
    }

    private void initClazzes() {
        if (clazzRepository.count() > 0) return;

        List<MajorEntity> majors = majorRepository.findAll();
        if (majors.size() < 4) return;

        clazzRepository.saveAll(List.of(
                createClazz("CS2301", "计算机2023级1班", majors.get(0).getId(), 1L, "2023级"),
                createClazz("CS2302", "计算机2023级2班", majors.get(0).getId(), 1L, "2023级"),
                createClazz("SE2401", "软件工程2024级1班", majors.get(1).getId(), 1L, "2024级"),
                createClazz("BD2401", "大数据2024级1班", majors.get(3).getId(), 2L, "2024级")
        ));
    }

    private void initCourses() {
        if (courseRepository.count() > 0) return;

        courseRepository.saveAll(List.of(
                createCourse("COURSE001", "计算机应用基础", "Computer Application Basics", "必修", 3.0, 48, 32, 8, 8, 0, "2025-2026-1"),
                createCourse("COURSE002", "Web前端开发", "Web Frontend Development", "必修", 4.0, 64, 32, 16, 16, 0, "2025-2026-1"),
                createCourse("COURSE003", "Java程序设计", "Java Programming", "必修", 4.0, 64, 36, 16, 12, 0, "2025-2026-1"),
                createCourse("COURSE004", "数据库原理与应用", "Database Principle", "必修", 3.5, 56, 30, 14, 12, 0, "2025-2026-1")
        ));
    }

    private void initTimeSlots() {
        if (timeSlotRepository.count() > 0) return;

        timeSlotRepository.saveAll(List.of(
                createTimeSlot("1-2节", LocalTime.of(8, 0), LocalTime.of(9, 40), 1),
                createTimeSlot("3-4节", LocalTime.of(10, 0), LocalTime.of(11, 40), 2),
                createTimeSlot("5-6节", LocalTime.of(14, 0), LocalTime.of(15, 40), 3),
                createTimeSlot("7-8节", LocalTime.of(16, 0), LocalTime.of(17, 40), 4),
                createTimeSlot("9-10节", LocalTime.of(19, 0), LocalTime.of(20, 40), 5)
        ));
    }

    private void initTeachingTasks() {
        List<TermEntity> terms = termRepository.findAll();
        List<ClazzEntity> clazzes = clazzRepository.findAll();
        List<Course> courses = courseRepository.findAll();
        List<UserEntity> teachers = userRepository.findAll().stream()
                .filter(user -> "teacher_liu".equals(user.getUsername()) || "lab_wang".equals(user.getUsername()))
                .toList();

        if (terms.isEmpty() || clazzes.size() < 3 || courses.size() < 3 || teachers.isEmpty()) return;

        Long teacherLiuId = teachers.stream().filter(u -> "teacher_liu".equals(u.getUsername())).findFirst().orElseThrow().getId();
        Long teacherWangId = teachers.stream().filter(u -> "lab_wang".equals(u.getUsername())).findFirst().orElseThrow().getId();

        List<TeachingTaskEntity> tasks = new ArrayList<>();
        upsertTask(tasks, courses.get(0).getId(), terms.get(0).getId(), clazzes.get(0).getId(), List.of(teacherLiuId));
        upsertTask(tasks, courses.get(1).getId(), terms.get(0).getId(), clazzes.get(0).getId(), List.of(teacherLiuId));
        upsertTask(tasks, courses.get(0).getId(), terms.get(0).getId(), clazzes.get(1).getId(), List.of(teacherWangId));
        upsertTask(tasks, courses.get(2).getId(), terms.get(0).getId(), clazzes.get(2).getId(), List.of(teacherLiuId, teacherWangId));

        if (terms.size() >= 2) {
            Long term2Id = terms.get(1).getId();
            upsertTask(tasks, courses.get(2).getId(), term2Id, clazzes.get(2).getId(), List.of(teacherLiuId));
            upsertTask(tasks, courses.get(1).getId(), term2Id, clazzes.get(1).getId(), List.of(teacherLiuId));
            upsertTask(tasks, courses.get(0).getId(), term2Id, clazzes.get(0).getId(), List.of(teacherWangId));
        }
        // 最新学期
        if (terms.size() >= 3) {
            Long term3Id = terms.get(2).getId();
            upsertTask(tasks, courses.get(0).getId(), term3Id, clazzes.get(0).getId(), List.of(teacherWangId));
            upsertTask(tasks, courses.get(1).getId(), term3Id, clazzes.get(1).getId(), List.of(teacherLiuId));
            upsertTask(tasks, courses.get(2).getId(), term3Id, clazzes.get(2).getId(), List.of(teacherLiuId));
        }

        if (!tasks.isEmpty()) {
            teachingTaskRepository.saveAll(tasks);
        }

        // 修复已存在但未分配教师的旧任务
        fixMissingTeacherIds(courses.get(0).getId(), terms.get(0).getId(), clazzes.get(0).getId(), List.of(teacherLiuId));
        fixMissingTeacherIds(courses.get(1).getId(), terms.get(0).getId(), clazzes.get(0).getId(), List.of(teacherLiuId));
        fixMissingTeacherIds(courses.get(0).getId(), terms.get(0).getId(), clazzes.get(1).getId(), List.of(teacherWangId));
        fixMissingTeacherIds(courses.get(2).getId(), terms.get(0).getId(), clazzes.get(2).getId(), List.of(teacherLiuId, teacherWangId));

        if (terms.size() >= 2) {
            Long term2Id = terms.get(1).getId();
            fixMissingTeacherIds(courses.get(2).getId(), term2Id, clazzes.get(2).getId(), List.of(teacherLiuId));
            fixMissingTeacherIds(courses.get(1).getId(), term2Id, clazzes.get(1).getId(), List.of(teacherLiuId));
            fixMissingTeacherIds(courses.get(0).getId(), term2Id, clazzes.get(0).getId(), List.of(teacherWangId));
        }
    }

    private void initExpTasks() {
        if (expTaskRepository.count() > 0) return;

        List<ClazzEntity> clazzes = clazzRepository.findAll();
        if (clazzes.size() < 4) return;

        List<ExpTask> tasks = List.of(
                createExpTask("2025-2026-2", "计算机科学与技术", clazzes.get(0).getId().intValue(), 45, "本科", "Java程序设计", "专业必修课", true, 12, 12, "计算机学院", "软件工程系", "刘老师"),
                createExpTask("2025-2026-2", "计算机科学与技术", clazzes.get(1).getId().intValue(), 42, "本科", "计算机应用基础", "公共必修课", true, 8, 8, "计算机学院", "计算机基础教研室", "王老师"),
                createExpTask("2025-2026-2", "软件工程", clazzes.get(2).getId().intValue(), 48, "本科", "Web前端开发", "专业必修课", true, 16, 16, "计算机学院", "软件工程系", "刘老师"),
                createExpTask("2025-2026-2", "大数据技术", clazzes.get(3).getId().intValue(), 40, "本科", "数据库原理与应用", "专业必修课", true, 12, 12, "计算机学院", "大数据系", "王老师"),
                createExpTask("2025-2026-2", "软件工程", clazzes.get(2).getId().intValue(), 48, "本科", "软件测试基础", "专业选修课", false, 8, 8, "计算机学院", "软件工程系", "刘老师")
        );

        expTaskRepository.saveAll(tasks);
        System.out.println("[DataInitializer] 已初始化 " + tasks.size() + " 条实验教学任务");
    }

    // ========================================================================
    // Step 4: 测试业务数据（使用新楼栋/房间体系）
    // ========================================================================

    private void initScheduleReservations() {
        List<TermEntity> terms = termRepository.findAll();
        if (terms.size() < 2) return;
        // 使用最新学期
        TermEntity latestTerm = terms.stream().max(Comparator.comparing(TermEntity::getId)).orElse(terms.get(terms.size() - 1));
        Long termId = latestTerm.getId();

        List<ScheduleReservationEntity> existing = scheduleReservationRepository
                .findByTermIdAndDeletedFalse(termId);
        if (existing.stream().anyMatch(r -> "实验大楼".equals(r.getBuildingName()) && "101".equals(r.getRoomNumber()))) {
            return;
        }

        List<ClazzEntity> clazzes = clazzRepository.findAll();
        List<Course> courses = courseRepository.findAll();
        List<UserEntity> teachers = userRepository.findAll().stream()
                .filter(user -> "teacher_liu".equals(user.getUsername()) || "lab_wang".equals(user.getUsername()))
                .toList();
        if (clazzes.size() < 4 || courses.size() < 4 || teachers.size() < 2) return;

        Long teacherLiuId = teachers.stream().filter(u -> "teacher_liu".equals(u.getUsername())).findFirst().get().getId();
        Long taskId = findTeachingTaskId(courses.get(2).getId(), termId, clazzes.get(2).getId());
        if (taskId == null) return;

        List<TimeSlotEntity> timeSlots = timeSlotRepository.findAllByOrderBySortOrderAsc();
        if (timeSlots.isEmpty()) return;

        ScheduleReservationEntity res = new ScheduleReservationEntity();
        res.setReservationNo("RES-" + UUID.randomUUID().toString().replace("-", ""));
        res.setTermId(termId);
        res.setTeachingTaskId(taskId);
        res.setCourseId(courses.get(2).getId());
        res.setClazzId(clazzes.get(2).getId());
        res.setTeacherId(teacherLiuId);
        res.setBuildingName("实验大楼");
        res.setRoomNumber("101");
        LocalDate baseMonday = latestTerm.getStartDate().with(java.time.DayOfWeek.MONDAY);
        res.setUseDate(baseMonday);
        res.setWeekNo(1);
        res.setDayOfWeek(1);
        res.setTimeSlotId(timeSlots.get(0).getId());
        res.setStartTime(timeSlots.get(0).getStartTime());
        res.setEndTime(timeSlots.get(0).getEndTime());
        res.setStudentCount(30);
        res.setExperimentContent("Java面向对象编程实验");
        res.setStatus("APPROVED");

        scheduleReservationRepository.save(res);
        System.out.println("[DataInitializer] 已插入基础排课记录 (最新学期/week1/实验大楼101/周一1-2节)");
    }

    private void initMoreReservations() {
        List<TermEntity> terms = termRepository.findAll();
        if (terms.size() < 2) return;
        TermEntity latestTerm = terms.stream().max(Comparator.comparing(TermEntity::getId)).orElse(terms.get(terms.size() - 1));
        Long termId = latestTerm.getId();

        List<ScheduleReservationEntity> existing = scheduleReservationRepository
                .findByTermIdAndDeletedFalse(termId);

        List<ClazzEntity> clazzes = clazzRepository.findAll();
        List<Course> courses = courseRepository.findAll();
        List<UserEntity> teachers = userRepository.findAll().stream()
                .filter(u -> "teacher_liu".equals(u.getUsername()) || "lab_wang".equals(u.getUsername())
                        || "teacher_zhang".equals(u.getUsername()))
                .toList();
        if (clazzes.size() < 4 || courses.size() < 4 || teachers.size() < 2) return;

        Long teacherLiuId = teachers.stream().filter(u -> "teacher_liu".equals(u.getUsername())).findFirst().orElseThrow().getId();
        Long teacherWangId = teachers.stream().filter(u -> "lab_wang".equals(u.getUsername())).findFirst().orElseThrow().getId();

        Long taskJava = findTeachingTaskId(courses.get(2).getId(), termId, clazzes.get(2).getId());
        Long taskWeb = findTeachingTaskId(courses.get(1).getId(), termId, clazzes.get(1).getId());
        Long taskBasic = findTeachingTaskId(courses.get(0).getId(), termId, clazzes.get(0).getId());
        if (taskJava == null || taskWeb == null || taskBasic == null) return;

        List<RoomEntity> rooms = roomRepository.findAll();
        if (rooms.size() < 6) return;

        List<TimeSlotEntity> timeSlots = timeSlotRepository.findAllByOrderBySortOrderAsc();
        if (timeSlots.size() < 5) return;

        LocalDate baseMonday = latestTerm.getStartDate().with(java.time.DayOfWeek.MONDAY);

        // 实验大楼房间（5间）
        List<RoomEntity> labRooms = rooms.stream()
                .filter(r -> r.getBuildingId().equals(buildingRepository.findAll().get(0).getId()))
                .limit(5).toList();
        // 理工楼房间（2间）
        List<RoomEntity> engineeringRooms = rooms.stream()
                .filter(r -> r.getBuildingId().equals(buildingRepository.findAll().get(1).getId()))
                .limit(2).toList();

        List<ScheduleReservationEntity> newReservations = new ArrayList<>();

        // 已批准排课：实验大楼 101~103
        for (int i = 0; i < Math.min(3, labRooms.size()); i++) {
            RoomEntity room = labRooms.get(i);
            if (existing.stream().anyMatch(r -> room.getCode().equals(r.getRoomNumber()))) continue;
            newReservations.add(buildReservation(termId, "实验大楼", room.getCode(),
                    i + 1, 1, timeSlots.get(i % timeSlots.size()), baseMonday,
                    i % 2 == 0 ? teacherLiuId : teacherWangId,
                    i == 0 ? taskJava : (i == 1 ? taskWeb : taskBasic),
                    courses.get(i == 0 ? 2 : (i == 1 ? 1 : 0)),
                    clazzes.get(i == 0 ? 2 : (i == 1 ? 1 : 0)),
                    25 + i * 5, "APPROVED"));
        }

        // 已完成：理工楼 101~102
        for (int i = 0; i < engineeringRooms.size(); i++) {
            RoomEntity room = engineeringRooms.get(i);
            if (existing.stream().anyMatch(r -> room.getCode().equals(r.getRoomNumber()))) continue;
            newReservations.add(buildReservation(termId, "理工楼", room.getCode(),
                    i + 1, 2, timeSlots.get(0), baseMonday,
                    teacherLiuId, taskBasic, courses.get(0), clazzes.get(0), 30, "COMPLETED"));
        }

        scheduleReservationRepository.saveAll(newReservations);
        System.out.println("[DataInitializer] 已扩充排课记录: 新增 " + newReservations.size() + " 条");
    }

    private void initScheduleApplications() {
        List<TermEntity> terms = termRepository.findAll();
        if (terms.size() < 2) return;
        TermEntity latestTerm = terms.stream().max(Comparator.comparing(TermEntity::getId)).orElse(terms.get(terms.size() - 1));
        Long termId = latestTerm.getId();

        List<ScheduleApplicationEntity> existing = scheduleApplicationRepository
                .findByTermIdAndPreferredWeekNoAndPreferredBuildingNameAndStatusAndDeletedFalse(
                        termId, 1, "实验大楼", "APPROVED");

        List<ClazzEntity> clazzes = clazzRepository.findAll();
        List<Course> courses = courseRepository.findAll();
        List<UserEntity> teachers = userRepository.findAll().stream()
                .filter(user -> "teacher_liu".equals(user.getUsername()) || "lab_wang".equals(user.getUsername()))
                .toList();
        if (clazzes.size() < 4 || courses.size() < 4 || teachers.size() < 2) return;

        Long teacherLiuId = teachers.stream().filter(u -> "teacher_liu".equals(u.getUsername())).findFirst().get().getId();
        Long teacherWangId = teachers.stream().filter(u -> "lab_wang".equals(u.getUsername())).findFirst().get().getId();

        boolean hasTeachApp = existing.stream().anyMatch(a -> "102".equals(a.getPreferredRoomNumber()));
        if (!hasTeachApp) {
            ScheduleApplicationEntity teachApp = new ScheduleApplicationEntity();
            teachApp.setApplicationNo("APP-" + UUID.randomUUID().toString().replace("-", ""));
            teachApp.setTermId(termId);
            teachApp.setTeacherId(teacherLiuId);
            teachApp.setProjectName("Web前端开发实验");
            teachApp.setProjectCategory("课程教学项目");
            teachApp.setProjectLeader("刘老师");
            teachApp.setContactPhone("13800138001");
            teachApp.setDuration(java.math.BigDecimal.valueOf(2.0));
            teachApp.setPreferredBuildingName("实验大楼");
            teachApp.setPreferredRoomNumber("102");
            teachApp.setPreferredWeekNo(1);
            teachApp.setPreferredDayOfWeek(3);
            teachApp.setPreferredTimeSlotId(2L);
            teachApp.setStudentCount(35);
            teachApp.setExperimentContent("HTML+CSS+JavaScript综合前端开发实验");
            teachApp.setExperimentRequirement("需安装Node.js和VS Code");
            teachApp.setStatus("APPROVED");
            teachApp.setApplicationType("COURSE");
            teachApp.setReviewedBy(1L);
            teachApp.setReviewedAt(LocalDateTime.now());
            scheduleApplicationRepository.save(teachApp);
            System.out.println("[DataInitializer] 已插入授课申请 (最新学期/week1/实验大楼102)");
        }

        boolean hasTempApp = existing.stream().anyMatch(a -> "103".equals(a.getPreferredRoomNumber()));
        if (!hasTempApp) {
            ScheduleApplicationEntity tempApp = new ScheduleApplicationEntity();
            tempApp.setApplicationNo("APP-" + UUID.randomUUID().toString().replace("-", ""));
            tempApp.setTermId(termId);
            tempApp.setTeacherId(teacherWangId);
            tempApp.setProjectName("计算机基础操作综合实训");
            tempApp.setProjectCategory("开放实验项目");
            tempApp.setProjectLeader("王老师");
            tempApp.setContactPhone("13800138002");
            tempApp.setDuration(java.math.BigDecimal.valueOf(3.0));
            tempApp.setPreferredBuildingName("实验大楼");
            tempApp.setPreferredRoomNumber("103");
            tempApp.setPreferredWeekNo(1);
            tempApp.setPreferredDayOfWeek(4);
            tempApp.setPreferredTimeSlotId(3L);
            tempApp.setStudentCount(40);
            tempApp.setExperimentContent("计算机基础操作综合实训");
            tempApp.setExperimentRequirement("需机房安装Windows 10+及Office套件");
            tempApp.setStatus("APPROVED");
            tempApp.setApplicationType("TEMPORARY");
            tempApp.setReviewedBy(1L);
            tempApp.setReviewedAt(LocalDateTime.now());
            scheduleApplicationRepository.save(tempApp);
            System.out.println("[DataInitializer] 已插入临时预约 (最新学期/week1/实验大楼103)");
        }
    }

    private void initScheduleApplicationsExtra() {
        List<TermEntity> terms = termRepository.findAll();
        if (terms.size() < 2) return;
        TermEntity latestTerm = terms.stream().max(Comparator.comparing(TermEntity::getId)).orElse(terms.get(terms.size() - 1));
        Long termId = latestTerm.getId();

        Course pythonCourse = ensureCourse("COURSE005", "Python程序设计", "Python Programming", "必修", 3.0, 48, 32, 8, 8, 0);
        Course selfExpCourse = ensureCourse("COURSE006", "课外自主实验", "Independent Lab", "选修", 1.0, 16, 0, 0, 16, 0);
        Course algoCourse = ensureCourse("COURSE007", "算法与数据结构", "Algorithms & Data Structures", "必修", 4.0, 64, 48, 8, 8, 0);

        UserEntity teacherZhang = ensureTeacher("teacher_zhang", "张老师", "T2021002");
        UserEntity teacherWang = userRepository.findByUsername("lab_wang").orElse(null);
        UserEntity teacherLiu = userRepository.findByUsername("teacher_liu").orElse(null);
        if (teacherWang == null || teacherLiu == null) return;

        List<Course> courses = courseRepository.findAll();
        List<ClazzEntity> clazzes = clazzRepository.findAll();
        if (clazzes.size() < 4 || courses.size() < 7) return;

        ensureTeachingTaskById(pythonCourse.getId(), termId, clazzes.get(0).getId(), List.of(teacherZhang.getId()));
        ensureTeachingTaskById(selfExpCourse.getId(), termId, clazzes.get(1).getId(), List.of(teacherWang.getId()));
        ensureTeachingTaskById(algoCourse.getId(), termId, clazzes.get(2).getId(), List.of(teacherLiu.getId()));
        ensureTeachingTaskById(courses.get(3).getId(), termId, clazzes.get(3).getId(), List.of(teacherLiu.getId()));

        // 待审批
        List<ScheduleApplicationEntity> pendingApps = scheduleApplicationRepository
                .findByTermIdAndPreferredWeekNoAndPreferredBuildingNameAndStatusAndDeletedFalse(
                        termId, 1, "实验大楼", "PENDING");

        if (pendingApps.stream().noneMatch(a -> "201".equals(a.getPreferredRoomNumber()))) {
            ScheduleApplicationEntity app = new ScheduleApplicationEntity();
            app.setApplicationNo("APP-" + UUID.randomUUID().toString().replace("-", ""));
            app.setTermId(termId);
            app.setTeacherId(teacherZhang.getId());
            app.setProjectName("Python基础语法与数据结构实验");
            app.setProjectCategory("课程教学项目");
            app.setProjectLeader("张老师");
            app.setContactPhone("13800138003");
            app.setDuration(java.math.BigDecimal.valueOf(2.0));
            app.setPreferredBuildingName("实验大楼");
            app.setPreferredRoomNumber("201");
            app.setPreferredWeekNo(1);
            app.setPreferredDayOfWeek(1);
            app.setPreferredTimeSlotId(2L);
            app.setStudentCount(30);
            app.setExperimentContent("Python基础语法与数据结构实验");
            app.setStatus("PENDING");
            app.setApplicationType("COURSE");
            scheduleApplicationRepository.save(app);
        }

        if (pendingApps.stream().noneMatch(a -> "202".equals(a.getPreferredRoomNumber()))) {
            ScheduleApplicationEntity app = new ScheduleApplicationEntity();
            app.setApplicationNo("APP-" + UUID.randomUUID().toString().replace("-", ""));
            app.setTermId(termId);
            app.setTeacherId(teacherWang.getId());
            app.setProjectName("课外自主实验练习");
            app.setProjectCategory("开放实验项目");
            app.setProjectLeader("王老师");
            app.setContactPhone("13800138002");
            app.setDuration(java.math.BigDecimal.valueOf(1.5));
            app.setPreferredBuildingName("实验大楼");
            app.setPreferredRoomNumber("202");
            app.setPreferredWeekNo(1);
            app.setPreferredDayOfWeek(1);
            app.setPreferredTimeSlotId(3L);
            app.setStudentCount(20);
            app.setExperimentContent("课外自主实验练习");
            app.setStatus("PENDING");
            app.setApplicationType("TEMPORARY");
            scheduleApplicationRepository.save(app);
        }

        // 已拒绝
        List<ScheduleApplicationEntity> rejectedApps = scheduleApplicationRepository
                .findByTermIdAndPreferredWeekNoAndPreferredBuildingNameAndStatusAndDeletedFalse(
                        termId, 1, "实验大楼", "REJECTED");

        if (rejectedApps.stream().noneMatch(a -> "203".equals(a.getPreferredRoomNumber()))) {
            ScheduleApplicationEntity app = new ScheduleApplicationEntity();
            app.setApplicationNo("APP-" + UUID.randomUUID().toString().replace("-", ""));
            app.setTermId(termId);
            app.setTeacherId(teacherLiu.getId());
            app.setProjectName("排序与查找算法实验");
            app.setProjectCategory("课程教学项目");
            app.setProjectLeader("刘老师");
            app.setContactPhone("13800138001");
            app.setDuration(java.math.BigDecimal.valueOf(2.0));
            app.setPreferredBuildingName("实验大楼");
            app.setPreferredRoomNumber("203");
            app.setPreferredWeekNo(1);
            app.setPreferredDayOfWeek(5);
            app.setPreferredTimeSlotId(1L);
            app.setStudentCount(30);
            app.setExperimentContent("排序与查找算法实验");
            app.setStatus("REJECTED");
            app.setApplicationType("COURSE");
            app.setReviewComment("实验室资源冲突，已调整为其他时段");
            app.setReviewedBy(1L);
            app.setReviewedAt(LocalDateTime.now());
            scheduleApplicationRepository.save(app);
        }

        // 已通过（分页测试）
        List<ScheduleApplicationEntity> approvedApps = scheduleApplicationRepository
                .findByTermIdAndPreferredWeekNoAndPreferredBuildingNameAndStatusAndDeletedFalse(
                        termId, 1, "实验大楼", "APPROVED");

        if (approvedApps.stream().noneMatch(a -> "301".equals(a.getPreferredRoomNumber()))) {
            ScheduleApplicationEntity app = new ScheduleApplicationEntity();
            app.setApplicationNo("APP-" + UUID.randomUUID().toString().replace("-", ""));
            app.setTermId(termId);
            app.setTeacherId(teacherLiu.getId());
            app.setProjectName("SQL查询优化与索引设计实验");
            app.setProjectCategory("课程教学项目");
            app.setProjectLeader("刘老师");
            app.setContactPhone("13800138001");
            app.setDuration(java.math.BigDecimal.valueOf(2.0));
            app.setPreferredBuildingName("实验大楼");
            app.setPreferredRoomNumber("301");
            app.setPreferredWeekNo(1);
            app.setPreferredDayOfWeek(5);
            app.setPreferredTimeSlotId(2L);
            app.setStudentCount(25);
            app.setExperimentContent("SQL查询优化与索引设计实验");
            app.setStatus("APPROVED");
            app.setApplicationType("COURSE");
            app.setReviewedBy(1L);
            app.setReviewedAt(LocalDateTime.now());
            scheduleApplicationRepository.save(app);
        }

        System.out.println("[DataInitializer] 全状态排课申请测试数据检查完成");
    }

    private void fixApplicationTypes() {
        List<ScheduleApplicationEntity> all = scheduleApplicationRepository.findAll();
        int fixed = 0;
        for (ScheduleApplicationEntity app : all) {
            if (app.getApplicationType() != null) continue;
            String room = app.getPreferredRoomNumber();
            if ("102".equals(room) || "201".equals(room) || "203".equals(room) || "301".equals(room)) {
                app.setApplicationType("COURSE");
            } else if ("103".equals(room) || "202".equals(room)) {
                app.setApplicationType("TEMPORARY");
            } else {
                continue;
            }
            scheduleApplicationRepository.save(app);
            fixed++;
        }
        if (fixed > 0) {
            System.out.println("[DataInitializer] 已修复 " + fixed + " 条记录的 applicationType");
        }
    }

    // ========================================================================
    // 辅助方法
    // ========================================================================

    private void fixMissingTeacherIds(Long courseId, Long termId, Long clazzId, List<Long> teacherIds) {
        teachingTaskRepository.findByTermIdAndClazzId(termId, clazzId).stream()
                .filter(t -> t.getCourseId().equals(courseId))
                .filter(t -> t.getTeacherIdsValue() == null || t.getTeacherIdsValue().isBlank())
                .forEach(t -> {
                    t.setTeacherIds(teacherIds);
                    teachingTaskRepository.save(t);
                });
    }

    private void upsertTask(List<TeachingTaskEntity> tasks, Long courseId, Long termId, Long clazzId, List<Long> teacherIds) {
        boolean exists = teachingTaskRepository.findByTermIdAndClazzId(termId, clazzId).stream()
                .anyMatch(t -> t.getCourseId().equals(courseId));
        if (!exists) {
            tasks.add(createTeachingTask(courseId, termId, clazzId, teacherIds));
        }
    }

    private ScheduleReservationEntity buildReservation(Long termId, String building, String room,
                                                       int weekNo, int dayOfWeek, TimeSlotEntity slot,
                                                       LocalDate baseMonday, Long teacherId, Long taskId,
                                                       Course course, ClazzEntity clazz,
                                                       int studentCount, String status) {
        ScheduleReservationEntity res = new ScheduleReservationEntity();
        res.setReservationNo("RES-" + UUID.randomUUID().toString().replace("-", ""));
        res.setTermId(termId);
        res.setTeachingTaskId(taskId);
        res.setCourseId(course.getId());
        res.setClazzId(clazz.getId());
        res.setTeacherId(teacherId);
        res.setBuildingName(building);
        res.setRoomNumber(room);
        res.setUseDate(baseMonday.plusWeeks(weekNo - 1).plusDays(dayOfWeek - 1));
        res.setWeekNo(weekNo);
        res.setDayOfWeek(dayOfWeek);
        res.setTimeSlotId(slot.getId());
        res.setStartTime(slot.getStartTime());
        res.setEndTime(slot.getEndTime());
        res.setStudentCount(studentCount);
        res.setExperimentContent(course.getCnName() + "实验");
        res.setStatus(status);
        return res;
    }

    private Long findTeachingTaskId(Long courseId, Long termId, Long clazzId) {
        return teachingTaskRepository.findByTermIdAndClazzId(termId, clazzId).stream()
                .filter(t -> t.getCourseId().equals(courseId))
                .findFirst()
                .map(TeachingTaskEntity::getId)
                .orElse(null);
    }

    private void bindRole(Long userId, String roleCode) {
        // 先清理该用户已有角色，再绑新角色
        userRoleRepository.deleteByUserId(userId);
        userRoleRepository.flush();

        roleRepository.findByCode(roleCode).ifPresent(role -> {
            UserRoleEntity userRole = new UserRoleEntity();
            userRole.setUserId(userId);
            userRole.setRoleId(role.getId());
            userRoleRepository.save(userRole);
        });
    }

    private void deduplicateUserRoles() {
        List<Long> userIds = jdbcTemplate.queryForList(
                "SELECT user_id FROM sys_user_role GROUP BY user_id HAVING COUNT(*) > 1", Long.class);
        for (Long userId : userIds) {
            List<UserRoleEntity> rels = userRoleRepository.findByUserId(userId);
            if (rels.size() > 1) {
                for (int i = 1; i < rels.size(); i++) {
                    userRoleRepository.delete(rels.get(i));
                }
                userRoleRepository.flush();
            }
        }
    }

    private void upsertRole(String code, String name, int level, String dataScope) {
        RoleEntity role = roleRepository.findByCode(code).orElseGet(RoleEntity::new);
        role.setCode(code);
        role.setName(name);
        role.setLevel(level);
        role.setDataScope(dataScope);
        roleRepository.save(role);
    }

    private Course ensureCourse(String code, String cnName, String enName, String type,
                                Double credit, Integer totalHour, Integer teachHour,
                                Integer practiceHour, Integer labHour, Integer netHour) {
        return courseRepository.findAll().stream()
                .filter(c -> code.equals(c.getCode()))
                .findFirst()
                .orElseGet(() -> {
                    Course c = new Course();
                    c.setCode(code);
                    c.setCnName(cnName);
                    c.setEnName(enName);
                    c.setType(type);
                    c.setCredit(credit);
                    c.setTotalHour(totalHour);
                    c.setTeachHour(teachHour);
                    c.setPracticeHour(practiceHour);
                    c.setLabHour(labHour);
                    c.setNetHour(netHour);
                    c.setTerm("2025-2026-2");
                    return courseRepository.save(c);
                });
    }

    private UserEntity ensureTeacher(String username, String realName, String jobNo) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            UserEntity u = new UserEntity();
            u.setUsername(username);
            u.setPassword(passwordEncoder.encode("teacher123"));
            u.setRealName(realName);
            u.setJobNo(jobNo);
            u.setStatus("启用");
            UserEntity saved = userRepository.save(u);
            bindRole(saved.getId(), "TEACHER");
            return saved;
        });
    }

    private void ensureTeachingTaskById(Long courseId, Long termId, Long clazzId, List<Long> teacherIds) {
        boolean exists = teachingTaskRepository.findByTermIdAndClazzId(termId, clazzId).stream()
                .anyMatch(t -> t.getCourseId().equals(courseId));
        if (!exists) {
            TeachingTaskEntity task = createTeachingTask(courseId, termId, clazzId, teacherIds);
            teachingTaskRepository.save(task);
        } else {
            fixMissingTeacherIds(courseId, termId, clazzId, teacherIds);
        }
    }

    private CampusEntity createCampus(String name, String address) {
        CampusEntity c = new CampusEntity();
        c.setName(name);
        c.setAddress(address);
        return c;
    }

    private BuildingEntity createBuilding(String name, String campus, int sortOrder) {
        BuildingEntity b = new BuildingEntity();
        b.setName(name);
        b.setCampus(campus);
        b.setSortOrder(sortOrder);
        return b;
    }

    private void addRoom(List<RoomEntity> list, Long buildingId, String code, int floor,
                         int seats, String area, String intro, String roomType) {
        RoomEntity r = new RoomEntity();
        r.setBuildingId(buildingId);
        r.setCode(code);
        r.setFloor(floor);
        r.setSeats(seats);
        r.setArea(area);
        r.setIntro(intro);
        r.setRoomType(roomType);
        list.add(r);
    }

    private void addDict(List<SysDictEntity> list, String dictType, String key,
                         String value, int sortOrder, String remark) {
        SysDictEntity d = new SysDictEntity();
        d.setDictType(dictType);
        d.setDictKey(key);
        d.setDictValue(value);
        d.setSortOrder(sortOrder);
        d.setRemark(remark);
        list.add(d);
    }

    private TermEntity createTerm(String termName, LocalDate startDate, LocalDate endDate,
                                  Integer totalWeeks, String holidayDates, String labOpenDays,
                                  LocalDate courseSelectStart, LocalDate courseAdjustStart,
                                  LocalDate midtermStart, LocalDate finalStart,
                                  LocalDate makeupStart, LocalDate thesisDeadline,
                                  LocalDate militaryStart, LocalDate sportsDay,
                                  LocalDate anniversary, LocalDate graduationStart,
                                  LocalDate enrollmentStart,
                                  LocalDate staffTrainingStart, LocalDate physicalStart,
                                  LocalDate safetyEducation,
                                  LocalDate jobFair, LocalDate campusOpenDay) {
        TermEntity t = new TermEntity();
        t.setTermName(termName);
        t.setStartDate(startDate);
        t.setEndDate(endDate);
        t.setTotalWeeks(totalWeeks);
        t.setHolidayDates(holidayDates);
        t.setLabOpenDays(labOpenDays);
        t.setCourseSelectStart(courseSelectStart);
        t.setCourseAdjustStart(courseAdjustStart);
        t.setMidtermStart(midtermStart);
        t.setFinalStart(finalStart);
        t.setMakeupStart(makeupStart);
        t.setThesisDeadline(thesisDeadline);
        t.setMilitaryStart(militaryStart);
        t.setSportsDay(sportsDay);
        t.setAnniversary(anniversary);
        t.setGraduationStart(graduationStart);
        t.setEnrollmentStart(enrollmentStart);
        t.setStaffTrainingStart(staffTrainingStart);
        t.setPhysicalStart(physicalStart);
        t.setSafetyEducation(safetyEducation);
        t.setJobFair(jobFair);
        t.setCampusOpenDay(campusOpenDay);
        return t;
    }

    private MajorEntity createMajor(String code, String name, Long deptId) {
        MajorEntity major = new MajorEntity();
        major.setMajorCode(code);
        major.setMajorName(name);
        major.setDeptId(deptId);
        major.setStatus("启用");
        return major;
    }

    private OrgEntity createOrg(String code, String name, String level, Long parentId, String leader) {
        OrgEntity org = new OrgEntity();
        org.setCode(code);
        org.setName(name);
        org.setLevel(level);
        org.setParentId(parentId);
        org.setLeader(leader);
        org.setStatus("启用");
        return org;
    }

    private ClazzEntity createClazz(String code, String name, Long majorId, Long deptId, String grade) {
        ClazzEntity clazz = new ClazzEntity();
        clazz.setClazzCode(code);
        clazz.setClazzName(name);
        clazz.setMajorId(majorId);
        clazz.setDeptId(deptId);
        clazz.setGrade(grade);
        clazz.setStatus("启用");
        return clazz;
    }

    private Course createCourse(String code, String cnName, String enName, String type, Double credit,
                                Integer totalHour, Integer teachHour, Integer practiceHour,
                                Integer labHour, Integer netHour, String term) {
        Course course = new Course();
        course.setCode(code);
        course.setCnName(cnName);
        course.setEnName(enName);
        course.setType(type);
        course.setCredit(credit);
        course.setTotalHour(totalHour);
        course.setTeachHour(teachHour);
        course.setPracticeHour(practiceHour);
        course.setLabHour(labHour);
        course.setNetHour(netHour);
        course.setTerm(term);
        return course;
    }

    private TimeSlotEntity createTimeSlot(String slotName, LocalTime startTime, LocalTime endTime, Integer sortOrder) {
        TimeSlotEntity slot = new TimeSlotEntity();
        slot.setSlotName(slotName);
        slot.setStartTime(startTime);
        slot.setEndTime(endTime);
        slot.setSortOrder(sortOrder);
        return slot;
    }

    private ExpTask createExpTask(String term, String major, Integer classId, Integer studentCount,
                                  String studentLevel, String courseName, String courseType,
                                  Boolean independentCourse, Integer totalExpHour, Integer currentExpHour,
                                  String organization, String department, String teacher) {
        ExpTask task = new ExpTask();
        task.setTerm(term);
        task.setMajor(major);
        task.setClassId(classId);
        task.setStudentCount(studentCount);
        task.setStudentLevel(studentLevel);
        task.setCourseName(courseName);
        task.setCourseType(courseType);
        task.setIndependentCourse(independentCourse);
        task.setTotalExpHour(totalExpHour);
        task.setCurrentExpHour(currentExpHour);
        task.setTotalPracticeHour(0);
        task.setCurrentPracticeHour(0);
        task.setTotalTrainingHour(0);
        task.setCurrentTrainingHour(0);
        task.setOrganization(organization);
        task.setDepartment(department);
        task.setTeacher(teacher);
        return task;
    }

    private TeachingTaskEntity createTeachingTask(Long courseId, Long termId, Long clazzId, List<Long> teacherIds) {
        TeachingTaskEntity task = new TeachingTaskEntity();
        task.setCourseId(courseId);
        task.setTermId(termId);
        task.setClazzId(clazzId);
        task.setTeacherIds(teacherIds);
        task.setStatus("进行中");
        return task;
    }

    // ========================================================================
    // Step 5: 设备管理模块初始化
    // ========================================================================

    private void initEquipmentCategories() {
        // 幂等初始化：如果已存在则跳过
        if (equipmentCategoryRepository.count() > 0) {
            System.out.println("[DataInitializer] 设备分类已存在，跳过初始化");
            return;
        }

        equipmentCategoryRepository.saveAll(List.of(
                createEquipmentCategory("COMPUTER", "计算机设备", null, 1, "计算机及相关设备"),
                createEquipmentCategory("NETWORK", "网络设备", null, 2, "路由器、交换机等"),
                createEquipmentCategory("ELECTRONIC", "电子仪器", null, 3, "示波器、万用表等"),
                createEquipmentCategory("MECHANICAL", "机械设备", null, 4, "3D打印机、车床等"),
                createEquipmentCategory("DISPLAY", "显示设备", null, 5, "投影仪、显示器等"),
                createEquipmentCategory("PERIPHERAL", "外设配件", null, 6, "键盘、鼠标、U盘等"),
                createEquipmentCategory("DESKTOP", "台式电脑", 1L, 1, null),
                createEquipmentCategory("LAPTOP", "笔记本电脑", 1L, 2, null),
                createEquipmentCategory("SERVER", "服务器", 1L, 3, null),
                createEquipmentCategory("PRINTER", "打印机", 6L, 1, null)
        ));
        System.out.println("[DataInitializer] 已初始化 10 个设备分类");
    }

    // 迁移/修复设备位置数据：将教室编号改为设备存放位置名称，并确保所属楼栋正确关联
    private void migrateEquipmentLocations() {
        List<EquipmentLocationEntity> locations = equipmentLocationRepository.findAll();
        if (locations.isEmpty()) return;

        Map<String, Long> buildingMap = buildingRepository.findAll().stream()
                .collect(Collectors.toMap(BuildingEntity::getName, BuildingEntity::getId));

        // 建立编码到楼栋的映射关系
        Map<String, String> codeToBuilding = new java.util.HashMap<>();
        codeToBuilding.put("LOC-WH-01", "实验大楼");
        codeToBuilding.put("LOC-WH-02", "实验大楼");
        codeToBuilding.put("LOC-LAB-01", "实验大楼");
        codeToBuilding.put("LOC-LAB-02", "实验大楼");
        codeToBuilding.put("LOC-STORAGE", "实验大楼");
        codeToBuilding.put("LOC-REP-01", "理工楼");
        codeToBuilding.put("LOC-REP-02", "理工楼");
        // 旧编码映射
        codeToBuilding.put("LOC-A101", "实验大楼");
        codeToBuilding.put("LOC-A102", "实验大楼");
        codeToBuilding.put("LOC-A201", "实验大楼");
        codeToBuilding.put("LOC-A301", "实验大楼");
        codeToBuilding.put("LOC-B101", "理工楼");
        codeToBuilding.put("LOC-B201", "理工楼");

        boolean updated = false;
        for (EquipmentLocationEntity loc : locations) {
            String code = loc.getCode();
            String buildingName = codeToBuilding.get(code);

            // 旧编码迁移
            switch (code) {
                case "LOC-A101":
                    loc.setCode("LOC-WH-01");
                    loc.setName("设备仓库-电子类");
                    loc.setRoomNumber("A101");
                    loc.setDescription("存放电子类设备");
                    break;
                case "LOC-A102":
                    loc.setCode("LOC-WH-02");
                    loc.setName("设备仓库-机械类");
                    loc.setRoomNumber("A102");
                    loc.setDescription("存放机械类设备");
                    break;
                case "LOC-A201":
                    loc.setCode("LOC-LAB-01");
                    loc.setName("仪器室-光学类");
                    loc.setRoomNumber("A201");
                    loc.setDescription("存放光学仪器");
                    break;
                case "LOC-A301":
                    loc.setCode("LOC-LAB-02");
                    loc.setName("仪器室-测量类");
                    loc.setRoomNumber("A301");
                    loc.setDescription("存放测量仪器");
                    break;
                case "LOC-B101":
                    loc.setCode("LOC-REP-01");
                    loc.setName("维修间-电子类");
                    loc.setRoomNumber("B101");
                    loc.setDescription("电子设备维修间");
                    break;
                case "LOC-B201":
                    loc.setCode("LOC-REP-02");
                    loc.setName("维修间-机械类");
                    loc.setRoomNumber("B201");
                    loc.setDescription("机械设备维修间");
                    break;
                case "LOC-STORAGE":
                    loc.setName("设备仓库-耗材区");
                    loc.setRoomNumber("A100");
                    loc.setDescription("设备存放仓库");
                    break;
            }

            // 修复所属楼栋关联（强制更新，确保关联正确）
            if (buildingName != null) {
                Long buildingId = buildingMap.get(buildingName);
                if (buildingId != null && !buildingId.equals(loc.getBuildingId())) {
                    loc.setBuildingId(buildingId);
                    updated = true;
                    System.out.println("[DataInitializer] 修复位置 " + loc.getCode() + " 的楼栋关联: " + buildingName);
                }
            }
        }

        if (updated) {
            equipmentLocationRepository.saveAll(locations);
            System.out.println("[DataInitializer] 已修复设备位置所属楼栋关联");
        }
    }

    private void initEquipmentLocations() {
        if (equipmentLocationRepository.count() > 0) return;

        Map<String, Long> buildingMap = buildingRepository.findAll().stream()
                .collect(Collectors.toMap(BuildingEntity::getName, BuildingEntity::getId));

        // 设备存放位置：使用仓库、仪器室、维修间等名称，不使用教室编号
        equipmentLocationRepository.saveAll(List.of(
                createEquipmentLocation("LOC-WH-01", "设备仓库-电子类", buildingMap.get("实验大楼"), "A101", 1, "存放电子类设备"),
                createEquipmentLocation("LOC-WH-02", "设备仓库-机械类", buildingMap.get("实验大楼"), "A102", 1, "存放机械类设备"),
                createEquipmentLocation("LOC-LAB-01", "仪器室-光学类", buildingMap.get("实验大楼"), "A201", 2, "存放光学仪器"),
                createEquipmentLocation("LOC-LAB-02", "仪器室-测量类", buildingMap.get("实验大楼"), "A301", 3, "存放测量仪器"),
                createEquipmentLocation("LOC-REP-01", "维修间-电子类", buildingMap.get("理工楼"), "B101", 1, "电子设备维修间"),
                createEquipmentLocation("LOC-REP-02", "维修间-机械类", buildingMap.get("理工楼"), "B201", 2, "机械设备维修间"),
                createEquipmentLocation("LOC-STORAGE", "设备仓库-耗材区", buildingMap.get("实验大楼"), "A100", 1, "设备存放仓库")
        ));
        System.out.println("[DataInitializer] 已初始化 7 个设备位置");
    }

    private void initEquipmentData() {
        System.out.println("[DataInitializer] 开始初始化设备数据...");
        System.out.println("[DataInitializer] 当前设备数量: " + equipmentRepository.count());
        
        // 幂等初始化：如果已存在则跳过
        if (equipmentRepository.count() > 0) {
            System.out.println("[DataInitializer] 设备数据已存在，跳过初始化");
            return;
        }

        List<UserEntity> allUsers = userRepository.findAll();
        System.out.println("[DataInitializer] 用户总数: " + allUsers.size());
        System.out.println("[DataInitializer] 用户名列表: " + allUsers.stream().map(UserEntity::getUsername).collect(Collectors.toList()));
        
        List<UserEntity> teachers = allUsers.stream()
                .filter(u -> "teacher_liu".equals(u.getUsername()) || "lab_wang".equals(u.getUsername()))
                .toList();
        if (teachers.isEmpty()) {
            System.out.println("[DataInitializer] 警告：找不到教师用户，跳过设备数据初始化");
            return;
        }
        Long teacherLiuId = teachers.stream().filter(u -> "teacher_liu".equals(u.getUsername())).findFirst().get().getId();
        System.out.println("[DataInitializer] teacher_liu ID: " + teacherLiuId);

        List<EquipmentCategoryEntity> allCategories = equipmentCategoryRepository.findAll();
        System.out.println("[DataInitializer] 设备分类数量: " + allCategories.size());
        System.out.println("[DataInitializer] 设备分类编码: " + allCategories.stream().map(EquipmentCategoryEntity::getCode).collect(Collectors.toList()));
        
        Map<String, Long> categoryMap = allCategories.stream()
                .collect(Collectors.toMap(EquipmentCategoryEntity::getCode, EquipmentCategoryEntity::getId));

        List<EquipmentLocationEntity> allLocations = equipmentLocationRepository.findAll();
        System.out.println("[DataInitializer] 设备位置数量: " + allLocations.size());
        System.out.println("[DataInitializer] 设备位置编码: " + allLocations.stream().map(EquipmentLocationEntity::getCode).collect(Collectors.toList()));
        
        Map<String, Long> locationMap = allLocations.stream()
                .collect(Collectors.toMap(EquipmentLocationEntity::getCode, EquipmentLocationEntity::getId));

        // 检查必要的分类和位置是否存在
        if (!categoryMap.containsKey("DESKTOP")) {
            System.out.println("[DataInitializer] 警告：缺少 DESKTOP 分类，跳过设备数据初始化");
            return;
        }
        if (!locationMap.containsKey("LOC-WH-01")) {
            System.out.println("[DataInitializer] 警告：缺少 LOC-WH-01 位置，跳过设备数据初始化");
            return;
        }
        
        System.out.println("[DataInitializer] 所有检查通过，开始创建设备数据...");

        List<EquipmentEntity> equipmentList = new ArrayList<>();

        // 台式电脑
        for (int i = 1; i <= 5; i++) {
            equipmentList.add(createEquipment(
                    "AST-2024-00" + i, "台式电脑", "OptiPlex 7090", categoryMap.get("DESKTOP"),
                    "台", "Dell", "SN-DELL-00" + i, "i7-12700/16GB/512GB SSD",
                    new java.math.BigDecimal("4500.00"), "教学经费",
                    LocalDate.of(2024, 3, 15), 5, "戴尔中国", 36,
                    locationMap.get("LOC-WH-01"), teacherLiuId, "在库-可用",
                    i <= 2, "教学用", null
            ));
        }

        // 笔记本电脑
        for (int i = 1; i <= 3; i++) {
            equipmentList.add(createEquipment(
                    "AST-2024-01" + i, "笔记本电脑", "ThinkPad X1 Carbon", categoryMap.get("LAPTOP"),
                    "台", "Lenovo", "SN-TP-01" + i, "i7-1365U/16GB/1TB SSD",
                    new java.math.BigDecimal("8999.00"), "科研经费",
                    LocalDate.of(2024, 6, 1), 4, "联想中国", 36,
                    locationMap.get("LOC-WH-02"), teacherLiuId, i == 3 ? "借出" : "在库-可用",
                    true, "科研用", null
            ));
        }

        // 服务器
        equipmentList.add(createEquipment(
                "AST-2023-020", "服务器", "PowerEdge R750", categoryMap.get("SERVER"),
                "台", "Dell", "SN-SRV-020", "Xeon Silver 4314/64GB/4TB SSD",
                new java.math.BigDecimal("35000.00"), "实验室建设经费",
                LocalDate.of(2023, 9, 1), 6, "戴尔中国", 36,
                locationMap.get("LOC-LAB-01"), teacherLiuId, "在库-可用",
                true, "核心服务器", null
        ));

        // 示波器
        for (int i = 1; i <= 4; i++) {
            equipmentList.add(createEquipment(
                    "AST-2024-03" + i, "数字示波器", "DS1054Z", categoryMap.get("ELECTRONIC"),
                    "台", "RIGOL", "SN-RIGOL-03" + i, "4通道/50MHz/1GSa/s",
                    new java.math.BigDecimal("2800.00"), "实验耗材经费",
                    LocalDate.of(2024, 4, 10), 8, "普源精电", 24,
                    locationMap.get("LOC-REP-01"), teacherLiuId,
                    i == 4 ? "在库-待维修" : "在库-可用",
                    false, "电子实验", null
            ));
        }

        // 3D打印机
        equipmentList.add(createEquipment(
                "AST-2024-040", "3D打印机", "Ender-3 V2", categoryMap.get("MECHANICAL"),
                "台", "Creality", "SN-3DP-040", "FDM/220x220x250mm",
                new java.math.BigDecimal("1800.00"), "创新基金",
                LocalDate.of(2024, 5, 20), 5, "创想三维", 12,
                locationMap.get("LOC-REP-02"), teacherLiuId, "在库-可用",
                false, "创客空间", null
        ));

        // 投影仪
        equipmentList.add(createEquipment(
                "AST-2023-050", "投影仪", "CB-FH52", categoryMap.get("DISPLAY"),
                "台", "Epson", "SN-EP-050", "1080P/4000流明",
                new java.math.BigDecimal("5500.00"), "教学经费",
                LocalDate.of(2023, 11, 5), 6, "爱普生中国", 36,
                locationMap.get("LOC-WH-01"), teacherLiuId, "在库-可用",
                false, null, null
        ));

        // 交换机
        equipmentList.add(createEquipment(
                "AST-2024-060", "交换机", "S5735-L24T4S-A", categoryMap.get("NETWORK"),
                "台", "Huawei", "SN-HW-060", "24口千兆+4口万兆",
                new java.math.BigDecimal("6800.00"), "网络建设经费",
                LocalDate.of(2024, 2, 20), 7, "华为技术", 36,
                locationMap.get("LOC-LAB-01"), teacherLiuId, "在库-可用",
                true, "核心交换机", null
        ));

        // 万用表
        for (int i = 1; i <= 3; i++) {
            equipmentList.add(createEquipment(
                    "AST-2024-07" + i, "数字万用表", "DM3058E", categoryMap.get("ELECTRONIC"),
                    "台", "RIGOL", "SN-DMM-07" + i, "5位半/自动量程",
                    new java.math.BigDecimal("1200.00"), "实验耗材经费",
                    LocalDate.of(2024, 5, 10), 6, "普源精电", 24,
                    locationMap.get("LOC-REP-01"), teacherLiuId, "在库-可用",
                    false, "电子实验基础", null
            ));
        }

        // 频谱分析仪
        equipmentList.add(createEquipment(
                "AST-2023-080", "频谱分析仪", "DSA832", categoryMap.get("ELECTRONIC"),
                "台", "RIGOL", "SN-SA-080", "3.2GHz/10Hz分辨率",
                new java.math.BigDecimal("15000.00"), "科研经费",
                LocalDate.of(2023, 10, 15), 8, "普源精电", 36,
                locationMap.get("LOC-LAB-02"), teacherLiuId, "在库-可用",
                true, "通信实验", null
        ));

        // 激光打印机
        equipmentList.add(createEquipment(
                "AST-2024-090", "激光打印机", "LaserJet Pro M404dn", categoryMap.get("PRINTER"),
                "台", "HP", "SN-HP-090", "黑白/40页/分钟/双面打印",
                new java.math.BigDecimal("2200.00"), "办公经费",
                LocalDate.of(2024, 3, 20), 5, "惠普中国", 24,
                locationMap.get("LOC-WH-01"), teacherLiuId, "在库-可用",
                false, "实验室打印", null
        ));

        // 信号发生器
        equipmentList.add(createEquipment(
                "AST-2024-100", "函数信号发生器", "DG1032Z", categoryMap.get("ELECTRONIC"),
                "台", "RIGOL", "SN-SG-100", "双通道/30MHz/任意波",
                new java.math.BigDecimal("3200.00"), "实验耗材经费",
                LocalDate.of(2024, 6, 5), 7, "普源精电", 24,
                locationMap.get("LOC-REP-01"), teacherLiuId, "在库-可用",
                false, "信号源实验", null
        ));

        // 稳压电源
        equipmentList.add(createEquipment(
                "AST-2024-110", "直流稳压电源", "DP832", categoryMap.get("ELECTRONIC"),
                "台", "RIGOL", "SN-PS-110", "三通道/30V/3A",
                new java.math.BigDecimal("1800.00"), "实验耗材经费",
                LocalDate.of(2024, 4, 25), 6, "普源精电", 24,
                locationMap.get("LOC-REP-01"), teacherLiuId, "在库-可用",
                false, "电源实验", null
        ));

        // 路由器
        equipmentList.add(createEquipment(
                "AST-2023-120", "企业级路由器", "AR6121H-S", categoryMap.get("NETWORK"),
                "台", "Huawei", "SN-RT-120", "千兆/VPN/防火墙",
                new java.math.BigDecimal("4500.00"), "网络建设经费",
                LocalDate.of(2023, 12, 10), 7, "华为技术", 36,
                locationMap.get("LOC-LAB-01"), teacherLiuId, "送修",
                true, "网络实验", null
        ));

        // 机械键盘
        equipmentList.add(createEquipment(
                "AST-2024-130", "机械键盘", "K8", categoryMap.get("PERIPHERAL"),
                "个", "Keychron", "SN-KB-130", "87键/热插拔/RGB",
                new java.math.BigDecimal("450.00"), "办公经费",
                LocalDate.of(2024, 7, 15), 3, "Keychron", 12,
                locationMap.get("LOC-WH-01"), teacherLiuId, "在库-可用",
                false, null, null
        ));

        // 无线鼠标
        equipmentList.add(createEquipment(
                "AST-2024-140", "无线鼠标", "MX Master 3S", categoryMap.get("PERIPHERAL"),
                "个", "Logitech", "SN-MS-140", "8K DPI/静音/多设备",
                new java.math.BigDecimal("699.00"), "办公经费",
                LocalDate.of(2024, 8, 1), 3, "罗技中国", 12,
                locationMap.get("LOC-WH-01"), teacherLiuId, "在库-可用",
                false, null, null
        ));

        // 投影仪（备用）
        equipmentList.add(createEquipment(
                "AST-2022-150", "投影仪", "EX100", categoryMap.get("DISPLAY"),
                "台", "Epson", "SN-EP-150", "XGA/3600流明",
                new java.math.BigDecimal("3800.00"), "教学经费",
                LocalDate.of(2022, 9, 10), 5, "爱普生中国", 36,
                locationMap.get("LOC-STORAGE"), teacherLiuId, "报废",
                false, "老旧设备", null
        ));

        // 数据采集卡
        equipmentList.add(createEquipment(
                "AST-2024-160", "数据采集卡", "USB-6001", categoryMap.get("ELECTRONIC"),
                "台", "NI", "SN-DAQ-160", "12位/8通道/50kS/s",
                new java.math.BigDecimal("2500.00"), "科研经费",
                LocalDate.of(2024, 5, 20), 6, "美国国家仪器", 24,
                locationMap.get("LOC-LAB-02"), teacherLiuId, "在库-可用",
                true, "数据采集实验", null
        ));

        // 逻辑分析仪
        equipmentList.add(createEquipment(
                "AST-2024-170", "逻辑分析仪", "LA1016", categoryMap.get("ELECTRONIC"),
                "台", "Kingst", "SN-LA-170", "16通道/100MHz",
                new java.math.BigDecimal("800.00"), "实验耗材经费",
                LocalDate.of(2024, 6, 15), 5, "金思特", 24,
                locationMap.get("LOC-REP-01"), teacherLiuId, "丢失",
                false, "数字电路实验", null
        ));

        // 示波器（第二批次）
        for (int i = 1; i <= 3; i++) {
            equipmentList.add(createEquipment(
                    "AST-2023-04" + i, "数字示波器", "DS1102E", categoryMap.get("ELECTRONIC"),
                    "台", "RIGOL", "SN-RIGOL-04" + i, "2通道/100MHz/1GSa/s",
                    new java.math.BigDecimal("2200.00"), "教学经费",
                    LocalDate.of(2023, 6, 20), 7, "普源精电", 24,
                    locationMap.get("LOC-LAB-02"), teacherLiuId, "在库-可用",
                    false, "基础实验", null
            ));
        }

        // 台式电脑（第二批次）
        for (int i = 6; i <= 10; i++) {
            equipmentList.add(createEquipment(
                    "AST-2023-00" + i, "台式电脑", "OptiPlex 3080", categoryMap.get("DESKTOP"),
                    "台", "Dell", "SN-DELL-0" + i, "i5-10500/8GB/256GB SSD",
                    new java.math.BigDecimal("3200.00"), "教学经费",
                    LocalDate.of(2023, 9, 10), 6, "戴尔中国", 36,
                    locationMap.get("LOC-WH-01"), teacherLiuId, "在库-可用",
                    false, "教学用", null
            ));
        }

        // 网络交换机（第二批次）
        equipmentList.add(createEquipment(
                "AST-2023-061", "接入交换机", "S5735-L48T4S-A", categoryMap.get("NETWORK"),
                "台", "Huawei", "SN-HW-061", "48口千兆+4口万兆",
                new java.math.BigDecimal("8500.00"), "网络建设经费",
                LocalDate.of(2023, 8, 15), 7, "华为技术", 36,
                locationMap.get("LOC-LAB-01"), teacherLiuId, "在库-可用",
                true, "接入层交换机", null
        ));

        // 无线AP
        equipmentList.add(createEquipment(
                "AST-2024-062", "无线接入点", "AirEngine 5761-11W", categoryMap.get("NETWORK"),
                "台", "Huawei", "SN-AP-062", "Wi-Fi 6/双频/2.97Gbps",
                new java.math.BigDecimal("3200.00"), "网络建设经费",
                LocalDate.of(2024, 1, 10), 5, "华为技术", 36,
                locationMap.get("LOC-LAB-01"), teacherLiuId, "在库-可用",
                false, "无线覆盖", null
        ));

        // 机柜
        equipmentList.add(createEquipment(
                "AST-2022-200", "网络机柜", "T6642", categoryMap.get("NETWORK"),
                "台", "APC", "SN-CAB-200", "42U/600x1000mm",
                new java.math.BigDecimal("4800.00"), "实验室建设经费",
                LocalDate.of(2022, 5, 10), 10, "施耐德电气", 36,
                locationMap.get("LOC-LAB-01"), teacherLiuId, "在库-可用",
                true, "服务器机柜", null
        ));

        // UPS电源
        equipmentList.add(createEquipment(
                "AST-2022-210", "UPS不间断电源", "SRT5KXLI", categoryMap.get("NETWORK"),
                "台", "APC", "SN-UPS-210", "5kVA/4500W/在线式",
                new java.math.BigDecimal("12000.00"), "实验室建设经费",
                LocalDate.of(2022, 5, 10), 8, "施耐德电气", 36,
                locationMap.get("LOC-LAB-01"), teacherLiuId, "在库-可用",
                true, "核心供电保障", null
        ));

        // 投影仪（会议室）
        equipmentList.add(createEquipment(
                "AST-2024-051", "投影仪", "CB-X06", categoryMap.get("DISPLAY"),
                "台", "Epson", "SN-EP-051", "XGA/3600流明",
                new java.math.BigDecimal("4200.00"), "办公经费",
                LocalDate.of(2024, 2, 15), 5, "爱普生中国", 36,
                locationMap.get("LOC-WH-01"), teacherLiuId, "在库-可用",
                false, "会议室用", null
        ));

        // 电子天平
        equipmentList.add(createEquipment(
                "AST-2024-300", "电子天平", "ME204E", categoryMap.get("MECHANICAL"),
                "台", "Mettler Toledo", "SN-BA-300", "220g/0.1mg",
                new java.math.BigDecimal("8500.00"), "科研经费",
                LocalDate.of(2024, 3, 20), 8, "梅特勒-托利多", 24,
                locationMap.get("LOC-LAB-02"), teacherLiuId, "在库-可用",
                true, "精密称量", null
        ));

        // 恒温干燥箱
        equipmentList.add(createEquipment(
                "AST-2023-310", "恒温干燥箱", "DHG-9140A", categoryMap.get("MECHANICAL"),
                "台", "上海一恒", "SN-DO-310", "140L/室温+10~200℃",
                new java.math.BigDecimal("3500.00"), "实验耗材经费",
                LocalDate.of(2023, 11, 5), 7, "上海一恒", 24,
                locationMap.get("LOC-LAB-02"), teacherLiuId, "在库-可用",
                false, "样品烘干", null
        ));

        // 离心机
        equipmentList.add(createEquipment(
                "AST-2024-320", "台式离心机", "TGL-16G", categoryMap.get("MECHANICAL"),
                "台", "上海安亭", "SN-CN-320", "16000r/min/6x50ml",
                new java.math.BigDecimal("2800.00"), "实验耗材经费",
                LocalDate.of(2024, 4, 15), 6, "上海安亭", 24,
                locationMap.get("LOC-LAB-02"), teacherLiuId, "在库-可用",
                false, "样品分离", null
        ));

        // 显微镜
        equipmentList.add(createEquipment(
                "AST-2023-330", "生物显微镜", "CX23", categoryMap.get("MECHANICAL"),
                "台", "Olympus", "SN-MI-330", "1600倍/LED照明",
                new java.math.BigDecimal("6500.00"), "教学经费",
                LocalDate.of(2023, 9, 20), 8, "奥林巴斯", 36,
                locationMap.get("LOC-LAB-02"), teacherLiuId, "在库-可用",
                false, "生物实验", null
        ));

        // 电烙铁
        equipmentList.add(createEquipment(
                "AST-2024-340", "恒温电烙铁", "936A", categoryMap.get("ELECTRONIC"),
                "把", "白光", "SN-SI-340", "60W/200-480℃可调",
                new java.math.BigDecimal("180.00"), "实验耗材经费",
                LocalDate.of(2024, 3, 10), 3, "白光电子", 12,
                locationMap.get("LOC-REP-01"), teacherLiuId, "在库-可用",
                false, "焊接维修", null
        ));

        // 热风枪
        equipmentList.add(createEquipment(
                "AST-2024-350", "热风拆焊台", "858D+", categoryMap.get("ELECTRONIC"),
                "台", "快克", "SN-HG-350", "700W/100-500℃",
                new java.math.BigDecimal("350.00"), "实验耗材经费",
                LocalDate.of(2024, 3, 10), 3, "快克电子", 12,
                locationMap.get("LOC-REP-01"), teacherLiuId, "在库-可用",
                false, "拆焊维修", null
        ));

        // 稳压电源（第二批次）
        equipmentList.add(createEquipment(
                "AST-2023-111", "直流稳压电源", "KORAD KA3005D", categoryMap.get("ELECTRONIC"),
                "台", "KORAD", "SN-PS-111", "30V/5A/单通道",
                new java.math.BigDecimal("650.00"), "实验耗材经费",
                LocalDate.of(2023, 10, 15), 5, "科睿德", 24,
                locationMap.get("LOC-REP-01"), teacherLiuId, "在库-可用",
                false, "电源实验", null
        ));

        // 函数信号发生器（第二批次）
        equipmentList.add(createEquipment(
                "AST-2023-101", "函数信号发生器", "DG1022Z", categoryMap.get("ELECTRONIC"),
                "台", "RIGOL", "SN-SG-101", "双通道/25MHz",
                new java.math.BigDecimal("2500.00"), "实验耗材经费",
                LocalDate.of(2023, 8, 20), 7, "普源精电", 24,
                locationMap.get("LOC-REP-01"), teacherLiuId, "在库-可用",
                false, "信号源实验", null
        ));

        // 网络测试仪
        equipmentList.add(createEquipment(
                "AST-2024-400", "网络线缆测试仪", "DSX-5000", categoryMap.get("NETWORK"),
                "台", "Fluke", "SN-NT-400", "Cat 6A/光纤测试",
                new java.math.BigDecimal("28000.00"), "网络建设经费",
                LocalDate.of(2024, 1, 20), 8, "福禄克", 36,
                locationMap.get("LOC-LAB-01"), teacherLiuId, "在库-可用",
                true, "网络布线测试", null
        ));

        // 标签打印机
        equipmentList.add(createEquipment(
                "AST-2024-410", "标签打印机", "PT-E550W", categoryMap.get("PERIPHERAL"),
                "台", "Brother", "SN-LP-410", "热转印/无线/Wi-Fi",
                new java.math.BigDecimal("1200.00"), "办公经费",
                LocalDate.of(2024, 2, 10), 5, "兄弟中国", 24,
                locationMap.get("LOC-WH-01"), teacherLiuId, "在库-可用",
                false, "设备标签打印", null
        ));

        // 碎纸机
        equipmentList.add(createEquipment(
                "AST-2023-420", "碎纸机", "990Ci", categoryMap.get("PERIPHERAL"),
                "台", "Fellowes", "SN-PS-420", "交叉切割/23张/保密级",
                new java.math.BigDecimal("1800.00"), "办公经费",
                LocalDate.of(2023, 7, 15), 5, "范罗士", 24,
                locationMap.get("LOC-WH-01"), teacherLiuId, "在库-可用",
                false, "文件销毁", null
        ));

        equipmentRepository.saveAll(equipmentList);
        System.out.println("[DataInitializer] 已初始化 " + equipmentList.size() + " 台设备");
    }

    private void initEquipmentBorrowRecords() {
        if (equipmentBorrowRecordRepository.count() > 0) return;

        List<EquipmentEntity> equipmentList = equipmentRepository.findAll();
        List<UserEntity> users = userRepository.findAll();

        if (equipmentList.isEmpty() || users.size() < 3) return;

        Long teacherLiuId = users.stream().filter(u -> "teacher_liu".equals(u.getUsername())).findFirst().map(UserEntity::getId).orElse(null);
        Long stuChenId = users.stream().filter(u -> "stu_chen".equals(u.getUsername())).findFirst().map(UserEntity::getId).orElse(null);
        Long labWangId = users.stream().filter(u -> "lab_wang".equals(u.getUsername())).findFirst().map(UserEntity::getId).orElse(null);

        if (teacherLiuId == null || stuChenId == null || labWangId == null) return;

        List<EquipmentBorrowRecordEntity> records = new ArrayList<>();
        LocalDate now = LocalDate.now();

        // 1. 已归还记录
        EquipmentBorrowRecordEntity r1 = new EquipmentBorrowRecordEntity();
        r1.setRecordNo("BR20250108001");
        r1.setEquipmentId(equipmentList.get(0).getId());
        r1.setBorrowerId(teacherLiuId);
        r1.setPurpose("Java 程序设计课程实验教学");
        r1.setExpectedReturnDate(LocalDate.of(2025, 1, 15));
        r1.setActualReturnDate(LocalDate.of(2025, 1, 14));
        r1.setStatus("已归还");
        r1.setApproverId(labWangId);
        r1.setApproveRemark("同意借用，请妥善保管");
        r1.setCreateTime(LocalDate.of(2025, 1, 8));
        r1.setPickupTime(LocalDate.of(2025, 1, 9));
        r1.setPickupPersonId(teacherLiuId);
        r1.setPhone("13800138001");
        r1.setUseLocation("实验大楼 301");
        r1.setReturnResult("完好");
        r1.setReturnLocation("实验大楼 301");
        r1.setAccessoriesInfo("电源线、鼠标、说明书齐全");
        r1.setVerifierId(labWangId);
        r1.setReturnConfirmTime(LocalDate.of(2025, 1, 14));
        records.add(r1);

        // 2. 已借出记录 - 正常
        EquipmentBorrowRecordEntity r2 = new EquipmentBorrowRecordEntity();
        r2.setRecordNo("BR20250520002");
        r2.setEquipmentId(equipmentList.get(6).getId());
        r2.setBorrowerId(teacherLiuId);
        r2.setPurpose("科研项目数据采集与处理");
        r2.setExpectedReturnDate(now.plusDays(15));
        r2.setStatus("已借出");
        r2.setApproverId(labWangId);
        r2.setApproveRemark("科研用途，批准借用");
        r2.setCreateTime(LocalDate.of(2025, 5, 20));
        r2.setPhone("13800138002");
        r2.setUseLocation("理工楼 201");
        records.add(r2);

        // 3. 待管理员审批记录（老师申请，不需要导师审批）
        EquipmentBorrowRecordEntity r3 = new EquipmentBorrowRecordEntity();
        r3.setRecordNo("BR20250525003");
        r3.setEquipmentId(equipmentList.get(8).getId());
        r3.setBorrowerId(stuChenId);
        r3.setPurpose("电子实验课程信号测量练习");
        r3.setExpectedReturnDate(now.plusDays(10));
        r3.setStatus("待导师审批");
        r3.setMentorId(teacherLiuId);
        r3.setRemark("需要用到示波器进行方波信号测量实验");
        r3.setCreateTime(LocalDate.of(2025, 5, 25));
        r3.setPhone("13800138003");
        r3.setUseLocation("实验大楼 202");
        records.add(r3);

        // 4. 已借出 - 即将到期（明天）
        EquipmentBorrowRecordEntity r4 = new EquipmentBorrowRecordEntity();
        r4.setRecordNo("BR20250515004");
        r4.setEquipmentId(equipmentList.get(7).getId());
        r4.setBorrowerId(teacherLiuId);
        r4.setPurpose("深度学习模型训练");
        r4.setExpectedReturnDate(now.plusDays(1));
        r4.setStatus("已借出");
        r4.setApproverId(labWangId);
        r4.setApproveRemark("贵重设备，使用期间注意散热");
        r4.setCreateTime(LocalDate.of(2025, 5, 15));
        r4.setPhone("13800138004");
        r4.setUseLocation("实验大楼 301");
        records.add(r4);

        // 5. 已借出 - 逾期 3 天
        EquipmentBorrowRecordEntity r5 = new EquipmentBorrowRecordEntity();
        r5.setRecordNo("BR20250510005");
        r5.setEquipmentId(equipmentList.get(11).getId());
        r5.setBorrowerId(stuChenId);
        r5.setPurpose("创客空间 - 机器人外壳打印");
        r5.setExpectedReturnDate(now.minusDays(3));
        r5.setStatus("已借出");
        r5.setApproverId(labWangId);
        r5.setApproveRemark("注意耗材使用量");
        r5.setCreateTime(LocalDate.of(2025, 5, 10));
        r5.setPhone("13800138005");
        r5.setUseLocation("创客空间 A 区");
        records.add(r5);

        // 6. 已借出 - 逾期 7 天
        EquipmentBorrowRecordEntity r6 = new EquipmentBorrowRecordEntity();
        r6.setRecordNo("BR20250505006");
        r6.setEquipmentId(equipmentList.get(14).getId());
        r6.setBorrowerId(stuChenId);
        r6.setPurpose("电工实验电路测量");
        r6.setExpectedReturnDate(now.minusDays(7));
        r6.setStatus("已借出");
        r6.setApproverId(labWangId);
        r6.setApproveRemark("实验课需要使用万用表");
        r6.setCreateTime(LocalDate.of(2025, 5, 5));
        r6.setPhone("13800138006");
        r6.setUseLocation("电工实验室 101");
        records.add(r6);

        // 7. 已批准待领取
        EquipmentBorrowRecordEntity r7 = new EquipmentBorrowRecordEntity();
        r7.setRecordNo("BR20250526007");
        r7.setEquipmentId(equipmentList.get(19).getId());
        r7.setBorrowerId(stuChenId);
        r7.setPurpose("数字电路实验信号源");
        r7.setExpectedReturnDate(now.plusDays(15));
        r7.setStatus("已批准");
        r7.setApproverId(labWangId);
        r7.setApproveRemark("已批准，请到实验室领取");
        r7.setCreateTime(LocalDate.of(2025, 5, 26));
        r7.setPhone("13800138007");
        r7.setUseLocation("数字电路实验室");
        records.add(r7);

        // 8. 已拒绝记录
        EquipmentBorrowRecordEntity r8 = new EquipmentBorrowRecordEntity();
        r8.setRecordNo("BR20250522008");
        r8.setEquipmentId(equipmentList.get(5).getId());
        r8.setBorrowerId(stuChenId);
        r8.setPurpose("课程设计编程使用");
        r8.setExpectedReturnDate(now.plusDays(20));
        r8.setStatus("已拒绝");
        r8.setApproverId(labWangId);
        r8.setApproveRemark("笔记本电脑数量紧张，建议使用机房电脑");
        r8.setCreateTime(LocalDate.of(2025, 5, 22));
        r8.setPhone("13800138008");
        r8.setUseLocation("计算机房");
        records.add(r8);

        // 9. 已归还记录
        EquipmentBorrowRecordEntity r9 = new EquipmentBorrowRecordEntity();
        r9.setRecordNo("BR20250415009");
        r9.setEquipmentId(equipmentList.get(17).getId());
        r9.setBorrowerId(teacherLiuId);
        r9.setPurpose("通信原理实验 - 频谱分析");
        r9.setExpectedReturnDate(LocalDate.of(2025, 4, 25));
        r9.setActualReturnDate(LocalDate.of(2025, 4, 24));
        r9.setStatus("已归还");
        r9.setApproverId(labWangId);
        r9.setApproveRemark("贵重设备，使用请注意");
        r9.setCreateTime(LocalDate.of(2025, 4, 15));
        r9.setPickupTime(LocalDate.of(2025, 4, 16));
        r9.setPickupPersonId(teacherLiuId);
        r9.setPhone("13800138009");
        r9.setUseLocation("通信实验室 201");
        r9.setReturnResult("完好");
        r9.setReturnLocation("通信实验室 201");
        r9.setAccessoriesInfo("主机、探头、电源线齐全");
        r9.setVerifierId(labWangId);
        r9.setReturnConfirmTime(LocalDate.of(2025, 4, 24));
        records.add(r9);

        // 10. 已借出 - 逾期 1 天
        EquipmentBorrowRecordEntity r10 = new EquipmentBorrowRecordEntity();
        r10.setRecordNo("BR20250520010");
        r10.setEquipmentId(equipmentList.get(35).getId());
        r10.setBorrowerId(stuChenId);
        r10.setPurpose("生物实验课 - 细胞观察");
        r10.setExpectedReturnDate(now.minusDays(1));
        r10.setStatus("已借出");
        r10.setApproverId(labWangId);
        r10.setApproveRemark("精密仪器，注意防震");
        r10.setCreateTime(LocalDate.of(2025, 5, 20));
        r10.setPhone("13800138010");
        r10.setUseLocation("生物实验室 301");
        records.add(r10);

        equipmentBorrowRecordRepository.saveAll(records);
        System.out.println("[DataInitializer] 已初始化 " + records.size() + " 条设备借还记录");
    }

    private EquipmentCategoryEntity createEquipmentCategory(String code, String name, Long parentId, int sortOrder, String description) {
        EquipmentCategoryEntity entity = new EquipmentCategoryEntity();
        entity.setCode(code);
        entity.setName(name);
        entity.setParentId(parentId);
        entity.setSortOrder(sortOrder);
        entity.setDescription(description);
        return entity;
    }

    private EquipmentLocationEntity createEquipmentLocation(String code, String name, Long buildingId, String roomNumber, Integer floor, String description) {
        EquipmentLocationEntity entity = new EquipmentLocationEntity();
        entity.setCode(code);
        entity.setName(name);
        entity.setBuildingId(buildingId);
        entity.setRoomNumber(roomNumber);
        entity.setFloor(floor);
        entity.setDescription(description);
        return entity;
    }

    private EquipmentEntity createEquipment(String assetNo, String name, String model, Long categoryId,
                                            String unit, String brand, String serialNo, String spec,
                                            java.math.BigDecimal price, String fundSource, LocalDate purchaseDate,
                                            Integer useYears, String supplier, Integer warrantyMonths,
                                            Long locationId, Long responsibleId, String status,
                                            Boolean isImportant, String tags, String remark) {
        EquipmentEntity entity = new EquipmentEntity();
        entity.setAssetNo(assetNo);
        entity.setName(name);
        entity.setModel(model);
        entity.setCategoryId(categoryId);
        entity.setUnit(unit);
        entity.setBrand(brand);
        entity.setSerialNo(serialNo);
        entity.setSpec(spec);
        entity.setPrice(price);
        entity.setFundSource(fundSource);
        entity.setPurchaseDate(purchaseDate);
        entity.setUseYears(useYears);
        entity.setSupplier(supplier);
        entity.setWarrantyMonths(warrantyMonths);
        entity.setLocationId(locationId);
        entity.setResponsibleId(responsibleId);
        entity.setStatus(status);
        entity.setIsImportant(isImportant);
        entity.setTags(tags);
        entity.setRemark(remark);
        return entity;
    }

    /** 初始化设备维修记录 */
    private void initEquipmentRepairRecords() {
        if (equipmentRepairRecordRepository.count() > 0) return;

        List<EquipmentEntity> equipmentList = equipmentRepository.findAll();
        if (equipmentList.isEmpty()) return;

        // 找到待维修和送修的设备
        List<EquipmentEntity> repairEquipments = equipmentList.stream()
                .filter(e -> "在库-待维修".equals(e.getStatus()) || "送修".equals(e.getStatus()))
                .toList();

        List<EquipmentRepairRecordEntity> repairRecords = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (EquipmentEntity eq : repairEquipments) {
            // 为每个维修设备创建维修记录
            EquipmentRepairRecordEntity record = new EquipmentRepairRecordEntity();
            record.setEquipmentId(eq.getId());
            record.setRepairStatus("维修中");
            record.setReportDate(now.minusDays(5));
            record.setRepairStartDate(now.minusDays(3));
            record.setRepairDurationDays(3);
            record.setRepairCost(new java.math.BigDecimal("500.00"));
            record.setRepairPerson("张维修工程师");
            record.setCreateTime(now.minusDays(5));
            record.setUpdateTime(now);

            // 根据设备名称设置不同的故障描述
            if (eq.getName().contains("示波器")) {
                record.setFaultDescription("通道2信号采集异常，显示波形失真，需要更换ADC模块");
                record.setRepairRemark("已更换ADC模块，正在校准中");
            } else if (eq.getName().contains("路由器")) {
                record.setFaultDescription("WAN口无法获取IP地址，固件升级后故障依旧，疑似硬件故障");
                record.setRepairRemark("已送厂家维修，预计5个工作日返回");
            } else {
                record.setFaultDescription("设备运行异常，需要检测维修");
                record.setRepairRemark("待检测");
            }

            repairRecords.add(record);

            // 更新设备的维修频次和累计维修时长
            eq.setRepairCount(1);
            eq.setTotalRepairDays(3);
            equipmentRepository.save(eq);
        }

        equipmentRepairRecordRepository.saveAll(repairRecords);
        System.out.println("[DataInitializer] 已初始化 " + repairRecords.size() + " 条设备维修记录");
    }

    /** 初始化操作日志 */
    private void initOperationLogs() {
        if (operationLogRepository.count() > 0) return;

        List<UserEntity> users = userRepository.findAll();
        Long labWangId = users.stream().filter(u -> "lab_wang".equals(u.getUsername())).findFirst().map(UserEntity::getId).orElse(null);
        Long teacherLiuId = users.stream().filter(u -> "teacher_liu".equals(u.getUsername())).findFirst().map(UserEntity::getId).orElse(null);
        Long stuChenId = users.stream().filter(u -> "stu_chen".equals(u.getUsername())).findFirst().map(UserEntity::getId).orElse(null);

        if (labWangId == null || teacherLiuId == null || stuChenId == null) return;

        String labWangName = "王管理员";
        String teacherLiuName = "刘老师";
        String stuChenName = "陈学生";

        List<OperationLogEntity> logs = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // 设备管理相关日志
        logs.add(createLog(labWangId, labWangName, "设备管理", "新增", "新增设备：数字示波器（DS1054Z）", "192.168.1.100", now.minusDays(10)));
        logs.add(createLog(labWangId, labWangName, "设备管理", "修改", "修改设备：信号发生器（DG1022Z）位置信息", "192.168.1.100", now.minusDays(8)));
        logs.add(createLog(labWangId, labWangName, "设备管理", "删除", "报废设备：老旧万用表（资产编号：EQ20200015）", "192.168.1.100", now.minusDays(5)));

        // 设备借还相关日志
        logs.add(createLog(teacherLiuId, teacherLiuName, "设备借还", "新增", "提交设备借用申请：数字示波器（DS1054Z）", "192.168.1.101", now.minusDays(7)));
        logs.add(createLog(labWangId, labWangName, "设备借还", "审批", "管理员审批通过：数字示波器（DS1054Z），库存已锁定", "192.168.1.100", now.minusDays(6)));
        logs.add(createLog(teacherLiuId, teacherLiuName, "设备借还", "领取", "领取设备：数字示波器（DS1054Z），领取时状态：在库-已预约", "192.168.1.101", now.minusDays(6)));
        logs.add(createLog(labWangId, labWangName, "设备借还", "归还", "归还验收：数字示波器（DS1054Z），验收结果：完好", "192.168.1.100", now.minusDays(1)));
        logs.add(createLog(stuChenId, stuChenName, "设备借还", "续借", "提交续借申请：信号发生器（DG1022Z），新归还日期：" + now.plusDays(15).toLocalDate() + "，理由：实验未完成", "192.168.1.102", now.minusDays(2)));
        logs.add(createLog(labWangId, labWangName, "设备借还", "续借", "续借审批通过：信号发生器（DG1022Z），新归还日期：" + now.plusDays(15).toLocalDate(), "192.168.1.100", now.minusDays(1)));

        // 库存监控相关日志
        logs.add(createLog(labWangId, labWangName, "库存监控", "逾期告警", "管理员手动逾期告警：笔记本电脑（ThinkPad X1），借用人：刘老师，备注：请尽快归还", "192.168.1.100", now.minusHours(2)));

        operationLogRepository.saveAll(logs);
        System.out.println("[DataInitializer] 已初始化 " + logs.size() + " 条操作日志");
    }

    private OperationLogEntity createLog(Long operatorId, String operatorName, String module, String operationType, String content, String ipAddress, LocalDateTime time) {
        OperationLogEntity entity = new OperationLogEntity();
        entity.setOperatorId(operatorId);
        entity.setOperatorName(operatorName);
        entity.setModule(module);
        entity.setOperationType(operationType);
        entity.setContent(content);
        entity.setIpAddress(ipAddress);
        entity.setCreateTime(time);
        return entity;
    }

    /** 初始化消息通知 */
    private void initNotifications() {
        if (notificationRepository.count() > 0) return;

        List<UserEntity> users = userRepository.findAll();
        Long labWangId = users.stream().filter(u -> "lab_wang".equals(u.getUsername())).findFirst().map(UserEntity::getId).orElse(null);
        Long teacherLiuId = users.stream().filter(u -> "teacher_liu".equals(u.getUsername())).findFirst().map(UserEntity::getId).orElse(null);
        Long stuChenId = users.stream().filter(u -> "stu_chen".equals(u.getUsername())).findFirst().map(UserEntity::getId).orElse(null);

        if (labWangId == null || teacherLiuId == null || stuChenId == null) return;

        String labWangName = "王管理员";
        String teacherLiuName = "刘老师";
        String stuChenName = "陈学生";

        List<NotificationEntity> notifications = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // 借还申请通知
        notifications.add(createNotification(labWangId, labWangName, "借还申请", "新的借用申请待审批",
                teacherLiuName + " 申请借用 数字示波器（DS1054Z），请及时审批", false, now.minusDays(7)));
        notifications.add(createNotification(labWangId, labWangName, "借还申请", "设备归还申请待验收",
                teacherLiuName + " 申请归还设备 数字示波器（DS1054Z），请及时安排验收", false, now.minusDays(1)));

        // 审批通知
        notifications.add(createNotification(teacherLiuId, teacherLiuName, "审批", "借用申请已批准",
                "您的借用申请 数字示波器（DS1054Z） 已获批准，请及时领取设备", true, now.minusDays(6)));
        notifications.add(createNotification(stuChenId, stuChenName, "审批", "续借申请已批准",
                "您的续借申请 信号发生器（DG1022Z） 已获批准，新归还日期：" + now.plusDays(15).toLocalDate(), true, now.minusDays(1)));

        // 完成通知
        notifications.add(createNotification(teacherLiuId, teacherLiuName, "完成", "设备已领取",
                "您借用的设备 数字示波器（DS1054Z） 已成功领取，请妥善保管", true, now.minusDays(6)));
        notifications.add(createNotification(teacherLiuId, teacherLiuName, "完成", "设备归还完成",
                "您归还的设备 数字示波器（DS1054Z） 验收结果为：完好", true, now.minusDays(1)));

        // 逾期提醒通知
        notifications.add(createNotification(teacherLiuId, teacherLiuName, "逾期提醒", "到期提醒",
                "您借用的设备 笔记本电脑（ThinkPad X1） 今天到期，请尽快归还", false, now));
        notifications.add(createNotification(teacherLiuId, teacherLiuName, "逾期提醒", "设备逾期告警",
                "您借用的设备 笔记本电脑（ThinkPad X1） 已逾期，请尽快归还。管理员备注：请尽快归还，影响后续使用", false, now.minusHours(2)));

        // 检定到期通知
        notifications.add(createNotification(labWangId, labWangName, "检定到期", "设备检定即将到期",
                "数字示波器（DS1054Z） 将于 5 天后到期，请及时安排检定", false, now.minusDays(2)));

        notificationRepository.saveAll(notifications);
        System.out.println("[DataInitializer] 已初始化 " + notifications.size() + " 条消息通知");
    }

    private NotificationEntity createNotification(Long receiverId, String receiverName, String type, String title, String content, Boolean isRead, LocalDateTime time) {
        NotificationEntity entity = new NotificationEntity();
        entity.setReceiverId(receiverId);
        entity.setReceiverName(receiverName);
        entity.setType(type);
        entity.setTitle(title);
        entity.setContent(content);
        entity.setIsRead(isRead);
        entity.setCreateTime(time);
        return entity;
    }
}
