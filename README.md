# LoginHibernate - Ride-Sharing App (Hibernate Version)

![Java](https://img.shields.io/badge/Java-11-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![JSF](https://img.shields.io/badge/JSF-Jakarta_Faces-007396?style=for-the-badge&logo=java&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-ORM-59666C?style=for-the-badge&logo=hibernate&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build_Tool-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

This repository contains a ride-sharing web application developed as an academic project. The primary focus of this version is the implementation of a robust data persistence layer using **Hibernate ORM** and JPA, replacing previous object-oriented database approaches.

## Core features

* **Role-based User Management:** Authentication and registration system distinguishing between Drivers, Travelers, or Both.
* **Ride Operations:** Complete lifecycle for publishing rides (origin, destination, date, seats, price) and querying available routes.
* **Booking System:** Functionality for travelers to book seats on existing rides and manage their reservations.
* **Dynamic UI:** Frontend built with JSF (Jakarta Faces 2.3), utilizing PrimeFaces and OmniFaces components for a responsive experience.

## Architecture and Technologies

The application is structured in a strict four-layer architecture:

1. **Web Tier:** JSF pages and CDI-managed beans (Weld) handling user interactions and navigation flow.
2. **Business Logic (`BLFacade`):** A facade pattern interface that isolates the web tier from data operations, handling custom business exceptions (e.g., `RideMustBeLaterThanTodayException`).
3. **Data Access (`HibernateDataAccess`):** The core of this project. It manages the `EntityManagerFactory` lifecycle and executes all CRUD operations using JPA.
4. **Persistence:** An embedded **H2** file-based database for localized data storage.

The project is managed with Maven (`pom.xml`) and configured for deployment as a `.war` file via Eclipse WTP.

## Domain Model

The relational database schema is mapped to Java entities using JPA annotations. The core entities are:
* `User`: Stores credentials and role (`UserType`), linked to their rides and bookings.
* `Ride`: Represents a journey created by a driver, containing a One-to-Many relationship with bookings.
* `Booking`: Links a traveler (User) to a specific Ride.

## What I learned

This project was fundamental in understanding object-relational mapping (ORM) in enterprise Java applications. I learned how to configure the persistence unit, manage the `EntityManager` and `EntityTransaction` lifecycles, and handle bidirectional entity relationships and cascading operations safely using Hibernate.
