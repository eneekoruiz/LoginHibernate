package domain;

import java.io.Serializable;
import jakarta.persistence.*;

@Entity
public class Booking implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    private String travelerName; // Or travelerEmail
    private int seats;
    @ManyToOne
    private User traveler;
    
    // Relationship: A booking belongs to one Ride
    @ManyToOne
    private Ride ride;

    public Booking() {}

    public Booking(Ride ride, String travelerName, int seats) {
        this.ride = ride;
        this.travelerName = travelerName;
        this.seats = seats;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTravelerName() { return travelerName; }
    public void setTravelerName(String travelerName) { this.travelerName = travelerName; }
    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }
    public Ride getRide() { return ride; }
    public void setRide(Ride ride) { this.ride = ride; }
}