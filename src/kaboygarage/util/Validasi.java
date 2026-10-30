package kaboygarage.util;

import kaboygarage.exception.AplikasiException;

import java.math.BigDecimal;

/**
 * Kumpulan validasi sederhana agar aturan data tidak ditulis berulang di
 * service maupun GUI.
 */
public final class Validasi {

    private Validasi() {
    }

    public static String wajibIsi(String nilai, String namaField) {
        if (nilai == null || nilai.trim().isEmpty()) {
            throw new AplikasiException(namaField + " wajib diisi.");
        }
        return nilai.trim();
    }

    public static <T> T wajibAda(T nilai, String namaField) {
        if (nilai == null) {
            throw new AplikasiException(namaField + " wajib dipilih.");
        }
        return nilai;
    }

    public static int wajibPositif(int nilai, String namaField) {
        if (nilai <= 0) {
            throw new AplikasiException(namaField + " harus lebih besar dari 0.");
        }
        return nilai;
    }

    public static void wajibBenar(boolean kondisi, String pesan) {
        if (!kondisi) {
            throw new AplikasiException(pesan);
        }
    }

    public static BigDecimal parseHarga(String teks) {
        String bersih = wajibIsi(teks, "Harga").replace("Rp", "")
                .replace(" ", "").replace(".", "").replace(",", ".");
        try {
            BigDecimal nilai = new BigDecimal(bersih);
            if (nilai.signum() <= 0) {
                throw new AplikasiException("Harga harus lebih besar dari 0.");
            }
            return nilai;
        } catch (NumberFormatException e) {
            throw new AplikasiException("Harga harus berupa angka, contoh: 48000.");
        }
    }

    public static int parseAngka(String teks, String namaField) {
        try {
            return Integer.parseInt(wajibIsi(teks, namaField));
        } catch (NumberFormatException e) {
            throw new AplikasiException(namaField + " harus berupa angka bulat.");
        }
    }

    public static int parseTahun(String teks) {
        int tahun = parseAngka(teks, "Tahun kendaraan");
        int maksimal = java.time.Year.now().getValue() + 1;
        if (tahun < 1950 || tahun > maksimal) {
            throw new AplikasiException("Tahun kendaraan harus antara 1950 dan " + maksimal + ".");
        }
        return tahun;
    }
}
