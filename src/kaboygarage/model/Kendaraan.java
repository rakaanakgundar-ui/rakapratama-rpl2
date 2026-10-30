package kaboygarage.model;

import kaboygarage.util.Validasi;

/**
 * Kendaraan milik pelanggan yang didaftarkan untuk servis.
 *
 * <p>Relasi ke {@link Pelanggan} menunjukkan composition/association: satu
 * kendaraan pasti dimiliki satu pelanggan.</p>
 */
public class Kendaraan {

    private Long id;
    private Pelanggan pelanggan;
    private String merk;
    private String model;
    private String platNomor;
    private int tahun;

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
