package kaboygarage;

import org.springframework.context.ApplicationContext;

/**
 * Static accessor untuk Spring ApplicationContext.
 * Memungkinkan GUI classes akses ke beans tanpa passing context everywhere.
 */
public final class ApplicationContextHolder {
    
    private static ApplicationContext applicationContext;
    
    private ApplicationContextHolder() {
    }
    
    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }
    
    public static ApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext belum diset");
        }
        return applicationContext;
    }
    
    public static <T> T getBean(Class<T> beanClass) {
        return getApplicationContext().getBean(beanClass);
    }
}
