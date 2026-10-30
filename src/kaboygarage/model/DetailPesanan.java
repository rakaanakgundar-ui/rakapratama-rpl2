package kaboygarage.model;

import java.math.BigDecimal;

/**
 * Satu baris item pada pesanan. Harga disimpan sebagai snapshot agar total
 * pesanan lama tidak berubah ketika harga katalog diperbarui.
 */
public class DetailPesanan {

    private Long id;
    private Pesanan pesanan;
    private SparePart sparePart;
    private int jumlah;
    private BigDecimal hargaSatuan;

    public DetailPesanan(Pesanan pesanan, SparePart sparePart, int jumlah,
                         BigDecimal hargaSatuan) {
        this.pesanan = pesanan;
        this.sparePart = sparePart;
        this.jumlah = jumlah;
        this.hargaSatuan = hargaSatuan;
    }

    public BigDecimal getSubtotal() {
        if (hargaSatuan == null) {
            return BigDecimal.ZERO;
        }
        return hargaSatuan.multiply(BigDecimal.valueOf(jumlah));
    }

    public void tambahJumlah(int tambahan) {
        if (tambahan <= 0) {
            throw new IllegalArgumentException("Tambahan jumlah harus positif.");
        }
        this.jumlah += tambahan;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pesanan getPesanan() {
        return pesanan;
    }

    public void setPesanan(Pesanan pesanan) {
        this.pesanan = pesanan;
    }

    public SparePart getSparePart() {
        return sparePart;
    }

    public void setSparePart(SparePart sparePart) {
        this.sparePart = sparePart;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public BigDecimal getHargaSatuan() {
        return hargaSatuan;
    }

    public void setHargaSatuan(BigDecimal hargaSatuan) {
        this.hargaSatuan = hargaSatuan;
    }
}
