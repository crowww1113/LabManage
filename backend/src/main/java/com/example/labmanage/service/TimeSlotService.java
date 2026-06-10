package com.example.labmanage.service;

import com.example.labmanage.dto.TimeSlotDTO;
import com.example.labmanage.entity.TimeSlotEntity;
import com.example.labmanage.exception.NotFoundException;
import com.example.labmanage.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeSlotService {
    private final TimeSlotRepository timeSlotRepository;

    public List<TimeSlotDTO> list() {
        return timeSlotRepository.findAllByOrderBySortOrderAsc().stream().map(this::toDTO).toList();
    }

    public TimeSlotDTO getById(Long id) {
        return toDTO(timeSlotRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("节次不存在")));
    }

    @Transactional
    public TimeSlotDTO create(TimeSlotDTO dto) {
        TimeSlotEntity entity = new TimeSlotEntity();
        copy(dto, entity);
        return toDTO(timeSlotRepository.save(entity));
    }

    @Transactional
    public TimeSlotDTO update(Long id, TimeSlotDTO dto) {
        TimeSlotEntity entity = timeSlotRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("节次不存在"));
        copy(dto, entity);
        return toDTO(timeSlotRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!timeSlotRepository.existsById(id)) throw new NotFoundException("节次不存在");
        timeSlotRepository.deleteById(id);
    }

    // 初始化默认节次（直接调用即可生成标准节次）
    @Transactional
    public void initDefaultSlots() {
        if (timeSlotRepository.count() > 0) return;
        List<TimeSlotEntity> slots = List.of(
                createSlot("1-2节", "08:00", "09:40", 1),
                createSlot("3-4节", "10:00", "11:40", 2),
                createSlot("5-6节", "14:00", "15:40", 3),
                createSlot("7-8节", "16:00", "17:40", 4),
                createSlot("9-10节", "19:00", "20:40", 5)
        );
        timeSlotRepository.saveAll(slots);
    }

    private TimeSlotEntity createSlot(String name, String start, String end, Integer sort) {
        TimeSlotEntity e = new TimeSlotEntity();
        e.setSlotName(name);
        e.setStartTime(java.time.LocalTime.parse(start));
        e.setEndTime(java.time.LocalTime.parse(end));
        e.setSortOrder(sort);
        return e;
    }

    private void copy(TimeSlotDTO dto, TimeSlotEntity entity) {
        entity.setSlotName(dto.getSlotName());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setSortOrder(dto.getSortOrder());
    }

    private TimeSlotDTO toDTO(TimeSlotEntity entity) {
        TimeSlotDTO dto = new TimeSlotDTO();
        dto.setId(entity.getId());
        dto.setSlotName(entity.getSlotName());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setSortOrder(entity.getSortOrder());
        return dto;
    }
}