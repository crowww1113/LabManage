package com.example.labmanage.service;

import com.example.labmanage.dto.ClazzDTO;
import com.example.labmanage.entity.ClazzEntity;
import com.example.labmanage.exception.NotFoundException;
import com.example.labmanage.repository.ClazzRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClazzService {
    private final ClazzRepository clazzRepository;

    public List<ClazzDTO> list(Long majorId) {
        List<ClazzEntity> list = majorId == null
                ? clazzRepository.findAll()
                : clazzRepository.findByMajorId(majorId);
        return list.stream().map(this::toDTO).toList();
    }

    public ClazzDTO getById(Long id) {
        return toDTO(clazzRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("班级不存在")));
    }

    @Transactional
    public ClazzDTO create(ClazzDTO dto) {
        ClazzEntity entity = new ClazzEntity();
        copy(dto, entity);
        return toDTO(clazzRepository.save(entity));
    }

    @Transactional
    public ClazzDTO update(Long id, ClazzDTO dto) {
        ClazzEntity entity = clazzRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("班级不存在"));
        copy(dto, entity);
        return toDTO(clazzRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!clazzRepository.existsById(id)) throw new NotFoundException("班级不存在");
        clazzRepository.deleteById(id);
    }

    private void copy(ClazzDTO dto, ClazzEntity entity) {
        entity.setClazzCode(dto.getClazzCode());
        entity.setClazzName(dto.getClazzName());
        entity.setMajorId(dto.getMajorId());
        entity.setDeptId(dto.getDeptId());
        entity.setGrade(dto.getGrade());
        entity.setManagerId(dto.getManagerId());
        entity.setHeadTeacherId(dto.getHeadTeacherId());
        entity.setStatus(dto.getStatus() == null ? "启用" : dto.getStatus());
    }

    private ClazzDTO toDTO(ClazzEntity entity) {
        ClazzDTO dto = new ClazzDTO();
        dto.setId(entity.getId());
        dto.setClazzCode(entity.getClazzCode());
        dto.setClazzName(entity.getClazzName());
        dto.setMajorId(entity.getMajorId());
        dto.setDeptId(entity.getDeptId());
        dto.setGrade(entity.getGrade());
        dto.setManagerId(entity.getManagerId());
        dto.setHeadTeacherId(entity.getHeadTeacherId());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}