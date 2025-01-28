package com.ebay.task.listings.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ListingDTO {
    private String listingId;
    private LocalDateTime scanDate;
    private Boolean isActive;
    private List<Integer> datasetEntityIds;
    private List<String> imageHashes;
    private List<PropertyDTO> properties;
    private List<EntityDTO> entities;
}

