package com.ebay.task.listings.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ListingQueryRequest {
    @JsonProperty("dataset_entities")
    private Map<String, Object> datasetEntities;
    @JsonProperty("property_filters")
    private Map<Integer, Object> propertyFilters;
}
