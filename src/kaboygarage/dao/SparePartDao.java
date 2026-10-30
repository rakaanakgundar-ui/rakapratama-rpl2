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
