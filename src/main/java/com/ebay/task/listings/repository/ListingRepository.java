package com.ebay.task.listings.repository;

import com.ebay.task.listings.domain.ListingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ListingRepository extends JpaRepository<ListingEntity, String>, JpaSpecificationExecutor<ListingEntity> {
}

