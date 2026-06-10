package com.example.labmanage.controller;

import com.example.labmanage.entity.ExpQuality;
import com.example.labmanage.service.ExpQualityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exp-quality")
public class ExpQualityController {

    @Autowired
    private ExpQualityService expQualityService;

    @GetMapping
    public List<ExpQuality> list() {
        return expQualityService.listAll();
    }

    @GetMapping("/{id}")
    public ExpQuality getById(@PathVariable Integer id) {
        return expQualityService.getById(id)
                .orElseThrow(() -> new RuntimeException("登记信息不存在，ID: " + id));
    }

    @PostMapping
    public ExpQuality save(@RequestBody ExpQuality expQuality) {
        return expQualityService.save(expQuality);
    }

    @PutMapping("/{id}")
    public ExpQuality update(@PathVariable Integer id, @RequestBody ExpQuality expQuality) {
        expQuality.setId(id);
        return expQualityService.save(expQuality);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        expQualityService.delete(id);
    }
}
