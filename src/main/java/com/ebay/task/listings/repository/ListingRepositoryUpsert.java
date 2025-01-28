package com.ebay.task.listings.repository;

import com.ebay.task.listings.dto.EntityDTO;
import com.ebay.task.listings.dto.PropertyDTO;
import com.ebay.task.listings.dto.request.ListingUpsertDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ListingRepositoryUpsert {

    @PersistenceContext
    private final EntityManager em;

    /**
     * Upsert a listing and its related properties, entities, and image hashes.
     *
     * @param dto the listing DTO
     */
    @Transactional
    public void upsertListing(ListingUpsertDTO dto) {
        // 1) Check if listing exists
        String checkSql = "SELECT COUNT(*) FROM test_listings WHERE listing_id = :lid";
        Query checkQ = em.createNativeQuery(checkSql);
        checkQ.setParameter("lid", dto.getListingId());
        long count = ((Number) checkQ.getSingleResult()).longValue();

        // 2) Insert or Update
        if (count == 0) {
            // Insert
            String insertListingSql = """
                    INSERT INTO test_listings 
                        (listing_id, scan_date, is_active, dataset_entity_ids, image_hashes)
                    VALUES (:lid, :scanDate, :isActive, CAST(:emptyIntArr AS integer[]), CAST(:emptyTextArr AS text[]))
                    """;
            Query insQ = em.createNativeQuery(insertListingSql);
            insQ.setParameter("lid", dto.getListingId());
            insQ.setParameter("scanDate", dto.getScanDate());
            insQ.setParameter("isActive", dto.getIsActive());
            insQ.setParameter("emptyIntArr", new Integer[0]);
            insQ.setParameter("emptyTextArr", new String[0]);
            insQ.executeUpdate();
        } else {
            // Update base fields
            String updateListingSql = """
                    UPDATE test_listings
                    SET scan_date = :scanDate, is_active = :isActive
                    WHERE listing_id = :lid
                    """;
            Query updQ = em.createNativeQuery(updateListingSql);
            updQ.setParameter("lid", dto.getListingId());
            updQ.setParameter("scanDate", dto.getScanDate());
            updQ.setParameter("isActive", dto.getIsActive());
            updQ.executeUpdate();
        }

        // 3) Upsert each property
        if (dto.getProperties() != null) {
            for (PropertyDTO p : dto.getProperties()) {
                upsertProperty(dto.getListingId(), p);
            }
        }

        // 4) Upsert each entity
        Set<Integer> entityIds = new HashSet<>();
        if (dto.getEntities() != null) {
            for (EntityDTO e : dto.getEntities()) {
                Integer eid = upsertEntity(e);
                entityIds.add(eid);
            }
        }
        // Merge new entity IDs into dataset_entity_ids
        if (!entityIds.isEmpty()) {
            String fetchSql = "SELECT dataset_entity_ids FROM test_listings WHERE listing_id = :lid";
            Query fQ = em.createNativeQuery(fetchSql);
            fQ.setParameter("lid", dto.getListingId());
            Integer[] currentArr = (Integer[]) fQ.getSingleResult();

            Set<Integer> combined = new HashSet<>();
            if (currentArr != null) {
                combined.addAll(Arrays.asList(currentArr));
            }
            combined.addAll(entityIds);
            Integer[] newArr = combined.toArray(new Integer[0]);

            String updArrSql = "UPDATE test_listings SET dataset_entity_ids = :newArr WHERE listing_id = :lid";
            Query updArrQ = em.createNativeQuery(updArrSql);
            updArrQ.setParameter("newArr", newArr);
            updArrQ.setParameter("lid", dto.getListingId());
            updArrQ.executeUpdate();
        }

        // 5) Update image_hashes (replace entire array in this example)
        if (dto.getImageHashes() != null && !dto.getImageHashes().isEmpty()) {
            List<String> unique = dto.getImageHashes().stream().distinct().collect(Collectors.toList());
            String[] newHashArr = unique.toArray(new String[0]);

            String updImagesSql = "UPDATE test_listings SET image_hashes = :hashes WHERE listing_id = :lid";
            Query updImagesQ = em.createNativeQuery(updImagesSql);
            updImagesQ.setParameter("hashes", newHashArr);
            updImagesQ.setParameter("lid", dto.getListingId());
            updImagesQ.executeUpdate();
        }
    }

    private void upsertProperty(String listingId, PropertyDTO dto) {
        // Convert "str" -> "string", "bool" -> "boolean"
        String dbType = dto.getType().equals("str") ? "string" : "boolean";

        // 1) Check if property (name, type) exists
        String checkSql = "SELECT property_id FROM test_properties WHERE name = :name AND type = :type";
        Query checkQ = em.createNativeQuery(checkSql);
        checkQ.setParameter("name", dto.getName());
        checkQ.setParameter("type", dbType);

        @SuppressWarnings("unchecked")
        List<Integer> ids = checkQ.getResultList();

        Integer propertyId;
        if (ids.isEmpty()) {
            // Insert
            String insertPropSql = """
                    INSERT INTO test_properties (name, type)
                    VALUES (:name, :type)
                    RETURNING property_id
                    """;
            Query insQ = em.createNativeQuery(insertPropSql);
            insQ.setParameter("name", dto.getName());
            insQ.setParameter("type", dbType);
            propertyId = ((Number) insQ.getSingleResult()).intValue();
        } else {
            propertyId = ids.get(0);
        }

        // 2) Upsert property value
        if (dbType.equals("string")) {
            upsertStringPropertyValue(listingId, propertyId, (String) dto.getValue());
        } else {
            upsertBoolPropertyValue(listingId, propertyId, (Boolean) dto.getValue());
        }
    }

    private void upsertStringPropertyValue(String listingId, Integer propertyId, String val) {
        String checkSql = """
                SELECT COUNT(*) FROM test_property_values_str
                WHERE listing_id = :lid AND property_id = :pid
                """;
        Query checkQ = em.createNativeQuery(checkSql);
        checkQ.setParameter("lid", listingId);
        checkQ.setParameter("pid", propertyId);
        long count = ((Number) checkQ.getSingleResult()).longValue();

        if (count == 0) {
            String insertSql = """
                    INSERT INTO test_property_values_str (listing_id, property_id, value)
                    VALUES (:lid, :pid, :val)
                    """;
            Query insQ = em.createNativeQuery(insertSql);
            insQ.setParameter("lid", listingId);
            insQ.setParameter("pid", propertyId);
            insQ.setParameter("val", val);
            insQ.executeUpdate();
        } else {
            String updateSql = """
                    UPDATE test_property_values_str
                    SET value = :val
                    WHERE listing_id = :lid AND property_id = :pid
                    """;
            Query updQ = em.createNativeQuery(updateSql);
            updQ.setParameter("lid", listingId);
            updQ.setParameter("pid", propertyId);
            updQ.setParameter("val", val);
            updQ.executeUpdate();
        }
    }

    private void upsertBoolPropertyValue(String listingId, Integer propertyId, Boolean val) {
        String checkSql = """
                SELECT COUNT(*) FROM test_property_values_bool
                WHERE listing_id = :lid AND property_id = :pid
                """;
        Query checkQ = em.createNativeQuery(checkSql);
        checkQ.setParameter("lid", listingId);
        checkQ.setParameter("pid", propertyId);
        long count = ((Number) checkQ.getSingleResult()).longValue();

        if (count == 0) {
            String insertSql = """
                    INSERT INTO test_property_values_bool (listing_id, property_id, value)
                    VALUES (:lid, :pid, :val)
                    """;
            Query insQ = em.createNativeQuery(insertSql);
            insQ.setParameter("lid", listingId);
            insQ.setParameter("pid", propertyId);
            insQ.setParameter("val", val);
            insQ.executeUpdate();
        } else {
            String updateSql = """
                    UPDATE test_property_values_bool
                    SET value = :val
                    WHERE listing_id = :lid AND property_id = :pid
                    """;
            Query updQ = em.createNativeQuery(updateSql);
            updQ.setParameter("lid", listingId);
            updQ.setParameter("pid", propertyId);
            updQ.setParameter("val", val);
            updQ.executeUpdate();
        }
    }

    private Integer upsertEntity(EntityDTO dto) {
        // 1) Check if entity with this name already exists
        String checkSql = "SELECT entity_id FROM test_dataset_entities WHERE name = :name";
        Query checkQ = em.createNativeQuery(checkSql);
        checkQ.setParameter("name", dto.getName());

        @SuppressWarnings("unchecked")
        List<Integer> ids = checkQ.getResultList();

        Integer entityId;
        String dataJson = null;
        try {
            dataJson = new ObjectMapper().writeValueAsString(dto.getData());
        } catch (JsonProcessingException e) {
            log.error("Error converting entity data to JSON.", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid entity data JSON.");
        }
        if (ids.isEmpty()) {
            // Insert
            String insertSql = """
                    INSERT INTO test_dataset_entities (name, data)
                    VALUES (:name, CAST(:dataJson AS jsonb))
                    RETURNING entity_id
                    """;
            Query insQ = em.createNativeQuery(insertSql);
            insQ.setParameter("name", dto.getName());
            insQ.setParameter("dataJson", dataJson);
            entityId = ((Number) insQ.getSingleResult()).intValue();
        } else {
            entityId = ids.get(0);
            // Update data
            String updateSql = """
                    UPDATE test_dataset_entities
                    SET data = CAST(:dataJson AS jsonb)
                    WHERE entity_id = :eid
                    """;
            Query updQ = em.createNativeQuery(updateSql);
            updQ.setParameter("dataJson", dataJson);
            updQ.setParameter("eid", entityId);
            updQ.executeUpdate();
        }
        return entityId;
    }
}

