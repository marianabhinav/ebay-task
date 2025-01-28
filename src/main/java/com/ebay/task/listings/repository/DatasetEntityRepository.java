package com.ebay.task.listings.repository;

import com.ebay.task.listings.domain.DatasetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DatasetEntityRepository extends JpaRepository<DatasetEntity, Integer> {
    Optional<DatasetEntity> findByName(String name);
}

