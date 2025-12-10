package mybeans;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject; // Necesario para inyectar SessionManager

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.primefaces.event.SelectEvent;

import businessLogic.BLFacade;
import exceptions.RideAlreadyExistException;
import exceptions.RideMustBeLaterThanTodayException;

@Named
@RequestScoped
public class CreateRidesBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // 1. INYECTAR EL GESTOR DE SESIÓN
    @Inject
    private SessionManagerBean sessionManager;

    private String departCity;
    private String arrivalCity;
    private String email; // Se llenará automáticamente
    private Integer seats;
    private Float price;
    private Date data;
    private BLFacade facadeBL;
      
    private List<String> departCities;
    private List<String> arrivalCities;

    private boolean rideCreated = false; // controla el mensaje de éxito
    private String message; // mensaje de error o éxito

    // 2. MÉTODO DE INICIALIZACIÓN: Cargar Email y Ciudades
    @PostConstruct
    public void init() {
        facadeBL = FacadeBean.getBusinessLogic();
        
        // Inicializar Ciudades
        this.departCities = facadeBL.getDepartCities();
        
        this.data = new Date();
        
        // Inicializar Email desde la Sesión
        String loggedEmail = sessionManager.getLoggedInUserEmail();
        
        if (loggedEmail != null) {
            this.email = loggedEmail;
            System.out.println("Email de Driver cargado automáticamente: " + this.email);
        } else {
            // Caso de seguridad: Si no hay sesión, puedes redirigir o poner un error.
            this.email = "ERROR: Sesión requerida";
            System.err.println("Intento de crear ride sin sesión.");
            // Si quieres redirigir al login:
            // try { FacesContext.getCurrentInstance().getExternalContext().redirect("Login.xhtml"); } catch (Exception e) {}
        }
    }


    public void showAlert() {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Alerta", "Operación completada"));
    }
    
    // Getters y setters
    public String getDepartCity() { return departCity; }
    public void setDepartCity(String departCity) { this.departCity = departCity; }
    
    public Date getData() { return data; }
    public void setData(Date data) { this.data = data; }
        
    public String getArrivalCity() { return arrivalCity; }
    public void setArrivalCity(String arrivalCity) { this.arrivalCity = arrivalCity; }
    
    // Dejamos el Getter/Setter del email, aunque el Setter no se use desde el formulario
    public String getEmail() { return email; } 
    public void setEmail(String email) { this.email = email; }

    public Integer getSeats() { return seats; }
    public void setSeats(Integer seats) { this.seats = seats; }

    public Float getPrice() { return price; }
    public void setPrice(Float price) { this.price = price; }

    public List<String> getDepartCities() { return departCities; }
    public void setDepartCities(List<String> departCities) { this.departCities = departCities; }

    public List<String> getArrivalCities() { return arrivalCities; }
    public void setArrivalCities(List<String> arrivalCities) { this.arrivalCities = arrivalCities; }

    public boolean isRideCreated() { return rideCreated; }
    public void setRideCreated(boolean rideCreated) { this.rideCreated = rideCreated; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    
    public void onDateSelect(SelectEvent event) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Data aukeratua: " + event.getObject()));
    }

    // Acción del botón "Create Ride"
    public void createRide() {
        System.out.println("=== INICIANDO createRide ===");
        rideCreated = false;
        message = null; // Limpiar mensaje de error

        // Validación de campos
        if (data == null) { // Asignamos fecha de hoy si es nula
            data = new Date();
        }
        
        // Validación de los campos y asignación de mensaje en pantalla
        if (departCity == null || departCity.isEmpty()) {
            message = "Please select a departure city.";
            return;
        }
        if (arrivalCity == null || arrivalCity.isEmpty()) {
            message = "Please select an arrival city.";
            return;
        }
        if (departCity.equals(arrivalCity)) {
            message = "Departure and arrival cities cannot be the same.";
            return;
        }
        if (seats == null || seats < 1) {
            message = "Number of seats must be at least 1.";
            return;
        }
        if (price == null || price < 0) {
            message = "Price must be non-negative.";
            return;
        } 
        
        // 3. SE ELIMINA LA VALIDACIÓN DEL EMAIL (Ya se asume que existe y es válido)
        /*
        if (email == null|| email.isEmpty()) {
             message = "Enter email";
             return;
        }
        */

        System.out.println("Todas las validaciones pasadas");

        // Si todo está bien
        facadeBL=FacadeBean.getBusinessLogic();
        try {
            facadeBL.createRide(departCity, arrivalCity, data, seats, price, email);
            
            rideCreated = true;
            message = "✓ Ride created successfully!";
            
        } catch (RideMustBeLaterThanTodayException e1) {
            message = "Gaurko data baino beranduago jarri";
        } catch (RideAlreadyExistException e1) {
            message = "Ride hori jada existitzen da";
        } catch (Exception e) {
            System.err.println("Error creating ride: " + e.getMessage());
            e.printStackTrace();
            message = "Error desconocido al crear la ride.";
        }
    }
}