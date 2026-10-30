package kaboygarage.model;

import kaboygarage.util.Validasi;

import java.util.Objects;

/**
 * ABSTRACT CLASS: induk dari Admin dan Pelanggan.
 *
 * <p>Semua atribut private (encapsulation). Class ini tidak bisa dibuat
 * langsung karena perannya ditentukan oleh subclass melalui
 * {@link #getPeran()} (abstraction + polymorphism).</p>
 */
public abstract class Orang {

    private Long id;
    private String nama;
    private String noHp;

    protected Orang() {
    }

    protected Orang(String nama, String noHp) {
        setNama(nama);
        setNoHp(noHp);
    }

    /**
     * ABSTRACT METHOD: setiap turunan wajib menjelaskan perannya sendiri.
     */
    public abstract String getPeran();

    public String tampilInfo() {
        return getNama() + " (" + getPeran() + ")";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public final void setNama(String nama) {
        this.nama = Validasi.wajibIsi(nama, "Nama");
    }

    public String getNoHp() {
        return noHp;
    }

    public final void setNoHp(String noHp) {
        this.noHp = Validasi.wajibIsi(noHp, "Nomor HP");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Orang)) {
            return false;
        }
        Orang lain = (Orang) obj;
        return id != null && Objects.equals(id, lain.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
