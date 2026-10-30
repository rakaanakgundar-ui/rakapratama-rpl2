package kaboygarage.service;

import kaboygarage.dao.PelangganDao;
import kaboygarage.dao.PendaftaranServisDao;
import kaboygarage.dao.PesananDao;
import kaboygarage.dao.SparePartDao;
import kaboygarage.dto.Statistik;
import kaboygarage.model.PendaftaranServis;
import kaboygarage.model.Pesanan;
import kaboygarage.model.SparePart;

import java.util.List;

/**
 * Service yang menyiapkan ringkasan data untuk dashboard.
 */
public class DashboardService {

    private final SparePartDao sparePartDao = new SparePartDao();
    private final PesananDao pesananDao = new PesananDao();
    private final PendaftaranServisDao servisDao = new PendaftaranServisDao();
    private final PelangganDao pelangganDao = new PelangganDao();

    public Statistik statistik() {
        return new Statistik(
                sparePartDao.count(),
                pesananDao.countAktif(),
                servisDao.countAktif(),
                pelangganDao.count(),
                sparePartDao.countMenipis());
    }

    public List<SparePart> stokMenipis() {
        return sparePartDao.findMenipis();
    }

    public List<Pesanan> pesananTerbaru(int jumlah) {
        List<Pesanan> semua = pesananDao.findAll();
        if (semua.size() <= jumlah) {
            return semua;
        }
        return semua.subList(0, jumlah);
    }

    public List<PendaftaranServis> antreanTerbaru(int jumlah) {
        List<PendaftaranServis> semua = servisDao.findAll();
        if (semua.size() <= jumlah) {
            return semua;
        }
        return semua.subList(0, jumlah);
    }
}
