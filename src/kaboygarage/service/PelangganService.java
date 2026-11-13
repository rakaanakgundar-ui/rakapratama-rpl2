package kaboygarage.service;

import kaboygarage.exception.AplikasiException;
import kaboygarage.model.Kendaraan;
import kaboygarage.model.Pelanggan;
import kaboygarage.repository.KendaraanRepository;
import kaboygarage.repository.PelangganRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service pengelolaan pelanggan dan kendaraannya.
 */
@Service
public class PelangganService {

    @Autowired
    private PelangganRepository pelangganRepository;

    @Autowired
    private KendaraanRepository kendaraanRepository;

    public List<Pelanggan> semua() {
        return pelangganRepository.findAll();
    }

    public Pelanggan cari(Long id) {
        return pelangganRepository.findById(id).orElse(null);
    }

    public void simpan(Pelanggan pelanggan) {
        if (pelanggan == null) {
            throw new AplikasiException("Data pelanggan tidak boleh kosong.");
        }
        pelangganRepository.save(pelanggan);
    }

    public void hapus(Long id) {
        if (id == null) {
            throw new AplikasiException("Pelanggan wajib dipilih.");
        }
        pelangganRepository.deleteById(id);
    }

    public List<Kendaraan> kendaraan(Long pelangganId) {
        if (pelangganId == null) {
            throw new AplikasiException("Pelanggan wajib dipilih.");
        }
        return kendaraanRepository.findByPelangganId(pelangganId);
    }

    public List<Kendaraan> semuaKendaraan() {
        return kendaraanRepository.findAll();
    }

    public void simpanKendaraan(Kendaraan kendaraan) {
        if (kendaraan == null) {
            throw new AplikasiException("Data kendaraan tidak boleh kosong.");
        }
        if (kendaraan.getPelanggan() == null || kendaraan.getPelanggan().getId() == null) {
            throw new AplikasiException("Pilih pelanggan pemilik kendaraan terlebih dahulu.");
        }
        kendaraanRepository.save(kendaraan);
    }

    public void hapusKendaraan(Long id) {
        if (id == null) {
            throw new AplikasiException("Kendaraan wajib dipilih.");
        }
        kendaraanRepository.deleteById(id);
    }

    public int totalPelanggan() {
        return (int) pelangganRepository.count();
    }

    public int totalKendaraan() {
        return (int) kendaraanRepository.count();
    }
}
