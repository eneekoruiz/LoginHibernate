package businessLogic;

import java.util.Date;


import java.util.List;

import dataAccess.HibernateDataAccess;
import domain.Booking;
import domain.User;
import domain.Ride;
import exceptions.DriverAlreadyExistsException;
import exceptions.RideAlreadyExistException;
import exceptions.RideMustBeLaterThanTodayException;

/**
 * Implements the business logic as a web service.
 */
public class BLFacadeImplementation implements BLFacade {

    private HibernateDataAccess dbManager;

    public BLFacadeImplementation() {
        System.out.println("Creating BLFacadeImplementation instance");
        dbManager = new HibernateDataAccess();
    }

    public BLFacadeImplementation(HibernateDataAccess da) {
        System.out.println("Creating BLFacadeImplementation instance with DataAccess parameter");
        dbManager = da;
    }

    @Override
    public List<String> getDepartCities() {
        return dbManager.getDepartCities();
    }

    @Override
    public List<String> getDestinationCities(String from) {
        return dbManager.getArrivalCities(from);
    }

    @Override
    public Ride createRide(String from, String to, Date date, int nPlaces, float price, String driverEmail)
            throws RideMustBeLaterThanTodayException, RideAlreadyExistException {
        return dbManager.createRide(from, to, date, nPlaces, price, driverEmail);
    }

    @Override
    public List<Ride> getRides(String from, String to, Date date) {
        return dbManager.getRides(from, to, date);
    }

    @Override
    public List<Date> getThisMonthDatesWithRides(String from, String to, Date date) {
        return dbManager.getThisMonthDatesWithRides(from, to, date);
    }

    @Override
    public void initializeBD() {
        dbManager.initializeDB();
    }

public void registerDriver(User driver) throws DriverAlreadyExistsException {
        
        // 1. Validar contraseña (OBLIGATORIO)
        if (driver.getPassword() == null || driver.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        
        // 2. Usar tu método authenticate para verificar si ya existe
        
        // 3. Usar tu método saveDriver para guardar
        dbManager.saveDriver(driver);
        
        System.out.println("Driver registrado: " + driver.getEmail());
    }
    
    // También puedes implementar login usando tu dataAccess
@Override
public User login(String email, String password) {
    System.out.println(">>> BLFacade: Login para " + email);
    
    // Validaciones básicas
    if (email == null || email.trim().isEmpty()) {
        System.out.println(">>> Email vacío");
        return null;
    }
    
    if (password == null || password.trim().isEmpty()) {
        System.out.println(">>> Password vacío");
        return null;
    }
    
    try {
        // Llama al método authenticate de tu HibernateDataAccess
    	User driver = dbManager.authenticate(email, password);
        
        if (driver != null) {
            System.out.println(">>> Login exitoso: " + driver.getEmail());
        } else {
            System.out.println(">>> Login fallido para: " + email);
        }
        
        return driver;
        
    } catch (Exception e) {
        System.err.println(">>> Error en login: " + e.getMessage());
        e.printStackTrace();
        return null;
    }
}
@Override
public boolean bookRide(Ride ride, String travelerName, int seats) {
    // Basic validation
    if (ride == null || seats <= 0 || travelerName == null || travelerName.isEmpty()) {
        return false;
    }
    return dbManager.createBooking(ride, travelerName, seats);
}
@Override
public void close() {
    dbManager.close(); // Llama al método que creaste en el PASO 1
    System.out.println("BL: Base de datos cerrada.");
}

@Override
public List<Booking> getBookingsByTraveler(String name) {
    // dbManager zure DataAccess klasearen instantzia da
    List<Booking> bookings = dbManager.getBookingsByTraveler(name);
    return bookings;
}
@Override
public void cancelBooking(int bookingId) {
    dbManager.cancelBooking(bookingId);
}

@Override
public List<Ride> getRidesByDriver(String driverEmail) {
    List<Ride> rides = dbManager.getRidesByDriver(driverEmail);
    return rides;
}

@Override
public void deleteRide(Ride ride) {
    dbManager.deleteRide(ride);
}
@Override
public List<Ride> getRidesByQuery(String text) {
    List<Ride> rides = dbManager.getRidesByQuery(text);

    return rides;
}

}
