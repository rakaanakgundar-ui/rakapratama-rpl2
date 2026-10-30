package kaboygarage.tools;

import kaboygarage.db.Database;
import kaboygarage.dto.ItemPesanan;
import kaboygarage.dto.Statistik;
import kaboygarage.exception.AplikasiException;
import kaboygarage.exception.StokTidakCukupException;
import kaboygarage.model.Admin;
import kaboygarage.model.JenisServis;
import kaboygarage.model.Kendaraan;
import kaboygarage.model.Pelanggan;
import kaboygarage.model.PendaftaranServis;
import kaboygarage.model.Pesanan;
import kaboygarage.model.SparePart;
import kaboygarage.model.StatusPesanan;
import kaboygarage.model.StatusServis;
import kaboygarage.service.AuthService;
import kaboygarage.service.DashboardService;
import kaboygarage.service.PelangganService;
import kaboygarage.service.PesananService;
import kaboygarage.service.SparePartService;
import kaboygarage.service.WaitingListService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Smoke test database + service. Jalankan dengan:
 * java -cp "build\classes;lib\mysql-connector-j-8.3.0.jar" kaboygarage.tools.SmokeTest
 */
public final class SmokeTest {

    private SmokeTest() {
    }

    public static void main(String[] args) {
        Long pelangganId = null;
        boolean gagal = false;

        try {
            System.out.println("Database : " + Database.testConnection());

            AuthService authService = new AuthService();
            Admin admin = authService.login("admin", "admin123");
            cek(admin != null, "login admin / admin123");
            cek("Admin".equals(admin.getPeran()), "polymorphism Admin.getPeran()");

            try {
                authService.login("admin", "password-salah");
                cek(false, "login password salah ditolak");
            } catch (AplikasiException e) {
                cek(true, "login password salah ditolak");
            }

            SparePartService sparePartService = new SparePartService();
            List<SparePart> katalog = sparePartService.semua();
            cek(katalog.size() >= 5, "katalog spare part terbaca (" + katalog.size() + ")");
            SparePart part1 = katalog.get(0);
            SparePart part2 = katalog.get(1);
            int stokAwal1 = part1.getStok();

            String suffix = String.valueOf(System.currentTimeMillis());
            suffix = suffix.substring(Math.max(0, suffix.length() - 8));

            PelangganService pelangganService = new PelangganService();
            Pelanggan pelanggan = new Pelanggan(
                    "Pelanggan Uji " + suffix, "0812" + suffix,
                    "uji" + suffix + "@example.com", "Jl. Pengujian No. 1");
            pelangganService.simpan(pelanggan);
            pelangganId = pelanggan.getId();
            cek(pelangganId != null, "CREATE pelanggan");
            cek("Pelanggan".equals(pelanggan.getPeran()),
                    "polymorphism Pelanggan.getPeran()");

            Kendaraan kendaraan = new Kendaraan(
                    pelanggan, "Honda", "Vario Uji", "B " + suffix + " UJI", 2022);
            pelangganService.simpanKendaraan(kendaraan);
            cek(kendaraan.getId() != null, "CREATE kendaraan");
            cek(pelangganService.kendaraan(pelangganId).size() == 1,
                    "relasi pelanggan - kendaraan");

            PesananService pesananService = new PesananService();
            List<ItemPesanan> items = new ArrayList<>();
            items.add(new ItemPesanan(part1, 2));
            items.add(new ItemPesanan(part2, 1));
            Pesanan pesanan = pesananService.buatPesanan(pelanggan, items, "Smoke test");
            cek(pesanan.getId() != null, "CREATE pesanan + detail");
            cek(pesanan.hitungTotal().signum() > 0, "total pesanan dihitung");
            cek(pesanan.getStatus() == StatusPesanan.BARU, "status awal pesanan BARU");

            SparePart setelahPesan = sparePartService.cari(part1.getId());
            cek(setelahPesan.getStok() == stokAwal1 - 2,
                    "stok berkurang otomatis setelah pesanan");

            int stokSebelumGagal = sparePartService.cari(part1.getId()).getStok();
            try {
                pesananService.buatPesanan(pelanggan,
                        Arrays.asList(new ItemPesanan(part1, stokSebelumGagal + 1)),
                        "Stok tidak cukup");
                cek(false, "stok tidak cukup ditolak");
            } catch (StokTidakCukupException e) {
                cek(true, "stok tidak cukup ditolak");
            }
            cek(sparePartService.cari(part1.getId()).getStok() == stokSebelumGagal,
                    "rollback stok saat pesanan gagal");

            pesananService.ubahStatus(pesanan.getId(), StatusPesanan.DIPROSES);
            cek(pesananService.cari(pesanan.getId()).getStatus() == StatusPesanan.DIPROSES,
                    "status pesanan BARU -> DIPROSES");
            pesananService.ubahStatus(pesanan.getId(), StatusPesanan.SIAP_DIAMBIL);
            cek(pesananService.cari(pesanan.getId()).getStatus() == StatusPesanan.SIAP_DIAMBIL,
                    "status pesanan DIPROSES -> SIAP DIAMBIL");
            pesananService.ubahStatus(pesanan.getId(), StatusPesanan.SELESAI);
            cek(pesananService.cari(pesanan.getId()).getStatus() == StatusPesanan.SELESAI,
                    "status pesanan SIAP DIAMBIL -> SELESAI");

            Pesanan pesananBatal = pesananService.buatPesanan(
                    pelanggan, Arrays.asList(new ItemPesanan(part2, 1)), "Akan dibatalkan");
            int stokSebelumBatal = sparePartService.cari(part2.getId()).getStok();
            pesananService.batalkan(pesananBatal.getId());
            cek(sparePartService.cari(part2.getId()).getStok() == stokSebelumBatal + 1,
                    "pembatalan pesanan mengembalikan stok");
            cek(pesananService.cari(pesananBatal.getId()).getStatus()
                    == StatusPesanan.DIBATALKAN, "status pesanan DIBATALKAN");

            WaitingListService waitingListService = new WaitingListService();
            PendaftaranServis servis = waitingListService.daftar(
                    pelanggan, kendaraan, JenisServis.SERVIS_RINGAN,
                    "Keluhan uji integrasi waiting list");
            cek(servis.getId() != null, "CREATE pendaftaran servis");
            cek(servis.getStatus() == StatusServis.MENUNGGU, "status awal servis MENUNGGU");

            LocalDateTime jadwal = LocalDateTime.now().plusDays(1)
                    .withHour(10).withMinute(0).withSecond(0).withNano(0);
            waitingListService.jadwalkan(servis.getId(), jadwal);
            cek(waitingListService.cari(servis.getId()).getStatus()
                    == StatusServis.DIJADWALKAN, "jadwal servis disimpan");
            waitingListService.ubahStatus(servis.getId(), StatusServis.DIKERJAKAN);
            cek(waitingListService.cari(servis.getId()).getStatus()
                    == StatusServis.DIKERJAKAN, "status servis DIJADWALKAN -> DIKERJAKAN");
            waitingListService.ubahStatus(servis.getId(), StatusServis.SELESAI);
            cek(waitingListService.cari(servis.getId()).getStatus()
                    == StatusServis.SELESAI, "status servis DIKERJAKAN -> SELESAI");

            Statistik statistik = new DashboardService().statistik();
            cek(statistik.getTotalSparePart() >= 5, "statistik dashboard terbaca");
            System.out.println("           spare part=" + statistik.getTotalSparePart()
                    + ", pesanan aktif=" + statistik.getPesananAktif()
                    + ", antrean aktif=" + statistik.getAntreanAktif()
                    + ", pelanggan=" + statistik.getTotalPelanggan());

            System.out.println();
            System.out.println("SEMUA SMOKE TEST LULUS.");
        } catch (Throwable ex) {
            System.out.println();
            System.out.println("SMOKE TEST GAGAL: " + ex.getMessage());
            ex.printStackTrace(System.out);
            gagal = true;
        } finally {
            bersihkan(pelangganId);
        }
        if (gagal) {
            System.exit(1);
        }
    }

    private static void cek(boolean kondisi, String pesan) {
        if (!kondisi) {
            throw new IllegalStateException("Gagal: " + pesan);
        }
        System.out.println("[OK] " + pesan);
    }

    /**
     * Menghapus data uji dan mengembalikan stok pesanan yang belum dibatalkan.
     */
    private static void bersihkan(Long pelangganId) {
        if (pelangganId == null) {
            return;
        }
        System.out.println();
        System.out.println("Membersihkan data uji...");
        try (Connection connection = Database.getConnection()) {
            String restore = "UPDATE spare_part sp JOIN ("
                    + "SELECT d.spare_part_id, SUM(d.jumlah) AS qty "
                    + "FROM detail_pesanan d JOIN pesanan p ON p.id = d.pesanan_id "
                    + "WHERE p.pelanggan_id = ? AND p.status <> 'DIBATALKAN' "
                    + "GROUP BY d.spare_part_id"
                    + ") x ON x.spare_part_id = sp.id "
                    + "SET sp.stok = sp.stok + x.qty";
            try (PreparedStatement statement = connection.prepareStatement(restore)) {
                statement.setLong(1, pelangganId);
                statement.executeUpdate();
            }
            hapus(connection, "DELETE FROM pesanan WHERE pelanggan_id = ?", pelangganId);
            hapus(connection, "DELETE FROM pendaftaran_servis WHERE pelanggan_id = ?", pelangganId);
            hapus(connection, "DELETE FROM kendaraan WHERE pelanggan_id = ?", pelangganId);
            hapus(connection, "DELETE FROM pelanggan WHERE id = ?", pelangganId);
            System.out.println("[OK] data uji dibersihkan");
        } catch (Exception ex) {
            System.out.println("[PERINGATAN] gagal membersihkan data uji: " + ex.getMessage());
        }
    }

    private static void hapus(Connection connection, String sql, Long id) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }
}
