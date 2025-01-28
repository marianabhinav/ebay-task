package com.ebay.task.listings.dto.request;

import com.ebay.task.listings.dto.EntityDTO;
import com.ebay.task.listings.dto.PropertyDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ListingUpsertDTO {
    @NotBlank
    @JsonProperty("listing_id")
    private String listingId;
    @JsonProperty("scan_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scanDate;
    @JsonProperty("is_active")
    private Boolean isActive;
    @JsonProperty("image_hashes")
    private List<String> imageHashes;

    private List<PropertyDTO> properties;
    private List<EntityDTO> entities;
}