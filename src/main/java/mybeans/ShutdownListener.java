package mybeans;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import businessLogic.BLFacade;

/**
 * Listener honek Tomcat zerbitzaria gelditzen denean exekutatzen da.
 * Bere helburua baliabideak (memoria, konexioak) garbitzea da, errore gorriak ekiditeko.
 */
@WebListener // Hau beharrezkoa da Tomcat-ek klase hau automatikoki kargatzeko
public class ShutdownListener implements ServletContextListener {

    // Zerbitzaria piztean exekutatzen da
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("--> [INFO] Zerbitzaria martxan (ShutdownListener kargatuta).");
    }

    // Zerbitzaria itzaltzean (STOP) exekutatzen da. HEMEN DAGO GAKOA.
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("--> [INFO] Zerbitzaria gelditzen... Baliabideak askatzen.");

        // -----------------------------------------------------------
        // 1. URRATSA: HIBERNATE ETA DATU-BASEA IXTEA
        // Errorea ekiditen du: "Cleaning up connection pool" eta konexioak irekita geratzea.
        // -----------------------------------------------------------
        try {
            // Zure logika (BLFacade) lortzen dugu Singleton bidez
            BLFacade facade = FacadeBean.getBusinessLogic();
            
            if (facade != null) {
                // Hau exekutatzean, Hibernate-k "EntityManagerFactory" ixten du.
                // Zure kontsolan ikusi duzun "DA: EntityManagerFactory cerrado" lerroa honek sortzen du.
                // Oso garrantzitsua da fitxategia (ridesDB.mv.db) ez blokeatzeko.
                facade.close(); 
                System.out.println("--> [OK] Hibernate eta DB konexioak itxita.");
            }
        } catch (Exception e) {
            System.err.println("--> [ERROREA] Arazoa Hibernate ixtean: " + e.getMessage());
        }

        // -----------------------------------------------------------
        // 2. URRATSA: JDBC DRIVER-AK MEMORIATIK KENDU
        // Errorea ekiditen du: "WebappClassLoaderBase clearReferencesJdbc"
        // -----------------------------------------------------------
        
        // Tomcat kexatu egiten da H2 Driverra memorian geratzen delako aplikazioa itzali ondoren.
        // Loop (buka) honek eskuz bilatzen ditu gidariak eta "derrigortu" egiten ditu irtetera.
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                // Bakarrik gure aplikazioari dagozkion driverrak kentzen ditugu
                if (driver.getClass().getClassLoader() == getClass().getClassLoader()) {
                    DriverManager.deregisterDriver(driver);
                    System.out.println("--> [OK] JDBC Driver desregistratuta: " + driver);
                }
            } catch (SQLException e) {
                System.err.println("--> [ERROREA] Ezin izan da driverra kendu: " + e.getMessage());
            }
        }

        // -----------------------------------------------------------
        // OHARRA H2-KO "THREADS" ERROREARI BURUZ (clearReferencesThreads):
        // 
        // Zure kontsolan "H2 File Lock Watchdog" edo "H2 TCP Server" erroreak ikusten badituzu oraindik,
        // EZ LARRITU. Hori gertatzen da `AUTO_SERVER=TRUE` erabiltzen dugulako garapenean.
        // H2-k hari (thread) batzuk oso sakon sortzen ditu eta zaila da Tomcat-entzat hiltzea.
        // Datuak ondo gordetzen dira, beraz, garapen fasean (klasean) errore hori onartu daiteke.
        // -----------------------------------------------------------
        
        System.out.println("--> [INFO] Garbiketa prozesua amaituta.");
    }
}