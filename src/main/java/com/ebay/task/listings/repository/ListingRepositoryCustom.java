package com.ebay.task.listings.repository;

import com.ebay.task.listings.dto.request.ListingQueryDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class ListingRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    /**
     * Search for listing IDs based on the given query parameters.
     *
     * @param queryDTO the query parameters
     * @return a pair of listing IDs and the total count
     */
    @Transactional(readOnly = true)
    public Pair<List<String>, Long> searchListingIds(ListingQueryDTO queryDTO) {
        // Build the WHERE clause dynamically
        StringBuilder sb = new StringBuilder(" FROM test_listings l WHERE 1=1 ");
        Map<String, Object> params = new HashMap<>();

        // Filter: listingId
        if (queryDTO.getListingId() != null && !queryDTO.getListingId().isEmpty()) {
            sb.append(" AND l.listing_id = :listingId ");
            params.put("listingId", queryDTO.getListingId());
        }

        // Filter: scan_date range
        if (queryDTO.getScanDateFrom() != null) {
            sb.append(" AND l.scan_date >= :scanDateFrom ");
            params.put("scanDateFrom", queryDTO.getScanDateFrom());
        }
        if (queryDTO.getScanDateTo() != null) {
            sb.append(" AND l.scan_date <= :scanDateTo ");
            params.put("scanDateTo", queryDTO.getScanDateTo());
        }

        // Filter: is_active
        if (queryDTO.getIsActive() != null) {
            sb.append(" AND l.is_active = :isActive ");
            params.put("isActive", queryDTO.getIsActive());
        }

        // Filter: image_hashes (array intersection with input list)
        if (queryDTO.getImageHashes() != null && !queryDTO.getImageHashes().isEmpty()) {
            sb.append(" AND l.image_hashes && CAST(:imageHashes AS text[]) ");
            params.put("imageHashes", queryDTO.getImageHashes().toArray(new String[0]));
        }

        // Filter: datasetEntities -> JSONB containment check
        if (queryDTO.getDatasetEntities() != null && !queryDTO.getDatasetEntities().isEmpty()) {
            // Convert to JSON string
            String datasetEntitiesJson = null;
            try {
                datasetEntitiesJson = new ObjectMapper().writeValueAsString(queryDTO.getDatasetEntities());
            } catch (JsonProcessingException e) {
                log.error("Error converting datasetEntities to JSON.", e);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid datasetEntities JSON.");
            }
            sb.append(" AND EXISTS (");
            sb.append("   SELECT 1 FROM test_dataset_entities de ");
            sb.append("   WHERE de.entity_id = ANY(l.dataset_entity_ids) ");
            sb.append("     AND de.data @> CAST(:datasetEntitiesJson AS jsonb)");
            sb.append(" )");
            params.put("datasetEntitiesJson", datasetEntitiesJson);
        }

        // propertyFilters -> for each property_id -> expected value
        if (queryDTO.getPropertyFilters() != null && !queryDTO.getPropertyFilters().isEmpty()) {
            int i = 0;
            for (Map.Entry<Integer, Object> entry : queryDTO.getPropertyFilters().entrySet()) {
                Integer propertyId = entry.getKey();
                Object expectedValue = entry.getValue();

                String paramPropId = "propId" + i;
                String paramVal = "val" + i;

                sb.append(" AND EXISTS ( ");
                sb.append("   SELECT 1 FROM test_properties p ");
                sb.append("   WHERE p.property_id = :").append(paramPropId).append(" ");
                sb.append("     AND ( ");
                if (expectedValue instanceof Boolean) {
                    sb.append("       (p.type = 'boolean' AND EXISTS ( ");
                    sb.append("         SELECT 1 FROM test_property_values_bool b ");
                    sb.append("         WHERE b.listing_id = l.listing_id ");
                    sb.append("           AND b.property_id = p.property_id ");
                    sb.append("           AND b.value = CAST(:").append(paramVal).append(" AS boolean) ");
                    sb.append("       )) ");
                } else {
                    sb.append("       (p.type = 'string' AND EXISTS ( ");
                    sb.append("         SELECT 1 FROM test_property_values_str s ");
                    sb.append("         WHERE s.listing_id = l.listing_id ");
                    sb.append("           AND s.property_id = p.property_id ");
                    sb.append("           AND s.value = :").append(paramVal).append(" ");
                    sb.append("       )) ");
                }
                sb.append("     ) ");
                sb.append(" ) ");

                params.put(paramPropId, propertyId);
                params.put(paramVal, expectedValue);

                i++;
            }
        }

        // COUNT query
        String countSql = "SELECT COUNT(*) " + sb;
        Query countQuery = em.createNativeQuery(countSql);
        params.forEach(countQuery::setParameter);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        // SELECT query (just listing_id) with pagination
        String selectSql = "SELECT l.listing_id " + sb + " ORDER BY l.listing_id ASC";
        Query selectQuery = em.createNativeQuery(selectSql);
        params.forEach(selectQuery::setParameter);

        int pageSize = 100;
        int page = (queryDTO.getPage() == null || queryDTO.getPage() < 1) ? 1 : queryDTO.getPage();
        selectQuery.setFirstResult((page - 1) * pageSize);
        selectQuery.setMaxResults(pageSize);

        @SuppressWarnings("unchecked")
        List<String> listingIds = selectQuery.getResultList();

        return Pair.of(listingIds, total);
    }
}
