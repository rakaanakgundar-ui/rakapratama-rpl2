package kaboygarage.gui;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Entry point aplikasi desktop Kaboy Garage.
 */
public final class GuiMain {

    private GuiMain() {
    }

    public static void main(String[] args) {
        Theme.install();
        SwingUtilities.invokeLater(() -> {
            try {
                new LoginFrame().setVisible(true);
            } catch (Throwable ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Aplikasi gagal dijalankan:\n" + ex.getMessage(),
                        "Kaboy Garage", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
