-- Local development minimal seed.
-- This file contains synthetic sample data only. Apply it explicitly with:
-- docker exec -i asset-map-mysql mysql -uassetmap -passetmap asset_map < backend/src/main/resources/db/local/seed-minimal.sql
-- Run KRX security master sync before this seed. STOCK rows are referenced by ticker from SecurityItem.
-- ETF master sync is not implemented yet, so this seed keeps only minimal ETF master rows with real public tickers.

INSERT INTO app_user (id, email, password, nickname, created_at, updated_at) VALUES
(1, 'test@test.com', '$2a$10$3lV0LwuTcV7V5Y6sS1gSo.VbDSjBG/9T2Up/aAeYFYZyj2fGpIsmW', 'test', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO account (id, user_id, name, broker_name, account_type, currency, memo, created_at, updated_at) VALUES
(1, 1, 'Local General Account', 'Local Broker', 'GENERAL', 'KRW', 'minimal local sample', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 'Local ISA Account', 'Local Broker', 'ISA', 'KRW', 'minimal local sample', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO security_item (ticker, name, market, country, currency, security_type, created_at, updated_at) VALUES
('133690', 'TIGER 미국나스닥100', 'KOSPI', 'KOREA', 'KRW', 'ETF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('458730', 'TIGER 미국배당다우존스', 'KOSPI', 'KOREA', 'KRW', 'ETF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('379780', 'RISE 미국S&P500', 'KOSPI', 'KOREA', 'KRW', 'ETF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CASH', '예수금', 'CASH', 'KOREA', 'KRW', 'CASH', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
name = VALUES(name),
market = VALUES(market),
country = VALUES(country),
currency = VALUES(currency),
security_type = VALUES(security_type),
updated_at = CURRENT_TIMESTAMP;

INSERT INTO trade_transaction (
  id, user_id, account_id, security_item_id, trade_date, trade_type, quantity, price, gross_amount, fee, tax, net_amount, currency, source, memo, created_at, updated_at
) VALUES
(1, 1, 1, (SELECT id FROM security_item WHERE ticker = '005935'), DATE '2024-01-02', 'INITIAL', 3, 72000, 216000, 0, 0, 216000, 'KRW', 'INITIAL', 'minimal local sample', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 1, (SELECT id FROM security_item WHERE ticker = '002020'), DATE '2024-01-02', 'INITIAL', 2, 25000, 50000, 0, 0, 50000, 'KRW', 'INITIAL', 'minimal local sample', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, 2, (SELECT id FROM security_item WHERE ticker = '133690'), DATE '2024-01-02', 'INITIAL', 1, 150000, 150000, 0, 0, 150000, 'KRW', 'INITIAL', 'minimal local sample', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 1, 2, (SELECT id FROM security_item WHERE ticker = '458730'), DATE '2024-01-02', 'INITIAL', 1, 12000, 12000, 0, 0, 12000, 'KRW', 'INITIAL', 'minimal local sample', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 1, 2, (SELECT id FROM security_item WHERE ticker = '379780'), DATE '2024-01-02', 'INITIAL', 1, 18000, 18000, 0, 0, 18000, 'KRW', 'INITIAL', 'minimal local sample', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO holding (
  id, user_id, account_id, security_item_id, quantity, average_price, current_price, currency, created_at, updated_at
) VALUES
(1, 1, 1, (SELECT id FROM security_item WHERE ticker = '005935'), 3, 72000, 72000, 'KRW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 1, (SELECT id FROM security_item WHERE ticker = '002020'), 2, 25000, 25000, 'KRW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, 2, (SELECT id FROM security_item WHERE ticker = '133690'), 1, 150000, 150000, 'KRW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 1, 2, (SELECT id FROM security_item WHERE ticker = '458730'), 1, 12000, 12000, 'KRW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 1, 2, (SELECT id FROM security_item WHERE ticker = '379780'), 1, 18000, 18000, 'KRW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

ALTER TABLE app_user AUTO_INCREMENT = 2;
ALTER TABLE account AUTO_INCREMENT = 3;
ALTER TABLE trade_transaction AUTO_INCREMENT = 6;
ALTER TABLE holding AUTO_INCREMENT = 6;
