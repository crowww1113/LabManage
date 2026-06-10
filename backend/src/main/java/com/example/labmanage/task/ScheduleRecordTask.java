package com.example.labmanage.task;

import com.example.labmanage.entity.ScheduleUsageRecordEntity;
import com.example.labmanage.enums.ScheduleRegistrationStatusEnum;
import com.example.labmanage.repository.ScheduleUsageRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleRecordTask {

    private final ScheduleUsageRecordRepository usageRecordRepository;

    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void markOverdueRecords() {
        LocalDate deadline = LocalDate.now().minusDays(7);
        List<ScheduleUsageRecordEntity> pendingRecords = usageRecordRepository
                .findByRecordStatusAndDeletedFalse(ScheduleRegistrationStatusEnum.PENDING);

        int overdueCount = 0;
        for (ScheduleUsageRecordEntity record : pendingRecords) {
            LocalDate usageDate = record.getUsageDate();
            if (usageDate != null && !usageDate.isAfter(deadline)) {
                record.setRecordStatus(ScheduleRegistrationStatusEnum.OVERDUE);
                usageRecordRepository.save(record);
                overdueCount++;
            }
        }

        log.info("使用登记逾期扫描完成，标记逾期 {} 条", overdueCount);
    }
}
