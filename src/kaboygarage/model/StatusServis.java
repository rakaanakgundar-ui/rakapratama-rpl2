package kaboygarage.model;

/**
 * Status antrean / pengerjaan servis kendaraan.
 */
public enum StatusServis {

    MENUNGGU("Menunggu Jadwal"),
    DIJADWALKAN("Dijadwalkan"),
    DIKERJAKAN("Sedang Dikerjakan"),
    SELESAI("Selesai"),
    DIBATALKAN("Dibatalkan");

    private final String label;

    StatusServis(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean bisaBerubahKe(StatusServis tujuan) {
        if (tujuan == null) {
            return false;
        }
        switch (this) {
            case MENUNGGU:
                return tujuan == DIJADWALKAN || tujuan == DIBATALKAN;
            case DIJADWALKAN:
                return tujuan == DIKERJAKAN || tujuan == DIBATALKAN;
            case DIKERJAKAN:
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
