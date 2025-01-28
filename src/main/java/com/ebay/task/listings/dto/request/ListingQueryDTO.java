package com.ebay.task.listings.dto.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class ListingQueryDTO {
    private Integer page;
    private String listingId;
    private LocalDateTime scanDateFrom;
    private LocalDateTime scanDateTo;
    private Boolean isActive;
    private List<String> imageHashes;

    private Map<String, Object> datasetEntities;
    private Map<Integer, Object> propertyFilters;
}

