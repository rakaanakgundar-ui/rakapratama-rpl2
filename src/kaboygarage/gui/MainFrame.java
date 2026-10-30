package kaboygarage.gui;

import kaboygarage.model.Admin;
import kaboygarage.service.DashboardService;
import kaboygarage.service.PelangganService;
import kaboygarage.service.PesananService;
import kaboygarage.service.SparePartService;
import kaboygarage.service.WaitingListService;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Jendela utama admin: sidebar navigasi + konten panel.
 */
public class MainFrame extends JFrame {

    private final Admin admin;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();
    private final Map<String, Refreshable> panels = new LinkedHashMap<>();
    private final Map<String, String[]> infoHalaman = new LinkedHashMap<>();
    private final JLabel pageTitle = new JLabel();
    private final JLabel pageSubtitle = new JLabel();

    public MainFrame(Admin admin) {
        this.admin = admin;
        setTitle("Kaboy Garage - Admin Panel");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.add(buatSidebar(), BorderLayout.WEST);
        root.add(buatMain(), BorderLayout.CENTER);
        setContentPane(root);

        tampilkan("dashboard");
    }

    private JPanel buatSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Theme.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(235, 0));

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBorder(new EmptyBorder(26, 20, 24, 20));

        JLabel kaboy = new JLabel("KABOY");
        kaboy.setFont(new Font("Segoe UI", Font.BOLD, 25));
        kaboy.setForeground(Color.WHITE);
        kaboy.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel garage = new JLabel("GARAGE");
        garage.setFont(new Font("Segoe UI", Font.BOLD, 25));
        garage.setForeground(Theme.ACCENT);
        garage.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Admin Panel");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(new Color(150, 158, 170));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        brand.add(kaboy);
        brand.add(garage);
        brand.add(Box.createVerticalStrut(8));
        brand.add(sub);
        sidebar.add(brand, BorderLayout.NORTH);

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(6, 0, 6, 0));
        tambahNav(nav, "dashboard", "Dashboard");
        tambahNav(nav, "sparepart", "Spare Part");
        tambahNav(nav, "pesanan", "Pesanan");
        tambahNav(nav, "waitinglist", "Waiting List");
        tambahNav(nav, "pelanggan", "Pelanggan");
        sidebar.add(nav, BorderLayout.CENTER);

        JPanel bawah = new JPanel();
        bawah.setOpaque(false);
        bawah.setLayout(new BoxLayout(bawah, BoxLayout.Y_AXIS));
        bawah.setBorder(new EmptyBorder(14, 20, 20, 20));

        JLabel nama = new JLabel(admin.getNama());
        nama.setFont(Theme.FONT_BOLD);
        nama.setForeground(Color.WHITE);
        nama.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel peran = new JLabel("Administrator");
        peran.setFont(Theme.FONT_SMALL);
        peran.setForeground(new Color(150, 158, 170));
        peran.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton keluar = Theme.navButton("Keluar");
        keluar.setForeground(new Color(255, 170, 170));
        keluar.setAlignmentX(Component.LEFT_ALIGNMENT);
        keluar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        keluar.addActionListener(e -> keluar());

        bawah.add(nama);
        bawah.add(peran);
        bawah.add(Box.createVerticalStrut(12));
        bawah.add(keluar);
        sidebar.add(bawah, BorderLayout.SOUTH);

        return sidebar;
    }

    private void tambahNav(JPanel nav, String key, String label) {
        JButton button = Theme.navButton(label);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        button.addActionListener(e -> tampilkan(key));
        nav.add(button);
        nav.add(Box.createVerticalStrut(2));
        navButtons.put(key, button);
    }

    private JPanel buatMain() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Theme.BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.CARD);
        header.setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));
        header.setPreferredSize(new Dimension(0, 78));

        JPanel judul = new JPanel();
        judul.setOpaque(false);
        judul.setLayout(new BoxLayout(judul, BoxLayout.Y_AXIS));
        judul.setBorder(new EmptyBorder(14, 22, 12, 12));
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        pageTitle.setForeground(Theme.TEXT);
        pageSubtitle.setFont(Theme.FONT_SMALL);
        pageSubtitle.setForeground(Theme.MUTED);
        judul.add(pageTitle);
        judul.add(Box.createVerticalStrut(3));
        judul.add(pageSubtitle);
        header.add(judul, BorderLayout.WEST);

        JPanel kanan = new JPanel();
        kanan.setOpaque(false);
        kanan.setLayout(new BoxLayout(kanan, BoxLayout.X_AXIS));
        kanan.setBorder(new EmptyBorder(0, 0, 0, 22));
        JLabel badge = Theme.badge("ADMIN", Theme.ACCENT_DARK);
        badge.setFont(Theme.FONT_SMALL);
        kanan.add(badge);
        header.add(kanan, BorderLayout.EAST);

        main.add(header, BorderLayout.NORTH);

        content.setBackground(Theme.BACKGROUND);
        content.setBorder(new EmptyBorder(18, 20, 20, 20));

        DashboardPanel dashboard = new DashboardPanel(new DashboardService());
        SparePartPanel sparePart = new SparePartPanel(new SparePartService());
        PelangganPanel pelanggan = new PelangganPanel(new PelangganService());
        PesananPanel pesanan = new PesananPanel(
                new PesananService(), new PelangganService(), new SparePartService());
        WaitingListPanel waitingList = new WaitingListPanel(
                new WaitingListService(), new PelangganService());

        daftarPanel("dashboard", dashboard);
        daftarPanel("sparepart", sparePart);
        daftarPanel("pesanan", pesanan);
        daftarPanel("waitinglist", waitingList);
        daftarPanel("pelanggan", pelanggan);

        infoHalaman.put("dashboard",
                new String[]{"Dashboard", "Ringkasan operasional bengkel hari ini"});
        infoHalaman.put("sparepart",
                new String[]{"Spare Part", "Kelola katalog, harga, dan stok spare part"});
        infoHalaman.put("pesanan",
                new String[]{"Pesanan", "Buat pesanan baru dan pantau status pesanan"});
        infoHalaman.put("waitinglist",
                new String[]{"Waiting List Servis", "Daftar antrean dan jadwal servis kendaraan"});
        infoHalaman.put("pelanggan",
                new String[]{"Pelanggan", "Kelola data pelanggan dan kendaraannya"});

        main.add(content, BorderLayout.CENTER);
        return main;
    }

    private void daftarPanel(String key, Refreshable panel) {
        panels.put(key, panel);
        content.add((Component) panel, key);
    }

    private void tampilkan(String key) {
        if (!panels.containsKey(key)) {
            return;
        }
        cardLayout.show(content, key);
        String[] info = infoHalaman.get(key);
        if (info != null) {
            pageTitle.setText(info[0]);
            pageSubtitle.setText(info[1]);
        }
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            boolean aktif = entry.getKey().equals(key);
            entry.getValue().setBackground(aktif ? Theme.SIDEBAR_ACTIVE : Theme.SIDEBAR);
        }
        try {
            panels.get(key).refreshData();
        } catch (RuntimeException e) {
            Theme.showError(this, "Gagal memuat data: " + e.getMessage());
        }
    }

    private void keluar() {
        if (Theme.confirm(this, "Keluar dari aplikasi?")) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
