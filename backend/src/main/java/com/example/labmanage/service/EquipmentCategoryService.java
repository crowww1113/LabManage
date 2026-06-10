package com.example.labmanage.service;

import com.example.labmanage.dto.EquipmentCategoryCreateRequest;
import com.example.labmanage.dto.EquipmentCategoryDTO;
import com.example.labmanage.entity.EquipmentCategoryEntity;
import com.example.labmanage.exception.BusinessException;
import com.example.labmanage.exception.NotFoundException;
import com.example.labmanage.repository.EquipmentCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentCategoryService {

    private final EquipmentCategoryRepository categoryRepository;

    public List<EquipmentCategoryDTO> list() {
        return categoryRepository.findAll().stream().map(this::toDTO).toList();
    }

    public EquipmentCategoryDTO getById(Long id) {
        return toDTO(categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("设备分类不存在")));
    }

    @Transactional
    public EquipmentCategoryDTO create(EquipmentCategoryCreateRequest req) {
        categoryRepository.findByCode(req.getCode())
                .ifPresent(c -> { throw new BusinessException(409, "分类编码已存在"); });

        EquipmentCategoryEntity entity = new EquipmentCategoryEntity();
        copyFromRequest(req, entity);
        return toDTO(categoryRepository.save(entity));
    }

    @Transactional
    public EquipmentCategoryDTO update(Long id, EquipmentCategoryCreateRequest req) {
        EquipmentCategoryEntity entity = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("设备分类不存在"));

        categoryRepository.findByCode(req.getCode())
                .filter(c -> !c.getId().equals(id))
                .ifPresent(c -> { throw new BusinessException(409, "分类编码已存在"); });

        copyFromRequest(req, entity);
        return toDTO(categoryRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("设备分类不存在");
        }
        categoryRepository.deleteById(id);
    }

    private void copyFromRequest(EquipmentCategoryCreateRequest req, EquipmentCategoryEntity entity) {
        entity.setCode(req.getCode());
        entity.setName(req.getName());
        entity.setParentId(req.getParentId());
        entity.setSortOrder(req.getSortOrder());
        entity.setStatus(req.getStatus());
        entity.setDescription(req.getDescription());
    }

    private EquipmentCategoryDTO toDTO(EquipmentCategoryEntity entity) {
        EquipmentCategoryDTO dto = new EquipmentCategoryDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setParentId(entity.getParentId());
        dto.setSortOrder(entity.getSortOrder());
        dto.setStatus(entity.getStatus());
        dto.setDescription(entity.getDescription());

        if (entity.getParentId() != null) {
            categoryRepository.findById(entity.getParentId())
                    .ifPresent(p -> dto.setParentName(p.getName()));
        }
        return dto;
    }
}
