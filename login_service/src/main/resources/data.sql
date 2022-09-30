INSERT INTO roles (id , name) VALUES (1, 'CUSTOMER');
INSERT INTO roles (id , name) VALUES (2, 'ADMIN');
INSERT INTO roles (id, name) VALUES (3, 'ADMIN_E');
INSERT INTO roles (id, name) VALUES (4, 'EMBEDDED_SYSTEM');

/* INSERT STARTUP ADMIN IN USER
    nickname: "startup_admin"
    password : "Password2022!"
*/

INSERT INTO users (id, active, email, nickname, password) VALUES (9223372036854775807, true, 'admin@gmail.com', 'startup_admin', '$2a$10$tF89eHwXEblVRSeeIUB79e8pcTr5euSFSq8MOz.u0jZmgcToGz8Ha');
INSERT INTO user_role (user_id, role_id) VALUES (9223372036854775807, 3);
INSERT INTO user_role (user_id, role_id) VALUES (9223372036854775807, 1);
INSERT INTO user_role (user_id, role_id) VALUES (9223372036854775807, 2);

/* INSERT STARTUP EMBEDDED IN USER
    nickname: "embedded_system"
    password : "Password2022!"
*/

INSERT INTO users (id, active, email, nickname, password) VALUES (9223372036854775806, true, ' ', 'embedded_system', '$2a$10$tF89eHwXEblVRSeeIUB79e8pcTr5euSFSq8MOz.u0jZmgcToGz8Ha');
                                                /* TODO: insert an email for this embedded system */
INSERT INTO user_role (user_id, role_id) VALUES (9223372036854775806, 4);

/*TODO : check if tests have problems with the insertion of startup user*/

/* TODO: update sequence related to autoincrement */
/*
    This method does not work and we are unable to update sequence related to autoincrement
    So the first two users have the max value of Long and max value - 1

    sources:
    - https://dba.stackexchange.com/questions/65662/postgres-how-to-insert-row-with-autoincrement-id
    - https://newbedev.com/postgresql-unique-violation-7-error-duplicate-key-value-violates-unique-constraint-users-pkey
    - https://stackoverflow.com/questions/44744365/liquibase-postgresql-spring-jpa-id-auto-increment-issue
*/
-- ALTER SEQUENCE users_id_seq RESTART WITH 3;
-- or
-- SELECT setval('users_id_seq', (SELECT max(id)) FROM users));
-- or
-- SELECT setval(pg_get_serial_sequence('users', 'id'), coalesce(max(id)+1, 1), false) FROM users;
-- or
-- SELECT setval('users_id_seq', 2, true);  -- next value will be 3