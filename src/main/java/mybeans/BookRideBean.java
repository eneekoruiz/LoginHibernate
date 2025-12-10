package mybeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped; 
import jakarta.inject.Inject; // Beharrezkoa SessionManager injektatzeko
import jakarta.inject.Named;

import businessLogic.BLFacade;
import domain.User; // Ziurtatu Driver inportatuta dagoela
import domain.Ride;

@Named
@ViewScoped
public class BookRideBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // --- 1. INYECCIÓN DE DEPENDENCIAS ---
    @Inject
    private SessionManagerBean sessionManager;

    // --- 2. ALDAGIAK (VARIABLES) ---
    private String departCity;
    private String arrivalCity;
    private Date date;
    
    // Listak
    private List<String> departCities;
    private List<String> arrivalCities;
    private List<Ride> rides;

    // Aukeraketa eta Erreserba
    private Ride selectedRide;
    private String travelerName; // Saioatik kargatuko da
    private int seatsToBook = 1; // Defektuz 1

    // --- 3. HASIERAKETA (@PostConstruct) ---
    @PostConstruct
    public void init() {
        BLFacade facade = FacadeBean.getBusinessLogic();
        
        // A. Hiriak kargatu
        departCities = facade.getDepartCities();
        
        // B. Data gaurko egunean jarri (User request)
        this.date = new Date(); 

        // C. Erabiltzailearen izena saioatik lortu (User request)
        User loggedDriver = sessionManager.getCurrentUser();
        if (loggedDriver != null) {
            // Asumitzen dugu Driver klaseak getName() edo toString() duela
            // Hobeto getName() bada, bestela egokitu lerro hau:
            this.travelerName = loggedDriver.getName(); 
            System.out.println("BookRideBean: Usuario cargado desde sesión -> " + this.travelerName);
        } else {
            this.travelerName = "Ezezaguna"; // Edo utzi hutsik
        }
    }

    // --- 4. NEGOTZIO LOGIKA ---

    /**
     * Jatorria aldatzean, helmuga posibleak eguneratzen ditu (AJAX)
     */
    public void updateArrivalCities() {
        if (departCity != null && !departCity.isEmpty()) {
            BLFacade facade = FacadeBean.getBusinessLogic();
            arrivalCities = facade.getDestinationCities(departCity);
        } else {
            arrivalCities = new ArrayList<>();
        }
    }

    /**
     * Bidaiak bilatu emandako irizpideekin
     */
    public void findRides() {
        BLFacade facade = FacadeBean.getBusinessLogic();
        this.rides = null; // Garbitu aurreko emaitzak

        if (departCity != null && arrivalCity != null && date != null) {
            rides = facade.getRides(departCity, arrivalCity, date);
            
            if (rides == null || rides.isEmpty()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Oharra", "Ez da bidaiarik aurkitu irizpide horiekin.");
            }
        } else {
            addMessage(FacesMessage.SEVERITY_ERROR, "Errorea", "Mesedez, aukeratu jatorria, helmuga eta data.");
        }
    }

    /**
     * Aukeratutako bidaia erreserbatu
     */
    public void bookRide() {
        BLFacade facade = FacadeBean.getBusinessLogic();
        
        if (selectedRide == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Errorea", "Ez duzu bidaiarik aukeratu.");
            return;
        }

        try {
            // Hemen BLFacadeko metodoa deitzen dugu.
            // Ziurtatu metodo hau existitzen dela zure BLFacade-n!
            // boolean bookRide(Ride ride, String travelerName, int seats);
            boolean success = facade.bookRide(selectedRide, travelerName, seatsToBook);

            if (success) {
                addMessage(FacesMessage.SEVERITY_INFO, "Arrakasta", "Erreserba ondo egin da! Ondo pasa bidaian.");
                
                // Eguneratu bista lokalean, berriro bilatu gabe (UI update)
                selectedRide.setnPlaces(selectedRide.getnPlaces() - seatsToBook);
                
                // Resetear el spinner a 1 para la próxima vez
                this.seatsToBook = 1;
                
            } else {
                addMessage(FacesMessage.SEVERITY_ERROR, "Errorea", "Ezin izan da erreserba egin (baliteke lekurik ez egotea).");
            }
        } catch (Exception e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Salbuespena", "Errore tekniko bat gertatu da.");
        }
    }

    // Helper mezua gehitzeko
    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // --- 5. GETTERS & SETTERS ---

    public String getDepartCity() { return departCity; }
    public void setDepartCity(String departCity) { this.departCity = departCity; }

    public String getArrivalCity() { return arrivalCity; }
    public void setArrivalCity(String arrivalCity) { this.arrivalCity = arrivalCity; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public List<String> getDepartCities() { return departCities; }
    public void setDepartCities(List<String> departCities) { this.departCities = departCities; }

    public List<String> getArrivalCities() { return arrivalCities; }
    public void setArrivalCities(List<String> arrivalCities) { this.arrivalCities = arrivalCities; }

    public List<Ride> getRides() { return rides; }
    public void setRides(List<Ride> rides) { this.rides = rides; }

    public Ride getSelectedRide() { return selectedRide; }
    public void setSelectedRide(Ride selectedRide) { this.selectedRide = selectedRide; }

    public String getTravelerName() { return travelerName; }
    public void setTravelerName(String travelerName) { this.travelerName = travelerName; }

    public int getSeatsToBook() { return seatsToBook; }
    public void setSeatsToBook(int seatsToBook) { this.seatsToBook = seatsToBook; }
}