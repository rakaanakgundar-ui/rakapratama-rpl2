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
