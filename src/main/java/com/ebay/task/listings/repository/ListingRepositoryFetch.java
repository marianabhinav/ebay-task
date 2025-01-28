package com.ebay.task.listings.repository;

import com.ebay.task.listings.dto.EntityDTO;
import com.ebay.task.listings.dto.ListingDTO;
import com.ebay.task.listings.dto.PropertyDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Slf4j
@Repository
public class ListingRepositoryFetch {

    @PersistenceContext
    private EntityManager em;

    /**
     * Fetch listings by their IDs.
     *
     * @param listingIds the listing IDs
     * @return a list of ListingDTO objects
     */
    @Transactional(readOnly = true)
    public List<ListingDTO> fetchListingsByIds(List<String> listingIds) {
        if (listingIds == null || listingIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 1) Fetch from test_listings
        String listingsSql = """
                SELECT listing_id, scan_date, is_active, dataset_entity_ids, image_hashes
                FROM test_listings
                WHERE listing_id IN :ids
                ORDER BY listing_id ASC
                """;
        Query q1 = em.createNativeQuery(listingsSql);
        q1.setParameter("ids", listingIds);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q1.getResultList();

        // Build a map: listingId -> ListingDTO
        Map<String, ListingDTO> listingMap = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String listingId = (String) row[0];
            // row[1] might be a java.sql.Timestamp
            Object scanDateObj = row[1];
            Boolean isActive = row[2] == null ? null : (Boolean) row[2];
            Integer[] entityIds = (Integer[]) row[3];
            String[] imageHashes = (String[]) row[4];

            ListingDTO dto = ListingDTO.builder()
                    .listingId(listingId)
                    .scanDate(scanDateObj == null ? null : ((java.sql.Timestamp) scanDateObj).toLocalDateTime())
                    .isActive(isActive)
                    .datasetEntityIds(entityIds == null ? List.of() : Arrays.asList(entityIds))
                    .imageHashes(imageHashes == null ? List.of() : Arrays.asList(imageHashes))
                    .properties(new ArrayList<>())
                    .entities(new ArrayList<>())
                    .build();
            listingMap.put(listingId, dto);
        }

        // 2) Fetch string properties
        String strSql = """
                SELECT s.listing_id, s.property_id, s.value, p.name, p.type
                FROM test_property_values_str s
                JOIN test_properties p ON s.property_id = p.property_id
                WHERE s.listing_id IN :ids
                """;
        Query qStr = em.createNativeQuery(strSql);
        qStr.setParameter("ids", listingIds);

        @SuppressWarnings("unchecked")
        List<Object[]> strRows = qStr.getResultList();
        for (Object[] row : strRows) {
            String listingId = (String) row[0];
            Integer propertyId = (Integer) row[1];
            String propValue = (String) row[2];
            String propName = (String) row[3];
            String propType = (String) row[4]; // "string" or "boolean"

            ListingDTO listing = listingMap.get(listingId);
            if (listing != null) {
                PropertyDTO pDto = PropertyDTO.builder()
                        .name(propName)
                        .type(propType.equals("string") ? "str" : "bool")
                        .value(propValue)
                        .build();
                listing.getProperties().add(pDto);
            }
        }

        // 3) Fetch boolean properties
        String boolSql = """
                SELECT b.listing_id, b.property_id, b.value, p.name, p.type
                FROM test_property_values_bool b
                JOIN test_properties p ON b.property_id = p.property_id
                WHERE b.listing_id IN :ids
                """;
        Query qBool = em.createNativeQuery(boolSql);
        qBool.setParameter("ids", listingIds);

        @SuppressWarnings("unchecked")
        List<Object[]> boolRows = qBool.getResultList();
        for (Object[] row : boolRows) {
            String listingId = (String) row[0];
            Integer propertyId = (Integer) row[1];
            Boolean propValue = (Boolean) row[2];
            String propName = (String) row[3];
            String propType = (String) row[4]; // "string" or "boolean"

            ListingDTO listing = listingMap.get(listingId);
            if (listing != null) {
                PropertyDTO pDto = PropertyDTO.builder()
                        .name(propName)
                        .type(propType.equals("string") ? "str" : "bool")
                        .value(propValue)
                        .build();
                listing.getProperties().add(pDto);
            }
        }

        // 4) Fetch entity details
        // Gather all entity IDs from the listings
        Set<Integer> allEntityIds = new HashSet<>();
        for (ListingDTO l : listingMap.values()) {
            if (l.getDatasetEntityIds() != null) {
                allEntityIds.addAll(l.getDatasetEntityIds());
            }
        }

        if (!allEntityIds.isEmpty()) {
            String entitySql = """
                    SELECT entity_id, name, data
                    FROM test_dataset_entities
                    WHERE entity_id IN :eids
                    """;
            Query qE = em.createNativeQuery(entitySql);
            qE.setParameter("eids", allEntityIds);

            @SuppressWarnings("unchecked")
            List<Object[]> entityRows = qE.getResultList();

            // entity_id -> DTO
            Map<Integer, EntityDTO> entityMap = new HashMap<>();
            for (Object[] row : entityRows) {
                Integer entityId = (Integer) row[0];
                String entityName = (String) row[1];
                String dataJson = (String) row[2];
                // Parse JSON to a map
                Map<String, Object> dataMap = null;
                try {
                    dataMap = new ObjectMapper().readValue(dataJson, new TypeReference<>() {
                    });
                } catch (JsonProcessingException e) {
                    // Log the error
                    log.error("Cannot parse entity data JSON: {}", dataJson);
                    throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Cannot query listings.");
                }
                EntityDTO eDto = EntityDTO.builder()
                        .name(entityName)
                        .data(dataMap)
                        .build();
                entityMap.put(entityId, eDto);
            }

            // Attach to each listing
            for (ListingDTO listing : listingMap.values()) {
                List<Integer> eids = listing.getDatasetEntityIds();
                if (eids != null) {
                    List<EntityDTO> entities = new ArrayList<>();
                    for (Integer eid : eids) {
                        if (entityMap.containsKey(eid)) {
                            entities.add(entityMap.get(eid));
                        }
                    }
                    listing.setEntities(entities);
                }
            }
        }

        return new ArrayList<>(listingMap.values());
    }
}

