package exceptions;

public class DriverAlreadyExistsException extends Exception {
    
    private String email;
    
    // Constructor básico
    public DriverAlreadyExistsException() {
        super("El conductor ya existe en el sistema");
    }
    
    // Constructor con mensaje personalizado
    public DriverAlreadyExistsException(String message) {
        super(message);
    }
    
    // Constructor con email específico
    public DriverAlreadyExistsException(String email, String name) {
        super("El conductor con email '" + email + "' ya está registrado");
        this.email = email;
    }
    
    // Constructor con causa
    public DriverAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
    
    // Getters
    public String getEmail() {
        return email;
    }
    
    // Método para loguear el error
    public void logError() {
        System.err.println("DriverAlreadyExistsException: " + getMessage());
        if (getCause() != null) {
            System.err.println("Causa: " + getCause().getMessage());
        }
    }
}