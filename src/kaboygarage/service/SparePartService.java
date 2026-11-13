package kaboygarage.service;

import kaboygarage.exception.AplikasiException;
import kaboygarage.model.SparePart;
import kaboygarage.repository.SparePartRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service pengelolaan katalog spare part.
 */
@Service
public class SparePartService {

    @Autowired
    private SparePartRepository sparePartRepository;

    public List<SparePart> semua() {
        return sparePartRepository.findAll();
    }

    public List<SparePart> cari(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return sparePartRepository.findAll();
        }
        String pola = "%" + keyword.trim() + "%";
        return sparePartRepository.findByKeyword(pola);
    }

    public SparePart cari(Long id) {
        return sparePartRepository.findById(id).orElse(null);
    }

    public void simpan(SparePart sparePart) {
        if (sparePart == null) {
            throw new AplikasiException("Data spare part tidak boleh kosong.");
        }
        try {
            sparePartRepository.save(sparePart);
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                throw new AplikasiException("Kode spare part sudah dipakai. Gunakan kode lain.");
            }
            throw e;
        }
    }

    public void hapus(Long id) {
        if (id == null) {
            throw new AplikasiException("Spare part wajib dipilih.");
        }
        sparePartRepository.deleteById(id);
    }

    public List<SparePart> stokMenipis() {
        return sparePartRepository.findMenipis();
    }

    public int total() {
        return (int) sparePartRepository.count();
    }

    public int totalStokMenipis() {
        return (int) sparePartRepository.countMenipis();
    }

    public String kodeBerikutnya() {
        List<SparePart> semua = sparePartRepository.findAll();
        if (semua.isEmpty()) {
            return "SP001";
        }
        int maxNum = 0;
        for (SparePart sp : semua) {
            if (sp.getKode() != null && sp.getKode().startsWith("SP")) {
                try {
                    int num = Integer.parseInt(sp.getKode().substring(2));
                    if (num > maxNum) maxNum = num;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return String.format("SP%03d", maxNum + 1);
    }
}
