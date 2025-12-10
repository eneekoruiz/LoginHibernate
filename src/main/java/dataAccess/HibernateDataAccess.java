package dataAccess;

import domain.Booking;
import domain.User;
import domain.UserType; // Importante para definir los roles
import domain.Ride;
import exceptions.DriverAlreadyExistsException;
import exceptions.RideAlreadyExistException;
import exceptions.RideMustBeLaterThanTodayException;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HibernateDataAccess {

    private EntityManagerFactory emf;

    public HibernateDataAccess() {
        // persistence-unit definido en persistence.xml
        emf = Persistence.createEntityManagerFactory("ridesPU");
        
        // Ejecutamos la inicialización automática al arrancar
        initializeDB();
    }

    private EntityManager getEM() {
        return emf.createEntityManager();
    }
    
    // Cierra la fábrica al apagar la app
 // Cierra la fábrica al apagar la app
    public void close() {
        // 1. H2 ITZALTZEKO AGINDUA (SHUTDOWN)
        // Honek hari guztiak (Watchdog, TCP Server) hiltzen ditu
        try {
            EntityManager em = getEM();
            em.getTransaction().begin();
            // Agindu hau SQL natiboa da H2rentzat: "Itzali dena orain"
            em.createNativeQuery("SHUTDOWN").executeUpdate();
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            // Normala da hemen errorea ematea, konexioa bat-batean ixten delako.
            // Ez dugu ezer inprimatu behar, helburua lortu dugu.
        }

        // 2. HIBERNATE ITXI
        if (emf != null && emf.isOpen()) {
            emf.close();
            System.out.println("DA: EntityManagerFactory cerrado correctamente.");
        }
    }

    // --- MÉTODOS DE DRIVER / USER ---

    /**
     * Guarda un usuario nuevo (Driver o Traveler)
     */
    public void saveDriver(User user) throws DriverAlreadyExistsException {
        EntityManager em = getEM();
        EntityTransaction tx = em.getTransaction();
        
        try {
            tx.begin();
            
            // 1. VERIFICAR SI YA EXISTE
            User existingUser = em.find(User.class, user.getEmail());
            
            if (existingUser != null) {
                tx.commit(); 
                em.close();
                throw new DriverAlreadyExistsException("El usuario con email " + user.getEmail() + " ya existe.");
            }
            
            // 2. SI NO EXISTE, GUARDAMOS
            em.persist(user);
            tx.commit();
            System.out.println("DA: Usuario guardado: " + user.getEmail() + " Tipo: " + user.getType());
            
        } catch (DriverAlreadyExistsException e) {
            throw e; 
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    /**
     * Método antiguo mantenido por compatibilidad, redirige al nuevo
     */
    public void saveDriver(String email, String name, String password, UserType type) {
        try {
            saveDriver(new User(email, name, password, type));
        } catch (DriverAlreadyExistsException e) {
            System.out.println("Intento de duplicar usuario: " + email);
        }
    }

    /**
     * Login / Autenticación
     */
    public User authenticate(String email, String password) {
        EntityManager em = getEM();
        try {
            User user = em.find(User.class, email);
            if (user != null && password.equals(user.getPassword())) {
                return user;
            }
            return null;
        } finally {
            em.close();
        }
    }

    // --- MÉTODOS DE RIDE ---

    public Ride createRide(String from, String to, Date date, int nPlaces, float price, String driverEmail)
            throws RideMustBeLaterThanTodayException, RideAlreadyExistException {

        if (date.before(new Date())) throw new RideMustBeLaterThanTodayException("The ride must be in the future");

        EntityManager em = getEM();
        EntityTransaction tx = em.getTransaction();
        
        try {
            tx.begin();
            User driver = em.find(User.class, driverEmail);
            
            // Protección: Si el driver no existe (raro si viene de login)
            if (driver == null) {
                tx.rollback();
                throw new RuntimeException("El conductor no existe en la BD: " + driverEmail);
            }

            // Verificar duplicados usando el método del dominio
            if (driver.doesRideExists(from, to, date)) {
                tx.commit();
                throw new RideAlreadyExistException("Ride already exists for this driver");
            }

            Ride ride = new Ride(from, to, date, nPlaces, price, driver);
            
            // Mantener la coherencia bidireccional
            driver.addCreatedRide(ride); 
            
            em.persist(ride);
            tx.commit();
            return ride;
            
        } catch (RideAlreadyExistException e) {
            if (tx.isActive()) tx.rollback(); // Asegurar rollback si saltó la excepción tras begin
            throw e;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }

    public List<Ride> getRides(String from, String to, Date date) {
        EntityManager em = getEM();
        try {
            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            Date startOfDay = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endOfDay = Date.from(localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            return em.createQuery(
                    "SELECT r FROM Ride r WHERE r.fromCity = :from AND r.toCity = :to " +
                            "AND r.date >= :startDate AND r.date < :endDate", Ride.class)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .setParameter("startDate", startOfDay)
                    .setParameter("endDate", endOfDay)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<String> getDepartCities() {
        EntityManager em = getEM();
        try {
            return em.createQuery("SELECT DISTINCT r.fromCity FROM Ride r", String.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<String> getArrivalCities(String from) {
        EntityManager em = getEM();
        try {
            return em.createQuery("SELECT DISTINCT r.toCity FROM Ride r WHERE r.fromCity = :from", String.class)
                    .setParameter("from", from)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Date> getThisMonthDatesWithRides(String from, String to, Date monthDate) {
        EntityManager em = getEM();
        try {
            LocalDate localDate = monthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            int year = localDate.getYear();
            int month = localDate.getMonthValue();

            return em.createQuery(
                    "SELECT DISTINCT r.date FROM Ride r " +
                            "WHERE r.fromCity = :from AND r.toCity = :to " +
                            "AND FUNCTION('YEAR', r.date) = :year " +
                            "AND FUNCTION('MONTH', r.date) = :month", Date.class)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .setParameter("year", year)
                    .setParameter("month", month)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // --- MÉTODOS DE BOOKING ---

    public boolean createBooking(Ride ride, String travelerName, int seats) {
        EntityManager em = getEM();
        EntityTransaction tx = em.getTransaction();
        boolean success = false;

        try {
            tx.begin();
            Ride dbRide = em.find(Ride.class, ride.getRideNumber());

            if (dbRide.getnPlaces() >= seats) {
                Booking booking = new Booking(dbRide, travelerName, seats);
                dbRide.addBooking(booking);
                dbRide.setnPlaces(dbRide.getnPlaces() - seats);

                em.persist(booking);
                tx.commit();
                success = true;
            } else {
                tx.rollback();
            }
        } catch (Exception e) {
            if(tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
        return success;
    }

    public List<Booking> getBookingsByTraveler(String username) {
        EntityManager em = getEM(); 
        try {
            // Usamos JOIN FETCH para traer el Ride asociado en la misma consulta
            TypedQuery<Booking> query = em.createQuery(
                "SELECT b FROM Booking b JOIN FETCH b.ride WHERE b.travelerName = :name", 
                Booking.class
            );
            query.setParameter("name", username);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            em.close(); 
        }
    }
    
    public void cancelBooking(int bookingId) {
        EntityManager em = getEM();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Booking booking = em.find(Booking.class, bookingId);
            if (booking != null) {
                Ride ride = booking.getRide();
                ride.setnPlaces(ride.getnPlaces() + booking.getSeats());
                ride.getBookings().remove(booking);
                em.remove(booking);
                tx.commit();
                System.out.println("DA: Reserva cancelada ID: " + bookingId);
            }
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    // --- INICIALIZACIÓN INTELIGENTE ---

    public void initializeDB() {
        EntityManager em = getEM();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            // 1. COMPROBACIÓN DE SEGURIDAD
            // Si ya hay usuarios, asumimos que la DB está inicializada y NO HACEMOS NADA.
            Long count = em.createQuery("SELECT count(u) FROM User u", Long.class).getSingleResult();
            
            if (count > 0) {
                System.out.println("DEBUG: La base de datos ya contiene datos (" + count + " usuarios). Inicialización saltada.");
                tx.commit();
                return;
            }

            System.out.println("DEBUG: Base de datos vacía. Creando datos de prueba para Diciembre/Enero...");

            // 2. CREACIÓN DE USUARIOS CON ROLES (UserType)
            
            // Driver Puro
            User driver1 = new User("driver1@gmail.com", "Aitor Conductor", "123", UserType.DRIVER);
            // Usuario con ambos roles (puede conducir y reservar)
            User driver2 = new User("driver2@gmail.com", "Maria Chofer", "123", UserType.BOTH);
            // Viajero Puro
            User traveler1 = new User("traveler@gmail.com", "Jon Viajero", "123", UserType.TRAVELER);

            em.persist(driver1);
            em.persist(driver2);
            em.persist(traveler1);

            // 3. GENERACIÓN DE FECHAS (Navidad e Invierno)
            Calendar cal = Calendar.getInstance();
            
            // Fecha 1: 24 de Diciembre de 2025 (Nochebuena)
            cal.set(2025, Calendar.DECEMBER, 24, 8, 0, 0); // 08:00 AM
            Date dateDec24 = cal.getTime();
            
            // Fecha 2: 31 de Diciembre de 2025 (Nochevieja)
            cal.set(2025, Calendar.DECEMBER, 31, 15, 30, 0); // 15:30 PM
            Date dateDec31 = cal.getTime();
            
            // Fecha 3: 5 de Enero de 2026 (Reyes)
            cal.set(2026, Calendar.JANUARY, 5, 10, 0, 0); // 10:00 AM
            Date dateJan05 = cal.getTime();

            // 4. CREACIÓN DE VIAJES (Rides)
            
            // Viaje 1: Donostia -> Madrid (Nochebuena)
            Ride r1 = new Ride("Donostia", "Madrid", dateDec24, 4, 45.0f, driver1);
            driver1.addCreatedRide(r1);
            em.persist(r1);
            
            // Viaje 2: Madrid -> Donostia (Vuelta Reyes)
            Ride r2 = new Ride("Madrid", "Donostia", dateJan05, 4, 45.0f, driver1);
            driver1.addCreatedRide(r2);
            em.persist(r2);

            // Viaje 3: Valencia -> Barcelona (Nochevieja) - Creado por driver2 (BOTH)
            Ride r3 = new Ride("Valencia", "Barcelona", dateDec31, 3, 25.50f, driver2);
            driver2.addCreatedRide(r3);
            em.persist(r3);

            tx.commit();
            System.out.println("DEBUG: Datos de prueba inicializados correctamente.");
            
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
    
 // En dataAccess/HibernateDataAccess.java

 // 1. Obtener viajes creados por un conductor
 public List<Ride> getRidesByDriver(String driverEmail) {
     EntityManager em = getEM();
     try {
         // Usamos una query directa para evitar problemas de Lazy Loading
         return em.createQuery("SELECT r FROM Ride r WHERE r.driver.email = :email", Ride.class)
                  .setParameter("email", driverEmail)
                  .getResultList();
     } finally {
         em.close();
     }
 }

 // 2. Borrar un viaje (Cascade delete se encarga de las reservas si está configurado, si no, lo forzamos)
 public void deleteRide(Ride ride) {
     EntityManager em = getEM();
     EntityTransaction tx = em.getTransaction();
     try {
         tx.begin();
         // Adjuntamos el objeto a la sesión actual
         Ride r = em.find(Ride.class, ride.getRideNumber());
         
         // Eliminamos el viaje (Hibernate borrará las reservas asociadas si el CascadeType.ALL está bien puesto en Ride.java)
         em.remove(r);
         
         tx.commit();
         System.out.println("DA: Ride borrado id=" + ride.getRideNumber());
     } catch (Exception e) {
         if (tx.isActive()) tx.rollback();
         e.printStackTrace();
     } finally {
         em.close();
     }
     
     
 }
 
//En dataAccess/HibernateDataAccess.java

public List<Ride> getRidesByQuery(String text) {
  EntityManager em = getEM();
  try {
      // Usamos LIKE y UPPER para que no importen mayúsculas/minúsculas
      // Busca si el texto está en el origen O en el destino
      return em.createQuery(
          "SELECT r FROM Ride r WHERE UPPER(r.fromCity) LIKE UPPER(:text) OR UPPER(r.toCity) LIKE UPPER(:text)", 
          Ride.class)
          .setParameter("text", "%" + text + "%") // % es el comodín
          .getResultList();
  } finally {
      em.close();
  }
}
}