package mybeans;

import java.io.Serializable;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import businessLogic.BLFacade;
import domain.Booking; // Asegúrate de tener esta clase (o usa Ride si es directo)
import domain.User; // O User, según tu modelo

@Named
@ViewScoped
public class QueryReservationsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private SessionManagerBean sessionManager;

    // Lista de reservas del usuario
    private List<Booking> myBookings;

    @PostConstruct
    public void init() {
        // 1. Obtener el usuario logueado
    	User traveler = sessionManager.getCurrentUser();
        
        if (traveler != null) {
            BLFacade facade = FacadeBean.getBusinessLogic();
            // 2. Cargar reservas de este viajero
            // NOTA: Debes tener este método en tu BLFacade
            try {

                myBookings = facade.getBookingsByTraveler(traveler.getName());
                // ------------------------------------------------------
                
            } catch (Exception e) {
                e.printStackTrace();
                addMessage(FacesMessage.SEVERITY_ERROR, "Errorea", "Ezin dira datuak kargatu.");
            }
        }
    }

 // En mybeans/QueryReservationsBean.java

    // Método para cancelar reserva
    public void cancelBooking(Booking booking) {
        BLFacade facade = FacadeBean.getBusinessLogic();
        try {
            // 1. LLAMADA A LA LÓGICA (Base de datos)
            // Pasamos el ID para que la BD sepa cuál borrar y qué Ride actualizar
            facade.cancelBooking(booking.getId()); 
            
            // 2. ACTUALIZAR LA LISTA VISUAL (Para que desaparezca de la tabla al momento)
            myBookings.remove(booking); 
            
            addMessage(FacesMessage.SEVERITY_INFO, "Ezeztatua", "Erreserba ezeztatu da eta plazak askatu dira.");
            
        } catch (Exception e) {
            e.printStackTrace();
            addMessage(FacesMessage.SEVERITY_ERROR, "Errorea", "Ezin izan da ezeztatu.");
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // Getters y Setters
    public List<Booking> getMyBookings() { return myBookings; }
    public void setMyBookings(List<Booking> myBookings) { this.myBookings = myBookings; }
}