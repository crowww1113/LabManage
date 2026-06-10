package com.example.labmanage.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookingBatchScheduleResponse {
    private Long bookingId;
    private String bookingNo;
    private int totalReservations;
    private List<ScheduleReservationDTO> reservations;
}
