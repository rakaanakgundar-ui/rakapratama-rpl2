package kaboygarage.model;

/**
 * Status perjalanan sebuah pesanan spare part.
 *
 * <p>Perubahan status divalidasi oleh enum ini agar alur pesanan tidak
 * melompati tahapan (misalnya BARU langsung SELESAI).</p>
 */
public enum StatusPesanan {

    BARU("Baru"),
    DIPROSES("Diproses"),
    SIAP_DIAMBIL("Siap Diambil"),
    SELESAI("Selesai"),
    DIBATALKAN("Dibatalkan");

    private final String label;

    StatusPesanan(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * @return {@code true} bila status saat ini boleh berpindah ke {@code tujuan}.
     */
    public boolean bisaBerubahKe(StatusPesanan tujuan) {
        if (tujuan == null) {
            return false;
        }
        switch (this) {
            case BARU:
                return tujuan == DIPROSES || tujuan == DIBATALKAN;
            case DIPROSES:
                return tujuan == SIAP_DIAMBIL || tujuan == DIBATALKAN;
            case SIAP_DIAMBIL:
                return tujuan == SELESAI;
            default:
                return false;
        }
    }

    public boolean isSelesai() {
        return this == SELESAI || this == DIBATALKAN;
    }

    @Override
    public String toString() {
        return label;
    }
}
