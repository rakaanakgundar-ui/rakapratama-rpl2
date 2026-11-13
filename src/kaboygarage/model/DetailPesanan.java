package kaboygarage.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Satu baris item pada pesanan. Harga disimpan sebagai snapshot agar total
 * pesanan lama tidak berubah ketika harga katalog diperbarui.
 */
@Entity
@Table(name = "detail_pesanan")
public class DetailPesanan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pesanan_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_detail_pesanan"))
    private Pesanan pesanan;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "spare_part_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_detail_spare_part"))
    private SparePart sparePart;

    @Column(nullable = false)
    private int jumlah;

    @Column(nullable = false, name = "harga_satuan", precision = 12, scale = 2)
    private BigDecimal hargaSatuan;

    public DetailPesanan() {
    }

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
