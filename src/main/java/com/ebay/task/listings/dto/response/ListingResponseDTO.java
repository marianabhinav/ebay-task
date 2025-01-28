package com.ebay.task.listings.dto.response;

import com.ebay.task.listings.dto.ListingDTO;
import lombok.Data;

import java.util.List;

@Data
public class ListingResponseDTO {
    private List<ListingDTO> listings;
    private long total;
}

