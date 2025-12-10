-- Datos iniciales para Hibernate
INSERT INTO driver (email, name) VALUES ('driver1@gmail.com', 'Alice');
INSERT INTO driver (email, name) VALUES ('driver2@gmail.com', 'Bob');
INSERT INTO driver (email, name) VALUES ('driver3@gmail.com', 'Charlie');

-- Rides de ejemplo
INSERT INTO Ride (date, driver_email, fromCity, nPlaces, price, toCity) 
VALUES (CURRENT_DATE + 1, 'driver1@gmail.com', 'Madrid', 3, 25, 'Barcelona');

INSERT INTO Ride (date, driver_email, fromCity, nPlaces, price, toCity) 
VALUES (CURRENT_DATE + 1, 'driver2@gmail.com', 'Madrid', 2, 30, 'Valencia');

INSERT INTO Ride (date, driver_email, fromCity, nPlaces, price, toCity) 
VALUES (CURRENT_DATE + 2, 'driver3@gmail.com', 'Barcelona', 4, 20, 'Madrid');