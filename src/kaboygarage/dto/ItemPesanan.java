package kaboygarage.dto;

import kaboygarage.model.SparePart;

import java.math.BigDecimal;

/**
 * Item sementara pada keranjang pesanan sebelum disimpan ke database.
 */
public class ItemPesanan {

    private final SparePart sparePart;
    private int jumlah;

    public ItemPesanan(SparePart sparePart, int jumlah) {
        if (sparePart == null) {
            throw new IllegalArgumentException("Spare part wajib dipilih.");
        }
        if (jumlah <= 0) {
            throw new IllegalArgumentException("Jumlah item harus lebih besar dari 0.");
        }
        this.sparePart = sparePart;
        this.jumlah = jumlah;
    }

    public SparePart getSparePart() {
        return sparePart;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void tambahJumlah(int tambahan) {
        if (tambahan <= 0) {
            throw new IllegalArgumentException("Tambahan jumlah harus positif.");
        }
        this.jumlah += tambahan;
    }

    public BigDecimal getSubtotal() {
        return sparePart.getHarga().multiply(BigDecimal.valueOf(jumlah));
    }
}
