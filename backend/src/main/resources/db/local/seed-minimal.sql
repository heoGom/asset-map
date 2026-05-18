-- Local development minimal seed.
-- This file contains synthetic sample data only. Apply it explicitly with:
-- docker exec -i asset-map-mysql mysql -uassetmap -passetmap asset_map < backend/src/main/resources/db/local/seed-minimal.sql

INSERT INTO app_user (id, email, password, nickname, created_at, updated_at) VALUES
(1, 'test@test.com', '$2a$10$3lV0LwuTcV7V5Y6sS1gSo.VbDSjBG/9T2Up/aAeYFYZyj2fGpIsmW', 'test', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO account (id, user_id, name, broker_name, account_type, currency, memo, created_at, updated_at) VALUES
(1, 1, 'Local General Account', 'Local Broker', 'GENERAL', 'KRW', 'minimal local sample', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 'Local ISA Account', 'Local Broker', 'ISA', 'KRW', 'minimal local sample', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO security_item (id, ticker, name, market, country, currency, security_type, created_at, updated_at) VALUES
(1, '005935', '삼성전자우', 'KOSPI', 'KOREA', 'KRW', 'STOCK', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '002020', '코오롱', 'KOSPI', 'KOREA', 'KRW', 'STOCK', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '133690', 'TIGER 미국나스닥100', 'KOSPI', 'KOREA', 'KRW', 'ETF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'CASH', '예수금', 'CASH', 'KOREA', 'KRW', 'CASH', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO trade_transaction (
  id, user_id, account_id, security_item_id, trade_date, trade_type, quantity, price, gross_amount, fee, tax, net_amount, currency, source, memo, created_at, updated_at
) VALUES
(1, 1, 1, 1, DATE '2024-01-02', 'INITIAL', 3, 72000, 216000, 0, 0, 216000, 'KRW', 'INITIAL', 'minimal local sample', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 1, 2, DATE '2024-01-02', 'INITIAL', 2, 25000, 50000, 0, 0, 50000, 'KRW', 'INITIAL', 'minimal local sample', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, 2, 3, DATE '2024-01-02', 'INITIAL', 1, 150000, 150000, 0, 0, 150000, 'KRW', 'INITIAL', 'minimal local sample', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO holding (
  id, user_id, account_id, security_item_id, quantity, average_price, current_price, currency, created_at, updated_at
) VALUES
(1, 1, 1, 1, 3, 72000, 72000, 'KRW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 1, 2, 2, 25000, 25000, 'KRW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, 2, 3, 1, 150000, 150000, 'KRW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

ALTER TABLE app_user AUTO_INCREMENT = 2;
ALTER TABLE account AUTO_INCREMENT = 3;
ALTER TABLE security_item AUTO_INCREMENT = 5;
ALTER TABLE trade_transaction AUTO_INCREMENT = 4;
ALTER TABLE holding AUTO_INCREMENT = 4;
