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
