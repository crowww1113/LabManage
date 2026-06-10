package com.example.labmanage.repository;

import com.example.labmanage.entity.EquipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<EquipmentEntity, Long> {

    Optional<EquipmentEntity> findByAssetNo(String assetNo);

    boolean existsByAssetNo(String assetNo);

    List<EquipmentEntity> findByNameContaining(String keyword);

    List<EquipmentEntity> findByCategoryId(Long categoryId);

    List<EquipmentEntity> findByStatus(String status);

    List<EquipmentEntity> findByLocationId(Long locationId);

    @Query("SELECT e FROM EquipmentEntity e WHERE " +
           "(:id IS NULL OR e.id = :id) AND " +
           "(:assetNo IS NULL OR e.assetNo LIKE %:assetNo%) AND " +
           "(:name IS NULL OR e.name LIKE %:name%) AND " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:categoryId IS NULL OR e.categoryId = :categoryId) AND " +
           "(:locationId IS NULL OR e.locationId = :locationId) AND " +
           "(:keyword IS NULL OR e.name LIKE %:keyword% OR e.assetNo LIKE %:keyword%)")
    List<EquipmentEntity> findByConditions(
            @Param("id") Long id,
            @Param("assetNo") String assetNo,
            @Param("name") String name,
            @Param("status") String status,
            @Param("categoryId") Long categoryId,
            @Param("locationId") Long locationId,
            @Param("keyword") String keyword
    );

    long countByStatus(String status);

    long countByCategoryId(Long categoryId);
}
