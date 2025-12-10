package mybeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.omnifaces.cdi.ViewScoped;

import businessLogic.BLFacade;
import domain.Ride;
import jakarta.inject.Named;

@Named("qrBean")
@ViewScoped
public class QueryRidesBean implements Serializable {
    
    private static final long serialVersionUID = 1L; // Buena práctica añadir esto

    private BLFacade facadeBL;
    
    private List<String> cities = new ArrayList<>();
    private List<String> cities2 = new ArrayList<>();
    
    private String selectedCity;
    private String selectedCity2;
    private Date selectedDate;
    
    // Inicializamos la lista vacía para evitar errores de "null" en la tabla
    private List<Ride> availableRides = new ArrayList<>();

    public QueryRidesBean() {
        facadeBL = FacadeBean.getBusinessLogic(); 
        cities = facadeBL.getDepartCities();
    }
 // Añade esta propiedad
    private String query;

    // Getter y Setter
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    // MÉTODO DE BÚSQUEDA DINÁMICA
    public void onQueryType() {
        System.out.println("Buscando: " + query);
        
        if (query != null && !query.isEmpty()) {
            // Llamamos al método nuevo que busca por texto
            availableRides = facadeBL.getRidesByQuery(query);
        } else {
            // Si borran el texto, limpiamos la lista (o podrías mostrar todos)
            availableRides = new ArrayList<>();
        }
    }

    // --- CORRECCIÓN AQUÍ ---
    // Cambiamos String por void. No necesitamos devolver nada porque es una petición AJAX.
    public void findRides() {
        System.out.println(">>> findRides() ejecutado");
        System.out.println("From: " + selectedCity + " | To: " + selectedCity2 + " | Date: " + selectedDate);

        // Validación básica
        if (selectedCity != null && selectedCity2 != null && selectedDate != null) {
            availableRides = facadeBL.getRides(selectedCity, selectedCity2, selectedDate);
        } else {
            availableRides = new ArrayList<>(); // Lista vacía si faltan datos
        }

        System.out.println("Rides encontrados = " + availableRides.size());
        
        // NO DEVOLVER availableRides.toString();
    }

    public void updateArrivalCities() {
        if (selectedCity != null && !selectedCity.isEmpty()) {
            System.out.println("Selected depart city: " + selectedCity);
            cities2 = facadeBL.getDestinationCities(selectedCity);
        } else {
            cities2 = new ArrayList<>();
        }
    }

    // --- Getters y Setters ---

    public Date getSelectedDate() { return selectedDate; }
    public void setSelectedDate(Date selectedDate) { this.selectedDate = selectedDate; }

    public List<Ride> getAvailableRides() { return availableRides; }
    // No hace falta setter para availableRides normalmente

    public List<String> getCities() { return cities; }

    public String getSelectedCity() { return selectedCity; }
    public void setSelectedCity(String selectedCity) { this.selectedCity = selectedCity; }
    
    public List<String> getCities2() { return cities2; }

    public String getSelectedCity2() { return selectedCity2; }
    public void setSelectedCity2(String selectedCity2) { this.selectedCity2 = selectedCity2; }
}