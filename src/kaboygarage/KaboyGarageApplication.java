package kaboygarage;

import kaboygarage.gui.LoginFrame;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import javax.swing.SwingUtilities;

/**
 * Spring Boot Application starter untuk Kaboy Garage Desktop Swing App.
 */
@SpringBootApplication
public class KaboyGarageApplication {

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(KaboyGarageApplication.class, args);
        
        // Set static ApplicationContext untuk akses di GUI
        ApplicationContextHolder.setApplicationContext(applicationContext);
        
        // Launch Swing GUI on EDT
        SwingUtilities.invokeLater(() -> {
            try {
                new LoginFrame(applicationContext).setVisible(true);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        });
    }
}
