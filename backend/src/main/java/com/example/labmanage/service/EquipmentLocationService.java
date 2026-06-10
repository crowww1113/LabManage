package com.example.labmanage.service;

import com.example.labmanage.dto.EquipmentLocationCreateRequest;
import com.example.labmanage.dto.EquipmentLocationDTO;
import com.example.labmanage.entity.BuildingEntity;
import com.example.labmanage.entity.EquipmentLocationEntity;
import com.example.labmanage.exception.BusinessException;
import com.example.labmanage.exception.NotFoundException;
import com.example.labmanage.repository.BuildingRepository;
import com.example.labmanage.repository.EquipmentLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentLocationService {

    private final EquipmentLocationRepository locationRepository;
    private final BuildingRepository buildingRepository;

    public List<EquipmentLocationDTO> list() {
        return locationRepository.findAll().stream().map(this::toDTO).toList();
    }

    public EquipmentLocationDTO getById(Long id) {
        return toDTO(locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("设备位置不存在")));
    }

    @Transactional
    public EquipmentLocationDTO create(EquipmentLocationCreateRequest req) {
        locationRepository.findByCode(req.getCode())
                .ifPresent(l -> { throw new BusinessException(409, "位置编码已存在"); });

        EquipmentLocationEntity entity = new EquipmentLocationEntity();
        copyFromRequest(req, entity);
        return toDTO(locationRepository.save(entity));
    }

    @Transactional
    public EquipmentLocationDTO update(Long id, EquipmentLocationCreateRequest req) {
        EquipmentLocationEntity entity = locationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("设备位置不存在"));

        locationRepository.findByCode(req.getCode())
                .filter(l -> !l.getId().equals(id))
                .ifPresent(l -> { throw new BusinessException(409, "位置编码已存在"); });

        copyFromRequest(req, entity);
        return toDTO(locationRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new NotFoundException("设备位置不存在");
        }
        locationRepository.deleteById(id);
    }

    private void copyFromRequest(EquipmentLocationCreateRequest req, EquipmentLocationEntity entity) {
        entity.setCode(req.getCode());
        entity.setName(req.getName());
        entity.setBuildingId(req.getBuildingId());
        entity.setRoomNumber(req.getRoomNumber());
        entity.setFloor(req.getFloor());
        entity.setStatus(req.getStatus());
        entity.setDescription(req.getDescription());
    }

    private EquipmentLocationDTO toDTO(EquipmentLocationEntity entity) {
        EquipmentLocationDTO dto = new EquipmentLocationDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setBuildingId(entity.getBuildingId());
        dto.setRoomNumber(entity.getRoomNumber());
        dto.setFloor(entity.getFloor());
        dto.setStatus(entity.getStatus());
        dto.setDescription(entity.getDescription());

        // #region debug-point 1
        System.out.println("[DEBUG-LOCATION] 转换位置: code=" + entity.getCode() + ", buildingId=" + entity.getBuildingId());
        // #endregion

        if (entity.getBuildingId() != null) {
            buildingRepository.findById(entity.getBuildingId())
                    .ifPresent(b -> {
                        dto.setBuildingName(b.getName());
                        // #region debug-point 2
                        System.out.println("[DEBUG-LOCATION] 找到楼宇: buildingId=" + entity.getBuildingId() + ", buildingName=" + b.getName());
                        // #endregion
                    });
        } else {
            // #region debug-point 3
            System.out.println("[DEBUG-LOCATION] buildingId 为空，无法查询楼宇名称: code=" + entity.getCode());
            // #endregion
        }
        return dto;
    }
}
