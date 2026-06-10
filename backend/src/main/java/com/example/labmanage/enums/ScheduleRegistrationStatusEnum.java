package com.example.labmanage.enums;

import lombok.Getter;

@Getter
public enum ScheduleRegistrationStatusEnum {
    PENDING("待登记"),
    REGISTERED("已登记"),
    OVERDUE("逾期未登记");

    private final String label;

    ScheduleRegistrationStatusEnum(String label) {
        this.label = label;
    }
}
