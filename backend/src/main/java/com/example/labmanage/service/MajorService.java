package com.example.labmanage.service;

import com.example.labmanage.dto.MajorDTO;
import com.example.labmanage.entity.MajorEntity;
import com.example.labmanage.exception.NotFoundException;
import com.example.labmanage.repository.MajorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MajorService {
    private final MajorRepository majorRepository;

    public List<MajorDTO> list(Long deptId) {
        List<MajorEntity> list = deptId == null
                ? majorRepository.findAll()
                : majorRepository.findByDeptId(deptId);
        return list.stream().map(this::toDTO).toList();
    }

    public MajorDTO getById(Long id) {
        return toDTO(majorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("专业不存在")));
    }

    @Transactional
    public MajorDTO create(MajorDTO dto) {
        MajorEntity entity = new MajorEntity();
        copy(dto, entity);
        return toDTO(majorRepository.save(entity));
    }

    @Transactional
    public MajorDTO update(Long id, MajorDTO dto) {
        MajorEntity entity = majorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("专业不存在"));
        copy(dto, entity);
        return toDTO(majorRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!majorRepository.existsById(id)) throw new NotFoundException("专业不存在");
        majorRepository.deleteById(id);
    }

    private void copy(MajorDTO dto, MajorEntity entity) {
        entity.setMajorCode(dto.getMajorCode());
        entity.setMajorName(dto.getMajorName());
        entity.setDeptId(dto.getDeptId());
        entity.setStatus(dto.getStatus() == null ? "启用" : dto.getStatus());
    }

    private MajorDTO toDTO(MajorEntity entity) {
        MajorDTO dto = new MajorDTO();
        dto.setId(entity.getId());
        dto.setMajorCode(entity.getMajorCode());
        dto.setMajorName(entity.getMajorName());
        dto.setDeptId(entity.getDeptId());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}