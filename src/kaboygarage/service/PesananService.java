package kaboygarage.service;

import kaboygarage.dto.ItemPesanan;
import kaboygarage.exception.AplikasiException;
import kaboygarage.exception.StokTidakCukupException;
import kaboygarage.model.DetailPesanan;
import kaboygarage.model.Pelanggan;
import kaboygarage.model.Pesanan;
import kaboygarage.model.SparePart;
import kaboygarage.model.StatusPesanan;
import kaboygarage.repository.PesananRepository;
import kaboygarage.repository.SparePartRepository;
import kaboygarage.util.KodeGenerator;
import kaboygarage.util.Validasi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service pembuatan pesanan dan perubahan statusnya.
 */
@Service
public class PesananService {

    @Autowired
    private PesananRepository pesananRepository;

    @Autowired
    private SparePartRepository sparePartRepository;

    @Transactional
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
            SparePart terkini = sparePartRepository.findById(item.getSparePart().getId())
                    .orElseThrow(() -> new AplikasiException("Spare part sudah tidak tersedia di katalog."));
            
            if (terkini.getStok() < item.getJumlah()) {
                throw new StokTidakCukupException(terkini.getNama(), terkini.getStok(), item.getJumlah());
            }
            
            pesanan.tambahDetail(terkini, item.getJumlah());
        }
        pesanan.setCatatan(catatan == null ? null : catatan.trim());

        // Kurangi stok
        for (DetailPesanan detail : pesanan.getDetail()) {
            SparePart sp = detail.getSparePart();
            sp.kurangiStok(detail.getJumlah());
            sparePartRepository.save(sp);
        }

        return pesananRepository.save(pesanan);
    }

    public List<Pesanan> semua() {
        return pesananRepository.findAll();
    }

    public Pesanan cari(Long id) {
        return pesananRepository.findById(id).orElse(null);
    }

    public List<Pesanan> terbaru(int jumlah) {
        List<Pesanan> semua = pesananRepository.findAll();
        if (semua.size() <= jumlah) {
            return semua;
        }
        return new ArrayList<>(semua.subList(0, jumlah));
    }

    @Transactional
    public Pesanan ubahStatus(Long id, StatusPesanan statusBaru) {
        Pesanan pesanan = pesananRepository.findById(id)
                .orElseThrow(() -> new AplikasiException("Pesanan tidak ditemukan."));
        pesanan.ubahStatus(statusBaru);

        if (statusBaru == StatusPesanan.DIBATALKAN) {
            // Kembalikan stok
            for (DetailPesanan detail : pesanan.getDetail()) {
                SparePart sp = detail.getSparePart();
                sp.tambahStok(detail.getJumlah());
                sparePartRepository.save(sp);
            }
        }

        return pesananRepository.save(pesanan);
    }

    public Pesanan batalkan(Long id) {
        return ubahStatus(id, StatusPesanan.DIBATALKAN);
    }

    public int totalAktif() {
        return (int) pesananRepository.countAktif();
    }
}

