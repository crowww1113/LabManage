package com.example.labmanage.controller;

import com.example.labmanage.entity.TrainingPlan;
import com.example.labmanage.service.TrainingPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/training-plan")
public class TrainingPlanController {

    @Autowired
    private TrainingPlanService trainingPlanService;

    @GetMapping
    public List<TrainingPlan> list() {
        return trainingPlanService.listAll();
    }

    @PostMapping
    public TrainingPlan save(@RequestBody TrainingPlan trainingPlan) {
        return trainingPlanService.save(trainingPlan);
    }

    @PutMapping("/{id}")
    public TrainingPlan update(@PathVariable Integer id, @RequestBody TrainingPlan trainingPlan) {
        return trainingPlanService.update(id, trainingPlan);
    }

    @GetMapping("/{id}")
    public TrainingPlan getById(@PathVariable Integer id) {
        return trainingPlanService.getById(id).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        trainingPlanService.delete(id);
    }
}