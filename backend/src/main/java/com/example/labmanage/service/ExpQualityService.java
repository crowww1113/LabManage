package com.example.labmanage.service;

import com.example.labmanage.entity.ExpQuality;
import com.example.labmanage.repository.ExpQualityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExpQualityService {

    @Autowired
    private ExpQualityRepository expQualityRepository;

    public List<ExpQuality> listAll() {
        return expQualityRepository.findAll();
    }

    public ExpQuality save(ExpQuality expQuality) {
        return expQualityRepository.save(expQuality);
    }

    public Optional<ExpQuality> getById(Integer id) {
        return expQualityRepository.findById(id);
    }

    public void delete(Integer id) {
        expQualityRepository.deleteById(id);
    }
}
