package com.ebay.task.listings.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PropertyDTO {
    private String name;
    private String type;
    private Object value;
}

