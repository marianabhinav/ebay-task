package com.ebay.task.listings.repository;

import com.ebay.task.listings.config.PostgresTestConfig;
import com.ebay.task.listings.dto.ListingDTO;
import com.ebay.task.listings.dto.request.ListingQueryDTO;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
        PostgresTestConfig.class,
        ListingRepositoryCustom.class
})
@ActiveProfiles("test")
@Transactional
class ListingRepositoryCustomTest {

    @Autowired
    private ListingRepositoryCustom listingRepositoryCustom;

    @Autowired
    private EntityManager em;

    @BeforeEach
    void setupData() {
        ListingDTO l1 = ListingDTO.builder()
                .listingId("LID-123")
                .scanDate(LocalDateTime.parse("2024-10-22T12:00:00"))
                .isActive(true)
                .datasetEntityIds(List.of(new Integer[]{1, 2}))
                .imageHashes(List.of(new String[]{"hashA", "hashB"}))
                .build();

        ListingDTO l2 = ListingDTO.builder()
                .listingId("LID-456")
                .scanDate(LocalDateTime.parse("2024-10-23T14:30:00"))
                .isActive(false)
                .datasetEntityIds(List.of(new Integer[]{3}))
                .imageHashes(List.of(new String[]{"hashC"}))
                .build();

        em.persist(l1);
        em.persist(l2);
        em.flush();
    }

    @Test
    void testSearchListingIdsNoFilters() {
        ListingQueryDTO dto = new ListingQueryDTO();
        dto.setPage(1);

        Pair<List<String>, Long> result = listingRepositoryCustom.searchListingIds(dto);
        List<String> listingIds = result.getFirst();
        Long total = result.getSecond();

        assertThat(listingIds).hasSize(2);
        assertThat(total).isEqualTo(2);
    }

    @Test
    void testSearchListingIdsFilterByActive() {
        ListingQueryDTO dto = new ListingQueryDTO();
        dto.setIsActive(true);

        Pair<List<String>, Long> result = listingRepositoryCustom.searchListingIds(dto);
        List<String> listingIds = result.getFirst();
        Long total = result.getSecond();

        assertThat(listingIds).containsExactlyInAnyOrder("LID-123");
        assertThat(total).isEqualTo(1);
    }

    @Test
    void testSearchListingIdsImageHashes() {
        ListingQueryDTO dto = new ListingQueryDTO();
        dto.setImageHashes(List.of("hashC"));

        Pair<List<String>, Long> result = listingRepositoryCustom.searchListingIds(dto);
        List<String> listingIds = result.getFirst();
        Long total = result.getSecond();

        assertThat(listingIds).containsExactly("LID-456");
        assertThat(total).isEqualTo(1);
    }
}

