package kaboygarage.gui;

import kaboygarage.exception.AplikasiException;
import kaboygarage.model.JenisServis;
import kaboygarage.model.Kendaraan;
import kaboygarage.model.Pelanggan;
import kaboygarage.model.PendaftaranServis;
import kaboygarage.model.StatusServis;
import kaboygarage.service.PelangganService;
import kaboygarage.service.WaitingListService;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Panel pendaftaran servis (waiting list) dan penjadwalannya.
 */
public class WaitingListPanel extends JPanel implements Refreshable {

    private final WaitingListService waitingListService;
    private final PelangganService pelangganService;

    private final JComboBox<Pelanggan> pelangganCombo = new JComboBox<>();
    private final JComboBox<Kendaraan> kendaraanCombo = new JComboBox<>();
    private final JComboBox<JenisServis> jenisCombo = new JComboBox<>(JenisServis.values());
    private final JTextArea keluhanArea = Theme.textArea(2);

    private final JTable tabel = Theme.table();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Kode", "No", "Pelanggan", "Kendaraan", "Jenis",
                "Keluhan", "Jadwal", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final List<PendaftaranServis> data = new java.util.ArrayList<>();
    private Long selectedId;

    private final JComboBox<StatusServis> statusCombo = new JComboBox<>();
    private final JButton jadwalkanButton = Theme.primaryButton("Jadwalkan");
    private final JButton ubahStatusButton = Theme.primaryButton("Ubah Status");
    private final JButton batalkanButton = Theme.dangerButton("Batalkan");

    public WaitingListPanel(WaitingListService waitingListService,
                            PelangganService pelangganService) {
        this.waitingListService = waitingListService;
        this.pelangganService = pelangganService;
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));

        add(buatPanelDaftar(), BorderLayout.NORTH);
        add(buatPanelWaitingList(), BorderLayout.CENTER);

        pelangganCombo.addActionListener(e -> muatKendaraanCombo());
        perbaruiTombol();
    }

    private JPanel buatPanelDaftar() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 10));

        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setOpaque(false);
        header.add(Theme.bold("Daftar Servis Baru"), BorderLayout.NORTH);
        header.add(Theme.muted("Pelanggan mendaftar servis, kendaraan masuk waiting list"),
                BorderLayout.CENTER);

        JPanel isi = new JPanel(new BorderLayout(0, 10));
        isi.setOpaque(false);

        JPanel pilih = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pilih.setOpaque(false);
        pelangganCombo.setPreferredSize(new Dimension(200, 32));
        kendaraanCombo.setPreferredSize(new Dimension(220, 32));
        jenisCombo.setPreferredSize(new Dimension(200, 32));
        pilih.add(Theme.bold("Pelanggan"));
        pilih.add(pelangganCombo);
        pilih.add(Theme.bold("Kendaraan"));
        pilih.add(kendaraanCombo);
        pilih.add(Theme.bold("Jenis servis"));
        pilih.add(jenisCombo);
        isi.add(pilih, BorderLayout.NORTH);

        JPanel keluhan = new JPanel(new BorderLayout(8, 0));
        keluhan.setOpaque(false);
        keluhan.add(Theme.bold("Keluhan"), BorderLayout.WEST);
        JScrollPane scrollKeluhan = Theme.scroll(keluhanArea);
        scrollKeluhan.setPreferredSize(new Dimension(0, 55));
        keluhan.add(scrollKeluhan, BorderLayout.CENTER);

        JButton daftar = Theme.primaryButton("Daftar Servis");
        daftar.addActionListener(e -> daftarServis());
        keluhan.add(daftar, BorderLayout.EAST);
        isi.add(keluhan, BorderLayout.CENTER);

        card.add(header, BorderLayout.NORTH);
        card.add(isi, BorderLayout.CENTER);
        return card;
    }

    private JPanel buatPanelWaitingList() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 10));

        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setOpaque(false);
        JPanel judul = new JPanel(new BorderLayout(0, 3));
        judul.setOpaque(false);
        judul.add(Theme.bold("Waiting List Servis"), BorderLayout.NORTH);
        judul.add(Theme.muted("Atur jadwal dan status pengerjaan"), BorderLayout.CENTER);
        header.add(judul, BorderLayout.WEST);

        JButton refresh = Theme.secondaryButton("Muat Ulang");
        refresh.addActionListener(e -> refreshData());
        JPanel tombolHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        tombolHeader.setOpaque(false);
        tombolHeader.add(refresh);
        header.add(tombolHeader, BorderLayout.EAST);

        tabel.setModel(model);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabel.getColumnModel().getColumn(7).setCellRenderer(Theme.statusRenderer());
        Theme.styleTableColumns(tabel, 125, 40, 130, 170, 120, 180, 120, 110);
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                tampilkanPilihan();
            }
        });

        JPanel aksi = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        aksi.setOpaque(false);
        jadwalkanButton.addActionListener(e -> jadwalkan());
        ubahStatusButton.addActionListener(e -> ubahStatus());
        batalkanButton.addActionListener(e -> batalkan());
        statusCombo.setPreferredSize(new Dimension(160, 32));
        aksi.add(jadwalkanButton);
        aksi.add(statusCombo);
        aksi.add(ubahStatusButton);
        aksi.add(batalkanButton);

        card.add(header, BorderLayout.NORTH);
        card.add(Theme.scroll(tabel), BorderLayout.CENTER);
        card.add(aksi, BorderLayout.SOUTH);
        return card;
    }

    @Override
    public void refreshData() {
        try {
            muatPelangganCombo();
            muatTabel();
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal memuat waiting list: " + e.getMessage());
        }
    }

    private void muatPelangganCombo() {
        Long terpilih = pelangganCombo.getSelectedItem() == null
                ? null : ((Pelanggan) pelangganCombo.getSelectedItem()).getId();
        List<Pelanggan> semua = pelangganService.semua();
        pelangganCombo.removeAllItems();
        for (Pelanggan pelanggan : semua) {
            pelangganCombo.addItem(pelanggan);
        }
        if (terpilih != null) {
            for (int i = 0; i < pelangganCombo.getItemCount(); i++) {
                if (terpilih.equals(pelangganCombo.getItemAt(i).getId())) {
                    pelangganCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
        muatKendaraanCombo();
    }

    private void muatKendaraanCombo() {
        Long terpilih = kendaraanCombo.getSelectedItem() == null
                ? null : ((Kendaraan) kendaraanCombo.getSelectedItem()).getId();
        kendaraanCombo.removeAllItems();
        Pelanggan pelanggan = (Pelanggan) pelangganCombo.getSelectedItem();
        if (pelanggan == null) {
            return;
        }
        try {
            for (Kendaraan kendaraan : pelangganService.kendaraan(pelanggan.getId())) {
                kendaraanCombo.addItem(kendaraan);
            }
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal memuat kendaraan: " + e.getMessage());
            return;
        }
        if (terpilih != null) {
            for (int i = 0; i < kendaraanCombo.getItemCount(); i++) {
                if (terpilih.equals(kendaraanCombo.getItemAt(i).getId())) {
                    kendaraanCombo.setSelectedIndex(i);
                    return;
                }
            }
        }
    }

    private void muatTabel() {
        List<PendaftaranServis> semua = waitingListService.semua();
        data.clear();
        data.addAll(semua);
        model.setRowCount(0);
        for (PendaftaranServis servis : semua) {
            model.addRow(new Object[]{
                servis.getKodeAntrean(),
                servis.getNomorAntrean(),
                servis.getPelanggan().getNama(),
                servis.getKendaraan().getPlatNomor() + " - "
                        + servis.getKendaraan().getMerk(),
                servis.getJenisServis().getLabel(),
                servis.getKeluhan(),
                FormatUtil.tanggal(servis.getJadwalServis()),
                servis.getStatus().getLabel()
            });
        }

        int baris = -1;
        if (selectedId != null) {
            for (int i = 0; i < data.size(); i++) {
                if (selectedId.equals(data.get(i).getId())) {
                    baris = i;
                    break;
                }
            }
        }
        if (baris >= 0) {
            tabel.setRowSelectionInterval(baris, baris);
            tampilkanPilihan();
        } else {
            tabel.clearSelection();
            selectedId = null;
            perbaruiTombol();
        }
    }

    private void daftarServis() {
        Pelanggan pelanggan = (Pelanggan) pelangganCombo.getSelectedItem();
        Kendaraan kendaraan = (Kendaraan) kendaraanCombo.getSelectedItem();
        JenisServis jenis = (JenisServis) jenisCombo.getSelectedItem();
        try {
            PendaftaranServis servis = waitingListService.daftar(
                    pelanggan, kendaraan, jenis, keluhanArea.getText());
            Theme.showInfo(this, "Pendaftaran servis berhasil.\n"
                    + "Kode: " + servis.getKodeAntrean() + "\n"
                    + "Nomor antrean: " + servis.getNomorAntrean());
            keluhanArea.setText("");
            selectedId = servis.getId();
            refreshData();
        } catch (AplikasiException e) {
            Theme.showError(this, e.getMessage());
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal mendaftarkan servis: " + e.getMessage());
        }
    }

    private void tampilkanPilihan() {
        int baris = tabel.getSelectedRow();
        if (baris < 0 || baris >= data.size()) {
            selectedId = null;
            perbaruiTombol();
            return;
        }
        PendaftaranServis servis = data.get(baris);
        selectedId = servis.getId();
        statusCombo.removeAllItems();
        for (StatusServis status : StatusServis.values()) {
            if (servis.getStatus().bisaBerubahKe(status)) {
                statusCombo.addItem(status);
            }
        }
        perbaruiTombol();
    }

    private void perbaruiTombol() {
        PendaftaranServis servis = servisTerpilih();
        boolean adaPilihan = servis != null;
        boolean bisaJadwal = adaPilihan
                && (servis.getStatus() == StatusServis.MENUNGGU
                || servis.getStatus() == StatusServis.DIJADWALKAN);
        jadwalkanButton.setEnabled(bisaJadwal);
        ubahStatusButton.setEnabled(adaPilihan && statusCombo.getItemCount() > 0);
        batalkanButton.setEnabled(adaPilihan
                && servis.getStatus().bisaBerubahKe(StatusServis.DIBATALKAN));
    }

    private PendaftaranServis servisTerpilih() {
        if (selectedId == null) {
            return null;
        }
        for (PendaftaranServis servis : data) {
            if (selectedId.equals(servis.getId())) {
                return servis;
            }
        }
        return null;
    }

    private void jadwalkan() {
        PendaftaranServis servis = servisTerpilih();
        if (servis == null) {
            Theme.showError(this, "Pilih antrean servis terlebih dahulu.");
            return;
        }
        LocalDateTime awal = servis.getJadwalServis() != null
                ? servis.getJadwalServis()
                : LocalDateTime.now().plusDays(1).withHour(9).withMinute(0)
                        .withSecond(0).withNano(0);
        String teks = JOptionPane.showInputDialog(this,
                "Jadwal servis (format yyyy-MM-dd HH:mm):",
                FormatUtil.tanggalInput(awal));
        if (teks == null) {
            return;
        }
        try {
            LocalDateTime jadwal = FormatUtil.parseWaktu(teks);
            waitingListService.jadwalkan(servis.getId(), jadwal);
            Theme.showInfo(this, "Jadwal servis disimpan.");
            refreshData();
        } catch (IllegalArgumentException | AplikasiException e) {
            Theme.showError(this, e.getMessage());
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal menyimpan jadwal: " + e.getMessage());
        }
    }

    private void ubahStatus() {
        PendaftaranServis servis = servisTerpilih();
        if (servis == null) {
            Theme.showError(this, "Pilih antrean servis terlebih dahulu.");
            return;
        }
        StatusServis statusBaru = (StatusServis) statusCombo.getSelectedItem();
        if (statusBaru == null) {
            Theme.showError(this, "Tidak ada status berikutnya yang bisa dipilih.");
            return;
        }
        try {
            waitingListService.ubahStatus(servis.getId(), statusBaru);
            Theme.showInfo(this, "Status servis diubah menjadi " + statusBaru.getLabel() + ".");
            refreshData();
        } catch (AplikasiException e) {
            Theme.showError(this, e.getMessage());
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal mengubah status: " + e.getMessage());
        }
    }

    private void batalkan() {
        PendaftaranServis servis = servisTerpilih();
        if (servis == null) {
            Theme.showError(this, "Pilih antrean servis terlebih dahulu.");
            return;
        }
        if (!Theme.confirm(this, "Batalkan pendaftaran servis "
                + servis.getKodeAntrean() + "?")) {
            return;
        }
        try {
            waitingListService.batalkan(servis.getId());
            Theme.showInfo(this, "Pendaftaran servis dibatalkan.");
            refreshData();
        } catch (AplikasiException e) {
            Theme.showError(this, e.getMessage());
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal membatalkan servis: " + e.getMessage());
        }
    }
}
