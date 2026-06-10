package com.example.labmanage.controller;

import com.example.labmanage.entity.BuildingEntity;
import com.example.labmanage.entity.CampusEntity;
import com.example.labmanage.entity.RoomEntity;
import com.example.labmanage.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    // ==================== 校区 ====================

    @GetMapping("/api/campuses")
    public List<CampusEntity> getCampuses() {
        return venueService.getAllCampuses();
    }

    @GetMapping("/api/campuses/{id}")
    public CampusEntity getCampus(@PathVariable Long id) {
        return venueService.getCampusById(id);
    }

    @PostMapping("/api/campuses")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN')")
    public CampusEntity createCampus(@RequestBody CampusEntity entity) {
        return venueService.createCampus(entity);
    }

    @PutMapping("/api/campuses/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN')")
    public CampusEntity updateCampus(@PathVariable Long id, @RequestBody CampusEntity entity) {
        return venueService.updateCampus(id, entity);
    }

    @DeleteMapping("/api/campuses/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN')")
    public void deleteCampus(@PathVariable Long id) {
        venueService.deleteCampus(id);
    }

    // ==================== 楼栋 ====================

    @GetMapping("/api/buildings")
    public List<BuildingEntity> getBuildings() {
        return venueService.getAllBuildings();
    }

    @GetMapping("/api/buildings/{id}")
    public BuildingEntity getBuilding(@PathVariable Long id) {
        return venueService.getBuildingById(id);
    }

    @PostMapping("/api/buildings")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN')")
    public BuildingEntity createBuilding(@RequestBody BuildingEntity entity) {
        return venueService.createBuilding(entity);
    }

    @PutMapping("/api/buildings/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN')")
    public BuildingEntity updateBuilding(@PathVariable Long id, @RequestBody BuildingEntity entity) {
        return venueService.updateBuilding(id, entity);
    }

    @DeleteMapping("/api/buildings/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN')")
    public void deleteBuilding(@PathVariable Long id) {
        venueService.deleteBuilding(id);
    }

    // ==================== 房间 ====================

    @GetMapping("/api/rooms")
    public List<RoomEntity> getRooms(@RequestParam(required = false) Long buildingId) {
        return venueService.getRooms(buildingId);
    }

    @GetMapping("/api/rooms/{id}")
    public RoomEntity getRoom(@PathVariable Long id) {
        return venueService.getRoomById(id);
    }

    @PostMapping("/api/rooms")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN')")
    public RoomEntity createRoom(@RequestBody RoomEntity entity) {
        return venueService.createRoom(entity);
    }

    @PutMapping("/api/rooms/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN')")
    public RoomEntity updateRoom(@PathVariable Long id, @RequestBody RoomEntity entity) {
        return venueService.updateRoom(id, entity);
    }

    @DeleteMapping("/api/rooms/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','LAB_ADMIN')")
    public void deleteRoom(@PathVariable Long id) {
        venueService.deleteRoom(id);
    }
}
