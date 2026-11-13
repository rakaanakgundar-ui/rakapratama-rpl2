package kaboygarage.service;

import kaboygarage.dto.Statistik;
import kaboygarage.model.PendaftaranServis;
import kaboygarage.model.Pesanan;
import kaboygarage.model.SparePart;
import kaboygarage.repository.PelangganRepository;
import kaboygarage.repository.PendaftaranServisRepository;
import kaboygarage.repository.PesananRepository;
import kaboygarage.repository.SparePartRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service yang menyiapkan ringkasan data untuk dashboard.
 */
@Service
public class DashboardService {

    @Autowired
    private SparePartRepository sparePartRepository;

    @Autowired
    private PesananRepository pesananRepository;

    @Autowired
    private PendaftaranServisRepository servisRepository;

    @Autowired
    private PelangganRepository pelangganRepository;

    public Statistik statistik() {
        return new Statistik(
                (int) sparePartRepository.count(),
                (int) pesananRepository.countAktif(),
                (int) servisRepository.countAktif(),
                (int) pelangganRepository.count(),
                (int) sparePartRepository.countMenipis());
    }

    public List<SparePart> stokMenipis() {
        return sparePartRepository.findMenipis();
    }

    public List<Pesanan> pesananTerbaru(int jumlah) {
        List<Pesanan> semua = pesananRepository.findAll();
        if (semua.size() <= jumlah) {
            return semua;
        }
        return semua.subList(0, jumlah);
    }

    public List<PendaftaranServis> antreanTerbaru(int jumlah) {
        List<PendaftaranServis> semua = servisRepository.findAll();
        if (semua.size() <= jumlah) {
            return semua;
        }
        return semua.subList(0, jumlah);
    }
}
