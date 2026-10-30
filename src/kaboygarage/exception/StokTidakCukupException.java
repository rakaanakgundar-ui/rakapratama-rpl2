package kaboygarage.exception;

/**
 * Dilempar ketika jumlah stok spare part tidak mencukupi permintaan pelanggan.
 */
public class StokTidakCukupException extends AplikasiException {

    private final String namaBarang;
    private final int stokTersedia;
    private final int jumlahDiminta;

    public StokTidakCukupException(String namaBarang, int stokTersedia, int jumlahDiminta) {
        super("Stok " + namaBarang + " tidak mencukupi. Tersedia " + stokTersedia
                + ", diminta " + jumlahDiminta + ".");
        this.namaBarang = namaBarang;
        this.stokTersedia = stokTersedia;
        this.jumlahDiminta = jumlahDiminta;
    }

    public String getNamaBarang() {
        return namaBarang;
    }

    public int getStokTersedia() {
        return stokTersedia;
    }

    public int getJumlahDiminta() {
        return jumlahDiminta;
    }
}
