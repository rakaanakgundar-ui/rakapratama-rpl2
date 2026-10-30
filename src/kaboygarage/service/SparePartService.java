package kaboygarage.service;

import kaboygarage.dao.SparePartDao;
import kaboygarage.db.DataAccessException;
import kaboygarage.exception.AplikasiException;
import kaboygarage.model.SparePart;

import java.util.List;

/**
 * Service pengelolaan katalog spare part.
 */
public class SparePartService {

    private final SparePartDao sparePartDao = new SparePartDao();

    public List<SparePart> semua() {
        return sparePartDao.findAll();
    }

    public List<SparePart> cari(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return sparePartDao.findAll();
        }
        return sparePartDao.findByKeyword(keyword.trim());
    }

    public SparePart cari(Long id) {
        return sparePartDao.findById(id);
    }

    public void simpan(SparePart sparePart) {
        if (sparePart == null) {
            throw new AplikasiException("Data spare part tidak boleh kosong.");
        }
        try {
            if (sparePart.getId() == null) {
                sparePartDao.insert(sparePart);
            } else {
                sparePartDao.update(sparePart);
            }
        } catch (DataAccessException e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                throw new AplikasiException("Kode spare part sudah dipakai. Gunakan kode lain.");
            }
            throw e;
        }
    }

    public void hapus(Long id) {
        ValidasiId.wajib(id, "Spare part");
        sparePartDao.delete(id);
    }

    public String kodeBerikutnya() {
        return sparePartDao.nextKode();
    }

    public List<SparePart> stokMenipis() {
        return sparePartDao.findMenipis();
    }

    public int total() {
        return sparePartDao.count();
    }

    public int totalStokMenipis() {
        return sparePartDao.countMenipis();
    }

    /**
     * Validasi id sederhana agar pesan error konsisten.
     */
    private static final class ValidasiId {

        private ValidasiId() {
        }

        static void wajib(Long id, String nama) {
            if (id == null) {
                throw new AplikasiException(nama + " wajib dipilih.");
            }
        }
    }
}
