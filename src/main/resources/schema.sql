-- One-time fix: drop catalog resource tables so Hibernate recreates them with correct schema.
-- After the backend starts successfully once, set spring.sql.init.mode=never in application.properties
-- and delete (or leave) this file.
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS availability_windows;
DROP TABLE IF EXISTS resources;
DROP TABLE IF EXISTS locations;
SET FOREIGN_KEY_CHECKS = 1;
