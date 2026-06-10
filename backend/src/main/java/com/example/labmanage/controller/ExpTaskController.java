package com.example.labmanage.controller;

import com.example.labmanage.entity.ExpTask;
import com.example.labmanage.service.ExpTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/exp-task")
public class ExpTaskController {

    @Autowired
    private ExpTaskService expTaskService;

    @GetMapping
    public List<ExpTask> list() {
        return expTaskService.listAll();
    }

    @PostMapping
    public ExpTask save(@RequestBody ExpTask expTask) {
        return expTaskService.save(expTask);
    }

    @PutMapping("/{id}")
    public ExpTask update(@PathVariable Integer id, @RequestBody ExpTask expTask) {
        expTask.setId(id);
        return expTaskService.save(expTask);
    }

    @GetMapping("/{id}")
    public ExpTask getById(@PathVariable Integer id) {
        return expTaskService.getById(id).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        expTaskService.delete(id);
    }
}