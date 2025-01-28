package com.ebay.task.listings.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class PropertyValueBoolId implements Serializable {
    private String listingId;
    private Integer propertyId;
}

