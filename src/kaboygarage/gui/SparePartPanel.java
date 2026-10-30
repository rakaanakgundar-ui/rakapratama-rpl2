package kaboygarage.gui;

import kaboygarage.exception.AplikasiException;
import kaboygarage.model.KategoriSparePart;
import kaboygarage.model.SparePart;
import kaboygarage.service.SparePartService;
import kaboygarage.util.Validasi;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel CRUD katalog spare part.
 */
public class SparePartPanel extends JPanel implements Refreshable {

    private final SparePartService service;

    private final JTextField kodeField = Theme.textField();
    private final JTextField namaField = Theme.textField();
    private final JTextField merkField = Theme.textField();
    private final JComboBox<KategoriSparePart> kategoriCombo =
            new JComboBox<>(KategoriSparePart.values());
    private final JTextField hargaField = Theme.textField();
    private final JTextField stokField = Theme.textField();
    private final JTextField stokMinimalField = Theme.textField();
    private final JTextArea deskripsiArea = Theme.textArea(3);

    private final JTextField cariField = Theme.textField();
    private final JTable tabel = Theme.table();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Kode", "Nama", "Merk", "Kategori", "Harga", "Stok", "Min", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final List<SparePart> dataTabel = new ArrayList<>();

    private Long selectedId;

    public SparePartPanel(SparePartService service) {
        this.service = service;
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buatForm(), buatTabel());
        split.setBorder(null);
        split.setOpaque(false);
        split.setResizeWeight(0);
        split.setDividerLocation(350);
        add(split, BorderLayout.CENTER);

        resetForm();
    }

    private JPanel buatForm() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 12));
        card.setPreferredSize(new Dimension(350, 0));

        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setOpaque(false);
        header.add(Theme.bold("Tambah / Ubah Spare Part"), BorderLayout.NORTH);
        header.add(Theme.muted("Isi data lalu klik Simpan"), BorderLayout.CENTER);

        JPanel form = Theme.formPanel();
        GridBagConstraints gbc = new GridBagConstraints();
        int baris = 0;
        Theme.addFormRow(form, gbc, baris++, "Kode", kodeField);
        Theme.addFormRow(form, gbc, baris++, "Nama", namaField);
        Theme.addFormRow(form, gbc, baris++, "Merk", merkField);
        Theme.addFormRow(form, gbc, baris++, "Kategori", kategoriCombo);
        Theme.addFormRow(form, gbc, baris++, "Harga", hargaField);
        Theme.addFormRow(form, gbc, baris++, "Stok", stokField);
        Theme.addFormRow(form, gbc, baris++, "Stok minimal", stokMinimalField);

        JScrollPane deskripsiScroll = Theme.scroll(deskripsiArea);
        deskripsiScroll.setPreferredSize(new Dimension(0, 78));
        Theme.addFormRow(form, gbc, baris++, "Deskripsi", deskripsiScroll);
        Theme.fillForm(form);

        JButton simpan = Theme.primaryButton("Simpan");
        simpan.addActionListener(e -> simpan());
        JButton hapus = Theme.dangerButton("Hapus");
        hapus.addActionListener(e -> hapus());
        JButton reset = Theme.secondaryButton("Baru");
        reset.addActionListener(e -> resetForm());

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tombol.setOpaque(false);
        tombol.add(simpan);
        tombol.add(hapus);
        tombol.add(reset);

        card.add(header, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(tombol, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buatTabel() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 12));

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        JPanel judul = new JPanel(new BorderLayout(0, 3));
        judul.setOpaque(false);
        judul.add(Theme.bold("Daftar Spare Part"), BorderLayout.NORTH);
        judul.add(Theme.muted("Klik baris untuk mengubah data"), BorderLayout.CENTER);
        header.add(judul, BorderLayout.WEST);

        JPanel cari = new JPanel(new BorderLayout(8, 0));
        cari.setOpaque(false);
        JLabel label = Theme.muted("Cari");
        cari.add(label, BorderLayout.WEST);
        cariField.setPreferredSize(new Dimension(230, 32));
        cari.add(cariField, BorderLayout.CENTER);
        header.add(cari, BorderLayout.EAST);

        tabel.setModel(model);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabel.getColumnModel().getColumn(7).setCellRenderer(Theme.statusRenderer());
        Theme.styleTableColumns(tabel, 70, 170, 90, 100, 100, 55, 55, 85);
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                tampilkanPilihan();
            }
        });

        cariField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                muatTabel();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                muatTabel();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                muatTabel();
            }
        });

        card.add(header, BorderLayout.NORTH);
        card.add(Theme.scroll(tabel), BorderLayout.CENTER);
        return card;
    }

    @Override
    public void refreshData() {
        muatTabel();
        if (selectedId != null) {
            pilihBaris(selectedId);
        }
    }

    private void muatTabel() {
        try {
            List<SparePart> hasil = service.cari(cariField.getText());
            dataTabel.clear();
            dataTabel.addAll(hasil);
            model.setRowCount(0);
            for (SparePart sparePart : hasil) {
                model.addRow(new Object[]{
                    sparePart.getKode(),
                    sparePart.getNama(),
                    sparePart.getMerk() == null ? "-" : sparePart.getMerk(),
                    sparePart.getKategori().getLabel(),
                    FormatUtil.rupiah(sparePart.getHarga()),
                    sparePart.getStok(),
                    sparePart.getStokMinimal(),
                    sparePart.getStatusStok()
                });
            }
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal memuat spare part: " + e.getMessage());
        }
    }

    private void tampilkanPilihan() {
        int baris = tabel.getSelectedRow();
        if (baris < 0 || baris >= dataTabel.size()) {
            return;
        }
        SparePart sparePart = dataTabel.get(baris);
        selectedId = sparePart.getId();
        kodeField.setText(sparePart.getKode());
        namaField.setText(sparePart.getNama());
        merkField.setText(sparePart.getMerk());
        kategoriCombo.setSelectedItem(sparePart.getKategori());
        hargaField.setText(sparePart.getHarga().toPlainString());
        stokField.setText(String.valueOf(sparePart.getStok()));
        stokMinimalField.setText(String.valueOf(sparePart.getStokMinimal()));
        deskripsiArea.setText(sparePart.getDeskripsi());
    }

    private void pilihBaris(Long id) {
        for (int i = 0; i < dataTabel.size(); i++) {
            if (id.equals(dataTabel.get(i).getId())) {
                tabel.setRowSelectionInterval(i, i);
                tabel.scrollRectToVisible(tabel.getCellRect(i, 0, true));
                return;
            }
        }
    }

    private void simpan() {
        try {
            SparePart sparePart = new SparePart(
                    kodeField.getText(),
                    namaField.getText(),
                    merkField.getText(),
                    (KategoriSparePart) kategoriCombo.getSelectedItem(),
                    Validasi.parseHarga(hargaField.getText()),
                    Validasi.parseAngka(stokField.getText(), "Stok"));
            sparePart.setId(selectedId);
            sparePart.setStokMinimal(
                    Validasi.parseAngka(stokMinimalField.getText(), "Stok minimal"));
            sparePart.setDeskripsi(deskripsiArea.getText().trim());

            service.simpan(sparePart);
            selectedId = sparePart.getId();
            Theme.showInfo(this, "Spare part berhasil disimpan.");
            refreshData();
        } catch (AplikasiException e) {
            Theme.showError(this, e.getMessage());
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal menyimpan spare part: " + e.getMessage());
        }
    }

    private void hapus() {
        if (selectedId == null) {
            Theme.showError(this, "Pilih spare part yang ingin dihapus.");
            return;
        }
        if (!Theme.confirm(this, "Hapus spare part yang dipilih?")) {
            return;
        }
        try {
            service.hapus(selectedId);
            resetForm();
            refreshData();
            Theme.showInfo(this, "Spare part berhasil dihapus.");
        } catch (AplikasiException e) {
            Theme.showError(this, e.getMessage());
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal menghapus spare part: " + e.getMessage());
        }
    }

    private void resetForm() {
        selectedId = null;
        kodeField.setText("");
        namaField.setText("");
        merkField.setText("");
        kategoriCombo.setSelectedIndex(0);
        hargaField.setText("");
        stokField.setText("0");
        stokMinimalField.setText("3");
        deskripsiArea.setText("");
        tabel.clearSelection();
        try {
            kodeField.setText(service.kodeBerikutnya());
        } catch (RuntimeException ignored) {
            // kode bisa diisi manual bila database belum siap
        }
        kodeField.requestFocusInWindow();
    }
}
