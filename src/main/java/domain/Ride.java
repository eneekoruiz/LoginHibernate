package domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.*;

@Entity
public class Ride implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer rideNumber;

    private String fromCity;
    private String toCity;
    private int nPlaces;
    private float price;

    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @ManyToOne
    @JoinColumn(name = "driver_email")
    private User driver;

 // In domain/Ride.java

 // Add this list
 @OneToMany(mappedBy = "ride", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
 private List<Booking> bookings = new ArrayList<>();

 // Add getter and setter
 public List<Booking> getBookings() { return bookings; }
 public void setBookings(List<Booking> bookings) { this.bookings = bookings; }

 // Add a helper method to add a booking
 public void addBooking(Booking booking) {
     bookings.add(booking);
     booking.setRide(this);
 }
    public Ride() { }

    public Ride(String fromCity, String toCity, Date date, int nPlaces, float price, User driver) {
        this.fromCity = fromCity;
        this.toCity = toCity;
        this.date = date;
        this.nPlaces = nPlaces;
        this.price = price;
        this.driver = driver;
    }

    // Getters y setters
    public Integer getRideNumber() { return rideNumber; }
    public void setRideNumber(Integer rideNumber) { this.rideNumber = rideNumber; }

    public String getFrom() { return fromCity; }
    public void setFrom(String fromCity) { this.fromCity = fromCity; }

    public String getTo() { return toCity; }
    public void setTo(String toCity) { this.toCity = toCity; }

    public int getnPlaces() { return nPlaces; }
    public void setnPlaces(int nPlaces) { this.nPlaces = nPlaces; }

    public float getPrice() { return price; }
    public void setPrice(float price) { this.price = price; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public User getDriver() { return driver; }
    public void setDriver(User driver) { this.driver = driver; }

    @Override
    public String toString() {
        return rideNumber + ";" + fromCity + ";" + toCity + ";" + date;
    }
}
