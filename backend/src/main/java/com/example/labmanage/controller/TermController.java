package com.example.labmanage.controller;

import com.example.labmanage.dto.TermCalendarDTO;
import com.example.labmanage.dto.TermDTO;
import com.example.labmanage.service.TermService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
public class TermController {
    private final TermService termService;

    @GetMapping
    public List<TermDTO> list() {
        return termService.list();
    }

    @GetMapping("/{id}")
    public TermDTO get(@PathVariable Long id) {
        return termService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN')")
    public TermDTO create(@Valid @RequestBody TermDTO dto) {
        return termService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN')")
    public TermDTO update(@PathVariable Long id, @Valid @RequestBody TermDTO dto) {
        return termService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN')")
    public void delete(@PathVariable Long id) {
        termService.delete(id);
    }

    @GetMapping("/{id}/calendar")
    public List<TermCalendarDTO> getCalendar(@PathVariable Long id) {
        return termService.getCalendar(id);
    }

    @GetMapping("/{id}/week")
    public Integer getWeek(@PathVariable Long id, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return termService.getWeekByDate(id, date);
    }

    @GetMapping("/{id}/date")
    public List<LocalDate> getDate(@PathVariable Long id, @RequestParam Integer week) {
        return termService.getDateByWeek(id, week);
    }
}