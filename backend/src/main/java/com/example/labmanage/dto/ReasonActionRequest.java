package com.example.labmanage.dto;

import lombok.Data;

@Data
public class ReasonActionRequest {
    private Long operatorId;

    private String reason;
}
