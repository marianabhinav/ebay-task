package com.ebay.task.listings.repository;

import com.ebay.task.listings.domain.PropertyValueBoolEntity;
import com.ebay.task.listings.domain.PropertyValueBoolId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyValueBoolRepository extends JpaRepository<PropertyValueBoolEntity, PropertyValueBoolId> {
}

