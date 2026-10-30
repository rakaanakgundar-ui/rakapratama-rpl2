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
