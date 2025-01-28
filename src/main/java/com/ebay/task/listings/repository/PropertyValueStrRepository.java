package com.ebay.task.listings.repository;

import com.ebay.task.listings.domain.PropertyValueStrEntity;
import com.ebay.task.listings.domain.PropertyValueStrId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyValueStrRepository extends JpaRepository<PropertyValueStrEntity, PropertyValueStrId> {
}

