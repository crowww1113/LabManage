package com.example.labmanage.dto;

import lombok.Data;

@Data
public class ReviewActionRequest {
    private Long reviewerId;

    private String reviewComment;
}
