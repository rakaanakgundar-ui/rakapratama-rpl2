package kaboygarage.model;

import kaboygarage.util.Validasi;

import jakarta.persistence.*;

/**
 * Kendaraan milik pelanggan yang didaftarkan untuk servis.
 *
 * <p>Relasi ke {@link Pelanggan} menunjukkan composition/association: satu
 * kendaraan pasti dimiliki satu pelanggan.</p>
 */
@Entity
@Table(name = "kendaraan", uniqueConstraints = {
    @UniqueConstraint(name = "uk_kendaraan_plat", columnNames = "plat_nomor")
})
public class Kendaraan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pelanggan_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_kendaraan_pelanggan"))
    private Pelanggan pelanggan;

    @Column(nullable = false, length = 50)
    private String merk;

    @Column(nullable = false, length = 50)
    private String model;

    @Column(nullable = false, name = "plat_nomor", length = 15)
    private String platNomor;

    @Column(nullable = false)
    private int tahun;

    @OneToMany(mappedBy = "kendaraan", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<PendaftaranServis> daftarServis = new java.util.ArrayList<>();

    public Kendaraan() {
    }

    public Kendaraan(Pelanggan pelanggan, String merk, String model,
                     String platNomor, int tahun) {
        setPelanggan(pelanggan);
        this.merk = Validasi.wajibIsi(merk, "Merk kendaraan");
        this.model = Validasi.wajibIsi(model, "Model kendaraan");
        this.platNomor = Validasi.wajibIsi(platNomor, "Plat nomor").toUpperCase();
        if (tahun < 1950 || tahun > java.time.Year.now().getValue() + 1) {
            throw new IllegalArgumentException("Tahun kendaraan tidak wajar.");
        }
        this.tahun = tahun;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pelanggan getPelanggan() {
        return pelanggan;
    }

    public final void setPelanggan(Pelanggan pelanggan) {
        if (pelanggan == null) {
            throw new IllegalArgumentException("Pelanggan kendaraan wajib dipilih.");
        }
        this.pelanggan = pelanggan;
    }

    public String getMerk() {
        return merk;
    }

    public void setMerk(String merk) {
        this.merk = Validasi.wajibIsi(merk, "Merk kendaraan");
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = Validasi.wajibIsi(model, "Model kendaraan");
    }

    public String getPlatNomor() {
        return platNomor;
    }

    public void setPlatNomor(String platNomor) {
        this.platNomor = Validasi.wajibIsi(platNomor, "Plat nomor").toUpperCase();
    }

    public int getTahun() {
        return tahun;
    }

    public void setTahun(int tahun) {
        this.tahun = tahun;
    }

    public String getDeskripsi() {
        return merk + " " + model + " (" + platNomor + ")";
    }

    @Override
    public String toString() {
        return platNomor + " - " + merk + " " + model;
    }
}
