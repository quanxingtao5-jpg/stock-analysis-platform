CREATE DATABASE IF NOT EXISTS stock_analysis CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE stock_analysis;

CREATE TABLE IF NOT EXISTS stock (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(32) NOT NULL,
    name VARCHAR(128) NOT NULL,
    price DECIMAL(18, 4) NOT NULL,
    UNIQUE KEY uk_stock_symbol (symbol)
);
