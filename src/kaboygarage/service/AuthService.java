package kaboygarage.service;

import kaboygarage.model.Admin;
import kaboygarage.repository.AdminRepository;
import kaboygarage.util.PasswordUtil;
import kaboygarage.util.Validasi;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service autentikasi admin.
 */
@Service
public class AuthService {

    @Autowired
    private AdminRepository adminRepository;

    public Admin login(String username, String password) {
        String user = Validasi.wajibIsi(username, "Username");
        String pass = Validasi.wajibIsi(password, "Password");
        
        Admin admin = adminRepository.findByUsername(user);
        if (admin == null || !admin.isAktif()) {
            return null;
        }
        if (!PasswordUtil.cocok(pass, admin.getPasswordHash())) {
            return null;
        }
        return admin;
    }
}
