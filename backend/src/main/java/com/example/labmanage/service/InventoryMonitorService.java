package com.example.labmanage.service;

import com.example.labmanage.entity.EquipmentBorrowRecordEntity;
import com.example.labmanage.entity.EquipmentCategoryEntity;
import com.example.labmanage.entity.EquipmentEntity;
import com.example.labmanage.entity.EquipmentLocationEntity;
import com.example.labmanage.entity.EquipmentRepairRecordEntity;
import com.example.labmanage.entity.UserEntity;
import com.example.labmanage.repository.EquipmentBorrowRecordRepository;
import com.example.labmanage.repository.EquipmentCategoryRepository;
import com.example.labmanage.repository.EquipmentLocationRepository;
import com.example.labmanage.repository.EquipmentRepairRecordRepository;
import com.example.labmanage.repository.EquipmentRepository;
import com.example.labmanage.repository.UserRepository;
import com.example.labmanage.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryMonitorService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentBorrowRecordRepository borrowRecordRepository;
    private final EquipmentCategoryRepository categoryRepository;
    private final EquipmentLocationRepository locationRepository;
    private final EquipmentRepairRecordRepository repairRecordRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final NotificationService notificationService;

    private String getCategoryName(Long categoryId) {
        if (categoryId == null) return "未分类";
        return categoryRepository.findById(categoryId).map(EquipmentCategoryEntity::getName).orElse("未分类");
    }

    private String getLocationName(Long locationId) {
        if (locationId == null) return "未指定";
        return locationRepository.findById(locationId).map(EquipmentLocationEntity::getName).orElse("未指定");
    }

    private String getUserName(Long userId) {
        if (userId == null) return "未知";
        return userRepository.findById(userId).map(UserEntity::getRealName).orElse("未知");
    }

    /** 设备总账与分类统计 */
    public Map<String, Object> getEquipmentSummary() {
        List<EquipmentEntity> all = equipmentRepository.findAll();

        long total = all.size();
        double totalValue = all.stream()
                .mapToDouble(e -> e.getPrice() != null ? e.getPrice().doubleValue() : 0)
                .sum();

        // 按分类统计
        Map<String, Long> categoryCount = new HashMap<>();
        Map<String, Double> categoryValue = new HashMap<>();
        for (EquipmentEntity e : all) {
            String cat = getCategoryName(e.getCategoryId());
            categoryCount.merge(cat, 1L, Long::sum);
            categoryValue.merge(cat, e.getPrice() != null ? e.getPrice().doubleValue() : 0, Double::sum);
        }

        List<Map<String, Object>> categories = new ArrayList<>();
        for (String cat : categoryCount.keySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("category", cat);
            item.put("count", categoryCount.get(cat));
            item.put("value", categoryValue.get(cat));
            item.put("percentage", total > 0 ? String.format("%.1f%%", categoryCount.get(cat) * 100.0 / total) : "0%");
            categories.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("totalValue", totalValue);
        result.put("categories", categories);
        return result;
    }

    /** 状态分布表 */
    public List<Map<String, Object>> getStatusDistribution() {
        List<EquipmentEntity> all = equipmentRepository.findAll();

        Map<String, Long> statusCount = new LinkedHashMap<>();
        for (EquipmentEntity e : all) {
            String status = e.getStatus() != null ? e.getStatus() : "未知";
            statusCount.merge(status, 1L, Long::sum);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : statusCount.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("status", entry.getKey());
            item.put("count", entry.getValue());
            result.add(item);
        }
        return result;
    }

    /** 使用率统计 */
    public List<Map<String, Object>> getUsageRate() {
        List<EquipmentEntity> all = equipmentRepository.findAll();

        List<EquipmentBorrowRecordEntity> records = borrowRecordRepository.findAll();
        LocalDate today = LocalDate.now();

        List<Map<String, Object>> result = new ArrayList<>();
        for (EquipmentEntity e : all) {
            long borrowDays = records.stream()
                    .filter(r -> r.getEquipmentId() != null && r.getEquipmentId().equals(e.getId()) && r.getActualReturnDate() != null)
                    .mapToLong(r -> {
                        LocalDate start = r.getCreateTime() != null ? r.getCreateTime() : today;
                        return ChronoUnit.DAYS.between(start, r.getActualReturnDate());
                    })
                    .sum();
            long totalDays = e.getPurchaseDate() != null ? ChronoUnit.DAYS.between(e.getPurchaseDate(), today) : 365;
            if (totalDays <= 0) totalDays = 1;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("equipmentId", e.getId());
            item.put("assetNo", e.getAssetNo());
            item.put("name", e.getName());
            item.put("borrowDays", borrowDays);
            item.put("totalDays", totalDays);
            item.put("usageRate", String.format("%.1f%%", borrowDays * 100.0 / totalDays));
            result.add(item);
        }
        return result;
    }

    /** 借还流水账 */
    public List<Map<String, Object>> getBorrowLedger() {
        List<EquipmentBorrowRecordEntity> records = borrowRecordRepository.findAll();
        return records.stream().map(r -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("recordNo", r.getRecordNo());
            item.put("equipmentName", getEquipmentName(r.getEquipmentId()));
            item.put("equipmentAssetNo", getEquipmentAssetNo(r.getEquipmentId()));
            item.put("borrowerName", getUserName(r.getBorrowerId()));
            item.put("purpose", r.getPurpose());
            item.put("status", r.getStatus());
            item.put("createTime", r.getCreateTime());
            item.put("borrowDate", r.getPickupTime() != null ? r.getPickupTime() : (r.getApproveTime() != null ? r.getApproveTime() : r.getCreateTime()));
            item.put("expectedReturnDate", r.getExpectedReturnDate());
            item.put("actualReturnDate", r.getActualReturnDate());
            item.put("verifierName", r.getVerifierId() != null ? getUserName(r.getVerifierId()) : "-");
            item.put("returnResult", r.getReturnResult() != null ? r.getReturnResult() : "-");
            item.put("pickupPersonName", r.getPickupPersonId() != null ? getUserName(r.getPickupPersonId()) : "-");
            item.put("mentorApproveTime", r.getMentorApproveTime());
            item.put("approveTime", r.getApproveTime());
            item.put("pickupTime", r.getPickupTime());
            item.put("returnConfirmTime", r.getReturnConfirmTime());
            return item;
        }).collect(Collectors.toList());
    }

    /** 逾期清单 */
    public List<Map<String, Object>> getOverdueList() {
        LocalDate today = LocalDate.now();
        List<EquipmentBorrowRecordEntity> overdueRecords = borrowRecordRepository.findOverdueRecords(today);

        return overdueRecords.stream().map(r -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("recordNo", r.getRecordNo());
            item.put("equipmentName", getEquipmentName(r.getEquipmentId()));
            item.put("equipmentAssetNo", getEquipmentAssetNo(r.getEquipmentId()));
            item.put("borrowerName", getUserName(r.getBorrowerId()));
            item.put("borrowerPhone", r.getPhone());
            item.put("expectedReturnDate", r.getExpectedReturnDate());
            item.put("overdueDays", ChronoUnit.DAYS.between(r.getExpectedReturnDate(), today));
            item.put("status", r.getStatus());
            item.put("overdueWarning", r.getOverdueWarning());
            item.put("overdueWarningTime", r.getOverdueWarningTime());
            return item;
        }).collect(Collectors.toList());
    }

    /** 故障维修统计 */
    public List<Map<String, Object>> getRepairStats() {
        List<EquipmentEntity> all = equipmentRepository.findAll();

        List<Map<String, Object>> result = new ArrayList<>();
        for (EquipmentEntity e : all) {
            if ("在库-待维修".equals(e.getStatus()) || "送修".equals(e.getStatus())) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("equipmentId", e.getId());
                item.put("assetNo", e.getAssetNo());
                item.put("name", e.getName());
                item.put("status", e.getStatus());
                item.put("location", getLocationName(e.getLocationId()));
                item.put("repairCount", e.getRepairCount() != null ? e.getRepairCount() : 0);
                item.put("totalRepairDays", e.getTotalRepairDays() != null ? e.getTotalRepairDays() : 0);

                // 获取最新维修记录
                List<EquipmentRepairRecordEntity> repairRecords = repairRecordRepository.findByEquipmentId(e.getId());
                if (!repairRecords.isEmpty()) {
                    EquipmentRepairRecordEntity latest = repairRecords.get(repairRecords.size() - 1);
                    item.put("latestFaultDescription", latest.getFaultDescription());
                    item.put("latestRepairStatus", latest.getRepairStatus());
                    item.put("latestReportDate", latest.getReportDate());
                    item.put("latestRepairDuration", latest.getRepairDurationDays());
                    item.put("latestRepairPerson", latest.getRepairPerson());
                } else {
                    item.put("latestFaultDescription", "-");
                    item.put("latestRepairStatus", "-");
                    item.put("latestReportDate", "-");
                    item.put("latestRepairDuration", "-");
                    item.put("latestRepairPerson", "-");
                }
                result.add(item);
            }
        }
        return result;
    }

    private String getEquipmentName(Long equipmentId) {
        if (equipmentId == null) return "未知设备";
        return equipmentRepository.findById(equipmentId).map(EquipmentEntity::getName).orElse("未知设备");
    }

    private String getEquipmentAssetNo(Long equipmentId) {
        if (equipmentId == null) return "-";
        return equipmentRepository.findById(equipmentId).map(EquipmentEntity::getAssetNo).orElse("-");
    }

    /** 实时库存查询 - 多维筛选 */
    public List<Map<String, Object>> queryInventory(String category, String location, String brand, String status) {
        List<EquipmentEntity> all = equipmentRepository.findAll();

        return all.stream()
                .filter(e -> {
                    if (category == null || category.isEmpty()) return true;
                    String catName = getCategoryName(e.getCategoryId());
                    return catName.contains(category);
                })
                .filter(e -> {
                    if (location == null || location.isEmpty()) return true;
                    String locName = getLocationName(e.getLocationId());
                    return locName.contains(location);
                })
                .filter(e -> brand == null || brand.isEmpty() || (e.getBrand() != null && e.getBrand().contains(brand)))
                .filter(e -> status == null || status.isEmpty() || (e.getStatus() != null && e.getStatus().equals(status)))
                .map(e -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", e.getId());
                    item.put("assetNo", e.getAssetNo());
                    item.put("name", e.getName());
                    item.put("model", e.getModel());
                    item.put("category", getCategoryName(e.getCategoryId()));
                    item.put("brand", e.getBrand());
                    item.put("location", getLocationName(e.getLocationId()));
                    item.put("status", e.getStatus());
                    item.put("responsiblePerson", getUserName(e.getResponsibleId()));
                    item.put("price", e.getPrice());
                    item.put("purchaseDate", e.getPurchaseDate());
                    return item;
                })
                .collect(Collectors.toList());
    }

    /** 检定到期清单 */
    public List<Map<String, Object>> getCalibrationDueList() {
        LocalDate today = LocalDate.now();
        LocalDate nextMonth = today.plusMonths(1);
        List<EquipmentEntity> all = equipmentRepository.findAll();

        List<Map<String, Object>> result = new ArrayList<>();
        for (EquipmentEntity e : all) {
            if (e.getNextCalibrationDate() != null) {
                // 检定到期或即将到期（30天内）
                if (!e.getNextCalibrationDate().isAfter(nextMonth)) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("equipmentId", e.getId());
                    item.put("assetNo", e.getAssetNo());
                    item.put("name", e.getName());
                    item.put("category", getCategoryName(e.getCategoryId()));
                    item.put("location", getLocationName(e.getLocationId()));
                    item.put("lastCalibrationDate", e.getLastCalibrationDate());
                    item.put("nextCalibrationDate", e.getNextCalibrationDate());
                    item.put("calibrationPeriodMonths", e.getCalibrationPeriodMonths());
                    item.put("responsiblePerson", getUserName(e.getResponsibleId()));

                    long daysUntilDue = ChronoUnit.DAYS.between(today, e.getNextCalibrationDate());
                    item.put("daysUntilDue", daysUntilDue);
                    if (daysUntilDue < 0) {
                        item.put("status", "已逾期");
                    } else if (daysUntilDue <= 7) {
                        item.put("status", "即将到期");
                    } else {
                        item.put("status", "正常");
                    }
                    result.add(item);
                }
            }
        }
        return result;
    }

    /** 检查并发送检定到期提醒 */
    public void checkAndNotifyCalibrationDue() {
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusWeeks(1);
        List<EquipmentEntity> all = equipmentRepository.findAll();

        for (EquipmentEntity e : all) {
            if (e.getNextCalibrationDate() != null) {
                // 7天内到期
                if (!e.getNextCalibrationDate().isAfter(nextWeek) && !e.getNextCalibrationDate().isBefore(today)) {
                    String equipInfo = e.getName() + "（" + e.getAssetNo() + "）";
                    long daysLeft = ChronoUnit.DAYS.between(today, e.getNextCalibrationDate());

                    // 通知责任人
                    if (e.getResponsibleId() != null) {
                        String responsibleName = getUserName(e.getResponsibleId());
                        notificationService.createNotification(e.getResponsibleId(), responsibleName, "检定到期",
                                "设备检定到期提醒",
                                "您负责的设备 " + equipInfo + " 将于 " + daysLeft + " 天后到期（" + e.getNextCalibrationDate() + "），请及时安排检定");
                    }

                    // 通知管理员
                    sendNotificationToAdmins("检定到期", "设备检定即将到期",
                            equipInfo + " 将于 " + daysLeft + " 天后到期，请及时安排检定");
                }
            }
        }
    }

    /** 向所有管理员发送通知 */
    private void sendNotificationToAdmins(String type, String title, String content) {
        List<Long> adminIds = userRoleRepository.findUserIdsByRoleCodes(List.of("LAB_ADMIN", "EQUIPMENT_ADMIN"));
        for (Long adminId : adminIds) {
            userRepository.findById(adminId).ifPresent(admin -> {
                notificationService.createNotification(adminId, admin.getRealName(), type, title, content);
            });
        }
    }
}
