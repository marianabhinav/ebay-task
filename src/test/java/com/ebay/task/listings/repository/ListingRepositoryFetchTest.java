package com.ebay.task.listings.repository;

import com.ebay.task.listings.config.PostgresTestConfig;
import com.ebay.task.listings.domain.ListingEntity;
import com.ebay.task.listings.dto.ListingDTO;
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
        ListingRepositoryFetch.class
})
@ActiveProfiles("test")
@Transactional
class ListingRepositoryFetchTest {

    @Autowired
    private ListingRepositoryFetch listingRepositoryFetch;

    @Autowired
    private EntityManager em;

    @BeforeEach
    void setup() {
        ListingEntity l1 = ListingEntity.builder()
                .listingId("LID-111")
                .scanDate(LocalDateTime.parse("2024-10-22T12:00:00"))
                .isActive(true)
                .datasetEntityIds(new Integer[]{1, 2})
                .imageHashes(new String[]{"H1", "H2"})
                .build();

        ListingEntity l2 = ListingEntity.builder()
                .listingId("LID-222")
                .scanDate(LocalDateTime.parse("2024-10-23T14:30:00"))
                .isActive(false)
                .datasetEntityIds(new Integer[]{3})
                .imageHashes(new String[]{"H3"})
                .build();

        em.persist(l1);
        em.persist(l2);
        em.flush();
    }

    @Test
    void testFetchListingsByIds() {
        List<ListingDTO> results = listingRepositoryFetch.fetchListingsByIds(List.of("LID-111", "LID-222"));
        assertThat(results).hasSize(2);

        ListingDTO first = results.get(0);
        assertThat(first.getListingId()).isIn("LID-111", "LID-222");
    }

    @Test
    void testFetchListingsByIdsEmptyIds() {
        List<ListingDTO> results = listingRepositoryFetch.fetchListingsByIds(List.of());
        assertThat(results).isEmpty();
    }
}

