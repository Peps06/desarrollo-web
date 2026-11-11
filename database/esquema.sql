-- ============================================================================
-- Bitácora de viajes colaborativa (MySQL 8.0.16 o superior)
-- Referencia del diagrama ER. Hibernate crea las tablas con
-- ddl-auto=update; este archivo sirve para comparar con DESCRIBE.
-- ============================================================================

CREATE DATABASE IF NOT EXISTS bitacora_viajes
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bitacora_viajes;

CREATE TABLE usuario (
    id              INT          NOT NULL AUTO_INCREMENT,
    nombre          VARCHAR(100) NOT NULL,
    correo          VARCHAR(150) NOT NULL,
    contrasena_hash VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_usuario_correo UNIQUE (correo)
) ENGINE=InnoDB;

CREATE TABLE viaje (
    id                INT           NOT NULL AUTO_INCREMENT,
    destino           VARCHAR(150)  NOT NULL,
    fecha_inicio      DATE          NOT NULL,
    fecha_fin         DATE          NOT NULL,
    presupuesto_total DECIMAL(12,2) NOT NULL,
    moneda_base       CHAR(3)       NOT NULL DEFAULT 'MXN',
    estado            ENUM('ACTIVO','CANCELADO') NOT NULL DEFAULT 'ACTIVO',
    PRIMARY KEY (id),
    CONSTRAINT ck_fechas      CHECK (fecha_fin >= fecha_inicio),
    CONSTRAINT ck_presupuesto CHECK (presupuesto_total > 0)
) ENGINE=InnoDB;

CREATE TABLE participante_viaje (
    id         INT NOT NULL AUTO_INCREMENT,
    viaje_id   INT NOT NULL,
    usuario_id INT NOT NULL,
    rol        ENUM('ORGANIZADOR','PARTICIPANTE') NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_viaje_usuario UNIQUE (viaje_id, usuario_id),
    CONSTRAINT fk_participante_viaje   FOREIGN KEY (viaje_id)   REFERENCES viaje (id)   ON DELETE RESTRICT,
    CONSTRAINT fk_participante_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE actividad (
    id             INT           NOT NULL AUTO_INCREMENT,
    viaje_id       INT           NOT NULL,
    autor_id       INT           NOT NULL,
    titulo         VARCHAR(150)  NOT NULL,
    descripcion    VARCHAR(500)  NULL,
    dia            DATE          NOT NULL,
    hora           TIME          NOT NULL,
    costo_estimado DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    fecha_creacion DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT ck_costo CHECK (costo_estimado >= 0),
    CONSTRAINT fk_actividad_viaje FOREIGN KEY (viaje_id) REFERENCES viaje (id)   ON DELETE RESTRICT,
    CONSTRAINT fk_actividad_autor FOREIGN KEY (autor_id) REFERENCES usuario (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE voto (
    id           INT NOT NULL AUTO_INCREMENT,
    actividad_id INT NOT NULL,
    usuario_id   INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_voto UNIQUE (actividad_id, usuario_id),
    CONSTRAINT fk_voto_actividad FOREIGN KEY (actividad_id) REFERENCES actividad (id) ON DELETE CASCADE,
    CONSTRAINT fk_voto_usuario   FOREIGN KEY (usuario_id)   REFERENCES usuario (id)   ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE gasto (
    id              INT           NOT NULL AUTO_INCREMENT,
    viaje_id        INT           NOT NULL,
    usuario_id      INT           NOT NULL,
    concepto        VARCHAR(150)  NOT NULL,
    categoria       ENUM('COMIDA','TRANSPORTE','HOSPEDAJE','ACTIVIDADES','OTROS') NOT NULL,
    monto           DECIMAL(12,2) NOT NULL,
    moneda_original CHAR(3)       NOT NULL,
    monto_original  DECIMAL(12,2) NOT NULL,
    tipo_cambio     DECIMAL(12,6) NOT NULL DEFAULT 1,
    imagen_url      VARCHAR(255)  NULL,
    fecha           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT ck_monto CHECK (monto > 0),
    CONSTRAINT fk_gasto_viaje   FOREIGN KEY (viaje_id)   REFERENCES viaje (id)   ON DELETE RESTRICT,
    CONSTRAINT fk_gasto_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE tarea (
    id          INT          NOT NULL AUTO_INCREMENT,
    viaje_id    INT          NOT NULL,
    creador_id  INT          NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    completada  TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_tarea_viaje   FOREIGN KEY (viaje_id)   REFERENCES viaje (id)   ON DELETE RESTRICT,
    CONSTRAINT fk_tarea_creador FOREIGN KEY (creador_id) REFERENCES usuario (id) ON DELETE RESTRICT
) ENGINE=InnoDB;