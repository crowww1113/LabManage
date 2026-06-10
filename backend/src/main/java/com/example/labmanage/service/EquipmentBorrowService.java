package com.example.labmanage.service;

import com.example.labmanage.dto.EquipmentBorrowRecordDTO;
import com.example.labmanage.entity.EquipmentBorrowRecordEntity;
import com.example.labmanage.entity.EquipmentEntity;
import com.example.labmanage.entity.UserEntity;
import com.example.labmanage.repository.EquipmentBorrowRecordRepository;
import com.example.labmanage.repository.EquipmentRepository;
import com.example.labmanage.repository.UserRepository;
import com.example.labmanage.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipmentBorrowService {

    private final EquipmentBorrowRecordRepository borrowRecordRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final OperationLogService operationLogService;
    private final NotificationService notificationService;

    public List<EquipmentBorrowRecordDTO> listAll() {
        List<EquipmentBorrowRecordEntity> records = borrowRecordRepository.findAll();
        return records.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<EquipmentBorrowRecordDTO> listByStatus(String status) {
        return borrowRecordRepository.findByStatus(status).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<EquipmentBorrowRecordDTO> listByBorrower(Long borrowerId) {
        return borrowRecordRepository.findByBorrowerId(borrowerId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public EquipmentBorrowRecordDTO getById(Long id) {
        EquipmentBorrowRecordEntity record = borrowRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在"));
        return toDTO(record);
    }

    public List<EquipmentBorrowRecordDTO> getOverdueRecords() {
        LocalDate today = LocalDate.now();
        return borrowRecordRepository.findOverdueRecords(today).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<EquipmentBorrowRecordDTO> getDueSoonRecords() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        return borrowRecordRepository.findDueTomorrowRecords(tomorrow).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<EquipmentBorrowRecordDTO> getDueTodayRecords() {
        return borrowRecordRepository.findDueTodayRecords(LocalDate.now()).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public EquipmentBorrowRecordDTO createBorrowRequest(Long equipmentId, Long borrowerId, String purpose, LocalDate expectedReturnDate, String remark, String phone, String useLocation, Long mentorId) {
        EquipmentEntity equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException("设备不存在"));
        if (!"在库-可用".equals(equipment.getStatus())) {
            throw new RuntimeException("设备当前状态不可借出：" + equipment.getStatus());
        }

        EquipmentBorrowRecordEntity record = new EquipmentBorrowRecordEntity();
        record.setRecordNo(generateRecordNo());
        record.setEquipmentId(equipmentId);
        record.setBorrowerId(borrowerId);
        record.setPurpose(purpose);
        record.setExpectedReturnDate(expectedReturnDate);
        record.setRemark(remark);
        record.setCreateTime(LocalDate.now());
        record.setPhone(phone);
        record.setUseLocation(useLocation);
        record.setMentorId(mentorId);

        // 如果有导师ID，需要导师先审批；否则直接待管理员审批
        if (mentorId != null) {
            record.setStatus("待导师审批");
        } else {
            record.setStatus("待管理员审批");
        }

        borrowRecordRepository.save(record);

        // 获取借用人姓名
        String borrowerRealName = userRepository.findById(borrowerId).map(UserEntity::getRealName).orElse("申请人");

        // 记录操作日志
        operationLogService.logOperation(borrowerId, borrowerRealName, "设备借还", "新增",
                "提交设备借用申请：" + equipment.getName() + "（" + equipment.getAssetNo() + "）", null);

        // 发送通知给审批人
        if (mentorId != null) {
            userRepository.findById(mentorId).ifPresent(mentor -> {
                notificationService.createNotification(mentorId, mentor.getRealName(), "借还申请",
                        "新的借用申请待审批",
                        borrowerRealName + " 申请借用 " + equipment.getName() + "，请及时审批");
            });
        } else {
            // 通知管理员
            sendNotificationToAdmins("借还申请", "新的借用申请待审批",
                    borrowerRealName + " 申请借用 " + equipment.getName() + "，请及时审批");
        }

        return toDTO(record);
    }

    @Transactional
    public EquipmentBorrowRecordDTO approveRecord(Long recordId, Long approverId, String approveRemark) {
        EquipmentBorrowRecordEntity record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("记录不存在"));

        String approverName = userRepository.findById(approverId).map(UserEntity::getRealName).orElse("审批人");
        String borrowerName = userRepository.findById(record.getBorrowerId()).map(UserEntity::getRealName).orElse("申请人");
        EquipmentEntity equipment = equipmentRepository.findById(record.getEquipmentId()).orElse(null);
        String equipInfo = equipment != null ? equipment.getName() + "（" + equipment.getAssetNo() + "）" : "设备";

        // 导师审批：待导师审批 → 待管理员审批
        if ("待导师审批".equals(record.getStatus())) {
            record.setMentorApproveRemark(approveRemark);
            record.setMentorApproveTime(LocalDate.now());
            record.setStatus("待管理员审批");
            record.setUpdateTime(LocalDate.now());
            borrowRecordRepository.save(record);

            // 记录操作日志
            operationLogService.logOperation(approverId, approverName, "设备借还", "审批",
                    "导师审批通过：" + equipInfo + "，流转至管理员审批", null);

            // 通知管理员
            sendNotificationToAdmins("审批", "借用申请导师已审批通过",
                    borrowerName + " 的借用申请 " + equipInfo + " 导师已审批通过，请管理员审批");

            return toDTO(record);
        }

        // 管理员审批：待管理员审批 → 已批准（库存锁定）
        if ("待管理员审批".equals(record.getStatus())) {
            record.setStatus("已批准");
            record.setApproverId(approverId);
            record.setApproveRemark(approveRemark);
            record.setApproveTime(LocalDate.now());
            record.setUpdateTime(LocalDate.now());

            // 库存锁定：更新设备状态为在库-已预约
            EquipmentEntity eq = equipmentRepository.findById(record.getEquipmentId()).orElse(null);
            if (eq != null) {
                eq.setStatus("在库-已预约");
                equipmentRepository.save(eq);
            }

            borrowRecordRepository.save(record);

            // 记录操作日志
            operationLogService.logOperation(approverId, approverName, "设备借还", "审批",
                    "管理员审批通过：" + equipInfo + "，库存已锁定", null);

            // 通知借用人
            notificationService.createNotification(record.getBorrowerId(), borrowerName, "审批",
                    "借用申请已批准",
                    "您的借用申请 " + equipInfo + " 已获批准，请及时领取设备");

            return toDTO(record);
        }

        throw new RuntimeException("当前状态不可审批：" + record.getStatus());
    }

    @Transactional
    public EquipmentBorrowRecordDTO rejectRecord(Long recordId, Long approverId, String approveRemark) {
        EquipmentBorrowRecordEntity record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("记录不存在"));

        String approverName = userRepository.findById(approverId).map(UserEntity::getRealName).orElse("审批人");
        String borrowerName = userRepository.findById(record.getBorrowerId()).map(UserEntity::getRealName).orElse("申请人");
        EquipmentEntity equipment = equipmentRepository.findById(record.getEquipmentId()).orElse(null);
        String equipInfo = equipment != null ? equipment.getName() + "（" + equipment.getAssetNo() + "）" : "设备";

        if (!"待导师审批".equals(record.getStatus()) && !"待管理员审批".equals(record.getStatus())) {
            throw new RuntimeException("当前状态不可审批：" + record.getStatus());
        }

        record.setStatus("已拒绝");
        if ("待导师审批".equals(record.getStatus()) || record.getMentorId() != null) {
            record.setMentorApproveRemark(approveRemark);
            record.setMentorApproveTime(LocalDate.now());
        } else {
            record.setApproverId(approverId);
            record.setApproveRemark(approveRemark);
            record.setApproveTime(LocalDate.now());
        }
        record.setUpdateTime(LocalDate.now());

        borrowRecordRepository.save(record);

        // 记录操作日志
        operationLogService.logOperation(approverId, approverName, "设备借还", "审批",
                "审批拒绝：" + equipInfo + "，理由：" + approveRemark, null);

        // 通知借用人
        notificationService.createNotification(record.getBorrowerId(), borrowerName, "审批",
                "借用申请已拒绝",
                "您的借用申请 " + equipInfo + " 已被拒绝，理由：" + approveRemark);

        return toDTO(record);
    }

    @Transactional
    public EquipmentBorrowRecordDTO pickupEquipment(Long recordId, Long pickupPersonId, String pickupRemark) {
        EquipmentBorrowRecordEntity record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("记录不存在"));
        if (!"已批准".equals(record.getStatus())) {
            throw new RuntimeException("当前状态不可领取：" + record.getStatus());
        }

        String pickupPersonName = userRepository.findById(pickupPersonId).map(UserEntity::getRealName).orElse("领取人");
        String borrowerName = userRepository.findById(record.getBorrowerId()).map(UserEntity::getRealName).orElse("申请人");
        EquipmentEntity equipment = equipmentRepository.findById(record.getEquipmentId()).orElse(null);
        String equipInfo = equipment != null ? equipment.getName() + "（" + equipment.getAssetNo() + "）" : "设备";

        // 记录领取信息
        record.setPickupPersonId(pickupPersonId);
        record.setPickupTime(LocalDate.now());
        record.setPickupRemark(pickupRemark);

        // 获取设备当前状态并记录
        EquipmentEntity eq = equipmentRepository.findById(record.getEquipmentId()).orElse(null);
        if (eq != null) {
            record.setPickupEquipmentStatus(eq.getStatus());
            // 更新设备状态为借出
            eq.setStatus("借出");
            equipmentRepository.save(eq);
        }

        record.setStatus("已借出");
        record.setUpdateTime(LocalDate.now());

        borrowRecordRepository.save(record);

        // 记录操作日志
        operationLogService.logOperation(pickupPersonId, pickupPersonName, "设备借还", "领取",
                "领取设备：" + equipInfo + "，领取时状态：" + (eq != null ? eq.getStatus() : "未知"), null);

        // 通知借用人
        notificationService.createNotification(record.getBorrowerId(), borrowerName, "完成",
                "设备已领取",
                "您借用的设备 " + equipInfo + " 已成功领取，请妥善保管，预计归还日期：" + record.getExpectedReturnDate());

        return toDTO(record);
    }

    @Transactional
    public EquipmentBorrowRecordDTO returnEquipment(Long recordId, Long verifierId, String returnResult, String returnLocation, String accessoriesInfo, String damageDescription) {
        EquipmentBorrowRecordEntity record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("记录不存在"));
        if (!"已借出".equals(record.getStatus()) && !"待验收".equals(record.getStatus())) {
            throw new RuntimeException("当前状态不可归还：" + record.getStatus());
        }

        // 验证验收结果
        if (returnResult == null || returnResult.isEmpty()) {
            throw new RuntimeException("验收结果不能为空");
        }
        if (!returnResult.equals("完好") && !returnResult.equals("损坏") && !returnResult.equals("缺件")) {
            throw new RuntimeException("验收结果只能是：完好、损坏、缺件");
        }

        // 如果是损坏或缺件，必须有说明
        if (("损坏".equals(returnResult) || "缺件".equals(returnResult)) && (damageDescription == null || damageDescription.isEmpty())) {
            throw new RuntimeException("损坏或缺件时必须填写说明");
        }

        String verifierName = userRepository.findById(verifierId).map(UserEntity::getRealName).orElse("验收人");
        String borrowerName = userRepository.findById(record.getBorrowerId()).map(UserEntity::getRealName).orElse("申请人");
        EquipmentEntity equipment = equipmentRepository.findById(record.getEquipmentId()).orElse(null);
        String equipInfo = equipment != null ? equipment.getName() + "（" + equipment.getAssetNo() + "）" : "设备";

        record.setStatus("已归还");
        record.setActualReturnDate(LocalDate.now());
        record.setUpdateTime(LocalDate.now());

        // 设置验收信息
        record.setReturnResult(returnResult);
        record.setReturnLocation(returnLocation);
        record.setAccessoriesInfo(accessoriesInfo);
        record.setDamageDescription(damageDescription);
        record.setVerifierId(verifierId);
        record.setReturnConfirmTime(LocalDate.now());

        // 根据验收结果更新设备状态
        EquipmentEntity eq = equipmentRepository.findById(record.getEquipmentId()).orElse(null);
        if (eq != null) {
            if ("完好".equals(returnResult)) {
                eq.setStatus("在库-可用");
            } else if ("损坏".equals(returnResult)) {
                eq.setStatus("在库-待维修");
            } else if ("缺件".equals(returnResult)) {
                eq.setStatus("配件不全");
            }
            equipmentRepository.save(eq);
        }

        borrowRecordRepository.save(record);

        // 记录操作日志
        operationLogService.logOperation(verifierId, verifierName, "设备借还", "归还",
                "归还验收：" + equipInfo + "，验收结果：" + returnResult +
                        (damageDescription != null ? "，说明：" + damageDescription : ""), null);

        // 通知借用人
        String returnTitle = "完好".equals(returnResult) ? "设备归还完成" : "设备归还异常";
        String returnContent = "您归还的设备 " + equipInfo + " 验收结果为：" + returnResult;
        if ("损坏".equals(returnResult)) {
            returnContent += "，设备将进入维修流程";
        } else if ("缺件".equals(returnResult)) {
            returnContent += "，请补充缺失配件";
        }
        notificationService.createNotification(record.getBorrowerId(), borrowerName, "完成", returnTitle, returnContent);

        // 如果损坏或缺件，通知管理员
        if (!"完好".equals(returnResult)) {
            sendNotificationToAdmins("完成", "设备归还异常",
                    equipInfo + " 验收结果为：" + returnResult + "，请及时处理");
        }

        return toDTO(record);
    }

    /**
     * 提交归还申请
     */
    @Transactional
    public EquipmentBorrowRecordDTO applyReturn(Long recordId, String expectedReturnTime, String expectedReturnLocation) {
        EquipmentBorrowRecordEntity record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("记录不存在"));
        if (!"已借出".equals(record.getStatus())) {
            throw new RuntimeException("当前状态不可申请归还：" + record.getStatus());
        }

        String borrowerName = userRepository.findById(record.getBorrowerId()).map(UserEntity::getRealName).orElse("申请人");
        EquipmentEntity equipment = equipmentRepository.findById(record.getEquipmentId()).orElse(null);
        String equipInfo = equipment != null ? equipment.getName() + "（" + equipment.getAssetNo() + "）" : "设备";

        record.setExpectedReturnTime(expectedReturnTime);
        record.setExpectedReturnLocation(expectedReturnLocation);
        record.setReturnApplyTime(LocalDate.now());
        record.setStatus("待验收");
        record.setUpdateTime(LocalDate.now());

        borrowRecordRepository.save(record);

        // 记录操作日志
        operationLogService.logOperation(record.getBorrowerId(), borrowerName, "设备借还", "归还",
                "提交归还申请：" + equipInfo + "，预计归还时间：" + expectedReturnTime + "，地点：" + expectedReturnLocation, null);

        // 通知管理员
        sendNotificationToAdmins("借还申请", "设备归还申请待验收",
                borrowerName + " 申请归还设备 " + equipInfo + "，请及时安排验收");

        return toDTO(record);
    }

    @Transactional
    public EquipmentBorrowRecordDTO applyRenewal(Long recordId, LocalDate newReturnDate, String remark) {
        EquipmentBorrowRecordEntity record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("记录不存在"));
        if (!"已借出".equals(record.getStatus())) {
            throw new RuntimeException("当前状态不可续借：" + record.getStatus());
        }
        if (record.getRenewalStatus() != null && "续借待审批".equals(record.getRenewalStatus())) {
            throw new RuntimeException("已有待审批的续借申请");
        }

        String borrowerName = userRepository.findById(record.getBorrowerId()).map(UserEntity::getRealName).orElse("申请人");
        EquipmentEntity equipment = equipmentRepository.findById(record.getEquipmentId()).orElse(null);
        String equipInfo = equipment != null ? equipment.getName() + "（" + equipment.getAssetNo() + "）" : "设备";

        // 设置续借申请信息，不直接改归还日期
        record.setRenewalStatus("续借待审批");
        record.setRenewalNewReturnDate(newReturnDate);
        record.setRenewalRemark(remark);
        record.setRenewalApplyTime(LocalDate.now());
        record.setUpdateTime(LocalDate.now());

        borrowRecordRepository.save(record);

        // 记录操作日志
        operationLogService.logOperation(record.getBorrowerId(), borrowerName, "设备借还", "续借",
                "提交续借申请：" + equipInfo + "，新归还日期：" + newReturnDate + "，理由：" + remark, null);

        // 通知管理员
        sendNotificationToAdmins("借还申请", "续借申请待审批",
                borrowerName + " 申请续借设备 " + equipInfo + "，新归还日期：" + newReturnDate + "，请及时审批");

        return toDTO(record);
    }

    /**
     * 审批续借申请
     */
    @Transactional
    public EquipmentBorrowRecordDTO approveRenewal(Long recordId, Long approverId, boolean approved, String approveRemark) {
        EquipmentBorrowRecordEntity record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("记录不存在"));
        if (!"续借待审批".equals(record.getRenewalStatus())) {
            throw new RuntimeException("当前没有待审批的续借申请");
        }

        String approverName = userRepository.findById(approverId).map(UserEntity::getRealName).orElse("审批人");
        String borrowerName = userRepository.findById(record.getBorrowerId()).map(UserEntity::getRealName).orElse("申请人");
        EquipmentEntity equipment = equipmentRepository.findById(record.getEquipmentId()).orElse(null);
        String equipInfo = equipment != null ? equipment.getName() + "（" + equipment.getAssetNo() + "）" : "设备";

        record.setRenewalApproverId(approverId);
        record.setRenewalApproveTime(LocalDate.now());
        record.setRenewalApproveRemark(approveRemark);
        record.setUpdateTime(LocalDate.now());

        if (approved) {
            // 审批通过：更新应还日期
            record.setExpectedReturnDate(record.getRenewalNewReturnDate());
            record.setRenewalStatus("续借已批准");

            // 记录操作日志
            operationLogService.logOperation(approverId, approverName, "设备借还", "续借",
                    "续借审批通过：" + equipInfo + "，新归还日期：" + record.getRenewalNewReturnDate(), null);

            // 通知借用人
            notificationService.createNotification(record.getBorrowerId(), borrowerName, "审批",
                    "续借申请已批准",
                    "您的续借申请 " + equipInfo + " 已获批准，新归还日期：" + record.getRenewalNewReturnDate());
        } else {
            // 审批拒绝
            record.setRenewalStatus("续借已拒绝");

            // 记录操作日志
            operationLogService.logOperation(approverId, approverName, "设备借还", "续借",
                    "续借审批拒绝：" + equipInfo + "，理由：" + approveRemark, null);

            // 通知借用人
            notificationService.createNotification(record.getBorrowerId(), borrowerName, "审批",
                    "续借申请已拒绝",
                    "您的续借申请 " + equipInfo + " 已被拒绝，理由：" + approveRemark);
        }

        borrowRecordRepository.save(record);
        return toDTO(record);
    }

    /**
     * 管理员手动逾期告警
     */
    @Transactional
    public EquipmentBorrowRecordDTO markOverdueWarning(Long recordId, String warningRemark) {
        EquipmentBorrowRecordEntity record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("记录不存在"));

        String borrowerName = userRepository.findById(record.getBorrowerId()).map(UserEntity::getRealName).orElse("申请人");
        EquipmentEntity equipment = equipmentRepository.findById(record.getEquipmentId()).orElse(null);
        String equipInfo = equipment != null ? equipment.getName() + "（" + equipment.getAssetNo() + "）" : "设备";

        record.setOverdueWarning(true);
        record.setOverdueWarningTime(LocalDate.now());
        record.setOverdueWarningRemark(warningRemark);
        record.setUpdateTime(LocalDate.now());
        borrowRecordRepository.save(record);

        // 记录操作日志
        operationLogService.logOperation(null, "系统", "设备借还", "逾期告警",
                "管理员手动逾期告警：" + equipInfo + "，借用人：" + borrowerName + "，备注：" + warningRemark, null);

        // 通知借用人
        notificationService.createNotification(record.getBorrowerId(), borrowerName, "逾期提醒",
                "设备逾期告警",
                "您借用的设备 " + equipInfo + " 已逾期，请尽快归还。管理员备注：" + warningRemark);

        return toDTO(record);
    }

    /**
     * 记录逾期提醒（自动或手动触发）
     */
    @Transactional
    public void recordReminder(Long recordId, String reminderType) {
        EquipmentBorrowRecordEntity record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("记录不存在"));

        String borrowerName = userRepository.findById(record.getBorrowerId()).map(UserEntity::getRealName).orElse("申请人");
        EquipmentEntity equipment = equipmentRepository.findById(record.getEquipmentId()).orElse(null);
        String equipInfo = equipment != null ? equipment.getName() + "（" + equipment.getAssetNo() + "）" : "设备";

        switch (reminderType) {
            case "1day":
                record.setRemind1day(true);
                notificationService.createNotification(record.getBorrowerId(), borrowerName, "逾期提醒",
                        "归还提醒",
                        "您借用的设备 " + equipInfo + " 将于明天到期，请及时归还");
                break;
            case "today":
                record.setRemindToday(true);
                notificationService.createNotification(record.getBorrowerId(), borrowerName, "逾期提醒",
                        "到期提醒",
                        "您借用的设备 " + equipInfo + " 今天到期，请尽快归还");
                break;
            case "overdue":
                record.setRemindOverdue(true);
                notificationService.createNotification(record.getBorrowerId(), borrowerName, "逾期提醒",
                        "逾期提醒",
                        "您借用的设备 " + equipInfo + " 已逾期，请立即归还");
                break;
        }
        record.setUpdateTime(LocalDate.now());
        borrowRecordRepository.save(record);
    }

    @Transactional
    public EquipmentBorrowRecordDTO updateReturnDate(Long recordId, LocalDate newReturnDate) {
        EquipmentBorrowRecordEntity record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("记录不存在"));
        record.setExpectedReturnDate(newReturnDate);
        record.setUpdateTime(LocalDate.now());
        borrowRecordRepository.save(record);
        return toDTO(record);
    }

    private String generateRecordNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        long count = borrowRecordRepository.count() + 1;
        return "BR" + dateStr + String.format("%03d", count);
    }

    private EquipmentBorrowRecordDTO toDTO(EquipmentBorrowRecordEntity entity) {
        EquipmentBorrowRecordDTO dto = new EquipmentBorrowRecordDTO();
        dto.setId(entity.getId());
        dto.setRecordNo(entity.getRecordNo());
        dto.setEquipmentId(entity.getEquipmentId());
        dto.setBorrowerId(entity.getBorrowerId());
        dto.setPurpose(entity.getPurpose());
        dto.setCourseId(entity.getCourseId());
        dto.setExpectedReturnDate(entity.getExpectedReturnDate());
        dto.setActualReturnDate(entity.getActualReturnDate());
        dto.setStatus(entity.getStatus());
        dto.setMentorId(entity.getMentorId());
        dto.setMentorApproveRemark(entity.getMentorApproveRemark());
        dto.setMentorApproveTime(entity.getMentorApproveTime());
        dto.setApproverId(entity.getApproverId());
        dto.setApproveRemark(entity.getApproveRemark());
        dto.setApproveTime(entity.getApproveTime());
        dto.setRemark(entity.getRemark());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());

        // 填充关联信息
        equipmentRepository.findById(entity.getEquipmentId()).ifPresent(eq -> {
            dto.setEquipmentName(eq.getName());
            dto.setEquipmentAssetNo(eq.getAssetNo());
        });
        userRepository.findById(entity.getBorrowerId()).ifPresent(u -> dto.setBorrowerName(u.getRealName()));
        if (entity.getMentorId() != null) {
            userRepository.findById(entity.getMentorId()).ifPresent(u -> dto.setMentorName(u.getRealName()));
        }
        if (entity.getApproverId() != null) {
            userRepository.findById(entity.getApproverId()).ifPresent(u -> dto.setApproverName(u.getRealName()));
        }

        // 计算逾期
        if ("已借出".equals(entity.getStatus())) {
            LocalDate today = LocalDate.now();
            if (entity.getExpectedReturnDate().isBefore(today)) {
                dto.setOverdue(true);
                dto.setOverdueDays((int) ChronoUnit.DAYS.between(entity.getExpectedReturnDate(), today));
            }
        }

        // 归还验收信息
        dto.setReturnLocation(entity.getReturnLocation());
        dto.setReturnResult(entity.getReturnResult());
        dto.setAccessoriesInfo(entity.getAccessoriesInfo());
        dto.setDamageDescription(entity.getDamageDescription());
        dto.setVerifierId(entity.getVerifierId());
        if (entity.getVerifierId() != null) {
            userRepository.findById(entity.getVerifierId()).ifPresent(u -> dto.setVerifierName(u.getRealName()));
        }

        // 借用申请信息
        dto.setPhone(entity.getPhone());
        dto.setUseLocation(entity.getUseLocation());

        // 领取确认信息
        dto.setPickupPersonId(entity.getPickupPersonId());
        dto.setPickupTime(entity.getPickupTime());
        dto.setPickupEquipmentStatus(entity.getPickupEquipmentStatus());
        dto.setPickupRemark(entity.getPickupRemark());
        if (entity.getPickupPersonId() != null) {
            userRepository.findById(entity.getPickupPersonId()).ifPresent(u -> dto.setPickupPersonName(u.getRealName()));
        }

        // 归还申请信息
        dto.setExpectedReturnTime(entity.getExpectedReturnTime());
        dto.setExpectedReturnLocation(entity.getExpectedReturnLocation());
        dto.setReturnApplyTime(entity.getReturnApplyTime());
        dto.setActualReturnTime(entity.getActualReturnTime());
        dto.setReturnConfirmTime(entity.getReturnConfirmTime());

        // 续借审批信息
        dto.setRenewalStatus(entity.getRenewalStatus());
        dto.setRenewalNewReturnDate(entity.getRenewalNewReturnDate());
        dto.setRenewalRemark(entity.getRenewalRemark());
        dto.setRenewalApplyTime(entity.getRenewalApplyTime());
        dto.setRenewalApproverId(entity.getRenewalApproverId());
        dto.setRenewalApproveTime(entity.getRenewalApproveTime());
        dto.setRenewalApproveRemark(entity.getRenewalApproveRemark());
        if (entity.getRenewalApproverId() != null) {
            userRepository.findById(entity.getRenewalApproverId()).ifPresent(u -> dto.setRenewalApproverName(u.getRealName()));
        }

        // 逾期告警信息
        dto.setOverdueWarning(entity.getOverdueWarning());
        dto.setOverdueWarningTime(entity.getOverdueWarningTime());
        dto.setOverdueWarningRemark(entity.getOverdueWarningRemark());

        // 逾期提醒记录
        dto.setRemind1day(entity.getRemind1day());
        dto.setRemindToday(entity.getRemindToday());
        dto.setRemindOverdue(entity.getRemindOverdue());

        return dto;
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
