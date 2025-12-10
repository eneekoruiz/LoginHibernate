package mybeans;

import java.io.Serializable;
import domain.User;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
// Añade estos imports para que funcione el logout
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpSession;

@Named("sessionManager")
@SessionScoped
public class SessionManagerBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // Guardamos el User genérico (puede ser Driver, Traveler o Ambos)
    private User currentUser; 

    // --- GESTIÓN DE LOGIN ---

    // Este método lo llamas desde tu LoginBean cuando el login es correcto
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    // Método de utilidad para saber si hay alguien logueado
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    // Método para obtener email (útil para lógica de negocio)
    public String getLoggedInUserEmail() {
        return (currentUser != null) ? currentUser.getEmail() : null;
    }
    
    // --- GESTIÓN DE LOGOUT (UNIFICADO) ---
    
    public String logout() {
        // 1. Borrar la referencia local
        this.currentUser = null;

        // 2. Obtener la sesión HTTP y destruirla (Invalida toda la memoria de sesión)
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        
        if (session != null) {
            session.invalidate(); // Esto es lo importante: borra todo rastro del servidor
        }
        
        System.out.println(">>> SessionManager: Sesión cerrada y destruida.");

        // 3. Redirigir al Login limpiando la URL
        return "Login.xhtml?faces-redirect=true";
    }
    
    
}