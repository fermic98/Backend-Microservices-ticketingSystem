drop table if exists payment_infos;
drop table if exists orders;
drop table if exists tickets;

create table if not exists  tickets (
    ticket_id BIGINT GENERATED ALWAYS AS IDENTITY,
    price DECIMAL(10,2) NOT NULL ,
    type VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    min_age INT,
    max_age INT,
    start_period TIMESTAMP,
    end_period TIMESTAMP,
    duration INTEGER,
    PRIMARY KEY (ticket_id)
    );





create table if not exists  orders (
                                        order_id BIGINT GENERATED ALWAYS AS IDENTITY,
                                        status VARCHAR(20) NOT NULL ,
                                        user_id VARCHAR(255) NOT NULL ,
                                        quantity INTEGER NOT NULL,
                                        total_price NUMERIC(7,2) NOT NULL,
                                        ticket_type BIGINT NOT NULL ,
                                        not_before TIMESTAMP NOT NULL,
                                        zone_id VARCHAR(255) NOT NULL,
                                        PRIMARY KEY (order_id),
                                        CONSTRAINT fk_customer
                                            FOREIGN KEY(ticket_type)
                                                REFERENCES tickets(ticket_id)

);

create table if not exists  payment_infos (
                                       order_id BIGINT,
                                       credit_card_number VARCHAR(255) NOT NULL,

                                       expiration_date VARCHAR(255) NOT NULL,

                                       cvv VARCHAR(255) NOT NULL,

                                       card_holder VARCHAR(255) NOT NULL,

                                    PRIMARY KEY (order_id)

    );

INSERT INTO tickets ( price, type, name, min_age, max_age, duration, start_period, end_period) VALUES ( 1.70, 'ordinal', '70 minutes',NULL,NULL,NULL,NULL,NULL);           --1
INSERT INTO tickets ( price, type, name, min_age, max_age, duration, start_period, end_period) VALUES ( 3.00, 'ordinal', 'daily',NULL,NULL,NULL,NULL,NULL);                --2
INSERT INTO tickets ( price, type, name, min_age, max_age, duration, start_period, end_period) VALUES ( 10.00, 'ordinal', 'weekly',NULL,NULL,NULL,NULL,NULL);              --3
INSERT INTO tickets ( price, type, name, min_age, max_age, duration, start_period, end_period) VALUES ( 24.00, 'ordinal', 'monthly',NULL,NULL,NULL,NULL,NULL);             --4
INSERT INTO tickets ( price, type, name, min_age, max_age, duration, start_period, end_period) VALUES ( 60.00, 'ordinal', 'biannually',NULL,NULL,NULL,NULL,NULL);          --5
INSERT INTO tickets ( price, type, name, min_age, max_age, duration, start_period, end_period) VALUES ( 110.00, 'ordinal', 'yearly',NULL,NULL,NULL,NULL,NULL);             --6
INSERT INTO tickets ( price, type, name, min_age, max_age, duration, start_period, end_period) VALUES ( 3.80, 'ordinal', 'weekend_pass', NULL, 27,NULL,NULL,NULL);         --7
