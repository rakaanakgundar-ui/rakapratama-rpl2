package kaboygarage.service;

import kaboygarage.dao.KendaraanDao;
import kaboygarage.dao.PelangganDao;
import kaboygarage.exception.AplikasiException;
import kaboygarage.model.Kendaraan;
import kaboygarage.model.Pelanggan;

import java.util.List;

/**
 * Service pengelolaan pelanggan dan kendaraannya.
 */
public class PelangganService {

    private final PelangganDao pelangganDao = new PelangganDao();
    private final KendaraanDao kendaraanDao = new KendaraanDao();

    public List<Pelanggan> semua() {
        return pelangganDao.findAll();
    }

    public Pelanggan cari(Long id) {
        return pelangganDao.findById(id);
    }

    public void simpan(Pelanggan pelanggan) {
        if (pelanggan == null) {
            throw new AplikasiException("Data pelanggan tidak boleh kosong.");
        }
        if (pelanggan.getId() == null) {
            pelangganDao.insert(pelanggan);
        } else {
            pelangganDao.update(pelanggan);
        }
    }

    public void hapus(Long id) {
        if (id == null) {
            throw new AplikasiException("Pelanggan wajib dipilih.");
        }
        pelangganDao.delete(id);
    }

    public List<Kendaraan> kendaraan(Long pelangganId) {
        if (pelangganId == null) {
            throw new AplikasiException("Pelanggan wajib dipilih.");
        }
        return kendaraanDao.findByPelanggan(pelangganId);
    }

    public List<Kendaraan> semuaKendaraan() {
        return kendaraanDao.findAll();
    }

    public void simpanKendaraan(Kendaraan kendaraan) {
        if (kendaraan == null) {
            throw new AplikasiException("Data kendaraan tidak boleh kosong.");
        }
        if (kendaraan.getPelanggan() == null || kendaraan.getPelanggan().getId() == null) {
            throw new AplikasiException("Pilih pelanggan pemilik kendaraan terlebih dahulu.");
        }
        if (kendaraan.getId() == null) {
            kendaraanDao.insert(kendaraan);
        } else {
            kendaraanDao.update(kendaraan);
        }
    }

    public void hapusKendaraan(Long id) {
        if (id == null) {
            throw new AplikasiException("Kendaraan wajib dipilih.");
        }
        kendaraanDao.delete(id);
    }

    public int totalPelanggan() {
        return pelangganDao.count();
    }

    public int totalKendaraan() {
        return kendaraanDao.count();
    }
}
