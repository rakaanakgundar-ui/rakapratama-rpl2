package kaboygarage.gui;

import kaboygarage.dto.ItemPesanan;
import kaboygarage.exception.AplikasiException;
import kaboygarage.model.DetailPesanan;
import kaboygarage.model.Pelanggan;
import kaboygarage.model.Pesanan;
import kaboygarage.model.SparePart;
import kaboygarage.model.StatusPesanan;
import kaboygarage.service.PelangganService;
import kaboygarage.service.PesananService;
import kaboygarage.service.SparePartService;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel pembuatan pesanan dan pemantauan status pesanan.
 */
public class PesananPanel extends JPanel implements Refreshable {

    private final PesananService pesananService;
    private final PelangganService pelangganService;
    private final SparePartService sparePartService;

    private final JComboBox<Pelanggan> pelangganCombo = new JComboBox<>();
    private final JComboBox<SparePart> sparePartCombo = new JComboBox<>();
    private final JSpinner jumlahSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JTextField catatanField = Theme.textField();

    private final JTable tabelKeranjang = Theme.table();
    private final DefaultTableModel modelKeranjang = new DefaultTableModel(
            new Object[]{"Kode", "Nama", "Harga", "Qty", "Subtotal"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final List<ItemPesanan> keranjang = new ArrayList<>();
    private final JLabel totalLabel = new JLabel("Total: Rp 0");

    private final JTable tabelPesanan = Theme.table();
    private final DefaultTableModel modelPesanan = new DefaultTableModel(
            new Object[]{"Kode", "Pelanggan", "Tanggal", "Status", "Item", "Total"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final List<Pesanan> dataPesanan = new ArrayList<>();
    private Long selectedPesananId;

    private final JComboBox<StatusPesanan> statusCombo = new JComboBox<>();
    private final JButton ubahStatusButton = Theme.primaryButton("Ubah Status");
    private final JButton batalkanButton = Theme.dangerButton("Batalkan");
    private final JButton detailButton = Theme.secondaryButton("Detail");

    public PesananPanel(PesananService pesananService,
                        PelangganService pelangganService,
                        SparePartService sparePartService) {
        this.pesananService = pesananService;
        this.pelangganService = pelangganService;
        this.sparePartService = sparePartService;
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));

        add(buatPanelBuat(), BorderLayout.NORTH);
        add(buatPanelDaftar(), BorderLayout.CENTER);

        perbaruiKeranjang();
        perbaruiTombolStatus();
    }

    private JPanel buatPanelBuat() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 10));

        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setOpaque(false);
        header.add(Theme.bold("Buat Pesanan Baru"), BorderLayout.NORTH);
        header.add(Theme.muted("Pilih pelanggan, tambahkan spare part, lalu simpan"), BorderLayout.CENTER);

        JPanel isi = new JPanel(new BorderLayout(0, 10));
        isi.setOpaque(false);

        JPanel pilih = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pilih.setOpaque(false);
        pelangganCombo.setPreferredSize(new Dimension(210, 32));
        sparePartCombo.setPreferredSize(new Dimension(250, 32));
        jumlahSpinner.setPreferredSize(new Dimension(70, 32));
        JButton tambah = Theme.primaryButton("Tambah Item");
        tambah.addActionListener(e -> tambahItem());
        pilih.add(Theme.bold("Pelanggan"));
        pilih.add(pelangganCombo);
        pilih.add(Theme.bold("Spare part"));
        pilih.add(sparePartCombo);
        pilih.add(Theme.bold("Qty"));
        pilih.add(jumlahSpinner);
        pilih.add(tambah);
        isi.add(pilih, BorderLayout.NORTH);

        tabelKeranjang.setModel(modelKeranjang);
        Theme.styleTableColumns(tabelKeranjang, 80, 230, 110, 60, 130);
        JScrollPane scrollKeranjang = Theme.scroll(tabelKeranjang);
        scrollKeranjang.setPreferredSize(new Dimension(0, 145));
        isi.add(scrollKeranjang, BorderLayout.CENTER);

        JPanel bawah = new JPanel(new BorderLayout(12, 0));
        bawah.setOpaque(false);

        JPanel kiri = new JPanel(new BorderLayout(8, 0));
        kiri.setOpaque(false);
        kiri.add(Theme.bold("Catatan"), BorderLayout.WEST);
        catatanField.setPreferredSize(new Dimension(330, 32));
        kiri.add(catatanField, BorderLayout.CENTER);
        bawah.add(kiri, BorderLayout.WEST);

        JPanel kanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        kanan.setOpaque(false);
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalLabel.setForeground(Theme.ACCENT_DARK);
        JButton hapusItem = Theme.secondaryButton("Hapus Item");
        hapusItem.addActionListener(e -> hapusItem());
        JButton kosongkan = Theme.secondaryButton("Kosongkan");
        kosongkan.addActionListener(e -> {
            keranjang.clear();
            perbaruiKeranjang();
        });
        JButton simpan = Theme.primaryButton("Simpan Pesanan");
        simpan.addActionListener(e -> simpanPesanan());
        kanan.add(totalLabel);
        kanan.add(hapusItem);
        kanan.add(kosongkan);
        kanan.add(simpan);
        bawah.add(kanan, BorderLayout.EAST);

        isi.add(bawah, BorderLayout.SOUTH);
        card.add(header, BorderLayout.NORTH);
        card.add(isi, BorderLayout.CENTER);
        return card;
    }

    private JPanel buatPanelDaftar() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 10));

        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setOpaque(false);
        JPanel judul = new JPanel(new BorderLayout(0, 3));
        judul.setOpaque(false);
        judul.add(Theme.bold("Daftar Pesanan"), BorderLayout.NORTH);
        judul.add(Theme.muted("Klik pesanan untuk melihat detail atau mengubah status"), BorderLayout.CENTER);
        header.add(judul, BorderLayout.WEST);

        JButton refresh = Theme.secondaryButton("Muat Ulang");
        refresh.addActionListener(e -> refreshData());
        JPanel tombolHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        tombolHeader.setOpaque(false);
        tombolHeader.add(refresh);
        header.add(tombolHeader, BorderLayout.EAST);

        tabelPesanan.setModel(modelPesanan);
        tabelPesanan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelPesanan.getColumnModel().getColumn(3).setCellRenderer(Theme.statusRenderer());
        Theme.styleTableColumns(tabelPesanan, 135, 160, 120, 100, 55, 110);
        tabelPesanan.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                tampilkanPilihanPesanan();
            }
        });

        JPanel aksi = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        aksi.setOpaque(false);
        detailButton.addActionListener(e -> tampilkanDetail());
        ubahStatusButton.addActionListener(e -> ubahStatus());
        batalkanButton.addActionListener(e -> batalkanPesanan());
        statusCombo.setPreferredSize(new Dimension(150, 32));
        aksi.add(detailButton);
        aksi.add(statusCombo);
        aksi.add(ubahStatusButton);
        aksi.add(batalkanButton);

        card.add(header, BorderLayout.NORTH);
        card.add(Theme.scroll(tabelPesanan), BorderLayout.CENTER);
        card.add(aksi, BorderLayout.SOUTH);
        return card;
    }

    @Override
    public void refreshData() {
        try {
            muatPelangganCombo();
            muatSparePartCombo();
            muatPesanan();
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal memuat data pesanan: " + e.getMessage());
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
                    return;
                }
            }
        }
    }

    private void muatSparePartCombo() {
        Long terpilih = sparePartCombo.getSelectedItem() == null
                ? null : ((SparePart) sparePartCombo.getSelectedItem()).getId();
        List<SparePart> semua = sparePartService.semua();
        sparePartCombo.removeAllItems();
        for (SparePart sparePart : semua) {
            sparePartCombo.addItem(sparePart);
        }
        if (terpilih != null) {
            for (int i = 0; i < sparePartCombo.getItemCount(); i++) {
                if (terpilih.equals(sparePartCombo.getItemAt(i).getId())) {
                    sparePartCombo.setSelectedIndex(i);
                    return;
                }
            }
        }
    }

    private void muatPesanan() {
        List<Pesanan> semua = pesananService.semua();
        dataPesanan.clear();
        dataPesanan.addAll(semua);
        modelPesanan.setRowCount(0);
        for (Pesanan pesanan : semua) {
            modelPesanan.addRow(new Object[]{
                pesanan.getKode(),
                pesanan.getPelanggan().getNama(),
                FormatUtil.tanggal(pesanan.getTanggalDibuat()),
                pesanan.getStatus().getLabel(),
                pesanan.hitungTotalItem(),
                FormatUtil.rupiah(pesanan.hitungTotal())
            });
        }

        int baris = -1;
        if (selectedPesananId != null) {
            for (int i = 0; i < dataPesanan.size(); i++) {
                if (selectedPesananId.equals(dataPesanan.get(i).getId())) {
                    baris = i;
                    break;
                }
            }
        }
        if (baris >= 0) {
            tabelPesanan.setRowSelectionInterval(baris, baris);
            tampilkanPilihanPesanan();
        } else {
            tabelPesanan.clearSelection();
            selectedPesananId = null;
            perbaruiTombolStatus();
        }
    }

    private void tambahItem() {
        SparePart sparePart = (SparePart) sparePartCombo.getSelectedItem();
        if (sparePart == null) {
            Theme.showError(this, "Pilih spare part terlebih dahulu.");
            return;
        }
        int jumlah = (Integer) jumlahSpinner.getValue();
        int totalJumlah = jumlah;
        for (ItemPesanan item : keranjang) {
            if (item.getSparePart().getId().equals(sparePart.getId())) {
                totalJumlah += item.getJumlah();
                break;
            }
        }
        if (!sparePart.isTersedia(totalJumlah)) {
            Theme.showError(this, "Stok " + sparePart.getNama() + " tidak mencukupi. "
                    + "Tersedia " + sparePart.getStok() + ".");
            return;
        }

        boolean ditemukan = false;
        for (ItemPesanan item : keranjang) {
            if (item.getSparePart().getId().equals(sparePart.getId())) {
                item.tambahJumlah(jumlah);
                ditemukan = true;
                break;
            }
        }
        if (!ditemukan) {
            keranjang.add(new ItemPesanan(sparePart, jumlah));
        }
        jumlahSpinner.setValue(1);
        perbaruiKeranjang();
    }

    private void hapusItem() {
        int baris = tabelKeranjang.getSelectedRow();
        if (baris < 0 || baris >= keranjang.size()) {
            Theme.showError(this, "Pilih item yang ingin dihapus dari keranjang.");
            return;
        }
        keranjang.remove(baris);
        perbaruiKeranjang();
    }

    private void perbaruiKeranjang() {
        modelKeranjang.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        for (ItemPesanan item : keranjang) {
            total = total.add(item.getSubtotal());
            modelKeranjang.addRow(new Object[]{
                item.getSparePart().getKode(),
                item.getSparePart().getNama(),
                FormatUtil.rupiah(item.getSparePart().getHarga()),
                item.getJumlah(),
                FormatUtil.rupiah(item.getSubtotal())
            });
        }
        totalLabel.setText("Total: " + FormatUtil.rupiah(total));
    }

    private void simpanPesanan() {
        Pelanggan pelanggan = (Pelanggan) pelangganCombo.getSelectedItem();
        if (pelanggan == null) {
            Theme.showError(this, "Pilih pelanggan terlebih dahulu.");
            return;
        }
        if (keranjang.isEmpty()) {
            Theme.showError(this, "Tambahkan minimal satu spare part ke pesanan.");
            return;
        }
        try {
            Pesanan pesanan = pesananService.buatPesanan(
                    pelanggan, new ArrayList<>(keranjang), catatanField.getText());
            Theme.showInfo(this, "Pesanan " + pesanan.getKode() + " berhasil dibuat.\n"
                    + "Total: " + FormatUtil.rupiah(pesanan.hitungTotal()));
            keranjang.clear();
            catatanField.setText("");
            perbaruiKeranjang();
            selectedPesananId = pesanan.getId();
            refreshData();
        } catch (AplikasiException e) {
            Theme.showError(this, e.getMessage());
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal membuat pesanan: " + e.getMessage());
        }
    }

    private void tampilkanPilihanPesanan() {
        int baris = tabelPesanan.getSelectedRow();
        if (baris < 0 || baris >= dataPesanan.size()) {
            selectedPesananId = null;
            perbaruiTombolStatus();
            return;
        }
        Pesanan pesanan = dataPesanan.get(baris);
        selectedPesananId = pesanan.getId();
        statusCombo.removeAllItems();
        for (StatusPesanan status : StatusPesanan.values()) {
            if (pesanan.getStatus().bisaBerubahKe(status)) {
                statusCombo.addItem(status);
            }
        }
        perbaruiTombolStatus();
    }

    private void perbaruiTombolStatus() {
        Pesanan pesanan = pesananTerpilih();
        boolean adaPilihan = statusCombo.getItemCount() > 0 && pesanan != null;
        ubahStatusButton.setEnabled(adaPilihan);
        batalkanButton.setEnabled(pesanan != null
                && pesanan.getStatus().bisaBerubahKe(StatusPesanan.DIBATALKAN));
        detailButton.setEnabled(pesanan != null);
    }

    private void ubahStatus() {
        Pesanan pesanan = pesananTerpilih();
        if (pesanan == null) {
            Theme.showError(this, "Pilih pesanan terlebih dahulu.");
            return;
        }
        StatusPesanan statusBaru = (StatusPesanan) statusCombo.getSelectedItem();
        if (statusBaru == null) {
            Theme.showError(this, "Tidak ada status berikutnya yang bisa dipilih.");
            return;
        }
        try {
            pesananService.ubahStatus(pesanan.getId(), statusBaru);
            Theme.showInfo(this, "Status pesanan diubah menjadi " + statusBaru.getLabel() + ".");
            refreshData();
        } catch (AplikasiException e) {
            Theme.showError(this, e.getMessage());
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal mengubah status: " + e.getMessage());
        }
    }

    private void batalkanPesanan() {
        Pesanan pesanan = pesananTerpilih();
        if (pesanan == null) {
            Theme.showError(this, "Pilih pesanan terlebih dahulu.");
            return;
        }
        if (!Theme.confirm(this, "Batalkan pesanan " + pesanan.getKode()
                + "? Stok akan dikembalikan.")) {
            return;
        }
        try {
            pesananService.batalkan(pesanan.getId());
            Theme.showInfo(this, "Pesanan berhasil dibatalkan dan stok dikembalikan.");
            refreshData();
        } catch (AplikasiException e) {
            Theme.showError(this, e.getMessage());
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal membatalkan pesanan: " + e.getMessage());
        }
    }

    private Pesanan pesananTerpilih() {
        if (selectedPesananId == null) {
            return null;
        }
        for (Pesanan pesanan : dataPesanan) {
            if (selectedPesananId.equals(pesanan.getId())) {
                return pesanan;
            }
        }
        return null;
    }

    private void tampilkanDetail() {
        Pesanan pesanan = pesananTerpilih();
        if (pesanan == null) {
            return;
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Detail Pesanan " + pesanan.getKode(), JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(650, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setBackground(Theme.CARD);
        header.setBorder(new EmptyBorder(18, 20, 12, 20));
        JLabel kode = new JLabel(pesanan.getKode());
        kode.setFont(new Font("Segoe UI", Font.BOLD, 20));
        kode.setForeground(Theme.TEXT);
        JLabel info = Theme.muted(pesanan.getPelanggan().getNama()
                + "  |  " + FormatUtil.tanggal(pesanan.getTanggalDibuat()));
        JLabel status = Theme.badge(pesanan.getStatus().getLabel(),
                Theme.statusColor(pesanan.getStatus().getLabel()));
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusPanel.setOpaque(false);
        statusPanel.add(status);
        header.add(kode, BorderLayout.NORTH);
        header.add(info, BorderLayout.CENTER);
        header.add(statusPanel, BorderLayout.SOUTH);
        dialog.add(header, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Kode", "Nama", "Harga", "Qty", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (DetailPesanan detail : pesanan.getDetail()) {
            model.addRow(new Object[]{
                detail.getSparePart().getKode(),
                detail.getSparePart().getNama(),
                FormatUtil.rupiah(detail.getHargaSatuan()),
                detail.getJumlah(),
                FormatUtil.rupiah(detail.getSubtotal())
            });
        }
        JTable tabel = Theme.table();
        tabel.setModel(model);
        Theme.styleTableColumns(tabel, 80, 250, 110, 60, 120);
        JScrollPane scroll = Theme.scroll(tabel);
        scroll.setBorder(new EmptyBorder(0, 20, 0, 20));
        dialog.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Theme.CARD);
        footer.setBorder(new EmptyBorder(12, 20, 16, 20));
        JLabel total = new JLabel("Total: " + FormatUtil.rupiah(pesanan.hitungTotal()));
        total.setFont(new Font("Segoe UI", Font.BOLD, 16));
        total.setForeground(Theme.ACCENT_DARK);
        JButton tutup = Theme.primaryButton("Tutup");
        tutup.addActionListener(e -> dialog.dispose());
        footer.add(total, BorderLayout.WEST);
        footer.add(tutup, BorderLayout.EAST);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}
