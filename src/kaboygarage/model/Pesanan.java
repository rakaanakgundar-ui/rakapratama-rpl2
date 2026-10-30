package kaboygarage.model;

import kaboygarage.exception.AplikasiException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pesanan spare part yang dibuat pelanggan.
 *
 * <p>Pesanan memiliki banyak detail item (one-to-many). Total selalu dihitung
 * dari detail sehingga tidak ada data uang yang terduplikasi.</p>
 */
public class Pesanan {

    private Long id;
    private String kode;
    private Pelanggan pelanggan;
    private LocalDateTime tanggalDibuat;
    private StatusPesanan status = StatusPesanan.BARU;
    private String catatan;
    private final List<DetailPesanan> detail = new ArrayList<>();

    public Pesanan(String kode, Pelanggan pelanggan) {
        this(kode, pelanggan, LocalDateTime.now());
    }

    public Pesanan(String kode, Pelanggan pelanggan, LocalDateTime tanggalDibuat) {
        if (kode == null || kode.isBlank()) {
            throw new AplikasiException("Kode pesanan wajib diisi.");
        }
        if (pelanggan == null) {
            throw new AplikasiException("Pelanggan pesanan wajib dipilih.");
        }
        this.kode = kode;
        this.pelanggan = pelanggan;
        this.tanggalDibuat = tanggalDibuat;
        this.status = StatusPesanan.BARU;
    }

    /**
     * Menambahkan item ke pesanan. Bila spare part yang sama sudah ada,
     * jumlahnya digabung agar daftar item tetap rapi.
     */
    public void tambahDetail(SparePart sparePart, int jumlah) {
        tambahDetail(sparePart, jumlah, sparePart == null ? null : sparePart.getHarga());
    }

    /**
     * Menambahkan item dengan harga snapshot (dipakai saat membaca dari
     * database agar total pesanan lama tidak berubah).
     */
    public void tambahDetail(SparePart sparePart, int jumlah, BigDecimal hargaSatuan) {
        if (sparePart == null) {
            throw new AplikasiException("Spare part tidak boleh kosong.");
        }
        if (jumlah <= 0) {
            throw new AplikasiException("Jumlah item harus lebih besar dari 0.");
        }
        for (DetailPesanan item : detail) {
            if (item.getSparePart() != null
                    && item.getSparePart().getId() != null
                    && item.getSparePart().getId().equals(sparePart.getId())) {
                item.tambahJumlah(jumlah);
                return;
            }
        }
        detail.add(new DetailPesanan(this, sparePart, jumlah, hargaSatuan));
    }

    public void hapusDetail(DetailPesanan item) {
        if (item != null) {
            detail.remove(item);
        }
    }

    public BigDecimal hitungTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (DetailPesanan item : detail) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }

    public int hitungTotalItem() {
        int total = 0;
        for (DetailPesanan item : detail) {
            total += item.getJumlah();
        }
        return total;
    }

    /**
     * Mengubah status dengan memeriksa alur status yang diizinkan.
     */
    public void ubahStatus(StatusPesanan statusBaru) {
        if (!status.bisaBerubahKe(statusBaru)) {
            throw new AplikasiException("Status pesanan tidak dapat diubah dari "
                    + status.getLabel() + " menjadi "
                    + (statusBaru == null ? "-" : statusBaru.getLabel()) + ".");
        }
        this.status = statusBaru;
    }

    public void batalkan() {
        ubahStatus(StatusPesanan.DIBATALKAN);
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

    public String getKode() {
        return kode;
    }

    public void setKode(String kode) {
        this.kode = kode;
    }

    public Pelanggan getPelanggan() {
        return pelanggan;
    }

    public void setPelanggan(Pelanggan pelanggan) {
        this.pelanggan = pelanggan;
    }

    public LocalDateTime getTanggalDibuat() {
        return tanggalDibuat;
    }

    public void setTanggalDibuat(LocalDateTime tanggalDibuat) {
        this.tanggalDibuat = tanggalDibuat;
    }

    public StatusPesanan getStatus() {
        return status;
    }

    public void setStatus(StatusPesanan status) {
        this.status = status;
    }

    public String getCatatan() {
        return catatan;
    }

    public void setCatatan(String catatan) {
        this.catatan = catatan;
    }

    /**
     * Read-only agar detail hanya bisa diubah lewat method tambahDetail().
     */
    public List<DetailPesanan> getDetail() {
        return Collections.unmodifiableList(detail);
    }

    @Override
    public String toString() {
        return kode + " - " + pelanggan.getNama() + " (" + status.getLabel() + ")";
    }
}
