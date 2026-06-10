package com.example.labmanage.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.labmanage.dto.RoleRequest;
import com.example.labmanage.dto.RoleResponse;
import com.example.labmanage.service.RoleService;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public List<RoleResponse> list() {
        return roleService.list();
    }

    @GetMapping("/{id}")
    public RoleResponse detail(@PathVariable Long id) {
        return roleService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public RoleResponse create(@Valid @RequestBody RoleRequest req) {
        return roleService.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public RoleResponse update(@PathVariable Long id, @Valid @RequestBody RoleRequest req) {
        return roleService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void delete(@PathVariable Long id) {
        roleService.delete(id);
    }
}
