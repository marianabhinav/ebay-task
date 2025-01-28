package com.ebay.task.listings.serivce;

import com.ebay.task.listings.config.PostgresTestConfig;
import com.ebay.task.listings.dto.request.ListingQueryDTO;
import com.ebay.task.listings.dto.request.ListingUpsertDTO;
import com.ebay.task.listings.dto.response.ListingResponseDTO;
import com.ebay.task.listings.repository.ListingRepositoryCustom;
import com.ebay.task.listings.repository.ListingRepositoryFetch;
import com.ebay.task.listings.repository.ListingRepositoryUpsert;
import com.ebay.task.listings.service.ListingService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
        PostgresTestConfig.class,
        ListingService.class,
        ListingRepositoryCustom.class,
        ListingRepositoryFetch.class,
        ListingRepositoryUpsert.class
})
@ActiveProfiles("test")
@Transactional
class ListingServiceTest {

    @Autowired
    private ListingService listingService;

    @Autowired
    private EntityManager em;

    @BeforeEach
    void setup() {
        // Insert some initial listings (optional)
    }

    @Test
    void testSaveOrUpdateListingsAndSearch() {
        ListingUpsertDTO newListing = ListingUpsertDTO.builder()
                .listingId("SERVICE-101")
                .scanDate(LocalDateTime.now())
                .isActive(true)
                .imageHashes(List.of("hashA"))
                .build();
        listingService.upsertListings(List.of(newListing));

        ListingQueryDTO query = new ListingQueryDTO();
        query.setListingId("SERVICE-101");
        ListingResponseDTO response = listingService.searchListings(query);

        assertThat(response.getListings()).hasSize(1);
        assertThat(response.getListings().get(0).getListingId()).isEqualTo("SERVICE-101");
    }
}
