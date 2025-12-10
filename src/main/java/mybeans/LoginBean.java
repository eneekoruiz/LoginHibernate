package mybeans;

import java.io.Serializable;


import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject; // IMPORTANTE: Para inyectar el SessionManager
import jakarta.inject.Named;

import businessLogic.BLFacade;
import domain.User;

@Named("login")
@RequestScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // 1. INYECCIÓN DEL GESTOR DE SESIÓN
    // Esto nos permite guardar el driver logueado para usarlo en otros beans (como CreateRidesBean)
    @Inject
    private SessionManagerBean sessionManager;

    private String email;
    private String password;
    private BLFacade facadeBL;

    public LoginBean() {
        // Obtenemos la lógica de negocio
        facadeBL = FacadeBean.getBusinessLogic();
    }

    // --- Getters y Setters ---
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // --- Método de Autenticación Modificado ---
    public String egiaztatu() {
        System.out.println("Intentando login para: " + email);

        // Llamamos al Facade. Debe devolver un objeto Driver si es correcto, o null si falla.
        User driver = facadeBL.login(email, password);

        if (driver == null) {
            // LOGIN FALLIDO
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Errorea", "Pasahitza edo emaila okerra da"));
            return null; // Se queda en la misma página mostrando el error
        } else {
            // LOGIN EXITOSO
            
            // 2. GUARDAMOS EL DRIVER EN LA SESIÓN
            sessionManager.setCurrentUser(driver);
            System.out.println("Driver guardado en sesión: " + driver.getEmail());

            // 3. REDIRECCIÓN
            // "Hasiera" te lleva al menú principal. 
            // Si prefieres ir directo a crear viaje, pon "CreateRide?faces-redirect=true"
            return "Home?faces-redirect=true";
        }
    }
    
}