package kaboygarage.service;

import kaboygarage.dao.PendaftaranServisDao;
import kaboygarage.exception.AplikasiException;
import kaboygarage.model.JenisServis;
import kaboygarage.model.Kendaraan;
import kaboygarage.model.Pelanggan;
import kaboygarage.model.PendaftaranServis;
import kaboygarage.model.StatusServis;
import kaboygarage.util.KodeGenerator;
import kaboygarage.util.Validasi;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pendaftaran servis (waiting list) dan penjadwalannya.
 */
public class WaitingListService {

    private final PendaftaranServisDao servisDao = new PendaftaranServisDao();

    public PendaftaranServis daftar(Pelanggan pelanggan, Kendaraan kendaraan,
                                    JenisServis jenisServis, String keluhan) {
        Validasi.wajibAda(pelanggan, "Pelanggan");
        Validasi.wajibAda(kendaraan, "Kendaraan");
        Validasi.wajibAda(jenisServis, "Jenis servis");
        if (pelanggan.getId() == null || kendaraan.getId() == null) {
            throw new AplikasiException("Pelanggan dan kendaraan harus sudah tersimpan.");
        }
        if (kendaraan.getPelanggan() == null
                || !pelanggan.getId().equals(kendaraan.getPelanggan().getId())) {
            throw new AplikasiException("Kendaraan yang dipilih bukan milik pelanggan tersebut.");
        }

        LocalDateTime sekarang = LocalDateTime.now();
        int nomor = servisDao.nextNomorAntrean();
        PendaftaranServis pendaftaran = new PendaftaranServis(
                KodeGenerator.kodeAntreanServis(sekarang, nomor),
                nomor, pelanggan, kendaraan, jenisServis, keluhan);
        servisDao.insert(pendaftaran);
        return pendaftaran;
    }

    public List<PendaftaranServis> semua() {
        return servisDao.findAll();
    }

    public PendaftaranServis cari(Long id) {
        return servisDao.findById(id);
    }

    public List<PendaftaranServis> terbaru(int jumlah) {
        List<PendaftaranServis> semua = servisDao.findAll();
        if (semua.size() <= jumlah) {
            return semua;
        }
        return new ArrayList<>(semua.subList(0, jumlah));
    }

    public PendaftaranServis jadwalkan(Long id, LocalDateTime jadwal) {
        PendaftaranServis pendaftaran = servisDao.findById(id);
        if (pendaftaran == null) {
            throw new AplikasiException("Pendaftaran servis tidak ditemukan.");
        }
        pendaftaran.jadwalkan(jadwal);
        servisDao.updateJadwal(id, jadwal, pendaftaran.getStatus(),
                pendaftaran.getCatatanAdmin());
        return pendaftaran;
    }

    public PendaftaranServis ubahStatus(Long id, StatusServis statusBaru) {
        PendaftaranServis pendaftaran = servisDao.findById(id);
        if (pendaftaran == null) {
            throw new AplikasiException("Pendaftaran servis tidak ditemukan.");
        }
        pendaftaran.ubahStatus(statusBaru);
        servisDao.updateStatus(id, statusBaru);
        return pendaftaran;
    }

    public PendaftaranServis batalkan(Long id) {
        return ubahStatus(id, StatusServis.DIBATALKAN);
    }

    public int totalAktif() {
        return servisDao.countAktif();
    }
}
