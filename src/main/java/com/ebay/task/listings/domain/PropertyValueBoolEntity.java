package com.ebay.task.listings.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "test_property_values_bool")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PropertyValueBoolId.class)
public class PropertyValueBoolEntity {

    @Id
    @Column(name = "listing_id")
    private String listingId;

    @Id
    @Column(name = "property_id")
    private Integer propertyId;

    @Column(name = "value")
    private Boolean value;
}

