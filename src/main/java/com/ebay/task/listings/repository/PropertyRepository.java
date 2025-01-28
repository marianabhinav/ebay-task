package com.ebay.task.listings.repository;


import com.ebay.task.listings.domain.PropertyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PropertyRepository extends JpaRepository<PropertyEntity, Integer> {
    Optional<PropertyEntity> findByName(String name);
}

