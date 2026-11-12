CREATE DATABASE IF NOT EXISTS supermercado_mcp
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE supermercado_mcp;

-- 1. Tabla de Categorías
CREATE TABLE IF NOT EXISTS categoria (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Tabla de Productos
CREATE TABLE IF NOT EXISTS producto (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    precio DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    categoria_id BIGINT NOT NULL,
    version BIGINT DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_producto_categoria 
        FOREIGN KEY (categoria_id) REFERENCES categoria(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Tabla de Clientes
CREATE TABLE IF NOT EXISTS cliente (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    telefono VARCHAR(50) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Tabla de Pedidos
CREATE TABLE IF NOT EXISTS pedido (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cliente_id BIGINT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(50) NOT NULL DEFAULT 'PENDIENTE',
    total DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    version BIGINT DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_pedido_cliente 
        FOREIGN KEY (cliente_id) REFERENCES cliente(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Detalle del Pedido
CREATE TABLE IF NOT EXISTS detalle_pedido (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pedido_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_detalle_pedido 
        FOREIGN KEY (pedido_id) REFERENCES pedido(id),
    CONSTRAINT fk_detalle_producto 
        FOREIGN KEY (producto_id) REFERENCES producto(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Datos iniciales
INSERT INTO categoria (id, nombre) VALUES 
(1, 'Lácteos y Huevos'),
(2, 'Frutas y Verduras'),
(3, 'Despensa y Granos');

INSERT INTO producto (nombre, precio, stock, categoria_id, version) VALUES 
('Leche Entera 1L', 4200.00, 50, 1, 0),
('Queso Campesino 500g', 12500.00, 20, 1, 0),
('Manzana Red 1kg', 8500.00, 35, 2, 0),
('Bolsa de Arroz 1kg', 4800.00, 100, 3, 0),
('Aceite Vegetal 900ml', 11000.00, 15, 3, 0);

INSERT INTO cliente (nombre, direccion, telefono) VALUES 
('Manuel Sepúlveda', 'Calle 50 #40-20', '3001234567');