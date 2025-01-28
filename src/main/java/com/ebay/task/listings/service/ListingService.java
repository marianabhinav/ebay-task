package com.ebay.task.listings.service;

import com.ebay.task.listings.dto.ListingDTO;
import com.ebay.task.listings.dto.request.ListingQueryDTO;
import com.ebay.task.listings.dto.request.ListingUpsertDTO;
import com.ebay.task.listings.dto.response.ListingResponseDTO;
import com.ebay.task.listings.repository.ListingRepositoryCustom;
import com.ebay.task.listings.repository.ListingRepositoryFetch;
import com.ebay.task.listings.repository.ListingRepositoryUpsert;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepositoryCustom listingRepositoryCustom;
    private final ListingRepositoryFetch listingRepositoryFetch;
    private final ListingRepositoryUpsert listingRepositoryUpsert;

    /**
     * Search for listings based on the given query parameters.
     *
     * @param queryDTO the query parameters
     * @return the search result
     */
    @Transactional(readOnly = true)
    public ListingResponseDTO searchListings(ListingQueryDTO queryDTO) {
        // 1) Get listing_ids + total
        Pair<List<String>, Long> pair = listingRepositoryCustom.searchListingIds(queryDTO);
        List<String> listingIds = pair.getFirst();
        long total = pair.getSecond();

        // 2) Fetch details
        List<ListingDTO> listingDTOs = listingRepositoryFetch.fetchListingsByIds(listingIds);

        // 3) Build and return
        ListingResponseDTO resp = new ListingResponseDTO();
        resp.setListings(listingDTOs);
        resp.setTotal(total);
        return resp;
    }


    /**
     * Upsert a list of listings.
     *
     * @param listingUpsertDTOs the listings to upsert
     */
    @Transactional
    public void upsertListings(List<ListingUpsertDTO> listingUpsertDTOs) {
        for (ListingUpsertDTO dto : listingUpsertDTOs) {
            listingRepositoryUpsert.upsertListing(dto);
        }
    }
}
