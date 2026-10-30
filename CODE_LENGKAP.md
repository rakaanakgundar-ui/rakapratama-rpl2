# Kode Lengkap Kaboy Garage (Desktop Swing + MySQL)

Dokumen ini dihasilkan dari source terbaru aplikasi desktop.

- **Nama** : Raka Pratama Putra Sodiq
- **NPM** : 51423218
- **Kelas** : 4IA24

Kompilasi dari root project:

```bat
dir /s /b src\*.java > sources.txt
javac --release 11 -encoding UTF-8 -cp lib\mysql-connector-j-8.3.0.jar -d build\classes @sources.txt
java -cp "build\classes;lib\mysql-connector-j-8.3.0.jar" kaboygarage.gui.GuiMain
```

---

## src/kaboygarage/dao/AdminDao.java

```java
package kaboygarage.dao;

import kaboygarage.db.DataAccessException;
import kaboygarage.db.Database;
import kaboygarage.model.Admin;
import kaboygarage.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO untuk tabel admin dan proses login.
 */
public class AdminDao {

    private static final String KOLOM =
            "id, nama, no_hp, username, password_hash, aktif";

    /**
     * Mengembalikan Admin jika username dan password cocok, atau null jika salah.
     */
    public Admin login(String username, String password) {
        Admin admin = findByUsername(username);
        if (admin == null || !admin.isAktif()) {
            return null;
        }
        if (!PasswordUtil.cocok(password, admin.getPasswordHash())) {
            return null;
        }
        return admin;
    }

    public Admin findByUsername(String username) {
        String sql = "SELECT " + KOLOM + " FROM admin WHERE username = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Gagal mencari admin: " + e.getMessage(), e);
        }
    }

    private Admin map(ResultSet resultSet) throws SQLException {
        Admin admin = new Admin(
                resultSet.getString("nama"),
                resultSet.getString("no_hp"),
                resultSet.getString("username"),
                resultSet.getString("password_hash"));
        admin.setId(resultSet.getLong("id"));
        admin.setAktif(resultSet.getBoolean("aktif"));
        return admin;
    }
}
```

## src/kaboygarage/dao/KendaraanDao.java

```java
package kaboygarage.dao;

import kaboygarage.db.DataAccessException;
import kaboygarage.db.Database;
import kaboygarage.model.Kendaraan;
import kaboygarage.model.Pelanggan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO untuk tabel kendaraan.
 */
public class KendaraanDao {

    private static final String SELECT_JOIN =
            "SELECT k.id, k.merk, k.model, k.plat_nomor, k.tahun, "
            + "p.id AS pelanggan_id, p.nama, p.no_hp, p.email, p.alamat "
            + "FROM kendaraan k JOIN pelanggan p ON p.id = k.pelanggan_id ";

    public List<Kendaraan> findAll() {
        String sql = SELECT_JOIN + "ORDER BY k.plat_nomor";
        List<Kendaraan> hasil = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                hasil.add(map(resultSet));
            }
            return hasil;
        } catch (SQLException e) {
            throw new DataAccessException("Gagal membaca data kendaraan: " + e.getMessage(), e);
        }
    }

    public List<Kendaraan> findByPelanggan(Long pelangganId) {
        String sql = SELECT_JOIN + "WHERE k.pelanggan_id = ? ORDER BY k.plat_nomor";
        List<Kendaraan> hasil = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, pelangganId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    hasil.add(map(resultSet));
                }
            }
            return hasil;
        } catch (SQLException e) {
            throw new DataAccessException("Gagal membaca kendaraan pelanggan: "
                    + e.getMessage(), e);
        }
    }

    public Kendaraan findById(Long id) {
        String sql = SELECT_JOIN + "WHERE k.id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Gagal mencari kendaraan: " + e.getMessage(), e);
        }
    }

    public void insert(Kendaraan kendaraan) {
        String sql = "INSERT INTO kendaraan (pelanggan_id, merk, model, plat_nomor, tahun) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, kendaraan.getPelanggan().getId());
            statement.setString(2, kendaraan.getMerk());
            statement.setString(3, kendaraan.getModel());
            statement.setString(4, kendaraan.getPlatNomor());
            statement.setInt(5, kendaraan.getTahun());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    kendaraan.setId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menyimpan kendaraan: " + e.getMessage(), e);
        }
    }

    public void update(Kendaraan kendaraan) {
        String sql = "UPDATE kendaraan SET pelanggan_id = ?, merk = ?, model = ?, "
                + "plat_nomor = ?, tahun = ? WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, kendaraan.getPelanggan().getId());
            statement.setString(2, kendaraan.getMerk());
            statement.setString(3, kendaraan.getModel());
            statement.setString(4, kendaraan.getPlatNomor());
            statement.setInt(5, kendaraan.getTahun());
            statement.setLong(6, kendaraan.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Gagal mengubah kendaraan: " + e.getMessage(), e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM kendaraan WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(
                    "Kendaraan tidak bisa dihapus karena masih dipakai di pendaftaran servis.", e);
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM kendaraan";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menghitung kendaraan: " + e.getMessage(), e);
        }
    }

    private Kendaraan map(ResultSet resultSet) throws SQLException {
        Pelanggan pelanggan = new Pelanggan(
                resultSet.getString("nama"),
                resultSet.getString("no_hp"),
                resultSet.getString("email"),
                resultSet.getString("alamat"));
        pelanggan.setId(resultSet.getLong("pelanggan_id"));

        Kendaraan kendaraan = new Kendaraan(
                pelanggan,
                resultSet.getString("merk"),
                resultSet.getString("model"),
                resultSet.getString("plat_nomor"),
                resultSet.getInt("tahun"));
        kendaraan.setId(resultSet.getLong("id"));
        return kendaraan;
    }
}
```

## src/kaboygarage/dao/PelangganDao.java

```java
package kaboygarage.dao;

import kaboygarage.db.DataAccessException;
import kaboygarage.db.Database;
import kaboygarage.model.Pelanggan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO untuk tabel pelanggan.
 */
public class PelangganDao {

    private static final String KOLOM = "id, nama, no_hp, email, alamat";

    public List<Pelanggan> findAll() {
        String sql = "SELECT " + KOLOM + " FROM pelanggan ORDER BY nama";
        List<Pelanggan> hasil = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                hasil.add(map(resultSet));
            }
            return hasil;
        } catch (SQLException e) {
            throw new DataAccessException("Gagal membaca data pelanggan: " + e.getMessage(), e);
        }
    }

    public Pelanggan findById(Long id) {
        String sql = "SELECT " + KOLOM + " FROM pelanggan WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Gagal mencari pelanggan: " + e.getMessage(), e);
        }
    }

    public void insert(Pelanggan pelanggan) {
        String sql = "INSERT INTO pelanggan (nama, no_hp, email, alamat) VALUES (?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, pelanggan.getNama());
            statement.setString(2, pelanggan.getNoHp());
            statement.setString(3, pelanggan.getEmail());
            statement.setString(4, pelanggan.getAlamat());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    pelanggan.setId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menyimpan pelanggan: " + e.getMessage(), e);
        }
    }

    public void update(Pelanggan pelanggan) {
        String sql = "UPDATE pelanggan SET nama = ?, no_hp = ?, email = ?, alamat = ? "
                + "WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pelanggan.getNama());
            statement.setString(2, pelanggan.getNoHp());
            statement.setString(3, pelanggan.getEmail());
            statement.setString(4, pelanggan.getAlamat());
            statement.setLong(5, pelanggan.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Gagal mengubah pelanggan: " + e.getMessage(), e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM pelanggan WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(
                    "Pelanggan tidak bisa dihapus karena masih memiliki kendaraan, "
                    + "pesanan, atau pendaftaran servis.", e);
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM pelanggan";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menghitung pelanggan: " + e.getMessage(), e);
        }
    }

    private Pelanggan map(ResultSet resultSet) throws SQLException {
        Pelanggan pelanggan = new Pelanggan(
                resultSet.getString("nama"),
                resultSet.getString("no_hp"),
                resultSet.getString("email"),
                resultSet.getString("alamat"));
        pelanggan.setId(resultSet.getLong("id"));
        return pelanggan;
    }
}
```

## src/kaboygarage/dao/PendaftaranServisDao.java

```java
package kaboygarage.dao;

import kaboygarage.db.DataAccessException;
import kaboygarage.db.Database;
import kaboygarage.model.JenisServis;
import kaboygarage.model.Kendaraan;
import kaboygarage.model.Pelanggan;
import kaboygarage.model.PendaftaranServis;
import kaboygarage.model.StatusServis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO untuk tabel pendaftaran_servis (waiting list).
 */
public class PendaftaranServisDao {

    private static final String SELECT_JOIN =
            "SELECT s.id, s.kode_antrean, s.nomor_antrean, s.jenis_servis, s.keluhan, "
            + "s.tanggal_diajukan, s.jadwal_servis, s.status, s.catatan_admin, "
            + "p.id AS pl_id, p.nama AS pl_nama, p.no_hp AS pl_no_hp, "
            + "p.email AS pl_email, p.alamat AS pl_alamat, "
            + "k.id AS k_id, k.merk, k.model, k.plat_nomor, k.tahun "
            + "FROM pendaftaran_servis s "
            + "JOIN pelanggan p ON p.id = s.pelanggan_id "
            + "JOIN kendaraan k ON k.id = s.kendaraan_id ";

    public List<PendaftaranServis> findAll() {
        String sql = SELECT_JOIN + "ORDER BY s.tanggal_diajukan DESC, s.id DESC";
        List<PendaftaranServis> hasil = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                hasil.add(map(resultSet));
            }
            return hasil;
        } catch (SQLException e) {
            throw new DataAccessException("Gagal membaca waiting list: " + e.getMessage(), e);
        }
    }

    public PendaftaranServis findById(Long id) {
        String sql = SELECT_JOIN + "WHERE s.id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Gagal mencari pendaftaran servis: "
                    + e.getMessage(), e);
        }
    }

    public void insert(PendaftaranServis pendaftaran) {
        String sql = "INSERT INTO pendaftaran_servis (kode_antrean, nomor_antrean, "
                + "pelanggan_id, kendaraan_id, jenis_servis, keluhan, tanggal_diajukan, "
                + "jadwal_servis, status, catatan_admin) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, pendaftaran.getKodeAntrean());
            statement.setInt(2, pendaftaran.getNomorAntrean());
            statement.setLong(3, pendaftaran.getPelanggan().getId());
            statement.setLong(4, pendaftaran.getKendaraan().getId());
            statement.setString(5, pendaftaran.getJenisServis().name());
            statement.setString(6, pendaftaran.getKeluhan());
            statement.setTimestamp(7, Timestamp.valueOf(pendaftaran.getTanggalDiajukan()));
            statement.setTimestamp(8, pendaftaran.getJadwalServis() == null
                    ? null : Timestamp.valueOf(pendaftaran.getJadwalServis()));
            statement.setString(9, pendaftaran.getStatus().name());
            statement.setString(10, pendaftaran.getCatatanAdmin());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    pendaftaran.setId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menyimpan pendaftaran servis: "
                    + e.getMessage(), e);
        }
    }

    public void updateJadwal(Long id, LocalDateTime jadwal, StatusServis status,
                             String catatanAdmin) {
        String sql = "UPDATE pendaftaran_servis SET jadwal_servis = ?, status = ?, "
                + "catatan_admin = ? WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, jadwal == null ? null : Timestamp.valueOf(jadwal));
            statement.setString(2, status.name());
            statement.setString(3, catatanAdmin);
            statement.setLong(4, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menyimpan jadwal servis: "
                    + e.getMessage(), e);
        }
    }

    public void updateStatus(Long id, StatusServis status) {
        String sql = "UPDATE pendaftaran_servis SET status = ? WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setLong(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Gagal mengubah status servis: "
                    + e.getMessage(), e);
        }
    }

    public int countAktif() {
        String sql = "SELECT COUNT(*) FROM pendaftaran_servis "
                + "WHERE status NOT IN ('SELESAI', 'DIBATALKAN')";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menghitung antrean servis: "
                    + e.getMessage(), e);
        }
    }

    /**
     * Nomor antrean dimulai dari 1 setiap hari.
     */
    public int nextNomorAntrean() {
        String sql = "SELECT COALESCE(MAX(nomor_antrean), 0) + 1 FROM pendaftaran_servis "
                + "WHERE DATE(tanggal_diajukan) = CURDATE()";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            throw new DataAccessException("Gagal membuat nomor antrean: "
                    + e.getMessage(), e);
        }
    }

    private PendaftaranServis map(ResultSet resultSet) throws SQLException {
        Pelanggan pelanggan = new Pelanggan(
                resultSet.getString("pl_nama"),
                resultSet.getString("pl_no_hp"),
                resultSet.getString("pl_email"),
                resultSet.getString("pl_alamat"));
        pelanggan.setId(resultSet.getLong("pl_id"));

        Kendaraan kendaraan = new Kendaraan(
                pelanggan,
                resultSet.getString("merk"),
                resultSet.getString("model"),
                resultSet.getString("plat_nomor"),
                resultSet.getInt("tahun"));
        kendaraan.setId(resultSet.getLong("k_id"));

        PendaftaranServis pendaftaran = new PendaftaranServis(
                resultSet.getString("kode_antrean"),
                resultSet.getInt("nomor_antrean"),
                pelanggan,
                kendaraan,
                JenisServis.valueOf(resultSet.getString("jenis_servis")),
                resultSet.getString("keluhan"));
        pendaftaran.setId(resultSet.getLong("id"));
        pendaftaran.setTanggalDiajukan(
                resultSet.getTimestamp("tanggal_diajukan").toLocalDateTime());
        Timestamp jadwal = resultSet.getTimestamp("jadwal_servis");
        pendaftaran.setJadwalServis(jadwal == null ? null : jadwal.toLocalDateTime());
        pendaftaran.setStatus(StatusServis.valueOf(resultSet.getString("status")));
        pendaftaran.setCatatanAdmin(resultSet.getString("catatan_admin"));
        return pendaftaran;
    }
}
```

## src/kaboygarage/dao/PesananDao.java

```java
package kaboygarage.dao;

import kaboygarage.db.DataAccessException;
import kaboygarage.db.Database;
import kaboygarage.model.DetailPesanan;
import kaboygarage.model.KategoriSparePart;
import kaboygarage.model.Pelanggan;
import kaboygarage.model.Pesanan;
import kaboygarage.model.SparePart;
import kaboygarage.model.StatusPesanan;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO untuk tabel pesanan dan detail_pesanan.
 */
public class PesananDao {

    private static final String SELECT_JOIN =
            "SELECT p.id AS p_id, p.kode, p.tanggal_dibuat, p.status, p.catatan, "
            + "pl.id AS pl_id, pl.nama AS pl_nama, pl.no_hp AS pl_no_hp, "
            + "pl.email AS pl_email, pl.alamat AS pl_alamat, "
            + "d.id AS d_id, d.jumlah, d.harga_satuan, "
            + "sp.id AS sp_id, sp.kode AS sp_kode, sp.nama AS sp_nama, sp.merk AS sp_merk, "
            + "sp.kategori AS sp_kategori, sp.harga AS sp_harga, sp.stok AS sp_stok, "
            + "sp.stok_minimal AS sp_stok_minimal, sp.deskripsi AS sp_deskripsi "
            + "FROM pesanan p "
            + "JOIN pelanggan pl ON pl.id = p.pelanggan_id "
            + "LEFT JOIN detail_pesanan d ON d.pesanan_id = p.id "
            + "LEFT JOIN spare_part sp ON sp.id = d.spare_part_id ";

    public List<Pesanan> findAll() {
        return query(SELECT_JOIN + "ORDER BY p.tanggal_dibuat DESC, p.id DESC, d.id", null);
    }

    public Pesanan findById(Long id) {
        List<Pesanan> hasil = query(SELECT_JOIN + "WHERE p.id = ?", id);
        return hasil.isEmpty() ? null : hasil.get(0);
    }

    /**
     * Menyimpan pesanan beserta detailnya. Harus dipanggil di dalam transaksi
     * agar stok dan pesanan tetap konsisten.
     */
    public void insert(Connection connection, Pesanan pesanan) throws SQLException {
        String sql = "INSERT INTO pesanan (kode, pelanggan_id, tanggal_dibuat, status, catatan) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, pesanan.getKode());
            statement.setLong(2, pesanan.getPelanggan().getId());
            statement.setTimestamp(3, Timestamp.valueOf(pesanan.getTanggalDibuat()));
            statement.setString(4, pesanan.getStatus().name());
            statement.setString(5, pesanan.getCatatan());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    pesanan.setId(keys.getLong(1));
                }
            }
        }

        String detailSql = "INSERT INTO detail_pesanan "
                + "(pesanan_id, spare_part_id, jumlah, harga_satuan) VALUES (?, ?, ?, ?)";
        for (DetailPesanan detail : pesanan.getDetail()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    detailSql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setLong(1, pesanan.getId());
                statement.setLong(2, detail.getSparePart().getId());
                statement.setInt(3, detail.getJumlah());
                statement.setBigDecimal(4, detail.getHargaSatuan());
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        detail.setId(keys.getLong(1));
                    }
                }
            }
        }
    }

    public void updateStatus(Long id, StatusPesanan status) {
        try (Connection connection = Database.getConnection()) {
            updateStatus(connection, id, status);
        } catch (SQLException e) {
            throw new DataAccessException("Gagal mengubah status pesanan: "
                    + e.getMessage(), e);
        }
    }

    public void updateStatus(Connection connection, Long id, StatusPesanan status)
            throws SQLException {
        String sql = "UPDATE pesanan SET status = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setLong(2, id);
            statement.executeUpdate();
        }
    }

    public int countAktif() {
        String sql = "SELECT COUNT(*) FROM pesanan "
                + "WHERE status NOT IN ('SELESAI', 'DIBATALKAN')";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menghitung pesanan aktif: "
                    + e.getMessage(), e);
        }
    }

    private List<Pesanan> query(String sql, Long id) {
        Map<Long, Pesanan> map = new LinkedHashMap<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (id != null) {
                statement.setLong(1, id);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    long pesananId = resultSet.getLong("p_id");
                    Pesanan pesanan = map.get(pesananId);
                    if (pesanan == null) {
                        Pelanggan pelanggan = new Pelanggan(
                                resultSet.getString("pl_nama"),
                                resultSet.getString("pl_no_hp"),
                                resultSet.getString("pl_email"),
                                resultSet.getString("pl_alamat"));
                        pelanggan.setId(resultSet.getLong("pl_id"));

                        pesanan = new Pesanan(
                                resultSet.getString("kode"),
                                pelanggan,
                                resultSet.getTimestamp("tanggal_dibuat").toLocalDateTime());
                        pesanan.setId(pesananId);
                        pesanan.setStatus(StatusPesanan.valueOf(resultSet.getString("status")));
                        pesanan.setCatatan(resultSet.getString("catatan"));
                        map.put(pesananId, pesanan);
                    }

                    long detailId = resultSet.getLong("d_id");
                    if (!resultSet.wasNull()) {
                        SparePart sparePart = new SparePart(
                                resultSet.getString("sp_kode"),
                                resultSet.getString("sp_nama"),
                                resultSet.getString("sp_merk"),
                                KategoriSparePart.valueOf(resultSet.getString("sp_kategori")),
                                resultSet.getBigDecimal("sp_harga"),
                                resultSet.getInt("sp_stok"));
                        sparePart.setId(resultSet.getLong("sp_id"));
                        sparePart.setStokMinimal(resultSet.getInt("sp_stok_minimal"));
                        sparePart.setDeskripsi(resultSet.getString("sp_deskripsi"));

                        BigDecimal hargaSatuan = resultSet.getBigDecimal("harga_satuan");
                        pesanan.tambahDetail(sparePart, resultSet.getInt("jumlah"), hargaSatuan);
                    }
                }
            }
            return new ArrayList<>(map.values());
        } catch (SQLException e) {
            throw new DataAccessException("Gagal membaca pesanan: " + e.getMessage(), e);
        }
    }
}
```

## src/kaboygarage/dao/SparePartDao.java

```java
package kaboygarage.dao;

import kaboygarage.db.DataAccessException;
import kaboygarage.db.Database;
import kaboygarage.model.KategoriSparePart;
import kaboygarage.model.SparePart;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO untuk tabel spare_part.
 */
public class SparePartDao {

    private static final String KOLOM =
            "id, kode, nama, merk, kategori, harga, stok, stok_minimal, deskripsi";

    public List<SparePart> findAll() {
        String sql = "SELECT " + KOLOM + " FROM spare_part ORDER BY kode";
        List<SparePart> hasil = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                hasil.add(map(resultSet));
            }
            return hasil;
        } catch (SQLException e) {
            throw new DataAccessException("Gagal membaca spare part: " + e.getMessage(), e);
        }
    }

    public List<SparePart> findByKeyword(String keyword) {
        String sql = "SELECT " + KOLOM + " FROM spare_part "
                + "WHERE LOWER(kode) LIKE ? OR LOWER(nama) LIKE ? OR LOWER(merk) LIKE ? "
                + "ORDER BY kode";
        String pola = "%" + (keyword == null ? "" : keyword.toLowerCase()) + "%";
        List<SparePart> hasil = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pola);
            statement.setString(2, pola);
            statement.setString(3, pola);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    hasil.add(map(resultSet));
                }
            }
            return hasil;
        } catch (SQLException e) {
            throw new DataAccessException("Gagal mencari spare part: " + e.getMessage(), e);
        }
    }

    public SparePart findById(Long id) {
        String sql = "SELECT " + KOLOM + " FROM spare_part WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Gagal mencari spare part: " + e.getMessage(), e);
        }
    }

    public void insert(SparePart sparePart) {
        String sql = "INSERT INTO spare_part (kode, nama, merk, kategori, harga, stok, "
                + "stok_minimal, deskripsi) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            isiStatement(statement, sparePart);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    sparePart.setId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menyimpan spare part: " + e.getMessage(), e);
        }
    }

    public void update(SparePart sparePart) {
        String sql = "UPDATE spare_part SET kode = ?, nama = ?, merk = ?, kategori = ?, "
                + "harga = ?, stok = ?, stok_minimal = ?, deskripsi = ? WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            isiStatement(statement, sparePart);
            statement.setLong(9, sparePart.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Gagal mengubah spare part: " + e.getMessage(), e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM spare_part WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(
                    "Spare part tidak bisa dihapus karena sudah dipakai pada pesanan.", e);
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM spare_part";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menghitung spare part: " + e.getMessage(), e);
        }
    }

    public int countMenipis() {
        String sql = "SELECT COUNT(*) FROM spare_part WHERE stok <= stok_minimal";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        } catch (SQLException e) {
            throw new DataAccessException("Gagal menghitung stok menipis: " + e.getMessage(), e);
        }
    }

    public List<SparePart> findMenipis() {
        String sql = "SELECT " + KOLOM + " FROM spare_part "
                + "WHERE stok <= stok_minimal ORDER BY stok, kode";
        List<SparePart> hasil = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                hasil.add(map(resultSet));
            }
            return hasil;
        } catch (SQLException e) {
            throw new DataAccessException("Gagal membaca stok menipis: " + e.getMessage(), e);
        }
    }

    /**
     * Menghasilkan kode berikutnya, misalnya SP-006.
     */
    public String nextKode() {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(kode, 4) AS UNSIGNED)), 0) + 1 "
                + "FROM spare_part WHERE kode LIKE 'SP-%'";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return String.format("SP-%03d", resultSet.getInt(1));
        } catch (SQLException e) {
            throw new DataAccessException("Gagal membuat kode spare part: " + e.getMessage(), e);
        }
    }

    /**
     * Mengurangi stok secara atomik. Mengembalikan false bila stok tidak cukup.
     */
    public boolean kurangiStok(Connection connection, Long sparePartId, int jumlah)
            throws SQLException {
        String sql = "UPDATE spare_part SET stok = stok - ? WHERE id = ? AND stok >= ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, jumlah);
            statement.setLong(2, sparePartId);
            statement.setInt(3, jumlah);
            return statement.executeUpdate() > 0;
        }
    }

    public void tambahStok(Connection connection, Long sparePartId, int jumlah)
            throws SQLException {
        String sql = "UPDATE spare_part SET stok = stok + ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, jumlah);
            statement.setLong(2, sparePartId);
            statement.executeUpdate();
        }
    }

    private void isiStatement(PreparedStatement statement, SparePart sparePart)
            throws SQLException {
        statement.setString(1, sparePart.getKode());
        statement.setString(2, sparePart.getNama());
        statement.setString(3, sparePart.getMerk());
        statement.setString(4, sparePart.getKategori().name());
        statement.setBigDecimal(5, sparePart.getHarga());
        statement.setInt(6, sparePart.getStok());
        statement.setInt(7, sparePart.getStokMinimal());
        statement.setString(8, sparePart.getDeskripsi());
    }

    private SparePart map(ResultSet resultSet) throws SQLException {
        SparePart sparePart = new SparePart(
                resultSet.getString("kode"),
                resultSet.getString("nama"),
                resultSet.getString("merk"),
                KategoriSparePart.valueOf(resultSet.getString("kategori")),
                resultSet.getBigDecimal("harga"),
                resultSet.getInt("stok"));
        sparePart.setId(resultSet.getLong("id"));
        sparePart.setStokMinimal(resultSet.getInt("stok_minimal"));
        sparePart.setDeskripsi(resultSet.getString("deskripsi"));
        return sparePart;
    }
}
```

## src/kaboygarage/db/DataAccessException.java

```java
package kaboygarage.db;

/**
 * Exception untuk kesalahan akses database (koneksi, query, constraint).
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

## src/kaboygarage/db/Database.java

```java
package kaboygarage.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Pengelola koneksi JDBC ke MySQL/MariaDB XAMPP.
 *
 * <p>Konfigurasi dibaca dari file db.properties di folder project. Jika file
 * tidak ditemukan, dipakai nilai default XAMPP (root tanpa password).</p>
 */
public final class Database {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/kaboy_garage"
            + "?useSSL=false&allowPublicKeyRetrieval=true"
            + "&serverTimezone=Asia/Jakarta&characterEncoding=UTF-8";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties properties = new Properties();
        properties.setProperty("db.url", DEFAULT_URL);
        properties.setProperty("db.user", DEFAULT_USER);
        properties.setProperty("db.password", DEFAULT_PASSWORD);

        File file = new File("db.properties");
        if (file.isFile()) {
            try (Reader reader = new InputStreamReader(
                    new FileInputStream(file), StandardCharsets.UTF_8)) {
                properties.load(reader);
            } catch (IOException e) {
                System.err.println("Gagal membaca db.properties: " + e.getMessage());
            }
        } else {
            try (InputStream input = Database.class.getResourceAsStream("/db.properties")) {
                if (input != null) {
                    try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                        properties.load(reader);
                    }
                }
            } catch (IOException e) {
                System.err.println("Gagal membaca db.properties dari classpath: "
                        + e.getMessage());
            }
        }

        URL = System.getProperty("kaboy.db.url", properties.getProperty("db.url"));
        USER = System.getProperty("kaboy.db.user", properties.getProperty("db.user"));
        PASSWORD = System.getProperty("kaboy.db.password",
                properties.getProperty("db.password"));

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                    "Driver MySQL belum ada. Pastikan lib/mysql-connector-j-8.3.0.jar "
                    + "masuk ke classpath project.");
        }
    }

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Menguji koneksi dan mengembalikan versi server database.
     */
    public static String testConnection() throws SQLException {
        try (Connection connection = getConnection()) {
            return connection.getMetaData().getDatabaseProductName() + " "
                    + connection.getMetaData().getDatabaseProductVersion();
        }
    }

    public static String getUrl() {
        return URL;
    }

    public static String getUser() {
        return USER;
    }

    /**
     * Menjalankan beberapa operasi DAO dalam satu transaksi.
     */
    public static <T> T inTransaction(TransactionWork<T> work) {
        try (Connection connection = getConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                T result = work.run(connection);
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            } finally {
                try {
                    connection.setAutoCommit(autoCommit);
                } catch (SQLException ignored) {
                    // koneksi akan ditutup oleh try-with-resources
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Transaksi database gagal: " + e.getMessage(), e);
        }
    }

    @FunctionalInterface
    public interface TransactionWork<T> {
        T run(Connection connection) throws SQLException;
    }
}
```

## src/kaboygarage/dto/ItemPesanan.java

```java
package kaboygarage.dto;

import kaboygarage.model.SparePart;

import java.math.BigDecimal;

/**
 * Item sementara pada keranjang pesanan sebelum disimpan ke database.
 */
public class ItemPesanan {

    private final SparePart sparePart;
    private int jumlah;

    public ItemPesanan(SparePart sparePart, int jumlah) {
        if (sparePart == null) {
            throw new IllegalArgumentException("Spare part wajib dipilih.");
        }
        if (jumlah <= 0) {
            throw new IllegalArgumentException("Jumlah item harus lebih besar dari 0.");
        }
        this.sparePart = sparePart;
        this.jumlah = jumlah;
    }

    public SparePart getSparePart() {
        return sparePart;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void tambahJumlah(int tambahan) {
        if (tambahan <= 0) {
            throw new IllegalArgumentException("Tambahan jumlah harus positif.");
        }
        this.jumlah += tambahan;
    }

    public BigDecimal getSubtotal() {
        return sparePart.getHarga().multiply(BigDecimal.valueOf(jumlah));
    }
}
```

## src/kaboygarage/dto/Statistik.java

```java
package kaboygarage.dto;

/**
 * Ringkasan angka untuk dashboard.
 */
public class Statistik {

    private final int totalSparePart;
    private final int pesananAktif;
    private final int antreanAktif;
    private final int totalPelanggan;
    private final int stokMenipis;

    public Statistik(int totalSparePart, int pesananAktif, int antreanAktif,
                     int totalPelanggan, int stokMenipis) {
        this.totalSparePart = totalSparePart;
        this.pesananAktif = pesananAktif;
        this.antreanAktif = antreanAktif;
        this.totalPelanggan = totalPelanggan;
        this.stokMenipis = stokMenipis;
    }

    public int getTotalSparePart() {
        return totalSparePart;
    }

    public int getPesananAktif() {
        return pesananAktif;
    }

    public int getAntreanAktif() {
        return antreanAktif;
    }

    public int getTotalPelanggan() {
        return totalPelanggan;
    }

    public int getStokMenipis() {
        return stokMenipis;
    }
}
```

## src/kaboygarage/exception/AplikasiException.java

```java
package kaboygarage.exception;

/**
 * Exception dasar untuk kesalahan aturan bisnis aplikasi.
 * Dipakai agar lapisan GUI dapat menampilkan pesan yang ramah pengguna.
 */
public class AplikasiException extends RuntimeException {

    public AplikasiException(String message) {
        super(message);
    }

    public AplikasiException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

## src/kaboygarage/exception/StokTidakCukupException.java

```java
package kaboygarage.exception;

/**
 * Dilempar ketika jumlah stok spare part tidak mencukupi permintaan pelanggan.
 */
public class StokTidakCukupException extends AplikasiException {

    private final String namaBarang;
    private final int stokTersedia;
    private final int jumlahDiminta;

    public StokTidakCukupException(String namaBarang, int stokTersedia, int jumlahDiminta) {
        super("Stok " + namaBarang + " tidak mencukupi. Tersedia " + stokTersedia
                + ", diminta " + jumlahDiminta + ".");
        this.namaBarang = namaBarang;
        this.stokTersedia = stokTersedia;
        this.jumlahDiminta = jumlahDiminta;
    }

    public String getNamaBarang() {
        return namaBarang;
    }

    public int getStokTersedia() {
        return stokTersedia;
    }

    public int getJumlahDiminta() {
        return jumlahDiminta;
    }
}
```

## src/kaboygarage/gui/DashboardPanel.java

```java
package kaboygarage.gui;

import kaboygarage.dto.Statistik;
import kaboygarage.model.Pesanan;
import kaboygarage.model.SparePart;
import kaboygarage.service.DashboardService;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

/**
 * Panel dashboard: ringkasan angka, stok menipis, dan pesanan terbaru.
 */
public class DashboardPanel extends JPanel implements Refreshable {

    private final DashboardService dashboardService;

    private final JLabel sparePartValue = nilaiLabel(Theme.ACCENT_DARK);
    private final JLabel pesananValue = nilaiLabel(Theme.ACCENT);
    private final JLabel antreanValue = nilaiLabel(Theme.WARNING);
    private final JLabel pelangganValue = nilaiLabel(Theme.SIDEBAR);

    private final DefaultTableModel modelStok = modelBaru(
            "Kode", "Nama", "Stok", "Minimal", "Status");
    private final DefaultTableModel modelPesanan = modelBaru(
            "Kode", "Pelanggan", "Tanggal", "Status", "Total");

    public DashboardPanel(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));

        JPanel kartu = new JPanel(new GridLayout(1, 4, 14, 0));
        kartu.setOpaque(false);
        kartu.add(kartu("Spare Part", sparePartValue, "item di katalog", Theme.ACCENT_DARK));
        kartu.add(kartu("Pesanan Aktif", pesananValue, "belum selesai", Theme.ACCENT));
        kartu.add(kartu("Antrean Servis", antreanValue, "menunggu / dikerjakan", Theme.WARNING));
        kartu.add(kartu("Pelanggan", pelangganValue, "terdaftar", Theme.SIDEBAR));
        add(kartu, BorderLayout.NORTH);

        JPanel tengah = new JPanel(new GridLayout(1, 2, 16, 0));
        tengah.setOpaque(false);

        JTable tabelStok = Theme.table();
        tabelStok.setModel(modelStok);
        tabelStok.getColumnModel().getColumn(4).setCellRenderer(Theme.statusRenderer());
        Theme.styleTableColumns(tabelStok, 80, 190, 60, 70, 90);

        JTable tabelPesanan = Theme.table();
        tabelPesanan.setModel(modelPesanan);
        tabelPesanan.getColumnModel().getColumn(3).setCellRenderer(Theme.statusRenderer());
        Theme.styleTableColumns(tabelPesanan, 130, 150, 110, 90, 100);

        tengah.add(kartuTabel("Stok Menipis",
                "Spare part yang perlu segera ditambah", tabelStok));
        tengah.add(kartuTabel("Pesanan Terbaru",
                "5 pesanan terakhir yang masuk", tabelPesanan));
        add(tengah, BorderLayout.CENTER);
    }

    private static JLabel nilaiLabel(Color color) {
        JLabel label = new JLabel("0");
        label.setFont(Theme.BIG_NUMBER);
        label.setForeground(color);
        return label;
    }

    private JPanel kartu(String judul, JLabel nilai, String keterangan, Color aksen) {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.MatteBorder(0, 4, 0, 0, aksen),
                new EmptyBorder(14, 16, 14, 16)));

        JLabel label = Theme.muted(judul);
        JLabel sub = Theme.muted(keterangan);
        sub.setFont(Theme.FONT_SMALL);

        card.add(label, BorderLayout.NORTH);
        card.add(nilai, BorderLayout.CENTER);
        card.add(sub, BorderLayout.SOUTH);
        return card;
    }

    private JPanel kartuTabel(String judul, String keterangan, JTable tabel) {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 12));

        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setOpaque(false);
        header.add(Theme.bold(judul), BorderLayout.NORTH);
        JLabel sub = Theme.muted(keterangan);
        sub.setFont(Theme.FONT_SMALL);
        header.add(sub, BorderLayout.CENTER);

        card.add(header, BorderLayout.NORTH);
        JScrollPane scroll = Theme.scroll(tabel);
        scroll.setPreferredSize(new Dimension(0, 260));
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private static DefaultTableModel modelBaru(String... kolom) {
        return new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    @Override
    public void refreshData() {
        Statistik statistik = dashboardService.statistik();
        sparePartValue.setText(String.valueOf(statistik.getTotalSparePart()));
        pesananValue.setText(String.valueOf(statistik.getPesananAktif()));
        antreanValue.setText(String.valueOf(statistik.getAntreanAktif()));
        pelangganValue.setText(String.valueOf(statistik.getTotalPelanggan()));

        modelStok.setRowCount(0);
        List<SparePart> menipis = dashboardService.stokMenipis();
        for (SparePart sparePart : menipis) {
            modelStok.addRow(new Object[]{
                sparePart.getKode(),
                sparePart.getNama(),
                sparePart.getStok(),
                sparePart.getStokMinimal(),
                sparePart.getStatusStok()
            });
        }

        modelPesanan.setRowCount(0);
        List<Pesanan> pesanan = dashboardService.pesananTerbaru(5);
        for (Pesanan item : pesanan) {
            modelPesanan.addRow(new Object[]{
                item.getKode(),
                item.getPelanggan().getNama(),
                FormatUtil.tanggal(item.getTanggalDibuat()),
                item.getStatus().getLabel(),
                FormatUtil.rupiah(item.hitungTotal())
            });
        }
    }
}
```

## src/kaboygarage/gui/FormatUtil.java

```java
package kaboygarage.gui;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Format rupiah dan tanggal/jam untuk form dan tabel.
 */
public final class FormatUtil {

    public static final DateTimeFormatter TANGGAL_INPUT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TANGGAL_VIEW =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final DateTimeFormatter WAKTU_INPUT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter WAKTU_VIEW =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private static final DecimalFormat RUPIAH = new DecimalFormat(
            "#,##0", new DecimalFormatSymbols(Locale.forLanguageTag("id-ID")));

    private FormatUtil() {
    }

    public static String rupiah(BigDecimal nilai) {
        return "Rp " + RUPIAH.format(nilai == null ? BigDecimal.ZERO : nilai);
    }

    public static String tanggal(LocalDateTime waktu) {
        return waktu == null ? "-" : waktu.format(WAKTU_VIEW);
    }

    public static String tanggalInput(LocalDateTime waktu) {
        return waktu == null ? "" : waktu.format(WAKTU_INPUT);
    }

    public static String tanggalSingkat(LocalDateTime waktu) {
        return waktu == null ? "-" : waktu.format(TANGGAL_VIEW);
    }

    public static LocalDateTime parseWaktu(String teks) {
        if (teks == null || teks.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Jadwal wajib diisi dengan format yyyy-MM-dd HH:mm, contoh 2026-10-30 09:15.");
        }
        try {
            return LocalDateTime.parse(teks.trim(), WAKTU_INPUT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Format jadwal harus yyyy-MM-dd HH:mm, contoh 2026-10-30 09:15.");
        }
    }
}
```

## src/kaboygarage/gui/GuiMain.java

```java
package kaboygarage.gui;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Entry point aplikasi desktop Kaboy Garage.
 */
public final class GuiMain {

    private GuiMain() {
    }

    public static void main(String[] args) {
        Theme.install();
        SwingUtilities.invokeLater(() -> {
            try {
                new LoginFrame().setVisible(true);
            } catch (Throwable ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Aplikasi gagal dijalankan:\n" + ex.getMessage(),
                        "Kaboy Garage", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
```

## src/kaboygarage/gui/LoginFrame.java

```java
package kaboygarage.gui;

import kaboygarage.db.Database;
import kaboygarage.exception.AplikasiException;
import kaboygarage.model.Admin;
import kaboygarage.service.AuthService;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.sql.SQLException;

/**
 * Halaman login admin Kaboy Garage.
 */
public class LoginFrame extends JFrame {

    private final AuthService authService = new AuthService();
    private final JTextField usernameField = Theme.textField();
    private final JPasswordField passwordField = Theme.passwordField();
    private final JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JButton loginButton = Theme.primaryButton("Masuk");

    public LoginFrame() {
        setTitle("Kaboy Garage - Login Admin");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Dimension layar = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(480, layar.width - 60), Math.min(650, layar.height - 80));
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.CARD);
        setContentPane(root);

        root.add(buatHeader(), BorderLayout.NORTH);
        root.add(buatForm(), BorderLayout.CENTER);

        cekDatabase();
    }

    private JPanel buatHeader() {
        JPanel header = new GradientPanel();
        header.setPreferredSize(new Dimension(0, 215));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(38, 40, 28, 40));

        JLabel logo = new JLabel("KABOY GARAGE");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 36));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel judul = new JLabel("Sistem Manajemen Bengkel");
        judul.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        judul.setForeground(new Color(255, 224, 196));
        judul.setAlignmentX(LEFT_ALIGNMENT);

        JLabel kampus = new JLabel("Spare Part  |  Pesanan  |  Waiting List Servis");
        kampus.setFont(Theme.FONT);
        kampus.setForeground(new Color(230, 210, 190));
        kampus.setAlignmentX(LEFT_ALIGNMENT);

        header.add(logo);
        header.add(Box.createVerticalStrut(4));
        header.add(judul);
        header.add(Box.createVerticalStrut(10));
        header.add(kampus);
        return header;
    }

    private JPanel buatForm() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(Theme.CARD);
        wrapper.setBorder(new EmptyBorder(24, 42, 28, 42));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 4, 0);

        JLabel heading = new JLabel("Masuk sebagai Admin");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        heading.setForeground(Theme.TEXT);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 18, 0);
        form.add(heading, gbc);

        gbc.insets = new Insets(0, 0, 4, 0);
        gbc.gridy = 1;
        form.add(Theme.bold("Username"), gbc);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 14, 0);
        form.add(usernameField, gbc);

        gbc.insets = new Insets(0, 0, 4, 0);
        gbc.gridy = 3;
        form.add(Theme.bold("Password"), gbc);
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 8, 0);
        form.add(passwordField, gbc);

        JCheckBox tampilPassword = new JCheckBox("Tampilkan password");
        tampilPassword.setOpaque(false);
        tampilPassword.setFont(Theme.FONT);
        tampilPassword.setForeground(Theme.MUTED);
        tampilPassword.setFocusPainted(false);
        tampilPassword.addActionListener(e -> passwordField.setEchoChar(
                tampilPassword.isSelected() ? (char) 0 : '\u2022'));
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 16, 0);
        form.add(tampilPassword, gbc);

        loginButton.setPreferredSize(new Dimension(0, 42));
        loginButton.addActionListener(e -> login());
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 12, 0);
        form.add(loginButton, gbc);
        getRootPane().setDefaultButton(loginButton);

        statusLabel.setFont(Theme.FONT_SMALL);
        statusLabel.setForeground(Theme.MUTED);
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 10, 0);
        form.add(statusLabel, gbc);

        JLabel hint = new JLabel("Login default: admin / admin123", SwingConstants.CENTER);
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.MUTED);
        gbc.gridy = 8;
        gbc.insets = new Insets(0, 0, 0, 0);
        form.add(hint, gbc);

        wrapper.add(form);
        return wrapper;
    }

    private void cekDatabase() {
        try {
            String versi = Database.testConnection();
            statusLabel.setText("Terhubung ke " + versi);
            statusLabel.setForeground(Theme.ACCENT_DARK);
        } catch (SQLException e) {
            statusLabel.setText("Database belum siap - jalankan MySQL dan import SQL");
            statusLabel.setForeground(Theme.DANGER);
        }
    }

    private void login() {
        try {
            Admin admin = authService.login(
                    usernameField.getText(), new String(passwordField.getPassword()));
            MainFrame mainFrame = new MainFrame(admin);
            mainFrame.setVisible(true);
            dispose();
        } catch (AplikasiException e) {
            Theme.showError(this, e.getMessage());
            passwordField.selectAll();
            passwordField.requestFocusInWindow();
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal login: " + e.getMessage());
        }
    }

    /**
     * Panel header dengan gradasi warna gelap ke oranye.
     */
    private static final class GradientPanel extends JPanel {

        private GradientPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, Theme.SIDEBAR,
                    getWidth(), getHeight(), Theme.ACCENT_DARK));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(graphics);
        }
    }
}
```

## src/kaboygarage/gui/MainFrame.java

```java
package kaboygarage.gui;

import kaboygarage.model.Admin;
import kaboygarage.service.DashboardService;
import kaboygarage.service.PelangganService;
import kaboygarage.service.PesananService;
import kaboygarage.service.SparePartService;
import kaboygarage.service.WaitingListService;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Jendela utama admin: sidebar navigasi + konten panel.
 */
public class MainFrame extends JFrame {

    private final Admin admin;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();
    private final Map<String, Refreshable> panels = new LinkedHashMap<>();
    private final Map<String, String[]> infoHalaman = new LinkedHashMap<>();
    private final JLabel pageTitle = new JLabel();
    private final JLabel pageSubtitle = new JLabel();

    public MainFrame(Admin admin) {
        this.admin = admin;
        setTitle("Kaboy Garage - Admin Panel");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.add(buatSidebar(), BorderLayout.WEST);
        root.add(buatMain(), BorderLayout.CENTER);
        setContentPane(root);

        tampilkan("dashboard");
    }

    private JPanel buatSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Theme.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(235, 0));

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBorder(new EmptyBorder(26, 20, 24, 20));

        JLabel kaboy = new JLabel("KABOY");
        kaboy.setFont(new Font("Segoe UI", Font.BOLD, 25));
        kaboy.setForeground(Color.WHITE);
        kaboy.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel garage = new JLabel("GARAGE");
        garage.setFont(new Font("Segoe UI", Font.BOLD, 25));
        garage.setForeground(Theme.ACCENT);
        garage.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Admin Panel");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(new Color(150, 158, 170));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        brand.add(kaboy);
        brand.add(garage);
        brand.add(Box.createVerticalStrut(8));
        brand.add(sub);
        sidebar.add(brand, BorderLayout.NORTH);

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(6, 0, 6, 0));
        tambahNav(nav, "dashboard", "Dashboard");
        tambahNav(nav, "sparepart", "Spare Part");
        tambahNav(nav, "pesanan", "Pesanan");
        tambahNav(nav, "waitinglist", "Waiting List");
        tambahNav(nav, "pelanggan", "Pelanggan");
        sidebar.add(nav, BorderLayout.CENTER);

        JPanel bawah = new JPanel();
        bawah.setOpaque(false);
        bawah.setLayout(new BoxLayout(bawah, BoxLayout.Y_AXIS));
        bawah.setBorder(new EmptyBorder(14, 20, 20, 20));

        JLabel nama = new JLabel(admin.getNama());
        nama.setFont(Theme.FONT_BOLD);
        nama.setForeground(Color.WHITE);
        nama.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel peran = new JLabel("Administrator");
        peran.setFont(Theme.FONT_SMALL);
        peran.setForeground(new Color(150, 158, 170));
        peran.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton keluar = Theme.navButton("Keluar");
        keluar.setForeground(new Color(255, 170, 170));
        keluar.setAlignmentX(Component.LEFT_ALIGNMENT);
        keluar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        keluar.addActionListener(e -> keluar());

        bawah.add(nama);
        bawah.add(peran);
        bawah.add(Box.createVerticalStrut(12));
        bawah.add(keluar);
        sidebar.add(bawah, BorderLayout.SOUTH);

        return sidebar;
    }

    private void tambahNav(JPanel nav, String key, String label) {
        JButton button = Theme.navButton(label);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        button.addActionListener(e -> tampilkan(key));
        nav.add(button);
        nav.add(Box.createVerticalStrut(2));
        navButtons.put(key, button);
    }

    private JPanel buatMain() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Theme.BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.CARD);
        header.setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));
        header.setPreferredSize(new Dimension(0, 78));

        JPanel judul = new JPanel();
        judul.setOpaque(false);
        judul.setLayout(new BoxLayout(judul, BoxLayout.Y_AXIS));
        judul.setBorder(new EmptyBorder(14, 22, 12, 12));
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        pageTitle.setForeground(Theme.TEXT);
        pageSubtitle.setFont(Theme.FONT_SMALL);
        pageSubtitle.setForeground(Theme.MUTED);
        judul.add(pageTitle);
        judul.add(Box.createVerticalStrut(3));
        judul.add(pageSubtitle);
        header.add(judul, BorderLayout.WEST);

        JPanel kanan = new JPanel();
        kanan.setOpaque(false);
        kanan.setLayout(new BoxLayout(kanan, BoxLayout.X_AXIS));
        kanan.setBorder(new EmptyBorder(0, 0, 0, 22));
        JLabel badge = Theme.badge("ADMIN", Theme.ACCENT_DARK);
        badge.setFont(Theme.FONT_SMALL);
        kanan.add(badge);
        header.add(kanan, BorderLayout.EAST);

        main.add(header, BorderLayout.NORTH);

        content.setBackground(Theme.BACKGROUND);
        content.setBorder(new EmptyBorder(18, 20, 20, 20));

        DashboardPanel dashboard = new DashboardPanel(new DashboardService());
        SparePartPanel sparePart = new SparePartPanel(new SparePartService());
        PelangganPanel pelanggan = new PelangganPanel(new PelangganService());
        PesananPanel pesanan = new PesananPanel(
                new PesananService(), new PelangganService(), new SparePartService());
        WaitingListPanel waitingList = new WaitingListPanel(
                new WaitingListService(), new PelangganService());

        daftarPanel("dashboard", dashboard);
        daftarPanel("sparepart", sparePart);
        daftarPanel("pesanan", pesanan);
        daftarPanel("waitinglist", waitingList);
        daftarPanel("pelanggan", pelanggan);

        infoHalaman.put("dashboard",
                new String[]{"Dashboard", "Ringkasan operasional bengkel hari ini"});
        infoHalaman.put("sparepart",
                new String[]{"Spare Part", "Kelola katalog, harga, dan stok spare part"});
        infoHalaman.put("pesanan",
                new String[]{"Pesanan", "Buat pesanan baru dan pantau status pesanan"});
        infoHalaman.put("waitinglist",
                new String[]{"Waiting List Servis", "Daftar antrean dan jadwal servis kendaraan"});
        infoHalaman.put("pelanggan",
                new String[]{"Pelanggan", "Kelola data pelanggan dan kendaraannya"});

        main.add(content, BorderLayout.CENTER);
        return main;
    }

    private void daftarPanel(String key, Refreshable panel) {
        panels.put(key, panel);
        content.add((Component) panel, key);
    }

    private void tampilkan(String key) {
        if (!panels.containsKey(key)) {
            return;
        }
        cardLayout.show(content, key);
        String[] info = infoHalaman.get(key);
        if (info != null) {
            pageTitle.setText(info[0]);
            pageSubtitle.setText(info[1]);
        }
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            boolean aktif = entry.getKey().equals(key);
            entry.getValue().setBackground(aktif ? Theme.SIDEBAR_ACTIVE : Theme.SIDEBAR);
        }
        try {
            panels.get(key).refreshData();
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal memuat data: " + e.getMessage());
        }
    }

    private void keluar() {
        if (Theme.confirm(this, "Keluar dari aplikasi?")) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
```

## src/kaboygarage/gui/PelangganPanel.java

```java
package kaboygarage.gui;

import kaboygarage.exception.AplikasiException;
import kaboygarage.model.Kendaraan;
import kaboygarage.model.Pelanggan;
import kaboygarage.service.PelangganService;
import kaboygarage.util.Validasi;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel data pelanggan beserta kendaraannya.
 */
public class PelangganPanel extends JPanel implements Refreshable {

    private final PelangganService service;

    private final JTextField namaField = Theme.textField();
    private final JTextField noHpField = Theme.textField();
    private final JTextField emailField = Theme.textField();
    private final JTextField alamatField = Theme.textField();

    private final JTable tabelPelanggan = Theme.table();
    private final DefaultTableModel modelPelanggan = new DefaultTableModel(
            new Object[]{"Nama", "No HP", "Email", "Alamat"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final List<Pelanggan> dataPelanggan = new ArrayList<>();
    private Long selectedPelangganId;

    private final JLabel ownerLabel = Theme.muted("Pilih pelanggan terlebih dahulu");
    private final JTextField merkField = Theme.textField();
    private final JTextField modelField = Theme.textField();
    private final JTextField platField = Theme.textField();
    private final JTextField tahunField = Theme.textField();

    private final JTable tabelKendaraan = Theme.table();
    private final DefaultTableModel modelKendaraan = new DefaultTableModel(
            new Object[]{"Plat Nomor", "Merk", "Model", "Tahun"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final List<Kendaraan> dataKendaraan = new ArrayList<>();
    private Long selectedKendaraanId;

    public PelangganPanel(PelangganService service) {
        this.service = service;
        setOpaque(false);
        setLayout(new BorderLayout());

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                buatPanelPelanggan(), buatPanelKendaraan());
        split.setBorder(null);
        split.setOpaque(false);
        split.setResizeWeight(0.5);
        split.setDividerLocation(330);
        add(split, BorderLayout.CENTER);

        resetPelangganForm();
        resetKendaraanForm();
    }

    private JPanel buatPanelPelanggan() {
        JPanel panel = new JPanel(new BorderLayout(14, 0));
        panel.setOpaque(false);
        panel.add(buatFormPelanggan(), BorderLayout.WEST);
        panel.add(buatTabelPelanggan(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buatFormPelanggan() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 10));
        card.setPreferredSize(new Dimension(330, 0));

        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setOpaque(false);
        header.add(Theme.bold("Data Pelanggan"), BorderLayout.NORTH);
        header.add(Theme.muted("Pelanggan bisa langsung memesan di bengkel"), BorderLayout.CENTER);

        JPanel form = Theme.formPanel();
        GridBagConstraints gbc = new GridBagConstraints();
        int baris = 0;
        Theme.addFormRow(form, gbc, baris++, "Nama", namaField);
        Theme.addFormRow(form, gbc, baris++, "No HP", noHpField);
        Theme.addFormRow(form, gbc, baris++, "Email", emailField);
        Theme.addFormRow(form, gbc, baris++, "Alamat", alamatField);
        Theme.fillForm(form);

        JButton simpan = Theme.primaryButton("Simpan");
        simpan.addActionListener(e -> simpanPelanggan());
        JButton hapus = Theme.dangerButton("Hapus");
        hapus.addActionListener(e -> hapusPelanggan());
        JButton baru = Theme.secondaryButton("Baru");
        baru.addActionListener(e -> resetPelangganForm());

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tombol.setOpaque(false);
        tombol.add(simpan);
        tombol.add(hapus);
        tombol.add(baru);

        card.add(header, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(tombol, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buatTabelPelanggan() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 10));

        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setOpaque(false);
        header.add(Theme.bold("Daftar Pelanggan"), BorderLayout.NORTH);
        header.add(Theme.muted("Klik pelanggan untuk melihat kendaraannya"), BorderLayout.CENTER);

        tabelPelanggan.setModel(modelPelanggan);
        tabelPelanggan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Theme.styleTableColumns(tabelPelanggan, 170, 120, 170, 230);
        tabelPelanggan.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                tampilkanPilihanPelanggan();
            }
        });

        card.add(header, BorderLayout.NORTH);
        card.add(Theme.scroll(tabelPelanggan), BorderLayout.CENTER);
        return card;
    }

    private JPanel buatPanelKendaraan() {
        JPanel panel = new JPanel(new BorderLayout(14, 0));
        panel.setOpaque(false);
        panel.add(buatFormKendaraan(), BorderLayout.WEST);
        panel.add(buatTabelKendaraan(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buatFormKendaraan() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 10));
        card.setPreferredSize(new Dimension(330, 0));

        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setOpaque(false);
        header.add(Theme.bold("Kendaraan Pelanggan"), BorderLayout.NORTH);
        header.add(ownerLabel, BorderLayout.CENTER);

        JPanel form = Theme.formPanel();
        GridBagConstraints gbc = new GridBagConstraints();
        int baris = 0;
        Theme.addFormRow(form, gbc, baris++, "Merk", merkField);
        Theme.addFormRow(form, gbc, baris++, "Model", modelField);
        Theme.addFormRow(form, gbc, baris++, "Plat nomor", platField);
        Theme.addFormRow(form, gbc, baris++, "Tahun", tahunField);
        Theme.fillForm(form);

        JButton simpan = Theme.primaryButton("Simpan");
        simpan.addActionListener(e -> simpanKendaraan());
        JButton hapus = Theme.dangerButton("Hapus");
        hapus.addActionListener(e -> hapusKendaraan());
        JButton baru = Theme.secondaryButton("Baru");
        baru.addActionListener(e -> resetKendaraanForm());

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tombol.setOpaque(false);
        tombol.add(simpan);
        tombol.add(hapus);
        tombol.add(baru);

        card.add(header, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(tombol, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buatTabelKendaraan() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 10));

        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setOpaque(false);
        header.add(Theme.bold("Daftar Kendaraan"), BorderLayout.NORTH);
        header.add(Theme.muted("Kendaraan dipakai saat pendaftaran servis"), BorderLayout.CENTER);

        tabelKendaraan.setModel(modelKendaraan);
        tabelKendaraan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Theme.styleTableColumns(tabelKendaraan, 130, 150, 180, 80);
        tabelKendaraan.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                tampilkanPilihanKendaraan();
            }
        });

        card.add(header, BorderLayout.NORTH);
        card.add(Theme.scroll(tabelKendaraan), BorderLayout.CENTER);
        return card;
    }

    @Override
    public void refreshData() {
        muatPelanggan(selectedPelangganId);
    }

    private void muatPelanggan(Long pilihId) {
        try {
            List<Pelanggan> hasil = service.semua();
            dataPelanggan.clear();
            dataPelanggan.addAll(hasil);
            modelPelanggan.setRowCount(0);
            for (Pelanggan pelanggan : hasil) {
                modelPelanggan.addRow(new Object[]{
                    pelanggan.getNama(),
                    pelanggan.getNoHp(),
                    pelanggan.getEmail() == null ? "-" : pelanggan.getEmail(),
                    pelanggan.getAlamat() == null ? "-" : pelanggan.getAlamat()
                });
            }

            int baris = cariBarisPelanggan(pilihId);
            if (baris < 0 && !hasil.isEmpty()) {
                baris = 0;
            }
            if (baris >= 0) {
                tabelPelanggan.setRowSelectionInterval(baris, baris);
                tampilkanPilihanPelanggan();
            } else {
                resetPelangganForm();
            }
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal memuat pelanggan: " + e.getMessage());
        }
    }

    private int cariBarisPelanggan(Long id) {
        if (id == null) {
            return -1;
        }
        for (int i = 0; i < dataPelanggan.size(); i++) {
            if (id.equals(dataPelanggan.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }

    private void tampilkanPilihanPelanggan() {
        int baris = tabelPelanggan.getSelectedRow();
        if (baris < 0 || baris >= dataPelanggan.size()) {
            return;
        }
        Pelanggan pelanggan = dataPelanggan.get(baris);
        selectedPelangganId = pelanggan.getId();
        namaField.setText(pelanggan.getNama());
        noHpField.setText(pelanggan.getNoHp());
        emailField.setText(pelanggan.getEmail());
        alamatField.setText(pelanggan.getAlamat());
        ownerLabel.setText("Kendaraan milik: " + pelanggan.getNama());
        muatKendaraan(true);
    }

    private void simpanPelanggan() {
        try {
            Pelanggan pelanggan = new Pelanggan(
                    namaField.getText(), noHpField.getText(),
                    emailField.getText().trim(), alamatField.getText().trim());
            pelanggan.setId(selectedPelangganId);
            service.simpan(pelanggan);
            selectedPelangganId = pelanggan.getId();
            Theme.showInfo(this, "Data pelanggan berhasil disimpan.");
            muatPelanggan(selectedPelangganId);
        } catch (AplikasiException e) {
            Theme.showError(this, e.getMessage());
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal menyimpan pelanggan: " + e.getMessage());
        }
    }

    private void hapusPelanggan() {
        if (selectedPelangganId == null) {
            Theme.showError(this, "Pilih pelanggan yang ingin dihapus.");
            return;
        }
        if (!Theme.confirm(this, "Hapus pelanggan yang dipilih?")) {
            return;
        }
        try {
            service.hapus(selectedPelangganId);
            selectedPelangganId = null;
            resetPelangganForm();
            muatPelanggan(null);
            Theme.showInfo(this, "Pelanggan berhasil dihapus.");
        } catch (AplikasiException e) {
            Theme.showError(this, e.getMessage());
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal menghapus pelanggan: " + e.getMessage());
        }
    }

    private void muatKendaraan(boolean resetForm) {
        dataKendaraan.clear();
        modelKendaraan.setRowCount(0);
        if (selectedPelangganId == null) {
            if (resetForm) {
                resetKendaraanForm();
            }
            return;
        }
        try {
            List<Kendaraan> hasil = service.kendaraan(selectedPelangganId);
            dataKendaraan.addAll(hasil);
            for (Kendaraan kendaraan : hasil) {
                modelKendaraan.addRow(new Object[]{
                    kendaraan.getPlatNomor(),
                    kendaraan.getMerk(),
                    kendaraan.getModel(),
                    kendaraan.getTahun()
                });
            }
            if (resetForm) {
                resetKendaraanForm();
            } else if (selectedKendaraanId != null) {
                pilihKendaraan(selectedKendaraanId);
            }
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal memuat kendaraan: " + e.getMessage());
        }
    }

    private void tampilkanPilihanKendaraan() {
        int baris = tabelKendaraan.getSelectedRow();
        if (baris < 0 || baris >= dataKendaraan.size()) {
            return;
        }
        Kendaraan kendaraan = dataKendaraan.get(baris);
        selectedKendaraanId = kendaraan.getId();
        merkField.setText(kendaraan.getMerk());
        modelField.setText(kendaraan.getModel());
        platField.setText(kendaraan.getPlatNomor());
        tahunField.setText(String.valueOf(kendaraan.getTahun()));
    }

    private void pilihKendaraan(Long id) {
        for (int i = 0; i < dataKendaraan.size(); i++) {
            if (id.equals(dataKendaraan.get(i).getId())) {
                tabelKendaraan.setRowSelectionInterval(i, i);
                tampilkanPilihanKendaraan();
                return;
            }
        }
    }

    private void simpanKendaraan() {
        if (selectedPelangganId == null) {
            Theme.showError(this, "Pilih pelanggan pemilik kendaraan terlebih dahulu.");
            return;
        }
        Pelanggan pelanggan = null;
        for (Pelanggan item : dataPelanggan) {
            if (selectedPelangganId.equals(item.getId())) {
                pelanggan = item;
                break;
            }
        }
        if (pelanggan == null) {
            Theme.showError(this, "Data pelanggan tidak ditemukan.");
            return;
        }
        try {
            Kendaraan kendaraan = new Kendaraan(
                    pelanggan,
                    merkField.getText(),
                    modelField.getText(),
                    platField.getText(),
                    Validasi.parseTahun(tahunField.getText()));
            kendaraan.setId(selectedKendaraanId);
            service.simpanKendaraan(kendaraan);
            selectedKendaraanId = kendaraan.getId();
            Theme.showInfo(this, "Data kendaraan berhasil disimpan.");
            muatKendaraan(false);
        } catch (AplikasiException e) {
            Theme.showError(this, e.getMessage());
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal menyimpan kendaraan: " + e.getMessage());
        }
    }

    private void hapusKendaraan() {
        if (selectedKendaraanId == null) {
            Theme.showError(this, "Pilih kendaraan yang ingin dihapus.");
            return;
        }
        if (!Theme.confirm(this, "Hapus kendaraan yang dipilih?")) {
            return;
        }
        try {
            service.hapusKendaraan(selectedKendaraanId);
            selectedKendaraanId = null;
            resetKendaraanForm();
            muatKendaraan(false);
            Theme.showInfo(this, "Kendaraan berhasil dihapus.");
        } catch (AplikasiException e) {
            Theme.showError(this, e.getMessage());
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal menghapus kendaraan: " + e.getMessage());
        }
    }

    private void resetPelangganForm() {
        selectedPelangganId = null;
        namaField.setText("");
        noHpField.setText("");
        emailField.setText("");
        alamatField.setText("");
        tabelPelanggan.clearSelection();
        dataKendaraan.clear();
        modelKendaraan.setRowCount(0);
        ownerLabel.setText("Pilih pelanggan terlebih dahulu");
        resetKendaraanForm();
        namaField.requestFocusInWindow();
    }

    private void resetKendaraanForm() {
        selectedKendaraanId = null;
        merkField.setText("");
        modelField.setText("");
        platField.setText("");
        tahunField.setText(String.valueOf(Year.now().getValue()));
        tabelKendaraan.clearSelection();
    }
}
```

## src/kaboygarage/gui/PesananPanel.java

```java
package kaboygarage.gui;

import kaboygarage.dto.ItemPesanan;
import kaboygarage.exception.AplikasiException;
import kaboygarage.model.DetailPesanan;
import kaboygarage.model.Pelanggan;
import kaboygarage.model.Pesanan;
import kaboygarage.model.SparePart;
import kaboygarage.model.StatusPesanan;
import kaboygarage.service.PelangganService;
import kaboygarage.service.PesananService;
import kaboygarage.service.SparePartService;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel pembuatan pesanan dan pemantauan status pesanan.
 */
public class PesananPanel extends JPanel implements Refreshable {

    private final PesananService pesananService;
    private final PelangganService pelangganService;
    private final SparePartService sparePartService;

    private final JComboBox<Pelanggan> pelangganCombo = new JComboBox<>();
    private final JComboBox<SparePart> sparePartCombo = new JComboBox<>();
    private final JSpinner jumlahSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JTextField catatanField = Theme.textField();

    private final JTable tabelKeranjang = Theme.table();
    private final DefaultTableModel modelKeranjang = new DefaultTableModel(
            new Object[]{"Kode", "Nama", "Harga", "Qty", "Subtotal"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final List<ItemPesanan> keranjang = new ArrayList<>();
    private final JLabel totalLabel = new JLabel("Total: Rp 0");

    private final JTable tabelPesanan = Theme.table();
    private final DefaultTableModel modelPesanan = new DefaultTableModel(
            new Object[]{"Kode", "Pelanggan", "Tanggal", "Status", "Item", "Total"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final List<Pesanan> dataPesanan = new ArrayList<>();
    private Long selectedPesananId;

    private final JComboBox<StatusPesanan> statusCombo = new JComboBox<>();
    private final JButton ubahStatusButton = Theme.primaryButton("Ubah Status");
    private final JButton batalkanButton = Theme.dangerButton("Batalkan");
    private final JButton detailButton = Theme.secondaryButton("Detail");

    public PesananPanel(PesananService pesananService,
                        PelangganService pelangganService,
                        SparePartService sparePartService) {
        this.pesananService = pesananService;
        this.pelangganService = pelangganService;
        this.sparePartService = sparePartService;
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));

        add(buatPanelBuat(), BorderLayout.NORTH);
        add(buatPanelDaftar(), BorderLayout.CENTER);

        perbaruiKeranjang();
        perbaruiTombolStatus();
    }

    private JPanel buatPanelBuat() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 10));

        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setOpaque(false);
        header.add(Theme.bold("Buat Pesanan Baru"), BorderLayout.NORTH);
        header.add(Theme.muted("Pilih pelanggan, tambahkan spare part, lalu simpan"), BorderLayout.CENTER);

        JPanel isi = new JPanel(new BorderLayout(0, 10));
        isi.setOpaque(false);

        JPanel pilih = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pilih.setOpaque(false);
        pelangganCombo.setPreferredSize(new Dimension(210, 32));
        sparePartCombo.setPreferredSize(new Dimension(250, 32));
        jumlahSpinner.setPreferredSize(new Dimension(70, 32));
        JButton tambah = Theme.primaryButton("Tambah Item");
        tambah.addActionListener(e -> tambahItem());
        pilih.add(Theme.bold("Pelanggan"));
        pilih.add(pelangganCombo);
        pilih.add(Theme.bold("Spare part"));
        pilih.add(sparePartCombo);
        pilih.add(Theme.bold("Qty"));
        pilih.add(jumlahSpinner);
        pilih.add(tambah);
        isi.add(pilih, BorderLayout.NORTH);

        tabelKeranjang.setModel(modelKeranjang);
        Theme.styleTableColumns(tabelKeranjang, 80, 230, 110, 60, 130);
        JScrollPane scrollKeranjang = Theme.scroll(tabelKeranjang);
        scrollKeranjang.setPreferredSize(new Dimension(0, 145));
        isi.add(scrollKeranjang, BorderLayout.CENTER);

        JPanel bawah = new JPanel(new BorderLayout(12, 0));
        bawah.setOpaque(false);

        JPanel kiri = new JPanel(new BorderLayout(8, 0));
        kiri.setOpaque(false);
        kiri.add(Theme.bold("Catatan"), BorderLayout.WEST);
        catatanField.setPreferredSize(new Dimension(330, 32));
        kiri.add(catatanField, BorderLayout.CENTER);
        bawah.add(kiri, BorderLayout.WEST);

        JPanel kanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        kanan.setOpaque(false);
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalLabel.setForeground(Theme.ACCENT_DARK);
        JButton hapusItem = Theme.secondaryButton("Hapus Item");
        hapusItem.addActionListener(e -> hapusItem());
        JButton kosongkan = Theme.secondaryButton("Kosongkan");
        kosongkan.addActionListener(e -> {
            keranjang.clear();
            perbaruiKeranjang();
        });
        JButton simpan = Theme.primaryButton("Simpan Pesanan");
        simpan.addActionListener(e -> simpanPesanan());
        kanan.add(totalLabel);
        kanan.add(hapusItem);
        kanan.add(kosongkan);
        kanan.add(simpan);
        bawah.add(kanan, BorderLayout.EAST);

        isi.add(bawah, BorderLayout.SOUTH);
        card.add(header, BorderLayout.NORTH);
        card.add(isi, BorderLayout.CENTER);
        return card;
    }

    private JPanel buatPanelDaftar() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 10));

        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setOpaque(false);
        JPanel judul = new JPanel(new BorderLayout(0, 3));
        judul.setOpaque(false);
        judul.add(Theme.bold("Daftar Pesanan"), BorderLayout.NORTH);
        judul.add(Theme.muted("Klik pesanan untuk melihat detail atau mengubah status"), BorderLayout.CENTER);
        header.add(judul, BorderLayout.WEST);

        JButton refresh = Theme.secondaryButton("Muat Ulang");
        refresh.addActionListener(e -> refreshData());
        JPanel tombolHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        tombolHeader.setOpaque(false);
        tombolHeader.add(refresh);
        header.add(tombolHeader, BorderLayout.EAST);

        tabelPesanan.setModel(modelPesanan);
        tabelPesanan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelPesanan.getColumnModel().getColumn(3).setCellRenderer(Theme.statusRenderer());
        Theme.styleTableColumns(tabelPesanan, 135, 160, 120, 100, 55, 110);
        tabelPesanan.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                tampilkanPilihanPesanan();
            }
        });

        JPanel aksi = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        aksi.setOpaque(false);
        detailButton.addActionListener(e -> tampilkanDetail());
        ubahStatusButton.addActionListener(e -> ubahStatus());
        batalkanButton.addActionListener(e -> batalkanPesanan());
        statusCombo.setPreferredSize(new Dimension(150, 32));
        aksi.add(detailButton);
        aksi.add(statusCombo);
        aksi.add(ubahStatusButton);
        aksi.add(batalkanButton);

        card.add(header, BorderLayout.NORTH);
        card.add(Theme.scroll(tabelPesanan), BorderLayout.CENTER);
        card.add(aksi, BorderLayout.SOUTH);
        return card;
    }

    @Override
    public void refreshData() {
        try {
            muatPelangganCombo();
            muatSparePartCombo();
            muatPesanan();
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal memuat data pesanan: " + e.getMessage());
        }
    }

    private void muatPelangganCombo() {
        Long terpilih = pelangganCombo.getSelectedItem() == null
                ? null : ((Pelanggan) pelangganCombo.getSelectedItem()).getId();
        List<Pelanggan> semua = pelangganService.semua();
        pelangganCombo.removeAllItems();
        for (Pelanggan pelanggan : semua) {
            pelangganCombo.addItem(pelanggan);
        }
        if (terpilih != null) {
            for (int i = 0; i < pelangganCombo.getItemCount(); i++) {
                if (terpilih.equals(pelangganCombo.getItemAt(i).getId())) {
                    pelangganCombo.setSelectedIndex(i);
                    return;
                }
            }
        }
    }

    private void muatSparePartCombo() {
        Long terpilih = sparePartCombo.getSelectedItem() == null
                ? null : ((SparePart) sparePartCombo.getSelectedItem()).getId();
        List<SparePart> semua = sparePartService.semua();
        sparePartCombo.removeAllItems();
        for (SparePart sparePart : semua) {
            sparePartCombo.addItem(sparePart);
        }
        if (terpilih != null) {
            for (int i = 0; i < sparePartCombo.getItemCount(); i++) {
                if (terpilih.equals(sparePartCombo.getItemAt(i).getId())) {
                    sparePartCombo.setSelectedIndex(i);
                    return;
                }
            }
        }
    }

    private void muatPesanan() {
        List<Pesanan> semua = pesananService.semua();
        dataPesanan.clear();
        dataPesanan.addAll(semua);
        modelPesanan.setRowCount(0);
        for (Pesanan pesanan : semua) {
            modelPesanan.addRow(new Object[]{
                pesanan.getKode(),
                pesanan.getPelanggan().getNama(),
                FormatUtil.tanggal(pesanan.getTanggalDibuat()),
                pesanan.getStatus().getLabel(),
                pesanan.hitungTotalItem(),
                FormatUtil.rupiah(pesanan.hitungTotal())
            });
        }

        int baris = -1;
        if (selectedPesananId != null) {
            for (int i = 0; i < dataPesanan.size(); i++) {
                if (selectedPesananId.equals(dataPesanan.get(i).getId())) {
                    baris = i;
                    break;
                }
            }
        }
        if (baris >= 0) {
            tabelPesanan.setRowSelectionInterval(baris, baris);
            tampilkanPilihanPesanan();
        } else {
            tabelPesanan.clearSelection();
            selectedPesananId = null;
            perbaruiTombolStatus();
        }
    }

    private void tambahItem() {
        SparePart sparePart = (SparePart) sparePartCombo.getSelectedItem();
        if (sparePart == null) {
            Theme.showError(this, "Pilih spare part terlebih dahulu.");
            return;
        }
        int jumlah = (Integer) jumlahSpinner.getValue();
        int totalJumlah = jumlah;
        for (ItemPesanan item : keranjang) {
            if (item.getSparePart().getId().equals(sparePart.getId())) {
                totalJumlah += item.getJumlah();
                break;
            }
        }
        if (!sparePart.isTersedia(totalJumlah)) {
            Theme.showError(this, "Stok " + sparePart.getNama() + " tidak mencukupi. "
                    + "Tersedia " + sparePart.getStok() + ".");
            return;
        }

        boolean ditemukan = false;
        for (ItemPesanan item : keranjang) {
            if (item.getSparePart().getId().equals(sparePart.getId())) {
                item.tambahJumlah(jumlah);
                ditemukan = true;
                break;
            }
        }
        if (!ditemukan) {
            keranjang.add(new ItemPesanan(sparePart, jumlah));
        }
        jumlahSpinner.setValue(1);
        perbaruiKeranjang();
    }

    private void hapusItem() {
        int baris = tabelKeranjang.getSelectedRow();
        if (baris < 0 || baris >= keranjang.size()) {
            Theme.showError(this, "Pilih item yang ingin dihapus dari keranjang.");
            return;
        }
        keranjang.remove(baris);
        perbaruiKeranjang();
    }

    private void perbaruiKeranjang() {
        modelKeranjang.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        for (ItemPesanan item : keranjang) {
            total = total.add(item.getSubtotal());
            modelKeranjang.addRow(new Object[]{
                item.getSparePart().getKode(),
                item.getSparePart().getNama(),
                FormatUtil.rupiah(item.getSparePart().getHarga()),
                item.getJumlah(),
                FormatUtil.rupiah(item.getSubtotal())
            });
        }
        totalLabel.setText("Total: " + FormatUtil.rupiah(total));
    }

    private void simpanPesanan() {
        Pelanggan pelanggan = (Pelanggan) pelangganCombo.getSelectedItem();
        if (pelanggan == null) {
            Theme.showError(this, "Pilih pelanggan terlebih dahulu.");
            return;
        }
        if (keranjang.isEmpty()) {
            Theme.showError(this, "Tambahkan minimal satu spare part ke pesanan.");
            return;
        }
        try {
            Pesanan pesanan = pesananService.buatPesanan(
                    pelanggan, new ArrayList<>(keranjang), catatanField.getText());
            Theme.showInfo(this, "Pesanan " + pesanan.getKode() + " berhasil dibuat.\n"
                    + "Total: " + FormatUtil.rupiah(pesanan.hitungTotal()));
            keranjang.clear();
            catatanField.setText("");
            perbaruiKeranjang();
            selectedPesananId = pesanan.getId();
            refreshData();
        } catch (AplikasiException e) {
            Theme.showError(this, e.getMessage());
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal membuat pesanan: " + e.getMessage());
        }
    }

    private void tampilkanPilihanPesanan() {
        int baris = tabelPesanan.getSelectedRow();
        if (baris < 0 || baris >= dataPesanan.size()) {
            selectedPesananId = null;
            perbaruiTombolStatus();
            return;
        }
        Pesanan pesanan = dataPesanan.get(baris);
        selectedPesananId = pesanan.getId();
        statusCombo.removeAllItems();
        for (StatusPesanan status : StatusPesanan.values()) {
            if (pesanan.getStatus().bisaBerubahKe(status)) {
                statusCombo.addItem(status);
            }
        }
        perbaruiTombolStatus();
    }

    private void perbaruiTombolStatus() {
        Pesanan pesanan = pesananTerpilih();
        boolean adaPilihan = statusCombo.getItemCount() > 0 && pesanan != null;
        ubahStatusButton.setEnabled(adaPilihan);
        batalkanButton.setEnabled(pesanan != null
                && pesanan.getStatus().bisaBerubahKe(StatusPesanan.DIBATALKAN));
        detailButton.setEnabled(pesanan != null);
    }

    private void ubahStatus() {
        Pesanan pesanan = pesananTerpilih();
        if (pesanan == null) {
            Theme.showError(this, "Pilih pesanan terlebih dahulu.");
            return;
        }
        StatusPesanan statusBaru = (StatusPesanan) statusCombo.getSelectedItem();
        if (statusBaru == null) {
            Theme.showError(this, "Tidak ada status berikutnya yang bisa dipilih.");
            return;
        }
        try {
            pesananService.ubahStatus(pesanan.getId(), statusBaru);
            Theme.showInfo(this, "Status pesanan diubah menjadi " + statusBaru.getLabel() + ".");
            refreshData();
        } catch (AplikasiException e) {
            Theme.showError(this, e.getMessage());
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal mengubah status: " + e.getMessage());
        }
    }

    private void batalkanPesanan() {
        Pesanan pesanan = pesananTerpilih();
        if (pesanan == null) {
            Theme.showError(this, "Pilih pesanan terlebih dahulu.");
            return;
        }
        if (!Theme.confirm(this, "Batalkan pesanan " + pesanan.getKode()
                + "? Stok akan dikembalikan.")) {
            return;
        }
        try {
            pesananService.batalkan(pesanan.getId());
            Theme.showInfo(this, "Pesanan berhasil dibatalkan dan stok dikembalikan.");
            refreshData();
        } catch (AplikasiException e) {
            Theme.showError(this, e.getMessage());
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal membatalkan pesanan: " + e.getMessage());
        }
    }

    private Pesanan pesananTerpilih() {
        if (selectedPesananId == null) {
            return null;
        }
        for (Pesanan pesanan : dataPesanan) {
            if (selectedPesananId.equals(pesanan.getId())) {
                return pesanan;
            }
        }
        return null;
    }

    private void tampilkanDetail() {
        Pesanan pesanan = pesananTerpilih();
        if (pesanan == null) {
            return;
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Detail Pesanan " + pesanan.getKode(), JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(650, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setBackground(Theme.CARD);
        header.setBorder(new EmptyBorder(18, 20, 12, 20));
        JLabel kode = new JLabel(pesanan.getKode());
        kode.setFont(new Font("Segoe UI", Font.BOLD, 20));
        kode.setForeground(Theme.TEXT);
        JLabel info = Theme.muted(pesanan.getPelanggan().getNama()
                + "  |  " + FormatUtil.tanggal(pesanan.getTanggalDibuat()));
        JLabel status = Theme.badge(pesanan.getStatus().getLabel(),
                Theme.statusColor(pesanan.getStatus().getLabel()));
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusPanel.setOpaque(false);
        statusPanel.add(status);
        header.add(kode, BorderLayout.NORTH);
        header.add(info, BorderLayout.CENTER);
        header.add(statusPanel, BorderLayout.SOUTH);
        dialog.add(header, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Kode", "Nama", "Harga", "Qty", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (DetailPesanan detail : pesanan.getDetail()) {
            model.addRow(new Object[]{
                detail.getSparePart().getKode(),
                detail.getSparePart().getNama(),
                FormatUtil.rupiah(detail.getHargaSatuan()),
                detail.getJumlah(),
                FormatUtil.rupiah(detail.getSubtotal())
            });
        }
        JTable tabel = Theme.table();
        tabel.setModel(model);
        Theme.styleTableColumns(tabel, 80, 250, 110, 60, 120);
        JScrollPane scroll = Theme.scroll(tabel);
        scroll.setBorder(new EmptyBorder(0, 20, 0, 20));
        dialog.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Theme.CARD);
        footer.setBorder(new EmptyBorder(12, 20, 16, 20));
        JLabel total = new JLabel("Total: " + FormatUtil.rupiah(pesanan.hitungTotal()));
        total.setFont(new Font("Segoe UI", Font.BOLD, 16));
        total.setForeground(Theme.ACCENT_DARK);
        JButton tutup = Theme.primaryButton("Tutup");
        tutup.addActionListener(e -> dialog.dispose());
        footer.add(total, BorderLayout.WEST);
        footer.add(tutup, BorderLayout.EAST);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}
```

## src/kaboygarage/gui/Refreshable.java

```java
package kaboygarage.gui;

/**
 * Kontrak untuk panel yang datanya perlu dimuat ulang saat dibuka.
 */
public interface Refreshable {

    void refreshData();
}
```

## src/kaboygarage/gui/SparePartPanel.java

```java
package kaboygarage.gui;

import kaboygarage.exception.AplikasiException;
import kaboygarage.model.KategoriSparePart;
import kaboygarage.model.SparePart;
import kaboygarage.service.SparePartService;
import kaboygarage.util.Validasi;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel CRUD katalog spare part.
 */
public class SparePartPanel extends JPanel implements Refreshable {

    private final SparePartService service;

    private final JTextField kodeField = Theme.textField();
    private final JTextField namaField = Theme.textField();
    private final JTextField merkField = Theme.textField();
    private final JComboBox<KategoriSparePart> kategoriCombo =
            new JComboBox<>(KategoriSparePart.values());
    private final JTextField hargaField = Theme.textField();
    private final JTextField stokField = Theme.textField();
    private final JTextField stokMinimalField = Theme.textField();
    private final JTextArea deskripsiArea = Theme.textArea(3);

    private final JTextField cariField = Theme.textField();
    private final JTable tabel = Theme.table();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Kode", "Nama", "Merk", "Kategori", "Harga", "Stok", "Min", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final List<SparePart> dataTabel = new ArrayList<>();

    private Long selectedId;

    public SparePartPanel(SparePartService service) {
        this.service = service;
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buatForm(), buatTabel());
        split.setBorder(null);
        split.setOpaque(false);
        split.setResizeWeight(0);
        split.setDividerLocation(350);
        add(split, BorderLayout.CENTER);

        resetForm();
    }

    private JPanel buatForm() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 12));
        card.setPreferredSize(new Dimension(350, 0));

        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setOpaque(false);
        header.add(Theme.bold("Tambah / Ubah Spare Part"), BorderLayout.NORTH);
        header.add(Theme.muted("Isi data lalu klik Simpan"), BorderLayout.CENTER);

        JPanel form = Theme.formPanel();
        GridBagConstraints gbc = new GridBagConstraints();
        int baris = 0;
        Theme.addFormRow(form, gbc, baris++, "Kode", kodeField);
        Theme.addFormRow(form, gbc, baris++, "Nama", namaField);
        Theme.addFormRow(form, gbc, baris++, "Merk", merkField);
        Theme.addFormRow(form, gbc, baris++, "Kategori", kategoriCombo);
        Theme.addFormRow(form, gbc, baris++, "Harga", hargaField);
        Theme.addFormRow(form, gbc, baris++, "Stok", stokField);
        Theme.addFormRow(form, gbc, baris++, "Stok minimal", stokMinimalField);

        JScrollPane deskripsiScroll = Theme.scroll(deskripsiArea);
        deskripsiScroll.setPreferredSize(new Dimension(0, 78));
        Theme.addFormRow(form, gbc, baris++, "Deskripsi", deskripsiScroll);
        Theme.fillForm(form);

        JButton simpan = Theme.primaryButton("Simpan");
        simpan.addActionListener(e -> simpan());
        JButton hapus = Theme.dangerButton("Hapus");
        hapus.addActionListener(e -> hapus());
        JButton reset = Theme.secondaryButton("Baru");
        reset.addActionListener(e -> resetForm());

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tombol.setOpaque(false);
        tombol.add(simpan);
        tombol.add(hapus);
        tombol.add(reset);

        card.add(header, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(tombol, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buatTabel() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 12));

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        JPanel judul = new JPanel(new BorderLayout(0, 3));
        judul.setOpaque(false);
        judul.add(Theme.bold("Daftar Spare Part"), BorderLayout.NORTH);
        judul.add(Theme.muted("Klik baris untuk mengubah data"), BorderLayout.CENTER);
        header.add(judul, BorderLayout.WEST);

        JPanel cari = new JPanel(new BorderLayout(8, 0));
        cari.setOpaque(false);
        JLabel label = Theme.muted("Cari");
        cari.add(label, BorderLayout.WEST);
        cariField.setPreferredSize(new Dimension(230, 32));
        cari.add(cariField, BorderLayout.CENTER);
        header.add(cari, BorderLayout.EAST);

        tabel.setModel(model);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabel.getColumnModel().getColumn(7).setCellRenderer(Theme.statusRenderer());
        Theme.styleTableColumns(tabel, 70, 170, 90, 100, 100, 55, 55, 85);
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                tampilkanPilihan();
            }
        });

        cariField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                muatTabel();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                muatTabel();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                muatTabel();
            }
        });

        card.add(header, BorderLayout.NORTH);
        card.add(Theme.scroll(tabel), BorderLayout.CENTER);
        return card;
    }

    @Override
    public void refreshData() {
        muatTabel();
        if (selectedId != null) {
            pilihBaris(selectedId);
        }
    }

    private void muatTabel() {
        try {
            List<SparePart> hasil = service.cari(cariField.getText());
            dataTabel.clear();
            dataTabel.addAll(hasil);
            model.setRowCount(0);
            for (SparePart sparePart : hasil) {
                model.addRow(new Object[]{
                    sparePart.getKode(),
                    sparePart.getNama(),
                    sparePart.getMerk() == null ? "-" : sparePart.getMerk(),
                    sparePart.getKategori().getLabel(),
                    FormatUtil.rupiah(sparePart.getHarga()),
                    sparePart.getStok(),
                    sparePart.getStokMinimal(),
                    sparePart.getStatusStok()
                });
            }
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal memuat spare part: " + e.getMessage());
        }
    }

    private void tampilkanPilihan() {
        int baris = tabel.getSelectedRow();
        if (baris < 0 || baris >= dataTabel.size()) {
            return;
        }
        SparePart sparePart = dataTabel.get(baris);
        selectedId = sparePart.getId();
        kodeField.setText(sparePart.getKode());
        namaField.setText(sparePart.getNama());
        merkField.setText(sparePart.getMerk());
        kategoriCombo.setSelectedItem(sparePart.getKategori());
        hargaField.setText(sparePart.getHarga().toPlainString());
        stokField.setText(String.valueOf(sparePart.getStok()));
        stokMinimalField.setText(String.valueOf(sparePart.getStokMinimal()));
        deskripsiArea.setText(sparePart.getDeskripsi());
    }

    private void pilihBaris(Long id) {
        for (int i = 0; i < dataTabel.size(); i++) {
            if (id.equals(dataTabel.get(i).getId())) {
                tabel.setRowSelectionInterval(i, i);
                tabel.scrollRectToVisible(tabel.getCellRect(i, 0, true));
                return;
            }
        }
    }

    private void simpan() {
        try {
            SparePart sparePart = new SparePart(
                    kodeField.getText(),
                    namaField.getText(),
                    merkField.getText(),
                    (KategoriSparePart) kategoriCombo.getSelectedItem(),
                    Validasi.parseHarga(hargaField.getText()),
                    Validasi.parseAngka(stokField.getText(), "Stok"));
            sparePart.setId(selectedId);
            sparePart.setStokMinimal(
                    Validasi.parseAngka(stokMinimalField.getText(), "Stok minimal"));
            sparePart.setDeskripsi(deskripsiArea.getText().trim());

            service.simpan(sparePart);
            selectedId = sparePart.getId();
            Theme.showInfo(this, "Spare part berhasil disimpan.");
            refreshData();
        } catch (AplikasiException e) {
            Theme.showError(this, e.getMessage());
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal menyimpan spare part: " + e.getMessage());
        }
    }

    private void hapus() {
        if (selectedId == null) {
            Theme.showError(this, "Pilih spare part yang ingin dihapus.");
            return;
        }
        if (!Theme.confirm(this, "Hapus spare part yang dipilih?")) {
            return;
        }
        try {
            service.hapus(selectedId);
            resetForm();
            refreshData();
            Theme.showInfo(this, "Spare part berhasil dihapus.");
        } catch (AplikasiException e) {
            Theme.showError(this, e.getMessage());
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal menghapus spare part: " + e.getMessage());
        }
    }

    private void resetForm() {
        selectedId = null;
        kodeField.setText("");
        namaField.setText("");
        merkField.setText("");
        kategoriCombo.setSelectedIndex(0);
        hargaField.setText("");
        stokField.setText("0");
        stokMinimalField.setText("3");
        deskripsiArea.setText("");
        tabel.clearSelection();
        try {
            kodeField.setText(service.kodeBerikutnya());
        } catch (RuntimeException ignored) {
            // kode bisa diisi manual bila database belum siap
        }
        kodeField.requestFocusInWindow();
    }
}
```

## src/kaboygarage/gui/Theme.java

```java
package kaboygarage.gui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Tema warna, font, dan komponen standar aplikasi GUI Kaboy Garage.
 */
public final class Theme {

    public static final Color SIDEBAR = new Color(24, 28, 36);
    public static final Color SIDEBAR_ACTIVE = new Color(40, 46, 58);
    public static final Color SIDEBAR_TEXT = new Color(214, 219, 226);
    public static final Color ACCENT = new Color(230, 126, 34);
    public static final Color ACCENT_DARK = new Color(193, 100, 18);
    public static final Color ACCENT_SOFT = new Color(253, 240, 227);
    public static final Color DANGER = new Color(200, 60, 60);
    public static final Color WARNING = new Color(191, 120, 0);
    public static final Color BACKGROUND = new Color(244, 246, 248);
    public static final Color CARD = Color.WHITE;
    public static final Color TEXT = new Color(33, 37, 41);
    public static final Color MUTED = new Color(108, 117, 125);
    public static final Color BORDER = new Color(222, 226, 230);
    public static final Color TABLE_HEADER = new Color(248, 249, 250);

    public static final Font FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font BIG_NUMBER = new Font("Segoe UI", Font.BOLD, 28);

    private Theme() {
    }

    public static void install() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // fallback ke look and feel default
        }
        UIManager.put("control", BACKGROUND);
        UIManager.put("info", CARD);
        UIManager.put("nimbusBase", SIDEBAR);
        UIManager.put("nimbusBlueGrey", new Color(210, 216, 222));
        UIManager.put("nimbusLightBackground", CARD);
        UIManager.put("text", TEXT);
        UIManager.put("Table.alternateRowColor", new Color(250, 251, 252));
        UIManager.put("Table.showGrid", false);
        UIManager.put("defaultFont", FONT);
    }

    public static JButton primaryButton(String text) {
        return new RoundedButton(text, ACCENT, Color.WHITE);
    }

    public static JButton secondaryButton(String text) {
        return new RoundedButton(text, new Color(233, 236, 239), TEXT);
    }

    public static JButton dangerButton(String text) {
        return new RoundedButton(text, DANGER, Color.WHITE);
    }

    public static JButton navButton(String text) {
        JButton button = new JButton(text);
        button.setUI(new BasicButtonUI());
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(SIDEBAR_TEXT);
        button.setBackground(SIDEBAR);
        button.setOpaque(true);
        button.setBorder(new EmptyBorder(12, 20, 12, 20));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!SIDEBAR_ACTIVE.equals(button.getBackground())) {
                    button.setBackground(new Color(32, 37, 47));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!SIDEBAR_ACTIVE.equals(button.getBackground())) {
                    button.setBackground(SIDEBAR);
                }
            }
        });
        return button;
    }

    public static JPanel card() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD);
        panel.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)));
        return panel;
    }

    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(TITLE);
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel subtitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SUBTITLE);
        label.setForeground(MUTED);
        return label;
    }

    public static JLabel bold(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BOLD);
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT);
        label.setForeground(MUTED);
        return label;
    }

    public static JTextField textField() {
        JTextField field = new JTextField();
        field.setFont(FONT);
        field.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(7, 9, 7, 9)));
        return field;
    }

    public static JPasswordField passwordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(FONT);
        field.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(7, 9, 7, 9)));
        return field;
    }

    public static JTextArea textArea(int rows) {
        JTextArea area = new JTextArea(rows, 20);
        area.setFont(FONT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(6, 8, 6, 8));
        area.setBackground(CARD);
        area.setForeground(TEXT);
        return area;
    }

    public static JScrollPane scroll(Component component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(new LineBorder(BORDER, 1, true));
        scrollPane.getViewport().setBackground(CARD);
        return scrollPane;
    }

    public static JTable table() {
        JTable table = new JTable();
        table.setFont(FONT);
        table.setRowHeight(30);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(237, 240, 243));
        table.setSelectionBackground(ACCENT_SOFT);
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(FONT_BOLD);
        table.getTableHeader().setBackground(TABLE_HEADER);
        table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setPreferredSize(new Dimension(0, 34));
        return table;
    }

    public static JComboBox<String> comboBox(String... items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(FONT);
        comboBox.setBackground(CARD);
        return comboBox;
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Terjadi Kesalahan",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Informasi",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Konfirmasi",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                == JOptionPane.YES_OPTION;
    }

    public static Color statusColor(String status) {
        if (status == null) {
            return TEXT;
        }
        String value = status.trim();
        if (value.equalsIgnoreCase("Selesai")
                || value.equalsIgnoreCase("Tersedia")
                || value.equalsIgnoreCase("Aktif")
                || value.equalsIgnoreCase("Baik")) {
            return ACCENT_DARK;
        }
        if (value.equalsIgnoreCase("Diproses")
                || value.equalsIgnoreCase("Siap Diambil")
                || value.equalsIgnoreCase("Dijadwalkan")
                || value.equalsIgnoreCase("Menunggu Jadwal")
                || value.equalsIgnoreCase("Menipis")) {
            return WARNING;
        }
        if (value.equalsIgnoreCase("Dibatalkan")
                || value.equalsIgnoreCase("Habis")) {
            return DANGER;
        }
        return TEXT;
    }

    public static DefaultTableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected,
                                                           boolean hasFocus, int row,
                                                           int column) {
                Component component = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                component.setFont(FONT_BOLD);
                if (!isSelected) {
                    component.setForeground(statusColor(String.valueOf(value)));
                }
                return component;
            }
        };
    }

    public static JPanel formPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new java.awt.GridBagLayout());
        return panel;
    }

    public static void addFormRow(JPanel panel, java.awt.GridBagConstraints gbc,
                                  int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = java.awt.GridBagConstraints.NONE;
        gbc.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gbc.insets = new java.awt.Insets(6, 0, 6, 12);
        panel.add(bold(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(6, 0, 6, 0);
        panel.add(field, gbc);
    }

    public static void fillForm(JPanel panel) {
        java.awt.GridBagConstraints spacer = new java.awt.GridBagConstraints();
        spacer.gridx = 0;
        spacer.gridy = 999;
        spacer.weighty = 1;
        panel.add(new JLabel(""), spacer);
    }

    public static JPanel labelValue(String label, JComponent value) {
        JPanel panel = new JPanel(new java.awt.BorderLayout(0, 4));
        panel.setOpaque(false);
        panel.add(bold(label), java.awt.BorderLayout.NORTH);
        panel.add(value, java.awt.BorderLayout.CENTER);
        return panel;
    }

    public static JPanel toolbar() {
        JPanel panel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);
        return panel;
    }

    public static JPanel rightToolbar() {
        JPanel panel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        panel.setOpaque(false);
        return panel;
    }

    public static void styleTableColumns(JTable table, int... widths) {
        for (int i = 0; i < widths.length && i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    public static JLabel badge(String text, Color color) {
        JLabel label = new JLabel("  " + text + "  ");
        label.setFont(FONT_BOLD);
        label.setForeground(color);
        label.setOpaque(true);
        label.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 24));
        label.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
        return label;
    }

    private static final class RoundedButton extends JButton {

        private final Color baseColor;

        private RoundedButton(String text, Color background, Color foreground) {
            super(text);
            this.baseColor = background;
            setFont(FONT_BOLD);
            setForeground(foreground);
            setBackground(background);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(9, 16, 9, 16));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) {
                        setBackground(baseColor.brighter());
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(baseColor);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isEnabled() ? getBackground() : new Color(205, 210, 216));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }
}
```

## src/kaboygarage/gui/WaitingListPanel.java

```java
package kaboygarage.gui;

import kaboygarage.exception.AplikasiException;
import kaboygarage.model.JenisServis;
import kaboygarage.model.Kendaraan;
import kaboygarage.model.Pelanggan;
import kaboygarage.model.PendaftaranServis;
import kaboygarage.model.StatusServis;
import kaboygarage.service.PelangganService;
import kaboygarage.service.WaitingListService;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Panel pendaftaran servis (waiting list) dan penjadwalannya.
 */
public class WaitingListPanel extends JPanel implements Refreshable {

    private final WaitingListService waitingListService;
    private final PelangganService pelangganService;

    private final JComboBox<Pelanggan> pelangganCombo = new JComboBox<>();
    private final JComboBox<Kendaraan> kendaraanCombo = new JComboBox<>();
    private final JComboBox<JenisServis> jenisCombo = new JComboBox<>(JenisServis.values());
    private final JTextArea keluhanArea = Theme.textArea(2);

    private final JTable tabel = Theme.table();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Kode", "No", "Pelanggan", "Kendaraan", "Jenis",
                "Keluhan", "Jadwal", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final List<PendaftaranServis> data = new java.util.ArrayList<>();
    private Long selectedId;

    private final JComboBox<StatusServis> statusCombo = new JComboBox<>();
    private final JButton jadwalkanButton = Theme.primaryButton("Jadwalkan");
    private final JButton ubahStatusButton = Theme.primaryButton("Ubah Status");
    private final JButton batalkanButton = Theme.dangerButton("Batalkan");

    public WaitingListPanel(WaitingListService waitingListService,
                            PelangganService pelangganService) {
        this.waitingListService = waitingListService;
        this.pelangganService = pelangganService;
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));

        add(buatPanelDaftar(), BorderLayout.NORTH);
        add(buatPanelWaitingList(), BorderLayout.CENTER);

        pelangganCombo.addActionListener(e -> muatKendaraanCombo());
        perbaruiTombol();
    }

    private JPanel buatPanelDaftar() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 10));

        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setOpaque(false);
        header.add(Theme.bold("Daftar Servis Baru"), BorderLayout.NORTH);
        header.add(Theme.muted("Pelanggan mendaftar servis, kendaraan masuk waiting list"),
                BorderLayout.CENTER);

        JPanel isi = new JPanel(new BorderLayout(0, 10));
        isi.setOpaque(false);

        JPanel pilih = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pilih.setOpaque(false);
        pelangganCombo.setPreferredSize(new Dimension(200, 32));
        kendaraanCombo.setPreferredSize(new Dimension(220, 32));
        jenisCombo.setPreferredSize(new Dimension(200, 32));
        pilih.add(Theme.bold("Pelanggan"));
        pilih.add(pelangganCombo);
        pilih.add(Theme.bold("Kendaraan"));
        pilih.add(kendaraanCombo);
        pilih.add(Theme.bold("Jenis servis"));
        pilih.add(jenisCombo);
        isi.add(pilih, BorderLayout.NORTH);

        JPanel keluhan = new JPanel(new BorderLayout(8, 0));
        keluhan.setOpaque(false);
        keluhan.add(Theme.bold("Keluhan"), BorderLayout.WEST);
        JScrollPane scrollKeluhan = Theme.scroll(keluhanArea);
        scrollKeluhan.setPreferredSize(new Dimension(0, 55));
        keluhan.add(scrollKeluhan, BorderLayout.CENTER);

        JButton daftar = Theme.primaryButton("Daftar Servis");
        daftar.addActionListener(e -> daftarServis());
        keluhan.add(daftar, BorderLayout.EAST);
        isi.add(keluhan, BorderLayout.CENTER);

        card.add(header, BorderLayout.NORTH);
        card.add(isi, BorderLayout.CENTER);
        return card;
    }

    private JPanel buatPanelWaitingList() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 10));

        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setOpaque(false);
        JPanel judul = new JPanel(new BorderLayout(0, 3));
        judul.setOpaque(false);
        judul.add(Theme.bold("Waiting List Servis"), BorderLayout.NORTH);
        judul.add(Theme.muted("Atur jadwal dan status pengerjaan"), BorderLayout.CENTER);
        header.add(judul, BorderLayout.WEST);

        JButton refresh = Theme.secondaryButton("Muat Ulang");
        refresh.addActionListener(e -> refreshData());
        JPanel tombolHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        tombolHeader.setOpaque(false);
        tombolHeader.add(refresh);
        header.add(tombolHeader, BorderLayout.EAST);

        tabel.setModel(model);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabel.getColumnModel().getColumn(7).setCellRenderer(Theme.statusRenderer());
        Theme.styleTableColumns(tabel, 125, 40, 130, 170, 120, 180, 120, 110);
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                tampilkanPilihan();
            }
        });

        JPanel aksi = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        aksi.setOpaque(false);
        jadwalkanButton.addActionListener(e -> jadwalkan());
        ubahStatusButton.addActionListener(e -> ubahStatus());
        batalkanButton.addActionListener(e -> batalkan());
        statusCombo.setPreferredSize(new Dimension(160, 32));
        aksi.add(jadwalkanButton);
        aksi.add(statusCombo);
        aksi.add(ubahStatusButton);
        aksi.add(batalkanButton);

        card.add(header, BorderLayout.NORTH);
        card.add(Theme.scroll(tabel), BorderLayout.CENTER);
        card.add(aksi, BorderLayout.SOUTH);
        return card;
    }

    @Override
    public void refreshData() {
        try {
            muatPelangganCombo();
            muatTabel();
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal memuat waiting list: " + e.getMessage());
        }
    }

    private void muatPelangganCombo() {
        Long terpilih = pelangganCombo.getSelectedItem() == null
                ? null : ((Pelanggan) pelangganCombo.getSelectedItem()).getId();
        List<Pelanggan> semua = pelangganService.semua();
        pelangganCombo.removeAllItems();
        for (Pelanggan pelanggan : semua) {
            pelangganCombo.addItem(pelanggan);
        }
        if (terpilih != null) {
            for (int i = 0; i < pelangganCombo.getItemCount(); i++) {
                if (terpilih.equals(pelangganCombo.getItemAt(i).getId())) {
                    pelangganCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
        muatKendaraanCombo();
    }

    private void muatKendaraanCombo() {
        Long terpilih = kendaraanCombo.getSelectedItem() == null
                ? null : ((Kendaraan) kendaraanCombo.getSelectedItem()).getId();
        kendaraanCombo.removeAllItems();
        Pelanggan pelanggan = (Pelanggan) pelangganCombo.getSelectedItem();
        if (pelanggan == null) {
            return;
        }
        try {
            for (Kendaraan kendaraan : pelangganService.kendaraan(pelanggan.getId())) {
                kendaraanCombo.addItem(kendaraan);
            }
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal memuat kendaraan: " + e.getMessage());
            return;
        }
        if (terpilih != null) {
            for (int i = 0; i < kendaraanCombo.getItemCount(); i++) {
                if (terpilih.equals(kendaraanCombo.getItemAt(i).getId())) {
                    kendaraanCombo.setSelectedIndex(i);
                    return;
                }
            }
        }
    }

    private void muatTabel() {
        List<PendaftaranServis> semua = waitingListService.semua();
        data.clear();
        data.addAll(semua);
        model.setRowCount(0);
        for (PendaftaranServis servis : semua) {
            model.addRow(new Object[]{
                servis.getKodeAntrean(),
                servis.getNomorAntrean(),
                servis.getPelanggan().getNama(),
                servis.getKendaraan().getPlatNomor() + " - "
                        + servis.getKendaraan().getMerk(),
                servis.getJenisServis().getLabel(),
                servis.getKeluhan(),
                FormatUtil.tanggal(servis.getJadwalServis()),
                servis.getStatus().getLabel()
            });
        }

        int baris = -1;
        if (selectedId != null) {
            for (int i = 0; i < data.size(); i++) {
                if (selectedId.equals(data.get(i).getId())) {
                    baris = i;
                    break;
                }
            }
        }
        if (baris >= 0) {
            tabel.setRowSelectionInterval(baris, baris);
            tampilkanPilihan();
        } else {
            tabel.clearSelection();
            selectedId = null;
            perbaruiTombol();
        }
    }

    private void daftarServis() {
        Pelanggan pelanggan = (Pelanggan) pelangganCombo.getSelectedItem();
        Kendaraan kendaraan = (Kendaraan) kendaraanCombo.getSelectedItem();
        JenisServis jenis = (JenisServis) jenisCombo.getSelectedItem();
        try {
            PendaftaranServis servis = waitingListService.daftar(
                    pelanggan, kendaraan, jenis, keluhanArea.getText());
            Theme.showInfo(this, "Pendaftaran servis berhasil.\n"
                    + "Kode: " + servis.getKodeAntrean() + "\n"
                    + "Nomor antrean: " + servis.getNomorAntrean());
            keluhanArea.setText("");
            selectedId = servis.getId();
            refreshData();
        } catch (AplikasiException e) {
            Theme.showError(this, e.getMessage());
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal mendaftarkan servis: " + e.getMessage());
        }
    }

    private void tampilkanPilihan() {
        int baris = tabel.getSelectedRow();
        if (baris < 0 || baris >= data.size()) {
            selectedId = null;
            perbaruiTombol();
            return;
        }
        PendaftaranServis servis = data.get(baris);
        selectedId = servis.getId();
        statusCombo.removeAllItems();
        for (StatusServis status : StatusServis.values()) {
            if (servis.getStatus().bisaBerubahKe(status)) {
                statusCombo.addItem(status);
            }
        }
        perbaruiTombol();
    }

    private void perbaruiTombol() {
        PendaftaranServis servis = servisTerpilih();
        boolean adaPilihan = servis != null;
        boolean bisaJadwal = adaPilihan
                && (servis.getStatus() == StatusServis.MENUNGGU
                || servis.getStatus() == StatusServis.DIJADWALKAN);
        jadwalkanButton.setEnabled(bisaJadwal);
        ubahStatusButton.setEnabled(adaPilihan && statusCombo.getItemCount() > 0);
        batalkanButton.setEnabled(adaPilihan
                && servis.getStatus().bisaBerubahKe(StatusServis.DIBATALKAN));
    }

    private PendaftaranServis servisTerpilih() {
        if (selectedId == null) {
            return null;
        }
        for (PendaftaranServis servis : data) {
            if (selectedId.equals(servis.getId())) {
                return servis;
            }
        }
        return null;
    }

    private void jadwalkan() {
        PendaftaranServis servis = servisTerpilih();
        if (servis == null) {
            Theme.showError(this, "Pilih antrean servis terlebih dahulu.");
            return;
        }
        LocalDateTime awal = servis.getJadwalServis() != null
                ? servis.getJadwalServis()
                : LocalDateTime.now().plusDays(1).withHour(9).withMinute(0)
                        .withSecond(0).withNano(0);
        String teks = JOptionPane.showInputDialog(this,
                "Jadwal servis (format yyyy-MM-dd HH:mm):",
                FormatUtil.tanggalInput(awal));
        if (teks == null) {
            return;
        }
        try {
            LocalDateTime jadwal = FormatUtil.parseWaktu(teks);
            waitingListService.jadwalkan(servis.getId(), jadwal);
            Theme.showInfo(this, "Jadwal servis disimpan.");
            refreshData();
        } catch (IllegalArgumentException | AplikasiException e) {
            Theme.showError(this, e.getMessage());
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal menyimpan jadwal: " + e.getMessage());
        }
    }

    private void ubahStatus() {
        PendaftaranServis servis = servisTerpilih();
        if (servis == null) {
            Theme.showError(this, "Pilih antrean servis terlebih dahulu.");
            return;
        }
        StatusServis statusBaru = (StatusServis) statusCombo.getSelectedItem();
        if (statusBaru == null) {
            Theme.showError(this, "Tidak ada status berikutnya yang bisa dipilih.");
            return;
        }
        try {
            waitingListService.ubahStatus(servis.getId(), statusBaru);
            Theme.showInfo(this, "Status servis diubah menjadi " + statusBaru.getLabel() + ".");
            refreshData();
        } catch (AplikasiException e) {
            Theme.showError(this, e.getMessage());
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal mengubah status: " + e.getMessage());
        }
    }

    private void batalkan() {
        PendaftaranServis servis = servisTerpilih();
        if (servis == null) {
            Theme.showError(this, "Pilih antrean servis terlebih dahulu.");
            return;
        }
        if (!Theme.confirm(this, "Batalkan pendaftaran servis "
                + servis.getKodeAntrean() + "?")) {
            return;
        }
        try {
            waitingListService.batalkan(servis.getId());
            Theme.showInfo(this, "Pendaftaran servis dibatalkan.");
            refreshData();
        } catch (AplikasiException e) {
            Theme.showError(this, e.getMessage());
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal membatalkan servis: " + e.getMessage());
        }
    }
}
```

## src/kaboygarage/main/Main.java

```java
package kaboygarage.main;

import kaboygarage.gui.GuiMain;

/**
 * Launcher kompatibilitas. Entry point utama ada di {@link GuiMain}.
 */
public class Main {

    public static void main(String[] args) {
        GuiMain.main(args);
    }
}
```

## src/kaboygarage/model/Admin.java

```java
package kaboygarage.model;

import kaboygarage.util.Validasi;

/**
 * INHERITANCE: Admin adalah Orang yang memiliki akun untuk masuk ke aplikasi.
 */
public class Admin extends Orang {

    private String username;
    private String passwordHash;
    private boolean aktif = true;

    public Admin(String nama, String noHp, String username, String passwordHash) {
        super(nama, noHp);
        this.username = Validasi.wajibIsi(username, "Username");
        this.passwordHash = Validasi.wajibIsi(passwordHash, "Password hash");
    }

    @Override
    public String getPeran() {
        return "Admin";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = Validasi.wajibIsi(username, "Username");
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = Validasi.wajibIsi(passwordHash, "Password hash");
    }

    public boolean isAktif() {
        return aktif;
    }

    public void setAktif(boolean aktif) {
        this.aktif = aktif;
    }

    @Override
    public String toString() {
        return getNama() + " (" + username + ")";
    }
}
```

## src/kaboygarage/model/DetailPesanan.java

```java
package kaboygarage.model;

import java.math.BigDecimal;

/**
 * Satu baris item pada pesanan. Harga disimpan sebagai snapshot agar total
 * pesanan lama tidak berubah ketika harga katalog diperbarui.
 */
public class DetailPesanan {

    private Long id;
    private Pesanan pesanan;
    private SparePart sparePart;
    private int jumlah;
    private BigDecimal hargaSatuan;

    public DetailPesanan(Pesanan pesanan, SparePart sparePart, int jumlah,
                         BigDecimal hargaSatuan) {
        this.pesanan = pesanan;
        this.sparePart = sparePart;
        this.jumlah = jumlah;
        this.hargaSatuan = hargaSatuan;
    }

    public BigDecimal getSubtotal() {
        if (hargaSatuan == null) {
            return BigDecimal.ZERO;
        }
        return hargaSatuan.multiply(BigDecimal.valueOf(jumlah));
    }

    public void tambahJumlah(int tambahan) {
        if (tambahan <= 0) {
            throw new IllegalArgumentException("Tambahan jumlah harus positif.");
        }
        this.jumlah += tambahan;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pesanan getPesanan() {
        return pesanan;
    }

    public void setPesanan(Pesanan pesanan) {
        this.pesanan = pesanan;
    }

    public SparePart getSparePart() {
        return sparePart;
    }

    public void setSparePart(SparePart sparePart) {
        this.sparePart = sparePart;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public BigDecimal getHargaSatuan() {
        return hargaSatuan;
    }

    public void setHargaSatuan(BigDecimal hargaSatuan) {
        this.hargaSatuan = hargaSatuan;
    }
}
```

## src/kaboygarage/model/JenisServis.java

```java
package kaboygarage.model;

/**
 * Jenis layanan servis yang tersedia di Kaboy Garage beserta estimasi durasinya.
 */
public enum JenisServis {

    GANTI_OLI("Ganti Oli", 30),
    SERVIS_RINGAN("Servis Ringan", 60),
    TUNE_UP("Tune Up", 90),
    SERVIS_BESAR("Servis Besar", 180),
    PERBAIKAN_MESIN("Perbaikan Mesin", 240);

    private final String label;
    private final int estimasiMenit;

    JenisServis(String label, int estimasiMenit) {
        this.label = label;
        this.estimasiMenit = estimasiMenit;
    }

    public String getLabel() {
        return label;
    }

    public int getEstimasiMenit() {
        return estimasiMenit;
    }

    @Override
    public String toString() {
        return label + " (" + estimasiMenit + " menit)";
    }
}
```

## src/kaboygarage/model/KategoriSparePart.java

```java
package kaboygarage.model;

/**
 * Kategori spare part motor yang dijual pada Kaboy Garage.
 */
public enum KategoriSparePart {

    MESIN("Mesin"),
    KELISTRIKAN("Kelistrikan"),
    BODY("Body & Panel"),
    OLI_CAIRAN("Oli & Cairan"),
    AKSESORIS("Aksesoris");

    private final String label;

    KategoriSparePart(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
```

## src/kaboygarage/model/Kendaraan.java

```java
package kaboygarage.model;

import kaboygarage.util.Validasi;

/**
 * Kendaraan milik pelanggan yang didaftarkan untuk servis.
 *
 * <p>Relasi ke {@link Pelanggan} menunjukkan composition/association: satu
 * kendaraan pasti dimiliki satu pelanggan.</p>
 */
public class Kendaraan {

    private Long id;
    private Pelanggan pelanggan;
    private String merk;
    private String model;
    private String platNomor;
    private int tahun;

    public Kendaraan(Pelanggan pelanggan, String merk, String model,
                     String platNomor, int tahun) {
        setPelanggan(pelanggan);
        this.merk = Validasi.wajibIsi(merk, "Merk kendaraan");
        this.model = Validasi.wajibIsi(model, "Model kendaraan");
        this.platNomor = Validasi.wajibIsi(platNomor, "Plat nomor").toUpperCase();
        if (tahun < 1950 || tahun > java.time.Year.now().getValue() + 1) {
            throw new IllegalArgumentException("Tahun kendaraan tidak wajar.");
        }
        this.tahun = tahun;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pelanggan getPelanggan() {
        return pelanggan;
    }

    public final void setPelanggan(Pelanggan pelanggan) {
        if (pelanggan == null) {
            throw new IllegalArgumentException("Pelanggan kendaraan wajib dipilih.");
        }
        this.pelanggan = pelanggan;
    }

    public String getMerk() {
        return merk;
    }

    public void setMerk(String merk) {
        this.merk = Validasi.wajibIsi(merk, "Merk kendaraan");
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = Validasi.wajibIsi(model, "Model kendaraan");
    }

    public String getPlatNomor() {
        return platNomor;
    }

    public void setPlatNomor(String platNomor) {
        this.platNomor = Validasi.wajibIsi(platNomor, "Plat nomor").toUpperCase();
    }

    public int getTahun() {
        return tahun;
    }

    public void setTahun(int tahun) {
        this.tahun = tahun;
    }

    public String getDeskripsi() {
        return merk + " " + model + " (" + platNomor + ")";
    }

    @Override
    public String toString() {
        return platNomor + " - " + merk + " " + model;
    }
}
```

## src/kaboygarage/model/Orang.java

```java
package kaboygarage.model;

import kaboygarage.util.Validasi;

import java.util.Objects;

/**
 * ABSTRACT CLASS: induk dari Admin dan Pelanggan.
 *
 * <p>Semua atribut private (encapsulation). Class ini tidak bisa dibuat
 * langsung karena perannya ditentukan oleh subclass melalui
 * {@link #getPeran()} (abstraction + polymorphism).</p>
 */
public abstract class Orang {

    private Long id;
    private String nama;
    private String noHp;

    protected Orang() {
    }

    protected Orang(String nama, String noHp) {
        setNama(nama);
        setNoHp(noHp);
    }

    /**
     * ABSTRACT METHOD: setiap turunan wajib menjelaskan perannya sendiri.
     */
    public abstract String getPeran();

    public String tampilInfo() {
        return getNama() + " (" + getPeran() + ")";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public final void setNama(String nama) {
        this.nama = Validasi.wajibIsi(nama, "Nama");
    }

    public String getNoHp() {
        return noHp;
    }

    public final void setNoHp(String noHp) {
        this.noHp = Validasi.wajibIsi(noHp, "Nomor HP");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Orang)) {
            return false;
        }
        Orang lain = (Orang) obj;
        return id != null && Objects.equals(id, lain.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
```

## src/kaboygarage/model/Pelanggan.java

```java
package kaboygarage.model;

/**
 * INHERITANCE: Pelanggan adalah Orang yang memesan spare part dan/atau
 * mendaftarkan kendaraannya untuk servis.
 */
public class Pelanggan extends Orang {

    private String email;
    private String alamat;

    public Pelanggan(String nama, String noHp, String email, String alamat) {
        super(nama, noHp);
        this.email = email;
        this.alamat = alamat;
    }

    @Override
    public String getPeran() {
        return "Pelanggan";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    @Override
    public String toString() {
        return getNama() + " - " + getNoHp();
    }
}
```

## src/kaboygarage/model/PendaftaranServis.java

```java
package kaboygarage.model;

import kaboygarage.exception.AplikasiException;

import java.time.LocalDateTime;

/**
 * Pendaftaran servis kendaraan (waiting list) beserta nomor antrean dan jadwal.
 */
public class PendaftaranServis {

    private Long id;
    private String kodeAntrean;
    private int nomorAntrean;
    private Pelanggan pelanggan;
    private Kendaraan kendaraan;
    private JenisServis jenisServis;
    private String keluhan;
    private LocalDateTime tanggalDiajukan;
    private LocalDateTime jadwalServis;
    private StatusServis status = StatusServis.MENUNGGU;
    private String catatanAdmin;

    public PendaftaranServis(String kodeAntrean, int nomorAntrean, Pelanggan pelanggan,
                             Kendaraan kendaraan, JenisServis jenisServis, String keluhan) {
        if (kodeAntrean == null || kodeAntrean.isBlank()) {
            throw new AplikasiException("Kode antrean wajib diisi.");
        }
        if (pelanggan == null || kendaraan == null) {
            throw new AplikasiException("Pelanggan dan kendaraan wajib dipilih.");
        }
        if (jenisServis == null) {
            throw new AplikasiException("Jenis servis wajib dipilih.");
        }
        if (keluhan == null || keluhan.isBlank()) {
            throw new AplikasiException("Keluhan wajib diisi.");
        }
        this.kodeAntrean = kodeAntrean;
        this.nomorAntrean = nomorAntrean;
        this.pelanggan = pelanggan;
        this.kendaraan = kendaraan;
        this.jenisServis = jenisServis;
        this.keluhan = keluhan.trim();
        this.tanggalDiajukan = LocalDateTime.now();
        this.status = StatusServis.MENUNGGU;
    }

    /**
     * Admin menetapkan jadwal servis. Status otomatis berpindah ke DIJADWALKAN.
     */
    public void jadwalkan(LocalDateTime jadwal) {
        if (jadwal == null) {
            throw new AplikasiException("Jadwal servis wajib diisi.");
        }
        if (jadwal.isBefore(tanggalDiajukan)) {
            throw new AplikasiException("Jadwal servis tidak boleh sebelum tanggal pendaftaran.");
        }
        if (status != StatusServis.MENUNGGU && status != StatusServis.DIJADWALKAN) {
            throw new AplikasiException("Jadwal servis tidak dapat diubah pada status "
                    + status.getLabel() + ".");
        }
        this.jadwalServis = jadwal;
        if (status == StatusServis.MENUNGGU) {
            ubahStatus(StatusServis.DIJADWALKAN);
        }
    }

    public void ubahStatus(StatusServis statusBaru) {
        if (!status.bisaBerubahKe(statusBaru)) {
            throw new AplikasiException("Status servis tidak dapat diubah dari "
                    + status.getLabel() + " menjadi "
                    + (statusBaru == null ? "-" : statusBaru.getLabel()) + ".");
        }
        this.status = statusBaru;
    }

    public void batalkan() {
        ubahStatus(StatusServis.DIBATALKAN);
    }

    /**
     * Estimasi waktu selesai dihitung dari jadwal + durasi jenis servis.
     */
    public LocalDateTime getEstimasiSelesai() {
        if (jadwalServis == null || jenisServis == null) {
            return null;
        }
        return jadwalServis.plusMinutes(jenisServis.getEstimasiMenit());
    }

    public boolean isSelesai() {
        return status.isSelesai();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKodeAntrean() {
        return kodeAntrean;
    }

    public void setKodeAntrean(String kodeAntrean) {
        this.kodeAntrean = kodeAntrean;
    }

    public int getNomorAntrean() {
        return nomorAntrean;
    }

    public void setNomorAntrean(int nomorAntrean) {
        this.nomorAntrean = nomorAntrean;
    }

    public Pelanggan getPelanggan() {
        return pelanggan;
    }

    public void setPelanggan(Pelanggan pelanggan) {
        this.pelanggan = pelanggan;
    }

    public Kendaraan getKendaraan() {
        return kendaraan;
    }

    public void setKendaraan(Kendaraan kendaraan) {
        this.kendaraan = kendaraan;
    }

    public JenisServis getJenisServis() {
        return jenisServis;
    }

    public void setJenisServis(JenisServis jenisServis) {
        this.jenisServis = jenisServis;
    }

    public String getKeluhan() {
        return keluhan;
    }

    public void setKeluhan(String keluhan) {
        this.keluhan = keluhan;
    }

    public LocalDateTime getTanggalDiajukan() {
        return tanggalDiajukan;
    }

    public void setTanggalDiajukan(LocalDateTime tanggalDiajukan) {
        this.tanggalDiajukan = tanggalDiajukan;
    }

    public LocalDateTime getJadwalServis() {
        return jadwalServis;
    }

    public void setJadwalServis(LocalDateTime jadwalServis) {
        this.jadwalServis = jadwalServis;
    }

    public StatusServis getStatus() {
        return status;
    }

    public void setStatus(StatusServis status) {
        this.status = status;
    }

    public String getCatatanAdmin() {
        return catatanAdmin;
    }

    public void setCatatanAdmin(String catatanAdmin) {
        this.catatanAdmin = catatanAdmin;
    }

    @Override
    public String toString() {
        return kodeAntrean + " - " + kendaraan.getPlatNomor()
                + " (" + status.getLabel() + ")";
    }
}
```

## src/kaboygarage/model/Pesanan.java

```java
package kaboygarage.model;

import kaboygarage.exception.AplikasiException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pesanan spare part yang dibuat pelanggan.
 *
 * <p>Pesanan memiliki banyak detail item (one-to-many). Total selalu dihitung
 * dari detail sehingga tidak ada data uang yang terduplikasi.</p>
 */
public class Pesanan {

    private Long id;
    private String kode;
    private Pelanggan pelanggan;
    private LocalDateTime tanggalDibuat;
    private StatusPesanan status = StatusPesanan.BARU;
    private String catatan;
    private final List<DetailPesanan> detail = new ArrayList<>();

    public Pesanan(String kode, Pelanggan pelanggan) {
        this(kode, pelanggan, LocalDateTime.now());
    }

    public Pesanan(String kode, Pelanggan pelanggan, LocalDateTime tanggalDibuat) {
        if (kode == null || kode.isBlank()) {
            throw new AplikasiException("Kode pesanan wajib diisi.");
        }
        if (pelanggan == null) {
            throw new AplikasiException("Pelanggan pesanan wajib dipilih.");
        }
        this.kode = kode;
        this.pelanggan = pelanggan;
        this.tanggalDibuat = tanggalDibuat;
        this.status = StatusPesanan.BARU;
    }

    /**
     * Menambahkan item ke pesanan. Bila spare part yang sama sudah ada,
     * jumlahnya digabung agar daftar item tetap rapi.
     */
    public void tambahDetail(SparePart sparePart, int jumlah) {
        tambahDetail(sparePart, jumlah, sparePart == null ? null : sparePart.getHarga());
    }

    /**
     * Menambahkan item dengan harga snapshot (dipakai saat membaca dari
     * database agar total pesanan lama tidak berubah).
     */
    public void tambahDetail(SparePart sparePart, int jumlah, BigDecimal hargaSatuan) {
        if (sparePart == null) {
            throw new AplikasiException("Spare part tidak boleh kosong.");
        }
        if (jumlah <= 0) {
            throw new AplikasiException("Jumlah item harus lebih besar dari 0.");
        }
        for (DetailPesanan item : detail) {
            if (item.getSparePart() != null
                    && item.getSparePart().getId() != null
                    && item.getSparePart().getId().equals(sparePart.getId())) {
                item.tambahJumlah(jumlah);
                return;
            }
        }
        detail.add(new DetailPesanan(this, sparePart, jumlah, hargaSatuan));
    }

    public void hapusDetail(DetailPesanan item) {
        if (item != null) {
            detail.remove(item);
        }
    }

    public BigDecimal hitungTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (DetailPesanan item : detail) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }

    public int hitungTotalItem() {
        int total = 0;
        for (DetailPesanan item : detail) {
            total += item.getJumlah();
        }
        return total;
    }

    /**
     * Mengubah status dengan memeriksa alur status yang diizinkan.
     */
    public void ubahStatus(StatusPesanan statusBaru) {
        if (!status.bisaBerubahKe(statusBaru)) {
            throw new AplikasiException("Status pesanan tidak dapat diubah dari "
                    + status.getLabel() + " menjadi "
                    + (statusBaru == null ? "-" : statusBaru.getLabel()) + ".");
        }
        this.status = statusBaru;
    }

    public void batalkan() {
        ubahStatus(StatusPesanan.DIBATALKAN);
    }

    public boolean isSelesai() {
        return status.isSelesai();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKode() {
        return kode;
    }

    public void setKode(String kode) {
        this.kode = kode;
    }

    public Pelanggan getPelanggan() {
        return pelanggan;
    }

    public void setPelanggan(Pelanggan pelanggan) {
        this.pelanggan = pelanggan;
    }

    public LocalDateTime getTanggalDibuat() {
        return tanggalDibuat;
    }

    public void setTanggalDibuat(LocalDateTime tanggalDibuat) {
        this.tanggalDibuat = tanggalDibuat;
    }

    public StatusPesanan getStatus() {
        return status;
    }

    public void setStatus(StatusPesanan status) {
        this.status = status;
    }

    public String getCatatan() {
        return catatan;
    }

    public void setCatatan(String catatan) {
        this.catatan = catatan;
    }

    /**
     * Read-only agar detail hanya bisa diubah lewat method tambahDetail().
     */
    public List<DetailPesanan> getDetail() {
        return Collections.unmodifiableList(detail);
    }

    @Override
    public String toString() {
        return kode + " - " + pelanggan.getNama() + " (" + status.getLabel() + ")";
    }
}
```

## src/kaboygarage/model/SparePart.java

```java
package kaboygarage.model;

import kaboygarage.exception.StokTidakCukupException;
import kaboygarage.util.Validasi;

import java.math.BigDecimal;

/**
 * Spare part motor yang dijual pada katalog Kaboy Garage.
 *
 * <p>Stok hanya boleh berubah melalui {@link #kurangiStok(int)} dan
 * {@link #tambahStok(int)} agar aturan stok tidak tersebar di GUI
 * (encapsulation perilaku).</p>
 */
public class SparePart {

    private Long id;
    private String kode;
    private String nama;
    private String merk;
    private KategoriSparePart kategori;
    private BigDecimal harga;
    private int stok;
    private int stokMinimal = 3;
    private String deskripsi;

    public SparePart(String kode, String nama, String merk, KategoriSparePart kategori,
                     BigDecimal harga, int stok) {
        this.kode = Validasi.wajibIsi(kode, "Kode spare part");
        this.nama = Validasi.wajibIsi(nama, "Nama spare part");
        this.merk = merk;
        setKategori(kategori);
        setHarga(harga);
        setStok(stok);
    }

    /**
     * Mengurangi stok setelah pesanan dibuat.
     *
     * @throws StokTidakCukupException bila stok lebih kecil dari jumlah yang diminta
     */
    public void kurangiStok(int jumlah) {
        Validasi.wajibPositif(jumlah, "Jumlah pengurangan stok");
        if (stok < jumlah) {
            throw new StokTidakCukupException(nama, stok, jumlah);
        }
        stok -= jumlah;
    }

    public void tambahStok(int jumlah) {
        Validasi.wajibPositif(jumlah, "Jumlah penambahan stok");
        stok += jumlah;
    }

    public boolean isStokMenipis() {
        return stok > 0 && stok <= stokMinimal;
    }

    public boolean isHabis() {
        return stok <= 0;
    }

    public boolean isTersedia(int jumlah) {
        return stok >= jumlah;
    }

    public String getStatusStok() {
        if (isHabis()) {
            return "Habis";
        }
        return isStokMenipis() ? "Menipis" : "Tersedia";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKode() {
        return kode;
    }

    public void setKode(String kode) {
        this.kode = Validasi.wajibIsi(kode, "Kode spare part");
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = Validasi.wajibIsi(nama, "Nama spare part");
    }

    public String getMerk() {
        return merk;
    }

    public void setMerk(String merk) {
        this.merk = merk;
    }

    public KategoriSparePart getKategori() {
        return kategori;
    }

    public final void setKategori(KategoriSparePart kategori) {
        if (kategori == null) {
            throw new IllegalArgumentException("Kategori spare part wajib dipilih.");
        }
        this.kategori = kategori;
    }

    public BigDecimal getHarga() {
        return harga;
    }

    public final void setHarga(BigDecimal harga) {
        if (harga == null || harga.signum() <= 0) {
            throw new IllegalArgumentException("Harga spare part harus lebih besar dari 0.");
        }
        this.harga = harga;
    }

    public int getStok() {
        return stok;
    }

    public void setStok(int stok) {
        if (stok < 0) {
            throw new IllegalArgumentException("Stok tidak boleh negatif.");
        }
        this.stok = stok;
    }

    public int getStokMinimal() {
        return stokMinimal;
    }

    public void setStokMinimal(int stokMinimal) {
        if (stokMinimal < 0) {
            throw new IllegalArgumentException("Stok minimal tidak boleh negatif.");
        }
        this.stokMinimal = stokMinimal;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    @Override
    public String toString() {
        return kode + " - " + nama;
    }
}
```

## src/kaboygarage/model/StatusPesanan.java

```java
package kaboygarage.model;

/**
 * Status perjalanan sebuah pesanan spare part.
 *
 * <p>Perubahan status divalidasi oleh enum ini agar alur pesanan tidak
 * melompati tahapan (misalnya BARU langsung SELESAI).</p>
 */
public enum StatusPesanan {

    BARU("Baru"),
    DIPROSES("Diproses"),
    SIAP_DIAMBIL("Siap Diambil"),
    SELESAI("Selesai"),
    DIBATALKAN("Dibatalkan");

    private final String label;

    StatusPesanan(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * @return {@code true} bila status saat ini boleh berpindah ke {@code tujuan}.
     */
    public boolean bisaBerubahKe(StatusPesanan tujuan) {
        if (tujuan == null) {
            return false;
        }
        switch (this) {
            case BARU:
                return tujuan == DIPROSES || tujuan == DIBATALKAN;
            case DIPROSES:
                return tujuan == SIAP_DIAMBIL || tujuan == DIBATALKAN;
            case SIAP_DIAMBIL:
                return tujuan == SELESAI;
            default:
                return false;
        }
    }

    public boolean isSelesai() {
        return this == SELESAI || this == DIBATALKAN;
    }

    @Override
    public String toString() {
        return label;
    }
}
```

## src/kaboygarage/model/StatusServis.java

```java
package kaboygarage.model;

/**
 * Status antrean / pengerjaan servis kendaraan.
 */
public enum StatusServis {

    MENUNGGU("Menunggu Jadwal"),
    DIJADWALKAN("Dijadwalkan"),
    DIKERJAKAN("Sedang Dikerjakan"),
    SELESAI("Selesai"),
    DIBATALKAN("Dibatalkan");

    private final String label;

    StatusServis(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean bisaBerubahKe(StatusServis tujuan) {
        if (tujuan == null) {
            return false;
        }
        switch (this) {
            case MENUNGGU:
                return tujuan == DIJADWALKAN || tujuan == DIBATALKAN;
            case DIJADWALKAN:
                return tujuan == DIKERJAKAN || tujuan == DIBATALKAN;
            case DIKERJAKAN:
                return tujuan == SELESAI;
            default:
                return false;
        }
    }

    public boolean isSelesai() {
        return this == SELESAI || this == DIBATALKAN;
    }

    @Override
    public String toString() {
        return label;
    }
}
```

## src/kaboygarage/service/AuthService.java

```java
package kaboygarage.service;

import kaboygarage.dao.AdminDao;
import kaboygarage.exception.AplikasiException;
import kaboygarage.model.Admin;
import kaboygarage.util.Validasi;

/**
 * Service autentikasi admin.
 */
public class AuthService {

    private final AdminDao adminDao = new AdminDao();

    public Admin login(String username, String password) {
        String user = Validasi.wajibIsi(username, "Username");
        String pass = Validasi.wajibIsi(password, "Password");
        Admin admin = adminDao.login(user, pass);
        if (admin == null) {
            throw new AplikasiException("Username atau password salah.");
        }
        return admin;
    }
}
```

## src/kaboygarage/service/DashboardService.java

```java
package kaboygarage.service;

import kaboygarage.dao.PelangganDao;
import kaboygarage.dao.PendaftaranServisDao;
import kaboygarage.dao.PesananDao;
import kaboygarage.dao.SparePartDao;
import kaboygarage.dto.Statistik;
import kaboygarage.model.PendaftaranServis;
import kaboygarage.model.Pesanan;
import kaboygarage.model.SparePart;

import java.util.List;

/**
 * Service yang menyiapkan ringkasan data untuk dashboard.
 */
public class DashboardService {

    private final SparePartDao sparePartDao = new SparePartDao();
    private final PesananDao pesananDao = new PesananDao();
    private final PendaftaranServisDao servisDao = new PendaftaranServisDao();
    private final PelangganDao pelangganDao = new PelangganDao();

    public Statistik statistik() {
        return new Statistik(
                sparePartDao.count(),
                pesananDao.countAktif(),
                servisDao.countAktif(),
                pelangganDao.count(),
                sparePartDao.countMenipis());
    }

    public List<SparePart> stokMenipis() {
        return sparePartDao.findMenipis();
    }

    public List<Pesanan> pesananTerbaru(int jumlah) {
        List<Pesanan> semua = pesananDao.findAll();
        if (semua.size() <= jumlah) {
            return semua;
        }
        return semua.subList(0, jumlah);
    }

    public List<PendaftaranServis> antreanTerbaru(int jumlah) {
        List<PendaftaranServis> semua = servisDao.findAll();
        if (semua.size() <= jumlah) {
            return semua;
        }
        return semua.subList(0, jumlah);
    }
}
```

## src/kaboygarage/service/PelangganService.java

```java
package kaboygarage.service;

import kaboygarage.dao.KendaraanDao;
import kaboygarage.dao.PelangganDao;
import kaboygarage.exception.AplikasiException;
import kaboygarage.model.Kendaraan;
import kaboygarage.model.Pelanggan;

import java.util.List;

/**
 * Service pengelolaan pelanggan dan kendaraannya.
 */
public class PelangganService {

    private final PelangganDao pelangganDao = new PelangganDao();
    private final KendaraanDao kendaraanDao = new KendaraanDao();

    public List<Pelanggan> semua() {
        return pelangganDao.findAll();
    }

    public Pelanggan cari(Long id) {
        return pelangganDao.findById(id);
    }

    public void simpan(Pelanggan pelanggan) {
        if (pelanggan == null) {
            throw new AplikasiException("Data pelanggan tidak boleh kosong.");
        }
        if (pelanggan.getId() == null) {
            pelangganDao.insert(pelanggan);
        } else {
            pelangganDao.update(pelanggan);
        }
    }

    public void hapus(Long id) {
        if (id == null) {
            throw new AplikasiException("Pelanggan wajib dipilih.");
        }
        pelangganDao.delete(id);
    }

    public List<Kendaraan> kendaraan(Long pelangganId) {
        if (pelangganId == null) {
            throw new AplikasiException("Pelanggan wajib dipilih.");
        }
        return kendaraanDao.findByPelanggan(pelangganId);
    }

    public List<Kendaraan> semuaKendaraan() {
        return kendaraanDao.findAll();
    }

    public void simpanKendaraan(Kendaraan kendaraan) {
        if (kendaraan == null) {
            throw new AplikasiException("Data kendaraan tidak boleh kosong.");
        }
        if (kendaraan.getPelanggan() == null || kendaraan.getPelanggan().getId() == null) {
            throw new AplikasiException("Pilih pelanggan pemilik kendaraan terlebih dahulu.");
        }
        if (kendaraan.getId() == null) {
            kendaraanDao.insert(kendaraan);
        } else {
            kendaraanDao.update(kendaraan);
        }
    }

    public void hapusKendaraan(Long id) {
        if (id == null) {
            throw new AplikasiException("Kendaraan wajib dipilih.");
        }
        kendaraanDao.delete(id);
    }

    public int totalPelanggan() {
        return pelangganDao.count();
    }

    public int totalKendaraan() {
        return kendaraanDao.count();
    }
}
```

## src/kaboygarage/service/PesananService.java

```java
package kaboygarage.service;

import kaboygarage.dao.PesananDao;
import kaboygarage.dao.SparePartDao;
import kaboygarage.db.Database;
import kaboygarage.dto.ItemPesanan;
import kaboygarage.exception.AplikasiException;
import kaboygarage.exception.StokTidakCukupException;
import kaboygarage.model.DetailPesanan;
import kaboygarage.model.Pelanggan;
import kaboygarage.model.Pesanan;
import kaboygarage.model.SparePart;
import kaboygarage.model.StatusPesanan;
import kaboygarage.util.KodeGenerator;
import kaboygarage.util.Validasi;

import java.util.ArrayList;
import java.util.List;

/**
 * Service pembuatan pesanan dan perubahan statusnya.
 *
 * <p>Pembuatan pesanan dan pengurangan stok dijalankan dalam satu transaksi
 * database (atomic), sehingga tidak ada pesanan yang tersimpan tanpa stok
 * yang benar.</p>
 */
public class PesananService {

    private final PesananDao pesananDao = new PesananDao();
    private final SparePartDao sparePartDao = new SparePartDao();

    public Pesanan buatPesanan(Pelanggan pelanggan, List<ItemPesanan> items, String catatan) {
        Validasi.wajibAda(pelanggan, "Pelanggan");
        if (items == null || items.isEmpty()) {
            throw new AplikasiException("Tambahkan minimal satu spare part ke pesanan.");
        }

        Pesanan pesanan = new Pesanan(KodeGenerator.kodePesanan(), pelanggan);
        for (ItemPesanan item : items) {
            if (item.getSparePart() == null || item.getSparePart().getId() == null) {
                throw new AplikasiException("Spare part pesanan tidak valid.");
            }
            SparePart terkini = sparePartDao.findById(item.getSparePart().getId());
            if (terkini == null) {
                throw new AplikasiException("Spare part sudah tidak tersedia di katalog.");
            }
            pesanan.tambahDetail(terkini, item.getJumlah());
        }
        pesanan.setCatatan(catatan == null ? null : catatan.trim());

        return Database.inTransaction(connection -> {
            for (DetailPesanan detail : pesanan.getDetail()) {
                boolean cukup = sparePartDao.kurangiStok(
                        connection, detail.getSparePart().getId(), detail.getJumlah());
                if (!cukup) {
                    SparePart cek = sparePartDao.findById(detail.getSparePart().getId());
                    int stok = cek == null ? 0 : cek.getStok();
                    throw new StokTidakCukupException(
                            detail.getSparePart().getNama(), stok, detail.getJumlah());
                }
            }
            pesananDao.insert(connection, pesanan);
            return pesanan;
        });
    }

    public List<Pesanan> semua() {
        return pesananDao.findAll();
    }

    public Pesanan cari(Long id) {
        return pesananDao.findById(id);
    }

    public List<Pesanan> terbaru(int jumlah) {
        List<Pesanan> semua = pesananDao.findAll();
        if (semua.size() <= jumlah) {
            return semua;
        }
        return new ArrayList<>(semua.subList(0, jumlah));
    }

    /**
     * Mengubah status pesanan. Bila pesanan dibatalkan, stok dikembalikan.
     */
    public Pesanan ubahStatus(Long id, StatusPesanan statusBaru) {
        Pesanan pesanan = pesananDao.findById(id);
        if (pesanan == null) {
            throw new AplikasiException("Pesanan tidak ditemukan.");
        }
        pesanan.ubahStatus(statusBaru);

        if (statusBaru == StatusPesanan.DIBATALKAN) {
            Database.inTransaction(connection -> {
                for (DetailPesanan detail : pesanan.getDetail()) {
                    sparePartDao.tambahStok(
                            connection, detail.getSparePart().getId(), detail.getJumlah());
                }
                pesananDao.updateStatus(connection, id, statusBaru);
                return null;
            });
        } else {
            pesananDao.updateStatus(id, statusBaru);
        }
        return pesanan;
    }

    public Pesanan batalkan(Long id) {
        return ubahStatus(id, StatusPesanan.DIBATALKAN);
    }

    public int totalAktif() {
        return pesananDao.countAktif();
    }
}
```

## src/kaboygarage/service/SparePartService.java

```java
package kaboygarage.service;

import kaboygarage.dao.SparePartDao;
import kaboygarage.db.DataAccessException;
import kaboygarage.exception.AplikasiException;
import kaboygarage.model.SparePart;

import java.util.List;

/**
 * Service pengelolaan katalog spare part.
 */
public class SparePartService {

    private final SparePartDao sparePartDao = new SparePartDao();

    public List<SparePart> semua() {
        return sparePartDao.findAll();
    }

    public List<SparePart> cari(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return sparePartDao.findAll();
        }
        return sparePartDao.findByKeyword(keyword.trim());
    }

    public SparePart cari(Long id) {
        return sparePartDao.findById(id);
    }

    public void simpan(SparePart sparePart) {
        if (sparePart == null) {
            throw new AplikasiException("Data spare part tidak boleh kosong.");
        }
        try {
            if (sparePart.getId() == null) {
                sparePartDao.insert(sparePart);
            } else {
                sparePartDao.update(sparePart);
            }
        } catch (DataAccessException e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                throw new AplikasiException("Kode spare part sudah dipakai. Gunakan kode lain.");
            }
            throw e;
        }
    }

    public void hapus(Long id) {
        ValidasiId.wajib(id, "Spare part");
        sparePartDao.delete(id);
    }

    public String kodeBerikutnya() {
        return sparePartDao.nextKode();
    }

    public List<SparePart> stokMenipis() {
        return sparePartDao.findMenipis();
    }

    public int total() {
        return sparePartDao.count();
    }

    public int totalStokMenipis() {
        return sparePartDao.countMenipis();
    }

    /**
     * Validasi id sederhana agar pesan error konsisten.
     */
    private static final class ValidasiId {

        private ValidasiId() {
        }

        static void wajib(Long id, String nama) {
            if (id == null) {
                throw new AplikasiException(nama + " wajib dipilih.");
            }
        }
    }
}
```

## src/kaboygarage/service/WaitingListService.java

```java
package kaboygarage.service;

import kaboygarage.dao.PendaftaranServisDao;
import kaboygarage.exception.AplikasiException;
import kaboygarage.model.JenisServis;
import kaboygarage.model.Kendaraan;
import kaboygarage.model.Pelanggan;
import kaboygarage.model.PendaftaranServis;
import kaboygarage.model.StatusServis;
import kaboygarage.util.KodeGenerator;
import kaboygarage.util.Validasi;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pendaftaran servis (waiting list) dan penjadwalannya.
 */
public class WaitingListService {

    private final PendaftaranServisDao servisDao = new PendaftaranServisDao();

    public PendaftaranServis daftar(Pelanggan pelanggan, Kendaraan kendaraan,
                                    JenisServis jenisServis, String keluhan) {
        Validasi.wajibAda(pelanggan, "Pelanggan");
        Validasi.wajibAda(kendaraan, "Kendaraan");
        Validasi.wajibAda(jenisServis, "Jenis servis");
        if (pelanggan.getId() == null || kendaraan.getId() == null) {
            throw new AplikasiException("Pelanggan dan kendaraan harus sudah tersimpan.");
        }
        if (kendaraan.getPelanggan() == null
                || !pelanggan.getId().equals(kendaraan.getPelanggan().getId())) {
            throw new AplikasiException("Kendaraan yang dipilih bukan milik pelanggan tersebut.");
        }

        LocalDateTime sekarang = LocalDateTime.now();
        int nomor = servisDao.nextNomorAntrean();
        PendaftaranServis pendaftaran = new PendaftaranServis(
                KodeGenerator.kodeAntreanServis(sekarang, nomor),
                nomor, pelanggan, kendaraan, jenisServis, keluhan);
        servisDao.insert(pendaftaran);
        return pendaftaran;
    }

    public List<PendaftaranServis> semua() {
        return servisDao.findAll();
    }

    public PendaftaranServis cari(Long id) {
        return servisDao.findById(id);
    }

    public List<PendaftaranServis> terbaru(int jumlah) {
        List<PendaftaranServis> semua = servisDao.findAll();
        if (semua.size() <= jumlah) {
            return semua;
        }
        return new ArrayList<>(semua.subList(0, jumlah));
    }

    public PendaftaranServis jadwalkan(Long id, LocalDateTime jadwal) {
        PendaftaranServis pendaftaran = servisDao.findById(id);
        if (pendaftaran == null) {
            throw new AplikasiException("Pendaftaran servis tidak ditemukan.");
        }
        pendaftaran.jadwalkan(jadwal);
        servisDao.updateJadwal(id, jadwal, pendaftaran.getStatus(),
                pendaftaran.getCatatanAdmin());
        return pendaftaran;
    }

    public PendaftaranServis ubahStatus(Long id, StatusServis statusBaru) {
        PendaftaranServis pendaftaran = servisDao.findById(id);
        if (pendaftaran == null) {
            throw new AplikasiException("Pendaftaran servis tidak ditemukan.");
        }
        pendaftaran.ubahStatus(statusBaru);
        servisDao.updateStatus(id, statusBaru);
        return pendaftaran;
    }

    public PendaftaranServis batalkan(Long id) {
        return ubahStatus(id, StatusServis.DIBATALKAN);
    }

    public int totalAktif() {
        return servisDao.countAktif();
    }
}
```

## src/kaboygarage/tools/GuiSmokeTest.java

```java
package kaboygarage.tools;

import kaboygarage.gui.DashboardPanel;
import kaboygarage.gui.LoginFrame;
import kaboygarage.gui.MainFrame;
import kaboygarage.gui.PelangganPanel;
import kaboygarage.gui.PesananPanel;
import kaboygarage.gui.SparePartPanel;
import kaboygarage.gui.Theme;
import kaboygarage.gui.WaitingListPanel;
import kaboygarage.model.Admin;
import kaboygarage.service.AuthService;
import kaboygarage.service.DashboardService;
import kaboygarage.service.PelangganService;
import kaboygarage.service.PesananService;
import kaboygarage.service.SparePartService;
import kaboygarage.service.WaitingListService;

import javax.swing.SwingUtilities;

/**
 * Menguji inisialisasi seluruh jendela GUI dan pembacaan data dari database.
 */
public final class GuiSmokeTest {

    private GuiSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        Theme.install();
        Admin admin = new AuthService().login("admin", "admin123");
        if (admin == null) {
            throw new IllegalStateException("Login admin gagal.");
        }

        SwingUtilities.invokeAndWait(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.dispose();

            MainFrame mainFrame = new MainFrame(admin);
            mainFrame.dispose();

            new DashboardPanel(new DashboardService()).refreshData();
            new SparePartPanel(new SparePartService()).refreshData();
            new PelangganPanel(new PelangganService()).refreshData();
            new PesananPanel(new PesananService(), new PelangganService(),
                    new SparePartService()).refreshData();
            new WaitingListPanel(new WaitingListService(),
                    new PelangganService()).refreshData();
        });

        System.out.println("GUI SMOKE TEST LULUS.");
    }
}
```

## src/kaboygarage/tools/SmokeTest.java

```java
package kaboygarage.tools;

import kaboygarage.db.Database;
import kaboygarage.dto.ItemPesanan;
import kaboygarage.dto.Statistik;
import kaboygarage.exception.AplikasiException;
import kaboygarage.exception.StokTidakCukupException;
import kaboygarage.model.Admin;
import kaboygarage.model.JenisServis;
import kaboygarage.model.Kendaraan;
import kaboygarage.model.Pelanggan;
import kaboygarage.model.PendaftaranServis;
import kaboygarage.model.Pesanan;
import kaboygarage.model.SparePart;
import kaboygarage.model.StatusPesanan;
import kaboygarage.model.StatusServis;
import kaboygarage.service.AuthService;
import kaboygarage.service.DashboardService;
import kaboygarage.service.PelangganService;
import kaboygarage.service.PesananService;
import kaboygarage.service.SparePartService;
import kaboygarage.service.WaitingListService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Smoke test database + service. Jalankan dengan:
 * java -cp "build\classes;lib\mysql-connector-j-8.3.0.jar" kaboygarage.tools.SmokeTest
 */
public final class SmokeTest {

    private SmokeTest() {
    }

    public static void main(String[] args) {
        Long pelangganId = null;
        boolean gagal = false;

        try {
            System.out.println("Database : " + Database.testConnection());

            AuthService authService = new AuthService();
            Admin admin = authService.login("admin", "admin123");
            cek(admin != null, "login admin / admin123");
            cek("Admin".equals(admin.getPeran()), "polymorphism Admin.getPeran()");

            try {
                authService.login("admin", "password-salah");
                cek(false, "login password salah ditolak");
            } catch (AplikasiException e) {
                cek(true, "login password salah ditolak");
            }

            SparePartService sparePartService = new SparePartService();
            List<SparePart> katalog = sparePartService.semua();
            cek(katalog.size() >= 5, "katalog spare part terbaca (" + katalog.size() + ")");
            SparePart part1 = katalog.get(0);
            SparePart part2 = katalog.get(1);
            int stokAwal1 = part1.getStok();

            String suffix = String.valueOf(System.currentTimeMillis());
            suffix = suffix.substring(Math.max(0, suffix.length() - 8));

            PelangganService pelangganService = new PelangganService();
            Pelanggan pelanggan = new Pelanggan(
                    "Pelanggan Uji " + suffix, "0812" + suffix,
                    "uji" + suffix + "@example.com", "Jl. Pengujian No. 1");
            pelangganService.simpan(pelanggan);
            pelangganId = pelanggan.getId();
            cek(pelangganId != null, "CREATE pelanggan");
            cek("Pelanggan".equals(pelanggan.getPeran()),
                    "polymorphism Pelanggan.getPeran()");

            Kendaraan kendaraan = new Kendaraan(
                    pelanggan, "Honda", "Vario Uji", "B " + suffix + " UJI", 2022);
            pelangganService.simpanKendaraan(kendaraan);
            cek(kendaraan.getId() != null, "CREATE kendaraan");
            cek(pelangganService.kendaraan(pelangganId).size() == 1,
                    "relasi pelanggan - kendaraan");

            PesananService pesananService = new PesananService();
            List<ItemPesanan> items = new ArrayList<>();
            items.add(new ItemPesanan(part1, 2));
            items.add(new ItemPesanan(part2, 1));
            Pesanan pesanan = pesananService.buatPesanan(pelanggan, items, "Smoke test");
            cek(pesanan.getId() != null, "CREATE pesanan + detail");
            cek(pesanan.hitungTotal().signum() > 0, "total pesanan dihitung");
            cek(pesanan.getStatus() == StatusPesanan.BARU, "status awal pesanan BARU");

            SparePart setelahPesan = sparePartService.cari(part1.getId());
            cek(setelahPesan.getStok() == stokAwal1 - 2,
                    "stok berkurang otomatis setelah pesanan");

            int stokSebelumGagal = sparePartService.cari(part1.getId()).getStok();
            try {
                pesananService.buatPesanan(pelanggan,
                        Arrays.asList(new ItemPesanan(part1, stokSebelumGagal + 1)),
                        "Stok tidak cukup");
                cek(false, "stok tidak cukup ditolak");
            } catch (StokTidakCukupException e) {
                cek(true, "stok tidak cukup ditolak");
            }
            cek(sparePartService.cari(part1.getId()).getStok() == stokSebelumGagal,
                    "rollback stok saat pesanan gagal");

            pesananService.ubahStatus(pesanan.getId(), StatusPesanan.DIPROSES);
            cek(pesananService.cari(pesanan.getId()).getStatus() == StatusPesanan.DIPROSES,
                    "status pesanan BARU -> DIPROSES");
            pesananService.ubahStatus(pesanan.getId(), StatusPesanan.SIAP_DIAMBIL);
            cek(pesananService.cari(pesanan.getId()).getStatus() == StatusPesanan.SIAP_DIAMBIL,
                    "status pesanan DIPROSES -> SIAP DIAMBIL");
            pesananService.ubahStatus(pesanan.getId(), StatusPesanan.SELESAI);
            cek(pesananService.cari(pesanan.getId()).getStatus() == StatusPesanan.SELESAI,
                    "status pesanan SIAP DIAMBIL -> SELESAI");

            Pesanan pesananBatal = pesananService.buatPesanan(
                    pelanggan, Arrays.asList(new ItemPesanan(part2, 1)), "Akan dibatalkan");
            int stokSebelumBatal = sparePartService.cari(part2.getId()).getStok();
            pesananService.batalkan(pesananBatal.getId());
            cek(sparePartService.cari(part2.getId()).getStok() == stokSebelumBatal + 1,
                    "pembatalan pesanan mengembalikan stok");
            cek(pesananService.cari(pesananBatal.getId()).getStatus()
                    == StatusPesanan.DIBATALKAN, "status pesanan DIBATALKAN");

            WaitingListService waitingListService = new WaitingListService();
            PendaftaranServis servis = waitingListService.daftar(
                    pelanggan, kendaraan, JenisServis.SERVIS_RINGAN,
                    "Keluhan uji integrasi waiting list");
            cek(servis.getId() != null, "CREATE pendaftaran servis");
            cek(servis.getStatus() == StatusServis.MENUNGGU, "status awal servis MENUNGGU");

            LocalDateTime jadwal = LocalDateTime.now().plusDays(1)
                    .withHour(10).withMinute(0).withSecond(0).withNano(0);
            waitingListService.jadwalkan(servis.getId(), jadwal);
            cek(waitingListService.cari(servis.getId()).getStatus()
                    == StatusServis.DIJADWALKAN, "jadwal servis disimpan");
            waitingListService.ubahStatus(servis.getId(), StatusServis.DIKERJAKAN);
            cek(waitingListService.cari(servis.getId()).getStatus()
                    == StatusServis.DIKERJAKAN, "status servis DIJADWALKAN -> DIKERJAKAN");
            waitingListService.ubahStatus(servis.getId(), StatusServis.SELESAI);
            cek(waitingListService.cari(servis.getId()).getStatus()
                    == StatusServis.SELESAI, "status servis DIKERJAKAN -> SELESAI");

            Statistik statistik = new DashboardService().statistik();
            cek(statistik.getTotalSparePart() >= 5, "statistik dashboard terbaca");
            System.out.println("           spare part=" + statistik.getTotalSparePart()
                    + ", pesanan aktif=" + statistik.getPesananAktif()
                    + ", antrean aktif=" + statistik.getAntreanAktif()
                    + ", pelanggan=" + statistik.getTotalPelanggan());

            System.out.println();
            System.out.println("SEMUA SMOKE TEST LULUS.");
        } catch (Throwable ex) {
            System.out.println();
            System.out.println("SMOKE TEST GAGAL: " + ex.getMessage());
            ex.printStackTrace(System.out);
            gagal = true;
        } finally {
            bersihkan(pelangganId);
        }
        if (gagal) {
            System.exit(1);
        }
    }

    private static void cek(boolean kondisi, String pesan) {
        if (!kondisi) {
            throw new IllegalStateException("Gagal: " + pesan);
        }
        System.out.println("[OK] " + pesan);
    }

    /**
     * Menghapus data uji dan mengembalikan stok pesanan yang belum dibatalkan.
     */
    private static void bersihkan(Long pelangganId) {
        if (pelangganId == null) {
            return;
        }
        System.out.println();
        System.out.println("Membersihkan data uji...");
        try (Connection connection = Database.getConnection()) {
            String restore = "UPDATE spare_part sp JOIN ("
                    + "SELECT d.spare_part_id, SUM(d.jumlah) AS qty "
                    + "FROM detail_pesanan d JOIN pesanan p ON p.id = d.pesanan_id "
                    + "WHERE p.pelanggan_id = ? AND p.status <> 'DIBATALKAN' "
                    + "GROUP BY d.spare_part_id"
                    + ") x ON x.spare_part_id = sp.id "
                    + "SET sp.stok = sp.stok + x.qty";
            try (PreparedStatement statement = connection.prepareStatement(restore)) {
                statement.setLong(1, pelangganId);
                statement.executeUpdate();
            }
            hapus(connection, "DELETE FROM pesanan WHERE pelanggan_id = ?", pelangganId);
            hapus(connection, "DELETE FROM pendaftaran_servis WHERE pelanggan_id = ?", pelangganId);
            hapus(connection, "DELETE FROM kendaraan WHERE pelanggan_id = ?", pelangganId);
            hapus(connection, "DELETE FROM pelanggan WHERE id = ?", pelangganId);
            System.out.println("[OK] data uji dibersihkan");
        } catch (Exception ex) {
            System.out.println("[PERINGATAN] gagal membersihkan data uji: " + ex.getMessage());
        }
    }

    private static void hapus(Connection connection, String sql, Long id) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }
}
```

## src/kaboygarage/util/KodeGenerator.java

```java
package kaboygarage.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Pembuat kode transaksi yang mudah dibaca, misalnya PSN-20261030-4821.
 */
public final class KodeGenerator {

    private static final DateTimeFormatter TANGGAL = DateTimeFormatter.ofPattern("yyyyMMdd");

    private KodeGenerator() {
    }

    public static String kodePesanan() {
        return "PSN-" + LocalDateTime.now().format(TANGGAL) + "-" + nomorAcak();
    }

    public static String kodeAntreanServis(LocalDateTime waktu) {
        return "SRV-" + waktu.format(TANGGAL) + "-" + nomorAcak();
    }

    /**
     * Kode antrean memakai nomor urut harian agar dijamin unik per hari.
     */
    public static String kodeAntreanServis(LocalDateTime waktu, int nomorAntrean) {
        return "SRV-" + waktu.format(TANGGAL) + "-" + String.format("%04d", nomorAntrean);
    }

    private static String nomorAcak() {
        return String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }
}
```

## src/kaboygarage/util/PasswordUtil.java

```java
package kaboygarage.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilitas hash password admin.
 *
 * <p>Catatan: untuk kebutuhan praktikum dipakai SHA-256. Pada aplikasi produksi
 * sebaiknya gunakan algoritma khusus password seperti BCrypt/Argon2.</p>
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hash(String passwordAsli) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(passwordAsli.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Algoritma SHA-256 tidak tersedia", ex);
        }
    }

    public static boolean cocok(String passwordAsli, String passwordTersimpan) {
        if (passwordAsli == null || passwordTersimpan == null) {
            return false;
        }
        return hash(passwordAsli).equalsIgnoreCase(passwordTersimpan);
    }
}
```

## src/kaboygarage/util/Validasi.java

```java
package kaboygarage.util;

import kaboygarage.exception.AplikasiException;

import java.math.BigDecimal;

/**
 * Kumpulan validasi sederhana agar aturan data tidak ditulis berulang di
 * service maupun GUI.
 */
public final class Validasi {

    private Validasi() {
    }

    public static String wajibIsi(String nilai, String namaField) {
        if (nilai == null || nilai.trim().isEmpty()) {
            throw new AplikasiException(namaField + " wajib diisi.");
        }
        return nilai.trim();
    }

    public static <T> T wajibAda(T nilai, String namaField) {
        if (nilai == null) {
            throw new AplikasiException(namaField + " wajib dipilih.");
        }
        return nilai;
    }

    public static int wajibPositif(int nilai, String namaField) {
        if (nilai <= 0) {
            throw new AplikasiException(namaField + " harus lebih besar dari 0.");
        }
        return nilai;
    }

    public static void wajibBenar(boolean kondisi, String pesan) {
        if (!kondisi) {
            throw new AplikasiException(pesan);
        }
    }

    public static BigDecimal parseHarga(String teks) {
        String bersih = wajibIsi(teks, "Harga").replace("Rp", "")
                .replace(" ", "").replace(".", "").replace(",", ".");
        try {
            BigDecimal nilai = new BigDecimal(bersih);
            if (nilai.signum() <= 0) {
                throw new AplikasiException("Harga harus lebih besar dari 0.");
            }
            return nilai;
        } catch (NumberFormatException e) {
            throw new AplikasiException("Harga harus berupa angka, contoh: 48000.");
        }
    }

    public static int parseAngka(String teks, String namaField) {
        try {
            return Integer.parseInt(wajibIsi(teks, namaField));
        } catch (NumberFormatException e) {
            throw new AplikasiException(namaField + " harus berupa angka bulat.");
        }
    }

    public static int parseTahun(String teks) {
        int tahun = parseAngka(teks, "Tahun kendaraan");
        int maksimal = java.time.Year.now().getValue() + 1;
        if (tahun < 1950 || tahun > maksimal) {
            throw new AplikasiException("Tahun kendaraan harus antara 1950 dan " + maksimal + ".");
        }
        return tahun;
    }
}
```

---

# Lampiran: Script Database

## database/kaboy_garage.sql

```sql
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
```

# Lampiran: Konfigurasi

## db.properties

`$lang
# Konfigurasi koneksi database MySQL/MariaDB (XAMPP)
# Ubah nilainya jika MySQL memakai password atau port berbeda.
db.url=jdbc:mysql://localhost:3306/kaboy_garage?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta&characterEncoding=UTF-8
db.user=root
db.password=
```

## build.bat

`$lang
@echo off
setlocal
cd /d "%~dp0"

set "JDBC=lib\mysql-connector-j-8.3.0.jar"
if not exist "%JDBC%" (
    echo [GAGAL] Driver JDBC tidak ditemukan: %JDBC%
    echo Salin mysql-connector-j-8.3.0.jar ke folder lib.
    pause
    exit /b 1
)

if not exist build\classes mkdir build\classes
if exist sources.txt del sources.txt
dir /s /b src\*.java > sources.txt

echo Mengompilasi aplikasi...
javac --release 11 -encoding UTF-8 -cp "%JDBC%" -d build\classes @sources.txt
if errorlevel 1 (
    echo.
    echo Kompilasi gagal.
    pause
    exit /b 1
)

echo Menjalankan GUI Kaboy Garage...
java -cp "build\classes;%JDBC%" kaboygarage.gui.GuiMain
endlocal
```

## setup_database.bat

`$lang
@echo off
setlocal
cd /d "%~dp0"

set "MYSQL=C:\xampp\mysql\bin\mysql.exe"
if not exist "%MYSQL%" (
    where mysql >nul 2>nul
    if errorlevel 1 (
        echo [GAGAL] mysql.exe tidak ditemukan.
        echo Pastikan XAMPP terpasang di C:\xampp atau MySQL ada di PATH.
        pause
        exit /b 1
    )
    set "MYSQL=mysql"
)

echo ============================================================
echo  Membuat database kaboy_garage dan mengisi data awal...
echo ============================================================
"%MYSQL%" -u root < "database\kaboy_garage.sql"
if errorlevel 1 (
    echo.
    echo [GAGAL] Import database gagal.
    echo Pastikan MySQL XAMPP sudah berjalan dan password root kosong.
    pause
    exit /b 1
)

echo.
echo [OK] Database kaboy_garage siap dipakai.
echo      Login GUI: admin / admin123
echo ============================================================
pause
endlocal
```

## build.xml

`$lang
<?xml version="1.0" encoding="UTF-8"?>
<project name="KaboyGarage" default="default" basedir=".">
    <description>Builds, tests, and runs the project KaboyGarage.</description>
    <import file="nbproject/build-impl.xml"/>
</project>
```

## nbproject/project.properties

`$lang
annotation.processing.enabled=true
annotation.processing.enabled.in.editor=false
annotation.processing.processors.list=
annotation.processing.run.all.processors=true
annotation.processing.source.output=${build.generated.sources.dir}/ap-source-output
application.title=KaboyGarage
application.vendor=Raka Pratama
build.classes.dir=${build.dir}/classes
build.classes.excludes=**/*.java,**/*.form
build.dir=build
build.generated.dir=${build.dir}/generated
build.generated.sources.dir=${build.dir}/generated-sources
build.sysclasspath=ignore
build.test.classes.dir=${build.dir}/test/classes
build.test.results.dir=${build.dir}/test/results
build.webapps.dir=${build.dir}/webapps
debug.classpath=${run.classpath}
debug.modulepath=\
    ${run.modulepath}
debug.test.classpath=${run.test.classpath}
debug.test.modulepath=\
    ${run.test.modulepath}
dist.archive.excludes=
dist.dir=dist
dist.jar=${dist.dir}/KaboyGarage.jar
dist.javadoc.dir=${dist.dir}/javadoc
endorsed.classpath=
excludes=
includes=**
jar.compress=false
javac.classpath=lib/mysql-connector-j-8.3.0.jar
javac.compilerargs=
javac.deprecation=false
javac.modulepath=
javac.processormodulepath=
javac.processorpath=${javac.classpath}
javac.source=11
javac.target=11
javac.test.classpath=${javac.classpath}:${build.classes.dir}
javac.test.modulepath=\
    ${javac.modulepath}
javac.test.processorpath=${javac.test.classpath}
javadoc.additionalparam=
javadoc.author=false
javadoc.encoding=${source.encoding}
javadoc.noindex=false
javadoc.nonavbar=false
javadoc.notree=false
javadoc.private=false
javadoc.splitindex=true
javadoc.use=true
javadoc.version=false
javadoc.windowtitle=
main.class=kaboygarage.gui.GuiMain
manifest.file=manifest.mf
meta.inf.dir=${src.dir}/META-INF
mkdist.disabled=false
platform.active=default_platform
run.classpath=${javac.classpath}:${build.classes.dir}
run.jvmargs=
run.modulepath=\
    ${javac.modulepath}
run.test.classpath=${javac.test.classpath}:${build.test.classes.dir}
run.test.modulepath=\
    ${javac.test.modulepath}
source.encoding=UTF-8
src.dir=src
test.src.dir=test
```

