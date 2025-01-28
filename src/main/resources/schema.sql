CREATE TABLE IF NOT EXISTS test_listings
(
    listing_id
    VARCHAR
(
    50
) PRIMARY KEY,
    scan_date TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL,
    dataset_entity_ids integer [] DEFAULT '{}',
    image_hashes text[] DEFAULT '{}'
    );
CREATE INDEX IF NOT EXISTS idx_test_listings_is_active ON test_listings(is_active);
CREATE INDEX IF NOT EXISTS idx_test_listings_scan_date ON test_listings(scan_date);
-- Array GIN indexes to allow filtering by array content
CREATE INDEX IF NOT EXISTS idx_listings_image_hashes ON test_listings USING GIN (image_hashes);
CREATE INDEX IF NOT EXISTS idx_listings_dataset_entity_ids ON test_listings USING GIN (dataset_entity_ids);

CREATE TABLE IF NOT EXISTS test_properties
(
    property_id
    INT
    PRIMARY
    KEY,
    name
    VARCHAR
(
    255
) NOT NULL UNIQUE,
    type VARCHAR
(
    20
) NOT NULL CHECK
(
    type
    IN
(
    'string',
    'boolean'
))
    );
CREATE UNIQUE INDEX IF NOT EXISTS ux_properties_name ON test_properties (name);


CREATE TABLE IF NOT EXISTS test_property_values_str
(
    listing_id
    VARCHAR
(
    50
) NOT NULL,
    property_id INT NOT NULL,
    value TEXT,
    PRIMARY KEY
(
    listing_id,
    property_id
),
    CONSTRAINT fk_listing_str FOREIGN KEY
(
    listing_id
)
    REFERENCES test_listings
(
    listing_id
)
    ON DELETE CASCADE,
    CONSTRAINT fk_property_str FOREIGN KEY
(
    property_id
)
    REFERENCES test_properties
(
    property_id
)
    ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS test_property_values_bool
(
    listing_id
    VARCHAR
(
    50
) NOT NULL,
    property_id INT NOT NULL,
    value BOOLEAN,
    PRIMARY KEY
(
    listing_id,
    property_id
),
    CONSTRAINT fk_listing_bool FOREIGN KEY
(
    listing_id
)
    REFERENCES test_listings
(
    listing_id
)
    ON DELETE CASCADE,
    CONSTRAINT fk_property_bool FOREIGN KEY
(
    property_id
)
    REFERENCES test_properties
(
    property_id
)
    ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS test_dataset_entities
(
    entity_id
    INT
    PRIMARY
    KEY,
    name
    VARCHAR
(
    255
) NOT NULL UNIQUE,
    data JSONB
    );

-- JSONB GIN index to allow filtering by JSON content
CREATE INDEX IF NOT EXISTS idx_entities_data_jsonb ON test_dataset_entities USING GIN (data);