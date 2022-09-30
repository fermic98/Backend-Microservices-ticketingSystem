drop table if exists transactions;

create table if not exists  transactions (
                                        transaction_id BIGINT GENERATED ALWAYS AS IDENTITY,
                                        amount DECIMAL(10,2) NOT NULL ,
                                        customer VARCHAR(255) NOT NULL,
                                        order_id BIGINT NOT NULL,
                                        transaction_date TIMESTAMP NOT NULL,
                                        status VARCHAR(255) NOT NULL,
                                        credit_card_number VARCHAR(255) NOT NULL,
                                        expiration_date VARCHAR(255) NOT NULL,
                                        cvv VARCHAR(255) NOT NULL,
                                        card_holder VARCHAR(255) NOT NULL,
                                        PRIMARY KEY (transaction_id)
                                        );