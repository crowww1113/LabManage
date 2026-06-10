package com.example.labmanage.service;

import com.example.labmanage.dto.StatisticsDTOs.*;
import com.example.labmanage.entity.BuildingEntity;
import com.example.labmanage.entity.RoomEntity;
import com.example.labmanage.entity.TimeSlotEntity;
import com.example.labmanage.repository.*;
import com.example.labmanage.util.ExcelExportUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StatisticsRepository statisticsRepository;
    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final TermRepository termRepository;

    private static final List<String> ACTIVE_STATUSES = List.of("APPROVED", "IN_USE", "COMPLETED");

    /**
     * 7.1 楼宇×周次 使用率统计（含房间粒度）
     */
    public List<RoomWeeklyStats> getRoomWeeklyStats(Long termId) {
        // 已预约槽位：building, room, week, distinct(day+slot) count
        List<Object[]> reserved = statisticsRepository.countReservedSlotsByRoomAndWeek(termId);

        // 每栋楼的房间数
        List<BuildingEntity> buildings = buildingRepository.findAllByOrderBySortOrderAsc();
        Map<String, Integer> roomCountMap = new LinkedHashMap<>();
        for (BuildingEntity b : buildings) {
            List<RoomEntity> rooms = roomRepository.findByBuildingId(b.getId());
            roomCountMap.put(b.getName(), rooms.size());
        }

        // 节次总数
        int totalSlots = timeSlotRepository.findAllByOrderBySortOrderAsc().size() * 5;

        // 按 building, room, week 索引
        Map<String, Map<Integer, Long>> reservedMap = new LinkedHashMap<>();
        Set<String> allBuildings = new LinkedHashSet<>();
        Set<Integer> allWeeks = new TreeSet<>();
        for (Object[] row : reserved) {
            String bName = Objects.toString(row[0], "");
            String rNum = Objects.toString(row[1], "");
            Integer week = toInt(row[2]);
            Long count = toLong(row[3]);
            allBuildings.add(bName);
            allWeeks.add(week);
            reservedMap
                .computeIfAbsent(bName + "|" + rNum, k -> new LinkedHashMap<>())
                .put(week, count);
        }

        // 组装结果（仅输出有数据的楼宇-房间-周次）
        List<RoomWeeklyStats> result = new ArrayList<>();
        for (Map.Entry<String, Map<Integer, Long>> entry : reservedMap.entrySet()) {
            String[] parts = entry.getKey().split("\\|", 2);
            String bName = parts[0];
            String rNum = parts.length > 1 ? parts[1] : "";
            int rooms = roomCountMap.getOrDefault(bName, 1);
            int totalPerWeek = totalSlots * rooms;
            for (Map.Entry<Integer, Long> weekEntry : entry.getValue().entrySet()) {
                int reservedSlots = weekEntry.getValue().intValue();
                RoomWeeklyStats dto = new RoomWeeklyStats();
                dto.setBuildingName(bName);
                dto.setRoomNumber(rNum);
                dto.setWeekNo(weekEntry.getKey());
                dto.setTotalSlots(totalPerWeek);
                dto.setReservedSlots(reservedSlots);
                dto.setFreeSlots(Math.max(0, totalPerWeek - reservedSlots));
                dto.setUsageRate(totalPerWeek > 0 ? Math.round(reservedSlots * 10000.0 / totalPerWeek) / 100.0 : 0);
                result.add(dto);
            }
        }
        return result;
    }

    /**
     * 7.1 楼宇/房间 人数统计
     */
    public List<HeadcountStats> getHeadcountStats(Long termId) {
        List<Object[]> rows = statisticsRepository.aggregateHeadcountByRoom(termId);
        List<HeadcountStats> result = new ArrayList<>();
        for (Object[] row : rows) {
            HeadcountStats dto = new HeadcountStats();
            dto.setBuildingName(Objects.toString(row[0], ""));
            dto.setRoomNumber(Objects.toString(row[1], ""));
            dto.setExpectedTotal(toInt(row[2]));
            dto.setActualTotal(toInt(row[3]));
            result.add(dto);
        }
        return result;
    }

    /**
     * 7.1 专业维度课时统计
     */
    public List<MajorUsageStats> getMajorStats(Long termId) {
        List<Object[]> rows = statisticsRepository.aggregateHoursByMajor(termId);
        List<MajorUsageStats> result = new ArrayList<>();
        for (Object[] row : rows) {
            MajorUsageStats dto = new MajorUsageStats();
            dto.setMajorName(Objects.toString(row[0], ""));
            dto.setCourseHours(toInt(row[1]));
            result.add(dto);
        }
        return result;
    }

    /**
     * 7.1 班级维度课时统计
     */
    public List<ClassUsageStats> getClassStats(Long termId) {
        List<Object[]> rows = statisticsRepository.aggregateHoursByClazz(termId);
        List<ClassUsageStats> result = new ArrayList<>();
        for (Object[] row : rows) {
            ClassUsageStats dto = new ClassUsageStats();
            dto.setClazzId(toLong(row[0]));
            dto.setClassName(Objects.toString(row[1], ""));
            dto.setCourseHours(toInt(row[2]));
            result.add(dto);
        }
        return result;
    }

    /**
     * 7.1 年级维度课时统计
     */
    public List<GradeUsageStats> getGradeStats(Long termId) {
        List<Object[]> rows = statisticsRepository.aggregateHoursByGrade(termId);
        List<GradeUsageStats> result = new ArrayList<>();
        for (Object[] row : rows) {
            GradeUsageStats dto = new GradeUsageStats();
            dto.setGrade(Objects.toString(row[0], ""));
            dto.setCourseHours(toInt(row[1]));
            result.add(dto);
        }
        return result;
    }

    /**
     * 7.1 课程维度课时统计
     */
    public List<CourseUsageStats> getCourseStats(Long termId) {
        List<Object[]> rows = statisticsRepository.aggregateHoursByCourse(termId);
        List<CourseUsageStats> result = new ArrayList<>();
        for (Object[] row : rows) {
            CourseUsageStats dto = new CourseUsageStats();
            dto.setCourseId(toLong(row[0]));
            dto.setCourseName(Objects.toString(row[1], ""));
            dto.setCourseHours(toInt(row[2]));
            result.add(dto);
        }
        return result;
    }

    /**
     * 7.1 项目类别维度预约统计
     */
    public List<ReservationStats> getReservationStats(Long termId) {
        List<Object[]> rows = statisticsRepository.aggregateByProjectCategory(termId);
        List<ReservationStats> result = new ArrayList<>();
        for (Object[] row : rows) {
            ReservationStats dto = new ReservationStats();
            dto.setProjectCategory(Objects.toString(row[0], ""));
            dto.setReservationCount(toInt(row[1]));
            dto.setTotalDuration(row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO);
            result.add(dto);
        }
        return result;
    }

    /**
     * 7.1 登记率统计（教师维度）
     * 分母 = 该教师已完成(COMPLETED)的预约数
     * 分子 = 其中有对应已登记(REGISTERED)使用记录的预约数
     */
    public List<RegistrationRateStats> getRegistrationRateStats(Long termId) {
        // 已完成预约：[reservationId, teacherId]
        List<Object[]> completedRows = statisticsRepository.findCompletedReservationsForRate(termId);
        if (completedRows.isEmpty()) return List.of();

        // 按教师分组
        Map<Long, List<Long>> teacherReservations = new LinkedHashMap<>();
        List<Long> allReservationIds = new ArrayList<>();
        for (Object[] row : completedRows) {
            Long resId = toLong(row[0]);
            Long teacherId = toLong(row[1]);
            teacherReservations.computeIfAbsent(teacherId, k -> new ArrayList<>()).add(resId);
            allReservationIds.add(resId);
        }

        // 已登记的预约 ID
        List<Long> registeredIds = allReservationIds.isEmpty()
                ? List.of()
                : statisticsRepository.findRegisteredReservationIds(allReservationIds);
        Set<Long> registeredSet = new HashSet<>(registeredIds);

        // 教师姓名
        Set<Long> teacherIds = teacherReservations.keySet();
        Map<Long, String> teacherNameMap = new HashMap<>();
        if (!teacherIds.isEmpty()) {
            List<Object[]> nameRows = statisticsRepository.findUserNamesByIds(new ArrayList<>(teacherIds));
            for (Object[] row : nameRows) {
                teacherNameMap.put(toLong(row[0]), Objects.toString(row[1], ""));
            }
        }

        // 组装结果
        List<RegistrationRateStats> result = new ArrayList<>();
        for (Map.Entry<Long, List<Long>> entry : teacherReservations.entrySet()) {
            Long teacherId = entry.getKey();
            List<Long> resIds = entry.getValue();
            long registeredCount = resIds.stream().filter(registeredSet::contains).count();
            RegistrationRateStats dto = new RegistrationRateStats();
            dto.setTeacherId(teacherId);
            dto.setTeacherName(teacherNameMap.getOrDefault(teacherId, ""));
            dto.setExpectedCount(resIds.size());
            dto.setRegisteredCount((int) registeredCount);
            dto.setRegistrationRate(resIds.isEmpty() ? 0.0
                    : Math.round(registeredCount * 10000.0 / resIds.size()) / 100.0);
            result.add(dto);
        }
        result.sort(Comparator.comparing(RegistrationRateStats::getTeacherName,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return result;
    }

    // ---- 工具方法 ----
    private int toInt(Object o) {
        if (o instanceof Number n) return n.intValue();
        if (o != null) try { return Integer.parseInt(o.toString()); } catch (NumberFormatException ignored) {}
        return 0;
    }

    private long toLong(Object o) {
        if (o instanceof Number n) return n.longValue();
        if (o != null) try { return Long.parseLong(o.toString()); } catch (NumberFormatException ignored) {}
        return 0L;
    }

    private String toStr(Object o) {
        return o != null ? o.toString() : "";
    }

    // ========== Dashboard 大屏 ==========

    public DashboardData getDashboard(Long termId, String mode, LocalDate startDate, LocalDate endDate) {
        // 兜底：termId 为空时自动取最新学期
        if (termId == null) {
            var terms = termRepository.findAll();
            if (!terms.isEmpty()) {
                termId = terms.get(terms.size() - 1).getId();
            }
        }

        // 解析日期范围
        LocalDate effectiveStart = startDate;
        LocalDate effectiveEnd = endDate;
        if (effectiveStart == null || effectiveEnd == null) {
            if ("STAGE".equalsIgnoreCase(mode) && termId != null) {
                // 阶段统计：用学期日期范围
                var term = termRepository.findById(termId).orElse(null);
                if (term != null) {
                    effectiveStart = term.getStartDate();
                    effectiveEnd = term.getEndDate();
                }
            }
            // DAILY 模式不过滤日期，展示全量数据
        }

        DashboardData data = new DashboardData();
        data.setOccupancy(buildOccupancyModule());
        data.setDensity(buildDensityModule(termId, effectiveStart, effectiveEnd));
        data.setTrend(buildTrendModule(termId, effectiveStart, effectiveEnd));
        data.setProportion(buildProportionModule(termId, effectiveStart, effectiveEnd));
        data.setRegistration(buildRegistrationModule(termId));
        data.setAlerts(buildAlertModule(termId));
        return data;
    }

    private OccupancyModule buildOccupancyModule() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<Object[]> occupiedRows = statisticsRepository.findCurrentlyOccupiedRooms(today, now);
        Set<String> occupiedSet = new LinkedHashSet<>();
        for (Object[] row : occupiedRows) {
            occupiedSet.add(toStr(row[0]) + "|" + toStr(row[1]));
        }

        List<BuildingEntity> buildings = buildingRepository.findAllByOrderBySortOrderAsc();
        List<RoomOccupancy> roomList = new ArrayList<>();
        int totalRooms = 0;

        for (BuildingEntity b : buildings) {
            List<RoomEntity> rooms = roomRepository.findByBuildingId(b.getId());
            for (RoomEntity r : rooms) {
                totalRooms++;
                RoomOccupancy ro = new RoomOccupancy();
                ro.setBuildingName(b.getName());
                ro.setRoomNumber(r.getCode());
                ro.setStatus(occupiedSet.contains(b.getName() + "|" + r.getCode()) ? "occupied" : "free");
                roomList.add(ro);
            }
        }

        OccupancyModule mod = new OccupancyModule();
        mod.setTotalRooms(totalRooms);
        mod.setOccupiedRooms(occupiedSet.size());
        mod.setFreeRooms(totalRooms - occupiedSet.size());
        mod.setRooms(roomList);
        return mod;
    }

    private DensityModule buildDensityModule(Long termId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = statisticsRepository.aggregateDensity(termId, startDate, endDate);

        Map<String, List<DateCount>> buildingMap = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String bName = toStr(row[0]);
            DateCount dc = new DateCount();
            dc.setDate(toStr(row[1]));
            dc.setCount(toInt(row[2]));
            buildingMap.computeIfAbsent(bName, k -> new ArrayList<>()).add(dc);
        }

        DensityModule mod = new DensityModule();
        List<BuildingDensity> list = new ArrayList<>();
        for (Map.Entry<String, List<DateCount>> entry : buildingMap.entrySet()) {
            BuildingDensity bd = new BuildingDensity();
            bd.setBuildingName(entry.getKey());
            bd.setDates(entry.getValue());
            list.add(bd);
        }
        mod.setBuildings(list);
        return mod;
    }

    private TrendModule buildTrendModule(Long termId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = statisticsRepository.aggregateTrend(termId, startDate, endDate);

        List<BuildingEntity> buildings = buildingRepository.findAllByOrderBySortOrderAsc();
        Map<String, Integer> roomCountMap = new LinkedHashMap<>();
        for (BuildingEntity b : buildings) {
            roomCountMap.put(b.getName(), roomRepository.findByBuildingId(b.getId()).size());
        }
        int slotsPerRoom = timeSlotRepository.findAllByOrderBySortOrderAsc().size() * 5;

        // 按周汇总
        Map<Integer, int[]> weekData = new TreeMap<>();
        for (Object[] row : rows) {
            String bName = toStr(row[0]);
            Integer weekNo = toInt(row[2]);
            Long count = toLong(row[3]);
            int rooms = roomCountMap.getOrDefault(bName, 1);
            int[] arr = weekData.computeIfAbsent(weekNo, k -> new int[]{0, 0});
            arr[0] += count.intValue();
            arr[1] += slotsPerRoom * rooms;
        }

        TrendModule mod = new TrendModule();
        List<WeeklyRate> weeks = new ArrayList<>();
        for (Map.Entry<Integer, int[]> entry : weekData.entrySet()) {
            WeeklyRate wr = new WeeklyRate();
            wr.setWeekNo(entry.getKey());
            wr.setReservedSlots(entry.getValue()[0]);
            wr.setTotalSlots(entry.getValue()[1]);
            double rate = entry.getValue()[1] > 0
                    ? Math.round(entry.getValue()[0] * 10000.0 / entry.getValue()[1]) / 100.0 : 0;
            wr.setUsageRate(rate);
            weeks.add(wr);
        }
        mod.setWeeks(weeks);
        return mod;
    }

    private ProportionModule buildProportionModule(Long termId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = statisticsRepository.aggregateByApplicationType(termId, startDate, endDate);

        int total = 0;
        List<CategoryCount> categories = new ArrayList<>();
        for (Object[] row : rows) {
            int count = toInt(row[1]);
            total += count;
            CategoryCount cc = new CategoryCount();
            cc.setCategory(toStr(row[0]));
            cc.setCount(count);
            categories.add(cc);
        }
        for (CategoryCount cc : categories) {
            cc.setPercentage(total > 0 ? Math.round(cc.getCount() * 10000.0 / total) / 100.0 : 0);
        }

        ProportionModule mod = new ProportionModule();
        mod.setTotal(total);
        mod.setCategories(categories);
        return mod;
    }

    private RegistrationModule buildRegistrationModule(Long termId) {
        List<Object[]> rows = termId != null
                ? statisticsRepository.aggregateRegistrationByDept(termId)
                : List.of();

        Map<String, int[]> deptMap = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String dept = toStr(row[0]);
            String status = toStr(row[1]);
            int count = toInt(row[2]);
            int[] arr = deptMap.computeIfAbsent(dept, k -> new int[]{0, 0});
            arr[0] += count;
            if ("REGISTERED".equals(status)) arr[1] += count;
        }

        int totalCount = 0, registeredCount = 0;
        List<DepartmentRegistration> depts = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : deptMap.entrySet()) {
            int total = entry.getValue()[0];
            int reg = entry.getValue()[1];
            totalCount += total;
            registeredCount += reg;
            DepartmentRegistration dr = new DepartmentRegistration();
            dr.setDepartment(entry.getKey());
            dr.setTotalCount(total);
            dr.setRegisteredCount(reg);
            dr.setRegistrationRate(total > 0 ? Math.round(reg * 10000.0 / total) / 100.0 : 0);
            depts.add(dr);
        }

        RegistrationModule mod = new RegistrationModule();
        mod.setTotalCount(totalCount);
        mod.setRegisteredCount(registeredCount);
        mod.setOverallRate(totalCount > 0 ? Math.round(registeredCount * 10000.0 / totalCount) / 100.0 : 0);
        mod.setDepartments(depts);
        return mod;
    }

    private AlertModule buildAlertModule(Long termId) {
        if (termId == null) {
            AlertModule mod = new AlertModule();
            mod.setOverdueRecords(List.of());
            mod.setEquipmentAnomalies(List.of());
            return mod;
        }

        List<Object[]> overdueRows = statisticsRepository.findOverdueAlerts(termId);
        List<AlertItem> overdueList = new ArrayList<>();
        for (Object[] row : overdueRows) {
            if (overdueList.size() >= 5) break;
            AlertItem item = new AlertItem();
            item.setCourseName(toStr(row[0]));
            item.setDepartment(toStr(row[1]));
            item.setReporterName(toStr(row[2]));
            item.setUsageDate(toStr(row[3]));
            item.setBuildingName(toStr(row[4]));
            item.setRoomNumber(toStr(row[5]));
            item.setStatus("逾期未登记");
            overdueList.add(item);
        }

        List<Object[]> equipRows = statisticsRepository.findEquipmentAlerts(termId);
        List<AlertItem> equipList = new ArrayList<>();
        for (Object[] row : equipRows) {
            if (equipList.size() >= 5) break;
            AlertItem item = new AlertItem();
            item.setCourseName(toStr(row[0]));
            item.setDepartment(toStr(row[1]));
            item.setReporterName(toStr(row[2]));
            item.setUsageDate(toStr(row[3]));
            item.setBuildingName(toStr(row[4]));
            item.setRoomNumber(toStr(row[5]));
            String teach = toStr(row[6]);
            String equip = toStr(row[7]);
            item.setStatus("教学:" + teach + " 设备:" + equip);
            equipList.add(item);
        }

        AlertModule mod = new AlertModule();
        mod.setOverdueRecords(overdueList);
        mod.setEquipmentAnomalies(equipList);
        return mod;
    }

    // ========== Excel 导出 ==========

    public void exportRoomWeekly(Long termId, HttpServletResponse response) throws Exception {
        List<String> headers = List.of("楼宇", "房间", "周次", "总槽位", "已预约", "空闲", "使用率(%)");
        List<List<String>> data = getRoomWeeklyStats(termId).stream()
                .map(d -> List.of(d.getBuildingName(), d.getRoomNumber(),
                        String.valueOf(d.getWeekNo()), String.valueOf(d.getTotalSlots()),
                        String.valueOf(d.getReservedSlots()), String.valueOf(d.getFreeSlots()),
                        String.valueOf(d.getUsageRate())))
                .collect(Collectors.toList());
        ExcelExportUtil.exportToExcel("楼宇周次使用率统计", headers, data, response);
    }

    public void exportHeadcount(Long termId, HttpServletResponse response) throws Exception {
        List<String> headers = List.of("楼宇", "房间", "预计人数", "实到人数");
        List<List<String>> data = getHeadcountStats(termId).stream()
                .map(d -> List.of(d.getBuildingName(), d.getRoomNumber(),
                        String.valueOf(d.getExpectedTotal()), String.valueOf(d.getActualTotal())))
                .collect(Collectors.toList());
        ExcelExportUtil.exportToExcel("人数统计", headers, data, response);
    }

    public void exportMajor(Long termId, HttpServletResponse response) throws Exception {
        List<String> headers = List.of("专业", "课时数");
        List<List<String>> data = getMajorStats(termId).stream()
                .map(d -> List.of(d.getMajorName(), String.valueOf(d.getCourseHours())))
                .collect(Collectors.toList());
        ExcelExportUtil.exportToExcel("专业课时统计", headers, data, response);
    }

    public void exportClass(Long termId, HttpServletResponse response) throws Exception {
        List<String> headers = List.of("班级ID", "班级名称", "课时数");
        List<List<String>> data = getClassStats(termId).stream()
                .map(d -> List.of(String.valueOf(d.getClazzId()), d.getClassName(),
                        String.valueOf(d.getCourseHours())))
                .collect(Collectors.toList());
        ExcelExportUtil.exportToExcel("班级课时统计", headers, data, response);
    }

    public void exportGrade(Long termId, HttpServletResponse response) throws Exception {
        List<String> headers = List.of("年级", "课时数");
        List<List<String>> data = getGradeStats(termId).stream()
                .map(d -> List.of(d.getGrade(), String.valueOf(d.getCourseHours())))
                .collect(Collectors.toList());
        ExcelExportUtil.exportToExcel("年级课时统计", headers, data, response);
    }

    public void exportCourse(Long termId, HttpServletResponse response) throws Exception {
        List<String> headers = List.of("课程ID", "课程名称", "课时数");
        List<List<String>> data = getCourseStats(termId).stream()
                .map(d -> List.of(String.valueOf(d.getCourseId()), d.getCourseName(),
                        String.valueOf(d.getCourseHours())))
                .collect(Collectors.toList());
        ExcelExportUtil.exportToExcel("课程课时统计", headers, data, response);
    }

    public void exportReservation(Long termId, HttpServletResponse response) throws Exception {
        List<String> headers = List.of("项目类别", "预约次数", "总时长");
        List<List<String>> data = getReservationStats(termId).stream()
                .map(d -> List.of(d.getProjectCategory(), String.valueOf(d.getReservationCount()),
                        d.getTotalDuration() != null ? d.getTotalDuration().toPlainString() : "0"))
                .collect(Collectors.toList());
        ExcelExportUtil.exportToExcel("预约类别统计", headers, data, response);
    }

    public void exportRegistrationRate(Long termId, HttpServletResponse response) throws Exception {
        List<String> headers = List.of("教师ID", "教师姓名", "应登记数", "已登记数", "登记率(%)");
        List<List<String>> data = getRegistrationRateStats(termId).stream()
                .map(d -> List.of(String.valueOf(d.getTeacherId()), d.getTeacherName(),
                        String.valueOf(d.getExpectedCount()), String.valueOf(d.getRegisteredCount()),
                        String.valueOf(d.getRegistrationRate())))
                .collect(Collectors.toList());
        ExcelExportUtil.exportToExcel("登记率统计", headers, data, response);
    }
}
