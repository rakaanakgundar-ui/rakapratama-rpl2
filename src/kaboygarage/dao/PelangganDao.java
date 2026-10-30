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
