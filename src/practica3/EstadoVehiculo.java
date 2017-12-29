package practica3;

import es.upv.dsic.gti_ia.core.AgentID;

/**
 * @author Jose Luis Martínez Ortiz
 * Clase para llevar el estado de cada vehículo en supermente.
 */
public class EstadoVehiculo {

    public AgentID id;
    public int coor_x,coor_y;
    public boolean objetivoAlcanzado;
    public int battery;
    public TipoVehiculo tipoVehiculo;
    public int consumo;

    public EstadoVehiculo(AgentID id) {
        this.id = id;
    }

    public EstadoVehiculo(AgentID id, int battery, TipoVehiculo tipoVehiculo) {
        this.id = id;
        this.battery = battery;
        this.tipoVehiculo = tipoVehiculo;

        switch (tipoVehiculo){
            case DRON: consumo = 1;break;
            case COCHE: consumo = 2;break;
            case CAMION: consumo = 4;break;
        }
    }

    public EstadoVehiculo(AgentID id, int coor_x, int coor_y, boolean objetivoAlcanzado, int battery, TipoVehiculo tipoVehiculo) {
        this.id = id;
        this.coor_x = coor_x;
        this.coor_y = coor_y;
        this.objetivoAlcanzado = objetivoAlcanzado;
        this.battery = battery;
        this.tipoVehiculo = tipoVehiculo;
        switch (tipoVehiculo){
            case DRON: consumo = 1;break;
            case COCHE: consumo = 2;break;
            case CAMION: consumo = 4;break;
        }
    }
}
