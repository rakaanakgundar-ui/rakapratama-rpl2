package kaboygarage.service;

import kaboygarage.exception.AplikasiException;
import kaboygarage.model.JenisServis;
import kaboygarage.model.Kendaraan;
import kaboygarage.model.Pelanggan;
import kaboygarage.model.PendaftaranServis;
import kaboygarage.model.StatusServis;
import kaboygarage.repository.PendaftaranServisRepository;
import kaboygarage.util.KodeGenerator;
import kaboygarage.util.Validasi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pendaftaran servis (waiting list) dan penjadwalannya.
 */
@Service
public class WaitingListService {

    @Autowired
    private PendaftaranServisRepository servisRepository;

    @Transactional
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
        int nomor = (int) (servisRepository.countAktif() + 1);
        PendaftaranServis pendaftaran = new PendaftaranServis(
                KodeGenerator.kodeAntreanServis(sekarang, nomor),
                nomor, pelanggan, kendaraan, jenisServis, keluhan);
        return servisRepository.save(pendaftaran);
    }

    public List<PendaftaranServis> semua() {
        return servisRepository.findAll();
    }

    public PendaftaranServis cari(Long id) {
        return servisRepository.findById(id).orElse(null);
    }

    public List<PendaftaranServis> terbaru(int jumlah) {
        List<PendaftaranServis> semua = servisRepository.findAll();
        if (semua.size() <= jumlah) {
            return semua;
        }
        return new ArrayList<>(semua.subList(0, jumlah));
    }

    @Transactional
    public PendaftaranServis jadwalkan(Long id, LocalDateTime jadwal) {
        PendaftaranServis pendaftaran = servisRepository.findById(id)
                .orElseThrow(() -> new AplikasiException("Pendaftaran servis tidak ditemukan."));
        pendaftaran.jadwalkan(jadwal);
        return servisRepository.save(pendaftaran);
    }

    @Transactional
    public PendaftaranServis ubahStatus(Long id, StatusServis statusBaru) {
        PendaftaranServis pendaftaran = servisRepository.findById(id)
                .orElseThrow(() -> new AplikasiException("Pendaftaran servis tidak ditemukan."));
        pendaftaran.ubahStatus(statusBaru);
        return servisRepository.save(pendaftaran);
    }

    public PendaftaranServis batalkan(Long id) {
        return ubahStatus(id, StatusServis.DIBATALKAN);
    }

    public int totalAktif() {
        return (int) servisRepository.countAktif();
    }
}
