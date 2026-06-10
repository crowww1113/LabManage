package com.example.labmanage.controller;

import com.example.labmanage.dto.MajorDTO;
import com.example.labmanage.service.MajorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/majors")
@RequiredArgsConstructor
public class MajorController {
    private final MajorService majorService;

    @GetMapping
    public List<MajorDTO> list(@RequestParam(required = false) Long deptId) {
        return majorService.list(deptId);
    }

    @GetMapping("/{id}")
    public MajorDTO get(@PathVariable Long id) {
        return majorService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN')")
    public MajorDTO create(@Valid @RequestBody MajorDTO dto) {
        return majorService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN')")
    public MajorDTO update(@PathVariable Long id, @Valid @RequestBody MajorDTO dto) {
        return majorService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN')")
    public void delete(@PathVariable Long id) {
        majorService.delete(id);
    }
}