package kaboygarage.model;

import kaboygarage.util.Validasi;

import jakarta.persistence.*;

/**
 * INHERITANCE: Admin adalah Orang yang memiliki akun untuk masuk ke aplikasi.
 */
@Entity
@Table(name = "admin", uniqueConstraints = {
    @UniqueConstraint(name = "uk_admin_username", columnNames = "username")
})
public class Admin extends Orang {

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, name = "password_hash", length = 64)
    private String passwordHash;

    @Column(nullable = false)
    private boolean aktif = true;

    public Admin() {
    }

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
