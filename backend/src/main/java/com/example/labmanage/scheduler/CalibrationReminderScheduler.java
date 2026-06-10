package com.example.labmanage.scheduler;

import com.example.labmanage.entity.EquipmentEntity;
import com.example.labmanage.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 设备检定提醒定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CalibrationReminderScheduler {

    private final EquipmentRepository equipmentRepository;

    /**
     * 每天凌晨6点检查即将到期的检定
     */
    @Scheduled(cron = "0 0 6 * * ?")
    public void checkCalibrationReminders() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysLater = today.plusDays(30);

        List<EquipmentEntity> allEquipment = equipmentRepository.findAll();
        int warningCount = 0;
        int expiredCount = 0;

        for (EquipmentEntity eq : allEquipment) {
            if (eq.getNextCalibrationDate() == null) continue;

            long daysUntilCalibration = ChronoUnit.DAYS.between(today, eq.getNextCalibrationDate());

            if (daysUntilCalibration < 0) {
                // 已过期
                expiredCount++;
                log.warn("[检定提醒] 设备 [{}] ({}) 检定已过期 {} 天！",
                        eq.getName(), eq.getAssetNo(), Math.abs(daysUntilCalibration));
            } else if (daysUntilCalibration <= 30) {
                // 30天内即将到期
                warningCount++;
                log.warn("[检定提醒] 设备 [{}] ({}) 检定将在 {} 天后到期（{}），请及时安排检定！",
                        eq.getName(), eq.getAssetNo(), daysUntilCalibration, eq.getNextCalibrationDate());
            }
        }

        if (warningCount > 0 || expiredCount > 0) {
            log.info("[检定提醒] 检查完成：{} 台设备即将到期，{} 台设备已过期", warningCount, expiredCount);
        }
    }

    /**
     * 获取即将到期的检定设备列表（供前端调用）
     */
    public List<EquipmentEntity> getUpcomingCalibrations() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysLater = today.plusDays(30);

        return equipmentRepository.findAll().stream()
                .filter(eq -> eq.getNextCalibrationDate() != null)
                .filter(eq -> {
                    long days = ChronoUnit.DAYS.between(today, eq.getNextCalibrationDate());
                    return days <= 30; // 30天内到期或已过期
                })
                .sorted((a, b) -> a.getNextCalibrationDate().compareTo(b.getNextCalibrationDate()))
                .toList();
    }
}
