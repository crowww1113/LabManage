package com.example.labmanage.service;

import com.example.labmanage.entity.ExpItem;
import com.example.labmanage.repository.ExpItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExpItemService {

    @Autowired
    private ExpItemRepository expItemRepository;

    public List<ExpItem> listAll() {
        return expItemRepository.findAll();
    }

    public ExpItem save(ExpItem expItem) {
        return expItemRepository.save(expItem);
    }

    public Optional<ExpItem> getById(Integer id) {
        return expItemRepository.findById(id);
    }

    public void delete(Integer id) {
        expItemRepository.deleteById(id);
    }

    public ExpItem update(Integer id, ExpItem expItem) {
        return expItemRepository.findById(id).map(existingItem -> {
            existingItem.setCourseId(expItem.getCourseId());
            existingItem.setItemName(expItem.getItemName());
            existingItem.setHour(expItem.getHour());
            existingItem.setExpType(expItem.getExpType());
            existingItem.setRequirement(expItem.getRequirement());
            return expItemRepository.save(existingItem);
        }).orElseThrow(() -> new RuntimeException("Item not found with id: " + id));
    }
}