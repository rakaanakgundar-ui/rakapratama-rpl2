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
