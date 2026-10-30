package kaboygarage.db;

/**
 * Exception untuk kesalahan akses database (koneksi, query, constraint).
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
