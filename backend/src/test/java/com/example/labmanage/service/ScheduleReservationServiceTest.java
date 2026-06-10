package com.example.labmanage.service;

import com.example.labmanage.dto.LabAvailabilityItem;
import com.example.labmanage.entity.ScheduleApplicationEntity;
import com.example.labmanage.entity.TermEntity;
import com.example.labmanage.entity.TimeSlotEntity;
import com.example.labmanage.entity.UserEntity;
import com.example.labmanage.repository.ClazzRepository;
import com.example.labmanage.repository.CourseRepository;
import com.example.labmanage.repository.ScheduleApplicationRepository;
import com.example.labmanage.repository.ScheduleOperationLogRepository;
import com.example.labmanage.repository.ScheduleReservationRepository;
import com.example.labmanage.repository.ScheduleUsageRecordRepository;
import com.example.labmanage.repository.TeachingTaskRepository;
import com.example.labmanage.repository.TermRepository;
import com.example.labmanage.repository.TimeSlotRepository;
import com.example.labmanage.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleReservationServiceTest {

    @Mock
    private ScheduleReservationRepository repository;

    @Mock
    private ScheduleApplicationRepository applicationRepository;

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private TermRepository termRepository;

    @Mock
    private TeachingTaskRepository teachingTaskRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClazzRepository clazzRepository;

    @Mock
    private ScheduleUsageRecordRepository usageRecordRepository;

    @Mock
    private ScheduleOperationLogService operationLogService;

    @Mock
    private ScheduleNoticeService noticeService;

    @InjectMocks
    private ScheduleReservationService scheduleReservationService;

    @Test
    void getLabAvailability_shouldHandleApplicationConflictWithNullCourseAndClazzIds() {
        TermEntity term = new TermEntity();
        term.setId(1L);
        term.setStartDate(LocalDate.of(2026, 2, 23));

        TimeSlotEntity selectedSlot = new TimeSlotEntity();
        selectedSlot.setId(1L);
        selectedSlot.setSlotName("1-2节");
        selectedSlot.setStartTime(LocalTime.of(8, 0));
        selectedSlot.setEndTime(LocalTime.of(9, 35));
        selectedSlot.setSortOrder(1);

        TimeSlotEntity otherSlot = new TimeSlotEntity();
        otherSlot.setId(2L);
        otherSlot.setSlotName("3-4节");
        otherSlot.setStartTime(LocalTime.of(10, 0));
        otherSlot.setEndTime(LocalTime.of(11, 35));
        otherSlot.setSortOrder(2);

        ScheduleApplicationEntity app = new ScheduleApplicationEntity();
        app.setId(10L);
        app.setTermId(1L);
        app.setPreferredWeekNo(2);
        app.setPreferredDayOfWeek(1);
        app.setPreferredBuildingName("明德楼");
        app.setPreferredRoomNumber("B201");
        app.setPreferredTimeSlotId(2L);
        app.setTeacherId(99L);
        app.setStatus("APPROVED");

        UserEntity teacher = new UserEntity();
        teacher.setId(99L);
        teacher.setRealName("张老师");

        when(termRepository.findById(1L)).thenReturn(Optional.of(term));
        when(timeSlotRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(selectedSlot, otherSlot));
        when(timeSlotRepository.findAll()).thenReturn(List.of(selectedSlot, otherSlot));
        when(repository.findAllDistinctLabs()).thenReturn(List.<Object[]>of(new Object[]{"明德楼", "B201"}));
        when(repository.findByTermIdAndWeekNoInAndStatusInAndDeletedFalse(1L, List.of(2), List.of("PENDING", "APPROVED", "IN_USE", "SUBMITTED", "APPROVING")))
                .thenReturn(List.of());
        when(applicationRepository.findByTermIdAndPreferredWeekNoInAndPreferredDayOfWeekAndStatusInAndDeletedFalse(
                1L, List.of(2), 1, List.of("APPROVED", "PENDING", "IN_USE")))
                .thenReturn(List.of(app));
        when(userRepository.findById(99L)).thenReturn(Optional.of(teacher));

        List<LabAvailabilityItem> result = scheduleReservationService.getLabAvailability(1L, List.of(2), 1, List.of(1L, 2L), "明德楼", null);

        assertThat(result).hasSize(1);
        LabAvailabilityItem item = result.get(0);
        assertThat(item.isAvailable()).isFalse();
        assertThat(item.getConflicts()).hasSize(1);
        assertThat(item.getConflicts().get(0).getCourseName()).isEqualTo("未知课程");
        assertThat(item.getConflicts().get(0).getClazzName()).isEqualTo("未知班级");
        assertThat(item.getConflicts().get(0).getTeacherName()).isEqualTo("张老师");
        verify(courseRepository, never()).findById(null);
        verify(clazzRepository, never()).findById(null);
    }
}
