package kaboygarage.model;

import kaboygarage.exception.AplikasiException;

import java.time.LocalDateTime;

/**
 * Pendaftaran servis kendaraan (waiting list) beserta nomor antrean dan jadwal.
 */
public class PendaftaranServis {

    private Long id;
    private String kodeAntrean;
    private int nomorAntrean;
    private Pelanggan pelanggan;
    private Kendaraan kendaraan;
    private JenisServis jenisServis;
    private String keluhan;
    private LocalDateTime tanggalDiajukan;
    private LocalDateTime jadwalServis;
    private StatusServis status = StatusServis.MENUNGGU;
    private String catatanAdmin;

    public PendaftaranServis(String kodeAntrean, int nomorAntrean, Pelanggan pelanggan,
                             Kendaraan kendaraan, JenisServis jenisServis, String keluhan) {
        if (kodeAntrean == null || kodeAntrean.isBlank()) {
            throw new AplikasiException("Kode antrean wajib diisi.");
        }
        if (pelanggan == null || kendaraan == null) {
            throw new AplikasiException("Pelanggan dan kendaraan wajib dipilih.");
        }
        if (jenisServis == null) {
            throw new AplikasiException("Jenis servis wajib dipilih.");
        }
        if (keluhan == null || keluhan.isBlank()) {
            throw new AplikasiException("Keluhan wajib diisi.");
        }
        this.kodeAntrean = kodeAntrean;
        this.nomorAntrean = nomorAntrean;
        this.pelanggan = pelanggan;
        this.kendaraan = kendaraan;
        this.jenisServis = jenisServis;
        this.keluhan = keluhan.trim();
        this.tanggalDiajukan = LocalDateTime.now();
        this.status = StatusServis.MENUNGGU;
    }

    /**
     * Admin menetapkan jadwal servis. Status otomatis berpindah ke DIJADWALKAN.
     */
    public void jadwalkan(LocalDateTime jadwal) {
        if (jadwal == null) {
            throw new AplikasiException("Jadwal servis wajib diisi.");
        }
        if (jadwal.isBefore(tanggalDiajukan)) {
            throw new AplikasiException("Jadwal servis tidak boleh sebelum tanggal pendaftaran.");
        }
        if (status != StatusServis.MENUNGGU && status != StatusServis.DIJADWALKAN) {
            throw new AplikasiException("Jadwal servis tidak dapat diubah pada status "
                    + status.getLabel() + ".");
        }
        this.jadwalServis = jadwal;
        if (status == StatusServis.MENUNGGU) {
            ubahStatus(StatusServis.DIJADWALKAN);
        }
    }

    public void ubahStatus(StatusServis statusBaru) {
        if (!status.bisaBerubahKe(statusBaru)) {
            throw new AplikasiException("Status servis tidak dapat diubah dari "
                    + status.getLabel() + " menjadi "
                    + (statusBaru == null ? "-" : statusBaru.getLabel()) + ".");
        }
        this.status = statusBaru;
    }

    public void batalkan() {
        ubahStatus(StatusServis.DIBATALKAN);
    }

    /**
     * Estimasi waktu selesai dihitung dari jadwal + durasi jenis servis.
     */
    public LocalDateTime getEstimasiSelesai() {
        if (jadwalServis == null || jenisServis == null) {
            return null;
        }
        return jadwalServis.plusMinutes(jenisServis.getEstimasiMenit());
    }

    public boolean isSelesai() {
        return status.isSelesai();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKodeAntrean() {
        return kodeAntrean;
    }

    public void setKodeAntrean(String kodeAntrean) {
        this.kodeAntrean = kodeAntrean;
    }

    public int getNomorAntrean() {
        return nomorAntrean;
    }

    public void setNomorAntrean(int nomorAntrean) {
        this.nomorAntrean = nomorAntrean;
    }

    public Pelanggan getPelanggan() {
        return pelanggan;
    }

    public void setPelanggan(Pelanggan pelanggan) {
        this.pelanggan = pelanggan;
    }

    public Kendaraan getKendaraan() {
        return kendaraan;
    }

    public void setKendaraan(Kendaraan kendaraan) {
        this.kendaraan = kendaraan;
    }

    public JenisServis getJenisServis() {
        return jenisServis;
    }

    public void setJenisServis(JenisServis jenisServis) {
        this.jenisServis = jenisServis;
    }

    public String getKeluhan() {
        return keluhan;
    }

    public void setKeluhan(String keluhan) {
        this.keluhan = keluhan;
    }

    public LocalDateTime getTanggalDiajukan() {
        return tanggalDiajukan;
    }

    public void setTanggalDiajukan(LocalDateTime tanggalDiajukan) {
        this.tanggalDiajukan = tanggalDiajukan;
    }

    public LocalDateTime getJadwalServis() {
        return jadwalServis;
    }

    public void setJadwalServis(LocalDateTime jadwalServis) {
        this.jadwalServis = jadwalServis;
    }

    public StatusServis getStatus() {
        return status;
    }

    public void setStatus(StatusServis status) {
        this.status = status;
    }

    public String getCatatanAdmin() {
        return catatanAdmin;
    }

    public void setCatatanAdmin(String catatanAdmin) {
        this.catatanAdmin = catatanAdmin;
    }

    @Override
    public String toString() {
        return kodeAntrean + " - " + kendaraan.getPlatNomor()
                + " (" + status.getLabel() + ")";
    }
}
