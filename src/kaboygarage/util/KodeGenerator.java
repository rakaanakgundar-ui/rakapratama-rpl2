package kaboygarage.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Pembuat kode transaksi yang mudah dibaca, misalnya PSN-20261030-4821.
 */
public final class KodeGenerator {

    private static final DateTimeFormatter TANGGAL = DateTimeFormatter.ofPattern("yyyyMMdd");

    private KodeGenerator() {
    }

    public static String kodePesanan() {
        return "PSN-" + LocalDateTime.now().format(TANGGAL) + "-" + nomorAcak();
    }

    public static String kodeAntreanServis(LocalDateTime waktu) {
        return "SRV-" + waktu.format(TANGGAL) + "-" + nomorAcak();
    }

    /**
     * Kode antrean memakai nomor urut harian agar dijamin unik per hari.
     */
    public static String kodeAntreanServis(LocalDateTime waktu, int nomorAntrean) {
        return "SRV-" + waktu.format(TANGGAL) + "-" + String.format("%04d", nomorAntrean);
    }

    private static String nomorAcak() {
        return String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }
}
