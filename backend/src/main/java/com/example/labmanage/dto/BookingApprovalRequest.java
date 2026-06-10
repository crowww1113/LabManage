package com.example.labmanage.dto;

import lombok.Data;

@Data
public class BookingApprovalRequest {
    private Long operatorId;
    private String reason;
}
