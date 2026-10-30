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
