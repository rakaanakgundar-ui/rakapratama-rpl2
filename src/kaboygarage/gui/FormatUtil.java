package kaboygarage.gui;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Format rupiah dan tanggal/jam untuk form dan tabel.
 */
public final class FormatUtil {

    public static final DateTimeFormatter TANGGAL_INPUT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TANGGAL_VIEW =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final DateTimeFormatter WAKTU_INPUT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter WAKTU_VIEW =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private static final DecimalFormat RUPIAH = new DecimalFormat(
            "#,##0", new DecimalFormatSymbols(Locale.forLanguageTag("id-ID")));

    private FormatUtil() {
    }

    public static String rupiah(BigDecimal nilai) {
        return "Rp " + RUPIAH.format(nilai == null ? BigDecimal.ZERO : nilai);
    }

    public static String tanggal(LocalDateTime waktu) {
        return waktu == null ? "-" : waktu.format(WAKTU_VIEW);
    }

    public static String tanggalInput(LocalDateTime waktu) {
        return waktu == null ? "" : waktu.format(WAKTU_INPUT);
    }

    public static String tanggalSingkat(LocalDateTime waktu) {
        return waktu == null ? "-" : waktu.format(TANGGAL_VIEW);
    }

    public static LocalDateTime parseWaktu(String teks) {
        if (teks == null || teks.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Jadwal wajib diisi dengan format yyyy-MM-dd HH:mm, contoh 2026-10-30 09:15.");
        }
        try {
            return LocalDateTime.parse(teks.trim(), WAKTU_INPUT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Format jadwal harus yyyy-MM-dd HH:mm, contoh 2026-10-30 09:15.");
        }
    }
}
