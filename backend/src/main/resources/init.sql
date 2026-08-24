-- MySQL 初始化脚本
CREATE DATABASE IF NOT EXISTS finance_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE finance_db;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    status INT DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 账户表
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL,
    balance DECIMAL(12,2) NOT NULL DEFAULT 0,
    remark VARCHAR(200),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 分类表（消费板块）
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    type VARCHAR(10) NOT NULL,
    icon VARCHAR(50),
    color VARCHAR(20),
    parent_id BIGINT,
    sort_order INT DEFAULT 0,
    status INT DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 交易流水表
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    type VARCHAR(10) NOT NULL,
    date DATE NOT NULL,
    note VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_date (date),
    INDEX idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 预算表
-- 贷款表
CREATE TABLE IF NOT EXISTS loans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    lender VARCHAR(100),
    type VARCHAR(30) NOT NULL,
    principal DECIMAL(14,2) NOT NULL,
    annual_rate DECIMAL(8,4),
    term_months INT,
    start_date DATE,
    payment_day INT,
    monthly_payment DECIMAL(14,2),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    remark VARCHAR(500),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    INDEX idx_loans_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 已签约项目表
CREATE TABLE IF NOT EXISTS projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    client VARCHAR(100),
    price DECIMAL(14,2) NOT NULL,
    deposit_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
    deposit_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    balance_amount DECIMAL(14,2) NOT NULL DEFAULT 0,
    balance_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    contract_date DATE,
    due_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    remark VARCHAR(500),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    INDEX idx_projects_user_id (user_id),
    INDEX idx_projects_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 固定资产表
CREATE TABLE IF NOT EXISTS assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    purchase_price DECIMAL(14,2) NOT NULL DEFAULT 0,
    current_value DECIMAL(14,2) NOT NULL DEFAULT 0,
    liquidatable BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    purchase_date DATE,
    remark VARCHAR(500),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    INDEX idx_assets_user_id (user_id),
    INDEX idx_assets_status (status),
    INDEX idx_assets_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 贷款还款记录表
CREATE TABLE IF NOT EXISTS loan_payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    loan_id BIGINT NOT NULL,
    amount DECIMAL(14,2) NOT NULL,
    payment_date DATE NOT NULL,
    principal_amount DECIMAL(14,2),
    interest_amount DECIMAL(14,2),
    note VARCHAR(500),
    created_at DATETIME(6) NOT NULL,
    INDEX idx_loan_payments_user_id (user_id),
    INDEX idx_loan_payments_loan_id (loan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
