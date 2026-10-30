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
