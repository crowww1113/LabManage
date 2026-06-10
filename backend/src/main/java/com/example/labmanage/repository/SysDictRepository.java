package com.example.labmanage.repository;

import com.example.labmanage.entity.SysDictEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SysDictRepository extends JpaRepository<SysDictEntity, Long> {
    List<SysDictEntity> findByDictTypeOrderBySortOrderAsc(String dictType);

    Optional<SysDictEntity> findByDictTypeAndDictKey(String dictType, String dictKey);
}
