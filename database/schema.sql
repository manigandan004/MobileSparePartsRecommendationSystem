CREATE DATABASE IF NOT EXISTS mobile_spare_parts;
USE mobile_spare_parts;

CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS spare_parts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    part_name VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    price DECIMAL(10,2) NOT NULL,
    CHECK (quantity >= 0),
    CHECK (price >= 0)
);

INSERT INTO spare_parts (part_name, model, quantity, price) VALUES
('Display Assembly', 'iPhone 15', 10, 8500.00),
('Battery', 'Samsung S24', 15, 4200.00),
('Charging Port', 'OnePlus 12', 12, 1800.00),
('Back Glass', 'iPhone 14', 8, 3500.00);
