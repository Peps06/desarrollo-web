CREATE DATABASE IF NOT EXISTS bitacora_viajes CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bitacora_viajes;

CREATE TABLE usuario (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100),
  correo VARCHAR(150),
  contrasena_hash VARCHAR(100)
);
CREATE TABLE viaje (
  id INT AUTO_INCREMENT PRIMARY KEY,
  destino VARCHAR(150),
  fecha_inicio DATE,
  fecha_fin DATE,
  presupuesto_total DECIMAL(12,2),
  moneda_base CHAR(3),
  estado VARCHAR(20)
);
CREATE TABLE participante_viaje (
  id INT AUTO_INCREMENT PRIMARY KEY,
  viaje_id INT,
  usuario_id INT,
  rol VARCHAR(15)
);
CREATE TABLE actividad (
  id INT AUTO_INCREMENT PRIMARY KEY,
  viaje_id INT,
  autor_id INT,
  titulo VARCHAR(150),
  descripcion VARCHAR(500),
  dia DATE,
  hora TIME,
  costo_estimado DECIMAL(10,2)
);
CREATE TABLE voto (
  id INT AUTO_INCREMENT PRIMARY KEY,
  actividad_id INT,
  usuario_id INT
);
CREATE TABLE gasto (
  id INT AUTO_INCREMENT PRIMARY KEY,
  viaje_id INT,
  usuario_id INT,
  concepto VARCHAR(150),
  categoria VARCHAR(20),
  monto DECIMAL(12,2),
  moneda_original CHAR(3),
  monto_original DECIMAL(12,2),
  tipo_cambio DECIMAL(12,6),
  imagen_url VARCHAR(255),
  fecha DATETIME
);
CREATE TABLE tarea (
  id INT AUTO_INCREMENT PRIMARY KEY,
  viaje_id INT,
  creador_id INT,
  descripcion VARCHAR(255),
  completada TINYINT(1)
);