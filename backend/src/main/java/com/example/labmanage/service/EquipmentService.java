package com.example.labmanage.service;

import com.example.labmanage.dto.*;
import com.example.labmanage.entity.*;
import com.example.labmanage.exception.BusinessException;
import com.example.labmanage.exception.NotFoundException;
import com.example.labmanage.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentCategoryRepository categoryRepository;
    private final EquipmentLocationRepository locationRepository;
    private final UserRepository userRepository;
    private final OrgRepository orgRepository;

    public List<EquipmentDTO> list(Long id, String assetNo, String name, String status, Long categoryId, Long locationId, String keyword) {
        List<EquipmentEntity> list = equipmentRepository.findByConditions(id, assetNo, name, status, categoryId, locationId, keyword);
        return list.stream().map(this::toDTO).toList();
    }

    public EquipmentDTO getById(Long id) {
        return toDTO(equipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("设备不存在")));
    }

    @Transactional
    public EquipmentDTO create(EquipmentCreateRequest req) {
        equipmentRepository.findByAssetNo(req.getAssetNo())
                .ifPresent(e -> { throw new BusinessException(409, "资产编号已存在"); });

        EquipmentEntity entity = new EquipmentEntity();
        copyFromRequest(req, entity);
        return toDTO(equipmentRepository.save(entity));
    }

    @Transactional
    public EquipmentDTO update(Long id, EquipmentCreateRequest req) {
        EquipmentEntity entity = equipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("设备不存在"));

        equipmentRepository.findByAssetNo(req.getAssetNo())
                .filter(e -> !e.getId().equals(id))
                .ifPresent(e -> { throw new BusinessException(409, "资产编号已存在"); });

        copyFromRequest(req, entity);
        return toDTO(equipmentRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        EquipmentEntity entity = equipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("设备不存在"));
        if (!"在库-可用".equals(entity.getStatus()) && !"在库-待维修".equals(entity.getStatus())) {
            throw new BusinessException(409, "设备不在库或状态不允许删除，当前状态：" + entity.getStatus());
        }
        equipmentRepository.deleteById(id);
    }

    @Transactional
    public EquipmentDTO updateStatus(Long id, String status) {
        EquipmentEntity entity = equipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("设备不存在"));
        entity.setStatus(status);
        return toDTO(equipmentRepository.save(entity));
    }

    /**
     * 设备状态流转：借出（在库-可用 -> 借出）
     */
    @Transactional
    public EquipmentDTO borrowEquipment(Long id) {
        EquipmentEntity entity = equipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("设备不存在"));
        if (!"在库-可用".equals(entity.getStatus())) {
            throw new BusinessException(409, "只有【在库-可用】状态的设备可以借出");
        }
        entity.setStatus("借出");
        return toDTO(equipmentRepository.save(entity));
    }

    /**
     * 设备状态流转：归还（借出 -> 在库-可用）
     */
    @Transactional
    public EquipmentDTO returnEquipment(Long id) {
        EquipmentEntity entity = equipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("设备不存在"));
        if (!"借出".equals(entity.getStatus())) {
            throw new BusinessException(409, "只有【借出】状态的设备可以归还");
        }
        entity.setStatus("在库-可用");
        return toDTO(equipmentRepository.save(entity));
    }

    /**
     * 设备状态流转：标记为待维修（在库-可用 -> 在库-待维修）
     */
    @Transactional
    public EquipmentDTO markForRepair(Long id) {
        EquipmentEntity entity = equipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("设备不存在"));
        if (!"在库-可用".equals(entity.getStatus())) {
            throw new BusinessException(409, "只有【在库-可用】状态的设备可以标记为待维修");
        }
        entity.setStatus("在库-待维修");
        return toDTO(equipmentRepository.save(entity));
    }

    /**
     * 设备状态流转：送修（在库-待维修 -> 送修）
     */
    @Transactional
    public EquipmentDTO sendForRepair(Long id) {
        EquipmentEntity entity = equipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("设备不存在"));
        if (!"在库-待维修".equals(entity.getStatus())) {
            throw new BusinessException(409, "只有【在库-待维修】状态的设备可以送修");
        }
        entity.setStatus("送修");
        return toDTO(equipmentRepository.save(entity));
    }

    /**
     * 设备状态流转：维修入库（送修 -> 在库-可用）
     */
    @Transactional
    public EquipmentDTO repairComplete(Long id) {
        EquipmentEntity entity = equipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("设备不存在"));
        if (!"送修".equals(entity.getStatus())) {
            throw new BusinessException(409, "只有【送修】状态的设备可以入库");
        }
        entity.setStatus("在库-可用");
        return toDTO(equipmentRepository.save(entity));
    }

    /**
     * 设备状态流转：报废（在库-待维修 -> 报废）
     */
    @Transactional
    public EquipmentDTO scrap(Long id) {
        EquipmentEntity entity = equipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("设备不存在"));
        if (!"在库-待维修".equals(entity.getStatus())) {
            throw new BusinessException(409, "只有【在库-待维修】状态的设备可以报废");
        }
        entity.setStatus("报废");
        return toDTO(equipmentRepository.save(entity));
    }

    /**
     * 设备状态流转：找回（丢失 -> 在库-可用）
     */
    @Transactional
    public EquipmentDTO recoverLost(Long id) {
        EquipmentEntity entity = equipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("设备不存在"));
        if (!"丢失".equals(entity.getStatus())) {
            throw new BusinessException(409, "只有【丢失】状态的设备可以找回");
        }
        entity.setStatus("在库-可用");
        return toDTO(equipmentRepository.save(entity));
    }

    /**
     * 设备状态流转：标记为丢失（在库-可用/在库-待维修 -> 丢失）
     */
    @Transactional
    public EquipmentDTO markAsLost(Long id) {
        EquipmentEntity entity = equipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("设备不存在"));
        if (!"在库-可用".equals(entity.getStatus()) && !"在库-待维修".equals(entity.getStatus())) {
            throw new BusinessException(409, "只有【在库-可用】或【在库-待维修】状态的设备可以标记为丢失");
        }
        entity.setStatus("丢失");
        return toDTO(equipmentRepository.save(entity));
    }

    /**
     * 获取设备可执行的状态操作
     */
    public List<String> getAvailableActions(Long id) {
        EquipmentEntity entity = equipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("设备不存在"));
        List<String> actions = new ArrayList<>();
        switch (entity.getStatus()) {
            case "在库-可用":
                actions.add("借出");
                actions.add("标记待维修");
                actions.add("标记丢失");
                break;
            case "在库-待维修":
                actions.add("送修");
                actions.add("报废");
                actions.add("标记丢失");
                break;
            case "在库-已预约":
                break;
            case "借出":
                actions.add("归还");
                break;
            case "送修":
                actions.add("维修入库");
                break;
            case "报废":
                break;
            case "丢失":
                actions.add("找回");
                break;
        }
        return actions;
    }

    public EquipmentStatisticsDTO getStatistics() {
        EquipmentStatisticsDTO stats = new EquipmentStatisticsDTO();
        stats.setTotalEquipment(equipmentRepository.count());
        stats.setAvailable(equipmentRepository.countByStatus("在库-可用"));
        stats.setPendingRepair(equipmentRepository.countByStatus("在库-待维修"));
        stats.setReserved(equipmentRepository.countByStatus("在库-已预约"));
        stats.setBorrowed(equipmentRepository.countByStatus("借出"));
        stats.setRepairing(equipmentRepository.countByStatus("送修"));
        stats.setScrapped(equipmentRepository.countByStatus("报废"));
        stats.setLost(equipmentRepository.countByStatus("丢失"));

        List<EquipmentStatisticsDTO.CategoryCount> categoryCounts = new ArrayList<>();
        List<EquipmentCategoryEntity> categories = categoryRepository.findAll();
        for (EquipmentCategoryEntity cat : categories) {
            long count = equipmentRepository.countByCategoryId(cat.getId());
            if (count > 0) {
                EquipmentStatisticsDTO.CategoryCount cc = new EquipmentStatisticsDTO.CategoryCount();
                cc.setCategoryName(cat.getName());
                cc.setCount(count);
                categoryCounts.add(cc);
            }
        }
        stats.setCategoryCounts(categoryCounts);
        return stats;
    }

    private void copyFromRequest(EquipmentCreateRequest req, EquipmentEntity entity) {
        entity.setAssetNo(req.getAssetNo());
        entity.setName(req.getName());
        entity.setModel(req.getModel());
        entity.setCategoryId(req.getCategoryId());
        entity.setUnit(req.getUnit());
        entity.setBrand(req.getBrand());
        entity.setSerialNo(req.getSerialNo());
        entity.setSpec(req.getSpec());
        entity.setPrice(req.getPrice());
        entity.setFundSource(req.getFundSource());
        entity.setPurchaseDate(req.getPurchaseDate());
        entity.setUseYears(req.getUseYears());
        entity.setSupplier(req.getSupplier());
        entity.setWarrantyMonths(req.getWarrantyMonths());
        entity.setLocationId(req.getLocationId());
        entity.setResponsibleId(req.getResponsibleId());
        entity.setDeptId(req.getDeptId());
        entity.setIsImportant(req.getIsImportant());
        entity.setTags(req.getTags());
        entity.setRemark(req.getRemark());
        entity.setAttachments(req.getAttachments());
        entity.setCalibrationPeriodMonths(req.getCalibrationPeriodMonths());
        entity.setLastCalibrationDate(req.getLastCalibrationDate());
        entity.setNextCalibrationDate(req.getNextCalibrationDate());
    }

    private EquipmentDTO toDTO(EquipmentEntity entity) {
        EquipmentDTO dto = new EquipmentDTO();
        dto.setId(entity.getId());
        dto.setAssetNo(entity.getAssetNo());
        dto.setName(entity.getName());
        dto.setModel(entity.getModel());
        dto.setCategoryId(entity.getCategoryId());
        dto.setUnit(entity.getUnit());
        dto.setBrand(entity.getBrand());
        dto.setSerialNo(entity.getSerialNo());
        dto.setSpec(entity.getSpec());
        dto.setPrice(entity.getPrice());
        dto.setFundSource(entity.getFundSource());
        dto.setPurchaseDate(entity.getPurchaseDate());
        dto.setUseYears(entity.getUseYears());
        dto.setSupplier(entity.getSupplier());
        dto.setWarrantyMonths(entity.getWarrantyMonths());
        dto.setLocationId(entity.getLocationId());
        dto.setResponsibleId(entity.getResponsibleId());
        dto.setStatus(entity.getStatus());
        dto.setDeptId(entity.getDeptId());
        dto.setIsImportant(entity.getIsImportant());
        dto.setTags(entity.getTags());
        dto.setRemark(entity.getRemark());
        dto.setAttachments(entity.getAttachments());
        dto.setCalibrationPeriodMonths(entity.getCalibrationPeriodMonths());
        dto.setLastCalibrationDate(entity.getLastCalibrationDate());
        dto.setNextCalibrationDate(entity.getNextCalibrationDate());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getCategoryId() != null) {
            categoryRepository.findById(entity.getCategoryId())
                    .ifPresent(c -> dto.setCategoryName(c.getName()));
        }
        if (entity.getLocationId() != null) {
            locationRepository.findById(entity.getLocationId())
                    .ifPresent(l -> dto.setLocationName(l.getName()));
        }
        if (entity.getResponsibleId() != null) {
            userRepository.findById(entity.getResponsibleId())
                    .ifPresent(u -> dto.setResponsibleName(u.getRealName()));
        }
        if (entity.getDeptId() != null) {
            orgRepository.findById(entity.getDeptId())
                    .ifPresent(o -> dto.setDeptName(o.getName()));
        }
        return dto;
    }
}
