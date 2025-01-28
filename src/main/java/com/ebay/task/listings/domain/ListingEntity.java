package com.ebay.task.listings.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_listings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListingEntity {

    @Id
    @Column(name = "listing_id", nullable = false, unique = true)
    private String listingId;

    @Column(name = "scan_date", nullable = false)
    private LocalDateTime scanDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "dataset_entity_ids", columnDefinition = "integer[]")
    private Integer[] datasetEntityIds;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "image_hashes", columnDefinition = "varchar[]")
    private String[] imageHashes;
}

