package com.example.labmanage.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.labmanage.service.PermissionService;
import com.example.labmanage.dto.PermissionResponse;
import com.example.labmanage.dto.PermissionRequest;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    public List<PermissionResponse> list(@RequestParam(required = false) String keyword) {
        return permissionService.list(keyword);
    }

    @GetMapping("/{id}")
    public PermissionResponse detail(@PathVariable Long id) {
        return permissionService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public PermissionResponse create(@Valid @RequestBody PermissionRequest req) {
        return permissionService.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public PermissionResponse update(@PathVariable Long id, @Valid @RequestBody PermissionRequest req) {
        return permissionService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void delete(@PathVariable Long id) {
        permissionService.delete(id);
    }
}
