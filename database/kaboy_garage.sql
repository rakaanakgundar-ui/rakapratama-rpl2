-- =========================================================
--  Database  : kaboy_garage
--  Aplikasi  : Kaboy Garage (Desktop Swing)
--  Login     : admin / admin123
-- =========================================================

CREATE DATABASE IF NOT EXISTS kaboy_garage
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE kaboy_garage;

-- ---------------------------------------------------------
-- Admin
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS admin (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    nama          VARCHAR(100) NOT NULL,
    no_hp         VARCHAR(20)  NOT NULL,
    username      VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(64)  NOT NULL,
    aktif         BIT(1)       NOT NULL DEFAULT b'1',
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_username (username)
) ENGINE = InnoDB;

-- ---------------------------------------------------------
-- Pelanggan (tanpa akun/login)
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS pelanggan (
    id     BIGINT       NOT NULL AUTO_INCREMENT,
    nama   VARCHAR(100) NOT NULL,
    no_hp  VARCHAR(20)  NOT NULL,
    email  VARCHAR(100) NULL,
    alamat VARCHAR(255) NULL,
    PRIMARY KEY (id),
    KEY idx_pelanggan_no_hp (no_hp)
) ENGINE = InnoDB;

-- ---------------------------------------------------------
-- Kendaraan
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS kendaraan (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    pelanggan_id BIGINT      NOT NULL,
    merk         VARCHAR(50) NOT NULL,
    model        VARCHAR(50) NOT NULL,
    plat_nomor   VARCHAR(15) NOT NULL,
    tahun        INT         NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_kendaraan_plat (plat_nomor),
    CONSTRAINT fk_kendaraan_pelanggan
        FOREIGN KEY (pelanggan_id) REFERENCES pelanggan (id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------
-- Spare part
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS spare_part (
    id           BIGINT         NOT NULL AUTO_INCREMENT,
    kode         VARCHAR(20)    NOT NULL,
    nama         VARCHAR(100)   NOT NULL,
    merk         VARCHAR(50)    NULL,
    kategori     VARCHAR(20)    NOT NULL,
    harga        DECIMAL(12, 2) NOT NULL,
    stok         INT            NOT NULL,
    stok_minimal INT            NOT NULL DEFAULT 3,
    deskripsi    VARCHAR(255)   NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_spare_part_kode (kode)
) ENGINE = InnoDB;

-- ---------------------------------------------------------
-- Pesanan
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS pesanan (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    kode           VARCHAR(30)  NOT NULL,
    pelanggan_id   BIGINT       NOT NULL,
    tanggal_dibuat DATETIME(6)  NOT NULL,
    status         VARCHAR(20)  NOT NULL,
    catatan        VARCHAR(255) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pesanan_kode (kode),
    CONSTRAINT fk_pesanan_pelanggan
        FOREIGN KEY (pelanggan_id) REFERENCES pelanggan (id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------
-- Detail pesanan
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS detail_pesanan (
    id            BIGINT         NOT NULL AUTO_INCREMENT,
    pesanan_id    BIGINT         NOT NULL,
    spare_part_id BIGINT         NOT NULL,
    jumlah        INT            NOT NULL,
    harga_satuan  DECIMAL(12, 2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_detail_pesanan
        FOREIGN KEY (pesanan_id) REFERENCES pesanan (id) ON DELETE CASCADE,
    CONSTRAINT fk_detail_spare_part
        FOREIGN KEY (spare_part_id) REFERENCES spare_part (id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------
-- Pendaftaran servis (waiting list)
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS pendaftaran_servis (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    kode_antrean     VARCHAR(30)  NOT NULL,
    nomor_antrean    INT          NOT NULL,
    pelanggan_id     BIGINT       NOT NULL,
    kendaraan_id     BIGINT       NOT NULL,
    jenis_servis     VARCHAR(30)  NOT NULL,
    keluhan          VARCHAR(255) NOT NULL,
    tanggal_diajukan DATETIME(6)  NOT NULL,
    jadwal_servis    DATETIME(6)  NULL,
    status           VARCHAR(20)  NOT NULL,
    catatan_admin    VARCHAR(255) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pendaftaran_kode (kode_antrean),
    CONSTRAINT fk_pendaftaran_pelanggan
        FOREIGN KEY (pelanggan_id) REFERENCES pelanggan (id),
    CONSTRAINT fk_pendaftaran_kendaraan
        FOREIGN KEY (kendaraan_id) REFERENCES kendaraan (id)
) ENGINE = InnoDB;

-- =========================================================
-- Data awal
-- Password admin: admin123 (SHA-256)
-- =========================================================
INSERT IGNORE INTO admin (nama, no_hp, username, password_hash, aktif)
VALUES ('Admin Kaboy', '081234567890', 'admin',
        '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', b'1');

INSERT IGNORE INTO spare_part (kode, nama, merk, kategori, harga, stok, stok_minimal, deskripsi)
VALUES ('SP-001', 'Oli Mesin MPX2', 'AHM', 'OLI_CAIRAN', 48000.00, 20, 5,
        'Oli mesin matic 0.8 liter'),
       ('SP-002', 'Kampas Rem Depan', 'Aspira', 'MESIN', 35000.00, 10, 3,
        'Kampas rem depan untuk motor matic'),
       ('SP-003', 'Busi Iridium', 'NGK', 'KELISTRIKAN', 65000.00, 15, 3,
        'Busi iridium performa stabil'),
       ('SP-004', 'Filter Udara', 'Sakura', 'MESIN', 42000.00, 8, 3,
        'Filter udara standar'),
       ('SP-005', 'Lampu LED Depan', 'Autovision', 'KELISTRIKAN', 125000.00, 6, 2,
        'Lampu LED hemat daya');

-- Data contoh pelanggan dan kendaraan agar GUI langsung bisa dicoba.
INSERT IGNORE INTO pelanggan (id, nama, no_hp, email, alamat)
VALUES (1, 'Budi Santoso', '081234567001', 'budi@example.com',
        'Jl. Melati No. 12, Depok'),
       (2, 'Siti Rahma', '081234567002', 'siti@example.com',
        'Jl. Kenanga No. 8, Jakarta'),
       (3, 'Agus Pratama', '081234567003', NULL,
        'Jl. Mawar No. 21, Bekasi');

INSERT IGNORE INTO kendaraan (id, pelanggan_id, merk, model, plat_nomor, tahun)
VALUES (1, 1, 'Honda', 'Vario 125', 'B 1234 KGY', 2021),
       (2, 2, 'Yamaha', 'NMAX', 'B 5678 KGY', 2022),
       (3, 3, 'Honda', 'Beat', 'B 9012 KGY', 2020);
