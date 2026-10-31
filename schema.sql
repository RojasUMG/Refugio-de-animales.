-- ============================================================
--  Huellitas · Esquema de base de datos
--  Ejecutar en MySQL: mysql -u root -p < schema.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS refugio_animales
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE refugio_animales;

-- ── Especies ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS especies (
  especie_id    INT          NOT NULL AUTO_INCREMENT,
  nombre_especie VARCHAR(50) NOT NULL,
  PRIMARY KEY (especie_id)
);

INSERT IGNORE INTO especies (nombre_especie) VALUES ('Perro'), ('Gato'), ('Otro');

-- ── Animales ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS animales (
  animal_id          INT          NOT NULL AUTO_INCREMENT,
  nombre             VARCHAR(100) NOT NULL,
  especie_id         INT          NOT NULL,
  raza               VARCHAR(100),
  fecha_de_nacimiento DATE,
  sexo               ENUM('Macho','Hembra'),
  color              VARCHAR(80),
  tamano             ENUM('Pequeño','Mediano','Grande'),
  descripcion        TEXT,
  estado_de_salud    TEXT,
  estado_de_adopcion ENUM('Disponible','En proceso','Adoptado') DEFAULT 'Disponible',
  url_de_imagen      VARCHAR(500),
  fecha_de_ingreso   DATE         NOT NULL DEFAULT (CURRENT_DATE),
  PRIMARY KEY (animal_id),
  FOREIGN KEY (especie_id) REFERENCES especies(especie_id)
);

-- ── Usuarios ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS usuarios (
  usuario_id    INT          NOT NULL AUTO_INCREMENT,
  primer_nombre VARCHAR(100) NOT NULL,
  apellido      VARCHAR(100) NOT NULL,
  email         VARCHAR(150) NOT NULL UNIQUE,
  `contraseña`  VARCHAR(255) NOT NULL,
  `teléfono`    INT,
  rol           ENUM('admin','veterinario','adoptante') DEFAULT 'adoptante',
  estado        TINYINT(1)   DEFAULT 1,
  creado_en     DATETIME     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (usuario_id)
);

-- Usuario administrador de prueba (contraseña: Admin123**)
INSERT IGNORE INTO usuarios (primer_nombre, apellido, email, `contraseña`, rol)
VALUES ('Administración', 'Sistema', 'admin@refugio.com', 'Admin123**', 'admin');

-- ── Solicitudes de adopción ───────────────────────────────
CREATE TABLE IF NOT EXISTS solicitudes_de_adopcion (
  solicitud_id       INT      NOT NULL AUTO_INCREMENT,
  solicitante_id     INT      NOT NULL,
  animal_id          INT      NOT NULL,
  fecha_de_solicitud DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  estado             ENUM('pendiente','aprobada','rechazada') DEFAULT 'pendiente',
  motivacion         TEXT,
  notas              TEXT,
  revisado_por       INT,
  revisado_el        DATETIME,
  PRIMARY KEY (solicitud_id),
  FOREIGN KEY (solicitante_id) REFERENCES usuarios(usuario_id),
  FOREIGN KEY (animal_id)      REFERENCES animales(animal_id),
  FOREIGN KEY (revisado_por)   REFERENCES usuarios(usuario_id)
);

-- ── Preferencias del adoptante ────────────────────────────
CREATE TABLE IF NOT EXISTS preferencias_del_adoptante (
  preferencia_id    INT          NOT NULL AUTO_INCREMENT,
  usuario_id        INT          NOT NULL,
  preferencia_clave VARCHAR(100) NOT NULL,
  preferencia_valor VARCHAR(255),
  PRIMARY KEY (preferencia_id),
  UNIQUE KEY uk_pref (usuario_id, preferencia_clave),
  FOREIGN KEY (usuario_id) REFERENCES usuarios(usuario_id)
);

-- ── Datos de ejemplo ──────────────────────────────────────
INSERT IGNORE INTO animales
  (nombre, especie_id, raza, fecha_de_nacimiento, sexo, color, tamano,
   descripcion, estado_de_salud, estado_de_adopcion, url_de_imagen)
VALUES
  ('Luna', 2, 'Mestiza', '2022-03-15', 'Hembra', 'Gris y blanco', 'Pequeño',
   'Luna es una gatita tranquila y cariñosa. Le encanta dormir al sol y ronronear.',
   'Saludable', 'Disponible', '/assets/mascotas/luna.png'),

  ('Canela', 1, 'Labrador', '2021-06-20', 'Hembra', 'Canela', 'Grande',
   'Canela es muy juguetona y enérgica. Ideal para familias activas con espacio.',
   'Saludable', 'Disponible', '/assets/mascotas/canela.png'),

  ('Oreo', 1, 'Dálmata', '2020-11-10', 'Macho', 'Blanco y negro', 'Grande',
   'Oreo es leal y protector. Le gustan las caminatas largas y los niños.',
   'En tratamiento por alergia leve', 'Disponible', '/assets/mascotas/oreo.png'),

  ('Michi', 2, 'Siamés', '2023-01-05', 'Macho', 'Crema y café', 'Pequeño',
   'Michi es curioso e independiente. Perfecto para apartamentos.',
   'Saludable', 'Disponible', '/assets/mascotas/michi.png'),

  ('Sol', 1, 'Golden Retriever', '2019-08-30', 'Macho', 'Dorado', 'Grande',
   'Sol es el más amigable del refugio. Ha estado enfermo pero se está recuperando.',
   'Enfermo — en observación veterinaria', 'Disponible', '/assets/mascotas/sol.png'),

  ('Tortuga', 3, 'Tortuga de tierra', '2018-04-12', 'Hembra', 'Verde y café', 'Pequeño',
   'Tortuga es un animal especial y de larga vida. Requiere cuidados específicos.',
   'Saludable', 'Disponible', '/assets/mascotas/tortuga.png');
