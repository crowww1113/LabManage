package com.example.labmanage.service;

import com.example.labmanage.dto.MatrixTimetableDTO;
import com.example.labmanage.dto.TimetableItemDTO;
import com.example.labmanage.dto.TimetableListDTO;
import com.example.labmanage.dto.TimeSlotDTO;
import com.example.labmanage.entity.*;
import com.example.labmanage.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TimetableService {

    private final ScheduleApplicationRepository applicationRepository;
    private final ScheduleReservationRepository reservationRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final CourseRepository courseRepository;
    private final ClazzRepository clazzRepository;
    private final UserRepository userRepository;

    private static final List<String> RESERVATION_ACTIVE_STATUSES = List.of("APPROVED", "IN_USE", "PENDING", "SUBMITTED", "APPROVING");
    private static final List<String> APPLICATION_VISIBLE_STATUSES = List.of("APPROVED", "PENDING", "REJECTED", "CANCELLED", "IN_USE");
    private static final List<String> DAYS = List.of("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日");

    /**
     * 获取课表矩阵视图
     *
     * @param termId       学期ID
     * @param weekNo       周次
     * @param buildingName 楼宇名称
     * @param roomNumber   教室/房间号（可选，为 null 时不过滤）
     * @return 二维矩阵课表
     */
    public MatrixTimetableDTO getTimetableMatrix(Long termId, Integer weekNo, String buildingName, String roomNumber) {
        // 1. 加载所有标准节次（按 sortOrder 排序）
        List<TimeSlotEntity> timeSlots = timeSlotRepository.findAllByOrderBySortOrderAsc();
        int slotCount = timeSlots.size();

        // 2. 查询双表数据（支持可选教室过滤）
        List<ScheduleApplicationEntity> applications = applicationRepository
                .findByTermIdAndWeekNoAndBuildingNameAndOptionalRoomAndStatusIn(
                        termId, weekNo, buildingName, roomNumber, APPLICATION_VISIBLE_STATUSES);

        List<ScheduleReservationEntity> reservations = reservationRepository
                .findByTermIdAndWeekNoAndBuildingNameAndOptionalRoomAndStatusIn(
                        termId, weekNo, buildingName, roomNumber, RESERVATION_ACTIVE_STATUSES);

        // 3. 预加载关联数据（课程、班级、教师名称）
        Map<Long, String> courseNameMap = loadCourseNames(applications, reservations);
        Map<Long, String> clazzNameMap = loadClazzNames(applications, reservations);
        Map<Long, String> teacherNameMap = loadTeacherNames(applications, reservations);

        // 4. 统一转换为 TimetableItemDTO
        List<TimetableItemDTO> allItems = new ArrayList<>();
        for (ScheduleApplicationEntity app : applications) {
            allItems.add(convertApplication(app, timeSlots, courseNameMap, clazzNameMap, teacherNameMap));
        }
        for (ScheduleReservationEntity res : reservations) {
            allItems.add(convertReservation(res, timeSlots, courseNameMap, clazzNameMap, teacherNameMap));
        }

        // 5. 构建二维矩阵
        List<List<List<TimetableItemDTO>>> matrix = buildMatrix(timeSlots, slotCount, allItems);

        // 6. 组装返回
        MatrixTimetableDTO result = new MatrixTimetableDTO();
        result.setTermId(termId);
        result.setWeekNo(weekNo);
        result.setBuildingName(buildingName);
        result.setRoomNumber(roomNumber);
        result.setDays(DAYS);
        result.setTimeSlots(timeSlots.stream().map(this::toTimeSlotDTO).toList());
        result.setMatrix(matrix);
        return result;
    }

    /**
     * 获取排课列表视图（分页、排序）
     *
     * @param termId       学期ID
     * @param weekNo       周次
     * @param buildingName 楼宇名称
     * @param roomNumber   教室/房间号（可选，为 null 时不过滤）
     * @param page         页码（从0开始）
     * @param size         每页条数
     * @param sort         排序字段，格式 "field,asc" 或 "field,desc"，支持 dayOfWeek/timeSlotName/status/courseName
     * @return 分页的列表DTO
     */
    public Page<TimetableListDTO> getTimetableList(Long termId, Integer weekNo, String buildingName, String roomNumber,
                                                    int page, int size, String sort) {
        // 1. 加载节次（用于排序映射）
        List<TimeSlotEntity> timeSlots = timeSlotRepository.findAllByOrderBySortOrderAsc();
        Map<String, Integer> slotSortMap = new LinkedHashMap<>();
        for (int i = 0; i < timeSlots.size(); i++) {
            slotSortMap.put(timeSlots.get(i).getSlotName(), timeSlots.get(i).getSortOrder());
        }

        // 2. 查询双表数据（与矩阵视图条件一致，支持可选教室过滤）
        List<ScheduleApplicationEntity> applications = applicationRepository
                .findByTermIdAndWeekNoAndBuildingNameAndOptionalRoomAndStatusIn(
                        termId, weekNo, buildingName, roomNumber, APPLICATION_VISIBLE_STATUSES);

        List<ScheduleReservationEntity> reservations = reservationRepository
                .findByTermIdAndWeekNoAndBuildingNameAndOptionalRoomAndStatusIn(
                        termId, weekNo, buildingName, roomNumber, RESERVATION_ACTIVE_STATUSES);

        // 3. 预加载关联数据
        Map<Long, String> courseNameMap = loadCourseNames(applications, reservations);
        Map<Long, String> clazzNameMap = loadClazzNames(applications, reservations);
        Map<Long, String> teacherNameMap = loadTeacherNames(applications, reservations);

        // 4. 统一转换为 TimetableListDTO
        List<TimetableListDTO> allItems = new ArrayList<>();
        for (ScheduleApplicationEntity app : applications) {
            allItems.add(convertApplicationToListDTO(app, timeSlots, courseNameMap, clazzNameMap, teacherNameMap));
        }
        for (ScheduleReservationEntity res : reservations) {
            allItems.add(convertReservationToListDTO(res, timeSlots, courseNameMap, clazzNameMap, teacherNameMap));
        }

        // 5. 排序
        Comparator<TimetableListDTO> comparator = buildComparator(sort, slotSortMap);
        allItems.sort(comparator);

        // 6. 手动分页
        int total = allItems.size();
        int fromIndex = page * size;
        if (fromIndex >= total) {
            return new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), total);
        }
        int toIndex = Math.min(fromIndex + size, total);
        List<TimetableListDTO> pageContent = allItems.subList(fromIndex, toIndex);

        return new PageImpl<>(pageContent, PageRequest.of(page, size), total);
    }

    /**
     * 获取某楼宇下的所有教室/房间号列表（去重合并自两张表）
     */
    public List<String> getRoomsByBuilding(String buildingName) {
        Set<String> rooms = new LinkedHashSet<>();
        rooms.addAll(reservationRepository.findDistinctRoomNumbersByBuildingName(buildingName));
        rooms.addAll(applicationRepository.findDistinctPreferredRoomNumbersByBuildingName(buildingName));
        return new ArrayList<>(rooms);
    }

    private Comparator<TimetableListDTO> buildComparator(String sort, Map<String, Integer> slotSortMap) {
        if (sort == null || sort.isEmpty()) {
            return Comparator.comparing(TimetableListDTO::getDayOfWeek, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(dto -> slotSortMap.getOrDefault(dto.getTimeSlotName(), Integer.MAX_VALUE));
        }

        String[] parts = sort.split(",");
        String field = parts[0].trim();
        boolean desc = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim());

        Comparator<TimetableListDTO> comparator = switch (field) {
            case "dayOfWeek" -> Comparator.comparing(TimetableListDTO::getDayOfWeek,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case "timeSlotName" -> Comparator.comparing(
                    dto -> slotSortMap.getOrDefault(dto.getTimeSlotName(), Integer.MAX_VALUE),
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case "status" -> Comparator.comparing(TimetableListDTO::getStatus,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case "courseName" -> Comparator.comparing(TimetableListDTO::getCourseName,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            default -> Comparator.comparing(TimetableListDTO::getDayOfWeek,
                    Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(dto -> slotSortMap.getOrDefault(dto.getTimeSlotName(), Integer.MAX_VALUE));
        };

        if (desc) {
            comparator = comparator.reversed();
        }
        // 二级排序：dayOfWeek + timeSlotName，但如果主排序就是这两个之一则加另一维度
        if (!"dayOfWeek".equals(field)) {
            comparator = comparator.thenComparing(
                    Comparator.comparing(TimetableListDTO::getDayOfWeek, Comparator.nullsLast(Comparator.naturalOrder())));
        }
        if (!"timeSlotName".equals(field)) {
            comparator = comparator.thenComparing(
                    dto -> slotSortMap.getOrDefault(dto.getTimeSlotName(), Integer.MAX_VALUE),
                    Comparator.nullsLast(Comparator.naturalOrder()));
        }
        return comparator;
    }

    private TimetableListDTO convertApplicationToListDTO(ScheduleApplicationEntity app,
                                                          List<TimeSlotEntity> timeSlots,
                                                          Map<Long, String> courseNameMap,
                                                          Map<Long, String> clazzNameMap,
                                                          Map<Long, String> teacherNameMap) {
        TimetableListDTO dto = new TimetableListDTO();
        dto.setId(app.getId());
        dto.setSourceType("APPLICATION");
        dto.setSourceNo(app.getApplicationNo());
        // 使用新字段 projectName 替代 courseId
        dto.setCourseName(app.getProjectName());
        // 使用新字段 className 替代 clazzId
        dto.setClazzName(app.getClassName());
        dto.setTeacherId(app.getTeacherId());
        dto.setTeacherName(teacherNameMap.get(app.getTeacherId()));
        dto.setDayOfWeek(app.getPreferredDayOfWeek());
        dto.setDayOfWeekText(toDayText(app.getPreferredDayOfWeek()));
        dto.setBuildingName(app.getPreferredBuildingName());
        dto.setRoomNumber(app.getPreferredRoomNumber());
        dto.setStatus(app.getStatus());
        dto.setApplicationType(app.getApplicationType());
        dto.setExperimentContent(app.getExperimentContent());
        dto.setStudentCount(app.getStudentCount());

        Long timeSlotId = app.getPreferredTimeSlotId();
        if (timeSlotId != null) {
            for (TimeSlotEntity ts : timeSlots) {
                if (ts.getId().equals(timeSlotId)) {
                    dto.setTimeSlotName(ts.getSlotName());
                    break;
                }
            }
        }
        return dto;
    }

    private TimetableListDTO convertReservationToListDTO(ScheduleReservationEntity res,
                                                          List<TimeSlotEntity> timeSlots,
                                                          Map<Long, String> courseNameMap,
                                                          Map<Long, String> clazzNameMap,
                                                          Map<Long, String> teacherNameMap) {
        TimetableListDTO dto = new TimetableListDTO();
        dto.setId(res.getId());
        dto.setSourceType("RESERVATION");
        dto.setSourceNo(res.getReservationNo());
        dto.setCourseName(courseNameMap.get(res.getCourseId()));
        dto.setClazzName(clazzNameMap.get(res.getClazzId()));
        dto.setTeacherId(res.getTeacherId());
        dto.setTeacherName(teacherNameMap.get(res.getTeacherId()));
        dto.setDayOfWeek(res.getDayOfWeek());
        dto.setDayOfWeekText(toDayText(res.getDayOfWeek()));
        dto.setBuildingName(res.getBuildingName());
        dto.setRoomNumber(res.getRoomNumber());
        dto.setStatus(res.getStatus());
        dto.setExperimentContent(res.getExperimentContent());
        dto.setStudentCount(res.getStudentCount());

        // 节次映射：优先 timeSlotId，否则通过时间匹配
        Long timeSlotId = res.getTimeSlotId();
        TimeSlotEntity matched = findTimeSlotById(timeSlots, timeSlotId);
        if (matched == null) {
            matched = findNearestTimeSlot(timeSlots, res.getStartTime(), res.getEndTime());
        }
        if (matched != null) {
            dto.setTimeSlotName(matched.getSlotName());
        }
        return dto;
    }

    private String toDayText(Integer dayOfWeek) {
        if (dayOfWeek == null || dayOfWeek < 1 || dayOfWeek > 7) return null;
        return DAYS.get(dayOfWeek - 1);
    }

    /**
     * 将排课申请转为统一的课表条目
     */
    private TimetableItemDTO convertApplication(ScheduleApplicationEntity app,
                                                  List<TimeSlotEntity> timeSlots,
                                                  Map<Long, String> courseNameMap,
                                                  Map<Long, String> clazzNameMap,
                                                  Map<Long, String> teacherNameMap) {
        TimetableItemDTO item = new TimetableItemDTO();
        item.setId(app.getId());
        item.setSourceType("APPLICATION");
        item.setSourceNo(app.getApplicationNo());
        // 使用新字段 projectName 替代 courseId
        item.setCourseName(app.getProjectName());
        item.setTeacherId(app.getTeacherId());
        item.setTeacherName(teacherNameMap.get(app.getTeacherId()));
        // 使用新字段 className 替代 clazzId
        item.setClazzName(app.getClassName());
        item.setBuildingName(app.getPreferredBuildingName());
        item.setRoomNumber(app.getPreferredRoomNumber());
        item.setDayOfWeek(app.getPreferredDayOfWeek());
        item.setStudentCount(app.getStudentCount());
        item.setExperimentContent(app.getExperimentContent());
        item.setStatus(app.getStatus());
        item.setApplicationType(app.getApplicationType());

        // 通过 preferredTimeSlotId 映射到标准节次
        Long timeSlotId = app.getPreferredTimeSlotId();
        if (timeSlotId != null) {
            item.setTimeSlotId(timeSlotId);
            for (TimeSlotEntity ts : timeSlots) {
                if (ts.getId().equals(timeSlotId)) {
                    item.setTimeSlotName(ts.getSlotName());
                    item.setStartTime(ts.getStartTime());
                    item.setEndTime(ts.getEndTime());
                    break;
                }
            }
        }
        return item;
    }

    /**
     * 将实验室预约转为统一的课表条目
     * 包含非标准时间兼容逻辑
     */
    private TimetableItemDTO convertReservation(ScheduleReservationEntity res,
                                                  List<TimeSlotEntity> timeSlots,
                                                  Map<Long, String> courseNameMap,
                                                  Map<Long, String> clazzNameMap,
                                                  Map<Long, String> teacherNameMap) {
        TimetableItemDTO item = new TimetableItemDTO();
        item.setId(res.getId());
        item.setSourceType("RESERVATION");
        item.setSourceNo(res.getReservationNo());
        // 保留原课程名称映射（预约表仍有 courseId）
        item.setCourseId(res.getCourseId());
        item.setCourseName(courseNameMap.get(res.getCourseId()));
        item.setTeacherId(res.getTeacherId());
        item.setTeacherName(teacherNameMap.get(res.getTeacherId()));
        item.setClazzId(res.getClazzId());
        item.setClazzName(clazzNameMap.get(res.getClazzId()));
        item.setBuildingName(res.getBuildingName());
        item.setRoomNumber(res.getRoomNumber());
        item.setDayOfWeek(res.getDayOfWeek());
        item.setStartTime(res.getStartTime());
        item.setEndTime(res.getEndTime());
        item.setStudentCount(res.getStudentCount());
        item.setExperimentContent(res.getExperimentContent());
        item.setStatus(res.getStatus());

        // 映射到标准节次：优先用 timeSlotId，否则通过时间匹配
        Long timeSlotId = res.getTimeSlotId();
        TimeSlotEntity matched = findTimeSlotById(timeSlots, timeSlotId);
        if (matched == null) {
            // 非标准时间段，通过时间中点匹配最近的节次
            matched = findNearestTimeSlot(timeSlots, res.getStartTime(), res.getEndTime());
        }
        if (matched != null) {
            item.setTimeSlotId(matched.getId());
            item.setTimeSlotName(matched.getSlotName());
        }
        return item;
    }

    /**
     * 通过 ID 查找标准节次
     */
    private TimeSlotEntity findTimeSlotById(List<TimeSlotEntity> timeSlots, Long timeSlotId) {
        if (timeSlotId == null) return null;
        for (TimeSlotEntity ts : timeSlots) {
            if (ts.getId().equals(timeSlotId)) {
                return ts;
            }
        }
        return null;
    }

    /**
     * 非标准时间兼容：通过时间中点匹配最近的标准节次
     * 策略：计算预约时间段的中点，找到与中点距离最近且有重叠的节次；
     *       如果无重叠，找中点落在其时间范围内的节次；
     *       仍无匹配则找距离最近的节次。
     */
    private TimeSlotEntity findNearestTimeSlot(List<TimeSlotEntity> timeSlots, LocalTime start, LocalTime end) {
        if (timeSlots.isEmpty()) return null;

        // 计算时间中点（秒数）
        long startSec = start.toSecondOfDay();
        long endSec = end.toSecondOfDay();
        long midSec = (startSec + endSec) / 2;
        LocalTime midTime = LocalTime.ofSecondOfDay(midSec);

        // 策略1：找中点落在其范围内的节次
        for (TimeSlotEntity ts : timeSlots) {
            if (!midTime.isBefore(ts.getStartTime()) && !midTime.isAfter(ts.getEndTime())) {
                return ts;
            }
        }

        // 策略2：找时间段有重叠的节次（startTime < ts.endTime && endTime > ts.startTime）
        for (TimeSlotEntity ts : timeSlots) {
            if (start.isBefore(ts.getEndTime()) && end.isAfter(ts.getStartTime())) {
                return ts;
            }
        }

        // 策略3：找距离中点最近的节次
        TimeSlotEntity nearest = null;
        long minDiff = Long.MAX_VALUE;
        for (TimeSlotEntity ts : timeSlots) {
            long tsMidSec = (ts.getStartTime().toSecondOfDay() + ts.getEndTime().toSecondOfDay()) / 2;
            long diff = Math.abs(midSec - tsMidSec);
            if (diff < minDiff) {
                minDiff = diff;
                nearest = ts;
            }
        }
        return nearest;
    }

    /**
     * 构建 7×slotCount 的三维矩阵
     */
    private List<List<List<TimetableItemDTO>>> buildMatrix(List<TimeSlotEntity> timeSlots, int slotCount, List<TimetableItemDTO> items) {
        // 初始化：7天 × slotCount 个空列表
        List<List<List<TimetableItemDTO>>> matrix = new ArrayList<>(7);
        for (int d = 0; d < 7; d++) {
            List<List<TimetableItemDTO>> daySlots = new ArrayList<>(slotCount);
            for (int s = 0; s < slotCount; s++) {
                daySlots.add(new ArrayList<>());
            }
            matrix.add(daySlots);
        }

        // 填充数据
        for (TimetableItemDTO item : items) {
            Integer dayOfWeek = item.getDayOfWeek();
            Long timeSlotId = item.getTimeSlotId();
            if (dayOfWeek == null || dayOfWeek < 1 || dayOfWeek > 7 || timeSlotId == null) {
                continue;
            }
            // 找到 timeSlot 的索引
            int slotIndex = findSlotIndex(timeSlots, timeSlotId);
            if (slotIndex >= 0 && slotIndex < slotCount) {
                matrix.get(dayOfWeek - 1).get(slotIndex).add(item);
            }
        }
        return matrix;
    }

    /**
     * 查找 timeSlotId 在排序后列表中的索引
     */
    private int findSlotIndex(List<TimeSlotEntity> timeSlots, Long timeSlotId) {
        for (int i = 0; i < timeSlots.size(); i++) {
            if (timeSlots.get(i).getId().equals(timeSlotId)) {
                return i;
            }
        }
        return -1;
    }

    private Map<Long, String> loadCourseNames(List<ScheduleApplicationEntity> apps,
                                                List<ScheduleReservationEntity> ress) {
        Set<Long> ids = new HashSet<>();
        // ScheduleApplicationEntity 已移除 courseId，只从预约表加载
        ress.forEach(r -> { if (r.getCourseId() != null) ids.add(r.getCourseId()); });
        if (ids.isEmpty()) return Collections.emptyMap();
        Map<Long, String> map = new HashMap<>();
        courseRepository.findAllById(ids).forEach(c -> map.put(c.getId(), c.getCnName()));
        return map;
    }

    private Map<Long, String> loadClazzNames(List<ScheduleApplicationEntity> apps,
                                               List<ScheduleReservationEntity> ress) {
        Set<Long> ids = new HashSet<>();
        // ScheduleApplicationEntity 已移除 clazzId，只从预约表加载
        ress.forEach(r -> { if (r.getClazzId() != null) ids.add(r.getClazzId()); });
        if (ids.isEmpty()) return Collections.emptyMap();
        Map<Long, String> map = new HashMap<>();
        clazzRepository.findAllById(ids).forEach(c -> map.put(c.getId(), c.getClazzName()));
        return map;
    }

    private Map<Long, String> loadTeacherNames(List<ScheduleApplicationEntity> apps,
                                                 List<ScheduleReservationEntity> ress) {
        Set<Long> ids = new HashSet<>();
        apps.forEach(a -> { if (a.getTeacherId() != null) ids.add(a.getTeacherId()); });
        ress.forEach(r -> { if (r.getTeacherId() != null) ids.add(r.getTeacherId()); });
        if (ids.isEmpty()) return Collections.emptyMap();
        Map<Long, String> map = new HashMap<>();
        userRepository.findAllById(ids).forEach(u -> map.put(u.getId(), u.getRealName()));
        return map;
    }

    private TimeSlotDTO toTimeSlotDTO(TimeSlotEntity entity) {
        TimeSlotDTO dto = new TimeSlotDTO();
        dto.setId(entity.getId());
        dto.setSlotName(entity.getSlotName());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setSortOrder(entity.getSortOrder());
        return dto;
    }
}
