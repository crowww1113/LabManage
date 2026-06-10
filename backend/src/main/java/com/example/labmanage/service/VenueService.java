package com.example.labmanage.service;

import com.example.labmanage.entity.BuildingEntity;
import com.example.labmanage.entity.CampusEntity;
import com.example.labmanage.entity.RoomEntity;
import com.example.labmanage.exception.NotFoundException;
import com.example.labmanage.repository.BuildingRepository;
import com.example.labmanage.repository.CampusRepository;
import com.example.labmanage.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;
    private final CampusRepository campusRepository;

    // ==================== 校区 ====================

    public List<CampusEntity> getAllCampuses() {
        return campusRepository.findAll();
    }

    public CampusEntity getCampusById(Long id) {
        return campusRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("校区不存在"));
    }

    @Transactional
    public CampusEntity createCampus(CampusEntity entity) {
        return campusRepository.save(entity);
    }

    @Transactional
    public CampusEntity updateCampus(Long id, CampusEntity entity) {
        CampusEntity existing = getCampusById(id);
        existing.setName(entity.getName());
        existing.setAddress(entity.getAddress());
        return campusRepository.save(existing);
    }

    @Transactional
    public void deleteCampus(Long id) {
        if (!campusRepository.existsById(id)) {
            throw new NotFoundException("校区不存在");
        }
        campusRepository.deleteById(id);
    }

    // ==================== 楼栋 ====================

    public List<BuildingEntity> getAllBuildings() {
        return buildingRepository.findAllByOrderBySortOrderAsc();
    }

    public BuildingEntity getBuildingById(Long id) {
        return buildingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("楼栋不存在"));
    }

    @Transactional
    public BuildingEntity createBuilding(BuildingEntity entity) {
        if (entity.getStatus() == null) entity.setStatus("启用");
        return buildingRepository.save(entity);
    }

    @Transactional
    public BuildingEntity updateBuilding(Long id, BuildingEntity entity) {
        BuildingEntity existing = getBuildingById(id);
        existing.setName(entity.getName());
        existing.setCampus(entity.getCampus());
        existing.setSortOrder(entity.getSortOrder());
        existing.setStatus(entity.getStatus() == null ? "启用" : entity.getStatus());
        return buildingRepository.save(existing);
    }

    @Transactional
    public void deleteBuilding(Long id) {
        if (!buildingRepository.existsById(id)) {
            throw new NotFoundException("楼栋不存在");
        }
        // 级联删除该楼栋下所有房间
        List<RoomEntity> rooms = roomRepository.findByBuildingId(id);
        if (!rooms.isEmpty()) {
            roomRepository.deleteAll(rooms);
        }
        buildingRepository.deleteById(id);
    }

    // ==================== 房间 ====================

    public List<RoomEntity> getRooms(Long buildingId) {
        if (buildingId != null) {
            return roomRepository.findByBuildingIdOrderByFloorAscCodeAsc(buildingId);
        }
        return roomRepository.findAll();
    }

    public RoomEntity getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("房间不存在"));
    }

    @Transactional
    public RoomEntity createRoom(RoomEntity entity) {
        if (entity.getStatus() == null) entity.setStatus("启用");
        return roomRepository.save(entity);
    }

    @Transactional
    public RoomEntity updateRoom(Long id, RoomEntity entity) {
        RoomEntity existing = getRoomById(id);
        existing.setBuildingId(entity.getBuildingId());
        existing.setCode(entity.getCode());
        existing.setFloor(entity.getFloor());
        existing.setSeats(entity.getSeats());
        existing.setArea(entity.getArea());
        existing.setIntro(entity.getIntro());
        existing.setRoomType(entity.getRoomType());
        existing.setStatus(entity.getStatus() == null ? "启用" : entity.getStatus());
        return roomRepository.save(existing);
    }

    @Transactional
    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new NotFoundException("房间不存在");
        }
        roomRepository.deleteById(id);
    }
}
