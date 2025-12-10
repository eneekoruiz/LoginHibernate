package mybeans;

import java.io.Serializable;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import businessLogic.BLFacade;
import domain.Ride;
import domain.User;

@Named
@ViewScoped
public class MyRidesBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private SessionManagerBean sessionManager;

    private List<Ride> myRides;

    @PostConstruct
    public void init() {
        // Cargar los viajes del conductor logueado
        User driver = sessionManager.getCurrentUser();
        if (driver != null && (driver.isDriver())) { // Asumiendo que tienes isDriver() en User
            BLFacade facade = FacadeBean.getBusinessLogic();
            myRides = facade.getRidesByDriver(driver.getEmail());
        }
    }

    public void deleteRide(Ride ride) {
        try {
            BLFacade facade = FacadeBean.getBusinessLogic();
            facade.deleteRide(ride);
            
            // Actualizar la lista visualmente
            myRides.remove(ride);
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Ezabatuta", "Bidaia ezabatu da."));
                
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Errorea", "Ezin izan da bidaia ezabatu."));
        }
    }

    public List<Ride> getMyRides() { return myRides; }
}