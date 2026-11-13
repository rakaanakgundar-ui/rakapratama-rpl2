package kaboygarage.model;

import kaboygarage.exception.StokTidakCukupException;
import kaboygarage.util.Validasi;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Spare part motor yang dijual pada katalog Kaboy Garage.
 *
 * <p>Stok hanya boleh berubah melalui {@link #kurangiStok(int)} dan
 * {@link #tambahStok(int)} agar aturan stok tidak tersebar di GUI
 * (encapsulation perilaku).</p>
 */
@Entity
@Table(name = "spare_part", uniqueConstraints = {
    @UniqueConstraint(name = "uk_spare_part_kode", columnNames = "kode")
})
public class SparePart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String kode;

    @Column(nullable = false, length = 100)
    private String nama;

    @Column(nullable = true, length = 50)
    private String merk;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private KategoriSparePart kategori;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal harga;

    @Column(nullable = false)
    private int stok;

    @Column(nullable = false, name = "stok_minimal")
    private int stokMinimal = 3;

    @Column(nullable = true, length = 255)
    private String deskripsi;

    @OneToMany(mappedBy = "sparePart")
    private java.util.List<DetailPesanan> daftarDetail = new java.util.ArrayList<>();

    public SparePart() {
    }

    public SparePart(String kode, String nama, String merk, KategoriSparePart kategori,
                     BigDecimal harga, int stok) {
        this.kode = Validasi.wajibIsi(kode, "Kode spare part");
        this.nama = Validasi.wajibIsi(nama, "Nama spare part");
        this.merk = merk;
        setKategori(kategori);
        setHarga(harga);
        setStok(stok);
    }

    /**
     * Mengurangi stok setelah pesanan dibuat.
     *
     * @throws StokTidakCukupException bila stok lebih kecil dari jumlah yang diminta
     */
    public void kurangiStok(int jumlah) {
        Validasi.wajibPositif(jumlah, "Jumlah pengurangan stok");
        if (stok < jumlah) {
            throw new StokTidakCukupException(nama, stok, jumlah);
        }
        stok -= jumlah;
    }

    public void tambahStok(int jumlah) {
        Validasi.wajibPositif(jumlah, "Jumlah penambahan stok");
        stok += jumlah;
    }

    public boolean isStokMenipis() {
        return stok > 0 && stok <= stokMinimal;
    }

    public boolean isHabis() {
        return stok <= 0;
    }

    public boolean isTersedia(int jumlah) {
        return stok >= jumlah;
    }

    public String getStatusStok() {
        if (isHabis()) {
            return "Habis";
        }
        return isStokMenipis() ? "Menipis" : "Tersedia";
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
        this.kode = Validasi.wajibIsi(kode, "Kode spare part");
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = Validasi.wajibIsi(nama, "Nama spare part");
    }

    public String getMerk() {
        return merk;
    }

    public void setMerk(String merk) {
        this.merk = merk;
    }

    public KategoriSparePart getKategori() {
        return kategori;
    }

    public final void setKategori(KategoriSparePart kategori) {
        if (kategori == null) {
            throw new IllegalArgumentException("Kategori spare part wajib dipilih.");
        }
        this.kategori = kategori;
    }

    public BigDecimal getHarga() {
        return harga;
    }

    public final void setHarga(BigDecimal harga) {
        if (harga == null || harga.signum() <= 0) {
            throw new IllegalArgumentException("Harga spare part harus lebih besar dari 0.");
        }
        this.harga = harga;
    }

    public int getStok() {
        return stok;
    }

    public void setStok(int stok) {
        if (stok < 0) {
            throw new IllegalArgumentException("Stok tidak boleh negatif.");
        }
        this.stok = stok;
    }

    public int getStokMinimal() {
        return stokMinimal;
    }

    public void setStokMinimal(int stokMinimal) {
        if (stokMinimal < 0) {
            throw new IllegalArgumentException("Stok minimal tidak boleh negatif.");
        }
        this.stokMinimal = stokMinimal;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    @Override
    public String toString() {
        return kode + " - " + nama;
    }
}
