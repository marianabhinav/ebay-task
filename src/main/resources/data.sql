-- Seed data
INSERT INTO test_listings (listing_id, scan_date, is_active, dataset_entity_ids, image_hashes)
VALUES ('1111223', '2024-10-22 12:00:00', true, '{1,2}', '{2e32d2, f54t45r}'),
       ('1111224', '2024-10-23 14:30:00', false, '{3}', '{a1b2c3}') ON CONFLICT (listing_id) DO NOTHING;

INSERT INTO test_properties (property_id, name, type)
VALUES (123, 'some str property', 'string'),
       (456, 'some bool property', 'boolean') ON CONFLICT (property_id) DO NOTHING;

INSERT INTO test_property_values_str (listing_id, property_id, value)
VALUES ('1111223', 123, 'str value') ON CONFLICT (listing_id, property_id) DO NOTHING;

INSERT INTO test_property_values_bool (listing_id, property_id, value)
VALUES ('1111223', 456, false) ON CONFLICT (listing_id, property_id) DO NOTHING;

INSERT INTO test_dataset_entities (entity_id, name, data)
VALUES (1, 'entity_one', '{"key1": "value1", "key2": 123}'),
       (2, 'entity_two', '{"key3": true, "key4": null}'),
       (3, 'entity_three', '{"key5": "another value", "key6": false}') ON CONFLICT (entity_id) DO NOTHING;
