package com.ebay.task.listings.controller;

import com.ebay.task.listings.dto.request.ListingQueryDTO;
import com.ebay.task.listings.dto.request.ListingQueryRequest;
import com.ebay.task.listings.dto.request.ListingUpsertRequest;
import com.ebay.task.listings.dto.response.ListingResponseDTO;
import com.ebay.task.listings.response.ResponseHandler;
import com.ebay.task.listings.service.ListingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/listings")
@RequiredArgsConstructor
@Tag(name = "Listing Controller", description = "Operations for listings")
public class ListingController {

    private final ListingService listingService;

    @PostMapping(path = "/query",
            consumes = {"application/json", "application/xml"},
            produces = {"application/json", "application/xml"})
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Query successful.",
                    content = @Content(schema = @Schema(implementation = ListingResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid datasetEntities JSON."),
            @ApiResponse(responseCode = "503", description = "Cannot query listings.")
    })
    @Operation(summary = "Retrieve listings with optional filters")
    public ResponseEntity<Object> getListings(
            @Valid @RequestParam(required = false) Integer page,
            @Valid @RequestParam(required = false) String listingId,
            @Valid @RequestParam(required = false) String scanDateFrom,
            @Valid @RequestParam(required = false) String scanDateTo,
            @Valid @RequestParam(required = false) Boolean isActive,
            @Valid @RequestParam(required = false) List<String> imageHashes,
            @Valid @RequestBody(required = false) ListingQueryRequest listingQueryRequest
    ) {
        ListingQueryDTO queryDTO = new ListingQueryDTO();
        queryDTO.setPage(page);
        queryDTO.setListingId(listingId);
        queryDTO.setScanDateFrom(scanDateFrom != null ? LocalDateTime.parse(scanDateFrom) : null);
        queryDTO.setScanDateTo(scanDateTo != null ? LocalDateTime.parse(scanDateTo) : null);
        queryDTO.setIsActive(isActive);
        queryDTO.setImageHashes(imageHashes);
        if (listingQueryRequest != null && listingQueryRequest.getDatasetEntities() != null) {
            queryDTO.setDatasetEntities(listingQueryRequest.getDatasetEntities());
        }
        if (listingQueryRequest != null && listingQueryRequest.getPropertyFilters() != null) {
            queryDTO.setPropertyFilters(listingQueryRequest.getPropertyFilters());
        }

        ListingResponseDTO listingResponseDTO = listingService.searchListings(queryDTO);
        return ResponseHandler.generateResponse("Query Success.", HttpStatus.OK, listingResponseDTO);
    }


    @PostMapping(path = "/upsert",
            consumes = {"application/json", "application/xml"},
            produces = {"application/json", "application/xml"})
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upsert operation successful."),
            @ApiResponse(responseCode = "400", description = "Invalid entity data JSON.")
    })
    @Operation(summary = "Update or Insert listings in the database")
    public ResponseEntity<Object> upsertListings(@Valid @RequestBody ListingUpsertRequest request) {
        listingService.upsertListings(request.getListings());
        return ResponseHandler.generateResponse("Upsert Success.", HttpStatus.OK);
    }
}

