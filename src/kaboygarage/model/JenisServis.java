package kaboygarage.model;

/**
 * Jenis layanan servis yang tersedia di Kaboy Garage beserta estimasi durasinya.
 */
public enum JenisServis {

    GANTI_OLI("Ganti Oli", 30),
    SERVIS_RINGAN("Servis Ringan", 60),
    TUNE_UP("Tune Up", 90),
    SERVIS_BESAR("Servis Besar", 180),
    PERBAIKAN_MESIN("Perbaikan Mesin", 240);

    private final String label;
    private final int estimasiMenit;

    JenisServis(String label, int estimasiMenit) {
        this.label = label;
        this.estimasiMenit = estimasiMenit;
    }

    public String getLabel() {
        return label;
    }

    public int getEstimasiMenit() {
        return estimasiMenit;
    }

    @Override
    public String toString() {
        return label + " (" + estimasiMenit + " menit)";
    }
}
