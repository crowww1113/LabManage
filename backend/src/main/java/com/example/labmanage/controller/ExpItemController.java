package com.example.labmanage.controller;

import com.example.labmanage.entity.ExpItem;
import com.example.labmanage.service.ExpItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/exp-item")
public class ExpItemController {

    @Autowired
    private ExpItemService expItemService;

    @GetMapping
    public List<ExpItem> list() {
        return expItemService.listAll();
    }

    @PostMapping
    public ExpItem save(@RequestBody ExpItem expItem) {
        return expItemService.save(expItem);
    }

    @PutMapping("/{id}")
    public ExpItem update(@PathVariable Integer id, @RequestBody ExpItem expItem) {
        return expItemService.update(id, expItem);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        expItemService.delete(id);
    }
}