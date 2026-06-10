package com.example.labmanage.controller;

import com.example.labmanage.entity.ExpOpen;
import com.example.labmanage.service.ExpOpenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/exp-open")
public class ExpOpenController {

    @Autowired
    private ExpOpenService expOpenService;

    @GetMapping
    public List<ExpOpen> list() {
        return expOpenService.listAll();
    }

    @PostMapping
    public ExpOpen save(@RequestBody ExpOpen expOpen) {
        return expOpenService.save(expOpen);
    }

    @PutMapping("/{id}")
    public ExpOpen update(@PathVariable Integer id, @RequestBody ExpOpen expOpen) {
        expOpen.setId(id);
        return expOpenService.save(expOpen);
    }

    @GetMapping("/{id}")
    public ExpOpen getById(@PathVariable Integer id) {
        return expOpenService.getById(id).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        expOpenService.delete(id);
    }
}