package mybeans;

import businessLogic.BLFacade;
import domain.User;
import domain.UserType; // <--- IMPORTANTE: No olvides importar el Enum
import exceptions.DriverAlreadyExistsException;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.bean.ManagedBean; // O @Named si usas CDI moderno
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;

@ManagedBean(name = "registerBean")
@RequestScoped
public class RegisterBean {

    // Datos del formulario
    private String username;
    private String password;
    private String email;
    
    // Nuevo campo para el desplegable (Por defecto DRIVER)
    private String userType = "DRIVER"; 

    @Inject
    private BLFacade bl;

    public RegisterBean() {
        System.out.println(">>> RegisterBean sortu da");
        // Fallback por si la inyección falla (común en configuraciones antiguas)
        if (bl == null) {
            bl = FacadeBean.getBusinessLogic();
        }
    }

    // ===========================
    //    GETTERS Y SETTERS
    // ===========================
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // Getter y Setter para el Tipo de Usuario
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    // ===========================
    //    MÉTODO PRINCIPAL
    // ===========================
    
    public String register() {
        System.out.println("=== ERREGISTROA HASITA ===");
        System.out.println("User: " + username + " | Email: " + email + " | Type: " + userType);

        // 1. Validaciones básicas
        if (isEmpty(username) || isEmpty(password) || isEmpty(email)) {
            addError("Eremu guztiak derrigorrezkoak dira (Todos los campos son obligatorios).");
            return null; // Se queda en la misma página
        }

        if (!isValidEmail(email)) {
            addError("Email formatu baliogabea (Formato de email incorrecto).");
            return null;
        }

        if (password.length() < 5) {
            addError("Pasahitzak gutxienez 5 karaktere izan behar ditu.");
            return null;
        }

        try {
            // 2. Convertir el String del formulario al Enum de Java
            // Si userType es "DRIVER" -> UserType.DRIVER
            UserType typeEnum = UserType.valueOf(userType);

            // 3. Crear el objeto User con el constructor de 4 argumentos
            User newUser = new User(email, username, password, typeEnum);
            
            // 4. Llamar a la Lógica de Negocio
            // Nota: Aunque el método se llame 'registerDriver', ahora acepta un objeto User
            bl.registerDriver(newUser); 
            
            System.out.println(">>> Erabiltzailea ondo sortu da datu-basean.");

        } catch (DriverAlreadyExistsException e) {
            System.out.println(">>> Excepción: Ya existe el usuario.");
            addError("Email hau jada erregistratuta dago (El email ya existe).");
            return null;

        } catch (Exception e) {
            System.out.println(">>> Error grave en registro: " + e.getMessage());
            e.printStackTrace();
            addError("Errorea sistema barruan. Saiatu berriro.");
            return null;
        }

        // 5. Éxito: Limpiar campos y redirigir
        addSuccess("Erabiltzailea ongi sortu da! Orain saioa hasi dezakezu.");
        
        username = null;
        password = null;
        email = null;
        userType = "DRIVER"; // Resetear al valor por defecto
        
        // Redirigir al Login
        return "Login.xhtml?faces-redirect=true";
    }

    // ===========================
    //    MÉTODOS AUXILIARES
    // ===========================

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean isValidEmail(String email) {
        // Regex simple para email
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private void addError(String text) {
        FacesContext.getCurrentInstance()
                    .addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, text, null));
    }
    
    private void addSuccess(String text) {
        FacesContext.getCurrentInstance()
                    .addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_INFO, text, null));
    }
}