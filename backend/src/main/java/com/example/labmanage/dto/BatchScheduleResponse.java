package com.example.labmanage.dto;

import lombok.Data;
import java.util.List;

@Data
public class BatchScheduleResponse {
    private int totalCount;
    private List<String> reservationNos;
    private List<ScheduleReservationDTO> reservations;
}
