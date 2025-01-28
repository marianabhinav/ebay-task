package com.ebay.task.listings.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ListingUpsertRequest {
    List<ListingUpsertDTO> listings;
}
