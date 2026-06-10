package com.example.labmanage.controller;

import com.example.labmanage.service.OrgService;
import com.example.labmanage.dto.OrgRequest;
import com.example.labmanage.dto.OrgResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orgs")
@RequiredArgsConstructor
public class OrgController {

    // 已经通过 @RequiredArgsConstructor 自动注入
    private final OrgService orgService;

    @GetMapping
    public List<OrgResponse> list() {
        return orgService.list();
    }

    @GetMapping("/{id}")
    public OrgResponse detail(@PathVariable Long id) {
        return orgService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LAB_ADMIN')")
    public OrgResponse create(@Valid @RequestBody OrgRequest req) {
        return orgService.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'LAB_ADMIN')")
    public OrgResponse update(@PathVariable Long id, @Valid @RequestBody OrgRequest req) {
        return orgService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void delete(@PathVariable Long id) {
        orgService.delete(id);
    }
}