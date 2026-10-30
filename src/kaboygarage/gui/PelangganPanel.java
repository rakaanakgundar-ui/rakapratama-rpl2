package kaboygarage.gui;

import kaboygarage.exception.AplikasiException;
import kaboygarage.model.Kendaraan;
import kaboygarage.model.Pelanggan;
import kaboygarage.service.PelangganService;
import kaboygarage.util.Validasi;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel data pelanggan beserta kendaraannya.
 */
public class PelangganPanel extends JPanel implements Refreshable {

    private final PelangganService service;

    private final JTextField namaField = Theme.textField();
    private final JTextField noHpField = Theme.textField();
    private final JTextField emailField = Theme.textField();
    private final JTextField alamatField = Theme.textField();

    private final JTable tabelPelanggan = Theme.table();
    private final DefaultTableModel modelPelanggan = new DefaultTableModel(
            new Object[]{"Nama", "No HP", "Email", "Alamat"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final List<Pelanggan> dataPelanggan = new ArrayList<>();
    private Long selectedPelangganId;

    private final JLabel ownerLabel = Theme.muted("Pilih pelanggan terlebih dahulu");
    private final JTextField merkField = Theme.textField();
    private final JTextField modelField = Theme.textField();
    private final JTextField platField = Theme.textField();
    private final JTextField tahunField = Theme.textField();

    private final JTable tabelKendaraan = Theme.table();
    private final DefaultTableModel modelKendaraan = new DefaultTableModel(
            new Object[]{"Plat Nomor", "Merk", "Model", "Tahun"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final List<Kendaraan> dataKendaraan = new ArrayList<>();
    private Long selectedKendaraanId;

    public PelangganPanel(PelangganService service) {
        this.service = service;
        setOpaque(false);
        setLayout(new BorderLayout());

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                buatPanelPelanggan(), buatPanelKendaraan());
        split.setBorder(null);
        split.setOpaque(false);
        split.setResizeWeight(0.5);
        split.setDividerLocation(330);
        add(split, BorderLayout.CENTER);

        resetPelangganForm();
        resetKendaraanForm();
    }

    private JPanel buatPanelPelanggan() {
        JPanel panel = new JPanel(new BorderLayout(14, 0));
        panel.setOpaque(false);
        panel.add(buatFormPelanggan(), BorderLayout.WEST);
        panel.add(buatTabelPelanggan(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buatFormPelanggan() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 10));
        card.setPreferredSize(new Dimension(330, 0));

        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setOpaque(false);
        header.add(Theme.bold("Data Pelanggan"), BorderLayout.NORTH);
        header.add(Theme.muted("Pelanggan bisa langsung memesan di bengkel"), BorderLayout.CENTER);

        JPanel form = Theme.formPanel();
        GridBagConstraints gbc = new GridBagConstraints();
        int baris = 0;
        Theme.addFormRow(form, gbc, baris++, "Nama", namaField);
        Theme.addFormRow(form, gbc, baris++, "No HP", noHpField);
        Theme.addFormRow(form, gbc, baris++, "Email", emailField);
        Theme.addFormRow(form, gbc, baris++, "Alamat", alamatField);
        Theme.fillForm(form);

        JButton simpan = Theme.primaryButton("Simpan");
        simpan.addActionListener(e -> simpanPelanggan());
        JButton hapus = Theme.dangerButton("Hapus");
        hapus.addActionListener(e -> hapusPelanggan());
        JButton baru = Theme.secondaryButton("Baru");
        baru.addActionListener(e -> resetPelangganForm());

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tombol.setOpaque(false);
        tombol.add(simpan);
        tombol.add(hapus);
        tombol.add(baru);

        card.add(header, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(tombol, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buatTabelPelanggan() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 10));

        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setOpaque(false);
        header.add(Theme.bold("Daftar Pelanggan"), BorderLayout.NORTH);
        header.add(Theme.muted("Klik pelanggan untuk melihat kendaraannya"), BorderLayout.CENTER);

        tabelPelanggan.setModel(modelPelanggan);
        tabelPelanggan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Theme.styleTableColumns(tabelPelanggan, 170, 120, 170, 230);
        tabelPelanggan.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                tampilkanPilihanPelanggan();
            }
        });

        card.add(header, BorderLayout.NORTH);
        card.add(Theme.scroll(tabelPelanggan), BorderLayout.CENTER);
        return card;
    }

    private JPanel buatPanelKendaraan() {
        JPanel panel = new JPanel(new BorderLayout(14, 0));
        panel.setOpaque(false);
        panel.add(buatFormKendaraan(), BorderLayout.WEST);
        panel.add(buatTabelKendaraan(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buatFormKendaraan() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 10));
        card.setPreferredSize(new Dimension(330, 0));

        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setOpaque(false);
        header.add(Theme.bold("Kendaraan Pelanggan"), BorderLayout.NORTH);
        header.add(ownerLabel, BorderLayout.CENTER);

        JPanel form = Theme.formPanel();
        GridBagConstraints gbc = new GridBagConstraints();
        int baris = 0;
        Theme.addFormRow(form, gbc, baris++, "Merk", merkField);
        Theme.addFormRow(form, gbc, baris++, "Model", modelField);
        Theme.addFormRow(form, gbc, baris++, "Plat nomor", platField);
        Theme.addFormRow(form, gbc, baris++, "Tahun", tahunField);
        Theme.fillForm(form);

        JButton simpan = Theme.primaryButton("Simpan");
        simpan.addActionListener(e -> simpanKendaraan());
        JButton hapus = Theme.dangerButton("Hapus");
        hapus.addActionListener(e -> hapusKendaraan());
        JButton baru = Theme.secondaryButton("Baru");
        baru.addActionListener(e -> resetKendaraanForm());

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tombol.setOpaque(false);
        tombol.add(simpan);
        tombol.add(hapus);
        tombol.add(baru);

        card.add(header, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(tombol, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buatTabelKendaraan() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 10));

        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setOpaque(false);
        header.add(Theme.bold("Daftar Kendaraan"), BorderLayout.NORTH);
        header.add(Theme.muted("Kendaraan dipakai saat pendaftaran servis"), BorderLayout.CENTER);

        tabelKendaraan.setModel(modelKendaraan);
        tabelKendaraan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Theme.styleTableColumns(tabelKendaraan, 130, 150, 180, 80);
        tabelKendaraan.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                tampilkanPilihanKendaraan();
            }
        });

        card.add(header, BorderLayout.NORTH);
        card.add(Theme.scroll(tabelKendaraan), BorderLayout.CENTER);
        return card;
    }

    @Override
    public void refreshData() {
        muatPelanggan(selectedPelangganId);
    }

    private void muatPelanggan(Long pilihId) {
        try {
            List<Pelanggan> hasil = service.semua();
            dataPelanggan.clear();
            dataPelanggan.addAll(hasil);
            modelPelanggan.setRowCount(0);
            for (Pelanggan pelanggan : hasil) {
                modelPelanggan.addRow(new Object[]{
                    pelanggan.getNama(),
                    pelanggan.getNoHp(),
                    pelanggan.getEmail() == null ? "-" : pelanggan.getEmail(),
                    pelanggan.getAlamat() == null ? "-" : pelanggan.getAlamat()
                });
            }

            int baris = cariBarisPelanggan(pilihId);
            if (baris < 0 && !hasil.isEmpty()) {
                baris = 0;
            }
            if (baris >= 0) {
                tabelPelanggan.setRowSelectionInterval(baris, baris);
                tampilkanPilihanPelanggan();
            } else {
                resetPelangganForm();
            }
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal memuat pelanggan: " + e.getMessage());
        }
    }

    private int cariBarisPelanggan(Long id) {
        if (id == null) {
            return -1;
        }
        for (int i = 0; i < dataPelanggan.size(); i++) {
            if (id.equals(dataPelanggan.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }

    private void tampilkanPilihanPelanggan() {
        int baris = tabelPelanggan.getSelectedRow();
        if (baris < 0 || baris >= dataPelanggan.size()) {
            return;
        }
        Pelanggan pelanggan = dataPelanggan.get(baris);
        selectedPelangganId = pelanggan.getId();
        namaField.setText(pelanggan.getNama());
        noHpField.setText(pelanggan.getNoHp());
        emailField.setText(pelanggan.getEmail());
        alamatField.setText(pelanggan.getAlamat());
        ownerLabel.setText("Kendaraan milik: " + pelanggan.getNama());
        muatKendaraan(true);
    }

    private void simpanPelanggan() {
        try {
            Pelanggan pelanggan = new Pelanggan(
                    namaField.getText(), noHpField.getText(),
                    emailField.getText().trim(), alamatField.getText().trim());
            pelanggan.setId(selectedPelangganId);
            service.simpan(pelanggan);
            selectedPelangganId = pelanggan.getId();
            Theme.showInfo(this, "Data pelanggan berhasil disimpan.");
            muatPelanggan(selectedPelangganId);
        } catch (AplikasiException e) {
            Theme.showError(this, e.getMessage());
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal menyimpan pelanggan: " + e.getMessage());
        }
    }

    private void hapusPelanggan() {
        if (selectedPelangganId == null) {
            Theme.showError(this, "Pilih pelanggan yang ingin dihapus.");
            return;
        }
        if (!Theme.confirm(this, "Hapus pelanggan yang dipilih?")) {
            return;
        }
        try {
            service.hapus(selectedPelangganId);
            selectedPelangganId = null;
            resetPelangganForm();
            muatPelanggan(null);
            Theme.showInfo(this, "Pelanggan berhasil dihapus.");
        } catch (AplikasiException e) {
            Theme.showError(this, e.getMessage());
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal menghapus pelanggan: " + e.getMessage());
        }
    }

    private void muatKendaraan(boolean resetForm) {
        dataKendaraan.clear();
        modelKendaraan.setRowCount(0);
        if (selectedPelangganId == null) {
            if (resetForm) {
                resetKendaraanForm();
            }
            return;
        }
        try {
            List<Kendaraan> hasil = service.kendaraan(selectedPelangganId);
            dataKendaraan.addAll(hasil);
            for (Kendaraan kendaraan : hasil) {
                modelKendaraan.addRow(new Object[]{
                    kendaraan.getPlatNomor(),
                    kendaraan.getMerk(),
                    kendaraan.getModel(),
                    kendaraan.getTahun()
                });
            }
            if (resetForm) {
                resetKendaraanForm();
            } else if (selectedKendaraanId != null) {
                pilihKendaraan(selectedKendaraanId);
            }
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal memuat kendaraan: " + e.getMessage());
        }
    }

    private void tampilkanPilihanKendaraan() {
        int baris = tabelKendaraan.getSelectedRow();
        if (baris < 0 || baris >= dataKendaraan.size()) {
            return;
        }
        Kendaraan kendaraan = dataKendaraan.get(baris);
        selectedKendaraanId = kendaraan.getId();
        merkField.setText(kendaraan.getMerk());
        modelField.setText(kendaraan.getModel());
        platField.setText(kendaraan.getPlatNomor());
        tahunField.setText(String.valueOf(kendaraan.getTahun()));
    }

    private void pilihKendaraan(Long id) {
        for (int i = 0; i < dataKendaraan.size(); i++) {
            if (id.equals(dataKendaraan.get(i).getId())) {
                tabelKendaraan.setRowSelectionInterval(i, i);
                tampilkanPilihanKendaraan();
                return;
            }
        }
    }

    private void simpanKendaraan() {
        if (selectedPelangganId == null) {
            Theme.showError(this, "Pilih pelanggan pemilik kendaraan terlebih dahulu.");
            return;
        }
        Pelanggan pelanggan = null;
        for (Pelanggan item : dataPelanggan) {
            if (selectedPelangganId.equals(item.getId())) {
                pelanggan = item;
                break;
            }
        }
        if (pelanggan == null) {
            Theme.showError(this, "Data pelanggan tidak ditemukan.");
            return;
        }
        try {
            Kendaraan kendaraan = new Kendaraan(
                    pelanggan,
                    merkField.getText(),
                    modelField.getText(),
                    platField.getText(),
                    Validasi.parseTahun(tahunField.getText()));
            kendaraan.setId(selectedKendaraanId);
            service.simpanKendaraan(kendaraan);
            selectedKendaraanId = kendaraan.getId();
            Theme.showInfo(this, "Data kendaraan berhasil disimpan.");
            muatKendaraan(false);
        } catch (AplikasiException e) {
            Theme.showError(this, e.getMessage());
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal menyimpan kendaraan: " + e.getMessage());
        }
    }

    private void hapusKendaraan() {
        if (selectedKendaraanId == null) {
            Theme.showError(this, "Pilih kendaraan yang ingin dihapus.");
            return;
        }
        if (!Theme.confirm(this, "Hapus kendaraan yang dipilih?")) {
            return;
        }
        try {
            service.hapusKendaraan(selectedKendaraanId);
            selectedKendaraanId = null;
            resetKendaraanForm();
            muatKendaraan(false);
            Theme.showInfo(this, "Kendaraan berhasil dihapus.");
        } catch (AplikasiException e) {
            Theme.showError(this, e.getMessage());
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal menghapus kendaraan: " + e.getMessage());
        }
    }

    private void resetPelangganForm() {
        selectedPelangganId = null;
        namaField.setText("");
        noHpField.setText("");
        emailField.setText("");
        alamatField.setText("");
        tabelPelanggan.clearSelection();
        dataKendaraan.clear();
        modelKendaraan.setRowCount(0);
        ownerLabel.setText("Pilih pelanggan terlebih dahulu");
        resetKendaraanForm();
        namaField.requestFocusInWindow();
    }

    private void resetKendaraanForm() {
        selectedKendaraanId = null;
        merkField.setText("");
        modelField.setText("");
        platField.setText("");
        tahunField.setText(String.valueOf(Year.now().getValue()));
        tabelKendaraan.clearSelection();
    }
}
