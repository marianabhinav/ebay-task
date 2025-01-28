package com.ebay.task.listings.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "test_property_values_str")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PropertyValueStrId.class)
public class PropertyValueStrEntity {

    @Id
    @Column(name = "listing_id")
    private String listingId;

    @Id
    @Column(name = "property_id")
    private Integer propertyId;

    @Column(name = "value")
    private String value;
}

