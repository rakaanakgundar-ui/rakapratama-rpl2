package kaboygarage.tools;

import kaboygarage.gui.DashboardPanel;
import kaboygarage.gui.LoginFrame;
import kaboygarage.gui.MainFrame;
import kaboygarage.gui.PelangganPanel;
import kaboygarage.gui.PesananPanel;
import kaboygarage.gui.SparePartPanel;
import kaboygarage.gui.Theme;
import kaboygarage.gui.WaitingListPanel;
import kaboygarage.model.Admin;
import kaboygarage.service.AuthService;
import kaboygarage.service.DashboardService;
import kaboygarage.service.PelangganService;
import kaboygarage.service.PesananService;
import kaboygarage.service.SparePartService;
import kaboygarage.service.WaitingListService;

import javax.swing.SwingUtilities;

/**
 * Menguji inisialisasi seluruh jendela GUI dan pembacaan data dari database.
 */
public final class GuiSmokeTest {

    private GuiSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        Theme.install();
        Admin admin = new AuthService().login("admin", "admin123");
        if (admin == null) {
            throw new IllegalStateException("Login admin gagal.");
        }

        SwingUtilities.invokeAndWait(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.dispose();

            MainFrame mainFrame = new MainFrame(admin);
            mainFrame.dispose();

            new DashboardPanel(new DashboardService()).refreshData();
            new SparePartPanel(new SparePartService()).refreshData();
            new PelangganPanel(new PelangganService()).refreshData();
            new PesananPanel(new PesananService(), new PelangganService(),
                    new SparePartService()).refreshData();
            new WaitingListPanel(new WaitingListService(),
                    new PelangganService()).refreshData();
        });

        System.out.println("GUI SMOKE TEST LULUS.");
    }
}
