package domain;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String email;
    private String name;
    private String password;

    // AQUI ESTÁ LA CLAVE: El tipo de usuario
    @Enumerated(EnumType.STRING)
    private UserType type;

    // Lista de viajes creados (Rol Driver)
    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ride> createdRides = new ArrayList<>();

    // Lista de reservas (Rol Traveler)
    @OneToMany(mappedBy = "traveler", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();

    public User() {}

    // Constructor para el Registro
    public User(String email, String name, String password, UserType type) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.type = type;
    }

    // --- MÉTODOS DE NEGOCIO ---

    // Crear un viaje (Solo si es DRIVER o BOTH)
    public void addCreatedRide(Ride r) {
        if (this.type == UserType.TRAVELER) {
            throw new IllegalStateException("Este usuario no tiene permiso para crear viajes");
        }
        this.createdRides.add(r);
        r.setDriver(this);
    }

    // Reservar (Solo si es TRAVELER o BOTH)
    public void addBooking(Booking b) {
        if (this.type == UserType.DRIVER) {
            throw new IllegalStateException("Este usuario no tiene permiso para reservar");
        }
        this.bookings.add(b);
        b.setTravelerName(this.name);
    }

    // --- GETTERS Y SETTERS ---
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public UserType getType() { return type; }
    public void setType(UserType type) { this.type = type; }
    
    public List<Ride> getCreatedRides() { return createdRides; }
    public void setCreatedRides(List<Ride> createdRides) { this.createdRides = createdRides; }
    
    public List<Booking> getBookings() { return bookings; }
    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }
    
    // Métodos helper para la vista (JSF/Web)
    public boolean isDriver() {
        return type == UserType.DRIVER || type == UserType.BOTH;
    }
    
    public boolean isTraveler() {
        return type == UserType.TRAVELER || type == UserType.BOTH;
    }
    
    public boolean doesRideExists(String from, String to, Date date) {
        for (Ride r : createdRides) {
            // Comparamos origen, destino y fecha.
            // Asegúrate de que tu clase Ride tenga los getters getFromCity(), getToCity() y getDate()
            if (r.getFrom().equals(from) && r.getTo().equals(to) && r.getDate().equals(date)) {
                return true;
            }
        }
        return false;
    }


}