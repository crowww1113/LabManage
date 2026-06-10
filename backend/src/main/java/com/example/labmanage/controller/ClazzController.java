package com.example.labmanage.controller;

import com.example.labmanage.dto.ClazzDTO;
import com.example.labmanage.service.ClazzService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/clazzes")
@RequiredArgsConstructor
public class ClazzController {
    private final ClazzService clazzService;

    @GetMapping
    public List<ClazzDTO> list(@RequestParam(required = false) Long majorId) {
        return clazzService.list(majorId);
    }

    @GetMapping("/{id}")
    public ClazzDTO get(@PathVariable Long id) {
        return clazzService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN')")
    public ClazzDTO create(@Valid @RequestBody ClazzDTO dto) {
        return clazzService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN')")
    public ClazzDTO update(@PathVariable Long id, @Valid @RequestBody ClazzDTO dto) {
        return clazzService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN')")
    public void delete(@PathVariable Long id) {
        clazzService.delete(id);
    }
}