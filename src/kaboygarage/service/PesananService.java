package kaboygarage.service;

import kaboygarage.dao.PesananDao;
import kaboygarage.dao.SparePartDao;
import kaboygarage.db.Database;
import kaboygarage.dto.ItemPesanan;
import kaboygarage.exception.AplikasiException;
import kaboygarage.exception.StokTidakCukupException;
import kaboygarage.model.DetailPesanan;
import kaboygarage.model.Pelanggan;
import kaboygarage.model.Pesanan;
import kaboygarage.model.SparePart;
import kaboygarage.model.StatusPesanan;
import kaboygarage.util.KodeGenerator;
import kaboygarage.util.Validasi;

import java.util.ArrayList;
import java.util.List;

/**
 * Service pembuatan pesanan dan perubahan statusnya.
 *
 * <p>Pembuatan pesanan dan pengurangan stok dijalankan dalam satu transaksi
 * database (atomic), sehingga tidak ada pesanan yang tersimpan tanpa stok
 * yang benar.</p>
 */
public class PesananService {

    private final PesananDao pesananDao = new PesananDao();
    private final SparePartDao sparePartDao = new SparePartDao();

    public Pesanan buatPesanan(Pelanggan pelanggan, List<ItemPesanan> items, String catatan) {
        Validasi.wajibAda(pelanggan, "Pelanggan");
        if (items == null || items.isEmpty()) {
            throw new AplikasiException("Tambahkan minimal satu spare part ke pesanan.");
        }

        Pesanan pesanan = new Pesanan(KodeGenerator.kodePesanan(), pelanggan);
        for (ItemPesanan item : items) {
            if (item.getSparePart() == null || item.getSparePart().getId() == null) {
                throw new AplikasiException("Spare part pesanan tidak valid.");
            }
            SparePart terkini = sparePartDao.findById(item.getSparePart().getId());
            if (terkini == null) {
                throw new AplikasiException("Spare part sudah tidak tersedia di katalog.");
            }
            pesanan.tambahDetail(terkini, item.getJumlah());
        }
        pesanan.setCatatan(catatan == null ? null : catatan.trim());

        return Database.inTransaction(connection -> {
            for (DetailPesanan detail : pesanan.getDetail()) {
                boolean cukup = sparePartDao.kurangiStok(
                        connection, detail.getSparePart().getId(), detail.getJumlah());
                if (!cukup) {
                    SparePart cek = sparePartDao.findById(detail.getSparePart().getId());
                    int stok = cek == null ? 0 : cek.getStok();
                    throw new StokTidakCukupException(
                            detail.getSparePart().getNama(), stok, detail.getJumlah());
                }
            }
            pesananDao.insert(connection, pesanan);
            return pesanan;
        });
    }

    public List<Pesanan> semua() {
        return pesananDao.findAll();
    }

    public Pesanan cari(Long id) {
        return pesananDao.findById(id);
    }

    public List<Pesanan> terbaru(int jumlah) {
        List<Pesanan> semua = pesananDao.findAll();
        if (semua.size() <= jumlah) {
            return semua;
        }
        return new ArrayList<>(semua.subList(0, jumlah));
    }

    /**
     * Mengubah status pesanan. Bila pesanan dibatalkan, stok dikembalikan.
     */
    public Pesanan ubahStatus(Long id, StatusPesanan statusBaru) {
        Pesanan pesanan = pesananDao.findById(id);
        if (pesanan == null) {
            throw new AplikasiException("Pesanan tidak ditemukan.");
        }
        pesanan.ubahStatus(statusBaru);

        if (statusBaru == StatusPesanan.DIBATALKAN) {
            Database.inTransaction(connection -> {
                for (DetailPesanan detail : pesanan.getDetail()) {
                    sparePartDao.tambahStok(
                            connection, detail.getSparePart().getId(), detail.getJumlah());
                }
                pesananDao.updateStatus(connection, id, statusBaru);
                return null;
            });
        } else {
            pesananDao.updateStatus(id, statusBaru);
        }
        return pesanan;
    }

    public Pesanan batalkan(Long id) {
        return ubahStatus(id, StatusPesanan.DIBATALKAN);
    }

    public int totalAktif() {
        return pesananDao.countAktif();
    }
}
