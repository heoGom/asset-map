-- 계좌
INSERT INTO account (user_id, name, account_number, account_type, currency, memo, created_at, updated_at) VALUES 
(1, '신한 주식 계좌', '123-456-789', 'STOCK', 'KRW', '주력 국내외 주식용', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'KB ISA 계좌', '987-654-321', 'ISA', 'KRW', '절세용 계좌', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 종목
INSERT INTO security_item (ticker, name, market, country, currency, security_type, created_at, updated_at) VALUES 
('AAPL', 'Apple Inc.', 'NASDAQ', 'USA', 'USD', 'COMMON_STOCK', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MSFT', 'Microsoft Corp.', 'NASDAQ', 'USA', 'USD', 'COMMON_STOCK', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('005930', '삼성전자', 'KOSPI', 'KOREA', 'KRW', 'COMMON_STOCK', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('SCHD', 'Schwab US Dividend', 'NYSE', 'USA', 'USD', 'ETF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 종목 분류
INSERT INTO security_classification (security_item_id, asset_group, sector, country_group, strategy_type, created_at, updated_at) VALUES 
(1, 'STOCK', 'TECHNOLOGY', 'USA', 'GROWTH', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'STOCK', 'TECHNOLOGY', 'USA', 'GROWTH', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'STOCK', 'TECHNOLOGY', 'KOREA', 'VALUE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'ETF', 'FINANCE', 'USA', 'DIVIDEND', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 보유 종목
INSERT INTO holding (user_id, account_id, security_item_id, quantity, average_price, current_price, currency, created_at, updated_at) VALUES 
(1, 1, 1, 10, 180.00, 185.00, 'USD', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 1, 2, 5, 300.00, 310.00, 'USD', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 2, 3, 50, 70000, 72000, 'KRW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 2, 4, 20, 75.00, 80.00, 'USD', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 배당 이벤트
INSERT INTO dividend_event (security_item_id, dividend_year, dividend_per_share, ex_dividend_date, created_at, updated_at) VALUES 
(1, 2026, 0.25, '2026-06-01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 2026, 0.70, '2026-06-15', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 배당 지급
INSERT INTO dividend_payment (user_id, security_item_id, holding_id, payment_date, amount, status, created_at, updated_at) VALUES 
(1, 1, 1, '2026-03-15', 2.50, 'PAID', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 4, 4, '2026-03-20', 14.00, 'PAID', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
