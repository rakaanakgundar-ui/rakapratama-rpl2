package kaboygarage.model;

import jakarta.persistence.*;

/**
 * INHERITANCE: Pelanggan adalah Orang yang memesan spare part dan/atau
 * mendaftarkan kendaraannya untuk servis.
 */
@Entity
@Table(name = "pelanggan")
public class Pelanggan extends Orang {

    @Column(nullable = true, length = 100)
    private String email;

    @Column(nullable = true, length = 255)
    private String alamat;

    @OneToMany(mappedBy = "pelanggan", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Kendaraan> daftarKendaraan = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "pelanggan", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Pesanan> daftarPesanan = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "pelanggan", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<PendaftaranServis> daftarServis = new java.util.ArrayList<>();

    public Pelanggan() {
    }

    public Pelanggan(String nama, String noHp, String email, String alamat) {
        super(nama, noHp);
        this.email = email;
        this.alamat = alamat;
    }

    @Override
    public String getPeran() {
        return "Pelanggan";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    @Override
    public String toString() {
        return getNama() + " - " + getNoHp();
    }
}
