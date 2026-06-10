package com.example.labmanage.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class TeachingTaskDTO {
    private Long id;

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotNull(message = "学期ID不能为空")
    private Long termId;

    @NotNull(message = "班级ID不能为空")
    private Long clazzId;

    @NotNull(message = "教师ID不能为空")
    private List<Long> teacherIds;

    private String status;

    private ApplicationInfoDTO applicationInfo;

    /** 展示用 */
    private String courseName;
    private String termName;
    private String clazzName;
    private String teacherNames;
}