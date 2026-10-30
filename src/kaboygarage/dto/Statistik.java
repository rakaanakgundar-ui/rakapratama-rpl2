package kaboygarage.dto;

/**
 * Ringkasan angka untuk dashboard.
 */
public class Statistik {

    private final int totalSparePart;
    private final int pesananAktif;
    private final int antreanAktif;
    private final int totalPelanggan;
    private final int stokMenipis;

    public Statistik(int totalSparePart, int pesananAktif, int antreanAktif,
                     int totalPelanggan, int stokMenipis) {
        this.totalSparePart = totalSparePart;
        this.pesananAktif = pesananAktif;
        this.antreanAktif = antreanAktif;
        this.totalPelanggan = totalPelanggan;
        this.stokMenipis = stokMenipis;
    }

    public int getTotalSparePart() {
        return totalSparePart;
    }

    public int getPesananAktif() {
        return pesananAktif;
    }

    public int getAntreanAktif() {
        return antreanAktif;
    }

    public int getTotalPelanggan() {
        return totalPelanggan;
    }

    public int getStokMenipis() {
        return stokMenipis;
    }
}
