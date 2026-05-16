-- 계좌
INSERT INTO account (user_id, name, broker_name, account_type, currency, memo, created_at, updated_at) VALUES
(1, '신한 주식 계좌', '신한투자증권', 'GENERAL', 'KRW', '주력 국내외 주식용', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'KB ISA 계좌', 'KB증권', 'ISA', 'KRW', '절세용 계좌', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 종목
INSERT INTO security_item (ticker, name, market, country, currency, security_type, created_at, updated_at) VALUES 
('AAPL', 'Apple Inc.', 'NASDAQ', 'USA', 'USD', 'STOCK', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MSFT', 'Microsoft Corp.', 'NASDAQ', 'USA', 'USD', 'STOCK', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('005930', '삼성전자', 'KOSPI', 'KOREA', 'KRW', 'STOCK', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('SCHD', 'Schwab US Dividend', 'NYSE', 'USA', 'USD', 'ETF', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
