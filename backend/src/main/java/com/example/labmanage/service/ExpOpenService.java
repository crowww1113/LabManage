package com.example.labmanage.service;

import com.example.labmanage.entity.ExpOpen;
import com.example.labmanage.repository.ExpOpenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExpOpenService {

    @Autowired
    private ExpOpenRepository expOpenRepository;

    public List<ExpOpen> listAll() {
        return expOpenRepository.findAll();
    }

    public ExpOpen save(ExpOpen expOpen) {
        return expOpenRepository.save(expOpen);
    }

    public Optional<ExpOpen> getById(Integer id) {
        return expOpenRepository.findById(id);
    }

    public void delete(Integer id) {
        expOpenRepository.deleteById(id);
    }
}