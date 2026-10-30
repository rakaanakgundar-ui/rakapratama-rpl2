package kaboygarage.gui;

import kaboygarage.dto.Statistik;
import kaboygarage.model.Pesanan;
import kaboygarage.model.SparePart;
import kaboygarage.service.DashboardService;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

/**
 * Panel dashboard: ringkasan angka, stok menipis, dan pesanan terbaru.
 */
public class DashboardPanel extends JPanel implements Refreshable {

    private final DashboardService dashboardService;

    private final JLabel sparePartValue = nilaiLabel(Theme.ACCENT_DARK);
    private final JLabel pesananValue = nilaiLabel(Theme.ACCENT);
    private final JLabel antreanValue = nilaiLabel(Theme.WARNING);
    private final JLabel pelangganValue = nilaiLabel(Theme.SIDEBAR);

    private final DefaultTableModel modelStok = modelBaru(
            "Kode", "Nama", "Stok", "Minimal", "Status");
    private final DefaultTableModel modelPesanan = modelBaru(
            "Kode", "Pelanggan", "Tanggal", "Status", "Total");

    public DashboardPanel(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));

        JPanel kartu = new JPanel(new GridLayout(1, 4, 14, 0));
        kartu.setOpaque(false);
        kartu.add(kartu("Spare Part", sparePartValue, "item di katalog", Theme.ACCENT_DARK));
        kartu.add(kartu("Pesanan Aktif", pesananValue, "belum selesai", Theme.ACCENT));
        kartu.add(kartu("Antrean Servis", antreanValue, "menunggu / dikerjakan", Theme.WARNING));
        kartu.add(kartu("Pelanggan", pelangganValue, "terdaftar", Theme.SIDEBAR));
        add(kartu, BorderLayout.NORTH);

        JPanel tengah = new JPanel(new GridLayout(1, 2, 16, 0));
        tengah.setOpaque(false);

        JTable tabelStok = Theme.table();
        tabelStok.setModel(modelStok);
        tabelStok.getColumnModel().getColumn(4).setCellRenderer(Theme.statusRenderer());
        Theme.styleTableColumns(tabelStok, 80, 190, 60, 70, 90);

        JTable tabelPesanan = Theme.table();
        tabelPesanan.setModel(modelPesanan);
        tabelPesanan.getColumnModel().getColumn(3).setCellRenderer(Theme.statusRenderer());
        Theme.styleTableColumns(tabelPesanan, 130, 150, 110, 90, 100);

        tengah.add(kartuTabel("Stok Menipis",
                "Spare part yang perlu segera ditambah", tabelStok));
        tengah.add(kartuTabel("Pesanan Terbaru",
                "5 pesanan terakhir yang masuk", tabelPesanan));
        add(tengah, BorderLayout.CENTER);
    }

    private static JLabel nilaiLabel(Color color) {
        JLabel label = new JLabel("0");
        label.setFont(Theme.BIG_NUMBER);
        label.setForeground(color);
        return label;
    }

    private JPanel kartu(String judul, JLabel nilai, String keterangan, Color aksen) {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.MatteBorder(0, 4, 0, 0, aksen),
                new EmptyBorder(14, 16, 14, 16)));

        JLabel label = Theme.muted(judul);
        JLabel sub = Theme.muted(keterangan);
        sub.setFont(Theme.FONT_SMALL);

        card.add(label, BorderLayout.NORTH);
        card.add(nilai, BorderLayout.CENTER);
        card.add(sub, BorderLayout.SOUTH);
        return card;
    }

    private JPanel kartuTabel(String judul, String keterangan, JTable tabel) {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, 12));

        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setOpaque(false);
        header.add(Theme.bold(judul), BorderLayout.NORTH);
        JLabel sub = Theme.muted(keterangan);
        sub.setFont(Theme.FONT_SMALL);
        header.add(sub, BorderLayout.CENTER);

        card.add(header, BorderLayout.NORTH);
        JScrollPane scroll = Theme.scroll(tabel);
        scroll.setPreferredSize(new Dimension(0, 260));
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private static DefaultTableModel modelBaru(String... kolom) {
        return new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    @Override
    public void refreshData() {
        Statistik statistik = dashboardService.statistik();
        sparePartValue.setText(String.valueOf(statistik.getTotalSparePart()));
        pesananValue.setText(String.valueOf(statistik.getPesananAktif()));
        antreanValue.setText(String.valueOf(statistik.getAntreanAktif()));
        pelangganValue.setText(String.valueOf(statistik.getTotalPelanggan()));

        modelStok.setRowCount(0);
        List<SparePart> menipis = dashboardService.stokMenipis();
        for (SparePart sparePart : menipis) {
            modelStok.addRow(new Object[]{
                sparePart.getKode(),
                sparePart.getNama(),
                sparePart.getStok(),
                sparePart.getStokMinimal(),
                sparePart.getStatusStok()
            });
        }

        modelPesanan.setRowCount(0);
        List<Pesanan> pesanan = dashboardService.pesananTerbaru(5);
        for (Pesanan item : pesanan) {
            modelPesanan.addRow(new Object[]{
                item.getKode(),
                item.getPelanggan().getNama(),
                FormatUtil.tanggal(item.getTanggalDibuat()),
                item.getStatus().getLabel(),
                FormatUtil.rupiah(item.hitungTotal())
            });
        }
    }
}
