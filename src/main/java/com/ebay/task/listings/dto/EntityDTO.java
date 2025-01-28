package com.ebay.task.listings.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class EntityDTO {
    private String name;
    private Map<String, Object> data;
}

