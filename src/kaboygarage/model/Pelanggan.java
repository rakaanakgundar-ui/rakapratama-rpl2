package kaboygarage.model;

/**
 * INHERITANCE: Pelanggan adalah Orang yang memesan spare part dan/atau
 * mendaftarkan kendaraannya untuk servis.
 */
public class Pelanggan extends Orang {

    private String email;
    private String alamat;

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
