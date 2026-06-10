package com.example.labmanage.service;

import com.example.labmanage.entity.TrainingPlan;
import com.example.labmanage.repository.TrainingPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainingPlanService {

    @Autowired
    private TrainingPlanRepository trainingPlanRepository;

    public List<TrainingPlan> listAll() {
        return trainingPlanRepository.findAll();
    }

    public TrainingPlan save(TrainingPlan trainingPlan) {
        return trainingPlanRepository.save(trainingPlan);
    }

    public Optional<TrainingPlan> getById(Integer id) {
        return trainingPlanRepository.findById(id);
    }

    public void delete(Integer id) {
        trainingPlanRepository.deleteById(id);
    }

    public TrainingPlan update(Integer id, TrainingPlan trainingPlan) {
        return trainingPlanRepository.findById(id).map(existingPlan -> {
            existingPlan.setCourseId(trainingPlan.getCourseId());
            existingPlan.setOrganizationMode(trainingPlan.getOrganizationMode());
            existingPlan.setTrainingPlace(trainingPlan.getTrainingPlace());
            existingPlan.setTrainingObjective(trainingPlan.getTrainingObjective());
            existingPlan.setTrainingContent(trainingPlan.getTrainingContent());
            existingPlan.setTrainingMethod(trainingPlan.getTrainingMethod());
            existingPlan.setAssessmentMethod(trainingPlan.getAssessmentMethod());
            existingPlan.setQualityMeasures(trainingPlan.getQualityMeasures());
            existingPlan.setExpCenterOpinion(trainingPlan.getExpCenterOpinion());
            existingPlan.setDeptOpinion(trainingPlan.getDeptOpinion());
            return trainingPlanRepository.save(existingPlan);
        }).orElseThrow(() -> new RuntimeException("Plan not found with id: " + id));
    }
}