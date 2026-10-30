package kaboygarage.exception;

/**
 * Exception dasar untuk kesalahan aturan bisnis aplikasi.
 * Dipakai agar lapisan GUI dapat menampilkan pesan yang ramah pengguna.
 */
public class AplikasiException extends RuntimeException {

    public AplikasiException(String message) {
        super(message);
    }

    public AplikasiException(String message, Throwable cause) {
        super(message, cause);
    }
}
