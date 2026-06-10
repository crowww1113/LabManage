package com.example.labmanage.controller;

import com.example.labmanage.dto.TimeSlotDTO;
import com.example.labmanage.service.TimeSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/time-slots")
@RequiredArgsConstructor
public class TimeSlotController {
    private final TimeSlotService timeSlotService;

    @GetMapping
    public List<TimeSlotDTO> list() {
        return timeSlotService.list();
    }

    @GetMapping("/{id}")
    public TimeSlotDTO get(@PathVariable Long id) {
        return timeSlotService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN')")
    public TimeSlotDTO create(@Valid @RequestBody TimeSlotDTO dto) {
        return timeSlotService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN')")
    public TimeSlotDTO update(@PathVariable Long id, @Valid @RequestBody TimeSlotDTO dto) {
        return timeSlotService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN')")
    public void delete(@PathVariable Long id) {
        timeSlotService.delete(id);
    }

    @PostMapping("/init")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN')")
    public void init() {
        timeSlotService.initDefaultSlots();
    }
}