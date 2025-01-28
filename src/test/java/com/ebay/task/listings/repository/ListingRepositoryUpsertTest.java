package com.ebay.task.listings.repository;

import com.ebay.task.listings.config.PostgresTestConfig;
import com.ebay.task.listings.domain.ListingEntity;
import com.ebay.task.listings.dto.EntityDTO;
import com.ebay.task.listings.dto.PropertyDTO;
import com.ebay.task.listings.dto.request.ListingUpsertDTO;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
        PostgresTestConfig.class,
        ListingRepositoryUpsert.class
})
@ActiveProfiles("test")
@Transactional
class ListingRepositoryUpsertTest {

    @Autowired
    private ListingRepositoryUpsert listingRepositoryUpsert;

    @Autowired
    private EntityManager em;

    @Test
    void testUpsertListingInsert() {
        ListingUpsertDTO dto = ListingUpsertDTO.builder()
                .listingId("NEW-111")
                .scanDate(LocalDateTime.now())
                .isActive(true)
                .imageHashes(List.of("hashX", "hashY"))
                .properties(List.of(
                        PropertyDTO.builder().name("some str property").type("str").value("Hello World").build(),
                        PropertyDTO.builder().name("some bool property").type("bool").value(true).build()
                ))
                .entities(List.of(
                        EntityDTO.builder().name("entity_one").data(Map.of("key1", "value1")).build()
                ))
                .build();

        listingRepositoryUpsert.upsertListing(dto);

        // Now check the DB
        ListingEntity found = em.find(ListingEntity.class, "NEW-111");
        assertThat(found).isNotNull();
        assertThat(found.getImageHashes()).containsExactlyInAnyOrder("hashX", "hashY");
        assertThat(found.getDatasetEntityIds()).isNotEmpty(); // we expect newly inserted entity
    }

    @Test
    void testUpsertListingUpdateExisting() {
        ListingEntity l = ListingEntity.builder()
                .listingId("EXISTING-123")
                .scanDate(LocalDateTime.parse("2024-10-22T12:00:00"))
                .isActive(false)
                .datasetEntityIds(new Integer[]{1})
                .imageHashes(new String[]{"h1"})
                .build();
        em.persist(l);
        em.flush();


        ListingUpsertDTO dto = ListingUpsertDTO.builder()
                .listingId("EXISTING-123")
                .scanDate(LocalDateTime.parse("2025-01-01T12:34:56"))
                .isActive(true)
                .imageHashes(List.of("h2", "h3")) // replaces old array
                .properties(List.of(
                        PropertyDTO.builder().name("some str property").type("str").value("updated str").build()
                ))
                .entities(List.of(
                        EntityDTO.builder().name("entity_two").data(Map.of("keyX", "valX")).build()
                ))
                .build();

        listingRepositoryUpsert.upsertListing(dto);


        ListingEntity updated = em.find(ListingEntity.class, "EXISTING-123");
        assertThat(updated).isNotNull();
        assertThat(updated.getIsActive()).isTrue();
        assertThat(updated.getImageHashes()).containsExactly("h2", "h3");
    }
}
