package com.example.labmanage.controller;

import com.example.labmanage.dto.MatrixTimetableDTO;
import com.example.labmanage.dto.TimetableItemDTO;
import com.example.labmanage.dto.TimeSlotDTO;
import com.example.labmanage.entity.ExpItem;
import com.example.labmanage.entity.ExpOpen;
import com.example.labmanage.entity.ExpQuality;
import com.example.labmanage.entity.ExpTask;
import com.example.labmanage.entity.TrainingPlan;
import com.example.labmanage.service.ExpItemService;
import com.example.labmanage.service.ExpOpenService;
import com.example.labmanage.service.ExpQualityService;
import com.example.labmanage.service.ExpTaskService;
import com.example.labmanage.service.TimetableService;
import com.example.labmanage.service.TrainingPlanService;
import com.example.labmanage.util.WordExportUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/export")
public class ExportController {

    @Autowired
    private ExpTaskService expTaskService;
    @Autowired
    private ExpItemService expItemService;
    @Autowired
    private ExpOpenService expOpenService;
    @Autowired
    private ExpQualityService expQualityService;
    @Autowired
    private TrainingPlanService trainingPlanService;
    @Autowired
    private TimetableService timetableService;

    // 1. 导出实验教学任务表（按模板格式）
    @GetMapping("/exp-task")
    public void exportExpTask(HttpServletResponse response) throws Exception {
        // 按照模板格式：专业、班级 | 学生人数 | 实验课程名称 | 实验总学时 | 本学期实验学时 | 课程承担部门 | 实验指导教师姓名及职称
        List<String> headers = List.of("专业、班级", "学生人数", "实验课程名称", "实验总学时", "本学期实验学时", "课程承担部门", "实验指导教师姓名及职称");
        List<List<String>> data = new ArrayList<>();

        List<ExpTask> taskList = expTaskService.listAll();
        for (ExpTask task : taskList) {
            List<String> row = new ArrayList<>();
            // 专业、班级
            String majorClass = task.getMajor() == null ? "" : task.getMajor();
            row.add(majorClass);
            // 学生人数
            row.add(task.getStudentCount() == null ? "" : task.getStudentCount().toString());
            // 实验课程名称
            row.add(task.getCourseName() == null ? "" : task.getCourseName());
            // 实验总学时
            row.add(task.getTotalExpHour() == null ? "" : task.getTotalExpHour().toString());
            // 本学期实验学时
            row.add(task.getCurrentExpHour() == null ? "" : task.getCurrentExpHour().toString());
            // 课程承担部门
            row.add(task.getDepartment() == null ? "" : task.getDepartment());
            // 实验指导教师姓名及职称
            String teacherInfo = "";
            if (task.getTeacher() != null) {
                teacherInfo = task.getTeacher();
                if (task.getTeacherTitle() != null) {
                    teacherInfo += " " + task.getTeacherTitle();
                }
            }
            row.add(teacherInfo);
            data.add(row);
        }

        WordExportUtil.exportExpTaskToWord("实验课程教学任务一览表", headers, data, "", response);
    }

    // 2. 导出实验项目库表
    @GetMapping("/exp-item")
    public void exportExpItem(HttpServletResponse response) throws Exception {
        List<String> headers = List.of("课程编号", "实验项目名称", "实验学时", "实验类别", "实验要求");
        List<List<String>> data = new ArrayList<>();

        List<ExpItem> itemList = expItemService.listAll();
        for (ExpItem item : itemList) {
            List<String> row = new ArrayList<>();
            row.add(item.getCourseId() == null ? "" : item.getCourseId().toString());
            row.add(item.getItemName() == null ? "" : item.getItemName());
            row.add(item.getHour() == null ? "" : item.getHour().toString());
            row.add(item.getExpType() == null ? "" : item.getExpType());
            row.add(item.getRequirement() == null ? "" : item.getRequirement());
            data.add(row);
        }

        WordExportUtil.exportTableToWord("实验项目库一览表", headers, data, response);
    }

    // 3. 导出实验项目开出表
    @GetMapping("/exp-open")
    public void exportExpOpen(HttpServletResponse response) throws Exception {
        List<String> headers = List.of("任务编号", "项目编号", "上课周次", "上课星期", "上课节次", "实验组数", "每组人数", "循环次数", "实验要求", "实验地点",
                "是否开出", "未能开出原因");
        List<List<String>> data = new ArrayList<>();

        List<ExpOpen> openList = expOpenService.listAll();
        for (ExpOpen open : openList) {
            List<String> row = new ArrayList<>();
            row.add(open.getTaskId() == null ? "" : open.getTaskId().toString());
            row.add(open.getItemId() == null ? "" : open.getItemId().toString());
            row.add(open.getWeek() == null ? "" : open.getWeek().toString());
            String dayOfWeek = "";
            if (open.getDayOfWeek() != null) {
                String[] days = { "", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日" };
                dayOfWeek = days[open.getDayOfWeek()];
            }
            row.add(dayOfWeek);
            row.add(open.getClassSection() == null ? "" : open.getClassSection().toString());
            row.add(open.getGroupCount() == null ? "" : open.getGroupCount().toString());
            row.add(open.getStudentsPerGroup() == null ? "" : open.getStudentsPerGroup().toString());
            row.add(open.getCycleCount() == null ? "" : open.getCycleCount().toString());
            row.add(open.getRequirement() == null ? "" : open.getRequirement());
            String location = "";
            if (open.getBuildingName() != null || open.getRoomNumber() != null) {
                location = (open.getBuildingName() == null ? "" : open.getBuildingName()) +
                        (open.getRoomNumber() == null ? "" : " " + open.getRoomNumber());
            }
            row.add(location.trim());
            row.add(open.getIsOpend() == null ? "" : (open.getIsOpend() ? "是" : "否"));
            row.add(open.getNotOpendReason() == null ? "" : open.getNotOpendReason());
            data.add(row);
        }

        WordExportUtil.exportTableToWord("实验项目开出一览表", headers, data, response);
    }

    // 4. 导出实训计划表
    @GetMapping("/training-plan")
    public void exportTrainingPlan(HttpServletResponse response) throws Exception {
        List<String> headers = List.of("课程编号", "实训教学组织方式", "实训教学地点", "实训教学目的和要求", "教学内容及（计划）安排",
                "实训方式（含循环分组情况）", "课程考核方式", "实训教学质量保障措施", "实验中心意见", "院系意见");
        List<List<String>> data = new ArrayList<>();

        List<TrainingPlan> planList = trainingPlanService.listAll();
        for (TrainingPlan plan : planList) {
            List<String> row = new ArrayList<>();
            row.add(plan.getCourseId() == null ? "" : plan.getCourseId().toString());
            row.add(plan.getOrganizationMode() == null ? "" : plan.getOrganizationMode());
            row.add(plan.getTrainingPlace() == null ? "" : plan.getTrainingPlace());
            row.add(plan.getTrainingObjective() == null ? "" : plan.getTrainingObjective());
            row.add(plan.getTrainingContent() == null ? "" : plan.getTrainingContent());
            row.add(plan.getTrainingMethod() == null ? "" : plan.getTrainingMethod());
            row.add(plan.getAssessmentMethod() == null ? "" : plan.getAssessmentMethod());
            row.add(plan.getQualityMeasures() == null ? "" : plan.getQualityMeasures());
            row.add(plan.getExpCenterOpinion() == null ? "" : plan.getExpCenterOpinion());
            row.add(plan.getDeptOpinion() == null ? "" : plan.getDeptOpinion());
            data.add(row);
        }

        WordExportUtil.exportTableToWord("实训教学计划表", headers, data, response);
    }

    // 5. 导出实验课程教学质量表
    @GetMapping("/exp-quality")
    public void exportExpQuality(HttpServletResponse response) throws Exception {
        List<String> headers = List.of("任务编号", "机构", "课程名", "实验学时", "是否独立设课", "主讲教师", "职称", "实验技术人员", "职称", "授课班级",
                "班级人数", "计划实验个数", "实际开出个数", "未开出原因", "考核方式", "考核人数", "考核时间");
        List<List<String>> data = new ArrayList<>();

        List<ExpQuality> qualityList = expQualityService.listAll();
        for (ExpQuality quality : qualityList) {
            List<String> row = new ArrayList<>();
            row.add(quality.getTaskId() == null ? "" : quality.getTaskId().toString());
            row.add(quality.getOrganization() == null ? "" : quality.getOrganization());
            row.add(quality.getCourseName() == null ? "" : quality.getCourseName());
            row.add(quality.getExpHour() == null ? "" : quality.getExpHour().toString());
            row.add(quality.getIndependentCourse() == null ? "" : quality.getIndependentCourse().toString());
            row.add(quality.getMainTeacher() == null ? "" : quality.getMainTeacher());
            row.add(quality.getTeacherTitle() == null ? "" : quality.getTeacherTitle());
            row.add(quality.getTechnician() == null ? "" : quality.getTechnician());
            row.add(quality.getTechnicianTitle() == null ? "" : quality.getTechnicianTitle());
            row.add(quality.getClassName() == null ? "" : quality.getClassName());
            row.add(quality.getStudentCount() == null ? "" : quality.getStudentCount().toString());
            row.add(quality.getPlannedExpCount() == null ? "" : quality.getPlannedExpCount().toString());
            row.add(quality.getActualExpCount() == null ? "" : quality.getActualExpCount().toString());
            row.add(quality.getMissedExpReason() == null ? "" : quality.getMissedExpReason());
            row.add(quality.getAssessmentMethod() == null ? "" : quality.getAssessmentMethod());
            row.add(quality.getAssessmentCount() == null ? "" : quality.getAssessmentCount().toString());
            row.add(quality.getAssessmentTime() == null ? "" : quality.getAssessmentTime());
            data.add(row);
        }

        WordExportUtil.exportTableToWord("实验课程教学质量一览表", headers, data, response);
    }

    // 6. 导出周课表矩阵
    @GetMapping("/weekly-timetable")
    public void exportWeeklyTimetable(
            @RequestParam Long termId,
            @RequestParam Integer weekNo,
            @RequestParam(required = false) String buildingName,
            HttpServletResponse response) throws Exception {

        MatrixTimetableDTO matrixData = timetableService.getTimetableMatrix(termId, weekNo, buildingName, null);

        // 表头：节次 + 星期一~星期日
        List<String> headers = new ArrayList<>();
        headers.add("节次");
        headers.addAll(matrixData.getDays()); // ["星期一", ..., "星期日"]

        // 数据行：每行一个节次
        List<List<String>> data = new ArrayList<>();
        List<TimeSlotDTO> timeSlots = matrixData.getTimeSlots();
        List<List<List<TimetableItemDTO>>> matrix = matrixData.getMatrix();

        for (int slotIndex = 0; slotIndex < timeSlots.size(); slotIndex++) {
            List<String> row = new ArrayList<>();
            // 第一列：节次名称
            row.add(timeSlots.get(slotIndex).getSlotName());

            // 后续7列：每天的课程
            for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
                List<TimetableItemDTO> items = matrix.get(dayIndex).get(slotIndex);
                if (items == null || items.isEmpty()) {
                    row.add("");
                } else {
                    // 每个课程条目格式: 课程名\n教师 | 班级 | 教室
                    String cellContent = items.stream()
                            .map(item -> {
                                String courseName = item.getCourseName() == null ? "" : item.getCourseName();
                                String teacher = item.getTeacherName() == null ? "" : item.getTeacherName();
                                String clazz = item.getClazzName() == null ? "" : item.getClazzName();
                                String room = "";
                                if (item.getBuildingName() != null || item.getRoomNumber() != null) {
                                    room = (item.getBuildingName() == null ? "" : item.getBuildingName()) +
                                           (item.getRoomNumber() == null ? "" : " " + item.getRoomNumber());
                                }
                                return courseName + "\n" + teacher + " | " + clazz + " | " + room.trim();
                            })
                            .collect(Collectors.joining("\n\n"));
                    row.add(cellContent);
                }
            }
            data.add(row);
        }

        // 标题
        String title = buildingName != null && !buildingName.isEmpty()
                ? "第" + weekNo + "周 - " + buildingName + " 课表"
                : "第" + weekNo + "周课表";

        WordExportUtil.exportTableToWord(title, headers, data, response);
    }
}