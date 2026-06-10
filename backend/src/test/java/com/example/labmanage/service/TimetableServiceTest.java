package com.example.labmanage.service;

import com.example.labmanage.dto.MatrixTimetableDTO;
import com.example.labmanage.entity.ScheduleApplicationEntity;
import com.example.labmanage.entity.TimeSlotEntity;
import com.example.labmanage.repository.ClazzRepository;
import com.example.labmanage.repository.CourseRepository;
import com.example.labmanage.repository.ScheduleApplicationRepository;
import com.example.labmanage.repository.ScheduleReservationRepository;
import com.example.labmanage.repository.TimeSlotRepository;
import com.example.labmanage.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimetableServiceTest {

    @Mock
    private ScheduleApplicationRepository applicationRepository;

    @Mock
    private ScheduleReservationRepository reservationRepository;

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ClazzRepository clazzRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TimetableService timetableService;

    @Test
    void getTimetableMatrix_shouldIgnoreRejectedAndCancelledApplications() {
        TimeSlotEntity slot = new TimeSlotEntity();
        slot.setId(1L);
        slot.setSlotName("1-2节");
        slot.setStartTime(LocalTime.of(8, 0));
        slot.setEndTime(LocalTime.of(9, 35));
        slot.setSortOrder(1);

        ScheduleApplicationEntity approved = new ScheduleApplicationEntity();
        approved.setId(1L);
        approved.setApplicationNo("APP-1");
        approved.setProjectName("有效课程");
        approved.setPreferredBuildingName("明德楼");
        approved.setPreferredRoomNumber("B201");
        approved.setPreferredDayOfWeek(2);
        approved.setPreferredTimeSlotId(1L);
        approved.setStatus("APPROVED");

        when(timeSlotRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of(slot));
        when(applicationRepository.findByTermIdAndWeekNoAndBuildingNameAndOptionalRoomAndStatusIn(
                1L, 3, "明德楼", "B201", List.of("APPROVED", "PENDING", "REJECTED", "CANCELLED", "IN_USE")))
                .thenReturn(List.of(approved));
        when(reservationRepository.findByTermIdAndWeekNoAndBuildingNameAndOptionalRoomAndStatusIn(
                1L, 3, "明德楼", "B201", List.of("APPROVED", "IN_USE", "PENDING", "SUBMITTED", "APPROVING")))
                .thenReturn(List.of());

        MatrixTimetableDTO result = timetableService.getTimetableMatrix(1L, 3, "明德楼", "B201");

        assertThat(result.getMatrix().get(1).get(0)).hasSize(1);
        assertThat(result.getMatrix().get(1).get(0).get(0).getCourseName()).isEqualTo("有效课程");
    }
}
